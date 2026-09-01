import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    AGP_COMPATIBILITY_URL,
    parseAgpGradleCompatibility,
} from '../sources/android-build-tools.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const html = await fetchText(AGP_COMPATIBILITY_URL);
    const entries = parseAgpGradleCompatibility(html, minimumVersions);
    return updateProperties('agp-gradle-compat', entries, {
        label: 'AGP and minimum Gradle compatibility map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
    });
}

runWhenDirect(import.meta.url, main);
