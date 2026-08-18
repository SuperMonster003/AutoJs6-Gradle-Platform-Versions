<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>為 AutoJs6 生態自動決定 AGP 與 Kotlin 插件版本的 Gradle Settings 插件</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.1.0+-02303A"/></a>
    <a href="https://developer.android.com/build/releases/gradle-plugin"><img alt="AGP" src="https://img.shields.io/badge/AGP-9.0+-3DDC84"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2025.2.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2026.1.2+-EE4677"/></a>
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
        id("org.autojs.build.platform-versions") version "1.4.1"
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

# v1.4.1

###### 2026/08/18

* `優化` 映射表滯後時不再向控制台輸出解釋性註記. 版本行末尾的 [auto-specified] 後綴已足以說明來由, 而該註記比它所解釋的摘要還長
* `優化` 隨之移除 notes 相關介面: PlatformVersionsExtension.notes 與 Formatted 的 notes 參數不再存在, 讀取過該屬性的消費端腳本需一併調整

# v1.4.0

###### 2026/08/18

* `修復` IDE 版本僅為補丁級更新時不再放寬 AGP 上限. 此前 IntelliJ IDEA 2026.2.1 會取到 AGP 9.2.1 而被 IDE 拒絕, 現在與 2026.2 一樣停留在 9.1 線
* `優化` IntelliJ IDEA 映射表補入 2026.2 條目, 採用 IDE 自報的 AGP 上限
* `優化` 遷移方案改為在根構建腳本聲明一次插件版本, 模塊腳本無須改動; 此前逐模塊添加版本的方式無法用於 Groovy 模塊, 因為其 plugins 塊只接受字符串字面量
* `優化` 控制台的註記移至版本摘要下方單獨成段, 不再與版本行交錯

# v1.3.0

###### 2026/08/18

* `提示` 本次不再支援 Gradle 8. AGP 9.0 是首個要求 Gradle 9 的版本, 因此支援範圍自 AGP 9.0 起
* `優化` 兼容數據表剔除 9 以前的條目, IntelliJ IDEA 映射表僅保留給出 AGP 9 的條目
* `優化` 當前 Gradle 舊於全部兼容條目時不再回落到最低條目, 改為明確報錯, 避免把無法載入的版本放到 classpath 上
* `優化` 最低支援版本上調: Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `優化` README 徽章同步上述版本, 並新增 AGP 徽章
* `優化` 遷移腳本支援 kotlin(...) 語法糖與 kotlin-android/kotlin-kapt/kotlin-parcelize 等舊式短名, 短名會展開為完整插件 id
* `優化` 遷移腳本跳過兩類無法遷移的倉庫: 使用 apply(from=) 引入且引用 AGP 類型的腳本片段, 以及啟用了依賴校驗的倉庫

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
