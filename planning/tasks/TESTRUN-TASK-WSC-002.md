# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-002-1
taskId: TASK-WSC-002
planId: PLAN-WSC-1.1
actorInstance: tester-wsc-002
contracts: wsc-contracts@1.1.0
basedOn:
  - planning/tasks/TASK-WSC-002.md
  - planning/tasks/DEV-TASK-WSC-002.md
  - planning/tasks/REV-TASK-WSC-002.md
  - contracts/rbac/matrix.yaml
reviewDecision: APPROVE
executedAt: 2026-07-28T17:52:00+08:00
status: PASSED
```

## 总体结论

**PASSED** — `REV-TASK-WSC-002` 已为 **APPROVE**；独立复跑后端 8 例与前端 3 例均通过；静态核对侧栏未开放提示与三角色矩阵与 `contracts/rbac/matrix.yaml` 一致。未兼任 developer / codeReviewer。

## Layers

| name | command | result | notes |
|---|---|---|---|
| review-gate | 读 `planning/tasks/REV-TASK-WSC-002.md` | PASSED | `decision: APPROVE`（P0=0/P1=0）；初检时文件尚未就绪，等待后复核再落盘 |
| backend-rbac | `mvn -f backend/pom.xml "-Dtest=SessionAuthIntegrationTest,RbacMatrixTest" test` | PASSED | Tests run: **8**, Failures: 0, Errors: 0；BUILD SUCCESS；exit 0 |
| frontend-canWrite | `pnpm --filter frontend exec vitest run src/features/auth/composables/useCanWrite.spec.ts` | PASSED | Test Files 1 passed；Tests **3** passed；exit 0 |
| static-shell-copy | 读 `WorkbenchLayout.vue` + `PlaceholderView.vue` + `routes.ts` | PASSED | 见下 |
| static-rbac-matrix | 对照 `matrix.yaml` ↔ `RbacMatrix` / `useCanWrite` / 集成测 | PASSED | 见下 |

## 后端明细

| 类 | Tests | Failures | Errors | 结果 |
|---|---|---|---|---|
| `com.wsc.rbac.RbacMatrixTest` | 2 | 0 | 0 | PASSED |
| `com.wsc.security.SessionAuthIntegrationTest` | 6 | 0 | 0 | PASSED |

覆盖抽样（Surefire + 控制台）：§4.1 三角色写 API；403/`ERR_FORBIDDEN`/`correlationId`；登出后拒绝；角色切换后旧会话拒绝；审计 `AUTH_LOGIN_FAILED` / `AUTH_FORBIDDEN`。

## 前端明细

| 规格 | Tests | 结果 |
|---|---|---|
| `src/features/auth/composables/useCanWrite.spec.ts` | 3 | PASSED |

ADMIN 可见分类+产品；PROVIDER 隐藏分类、可见产品；USER 全隐藏。

## 静态核对

### 1. 侧栏 / 「本版本未开放」

| 项 | 证据 | 结果 |
|---|---|---|
| 侧栏含「总览」「数据目录」 | `WorkbenchLayout.vue` `navOpen` | PASSED |
| 未开放菜单：数据登记 / 交易订单 / 连接器管理 | `navClosed` → `/unavailable/:feature` | PASSED |
| 侧栏徽标文案 | badge 为「未开放」（缩写） | PASSED（见注） |
| 点击后完整提示「本版本未开放」 | `routes.ts` `meta.unavailable: true` + `PlaceholderView.vue` | PASSED |

注：acceptance / OQ-001 要求壳层**点击提示**「本版本未开放」——完整文案在占位页；侧栏徽标为「未开放」。不视为阻塞。

### 2. 三角色矩阵 ↔ `contracts/rbac/matrix.yaml`

| 角色 | category UI | category API | product UI | product API | 代码/测 | 结果 |
|---|---|---|---|---|---|---|
| ADMIN | visible | 200 | visible | 200 | `useCanWrite` + `RbacMatrix` + 集成测 | PASSED |
| PROVIDER | hidden | 403 `ERR_FORBIDDEN` | visible | 200 | 同上 | PASSED |
| USER | hidden | 403 | hidden | 403 | 同上 | PASSED |

## 环境快照

| 项 | 值 |
|---|---|
| Java | 17.0.11 |
| Maven | 3.6.3（JDK 17） |
| Node / Vitest | 与仓库 frontend 锁定一致；Vitest v2.1.9 |
| cwd | `C:\WorkSpace\AI-SEP` |

## 残差（不阻塞 PASSED）

- REV 残余 P2/P3（原生 HTML、切角色错误展示、审计 timestamp 字段形态、登录失败文案模糊）不升格为本轮失败。
- 无 Playwright/E2E 壳层点击；静态+单测覆盖本任务 `testScope` 必跑项。
- `StubWriteController` 占位写路径；005 真实写落地后须回归。

## 决策权声明

- tester 未修改业务源码；仅产出本证据文件。
- `BLOCKED ≠ PASSED`：本轮审查已 APPROVE 且命令全绿，故 **PASSED**（非 BLOCKED）。
- 未把未执行的 E2E 记为 PASSED。
