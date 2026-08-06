# 安全审查（实现态 · 真登录 / 用户管理 / 切角色下线）

```yaml
reviewId: REV-SEC-TASK-WSC-602
taskId: TASK-WSC-602
planId: PLAN-WSC-5.2
round: 1
role: securityReviewer
actorInstance: security-reviewer-wsc-602-r1
decision: APPROVE
p0: 0
p1: 0
riskTagsReviewed:
  - auth-model-change
  - authn
  - handles-pii
  - rbac-breaking
contracts: wsc-contracts@2.2.0（只读对照）
reviewedAt: 2026-08-05T17:40:00+08:00
basedOn:
  - ai/agents/security-reviewer.md
  - ai/workflow/policies.yaml (riskTriggers.securityReviewer)
  - planning/approved/PLAN-WSC-5.2.md (§4 / §6.2 / TASK-WSC-602 acceptance)
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-602.md
  - planning/tasks/REV-TASK-WSC-602.md（round 2 APPROVE）
  - planning/tasks/TESTRUN-TASK-WSC-602.md（PASS）
  - 实现面只读：AuthController、UserAccountService、AdminUserController、PasswordHasher、AuthAuditLogger、LoginRequest、SysUserEntity、V6__create_sys_user.sql、authStore、RoleSwitcher、UsersAdminPage、WorkbenchLayout、SessionViews、SecurityWebConfig、SysUserAuthSecurityIntegrationTest
mustDifferFrom:
  - developer-wsc-602
  - developer-wsc-602-r2
  - code-reviewer-wsc-602-r2
  - tester-wsc-602
scopeNote: |
  正式 migration 可重复性/回滚门禁留给独立 migrationReviewer（schema-migration）。
  本审查仅对 V6 安全面做简评（无明文口令落盘、hash 列、软删字段）。
  产品写矩阵实证归 TASK-WSC-603；本审查确认 602 交付面未越权改产品授权语义。
```

## 结论

**APPROVE** — 对照 `auth-model-change` / `authn` / `handles-pii` / `rbac-breaking` 与 TASK-WSC-602 acceptance：真登录 BCrypt 校验且会话角色=账号角色；切角色 API 始终 410+`ERR_ROLE_SWITCH_DISABLED`且 UI 无可切换路径；`/admin/users` 仅 ADMIN（UI 门禁 + API 403）；响应与审计无明文口令/hash；种子口令仅文档化演示常量并以 BCrypt 入库；软删账号不可再登录；602 表面未越权改产品写矩阵。**P0=0，P1=0**。开放项均为 P2 残余加固，不阻塞本任务 securityReviewer 门禁。未标 VERIFIED。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-SEC-WSC-602-R1-001 | P2 | 软删仅置 `del_flag` 并阻断**新**登录（`authenticate`→`findByUsernameAndDelFlag(..., false)`）；**未**吊销该用户已有 `HttpSession`。停用后至会话失效/主动登出前，既有会话仍可调用需登录 API | 软删时吊销该用户会话，或在鉴权路径复核 `del_flag`/账号有效性 | REQ-USER-001 |
| FIND-SEC-WSC-602-R1-002 | P2 | `SecurityWebConfig` 配置 `WSC_SESSION`：`HttpOnly=true`，**未**设 `Secure` / `SameSite`。本地 HTTP 演示可接受；生产 HTTPS 下存在 Cookie 明文信道与 CSRF 纵深缺口 | 生产配置 `Secure=true` + `SameSite=Lax|Strict`（或等价 Spring Session cookie 属性） | REQ-SHELL-001；authn |
| FIND-SEC-WSC-602-R1-003 | P2 | `existsByUsername` 含软删行（与 CR FIND-WSC-602-R1-004 同面）：软删后同名无法重建，属账号生命周期/可用性，**非**认证绕过 | 可选：唯一约束改为未删范围，或文档化「软删占用用户名」 | REQ-USER-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 审查清单（对照焦点）

### 1. 真登录（authn / auth-model-change）

| 项 | 证据 | 结果 |
|---|---|---|
| 口令 BCrypt 单向哈希校验 | `PasswordHasher`（`BCryptPasswordEncoder`）；`UserAccountService.authenticate`→`passwordHasher.matches` | **PASS** |
| 会话角色 = 账号 `sys_user.role` | `authenticate` 构造 `SessionPrincipal` 时 `Role.fromString(user.getRole())`；登录响应 `SessionViews.toMap` 无客户端选角 | **PASS** |
| 错误密码失败 | `AuthController.login`→`Optional.empty()`→401；审计 `loginFailed(..., BAD_CREDENTIALS)`；集成测 `login_wrongPassword_returns401` | **PASS** |
| `LoginRequest` 无 `role`；未知字段（含旧 `role`）忽略 | `LoginRequest` record + `@JsonIgnoreProperties(ignoreUnknown = true)`；前端 `login(username, password)` 不传 role | **PASS** |
| 登录成功轮换会话（降 session fixation） | 先 `old.invalidate()` 再 `getSession(true)` | **PASS** |

### 2. 切角色下线（auth-model-change / REQ-SHELL-001）

