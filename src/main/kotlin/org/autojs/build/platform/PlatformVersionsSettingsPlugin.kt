package org.autojs.build.platform

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.util.Properties

/**
 * Settings plugin deciding the AGP and Kotlin Gradle plugin versions for the build.
 *
 * Apply it inside `pluginManagement` of a consuming settings script:
 *
 * ```kotlin
 * pluginManagement {
 *     plugins { id("org.autojs.build.platform-versions") version "1.0.0" }
 *     buildscript {
 *         repositories { mavenCentral(); google() }
 *         dependencies {
 *             val versions = extensions.getByType<PlatformVersionsExtension>()
 *             classpath(versions.agpClasspathNotation)
 *             classpath(versions.kotlinClasspathNotation)
 *         }
 *     }
 * }
 * ```
 *
 * zh-CN: 决定构建所用 AGP 与 Kotlin Gradle 插件版本的 Settings 插件,
 * 在消费端 settings 脚本的 pluginManagement 中应用.
 */
class PlatformVersionsSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val rootDir = settings.rootDir
        val versionProps = loadProperties(rootDir.resolve("version.properties"))
        val dataSource = DataSource(rootDir.resolve("gradle/data").takeIf { it.isDirectory })

        val systemProperties = SystemProperties.ofSystem()
        val platform = PlatformDetector(systemProperties, dataSource, versionProps).determine()

        platform.ensureMinimalIdeVersion()
        versionProps["JAVA_VERSION_MIN_SUPPORTED"]?.toIntOrNull()?.let { minSupported ->
            platform.ensureMinimalGradleJdkVersion(currentJavaMajorVersion(), minSupported)
        }

        val versionInfo = mutableListOf<String>()
        platform.prependConsoleInformation(versionInfo)

        val decider = VersionDecider(platform, dataSource, settings.gradle.gradleVersion)

        val overriddenJavaVersion = versionProps.escapeHatch("OVERRIDDEN_JAVA_VERSION")?.toIntOrNull()
        val overriddenAgpVersion = versionProps.escapeHatch("OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION")
        val overriddenKotlinVersion = versionProps.escapeHatch("OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION")

        val agpVersion = resolve(
            overridden = overriddenAgpVersion,
            decide = { decider.decideAgpVersion() },
            fallback = { decider.agpFallbackVersion() },
            label = "com.android.tools.build:gradle",
            versionInfo = versionInfo,
        )
        val kotlinVersion = resolve(
            overridden = overriddenKotlinVersion,
            decide = { decider.decideKotlinVersion() },
            fallback = { null },
            label = "org.jetbrains.kotlin:kotlin-gradle-plugin",
            versionInfo = versionInfo,
        )

        // Deliberately the Gradle version, not the AGP version. Both original settings
        // scripts passed the AGP version here, which reads the java-gradle-compat table
        // with the wrong input and coerces the toolchain lower than necessary: on Gradle
        // 9.3.0 with AGP 9.0.1 it yields 24 where the table allows 25.
        // zh-CN: 此处刻意传 Gradle 版本而非 AGP 版本. 两版原始 settings 脚本都误传了 AGP 版本,
        // 导致以错误输入查 java-gradle-compat 表, 把工具链上限压得过低:
        // Gradle 9.3.0 配 AGP 9.0.1 时会得到 24, 而按表应为 25.
        val maxSupportedJavaVersion = decider.getMaxSupportedJavaVersion(settings.gradle.gradleVersion)

        // Consumed by build-logic when it picks the toolchain and the JVM target.
        // zh-CN: 供 build-logic 选择 toolchain 与 JVM target 时消费.
        System.setProperty("gradle.java.version.coerced.by.gradle", "$maxSupportedJavaVersion")
        System.setProperty("gradle.java.version.overridden.by.user", "$overriddenJavaVersion")

        val extension = PlatformVersionsExtension(
            platform = platform,
            agpVersion = agpVersion,
            kotlinVersion = kotlinVersion,
            maxSupportedJavaVersion = maxSupportedJavaVersion,
            overriddenJavaVersion = overriddenJavaVersion,
            versionInfo = versionInfo.toList(),
        )
        settings.extensions.add(PlatformVersionsExtension::class.java, EXTENSION_NAME, extension)

        settings.gradle.extra.set("platform", platform)
        settings.gradle.extra.set("platformVersions", extension)

        settings.gradle.taskGraph.whenReady {
            if (allTasks.none { it.name == "clean" }) {
                Formatted(
                    "Version information for IDE platform and Gradle plugins",
                    versionInfo,
                    footers = listOf("Gradle version: ${settings.gradle.gradleVersion}"),
                ).print()
            }
        }
    }

    /** Applies the escape hatch when set, otherwise runs the decision and reports it. */
    private fun resolve(
        overridden: String?,
        decide: () -> Decision,
        fallback: () -> String?,
        label: String,
        versionInfo: MutableList<String>,
    ): String {
        overridden?.let {
            versionInfo += "Classpath: \"$label:$it\"${Identifier.USER_SPECIFIED_SUFFIX}"
            return it
        }
        val decision = decide()
        versionInfo += decision.notices
        val version = decision.version
        if (version != null) {
            versionInfo += "Classpath: \"$label:$version\"${decision.hintSuffix}"
            return version
        }
        val fallbackVersion = fallback()
            ?: throw IllegalStateException("Failed to determine version for classpath \"$label\"")
        versionInfo += "Classpath: \"$label:$fallbackVersion\"${Identifier.FALLBACK_SUFFIX}"
        return fallbackVersion
    }

    /** Reads an OVERRIDDEN_* pin, treating blank and "NONE" as unset. */
    private fun Map<String, String>.escapeHatch(key: String): String? =
        get(key)?.takeUnless { it.isBlank() || it == "NONE" }

    /** Major version of the running JVM, handling both "1.8.0_311" and "21.0.1" forms. */
    private fun currentJavaMajorVersion(): Int {
        val raw = System.getProperty("java.version") ?: return 0
        val head = raw.substringBefore('.').toIntOrNull() ?: return 0
        return when (head) {
            1 -> raw.split('.').getOrNull(1)?.toIntOrNull() ?: 0
            else -> head
        }
    }

    private fun loadProperties(file: File): Map<String, String> {
        if (!file.isFile) return emptyMap()
        @Suppress("UNCHECKED_CAST")
        return Properties().apply { file.inputStream().use { load(it) } } as Map<String, String>
    }

    companion object {
        const val EXTENSION_NAME = "platformVersions"
    }

}
