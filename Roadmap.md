# AutoJs6 Gradle Platform Versions — 开发路线图 (Roadmap)

> 修订日期: 2026-08-18

## 一. 项目定位

把 AutoJs6 主项目与各插件项目中重复维护的 "Gradle 构建版本自动决策机制" (约 700 行 settings.gradle.kts 逻辑 + gradle/data 数据文件) 抽取为一个独立发布的 **Gradle Settings 插件**, 让下游项目的 settings.gradle.kts 缩减为十几行 bootstrap + 一个插件版本号。

解决的两个核心痛点:

1. **数据滞后**: agpVersionMap 等映射表和兼容数据散落在几十个仓库中, 手动逐仓同步, 极易滞后并引发 "IDE 构建失败但命令行正常" 类事故。
2. **逻辑漂移**: 各仓库复制的机制代码独立演化 (如 stale-map 回退只在部分仓库存在), 修复无法自动惠及全部项目。

## 二. 开发原则

- **先跑起来**: 每个里程碑交付一条可验证的能力, 不做一次性完美方案。
- **融合超集**: 以 AutoJs6 主项目与 AutoJs6-Plugin-NodeJs-Runtime 两版代码为输入, 行为差异处取语义超集 (保留 stale-map 回退等新修复)。
- **兼容优先**: 下游项目迁移后, 三种典型场景 (IDE 已覆盖版本 / IDE 未覆盖新版本 / 纯命令行) 的决策结果必须与迁移前一致或更优。
- **逃生门保留**: version.properties 中 OVERRIDDEN_* 直接钉死版本的机制原样保留。

## 三. 里程碑

### M0 — 仓库骨架

- [x] M0.1 git init, 建立仓库目录 AutoJs6-Gradle-Platform-Versions。
- [x] M0.2 引入 Gradle Wrapper 9.3.0 (与主项目一致)。
- [x] M0.3 LICENSE (MPL-2.0, 与 AutoJs6 生态一致)。
- [x] M0.4 .gitignore。
- [x] M0.5 本 Roadmap.md 落库。

### M1 — Settings 插件核心

目标: `org.autojs.build.platform-versions` 插件可被 `pluginManagement` 内 `apply`, 完成与现有机制一致的版本决策。

