package org.autojs.build.platform

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra

/**
 * Settings plugin deciding the AGP and Kotlin Gradle plugin versions for the build.
 *
 * Apply it from a consuming settings script, above `includeBuild` so that an included
 * build sees the system properties this publishes:
 *
 * ```kotlin
 * pluginManagement {
 *     repositories { gradlePluginPortal(); mavenCentral(); google() }
 *     plugins { id("io.github.supermonster003.autojs6-platform-versions") version "x.y.z" }
 * }
 *
 * plugins { id("io.github.supermonster003.autojs6-platform-versions") }
 * ```
 *
 * A project that also needs AGP on its own buildscript classpath cannot use the plugin
 * alone, because that classpath resolves before any settings plugin is applied. Such a
 * project calls [PlatformVersionsFacade] from the script body instead.
 *
 * zh-CN: 决定构建所用 AGP 与 Kotlin Gradle 插件版本的 Settings 插件.
 * 需在 `includeBuild` 之前应用, 以便 included build 能读到本插件发布的系统属性.
 * 若项目还需要把 AGP 放到自己的 buildscript classpath 上, 仅用本插件无法满足
 * (该 classpath 的解析早于任何 settings 插件的应用), 此时改在脚本体中调用 [PlatformVersionsFacade].
 */
class PlatformVersionsSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val versions = PlatformVersionsFacade.decide(
            settings.rootDir,
            settings.gradle.gradleVersion,
            settings.gradle.startParameter.projectProperties,
        )

        settings.extensions.add(PlatformVersionsExtension::class.java, EXTENSION_NAME, versions)
        settings.gradle.extra.set("platform", versions.platform)
        settings.gradle.extra.set("platformVersions", versions)

        // Also exported as plain strings, because a settings buildscript block is compiled
        // against its own classpath and cannot resolve the extension type.
        // zh-CN: 同时以纯字符串导出: settings 的 buildscript 块使用独立的编译类路径, 无法解析扩展类型.
        settings.gradle.extra.set("agpClasspathNotation", versions.agpClasspathNotation)
        settings.gradle.extra.set("kotlinClasspathNotation", versions.kotlinClasspathNotation)
        settings.gradle.extra.set("r8ClasspathNotation", versions.r8ClasspathNotation)

        alignRootBuildscriptKotlin(settings, versions)

        settings.gradle.taskGraph.whenReady {
            if (allTasks.none { it.name == "clean" }) {
                PlatformVersionsFacade.printVersionInfo(versions, settings.gradle.gradleVersion)
            }
        }
    }

    companion object {
        const val EXTENSION_NAME = "platformVersions"

        private const val KOTLIN_GRADLE_PLUGIN_GROUP = "org.jetbrains.kotlin"
        private const val KOTLIN_GRADLE_PLUGIN_MODULE = "kotlin-gradle-plugin"
        private val MAVEN_CENTRAL_URLS = setOf(
            "https://repo.maven.apache.org/maven2",
            "https://repo1.maven.org/maven2",
        )
    }

    /**
     * Places the selected KGP on the root script classpath before its plugins are resolved.
     *
     * AGP 9 compiles Android Kotlin sources through a runtime dependency on KGP. That bundled
     * dependency can trail the version selected here; AGP 9.3.2, for example, carries KGP
     * 2.2.10 (JVM target 24), while Gradle 9.5 supports KGP 2.3.20 (JVM target 25). Merely
     * exporting `gradle.kotlin.version` does not participate in dependency resolution when a
     * consumer has no explicit Kotlin plugin. Adding the module before root-project evaluation
     * lets normal Gradle conflict resolution upgrade AGP's runtime consistently, without
     * applying a second Kotlin plugin to Android modules.
     *
     * zh-CN: 在根项目插件解析前把已选 KGP 放入脚本 classpath. AGP 9 通过 KGP 运行时依赖
     * 编译 Android Kotlin 源码, 其捆绑版本可能落后于中央决策; 仅导出属性并不会参与依赖
     * 解析. 此处交由 Gradle 的正常冲突解析提升 AGP 运行时, 且不会向 Android 模块重复应用插件.
     */
    private fun alignRootBuildscriptKotlin(
        settings: Settings,
        versions: PlatformVersionsExtension,
    ) {
        settings.gradle.beforeProject(Action<Project> {
            val project = this
            if (project == project.rootProject) {
                val repositories = project.buildscript.repositories
                val hasMavenCentral = repositories
                    .withType(MavenArtifactRepository::class.java)
                    .any { repository -> repository.url.toString().trimEnd('/') in MAVEN_CENTRAL_URLS }
                if (!hasMavenCentral) {
                    repositories.mavenCentral()
                }

                val classpath = project.buildscript.configurations.getByName("classpath")
                val alreadyDeclared = classpath.dependencies.any { dependency ->
                    dependency.group == KOTLIN_GRADLE_PLUGIN_GROUP &&
                            dependency.name == KOTLIN_GRADLE_PLUGIN_MODULE &&
                            dependency.version == versions.kotlinVersion
                }
                if (!alreadyDeclared) {
                    project.buildscript.dependencies.add("classpath", versions.kotlinClasspathNotation)
                }
            }
        })
    }

}
