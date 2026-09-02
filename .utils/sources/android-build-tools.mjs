import { tableRows } from '../lib/html.mjs';
import { compareVersions, compareVersionsDescending, firstVersionIn } from '../lib/versioning.mjs';

export const AGP_COMPATIBILITY_URL = 'https://developer.android.com/build/releases/about-agp?hl=en';
export const AGP_METADATA_URL = 'https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml';
export const KOTLIN_SUPPORT_URL = 'https://developer.android.com/build/kotlin-support?hl=en';

export function parseAgpGradleCompatibility(html, minimumVersions) {
    const result = new Map();
    for (const cells of tableRows(html, [ /Plugin version/i, /Minimum required Gradle version/i ])) {
        const agp = firstVersionIn(cells[0], 2);
        const gradle = firstVersionIn(cells[1], 2);
        if (!agp || !gradle) continue;
        if (compareVersions(agp, minimumVersions.agp) < 0) continue;
        if (compareVersions(gradle, minimumVersions.gradle) < 0) continue;
        result.set(agp, gradle);
    }
    if (result.size < 2) throw new Error(`Parsed too few AGP/Gradle compatibility rows: ${result.size}`);
    return [ ...result ];
}

export function parseAndroidApiAgpCompatibility(html) {
    const result = new Map();
    for (const cells of tableRows(html, [ /API level/i, /Minimum AGP version/i ])) {
        const androidApi = String(cells[0] || '').match(/\b\d+(?:\.\d+)?\b/)?.[0];
        const agp = firstVersionIn(cells.at(-1), 2);
        if (!androidApi || !agp) continue;
        if (result.has(androidApi) && result.get(androidApi) !== agp) {
            throw new Error(`Conflicting minimum AGP versions for Android API ${androidApi}: ${result.get(androidApi)} vs ${agp}`);
        }
        result.set(androidApi, agp);
    }
    if (result.size < 5) throw new Error(`Parsed too few Android API/AGP compatibility rows: ${result.size}`);
    return [ ...result ];
}

export function parseKotlinR8Compatibility(html) {
    const result = new Map();
    for (const cells of tableRows(html, [ /Kotlin version/i, /Required R8 version/i ])) {
        const kotlin = firstVersionIn(cells[0], 2);
        const r8 = firstVersionIn(cells[2], 3);
        if (!kotlin || !r8) continue;
        if (result.has(kotlin) && result.get(kotlin) !== r8) {
            throw new Error(`Conflicting R8 requirements for Kotlin ${kotlin}: ${result.get(kotlin)} vs ${r8}`);
        }
        result.set(kotlin, r8);
    }
    if (result.size < 5) throw new Error(`Parsed too few Kotlin/R8 compatibility rows: ${result.size}`);
    return [ ...result ];
}

export function parseAgpReleases(xml, minimumAgp) {
    const latestByLine = new Map();
    const pattern = /<version>\s*([^<\s]+)\s*<\/version>/g;
    for (const matched of xml.matchAll(pattern)) {
        const version = matched[1];
        try {
            if (compareVersions(version, minimumAgp) < 0) continue;
        } catch {
            continue;
        }
        const line = /^(\d+\.\d+)\./.exec(version)?.[1];
        if (!line) continue;
        const previous = latestByLine.get(line);
        if (!previous || compareVersions(version, previous) > 0) latestByLine.set(line, version);
    }
    const releases = [ ...latestByLine.values() ].sort(compareVersionsDescending);
    if (releases.length < 2) throw new Error(`Parsed too few AGP release lines: ${releases.length}`);
    return releases;
}
