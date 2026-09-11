package org.autojs.build.alignment

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ApkManifestAlignmentTest {
    private fun manifest(alignment: Int, compat: Boolean = false): ByteArray {
        val strings = listOf("application", "meta-data", "extractNativeLibs", "name", "value",
            ApkManifestAlignment.META_DATA, "pageSizeCompat", "http://schemas.android.com/apk/res/android")
        val payload = ByteArrayOutputStream()
        val offsets = strings.map { text ->
            val offset = payload.size()
            val bytes = text.toByteArray()
            payload.write(text.length); payload.write(bytes.size); payload.write(bytes); payload.write(0)
            offset
        }
        while (payload.size() % 4 != 0) payload.write(0)
        val pool = ByteBuffer.allocate(28 + offsets.size * 4 + payload.size()).order(ByteOrder.LITTLE_ENDIAN)
        pool.putShort(1); pool.putShort(28); pool.putInt(pool.capacity())
        pool.putInt(strings.size); pool.putInt(0); pool.putInt(0x100)
        pool.putInt(28 + offsets.size * 4); pool.putInt(0)
        offsets.forEach { pool.putInt(it) }; pool.put(payload.toByteArray())
        fun node(name: Int, attributes: List<Triple<Int, Int, Int>>): ByteArray {
            val data = ByteBuffer.allocate(36 + attributes.size * 20).order(ByteOrder.LITTLE_ENDIAN)
            data.putShort(0x102); data.putShort(16); data.putInt(data.capacity()); data.putInt(1); data.putInt(-1)
            data.putInt(-1); data.putInt(name); data.putShort(20); data.putShort(20); data.putShort(attributes.size.toShort())
            data.putShort(0); data.putShort(0); data.putShort(0)
            attributes.forEach { (attribute, type, value) ->
                data.putInt(7); data.putInt(attribute); data.putInt(-1); data.putShort(8); data.put(0); data.put(type.toByte()); data.putInt(value)
            }
            return data.array()
        }
        val application = node(0, listOf(Triple(2, 0x12, -1)) + if (compat) listOf(Triple(6, 0x12, 1)) else emptyList())
        val metadata = node(1, listOf(Triple(3, 3, 5), Triple(4, 0x10, alignment)))
        val chunks = pool.array() + application + metadata
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).apply {
            putShort(3); putShort(8); putInt(chunks.size + 8)
        }.array() + chunks
    }

    @Test fun readsNativeContractAndPackagingFlags() {
        assertEquals(ApkManifestAlignment.Info(true, 16384, false), ApkManifestAlignment.parse(manifest(16384)))
        assertEquals(ApkManifestAlignment.Info(true, 0, true), ApkManifestAlignment.parse(manifest(0, true)))
    }

    @Test fun rejectsMalformedOrNonPowerOfTwoDeclarations() {
        assertThrows<IllegalArgumentException> { ApkManifestAlignment.parse(manifest(12288)) }
        assertThrows<IllegalArgumentException> { ApkManifestAlignment.parse(manifest(16384).copyOf(32)) }
    }
}
