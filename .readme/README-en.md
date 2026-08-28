<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>A Gradle Settings plugin that decides the AGP and Kotlin plugin versions automatically for the AutoJs6 ecosystem</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.1.0+-02303A"/></a>
    <a href="https://developer.android.com/build/releases/gradle-plugin"><img alt="AGP" src="https://img.shields.io/badge/AGP-9.0+-3DDC84"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2025.2.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2026.1.2+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
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
- Caps the result against the AGP and Gradle compatibility rules, so the chosen version is always loadable by the current Gradle.
- Decides the KSP version, and raises the AGP version automatically when the chosen KSP requires a newer AGP.
- Decides the R8 version, pulling in an external R8 only when the R8 bundled with AGP is not new enough.
- Ships the compatibility data with the plugin, while a `gradle/data` directory in the consumer project takes precedence, which makes urgent data fixes easy.
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
        id("io.github.supermonster003.autojs6-platform-versions") version "1.5.0"
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

- Match the current platform version against that platform's AGP mapping table, rounding down to the nearest entry.
- Check whether the mapping table is stale, that is, whether the current IDE is newer than every entry in it; if so, fall back to auto selection.
- Cap the result against the AGP and Gradle compatibility table, so it never exceeds what the current Gradle can load.

The Kotlin version follows Gradle rather than the IDE, always taking the newest version the current Gradle supports.

******

### Pinning Versions

******

To skip the automatic decision entirely, specify the versions directly in `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

A value of `NONE` or an empty value means nothing is pinned, and the automatic decision flow applies.

******

### Compatibility Data

******

The decision relies on the following data files, which are distributed together with the plugin:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
gradle/data/android-studio-build-version.properties
gradle/data/android-studio-codename-version.properties
gradle/data/android-studio-codename.properties
gradle/data/kotlin-r8-compat.properties
gradle/data/ksp-agp-compat.properties
gradle/data/ksp-releases.properties
```

If a consumer project places a file of the same name under its own `gradle/data` directory, that file takes precedence.

******

### Data Updates

******

Developers can update all compatibility data by running the interactive batch entry point from the repository root:

```bat
run-scrapers.bat
```

A future scheduled CI job can use this read-only check entry point:

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

# v1.5.0

###### 2026/08/28

* `Feature` The complete compatibility-data update suite now lives in this repository, with an interactive `run-scrapers.bat` for manual updates and cross-platform update and read-only check commands ready for future scheduled CI runs
* `Improvement` The scrapers no longer depend on Puppeteer or Chrome: they parse official static sources, centralize retention bounds and output validation, and avoid rewrites caused only by timestamps
* `Improvement` Bundled data refreshed through Gradle 9.7 with Kotlin 2.4, the latest AGP lines up to 9.5.0-alpha03, 9.4.0-rc02, and 9.3.2, Android Studio Rabbit, and KSP 2.3.11

# v1.4.1

###### 2026/08/18

* `Improvement` No explanatory note is printed to the console when the mapping table has fallen behind. The [auto-specified] suffix at the end of the version line already says how the version was reached, and the note ran longer than the summary it explained
* `Improvement` The notes API is removed along with it: PlatformVersionsExtension.notes and the notes parameter of Formatted are gone, so a consuming script that read that property needs adjusting

# v1.4.0

###### 2026/08/18

* `Fix` The AGP ceiling is no longer relaxed when the IDE version is only a patch-level update. IntelliJ IDEA 2026.2.1 used to resolve to AGP 9.2.1 and be rejected by the IDE; it now stays on the 9.1 line, just as 2026.2 does
* `Improvement` A 2026.2 entry added to the IntelliJ IDEA mapping table, with its AGP ceiling taken from what the IDE itself reports
* `Improvement` The migration approach now declares the plugin version once in the root build script, leaving module scripts untouched; adding the version module by module could never work for Groovy modules, whose plugins block accepts string literals only
* `Improvement` Notes printed to the console moved into a separate paragraph below the version summary, instead of being interleaved with the version lines

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
