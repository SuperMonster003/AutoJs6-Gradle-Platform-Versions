package org.autojs.build.alignment

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Supported-ABI meta-data for AutoJs6 plugins.
 *
 * Every official plugin used to hand-write its supported ABI list (a string resource, a
 * manifest meta-data or a constant behind the INFO service), and the list went stale whenever
 * a plugin later gained an ABI. The build knows the truth: the merged native libraries of the
 * variant, narrowed by `splits.abi` and `ndk.abiFilters`. This writes that set into the merged
 * manifest as `org.autojs.plugin.contract.SUPPORTED_ABIS` so the AutoJs6 host reads it from
 * the installed package and the official plugin index can cross-check it; a module without
 * native code publishes [UNIVERSAL], which the host shows as "all".
 *
 * zh-CN: 自动维护的 "支持 ABI" meta-data. 真值来自变体合并后的原生库目录, 再按 `splits.abi` 与
 * `ndk.abiFilters` 收窄, 写入合并清单的 `org.autojs.plugin.contract.SUPPORTED_ABIS`;
 * 无原生代码的模块写入 [UNIVERSAL], 宿主显示为 "全部".
 */
object SupportedAbisManifest {

    /** Meta-data name shared with the AutoJs6 host and the official plugin index tool. */
    const val META_DATA_NAME = "org.autojs.plugin.contract.SUPPORTED_ABIS"

    /** Value for a package without native code; an empty value would read as "unknown" on the host. */
    const val UNIVERSAL = "universal"

    const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

    /** Canonical order; unknown ABIs follow alphabetically. */
    val ABI_ORDER = listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")

    /** ABIs that own at least one `.so` under `lib/<abi>/` (or `<abi>/`) in a merged native libs directory. */
    fun packagedNativeAbis(mergedNativeLibs: File?): Set<String> {
        val root = mergedNativeLibs?.takeIf { it.isDirectory } ?: return emptySet()
        val candidates = listOfNotNull(root.resolve("lib").takeIf { it.isDirectory }, root)
        val found = linkedSetOf<String>()
        for (base in candidates) {
            base.listFiles()?.filter { it.isDirectory }?.forEach { abiDirectory ->
                val hasLibrary = abiDirectory.walkTopDown().any { it.isFile && it.name.endsWith(".so", ignoreCase = true) }
                if (hasLibrary && abiDirectory.name != "lib") found += abiDirectory.name
            }
        }
        return found
    }

    /**
     * Packaged ABIs narrowed by the enabled ABI splits and the ndk filters; an explicit override
     * wins when the build cannot describe the plugin's ABI support (for example natives loaded
     * from assets).
     */
    fun compute(
        packaged: Set<String>,
        splitAbis: Set<String> = emptySet(),
        abiFilters: Set<String> = emptySet(),
        override: Set<String> = emptySet(),
    ): List<String> {
        if (override.isNotEmpty()) return sorted(override)
        var result: Set<String> = packaged
        if (splitAbis.isNotEmpty()) result = result.intersect(splitAbis)
        if (abiFilters.isNotEmpty()) result = result.intersect(abiFilters)
        return sorted(result)
    }

    fun sorted(abis: Collection<String>): List<String> =
        abis.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            .sortedWith(compareBy({ ABI_ORDER.indexOf(it).let { index -> if (index < 0) ABI_ORDER.size else index } }, { it }))

    /** The manifest value for a computed list. */
    fun value(abis: List<String>): String = abis.joinToString(",").ifEmpty { UNIVERSAL }

    /** Inserts or replaces the meta-data element inside `<application>`; other content is untouched. */
    fun inject(manifestXml: String, name: String, value: String): String {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(manifestXml.byteInputStream())
        val application = document.getElementsByTagName("application").item(0) as? Element
            ?: throw IllegalArgumentException("The merged manifest has no <application> element.")
        val existing = (0 until application.childNodes.length)
            .mapNotNull { application.childNodes.item(it) as? Element }
            .firstOrNull { it.tagName == "meta-data" && it.getAttributeNS(ANDROID_NAMESPACE, "name") == name }
        val metaData = existing ?: document.createElement("meta-data").also { element ->
            element.setAttributeNS(ANDROID_NAMESPACE, "android:name", name)
            application.appendChild(document.createTextNode("\n        "))
            application.appendChild(element)
            application.appendChild(document.createTextNode("\n    "))
        }
        metaData.setAttributeNS(ANDROID_NAMESPACE, "android:value", value)
        val writer = StringWriter()
        TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "no")
            setOutputProperty(OutputKeys.ENCODING, "utf-8")
        }.transform(DOMSource(document), StreamResult(writer))
        return writer.toString()
    }
}

/** Rewrites one variant's merged manifest with the computed supported-ABI meta-data. */
abstract class WriteSupportedAbisMetaData : DefaultTask() {
    @get:InputFile abstract val inputManifest: RegularFileProperty
    @get:InputFiles @get:Optional @get:PathSensitive(PathSensitivity.RELATIVE) abstract val mergedNativeLibs: DirectoryProperty
    @get:Input abstract val splitAbis: SetProperty<String>
    @get:Input abstract val abiFilters: SetProperty<String>
    @get:Input abstract val overrideAbis: SetProperty<String>
    @get:Input abstract val metaDataName: Property<String>
    @get:OutputFile abstract val outputManifest: RegularFileProperty

    @TaskAction fun write() {
        val packaged = SupportedAbisManifest.packagedNativeAbis(mergedNativeLibs.orNull?.asFile)
        val abis = SupportedAbisManifest.compute(packaged, splitAbis.get(), abiFilters.get(), overrideAbis.get())
        val value = SupportedAbisManifest.value(abis)
        val rewritten = SupportedAbisManifest.inject(inputManifest.get().asFile.readText(), metaDataName.get(), value)
        outputManifest.get().asFile.apply { parentFile.mkdirs() }.writeText(rewritten)
        logger.info("Supported ABIs meta-data: ${metaDataName.get()}=\"$value\" (packaged=$packaged, splits=${splitAbis.get()}, filters=${abiFilters.get()})")
    }
}
