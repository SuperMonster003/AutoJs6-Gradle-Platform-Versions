import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { DATA_CHANGE_MESSAGES } from '../lib/data-change-messages.mjs';
import {
    DESCRIBED_DATA_FILES,
    describeDataChanges,
    describeDataChangesSince,
    readDataSnapshotFromDirectory,
    renderDataChange,
    renderDataChangeCategories,
    renderDataChangeMarkdown,
} from '../lib/data-changes.mjs';
import { DATA_DIR } from '../lib/paths.mjs';
import { LANGUAGE_CODES } from '../lib/release-metadata.mjs';

const snapshot = (entries) => new Map(Object.entries(entries));
const english = (changes) => changes.map((change) => renderDataChange(change, 'en'));

function studioMetadata(version, name, sizeBytes = 1) {
    return `${JSON.stringify({
        schemaVersion: 1,
        name: `Android Studio ${name}`,
        version,
        downloads: { linuxTar: { sizeBytes } },
    }, null, 2)}\n`;
}

test('every bundled dataset has a semantic change describer', () => {
    const bundled = [ ...readDataSnapshotFromDirectory().keys() ].sort();
    assert.deepEqual([ ...DESCRIBED_DATA_FILES ].sort(), bundled);
});

test('every changelog language provides the same data change templates', () => {
    assert.deepEqual(Object.keys(DATA_CHANGE_MESSAGES).sort(), [ ...LANGUAGE_CODES ].sort());
    const shape = (value) => (typeof value === 'string'
        ? 'string'
        : Object.fromEntries(Object.entries(value).map(([ key, nested ]) => [ key, shape(nested) ])));
    const reference = shape(DATA_CHANGE_MESSAGES.en);
    for (const code of LANGUAGE_CODES) {
        assert.deepEqual(shape(DATA_CHANGE_MESSAGES[code]), reference, code);
    }
    const mappingDatasets = DESCRIBED_DATA_FILES
        .map((file) => file.replace(/\.[^.]+$/, ''))
        .filter((dataset) => ![ 'agp-releases', 'ksp-releases', 'android-studio-latest-stable' ].includes(dataset))
        .sort();
    assert.deepEqual(Object.keys(DATA_CHANGE_MESSAGES.en.facts).sort(), mappingDatasets);
});

test('AGP releases are compared per version line', () => {
    const changes = describeDataChanges(
        snapshot({ 'agp-releases.list': '#Fri Sep 25 14:09:22 GMT+8 2026\n9.5.0-alpha08\n9.4.1\n9.3.3\n9.0.1\n' }),
        snapshot({ 'agp-releases.list': '#Sat Oct 10 09:17:00 GMT+8 2026\n9.6.0-alpha01\n9.5.0\n9.4.2\n9.3.3\n' }),
    );
    assert.deepEqual(english(changes), [
        'Add AGP version 9.6.0-alpha01',
        'Upgrade AGP version 9.5.0-alpha08 -> 9.5.0',
        'Upgrade AGP version 9.4.1 -> 9.4.2',
        'Remove AGP version 9.0.1',
    ]);
    assert.ok(changes.every(({ category }) => category === 'dependency'));
    assert.deepEqual(renderDataChangeCategories(changes, 'zh-Hans').dependency, [
        '新增 AGP 版本 9.6.0-alpha01',
        '升级 AGP 版本 9.5.0-alpha08 -> 9.5.0',
        '升级 AGP 版本 9.4.1 -> 9.4.2',
        '移除 AGP 版本 9.0.1',
    ]);
});

test('KSP releases render full legacy tags and standalone KSP 2 versions', () => {
    const changes = describeDataChanges(
        snapshot({ 'ksp-releases.properties': '#Sep 10, 2026\n2.3.Z=2.3.12\n#Feb 3, 2026\n2.2.21=2.0.4\n' }),
        snapshot({ 'ksp-releases.properties': '#Oct 9, 2026\n2.3.Z=2.3.13\n#Feb 3, 2026\n2.2.21=2.0.5\n#Jan 1, 2026\n2.2.20=2.0.4\n' }),
    );
    assert.deepEqual(english(changes), [
        'Upgrade KSP version 2.3.12 -> 2.3.13',
        'Upgrade KSP version 2.2.21-2.0.4 -> 2.2.21-2.0.5',
        'Add KSP version 2.2.20-2.0.4',
    ]);
});

