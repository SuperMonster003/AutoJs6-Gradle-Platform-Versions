import fs from 'node:fs/promises';
import path from 'node:path';
import { isDeepStrictEqual } from 'node:util';
import { DATA_DIR } from './paths.mjs';

export const CHECK_MODE_EXIT_CODE = 2;

export function isCheckMode() {
    return process.env.PLATFORM_DATA_MODE === 'check';
}

function unescapeProperty(value) {
    return value.replace(/\\u([0-9a-f]{4})|\\(.)/gi, (_, unicode, escaped) => {
        if (unicode) return String.fromCharCode(Number.parseInt(unicode, 16));
        return { t: '\t', n: '\n', r: '\r', f: '\f' }[escaped] ?? escaped;
    });
}

function splitProperty(line) {
    let escaped = false;
    for (let index = 0; index < line.length; index += 1) {
        const character = line[index];
        if (escaped) {
            escaped = false;
            continue;
        }
        if (character === '\\') {
            escaped = true;
            continue;
        }
        if (character === '=' || character === ':') {
            return [ line.slice(0, index), line.slice(index + 1) ];
        }
    }
    return [ line, '' ];
}

export function parseProperties(text) {
    const result = new Map();
    for (const rawLine of String(text || '').split(/\r?\n/)) {
        const line = rawLine.trim();
        if (!line || line.startsWith('#') || line.startsWith('!')) continue;
        const [ rawKey, rawValue ] = splitProperty(line);
        result.set(unescapeProperty(rawKey.trim()), unescapeProperty(rawValue.trimStart()));
    }
    return result;
}

function escapeProperty(value, isKey) {
    let output = '';
    for (const character of String(value)) {
        const code = character.codePointAt(0);
        if (character === '\\') output += '\\\\';
        else if (character === '\t') output += '\\t';
        else if (character === '\n') output += '\\n';
        else if (character === '\r') output += '\\r';
        else if (character === '\f') output += '\\f';
        else if (code < 0x20 || code > 0x7e) output += `\\u${code.toString(16).padStart(4, '0').slice(-4)}`;
        else if (character === '=' || character === ':' || (isKey && character === ' ')) output += `\\${character}`;
        else if ((character === '#' || character === '!') && (isKey || output.length === 0)) output += `\\${character}`;
        else if (character === ' ' && !isKey && output.length === 0) output += '\\ ';
        else output += character;
    }
    return output;
}

function timestamp(date = new Date()) {
    const parts = new Intl.DateTimeFormat('en-US', {
        timeZone: 'Asia/Shanghai',
        weekday: 'short',
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hourCycle: 'h23',
        timeZoneName: 'short',
        year: 'numeric',
    }).formatToParts(date).reduce((result, part) => {
        result[part.type] = part.value;
        return result;
    }, {});
    return `#${parts.weekday} ${parts.month} ${parts.day} ${parts.hour}:${parts.minute}:${parts.second} ${parts.timeZoneName} ${parts.year}`;
}

function mapsEqual(left, right) {
    if (left.size !== right.size) return false;
    return [ ...left ].every(([ key, value ]) => right.get(key) === value);
}

function listsEqual(left, right) {
    return left.length === right.length && left.every((value, index) => value === right[index]);
}

function printMapDiff(original, updated) {
    for (const [ key, value ] of original) {
        if (!updated.has(key)) console.log(`  - ${key}=${value}`);
        else if (updated.get(key) !== value) console.log(`  ~ ${key}: ${value} -> ${updated.get(key)}`);
    }
    for (const [ key, value ] of updated) {
        if (!original.has(key)) console.log(`  + ${key}=${value}`);
    }
}

function printListDiff(original, updated) {
    const originalSet = new Set(original);
    const updatedSet = new Set(updated);
    original.filter((value) => !updatedSet.has(value)).forEach((value) => console.log(`  - ${value}`));
    updated.filter((value) => !originalSet.has(value)).forEach((value) => console.log(`  + ${value}`));
}

