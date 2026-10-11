import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { describeDataChanges } from '../lib/data-changes.mjs';
import { NON_ASCII_PUNCTUATION } from '../lib/punctuation.mjs';
import {
    LANGUAGE_CODES,
    dataReleaseEntry,
    nextPatchVersion,
    prepareDataRelease,
    shanghaiReleaseDate,
} from '../lib/release-metadata.mjs';

test('data releases increment only stable patch versions', () => {
    assert.equal(nextPatchVersion('1.7.1'), '1.7.2');
    assert.equal(nextPatchVersion('10.20.99'), '10.20.100');
    assert.throws(() => nextPatchVersion('1.8.0-rc1'), /stable semantic version/);
    assert.equal(shanghaiReleaseDate(new Date('2026-09-02T16:30:00Z')), '2026/09/03');
});

function releaseFixture(t) {
    const rootDir = fs.mkdtempSync(path.join(os.tmpdir(), 'autojs6-data-release-'));
    t.after(() => fs.rmSync(rootDir, { recursive: true, force: true }));
    fs.mkdirSync(path.join(rootDir, '.readme'), { recursive: true });
    fs.mkdirSync(path.join(rootDir, '.changelog'), { recursive: true });
    fs.writeFileSync(path.join(rootDir, 'version.properties'), 'VERSION_BUILD=57\nVERSION_NAME=1.7.1\n');
    fs.writeFileSync(
        path.join(rootDir, '.readme', 'common.json'),
        `${JSON.stringify({ plugin_version: '1.7.1' }, null, 2)}\n`,
    );
    LANGUAGE_CODES.forEach((code) => fs.writeFileSync(
        path.join(rootDir, '.changelog', `lang_${code}.json`),
        `${JSON.stringify({ $data: { 'v1.7.1': { released_date: '2026/09/02' } } }, null, 2)}\n`,
    ));
    const readChangelog = (code) => JSON.parse(
        fs.readFileSync(path.join(rootDir, '.changelog', `lang_${code}.json`), 'utf8'),
    );
    return { rootDir, readChangelog };
}

test('data release metadata stays aligned across every language', (t) => {
    const { rootDir, readChangelog } = releaseFixture(t);
    const result = prepareDataRelease({
        rootDir,
        versionBuild: 58,
        releasedDate: '2026/09/03',
    });

    assert.deepEqual(result, {
        previousVersion: '1.7.1',
        releaseVersion: '1.7.2',
        versionBuild: 58,
        releasedDate: '2026/09/03',
        dataChangeCount: 0,
    });
    assert.equal(
        fs.readFileSync(path.join(rootDir, 'version.properties'), 'utf8'),
        'VERSION_BUILD=58\nVERSION_NAME=1.7.2\n',
    );
    assert.equal(
        JSON.parse(fs.readFileSync(path.join(rootDir, '.readme', 'common.json'), 'utf8')).plugin_version,
        '1.7.2',
    );
    LANGUAGE_CODES.forEach((code) => {
        const changelog = readChangelog(code);
        assert.equal(Object.keys(changelog.$data)[0], 'v1.7.2');
        assert.equal(changelog.$data['v1.7.2'].released_date, '2026/09/03');
        assert.equal(changelog.$data['v1.7.2'].improvement.length, 1);
        assert.ok(changelog.$data['v1.7.2'].improvement[0].length > 20);
        assert.doesNotMatch(changelog.$data['v1.7.2'].improvement[0], NON_ASCII_PUNCTUATION, code);
        assert.equal(changelog.$data['v1.7.2'].dependency, undefined);
    });
});

test('data release notes list each data change in every language', (t) => {
    const { rootDir, readChangelog } = releaseFixture(t);
    const dataChanges = describeDataChanges(
        new Map([
            [ 'agp-releases.list', '#old\n9.5.0-alpha08\n9.4.1\n' ],
            [ 'gradle-kotlin-compat.properties', '#old\n9.7.0=2.4.0\n' ],
        ]),
        new Map([
            [ 'agp-releases.list', '#new\n9.5.0-alpha09\n9.4.2\n' ],
            [ 'gradle-kotlin-compat.properties', '#new\n9.8.0=2.4.10\n9.7.0=2.4.0\n' ],
        ]),
    );
    const result = prepareDataRelease({
        rootDir,
        versionBuild: 58,
        releasedDate: '2026/09/03',
        dataChanges,
    });
    assert.equal(result.dataChangeCount, 3);

    assert.deepEqual(readChangelog('en').$data['v1.7.2'], dataReleaseEntry('en', '2026/09/03', dataChanges));
    assert.deepEqual(readChangelog('en').$data['v1.7.2'].dependency, [
        'Upgrade AGP version 9.5.0-alpha08 -> 9.5.0-alpha09',
        'Upgrade AGP version 9.4.1 -> 9.4.2',
    ]);
    assert.deepEqual(readChangelog('zh-Hans').$data['v1.7.2'].improvement.slice(1), [
        '新增映射: Gradle 9.8.0 内置 Kotlin 2.4.10',
    ]);
    LANGUAGE_CODES.forEach((code) => {
        const entry = readChangelog(code).$data['v1.7.2'];
        assert.deepEqual(Object.keys(entry), [ 'released_date', 'improvement', 'dependency' ], code);
        assert.equal(entry.improvement.length, 2, code);
        assert.equal(entry.dependency.length, 2, code);
        entry.dependency.forEach((line) => assert.match(line, /9\.4\.1 -> 9\.4\.2|9\.5\.0-alpha08 -> 9\.5\.0-alpha09/, code));
    });
});
