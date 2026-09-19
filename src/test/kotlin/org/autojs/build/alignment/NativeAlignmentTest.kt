package org.autojs.build.alignment

import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class NativeAlignmentTest {
    @TempDir lateinit var directory: Path

    @Test fun `test assemblies cannot schedule APK alignment checks`() {
        for (task in listOf("assembleAppDebugUnitTest", "assembleInrtReleaseUnitTest",
            "assembleDebugAndroidTest", "assembleDebugTestFixtures", "assemble", "testDebugUnitTest")) {
            assertFalse(isApkAssembleTask(task), task)
        }
        for (task in listOf("assembleDebug", "assembleRelease", "assembleAppDebug", "assembleInrtRelease", "assembleApp")) {
            assertTrue(isApkAssembleTask(task), task)
        }
    }

    private fun elf(align: Long = 16384, is64: Boolean = true, order: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteArray {
        val bytes = ByteArray(160)
        val data = ByteBuffer.wrap(bytes).order(order)
        data.put(byteArrayOf(0x7f, 0x45, 0x4c, 0x46, if (is64) 2 else 1, if (order == ByteOrder.LITTLE_ENDIAN) 1 else 2, 1))
        data.putShort(18, if (is64) 183 else 40)
        data.putInt(20, 1)
        if (is64) {
            data.putLong(32, 64); data.putShort(54, 56); data.putShort(56, 1)
            data.putInt(64, 1); data.putLong(112, align)
        } else {
            data.putInt(28, 52); data.putShort(42, 32); data.putShort(44, 1)
            data.putInt(52, 1); data.putInt(80, align.toInt())
        }
        return bytes
    }

    private fun apk(name: String = "sample.apk", data: ByteArray = elf(), stored: Boolean = false,
                    alignedZip: Boolean = false, entryName: String = "lib/arm64-v8a/libtest.so"): File {
        val file = directory.resolve(name).toFile()
        file.parentFile.mkdirs()
        ZipOutputStream(file.outputStream()).use { zip ->
            val entry = ZipEntry(entryName)
            if (stored) {
                entry.method = ZipEntry.STORED; entry.size = data.size.toLong()
                entry.crc = CRC32().apply { update(data) }.value
            }
            if (alignedZip) {
                val extra = ByteArray(16384 - 30 - entryName.toByteArray().size)
                ByteBuffer.wrap(extra).order(ByteOrder.LITTLE_ENDIAN).apply {
                    putShort(0, 0xd935.toShort()); putShort(2, (extra.size - 4).toShort())
                }
                entry.extra = extra
            }
            zip.putNextEntry(entry); zip.write(data); zip.closeEntry()
        }
        return file
    }

    @Test fun `parses ELF32 and ELF64 in either byte order`() {
        for (is64 in listOf(false, true)) for (order in listOf(ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN)) {
            assertEquals(16384, ElfLoadAlignment.parse(elf(is64 = is64, order = order)).minLoadAlign)
        }
    }

    @Test fun `rejects malformed headers alignments and truncated program tables`() {
        for (bytes in listOf(ByteArray(64), elf().copyOf(80), elf(12288), elf(0),
            elf().also { ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).putLong(32, Long.MAX_VALUE) },
            elf().also { it[5] = 3 }, elf().also { it[64] = 0 })) {
            assertThrows<IllegalArgumentException> { ElfLoadAlignment.parse(bytes) }
        }
    }

    @Test fun `compressed APK needs ELF alignment but not ZIP alignment`() {
        assertTrue(NativeAlignmentScanner().scanApk(apk()).single().reasons.isEmpty())
        assertTrue(NativeAlignmentScanner().scanApk(apk("bad.apk", elf(4096))).single().reasons.any { "PT_LOAD alignment" in it })
    }

    @Test fun `stored APK checks actual local data offset including extra fields`() {
        assertTrue(NativeAlignmentScanner().scanApk(apk(stored = true)).single().reasons.any { "ZIP data offset" in it })
        val row = NativeAlignmentScanner().scanApk(apk("aligned.apk", stored = true, alignedZip = true)).single()
        assertEquals(16384, row.dataOffset)
        assertTrue(row.reasons.isEmpty())
    }

    @Test fun `32 bit alignment is advisory and pure JVM assertion rejects any ABI`() {
        val file = apk(data = elf(4096, false), entryName = "lib/armeabi-v7a/test.so")
        assertTrue(NativeAlignmentScanner().scanApk(file).single().reasons.isEmpty())
        assertTrue(NativeAlignmentScanner(expectNoNativeLibraries = true).scanApk(file).single().reasons.isNotEmpty())
        assertTrue(NativeAlignmentScanner().scanApk(apk("disguised.apk", elf(4096), entryName = "lib/x86/test.so")).single().reasons.isNotEmpty())
    }

    @Test fun `nested IMY payload and extensionless executable are inspected by magic`() {
        val inner = apk("payload.imy", elf(4096), entryName = "lib-dynload/ssl.so")
        val outer = apk("assets.zip", inner.readBytes(), entryName = "assets/chaquopy/stdlib.imy")
        assertTrue(NativeAlignmentScanner().scanPayload(outer).single().reasons.isNotEmpty())
        val executable = directory.resolve("node").toFile().apply { writeBytes(elf()) }
        assertTrue(NativeAlignmentScanner().scanPayload(executable).single().reasons.isEmpty())
    }

    @Test fun `missing APK and CI bypass fail with fresh failure reports`() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        project.pluginManager.apply(NativeAlignmentPlugin::class.java)
        val task = project.tasks.getByName("verifyNativePageAlignment") as VerifyNativePageAlignment
        assertThrows<GradleException> { task.verify() }
        assertTrue(task.reportFile.get().asFile.readText().contains("No APK found"))
        task.skipVerification.set(true); task.ci.set(true)
        assertThrows<GradleException> { task.verify() }
        assertTrue(task.reportFile.get().asFile.readText().contains("forbidden in CI"))
    }

    @Test fun `embedded native extensions participate in APK verification when enabled`() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        project.pluginManager.apply(NativeAlignmentPlugin::class.java)
        val inner = apk("extension.imy", elf(4096), entryName = "ssl.so")
        val file = apk("python.apk", inner.readBytes(), entryName = "assets/chaquopy/stdlib.imy")
        val task = project.tasks.getByName("verifyNativePageAlignment") as VerifyNativePageAlignment
        task.apkFiles.from(file)
        task.verify()
        task.scanEmbeddedPayloads.set(true)
        assertThrows<GradleException> { task.verify() }
        assertTrue(task.reportFile.get().asFile.readText().contains("ssl.so"))
    }

    @Test fun `custom filenames use metadata and missing split cannot reuse stale APK`() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        project.pluginManager.apply(NativeAlignmentPlugin::class.java)
        val file = apk("build/outputs/apk/app/debug/custom-release-looking-name.apk")
        val metadata = File(file.parentFile, "output-metadata.json")
        metadata.writeText("""{"variantName":"appDebug","elements":[{"outputFile":"${file.name}"}]}""")
        val task = project.tasks.getByName("verifyNativePageAlignment") as VerifyNativePageAlignment
        task.variant.set("AppDebug")
        task.verify()
        val result = JsonSlurper().parse(task.reportFile.get().asFile) as Map<*, *>
        assertEquals(true, (result["summary"] as Map<*, *>)["ok"])
        metadata.writeText("""{"variantName":"appDebug","elements":[{"outputFile":"missing-split.apk"}]}""")
        assertThrows<GradleException> { task.verify() }
    }
}
