******

### 語言 (Languages)

******

當前 CHANGELOG.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- 繁體中文 (香港) [zh-Hant-HK] # 當前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
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
