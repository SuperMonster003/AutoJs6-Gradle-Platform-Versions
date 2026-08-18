package org.autojs.build.platform

/**
 * A boxed console block, as printed by the AutoJs6 build scripts.
 *
 * ```
 * ==========================================
 * Version information for IDE platform ...
 * ------------------------------------------
 * Platform: IntelliJ IDEA | 2026.2
 * ==========================================
 * ```
 */
class Formatted(
    title: String,
    private val contents: Collection<String> = emptyList(),
    subtitle: String? = null,
    footers: Collection<String> = emptyList(),
    notes: Collection<String> = emptyList(),
) {

    private val lines: List<String> = run {
        val elements = mutableListOf<String>()
        subtitle?.let { elements.add(it) }
        elements.addAll(contents)
        elements.addAll(notes)
        val maxLength = elements.plus(title).plus(footers).maxOf { it.length }

        listOfNotNull(
            "=".repeat(maxLength),
            title,
            subtitle,
            "-".repeat(maxLength).takeUnless { contents.isEmpty() },
            *contents.toTypedArray(),
            "-".repeat(maxLength).takeUnless { footers.isEmpty() },
            *footers.toTypedArray(),
            // Notes sit below the footers, past a rule of their own, so a decision that
            // needs explaining does not break up the list of decisions.
            "-".repeat(maxLength).takeUnless { notes.isEmpty() },
            *notes.toTypedArray(),
            "=".repeat(maxLength),
            "",
        )
    }

    val text: String get() = lines.joinToString("\n")

    fun print(contentsMatters: Boolean = false) {
        lines.forEach { if (!contentsMatters || contents.isNotEmpty()) println(it) }
    }

}
