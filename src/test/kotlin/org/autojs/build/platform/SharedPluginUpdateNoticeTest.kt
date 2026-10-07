package org.autojs.build.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SharedPluginUpdateNoticeTest {
    @TempDir lateinit var directory: Path

    private fun checkout(name: String, version: String?): File {
        val root = directory.resolve(name).toFile().apply { mkdirs() }
        version?.let { root.resolve("version.properties").writeText("VERSION_BUILD=1\nVERSION_NAME=$it\n") }
        return root
    }

    private fun consumer(): File = directory.resolve("AutoJs6-Plugin-Sample").toFile().apply { mkdirs() }

    @Test fun `a newer sibling checkout is reported once as a footer line`() {
        checkout(SharedPluginUpdateNotice.SIBLING_DIRECTORY_NAME, "1.9.0")
        val notice = SharedPluginUpdateNotice.detect(consumer(), emptyMap(), current = "1.8.4")
        assertNotNull(notice)
        assertTrue(notice!!.contains("1.8.4 in use") && notice.contains("1.9.0 available"), notice)
    }

    @Test fun `equal older snapshot and missing checkouts stay silent`() {
        checkout(SharedPluginUpdateNotice.SIBLING_DIRECTORY_NAME, "1.8.4")
        assertNull(SharedPluginUpdateNotice.detect(consumer(), emptyMap(), current = "1.8.4"))
        assertNull(SharedPluginUpdateNotice.detect(consumer(), emptyMap(), current = "1.8.10"))
        assertNull(SharedPluginUpdateNotice.detect(consumer(), emptyMap(), current = "1.8.0-SNAPSHOT"))
        checkout(SharedPluginUpdateNotice.SIBLING_DIRECTORY_NAME, "2.0.0-SNAPSHOT")
        assertNull(SharedPluginUpdateNotice.detect(consumer(), emptyMap(), current = "1.8.4"))
        assertNull(SharedPluginUpdateNotice.detect(consumer(), emptyMap(), current = null))
        assertNull(SharedPluginUpdateNotice.detect(directory.resolve("elsewhere/consumer").toFile(), emptyMap(), current = "1.8.4"))
    }

    @Test fun `the checkout property overrides the sibling directory`() {
        checkout(SharedPluginUpdateNotice.SIBLING_DIRECTORY_NAME, "1.8.4")
        val custom = checkout("custom-checkout", "1.8.5")
        val properties = mapOf(SharedPluginUpdateNotice.LOCAL_CHECKOUT_PROPERTY to custom.path)
        assertTrue(SharedPluginUpdateNotice.detect(consumer(), properties, current = "1.8.4")!!.contains("1.8.5 available"))
    }

    @Test fun `numeric comparison pads shorter versions`() {
        assertEquals(0, SharedPluginUpdateNotice.compareVersions("1.8", "1.8.0"))
        assertTrue(SharedPluginUpdateNotice.compareVersions("1.10.0", "1.9.9") > 0)
        assertTrue(SharedPluginUpdateNotice.compareVersions("1.8.4", "1.8.10") < 0)
    }
}
