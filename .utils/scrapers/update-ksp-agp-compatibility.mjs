import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    buildKspAgpCompatibility,
    fetchKspReleases,
    formatReleaseDate,
    releaseDate,
} from '../sources/ksp.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const releases = await fetchKspReleases();
    const compatibility = await buildKspAgpCompatibility(releases, minimumVersions.kotlin);
    const entries = compatibility.map(({ version, minimumAgp }) => [ version, minimumAgp ]);
    return updateProperties('ksp-agp-compat', entries, {
        label: 'KSP and minimum AGP compatibility map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
        render: (sortedEntries) => [
            '# Generated from the official AGP guard in each google/ksp release tag',
            ...sortedEntries.flatMap(([ version, minimumAgp ]) => {
                const item = compatibility.find((entry) => entry.version === version);
                const date = releaseDate(item.release);
                return [
                    ...(date ? [ `#${formatReleaseDate(date)}` ] : []),
                    `${version}=${minimumAgp}`,
                ];
            }),
        ],
        compareRenderedBody: true,
    });
}

runWhenDirect(import.meta.url, main);
