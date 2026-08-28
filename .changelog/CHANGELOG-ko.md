******

### 언어 (Languages)

******

현재 CHANGELOG.md에서 지원하는 언어는 다음과 같습니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### 릴리스 기록

******

# v1.5.0

###### 2026/08/28

* `기능` 호환성 데이터 갱신 도구 전체를 이 저장소로 이전하고, 수동 갱신용 대화형 `run-scrapers.bat`와 향후 정기 CI 실행을 위한 크로스 플랫폼 갱신 및 읽기 전용 검사 명령을 추가
* `개선` 스크레이퍼에서 Puppeteer와 Chrome 의존성을 제거하고 공식 정적 소스 분석, 보존 경계 및 출력 검증을 한곳에 모았으며 타임스탬프만 바뀐 경우의 불필요한 재작성을 방지
* `개선` 내장 데이터를 Gradle 9.7/Kotlin 2.4, AGP 9.5.0-alpha03, 9.4.0-rc02 및 9.3.2, Android Studio Rabbit, KSP 2.3.11까지 갱신

# v1.4.1

###### 2026/08/18

* `개선` 매핑 표가 뒤처진 경우 콘솔에 설명용 참고 사항을 더 이상 출력하지 않음. 버전 줄 끝의 [auto-specified] 접미사만으로도 경위를 알 수 있으며, 그 참고 사항은 설명 대상인 요약보다 길었음
* `개선` 이에 따라 notes API를 제거함. PlatformVersionsExtension.notes와 Formatted의 notes 매개변수가 사라졌으므로, 해당 속성을 읽던 소비 측 스크립트는 함께 조정해야 함

# v1.4.0

###### 2026/08/18

* `수정` IDE 버전이 패치 수준 업데이트일 뿐인 경우 더 이상 AGP 상한을 완화하지 않음. 이전에는 IntelliJ IDEA 2026.2.1이 AGP 9.2.1을 얻어 IDE에 거부되었으나, 이제 2026.2와 마찬가지로 9.1 라인에 머무름
* `개선` IntelliJ IDEA 매핑 표에 2026.2 항목을 추가하고, AGP 상한은 IDE가 스스로 보고하는 값을 사용
* `개선` 마이그레이션 방식을 바꾸어 플러그인 버전을 루트 빌드 스크립트에서 한 번만 선언하고 모듈 스크립트는 그대로 둠. 이전처럼 모듈마다 버전을 붙이는 방식은 Groovy 모듈에서 쓸 수 없었는데, plugins block이 문자열 리터럴만 받기 때문
* `개선` 콘솔의 참고 사항을 버전 요약 아래 별도 문단으로 옮겨 버전 줄 사이에 섞이지 않도록 함

# v1.3.0

###### 2026/08/18

* `힌트` 이번부터 Gradle 8은 지원하지 않음. AGP 9.0이 Gradle 9을 요구하는 첫 버전이므로 지원 범위는 AGP 9.0부터 시작함
* `개선` 호환성 데이터 표에서 9 이전 항목을 제거하고, IntelliJ IDEA 매핑 표는 AGP 9을 제공하는 항목만 남김
* `개선` 현재 Gradle이 모든 호환 항목보다 오래된 경우 더 이상 가장 낮은 항목으로 내려가지 않고 명확하게 오류를 보고하도록 변경, 로드할 수 없는 버전이 classpath에 올라가지 않도록 함
* `개선` 최소 지원 버전 상향: Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `개선` README 배지를 위 버전에 맞추고 AGP 배지를 새로 추가
* `개선` 마이그레이션 스크립트가 kotlin(...) 문법 설탕과 kotlin-android/kotlin-kapt/kotlin-parcelize 같은 옛 방식의 짧은 이름을 지원하며, 짧은 이름은 완전한 플러그인 id로 확장됨
* `개선` 마이그레이션 스크립트가 마이그레이션할 수 없는 두 종류의 저장소를 건너뜀: apply(from=) 방식으로 가져온 스크립트 조각이 AGP 타입을 참조하는 저장소와, 의존성 검증이 켜져 있는 저장소

# v1.2.0

###### 2026/08/18

* `기능` 모듈 스크립트 개조 스크립트 `.python/migrate_modules.py`, 버전이 없는 플러그인 적용을 버전이 있는 형태로 바꾸며 버전은 system property에서 가져옴
* `기능` 결정된 KSP 버전을 `gradle.ksp.version` system property로도 게시, AGP 및 Kotlin의 명명과 맞춤
* `수정` 모듈 개조 스크립트의 롤백이 원본 파일을 복원하지 못하고 백업 파일을 남기던 문제
* `개선` settings 마이그레이션 스크립트가 모듈 스크립트의 준비 여부를 먼저 확인하고, 준비되지 않았으면 안내만 하고 수정하지 않아 빌드할 수 없는 중간 상태를 남기지 않음
* `개선` settings 마이그레이션 스크립트가 플러그인을 기존 plugins block에 합치고 `includeBuild` 앞으로 옮기도록 변경, 새 블록을 추가하지 않음

# v1.1.0

###### 2026/08/18

* `기능` R8 버전 결정, 현재 Kotlin 버전을 기준으로 표를 조회하며, AGP에 포함된 R8이 충분히 새롭지 않을 때만 외부 R8을 명시적으로 도입
* `기능` KSP 버전 결정, 버전 번호는 대상 Kotlin 버전을 따르며, 선택된 KSP가 더 높은 AGP를 요구하면 AGP 버전을 자동으로 올림
* `기능` 결정 결과에 `PlatformVersionsFacade` 호출 진입점 추가, settings 스크립트 본문에서 바로 사용 가능
* `기능` 결정 결과를 system property로도 게시, 모듈 스크립트가 plugins DSL 방식으로 플러그인 버전을 선언할 수 있음
* `기능` 다운스트림 저장소 일괄 마이그레이션 스크립트 `.python/migrate_downstream.py`, 미리 보기/적용/롤백을 지원하며 저장소마다 백업을 남김
* `수정` `getMaxSupportedJavaVersion`에 이전에는 AGP 버전을 잘못 전달하여 toolchain 상한이 낮아졌으나, 이제 Gradle 버전을 전달하도록 변경
* `개선` IntelliJ IDEA 매핑 표에서 2026.2.1 항목을 제거하여 2026.2와 2026.2.1 모두 AGP 9.0.1을 얻도록 하고, IDE의 실제 지원 범위와 일치시킴

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
