import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchJson, fetchText } from '../lib/fetch.mjs';
import { tableRows } from '../lib/html.mjs';
import { compareVersions, compareVersionsDescending, firstVersionIn } from '../lib/versioning.mjs';

const ANDROID_STUDIO_ARCHIVE_URL = 'https://jb.gg/android-studio-releases-list.json';
const ANDROID_STUDIO_COMPATIBILITY_URL = 'https://developer.android.com/studio/releases?hl=en';
const MANUAL_CODENAME_OVERRIDES = Object.freeze({});

function groupBy(values, keyOf) {
    const groups = new Map();
    for (const value of values) {
        const key = keyOf(value);
        if (!groups.has(key)) groups.set(key, []);
        groups.get(key).push(value);
    }
    return groups;
}

export function parseStudioAgpCompatibility(html, minimumVersions) {
    const result = new Map();
    for (const cells of tableRows(html, [ /Android Studio version/i, /Required AGP version/i ])) {
        const studio = String(cells[0] || '').match(/\b\d{4}\.\d+\.\d+\b/)?.[0];
        const agpVersions = String(cells[1] || '').match(/\b\d+\.\d+(?:\.\d+)?\b/g) ?? [];
        const agp = agpVersions.at(-1);
        if (!studio || !agp) continue;
        if (compareVersions(studio, minimumVersions.androidStudio) < 0) continue;
        if (compareVersions(agp, minimumVersions.agp) < 0) continue;

        // When the same IDE appears twice, retain the stricter ceiling.
        const previous = result.get(studio);
        if (!previous || compareVersions(agp, previous) < 0) result.set(studio, agp);
    }
    if (result.size < 2) throw new Error(`Parsed too few Android Studio/AGP compatibility rows: ${result.size}`);
    return [ ...result ];
}

function codenameFromReleaseName(name) {
    const matched = /^Android\s+Studio\s+(.+?)(?:\s+\d+)?(?:\s+Feature Drop)?\s*\|/i.exec(String(name || ''));
    const codename = matched?.[1]?.trim();
    return codename && /[A-Za-z]/.test(codename) ? codename : null;
}

function buildUniqueCodenameCodes(names) {
    const entries = names.map((name) => {
        const base = name.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
        const override = MANUAL_CODENAME_OVERRIDES[name]?.toUpperCase();
        if (!base) throw new Error(`Cannot derive a codename code from "${name}"`);
        const length = override?.length ?? 1;
        return { name, base, code: override ?? base.slice(0, length), length, locked: Boolean(override) };
    });

    for (let attempt = 0; attempt < 1024; attempt += 1) {
        const groups = groupBy(entries, (entry) => entry.code);
        const conflicts = [ ...groups.values() ].filter((group) => group.length > 1);
        if (conflicts.length === 0) return new Map(entries.map(({ name, code }) => [ name, code ]));

        for (const group of conflicts) {
            if (group.filter(({ locked }) => locked).length > 1) {
                throw new Error(`Conflicting manual Android Studio codename codes: ${group.map(({ name }) => name).join(', ')}`);
            }
            for (const entry of group) {
                if (entry.locked) continue;
                entry.length += 1;
                if (entry.length > entry.base.length) {
                    throw new Error(`Unable to create unique Android Studio codename codes for: ${group.map(({ name }) => name).join(', ')}`);
                }
                entry.code = entry.base.slice(0, entry.length);
            }
        }
    }
    throw new Error('Exceeded the Android Studio codename conflict-resolution limit');
}

function compressCodenameVersions(versionCodes) {
    const result = new Map();
    const byTwoParts = groupBy([ ...versionCodes ], ([ version ]) => version.split('.').slice(0, 2).join('.'));

    for (const [ twoParts, group ] of byTwoParts) {
        const groupCodes = new Set(group.map(([, code ]) => code));
        if (groupCodes.size === 1) {
            result.set(twoParts, group[0][1]);
            continue;
        }

        const byThreeParts = groupBy(group, ([ version ]) => version.split('.').slice(0, 3).join('.'));
        for (const [ threeParts, subGroup ] of byThreeParts) {
            const subCodes = new Set(subGroup.map(([, code ]) => code));
            if (subCodes.size === 1) result.set(threeParts, subGroup[0][1]);
            else subGroup.forEach(([ version, code ]) => result.set(version, code));
        }
    }
    return [ ...result ];
}

