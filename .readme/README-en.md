<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>A Gradle Settings plugin that decides the AGP and Kotlin plugin versions automatically for the AutoJs6 ecosystem</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.1.0+-02303A"/></a>
    <a href="https://developer.android.com/build/releases/gradle-plugin"><img alt="AGP" src="https://img.shields.io/badge/AGP-9.0+-335544"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2025.2.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2026.1.2+-EE4677"/></a>
  </p>
</div>

******

### Languages

******

README.md is currently available in the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### Introduction

******

This plugin extracts the build version decision logic that used to be maintained separately in the AutoJs6 main project and in every plugin project. Each repository carried several hundred nearly identical lines in its settings.gradle.kts, all devoted to working out which IDE was driving the build and picking suitable AGP and Kotlin versions accordingly.

Now that it ships as a publishable Settings plugin, a downstream project needs little more than a dozen lines to opt in. Improve the logic once and every project picks it up by bumping the plugin version, with no more copying and pasting from repository to repository.

******

### Features

******

- Detects the build host: Android Studio, IntelliJ IDEA, Temurin JDK and the bare command line.
- Picks an AGP version the current IDE can support, rounding down to the nearest entry when there is no exact match.
- Falls back to auto selection when the IDE is newer than every entry in the mapping table, instead of silently downgrading to an outdated AGP.
- Treats Temurin and the bare command line as explicitly headless, selecting AGP from Gradle compatibility instead of an IDE-version map.
- Intersects the IDE/Gradle upper boundary with minimum AGP requirements from Android API levels, KSP and the project, failing early when no compatible version exists.
- Decides the R8 version, pulling in an external R8 only when the R8 bundled with AGP is not new enough.
- Ships compatibility data with the plugin as the single default data source; official AutoJs6 host and plugin projects do not maintain consumer-side `gradle/data` copies.
- Keeps the `OVERRIDDEN_*` escape hatch in `version.properties`, so versions can be pinned outright whenever a deterministic build is needed.
- README and CHANGELOG are available in Spanish/French/Russian/Arabic/Japanese/Korean/English/Simplified Chinese/Traditional Chinese (Hong Kong)/Traditional Chinese (Taiwan).

******

### Usage

******

Apply the plugin in the `settings.gradle.kts` of the consumer project, before `includeBuild`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.0"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
}
```

Module scripts can then declare the plugins through the plugins DSL, with the versions taken from the decision results:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

The decision results can also be read as an object through `gradle.extra["platformVersions"]`.

******

### Decision Flow

******

The AGP version is decided in three steps:

- Use the oldest platform-map key as the central IDE support floor and the matched AGP as the upper boundary; a consumer IDE minimum can only tighten that floor. Keep the stale-map fallback for newer IDEs, and use Gradle compatibility directly for Temurin and bare command-line builds.
- Cap that upper boundary against the official AGP/Gradle compatibility table so the candidate is loadable by the running Gradle.
- Derive lower boundaries from compileSdk/targetSdk, KSP and an optional project minimum, returning AGP only when the boundaries intersect.

The Kotlin version follows Gradle rather than the IDE, always taking the newest version the current Gradle supports.

******

### Pinning Versions

******

For testing or a deterministic build, exact versions can be pinned directly in `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

A value of `NONE` or an empty value means nothing is pinned. Use `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` only for a genuine project-specific lower boundary that the central mechanism cannot infer; official AutoJs6 consumers must not repeat the platform-wide AGP 9 floor it already guarantees. Numeric `COMPILE_SDK_VERSION` and `TARGET_SDK_VERSION` values are considered automatically. Likewise, `MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` and `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` are optional project-specific restrictions: the oldest key in each central IDE map is a non-lowerable baseline, so omit these properties unless the project truly needs a newer IDE.

******

### Compatibility Data

******

The decision relies on the following data files, which are distributed together with the plugin:

```text
src/main/resources/org/autojs/build/platform/data/
  agp-releases.list
  agp-gradle-compat.properties
  android-api-agp-compat.properties
  gradle-kotlin-compat.properties
  java-gradle-compat.properties
  android-studio-agp-compat.properties
  android-studio-build-version.properties
  android-studio-codename-version.properties
  android-studio-codename.properties
  kotlin-r8-compat.properties
  ksp-agp-compat.properties
  ksp-releases.properties
```

