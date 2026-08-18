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
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.3.0"
    }
}

plugins {
    id("org.autojs.build.platform-versions")
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
```

If a consumer project places a file of the same name under its own `gradle/data` directory, that file takes precedence.

******

### Release History

******

# v1.3.0

###### 2026/08/18

* `Hint` Gradle 8 is no longer supported. AGP 9.0 is the first version to require Gradle 9, so the supported range starts at AGP 9.0
* `Improvement` Entries older than 9 are dropped from the compatibility tables, and the IntelliJ IDEA mapping table keeps only the entries that yield AGP 9
* `Improvement` When the current Gradle is older than every compatibility entry, it no longer falls back to the lowest entry but reports an error outright, so that a version which cannot be loaded is never put on the classpath
* `Improvement` Minimum supported versions raised: Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `Improvement` README badges brought in line with the versions above, with an AGP badge added
* `Improvement` The migration script now handles the kotlin(...) shorthand as well as legacy short names such as kotlin-android/kotlin-kapt/kotlin-parcelize, the short names being expanded into full plugin ids
* `Improvement` The migration script skips two kinds of repository that cannot be migrated: those whose script fragments pulled in through apply(from=) reference AGP types, and those with dependency verification enabled

# v1.2.0

###### 2026/08/18

* `Feature` Module script migration script `.python/migrate_modules.py`, which rewrites versionless plugin applications into the versioned form, with the version taken from a system property
* `Feature` The decided KSP version is now published as the `gradle.ksp.version` system property as well, in line with the naming used for AGP and Kotlin
* `Fix` Rollback in the module migration script failed to restore the original files and left the backups behind
* `Improvement` The settings migration script now checks first whether the module scripts are ready, and reports instead of rewriting when they are not, so that no unbuildable intermediate state is left behind
* `Improvement` The settings migration script now merges the plugin into the existing plugins block and moves it ahead of `includeBuild`, instead of adding a new block

# v1.1.0

###### 2026/08/18

* `Feature` R8 version decision, looked up by the current Kotlin version, pulling in an external R8 explicitly only when the R8 bundled with AGP is not new enough
* `Feature` KSP version decision, with the version number following the target Kotlin version; the AGP version is raised automatically when the chosen KSP requires a newer AGP
* `Feature` Decision results now also reachable through the `PlatformVersionsFacade` entry point, usable directly in the body of the settings script
* `Feature` Decision results published as system properties as well, so module scripts can declare plugin versions through the plugins DSL
* `Feature` Batch migration script `.python/migrate_downstream.py` for downstream repositories, supporting preview/apply/rollback and keeping a backup per repository
* `Fix` `getMaxSupportedJavaVersion` used to be handed the AGP version, which lowered the toolchain ceiling; it is now handed the Gradle version
* `Improvement` Removed the 2026.2.1 entry from the IntelliJ IDEA mapping table, so that both 2026.2 and 2026.2.1 resolve to AGP 9.0.1, in line with what the IDE actually supports

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
