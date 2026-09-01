import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    GRADLE_COMPATIBILITY_URL,
    parseGradleKotlinCompatibility,
} from '../sources/gradle-compatibility.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const html = await fetchText(GRADLE_COMPATIBILITY_URL);
    const entries = parseGradleKotlinCompatibility(html, minimumVersions.gradle);
    return updateProperties('gradle-kotlin-compat', entries, {
        label: 'Gradle and embedded Kotlin compatibility map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
    });
}

runWhenDirect(import.meta.url, main);
