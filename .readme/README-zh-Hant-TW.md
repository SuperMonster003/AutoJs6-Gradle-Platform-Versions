<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>為 AutoJs6 生態自動決定 AGP 與 Kotlin 插件版本的 Gradle Settings 插件</p>

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
- 依 AGP 與 Gradle 的相容關係設定上限，確保選出的版本當前 Gradle 一定能載入。
- 決定 KSP 版本，並在所選 KSP 要求更高的 AGP 時自動提升 AGP 版本。
- 決定 R8 版本，僅在 AGP 內建的 R8 不夠新時才引入外部 R8。
- 相容性資料隨插件一同散布，使用端專案若存在 `gradle/data` 目錄則優先採用，便於緊急修正資料。
- 保留 `version.properties` 中的 `OVERRIDDEN_*` 逃生門，需要確定性建置時可直接鎖定版本。
- README 與 CHANGELOG 支援西班牙文、法文、俄文、阿拉伯文、日文、韓文、英文、簡體中文、香港繁體、台灣繁體。

******

### 使用方法

******

在使用端專案的 `settings.gradle.kts` 中套用插件，位置需在 `includeBuild` 之前:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.2.0"
    }
}

plugins {
    id("org.autojs.build.platform-versions")
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

- 以當前平台版本在該平台的 AGP 對應表中進行就近向下比對。
- 判斷對應表是否落後，亦即當前 IDE 是否比表中全部項目都新；若是則回退至 auto 選擇。
- 依 AGP 與 Gradle 的相容表設定上限，結果不會超出當前 Gradle 能載入的範圍。

Kotlin 版本則跟隨 Gradle 而非 IDE，始終選取當前 Gradle 支援的最新版本.

******

### 指定版本

******

若希望跳過所有自動決策，可以在 `version.properties` 中直接指定版本:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

值為 `NONE` 或留空時表示不指定，走自動決策流程.

******

### 相容性資料

******

決策依據的資料檔案如下，它們隨插件一同散布:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

使用端專案若在自己的 `gradle/data` 目錄下放置同名檔案，則該檔案優先生效.

******

### 發行歷史

******

# v1.2.0

###### 2026/08/18

* `新增` 模組指令碼改造指令碼 `.python/migrate_modules.py`，把未指定版本的插件套用改為帶版本的形式，版本取自系統屬性
* `新增` 決策出的 KSP 版本新增以 `gradle.ksp.version` 系統屬性發布，與 AGP 及 Kotlin 的命名對齊
* `修復` 模組改造指令碼的回復未能還原原始檔案，且會遺留備份檔案
* `優化` settings 遷移指令碼會先檢查模組指令碼是否就緒，未就緒時給出提示而不改寫，避免留下無法建置的中間狀態
* `優化` settings 遷移指令碼改為將插件併入既有的 plugins 區塊並移到 `includeBuild` 之前，而非新增一個區塊

# v1.1.0

###### 2026/08/18

* `新增` R8 版本決策，依據當前 Kotlin 版本查表，僅在 AGP 內建的 R8 不夠新時才顯式引入外部 R8
* `新增` KSP 版本決策，版本號跟隨目標 Kotlin 版本；當所選 KSP 要求更高的 AGP 時自動提升 AGP 版本
* `新增` 決策結果新增 `PlatformVersionsFacade` 呼叫入口，可在 settings 指令碼本體中直接使用
* `新增` 決策結果同時以系統屬性發布，供模組指令碼以 plugins DSL 方式宣告插件版本
* `新增` 下游儲存庫批次遷移指令碼 `.python/migrate_downstream.py`，支援預覽、套用、回復，逐一儲存庫保留備份
* `修復` `getMaxSupportedJavaVersion` 先前誤傳 AGP 版本，導致工具鏈上限被壓低；現改為傳入 Gradle 版本
* `優化` 移除 IntelliJ IDEA 對應表中 2026.2.1 的項目，使 2026.2 與 2026.2.1 均得到 AGP 9.0.1，與 IDE 實際支援範圍一致

# v1.0.0

###### 2026/08/18

* `新增` Gradle Settings 插件 `org.autojs.build.platform-versions`，用於自動決定 AGP 與 Kotlin Gradle 插件版本
* `新增` 建置主機識別，支援 Android Studio、IntelliJ IDEA、Temurin JDK 以及裸命令列環境
* `新增` AGP 版本決策，依當前 IDE 版本在對應表中就近向下比對
* `新增` 對應表落後回退，當前 IDE 比表中全部項目都新時改用 auto 選擇，不再靜默降級到過舊的 AGP
* `新增` AGP 版本依 Gradle 相容表設定上限，確保選出的版本當前 Gradle 一定能載入
* `新增` Kotlin Gradle 插件版本決策，跟隨當前 Gradle 支援的最新版本
* `新增` 相容性資料隨插件一同散布，使用端專案的 `gradle/data` 目錄可覆寫同名資料檔案
* `新增` `version.properties` 中的 `OVERRIDDEN_*` 逃生門，可直接指定版本以跳過自動決策
* `新增` 決策結果透過 `PlatformVersionsExtension` 公開，可用於 buildscript 的 classpath 宣告
* `新增` 最小使用端專案 `sample`，用於驗證三種典型情境下的決策結果
* `新增` README 與 CHANGELOG 的多語言資源：西班牙文、法文、俄文、阿拉伯文、日文、韓文、英文、簡體中文、香港繁體、台灣繁體

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
