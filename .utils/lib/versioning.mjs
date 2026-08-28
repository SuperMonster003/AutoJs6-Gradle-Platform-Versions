const STABLE_PRIORITY = 100;

const SUFFIX_PRIORITIES = new Map([
    [ 'canary', 5 ],
    [ 'nightly', 5 ],
    [ 'snapshot', 5 ],
    [ 'dev', 5 ],
    [ 'experimental', 5 ],
    [ 'preview', 10 ],
    [ 'eap', 10 ],
    [ 'milestone', 10 ],
    [ 'alpha', 15 ],
    [ 'beta', 20 ],
    [ 'rc', 25 ],
    [ 'ga-candidate', 25 ],
    [ '', STABLE_PRIORITY ],
    [ 'stable', STABLE_PRIORITY ],
    [ 'ga', STABLE_PRIORITY ],
    [ 'final', STABLE_PRIORITY ],
    [ 'release', STABLE_PRIORITY ],
    [ 'patch', STABLE_PRIORITY ],
]);

const SUFFIX_ALIASES = new Map([
    [ 'a', 'alpha' ],
    [ 'b', 'beta' ],
    [ 'cr', 'rc' ],
    [ 'release-candidate', 'rc' ],
    [ 'm', 'milestone' ],
    [ 'pre', 'preview' ],
    [ 'prev', 'preview' ],
]);

function normalizeSuffix(value) {
    const suffix = String(value || '').toLowerCase().replaceAll('_', '-');
    return SUFFIX_ALIASES.get(suffix) ?? suffix;
}

/**
 * Parses common Gradle ecosystem versions, including alpha/RC qualifiers and
 * wildcard patch keys such as `2.3.Z`.
 */
export function parseVersion(version) {
    const value = String(version ?? '').trim();
    const matched = /^(\d+(?:\.(?:\d+|[xyz*?]))*)(?:[\s_+\-]*([A-Za-z][A-Za-z-]*)(?:[\s._-]*(\d+))?)?$/i.exec(value);
    if (!matched) throw new Error(`Invalid version: "${value}"`);

    const numbers = matched[1].split('.').map((part) => {
        if (/^[xyz*?]$/i.test(part)) return 0;
        const number = Number.parseInt(part, 10);
        if (!Number.isFinite(number)) throw new Error(`Invalid version part "${part}" in "${value}"`);
        return number;
    });
    const suffix = normalizeSuffix(matched[2]);
    const suffixNumber = Number.parseInt(matched[3] || '1', 10);
    return { numbers, suffix, suffixNumber };
}

/** Returns a negative, zero, or positive value using semantic version ordering. */
export function compareVersions(left, right) {
    const a = parseVersion(left);
    const b = parseVersion(right);
    const size = Math.max(a.numbers.length, b.numbers.length);
    for (let index = 0; index < size; index += 1) {
        const difference = (a.numbers[index] ?? 0) - (b.numbers[index] ?? 0);
        if (difference !== 0) return difference;
    }

    const aPriority = SUFFIX_PRIORITIES.get(a.suffix) ?? 50;
    const bPriority = SUFFIX_PRIORITIES.get(b.suffix) ?? 50;
    if (aPriority !== bPriority) return aPriority - bPriority;
    if (a.suffix !== b.suffix) return a.suffix.localeCompare(b.suffix);
    return a.suffixNumber - b.suffixNumber;
}

export const compareVersionsDescending = (left, right) => compareVersions(right, left);

export function isStableVersion(version) {
    const { suffix } = parseVersion(version);
    return (SUFFIX_PRIORITIES.get(suffix) ?? 50) === STABLE_PRIORITY;
}

/** Extracts the first dotted numeric version from prose in a documentation cell. */
export function firstVersionIn(value, minimumParts = 2) {
    const pattern = minimumParts >= 3
        ? /\b\d+\.\d+\.\d+(?:\.\d+)?(?:-[A-Za-z]+\d*)?\b/
        : /\b\d+\.\d+(?:\.\d+)?(?:-[A-Za-z]+\d*)?\b/;
    return String(value || '').match(pattern)?.[0] ?? null;
}

/** Returns the greatest map key that is less than or equal to the target version. */
export function floorVersionKey(map, target) {
    return [ ...map.keys() ]
        .filter((key) => compareVersions(key, target) <= 0)
        .sort(compareVersionsDescending)[0] ?? null;
}
