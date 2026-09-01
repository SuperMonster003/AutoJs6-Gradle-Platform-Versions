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
gradle/data/android-studio-build-version.properties
gradle/data/android-studio-codename-version.properties
gradle/data/android-studio-codename.properties
gradle/data/kotlin-r8-compat.properties
gradle/data/ksp-agp-compat.properties
gradle/data/ksp-releases.properties
```

使用端專案若在自己的 `gradle/data` 目錄下放置同名檔案，則該檔案優先生效.

******

### 資料更新

******

開發者可從儲存庫根目錄執行互動式批次檔，以更新所有相容性資料:

```bat
run-scrapers.bat
```

未來的排程 CI 可使用下列唯讀檢查進入點:

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

# v1.6.0

###### 2026/08/29

* `提示` 永久 Gradle 外掛程式 ID 已由 `org.autojs.build.platform-versions` 改為 `io.github.supermonster003.autojs6-platform-versions`，Maven 座標為 `io.github.supermonster003:autojs6-gradle-platform-versions`；Java／Kotlin 套件名稱仍為 `org.autojs.build.platform`
* `新增` 新增面向 Maven Central 與 Gradle Plugin Portal 的首次公開線上發布流程，實作成品、原始碼、Javadoc、模組中繼資料及外掛程式 marker 均附有正式簽章
* `優化` 新增公開 GitHub 儲存庫、完整 Central POM 中繼資料、可重現 Portal bundle、隔離取用端驗證，以及分別供本機 GPG 與 CI 使用的安全簽章入口
* `優化` 正式用法現可僅透過公共儲存庫解析，不再依賴 `mavenLocal()`；遷移工具也可辨識並更新舊外掛程式 ID

# v1.5.0

###### 2026/08/28

* `新增` 相容性資料更新工具已完整移入本儲存庫，新增供開發者手動更新的互動式 `run-scrapers.bat`，並預留跨平台更新與唯讀檢查命令供日後定期 CI 使用
* `優化` 擷取器不再依賴 Puppeteer／Chrome，改為解析官方靜態來源；保留邊界與輸出驗證集中設定，並避免只因時間戳記變化而重寫檔案
* `優化` 內建資料已更新至 Gradle 9.7／Kotlin 2.4、AGP 9.5.0-alpha03、9.4.0-rc02 與 9.3.2、Android Studio Rabbit 及 KSP 2.3.11

# v1.4.1

###### 2026/08/18

* `優化` 對應表落後時不再向主控台輸出解釋性註記。版本行末尾的 [auto-specified] 後綴已足以說明來由，而該註記比它所解釋的摘要還長
* `優化` 隨之移除 notes 相關介面：PlatformVersionsExtension.notes 與 Formatted 的 notes 參數不再存在，讀取過該屬性的取用端指令碼需一併調整

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
