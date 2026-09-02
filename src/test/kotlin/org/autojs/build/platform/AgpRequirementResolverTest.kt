package org.autojs.build.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AgpRequirementResolverTest {

    private val dataSource = DataSource(localDataDir = null)

    private fun resolver(vararg properties: Pair<String, String>) =
        AgpRequirementResolver(dataSource, mapOf(*properties))

    @Test
    fun `derives the official minimum AGP from compileSdk and targetSdk`() {
        val resolver = resolver(
            AgpRequirementResolver.COMPILE_SDK_PROPERTY to "36",
            AgpRequirementResolver.TARGET_SDK_PROPERTY to "35",
        )
        val requirements = resolver.projectRequirements()

        assertEquals("8.9.1", resolver.highestMinimum(requirements))
        assertTrue(requirements.any { it.source == "compileSdk 36" && it.minimumVersion == "8.9.1" })
        assertTrue(requirements.any { it.source == "targetSdk 35" && it.minimumVersion == "8.6.0" })
    }

    @Test
    fun `an explicit project minimum remains a lower boundary rather than an exact pin`() {
        val resolver = resolver(
            AgpRequirementResolver.COMPILE_SDK_PROPERTY to "36",
            AgpRequirementResolver.MIN_SUPPORTED_AGP_PROPERTY to "9.1.1",
        )

        assertEquals("9.1.1", resolver.highestMinimum(resolver.projectRequirements()))
    }

    @Test
    fun `API levels older than the official table add no lower boundary`() {
        assertEquals(
            emptyList<AgpRequirement>(),
            resolver(AgpRequirementResolver.TARGET_SDK_PROPERTY to "29").projectRequirements(),
        )
    }

    @Test
    fun `a newer unknown API fails instead of silently understating its AGP requirement`() {
        val error = assertThrows<IllegalStateException> {
            resolver(AgpRequirementResolver.COMPILE_SDK_PROPERTY to "99").projectRequirements()
        }
        assertTrue(error.message!!.contains("newest known Android API level"))
        assertTrue(error.message!!.contains(AgpRequirementResolver.MIN_SUPPORTED_AGP_PROPERTY))
    }

    @Test
    fun `an explicit minimum supports preview or newer API levels`() {
        val resolver = resolver(
            AgpRequirementResolver.COMPILE_SDK_PROPERTY to "99",
            AgpRequirementResolver.MIN_SUPPORTED_AGP_PROPERTY to "9.4.0",
        )

        assertEquals("9.4.0", resolver.highestMinimum(resolver.projectRequirements()))
    }
}
