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
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-LAYOUT]
pilot: WSC
---

# Code Review Skill（WSC 试点）

## 适用范围

- 审查对象：`frontend/**`、`backend/**`、`contracts/openapi/**`
- 输出模板：`ai/schemas/templates/code-review.md`
- 技术约定：`skill.frontend-developer`、`skill.backend-developer`、`RULE-ORG-STACK`
- 工作目录：仓库根目录

## 审查工作流

1. 对照任务包 `allowModify` 与 REQ 映射。
2. 按变更路径选用前端/后端检查清单。
3. 可复跑失败的静态/测试命令；不替代实现者自检。
4. 仅对规则内问题开 P0/P1；P0/P1 清零前不得批准。

## 前端必查（Vue）

- 变更是否落在 `frontend/src/features/<feature>/` 约定目录
- 是否经 `frontend/src/api/client.ts`；有无硬编码后端 URL
- 异步页是否覆盖 loading/empty/error
- 写入口是否仅靠前端隐藏而无后端鉴权（P0）
- 是否新增第二套状态管理/HTTP Client/UI（无 ADR 则 P0）

## 后端必查（Spring）

- 分层是否被穿透（Controller → Repository）
- 密钥/连接串是否入仓
- 迁移是否在 `db/migration/` 且破坏性变更含回滚说明
- 上链是否经适配层（DEC-WSC-002）
- 产品编码唯一校验（DEC-WSC-001）；分类删除保护（DEC-WSC-003）
- OpenAPI 是否同步 `contracts/openapi/`（若契约已冻结）

## 不可风格阻塞

- 已由 formatter / lint 统一的格式
- 未写入 Rule 且无用户影响的命名偏好

## 禁止事项

- 不得把 `REQUEST_CHANGES` 改写为 `APPROVE`
- 不得在审查意见中粘贴真实密钥或生产数据
