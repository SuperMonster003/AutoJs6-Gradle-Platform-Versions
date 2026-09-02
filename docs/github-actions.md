# GitHub Actions 配置与使用

仓库内置四条互相独立、又能串联的自动化链路:

- `Build and test`: 每次推送及拉取请求均在 Temurin 17 上运行抓取器单元测试、Gradle 测试和无头 sample 决策.
- `Platform data`: 每日北京时间 09:17 刷新官方上游数据; 仅在产生语义变化时准备下一补丁版本、提交并创建注释标签, 再启动受保护的正式发布工作流. 也可手动执行只读检查、数据 PR 或同一发行路径.
- `Publish release`: 从版本标签手动或由数据工作流触发. 通过 `release` Environment 审批后, 将 Central bundle 以 `USER_MANAGED` 上传, 等待 `VALIDATED`, 使用官方 API 最终发布, 同时发布 Gradle Plugin Portal; 两端公共构件均可解析后创建 GitHub Release.
- `Finalize Central deployment`: 输入已有 Central deployment UUID, 可只读检查或从 `VALIDATED` 继续发布并等待公共同步; 适用于历史运行、局部失败或 Central Portal 登录不可用时的恢复.

发布凭据只存在于 `release` Environment 中. 定时工作流可以自动发现、验证、提交、打标签并发起发布, 但在读取密钥和执行不可逆的仓库发布前仍保留一次人工批准.

## 一次性配置 release Environment

在 GitHub 仓库依次打开 `Settings` → `Environments` → `release`.

建议保持以下配置:

1. 将自己设为 Required reviewer. 如果目前只有自己维护仓库, 不要启用 `Prevent self-review`, 否则无人能够批准自己发起的发布.
2. 在 Deployment branches and tags 中只添加标签规则 `v*`; 不要允许普通分支取得发布凭据.
3. 根据风险偏好关闭管理员绕过保护规则的能力.
4. 在该 Environment 内保存下列六个 secrets, 不要把值写进仓库、Issue、Actions 日志或聊天记录:

| Secret | 内容 |
| --- | --- |
| `CENTRAL_USERNAME` | Central Portal User Token 的 username |
| `CENTRAL_PASSWORD` | Central Portal User Token 的 password |
| `GRADLE_PUBLISH_KEY` | Gradle Plugin Portal API key |
| `GRADLE_PUBLISH_SECRET` | Gradle Plugin Portal API secret |
| `SIGNING_KEY` | ASCII-armored PGP 私钥全文, 包含 BEGIN/END 行 |
| `SIGNING_PASSWORD` | 上述 PGP 私钥的口令 |

正式签名密钥指纹应由发布者在本机核验. 当前项目发行密钥的指纹为:

```text
6533 227D 9B22 7132 07B4  4CA5 3278 716E 2E61 74D7
```

若需从本机 GnuPG 导出供 Environment secret 使用的 ASCII 私钥, 请在受控终端自行运行以下命令, 随后把临时文件内容保存到 `SIGNING_KEY`, 并安全删除临时文件:

```powershell
gpg --armor --export-secret-keys 3278716E2E6174D7 > signing-key.asc
```

`Platform data` 的发行 job 明确申请 `contents: write` 与 `actions: write`, 分别用于原子推送发行提交/标签和触发 `Publish release`. 手动 `update-pr` 模式还申请 `pull-requests: write`; 若使用该模式, 需在 `Settings` → `Actions` → `General` 中允许 GitHub Actions 创建 pull request. 所有其他 job 保持只读或仅取得完成其职责所需的权限.

## 每日数据自动发行

定时器使用 UTC cron `17 1 * * *`, 即北京时间每天 09:17. 每次运行依次执行:

1. 从 `master` 的完整历史开始, 安装并运行全部上游抓取器.
2. 同时检查本次生成的工作区差异, 以及最新版本标签之后已经通过 PR 合并的数据差异. 后者保证 `update-pr` 合并后即使第二次抓取不再改文件, 下一次定时运行仍会发行这些数据.
3. 若数据无语义变化, 以成功状态结束, 不修改分支、标签、版本或任何远端仓库.
4. 若有变化, 检查最新标签与 `VERSION_NAME` 的基线关系, 并拒绝把插件实现、构建逻辑等超出自动数据发行边界的未发行改动意外带入补丁版本.
5. 自动将稳定版本的 patch 位加一, 令 `VERSION_BUILD` 等于将要产生的提交总数, 同步 `.readme/common.json`, 为 10 种语言生成数据更新日志并重新生成全部 README/CHANGELOG.
6. 执行翻译结构检查、Node 抓取器测试、Gradle 测试、Temurin 无头 sample 和隔离 Maven 发布测试; 同时验证生成器幂等、改动白名单以及目标版本在 GitHub、Central 和 Plugin Portal 上尚未占用.
7. 以 `github-actions[bot]` 创建一个发行提交和注释标签. 推送前再次确认远端 `master` 仍是本次运行开始时的提交, 然后使用一次原子 push 同步分支与标签, 避免只推成功其中一项.
8. 在新标签上触发 `Publish release`, 目标为 `both`, 并等待维护者批准 `release` Environment.
9. 获批后, 工作流签名并发布 Maven Central 与 Gradle Plugin Portal. 两套公开消费 URL 均返回成功后, 自动从英文 CHANGELOG 提取当前版本说明并创建 GitHub Release.

