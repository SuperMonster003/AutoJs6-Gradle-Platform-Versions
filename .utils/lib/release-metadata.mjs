import fs from 'node:fs';
import path from 'node:path';

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
    'zh-Hans': '从官方上游来源刷新随插件分发的平台兼容性与发行数据; 定时自动化在发布前已验证抓取器解析、Gradle 插件行为及无头消费者构建',
    'zh-Hant-HK': '從官方上游來源刷新隨插件發佈的平台兼容性與發行數據; 定時自動化在發佈前已驗證抓取器解析、Gradle 插件行為及無頭取用端構建',
    'zh-Hant-TW': '從官方上游來源更新隨外掛程式發布的平台相容性與發行資料；定時自動化在發布前已驗證擷取器解析、Gradle 外掛程式行為及無介面取用端建置',
    en: 'Refreshed the bundled platform compatibility and release data from the official upstream sources; the scheduled automation validated scraper parsing, Gradle plugin behavior, and a headless consumer build before publication',
    fr: 'Actualisation des données embarquées de compatibilité de plateforme et de versions depuis les sources officielles en amont ; avant publication, l’automatisation planifiée a validé les analyseurs, le comportement du plugin Gradle et un build consommateur sans interface',
    es: 'Se actualizaron desde las fuentes oficiales los datos integrados de compatibilidad de plataforma y versiones; antes de publicar, la automatización programada validó los analizadores, el comportamiento del plugin de Gradle y una compilación consumidora sin interfaz',
    ja: '公式アップストリームから同梱のプラットフォーム互換性データとリリースデータを更新。定期自動化により、公開前にスクレイパー解析、Gradle プラグインの動作、ヘッドレス利用側ビルドを検証',
    ko: '공식 업스트림 소스에서 내장 플랫폼 호환성 및 릴리스 데이터를 갱신함. 예약 자동화가 게시 전에 스크레이퍼 파싱, Gradle 플러그인 동작 및 헤드리스 소비자 빌드를 검증함',
    ru: 'Обновлены встроенные данные о совместимости платформ и выпусках из официальных первичных источников; перед публикацией плановая автоматизация проверила разбор сборщиками, поведение плагина Gradle и сборку потребителя без IDE',
    ar: 'حُدثت بيانات توافق المنصة والإصدارات المضمنة من المصادر الرسمية؛ وتحققت الأتمتة المجدولة قبل النشر من تحليل أدوات الجمع وسلوك إضافة Gradle وبناء مستهلك بلا واجهة',
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

export function prepareDataRelease({
    rootDir,
    versionBuild,
    releasedDate = shanghaiReleaseDate(),
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
            [releaseKey]: {
                released_date: releasedDate,
                improvement: [ DATA_RELEASE_MESSAGES[code] ],
            },
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
    };
}
