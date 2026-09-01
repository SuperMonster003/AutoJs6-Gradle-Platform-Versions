import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    buildKspReleaseMap,
    fetchKspReleases,
    formatReleaseDate,
} from '../sources/ksp.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const releases = await fetchKspReleases();
    const releaseMap = buildKspReleaseMap(releases, minimumVersions.kotlin);
    const entries = [ ...releaseMap ].map(([ key, { value } ]) => [ key, value ]);
    return updateProperties('ksp-releases', entries, {
        label: 'Kotlin and KSP release map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
        render: (sortedEntries) => sortedEntries.flatMap(([ key, value ]) => [
            `#${formatReleaseDate(releaseMap.get(key).date)}`,
            `${key}=${value}`,
        ]),
        compareRenderedBody: true,
    });
}

runWhenDirect(import.meta.url, main);
