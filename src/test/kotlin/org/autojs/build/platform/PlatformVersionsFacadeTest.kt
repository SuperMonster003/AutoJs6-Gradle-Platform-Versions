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
        assertTrue(versions.agpRequirements.isNotEmpty()) {
            "the lower boundary and its sources must remain available to consumers"
        }
        assertTrue(versions.versionInfo.none { it.startsWith("Minimum: ") }) {
            "a satisfied lower boundary should not add routine noise to the successful-build summary"
        }
    }

    @Test
    fun `satisfied KSP minimum stays machine readable without routine console noise`() {
        rootDir.resolve("version.properties").writeText("")

        val versions = PlatformVersionsFacade.decide(
            rootDir = rootDir.toFile(),
            gradleVersion = "9.5.0",
            systemProperties = temurinProperties,
        )

        assertEquals("2.3.20", versions.kotlinVersion)
        assertEquals("2.3.11", versions.kspVersion)
        assertEquals("8.10.0", versions.minimumAgpVersion)
        assertTrue(
            versions.agpRequirements.any {
                it.minimumVersion == "8.10.0" && it.source == "KSP 2.3.11"
            },
        ) { "the KSP lower boundary must remain available for selection and failure diagnostics" }
        assertTrue(versions.versionInfo.none { it.startsWith("Minimum: ") })
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

    @Test
    fun `central IDE floor is enforced when the consumer omits or lowers it`() {
        val oldAndroidStudio = SystemProperties(
            mapOf(
                "idea.paths.selector" to "AndroidStudio2023.3",
                "idea.version" to "2023.3",
                "idea.vendor.name" to "Google",
                "os.name" to "Linux",
            )::get
        )

        listOf(
            "",
            "MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION=2023.3",
        ).forEach { consumerProperties ->
            rootDir.resolve("version.properties").writeText(consumerProperties)
            val error = assertThrows<IllegalStateException> {
                PlatformVersionsFacade.decide(
                    rootDir = rootDir.toFile(),
                    gradleVersion = "9.3.0",
                    systemProperties = oldAndroidStudio,
                )
            }

            assertTrue(error.message!!.contains("effective minimum requirement 2025.2.3"))
            assertTrue(error.message!!.contains("central compatibility range"))
        }
    }

    @Test
    fun `Gradle project properties select latest Quail 3 AGP patch`() {
        rootDir.resolve("version.properties").writeText("")

        val versions = PlatformVersionsFacade.decide(
            rootDir = rootDir.toFile(),
            gradleVersion = "9.5.0",
            gradleProjectProperties = mapOf(
                "idea.paths.selector" to "AndroidStudio2026.1",
                "idea.version" to "2026.1",
                "idea.vendor.name" to "Google",
                "android.studio.version" to "261.26222.65.2613.16025427",
                "android.ide.strict.version" to "2026.1.3.8",
            ),
        )

        assertEquals("2026.1.3.8", versions.platform.version)
        assertEquals("9.3.2", versions.agpVersion)
        assertTrue(
            versions.versionInfo.any {
                it.contains("com.android.tools.build:gradle:9.3.2") &&
                        it.contains(Identifier.NEAREST_LOWER_MATCHED_SUFFIX)
            },
        )
    }
}