| 项 | 证据 | 结果 |
|---|---|---|
| `POST /auth/session/role` → 410 + `ERR_ROLE_SWITCH_DISABLED` | `AuthController.switchRole` 无成功分支；不读 body/不改 session | **PASS** |
| 三角色均禁用 | 集成测 `sessionRoleSwitch_disabledForAllRoles`（admin/provider/user） | **PASS** |
| UI 无可切换路径 | `RoleSwitcher.vue` 只读 `<p>`，无 listbox/chevron/`switchRole`；`WorkbenchLayout` **永不挂载** RoleSwitcher；footer 唯一 `session-role-label` | **PASS** |
| store 无成功切角路径 | `authStore.switchRole` 期望 `ERR_ROLE_SWITCH_DISABLED`；无 UI 调用点（grep） | **PASS** |

### 3. `/admin/users` 与敏感数据（handles-pii / rbac-breaking）

| 项 | 证据 | 结果 |
|---|---|---|
| API 仅 ADMIN | `AdminUserController.requireAdmin`→`RbacMatrix.canManageUsers`（仅 `Role.ADMIN`）；非 ADMIN→403 `ERR_FORBIDDEN`；未登录→401 | **PASS** |
| UI 仅 ADMIN | Layout `userManageVisible = role === 'ADMIN'`；`UsersAdminPage` 内联 `role === 'ADMIN'`；非 ADMIN 无 CRUD 结构 | **PASS** |
| 路由需登录 | `/admin/users` 嵌于 `WorkbenchLayout` `requiresAuth: true` | **PASS** |
| 响应无 `password` / `passwordHash` | `AdminUser` 无口令字段；`toUserMap` 显式白名单序列化；集成测断言 doesNotExist + 不回显明文口令 | **PASS** |
| 会话响应无口令 | `SessionViews.toMap` 仅 userId/displayName/role/enterpriseName | **PASS** |
| 审计/日志无明文口令 | `AuthAuditLogger.loginFailed` 仅 username/reason/correlationId；tester `05-audit-checklist.md` PASS | **PASS** |

### 4. 种子口令与软删

| 项 | 证据 | 结果 |
|---|---|---|
| 种子不落生产密钥明文 | `DEFAULT_DEMO_PASSWORD="demo"` 文档化演示常量；`seed`→`passwordHasher.hash`；V6 SQL **无**明文口令 INSERT | **PASS** |
| 软删不可登录 | `authenticate` 仅 `delFlag=false`；CRUD 测软删后同口令→401 | **PASS** |

### 5. 602 不改产品写矩阵（归 603）

| 项 | 证据 | 结果 |
|---|---|---|
| denyModify 含 `RbacMatrix` / `WriteAuthorizationInterceptor` | PLAN writeSet/denyModify；DEV 声明未改；602 表面不宣称产品写验收 | **PASS**（范围） |
| 用户管理仅消费 `canManageUsers` | `AdminUserController.requireAdmin` 唯一调用点 | **PASS** |
| 当前矩阵产品写仍仅 PROVIDER | `RbacMatrix.canWriteProduct` / `canImportProduct` → `PROVIDER`；ADMIN 用户管理独立 | **PASS**（只读确认；正式产品写门禁归 603） |
| 前端 602 不依赖 603 向 `canManageUsers`/`canSeeMyProducts` 导出 | Layout/UsersAdminPage 内联 ADMIN；无 my-products 菜单/路由（R2） | **PASS** |

### 6. Migration 安全面简评（正式门禁→migrationReviewer）

| 项 | 证据 | 结果 |
|---|---|---|
| 表含 `password_hash`、唯一 `username`、`del_flag`、审计列 | `V6__create_sys_user.sql` | **PASS**（安全简评） |
| 脚本无明文口令/生产密钥 | 注释声明种子由应用 BCrypt 写入 | **PASS** |
| 正式可重复/回滚/数据门禁 | — | **交 migrationReviewer**（本报告不代签） |

## 与 tester / CR 对账

| 来源 | 结论 | 本审查 |
|---|---|---|
| REV-TASK-WSC-602 R2 | APPROVE；P0=0 P1=0 | 采纳为前置；独立复核实现面 |
| TESTRUN-TASK-WSC-602 | PASS；17 backend + 10 vitest | 安全行为与测试证据一致；本角色未重跑命令 |

## 残余风险（交编排 / 603 / Owner）

- FIND-001/002 为会话吊销与 Cookie 生产加固，不构成当前 SNAP 演示态认证绕过。
- 产品写/导入/`create_by` 隔离正式实证 → **TASK-WSC-603** + 必要时再次 `securityReviewer`。
- `schema-migration` 正式通过 → 独立 **migrationReviewer**。

## 决策权声明

- 本实例 `security-reviewer-wsc-602-r1`；未兼任 developer / codeReviewer / tester / migrationReviewer。
- **未修改**被审业务代码；未写 `state.yaml` / `events.jsonl`；**未标 VERIFIED**。
- **decision: APPROVE**（P0/P1 清零；auth-model-change / authn / handles-pii / rbac-breaking 实现态门禁通过）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 0 |
| findingCount | 3 |
