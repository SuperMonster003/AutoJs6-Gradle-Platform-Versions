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

# v1.7.0

###### 2026/09/02

* `提示` 一般建置應宣告 SDK 版本，並僅在必要時宣告 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`；`OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` 應保留給刻意進行的精確版本測試或特殊逃生用途
* `新增` AGP 選擇現在會將 Android API、專案與 KSP 下界同目前 Gradle 和 IDE 上界求交集，沒有相容版本時會回報約束來源
* `新增` 新增 GitHub Actions 工作流程，用於 Temurin 建置、定期相容性資料檢查或更新 PR，以及受標籤和人工審核保護的 Maven Central／Gradle Plugin Portal 發布
* `修復` Temurin 與純命令列建置不再查詢舊有的 JDK 至 AGP 對應，因此 JDK `21.0.6+7` 不會再靜默選擇 AGP 8.7.3；Android API 36 現在會自動要求 AGP 8.9.1 或更新版本
* `修復` 修復兩段式 IDE 對應繞過 Gradle 的 AGP 上限，以及舊 Gradle 回退至本身無法載入的平台版本問題
* `優化` 新增獨立擷取的 Android API 至最低 AGP 官方資料，並更新 Android Studio、AGP 發行版及 AGP／Gradle 相容性資料
* `優化` 驗證範圍擴充至 70 項 JVM 測試、Node 解析與冪等測試，以及在真實 Temurin 17 CI 中執行無介面自動選擇的範例建置

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

# v1.4.0

###### 2026/08/18

* `修復` IDE 版本僅為修補程式層級的更新時不再放寬 AGP 上限。先前 IntelliJ IDEA 2026.2.1 會取得 AGP 9.2.1 而遭 IDE 拒絕，現在與 2026.2 一樣停留在 9.1 線
* `優化` IntelliJ IDEA 對應表補入 2026.2 項目，採用 IDE 自行回報的 AGP 上限
* `優化` 遷移方案改為在根建置指令碼中宣告一次插件版本，模組指令碼無須改動；先前逐模組加上版本的做法無法用於 Groovy 模組，因為其 plugins 區塊只接受字串常值
* `優化` 主控台的註記移至版本摘要下方單獨成段，不再與版本行交錯

# v1.3.0

###### 2026/08/18

* `提示` 本次不再支援 Gradle 8。AGP 9.0 是首個要求 Gradle 9 的版本，因此支援範圍自 AGP 9.0 起
* `優化` 相容性資料表剔除 9 以前的項目，IntelliJ IDEA 對應表僅保留給出 AGP 9 的項目
* `優化` 當前 Gradle 舊於全部相容項目時不再回退到最低項目，改為明確報錯，避免把無法載入的版本放到 classpath 上
* `優化` 最低支援版本上調：Gradle 9.1.0、Android Studio 2025.2.3、IntelliJ IDEA 2026.1.2、AGP 9.0
* `優化` README 徽章同步上述版本，並新增 AGP 徽章
* `優化` 遷移指令碼支援 kotlin(...) 語法糖與 kotlin-android/kotlin-kapt/kotlin-parcelize 等舊式簡稱，簡稱會展開為完整的插件 id
* `優化` 遷移指令碼會跳過兩類無法遷移的儲存庫：以 apply(from=) 引入且引用 AGP 型別的指令碼片段，以及啟用了相依性驗證的儲存庫

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