这条链路不会取消人工最终门禁. 维护者唯一的常规操作是在收到待审批的 deployment 后检查版本/标签并批准. 若未批准或主动拒绝, 已生成的提交与标签会保留, 但发布凭据不会暴露、仓库构件也不会上传; 日后可直接从该标签手动重跑 `Publish release`, 无需制造另一个版本.

## Platform data 手动模式

打开 `Actions` → `Platform data` → `Run workflow`, 从 `master` 选择以下模式之一:

- `check`: 只读抓取和比较. 最新时成功; 发现更新时以明确提示失败, 不写文件.
- `update-pr`: 更新数据、运行 Node/Gradle/sample 验证, 将严格限制在数据资源目录内的差异提交到临时分支并创建 PR. 合并后由下一次定时 `release` 识别并发行.
- `release`: 立即执行与每日定时器完全相同的安全发行链, 适合不等待下一次 cron.

## 手动正式发行

非纯数据版本仍由维护者准备版本元数据和注释标签, 然后使用统一的受保护发布入口:

1. 更新版本号、发行日志与文档, 完成测试并提交发行提交.
2. 确认 `VERSION_BUILD` 等于该发行提交包含的提交总数.
3. 创建并推送与 `VERSION_NAME` 对应的注释标签, 例如 `v1.8.0`.
4. 打开 `Actions` → `Publish release` → `Run workflow`, 在分支/标签选择器中选择刚推送的标签.
5. `version` 填写不带 `v` 的版本号; `targets` 通常选择 `both`; `complete_github_release` 保持启用.
6. `validate` job 会在不接触发布凭据的情况下核对标签、版本、提交总数并执行测试. 通过后检查等待中的 `release` deployment 并批准.

Central 不再提供 `AUTOMATIC`/`USER_MANAGED` 人工选择. 工作流固定使用更易审计的 `USER_MANAGED`: 上传后先得到独立的 `VALIDATED` 状态和 UUID, 再在同一受保护 job 中核对 deployment 身份、名称及所有 PURL, 最后调用 Sonatype 官方发布 API并等待 `PUBLISHED`. 因而正常发行不依赖 Central Portal 登录, 也不会跳过中间校验点.

`targets` 用于不可变版本的局部恢复. 若 Central 已公开而 Portal 失败, 重跑时只选 `gradle-plugin-portal`; 反之只选 `maven-central`. GitHub Release job 始终再次验证两端均公开, 因此局部恢复不会提前宣布一个不完整版本.

## 手填 UUID 的 Central 恢复入口

若已有 deployment 到达 `VALIDATED` 但原工作流没有继续, 不要重新上传相同的不可变版本. 打开 `Actions` → `Finalize Central deployment`:

1. 在分支/标签选择器中选择与构件版本相同的 `v<version>` 标签.
2. `deployment_id` 粘贴 Central UUID; `expected_version` 填写不带 `v` 的版本.
3. 第一次可选择 `inspect`, 只核对 UUID、deployment 名称、PURL 和当前状态.
4. 准备完成后选择 `publish`; 通常保持 `wait_for_public` 和 `complete_github_release` 启用, 然后批准 `release` Environment.

`publish` 会自动完成 API 发布、等待 `PUBLISHED`、等待 Central 公共 POM/JAR/marker 可解析, 再检查 Plugin Portal. 若两端都已公开且 GitHub Release 尚不存在, 会自动补建; 已发布的 Central deployment 或已存在的 GitHub Release均按幂等成功处理.

如果 Central 已经发布、但 Plugin Portal 尚未发布, UUID 恢复 job 的 Central 部分仍会成功, GitHub Release 步骤则会等待后失败. 此时从相同标签运行 `Publish release`, 将 `targets` 设为 `gradle-plugin-portal`, 即可完成剩余目标并创建 GitHub Release.

## 关键安全边界

- 所有正式发布只接受与输入版本完全一致的 `v<version>` 标签.
- Environment secrets 在人工批准前不可读取.
- Central UUID、deployment 名称和每个 PURL 的版本必须全部匹配, 才允许调用发布 API.
- 自动数据发行只接受已声明的生成/文档文件; 未发行的产品代码变化会要求改走人工发行.
- 目标版本必须在标签、GitHub Release、Central 与 Plugin Portal 中均未占用.
- 远端 `master` 在长时间测试期间若有推进, 原子 push 会停止, 避免覆盖并发工作.
- GitHub Release 仅在 Central 与 Plugin Portal 的 marker 和实现构件都可从公共仓库下载后创建.

官方行为说明:

- GitHub `GITHUB_TOKEN` 触发的普通 push 不会递归启动新工作流, 但 `workflow_dispatch` 是明确允许的例外: <https://docs.github.com/en/actions/concepts/security/github_token#when-github_token-triggers-workflow-runs>
- Environment 审批、标签限制和 secrets 可见性: <https://docs.github.com/en/actions/reference/workflows-and-actions/deployments-and-environments>
- Central Publisher API 的状态与 `POST /api/v1/publisher/deployment/<deploymentId>`: <https://central.sonatype.org/publish/publish-portal-api/>
