<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>{{ text_plugin_synopsis }}</p>

  <p>
    <a href="{{ repo_url }}/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="{{ repo_url }}/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.1.0+-02303A"/></a>
    <a href="https://developer.android.com/build/releases/gradle-plugin"><img alt="AGP" src="https://img.shields.io/badge/AGP-9.0+-3DDC84"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2025.2.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2026.1.2+-EE4677"/></a>
    <a href="{{ repo_url }}/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
  </p>
</div>

******

### {{ h3_languages_with_ascii }}

******

{{ p_languages_all_supported_for_readme }}:

{{ placeholder_ul_languages_all_supported }}

******

### {{ h3_introduction }}

******

{{ p_introduction }}

{{ p_introduction_extra }}

******

### {{ h3_functions }}

******

{{ placeholder_features }}

******

### {{ h3_usage }}

******

{{ p_usage_intro }}:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("{{ plugin_id }}") version "{{ plugin_version }}"
    }
}

plugins {
    id("{{ plugin_id }}")
}
```

{{ p_usage_consume }}:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

{{ p_usage_note }}.

******

### {{ h3_decision_flow }}

******

{{ p_decision_flow_intro }}:

{{ placeholder_decision_steps }}

{{ p_decision_flow_outro }}.

******

### {{ h3_pinning }}

******

{{ p_pinning_intro }}:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

{{ p_pinning_outro }}.

******

### {{ h3_data }}

******

{{ p_data_intro }}:

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

{{ p_data_outro }}.

******

### {{ h3_data_updates }}

******

{{ p_data_updates_intro }}:

```bat
run-scrapers.bat
```

{{ p_data_updates_ci }}:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

{{ p_data_updates_exit_codes }}.

{{ text_data_updates_details }} [.utils/README.md]({{ repo_url }}/blob/master/.utils/README.md).

******

### {{ h3_release_history }}

******

{{ placeholder_latest_release_history }}

##### {{ h5_for_more_release_history }}

* {{ placeholder_read_more_in_changelog_md }}

******

### {{ h3_build }}

******

```powershell
.\gradlew.bat build
```

{{ text_publish_build }}:

```powershell
.\gradlew.bat publishToMavenLocal
```

{{ p_build_params }}.

******

### {{ h3_resource_layout }}

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

{{ p_resource_layout }}.

******

### {{ h3_links }}

******

- {{ text_link_autojs6 }}: {{ autojs6_url }}
- {{ text_link_agp_releases }}: {{ agp_releases_url }}
- {{ text_link_gradle_compat }}: {{ gradle_compat_url }}
