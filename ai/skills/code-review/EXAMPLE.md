# Code Review Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认审查标准。以下路径与检查项均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.code-review
version: 1.0.0
status: active
owner: tech-lead
inputSchema: task-package@1
outputSchema: code-review@1
compatibleRoles: [codeReviewer]
appliesTo:
  paths: ["**/*"]
relatedRules: [RULE-ORG-CODING, RULE-ORG-SECURITY, RULE-PROJECT-ARCHITECTURE]
---

# Code Review Skill

## 适用范围

- 审查对象：`frontend/**`、`backend/**`、`contracts/openapi/**`
- 输出模板：`ai/schemas/templates/code-review.md`
- 技术约定：`skill.frontend-developer`、`skill.backend-developer`、`RULE-ORG-CODING`
- 工作目录：仓库根目录

## 审查工作流

1. 对照任务包 `allowModify` 与 REQ 映射。
2. 按变更路径选用前端/后端检查清单。
3. 运行适用静态命令（不替代实现者自检，但可复跑失败项）。
4. 仅对规则内问题开 P0/P1；风格类已由 formatter 覆盖的不得阻塞。

## 前端必查（Vue）

- 变更是否落在 `frontend/src/features/<feature>/` 约定目录
- 是否经 `frontend/src/api/client.ts`；有无硬编码后端 URL
- Props/Emits 是否有类型；`v-for` 是否用稳定业务 ID
- 异步页是否覆盖 loading/empty/error
- 是否新增第二套状态管理/HTTP Client（无 ADR 则 P0）

## 后端必查（Spring）

- 分层是否被穿透（Controller → Repository）
- 错误体是否符合 `{ code, message, correlationId, details }`
- 密钥/连接串是否入仓
- 迁移是否在 `db/migration/` 且含回滚说明（破坏性变更）
- OpenAPI 是否同步 `contracts/openapi/`

## 不可风格阻塞

- Spotless / ESLint / Prettier 已统一的格式
- 未写入 Rule 且无用户影响的命名偏好

## 输出示例

- 任务：`TASK-ORDER-014`
- 结论：`APPROVE`（无 P0/P1）
- P0：无
- P1：无
- 建议：订单筛选空态文案可后续统一到 i18n（非阻塞）
```
