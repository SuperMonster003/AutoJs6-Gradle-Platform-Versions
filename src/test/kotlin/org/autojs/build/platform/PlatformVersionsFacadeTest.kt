package org.autojs.build.platform

import java.nio.file.Path
import kotlin.io.path.writeText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

class PlatformVersionsFacadeTest {

    @TempDir
    lateinit var rootDir: Path

    private val temurinProperties = SystemProperties(
        mapOf(
            "java.vendor.version" to "Temurin-17.0.16+8",
            "java.vendor" to "Eclipse Adoptium",
            "os.name" to "Linux",
        )::get
    )

    @Test
    fun `full headless decision derives API 36 minimum without pinning AGP`() {
        rootDir.resolve("version.properties").writeText(
            """
            COMPILE_SDK_VERSION=36
            TARGET_SDK_VERSION=36
            MIN_SUPPORTED_GRADLE_VERSION=9.1.0
            OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=NONE
            """.trimIndent()
        )

        val versions = PlatformVersionsFacade.decide(
            rootDir = rootDir.toFile(),
            gradleVersion = "9.3.0",
            systemProperties = temurinProperties,
        )

        assertEquals(AgpSelectionMode.GRADLE_COMPATIBILITY, versions.platform.agpSelectionMode)
        assertEquals("8.9.1", versions.minimumAgpVersion)
        assertEquals("9.0.1", versions.agpVersion)
        assertTrue(VersionComparator.compareVersionStrings(versions.agpVersion, versions.minimumAgpVersion!!) > 0) {
            "the API-derived lower boundary must not become an exact AGP pin"
        }
    }

    @Test
    fun `consumer minimum Gradle version is enforced before plugin decisions`() {
        rootDir.resolve("version.properties").writeText("MIN_SUPPORTED_GRADLE_VERSION=9.4.0")

        val error = assertThrows<IllegalStateException> {
            PlatformVersionsFacade.decide(
                rootDir = rootDir.toFile(),
                gradleVersion = "9.3.0",
                systemProperties = temurinProperties,
            )
        }
        assertTrue(error.message!!.contains("Current Gradle version 9.3.0"))
        assertTrue(error.message!!.contains("9.4.0"))
    }
}