A same-named file under a consumer's `gradle/data` directory still takes precedence for legacy compatibility or temporary diagnostics only; it is not the official operating model. Official AutoJs6 host and plugin projects must not commit such overrides. Update compatibility data in this central repository and publish it with a new immutable plugin version instead.

******

### Data Updates

******

Developers can update all compatibility data by running the interactive batch entry point from the repository root:

```bat
run-scrapers.bat
```

The scheduled repository workflow uses this read-only check entry point; manual update mode opens a data pull request after validation:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` does not modify the workspace: exit code `0` means the data is current, `2` means updates were found, and `1` means the task failed.

For the complete update scope and execution contract, see [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### Release History

******

# v1.7.0

###### 2026/09/02

* `Hint` Normal builds should declare their SDK levels and, only when necessary, `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; keep `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` for deliberate exact-version tests or exceptional escape-hatch use
* `Feature` AGP selection now intersects the Android API, project, and KSP lower bounds with the active Gradle and IDE upper bounds, reporting the source when no compatible version exists
* `Feature` Added GitHub Actions workflows for Temurin builds, scheduled compatibility-data checks or update PRs, and tag-gated protected publication to Maven Central and the Gradle Plugin Portal
* `Fix` Temurin and bare command-line builds no longer consult legacy JDK-to-AGP mappings, so JDK `21.0.6+7` cannot silently choose AGP 8.7.3; Android API 36 now automatically requires AGP 8.9.1 or newer
* `Fix` Fixed two-component IDE mappings bypassing the Gradle AGP ceiling and old Gradle versions falling back to a platform version they cannot load
* `Improvement` Added independently scraped official Android API-to-minimum-AGP data and refreshed Android Studio, AGP release, and AGP/Gradle compatibility data
* `Improvement` Expanded verification to 70 JVM tests, Node parser and idempotency tests, and a real Temurin 17 CI sample that exercises headless automatic selection

# v1.6.0

###### 2026/08/29

* `Hint` The permanent Gradle plugin ID is now `io.github.supermonster003.autojs6-platform-versions` instead of `org.autojs.build.platform-versions`, and the Maven coordinates are `io.github.supermonster003:autojs6-gradle-platform-versions`; the Java/Kotlin package remains `org.autojs.build.platform`
* `Feature` First public online-release pipeline for Maven Central and the Gradle Plugin Portal, with signed implementation, sources, Javadoc, module-metadata, and plugin-marker artifacts
* `Improvement` Added the public GitHub repository, complete Central POM metadata, a reproducible Portal bundle, isolated consumer validation, and separate secure signing paths for local GPG and CI
* `Improvement` Official usage now resolves through public repositories without `mavenLocal()`; the migration tool also recognizes and updates the legacy plugin ID

# v1.5.0

###### 2026/08/28

* `Feature` The complete compatibility-data update suite now lives in this repository, with an interactive `run-scrapers.bat` for manual updates and cross-platform update and read-only check commands ready for future scheduled CI runs
* `Improvement` The scrapers no longer depend on Puppeteer or Chrome: they parse official static sources, centralize retention bounds and output validation, and avoid rewrites caused only by timestamps
* `Improvement` Bundled data refreshed through Gradle 9.7 with Kotlin 2.4, the latest AGP lines up to 9.5.0-alpha03, 9.4.0-rc02, and 9.3.2, Android Studio Rabbit, and KSP 2.3.11

##### For more release history, see

* [CHANGELOG-en.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)

******

### Build

******

```powershell
.\gradlew.bat build
```

Publish to the local Maven repository:

```powershell
.\gradlew.bat publishToMavenLocal
```

The plugin version comes from `VERSION_NAME` in `version.properties`.

******

### Resource Layout

******

```text
run-scrapers.bat
.utils/
src/main/kotlin/org/autojs/build/platform/
src/main/resources/org/autojs/build/platform/data/
sample/
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
```

The decision logic lives in `src/main/kotlin` and the compatibility data is packaged as resources in `src/main/resources`; `sample` is a minimal consumer project used to verify the decision results. README and CHANGELOG are generated from the JSON sources by `.python/generate_markdown.py`.

******

### Links

******

- AutoJs6 main project: https://github.com/SuperMonster003/AutoJs6
- Android Gradle Plugin release notes: https://developer.android.com/build/releases/gradle-plugin
- Gradle compatibility matrix: https://docs.gradle.org/current/userguide/compatibility.html
