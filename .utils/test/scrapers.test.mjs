import assert from 'node:assert/strict';
import test from 'node:test';
import {
    compareVersions,
    floorVersionKey,
    isStableVersion,
} from '../lib/versioning.mjs';
import {
    parseGradleKotlinCompatibility,
    parseJavaGradleCompatibility,
} from '../sources/gradle-compatibility.mjs';
import {
    parseAgpGradleCompatibility,
    parseAgpReleases,
    parseAndroidApiAgpCompatibility,
    parseKotlinR8Compatibility,
} from '../sources/android-build-tools.mjs';
import {
    buildAndroidStudioArchiveMaps,
    buildLatestStableMetadata,
    findLatestStableRelease,
    parseStudioAgpCompatibility,
} from '../sources/android-studio.mjs';
import {
    buildKspReleaseMap,
    parseMinimumAgpVersion,
    splitKspTag,
} from '../sources/ksp.mjs';

const minimumVersions = {
    agp: '9.0',
    androidStudio: '2025.2.3',
    androidStudioArchive: '2023.3',
    gradle: '9.1.0',
    java: 17,
    kotlin: '1.8.20',
};

test('version ordering handles qualifiers, stable releases, and wildcard patches', () => {
    assert.ok(compareVersions('9.4.0-alpha10', '9.4.0-alpha2') > 0);
    assert.ok(compareVersions('9.4.0', '9.4.0-rc1') > 0);
    assert.equal(compareVersions('2.3.Z', '2.3.0'), 0);
    assert.equal(isStableVersion('9.3.0'), true);
    assert.equal(floorVersionKey(new Map([ [ '9.0.0', 'a' ], [ '9.2.0', 'b' ] ]), '9.1.0'), '9.0.0');
});

test('Gradle tables retain the Kotlin change point below the supported Gradle floor', () => {
    const html = `
      <table><tr><th>Embedded Kotlin version</th><th>Minimum Gradle version</th><th>Kotlin Language version</th></tr>
        <tr><td>2.2.0</td><td>9.0.0</td><td>2.2</td></tr>
        <tr><td>2.2.20</td><td>9.2.0</td><td>2.2</td></tr>
        <tr><td>2.3.0</td><td>9.4.0</td><td>2.2</td></tr></table>
      <table><tr><th>Java version</th><th>Support for toolchains</th><th>Support for running Gradle</th></tr>
        <tr><td>17</td><td>7.3</td><td>7.3 and after</td></tr>
        <tr><td>25</td><td>9.1.0</td><td>9.1.0 and after</td></tr></table>`;
    assert.deepEqual(parseGradleKotlinCompatibility(html, minimumVersions.gradle), [
        [ '9.0.0', '2.2.0' ],
        [ '9.2.0', '2.2.20' ],
        [ '9.4.0', '2.3.0' ],
    ]);
    assert.deepEqual(
        parseJavaGradleCompatibility(html, minimumVersions.java),
        [ [ '17', '7.3' ], [ '25', '9.1.0' ] ],
    );
});

test('Android build-tool parsers select official compatibility rows and latest line releases', () => {
    const agpHtml = `<table><tr><th>Plugin version</th><th>Minimum required Gradle version</th></tr>
      <tr><td>9.1</td><td>9.3.1</td></tr><tr><td>9.0</td><td>9.1.0</td></tr><tr><td>8.13</td><td>8.13</td></tr></table>`;
    assert.deepEqual(parseAgpGradleCompatibility(agpHtml, minimumVersions), [
        [ '9.1', '9.3.1' ],
        [ '9.0', '9.1.0' ],
    ]);

    const apiHtml = `<table><tr><th>API level</th><th>Minimum Android Studio version</th><th>Minimum AGP version</th></tr>
      <tr><td>37.0</td><td>Panda 3 | 2025.3.3 Patch 1</td><td>9.1.1</td></tr>
      <tr><td>36.1</td><td>Narwhal 3 | 2025.1.3</td><td>8.13.0</td></tr>
      <tr><td>36</td><td>Meerkat | 2024.3.1 Patch 1</td><td>8.9.1</td></tr>
      <tr><td>35</td><td>Koala | 2024.2.1</td><td>8.6.0</td></tr>
      <tr><td>34</td><td>Hedgehog | 2023.1.1</td><td>8.1.1</td></tr></table>`;
    assert.deepEqual(parseAndroidApiAgpCompatibility(apiHtml), [
        [ '37.0', '9.1.1' ],
        [ '36.1', '8.13.0' ],
        [ '36', '8.9.1' ],
        [ '35', '8.6.0' ],
        [ '34', '8.1.1' ],
    ]);

    const metadata = '<metadata><versioning><versions><version>9.0.1</version><version>9.1.0-alpha01</version><version>9.1.0</version><version>9.2.0-alpha02</version></versions></versioning></metadata>';
    assert.deepEqual(parseAgpReleases(metadata, '9.0'), [ '9.2.0-alpha02', '9.1.0', '9.0.1' ]);
});

test('Kotlin/R8 parser removes superscript footnote markers', () => {
    const rows = [
        [ '2.4', '8.5.2+', '9.1.29' ],
        [ '2.3', '8.2.2-8.13', '8.13.19<sup>1</sup>' ],
        [ '2.2', '7.3.1-8.10', '8.10.21' ],
        [ '2.1', '8.6', '8.6.17' ],
        [ '2.0', '8.5', '8.5.10' ],
    ];
    const html = `<table><tr><th>Kotlin version</th><th>Required AGP version</th><th>Required R8 version</th></tr>${rows.map((cells) => `<tr>${cells.map((cell) => `<td>${cell}</td>`).join('')}</tr>`).join('')}</table>`;
    const parsed = new Map(parseKotlinR8Compatibility(html));
    assert.equal(parsed.get('2.3'), '8.13.19');
});

