package org.autojs.build.alignment

import java.io.File
import java.io.FilterInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class NativeAlignmentScanner(
    private val pageSize: Long = 16384,
    private val strictAbis: Set<String> = setOf("arm64-v8a", "x86_64"),
    private val expectNoNativeLibraries: Boolean = false,
    private val checkRelro: Boolean = false,
) {
    data class Entry(val name: String, val abi: String, val compressed: Boolean?, val dataOffset: Long?,
                     val minLoadAlign: Long?, val reasons: List<String>) {
        fun report(): Map<String, Any?> = linkedMapOf("name" to name, "abi" to abi, "compressed" to compressed,
            "dataOffset" to dataOffset, "minLoadAlign" to minLoadAlign, "ok" to reasons.isEmpty(), "reasons" to reasons)
    }

    init { require(pageSize > 0 && pageSize and (pageSize - 1) == 0L) { "pageSize must be a positive power of two" } }

    fun scanApk(file: File): List<Entry> {
        val offsets = ApkZipOffsets.read(file)
        return ZipFile(file).use { zip ->
            zip.entries().asSequence().filter { !it.isDirectory && it.name.startsWith("lib/") && it.name.endsWith(".so") }
                .map { entry ->
                    zip.getInputStream(entry).use { input ->
                        inspect(entry.name, input.readNBytes(ElfLoadAlignment.PREFIX_SIZE), entry.method != ZipEntry.STORED,
                            offsets.getValue(entry.name), entry.name.split('/').getOrNull(1))
                    }
                }.toList()
        }
    }

    /** Recursively inspects extracted executables and ZIP/IMY/AAR payloads by magic, including extensionless ELF. */
    fun scanPayload(file: File): List<Entry> = if (file.isDirectory) {
        file.walkTopDown().filter { it.isFile }.flatMap { scanPayload(it) }.toList()
    } else file.inputStream().use { scanStream(it, file.path, 0) }

    private fun scanStream(input: InputStream, name: String, depth: Int): List<Entry> {
        require(depth <= 8) { "Native archive nesting exceeds 8: $name" }
        val magic = input.readNBytes(4)
        if (magic.contentEquals(byteArrayOf(0x7f, 0x45, 0x4c, 0x46))) {
            val prefix = magic + input.readNBytes(ElfLoadAlignment.PREFIX_SIZE - 4)
            return listOf(inspect(name, prefix, null, null, null))
        }
        if (magic.contentEquals(byteArrayOf(0x50, 0x4b, 3, 4))) {
            val entries = mutableListOf<Entry>()
            // Do not close the caller's stream: it may be a parent ZIP entry.
            val borrowed = object : FilterInputStream(input) { override fun close() = Unit }
            ZipInputStream(SequenceInputStream(magic.inputStream(), borrowed)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (!entry.isDirectory) entries += scanStream(zip, "$name!/${entry.name}", depth + 1)
                    zip.closeEntry()
                }
            }
            return entries
        }
        require(!name.endsWith(".so")) { "Native payload is not ELF: $name" }
        return emptyList()
    }

    private fun inspect(name: String, prefix: ByteArray, compressed: Boolean?, offset: Long?, declaredAbi: String?): Entry {
        val parsed = runCatching { ElfLoadAlignment.parse(prefix) }
        val elf = parsed.getOrNull()
        val abi = declaredAbi ?: elf?.abi ?: "unknown"
        val reasons = mutableListOf<String>()
        if (expectNoNativeLibraries) reasons += "Expected no native libraries"
        // Malformed ELF and disguised 64-bit entries must never silently pass.
        if (elf == null) reasons += "Invalid ELF: ${parsed.exceptionOrNull()?.message}"
        else {
            if (declaredAbi != null && declaredAbi != elf.abi) reasons += "ABI path $declaredAbi disagrees with ELF ${elf.abi}"
            if (abi in strictAbis || elf.abi in strictAbis) {
                if (elf.minLoadAlign < pageSize) reasons += "PT_LOAD alignment ${elf.minLoadAlign} < $pageSize"
                if (elf.loads.any { it.offset % pageSize != it.address % pageSize }) reasons += "PT_LOAD is not congruent at $pageSize bytes"
                if (compressed == false && (offset == null || offset % pageSize != 0L)) reasons += "Uncompressed ZIP data offset $offset is not $pageSize aligned"
                if (checkRelro && elf.relroEnds.any { it % pageSize != 0L }) reasons += "PT_GNU_RELRO end is not $pageSize aligned"
            }
        }
        return Entry(name, abi, compressed, offset, elf?.minLoadAlign, reasons)
    }
}
