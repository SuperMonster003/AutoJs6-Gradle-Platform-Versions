import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { CHECK_MODE_EXIT_CODE } from './lib/data-files.mjs';

const UTILS_DIR = path.dirname(fileURLToPath(import.meta.url));
const SCRAPERS = [
    'scrapers/update-gradle-kotlin-compatibility.mjs',
    'scrapers/update-kotlin-r8-compatibility.mjs',
    'scrapers/update-android-studio-releases.mjs',
    'scrapers/update-android-studio-agp-compatibility.mjs',
    'scrapers/update-java-gradle-compatibility.mjs',
    'scrapers/update-agp-gradle-compatibility.mjs',
    'scrapers/update-agp-releases.mjs',
    'scrapers/update-ksp-releases.mjs',
    'scrapers/update-ksp-agp-compatibility.mjs',
];

function usage() {
    console.log(`Usage: node run-scrapers.mjs [--check]

Options:
  --check  Compare generated data without modifying tracked files. Exits 2 when updates exist.
  --help   Show this message.

Without --check, changed datasets are written to the plugin resource directory.`);
}

function parseArguments() {
    const options = { check: false };
    for (const argument of process.argv.slice(2)) {
        if (argument === '--check') options.check = true;
        else if (argument === '--help' || argument === '-h') options.help = true;
        else throw new Error(`Unknown argument: ${argument}`);
    }
    return options;
}

function formatDuration(milliseconds) {
    return `${(milliseconds / 1000).toFixed(3)}s`;
}

function runScraper(relativePath, check) {
    return new Promise((resolve) => {
        const started = Date.now();
        const absolutePath = path.join(UTILS_DIR, relativePath);
        const child = spawn(process.execPath, [ absolutePath ], {
            cwd: UTILS_DIR,
            stdio: 'inherit',
            env: {
                ...process.env,
                PLATFORM_DATA_MODE: check ? 'check' : 'update',
            },
        });
        child.on('error', (error) => resolve({ code: 1, error, milliseconds: Date.now() - started }));
        child.on('close', (code, signal) => resolve({
            code: code ?? 1,
            signal,
            milliseconds: Date.now() - started,
        }));
    });
}

async function main() {
    const options = parseArguments();
    if (options.help) {
        usage();
        return;
    }

    for (const scraper of SCRAPERS) {
        const absolutePath = path.join(UTILS_DIR, scraper);
        if (!fs.existsSync(absolutePath)) throw new Error(`Scraper not found: ${absolutePath}`);
    }

    console.log('='.repeat(72));
    console.log(options.check ? ' Checking platform-version data' : ' Updating platform-version data');
    console.log(` Project : ${path.resolve(UTILS_DIR, '..')}`);
    console.log(` Node    : ${process.execPath}`);
    console.log(` Mode    : ${options.check ? 'check (read-only)' : 'update'}`);
    console.log('='.repeat(72));

    const changed = [];
    const failed = [];
    for (const [ index, scraper ] of SCRAPERS.entries()) {
        console.log(`\n[${index + 1}/${SCRAPERS.length}] ${scraper}`);
        const result = await runScraper(scraper, options.check);
        const suffix = `(${formatDuration(result.milliseconds)})`;
        if (options.check && result.code === CHECK_MODE_EXIT_CODE) {
            changed.push(scraper);
            console.log(`[${index + 1}/${SCRAPERS.length}] Updates detected ${suffix}`);
        } else if (result.code === 0) {
            console.log(`[${index + 1}/${SCRAPERS.length}] Completed ${suffix}`);
        } else {
            failed.push({ scraper, ...result });
            console.error(`[${index + 1}/${SCRAPERS.length}] Failed with exit code ${result.code} ${suffix}`);
            if (result.error) console.error(result.error);
            if (result.signal) console.error(`Terminated by signal ${result.signal}`);
        }
    }

    console.log(`\n${'='.repeat(72)}`);
    if (failed.length > 0) {
        console.error(`${failed.length} scraper task(s) failed.`);
        process.exitCode = 1;
    } else if (changed.length > 0) {
        console.log(`${changed.length} scraper task(s) found data updates; tracked files were not modified.`);
        process.exitCode = CHECK_MODE_EXIT_CODE;
    } else {
        console.log(options.check ? 'All platform-version data is up to date.' : 'All scraper tasks completed successfully.');
    }
    console.log('='.repeat(72));
}

main().catch((error) => {
    console.error(error);
    usage();
    process.exitCode = 1;
});
