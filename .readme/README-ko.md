<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/app/src/main/res/mipmap-night/banner.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/app/src/main/res/mipmap/banner.png?raw=true" alt="banner" border="0" width="512" />
    </picture>
  </p>

  <p>AutoJs6 생태계를 위해 AGP와 Kotlin 플러그인 버전을 자동으로 결정하는 Gradle Settings 플러그인</p>

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
- Temurin과 순수 명령줄을 명시적인 헤드리스 환경으로 취급하여, IDE 버전 표가 아니라 Gradle 호환성에 따라 AGP를 선택합니다.
- IDE/Gradle 상한과 Android API 수준, KSP 및 프로젝트가 요구하는 AGP 하한의 교집합을 구하고, 호환 버전이 없으면 일찍 오류를 냅니다.
- R8 버전을 결정하며, AGP에 포함된 R8이 충분히 새롭지 않을 때만 외부 R8을 도입합니다.
- 호환성 데이터는 플러그인과 함께 기본 단일 데이터 원본으로 배포됩니다; 공식 AutoJs6 호스트 및 플러그인 프로젝트는 소비 측 `gradle/data` 복사본을 유지하지 않습니다.
- `version.properties`의 `OVERRIDDEN_*` 비상구를 그대로 남겨 두어, 결정적인 빌드가 필요할 때 버전을 직접 고정할 수 있습니다.
- README와 CHANGELOG는 스페인어/프랑스어/러시아어/아랍어/일본어/한국어/영어/중국어 간체/홍콩 번체/대만 번체를 지원합니다.

******

### 사용 방법

******

소비 측 프로젝트의 `settings.gradle.kts`에서 플러그인을 적용하며, 위치는 `includeBuild`보다 앞이어야 합니다:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.2"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
}
```

이후 모듈 스크립트에서 plugins DSL로 플러그인을 선언할 수 있으며, 버전은 결정 결과에서 가져옵니다:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

결정 결과는 `gradle.extra["platformVersions"]`를 통해 객체 형태로도 읽을 수 있습니다.

******

### 결정 흐름

******

AGP 버전을 결정하는 과정은 세 단계로 나뉩니다:

- IDE에서는 플랫폼 표의 가장 오래된 키를 중앙 지원 하한으로, 일치한 AGP를 상한으로 사용합니다. 소비 저장소의 IDE 최저 버전은 이 하한을 더 엄격하게 만들 수만 있습니다. 더 새로운 IDE에서 표가 뒤처졌을 때의 대체 경로를 유지하고, Temurin과 순수 명령줄에서는 Gradle 호환 상한을 직접 사용합니다.
- 공식 AGP/Gradle 호환 표로 상한을 다시 제한하여 실행 중인 Gradle이 후보를 로드할 수 있게 합니다.
- compileSdk/targetSdk, KSP 및 선택적 프로젝트 최솟값에서 하한을 구하고, 상하한이 교차할 때만 AGP를 반환합니다.

Kotlin 버전은 IDE가 아니라 Gradle을 따르며, 항상 현재 Gradle이 지원하는 최신 버전을 선택합니다.

******

### 버전 고정

******

테스트 또는 결정적인 빌드가 필요하면 `version.properties`에서 정확한 버전을 직접 고정할 수 있습니다:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

값이 `NONE`이거나 비어 있으면 고정하지 않습니다. `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`은 중앙 메커니즘이 추론할 수 없는 실제 프로젝트별 하한에만 사용합니다; 공식 AutoJs6 소비 저장소는 플랫폼이 이미 보장하는 공통 AGP 9 하한을 이 값으로 중복 선언하면 안 됩니다. 숫자 형식의 `COMPILE_SDK_VERSION`과 `TARGET_SDK_VERSION`은 자동으로 고려됩니다. 마찬가지로 `MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION`과 `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION`은 선택적인 프로젝트별 제한입니다. 각 중앙 IDE 표의 가장 오래된 키가 낮출 수 없는 기준이므로 실제로 더 새로운 IDE가 필요한 경우가 아니면 이 속성들을 생략하십시오.

******

### 호환성 데이터

******

결정의 근거가 되는 데이터 파일은 다음과 같으며, 플러그인과 함께 배포됩니다:

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

소비 측 `gradle/data`의 같은 이름 파일은 레거시 호환 또는 임시 진단에 한해서만 계속 우선 적용되며 공식 운영 방식은 아닙니다. 공식 AutoJs6 호스트 및 플러그인 프로젝트는 이러한 재정의를 커밋하면 안 됩니다; 호환성 데이터는 이 중앙 저장소에서 갱신하고 새로운 변경 불가능한 플러그인 버전과 함께 게시해야 합니다.

******

### 데이터 업데이트

******

개발자는 저장소 루트에서 대화형 배치 진입점을 실행하여 모든 호환성 데이터를 업데이트할 수 있습니다:

```bat
run-scrapers.bat
```

매일 실행되는 workflow는 데이터를 갱신하고 검증한 뒤 의미 있는 변경이 있을 때만 패치 릴리스 커밋과 태그를 만들고 보호된 두 레지스트리 및 GitHub Release 게시 체인을 시작합니다. 수동 check 및 update-pr 모드도 계속 사용할 수 있습니다:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data`는 작업 영역을 수정하지 않습니다. 종료 코드 `0`은 최신 데이터, `2`는 업데이트 발견, `1`은 작업 실패를 뜻합니다.

