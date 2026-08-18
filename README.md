<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>为 AutoJs6 生态自动决定 AGP 与 Kotlin 插件版本的 Gradle Settings 插件</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.2+-02303A"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 语言 (Languages)

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### 简介

******

这个插件把 AutoJs6 主项目和各插件项目里重复维护的那套构建版本决策逻辑抽了出来. 以前每个仓库的 settings.gradle.kts 都带着几百行几乎相同的代码, 用来判断当前是哪个 IDE 在构建, 再据此挑选合适的 AGP 与 Kotlin 版本.

把它做成一个可发布的 Settings 插件之后, 下游项目只需要写十几行引入代码. 逻辑改进一次, 所有项目升级插件版本即可获得, 不必再逐个仓库复制粘贴.

******

### 功能

******

- 识别构建宿主: Android Studio, IntelliJ IDEA, Temurin JDK, 以及裸命令行环境.
- 按当前 IDE 版本挑选它能支持的 AGP 版本, 版本之间不完全匹配时向下就近选取.
- IDE 版本比映射表全部条目都新时, 自动回退到 auto 选择, 避免静默降级到过旧的 AGP.
- 按 AGP 与 Gradle 的兼容关系封顶, 保证选出的版本当前 Gradle 一定能加载.
- 兼容数据随插件分发, 消费端项目若存在 `gradle/data` 目录则优先使用, 便于紧急修数据.
- 保留 `version.properties` 中的 `OVERRIDDEN_*` 逃生门, 需要确定性构建时可直接钉死版本.
- README 与 CHANGELOG 支持西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体.

******

### 使用方法

******

在消费端项目的 `settings.gradle.kts` 里应用插件:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.0.0"
    }
}
```

随后即可读取决策结果, 用于 buildscript 的 classpath:

```kotlin
import org.autojs.build.platform.PlatformVersionsExtension

val platformVersions = gradle.extra["platformVersions"] as PlatformVersionsExtension

buildscript {
    dependencies {
        classpath(platformVersions.agpClasspathNotation)
        classpath(platformVersions.kotlinClasspathNotation)
    }
}
```

******

### 决策流程

******

AGP 版本的决定过程分为三步:

- 用当前平台版本在该平台的 AGP 映射表里做就近向下匹配.
- 判断映射表是否滞后, 即当前 IDE 是否比表中全部条目都新; 若是则回退到 auto 选择.
- 按 AGP 与 Gradle 的兼容表封顶, 结果不会超出当前 Gradle 能加载的范围.

Kotlin 版本则跟随 Gradle 而非 IDE, 始终选取当前 Gradle 支持的最新版本.

******

### 指定版本

******

如果希望跳过全部自动决策, 可以在 `version.properties` 里直接指定版本:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

值为 `NONE` 或留空时表示不指定, 走自动决策流程.

******

### 兼容数据

******

决策依据的数据文件如下, 它们随插件一同分发:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

消费端项目如果在自己的 `gradle/data` 目录下放了同名文件, 则该文件优先生效.

******

### 发行历史

******

# v1.0.0

###### 2026/08/18

* `新增` Gradle Settings 插件 `org.autojs.build.platform-versions`, 用于自动决定 AGP 与 Kotlin Gradle 插件版本
* `新增` 构建宿主识别, 支持 Android Studio/IntelliJ IDEA/Temurin JDK 以及裸命令行环境
* `新增` AGP 版本决策, 按当前 IDE 版本在映射表中就近向下匹配
* `新增` 映射表滞后回退, 当前 IDE 比表中全部条目都新时改用 auto 选择, 不再静默降级到过旧的 AGP
* `新增` AGP 版本按 Gradle 兼容表封顶, 保证选出的版本当前 Gradle 一定能加载
* `新增` Kotlin Gradle 插件版本决策, 跟随当前 Gradle 支持的最新版本
* `新增` 兼容数据随插件分发, 消费端项目的 `gradle/data` 目录可覆盖同名数据文件
* `新增` `version.properties` 中 `OVERRIDDEN_*` 逃生门, 可直接指定版本以跳过自动决策
* `新增` 决策结果通过 `PlatformVersionsExtension` 暴露, 可用于 buildscript 的 classpath 声明
* `新增` 最小消费端工程 `sample`, 用于验证三种典型场景下的决策结果
* `新增` README 与 CHANGELOG 的多语言资源: 西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体

##### 更多发行历史可参阅

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)

******

### 构建

******

```powershell
.\gradlew.bat build
```

发布到本地 Maven 仓库:

```powershell
.\gradlew.bat publishToMavenLocal
```

插件版本号取自 `version.properties` 的 `VERSION_NAME`.

******

### 资源结构

******

```text
src/main/kotlin/org/autojs/build/platform/
src/main/resources/org/autojs/build/platform/data/
sample/
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
```

决策逻辑位于 `src/main/kotlin`, 兼容数据作为资源打包在 `src/main/resources`; `sample` 是一个最小消费端工程, 用于验证决策结果. README 与 CHANGELOG 由 `.python/generate_markdown.py` 根据 JSON 源文件生成.

******

### 相关链接

******

- AutoJs6 主项目: https://github.com/SuperMonster003/AutoJs6
- Android Gradle 插件发行说明: https://developer.android.com/build/releases/gradle-plugin
- Gradle 兼容性矩阵: https://docs.gradle.org/current/userguide/compatibility.html
