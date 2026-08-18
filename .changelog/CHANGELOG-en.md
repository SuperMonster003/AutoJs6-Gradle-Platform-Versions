******

### Languages

******

CHANGELOG.md is currently available in the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### Release History

******

# v1.4.0

###### 2026/08/18

* `Fix` The AGP ceiling is no longer relaxed when the IDE version is only a patch-level update. IntelliJ IDEA 2026.2.1 used to resolve to AGP 9.2.1 and be rejected by the IDE; it now stays on the 9.1 line, just as 2026.2 does
* `Improvement` A 2026.2 entry added to the IntelliJ IDEA mapping table, with its AGP ceiling taken from what the IDE itself reports
* `Improvement` The migration approach now declares the plugin version once in the root build script, leaving module scripts untouched; adding the version module by module could never work for Groovy modules, whose plugins block accepts string literals only
* `Improvement` Notes printed to the console moved into a separate paragraph below the version summary, instead of being interleaved with the version lines

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

# v1.0.0

###### 2026/08/18

* `Feature` Gradle Settings plugin `org.autojs.build.platform-versions`, which decides the AGP and Kotlin Gradle plugin versions automatically
* `Feature` Build host detection, covering Android Studio/IntelliJ IDEA/Temurin JDK and the bare command line
* `Feature` AGP version decision, matching the current IDE version against the mapping table and rounding down to the nearest entry
* `Feature` Stale mapping table fallback, switching to auto selection when the current IDE is newer than every entry in the table, instead of silently downgrading to an outdated AGP
* `Feature` AGP version capped against the Gradle compatibility table, so the chosen version is always loadable by the current Gradle
* `Feature` Kotlin Gradle plugin version decision, following the newest version the current Gradle supports
* `Feature` Compatibility data shipped with the plugin, with a `gradle/data` directory in the consumer project able to override any data file of the same name
* `Feature` `OVERRIDDEN_*` escape hatch in `version.properties`, for pinning versions directly and skipping the automatic decision
* `Feature` Decision results exposed through `PlatformVersionsExtension`, ready for buildscript classpath declarations
* `Feature` Minimal consumer project `sample`, for verifying the decision results in three typical scenarios
* `Feature` Multilingual resources for README and CHANGELOG: Spanish/French/Russian/Arabic/Japanese/Korean/English/Simplified Chinese/Traditional Chinese (Hong Kong)/Traditional Chinese (Taiwan)
