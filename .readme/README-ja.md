<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>AutoJs6 エコシステム向けに AGP と Kotlin プラグインのバージョンを自動決定する Gradle Settings プラグイン</p>

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

### 言語 (Languages)

******

現在 README.md は以下の言語に対応しています:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### 概要

******

このプラグインは、AutoJs6 本体と各プラグインプロジェクトで重複して管理されていたビルドバージョンの決定ロジックを切り出したものです。以前は各リポジトリの settings.gradle.kts に、どの IDE でビルドしているかを判定し、それに応じて適切な AGP と Kotlin のバージョンを選ぶための、ほぼ同一の数百行のコードが置かれていました。

公開可能な Settings プラグインにしたことで、利用側プロジェクトは十数行の導入コードを書くだけで済みます。ロジックを一度改善すれば、すべてのプロジェクトはプラグインのバージョンを上げるだけでその恩恵を受けられ、リポジトリごとにコピー & ペーストする必要はなくなります。

******

### 機能

******

- ビルドホストの識別: Android Studio、IntelliJ IDEA、Temurin JDK、および素のコマンドライン環境。
- 現在の IDE バージョンが対応できる AGP バージョンを選択し、完全に一致しない場合は直近の下位バージョンを採用。
- IDE のバージョンがマッピング表のすべての項目より新しい場合は auto 選択へ自動的にフォールバックし、古すぎる AGP への暗黙のダウングレードを回避。
- Temurin と素のコマンドラインを明示的にヘッドレス環境として扱い、IDE バージョン表ではなく Gradle の互換性から AGP を選択。
- IDE/Gradle の上限と、Android API レベル、KSP、プロジェクトが要求する AGP の下限を交差させ、互換バージョンがなければ早期にエラー。
- R8 バージョンを決定し、AGP に同梱された R8 が十分に新しくない場合にのみ外部の R8 を導入。
- 互換性データはプラグインに同梱して配布し、利用側プロジェクトに `gradle/data` ディレクトリがあればそちらを優先するため、緊急のデータ修正が容易。
- `version.properties` の `OVERRIDDEN_*` という避難口を用意しており、決定性のあるビルドが必要な場合はバージョンを直接固定可能。
- README と CHANGELOG はスペイン語、フランス語、ロシア語、アラビア語、日本語、韓国語、英語、簡体中国語、繁体中国語 (香港)、繁体中国語 (台湾) に対応。

******

### 使い方

******

利用側プロジェクトの `settings.gradle.kts` でプラグインを適用します。位置は `includeBuild` より前である必要があります:

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

その後、モジュールのスクリプトで plugins DSL によりプラグインを宣言でき、バージョンは決定結果から取得されます:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

決定結果は `gradle.extra["platformVersions"]` からオブジェクトとして読み取ることもできます.

******

### 決定フロー

******

AGP バージョンの決定は次の 3 ステップで行われます:

- IDE ではプラットフォーム表を上限とし、表が古い場合のフォールバックも適用する。Temurin と素のコマンドラインでは Gradle の互換上限を直接使う。
- 公式の AGP/Gradle 互換表でもう一度上限を設定し、実行中の Gradle が候補をロードできるようにする。
- compileSdk/targetSdk、KSP、任意のプロジェクト最小値から下限を導き、上下限が交差する場合にのみ AGP を返す。

一方 Kotlin のバージョンは IDE ではなく Gradle に追従し、常に現在の Gradle が対応する最新バージョンを選択します.

******

### バージョンの固定

******

テストまたは決定性のあるビルドでは、`version.properties` で正確なバージョンを直接固定できます:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

値が `NONE` または空の場合は固定されません。正確なバージョンを固定せず下限だけを示すには `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` を使います。数値形式の `COMPILE_SDK_VERSION` と `TARGET_SDK_VERSION` は自動的に考慮されます.

******

### 互換性データ

******

決定の根拠となるデータファイルは以下のとおりで、プラグインとともに配布されます:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/android-api-agp-compat.properties
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

利用側プロジェクトが自身の `gradle/data` ディレクトリに同名のファイルを置いた場合は、そちらが優先されます.

******

### データ更新

******

開発者はリポジトリのルートから対話型バッチエントリを実行して、すべての互換性データを更新できます:

```bat
run-scrapers.bat
```

リポジトリの定期 workflow は次の読み取り専用チェックを使い、手動の update モードは検証後にデータの pull request を作成します:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` はワークスペースを変更しません。終了コード `0` はデータが最新、`2` は更新あり、`1` はタスク失敗を表します.

更新範囲と実行規約の詳細は、次を参照してください [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### リリース履歴

******

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

##### 詳しいリリース履歴はこちらを参照してください

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)

******

### ビルド

******

```powershell
.\gradlew.bat build
```

ローカル Maven リポジトリへ公開:

```powershell
.\gradlew.bat publishToMavenLocal
```

プラグインのバージョン番号は `version.properties` の `VERSION_NAME` から取得されます.

******

### リソース構成

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

決定ロジックは `src/main/kotlin` にあり、互換性データはリソースとして `src/main/resources` にパッケージされます。`sample` は決定結果を検証するための最小の利用側プロジェクトです。README と CHANGELOG は `.python/generate_markdown.py` が JSON のソースファイルから生成します.

******

### 関連リンク

******

- AutoJs6 本体プロジェクト: https://github.com/SuperMonster003/AutoJs6
- Android Gradle プラグイン リリースノート: https://developer.android.com/build/releases/gradle-plugin
- Gradle 互換性マトリックス: https://docs.gradle.org/current/userguide/compatibility.html