function normalizedEntries(entries) {
    const map = new Map();
    for (const [ rawKey, rawValue ] of entries) {
        const key = String(rawKey).trim();
        const value = String(rawValue).trim();
        if (!key || !value) throw new Error(`Refusing to generate an empty property entry: ${rawKey}=${rawValue}`);
        if (map.has(key) && map.get(key) !== value) {
            throw new Error(`Conflicting generated values for ${key}: ${map.get(key)} vs ${value}`);
        }
        map.set(key, value);
    }
    if (map.size === 0) throw new Error('Refusing to replace a dataset with an empty map');
    return map;
}

export async function readDataProperties(name) {
    const file = path.join(DATA_DIR, name.endsWith('.properties') ? name : `${name}.properties`);
    return parseProperties(await fs.readFile(file, 'utf8'));
}

/**
 * Compares and optionally replaces a generated `.properties` dataset. `render`
 * may inject provenance/date comments without affecting semantic comparison.
 */
export async function updateProperties(name, entries, options = {}) {
    const fileName = name.endsWith('.properties') ? name : `${name}.properties`;
    const file = path.join(DATA_DIR, fileName);
    const originalText = await fs.readFile(file, 'utf8');
    const original = parseProperties(originalText);
    const updated = normalizedEntries(entries);
    const sortedEntries = options.sort ? [ ...updated ].sort(options.sort) : [ ...updated ];
    const body = options.render
        ? options.render(sortedEntries)
        : sortedEntries.map(([ key, value ]) => `${escapeProperty(key, true)}=${escapeProperty(value, false)}`);
    const originalBody = originalText.split(/\r?\n/).slice(1).join('\n').trimEnd();
    const generatedBody = body.join('\n').trimEnd();
    const renderedBodyMatches = !options.compareRenderedBody || originalBody === generatedBody;
    if (mapsEqual(original, updated) && renderedBodyMatches) return false;

    console.log(`[${fileName}] ${isCheckMode() ? 'Update available' : 'Updated'}${options.label ? ` (${options.label})` : ''}`);
    printMapDiff(original, updated);

    if (!isCheckMode()) {
        await fs.writeFile(file, `${timestamp()}\n${body.join('\n')}\n`, 'utf8');
    }
    return true;
}

export async function updateList(name, values, options = {}) {
    const fileName = name.endsWith('.list') ? name : `${name}.list`;
    const file = path.join(DATA_DIR, fileName);
    const original = (await fs.readFile(file, 'utf8'))
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith('#') && !line.startsWith('!'));
    const unique = [ ...new Set(values.map((value) => String(value).trim()).filter(Boolean)) ];
    const updated = options.sort ? unique.sort(options.sort) : unique;
    if (updated.length === 0) throw new Error('Refusing to replace a dataset with an empty list');
    if (listsEqual(original, updated)) return false;

    console.log(`[${fileName}] ${isCheckMode() ? 'Update available' : 'Updated'}${options.label ? ` (${options.label})` : ''}`);
    printListDiff(original, updated);
    if (!isCheckMode()) await fs.writeFile(file, `${timestamp()}\n${updated.join('\n')}\n`, 'utf8');
    return true;
}

export async function updateJson(name, value, options = {}) {
    const fileName = name.endsWith('.json') ? name : `${name}.json`;
    const file = path.join(DATA_DIR, fileName);
    let original;
    try {
        original = JSON.parse(await fs.readFile(file, 'utf8'));
    } catch (error) {
        if (error?.code !== 'ENOENT') throw error;
    }
    if (isDeepStrictEqual(original, value)) return false;

    console.log(`[${fileName}] ${isCheckMode() ? 'Update available' : 'Updated'}${options.label ? ` (${options.label})` : ''}`);
    if (!isCheckMode()) await fs.writeFile(file, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
    return true;
}
