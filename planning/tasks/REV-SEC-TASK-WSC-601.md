# 安全审查（契约冻结层）

```yaml
reviewId: REV-SEC-TASK-WSC-601
taskId: TASK-WSC-601
planId: PLAN-WSC-5.2
round: 1
role: securityReviewer
actorInstance: security-reviewer-wsc-601-r1
decision: APPROVE
p0: 0
p1: 0
riskTagsReviewed:
  - auth-model-change
  - rbac-breaking
contracts: wsc-contracts@2.2.0
reviewedAt: 2026-08-05T16:05:00+08:00
basedOn:
  - ai/agents/security-reviewer.md
  - ai/workflow/policies.yaml (riskTriggers.securityReviewer)
  - planning/approved/PLAN-WSC-5.2.md (§3.1 / §4 / §6.2 / TASK-WSC-601)
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-601.md
  - planning/tasks/REV-TASK-WSC-601.md
  - planning/tasks/TESTRUN-TASK-WSC-601.md
  - contracts/openapi/openapi.yaml
  - contracts/rbac/matrix.yaml
  - contracts/errors/codes.yaml
  - contracts/ui/state-matrix.md
  - contracts/security/sensitive-fields.md
  - frontend/src/api/**
mustDifferFrom:
  - developer-wsc-601
  - code-reviewer-wsc-601-r1
  - tester-wsc-601
scopeNote: |
  本任务为契约冻结层（contracts + frontend/src/api），非后端实现。
  后端鉴权/会话/RBAC 实证归 TASK-WSC-602/603；本审查不要求 601 实现后端。
```

## 结论

**APPROVE** — 对照 `auth-model-change` / `rbac-breaking` 与 §3.1 硬冻结字面：未发现契约层认证绕过、客户端选角色、匿名可达 `/admin/users`、或 ADMIN 产品写/导入被重新打开的语义。**P0=0，P1=0**。残留 P2 为 602 实现提示，不阻塞本任务契约门禁。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-SEC-WSC-601-R1-001 | P2 | `/admin/users` GET/POST 与 `/admin/users/{userId}` PUT 声明「仅 ADMIN」+ `403 Forbidden`，并继承全局 `security: SessionCookie`（全文件仅 `POST /auth/session` 以 `security: []` 匿名开放）。但三操作均未显式列出 `401 Unauthorized` 响应条目。不构成匿名可达或绕过；可能使 602 实现者混淆未登录 vs 非 ADMIN | 602 OpenAPI 消费/实现时：未登录→401；已登录非 ADMIN→403/`ERR_FORBIDDEN`；可选在后续契约修订补 401 响应引用（非 601 必改） | REQ-USER-001 |
| FIND-SEC-WSC-601-R1-002 | P2 | `LoginRequest` 已删除 `role` 属性且 description 禁止客户端选角色；OAS 3.0 对象默认允许未声明附加属性，契约未写 `additionalProperties: false`。属纵深加固缺口，非「仍允许 LoginRequest.role」字面绕过（属性已删；client `LoginRequest` 亦无 role） | 可选后续契约加固 `additionalProperties: false`；602 登录实现须忽略/拒绝 body 中未知 `role` 字段，不得写入会话 | REQ-SHELL-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 审查清单（对照焦点）

### 1. 会话切角色 / LoginRequest（auth-model-change）

| 项 | 证据 | 结果 |
|---|---|---|
| `POST /auth/session/role` 始终 410 + `ERR_ROLE_SWITCH_DISABLED`；无成功路径 | `openapi.yaml` L87–118：summary「始终禁用」；responses 仅 410(+example) 与 401 | PASS |
| 同码 ∈ `codes.yaml` + `ApiResponseError.code` enum | `codes.yaml` L82–86；openapi enum L1054 | PASS |
| `LoginRequest` **无** `role`；角色来自 `sys_user` | openapi L1090–1098；`frontend/src/api/auth.ts` `LoginRequest` 仅 username/password | PASS |
| client `switchSessionRole` 预期 410 / 无成功路径 | `auth.ts` L40–48 注释与调用 | PASS |

