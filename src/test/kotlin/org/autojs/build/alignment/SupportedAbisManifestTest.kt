package org.autojs.build.alignment

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.w3c.dom.Element
import java.io.File
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class SupportedAbisManifestTest {
    @TempDir lateinit var directory: Path

    private val manifest = """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="org.autojs.test">
            <application android:label="Fixture">
                <meta-data android:name="org.autojs.plugin.contract.NATIVE_PAGE_ALIGNMENT" android:value="16384" />
                <service android:name=".InfoService" android:exported="true" />
            </application>
        </manifest>
    """.trimIndent()

    private fun mergedNativeLibs(vararg libraries: String, flat: Boolean = false): File {
        val root = directory.resolve("merged_native_libs").toFile()
        libraries.forEach { path ->
            root.resolve(if (flat) path.removePrefix("lib/") else path).apply { parentFile.mkdirs(); writeBytes(ByteArray(8)) }
        }
        return root
    }

    private fun metaData(xml: String, name: String): List<String> {
        val document = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(xml.byteInputStream())
        val elements = document.getElementsByTagName("meta-data")
        return (0 until elements.length).map { elements.item(it) as Element }
            .filter { it.getAttributeNS(SupportedAbisManifest.ANDROID_NAMESPACE, "name") == name }
            .map { it.getAttributeNS(SupportedAbisManifest.ANDROID_NAMESPACE, "value") }
    }

    @Test fun `packaged ABIs come from directories holding at least one library`() {
        val root = mergedNativeLibs("lib/arm64-v8a/libnode.so", "lib/x86_64/nested/libnode.so", "lib/armeabi-v7a/readme.txt")
        root.resolve("lib/x86").mkdirs()
        assertEquals(setOf("arm64-v8a", "x86_64"), SupportedAbisManifest.packagedNativeAbis(root))
        assertEquals(emptySet<String>(), SupportedAbisManifest.packagedNativeAbis(null))
        assertEquals(emptySet<String>(), SupportedAbisManifest.packagedNativeAbis(directory.resolve("missing").toFile()))
        val flat = directory.resolve("flat").toFile().also { it.resolve("armeabi-v7a").mkdirs(); it.resolve("armeabi-v7a/libx.so").writeBytes(ByteArray(1)) }
        assertEquals(setOf("armeabi-v7a"), SupportedAbisManifest.packagedNativeAbis(flat))
    }

    @Test fun `splits and ndk filters narrow the packaged set and an override replaces it`() {
        val packaged = setOf("x86_64", "armeabi-v7a", "arm64-v8a", "riscv64")
        assertEquals(listOf("arm64-v8a", "armeabi-v7a", "x86_64", "riscv64"), SupportedAbisManifest.compute(packaged))
        assertEquals(listOf("arm64-v8a", "x86_64"), SupportedAbisManifest.compute(packaged, splitAbis = setOf("arm64-v8a", "x86_64", "x86")))
        assertEquals(listOf("arm64-v8a"), SupportedAbisManifest.compute(packaged, splitAbis = setOf("arm64-v8a", "x86_64"), abiFilters = setOf("arm64-v8a", "armeabi-v7a")))
        assertEquals(listOf("x86_64", "x86"), SupportedAbisManifest.compute(packaged, override = setOf("x86", " x86_64 ")))
        assertEquals(emptyList<String>(), SupportedAbisManifest.compute(emptySet(), splitAbis = setOf("arm64-v8a")))
        assertEquals("universal", SupportedAbisManifest.value(emptyList()))
        assertEquals("arm64-v8a,x86_64", SupportedAbisManifest.value(listOf("arm64-v8a", "x86_64")))
    }

    @Test fun `meta-data is added once and replaced in place`() {
        val name = SupportedAbisManifest.META_DATA_NAME
        val added = SupportedAbisManifest.inject(manifest, name, "arm64-v8a,x86_64")
        assertEquals(listOf("arm64-v8a,x86_64"), metaData(added, name))
        assertEquals(listOf("16384"), metaData(added, "org.autojs.plugin.contract.NATIVE_PAGE_ALIGNMENT"))
        assertTrue(added.contains("<service"))
        val replaced = SupportedAbisManifest.inject(added, name, "universal")
        assertEquals(listOf("universal"), metaData(replaced, name))
        assertThrows<IllegalArgumentException> { SupportedAbisManifest.inject("<manifest/>", name, "x") }
    }

    @Test fun `task rewrites the merged manifest from merged native libraries`() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val input = directory.resolve("in/AndroidManifest.xml").toFile().apply { parentFile.mkdirs(); writeText(manifest) }
        val output = directory.resolve("out/AndroidManifest.xml").toFile()
        val task = project.tasks.register("writeSupportedAbis", WriteSupportedAbisMetaData::class.java).get()
        task.inputManifest.set(input)
        task.outputManifest.set(output)
        task.mergedNativeLibs.set(mergedNativeLibs("lib/arm64-v8a/libnode.so", "lib/armeabi-v7a/libnode.so", "lib/x86_64/libnode.so"))
        task.splitAbis.set(setOf("arm64-v8a", "x86_64"))
        task.abiFilters.set(emptySet())
        task.overrideAbis.set(emptySet())
        task.metaDataName.set(SupportedAbisManifest.META_DATA_NAME)
        task.write()
        assertEquals(listOf("arm64-v8a,x86_64"), metaData(output.readText(), SupportedAbisManifest.META_DATA_NAME))

        task.mergedNativeLibs.set(directory.resolve("none").toFile())
        task.write()
        assertEquals(listOf("universal"), metaData(output.readText(), SupportedAbisManifest.META_DATA_NAME))
    }

    @Test fun `plugin conventions enable the meta-data with the contract name`() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        project.pluginManager.apply(NativeAlignmentPlugin::class.java)
        val extension = project.extensions.getByType(NativeAlignmentExtension::class.java)
        assertTrue(extension.supportedAbisMetaData.get())
        assertEquals(emptySet<String>(), extension.supportedAbis.get())
        assertEquals("org.autojs.plugin.contract.SUPPORTED_ABIS", extension.supportedAbisMetaDataName.get())
    }
}
