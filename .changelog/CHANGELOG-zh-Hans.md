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

# v1.9.1

###### 2026/10/09

* `优化` 将插件内置的版本兼容数据 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自动同步至官方最新信息, 发布前已通过自动化测试与示例项目构建验证

# v1.9.0

###### 2026/10/07

* `新增` nativeAlignment 插件为每个应用变体的合并清单写入 org.autojs.plugin.contract.SUPPORTED_ABIS meta-data, 取值由合并后的原生库按 ABI 分包与 ndk.abiFilters 收窄得出, 无原生代码时写入 universal, 可用 supportedAbis 覆盖或用 supportedAbisMetaData 关闭
* `新增` settings 插件在版本信息末尾提示相邻检出 (或 autojs.buildPlugins.localCheckout 指定的目录) 中存在更新的插件版本
* `新增` .python/bump_consumers.py 将兄弟仓库 settings.gradle.kts 中钉住的两个插件版本批量升级到当前版本, 支持 --list, --dry-run, --apply 与 --commit

# v1.8.7

###### 2026/10/02

* `优化` 将插件内置的版本兼容数据 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自动同步至官方最新信息, 发布前已通过自动化测试与示例项目构建验证

# v1.8.6

###### 2026/09/29

* `优化` 将插件内置的版本兼容数据 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自动同步至官方最新信息, 发布前已通过自动化测试与示例项目构建验证

# v1.8.5

###### 2026/09/25

* `优化` 将插件内置的版本兼容数据 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自动同步至官方最新信息, 发布前已通过自动化测试与示例项目构建验证

# v1.8.4

###### 2026/09/20

* `优化` 将插件内置的版本兼容数据 (AGP, Gradle, Kotlin, KSP, Android Studio 等) 自动同步至官方最新信息, 发布前已通过自动化测试与示例项目构建验证

# v1.8.3

###### 2026/09/19

* `修复` AGP 9.1 读取 SDK XML v4 时的解析警告, 更新 sdklib 并保持 AAPT2 与所选 AGP 一致
* `修复` JVM 单元测试及测试夹具任务误触发 APK 原生库对齐检查的问题

# v1.8.2

###### 2026/09/18

* `修复` 平台数据未变化时仍执行发行版本校验的问题, 并避免 CI 安装已废弃的 Android SDK tools 软件包
* `优化` 将两个消费端示例的 compileSdk / targetSdk 升级至 37, 同步所需的 Gradle 与 AGP 版本, 并将原生库对齐负样例及 CI 切换到 Android SDK 37

# v1.8.1

###### 2026/09/12

* `优化` 同步官方平台兼容性及发行数据, 自动发布前校验数据解析, 插件行为及无界面环境下的消费端构建

# v1.8.0

###### 2026/09/11

* `新增` 共享 nativeAlignment Gradle 插件验证 16 KB ELF 与 APK ZIP 对齐, 扫描原生运行时归档, 并在分发前拒绝不合规或缺失的产物
* `优化` CI 在运行原生库对齐负样例前准备 Android SDK

# v1.7.5

###### 2026/09/10

* `优化` 同步官方平台兼容性及发行数据, 自动发布前校验数据解析, 插件行为及无界面环境下的消费端构建

# v1.7.4

###### 2026/09/04

* `优化` 同步官方平台兼容性及发行数据, 自动发布前校验数据解析, 插件行为及无界面环境下的消费端构建

# v1.7.3

###### 2026/09/03

* `修复` AGP 9 内置 Kotlin 未使用自动选择的 KGP, 导致 JDK 25 下无法配置 JVM target 25 的问题
* `优化` 补充 KGP 实际加载版本校验及 JDK 25/26 的消费端构建和设备验证

# v1.7.2

###### 2026/09/03

* `修复` Android Studio 通过 Gradle 项目属性传入的版本识别不完整, 导致 Quail 3 误选 AGP 的问题
* `优化` Facade 支持显式传入 Gradle 项目属性, 未收录的 IDE 构建可按严格版本匹配 (保持二进制兼容)

# v1.7.1

###### 2026/09/02

* `修复` 消费端自定义最低 IDE 版本时可能放宽中央支持范围, 使不受支持的 IDE 进入回退选择的问题
* `优化` 成功摘要省略已满足的 AGP 最低约束, 不兼容错误显示 IDE 版本及约束来源
* `优化` 官方项目统一使用内置兼容数据, gradle/data 覆盖仅保留用于旧版兼容及临时诊断

# v1.7.0

###### 2026/09/02

* `提示` 常规构建声明 SDK 版本即可, 必要时使用 MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION 设置下限, OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION 仅用于显式指定版本
* `新增` AGP 版本选择同时考虑 Android API, 项目, KSP, Gradle 及 IDE 约束, 无兼容版本时显示原因
* `新增` GitHub Actions 构建, 兼容数据检查及发布流程, 支持自动更新拉取请求及经审批发布至公共仓库
* `修复` Temurin 及纯命令行构建按旧 JDK 映射选择过低 AGP 版本的问题, Android API 36 最低要求 AGP 8.9.1
* `修复` 两段式 IDE 映射绕过 Gradle 的 AGP 上限, 以及旧 Gradle 回落到自身无法加载的平台版本的问题
* `优化` 独立抓取的 Android API 到最低 AGP 官方数据, 并刷新 Android Studio/AGP 发行版及 AGP/Gradle 兼容数据
* `优化` 补充 JVM, 数据解析, 重复运行及 Temurin 17 环境的消费端构建验证

