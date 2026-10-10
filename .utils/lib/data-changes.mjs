import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { isDeepStrictEqual } from 'node:util';
import { DATA_CHANGE_MESSAGES } from './data-change-messages.mjs';
import { parseProperties } from './data-files.mjs';
import { DATA_DIR, PROJECT_DIR } from './paths.mjs';
import { compareVersions } from './versioning.mjs';

/** Changelog categories in the order the Markdown generator renders them. */
export const DATA_CHANGE_CATEGORIES = [ 'improvement', 'dependency' ];

/** Longest build list rendered in full before it is summarized. */
const MAX_LISTED_VERSIONS = 8;

// Snapshots ---------------------------------------------------------------

/** Reads every data file in a directory as a `Map<fileName, text>`. */
export function readDataSnapshotFromDirectory(dataDir = DATA_DIR) {
    const snapshot = new Map();
    for (const entry of fs.readdirSync(dataDir, { withFileTypes: true })) {
        if (entry.isFile()) snapshot.set(entry.name, fs.readFileSync(path.join(dataDir, entry.name), 'utf8'));
    }
    return snapshot;
}

/** Reads the data directory as it existed at a Git revision, e.g. the latest release tag. */
export function readDataSnapshotFromGit(ref, { rootDir = PROJECT_DIR, dataDir = DATA_DIR } = {}) {
    const git = (...args) => execFileSync('git', args, {
        cwd: rootDir,
        encoding: 'utf8',
        maxBuffer: 64 * 1024 * 1024,
        stdio: [ 'ignore', 'pipe', 'pipe' ],
    });
    try {
        git('rev-parse', '--verify', '--quiet', `${ref}^{commit}`);
    } catch {
        throw new Error(`Unknown baseline Git revision: ${ref}`);
    }

    const relativeDir = path.relative(rootDir, dataDir).split(path.sep).join('/');
    const snapshot = new Map();
    const files = git('ls-tree', '-r', '-z', '--name-only', ref, '--', `${relativeDir}/`).split('\0').filter(Boolean);
    for (const file of files) {
        const name = file.slice(relativeDir.length + 1);
        if (!name || name.includes('/')) continue;
        snapshot.set(name, git('show', `${ref}:${file}`));
    }
    return snapshot;
}

// Parsing helpers ---------------------------------------------------------

function parseList(text) {
    return String(text ?? '')
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith('#') && !line.startsWith('!'));
}

function parseJson(text) {
    return text === undefined ? undefined : JSON.parse(text);
}

function compareKeysDescending(left, right) {
    try {
        const difference = compareVersions(right, left);
        if (difference !== 0) return difference;
    } catch {
        // Non-version keys such as codename letters fall back to text order.
    }
    return String(left).localeCompare(String(right));
}

function sortedKeys(...maps) {
    return [ ...new Set(maps.flatMap((map) => [ ...map.keys() ])) ].sort(compareKeysDescending);
}

function releaseKind(from, to) {
    try {
        const difference = compareVersions(to, from);
        if (difference > 0) return 'upgrade';
        if (difference < 0) return 'downgrade';
    } catch {
        // Unparseable versions are reported as a neutral change.
    }
    return 'changed';
}

function normalizedText(text) {
    return String(text ?? '')
        .split(/\r?\n/)
        .map((line) => line.trimEnd())
        .filter((line) => !line.trimStart().startsWith('#'))
        .join('\n')
        .trim();
}

// Dataset describers ------------------------------------------------------

/**
 * Compares two `key -> version` maps of tracked releases. `display` turns a
 * map entry into the version string readers know, e.g. a full KSP tag.
 */
function describeReleaseMaps(before, after, { dataset, subject, display = (key, value) => value }) {
    return sortedKeys(before, after).flatMap((key) => {
        const previous = before.get(key);
        const next = after.get(key);
        if (previous === next) return [];
        if (previous === undefined) {
            return [ { dataset, type: 'release', subject, kind: 'added', to: display(key, next) } ];
        }
        if (next === undefined) {
            return [ { dataset, type: 'release', subject, kind: 'removed', from: display(key, previous) } ];
        }
        return [ {
            dataset,
            type: 'release',
            subject,
            kind: releaseKind(previous, next),
            from: display(key, previous),
            to: display(key, next),
        } ];
    });
}

function agpReleaseLines(text) {
    const lines = new Map();
    for (const version of parseList(text)) {
        const matched = /^(\d+)\.(\d+)(?:\.|$)/.exec(version);
        const line = matched ? `${matched[1]}.${matched[2]}` : version;
        lines.set(lines.has(line) ? version : line, version);
    }
    return lines;
}

function describeAgpReleases(before, after) {
    return describeReleaseMaps(agpReleaseLines(before), agpReleaseLines(after), {
        dataset: 'agp-releases',
        subject: 'agp',
    });
}

