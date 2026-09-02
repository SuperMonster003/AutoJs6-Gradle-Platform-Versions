<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>为 AutoJs6 生态自动决定 AGP 与 Kotlin 插件版本的 Gradle Settings 插件</p>

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
- Temurin 与裸命令行不再使用平台版本映射, 而是显式按 Gradle 兼容性自动选择 AGP.
- 把 Android API、KSP 及项目声明的最低 AGP 作为下界, 与 IDE/Gradle 上界求交; 无兼容交集时提前报错.
- 决定 R8 版本, 仅在 AGP 自带的 R8 不够新时才引入外部 R8.
- 兼容数据随插件分发, 消费端项目若存在 `gradle/data` 目录则优先使用, 便于紧急修数据.
- 保留 `version.properties` 中的 `OVERRIDDEN_*` 逃生门, 需要确定性构建时可直接钉死版本.
- README 与 CHANGELOG 支持西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体.

******

### 使用方法

******

在消费端项目的 `settings.gradle.kts` 里应用插件, 位置需在 `includeBuild` 之前:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.0"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
}
```

模块脚本随后即可用 plugins DSL 声明插件, 版本取自决策结果:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

决策结果也可通过 `gradle.extra["platformVersions"]` 以对象形式读取.

******

### 决策流程

******

AGP 版本的决定过程分为三步:

- IDE 环境用平台映射表确定上界并保留滞后回退; Temurin 与裸命令行直接采用 Gradle 兼容上界.
- 按 AGP 与 Gradle 的官方兼容表再次封顶, 保证候选版本可由当前 Gradle 加载.
- 从 compileSdk/targetSdk、KSP 及可选的项目最低版本推导下界, 仅在上下界存在交集时返回 AGP.

Kotlin 版本则跟随 Gradle 而非 IDE, 始终选取当前 Gradle 支持的最新版本.

******

### 指定版本

******

如果出于测试或确定性构建需要固定版本, 可以在 `version.properties` 里直接指定:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

值为 `NONE` 或留空时表示不固定. 若只想声明下界而不钉死版本, 可使用 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; 数字形式的 `COMPILE_SDK_VERSION` 与 `TARGET_SDK_VERSION` 会自动参与判断.

******

### 兼容数据

******

决策依据的数据文件如下, 它们随插件一同分发:

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

消费端项目如果在自己的 `gradle/data` 目录下放了同名文件, 则该文件优先生效.

******

### 数据更新

******

开发者可从仓库根目录运行交互式批处理来更新全部兼容数据:

```bat
run-scrapers.bat
```

仓库的周期 CI 使用以下只读检查入口; 手动 update 模式会在验证后创建数据 PR:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` 不修改工作区: 退出码 `0` 表示数据最新, `2` 表示发现更新, `1` 表示任务失败.

完整的更新范围与运行约定见 [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### 发行历史

******

# v1.7.0

###### 2026/09/02

* `提示` 常规构建应声明 SDK 版本, 并仅在必要时声明 `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` 应留给有意进行的精确版本测试或特殊逃生场景
* `新增` AGP 选择现会将 Android API、项目与 KSP 下界同当前 Gradle 和 IDE 上界求交, 无兼容版本时会报告约束来源
* `新增` 新增 GitHub Actions 工作流, 用于 Temurin 构建、定期兼容数据检查或更新 PR, 以及受标签和人工审批保护的 Maven Central / Gradle Plugin Portal 发布
* `修复` Temurin 与裸命令行构建不再查询旧的 JDK 到 AGP 映射, 因此 JDK `21.0.6+7` 不会再静默选择 AGP 8.7.3; Android API 36 现会自动要求 AGP 8.9.1 或更高版本
* `修复` 修复两段式 IDE 映射绕过 Gradle 的 AGP 上限, 以及旧 Gradle 回落到自身无法加载的平台版本的问题
* `优化` 新增独立抓取的 Android API 到最低 AGP 官方数据, 并刷新 Android Studio、AGP 发行版及 AGP/Gradle 兼容数据
* `优化` 验证范围扩展到 70 项 JVM 测试、Node 解析与幂等测试, 以及在真实 Temurin 17 CI 中执行无头自动选择的示例构建

# v1.6.0

###### 2026/08/29

* `提示` 永久 Gradle 插件 ID 已由 `org.autojs.build.platform-versions` 改为 `io.github.supermonster003.autojs6-platform-versions`, Maven 坐标为 `io.github.supermonster003:autojs6-gradle-platform-versions`; Java/Kotlin 包名仍为 `org.autojs.build.platform`
* `新增` 新增面向 Maven Central 与 Gradle Plugin Portal 的首次公开在线发布链路, 实现构件、源码、Javadoc、模块元数据及插件 marker 均带正式签名
* `优化` 新增公开 GitHub 仓库、完整 Central POM 元数据、可复现 Portal bundle、隔离消费者验证, 以及分别供本机 GPG 与 CI 使用的安全签名入口
* `优化` 正式用法现可仅通过公共仓库解析, 不再依赖 `mavenLocal()`; 迁移工具也可识别并更新旧插件 ID

# v1.5.0

###### 2026/08/28

* `新增` 兼容数据更新套件已完整迁入本仓库, 新增供开发者手动更新的交互式 `run-scrapers.bat`, 并预留跨平台更新与只读检查命令供未来定期 CI 使用
* `优化` 抓取器不再依赖 Puppeteer/Chrome, 改为解析官方静态来源; 保留边界与输出校验集中配置, 且避免仅因时间戳变化而重写文件
* `优化` 内置数据已更新至 Gradle 9.7/Kotlin 2.4, AGP 9.5.0-alpha03、9.4.0-rc02 与 9.3.2, Android Studio Rabbit 及 KSP 2.3.11

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
run-scrapers.bat
.utils/
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
