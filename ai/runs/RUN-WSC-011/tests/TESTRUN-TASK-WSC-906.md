# 测试证据 — TASK-WSC-906

```yaml
testrunId: TESTRUN-TASK-WSC-906
taskId: TASK-WSC-906
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: tester-wsc-906
mustDifferFrom:
  - developer-wsc-906
  - code-reviewer-wsc-906
  - security-reviewer-wsc-906
basedOn:
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-906 testScope/acceptance；§4.1）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-906.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-906.md（APPROVE；P0/P1=0）
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-906.md（APPROVE；P0/P1=0）
  - product/requirements/SNAP-WSC-008.md（REQ-SHELL-009、REQ-SHELL-010）
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
securityReviewDecision: APPROVE
securityReviewP0: 0
securityReviewP1: 0
executedAt: 2026-08-17T13:51:30+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-SHELL-009, REQ-SHELL-010]
riskTags: [shell-nav, rbac-visibility, prelanding-reconcile, auth-model-change]
note: |
  独立 tester-wsc-906 实跑 DEV 所列 4 套件（writeSet 内 spec）。未改源码、审查结论、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  无 routeGuards.spec.ts（DEV 仅列 routeGuards.ts 实现）。denyModify 内 V1.4 CatalogBrowsePage / useProductImport 红测已独立实跑观察，不记 906 FAIL。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-906` 依 PLAN-WSC-8.3 §5 TASK-WSC-906 `testScope` 与 DEV 套件清单实际执行：前端 4 文件共 **20** 测 **全部通过**（exitCode=0）。命名用例 `906-nav-my-catalog-submenu`、`906-provider-no-maintenance-menu` 已执行并通过。`useCanWrite` ADMIN `canWriteProduct` / `canImportProduct` **true** 已实跑。

审查前提：codeReview **APPROVE（P0=0、P1=0）**；securityReview **APPROVE（P0=0、P1=0）**。本报告不代标 VERIFIED。

denyModify 内 V1.4 红测（`CatalogBrowsePage.spec.ts` / `useProductImport.spec.ts` 仍期望 ADMIN 不可写）：本 tester **独立实跑**，2 失败 / 30 通过（观察 exitCode=1）。**不**记 906 FAIL、**不**开 `BUG-*`；**也未**把该观察当作 906 门禁 PASS 的替代。CR FIND-WSC-906-001/002 与本轮观察一致，但不替代本实测。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | 结果 | 证据 |
|---|---|---|---|---:|---|---|
| 1 | frontend-unit（writeSet 必跑） | `pnpm --dir frontend exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/layouts/WorkbenchLayout.spec.ts src/features/shell/navSurfaces.spec.ts src/features/shell/deepLinkAccess.spec.ts` | repo root | **0** | **PASSED** — Test Files **4** passed；Tests **20** passed（0 failed） | `vitest-906.txt`；`vitest-906.json` |
| 2 | 独立观察（非门禁） | `pnpm --dir frontend exec vitest run src/features/catalog/browse/CatalogBrowsePage.spec.ts src/features/catalog/import/composables/useProductImport.spec.ts` | repo root | **1** | **观察失败 2 / 通过 30** — 不计入 906 `failedCommandCount` | `vitest-906-denymodify-observe.txt`；`vitest-906-denymodify-observe.json` |

**failedCommandCount = 0**（仅计 #1 门禁命令）。无环境阻塞；未把未执行项记为 PASS。

环境：Node v22.22.0、pnpm 9.15.0、Vitest v2.1.9。命令 #2 失败后 PowerShell 另打印 `ERR_PNPM_RECURSIVE_EXEC_FIRST_FAIL` / `Command "vitest" not found`：JSON 报告已写出且 32 例已执行，**不**记环境阻塞。

未跑：无 `routeGuards.spec.ts`（实现有、spec 无）；未跑 E2E（908 独占）；未把 denyModify 红测当作 906 必跑失败。

## 前端分套件（Vitest 门禁）

| 文件 | tests | failures | 结果 |
|---|---:|---:|---|
| `useCanWrite.spec.ts` | 5 | 0 | PASS |
| `WorkbenchLayout.spec.ts` | 8 | 0 | PASS |
| `navSurfaces.spec.ts` | 4 | 0 | PASS |
| `deepLinkAccess.spec.ts` | 3 | 0 | PASS |
| **合计** | **20** | **0** | **PASS** |

