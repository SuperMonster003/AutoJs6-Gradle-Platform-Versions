******

### 語言 (Languages)

******

當前 CHANGELOG.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 當前
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### 發行歷史

******

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
