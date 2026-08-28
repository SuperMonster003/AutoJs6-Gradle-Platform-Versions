import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { CHECK_MODE_EXIT_CODE, isCheckMode } from './data-files.mjs';

function samePath(left, right) {
    const a = path.resolve(left);
    const b = path.resolve(right);
    return process.platform === 'win32' ? a.toLowerCase() === b.toLowerCase() : a === b;
}

/** Runs a scraper only when its module is the process entry point. */
export function runWhenDirect(moduleUrl, main) {
    if (!process.argv[1] || !samePath(fileURLToPath(moduleUrl), process.argv[1])) return;

    main().then((changed) => {
        if (changed && isCheckMode()) process.exitCode = CHECK_MODE_EXIT_CODE;
    }).catch((error) => {
        console.error(error);
        process.exitCode = 1;
    });
}