function isStandaloneKspKey(key) {
    return /^\d+\.\d+\.Z(?:-|$)/i.test(key);
}

function describeKspReleases(before, after) {
    return describeReleaseMaps(parseProperties(before), parseProperties(after), {
        dataset: 'ksp-releases',
        subject: 'ksp',
        // Legacy KSP tags are `<kotlin>-<ksp>`; KSP 2 tags stand alone under an `x.y.Z` key.
        display: (key, value) => (isStandaloneKspKey(key) ? value : `${key}-${value}`),
    });
}

function androidStudioDisplayName(metadata) {
    return String(metadata?.name ?? '').replace(/^Android Studio\s+/i, '').trim() || undefined;
}

function describeAndroidStudioLatestStable(beforeText, afterText) {
    const before = parseJson(beforeText);
    const after = parseJson(afterText);
    if (isDeepStrictEqual(before, after)) return [];

    const dataset = 'android-studio-latest-stable';
    const base = { dataset, type: 'release', subject: 'androidStudioStable' };
    if (before === undefined) {
        return [ { ...base, kind: 'added', to: after.version, detail: androidStudioDisplayName(after) } ];
    }
    if (after === undefined) {
        return [ { ...base, kind: 'removed', from: before.version, detail: androidStudioDisplayName(before) } ];
    }
    if (before.version !== after.version) {
        return [ {
            ...base,
            kind: releaseKind(before.version, after.version),
            from: before.version,
            to: after.version,
            detail: androidStudioDisplayName(after),
        } ];
    }
    return [ { dataset, type: 'androidStudioMetadata', version: after.version } ];
}

function describeMapEntries(dataset, before, after) {
    return sortedKeys(before, after).flatMap((key) => {
        const previous = before.get(key);
        const next = after.get(key);
        if (previous === next) return [];
        if (previous === undefined) return [ { dataset, type: 'mapping', kind: 'added', key, to: next } ];
        if (next === undefined) return [ { dataset, type: 'mapping', kind: 'removed', key, from: previous } ];
        return [ { dataset, type: 'mapping', kind: 'changed', key, from: previous, to: next } ];
    });
}

function mappingDescriber(dataset) {
    return (before, after) => describeMapEntries(dataset, parseProperties(before), parseProperties(after));
}

function describeAndroidStudioBuilds(beforeText, afterText) {
    const dataset = 'android-studio-build-version';
    const before = parseProperties(beforeText);
    const after = parseProperties(afterText);
    const versionsOnlyIn = (source, other) => [ ...new Set(
        [ ...source ].filter(([ build ]) => !other.has(build)).map(([ , version ]) => version),
    ) ].sort(compareKeysDescending);

    const changes = [];
    const added = versionsOnlyIn(after, before);
    const removed = versionsOnlyIn(before, after);
    if (added.length > 0) changes.push({ dataset, type: 'builds', kind: 'added', versions: added });
    if (removed.length > 0) changes.push({ dataset, type: 'builds', kind: 'removed', versions: removed });

    const shared = (map) => new Map([ ...map ].filter(([ build ]) => before.has(build) && after.has(build)));
    changes.push(...describeMapEntries(dataset, shared(before), shared(after)));
    return changes;
}

/**
 * Every generated dataset and the semantic describer for it, in the order the
 * release notes list them: tracked releases first, then compatibility maps.
 */
const DESCRIBERS = new Map([
    [ 'agp-releases.list', describeAgpReleases ],
    [ 'ksp-releases.properties', describeKspReleases ],
    [ 'android-studio-latest-stable.json', describeAndroidStudioLatestStable ],
    [ 'agp-gradle-compat.properties', mappingDescriber('agp-gradle-compat') ],
    [ 'android-api-agp-compat.properties', mappingDescriber('android-api-agp-compat') ],
    [ 'android-studio-agp-compat.properties', mappingDescriber('android-studio-agp-compat') ],
    [ 'gradle-kotlin-compat.properties', mappingDescriber('gradle-kotlin-compat') ],
    [ 'java-gradle-compat.properties', mappingDescriber('java-gradle-compat') ],
    [ 'kotlin-r8-compat.properties', mappingDescriber('kotlin-r8-compat') ],
    [ 'ksp-agp-compat.properties', mappingDescriber('ksp-agp-compat') ],
    [ 'android-studio-build-version.properties', describeAndroidStudioBuilds ],
    [ 'android-studio-codename-version.properties', mappingDescriber('android-studio-codename-version') ],
    [ 'android-studio-codename.properties', mappingDescriber('android-studio-codename') ],
]);

export const DESCRIBED_DATA_FILES = Object.freeze([ ...DESCRIBERS.keys() ]);