test('the latest stable Android Studio release reports versions and its display name', () => {
    const upgraded = describeDataChanges(
        snapshot({ 'android-studio-latest-stable.json': studioMetadata('2026.1.4.8', 'Quail 4 | 2026.1.4 Patch 1') }),
        snapshot({ 'android-studio-latest-stable.json': studioMetadata('2026.2.1.8', 'Rabbit 1 | 2026.2.1') }),
    );
    assert.deepEqual(english(upgraded), [
        'Upgrade the latest stable Android Studio version 2026.1.4.8 -> 2026.2.1.8 (Rabbit 1 | 2026.2.1)',
    ]);
    assert.equal(upgraded[0].category, 'dependency');

    const refreshed = describeDataChanges(
        snapshot({ 'android-studio-latest-stable.json': studioMetadata('2026.2.1.8', 'Rabbit 1 | 2026.2.1', 1) }),
        snapshot({ 'android-studio-latest-stable.json': studioMetadata('2026.2.1.8', 'Rabbit 1 | 2026.2.1', 2) }),
    );
    assert.deepEqual(english(refreshed), [ 'Refresh the release metadata of Android Studio 2026.2.1.8' ]);
    assert.equal(refreshed[0].category, 'improvement');
});

test('compatibility maps report added, changed, and removed entries', () => {
    const changes = describeDataChanges(
        snapshot({
            'agp-gradle-compat.properties': '#Wed Sep 02 10:57:18 GMT+8 2026\n9.4=9.6.0\n9.0=9.1.0\n',
            'java-gradle-compat.properties': '#Fri Sep 25 14:09:21 GMT+8 2026\n26=9.4.0\n',
        }),
        snapshot({
            'agp-gradle-compat.properties': '#Sat Oct 10 09:17:00 GMT+8 2026\n9.5=9.7.0\n9.4=9.6.1\n',
            'java-gradle-compat.properties': '#Sat Oct 10 09:17:00 GMT+8 2026\n27=9.8.0\n26=9.4.0\n',
        }),
    );
    assert.deepEqual(english(changes), [
        'Add mapping: AGP 9.5 requires Gradle 9.7.0',
        'Update mapping: AGP 9.4 requires Gradle 9.6.0 -> 9.6.1',
        'Remove mapping: AGP 9.0 requires Gradle 9.1.0',
        'Add mapping: Running on Java 27 requires Gradle 9.8.0',
    ]);
    assert.ok(changes.every(({ category }) => category === 'improvement'));
    assert.equal(renderDataChange(changes[1], 'zh-Hans'), '更新映射: AGP 9.4 最低需要 Gradle 9.6.0 -> 9.6.1');
});

test('new Android Studio builds are listed by version and long lists are summarized', () => {
    const builds = (count) => Array.from({ length: count }, (_, index) => `262.${index}=2026.2.1.${index + 1}`).join('\n');
    const changes = describeDataChanges(
        snapshot({ 'android-studio-build-version.properties': `#old\n${builds(1)}\n` }),
        snapshot({ 'android-studio-build-version.properties': `#new\n${builds(11)}\n` }),
    );
    assert.deepEqual(english(changes), [
        'Recognize Android Studio builds: 2026.2.1.11, 2026.2.1.10, 2026.2.1.9, 2026.2.1.8, '
        + '2026.2.1.7, 2026.2.1.6, 2026.2.1.5, 2026.2.1.4 and 2 more',
    ]);
    assert.match(renderDataChange(changes[0], 'zh-Hans'), /2026\.2\.1\.4 等 10 项$/);
});

