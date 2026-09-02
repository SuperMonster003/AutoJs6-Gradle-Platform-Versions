# Platform-version data scrapers

本目录维护随 Gradle Settings 插件发布的兼容性与版本元数据. 它只包含从 AutoJs6 迁移而来的构建版本数据任务; Gradle Wrapper、Foojay Resolver、Rhino、Android 版本和贡献者表等会改动 AutoJs6 自身配置或内容的任务仍由 AutoJs6 自己维护. Android Studio 最新稳定版及下载文件信息在此生成, AutoJs6 只负责将其投影到自己的 README 通用数据中.

This directory owns the compatibility and version metadata shipped with the Gradle Settings plugin. It contains only the build-version data tasks migrated from AutoJs6; jobs that mutate AutoJs6 itself, including its Gradle Wrapper, Foojay Resolver, Rhino, Android releases, and contributor table, remain in the main AutoJs6 project. The latest stable Android Studio release and download artifacts are generated here, while AutoJs6 only projects that shared metadata into its README data.

## 开发者手动更新

在仓库根目录双击或从终端运行:

```bat
run-scrapers.bat
```

批处理会在首次运行时通过 `npm ci` 安装依赖, 然后更新全部数据. 完成后可按 `R` 再次执行, 按 `Shift+R` 清屏后再次执行, 或按 `Esc` / `Enter` / `Space` 退出. 批处理退出码会保留最后一次抓取结果.

也可直接使用无交互入口:

```powershell
npm --prefix .utils ci
npm --prefix .utils run update-data
```

## CI 预留入口

定时 CI 建议调用只读检查模式. 它会拉取并解析全部上游数据, 显示语义差异, 但不会修改工作区:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

退出码约定:

- `0`: 所有数据均为最新.
- `2`: 发现数据更新, 但检查模式未修改文件.
- `1`: 网络、上游格式、数据校验或脚本执行失败.

未来的 GitHub Actions 可以把退出码 `2` 用作提醒, 也可以改为运行 `update-data`, 再根据 Git diff 创建提交或拉取请求. GitHub API 请求会自动使用可选的 `GITHUB_TOKEN`, 以提高 API 限额.

## 数据范围

入口按数据集职责边界依次运行 10 个独立任务: Gradle/Kotlin、Kotlin/R8、Android Studio 发行版、Android Studio/AGP、Java/Gradle、AGP/Gradle、Android API/AGP、AGP 发行版、KSP 发行版与 KSP/AGP. 一个来源或数据集解析失败时会被精确定位到对应任务, 不再由聚合任务掩盖失败范围.

抓取器更新以下资源:

```text
agp-gradle-compat.properties
agp-releases.list
android-api-agp-compat.properties
android-studio-agp-compat.properties
android-studio-build-version.properties
android-studio-codename-version.properties
android-studio-codename.properties
android-studio-latest-stable.json
gradle-kotlin-compat.properties
java-gradle-compat.properties
kotlin-r8-compat.properties
ksp-agp-compat.properties
ksp-releases.properties
```

各数据集的保留边界集中在 `scraper.config.json`. `androidStudio` 是 AGP 兼容表的支持下限, `androidStudioArchive` 与 `kotlin` 则保留更宽的历史数据, 避免迁移更新器本身时意外缩减已有覆盖范围. Android API/AGP 表完整保留官方列出的 API 等级, 用于从消费端的 `COMPILE_SDK_VERSION` / `TARGET_SDK_VERSION` 自动推导 AGP 下界. 生成目标固定为 `src/main/resources/org/autojs/build/platform/data`. `android-studio-latest-stable.json` 是供 AutoJs6 等下游仓库读取的显式数据契约, 包含稳定版名称、版本、发布日期和 Windows EXE/ZIP 及 Linux TAR 的精确字节数. 写入前会先比较忽略生成时间戳后的内容; KSP 发布日期和 Android Studio 代号诞生日期作为数据的一部分参与比较, 因此上游没有变化时不会产生无意义的文件改动.
