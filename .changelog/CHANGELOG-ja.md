******

### 言語 (Languages)

******

現在 CHANGELOG.md は以下の言語に対応しています:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### リリース履歴

******

# v1.1.0

###### 2026/08/18

* `機能` R8 バージョンの決定。現在の Kotlin バージョンをもとに表を参照し、AGP に同梱された R8 が十分に新しくない場合にのみ外部の R8 を明示的に導入
* `機能` KSP バージョンの決定。バージョン番号は対象の Kotlin バージョンに追従し、選択された KSP がより新しい AGP を要求する場合は AGP バージョンを自動的に引き上げ
* `機能` 決定結果に `PlatformVersionsFacade` という呼び出し口を追加。settings スクリプト本体から直接利用可能
* `機能` 決定結果をシステムプロパティとしても公開。モジュールのスクリプトから plugins DSL でプラグインのバージョンを宣言可能
* `機能` 下流リポジトリの一括移行スクリプト `.python/migrate_downstream.py`。プレビュー、適用、ロールバックに対応し、リポジトリごとにバックアップを保持
* `修正` `getMaxSupportedJavaVersion` にこれまで誤って AGP バージョンを渡しており、ツールチェーンの上限が引き下げられていた。現在は Gradle バージョンを渡すように修正
* `改善` IntelliJ IDEA のマッピング表から 2026.2.1 の項目を削除し、2026.2 と 2026.2.1 のどちらでも AGP 9.0.1 が得られるようにして、IDE の実際の対応範囲と一致させた

# v1.0.0

###### 2026/08/18

* `機能` AGP と Kotlin Gradle プラグインのバージョンを自動決定する Gradle Settings プラグイン `org.autojs.build.platform-versions`
* `機能` ビルドホストの識別。Android Studio、IntelliJ IDEA、Temurin JDK および素のコマンドライン環境に対応
* `機能` AGP バージョンの決定。現在の IDE バージョンをもとにマッピング表で直近の下位一致を探索
* `機能` マッピング表が古い場合のフォールバック。現在の IDE が表のすべての項目より新しいときは auto 選択に切り替え、古すぎる AGP へ暗黙にダウングレードしない
* `機能` AGP バージョンに Gradle 互換表による上限を適用し、選ばれたバージョンを現在の Gradle が必ずロードできることを保証
* `機能` Kotlin Gradle プラグインバージョンの決定。現在の Gradle が対応する最新バージョンに追従
* `機能` 互換性データをプラグインに同梱して配布。利用側プロジェクトの `gradle/data` ディレクトリで同名のデータファイルを上書き可能
* `機能` `version.properties` の `OVERRIDDEN_*` という避難口。バージョンを直接指定して自動決定をスキップ可能
* `機能` 決定結果を `PlatformVersionsExtension` 経由で公開。buildscript の classpath 宣言に利用可能
* `機能` 最小の利用側プロジェクト `sample`。3 つの典型的なシナリオでの決定結果を検証
* `機能` README と CHANGELOG の多言語リソース: スペイン語、フランス語、ロシア語、アラビア語、日本語、韓国語、英語、簡体中国語、繁体中国語 (香港)、繁体中国語 (台湾)
