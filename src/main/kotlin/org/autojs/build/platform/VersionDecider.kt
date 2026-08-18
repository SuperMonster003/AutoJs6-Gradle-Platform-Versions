package org.autojs.build.platform

import org.autojs.build.platform.VersionComparator.sortByVersionName
import org.gradle.util.GradleVersion

/** A decided version plus the hint suffix explaining how it was reached. */
data class Decision(val version: String?, val hintSuffix: String, val notices: List<String> = emptyList())

/**
 * Decides which AGP and Kotlin Gradle plugin versions to put on the buildscript classpath.
 *
 * The AGP decision runs in three steps:
 * 1. nearest-lower match of the platform version against the platform's AGP map;
 * 2. a staleness check, so an IDE newer than the whole map falls back to auto selection
 *    instead of being downgraded to the newest known entry;
 * 3. capping against the AGP-to-Gradle compatibility table, so the result never exceeds
 *    what the running Gradle can load.
 *
 * zh-CN: 决定放到 buildscript classpath 上的 AGP 与 Kotlin Gradle 插件版本. AGP 决策分三步:
 * 1. 用平台版本对该平台的 AGP 映射表做就近向下匹配;
 * 2. 滞后判定: 若 IDE 比整张映射表都新, 回退到 auto 选择, 而不是降级到映射表中最新的已知条目;
 * 3. 按 AGP-Gradle 兼容表封顶, 保证结果不超出当前 Gradle 能加载的范围.
 */
