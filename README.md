<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>为 AutoJs6 生态自动决定 AGP 与 Kotlin 插件版本的 Gradle Settings 插件</p>

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
- 决定 KSP 版本, 并在所选 KSP 要求更高 AGP 时自动抬升 AGP 版本.
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
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.5.0"
    }
}

plugins {
    id("org.autojs.build.platform-versions")
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

未来的定时 CI 可使用以下只读检查入口:

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

# v1.5.0

###### 2026/08/28

* `新增` 兼容数据更新套件已完整迁入本仓库, 新增供开发者手动更新的交互式 `run-scrapers.bat`, 并预留跨平台更新与只读检查命令供未来定期 CI 使用
* `优化` 抓取器不再依赖 Puppeteer/Chrome, 改为解析官方静态来源; 保留边界与输出校验集中配置, 且避免仅因时间戳变化而重写文件
* `优化` 内置数据已更新至 Gradle 9.7/Kotlin 2.4, AGP 9.5.0-alpha03、9.4.0-rc02 与 9.3.2, Android Studio Rabbit 及 KSP 2.3.11

# v1.4.1

###### 2026/08/18

* `优化` 映射表滞后时不再向控制台输出解释性注记. 版本行末尾的 [auto-specified] 后缀已足以说明来由, 而该注记比它所解释的摘要还长
* `优化` 随之移除 notes 相关接口: PlatformVersionsExtension.notes 与 Formatted 的 notes 参数不再存在, 读取过该属性的消费端脚本需一并调整

# v1.4.0

###### 2026/08/18

* `修复` IDE 版本仅为补丁级更新时不再放宽 AGP 上限. 此前 IntelliJ IDEA 2026.2.1 会取到 AGP 9.2.1 而被 IDE 拒绝, 现在与 2026.2 一样停留在 9.1 线
* `优化` IntelliJ IDEA 映射表补入 2026.2 条目, 采用 IDE 自报的 AGP 上限
* `优化` 迁移方案改为在根构建脚本声明一次插件版本, 模块脚本无须改动; 此前逐模块添加版本的方式无法用于 Groovy 模块, 因为其 plugins 块只接受字符串字面量
* `优化` 控制台的注记移至版本摘要下方单独成段, 不再与版本行交错

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
