package org.autojs.build.alignment

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile

/** Minimal bounded binary-XML reader for application packaging and native contract metadata. */
object ApkManifestAlignment {
    const val META_DATA = "org.autojs.plugin.contract.NATIVE_PAGE_ALIGNMENT"
    data class Info(val extractNativeLibs: Boolean?, val declaredAlignment: Long?, val pageSizeCompatDeclared: Boolean)

    fun read(apk: File): Info = ZipFile(apk).use { zip ->
        val entry = zip.getEntry("AndroidManifest.xml") ?: return Info(null, null, false)
        val bytes = zip.getInputStream(entry).use { it.readNBytes(4 * 1024 * 1024 + 1) }
        require(bytes.size <= 4 * 1024 * 1024) { "Manifest exceeds 4 MiB" }
        parse(bytes)
    }

    fun parse(bytes: ByteArray): Info {
        val data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        fun u16(at: Int) = data.getShort(at).toInt() and 0xffff
        fun u32(at: Int) = data.getInt(at).toLong() and 0xffffffffL
        require(bytes.size >= 8 && u16(0) == 3 && u32(4) == bytes.size.toLong()) { "Invalid binary Android manifest" }
        var strings = emptyList<String>()
        fun string(index: Int): String? = if (index == -1) null else strings.getOrNull(index)
            ?: throw IllegalArgumentException("Invalid binary XML string index: $index")
        var extract: Boolean? = null
        var compat = false
        val declarations = mutableSetOf<Long>()
        var offset = u16(2)
        while (offset < bytes.size) {
            require(offset >= 8 && offset + 8 <= bytes.size) { "Truncated manifest chunk" }
            val type = u16(offset)
            val header = u16(offset + 2)
            val size = u32(offset + 4)
            require(header >= 8 && size >= header && size <= bytes.size - offset) { "Invalid manifest chunk size" }
            val end = offset + size.toInt()
            if (type == 1) {
                require(header >= 28) { "Invalid string pool" }
                val count = u32(offset + 8)
                val utf8 = u32(offset + 16) and 0x100L != 0L
                val start = u32(offset + 20)
                require(count * 4 <= size - header && start <= size) { "Invalid string pool offsets" }
                strings = List(count.toInt()) { index ->
                    val position = start + u32(offset + header + index * 4)
                    require(position < size) { "String outside string pool" }
                    var cursor = offset + position.toInt()
                    fun length8(): Int {
                        require(cursor < end) { "Truncated UTF-8 length" }
                        val first = bytes[cursor++].toInt() and 0xff
                        if (first and 0x80 == 0) return first
                        require(cursor < end) { "Truncated UTF-8 length" }
                        return ((first and 0x7f) shl 8) or (bytes[cursor++].toInt() and 0xff)
                    }
                    val length: Int
                    if (utf8) { length8(); length = length8() }
                    else {
                        require(cursor + 2 <= end) { "Truncated UTF-16 length" }
                        val first = u16(cursor); cursor += 2
                        val chars = if (first and 0x8000 == 0) first else {
                            require(cursor + 2 <= end) { "Truncated UTF-16 length" }
                            (((first and 0x7fff) shl 16) or u16(cursor)).also { cursor += 2 }
                        }
                        require(chars <= (end - cursor) / 2) { "UTF-16 string exceeds pool" }
                        length = chars * 2
                    }
                    require(length <= end - cursor) { "String exceeds pool" }
                    String(bytes, cursor, length, if (utf8) Charsets.UTF_8 else Charsets.UTF_16LE)
                }
            } else if (type == 0x0102) {
                require(header >= 16 && size >= header + 20) { "Invalid start element" }
                val ext = offset + header
                val tag = string(data.getInt(ext + 4))
                val attributeStart = u16(ext + 8)
                val attributeSize = u16(ext + 10)
                val count = u16(ext + 12)
                require(attributeStart >= 20 && attributeSize >= 20 &&
                    attributeStart.toLong() + count.toLong() * attributeSize <= end - ext) { "Invalid XML attributes" }
                val attributes = (0 until count).associate { index ->
                    val at = ext + attributeStart + index * attributeSize
                    val name = string(data.getInt(at + 4)) ?: error("Unnamed XML attribute")
                    val raw = data.getInt(at + 8)
                    val value = if (raw != -1) string(raw) else when (bytes[at + 15].toInt() and 0xff) {
                        3 -> string(data.getInt(at + 16))
                        0x10, 0x11 -> u32(at + 16).toString()
                        0x12 -> (data.getInt(at + 16) != 0).toString()
                        else -> null
                    }
                    name to value
                }
                if (tag == "application") {
                    extract = attributes["extractNativeLibs"]?.toBooleanStrictOrNull()
                    compat = "pageSizeCompat" in attributes
                }
                if (tag == "meta-data" && attributes["name"] == META_DATA) {
                    val alignment = attributes["value"]?.toLongOrNull() ?: error("Invalid $META_DATA value")
                    require(alignment >= 0 && (alignment == 0L || alignment and (alignment - 1) == 0L)) { "Invalid declared native alignment: $alignment" }
                    declarations += alignment
                }
            }
            offset = end
        }
        require(declarations.size <= 1) { "Conflicting $META_DATA declarations" }
        return Info(extract, declarations.singleOrNull(), compat)
    }
}
