package org.autojs.build.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlatformDetectorTest {

    private val dataSource = DataSource(localDataDir = null)

    private fun detect(vararg properties: Pair<String, String>): Platform {
        val values = mapOf(*properties)
        return PlatformDetector(SystemProperties(values::get), dataSource, emptyMap()).determine()
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
}