function describeUnknownFile(file, before, after) {
    if (before === undefined) return [ { dataset: file, type: 'file', kind: 'added', file } ];
    if (after === undefined) return [ { dataset: file, type: 'file', kind: 'removed', file } ];
    if (normalizedText(before) === normalizedText(after)) return [];
    return [ { dataset: file, type: 'file', kind: 'changed', file } ];
}

/**
 * Lists the semantic differences between two data snapshots. Generation
 * timestamps and comments are ignored, so the result is empty when only
 * metadata headers changed.
 */
export function describeDataChanges(before, after) {
    const changes = [];
    for (const [ file, describe ] of DESCRIBERS) {
        const previous = before.get(file);
        const next = after.get(file);
        if (previous === next) continue;
        changes.push(...describe(previous, next).map((change) => ({ ...change, category: categoryOf(change) })));
    }
    const unknownFiles = [ ...new Set([ ...before.keys(), ...after.keys() ]) ]
        .filter((file) => !DESCRIBERS.has(file))
        .sort();
    for (const file of unknownFiles) {
        changes.push(...describeUnknownFile(file, before.get(file), after.get(file))
            .map((change) => ({ ...change, category: categoryOf(change) })));
    }
    return changes;
}

/** Describes the changes between a Git revision and the data directory in the working tree. */
export function describeDataChangesSince(ref, { rootDir = PROJECT_DIR, dataDir = DATA_DIR } = {}) {
    return describeDataChanges(
        readDataSnapshotFromGit(ref, { rootDir, dataDir }),
        readDataSnapshotFromDirectory(dataDir),
    );
}

function categoryOf(change) {
    return change.type === 'release' ? 'dependency' : 'improvement';
}

// Rendering ---------------------------------------------------------------

/**
 * Upstream text is inserted into Markdown and into the generator's `{{ }}`
 * templates, so it is reduced to plain single-line text before rendering.
 */
function sanitize(value) {
    return String(value ?? '')
        .replace(/[\u0000-\u001f\u007f]+/g, ' ')
        .replace(/[{}`<>]/g, '')
        .replace(/\s+/g, ' ')
        .trim();
}

/** Replaces `{name}` placeholders with already sanitized values. */
function format(template, values) {
    if (typeof template !== 'string') throw new Error('Missing data change message template');
    return template.replace(/\{(\w+)\}/g, (placeholder, name) => {
        if (!Object.hasOwn(values, name)) throw new Error(`Missing value for ${placeholder} in "${template}"`);
        return String(values[name]);
    });
}

function messagesFor(language) {
    const messages = DATA_CHANGE_MESSAGES[language];
    if (!messages) throw new Error(`No data change messages for language ${language}`);
    return messages;
}

function formatList(values, messages) {
    const list = values.slice(0, MAX_LISTED_VERSIONS).map(sanitize).join(messages.listSeparator);
    if (values.length <= MAX_LISTED_VERSIONS) return list;
    return format(messages.listOverflow, {
        list,
        rest: values.length - MAX_LISTED_VERSIONS,
        total: values.length,
    });
}

/** Renders one change as a localized changelog sentence. */
export function renderDataChange(change, language) {
    const messages = messagesFor(language);
    const from = sanitize(change.from);
    const to = sanitize(change.to);
    switch (change.type) {
        case 'release': {
            const text = format(messages.release[change.kind], { subject: messages.subjects[change.subject], from, to });
            const detail = sanitize(change.detail);
            return detail ? `${text} (${detail})` : text;
        }
        case 'mapping': {
            const value = { added: to, removed: from, changed: `${from} -> ${to}` }[change.kind];
            const fact = format(messages.facts[change.dataset], { key: sanitize(change.key), value });
            return format(messages.mapping[change.kind], { fact });
        }
        case 'builds':
            return format(messages.builds[change.kind], { list: formatList(change.versions, messages) });
        case 'androidStudioMetadata':
            return format(messages.androidStudioMetadata, { version: sanitize(change.version) });
        case 'file':
            return format(messages.file[change.kind], { file: sanitize(change.file) });
        default:
            throw new Error(`Unknown data change type: ${change.type}`);
    }
}

/** Groups localized sentences by changelog category. */
export function renderDataChangeCategories(changes, language) {
    const categories = Object.fromEntries(DATA_CHANGE_CATEGORIES.map((category) => [ category, [] ]));
    for (const change of changes) {
        categories[change.category ?? categoryOf(change)].push(renderDataChange(change, language));
    }
    return categories;
}

/** Renders a Markdown bullet list, version upgrades first, for commit messages and summaries. */
export function renderDataChangeMarkdown(changes, language = 'en') {
    const { improvement, dependency } = renderDataChangeCategories(changes, language);
    return [ ...dependency, ...improvement ].map((line) => `- ${line}`).join('\n');
}