- [x] M1.1 插件工程骨架: settings.gradle.kts + build.gradle.kts (java-gradle-plugin + kotlin-dsl), 插件 id `org.autojs.build.platform-versions`, 实现类骨架与空 apply 可编译。
- [x] M1.2 平台识别模块: 从系统属性 (idea.paths.selector / idea.vendor.name / java.vendor 等) 识别 AndroidStudio / IntelliJIdea / Temurin / 操作系统兜底等平台, 语义与两版 settings.gradle.kts 的超集一致。
- [x] M1.3 版本决策模块: agpVersionMap 就近向下匹配 + stale-map 回退 (平台版本新于映射表全部条目时走 auto) + gradle/data 兼容表封顶 + Kotlin 版本决策, 输出与现有机制相同的 gradle.extra 键与 System.setProperty 键。
- [x] M1.4 数据资源打包: gradle/data/* 作为插件 jar 内置资源; 支持消费端本地 gradle/data 覆盖 (本地存在时优先), 便于紧急修数据不必等插件发版。
- [x] M1.5 OVERRIDDEN_* 逃生门与 version.properties 读取逻辑移植。
- [x] M1.6 纯逻辑部分 (版本比较 / 就近匹配 / stale 判定 / 兼容封顶) 单元测试全绿。

### M2 — 消费端样板与三场景验证

目标: 一个最小 Android 消费项目通过本插件完成版本决策, 三场景实测结果与迁移前的现有机制一致。

- [x] M2.1 sample 消费项目: settings.gradle.kts 以 bootstrap 方式应用插件 (includeBuild 直连开发版), 十几行以内。
- [x] M2.2 场景 A: 模拟未来 IDEA (映射表滞后) → stale-map 回退, AGP 走 auto 选择。
- [x] M2.3 场景 B: 模拟当前 IDEA 2026.2 → 决策结果与映射表一致。
- [x] M2.4 场景 C: 纯命令行 → auto 选择, 行为不变。
- [x] M2.5 mavenLocal 发布链路: `publishToMavenLocal` 后, sample 改用版本号消费验证通过。
- [x] M2.6 OVERRIDDEN_* 逃生门实测: 指定版本后跳过自动决策, 标记为 [user-specified]。

### M3 — 多语言文档管线

目标: 完全复刻 AutoJs6-Plugin-OpenCC 的 json + 脚本生成机制。

- [x] M3.1 移植生成脚本与 json 骨架, 语言覆盖与 OpenCC 一致 (简中 / 港繁 / 台繁 / 英 / 法 / 西 / 日 / 韩 / 俄 / 阿, 共 10 种)。
- [x] M3.2 撰写 README 源内容 (自然语言风格, 不含安全边界 / 签名等内部细节) 并翻译为全部语言。
- [x] M3.3 撰写 v1.0.0 更新日志 (标签严格沿用 OpenCC 词表: 提示 / 新增 / 修复 / 优化 / 依赖), 翻译为全部语言。
- [x] M3.4 运行脚本生成全部 md: 根目录仅保留 README.md, 不出现根目录 CHANGELOG.md; 各语言 changelog 落在 .changelog 目录。
- [x] M3.5 全角符号检查: 标点风格与 OpenCC 一致 (简中 / 港繁及各西文语言用半角, 台繁与日语保留各自惯例), 由 .python/check_translations.py 自动校验。

### M4 — 版本与提交

- [x] M4.1 version.properties: VERSION_BUILD 与最终提交总数一致 (v1.0.0 时为 15, M5 后升至 v1.1.0)。
- [x] M4.2 全部文件按逻辑拆分提交, 提交信息清晰有序。
- [x] M4.3 核对 `git rev-list --count HEAD` == VERSION_BUILD。

### M5 — 决策链路补齐与下游迁移

- [x] M5.1 移除 IntelliJ IDEA 映射表中 `2026.2.1` 到 `9.1.1` 的条目, 使 2026.2 与 2026.2.1 均落到 9.0.1; 同步修改主项目与 NodeJs 插件项目。
- [x] M5.2 stale-map 回退回移主项目 AutoJs6, 实测 IDEA 2026.2 与 2027.1 两场景均正确回退。
- [x] M5.3 `getMaxSupportedJavaVersion` 改传 Gradle 版本, 主项目与 NodeJs 插件项目同步修正, 实测工具链上限由 24 升为 25。
- [x] M5.4 R8 决策链路: `kotlin-r8-compat` 查表 + AGP 自带 R8 判定, 仅在需要时显式引入外部 R8。
- [x] M5.5 KSP 决策链路: `ksp-releases` 版本决策 + `ksp-agp-compat` 反向抬升 AGP, 用户钉死 AGP 冲突时抛异常而非静默改写。
- [x] M5.6 决策结果以 `PlatformVersionsFacade` 与系统属性双通道暴露, 适配 settings buildscript classpath 早于插件应用的时序约束。
- [x] M5.7 下游批量迁移脚本 `.python/migrate_downstream.py`: 结构化定位 pluginManagement 块, 支持 `--list`/`--dry-run`/`--apply`/`--revert`/`--force`, 逐仓备份可回滚。

### M6 — 两步迁移落地

- [x] M6.1 勘察下游模块脚本结构: 确认无版本应用的是 `com.android.application`/`com.android.library`/`com.google.devtools.ksp` 三者, convention 插件与 `kotlin()` 语法糖不受影响。
- [x] M6.2 模块改造脚本 `.python/migrate_modules.py`: 按插件 id 精确匹配改写为带版本形式, 版本取自系统属性。
- [x] M6.3 插件补发 `gradle.ksp.version` 属性, 与 AGP/Kotlin 命名对齐。
- [x] M6.4 第二步脚本自动检测模块状态, 未完成第一步时拒绝执行。
- [x] M6.5 试点仓库端到端实测: 两步迁移后成功产出 APK, 随后完整回滚。

## 四. 迁移期间发现的上游缺陷

抽取过程中发现的原始 settings.gradle.kts 缺陷, 本项目已修正, 并已回移主项目:

1. **`getMaxSupportedJavaVersion` 误传 AGP 版本**: 该函数按 `java-gradle-compat.properties` 查表, 参数名与属性名 (`gradle.java.version.coerced.by.gradle`) 均指向 Gradle 版本, 但 AutoJs6 与 NodeJs 插件项目都传入了 AGP 版本。后果是工具链上限被压低: Gradle 9.3.0 配 AGP 9.0.1 时得到 24, 而按表应为 25。三处均已改为传 Gradle 版本, 本项目并加测试锁定。
2. **`toVersionParts` 注释与实现不符**: 注释声称支持 `1.2.3_preview-2` 形式, 但下划线不在分隔符集合内, 该输入实际抛异常。本项目已修正注释并加测试固化实际行为。

## 五. 下游迁移: 两步走

全部 57 个下游仓库的 settings.gradle.kts 都在 `pluginManagement` 内用 `buildscript { classpath(AGP) }` 声明 AGP, 这构成迁移的硬约束:

- Gradle 规定 `buildscript` 块不得早于 `pluginManagement`, 每个脚本仅允许一个, 且其 classpath configuration 一经解析不可再追加;
- settings 插件的应用晚于该 configuration 的解析。

因此没有任何 settings 插件能为这条 classpath 供版本。原机制不受此限, 是因为决策代码内联于同一块中, 无需先解析任何依赖。

出路是让模块自己声明版本, 取自本插件发布的系统属性。两个脚本配套完成:

| 步骤 | 脚本 | 作用 |
|---|---|---|
| 第一步 | `.python/migrate_modules.py` | 模块脚本中无版本的 AGP/KSP 应用改为带版本形式 |
| 第二步 | `.python/migrate_downstream.py` | settings 脚本的机制段替换为十余行 bootstrap |

**两步必须成对完成**: 中间态下模块已索要版本而旧 settings 尚未发布, 构建会以 `plugin version 'null' is invalid` 失败。第二步脚本会自动检测模块状态, 未完成第一步时拒绝执行并给出提示。

两脚本均支持 `--list` / `--dry-run` / `--apply` / `--revert`, 逐文件保留备份可回滚。

### 已验证

在 AutoJs6-Plugin-AI-Text-Generation-Catalog 上完成端到端实测: 两步迁移后 `:app:assembleDebug` 成功产出 APK, settings 脚本由 720 行缩减至 35 行, 随后完整回滚至原状。

### 已知限制

- `kotlin("plugin.parcelize")` 等 `kotlin()` 语法糖不被改写, 它依赖 classpath 上已有的 Kotlin 插件。AGP 9 起由其内置 Kotlin 提供, 当前无影响。
- 部分仓库的 convention 插件会在 AGP 低于 9 时自行应用 `org.jetbrains.kotlin.android`。该路径在 AGP 9 下为死代码, 迁移后未在 AGP 8 环境验证。
- 主项目 AutoJs6 的 13 个 `libs/*` 模块只用 convention 插件, 不含 AGP, 脚本按插件 id 精确匹配, 已确认不会误伤。

## 六. 后续展望 (不在本期范围)

- 发布到 GitHub Packages / Gradle Plugin Portal, 下游用动态版本自动跟进。
- 主项目 CI cron 定时跑 scraper 并自动发版。
- 模块脚本改用 plugins DSL 的批量改造 (迁移脚本的前置条件, 见第五节)。