전체 업데이트 범위와 실행 규약은 다음을 참조하세요 [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### 릴리스 기록

******

# v1.7.2

###### 2026/09/03

* `수정` Android Studio가 Gradle `-P`로 제공하는 식별 속성을 JVM 시스템 속성과 병합하여 Quail 3가 `2026.1`로 축약되고 AGP 9.2.1을 잘못 선택하거나 자동 JVM 대상 25를 거부하는 문제를 수정함
* `개선` Gradle 프로젝트 속성을 명시적으로 전달하는 바이너리 호환 Facade 오버로드와 아직 수집되지 않은 IDE build를 위한 strict 버전 폴백을 추가함; Gradle 9.5/9.7 및 JDK 25/26의 Quail 3에서 AGP 9.3.2 자동 선택과 Kotlin/KSP 작업 생성을 검증함

# v1.7.1

###### 2026/09/02

* `수정` 각 IDE 호환성 매핑의 가장 오래된 항목을 중앙 지원 하한으로 강제함. 소비 측 `MIN_SUPPORTED_*_IDE_VERSION` 값은 이 범위를 더 좁힐 수만 있으며, 지원되지 않는 이전 IDE를 Gradle 전용 대체 경로로 보낼 수 없도록 수정
* `개선` 충족된 AGP 최소 제약은 기계 판독 가능한 결과로 유지하되 일반적인 성공 빌드 요약에는 표시하지 않도록 변경. 비호환 오류에는 감지된 IDE 버전과 모든 요구 사항 출처를 포함
* `개선` 내장 호환성 데이터를 AutoJs6 공식 소비 프로젝트의 권위 있는 출처로 명확히 함. `gradle/data` 재정의는 이전 버전 호환성 또는 임시 진단 용도로만 유지

# v1.7.0

###### 2026/09/02

* `힌트` 일반 빌드는 SDK 수준을 선언하고 필요한 경우에만 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`을 지정해야 함. `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION`은 의도적인 정확한 버전 테스트나 예외적인 탈출 경로 용도로 남겨 둘 것
* `기능` AGP 선택 시 Android API, 프로젝트 및 KSP 하한과 현재 Gradle 및 IDE 상한의 교집합을 구하고 호환 버전이 없으면 제약 조건의 출처를 보고하도록 변경
* `기능` Temurin 빌드, 정기 호환성 데이터 검사 또는 갱신 PR, 태그와 승인으로 보호되는 Maven Central 및 Gradle Plugin Portal 게시를 위한 GitHub Actions 워크플로를 추가
* `수정` Temurin 및 순수 명령줄 빌드에서 더 이상 이전 JDK→AGP 매핑을 조회하지 않으므로 JDK `21.0.6+7`이 AGP 8.7.3을 조용히 선택할 수 없음. Android API 36은 이제 AGP 8.9.1 이상을 자동으로 요구
* `수정` 두 요소 IDE 매핑이 Gradle의 AGP 상한을 우회하던 문제와 오래된 Gradle이 로드할 수 없는 플랫폼 버전으로 대체되던 문제를 수정
* `개선` Android API별 최소 AGP 공식 데이터를 독립 스크레이핑 대상으로 추가하고 Android Studio, AGP 릴리스 및 AGP/Gradle 호환성 데이터를 갱신
* `개선` 검증 범위를 JVM 테스트 70개, Node 파서 및 멱등성 테스트, 헤드리스 자동 선택을 실행하는 실제 Temurin 17 CI 예제 빌드까지 확대

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
run-scrapers.bat
.utils/
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