test('timestamp and comment-only rewrites produce no change items', () => {
    const changes = describeDataChanges(
        snapshot({
            'agp-releases.list': '#Fri Sep 25 14:09:22 GMT+8 2026\n9.4.1\n',
            'kotlin-r8-compat.properties': '# Generated from A\n2.4=9.1.29\n',
            'notes.txt': '# first\nvalue\n',
        }),
        snapshot({
            'agp-releases.list': '#Sat Oct 10 09:17:00 GMT+8 2026\n9.4.1\n',
            'kotlin-r8-compat.properties': '# Generated from B\n2.4=9.1.29\n',
            'notes.txt': '# second\nvalue\n',
        }),
    );
    assert.deepEqual(changes, []);
});

test('unknown data files are still reported', () => {
    const changes = describeDataChanges(
        snapshot({ 'retired.properties': 'a=1\n', 'notes.txt': 'old\n' }),
        snapshot({ 'future.properties': 'a=1\n', 'notes.txt': 'new\n' }),
    );
    assert.deepEqual(english(changes), [
        'Add data file future.properties',
        'Update data file notes.txt',
        'Remove data file retired.properties',
    ]);
});

test('upstream text cannot inject Markdown or generator placeholders', () => {
    const [ change ] = describeDataChanges(
        snapshot({ 'android-studio-latest-stable.json': studioMetadata('1.0', 'Old') }),
        snapshot({ 'android-studio-latest-stable.json': studioMetadata('2.0', '{{ x }} <b>`New`</b>\nline') }),
    );
    assert.equal(renderDataChange(change, 'en'), 'Upgrade the latest stable Android Studio version 1.0 -> 2.0 (x bNew/b line)');
});

test('every language renders complete, single-line sentences', () => {
    const changes = describeDataChanges(
        snapshot({
            'agp-releases.list': '9.4.1\n9.0.1\n',
            'ksp-releases.properties': '2.3.Z=2.3.12\n',
            'android-studio-latest-stable.json': studioMetadata('2026.1.4.8', 'Quail 4 | 2026.1.4'),
            'agp-gradle-compat.properties': '9.4=9.6.0\n9.0=9.1.0\n',
            'android-api-agp-compat.properties': '36=8.9.1\n',
            'android-studio-agp-compat.properties': '2026.1.4=9.4\n',
            'gradle-kotlin-compat.properties': '9.7.0=2.4.0\n',
            'java-gradle-compat.properties': '26=9.4.0\n',
            'kotlin-r8-compat.properties': '2.3=8.13.19\n',
            'ksp-agp-compat.properties': '2.3.12=8.12.0\n',
            'android-studio-build-version.properties': '261.1=2026.1.4.7\n261.2=2026.1.4.8\n',
            'android-studio-codename-version.properties': '2026.2=R\n',
            'android-studio-codename.properties': 'R=Rabbit\n',
            'retired.json': '{}\n',
        }),
        snapshot({
            'agp-releases.list': '9.5.0-alpha01\n9.4.0\n',
            'ksp-releases.properties': '2.3.Z=2.3.13\n',
            'android-studio-latest-stable.json': studioMetadata('2026.2.1.8', 'Rabbit 1 | 2026.2.1'),
            'agp-gradle-compat.properties': '9.5=9.7.0\n9.4=9.6.1\n',
            'android-api-agp-compat.properties': '37.0=9.1.1\n',
            'android-studio-agp-compat.properties': '2026.1.4=9.5\n',
            'gradle-kotlin-compat.properties': '9.8.0=2.4.10\n',
            'java-gradle-compat.properties': '27=9.8.0\n',
            'kotlin-r8-compat.properties': '2.4=9.1.29\n',
            'ksp-agp-compat.properties': '2.3.13=8.12.0\n',
            'android-studio-build-version.properties': '261.2=2026.1.4.9\n262.1=2026.2.1.8\n',
            'android-studio-codename-version.properties': '2026.3=S\n',
            'android-studio-codename.properties': 'S=Salamander\n',
            'future.json': '{}\n',
        }),
    );
    const kinds = new Set(changes.map(({ type, kind }) => `${type}:${kind ?? ''}`));
    for (const kind of [ 'release:upgrade', 'release:downgrade', 'release:added', 'release:removed', 'mapping:added',
        'mapping:changed', 'mapping:removed', 'builds:added', 'builds:removed', 'file:added', 'file:removed' ]) {
        assert.ok(kinds.has(kind), `fixture should cover ${kind}`);
    }

    const fullwidthAllowed = new Set([ 'zh-Hant-TW', 'ja' ]);
    for (const code of LANGUAGE_CODES) {
        const categories = renderDataChangeCategories(changes, code);
        const lines = [ ...categories.improvement, ...categories.dependency ];
        assert.equal(lines.length, changes.length, code);
        for (const line of lines) {
            assert.doesNotMatch(line, /[{}\n]|undefined/, `${code}: ${line}`);
            if (!fullwidthAllowed.has(code)) assert.doesNotMatch(line, /[，。；：！？（）]/, `${code}: ${line}`);
        }
    }
});

