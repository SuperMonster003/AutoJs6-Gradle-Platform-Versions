<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>AutoJs6 생태계를 위해 AGP와 Kotlin 플러그인 버전을 자동으로 결정하는 Gradle Settings 플러그인</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.2+-02303A"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어 (Languages)

******

현재 README.md에서 지원하는 언어는 다음과 같습니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### 소개

******

이 플러그인은 AutoJs6 메인 프로젝트와 각 플러그인 프로젝트에서 중복으로 관리하던 빌드 버전 결정 로직을 따로 떼어낸 것입니다. 예전에는 모든 저장소의 settings.gradle.kts에 거의 똑같은 코드가 수백 줄씩 들어 있었고, 지금 어떤 IDE가 빌드를 수행하는지 판별한 뒤 그에 맞는 AGP와 Kotlin 버전을 고르는 일을 했습니다.

배포 가능한 Settings 플러그인으로 만들고 나면 다운스트림 프로젝트는 열 몇 줄의 도입 코드만 작성하면 됩니다. 로직을 한 번 개선하면 모든 프로젝트가 플러그인 버전만 올려서 그 개선을 그대로 얻을 수 있고, 더 이상 저장소마다 복사해 붙여넣을 필요가 없습니다.

******

### 기능

******

- 빌드 호스트 식별: Android Studio, IntelliJ IDEA, Temurin JDK 그리고 순수 명령줄 환경.
- 현재 IDE 버전이 지원할 수 있는 AGP 버전을 선택하며, 정확히 일치하는 항목이 없으면 가장 가까운 하위 버전을 고릅니다.
- IDE 버전이 매핑 표의 모든 항목보다 새로우면 자동으로 auto 선택으로 되돌아가, 지나치게 낮은 AGP로 조용히 내려가는 일을 막습니다.
- AGP와 Gradle의 호환 관계에 따라 상한을 적용해, 선택된 버전을 현재 Gradle이 반드시 로드할 수 있도록 보장합니다.
- 호환성 데이터는 플러그인과 함께 배포되며, 소비 측 프로젝트에 `gradle/data` 디렉터리가 있으면 그쪽을 우선 사용하므로 데이터를 급히 수정하기 좋습니다.
- `version.properties`의 `OVERRIDDEN_*` 비상구를 그대로 남겨 두어, 결정적인 빌드가 필요할 때 버전을 직접 고정할 수 있습니다.
- README와 CHANGELOG는 스페인어/프랑스어/러시아어/아랍어/일본어/한국어/영어/중국어 간체/홍콩 번체/대만 번체를 지원합니다.

******

### 사용 방법

******

소비 측 프로젝트의 `settings.gradle.kts`에서 플러그인을 적용합니다:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.0.0"
    }
}
```

이후 결정 결과를 읽어 buildscript의 classpath에 사용할 수 있습니다:

```kotlin
import org.autojs.build.platform.PlatformVersionsExtension

val platformVersions = gradle.extra["platformVersions"] as PlatformVersionsExtension

buildscript {
    dependencies {
        classpath(platformVersions.agpClasspathNotation)
        classpath(platformVersions.kotlinClasspathNotation)
    }
}
```

******

### 결정 흐름

******

AGP 버전을 결정하는 과정은 세 단계로 나뉩니다:

- 현재 플랫폼 버전으로 해당 플랫폼의 AGP 매핑 표에서 가장 가까운 하위 항목을 찾습니다.
- 매핑 표가 뒤처졌는지, 즉 현재 IDE가 표의 모든 항목보다 새로운지 판단하고, 그렇다면 auto 선택으로 되돌아갑니다.
- AGP와 Gradle의 호환 표에 따라 상한을 적용하므로, 결과가 현재 Gradle이 로드할 수 있는 범위를 벗어나지 않습니다.

Kotlin 버전은 IDE가 아니라 Gradle을 따르며, 항상 현재 Gradle이 지원하는 최신 버전을 선택합니다.

******

### 버전 고정

******

자동 결정을 모두 건너뛰고 싶다면 `version.properties`에서 버전을 직접 지정할 수 있습니다:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

값이 `NONE`이거나 비어 있으면 지정하지 않은 것으로 보고 자동 결정 흐름을 따릅니다.

******

### 호환성 데이터

******

결정의 근거가 되는 데이터 파일은 다음과 같으며, 플러그인과 함께 배포됩니다:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

소비 측 프로젝트가 자신의 `gradle/data` 디렉터리에 같은 이름의 파일을 두면 그 파일이 우선 적용됩니다.

******

### 릴리스 기록

******

# v1.0.0

###### 2026/08/18

* `기능` AGP와 Kotlin Gradle 플러그인 버전을 자동으로 결정하는 Gradle Settings 플러그인 `org.autojs.build.platform-versions`
* `기능` 빌드 호스트 식별, Android Studio/IntelliJ IDEA/Temurin JDK 및 순수 명령줄 환경 지원
* `기능` AGP 버전 결정, 현재 IDE 버전을 기준으로 매핑 표에서 가장 가까운 하위 항목 선택
* `기능` 매핑 표 지연 시 대체 처리, 현재 IDE가 표의 모든 항목보다 새로우면 auto 선택으로 전환하여 지나치게 낮은 AGP로 조용히 내려가지 않음
* `기능` AGP 버전에 Gradle 호환 표 기반 상한 적용, 선택된 버전을 현재 Gradle이 반드시 로드할 수 있도록 보장
* `기능` Kotlin Gradle 플러그인 버전 결정, 현재 Gradle이 지원하는 최신 버전을 따름
* `기능` 호환성 데이터를 플러그인과 함께 배포, 소비 측 프로젝트의 `gradle/data` 디렉터리로 같은 이름의 데이터 파일을 덮어쓸 수 있음
* `기능` `version.properties`의 `OVERRIDDEN_*` 비상구, 버전을 직접 지정하여 자동 결정을 건너뛸 수 있음
* `기능` 결정 결과를 `PlatformVersionsExtension`으로 노출, buildscript의 classpath 선언에 사용 가능
* `기능` 최소 소비 측 프로젝트 `sample`, 세 가지 대표 시나리오에서 결정 결과를 검증
* `기능` README와 CHANGELOG의 다국어 리소스: 스페인어/프랑스어/러시아어/아랍어/일본어/한국어/영어/중국어 간체/홍콩 번체/대만 번체

##### 더 많은 릴리스 기록은 다음에서 확인할 수 있습니다

* [CHANGELOG-ko.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)

******

### 빌드

******

```powershell
.\gradlew.bat build
```

로컬 Maven 저장소에 게시:

```powershell
.\gradlew.bat publishToMavenLocal
```

플러그인 버전 번호는 `version.properties`의 `VERSION_NAME`에서 가져옵니다.

******

### 리소스 구조

******

```text
src/main/kotlin/org/autojs/build/platform/
src/main/resources/org/autojs/build/platform/data/
sample/
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
```

결정 로직은 `src/main/kotlin`에 있고, 호환성 데이터는 리소스로 `src/main/resources`에 패키징됩니다; `sample`은 결정 결과를 검증하기 위한 최소 소비 측 프로젝트입니다. README와 CHANGELOG는 `.python/generate_markdown.py`가 JSON 원본 파일을 바탕으로 생성합니다.

******

### 관련 링크

******

- AutoJs6 메인 프로젝트: https://github.com/SuperMonster003/AutoJs6
- Android Gradle 플러그인 릴리스 노트: https://developer.android.com/build/releases/gradle-plugin
- Gradle 호환성 매트릭스: https://docs.gradle.org/current/userguide/compatibility.html