# v1.6.0

###### 2026/08/29

* `提示` 永久 Gradle 插件 ID 已由 `org.autojs.build.platform-versions` 改为 `io.github.supermonster003.autojs6-platform-versions`, Maven 坐标为 `io.github.supermonster003:autojs6-gradle-platform-versions`; Java/Kotlin 包名仍为 org.autojs.build.platform
* `新增` Maven Central 及 Gradle Plugin Portal 发布支持, 构件, 源码, Javadoc 及插件元数据均提供签名
* `优化` 补充公共仓库元数据, 可复现发布包及本地和 CI 签名流程
* `优化` 正式用法可仅通过公共仓库解析, 不再依赖 `mavenLocal()`; 迁移工具也可识别并更新旧插件 ID

# v1.5.0

###### 2026/08/28

* `新增` 兼容数据更新工具, 提供 run-scrapers.bat 交互入口及跨平台更新和只读检查命令
* `优化` 抓取器不再依赖 Puppeteer/Chrome, 改为解析官方静态来源; 保留边界与输出校验集中配置, 且避免仅因时间戳变化而重写文件
* `优化` 内置数据已更新至 Gradle 9.7/Kotlin 2.4, AGP 9.5.0-alpha03/9.4.0-rc02 与 9.3.2, Android Studio Rabbit 及 KSP 2.3.11

# v1.4.1

###### 2026/08/18

* `优化` 移除映射表滞后时的冗长控制台注记, 保留版本行的 [auto-specified] 标记
* `优化` 移除 PlatformVersionsExtension.notes 及 Formatted 的 notes 参数, 使用这些接口的构建脚本需要同步调整

# v1.4.0

###### 2026/08/18

* `修复` IDE 补丁更新错误放宽 AGP 上限的问题, IntelliJ IDEA 2026.2.1 与 2026.2 均使用 AGP 9.1
* `优化` IntelliJ IDEA 映射表补入 2026.2 条目, 采用 IDE 自报的 AGP 上限
* `优化` 迁移脚本改为在根构建声明一次插件版本, 兼容 Groovy 模块
* `优化` 控制台的注记移至版本摘要下方单独成段, 不再与版本行交错

# v1.3.0

###### 2026/08/18

* `提示` 停止支持 Gradle 8, AGP 最低支持版本调整为 9.0
* `优化` 兼容数据表剔除 9 以前的条目, IntelliJ IDEA 映射表仅保留给出 AGP 9 的条目
* `优化` 当前 Gradle 旧于全部兼容条目时不再回落到最低条目, 改为明确报错, 避免把无法加载的版本放到 classpath 上
* `优化` 最低支持版本上调: Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `优化` README 徽章同步上述版本, 并新增 AGP 徽章
* `优化` 迁移脚本支持 kotlin(...) 语法糖与 kotlin-android/kotlin-kapt/kotlin-parcelize 等旧式短名, 短名会展开为完整插件 id
* `优化` 迁移脚本跳过两类无法迁移的仓库: 使用 apply(from=) 引入且引用 AGP 类型的脚本片段, 以及启用了依赖校验的仓库

# v1.2.0

###### 2026/08/18

* `新增` 模块脚本改造脚本 `.python/migrate_modules.py`, 把无版本的插件应用改为带版本形式, 版本取自系统属性
* `新增` 决策出的 KSP 版本新增以 gradle.ksp.version 系统属性发布, 与 AGP 和 Kotlin 的命名对齐
* `修复` 模块迁移回滚未还原原文件且遗留备份的问题
* `优化` settings 迁移脚本会先检查模块脚本是否就绪, 未就绪时给出提示而不改写, 避免留下无法构建的中间状态
* `优化` settings 迁移脚本改为把插件并入已有的 plugins 块并移到 includeBuild 之前, 而非新增一个块

# v1.1.0

###### 2026/08/18

* `新增` R8 版本决策, 依据当前 Kotlin 版本查表, 仅在 AGP 自带的 R8 不够新时才显式引入外部 R8
* `新增` KSP 版本决策, 版本号跟随目标 Kotlin 版本; 当所选 KSP 要求更高的 AGP 时自动抬升 AGP 版本
* `新增` 决策结果新增 PlatformVersionsFacade 调用入口, 可在 settings 脚本体中直接使用
* `新增` 决策结果同时以系统属性发布, 供模块脚本以 plugins DSL 方式声明插件版本
* `新增` 下游仓库批量迁移脚本 `.python/migrate_downstream.py`, 支持预览/应用/回滚, 逐仓保留备份
* `修复` getMaxSupportedJavaVersion 误用 AGP 版本而降低工具链上限的问题, 改为使用 Gradle 版本
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
* `新增` version.properties 的 OVERRIDDEN_* 选项, 支持显式指定版本并跳过自动选择
* `新增` 决策结果通过 PlatformVersionsExtension 暴露, 可用于 buildscript 的 classpath 声明
* `新增` 最小消费端工程 sample, 用于验证三种典型场景下的决策结果
* `新增` README 与 CHANGELOG 的多语言资源: 西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体
