<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/src/main/res/mipmap-night/banner.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/src/main/res/mipmap/banner.png?raw=true" alt="banner" border="0" width="512" />
    </picture>
  </p>

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
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 當前
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

這個插件將 AutoJs6 主專案與各插件專案中重複維護的那套建置版本決策邏輯抽離出來。過去每個儲存庫的 settings.gradle.kts 都帶著數百行幾乎相同的程式碼，用來判斷當前是哪個 IDE 在建置，再據此挑選合適的 AGP 與 Kotlin 版本。

將它做成可發布的 Settings 插件之後，下游專案只需要寫十幾行引入程式碼。邏輯改進一次，所有專案升級插件版本即可獲得，不必再逐個儲存庫複製貼上。

******

### 功能

******

- 識別建置主機：Android Studio、IntelliJ IDEA、Temurin JDK，以及裸命令列環境。
- 依當前 IDE 版本挑選它所能支援的 AGP 版本，版本之間未完全相符時向下就近選取。
- IDE 版本比對應表中全部項目都新時，自動回退至 auto 選擇，避免靜默降級到過舊的 AGP。
- Temurin 與裸命令列不再使用平台版本對應表，而是明確依 Gradle 相容性自動選擇 AGP。
- 將 Android API、KSP 與專案宣告的最低 AGP 作為下限，和 IDE/Gradle 上限求交集；沒有相容交集時提前報錯。
- 決定 R8 版本，僅在 AGP 內建的 R8 不夠新時才引入外部 R8。
- 相容性資料隨插件一同散布，並作為預設的唯一資料來源；AutoJs6 官方宿主與插件專案不在使用端重複維護 `gradle/data` 副本。
- 保留 `version.properties` 中的 `OVERRIDDEN_*` 逃生門，需要確定性建置時可直接鎖定版本。
- README 與 CHANGELOG 支援西班牙文、法文、俄文、阿拉伯文、日文、韓文、英文、簡體中文、香港繁體、台灣繁體。

******

### 使用方法

******

在使用端專案的 `settings.gradle.kts` 中套用插件，位置需在 `includeBuild` 之前:

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

模組指令碼隨後即可使用 plugins DSL 宣告插件，版本取自決策結果:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

決策結果亦可透過 `gradle.extra["platformVersions"]` 以物件形式讀取.

******

### 決策流程

******

AGP 版本的決定過程分為三個步驟:

- IDE 環境以平台對應表最早的 key 作為中央支援下限，並以配對到的 AGP 作為上限；使用端的 IDE 最低版本只能收緊該下限。對較新的 IDE 保留對應表落後回退；Temurin 與裸命令列直接採用 Gradle 相容上限。
- 依 AGP 與 Gradle 的官方相容表再次設定上限，確保候選版本可由當前 Gradle 載入。
- 從 compileSdk/targetSdk、KSP 與選用的專案最低版本推導下限，僅在上下限存在交集時回傳 AGP。

Kotlin 版本則跟隨 Gradle 而非 IDE，始終選取當前 Gradle 支援的最新版本.

******

### 指定版本

******

若因測試或確定性建置而需要固定版本，可以在 `version.properties` 中直接指定:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

值為 `NONE` 或留空時表示不固定。僅在中央機制無法推導真實專案專用下限時，才使用 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`；AutoJs6 官方使用端倉庫不得用它重複宣告平台已保證的通用 AGP 9 下限。數字形式的 `COMPILE_SDK_VERSION` 與 `TARGET_SDK_VERSION` 會自動納入判斷。同樣地，`MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` 與 `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` 只是選用的專案專用收緊項目：各中央 IDE 對應表最早的 key 是不可降低的基線，專案沒有更高要求時應省略這兩個屬性.

******

### 相容性資料

******

決策依據的資料檔案如下，它們隨插件一同散布:

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

使用端 `gradle/data` 中的同名檔案仍會優先生效，但此能力僅為舊版相容或臨時診斷保留，並非官方常態。AutoJs6 官方宿主與插件專案不得提交這類覆寫；應在本中央倉庫更新相容性資料，並隨新的不可變插件版本發布.

******

### 資料更新

******

開發者可從儲存庫根目錄執行互動式批次檔，以更新所有相容性資料:

```bat
run-scrapers.bat
```

每日工作流程會更新並驗證資料，僅在語意資料有變化時建立修訂版提交與標籤，接著啟動受保護的雙儲存庫及 GitHub Release 發布鏈；仍可手動使用 check 與 update-pr 模式:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` 不會修改工作區：結束碼 `0` 表示資料為最新，`2` 表示發現更新，`1` 表示工作失敗.

