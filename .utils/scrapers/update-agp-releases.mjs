import { runWhenDirect } from '../lib/cli.mjs';
import { loadConfig } from '../lib/config.mjs';
import { updateList } from '../lib/data-files.mjs';
import { fetchText } from '../lib/fetch.mjs';
import { compareVersionsDescending } from '../lib/versioning.mjs';
import {
    AGP_METADATA_URL,
    parseAgpReleases,
} from '../sources/android-build-tools.mjs';

export async function main() {
    const { minimumVersions } = await loadConfig();
    const xml = await fetchText(AGP_METADATA_URL, {
        headers: { accept: 'application/xml,text/xml,text/plain' },
    });
    const releases = parseAgpReleases(xml, minimumVersions.agp);
    return updateList('agp-releases', releases, {
        label: 'latest AGP release in each version line',
        sort: compareVersionsDescending,
    });
}

runWhenDirect(import.meta.url, main);
