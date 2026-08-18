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

### M7 — 只支持 Gradle 9

Gradle 8 迟早废弃, 与其维护双版本兼容, 不如收敛到 Gradle 9 单线。AGP 9.0 是首个要求 Gradle 9 的 AGP, 因此 "只支持 Gradle 9" 等价于 "只支持 AGP 9 及以上"。

- [x] M7.1 兼容数据表剔除 9 以前的条目: `agp-gradle-compat` / `gradle-kotlin-compat` / `android-studio-agp-compat` / `agp-releases`。
- [x] M7.2 IntelliJ IDEA 映射表仅保留给出 AGP 9 的条目; Temurin 映射表清空 (其条目全部指向 8.x 线)。
- [x] M7.3 `maxSupportedAgpVersion` 在当前 Gradle 旧于全部条目时返回 null, 不再回落到最低条目。
- [x] M7.4 最低版本上调: Gradle 9.1.0 / Android Studio 2025.2.3 / IntelliJ IDEA 2026.1.2 / AGP 9.0; Java 保持 17 (Gradle 9 的实际要求)。
- [x] M7.5 README 徽章同步, 并新增 AGP 徽章。
- [x] M7.6 单元测试更新并补充 Gradle 8 边界用例; sample 三场景与最低版本守卫实测通过。

`kotlin-r8-compat` 与 `ksp-agp-compat` 两表未做删减: 前者按 Kotlin 版本线索引 R8 版本, 后者给出 KSP 所需的最低 AGP, 其中 8.x 取值对 AGP 9 仍然有效。

### M8 — 全量批量迁移

- [x] M8.1 对 57 个仓库执行两步迁移, 逐仓验证配置阶段通过。
- [x] M8.2 迁移脚本补充 `kotlin(...)` 语法糖支持。
- [x] M8.3 迁移脚本补充旧式短名支持, 并展开为完整插件 id。
- [x] M8.4 迁移脚本新增两类跳过判定: AGP 类型脚本片段, 依赖校验。
- [x] M8.5 已迁移仓库全部验证通过, 并抽样完成真实编译 (产出 APK)。

**首轮结果: 40 个仓库迁移完成, 17 个保留原机制。**

保留原因分两类:

1. **脚本片段引用 AGP 类型 (16 个)**: 主项目 AutoJs6, NodeJs 插件项目, 以及 14 个主项目 Worktree。它们用 `apply(from = ...)` 引入 `node-android-config.gradle.kts` 一类片段, 片段内 `import com.android.build.api.dsl.*` 并配置 `android {}`。这类片段拥有独立的编译类路径, 模块的 plugins DSL 与 buildscript 块均不覆盖它, 迁移后必然编译失败。已实测确认无法通过调整 settings 或模块脚本解决 —— 出路是把片段改写为 build-logic 中的 convention 插件, 或内联进模块脚本。
2. **启用依赖校验 (1 个)**: AutoJs6-Plugin-Python-Runtime 的 `gradle/verification-metadata.xml` 按校验和锁定全部依赖, 新插件不在其中。是否信任本地发布的未签名产物属于该仓库的安全决策, 记入白名单后即可迁移。

`Utils.kt` 的 A/B 变体差异未造成任何问题: 两个变体只在 AGP 低于 9 时行为不同, 而迁移后决策恒为 AGP 9.x。

### M9 — 收尾余下仓库

- [x] M9.1 修复 IDEA 下 AGP 版本溢出: 映射表补回 2026.2 条目, 并给 stale-map 回退加上界。
- [x] M9.2 Python-Runtime 移除依赖校验并完成迁移。
- [x] M9.3 迁移方案改为根声明模式, 全部已迁移仓库统一重跑。
- [x] M9.4 AutoJs6 与 AutoJs6-Plugin-NodeJs-Runtime 的脚本片段改写为 convention 插件。
- [ ] M9.5 14 个 Worktree 的脚本片段改写 (各自位于独立分支, 需逐仓处理)。

#### 迁移方案改为根声明模式

原方案逐个模块脚本添加版本, 有两个缺陷:

1. **Groovy 模块无法带版本**: Gradle 的 `plugins {}` 块在 Groovy 中只接受字符串字面量, `System.getProperty(...)` 是方法调用, 直接编译失败。16 个仓库各有 3 个 Groovy 模块。
2. 主项目有 39 个模块脚本, 版本散落各处。

新方案在根 `build.gradle.kts` 声明一次:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version") apply false
    id("com.android.library") version System.getProperty("gradle.agp.version") apply false
}
```

模块脚本**完全不动**, 两种 DSL 均可无版本应用。版本只出现一处, convention 插件照常工作。

全部 43 个已迁移仓库均按此模式重跑并验证通过, 其中主项目 (39 模块 + KSP + parcelize + Groovy + convention 插件改写) 完整编译产出 APK。

#### AGP 版本溢出

移除 9 以前的条目后, IntelliJ 映射表只剩 2026.1.2 一条, 导致更新的 IDE 被判为映射表滞后而回退到 auto 选择。auto 只受 Gradle 约束, 在 Gradle 9.6.1 上取到 AGP 9.2.1, 被 IDE 以 `Latest supported version is AGP 9.1.0` 拒绝。

两处修改: 映射表补上 `2026.2` 条目并写入 IDE 自报的上限; stale-map 回退的结果被限制在最新已知条目之上一个小版本线内 (映射表滞后到下一线都不可用时该限制让位)。

教训: **映射表的最新条目必须写出最新 IDE 的真实上限**, 而不只是保留最旧的受支持版本 —— 越过表尾即进入只受 Gradle 约束的 auto 选择。

#### 脚本片段改写方案 (已验证)

`apply(from = ...)` 片段自带 buildscript 的做法**不可行**: 会得到 `ApplicationExtensionImpl cannot be cast to ApplicationExtension`, 因为两个 classloader 各持一份 AGP 类。

可行方案是改为 build-logic 的 convention 插件:

1. `build-logic/convention/build.gradle.kts` 加 `compileOnly("com.android.tools.build:gradle:${System.getProperty("gradle.agp.version")}")` — 用 compileOnly, 运行时复用模块已加载的 AGP;
2. 插件类在 `project.plugins.withId("com.android.application") { }` 回调内配置 `android {}`;
3. 模块脚本以 `id("...")` 应用, 删除原 `apply(from = ...)`。

片段共 3 个变体 (227 / 227 / 345 行), 其中一个变体覆盖 13 个 Worktree。

**但改写成果无法直接复制到 Worktree**: 它们各自位于独立开发分支 (`codex/ai-public-chat` 等), `app/build.gradle.kts` 等文件与主项目 HEAD 差异显著, 覆盖会破坏在研工作。这 14 个仓库需在各自分支上逐一改写, 或等其分支合并回主项目后再迁移。

若 convention 插件所在的 build-logic 在配置阶段早于 settings 插件求值 (`gradle.agp.version` 尚未发布), 则不能用 `compileOnly` 依赖 AGP, 改以反射访问 android 扩展 —— 这与各仓库 `Utils.kt` 既有的 AGP 探测方式一致。AutoJs6-Plugin-NodeJs-Runtime 即按此改写: 32 行片段变为 `org.autojs.build.node-runtime-kit` 插件, 迁移后 settings 由 763 行降至 38 行, `:app:assembleDebug` 通过且 4 个 BuildConfig 字段照常生成。

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
