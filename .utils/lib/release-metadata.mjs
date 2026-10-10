import fs from 'node:fs';
import path from 'node:path';
import { renderDataChangeCategories } from './data-changes.mjs';

export const LANGUAGE_CODES = [
    'zh-Hans',
    'zh-Hant-HK',
    'zh-Hant-TW',
    'en',
    'fr',
    'es',
    'ja',
    'ko',
    'ru',
    'ar',
];

const DATA_RELEASE_MESSAGES = {
    'zh-Hans': '将插件内置的版本兼容数据 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自动同步至官方最新信息, 发布前已通过自动化测试与示例项目构建验证',
    'zh-Hant-HK': '將插件內置的版本兼容數據 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自動同步至官方最新資訊, 發佈前已通過自動化測試與示例項目構建驗證',
    'zh-Hant-TW': '將外掛程式內建的版本相容性資料 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自動同步至官方最新資訊, 發布前已通過自動化測試與範例專案建置驗證',
    en: 'Automatically sync the built-in version compatibility data (AGP, Gradle, Kotlin, KSP, Android Studio, etc.) with the latest official information, verified by tests and a sample project build before release',
    fr: 'Synchronisation automatique des données intégrées de compatibilité des versions (AGP, Gradle, Kotlin, KSP, Android Studio, etc.) avec les dernières informations officielles, vérifiée avant publication par des tests et la compilation du projet exemple',
    es: 'Sincronización automática de los datos integrados de compatibilidad de versiones (AGP, Gradle, Kotlin, KSP, Android Studio, etc.) con la información oficial más reciente, verificada antes de publicar mediante pruebas y la compilación del proyecto de ejemplo',
    ja: '内蔵のバージョン互換性データ (AGP, Gradle, Kotlin, KSP, Android Studio など) を公式の最新情報に自動同期し, 公開前にテストとサンプルプロジェクトのビルドで検証',
    ko: '내장 버전 호환성 데이터 (AGP, Gradle, Kotlin, KSP, Android Studio 등) 를 공식 최신 정보에 맞춰 자동 동기화하고, 배포 전 테스트와 샘플 프로젝트 빌드로 검증함',
    ru: 'Встроенные данные о совместимости версий (AGP, Gradle, Kotlin, KSP, Android Studio и др.) автоматически синхронизированы с актуальной официальной информацией и перед публикацией проверены тестами и сборкой демонстрационного проекта',
    ar: 'مزامنة تلقائية لبيانات توافق الإصدارات المضمنة (AGP, Gradle, Kotlin, KSP, Android Studio وغيرها) مع أحدث المعلومات الرسمية, مع التحقق قبل النشر عبر الاختبارات وبناء مشروع نموذجي',
};

function readJson(file) {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function serializeJson(value) {
    return `${JSON.stringify(value, null, 2)}\n`;
}

function propertyValue(contents, name) {
    const matches = [ ...contents.matchAll(new RegExp(`^${name}=(.*)$`, 'gm')) ];
    if (matches.length !== 1) throw new Error(`Expected exactly one ${name} property, found ${matches.length}.`);
    return matches[0][1].trim();
}

function replaceProperty(contents, name, value) {
    propertyValue(contents, name);
    return contents.replace(new RegExp(`^${name}=.*$`, 'm'), `${name}=${value}`);
}

export function nextPatchVersion(version) {
    const match = /^(\d+)\.(\d+)\.(\d+)$/.exec(version);
    if (!match) throw new Error(`Expected a stable semantic version, received ${version}.`);
    return `${match[1]}.${match[2]}.${Number(match[3]) + 1}`;
}

export function shanghaiReleaseDate(date = new Date()) {
    const parts = new Intl.DateTimeFormat('en-US', {
        timeZone: 'Asia/Shanghai',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    }).formatToParts(date);
    const values = Object.fromEntries(parts.map(({ type, value }) => [ type, value ]));
    return `${values.year}/${values.month}/${values.day}`;
}

/**
 * Builds one localized changelog entry: the fixed data-release summary first,
 * then one sentence per semantic data change, e.g. an AGP version upgrade.
 */
export function dataReleaseEntry(code, releasedDate, dataChanges = []) {
    const { improvement, dependency } = renderDataChangeCategories(dataChanges, code);
    const entry = {
        released_date: releasedDate,
        improvement: [ DATA_RELEASE_MESSAGES[code], ...improvement ],
    };
    if (dependency.length > 0) entry.dependency = dependency;
    return entry;
}

export function prepareDataRelease({
    rootDir,
    versionBuild,
    releasedDate = shanghaiReleaseDate(),
    dataChanges = [],
}) {
    if (!Number.isSafeInteger(versionBuild) || versionBuild <= 0) {
        throw new Error(`versionBuild must be a positive integer, received ${versionBuild}.`);
    }
    if (!/^\d{4}\/\d{2}\/\d{2}$/.test(releasedDate)) {
        throw new Error(`releasedDate must use YYYY/MM/DD, received ${releasedDate}.`);
    }

    const versionFile = path.join(rootDir, 'version.properties');
    const commonFile = path.join(rootDir, '.readme', 'common.json');
    const versionProperties = fs.readFileSync(versionFile, 'utf8');
    const currentVersion = propertyValue(versionProperties, 'VERSION_NAME');
    const releaseVersion = nextPatchVersion(currentVersion);
    const releaseKey = `v${releaseVersion}`;

    const common = readJson(commonFile);
    if (common.plugin_version !== currentVersion) {
        throw new Error(`README plugin version ${common.plugin_version} does not match VERSION_NAME ${currentVersion}.`);
    }
    common.plugin_version = releaseVersion;

    const changelogWrites = LANGUAGE_CODES.map((code) => {
        const file = path.join(rootDir, '.changelog', `lang_${code}.json`);
        const changelog = readJson(file);
        if (!changelog.$data || typeof changelog.$data !== 'object' || Array.isArray(changelog.$data)) {
            throw new Error(`${file} does not contain a changelog $data object.`);
        }
        if (Object.hasOwn(changelog.$data, releaseKey)) {
            throw new Error(`${file} already contains ${releaseKey}.`);
        }
        if (!Object.hasOwn(changelog.$data, `v${currentVersion}`)) {
            throw new Error(`${file} does not contain the current release v${currentVersion}.`);
        }
        changelog.$data = {
            [releaseKey]: dataReleaseEntry(code, releasedDate, dataChanges),
            ...changelog.$data,
        };
        return [ file, serializeJson(changelog) ];
    });

    let updatedProperties = replaceProperty(versionProperties, 'VERSION_BUILD', versionBuild);
    updatedProperties = replaceProperty(updatedProperties, 'VERSION_NAME', releaseVersion);

    fs.writeFileSync(versionFile, updatedProperties, 'utf8');
    fs.writeFileSync(commonFile, serializeJson(common), 'utf8');
    changelogWrites.forEach(([ file, contents ]) => fs.writeFileSync(file, contents, 'utf8'));

    return {
        previousVersion: currentVersion,
        releaseVersion,
        versionBuild,
        releasedDate,
        dataChangeCount: dataChanges.length,
    };
}
