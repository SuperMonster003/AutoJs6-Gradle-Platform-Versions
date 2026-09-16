import assert from 'node:assert/strict';
import { execFileSync, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';

const workflow = fs.readFileSync(new URL('../../.github/workflows/platform-data.yml', import.meta.url), 'utf8');
const lines = workflow.replaceAll('\r\n', '\n').split('\n');
const stepStart = lines.indexOf('      - name: Detect releasable platform-data changes');
assert.ok(stepStart >= 0, 'The workflow must contain the release detection step.');
const scriptStart = lines.indexOf('        run: |', stepStart) + 1;
assert.ok(scriptStart > stepStart, 'The release detection step must contain an inline Bash script.');
const scriptLines = [];
for (const line of lines.slice(scriptStart)) {
    if (line && !line.startsWith('          ')) break;
    scriptLines.push(line.slice(10));
}
const detectionScript = scriptLines.join('\n');
const dataDir = 'src/main/resources/org/autojs/build/platform/data';
// Use Git Bash on Windows instead of the unrelated WSL bash launcher on PATH.
const bash = process.env.BASH ?? (process.platform === 'win32'
    ? path.resolve(execFileSync('git', [ '--exec-path' ], { encoding: 'utf8' }).trim(), '../../../bin/bash.exe')
    : 'bash');

function fixture(t, { version, change, tag = 'annotated' }) {
    const tempDir = path.resolve(os.tmpdir());
    const root = fs.mkdtempSync(path.join(tempDir, 'autojs6-release-detection-'));
    assert.equal(path.dirname(root), tempDir);
    t.after(() => fs.rmSync(root, { recursive: true, force: true }));
    const repo = path.join(root, 'repo');
    fs.mkdirSync(repo);
    const gitConfig = path.join(root, 'gitconfig');
    fs.writeFileSync(gitConfig, '');
    const env = { ...process.env, GIT_CONFIG_NOSYSTEM: '1', GIT_CONFIG_GLOBAL: gitConfig.replaceAll('\\', '/') };
    const git = (...args) => execFileSync('git', [
        '-c', 'user.name=Release detection test',
        '-c', 'user.email=release-detection@example.invalid',
        '-c', 'commit.gpgsign=false',
        '-c', 'tag.gpgsign=false',
        '-c', 'core.autocrlf=false',
        ...args,
    ], { cwd: repo, env, encoding: 'utf8' });
    const write = (file, contents) => {
        const target = path.join(repo, file);
        fs.mkdirSync(path.dirname(target), { recursive: true });
        fs.writeFileSync(target, contents);
    };
    const commit = () => {
        git('add', '--all');
        git('commit', '--quiet', '--message', 'Fixture update');
    };
    git('init', '--quiet', '--initial-branch', 'master');
    write('version.properties', 'VERSION_BUILD=1\nVERSION_NAME=1.8.1\n');
    write(`${dataDir}/versions.properties`, 'version=1\n');
    commit();
    if (tag === 'annotated') git('tag', '--annotate', 'v1.8.1', '--message', 'Release 1.8.1');
    else if (tag === 'lightweight') git('tag', 'v1.8.1');

    if (version !== '1.8.1') {
        write('version.properties', `VERSION_BUILD=2\nVERSION_NAME=${version}\n`);
        commit();
    }
    if (change === 'generated' || change === 'committed') {
        write(`${dataDir}/versions.properties`, 'version=2\n');
        if (change === 'committed') commit();
    } else if (change === 'untracked') write(`${dataDir}/new.properties`, 'version=2\n');
    else if (change === 'unexpected') write('unexpected.txt', 'Outside the generated data directory.\n');

    return () => {
        const before = git('status', '--porcelain');
        const head = git('rev-parse', 'HEAD');
        const tags = git('tag', '--list');
        const output = path.join(root, 'output');
        const summary = path.join(root, 'summary');
        fs.writeFileSync(output, '');
        fs.writeFileSync(summary, '');
        const result = spawnSync(bash, [ '--noprofile', '--norc', '-e', '-o', 'pipefail' ], {
            cwd: repo,
            env: {
                ...env,
                GITHUB_OUTPUT: output.replaceAll('\\', '/'),
                GITHUB_STEP_SUMMARY: summary.replaceAll('\\', '/'),
            },
            input: detectionScript,
            encoding: 'utf8',
            timeout: 30_000,
        });
        assert.ifError(result.error);
        assert.equal(git('status', '--porcelain'), before, 'Detection must not change the checkout.');
        assert.equal(git('rev-parse', 'HEAD'), head, 'Detection must not create commits.');
        assert.equal(git('tag', '--list'), tags, 'Detection must not create tags.');
        return {
            ...result,
            output: fs.readFileSync(output, 'utf8'),
            summary: fs.readFileSync(summary, 'utf8'),
        };
    };
}

for (const version of [ '1.8.1', '1.8.2' ]) {
    for (const change of [ 'none', 'generated', 'untracked', 'committed' ]) {
        test(`release detection with VERSION_NAME=${version} and ${change} data changes`, (t) => {
            const result = fixture(t, { version, change })();
            if (change === 'none') {
                assert.equal(result.status, 0, result.stdout + result.stderr);
                assert.equal(result.output, 'should_release=false\n');
                assert.match(result.summary, /No new platform-data changes/);
            } else if (version === '1.8.1') {
                assert.equal(result.status, 0, result.stdout + result.stderr);
                assert.equal(result.output, 'should_release=true\nlatest_tag=v1.8.1\n');
                assert.match(result.summary, /Platform-data release required/);
            } else {
                assert.equal(result.status, 1, result.stdout + result.stderr);
                assert.match(result.stdout, /Version baseline mismatch/);
                assert.equal(result.output, '');
            }
        });
    }
}

for (const version of [ '1.8.1', '1.8.2' ]) {
    test(`release detection rejects unexpected generated files with VERSION_NAME=${version}`, (t) => {
        const result = fixture(t, { version, change: 'unexpected' })();
        assert.equal(result.status, 1, result.stdout + result.stderr);
        assert.match(result.stdout, /Unexpected generated file/);
        assert.equal(result.output, '');
    });
}

for (const [ tag, error ] of [ [ 'missing', /Release history missing/ ], [ 'lightweight', /Annotated tag required/ ] ]) {
    test(`release detection rejects a ${tag} baseline tag`, (t) => {
        const result = fixture(t, { version: '1.8.1', change: 'none', tag })();
        assert.equal(result.status, 1, result.stdout + result.stderr);
        assert.match(result.stdout, error);
        assert.equal(result.output, '');
    });
}
