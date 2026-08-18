******

### 语言 (Languages)

******

当前 CHANGELOG.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### 发行历史

******

# v1.1.0

###### 2026/08/18

* `新增` R8 版本决策, 依据当前 Kotlin 版本查表, 仅在 AGP 自带的 R8 不够新时才显式引入外部 R8
* `新增` KSP 版本决策, 版本号跟随目标 Kotlin 版本; 当所选 KSP 要求更高的 AGP 时自动抬升 AGP 版本
* `新增` 决策结果新增 `PlatformVersionsFacade` 调用入口, 可在 settings 脚本体中直接使用
* `新增` 决策结果同时以系统属性发布, 供模块脚本以 plugins DSL 方式声明插件版本
* `新增` 下游仓库批量迁移脚本 `.python/migrate_downstream.py`, 支持预览/应用/回滚, 逐仓保留备份
* `修复` `getMaxSupportedJavaVersion` 此前误传 AGP 版本, 导致工具链上限被压低; 现改为传入 Gradle 版本
* `优化` 移除 IntelliJ IDEA 映射表中 2026.2.1 的条目, 使 2026.2 与 2026.2.1 均得到 AGP 9.0.1, 与 IDE 实际支持范围一致

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
