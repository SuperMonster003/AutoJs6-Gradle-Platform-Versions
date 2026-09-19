package org.autojs.build.alignment

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

abstract class NativeAlignmentExtension {
    abstract val strictAbis: SetProperty<String>
    abstract val pageSize: Property<Long>
    abstract val expectNoNativeLibraries: Property<Boolean>
    abstract val checkRelro: Property<Boolean>
    abstract val scanEmbeddedPayloads: Property<Boolean>
    abstract val apkFiles: ConfigurableFileCollection
    abstract val extraFiles: ConfigurableFileCollection
    fun apkFiles(vararg files: Any) { apkFiles.from(*files) }
    fun alsoScan(vararg files: Any) { extraFiles.from(*files) }
}

abstract class VerifyNativePageAlignment : DefaultTask() {
    @get:Input abstract val strictAbis: SetProperty<String>
    @get:Input abstract val pageSize: Property<Long>
    @get:Input abstract val expectNoNativeLibraries: Property<Boolean>
    @get:Input abstract val checkRelro: Property<Boolean>
    @get:Input abstract val scanEmbeddedPayloads: Property<Boolean>
    @get:Input abstract val variant: Property<String>
    @get:Input abstract val skipVerification: Property<Boolean>
    @get:Input abstract val ci: Property<Boolean>
    @get:Internal abstract val apkDirectory: DirectoryProperty
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val apkFiles: ConfigurableFileCollection
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val extraFiles: ConfigurableFileCollection
    @get:OutputFile abstract val reportFile: RegularFileProperty

    init {
        // A verifier must overwrite a previous PASS report after failures or changing variant selection.
        outputs.upToDateWhen { false }
    }

    @TaskAction fun verify() {
        val reports = mutableListOf<Map<String, Any?>>()
        val errors = mutableListOf<String>()
        var skipped = false
        try {
            if (skipVerification.get()) {
                check(!ci.get()) { "autojs.nativeAlignment.skip is forbidden in CI" }
                logger.warn("Native alignment verification explicitly skipped (local build only)")
                skipped = true
            } else {
                val scanner = NativeAlignmentScanner(pageSize.get(), strictAbis.get(), expectNoNativeLibraries.get(), checkRelro.get())
                val apks = if (apkFiles.isEmpty) discoverApks() else apkFiles.files.sorted()
                check(apks.isNotEmpty()) { "No APK found for '${variant.get()}'; assemble it first or set nativeAlignment.apkFiles(...)" }
                fun scan(file: File, apk: Boolean) {
                    try {
                        check(file.exists()) { "Missing input: $file" }
                        val entries = if (apk) scanner.scanApk(file) +
                            (if (scanEmbeddedPayloads.get()) scanner.scanPayload(file).filter { !it.name.substringAfter("!/").startsWith("lib/") } else emptyList())
                            else scanner.scanPayload(file)
                        val manifest = if (apk) ApkManifestAlignment.read(file) else null
                        reports += linkedMapOf("file" to file.absolutePath, "entries" to entries.map { it.report() },
                            "extractNativeLibs" to manifest?.extractNativeLibs, "declaredNativePageAlignment" to manifest?.declaredAlignment)
                        entries.filter { it.reasons.isNotEmpty() }.forEach { errors += "${file.name}!/${it.name} [${it.abi}]: ${it.reasons.joinToString("; ")}" }
                        if (manifest?.pageSizeCompatDeclared == true) errors += "$file: android:pageSizeCompat masks compatibility warnings; remove it"
                        manifest?.declaredAlignment?.let { declared ->
                            if (declared == 0L && entries.isNotEmpty()) errors += "$file: declares no native libraries but contains ELF code"
                            val measured = entries.filter { it.abi in strictAbis.get() }.mapNotNull { it.minLoadAlign }.minOrNull()
                            if (measured != null && declared > measured) errors += "$file: declared alignment $declared exceeds measured $measured"
                        }
                    } catch (e: Exception) { errors += "$file: ${e.message}" }
                }
                apks.forEach { scan(it, true) }
                extraFiles.files.sorted().forEach { scan(it, false) }
            }
        } catch (e: Exception) { errors += e.message ?: e.javaClass.name }
        val report = reportFile.get().asFile
        report.parentFile.mkdirs()
        report.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(linkedMapOf(
            "schemaVersion" to 1, "variant" to variant.get(), "pageSize" to pageSize.get(),
            "strictAbis" to strictAbis.get().sorted(), "artifacts" to reports,
            "summary" to mapOf("ok" to (errors.isEmpty() && !skipped), "skipped" to skipped, "errors" to errors),
        ))) + "\n")
        if (errors.isNotEmpty()) throw GradleException("Native page alignment verification failed:\n${errors.joinToString("\n")}\nReport: $report")
        if (!skipped) logger.lifecycle("Native page alignment verified (${pageSize.get()} bytes): $report")
    }

    private fun discoverApks(): List<File> {
        val root = apkDirectory.get().asFile
        val requested = variant.get()
        val metadata = root.walkTopDown().filter {
            it.isFile && it.name == "output-metadata.json" &&
                it.relativeTo(root).toPath().none { part -> part.toString().equals("androidTest", true) }
        }.sorted().toList()
        val matches = metadata.mapNotNull { file ->
            val value = JsonSlurper().parse(file) as? Map<*, *> ?: error("Invalid APK metadata: $file")
            val name = value["variantName"] as? String ?: error("Missing variantName: $file")
            if (requested.isEmpty() || name.equals(requested, true) || name.endsWith(requested, true) || name.startsWith(requested, true)) {
                val elements = value["elements"] as? List<*> ?: error("Missing APK elements: $file")
                elements.map { element ->
                    val filename = (element as? Map<*, *>)?.get("outputFile") as? String ?: error("Missing APK outputFile: $file")
                    val apk = file.parentFile.resolve(filename).canonicalFile
                    check(apk.toPath().startsWith(root.canonicalFile.toPath())) { "APK output escapes build directory: $apk" }
                    check(apk.isFile) { "Metadata references missing APK: $apk" }
                    apk
                }
            } else null
        }.flatten().distinct()
        // Never sweep stale files with guessed variant paths; customized outputs use the explicit DSL.
        return matches
    }
}

class NativeAlignmentPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val extension = extensions.create<NativeAlignmentExtension>("nativeAlignment")
        extension.strictAbis.convention(setOf("arm64-v8a", "x86_64"))
        extension.pageSize.convention(16384L)
        extension.expectNoNativeLibraries.convention(false)
        extension.checkRelro.convention(false)
        extension.scanEmbeddedPayloads.convention(false)
        val skip = providers.gradleProperty("autojs.nativeAlignment.skip").map { it.toBooleanStrict() }.orElse(false)
        val ciProvider = providers.provider {
            listOf("CI", "GITHUB_ACTIONS", "TF_BUILD", "JENKINS_URL", "BUILD_BUILDID").any { name ->
                providers.environmentVariable(name).orNull?.let { it.isNotBlank() && !it.equals("false", true) && it != "0" } == true
            }
        }
        fun registerVerification(suffix: String) = tasks.register<VerifyNativePageAlignment>("verify${suffix}NativePageAlignment") {
            group = "verification"
            description = "Verifies 16 KB ELF and APK ZIP alignment for ${suffix.ifEmpty { "all built variants" }}."
            strictAbis.set(extension.strictAbis)
            pageSize.set(extension.pageSize)
            expectNoNativeLibraries.set(extension.expectNoNativeLibraries)
            checkRelro.set(extension.checkRelro)
            scanEmbeddedPayloads.set(extension.scanEmbeddedPayloads)
            variant.set(suffix)
            skipVerification.set(skip)
            ci.set(ciProvider)
            apkDirectory.set(layout.buildDirectory.dir("outputs/apk"))
            apkFiles.from(extension.apkFiles)
            extraFiles.from(extension.extraFiles)
            reportFile.set(layout.buildDirectory.file("reports/native-alignment/${suffix.ifEmpty { "all" }}.json"))
        }
        registerVerification("")
        pluginManager.withPlugin("com.android.application") {
            afterEvaluate {
                tasks.names.filter(::isApkAssembleTask).forEach { name ->
                    val verification = registerVerification(name.removePrefix("assemble"))
                    tasks.named(name) { finalizedBy(verification) }
                }
            }
        }
    }
}

// Unit-test and test-fixture assemblies produce JVM classes or AARs, not APKs.
// Keep application variants and flavour/build-type aggregate assemblies verified.
internal fun isApkAssembleTask(name: String): Boolean =
    name.startsWith("assemble") && name.length > "assemble".length &&
        listOf("AndroidTest", "UnitTest", "TestFixtures").none { name.contains(it) }
