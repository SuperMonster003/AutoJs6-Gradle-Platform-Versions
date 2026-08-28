import path from 'node:path';
import { fileURLToPath } from 'node:url';

const LIB_DIR = path.dirname(fileURLToPath(import.meta.url));

export const UTILS_DIR = path.resolve(LIB_DIR, '..');
export const PROJECT_DIR = path.resolve(UTILS_DIR, '..');
export const DATA_DIR = path.join(
    PROJECT_DIR,
    'src',
    'main',
    'resources',
    'org',
    'autojs',
    'build',
    'platform',
    'data',
);
export const SCRAPER_CONFIG_FILE = path.join(UTILS_DIR, 'scraper.config.json');
