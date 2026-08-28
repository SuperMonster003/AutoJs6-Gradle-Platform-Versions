import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { tableRows } from '../lib/html.mjs';
import {
    compareVersions,
    compareVersionsDescending,
    firstVersionIn,
    floorVersionKey,
} from '../lib/versioning.mjs';

const GRADLE_COMPATIBILITY_URL = 'https://docs.gradle.org/current/userguide/compatibility.html';

export function parseGradleCompatibility(html, minimumVersions) {
    const kotlinByGradle = new Map();
    for (const cells of tableRows(html, [ /Embedded Kotlin version/i, /Minimum Gradle version/i ])) {
        const kotlin = firstVersionIn(cells[0], 2);
        const gradle = firstVersionIn(cells[1], 2);
        if (kotlin && gradle) kotlinByGradle.set(gradle, kotlin);
    }

    // Gradle may support the minimum configured version without changing its
    // embedded Kotlin on that exact release. Retain the nearest lower change
    // point as the baseline (for example 9.0.0 for a 9.1.0 support floor).
    const baseline = floorVersionKey(kotlinByGradle, minimumVersions.gradle);
    const filteredKotlin = [ ...kotlinByGradle ].filter(([ gradle ]) => (
        compareVersions(gradle, minimumVersions.gradle) >= 0 || gradle === baseline
    ));
    if (filteredKotlin.length < 2) {
        throw new Error(`Parsed too few Gradle/Kotlin compatibility rows: ${filteredKotlin.length}`);
    }

    const javaByVersion = new Map();
    for (const cells of tableRows(html, [ /Java version/i, /Support for running Gradle/i ])) {
        const java = Number.parseInt(cells[0], 10);
        const gradle = firstVersionIn(cells[2], 2);
        if (Number.isInteger(java) && java >= minimumVersions.java && gradle) {
            javaByVersion.set(String(java), gradle);
        }
    }
    if (javaByVersion.size < 2) {
        throw new Error(`Parsed too few Java/Gradle compatibility rows: ${javaByVersion.size}`);
    }

    return {
        kotlinByGradle: filteredKotlin,
        javaByVersion: [ ...javaByVersion ],
    };
}

export async function main() {
    const { minimumVersions } = await loadConfig();
    const html = await fetchText(GRADLE_COMPATIBILITY_URL);
    const { kotlinByGradle, javaByVersion } = parseGradleCompatibility(html, minimumVersions);

    const changed = await Promise.all([
        updateProperties('gradle-kotlin-compat', kotlinByGradle, {
            label: 'Gradle and embedded Kotlin compatibility map',
            sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
        }),
        updateProperties('java-gradle-compat', javaByVersion, {
            label: 'Java runtime and Gradle compatibility map',
            sort: ([ left ], [ right ]) => Number(right) - Number(left),
        }),
    ]);
    return changed.some(Boolean);
}

runWhenDirect(import.meta.url, main);
