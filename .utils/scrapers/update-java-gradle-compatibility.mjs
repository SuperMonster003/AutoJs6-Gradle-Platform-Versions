import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import {
    GRADLE_COMPATIBILITY_URL,
    parseJavaGradleCompatibility,
} from '../sources/gradle-compatibility.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const html = await fetchText(GRADLE_COMPATIBILITY_URL);
    const entries = parseJavaGradleCompatibility(html, minimumVersions.java);
    return updateProperties('java-gradle-compat', entries, {
        label: 'Java runtime and Gradle compatibility map',
        sort: ([ left ], [ right ]) => Number(right) - Number(left),
    });
}

runWhenDirect(import.meta.url, main);
