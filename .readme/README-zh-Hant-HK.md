<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>為 AutoJs6 生態自動決定 AGP 與 Kotlin 插件版本的 Gradle Settings 插件</p>

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

### 語言 (Languages)

******

當前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- 繁體中文 (香港) [zh-Hant-HK] # 當前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### 簡介

******

這個插件把 AutoJs6 主項目和各插件項目裡重複維護的那套構建版本決策邏輯抽了出來. 以前每個倉庫的 settings.gradle.kts 都帶著幾百行幾乎相同的代碼, 用來判斷當前是哪個 IDE 在構建, 再據此挑選合適的 AGP 與 Kotlin 版本.

把它做成一個可發佈的 Settings 插件之後, 下游項目只需要寫十幾行引入代碼. 邏輯改進一次, 所有項目升級插件版本即可獲得, 不必再逐個倉庫複製貼上.

******

### 功能

******

- 識別構建宿主: Android Studio, IntelliJ IDEA, Temurin JDK, 以及裸命令行環境.
- 按當前 IDE 版本挑選它能支援的 AGP 版本, 版本之間不完全匹配時向下就近選取.
- IDE 版本比映射表全部條目都新時, 自動回退到 auto 選擇, 避免靜默降級到過舊的 AGP.
- Temurin 與裸命令行不再使用平台版本映射, 而是明確按 Gradle 兼容性自動選擇 AGP.
- 把 Android API、KSP 及項目聲明的最低 AGP 作為下界, 與 IDE/Gradle 上界求交; 無兼容交集時提前報錯.
- 決定 R8 版本, 僅在 AGP 自帶的 R8 不夠新時才引入外部 R8.
- 兼容數據隨插件分發並作為默認的唯一數據源; AutoJs6 官方宿主和插件項目不在消費端重複維護 `gradle/data` 副本.
- 保留 `version.properties` 中的 `OVERRIDDEN_*` 逃生門, 需要確定性構建時可直接釘死版本.
- README 與 CHANGELOG 支援西班牙語/法語/俄語/阿拉伯語/日語/韓語/英語/簡體中文/香港繁體/台灣繁體.

******

### 使用方法

******

在消費端項目的 `settings.gradle.kts` 裡應用插件, 位置需在 `includeBuild` 之前:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.1"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
}
```

模塊腳本隨後即可用 plugins DSL 聲明插件, 版本取自決策結果:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

決策結果也可通過 `gradle.extra["platformVersions"]` 以對象形式讀取.

******

### 決策流程

******

AGP 版本的決定過程分為三步:

- IDE 環境以平台映射表的最早 key 作為中央支援下界, 以匹配到的 AGP 作為上界; 消費端 IDE 最低版本只能收緊該下界. 對較新的 IDE 保留映射滯後回退; Temurin 與裸命令行直接採用 Gradle 兼容上界.
- 按 AGP 與 Gradle 的官方兼容表再次封頂, 保證候選版本可由當前 Gradle 載入.
- 從 compileSdk/targetSdk、KSP 及可選的項目最低版本推導下界, 僅在上下界存在交集時返回 AGP.

Kotlin 版本則跟隨 Gradle 而非 IDE, 始終選取當前 Gradle 支援的最新版本.

******

### 指定版本

******

如果出於測試或確定性構建需要固定版本, 可以在 `version.properties` 裡直接指定:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

值為 `NONE` 或留空時表示不固定. 只有在中央機制無法推導真實項目專用下界時, 才使用 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; AutoJs6 官方消費端倉庫不得用它重複聲明平台已保證的通用 AGP 9 下界. 數字形式的 `COMPILE_SDK_VERSION` 與 `TARGET_SDK_VERSION` 會自動參與判斷. 同樣, `MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` 與 `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` 只是可選的項目專用收緊項: 每個中央 IDE 映射表的最早 key 是不可降低的基線, 項目沒有更高要求時應省略這兩個屬性.

******

### 兼容數據

******

決策依據的數據文件如下, 它們隨插件一同分發:

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

消費端 `gradle/data` 中的同名文件仍會優先生效, 但此能力只為舊版兼容或臨時診斷保留, 並非官方常態. AutoJs6 官方宿主和插件項目不得提交這類覆蓋; 應在本中央倉庫更新兼容數據, 並隨新的不可變插件版本發佈.

******

### 數據更新

******

開發者可從倉庫根目錄運行交互式批處理來更新全部兼容數據:

```bat
run-scrapers.bat
```

每日工作流程會刷新並驗證數據, 僅在語義數據有變化時建立修訂版本提交和標籤, 隨後啟動受保護的雙倉庫與 GitHub Release 發佈鏈; 仍可手動使用 check 和 update-pr 模式:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` 不修改工作區: 退出碼 `0` 表示數據最新, `2` 表示發現更新, `1` 表示任務失敗.

