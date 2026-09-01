<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
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
- AGP와 Gradle의 호환 관계에 따라 상한을 적용해, 선택된 버전을 현재 Gradle이 반드시 로드할 수 있도록 보장합니다.
- KSP 버전을 결정하며, 선택된 KSP가 더 높은 AGP를 요구하면 AGP 버전을 자동으로 올립니다.
- R8 버전을 결정하며, AGP에 포함된 R8이 충분히 새롭지 않을 때만 외부 R8을 도입합니다.
- 호환성 데이터는 플러그인과 함께 배포되며, 소비 측 프로젝트에 `gradle/data` 디렉터리가 있으면 그쪽을 우선 사용하므로 데이터를 급히 수정하기 좋습니다.
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
        id("io.github.supermonster003.autojs6-platform-versions") version "1.6.0"
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
gradle/data/android-studio-build-version.properties
gradle/data/android-studio-codename-version.properties
gradle/data/android-studio-codename.properties
gradle/data/kotlin-r8-compat.properties
gradle/data/ksp-agp-compat.properties
gradle/data/ksp-releases.properties
```

소비 측 프로젝트가 자신의 `gradle/data` 디렉터리에 같은 이름의 파일을 두면 그 파일이 우선 적용됩니다.

******

### 데이터 업데이트

******

개발자는 저장소 루트에서 대화형 배치 진입점을 실행하여 모든 호환성 데이터를 업데이트할 수 있습니다:

```bat
run-scrapers.bat
```

향후 예약 CI 작업에서는 다음 읽기 전용 검사 진입점을 사용할 수 있습니다:

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

# v1.6.0

###### 2026/08/29

* `힌트` 영구 Gradle 플러그인 ID를 `org.autojs.build.platform-versions`에서 `io.github.supermonster003.autojs6-platform-versions`로 변경하고 Maven 좌표를 `io.github.supermonster003:autojs6-gradle-platform-versions`로 확정함. Java/Kotlin 패키지 이름은 계속 `org.autojs.build.platform`임
* `기능` Maven Central과 Gradle Plugin Portal을 위한 첫 공개 온라인 릴리스 경로를 추가하고 구현, 소스, Javadoc, 모듈 메타데이터 및 플러그인 마커 산출물에 정식 서명을 적용
* `개선` 공개 GitHub 저장소, 완전한 Central POM 메타데이터, 재현 가능한 Portal bundle, 격리된 소비자 검증, 로컬 GPG와 CI용으로 분리된 안전한 서명 경로를 추가
* `개선` 공식 사용 예제는 이제 `mavenLocal()` 없이 공개 저장소만으로 해석되며 마이그레이션 도구도 기존 플러그인 ID를 인식하고 갱신

# v1.5.0

###### 2026/08/28

* `기능` 호환성 데이터 갱신 도구 전체를 이 저장소로 이전하고, 수동 갱신용 대화형 `run-scrapers.bat`와 향후 정기 CI 실행을 위한 크로스 플랫폼 갱신 및 읽기 전용 검사 명령을 추가
* `개선` 스크레이퍼에서 Puppeteer와 Chrome 의존성을 제거하고 공식 정적 소스 분석, 보존 경계 및 출력 검증을 한곳에 모았으며 타임스탬프만 바뀐 경우의 불필요한 재작성을 방지
* `개선` 내장 데이터를 Gradle 9.7/Kotlin 2.4, AGP 9.5.0-alpha03, 9.4.0-rc02 및 9.3.2, Android Studio Rabbit, KSP 2.3.11까지 갱신

# v1.4.1

###### 2026/08/18

* `개선` 매핑 표가 뒤처진 경우 콘솔에 설명용 참고 사항을 더 이상 출력하지 않음. 버전 줄 끝의 [auto-specified] 접미사만으로도 경위를 알 수 있으며, 그 참고 사항은 설명 대상인 요약보다 길었음
* `개선` 이에 따라 notes API를 제거함. PlatformVersionsExtension.notes와 Formatted의 notes 매개변수가 사라졌으므로, 해당 속성을 읽던 소비 측 스크립트는 함께 조정해야 함

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
