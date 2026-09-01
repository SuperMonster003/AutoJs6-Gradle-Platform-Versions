import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateJson, updateProperties } from '../lib/data-files.mjs';
import { fetchContentLength, fetchJson } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    ANDROID_STUDIO_ARCHIVE_URL,
    buildAndroidStudioArchiveMaps,
    buildLatestStableMetadata,
    formatReleaseDate,
} from '../sources/android-studio.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const archive = await fetchJson(ANDROID_STUDIO_ARCHIVE_URL);
    const releases = archive?.content?.item;
    const maps = buildAndroidStudioArchiveMaps(releases, minimumVersions.androidStudioArchive);
    const latestStable = await buildLatestStableMetadata(
        releases,
        (url) => fetchContentLength(url, { attempts: 2, timeout: 30_000 }),
    );

    const changed = await Promise.all([
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
        updateJson('android-studio-latest-stable', latestStable, {
            label: 'latest stable Android Studio release metadata for downstream projects',
        }),
    ]);
    return changed.some(Boolean);
}

runWhenDirect(import.meta.url, main);
