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

# v1.7.1

###### 2026/09/02

* `修正` 各 IDE 互換性マップの最古エントリを中央のサポート下限として強制。利用側の `MIN_SUPPORTED_*_IDE_VERSION` はこの範囲を狭めることしかできず、未サポートの古い IDE を Gradle のみに基づくフォールバックへ流せないように修正
* `改善` 満たされた AGP 最小要件は機械可読な結果として維持しつつ、通常の成功ビルド概要には表示しないように変更。非互換エラーには検出した IDE バージョンとすべての要件元を表示
* `改善` 組み込み互換性データを AutoJs6 公式利用プロジェクトの信頼できる情報源として明確化。`gradle/data` の上書きは旧版互換性または一時診断のためにのみ維持

# v1.7.0

###### 2026/09/02

* `ヒント` 通常のビルドでは SDK レベルを宣言し、必要な場合に限って `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` を指定してください。`OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` は、意図した厳密なバージョンテストや例外的な回避用途のために残してください
* `機能` AGP 選択で Android API、プロジェクト、KSP の下限と、使用中の Gradle および IDE の上限を交差させ、互換バージョンが存在しない場合は制約元を報告するように変更
* `機能` Temurin ビルド、互換性データの定期検査または更新 PR、タグと承認で保護された Maven Central／Gradle Plugin Portal 公開のための GitHub Actions ワークフローを追加
* `修正` Temurin および純粋なコマンドラインビルドで旧 JDK→AGP マッピングを参照しないようにし、JDK `21.0.6+7` が AGP 8.7.3 を暗黙に選ぶ問題を解消。Android API 36 は AGP 8.9.1 以上を自動的に要求
* `修正` 2 要素の IDE マッピングが Gradle の AGP 上限を迂回する問題と、古い Gradle が読み込めないプラットフォームバージョンへフォールバックする問題を修正
* `改善` Android API と最小 AGP の公式データを独立スクレイピング対象として追加し、Android Studio、AGP リリース、AGP／Gradle 互換性データを更新
* `改善` 検証を 70 件の JVM テスト、Node の解析・冪等性テスト、およびヘッドレス自動選択を実行する実際の Temurin 17 CI サンプルビルドまで拡充

# v1.6.0

###### 2026/08/29

* `ヒント` 恒久的な Gradle プラグイン ID を `org.autojs.build.platform-versions` から `io.github.supermonster003.autojs6-platform-versions` へ変更し、Maven 座標を `io.github.supermonster003:autojs6-gradle-platform-versions` に設定。Java／Kotlin パッケージ名は引き続き `org.autojs.build.platform`
* `機能` Maven Central と Gradle Plugin Portal に向けた初の公開オンラインリリース経路を追加し、実装、ソース、Javadoc、モジュールメタデータ、プラグインマーカーの各成果物に正式署名を付与
* `改善` 公開 GitHub リポジトリ、完全な Central POM メタデータ、再現可能な Portal bundle、隔離した利用側検証、およびローカル GPG と CI 向けに分離した安全な署名経路を追加
* `改善` 正式な使用例は `mavenLocal()` なしで公開リポジトリだけから解決するようになり、移行ツールも旧プラグイン ID を認識して更新

# v1.5.0

###### 2026/08/28

* `機能` 互換性データ更新スイートをこのリポジトリへ全面移行し、手動更新用の対話型 `run-scrapers.bat` と、将来の定期 CI 実行に備えたクロスプラットフォームの更新・読み取り専用検査コマンドを追加
* `改善` スクレイパーの Puppeteer／Chrome 依存を除去し、公式の静的ソース解析、保持下限、出力検証を一元化。タイムスタンプだけが変わる不要な再書き込みも抑制
* `改善` 組み込みデータを Gradle 9.7／Kotlin 2.4、AGP 9.5.0-alpha03・9.4.0-rc02・9.3.2、Android Studio Rabbit、KSP 2.3.11 まで更新

# v1.4.1

###### 2026/08/18

* `改善` マッピング表が古くなっている場合に、説明用の注記をコンソールへ出力しないようにした。バージョン行の末尾に付く [auto-specified] だけで由来は分かるうえ、注記はそれが説明する概要よりも長かった
* `改善` あわせて notes の API を削除。PlatformVersionsExtension.notes と Formatted の notes 引数はなくなったため、このプロパティを読んでいた利用側のスクリプトは修正が必要である

# v1.4.0

###### 2026/08/18

* `修正` IDE のバージョンがパッチレベルの更新にとどまる場合、AGP の上限を緩めないように修正。これまで IntelliJ IDEA 2026.2.1 では AGP 9.2.1 が得られて IDE に拒否されていたが、現在は 2026.2 と同じく 9.1 系にとどまる
* `改善` IntelliJ IDEA のマッピング表に 2026.2 の項目を追加。AGP の上限には IDE 自身が報告する値を採用
* `改善` 移行方式を変更し、プラグインのバージョンをルートのビルドスクリプトで一度だけ宣言するようにして、モジュールのスクリプトには手を加えない。これまでのモジュールごとにバージョンを付ける方式は Groovy のモジュールでは使えなかった。plugins ブロックが文字列リテラルしか受け付けないためである
* `改善` コンソールの注記をバージョン概要の下へ移し、独立した段落とした。バージョンの行と交互に並ぶことはなくなった

# v1.3.0

###### 2026/08/18

* `ヒント` 今回より Gradle 8 は非対応となりました。AGP 9.0 は Gradle 9 を要求する最初のバージョンであるため、対応範囲は AGP 9.0 以降となります
* `改善` 互換性データ表から 9 より前の項目を削除し、IntelliJ IDEA のマッピング表は AGP 9 が得られる項目のみを残すようにした
* `改善` 現在の Gradle がすべての互換項目より古い場合、最も低い項目へのフォールバックをやめ、明示的にエラーを報告するように変更。ロードできないバージョンが classpath に載ることを防ぐ
* `改善` 最低対応バージョンを引き上げ: Gradle 9.1.0、Android Studio 2025.2.3、IntelliJ IDEA 2026.1.2、AGP 9.0
* `改善` README のバッジを上記のバージョンに合わせ、AGP のバッジを追加
* `改善` 移行スクリプトが kotlin(...) の糖衣構文と、kotlin-android/kotlin-kapt/kotlin-parcelize といった旧来の短縮名に対応。短縮名は完全なプラグイン id へ展開される
* `改善` 移行スクリプトは移行できない 2 種類のリポジトリをスキップするようにした。apply(from=) で読み込むスクリプト片が AGP の型を参照しているものと、依存関係の検証を有効にしているもの

# v1.2.0

###### 2026/08/18

* `機能` モジュールスクリプトの改修スクリプト `.python/migrate_modules.py`。バージョン指定のないプラグイン適用をバージョン付きの形式へ書き換え、バージョンはシステムプロパティから取得
* `機能` 決定された KSP バージョンを `gradle.ksp.version` システムプロパティとしても公開。AGP や Kotlin の命名と揃えた
* `修正` モジュール改修スクリプトのロールバックが元のファイルを復元できず、バックアップファイルも残していた
* `改善` settings 移行スクリプトはモジュールスクリプトが準備できているかを先に確認し、未準備の場合は書き換えず案内のみを表示。ビルドできない中間状態を残さないようにした
* `改善` settings 移行スクリプトはプラグインを既存の plugins ブロックへまとめ、`includeBuild` の前へ移動するように変更。新しいブロックは追加しない

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
