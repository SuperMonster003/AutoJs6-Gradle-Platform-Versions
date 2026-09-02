import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import {
    LANGUAGE_CODES,
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

test('data release metadata stays aligned across every language', () => {
    const rootDir = fs.mkdtempSync(path.join(os.tmpdir(), 'autojs6-data-release-'));
    try {
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
            const changelog = JSON.parse(fs.readFileSync(path.join(rootDir, '.changelog', `lang_${code}.json`), 'utf8'));
            assert.equal(Object.keys(changelog.$data)[0], 'v1.7.2');
            assert.equal(changelog.$data['v1.7.2'].released_date, '2026/09/03');
            assert.equal(changelog.$data['v1.7.2'].improvement.length, 1);
            assert.ok(changelog.$data['v1.7.2'].improvement[0].length > 20);
        });
    } finally {
        fs.rmSync(rootDir, { recursive: true, force: true });
    }
});
