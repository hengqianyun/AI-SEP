# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-102-R1
taskId: TASK-WSC-102
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-102
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T15:36:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-102.md
  - planning/tasks/DEV-TASK-WSC-102.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
  - contracts/rbac/matrix.yaml
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet`：前端 `shell`/`auth`/`layouts` 与后端 `rbac`/`security`（含对称测试）；未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`、catalog/overview/chain 业务包（本任务交付面）。§3.3 会话吊销与 §4 三角色写过滤器对齐 `matrix.yaml`；壳层管理员「目录维护」+ 占位未开放 + 四类写入口可见性齐全。审查复跑后端 12 测与前端 `useCanWrite` 3 测均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-102-R1-001 | P2 | 侧栏/写入口链到 `/catalog/maintenance`，但 `frontend/src/router/routes.ts` 尚无该路由（属 TASK-WSC-106）；管理员点击后无业务页。DEV 已声明边界 | TASK-WSC-106 注册维护页路由并落地业务 | REQ-SHELL-001 |
| FIND-WSC-102-R1-002 | P2 | `StubWriteController` 占位 `/catalog/maintenance/**` 与 `/catalog/products/import/**`；106/107 真实 Controller 落地时须移除或迁出，否则映射冲突。DEV 已记 | 106/107 落地时删除/替换 stub，并由 Orchestrator 协调 security 写集 | REQ-RBAC-001, REQ-CAT-007/008 |
| FIND-WSC-102-R1-003 | P3 | `AuthAuditLogger.forbidden` 审计字段固定 `code=ERR_FORBIDDEN`；维护路径响应体已为 `ERR_MAINTENANCE_FORBIDDEN`（集成测断言通过） | 审计日志传入实际 errorCode，便于对账 | REQ-RBAC-001 |
| FIND-WSC-102-R1-004 | P3 | 壳层「非管理员不可见目录维护」由 `canMaintainCatalog` 单测覆盖；无 `WorkbenchLayout` 组件级 v-if 断言 | 可选：加布局组件测或 E2E 三角色侧栏可见性 | REQ-SHELL-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet` | PASS（`rbac/**`、`security/**`+对称 test；`features/auth/**`、`features/shell/**`、`layouts/**`） |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**` | PASS（相对本任务 DEV 变更清单；工作区另有 101 契约/api 改动不归本轮） |
| 未改 catalog/overview/chain 业务包 | PASS（stub 写路径放在 `security.StubWriteController`，未 penentrate denyModify 包） |
| 未改 `ai/rules/**`、`product/**`、`planning/approved/**` | PASS |

### 2. §3.3 会话鉴权（ISSUE-SEC-R1-001）

| 项 | 证据 | 结果 |
|---|---|---|
| 服务端会话 + Cookie `WSC_SESSION` | `SecurityWebConfig` / `AuthController` | PASS |
| 写 API 以会话角色为准 | `WriteAuthorizationInterceptor` + `RbacMatrix` | PASS |
| 登出失效后续写拒绝 | `logout_thenImportWriteApiRejected` → 401 | PASS（复跑） |
| 角色变更吊销旧会话 | `switchRole` invalidate；`roleSwitch_oldSessionImportRejected` 旧会话 401、新 USER 导入 403 | PASS（复跑） |
| 前端非唯一防护 | UI `useCanWrite` + 服务端拦截器 | PASS |

### 3. §4 / REQ-RBAC-001 / REQ-SHELL-001

| 项 | 证据 | 结果 |
|---|---|---|
| ADMIN：分类+维护+产品写+导入 | 集成测 200；UI 四类可见 | PASS |
| PROVIDER：维护 `ERR_MAINTENANCE_FORBIDDEN`；产品/导入 200 | 集成测；UI 隐藏维护、显示导入 | PASS |
| USER：全写 + 报告 GET 403 | 集成测 | PASS |
| 管理员侧栏「目录维护」 | `WorkbenchLayout` `v-if="catalogMaintenanceVisible"` | PASS |
| 占位菜单未开放 | `navClosed` → `/unavailable/*` +「未开放」徽标 | PASS |
| 三角色写入口含批量导入 | `WriteEntryDemo` + `canImportProduct` | PASS |
| 角色变更后入口立即更新 | `authStore.switchRole` 更新 session；`useCanWrite` computed 依赖 `role` | PASS（实现） |

### 4. 测试覆盖（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| §4 矩阵（过滤器） | PASS — `SessionAuthIntegrationTest` 三角色 |
| §4 壳层可见性 | PASS — `useCanWrite.spec.ts` 3 tests |
| 登出/角色变更后写 API 拒绝 | PASS — 导入路径负例 |
| 审查复跑 | 后端 12 tests PASSED；vitest 3 tests PASSED |

## 残余风险（交 tester）

- 壳层点「目录维护」在 106 前无路由页；验收可见性即可，勿将空白页判为 102 回归失败。
- Stub 维护/导入 API 非业务实现；E2E 勿把 stub 200 当 106/107 业务 VERIFIED。
- 工作区仍有 TASK-WSC-101 契约/api 等未提交改动；tester 取证时注意模块基线为 `data-chain-service`（`repos.yaml`）。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2 |
| P3 | 2 |
