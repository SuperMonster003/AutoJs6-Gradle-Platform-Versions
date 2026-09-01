import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    ANDROID_STUDIO_COMPATIBILITY_URL,
    parseStudioAgpCompatibility,
} from '../sources/android-studio.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const html = await fetchText(ANDROID_STUDIO_COMPATIBILITY_URL);
    const entries = parseStudioAgpCompatibility(html, minimumVersions);
    return updateProperties('android-studio-agp-compat', entries, {
        label: 'Android Studio and maximum AGP compatibility map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
    });
}

runWhenDirect(import.meta.url, main);
