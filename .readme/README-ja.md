<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/src/main/res/mipmap-night/banner.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/src/main/res/mipmap/banner.png?raw=true" alt="banner" border="0" width="512" />
    </picture>
  </p>

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
- 自動選択した KGP をルートプロジェクトの buildscript classpath に追加し、AGP 9 の組み込み Kotlin が古い同梱版ではなく、そのコンパイラと JVM target 対応を実際に使用。
- 互換性データをプラグインに同梱し、既定の唯一のデータソースとします。AutoJs6 の公式ホストおよびプラグインプロジェクトは、利用側の `gradle/data` にコピーを保持しません。
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
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.4"
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
}
```

Settings プラグインは選択した KGP をルートプロジェクトの buildscript classpath に自動追加します。AGP 9 の組み込み Kotlin を使う場合は `org.jetbrains.kotlin.android` を重ねて適用しないでください。決定結果は `gradle.extra["platformVersions"]` からオブジェクトとして読み取れます.

******

### 決定フロー

******

AGP バージョンの決定は次の 3 ステップで行われます:

- IDE ではプラットフォーム表の最古のキーを中央のサポート下限、対応する AGP を上限とする。利用側の IDE 最低バージョンはこの下限を厳しくすることしかできない。新しい IDE に対して表が古い場合のフォールバックを維持し、Temurin と素のコマンドラインでは Gradle の互換上限を直接使う。
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

値が `NONE` または空の場合は固定されません。`MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` は、中央の仕組みで推論できない実際のプロジェクト固有の下限にのみ使用します。AutoJs6 の公式利用側リポジトリは、プラットフォームが保証済みの共通 AGP 9 下限をこの値で重複して宣言してはいけません。数値形式の `COMPILE_SDK_VERSION` と `TARGET_SDK_VERSION` は自動的に考慮されます。同様に、`MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` と `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` は任意のプロジェクト固有の制限です。各中央 IDE 表の最古のキーが引き下げ不能な基準となるため、実際により新しい IDE が必要な場合を除いて、これらのプロパティは省略してください.

******

### 互換性データ

******

決定の根拠となるデータファイルは以下のとおりで、プラグインとともに配布されます:

```text
src/main/resources/org/autojs/build/platform/data/
  agp-releases.list
  agp-gradle-compat.properties
  android-api-agp-compat.properties
  gradle-kotlin-compat.properties
  java-gradle-compat.properties
  android-studio-agp-compat.properties
  android-studio-build-version.properties
  android-studio-codename-version.properties
  android-studio-codename.properties
  kotlin-r8-compat.properties
  ksp-agp-compat.properties
  ksp-releases.properties
```

利用側の `gradle/data` にある同名ファイルは、従来互換または一時的な診断のために限り引き続き優先されますが、公式の運用形態ではありません。AutoJs6 の公式ホストおよびプラグインプロジェクトは、このような上書きをコミットしてはいけません。互換性データはこの中央リポジトリで更新し、新しい不変のプラグインバージョンとして公開します.

******

### データ更新

******

開発者はリポジトリのルートから対話型バッチエントリを実行して、すべての互換性データを更新できます:

```bat
run-scrapers.bat
```

毎日の workflow はデータを更新して検証し、意味のある変更がある場合にのみパッチリリースのコミットとタグを作成して、保護された 2 つのレジストリと GitHub Release の公開チェーンを開始します。手動の check と update-pr モードも引き続き利用できます:

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

# v1.7.4

###### 2026/09/04

* `改善` 公式アップストリームから同梱のプラットフォーム互換性データとリリースデータを更新。定期自動化により、公開前にスクレイパー解析、Gradle プラグインの動作、ヘッドレス利用側ビルドを検証

# v1.7.3

###### 2026/09/03

* `修正` Settings プラグインがプロジェクトのプラグイン解決前に、自動選択した KGP をルート buildscript classpath へ追加するよう修正。AGP 9 の組み込み Kotlin が同梱 KGP 2.2.10 に留まり、JDK 25 で JVM target 25 を拒否する問題を解消
* `改善` Kotlin プラグインを明示しない AGP 利用側サンプルに、選択した KGP とルート classpath の実解決版が一致することを確認するアサーションを追加。Accessibility Compat を Gradle 9.5/AGP 9.3.2、JDK 25/26 で単体テスト、lint、APK ビルド、4 台計 28 テストまで検証

# v1.7.2

###### 2026/09/03

* `修正` Android Studio が Gradle `-P` で渡す識別プロパティを JVM システムプロパティと統合。Quail 3 が `2026.1` に切り詰められて AGP 9.2.1 を誤選択することがなくなり、JDK 25/26 の自動 JVM ターゲットを正常に設定
* `改善` Gradle プロジェクトプロパティを明示的に渡せるバイナリ互換の Facade オーバーロードと、未収集の IDE build 向け strict バージョンフォールバックを追加。Gradle 9.5/9.7 と JDK 25/26 の Quail 3 で AGP 9.3.2 が自動選択され、Kotlin/KSP タスクが正常に作成されることを検証

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
