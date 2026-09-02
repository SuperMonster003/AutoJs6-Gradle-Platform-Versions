import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';
import {
    prepareDataRelease,
    shanghaiReleaseDate,
} from './lib/release-metadata.mjs';

const UTILS_DIR = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(UTILS_DIR, '..');

function usage() {
    console.log(`Usage: node prepare-data-release.mjs --version-build <number> [--released-date YYYY/MM/DD]

Prepares the next patch release after verified platform-data changes. The caller
must regenerate Markdown and commit all changes as one release commit.`);
}

function parseArguments(arguments_) {
    const options = { releasedDate: shanghaiReleaseDate() };
    for (let index = 0; index < arguments_.length; index += 1) {
        const argument = arguments_[index];
        if (argument === '--version-build' || argument === '--released-date') {
            const value = arguments_[++index];
            if (value === undefined) throw new Error(`${argument} requires a value.`);
            if (argument === '--version-build') options.versionBuild = Number(value);
            else options.releasedDate = value;
        } else if (argument === '--help' || argument === '-h') options.help = true;
        else throw new Error(`Unknown argument: ${argument}`);
    }
    return options;
}

function main() {
    const options = parseArguments(process.argv.slice(2));
    if (options.help) {
        usage();
        return;
    }
    if (options.versionBuild === undefined) throw new Error('--version-build is required.');

    const result = prepareDataRelease({
        rootDir: ROOT_DIR,
        versionBuild: options.versionBuild,
        releasedDate: options.releasedDate,
    });
    console.log(`Prepared data release ${result.releaseVersion} (build ${result.versionBuild}, ${result.releasedDate}).`);
}

try {
    main();
} catch (error) {
    console.error(error instanceof Error ? error.message : error);
    usage();
    process.exitCode = 1;
}
