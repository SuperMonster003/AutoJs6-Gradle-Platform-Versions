<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>AutoJs6 エコシステム向けに AGP と Kotlin プラグインのバージョンを自動決定する Gradle Settings プラグイン</p>

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
- AGP と Gradle の互換関係を上限として適用し、選ばれたバージョンを現在の Gradle が必ずロードできることを保証。
- KSP バージョンを決定し、選択された KSP がより新しい AGP を要求する場合は AGP バージョンを自動的に引き上げ。
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

- 現在のプラットフォームバージョンを使い、そのプラットフォームの AGP マッピング表で直近の下位一致を探す。
- マッピング表が古くなっていないか、つまり現在の IDE が表のすべての項目より新しいかを判定し、そうであれば auto 選択へフォールバックする。
- AGP と Gradle の互換表を上限として適用し、現在の Gradle がロードできる範囲を超えないようにする。

一方 Kotlin のバージョンは IDE ではなく Gradle に追従し、常に現在の Gradle が対応する最新バージョンを選択します.

******

### バージョンの固定

******

自動決定をすべてスキップしたい場合は、`version.properties` でバージョンを直接指定できます:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

値が `NONE` または空の場合は未指定とみなされ、自動決定フローに従います.

******

### 互換性データ

******

決定の根拠となるデータファイルは以下のとおりで、プラグインとともに配布されます:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

利用側プロジェクトが自身の `gradle/data` ディレクトリに同名のファイルを置いた場合は、そちらが優先されます.

******

### リリース履歴

******

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