test('Markdown summaries list version upgrades before compatibility changes', () => {
    const changes = describeDataChanges(
        snapshot({ 'agp-releases.list': '9.4.1\n', 'java-gradle-compat.properties': '26=9.4.0\n' }),
        snapshot({ 'agp-releases.list': '9.4.2\n', 'java-gradle-compat.properties': '27=9.8.0\n26=9.4.0\n' }),
    );
    assert.equal(renderDataChangeMarkdown(changes), [
        '- Upgrade AGP version 9.4.1 -> 9.4.2',
        '- Add mapping: Running on Java 27 requires Gradle 9.8.0',
    ].join('\n'));
    assert.equal(renderDataChangeMarkdown([]), '');
});

test('changes are measured from a Git revision to the working tree', (t) => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'autojs6-data-changes-'));
    t.after(() => fs.rmSync(root, { recursive: true, force: true }));
    const dataDir = path.join(root, 'src', 'main', 'resources', 'data');
    fs.mkdirSync(dataDir, { recursive: true });
    const gitConfig = path.join(root, 'gitconfig');
    fs.writeFileSync(gitConfig, '');
    const env = { ...process.env, GIT_CONFIG_NOSYSTEM: '1', GIT_CONFIG_GLOBAL: gitConfig.replaceAll('\\', '/') };
    const git = (...args) => execFileSync('git', [
        '-c', 'user.name=Data change test',
        '-c', 'user.email=data-change@example.invalid',
        '-c', 'commit.gpgsign=false',
        '-c', 'tag.gpgsign=false',
        ...args,
    ], { cwd: root, env, encoding: 'utf8' });

    git('init', '--quiet', '--initial-branch', 'master');
    fs.writeFileSync(path.join(dataDir, 'agp-releases.list'), '#old\n9.4.1\n');
    fs.writeFileSync(path.join(dataDir, 'retired.properties'), 'a=1\n');
    git('add', '--all');
    git('commit', '--quiet', '--message', 'Baseline');
    git('tag', '--annotate', 'v1.0.0', '--message', 'Release 1.0.0');

    fs.writeFileSync(path.join(dataDir, 'agp-releases.list'), '#new\n9.4.2\n');
    fs.rmSync(path.join(dataDir, 'retired.properties'));
    fs.writeFileSync(path.join(dataDir, 'java-gradle-compat.properties'), '27=9.8.0\n');

    assert.deepEqual(english(describeDataChangesSince('v1.0.0', { rootDir: root, dataDir })), [
        'Upgrade AGP version 9.4.1 -> 9.4.2',
        'Add mapping: Running on Java 27 requires Gradle 9.8.0',
        'Remove data file retired.properties',
    ]);
    assert.throws(() => describeDataChangesSince('v9.9.9', { rootDir: root, dataDir }), /Unknown baseline Git revision/);
});

test('automatic releases describe changes since the latest release tag', () => {
    const workflow = fs.readFileSync(new URL('../../.github/workflows/platform-data.yml', import.meta.url), 'utf8');
    assert.match(workflow, /prepare-data-release -- --version-build "\$next_build" --baseline-ref "\$LATEST_TAG"/);
    assert.match(workflow, /describe-data-changes\.mjs --baseline-ref "\$LATEST_TAG"/);
    assert.ok(fs.existsSync(DATA_DIR));
});