function formatReleaseDate(date) {
    return new Intl.DateTimeFormat('en-US', {
        timeZone: 'UTC',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
    }).format(date);
}

export function buildAndroidStudioArchiveMaps(releases, minimumAndroidStudio) {
    if (!Array.isArray(releases) || releases.length === 0) {
        throw new Error('Android Studio archive response contains no releases');
    }

    const names = [ ...new Set(releases.map(({ name }) => codenameFromReleaseName(name)).filter(Boolean)) ];
    const codeByName = buildUniqueCodenameCodes(names);
    const buildVersions = new Map();
    const versionCodes = new Map();
    const codenameInfo = new Map();

    for (const release of releases) {
        const version = String(release.version || '').trim();
        if (!/^\d{4}(?:\.\d+){1,3}$/.test(version)) continue;

        if (compareVersions(version, minimumAndroidStudio) >= 0) {
            const build = String(release.build || '').replace(/[^\d.]/g, '');
            if (build) {
                const previous = buildVersions.get(build);
                if (!previous || compareVersions(version, previous) > 0) buildVersions.set(build, version);
            }
        }

        const name = codenameFromReleaseName(release.name);
        const code = name ? codeByName.get(name) : null;
        if (!code) continue;
        if (versionCodes.has(version) && versionCodes.get(version) !== code) {
            throw new Error(`Android Studio version ${version} has conflicting codenames`);
        }
        versionCodes.set(version, code);

        // Archive dates carry no timezone. Parse them explicitly as UTC so a
        // developer's local timezone cannot shift a codename's birthday.
        const born = new Date(`${release.date} 00:00:00 UTC`);
        if (Number.isNaN(born.getTime())) continue;
        const previous = codenameInfo.get(code);
        if (!previous || born < previous.born) codenameInfo.set(code, { name, born });
    }

    if (buildVersions.size === 0 || versionCodes.size === 0 || codenameInfo.size === 0) {
        throw new Error('Failed to derive complete Android Studio archive maps');
    }
    return {
        buildVersions: [ ...buildVersions ],
        codenameVersions: compressCodenameVersions(versionCodes),
        codenames: [ ...codenameInfo ].map(([ code, info ]) => [ code, info.name ]),
        codenameInfo,
    };
}

export async function main() {
    const { minimumVersions } = await loadConfig();
    const [ archive, compatibilityHtml ] = await Promise.all([
        fetchJson(ANDROID_STUDIO_ARCHIVE_URL),
        fetchText(ANDROID_STUDIO_COMPATIBILITY_URL),
    ]);
    const releases = archive?.content?.item;
    const compatibility = parseStudioAgpCompatibility(compatibilityHtml, minimumVersions);
    const maps = buildAndroidStudioArchiveMaps(releases, minimumVersions.androidStudioArchive);

    const changed = await Promise.all([
        updateProperties('android-studio-agp-compat', compatibility, {
            label: 'Android Studio and maximum AGP compatibility map',
            sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
        }),
        updateProperties('android-studio-build-version', maps.buildVersions, {
            label: 'Android Studio build and marketing version map',
            sort: ([, left ], [, right ]) => compareVersionsDescending(left, right),
        }),
        updateProperties('android-studio-codename-version', maps.codenameVersions, {
            label: 'Android Studio version and codename code map',
            sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
        }),
        updateProperties('android-studio-codename', maps.codenames, {
            label: 'Android Studio codename code map',
            sort: ([ left ], [ right ]) => maps.codenameInfo.get(right).born - maps.codenameInfo.get(left).born,
            render: (entries) => entries.flatMap(([ code, name ]) => [
                `#Born on ${formatReleaseDate(maps.codenameInfo.get(code).born)}`,
                `${code}=${name}`,
            ]),
            compareRenderedBody: true,
        }),
    ]);
    return changed.some(Boolean);
}

runWhenDirect(import.meta.url, main);
