# RULE-ORG-RELEASE 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认发布流程。以下检查名与路径均是假设值，复制时必须按真实 CI/CD 修改。

```markdown
---
id: RULE-ORG-RELEASE
version: 1.0.0
status: active
owner: maintainer
override: allowed-with-adr
appliesTo:
  roles: ["maintainer", "orchestrator", "integrationReviewer", "tester"]
reviewBy: 2026-07-15
---

# RULE-ORG-RELEASE

## 分支模型

- 模型：trunk-based
- 默认基线分支：`main`
- 分支命名：`feature/<TASK-ID>-<简短描述>`、`fix/<TASK-ID>-<简短描述>`、`hotfix/<INCIDENT-ID>-<简短描述>`
- 提交约定：Conventional Commits（`feat:`/`fix:`/`chore:`）；格式由 commitlint 校验，与 Spotless/ESLint 独立

## PR

- 最少审查人数：1
- 必需审查身份/角色：techLead 或 codeReviewer（不可自审）
- 必需检查：`ci/lint`、`ci/test`、`ci/build`、`ci/openapi-diff`（契约变更时）
- 必需章节：Summary、Test plan、关联 REQ/TASK ID
- 合并条件：全部 CI 绿、无 open P0/P1 review 意见、OpenAPI 变更已同步 `contracts/openapi/`
- 紧急合并例外：生产 P0 故障；maintainer 口头批准 + 4h 内补 PR 与 retro 记录 `temp/ai/audit/`

## 发布

- 环境与晋级：`dev`（合并 main 自动）→ `staging`（maintainer 手动）→ `prod`（maintainer 手动 + 集成审查）
- 发布窗口：工作日 10:00–18:00（UTC+8）；窗口外须 maintainer + productOwner 双批
- 是否强制 Maintainer 人工批准：是（与 `ai/workflow/policies.yaml` 一致）
- 批准身份：`maintainer`（赵维护）
- 是否要求集成审查：是
- 是否要求全部 P0 已验证：是
- 回滚负责人：maintainer
- 紧急回滚程序：见 `docs/runbooks/rollback-acme-orders.md`（kubectl rollout undo / 上一版本镜像 tag）
```
