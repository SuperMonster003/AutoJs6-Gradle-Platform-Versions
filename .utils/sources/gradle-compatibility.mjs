import { tableRows } from '../lib/html.mjs';
import {
    compareVersions,
    firstVersionIn,
    floorVersionKey,
} from '../lib/versioning.mjs';

export const GRADLE_COMPATIBILITY_URL = 'https://docs.gradle.org/current/userguide/compatibility.html';

export function parseGradleKotlinCompatibility(html, minimumGradle) {
    const kotlinByGradle = new Map();
    for (const cells of tableRows(html, [ /Embedded Kotlin version/i, /Minimum Gradle version/i ])) {
        const kotlin = firstVersionIn(cells[0], 2);
        const gradle = firstVersionIn(cells[1], 2);
        if (kotlin && gradle) kotlinByGradle.set(gradle, kotlin);
    }

    // Gradle may support the minimum configured version without changing its
    // embedded Kotlin on that exact release. Retain the nearest lower change
    // point as the baseline (for example 9.0.0 for a 9.1.0 support floor).
    const baseline = floorVersionKey(kotlinByGradle, minimumGradle);
    const filteredKotlin = [ ...kotlinByGradle ].filter(([ gradle ]) => (
        compareVersions(gradle, minimumGradle) >= 0 || gradle === baseline
    ));
    if (filteredKotlin.length < 2) {
        throw new Error(`Parsed too few Gradle/Kotlin compatibility rows: ${filteredKotlin.length}`);
    }

    return filteredKotlin;
}

export function parseJavaGradleCompatibility(html, minimumJava) {
    const javaByVersion = new Map();
    for (const cells of tableRows(html, [ /Java version/i, /Support for running Gradle/i ])) {
        const java = Number.parseInt(cells[0], 10);
        const gradle = firstVersionIn(cells[2], 2);
        if (Number.isInteger(java) && java >= minimumJava && gradle) {
            javaByVersion.set(String(java), gradle);
        }
    }
    if (javaByVersion.size < 2) {
        throw new Error(`Parsed too few Java/Gradle compatibility rows: ${javaByVersion.size}`);
    }

    return [ ...javaByVersion ];
}
