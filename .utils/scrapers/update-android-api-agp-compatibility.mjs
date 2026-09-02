import { runWhenDirect } from '../lib/cli.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    AGP_COMPATIBILITY_URL,
    parseAndroidApiAgpCompatibility,
} from '../sources/android-build-tools.mjs';

export async function main() {
    const html = await fetchText(AGP_COMPATIBILITY_URL);
    const entries = parseAndroidApiAgpCompatibility(html);
    return updateProperties('android-api-agp-compat', entries, {
        label: 'Android API level and minimum AGP compatibility map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
    });
}

runWhenDirect(import.meta.url, main);
