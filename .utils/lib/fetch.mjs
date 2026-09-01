const DEFAULT_HEADERS = Object.freeze({
    'accept-language': 'en-US,en;q=0.9',
    'user-agent': 'AutoJs6-Gradle-Platform-Versions-Scraper/1.0 (+https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions)',
});

function delay(milliseconds) {
    return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

function retryAfterMilliseconds(response, attempt) {
    const header = response?.headers?.get('retry-after');
    const seconds = Number.parseInt(header || '', 10);
    if (Number.isFinite(seconds)) return Math.min(seconds * 1000, 30_000);
    return Math.min(500 * (2 ** (attempt - 1)), 5_000);
}

/**
 * Fetches an official data source with timeout and bounded retry handling for
 * transient network, rate-limit, and server failures.
 */
async function fetchResponse(url, options = {}) {
    const {
        attempts = 3,
        timeout = 60_000,
        headers = {},
        ...requestOptions
    } = options;
    let lastError;

    for (let attempt = 1; attempt <= attempts; attempt += 1) {
        const controller = new AbortController();
        const timer = setTimeout(
            () => controller.abort(new Error(`Request timed out after ${timeout} ms`)),
            timeout,
        );
        let response;
        try {
            response = await fetch(url, {
                ...requestOptions,
                headers: {
                    ...DEFAULT_HEADERS,
                    ...headers,
                },
                signal: controller.signal,
            });
            if (response.ok) return response;

            const error = new Error(`HTTP ${response.status} ${response.statusText} for ${url}`);
            error.status = response.status;
            lastError = error;
            const transient = response.status === 429 || response.status >= 500;
            if (!transient || attempt === attempts) throw error;
        } catch (error) {
            lastError = error;
            const status = Number(error?.status);
            const transient = !Number.isFinite(status) || status === 429 || status >= 500;
            if (!transient || attempt === attempts) throw error;
        } finally {
            clearTimeout(timer);
        }

        const wait = retryAfterMilliseconds(response, attempt);
        console.warn(`Fetch attempt ${attempt}/${attempts} failed; retrying in ${wait} ms: ${url}`);
        await delay(wait);
    }
    throw lastError;
}

export async function fetchText(url, options = {}) {
    return (await fetchResponse(url, options)).text();
}

export async function fetchJson(url, options = {}) {
    return (await fetchResponse(url, options)).json();
}

function positiveContentLength(response) {
    const contentRange = response.headers.get('content-range');
    const rangeLength = /bytes\s+\d+-\d+\/(\d+)/i.exec(contentRange || '')?.[1];
    const contentLength = rangeLength ?? response.headers.get('content-length');
    const parsed = Number.parseInt(contentLength || '', 10);
    return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null;
}

/** Reads a remote artifact size without downloading the artifact body. */
export async function fetchContentLength(url, options = {}) {
    try {
        const response = await fetchResponse(url, { ...options, method: 'HEAD' });
        const length = positiveContentLength(response);
        if (length) return length;
    } catch {
        // Some artifact servers reject HEAD. Fall through to a one-byte range request.
    }

    try {
        const response = await fetchResponse(url, {
            ...options,
            method: 'GET',
            headers: {
                ...options.headers,
                range: 'bytes=0-0',
            },
        });
        const length = positiveContentLength(response);
        await response.body?.cancel();
        return length;
    } catch {
        return null;
    }
}