完整的更新範圍與執行約定請參閱 [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### 發行歷史

******

# v1.7.2

###### 2026/09/03

* `修復` 合併 Android Studio 透過 Gradle `-P` 提供的識別屬性與 JVM 系統屬性，Quail 3 不再被截斷為 `2026.1` 並誤選 AGP 9.2.1，JDK 25/26 的自動目標因而可正常設定
* `優化` 新增可明確傳入 Gradle 專案屬性且維持二進位相容的 Facade 多載，並為尚未擷取的 IDE build 加入 strict 版本後援；已驗證 Quail 3 在 Gradle 9.5/9.7 與 JDK 25/26 下自動選擇 AGP 9.3.2 並成功建立 Kotlin/KSP 工作

# v1.7.1

###### 2026/09/02

* `修復` 強制以每份 IDE 相容性對應的最早項目作為中央支援下界；取用端 `MIN_SUPPORTED_*_IDE_VERSION` 只能收緊該範圍，不再允許不受支援的舊 IDE 落入僅依 Gradle 選擇 AGP 的回退路徑
* `優化` 已滿足的 AGP 最低約束仍以機器可讀結果公開，但不再干擾一般成功摘要；不相容錯誤現在會包含偵測到的 IDE 版本和完整約束來源
* `優化` 明確內建相容性資料是 AutoJs6 官方取用端的權威來源；`gradle/data` 覆寫僅為舊版相容或臨時診斷保留

# v1.7.0

###### 2026/09/02

* `提示` 一般建置應宣告 SDK 版本，並僅在必要時宣告 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`；`OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` 應保留給刻意進行的精確版本測試或特殊逃生用途
* `新增` AGP 選擇現在會將 Android API、專案與 KSP 下界同目前 Gradle 和 IDE 上界求交集，沒有相容版本時會回報約束來源
* `新增` 新增 GitHub Actions 工作流程，用於 Temurin 建置、定期相容性資料檢查或更新 PR，以及受標籤和人工審核保護的 Maven Central／Gradle Plugin Portal 發布
* `修復` Temurin 與純命令列建置不再查詢舊有的 JDK 至 AGP 對應，因此 JDK `21.0.6+7` 不會再靜默選擇 AGP 8.7.3；Android API 36 現在會自動要求 AGP 8.9.1 或更新版本
* `修復` 修復兩段式 IDE 對應繞過 Gradle 的 AGP 上限，以及舊 Gradle 回退至本身無法載入的平台版本問題
* `優化` 新增獨立擷取的 Android API 至最低 AGP 官方資料，並更新 Android Studio、AGP 發行版及 AGP／Gradle 相容性資料
* `優化` 驗證範圍擴充至 70 項 JVM 測試、Node 解析與冪等測試，以及在真實 Temurin 17 CI 中執行無介面自動選擇的範例建置

##### 更多發行歷史可參閱

* [CHANGELOG-zh-Hant-TW.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)

******

### 建置

******

```powershell
.\gradlew.bat build
```

發布至本機 Maven 儲存庫:

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

決策邏輯位於 `src/main/kotlin`，相容性資料作為資源打包在 `src/main/resources`；`sample` 是一個最小的使用端專案，用於驗證決策結果。README 與 CHANGELOG 由 `.python/generate_markdown.py` 依據 JSON 原始檔案生成.

******

### 相關連結

******

- AutoJs6 主專案: https://github.com/SuperMonster003/AutoJs6
- Android Gradle 插件發行說明: https://developer.android.com/build/releases/gradle-plugin
- Gradle 相容性矩陣: https://docs.gradle.org/current/userguide/compatibility.html
