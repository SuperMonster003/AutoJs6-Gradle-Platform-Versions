/**
 * Every language writes its punctuation in ASCII. Matches any non-ASCII
 * character in a Unicode punctuation category (ideographic comma and full
 * stop, corner brackets, the katakana middle dot, dashes, curly quotes, the
 * ellipsis, guillemets, Arabic and Spanish marks, ...), plus arrows, the
 * ideographic space and fullwidth forms. The middle dot U+00B7 stays allowed
 * as a value separator. Mirrors .python/check_translations.py; escapes keep
 * this file itself ASCII-only.
 */
export const NON_ASCII_PUNCTUATION =
    /[^\P{P}\x00-\x7f\u00b7]|[\u2190-\u21ff\u3000\uff01-\uff65]/u;