完整的更新範圍與運行約定見 [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### 發行歷史

******

# v1.7.1

###### 2026/09/02

* `修復` 強制以每份 IDE 兼容映射的最早項目作為中央支援下界; 取用端 `MIN_SUPPORTED_*_IDE_VERSION` 只可收緊該範圍, 不再容許不受支援的舊 IDE 落入僅按 Gradle 選擇 AGP 的回退路徑
* `優化` 已滿足的 AGP 最低約束仍以機器可讀結果公開, 但不再干擾一般成功摘要; 不兼容錯誤現會包含偵測到的 IDE 版本和完整約束來源
* `優化` 明確內置兼容數據是 AutoJs6 官方取用端的權威來源; `gradle/data` 覆寫只為舊版兼容或臨時診斷保留

# v1.7.0

###### 2026/09/02

* `提示` 一般構建應聲明 SDK 版本, 並只在有需要時聲明 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` 應保留作刻意的精確版本測試或特殊逃生用途
* `新增` AGP 選擇現會將 Android API、項目與 KSP 下界同目前 Gradle 和 IDE 上界求交集, 沒有兼容版本時會報告約束來源
* `新增` 新增 GitHub Actions 工作流程, 用於 Temurin 構建、定期兼容數據檢查或更新 PR, 以及受標籤和人工審批保護的 Maven Central / Gradle Plugin Portal 發佈
* `修復` Temurin 與純命令列構建不再查詢舊有的 JDK 至 AGP 映射, 因此 JDK `21.0.6+7` 不會再靜默選擇 AGP 8.7.3; Android API 36 現會自動要求 AGP 8.9.1 或以上版本
* `修復` 修復兩段式 IDE 映射繞過 Gradle 的 AGP 上限, 以及舊 Gradle 回落至本身無法載入的平台版本問題
* `優化` 新增獨立抓取的 Android API 至最低 AGP 官方數據, 並刷新 Android Studio、AGP 發行版及 AGP/Gradle 兼容數據
* `優化` 驗證範圍擴展至 70 項 JVM 測試、Node 解析與冪等測試, 以及在真實 Temurin 17 CI 中執行無頭自動選擇的範例構建

# v1.6.0

###### 2026/08/29

* `提示` 永久 Gradle 插件 ID 已由 `org.autojs.build.platform-versions` 改為 `io.github.supermonster003.autojs6-platform-versions`, Maven 座標為 `io.github.supermonster003:autojs6-gradle-platform-versions`; Java/Kotlin 套件名稱仍為 `org.autojs.build.platform`
* `新增` 新增面向 Maven Central 與 Gradle Plugin Portal 的首次公開網上發佈流程, 實作構件、原始碼、Javadoc、模組中繼資料及插件 marker 均附有正式簽名
* `優化` 新增公開 GitHub 倉庫、完整 Central POM 中繼資料、可重現 Portal bundle、隔離取用端驗證, 以及分別供本機 GPG 與 CI 使用的安全簽名入口
* `優化` 正式用法現可只透過公共倉庫解析, 不再依賴 `mavenLocal()`; 遷移工具亦可識別並更新舊插件 ID

##### 更多發行歷史可參閱

* [CHANGELOG-zh-Hant-HK.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)

******

### 構建

******

```powershell
.\gradlew.bat build
```

發佈到本地 Maven 倉庫:

```powershell
.\gradlew.bat publishToMavenLocal
```

插件版本號取自 `version.properties` 的 `VERSION_NAME`.

******

### 資源結構

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

決策邏輯位於 `src/main/kotlin`, 兼容數據作為資源打包在 `src/main/resources`; `sample` 是一個最小消費端工程, 用於驗證決策結果. README 與 CHANGELOG 由 `.python/generate_markdown.py` 根據 JSON 源文件生成.

******

### 相關鏈接

******

- AutoJs6 主項目: https://github.com/SuperMonster003/AutoJs6
- Android Gradle 插件發行說明: https://developer.android.com/build/releases/gradle-plugin
- Gradle 兼容性矩陣: https://docs.gradle.org/current/userguide/compatibility.html
