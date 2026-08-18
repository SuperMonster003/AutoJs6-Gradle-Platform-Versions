package org.autojs.build.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Covers the three scenarios that matter downstream: a map that covers the IDE,
 * a map that has fallen behind, and a plain command line build.
 */
class VersionDeciderTest {

    /** Reads the datasets bundled in this plugin's own resources. */
    private val dataSource = DataSource(localDataDir = null)

    private val ideaAgpVersionMap = mapOf(
        "2026.2.1" to "9.1.1",
        "2026.1.2" to "9.0.1",
        "2026.1" to "8.13.2",
        "2025.2.2" to "8.12.0",
        "2025.1" to "8.10.1",
        "2024.3" to "8.7.3",
        "2023.3" to "8.2.2",
    )

    private fun idea(version: String, map: Map<String, String> = ideaAgpVersionMap) = Platform(
        name = "IntelliJIdea",
        vendor = "Jetbrains",
        agpVersionMap = map,
        displayName = "IntelliJ IDEA",
    ).also { it.version = version }

    private fun commandLine() = Platform(name = "Windows 11", vendor = "unknown")

    private fun decider(platform: Platform, gradleVersion: String = "9.3.0") =
        VersionDecider(platform, dataSource, gradleVersion)

    @Test
    fun `bundled datasets load from plugin resources`() {
        assertTrue(dataSource.list("agp-releases").isNotEmpty())
        assertTrue(dataSource.props("agp-gradle-compat").isNotEmpty())
        assertTrue(dataSource.props("gradle-kotlin-compat").isNotEmpty())
        assertTrue(dataSource.props("java-gradle-compat").isNotEmpty())
    }

    @Test
    fun `scenario B - a covered IDE version follows the map`() {
        val decision = decider(idea("2026.1.2")).decideAgpVersion()
        assertEquals("9.0.1", decision.version)
        assertTrue(decision.notices.isEmpty()) { "an up-to-date map should not raise a notice" }
    }

    @Test
    fun `scenario B - a covered IDE version between entries matches the nearest lower`() {
        val decision = decider(idea("2025.1.4")).decideAgpVersion()
        assertEquals("8.10.1", decision.version)
        assertTrue(decision.hintSuffix.contains(Identifier.NEAREST_LOWER_MATCHED))
    }

    @Test
    fun `scenario A - an IDE newer than the whole map falls back to auto selection`() {
        val decision = decider(idea("2027.1")).decideAgpVersion()
        // Auto selection means the newest released AGP the running Gradle can load,
        // rather than 9.1.1, the newest entry the stale map happens to know.
        assertEquals(decider(idea("2027.1")).maxSupportedAgpVersion(), decision.version)
        assertEquals(Identifier.AUTO_SPECIFIED_SUFFIX, decision.hintSuffix)
        assertTrue(decision.notices.single().contains("newer than all agpVersionMap entries"))
    }

    @Test
    fun `reproduces the IDEA 2026 2 incident`() {
        // The map as it stood before the fix, topping out at 2025.2.2 to AGP 8.12.0.
        val staleMap = ideaAgpVersionMap.filterKeys { it < "2026" }
        val decision = decider(idea("2026.2", staleMap)).decideAgpVersion()
        // Gradle 9.3.0 caps AGP at the 9.0 line, which still carries the built-in Kotlin
        // support that AGP 8.12.0 lacks, so the build no longer breaks.
        assertEquals("9.0.1", decision.version) { "should not silently downgrade to 8.12.0" }
        assertTrue(decision.notices.isNotEmpty())
    }

    @Test
    fun `scenario C - a bare command line uses auto selection`() {
        val decision = decider(commandLine()).decideAgpVersion()
        // No map at all, so the decision rests purely on the Gradle compatibility table:
        // AGP 9.1 would need Gradle 9.3.1, leaving 9.0.1 as the newest usable release.
        assertEquals("9.0.1", decision.version)
        assertTrue(decision.hintSuffix.contains(Identifier.AUTO))
        assertTrue(decision.notices.isEmpty())
    }

    @Test
    fun `caps AGP against the running Gradle version`() {
        // Gradle 8.9 predates AGP 9.x, so a map promising 9.1.1 must be capped.
        val decision = decider(idea("2026.2.1"), gradleVersion = "8.9").decideAgpVersion()
        assertEquals("8.7.3", decision.version)
        assertTrue(decision.hintSuffix.contains(Identifier.DOWNGRADED))
    }

    @Test
    fun `resolves a two-segment map value to a released patch version`() {
        val decision = decider(idea("2026.2", mapOf("2026.1" to "9.0"))).decideAgpVersion()
        assertEquals("9.0.1", decision.version)
    }

    @Test
    fun `an IDE predating the whole map still gets auto selection`() {
        val platform = idea("2020.1")
        val decision = decider(platform).decideAgpVersion()
        // No entry matches, so auto selection applies rather than leaving it undecided.
        assertEquals("9.0.1", decision.version)
        assertTrue(decision.hintSuffix.contains(Identifier.AUTO))
        // The last-resort fallback stays available for callers when even auto yields nothing.
        assertEquals("8.2.2", decider(platform).agpFallbackVersion())
    }

    @Test
    fun `an unknown platform version still decides a version`() {
        val platform = idea(Consts.DEFAULT_VERSION)
        val decision = decider(platform).decideAgpVersion()
        assertEquals("9.0.1", decision.version)
        assertTrue(decision.notices.isEmpty()) { "an unknown version must not be reported as stale" }
    }

    @Test
    fun `decides Kotlin from the running Gradle version`() {
        val onGradle93 = decider(idea("2026.2.1")).decideKotlinVersion().version
        val onGradle89 = decider(idea("2026.2.1"), gradleVersion = "8.9").decideKotlinVersion().version
        assertTrue(onGradle93 != null && onGradle89 != null)
        assertTrue(VersionComparator.compareVersionStrings(onGradle93!!, onGradle89!!) > 0) {
            "a newer Gradle should allow a newer Kotlin: $onGradle93 vs $onGradle89"
        }
    }

    @Test
    fun `maps Gradle versions to the highest supported Java version`() {
        val decider = decider(commandLine())
        assertTrue(decider.getMaxSupportedJavaVersion("9.3.0") >= decider.getMaxSupportedJavaVersion("8.5"))
        assertTrue(decider.getMaxSupportedJavaVersion("8.5") >= 17)
    }

    @Test
    fun `reads the java table with the Gradle version rather than the AGP version`() {
        val decider = decider(commandLine())
        // Both original settings scripts fed the AGP version into this table. Guard the
        // fix by pinning the two results apart: 9.3.0 is a Gradle version allowing 25,
        // whereas 9.0.1 as an AGP version would land on 24.
        assertEquals(25, decider.getMaxSupportedJavaVersion("9.3.0"))
        assertEquals(24, decider.getMaxSupportedJavaVersion("9.0.1"))
    }

}
