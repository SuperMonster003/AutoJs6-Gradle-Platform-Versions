package org.autojs.build.alignment

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Uses central-directory offsets, so signatures/data descriptors cannot look like local entries. */
internal object ApkZipOffsets {
    fun read(file: File): Map<String, Long> = RandomAccessFile(file, "r").use { input ->
        fun u16(): Int = java.lang.Short.reverseBytes(input.readShort()).toInt() and 0xffff
        fun u32(): Long = Integer.reverseBytes(input.readInt()).toLong() and 0xffffffffL
        val tailSize = minOf(input.length(), 65557L).toInt()
        require(tailSize >= 22) { "Truncated ZIP" }
        val tail = ByteArray(tailSize)
        input.seek(input.length() - tailSize)
        input.readFully(tail)
        val bytes = ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN)
        val end = (tailSize - 22 downTo 0).firstOrNull { at ->
            bytes.getInt(at) == 0x06054b50 && at + 22 + (bytes.getShort(at + 20).toInt() and 0xffff) == tailSize
        } ?: error("Missing ZIP end record")
        require(bytes.getShort(end + 4).toInt() == 0 && bytes.getShort(end + 6).toInt() == 0) { "Multi-disk ZIP unsupported" }
        val count = bytes.getShort(end + 10).toInt() and 0xffff
        val centralOffset = bytes.getInt(end + 16).toLong() and 0xffffffffL
        // APK signing tools also disallow ZIP64. Reject it explicitly rather than misreading offsets.
        require(count != 0xffff && centralOffset != 0xffffffffL) { "ZIP64 APK unsupported" }
        val offsets = linkedMapOf<String, Long>()
        input.seek(centralOffset)
        repeat(count) {
            require(u32() == 0x02014b50L) { "Invalid ZIP central directory" }
            input.skipBytes(4)
            val flags = u16()
            require(flags and 1 == 0) { "Encrypted ZIP entry unsupported" }
            input.skipBytes(18)
            val nameSize = u16()
            val extraSize = u16()
            val commentSize = u16()
            input.skipBytes(8)
            val localOffset = u32()
            require(localOffset != 0xffffffffL) { "ZIP64 entry unsupported" }
            val nameBytes = ByteArray(nameSize)
            input.readFully(nameBytes)
            val name = nameBytes.toString(if (flags and 0x800 != 0) Charsets.UTF_8 else charset("CP437"))
            input.skipBytes(extraSize + commentSize)
            val next = input.filePointer
            input.seek(localOffset)
            require(u32() == 0x04034b50L) { "Invalid local ZIP header: $name" }
            input.skipBytes(22)
            val localNameSize = u16()
            val localExtraSize = u16()
            val localName = ByteArray(localNameSize)
            input.readFully(localName)
            require(localName.contentEquals(nameBytes)) { "Local/central ZIP name mismatch: $name" }
            require(offsets.put(name, localOffset + 30L + localNameSize + localExtraSize) == null) { "Duplicate ZIP entry: $name" }
            input.seek(next)
        }
        offsets
    }
}
