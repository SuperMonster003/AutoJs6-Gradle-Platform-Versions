package org.autojs.build.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PlatformDetectorTest {

    private val dataSource = DataSource(localDataDir = null)

    private fun detect(
        vararg properties: Pair<String, String>,
        versionProps: Map<String, String> = emptyMap(),
    ): Platform {
        val values = mapOf(*properties)
        return PlatformDetector(SystemProperties(values::get), dataSource, versionProps).determine()
    }

    @Test
    fun `Temurin is explicitly a Gradle-compatible headless platform`() {
        val platform = detect(
            "java.vendor.version" to "Temurin-21.0.6+7",
            "java.vendor" to "Eclipse Adoptium",
            "os.name" to "Linux",
        )

        assertEquals("Temurin", platform.fullName)
        assertEquals(AgpSelectionMode.GRADLE_COMPATIBILITY, platform.agpSelectionMode)
        assertTrue(platform.agpVersionMap.isEmpty())
    }

    @Test
    fun `a bare operating system is explicitly Gradle-compatible too`() {
        val platform = detect("os.name" to "Linux")

        assertEquals("Linux", platform.fullName)
        assertEquals(AgpSelectionMode.GRADLE_COMPATIBILITY, platform.agpSelectionMode)
    }

    @Test
    fun `Android Studio support floor comes from the oldest central map entry`() {
        val platform = detect(
            "idea.paths.selector" to "AndroidStudio2023.3",
            "idea.version" to "2023.3",
            versionProps = mapOf("MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION" to "2023.3"),
        )

        assertEquals("2025.2.3", platform.minSupportedVersion)
        val error = assertThrows<IllegalStateException>(platform::ensureMinimalIdeVersion)
        assertTrue(error.message!!.contains("effective minimum requirement 2025.2.3"))
    }

    @Test
    fun `IntelliJ IDEA support floor cannot be widened by a consumer`() {
        val platform = detect(
            "idea.paths.selector" to "IntelliJIdea2023.3",
            "idea.version" to "2023.3",
            versionProps = mapOf("MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION" to "2023.3"),
        )

        assertEquals("2026.1.2", platform.minSupportedVersion)
        assertThrows<IllegalStateException>(platform::ensureMinimalIdeVersion)
    }

    @Test
    fun `a project may tighten but not disable the central IDE support floor`() {
        val stricter = detect(
            "idea.paths.selector" to "AndroidStudio2025.3.4",
            "idea.version" to "2025.3.4",
            versionProps = mapOf("MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION" to "2026.1"),
        )
        val none = detect(
            "idea.paths.selector" to "AndroidStudio2025.3.4",
            "idea.version" to "2025.3.4",
            versionProps = mapOf("MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION" to "NONE"),
        )

        assertEquals("2026.1", stricter.minSupportedVersion)
        assertThrows<IllegalStateException>(stricter::ensureMinimalIdeVersion)
        assertEquals("2025.2.3", none.minSupportedVersion)
        none.ensureMinimalIdeVersion()
    }

    @Test
    fun `Android Studio exact build wins over truncated IDEA version`() {
        val platform = detect(
            "idea.paths.selector" to "AndroidStudio2026.1",
            "idea.version" to "2026.1",
            "idea.vendor.name" to "Google",
            "android.studio.version" to "261.26222.65.2613.16025427",
        )

        assertEquals("2026.1.3.8", platform.version)
        assertEquals("Android Studio Quail", platform.fullName)
    }

    @Test
    fun `Android Studio strict version preserves patch line for an unknown build`() {
        val platform = detect(
            "idea.paths.selector" to "AndroidStudio2026.1",
            "idea.version" to "2026.1",
            "idea.vendor.name" to "Google",
            "android.studio.version" to "261.99999.999.2613.99999999",
            "android.ide.strict.version" to "2026.1.3.8",
        )

        assertEquals("2026.1.3.8", platform.version)
    }
}
