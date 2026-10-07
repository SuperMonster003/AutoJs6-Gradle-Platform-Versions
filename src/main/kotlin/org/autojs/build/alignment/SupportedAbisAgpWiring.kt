package org.autojs.build.alignment

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import java.lang.reflect.Method

/**
 * Wires the manifest transform through AGP's variant API without compiling against AGP.
 *
 * Consumers load AGP in a classloader this plugin cannot see (for example through an included
 * `build-logic` convention plugin, while this plugin comes from `pluginManagement`), so AGP types
 * referenced directly fail with NoClassDefFoundError at configuration time. Every AGP call is
 * therefore made reflectively on the `androidComponents` extension object, through the
 * classloader that object came from. Only Gradle API and Kotlin stdlib types cross the boundary;
 * both live in the shared parent classloader.
 *
 * zh-CN: 不编译依赖 AGP, 而是通过 `androidComponents` 扩展对象所在的类加载器反射调用 AGP 的
 * variant API. 消费端常通过 included build 的约定插件加载 AGP, 与本插件的类加载器互不可见,
 * 直接引用 AGP 类型会在配置期抛出 NoClassDefFoundError.
 */
internal object SupportedAbisAgpWiring {

    private const val SINGLE_ARTIFACT = "com.android.build.api.artifact.SingleArtifact"

    fun configure(project: Project, extension: NativeAlignmentExtension) {
        val components = project.extensions.findByName("androidComponents") ?: return
        val android = project.extensions.findByName("android")
        val loader = components.javaClass.classLoader
        val mergedNativeLibs = artifactType(loader, "MERGED_NATIVE_LIBS")
        val mergedManifest = artifactType(loader, "MERGED_MANIFEST")

        // AGP offers onVariants with a Kotlin function and with a Gradle Action; this satisfies either.
        val callback = object : Action<Any>, (Any) -> Unit {
            override fun execute(variant: Any) = wire(variant)
            override fun invoke(variant: Any) = wire(variant)
            private fun wire(variant: Any) = wireVariant(project, extension, android, variant, mergedNativeLibs, mergedManifest)
        }

        val selector = call(call(components, "selector"), "all")
        val onVariants = components.javaClass.methods.first { method ->
            method.name == "onVariants" && method.parameterCount == 2 && method.parameterTypes[1].isAssignableFrom(callback.javaClass)
        }
        onVariants.isAccessible = true
        onVariants.invoke(components, selector, callback)
    }

    private fun wireVariant(
        project: Project,
        extension: NativeAlignmentExtension,
        android: Any?,
        variant: Any,
        mergedNativeLibs: Any,
        mergedManifest: Any,
    ) {
        if (!extension.supportedAbisMetaData.get()) return
        run {
            val name = call(variant, "getName") as String
            val suffix = name.replaceFirstChar { it.uppercaseChar() }
            // Enabled ABI splits show up as one ABI-filtered output per split; without splits
            // the outputs carry no ABI filter and the packaged set stands.
            val splitAbis = (call(variant, "getOutputs") as List<*>)
                .flatMap { output -> call(output!!, "getFilters") as List<*> }
                .filter { filter -> call(filter!!, "getFilterType").toString() == "ABI" }
                .map { filter -> call(filter!!, "getIdentifier") as String }
                .toSet()
            val abiFilters = android?.let { dsl ->
                (call(call(call(dsl, "getDefaultConfig"), "getNdk"), "getAbiFilters") as Collection<*>).map { it as String }.toSet()
            }.orEmpty()
            val artifacts = call(variant, "getArtifacts")
            @Suppress("UNCHECKED_CAST")
            val nativeLibs = call(artifacts, "get", mergedNativeLibs) as Provider<Directory>

            val task = project.tasks.register<WriteSupportedAbisMetaData>("write${suffix}SupportedAbisMetaData") {
                group = "build"
                description = "Writes the supported-ABI meta-data of the $suffix merged manifest from its merged native libraries."
                this.mergedNativeLibs.set(nativeLibs)
                this.splitAbis.set(splitAbis)
                this.abiFilters.set(abiFilters)
                overrideAbis.set(extension.supportedAbis)
                metaDataName.set(extension.supportedAbisMetaDataName)
            }
            val operation = call(artifacts, "use", task)
            val request = call(operation, "wiredWithFiles", WriteSupportedAbisMetaData::inputManifest, WriteSupportedAbisMetaData::outputManifest)
            invoke(request, "toTransform", mergedManifest)
        }
    }

    /** The singleton of a `SingleArtifact` subtype, such as `SingleArtifact.MERGED_MANIFEST`. */
    private fun artifactType(loader: ClassLoader, name: String): Any =
        Class.forName("$SINGLE_ARTIFACT\$$name", true, loader).getField("INSTANCE").get(null)

    /**
     * Invokes the first public method of that name and arity and returns its non-null result;
     * implementation classes of AGP are not always public, so the method is made accessible first.
     */
    private fun call(target: Any, name: String, vararg arguments: Any): Any =
        invoke(target, name, *arguments) ?: throw IllegalStateException("AGP API ${target.javaClass.name}.$name returned null.")

    /** [call] for methods whose result is void or may be null. */
    private fun invoke(target: Any, name: String, vararg arguments: Any): Any? {
        val method = target.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == arguments.size && accepts(it, arguments) }
            ?: throw IllegalStateException("AGP API ${target.javaClass.name}.$name/${arguments.size} is unavailable; the native-alignment plugin needs AGP 7.0 or newer.")
        method.isAccessible = true
        return method.invoke(target, *arguments)
    }

    private fun accepts(method: Method, arguments: Array<out Any>): Boolean =
        method.parameterTypes.withIndex().all { (index, type) -> type.isPrimitive || type.isInstance(arguments[index]) }
}
