# 代码审查

```yaml
reviewId: REV-TASK-WSC-601
taskId: TASK-WSC-601
planId: PLAN-WSC-5.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-601-r1
decision: APPROVE
p0: 0
p1: 0
contracts: wsc-contracts@2.2.0
reviewedAt: 2026-08-05T15:35:00+08:00
basedOn:
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-601.md
  - planning/approved/PLAN-WSC-5.2.md (§3.1 / TASK-WSC-601)
  - contracts/VERSION
  - contracts/openapi/openapi.yaml
  - contracts/errors/codes.yaml
  - contracts/rbac/matrix.yaml
  - contracts/ui/state-matrix.md
  - contracts/req-coverage.md
  - contracts/security/sensitive-fields.md
  - frontend/src/api/**
mustDifferFrom: developer-wsc-601
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。`wsc-contracts@2.2.0` 相对 §3.1 硬冻结字面齐全：`VERSION`/OpenAPI `info.version`=2.2.0；命名 `AdminUser*`；`POST /auth/session/role`→410+`ERR_ROLE_SWITCH_DISABLED`；`LoginRequest` 无 `role`；用户/切角色错误码与 OpenAPI enum 硬同步；matrix ADMIN 产品写/导入 hidden+403；state-matrix 导入宿主=我的产品；2.1.0 typeSpecific/导入四态码/报告白名单未回退。`frontend/src/api/**` 手工对齐可消费；审查复跑 `pnpm run typecheck` **EXIT 0**。正式 VERIFIED 仍须独立 tester 执行完整 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-601-R1-001 | P2 | `tests/contracts/check-contracts.mjs` 仍硬编码 `VERSION expected 1.1.0`；对照 `contracts/VERSION`=2.2.0 时脚本失败。脚本不在 601 `writeSet`；DEV 已声明未改。2.2.0 语义由契约文件与 typecheck 覆盖，不阻塞本任务 | 另开任务（如 605/契约工具链）将脚本期望升至当前 contracts 主版本并纳入 CI；勿要求 601 越界改 `tests/contracts/**` | PLAN-WSC-5.2 TASK-WSC-601 writeSet/testScope |
| FIND-WSC-601-R1-002 | P2 | 工作区另有 `frontend/src/features/**`、`layouts/**`、`router/**`、以及大量 `backend/**` 等 601 `denyModify` 路径脏改动。DEV-601 交付面声明仅 `contracts/**` + `frontend/src/api/**`；本审查将 601 范围限定于此 | 编排/提交时按任务 writeSet 隔离暂存区；勿将预落地/他任务脏文件并入 601 提交 | PLAN-WSC-5.2 § TASK-WSC-601 writeSet/denyModify |
| FIND-WSC-601-R1-003 | P3 | `listUsers` description 写「AdminUser[]」，实际 200 schema 为 `ApiResponseAdminUserPage`（`data.items`） | 可选统一 prose 与 schema 用语 | REQ-USER-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| 601 交付面 ⊆ writeSet（`contracts/**`、`frontend/src/api/**`；未改 package.json/lock） | PASS（见 FIND-002 工作区脏文件隔离说明） |
| 未改 denyModify：features/layouts/router/styles/theme/components、backend、sql、e2e、product、planning、state/events（本任务交付面） | PASS（交付 diff 限定 writeSet；审查未写 state/events） |
| `mustDifferFrom: developer-wsc-601` | PASS（本实例 `code-reviewer-wsc-601-r1`） |

### 2. §3.1 硬冻结

| 项 | 证据 | 结果 |
|---|---|---|
| `VERSION`=2.2.0；OpenAPI `info.version` 一致 | `contracts/VERSION`；`openapi.yaml` L4 | PASS |
| matrix：ADMIN 产品写/导入 hidden+403；PROVIDER 写/导入+myProducts；USER 只读；userManage 仅 ADMIN；sessionRoleSwitch 410 | `rbac/matrix.yaml` 三角色行 + notes | PASS |
| `GET /catalog/products?mine`；`Product.createBy` | OpenAPI `mine` 参数；`Product.createBy`；client `listProducts`/`createBy` | PASS |
| `GET /catalog/l2-distribution` 命名 `L2Distribution*`（totalProducts+items code/name/count） | OpenAPI schemas + `catalog.ts` `getL2Distribution` | PASS |
| `POST /auth/session/role` 始终 410 + `ERR_ROLE_SWITCH_DISABLED`；example 齐全 | OpenAPI path + examples | PASS |
| 同码 ∈ `codes.yaml` + `ApiResponseError.code` enum | codes + enum 列表 | PASS |
| `LoginRequest` **无** `role`；description 声明角色来自 `sys_user` | OpenAPI `LoginRequest`；`auth.ts` | PASS |
| `/admin/users` 命名 `AdminUser`/`Create`/`Update`；required 齐全；软删 PUT `deleted`；响应禁 password/hash | OpenAPI schemas + examples；`admin.ts`；`sensitive-fields.md` | PASS |
| `ERR_USER_USERNAME_CONFLICT`(409)、`ERR_USER_NOT_FOUND`(404)、`ERR_FORBIDDEN` 硬同步 | codes + OpenAPI 409/404 examples + enum | PASS |
| 废止 ADMIN 可产品写/导入叙述 | `createProduct`/`import` summary/description 仅 PROVIDER；分类维护「管理员」保留合理 | PASS |
| state-matrix 版本头 2.2.0；导入宿主=我的产品；角色只读；公共目录只读 | `ui/state-matrix.md` | PASS |
| req-coverage V1.4 行 | `req-coverage.md` SHELL/RBAC/CAT-009/USER-001/CAT-010 | PASS |

### 3. 2.1.0 不回退面

| 项 | 证据 | 结果 |
|---|---|---|
| `TypeSpecificApi` swaggerFileContent + endpoints；写冲突客户端为准 | OpenAPI TypeSpecific* | PASS |
| OTHER `contentDescription`；`NO_UPDATE` | ProductType / UpdateFrequency / TypeSpecificReportOrOther | PASS |
| 导入四态码 + `ERR_IMPORT_TEMPLATE_UNSUPPORTED` | codes `importSemantics` + OpenAPI examples + state-matrix 四态表 | PASS |
| 报告白名单四字段 | `reportFieldsWhitelist`；state-matrix 实现约束 | PASS |
| `ImportTemplateColumns` v0729 | OpenAPI example ≡ client `IMPORT_TEMPLATE_COLUMNS` | PASS |

### 4. frontend/src/api 对齐 + typecheck

| 项 | 证据 | 结果 |
|---|---|---|
| `CONTRACT_VERSION`=2.2.0 | `client.ts` | PASS |
| auth：无 role 登录；`switchSessionRole` | `auth.ts` | PASS |
| admin：CRUD + soft-delete 类型 | `admin.ts`（新建）+ `index.ts` re-export | PASS |
| catalog：`getL2Distribution` + `createBy` + mine 经 query | `catalog.ts` | PASS |
| typecheck | `cd frontend && pnpm run typecheck` → **EXIT 0**（vue-tsc --noEmit） | PASS |

### 5. 预落地对账自动化证据

| P0 检查 | 审查复核 | 结果 |
|---|---|---|
| 切角色码硬同步 | codes + enum 已读确认 | PASS |
| LoginRequest 无 role | OpenAPI + client | PASS |
| AdminUser required | OpenAPI required 六字段 | PASS |
| matrix 三角色 | matrix.yaml 全文 | PASS |
| 导入/typeSpecific/白名单不回退 | 见 §3 | PASS |

DEV 一次性 `_check-601-acceptance.mjs` 已删；本审查以契约文件字面复核替代，不升格为 P1。

## 复跑测试摘要

| 命令 | 结果 |
|---|---|
| `cd frontend && pnpm run typecheck` | **PASSED** — EXIT 0（2026-08-05） |
| `node tests/contracts/check-contracts.mjs` | **FAILED** — VERSION expected 1.1.0, got 2.2.0（FIND-001；非 601 writeSet） |

## 残余风险（交 tester）

- 契约 lint / OpenAPI 正式校验工具链（含升版 check-contracts）由 tester `testScope` 补跑或跟进 FIND-001 专任任务。
- 后端/UI 实现（602..605）不在本任务验收面；本报告不宣称端到端 RBAC/用户 CRUD 已实现。
- 提交前须按 FIND-002 隔离 writeSet，避免预落地脏树混入 601。

## 决策权声明

- 审查者未修改被审业务代码（contracts/frontend 实现）；未写 `state.yaml` / `events.jsonl`。
- 未兼任 developer；未调度 tester；未代替 tester 宣称 VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2 |
| P3 | 1 |
