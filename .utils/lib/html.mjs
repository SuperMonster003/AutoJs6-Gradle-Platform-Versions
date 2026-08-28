import * as cheerio from 'cheerio';

export const normalizeText = (value) => String(value || '').replace(/\s+/g, ' ').trim();

function cellText($, cell) {
    const copy = $(cell).clone();
    copy.find('sup').remove();
    return normalizeText(copy.text());
}

/**
 * Returns cell arrays from every table whose headers satisfy all supplied
 * patterns. Footnote markers are removed before cell text is returned.
 */
export function tableRows(html, requiredHeaderPatterns) {
    const $ = cheerio.load(html);
    const rows = [];

    $('table').each((_, table) => {
        const headers = $(table).find('th').map((__, th) => cellText($, th)).get();
        const matches = requiredHeaderPatterns.every((pattern) => headers.some((header) => pattern.test(header)));
        if (!matches) return;

        const candidates = $(table).find('tbody tr').length > 0
            ? $(table).find('tbody tr')
            : $(table).find('tr');
        candidates.each((__, row) => {
            const cells = $(row).find('td').map((___, cell) => cellText($, cell)).get();
            if (cells.length > 0) rows.push(cells);
        });
    });

    if (rows.length === 0) {
        throw new Error(`No table rows found for headers: ${requiredHeaderPatterns.join(', ')}`);
    }
    return rows;
}
