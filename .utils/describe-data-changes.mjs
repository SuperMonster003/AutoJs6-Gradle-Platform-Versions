import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';
import { DATA_CHANGE_MESSAGES } from './lib/data-change-messages.mjs';
import { describeDataChangesSince, renderDataChangeMarkdown } from './lib/data-changes.mjs';

const UTILS_DIR = path.dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = path.resolve(UTILS_DIR, '..');

function usage() {
    console.log(`Usage: node describe-data-changes.mjs --baseline-ref <git-ref> [--language <code>]

Prints the semantic platform-data changes between <git-ref> and the working tree
as a Markdown bullet list, e.g. "- Upgrade AGP version 9.4.1 -> 9.4.2".
Prints nothing when only timestamps or comments changed.

Languages: ${Object.keys(DATA_CHANGE_MESSAGES).join(', ')} (default: en)`);
}

function parseArguments(arguments_) {
    const options = { language: 'en' };
    for (let index = 0; index < arguments_.length; index += 1) {
        const argument = arguments_[index];
        if (argument === '--baseline-ref' || argument === '--language') {
            const value = arguments_[++index];
            if (value === undefined) throw new Error(`${argument} requires a value.`);
            if (argument === '--baseline-ref') options.baselineRef = value;
            else options.language = value;
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
    if (!options.baselineRef) throw new Error('--baseline-ref is required.');

    const markdown = renderDataChangeMarkdown(
        describeDataChangesSince(options.baselineRef, { rootDir: ROOT_DIR }),
        options.language,
    );
    if (markdown) process.stdout.write(`${markdown}\n`);
}

try {
    main();
} catch (error) {
    console.error(error instanceof Error ? error.message : error);
    usage();
    process.exitCode = 1;
}
