package org.autojs.build.platform

/**
 * A build host: an IDE, a JDK vendor, or a bare operating system.
 *
 * IDE platforms constrain AGP through a compatibility map. Its oldest key is
 * also their central support floor; a project may tighten, but never lower, that
 * boundary. Headless platforms instead select directly from the versions supported
 * by the running Gradle.
 *
 * zh-CN: 构建宿主, 可以是 IDE / JDK 发行方 / 裸操作系统.
 * IDE 平台通过兼容映射表约束 AGP, 且表中最早的 key 同时构成不可由项目降低的中央
 * 支持下界; 无头平台则直接按当前 Gradle 的能力自动选择.
 */
open class Platform(
    var name: String,
    val vendor: String,
    val agpVersionMap: Map<String, String> = emptyMap(),
    val weight: Int = -Int.MAX_VALUE,
    val gradleSettingsName: String? = null,
    val minSupportedVersion: String = Consts.DEFAULT_VERSION,
    val shouldPrintProgress: Boolean = true,
    val displayName: String? = null,
    val agpSelectionMode: AgpSelectionMode = when {
        agpVersionMap.isEmpty() -> AgpSelectionMode.GRADLE_COMPATIBILITY
        else -> AgpSelectionMode.PLATFORM_COMPATIBILITY
    },
) {

    var version: String = Consts.DEFAULT_VERSION

    open val fullName: String
        get() = displayName ?: name.replaceFirstChar { it.uppercase() }

    /** Tells whether the running environment looks like this platform. */
    open fun matchEnvironment(systemProperties: SystemProperties): Boolean =
        systemProperties.platform?.startsWith(name) == true
                || systemProperties.vendorName?.contains(vendor, true) == true

    /** Fails when the IDE predates the effective central/project support floor. */
    fun ensureMinimalIdeVersion() {
        if (minSupportedVersion == Consts.DEFAULT_VERSION) return
        if (VersionComparator.compareVersionStrings(version, minSupportedVersion) >= 0) return
        throw IllegalStateException(
            "Current IDE ($fullName) version $version does not meet the effective minimum requirement " +
                    "$minSupportedVersion from the central compatibility range and any stricter project requirement."
        )
    }

    /** Fails when the Gradle JDK predates the minimum this project supports. */
    fun ensureMinimalGradleJdkVersion(currentJavaMajorVersion: Int, javaVersionMinSupported: Int) {
        if (currentJavaMajorVersion >= javaVersionMinSupported) return
        throw IllegalStateException(
            Formatted(
                "Current Gradle JDK version $currentJavaMajorVersion does not meet " +
                        "the minimum requirement which $javaVersionMinSupported is needed",
                buildList {
                    gradleSettingsName?.let { add("Settings path: File | Settings | Build, Execution, Deployment | Build Tools | Gradle") }
                    add("Change \"${gradleSettingsName ?: "Gradle JDK"}\" to $javaVersionMinSupported at the least")
                },
            ).text
        )
    }

    /** Prepends the platform banner line to the console summary. */
    fun prependConsoleInformation(consoleInfo: MutableList<String>) {
        val versionSuffix = when (version.isNotEmpty() && version != Consts.DEFAULT_VERSION) {
            true -> " | $version"
            else -> ""
        }
        consoleInfo.add(0, "Platform: $fullName$versionSuffix")
    }

}

/**
 * Defines where the upper AGP compatibility boundary comes from.
 *
 * [PLATFORM_COMPATIBILITY] is used by IDEs whose bundled Android integration has
 * its own AGP ceiling. [GRADLE_COMPATIBILITY] is used by Temurin and bare command
 * line builds, where no IDE imposes an additional ceiling.
 *
 * zh-CN: 定义 AGP 兼容上界的来源. IDE 使用 [PLATFORM_COMPATIBILITY], 因为其内置
 * Android 集成有独立的 AGP 上限; Temurin 与裸命令行使用 [GRADLE_COMPATIBILITY],
 * 因为此时不存在额外的 IDE 上限.
 */
enum class AgpSelectionMode {
    PLATFORM_COMPATIBILITY,
    GRADLE_COMPATIBILITY,
}

/** The subset of system properties that identifies the build host. */
class SystemProperties(private val lookup: (String) -> String?) {

    val version: String? get() = lookup("idea.version")

    val platform: String?
        get() = lookup("idea.paths.selector")
            ?: lookup("idea.platform.prefix")
            ?: lookup("java.vendor.version")

    val vendorName: String?
        get() = lookup("idea.vendor.name")
            ?: lookup("java.vendor")
            ?: lookup("java.vm.vendor")

    val osName: String? get() = lookup("os.name")

    val androidStudioVersion: String? get() = lookup("android.studio.version")

    companion object {
        /** Reads from the real JVM system properties. */
        fun ofSystem() = SystemProperties { System.getProperty(it) }
    }

}