test('Android Studio data derives build, compatibility, and compressed codename maps', () => {
    const compatibilityHtml = `<table><tr><th>Android Studio version</th><th>Required AGP version</th></tr>
      <tr><td>Quail 3 | 2026.1.3</td><td>7.1-9.3</td></tr>
      <tr><td>Otter 3 | 2025.2.3</td><td>4.0-9.0</td></tr></table>`;
    assert.deepEqual(parseStudioAgpCompatibility(compatibilityHtml, minimumVersions), [
        [ '2026.1.3', '9.3' ],
        [ '2025.2.3', '9.0' ],
    ]);

    const releases = [
        { version: '2026.1.3.1', build: 'AI-261.1.2613.100', name: 'Android Studio Quail 3 | 2026.1.3 Canary 1', date: 'March 1, 2026' },
        { version: '2026.1.2.1', build: 'AI-261.1.2612.100', name: 'Android Studio Quail 2 | 2026.1.2 Canary 1', date: 'February 1, 2026' },
        { version: '2023.3.2.2', build: 'AI-233.2', name: 'Android Studio Koala | 2023.3.2 Canary 2', date: 'December 2, 2023' },
        { version: '2023.3.2.1', build: 'AI-233.1', name: 'Android Studio Jellyfish | 2023.3.2 Canary 1', date: 'December 1, 2023' },
        { version: '2023.3.1.1', build: 'AI-233.0', name: 'Android Studio Jellyfish | 2023.3.1 Canary 1', date: 'November 1, 2023' },
    ];
    const maps = buildAndroidStudioArchiveMaps(releases, '2025.2.3');
    assert.deepEqual(new Map(maps.buildVersions), new Map([
        [ '261.1.2613.100', '2026.1.3.1' ],
        [ '261.1.2612.100', '2026.1.2.1' ],
    ]));
    assert.deepEqual(new Map(maps.codenameVersions), new Map([
        [ '2026.1', 'Q' ],
        [ '2023.3.2.2', 'K' ],
        [ '2023.3.2.1', 'J' ],
        [ '2023.3.1', 'J' ],
    ]));
});

test('Android Studio latest-stable metadata selects Patch releases and exact artifact sizes', async () => {
    const downloads = [
        { link: 'https://example.test/android-studio-quail3-patch1-windows.exe' },
        { link: 'https://example.test/android-studio-quail3-patch1-windows.zip' },
        { link: 'https://example.test/android-studio-quail3-patch1-linux.tar.gz' },
    ];
    const releases = [
        {
            version: '2026.2.1.1',
            name: 'Android Studio Rabbit | 2026.2.1 Canary 1',
            channel: 'Canary',
            date: 'August 20, 2026',
            download: downloads,
        },
        {
            version: '2026.1.3.7',
            name: 'Android Studio Quail 3 | 2026.1.3',
            channel: 'Release',
            date: 'August 1, 2026',
            download: downloads,
        },
        {
            version: '2026.1.3.8',
            build: 'AI-261.8',
            platformVersion: '2026.1.4',
            name: 'Android Studio Quail 3 | 2026.1.3 Patch 1',
            channel: 'Patch',
            date: 'August 10, 2026',
            download: downloads,
        },
    ];
    assert.equal(findLatestStableRelease(releases).version, '2026.1.3.8');

    const sizes = new Map(downloads.map(({ link }, index) => [ link, 1_500_000_000 + index ]));
    const metadata = await buildLatestStableMetadata(releases, async (url) => sizes.get(url));
    assert.equal(metadata.schemaVersion, 1);
    assert.equal(metadata.name, 'Android Studio Quail 3 | 2026.1.3 Patch 1');
    assert.equal(metadata.releaseDate, '2026-08-10');
    assert.deepEqual(metadata.downloads.windowsZip, {
        fileName: 'android-studio-quail3-patch1-windows.zip',
        url: downloads[1].link,
        sizeBytes: 1_500_000_001,
    });
});

test('KSP parsers cover legacy and standalone release schemes', () => {
    assert.deepEqual(splitKspTag('2.2.21-2.0.5'), {
        kotlinKey: '2.2.21',
        kspVersion: '2.0.5',
        standalone: false,
    });
    assert.deepEqual(splitKspTag('2.3.10'), {
        kotlinKey: '2.3.Z',
        kspVersion: '2.3.10',
        standalone: true,
    });
    assert.equal(
        parseMinimumAgpVersion('val MINIMUM_SUPPORTED_AGP_VERSION = AndroidPluginVersion(9, 0, 0).alpha(14)'),
        '9.0.0-alpha14',
    );

    const releases = [
        { tag_name: '2.3.9', published_at: '2026-06-01T00:00:00Z' },
        { tag_name: '2.3.10', published_at: '2026-07-01T00:00:00Z' },
        { tag_name: '2.2.21-2.0.4', published_at: '2025-10-01T00:00:00Z' },
        { tag_name: '2.2.21-2.0.5', published_at: '2026-02-01T00:00:00Z' },
    ];
    const result = buildKspReleaseMap(releases, '2.2.0');
    assert.equal(result.get('2.3.Z').value, '2.3.10');
    assert.equal(result.get('2.2.21').value, '2.0.5');
});