class VersionDecider(
    private val platform: Platform,
    private val dataSource: DataSource,
    private val gradleVersion: String,
) {

    private val agpReleases by lazy { dataSource.list("agp-releases") }
    private val agpGradleCompatProps by lazy { dataSource.props("agp-gradle-compat") }
    private val gradleKotlinCompatProps by lazy { dataSource.props("gradle-kotlin-compat") }
    private val javaGradleCompatProps by lazy { dataSource.props("java-gradle-compat") }

    /** Decides the AGP version for the current platform and Gradle version. */
    fun decideAgpVersion(): Decision {
        val map = platform.agpVersionMap
        val matchedKey = VersionMapMatcher.findBestMatchingMapKey(map, platform.version)
        val matchedValue = matchedKey?.let { map[it] }
        var hintSuffix = when (matchedKey != null && matchedKey != platform.version) {
            true -> Identifier.NEAREST_LOWER_MATCHED_SUFFIX
            else -> ""
        }
        val notices = mutableListOf<String>()

        val maxSupportedAgpVersion = maxSupportedAgpVersion()
            ?: return Decision(matchedValue, hintSuffix)

        if (matchedValue != null && VersionMapMatcher.isPlatformNewerThanVersionMap(map, platform.version)) {
            // The platform is newer than every entry of the manually maintained agpVersionMap,
            // i.e. the map is stale. A newer IDE supports at least what its predecessors did,
            // so instead of silently downgrading to the newest known entry (which may be too
            // old to build this project), fall back to auto selection: the newest released AGP
            // compatible with the current Gradle version.
            // zh-CN: 当前平台版本比 agpVersionMap 全部条目都新, 说明手动维护的映射表已滞后.
            // 更新的 IDE 至少支持其前代所支持的 AGP, 因此不再静默降级到映射表中最新的已知条目
            // (可能旧到无法构建本项目), 而是回退到 auto 选择: 与当前 Gradle 兼容的最新 AGP 正式版.
            notices += "Notice: ${platform.fullName} ${platform.version} is newer than " +
                    "all agpVersionMap entries, AGP falls back to auto selection"
            return Decision(maxSupportedAgpVersion, Identifier.AUTO_SPECIFIED_SUFFIX, notices)
        }

        matchedValue ?: return Decision(
            maxSupportedAgpVersion,
            hintSuffix + Identifier.AUTO_SPECIFIED_SUFFIX,
            notices,
        )

        if (!matchedValue.contains("\\d+\\.\\d+\\.\\d+".toRegex())) {
            hintSuffix += Identifier.AUTO_SPECIFIED_SUFFIX
            return Decision(getAgpReleasedVersion(matchedValue) ?: "$matchedValue.0", hintSuffix, notices)
        }

        if (VersionComparator.compareVersionStrings(matchedValue, maxSupportedAgpVersion) > 0) {
            hintSuffix += Identifier.DOWNGRADED_SUFFIX
            return Decision(maxSupportedAgpVersion, hintSuffix, notices)
        }

        return Decision(matchedValue, hintSuffix, notices)
    }

    /**
     * Decides the Kotlin Gradle plugin version.
     *
     * Kotlin tracks Gradle rather than the IDE, so this always lands on the newest
     * Kotlin the running Gradle supports.
     */
    fun decideKotlinVersion(): Decision {
        val supported = gradleKotlinCompatProps.filter { (gradleMin, _) ->
            gradleVersion.toGradleVersion() >= gradleMin.toGradleVersion()
        }
        val kotlinMin = supported.maxByOrNull { it.key.toGradleVersion() }?.value

        val map = supported.toSortedMap(VersionComparator::compareVersionStringsDesc)
        val matchedKey = VersionMapMatcher.findBestMatchingMapKey(map, platform.version)
        val matchedValue = matchedKey?.let { map[it] }
        var hintSuffix = when (matchedKey != null && matchedKey != platform.version) {
            true -> Identifier.NEAREST_LOWER_MATCHED_SUFFIX
            else -> ""
        }

        return when {
            kotlinMin == null -> Decision(matchedValue, hintSuffix)
            matchedValue == null -> {
                hintSuffix += Identifier.AUTO_SPECIFIED_SUFFIX
                Decision(kotlinMin, hintSuffix)
            }
            matchedValue.toGradleVersion() < kotlinMin.toGradleVersion() -> {
                hintSuffix += Identifier.UPGRADED_SUFFIX
                Decision(kotlinMin, hintSuffix)
            }
            matchedValue.toGradleVersion() > kotlinMin.toGradleVersion() -> {
                hintSuffix += Identifier.DOWNGRADED_SUFFIX
                Decision(kotlinMin, hintSuffix)
            }
            else -> Decision(matchedValue, hintSuffix)
        }
    }

    /** Lowest version the AGP map may fall back to, used when nothing else matches. */
    fun agpFallbackVersion(): String? =
        platform.agpVersionMap.values.minWithOrNull(VersionComparator::compareVersionStrings)

    /** The newest released AGP that the running Gradle version can load. */
    fun maxSupportedAgpVersion(): String? {
        val currentGradleVersion = gradleVersion.toGradleVersion()
        val entries = agpGradleCompatProps.entries.sortedByDescending { it.value.toGradleVersion() }
        var maxSupportedAgpVersionPrefix: String? = null
        for ((agp, minRequiredGradle) in entries) {
            maxSupportedAgpVersionPrefix = agp
            if (currentGradleVersion >= minRequiredGradle.toGradleVersion()) {
                break
            }
        }
        return maxSupportedAgpVersionPrefix?.let { getAgpReleasedVersion(it) }
    }

    /** Newest stable AGP release carrying the given prefix, e.g. "9.0" resolves to "9.0.1". */
    private fun getAgpReleasedVersion(referenceAgpVersion: String): String? = with(VersionComparator) {
        agpReleases.sortByVersionName(isDescend = true)
            .find { it.startsWith(referenceAgpVersion) && !it.contains("-") }
    }

    /**
     * The highest Java version the given Gradle version fully supports.
     *
     * Exposed so downstream build logic can coerce toolchains rather than fail late.
     */
    fun getMaxSupportedJavaVersion(gradleVersion: String): Int {

        fun parseVersion(version: String) = version.split(Regex("[.-]")).map { it.toIntOrNull() ?: 0 }

        val inputGradleVersionInts = parseVersion(gradleVersion)
        val sortedJavaGradleCompatibility = javaGradleCompatProps.entries.map {
            it.key.toInt() to it.value
        }.sortedBy { it.first }

        var maxJavaVersion: Int = sortedJavaGradleCompatibility.first().first

        for ((presetJavaVersion, presetGradleVersion) in sortedJavaGradleCompatibility) {
            val presetGradleVersionInts: List<Int> = parseVersion(presetGradleVersion)

            for (i in presetGradleVersionInts.indices) {
                when {
                    i > inputGradleVersionInts.lastIndex -> break
                    inputGradleVersionInts[i] > presetGradleVersionInts[i] -> {
                        maxJavaVersion = presetJavaVersion
                        break
                    }
                    inputGradleVersionInts[i] < presetGradleVersionInts[i] -> break
                    i == presetGradleVersionInts.lastIndex -> maxJavaVersion = presetJavaVersion
                }
            }
        }

        return maxJavaVersion
    }

    private fun String.toGradleVersion() = GradleVersion.version(this)

}
