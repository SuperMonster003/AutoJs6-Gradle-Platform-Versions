package org.autojs.build.platform

/**
 * Identifies the build host and resolves its version.
 *
 * The IntelliJ IDEA map is maintained by hand here, whereas the Android Studio
 * map comes from scraped data. Temurin is explicitly headless and therefore uses
 * Gradle compatibility directly instead of pretending that an empty IDE map is a
 * compatibility policy. The oldest key in each IDE map defines the global support
 * floor; a consumer property can raise that floor for a project, but cannot lower it.
 *
 * zh-CN: 识别构建宿主并解析其版本. IntelliJ IDEA 映射表在此手动维护, Android Studio
 * 映射表来自抓取数据. Temurin 被显式建模为无头环境, 直接按 Gradle 兼容性选择,
 * 不再借助一个空的 IDE 映射表间接表达策略. 各 IDE 映射表最早的 key 定义全局支持
 * 下界; 消费仓属性可为项目提高该下界, 但不能将它降低.
 */
class PlatformDetector(
    private val systemProperties: SystemProperties,
    private val dataSource: DataSource,
    private val versionProps: Map<String, String>,
) {

    /**
     * @Reference AGP Upgrade Assistant integrated within JetBrains IntelliJ IDEA.
     *
     * The newest entry has to state the newest IDE's real ceiling, not just the oldest
     * supported one. Falling off the end of this map means auto selection, which is
     * bounded only by what the running Gradle can load: on Gradle 9.6.1 that reaches
     * AGP 9.2.1, which IDEA 2026.2 rejects with "Latest supported version is AGP 9.1.0".
     *
     * zh-CN: 最新条目必须写出最新 IDE 的真实上限, 而不只是保留最旧的受支持版本.
     * 越过表尾即进入 auto 选择, 其上界只受当前 Gradle 约束: Gradle 9.6.1 下会取到 AGP 9.2.1,
     * 而 IDEA 2026.2 会以 "Latest supported version is AGP 9.1.0" 拒绝它.
     */
    private val intelliJIdeaAgpVersionMap = mapOf(
        "2026.2" to "9.1.0", /* Reported by the IDE itself as its latest supported AGP. */
        "2026.1.2" to "9.0.1",
    )

    private val androidStudio by lazy {
        val agpVersionMap = dataSource.props("android-studio-agp-compat")
        object : Platform(
            name = "AndroidStudio",
            vendor = "Google",
            agpVersionMap = agpVersionMap,
            agpSelectionMode = AgpSelectionMode.PLATFORM_COMPATIBILITY,
            weight = Int.MAX_VALUE,
            gradleSettingsName = "Gradle JDK",
            minSupportedVersion = effectiveMinimumIdeVersion(
                agpVersionMap,
                "MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION",
            ),
        ) {
            override val fullName: String
                get() {
                    val codenameVersions = dataSource.props("android-studio-codename-version")
                    val codenames = dataSource.props("android-studio-codename")
                    val suffix = codenameVersions.let { prop ->
                        val letters = prop[version]
                            ?: prop[version.split(".").take(3).joinToString(".")]
                            ?: prop[version.split(".").take(2).joinToString(".")]
                            ?: return@let null
                        letters.split("|").joinToString(" / ", prefix = " ") { key ->
                            codenames[key.trim()] ?: key
                        }
                    } ?: ""
                    return "Android Studio$suffix"
                }
        }
    }

    private val intelliJIdea by lazy {
        Platform(
            name = "IntelliJIdea",
            vendor = "Jetbrains",
            agpVersionMap = intelliJIdeaAgpVersionMap,
            agpSelectionMode = AgpSelectionMode.PLATFORM_COMPATIBILITY,
            weight = 10,
            gradleSettingsName = "Gradle JVM",
            minSupportedVersion = effectiveMinimumIdeVersion(
                intelliJIdeaAgpVersionMap,
                "MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION",
            ),
            displayName = "IntelliJ IDEA",
        )
    }

    private val temurin by lazy {
        Platform(
            name = "Temurin",
            vendor = "temurin",
            agpSelectionMode = AgpSelectionMode.GRADLE_COMPATIBILITY,
            weight = 5,
            shouldPrintProgress = false,
        )
    }

    private val unknown by lazy {
        Platform(
            name = "Unknown",
            vendor = "unknown",
            agpSelectionMode = AgpSelectionMode.GRADLE_COMPATIBILITY,
        )
    }

    /** All known platforms, most specific first. */
    private val candidates by lazy { listOf(androidStudio, intelliJIdea, temurin) }

    /**
     * Picks the platform in use and fills in its version.
     *
     * With several matches, the heaviest wins: Android Studio reports itself as a
     * JetBrains product too, so its weight outranks IntelliJ IDEA.
     */
    fun determine(): Platform {
        val matched = candidates.filter { it.matchEnvironment(systemProperties) }
        val platform = when {
            matched.isEmpty() -> unknown.also { it.name = systemProperties.osName ?: it.name }
            matched.size > 1 -> matched.maxBy { it.weight }
            else -> matched.first()
        }
        platform.version = when (platform) {
            androidStudio -> parseAndroidStudioVersion() ?: parseVersion(platform)
            else -> parseVersion(platform)
        }
        return platform
    }

    /**
     * Uses Android Studio's exact build identity before its strict marketing version.
     *
     * Recent IDEs pass both values as Gradle project properties. The build map is the
     * most authoritative source because it comes from the same scraped release data;
     * `android.ide.strict.version` keeps patch-level identity when an unrecognised build
     * appears before the next data refresh.
     */
    private fun parseAndroidStudioVersion(): String? {
        val build = systemProperties.androidStudioVersion?.trim()?.takeIf { it.isNotEmpty() }

        build
            ?.let { dataSource.props("android-studio-build-version")[it] }
            ?.let { return it }

        systemProperties.androidIdeStrictVersion
            ?.trim()
            ?.takeIf(::isVersionLike)
            ?.let { return it }

        return build?.let(::parseAndroidStudioBuildNumber)
    }

    private fun parseVersion(platform: Platform): String {
        systemProperties.version?.let { return it }
        val raw = systemProperties.platform
        if (platform === unknown || raw == null) return Consts.DEFAULT_VERSION
        return raw.substring(platform.name.length)
            .replace(Regex("^\\W*"), "")
            .replace(Regex("^Preview", RegexOption.IGNORE_CASE), "")
    }

    /**
     * Resolves the IDE support floor without letting a consumer widen the central range.
     *
     * The oldest key in the central IDE-to-AGP map is the platform-wide baseline. A
     * consumer property is still useful for a project that deliberately supports fewer
     * IDE releases, but an older value cannot make an IDE outside the central map look
     * supported and fall through to Gradle-only AGP selection.
     *
     * zh-CN: 中央 IDE -> AGP 映射表的最早 key 是全局支持下界. 消费仓属性仍可为特定
     * 项目收紧范围, 但不能用更旧的值放宽中央下界, 让映射范围外的 IDE 落入仅由 Gradle
     * 决定 AGP 的回退路径.
     */
    private fun effectiveMinimumIdeVersion(
        agpVersionMap: Map<String, String>,
        consumerPropertyName: String,
    ): String {
        val centralMinimum = agpVersionMap.keys.minWithOrNull(VersionComparator::compareVersionStrings)
        val consumerMinimum = versionProps[consumerPropertyName]
            ?.trim()
            ?.takeUnless { it.isBlank() || it.equals("NONE", ignoreCase = true) || it == Consts.DEFAULT_VERSION }

        return listOfNotNull(centralMinimum, consumerMinimum)
            .maxWithOrNull(VersionComparator::compareVersionStrings)
            ?: Consts.DEFAULT_VERSION
    }

    /**
     * Turns an Android Studio build number into a marketing version.
     *
     * e.g. "251.26094.121.2513.13991806" becomes "2025.1.3".
     */
    fun parseAndroidStudioBuildToVersion(): String? {
        val build = systemProperties.androidStudioVersion?.trim()?.takeIf { it.isNotEmpty() } ?: return null

        dataSource.props("android-studio-build-version")[build]?.let { return it }

        return parseAndroidStudioBuildNumber(build)
    }

    private fun parseAndroidStudioBuildNumber(build: String): String? {
        val parts = build.split('.')
        val baseStr = parts.getOrNull(0) ?: return null
        val base = baseStr.toIntOrNull() ?: return null

        val year = 2000 + base / 10
        val minor = base % 10

        // Look for strings starting with base and having longer length in the remaining fragments.
        // e.g. "2513" means patch version is "3".
        // zh-CN: 在其余片段里找以 base 开头且长度更长的字段. 如 "2513" 意味着补丁版本为 "3".
        val patch = parts.drop(1).firstNotNullOfOrNull { seg ->
            if (seg.startsWith(baseStr) && seg.length > baseStr.length) {
                seg.substring(baseStr.length).toIntOrNull()
            } else null
        }

        return if (patch != null && patch > 0) "$year.$minor.$patch" else "$year.$minor"
    }

    private fun isVersionLike(value: String): Boolean =
        value.matches(Regex("\\d+(?:\\.\\d+){1,3}(?:[-+][0-9A-Za-z.-]+)?"))

}
