import fs from 'node:fs/promises';
import { SCRAPER_CONFIG_FILE } from './paths.mjs';

let cachedConfig;

/**
 * Loads and validates the version boundaries that define how far back generated
 * datasets need to go.
 */
export async function loadConfig() {
    if (cachedConfig) return cachedConfig;

    const config = JSON.parse(await fs.readFile(SCRAPER_CONFIG_FILE, 'utf8'));
    const versions = config?.minimumVersions;
    const requiredStrings = [ 'agp', 'androidStudio', 'androidStudioArchive', 'gradle', 'kotlin' ];
    for (const key of requiredStrings) {
        if (typeof versions?.[key] !== 'string' || !versions[key].trim()) {
            throw new Error(`Invalid or missing minimumVersions.${key} in ${SCRAPER_CONFIG_FILE}`);
        }
    }
    if (!Number.isInteger(versions?.java) || versions.java <= 0) {
        throw new Error(`Invalid or missing minimumVersions.java in ${SCRAPER_CONFIG_FILE}`);
    }

    cachedConfig = Object.freeze({
        minimumVersions: Object.freeze({ ...versions }),
    });
    return cachedConfig;
}
