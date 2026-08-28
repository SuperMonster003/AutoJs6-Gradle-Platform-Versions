import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchJson, fetchText } from '../lib/fetch.mjs';
import { compareVersions, compareVersionsDescending } from '../lib/versioning.mjs';

const KSP_RELEASES_URL = 'https://api.github.com/repos/google/ksp/releases';
const KSP_AGP_GUARD_PATH = 'gradle-plugin/src/main/kotlin/com/google/devtools/ksp/gradle/utils/agpUtils.kt';

function githubHeaders() {
    const token = process.env.GITHUB_TOKEN;
    return {
        accept: 'application/vnd.github+json',
        ...(token ? { authorization: `Bearer ${token}` } : {}),
    };
}

/** Splits legacy `<kotlin>-<ksp>` tags and standalone KSP 2 tags. */
export function splitKspTag(rawTag) {
    const tag = String(rawTag || '').trim();
    if (!tag) return null;
    const parts = tag.split('-');
    const finalPart = parts.at(-1);
    if (parts.length >= 2 && /^\d+\.\d+\.\d+(?:\.\d+)?$/.test(finalPart)) {
        return { kotlinKey: parts.slice(0, -1).join('-'), kspVersion: finalPart, standalone: false };
    }

    const matched = /^(\d+)\.(\d+)\.\d+(?:\.\d+)?(?:-([A-Za-z][A-Za-z0-9.]*))?$/.exec(tag);
    if (!matched) return null;
    const qualifier = matched[3] ? `-${matched[3]}` : '';
    return {
        kotlinKey: `${matched[1]}.${matched[2]}.Z${qualifier}`,
        kspVersion: tag,
        standalone: true,
    };
}

export async function fetchKspReleases() {
    const releases = [];
    for (let page = 1; ; page += 1) {
        const url = new URL(KSP_RELEASES_URL);
        url.searchParams.set('per_page', '100');
        url.searchParams.set('page', String(page));
        const response = await fetchJson(url, { headers: githubHeaders() });
        if (!Array.isArray(response)) throw new Error(`Unexpected KSP releases response on page ${page}`);
        releases.push(...response.filter(({ draft }) => !draft));
        if (response.length < 100) break;
    }
    if (releases.length === 0) throw new Error('No KSP releases were returned by GitHub');
    return releases;
}

function releaseDate(release) {
    const date = new Date(release.published_at);
    return Number.isNaN(date.getTime()) ? null : date;
}

function formatReleaseDate(date) {
    return new Intl.DateTimeFormat('en-US', {
        timeZone: 'Asia/Shanghai',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
    }).format(date);
}

export function buildKspReleaseMap(releases, minimumKotlin) {
    const latestByKotlin = new Map();
    for (const release of releases) {
        const parsed = splitKspTag(release.tag_name);
        const date = releaseDate(release);
        if (!parsed || !date) continue;
        if (compareVersions(parsed.kotlinKey, minimumKotlin) < 0) continue;

        const previous = latestByKotlin.get(parsed.kotlinKey);
        if (!previous || date > previous.date) {
            latestByKotlin.set(parsed.kotlinKey, { value: parsed.kspVersion, date });
        }
    }
    if (latestByKotlin.size === 0) throw new Error('Failed to derive any supported Kotlin/KSP release mappings');
    return latestByKotlin;
}

/** Parses KSP's official `MINIMUM_SUPPORTED_AGP_VERSION` expression. */
export function parseMinimumAgpVersion(source) {
    const matched = /MINIMUM_SUPPORTED_AGP_VERSION\s*=\s*AndroidPluginVersion\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)(?:\.([A-Za-z]+)\(\s*(\d+)\s*\))?/m.exec(source);
    if (!matched) return null;
    const base = `${matched[1]}.${matched[2]}.${matched[3]}`;
    return matched[4] ? `${base}-${matched[4].toLowerCase()}${matched[5]}` : base;
}

async function fetchMinimumAgpVersion(tag) {
    const sourceUrl = `https://raw.githubusercontent.com/google/ksp/${encodeURIComponent(tag)}/${KSP_AGP_GUARD_PATH}`;
    const source = await fetchText(sourceUrl, {
        attempts: 2,
        headers: { accept: 'text/plain' },
    });
    return parseMinimumAgpVersion(source);
}

async function mapWithConcurrency(values, concurrency, transform) {
    const results = new Array(values.length);
    let nextIndex = 0;
    async function worker() {
        while (nextIndex < values.length) {
            const index = nextIndex;
            nextIndex += 1;
            results[index] = await transform(values[index], index);
        }
    }
    await Promise.all(Array.from({ length: Math.min(concurrency, values.length) }, worker));
    return results;
}

export async function buildKspAgpCompatibility(releases, minimumKotlin) {
    const standalone = releases
        .map((release) => ({ release, parsed: splitKspTag(release.tag_name) }))
        .filter(({ parsed }) => parsed?.standalone && compareVersions(parsed.kotlinKey, minimumKotlin) >= 0)
        .sort((left, right) => compareVersionsDescending(left.parsed.kspVersion, right.parsed.kspVersion));
    if (standalone.length === 0) throw new Error('No supported standalone KSP releases were found');

    const resolved = await mapWithConcurrency(standalone, 4, async ({ release, parsed }, index) => {
        try {
            const minimumAgp = await fetchMinimumAgpVersion(parsed.kspVersion);
            return minimumAgp ? { release, version: parsed.kspVersion, minimumAgp } : null;
        } catch (error) {
            if (index === 0) throw new Error(
                `Failed to derive the AGP guard for latest standalone KSP ${parsed.kspVersion}`,
                { cause: error },
            );
            console.warn(`Skipping KSP ${parsed.kspVersion}; its official AGP guard could not be read: ${error}`);
            return null;
        }
    });
    const entries = resolved.filter(Boolean);
    if (entries.length === 0 || entries[0].version !== standalone[0].parsed.kspVersion) {
        throw new Error(`The latest standalone KSP release ${standalone[0].parsed.kspVersion} has no readable AGP guard`);
    }
    return entries;
}

export async function main() {
    const { minimumVersions } = await loadConfig();
    const releases = await fetchKspReleases();
    const releaseMap = buildKspReleaseMap(releases, minimumVersions.kotlin);
    const compatibility = await buildKspAgpCompatibility(releases, minimumVersions.kotlin);

    const releaseEntries = [ ...releaseMap ].map(([ key, { value } ]) => [ key, value ]);
    const compatibilityEntries = compatibility.map(({ version, minimumAgp }) => [ version, minimumAgp ]);
    const changed = await Promise.all([
        updateProperties('ksp-releases', releaseEntries, {
            label: 'Kotlin and KSP release map',
            sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
            render: (entries) => entries.flatMap(([ key, value ]) => [
                `#${formatReleaseDate(releaseMap.get(key).date)}`,
                `${key}=${value}`,
            ]),
            compareRenderedBody: true,
        }),
        updateProperties('ksp-agp-compat', compatibilityEntries, {
            label: 'KSP and minimum AGP compatibility map',
            sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
            render: (entries) => [
                '# Generated from the official AGP guard in each google/ksp release tag',
                ...entries.flatMap(([ version, minimumAgp ]) => {
                    const item = compatibility.find((entry) => entry.version === version);
                    const date = releaseDate(item.release);
                    return [
                        ...(date ? [ `#${formatReleaseDate(date)}` ] : []),
                        `${version}=${minimumAgp}`,
                    ];
                }),
            ],
            compareRenderedBody: true,
        }),
    ]);
    return changed.some(Boolean);
}

runWhenDirect(import.meta.url, main);