### 2. RBAC matrix（rbac-breaking）

| 项 | 证据 | 结果 |
|---|---|---|
| 产品写/导入仅 PROVIDER；ADMIN hidden+403 | `matrix.yaml` ADMIN L17–20；PROVIDER L32–35；USER L47–50 | PASS |
| `userManage*` 仅 ADMIN | ADMIN L22–23 expect 200；PROVIDER/USER L37–38 / L52–53 expect 403 | PASS |
| `sessionRoleSwitch` 三角色均 410 + `ERR_ROLE_SWITCH_DISABLED` | matrix L25 / L40 / L55 | PASS |
| OpenAPI 叙述对齐：create/import 仅 PROVIDER | createProduct L494–496；import L803–805；分类维护「管理员」保留合理（L262） | PASS |
| notes：登录绑定 / 隐藏≠授权 / 公共目录无写 | matrix notes L57–63 | PASS |

### 3. 用户 API 敏感字段与错误码

| 项 | 证据 | 结果 |
|---|---|---|
| `AdminUser` 响应 schema 无 password/passwordHash | openapi L1117–1130；examples 无口令字段 | PASS |
| Create/Update password 仅写输入 | `AdminUserCreate`/`AdminUserUpdate`；Update description「响应永不回显」 | PASS |
| client `AdminUser` 类型无 password/hash | `frontend/src/api/admin.ts` | PASS |
| `ERR_FORBIDDEN`(403)、`ERR_USER_USERNAME_CONFLICT`(409)、`ERR_USER_NOT_FOUND`(404) 硬同步 | codes L76–98；openapi 403/409/404 examples + enum | PASS |
| soft-delete = PUT `deleted` | openapi update description + example softDelete | PASS |

### 4. sensitive-fields / state-matrix

| 项 | 证据 | 结果 |
|---|---|---|
| password/passwordHash 响应禁止 | `contracts/security/sensitive-fields.md` 行「用户 password / passwordHash」 | PASS |
| 导入报告白名单不扩大 | sensitive-fields + state-matrix 实现约束；codes `reportFieldsWhitelist` | PASS |
| 角色只读；导入宿主=我的产品；公共目录只读 | `state-matrix.md` 版本头 2.2.0；菜单/角色区；导入宿主约束 L42–44 | PASS |

### 5. 匿名可达危险面

| 项 | 证据 | 结果 |
|---|---|---|
| 全局默认需会话 | openapi L26–27 `security: SessionCookie` | PASS |
| 唯一匿名入口为登录 | 全文件仅 L59 `createSession` `security: []` | PASS |
| `/admin/users*` 未清除 security；声明仅 ADMIN | paths L309–440；summary/description「仅 ADMIN」+ 403 | PASS |
| 未引入新的 `security: []` 写面或管理面 | grep `security:` 仅登录处清空 | PASS |

### 6. 范围边界

| 项 | 结果 |
|---|---|
| 未要求 601 实现后端安全控制 | PASS（本报告不宣称 602 实证） |
| 未发现契约绕过语义（如仍定义 `LoginRequest.role`） | PASS（属性已删；见 FIND-002 纵深加固仅为 P2） |
| 未修改 contracts/frontend；未写 state/events；未标 VERIFIED | PASS |

## 残余风险（交 602 / 编排）

- 后端会话绑定、口令哈希、拦截器与 matrix 单元格实证 → **TASK-WSC-602**（及 603 RBAC 实现），须再次触发 `securityReviewer`（计划已标 `auth-model-change`）。
- FIND-001/002 为契约清晰度/纵深加固，不构成 601 P0/P1；实现侧须按 §3.1 语义执行。

## 决策权声明

- 本实例 `security-reviewer-wsc-601-r1`；未兼任 developer / codeReviewer / tester。
- 未修改被审业务代码（contracts / frontend）；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- **decision: APPROVE**（P0/P1 清零；契约层 auth-model-change / rbac-breaking 门禁通过）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2 |
| P3 | 0 |
| findingCount | 2 |
