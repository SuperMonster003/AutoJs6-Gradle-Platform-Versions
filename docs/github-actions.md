# GitHub Actions 配置与使用

仓库内置三条自动化链路:

- `Build and test`: 每次推送及拉取请求均在 Temurin 17 上运行抓取器单元测试、Gradle 测试和无头 sample 决策.
- `Platform data`: 每周一北京时间 09:17 只读检查官方上游数据; 手动选择 `update` 时更新数据、复验并创建拉取请求.
- `Publish release`: 只允许从版本标签手动触发, 经 `release` Environment 审批后发布 Maven Central 和/或 Gradle Plugin Portal.

## 一次性配置 release Environment

在 GitHub 仓库依次打开 `Settings` → `Environments` → `New environment`, 名称填写 `release`.

建议配置:

1. 将自己设为 Required reviewer. 如果目前只有自己维护仓库, 不要启用 `Prevent self-review`, 否则无人能够批准自己发起的发布.
2. 在 Deployment branches and tags 中仅允许标签 `v*`.
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

正式签名密钥指纹应由发布者在本机核验. 当前项目首次发行使用的指纹为:

```text
6533 227D 9B22 7132 07B4  4CA5 3278 716E 2E61 74D7
```

若需从本机 GnuPG 导出供 Environment secret 使用的 ASCII 私钥, 请在受控终端自行运行以下命令, 随后把临时文件内容保存到 `SIGNING_KEY`, 并安全删除临时文件:

```powershell
gpg --armor --export-secret-keys 3278716E2E6174D7 > signing-key.asc
```

若要使用 `Platform data` 的 `update` 模式自动创建 PR, 还需在 `Settings` → `Actions` → `General` → `Workflow permissions` 中启用 `Allow GitHub Actions to create and approve pull requests`. 工作流自身只申请创建分支与 PR 所需的 `contents: write` / `pull-requests: write`, 不会批准或合并 PR.

## 发行步骤

1. 更新版本号、发行日志与文档, 完成测试并提交发行提交.
2. 确认 `VERSION_BUILD` 等于该发行提交包含的提交总数.
3. 创建并推送与 `VERSION_NAME` 对应的标签, 例如 `v1.7.0`.
4. 打开 `Actions` → `Publish release` → `Run workflow`, 在分支/标签选择器中选择刚推送的 `v1.7.0` 标签.
5. `version` 填写不带 `v` 的版本号. `targets` 通常选 `both`; 若一次发布部分成功, 重跑时只选尚未成功的目标, 避免重复上传不可变版本.
6. Maven Central 默认选择 `USER_MANAGED`: CI 会构建、签名、上传并等待校验通过, 之后仍需登录 Central Portal 检查对应 deployment 并点击 Publish. 只有明确希望校验通过后立即公开时才选择 `AUTOMATIC`.
7. `validate` job 全部通过后, 在等待中的 `release` Environment deployment 上人工批准; secrets 只会在批准后的 publish job 中读取.

发布工作流要求所选引用是 `v<version>` 标签, 且标签中的 `VERSION_NAME` 与输入一致、`VERSION_BUILD` 与提交总数一致. 任一条件不满足都会在接触发布凭据前停止.

## 数据更新

定时 `check` 只读运行. 如果官方数据发生语义变化, job 会以清晰提示失败, 但不会直接改写默认分支.

需要更新时手动运行 `Platform data`, 将 `mode` 选为 `update`. 工作流只允许生成 `src/main/resources/org/autojs/build/platform/data/` 下的改动; 随后会运行 Node/Gradle/sample 验证并由 `github-actions[bot]` 创建 PR, 仍需维护者审阅和合并.