### 命名用例（§0.2）

| DisplayName | 文件 | 结果 |
|---|---|---|
| **906-nav-my-catalog-submenu** | `WorkbenchLayout.spec.ts` | **PASS**（JSON `status=passed`；duration≈2.6ms） |
| **906-provider-no-maintenance-menu** | `WorkbenchLayout.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.8ms） |

源码 `it('906-nav-my-catalog-submenu: ...')` / `it('906-provider-no-maintenance-menu: ...')` 与本轮 JSON `title` 对应。

### 套件明细

| 文件 / 用例 | testScope 映射 | 结果 |
|---|---|---|
| `useCanWrite` ADMIN write/import **true** | ADMIN `canWriteProduct`/`canImportProduct` true | **PASS** |
| `useCanWrite` PROVIDER 无 catalogMaintenance；有 myCatalog/写/导入 | PROVIDER 菜单负例（维护）+ 正例（我的目录） | **PASS** |
| `useCanWrite` USER 全隐藏 | USER 菜单负例（composable） | **PASS** |
| `useCanWrite` computed 三角色镜像 + null fail-closed | §3.1 矩阵单元格 | **PASS** |
| `WorkbenchLayout` §4.1 三角色菜单正例+负例 | ADMIN 双入口；PROVIDER 无维护；USER 仅全链；无企业管理页 | **PASS**（源码扫描 + composable） |
| `WorkbenchLayout` USER 负例深链四路径接线 | routes 含 `beforeEnter: requireDeepLinkAccess` 与四 path | **PASS**（源码扫描，非 Router 挂载） |
| `WorkbenchLayout` 四 surface / meta.title | 侧栏 `isActive` 四路径；title「目录维护（全量）」vs「我的目录（本企业/本人）」 | **PASS** |
| `WorkbenchLayout` `/my-catalog` 统一；`/my-maintenance` 仅 redirect | 路由权威路径 | **PASS** |
| `WorkbenchLayout` aria/sessionStorage/chevron | 可收起组非回归 | **PASS**（源码扫描） |
| `WorkbenchLayout` 企业只读区 | SHELL-010 | **PASS**（源码扫描） |
| `navSurfaces` 四 surface 互斥 + 组头高亮 | 四 surface active | **PASS**（行为断言） |
| `deepLinkAccess` USER 四路径 false | USER 深链结构不可达谓词 | **PASS**（行为断言） |
| `deepLinkAccess` PROVIDER `/catalog/maintenance` false；`/my-catalog` true | PROVIDER 深链维护不可达；可见我的目录 | **PASS**（行为断言） |
| `deepLinkAccess` ADMIN 双入口 + users true | ADMIN 正例可达 | **PASS** |

`WorkbenchLayout.spec.ts` 以 `readFileSync` 扫描 layout/routes/placeholder 为主（仓内 602/603 风格）；深链**谓词行为**由 `deepLinkAccess.spec.ts` 覆盖；四 surface **active 行为**由 `navSurfaces.spec.ts` 覆盖。本轮**未**挂载 Vue Router 做导航集成，不把未跑的 router 集成标为 PASS。

## testScope 对照（PLAN-WSC-8.3 §5 TASK-WSC-906）

| # | testScope / 覆盖要求 | 实跑？ | 结果 | 证据 |
|---|---|---|---|---|
| 1 | §4.1 三角色菜单正例+负例 | **是** | **PASS** | `WorkbenchLayout` §4.1 用例；`useCanWrite` 三角色；`deepLinkAccess` 三角色 |
| 2 | USER：菜单不可见 + 深链结构不可达 — `/catalog/maintenance`、`/my-catalog`、`/my-products`、`/admin/users` | **是** | **PASS** | composable USER 全 false；`deepLinkAccess` 四路径 `toBe(false)`；layout 扫描 `v-if` + `beforeEnter` |
| 3 | PROVIDER：深链 `/catalog/maintenance` 不可达；可见我的目录 | **是** | **PASS** | `906-provider-no-maintenance-menu`；`isDeepLinkAllowed('/catalog/maintenance','PROVIDER')===false`；`/my-catalog` true |
| 4 | 四 surface active；页头/meta.title 区分 | **是** | **PASS** | `navSurfaces.spec.ts` 互斥；layout 扫描四 `isActive` + meta.title「目录维护（全量）」vs「我的目录（本企业/本人）」；占位页 h1 同文。维护页 DOM h1「目录维护」交 907 |
| 5 | 可收起组 aria/sessionStorage/chevron | **是** | **PASS** | layout spec 扫描 `aria-expanded` / `aria-controls` / `wsc.nav.catalogGroupOpen` / chevron `.open` / `v-show` / `catalogGroupActive` |
| 6 | 企业信息区只读渲染 | **是** | **PASS** | layout spec：`sidebar-enterprise` + `data-enterprise-readonly`；无 RoleSwitcher / 企业切换 / 企业管理页 |
| 7 | `useCanWrite` ADMIN `canWriteProduct`/`canImportProduct` true | **是** | **PASS** | `useCanWrite.spec.ts` ADMIN 用例 |
| 8 | §0.2 命名用例 `906-nav-my-catalog-submenu`、`906-provider-no-maintenance-menu` | **是** | **PASS** | JSON `status=passed` |

## §4.1 三角色（906 可完成面）

| 角色 | 正例 / 负例（计划字面） | 本轮 906 | 说明 |
|---|---|---|---|
| ADMIN | 全链 + 目录维护 + 我的目录 + 我的数据产品 + 用户管理 | **PASS（菜单/深链/composable）** | 座序图、myCatalog 结果集本企业 **不在** 906 writeSet（902/907） |
| ADMIN | 负例：无企业管理页 | **PASS** | layout 无「企业管理」 |
| PROVIDER | 全链 + 我的目录 + 我的数据产品 | **PASS（菜单/深链）** | 我的目录「本人 create_by」页业务交 907 |
| PROVIDER | 负例：无目录维护菜单；深链维护不可达 | **PASS** | `906-provider-no-maintenance-menu` + `deepLinkAccess`。`scope=full` API 403 归 905，本轮未跑后端 |
| USER | 仅全链 | **PASS** | composable + 菜单扫描 |
| USER | 负例：我的目录/我的产品/维护/用户管理 菜单隐藏 + 深链不可达 | **PASS** | 四路径谓词 + beforeEnter 接线扫描 |

## acceptance 对照（执行层）

| acceptance 项 | 结果 | 本轮证据 |
|---|---|---|
| IA：我的目录在 `nav-catalog-submenu` 内；序全链 → 我的目录 → 我的数据产品 →（ADMIN）目录维护 | **PASS** | `906-nav-my-catalog-submenu` |
| 路由统一 `/my-catalog`；`/my-maintenance` 仅 redirect | **PASS** | layout spec 读 `routes.ts` |
| 四 surface active；meta.title 可区分 | **PASS** | `navSurfaces` + layout meta/占位 h1。页头「（全量）」DOM 交 907 |
| ADMIN：全链 + 目录维护 + 我的目录 + 我的数据产品 + 用户管理 | **PASS** | useCanWrite + layout + deepLinkAccess |
| PROVIDER：全链 + 我的目录 + 我的数据产品；不可见目录维护；深链维护不可达 | **PASS** | `906-provider-no-maintenance-menu` + deepLinkAccess |
| USER：仅全链；其余菜单隐藏 + 深链结构不可达 | **PASS** | 见覆盖表 #2 |
| `useCanWrite` 与 §3.1 一致（ADMIN 写/导入 true） | **PASS** | `useCanWrite.spec.ts` 5 例 |
| 可收起组 aria / sessionStorage / chevron | **PASS** | layout spec |
| 侧栏企业只读；无切换/编辑；无企业管理页 | **PASS** | layout spec |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-SHELL-009 | 可收起子菜单 IA；ADMIN 双入口；PROVIDER 无维护；USER 深链四路径不可达；`useCanWrite` 对齐 §3.1 | **PASS（906 写集 Vitest）** |
| REQ-SHELL-010 | 侧栏企业名称/标识只读；无切换/编辑 | **PASS** |
| REQ-RBAC-002（UI 门控面） | ADMIN 写/导入 UI true；PROVIDER 无 catalogMaintenanceUI | **PASS（composable）**。API 403 归 905 |
| REQ-CAT-017（myCatalog 本企业结果集） | 不在 906 写集 | **未在本任务断言**；交 907 |

## denyModify V1.4 红测 — 独立实测

计划 / CR：`CatalogBrowsePage.spec.ts` 与 `useProductImport.spec.ts` 仍期望 ADMIN 不可写，属范围外已知债。本 tester **未**采信 DEV/CR 代替实测。

| 观察 | 本轮证据 |
|---|---|
| 是否实跑 | **是**。命令 #2 exitCode=1；JSON `numFailedTests=2`、`numPassedTests=30`、`numTotalTests=32` |
| 失败断言 | `canWriteProduct('ADMIN')` expected **false** received **true**（browse L17）；`canImportProduct('ADMIN')` expected **false** received **true**（import L391） |
| 与 906 acceptance | **冲突的是 V1.4 spec 期望**，不是 906 writeSet 行为。906 必跑 `useCanWrite.spec.ts` 要求 ADMIN **true** 且已 PASS |
| 是否记 906 FAIL | **否**。路径在 denyModify `browse/**` / 非 906 writeSet 的 import spec |
| 是否把该红测未跑当 906 PASS | **否**。906 PASS 仅来自命令 #1 的 20 例 |
| 是否开 `BUG-*` | **否**。与 FIND-WSC-906-001 / FIND-WSC-906-002 同向；闭合须改 denyModify 外任务的断言 |

## 已知限制（不阻塞 PASS）

| 来源 | 说明 |
|---|---|
| FIND-WSC-906-001（P2，OPEN） | browse spec 仍期望 ADMIN 写 false。本轮**已跑**并复现。不升格 906 FAIL |
| FIND-WSC-906-002（P2，OPEN） | import spec 仍期望 ADMIN 导入 false。本轮**已跑**并复现。不升格 906 FAIL |
| FIND-SEC-WSC-906-001（P2，OPEN） | 分类/编辑深链未进 `isDeepLinkAllowed`。不在 §4.1 点名四负例；本轮未跑编辑页 spec |
| FIND-SEC-WSC-906-002（P2，OPEN） | `/my-products` `meta.requiresProvider` 残留字面量。本轮未当缺陷开单 |
| 无 `routeGuards.spec.ts` | `beforeEnter` 由 layout spec 扫描 `routes.ts` 覆盖接线；谓词由 `deepLinkAccess.spec.ts` 覆盖。未跑 Router 导航集成 |
| 维护页 h1 vs meta「（全量）」 | 907 独占 `CatalogMaintenancePage` |

本 tester **不**因开放 P2 另开 `BUG-*`。

## 缺陷

无新开 `BUG-*`。

## 审查前提

| 项 | 值 |
|---|---|
| codeReview | `REV-TASK-WSC-906` **APPROVE**；P0=0、P1=0 |
| securityReview | `SEC-REV-TASK-WSC-906` **APPROVE**；P0=0、P1=0 |
| 开放 P2（不阻塞） | FIND-WSC-906-001/002；FIND-SEC-WSC-906-001/002 |

## 证据红线（自检）

- [x] 实际执行 testScope（命令 + exitCode），非仅读 DEV/REV
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未把 denyModify 内 V1.4 红测自动记为 906 FAIL
- [x] 未把未跑的 Router 导航集成 / E2E / 907 结果集记为 PASS
- [x] 未改源码 / 审查结论 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-906`，与 developer / code-reviewer / security-reviewer 隔离
- [x] 未自行标任务 VERIFIED
- [x] 门禁未失败，故未开 BUG-*
- [x] 无 `routeGuards.spec.ts`，未声称该文件已跑

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-906.md`
- 门禁 verbose 日志：`ai/runs/RUN-WSC-011/tests/vitest-906.txt`
- 门禁 JSON：`ai/runs/RUN-WSC-011/tests/vitest-906.json`
- denyModify 观察日志：`ai/runs/RUN-WSC-011/tests/vitest-906-denymodify-observe.txt`
- denyModify 观察 JSON：`ai/runs/RUN-WSC-011/tests/vitest-906-denymodify-observe.json`
- 任务包：`planning/approved/PLAN-WSC-8.3.md` §5 TASK-WSC-906 / §4.1
- 开发 / 审查：`DEV-TASK-WSC-906.md`、`reviews/REV-TASK-WSC-906.md`、`reviews/SEC-REV-TASK-WSC-906.md`
