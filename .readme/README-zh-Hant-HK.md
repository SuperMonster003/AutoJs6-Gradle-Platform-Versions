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
- 按 AGP 與 Gradle 的兼容關係封頂, 保證選出的版本當前 Gradle 一定能載入.
- 決定 KSP 版本, 並在所選 KSP 要求更高 AGP 時自動抬升 AGP 版本.
- 決定 R8 版本, 僅在 AGP 自帶的 R8 不夠新時才引入外部 R8.
- 兼容數據隨插件分發, 消費端項目若存在 `gradle/data` 目錄則優先使用, 便於緊急修數據.
- 保留 `version.properties` 中的 `OVERRIDDEN_*` 逃生門, 需要確定性構建時可直接釘死版本.
- README 與 CHANGELOG 支援西班牙語/法語/俄語/阿拉伯語/日語/韓語/英語/簡體中文/香港繁體/台灣繁體.

******

### 使用方法

******

在消費端項目的 `settings.gradle.kts` 裡應用插件, 位置需在 `includeBuild` 之前:

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

- 用當前平台版本在該平台的 AGP 映射表裡做就近向下匹配.
- 判斷映射表是否滯後, 即當前 IDE 是否比表中全部條目都新; 若是則回退到 auto 選擇.
- 按 AGP 與 Gradle 的兼容表封頂, 結果不會超出當前 Gradle 能載入的範圍.

Kotlin 版本則跟隨 Gradle 而非 IDE, 始終選取當前 Gradle 支援的最新版本.

******

### 指定版本

******

如果希望跳過全部自動決策, 可以在 `version.properties` 裡直接指定版本:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

值為 `NONE` 或留空時表示不指定, 走自動決策流程.

******

### 兼容數據

******

決策依據的數據文件如下, 它們隨插件一同分發:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

消費端項目如果在自己的 `gradle/data` 目錄下放了同名文件, 則該文件優先生效.

******

### 發行歷史

******

# v1.2.0

###### 2026/08/18

* `新增` 模塊腳本改造腳本 `.python/migrate_modules.py`, 把無版本的插件應用改為帶版本形式, 版本取自系統屬性
* `新增` 決策出的 KSP 版本新增以 `gradle.ksp.version` 系統屬性發佈, 與 AGP 和 Kotlin 的命名對齊
* `修復` 模塊改造腳本的回滾未能還原原始文件, 且會遺留備份文件
* `優化` settings 遷移腳本會先檢查模塊腳本是否就緒, 未就緒時給出提示而不改寫, 避免留下無法構建的中間狀態
* `優化` settings 遷移腳本改為把插件併入已有的 plugins 塊並移到 `includeBuild` 之前, 而非新增一個塊

# v1.1.0

###### 2026/08/18

* `新增` R8 版本決策, 依據當前 Kotlin 版本查表, 僅在 AGP 自帶的 R8 不夠新時才顯式引入外部 R8
* `新增` KSP 版本決策, 版本號跟隨目標 Kotlin 版本; 當所選 KSP 要求更高的 AGP 時自動抬升 AGP 版本
* `新增` 決策結果新增 `PlatformVersionsFacade` 調用入口, 可在 settings 腳本體中直接使用
* `新增` 決策結果同時以系統屬性發佈, 供模塊腳本以 plugins DSL 方式聲明插件版本
* `新增` 下游倉庫批量遷移腳本 `.python/migrate_downstream.py`, 支援預覽/應用/回滾, 逐倉保留備份
* `修復` `getMaxSupportedJavaVersion` 此前誤傳 AGP 版本, 導致工具鏈上限被壓低; 現改為傳入 Gradle 版本
* `優化` 移除 IntelliJ IDEA 映射表中 2026.2.1 的條目, 使 2026.2 與 2026.2.1 均得到 AGP 9.0.1, 與 IDE 實際支援範圍一致

# v1.0.0

###### 2026/08/18

* `新增` Gradle Settings 插件 `org.autojs.build.platform-versions`, 用於自動決定 AGP 與 Kotlin Gradle 插件版本
* `新增` 構建宿主識別, 支援 Android Studio/IntelliJ IDEA/Temurin JDK 以及裸命令行環境
* `新增` AGP 版本決策, 按當前 IDE 版本在映射表中就近向下匹配
* `新增` 映射表滯後回退, 當前 IDE 比表中全部條目都新時改用 auto 選擇, 不再靜默降級到過舊的 AGP
* `新增` AGP 版本按 Gradle 兼容表封頂, 保證選出的版本當前 Gradle 一定能載入
* `新增` Kotlin Gradle 插件版本決策, 跟隨當前 Gradle 支援的最新版本
* `新增` 兼容數據隨插件分發, 消費端項目的 `gradle/data` 目錄可覆蓋同名數據文件
* `新增` `version.properties` 中 `OVERRIDDEN_*` 逃生門, 可直接指定版本以跳過自動決策
* `新增` 決策結果通過 `PlatformVersionsExtension` 暴露, 可用於 buildscript 的 classpath 聲明
* `新增` 最小消費端工程 `sample`, 用於驗證三種典型場景下的決策結果
* `新增` README 與 CHANGELOG 的多語言資源: 西班牙語/法語/俄語/阿拉伯語/日語/韓語/英語/簡體中文/香港繁體/台灣繁體

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
