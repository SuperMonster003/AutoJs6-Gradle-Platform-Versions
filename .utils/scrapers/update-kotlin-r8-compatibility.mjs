import { runWhenDirect } from '../lib/cli.mjs';
import { updateProperties } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    KOTLIN_SUPPORT_URL,
    parseKotlinR8Compatibility,
} from '../sources/android-build-tools.mjs';

export async function main() {
    const html = await fetchText(KOTLIN_SUPPORT_URL);
    const entries = parseKotlinR8Compatibility(html);
    return updateProperties('kotlin-r8-compat', entries, {
        label: 'Kotlin and minimum R8 compatibility map',
        sort: ([ left ], [ right ]) => compareVersionsDescending(left, right),
        render: (sortedEntries) => [
            '# Generated from https://developer.android.com/build/kotlin-support',
            ...sortedEntries.map(([ kotlin, r8 ]) => `${kotlin}=${r8}`),
        ],
    });
}

runWhenDirect(import.meta.url, main);
