package org.autojs.build.platform

/** A lower AGP boundary and the project input that introduced it. */
data class AgpRequirement(
    val minimumVersion: String,
    val source: String,
)

/**
 * Derives project-level AGP requirements without turning them into exact pins.
 *
 * Android publishes the minimum AGP needed by each `compileSdk` / `targetSdk`
 * API level. Projects may also declare [MIN_SUPPORTED_AGP_PROPERTY] when an AAR
 * dependency or a preview SDK imposes a newer lower boundary that cannot be
 * inferred during settings evaluation.
 *
 * zh-CN: 从项目属性推导 AGP 最低版本要求, 但不把它们变成固定版本. Android 官方表
 * 给出各 compileSdk / targetSdk API 等级需要的最低 AGP; 若某个 AAR 依赖或预览 SDK
 * 还有无法在 settings 阶段自动推导的要求, 项目可通过 [MIN_SUPPORTED_AGP_PROPERTY]
 * 声明额外下界.
 */
class AgpRequirementResolver(
    private val dataSource: DataSource,
    private val versionProps: Map<String, String>,
) {

    private val androidApiAgpCompatProps by lazy { dataSource.props("android-api-agp-compat") }

    /** Returns every lower boundary that applies to the consuming project. */
    fun projectRequirements(): List<AgpRequirement> {
        val explicit = versionProps.optionalVersion(MIN_SUPPORTED_AGP_PROPERTY)?.let {
            validateVersion(MIN_SUPPORTED_AGP_PROPERTY, it)
            AgpRequirement(it, MIN_SUPPORTED_AGP_PROPERTY)
        }

        return buildList {
            listOf(
                COMPILE_SDK_PROPERTY to "compileSdk",
                TARGET_SDK_PROPERTY to "targetSdk",
            ).forEach { (propertyName, displayName) ->
                versionProps[propertyName]
                    ?.takeUnless { it.isBlank() || it == "NONE" }
                    ?.let { rawApi ->
                        minimumAgpForAndroidApi(
                            propertyName = propertyName,
                            displayName = displayName,
                            rawApi = rawApi,
                            hasExplicitMinimum = explicit != null,
                        )
                    }
                    ?.let(::add)
            }
            explicit?.let(::add)
        }
    }

    /** Highest minimum version across [requirements], or null when none apply. */
    fun highestMinimum(requirements: Collection<AgpRequirement>): String? = requirements
        .maxWithOrNull { left, right ->
            VersionComparator.compareVersionStrings(left.minimumVersion, right.minimumVersion)
        }
        ?.minimumVersion

    private fun minimumAgpForAndroidApi(
        propertyName: String,
        displayName: String,
        rawApi: String,
        hasExplicitMinimum: Boolean,
    ): AgpRequirement? {
        val api = rawApi.trim()
        if (!api.matches(ANDROID_API_PATTERN)) {
            if (hasExplicitMinimum) return null
            throw IllegalStateException(
                "$propertyName=$rawApi is not a numeric Android API level. " +
                        "Use a value such as 36 or 36.1, or declare " +
                        "$MIN_SUPPORTED_AGP_PROPERTY for a preview/custom SDK."
            )
        }

        val entries = androidApiAgpCompatProps.entries
        val newestKnown = entries.maxWithOrNull { left, right ->
            VersionComparator.compareVersionStrings(left.key, right.key)
        } ?: throw IllegalStateException("Android API/AGP compatibility data is empty")

        val isNewerThanKnown = VersionComparator.compareVersionStrings(api, newestKnown.key) > 0
        if (isNewerThanKnown && !hasExplicitMinimum) {
            throw IllegalStateException(
                "No minimum AGP compatibility data is available for $propertyName=$api. " +
                        "The newest known Android API level is ${newestKnown.key}. " +
                        "Update android-api-agp-compat.properties or declare " +
                        "$MIN_SUPPORTED_AGP_PROPERTY explicitly."
            )
        }

        val matched = entries
            .filter { VersionComparator.compareVersionStrings(it.key, api) <= 0 }
            .maxWithOrNull { left, right ->
                VersionComparator.compareVersionStrings(left.key, right.key)
            }
            ?: return null

        validateVersion("minimum AGP for Android API ${matched.key}", matched.value)
        return AgpRequirement(matched.value, "$displayName $api")
    }

    private fun validateVersion(label: String, version: String) {
        runCatching { VersionComparator.toVersionParts(version) }.getOrElse {
            throw IllegalStateException("$label has an invalid version value: $version", it)
        }
    }

    private fun Map<String, String>.optionalVersion(key: String): String? =
        get(key)?.trim()?.takeUnless { it.isBlank() || it == "NONE" }

    companion object {
        const val COMPILE_SDK_PROPERTY = "COMPILE_SDK_VERSION"
        const val TARGET_SDK_PROPERTY = "TARGET_SDK_VERSION"
        const val MIN_SUPPORTED_AGP_PROPERTY = "MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION"

        private val ANDROID_API_PATTERN = Regex("\\d+(?:\\.\\d+)?")
    }
}
