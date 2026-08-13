# 测试证据

```yaml
testrunId: TESTRUN-TASK-WSC-702
evidenceId: TESTRUN-TASK-WSC-702
taskId: TASK-WSC-702
planId: PLAN-WSC-6.2
snapshotId: SNAP-WSC-006
actorInstance: tester-wsc-702-r1
retest: false
decision: PASS
command: pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/layouts/WorkbenchLayout.spec.ts
exitCode: 0
contracts: wsc-contracts@2.2.0（只读；本任务无契约变更）
basedOn:
  - ai/agents/tester.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-702 acceptance + testScope）
  - planning/tasks/REV-TASK-WSC-702.md（round 1 APPROVE；P0=0 P1=0）
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-702.md
reviewRound: 1
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-12T10:31:13+08:00
cwd: frontend
result: PASS
failedCommandCount: 0
bugsClosed: []
bugsOpen: []
mustDifferFrom:
  - developer（Wave A TASK-WSC-702）
  - code-reviewer-wsc-702-r1
```

## 结论摘要

**PASS** — 独立 `tester-wsc-702-r1` 在 REV Round 1 **APPROVE**（P0=0、P1=0）后真实执行 TASK-WSC-702 `testScope`：

1. 必需 Vitest（`useCanWrite.spec.ts` + `WorkbenchLayout.spec.ts`）→ **exitCode=0**；**2 files / 9 tests PASSED**。
2. 可选回归 `UsersAdminPage.spec.ts` → **exitCode=0**；**8 passed**（用户管理不回退旁证）。
3. acceptance / §4.1 勾选全部 **PASS**（见下表）。

- developer / codeReviewer 复跑不算本门禁；本轮独立重跑
- 未将环境阻塞记为 PASS；未伪造 exitCode
- 未修改生产业务实现源码；仅写本 TESTRUN 证据制品
- 未自行宣称 VERIFIED / 未改 `state.yaml` / `events.jsonl`

## commands

| # | command | cwd | exitCode | result | notes |
|---|---|---|---|---|---|
| 1 | `pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/layouts/WorkbenchLayout.spec.ts` | `frontend` | **0** | **PASS** | 2 files / 9 tests；Duration ~1.39s |
| 2 | `pnpm exec vitest run src/features/users/UsersAdminPage.spec.ts` | `frontend` | **0** | **PASS** | 可选回归；8 tests（用户管理保留旁证） |
| 3 | acceptance 勾选对账 | n/a | n/a | **PASS** | 对照计划 acceptance + 源码/断言 |

**failedCommandCount = 0**（必需命令 #1 exit 0）。

### 命令 #1 摘要

```
✓ src/features/auth/composables/useCanWrite.spec.ts (4 tests)
✓ src/layouts/WorkbenchLayout.spec.ts (5 tests)
Test Files  2 passed (2)
Tests  9 passed (9)
```

## TASK-WSC-702 acceptance 勾选

| # | 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | `canMaintainCatalog` 仅 ADMIN（PROVIDER/USER/null/undefined=false） | **PASS** | `useCanWrite.spec.ts` 三角色用例 + `canMaintainCatalog is ADMIN-only`；实现 `role === 'ADMIN'` |
| 2 | 一级「数据目录」子菜单：全链数据目录 / 目录维护 / 我的数据产品 | **PASS** | `WorkbenchLayout.spec` nest 用例；`nav-catalog-group` + 三子项文案/testid |
| 3 | §4.1 角色可见性：ADMIN 全链+维护+用户管理、无我的产品；PROVIDER 全链+我的产品、无维护；USER 仅全链 | **PASS** | Layout `v-if` + composable 断言；spec「§4.1 nav visibility」 |
| 4 | 非 ADMIN 深链 `/catalog/maintenance` 结构不可达（非仅菜单隐藏） | **PASS** | Layout `watch([route.path, role], …, { immediate: true })` → `router.replace('/catalog')`；spec 深链用例正则断言门控存在 |
| 5 | active 互斥：browse / maintenance / mine 不同时高亮 | **PASS** | `isActive` + `onMineSurface` / `onMaintenanceSurface`；spec 六组路径用例 |
| 6 | 用户管理菜单与角色只读展示不回退 | **PASS** | `nav-users` / `userManageVisible` / `session-role-label` / routes `/admin/users`；可选 UsersAdmin 8/0 |
| 7 | 路由保持 `/catalog`、`/catalog/maintenance`、`/my-products` | **PASS** | `routes.ts` 路径断言于 Layout spec |

## testScope 覆盖

| testScope 项 | 覆盖 | 结果 |
|---|---|---|
| Vitest `useCanWrite` 三角色 `canMaintainCatalog`（含 PROVIDER=false） | `useCanWrite.spec.ts` 4 tests | **PASS** |
| §4.1 子菜单可见性正例+负例 | `WorkbenchLayout.spec.ts` visibility 用例 | **PASS** |
| active 互斥（catalog / maintenance / mine） | 镜像 `resolveActiveNav` + Layout `isActive` 符号断言 | **PASS** |
| 深链负例：非 ADMIN 打开维护仍拒绝 | 壳层 watch 源码门控断言（CR FIND-001：未 mount+router 真导航；本门禁以可执行 Vitest+字面门控为准，不升格 FAIL） | **PASS** |

## 残余说明（不阻塞）

- REV FIND-001（P2）：深链/可见性部分为源码字符串/镜像断言，非挂载 Router 组件测；与计划 testScope「Vitest 或组件测」相容，且必需 Vitest 全绿，故 **PASS**。
- API 403 / 后端 `RbacMatrix` ADMIN-only 属 703/704，不在本任务 scope。

## BUG

无（`bugsOpen: []`）。

## 证据红线

- 未将未执行项记为 PASS
- 未伪造 vitest exitCode；命令如实记录
- 未修改 frontend/backend/contracts 业务实现源码
- 未修改 REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-702-r1`（≠ developer / ≠ `code-reviewer-wsc-702-r1`）

## 决策

**decision: PASS**
