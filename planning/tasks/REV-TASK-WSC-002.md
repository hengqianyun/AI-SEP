# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-002-R1
taskId: TASK-WSC-002
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-002
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
reviewedAt: 2026-07-28T17:45:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-002.md
  - planning/tasks/DEV-TASK-WSC-002.md
  - planning/approved/PLAN-WSC-1.1.md
```

## 结论

**APPROVE** — 本轮无 P0/P1。HttpSession 鉴权、§4.1 三角色矩阵（服务端拦截 + 前端写入口可见性）、登出/角色切换后旧会话拒绝、`ERR_FORBIDDEN`、壳层未开放菜单与企业信息均落实；未误改 `routes.ts` / `frontend/src/api/**` / `pom.xml` / `contracts/**`。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-002-R1-001 | P2 | 壳层/登录（`LoginView`/`WorkbenchLayout`/`RoleSwitcher`）使用原生 HTML，未消费已声明的 `ant-design-vue`（RULE-ORG-STACK） | 后续壳层或统一 UI 任务改用 Ant Design Vue 组件，或书面冻结「002 原生 HTML 可接受」 | REQ-SHELL-001 |
| FIND-WSC-002-R1-002 | P2 | `RoleSwitcher` 捕获切角色失败后仅写入 `authStore.error`，模板无错误展示；与 acceptance「关键导航反馈明确」弱对齐（登录页已有 error） | 在切角色控件旁展示 `auth.error` 或等价提示 | REQ-SHELL-001 |
| FIND-WSC-002-R1-003 | P2 | `AuthAuditLogger` 结构化字段含 username/reason/userId/role/resource/`ERR_FORBIDDEN`/`correlationId`；`timestamp` 依赖 Logback 默认 pattern，消息内无显式 `result=` | 日志消息补 `result=FAILED` 等，或确认 staging appender pattern 含时间戳并写入 tester 清单 | REQ-RBAC-001, §6.1 |
| FIND-WSC-002-R1-004 | P3 | 登录失败与非法角色均返回文案「未登录或会话已失效」，与真实原因（`BAD_CREDENTIALS`/`INVALID_ROLE`）不一致（审计 reason 正确） | 对外 message 区分「凭证错误」等，或保持安全模糊但文档化 | REQ-RBAC-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. allowModify / denyModify 边界

| 项 | 结果 |
|---|---|
| 业务变更 ⊆ allowModify（auth/shell/layouts、`App.vue`、`router/index.ts`、rbac/security + 对应测试；SCOPE_AMEND 含 main/App/index） | PASS |
| 未改 `frontend/src/router/routes.ts` | PASS（git 无变更；index 只读导入并 nest） |
| 未改 `frontend/src/api/**` | PASS（切角色走 `features/auth/api/switchRole.ts`） |
| 未改 `backend/pom.xml`、`contracts/**`、`**/db/migration/**`、overview/catalog/chain feature | PASS |
| `StubWriteController` 置于 `security` 包，避免写入 catalog 业务包 | PASS（合理占位） |
| `ai/runs/RUN-WSC-001/state.yaml` 有改动 | 记为 Orchestrator 状态，**不**计入本任务 developer SCOPE_VIOLATION |
| `planning/tasks/DEV-TASK-WSC-002.md` | 计划允许的证据制品，不阻断 |

### 2. §3.2 会话 / 吊销

| 项 | 证据 | 结果 |
|---|---|---|
| Session Cookie `WSC_SESSION` + HttpOnly | `SecurityWebConfig` | PASS |
| 写 API 以服务端会话角色为准 | `WriteAuthorizationInterceptor` + `RbacMatrix` | PASS |
| 登出 `session.invalidate()` 后写 API 401 | `AuthController.logout`；集成测 `logout_thenWriteApiRejected` | PASS |
| 角色切换失效旧会话、旧凭证拒绝 | `POST /auth/session/role` invalidate+新会话；`roleSwitch_oldSessionRejected` | PASS |
| 禁止仅前端隐藏 | 前端 `useCanWrite` 仅 UI；服务端拦截器强制 403 | PASS |

### 3. §4.1 矩阵 + ERR_FORBIDDEN

| 角色 | 分类 UI | 分类写 API | 产品 UI | 产品写 API | 结果 |
|---|---|---|---|---|---|
| ADMIN | 可见 | 200 | 可见 | 200 | PASS（`useCanWrite` + 集成测） |
| PROVIDER | 不可见 | 403 `ERR_FORBIDDEN` | 可见 | 200 | PASS |
| USER | 不可见 | 403 | 不可见 | 403 | PASS |

403 响应含 `code=ERR_FORBIDDEN` 与 `correlationId`（集成测断言）。

### 4. 壳层 REQ-SHELL-001

| 项 | 证据 | 结果 |
|---|---|---|
| 侧栏「总览」「数据目录」 | `WorkbenchLayout` navOpen | PASS |
| 未开放菜单 → `/unavailable/*`，「本版本未开放」 | navClosed + `routes.ts` meta.unavailable + `PlaceholderView`（001 只读消费） | PASS |
| 企业信息 | `enterpriseName` / displayName | PASS |
| 角色切换后写入口立即更新 | Pinia `session` + `WriteEntryDemo`/`useCanWrite` 响应式 | PASS |
| 登录守卫 | `router/index.ts` beforeEach | PASS |
| 中文界面 | 登录/侧栏/写入口文案 | PASS |

### 5. §6.1 最低审计

| 事件 | 实现 | 结果 |
|---|---|---|
| 登录失败 | `AUTH_LOGIN_FAILED` + username/reason/correlationId | PASS（代码；tester 可抽日志） |
| 写 API 403 | `AUTH_FORBIDDEN` + userId/role/method/resource/`ERR_FORBIDDEN` | PASS |

### 6. 自测证据（审查侧抽样，不代替 tester）

| 项 | 结果 |
|---|---|
| Surefire：`SessionAuthIntegrationTest` 6 / `RbacMatrixTest` 2，failures=0 | 已有报告佐证 DEV 声明 |
| Vitest：`useCanWrite.spec.ts` 3 passed | 审查复跑 PASS |

## 残余风险（交 tester）

- 壳层交互 / Playwright 级「未开放」点击与三角色 UI 可见性仍属 `testScope`，本审查不宣称 E2E 通过。
- 跨端口联调依赖 CORS + credentials；DEV 已说明 `VITE_API_BASE_URL`。
- `StubWriteController` 仅为矩阵占位；005 落地真实写路径后须回归拦截器路径匹配。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称 testScope 通过。
- **decision: APPROVE**（进入独立 tester 门禁）。
