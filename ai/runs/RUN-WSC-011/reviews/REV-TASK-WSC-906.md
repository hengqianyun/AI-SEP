# 代码审查报告 — TASK-WSC-906

```yaml
reviewId: REV-TASK-WSC-906
taskId: TASK-WSC-906
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-906
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T13:45:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-8.3.md（§0.2/§0.3、§3.1 矩阵、§4.1、§5 TASK-WSC-906 writeSet/denyModify/acceptance/testScope；TASK-WSC-907 writeSet/acceptance）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-906.md
  - product/requirements/SNAP-WSC-008.md（REQ-SHELL-009、REQ-SHELL-010）
  - contracts/rbac/matrix.yaml（只读对照 useCanWrite）
  - 实地 diff：useCanWrite(+spec)、WorkbenchLayout.vue(+spec)、routes.ts、frontend/src/features/shell/**
mustDifferFrom: developer-wsc-906
riskTags: [shell-nav, rbac-visibility, prelanding-reconcile, auth-model-change]
note: |
  命中 auth-model-change：本 REV 仅 codeReview 门禁；**不得**代 securityReviewer 批准。
  未代 tester 宣称 VERIFIED；未改源码 / state.yaml / events.jsonl。
```

## 结论

**APPROVE** — **P0=0，P1=0**。实现满足 PLAN-WSC-8.3 §5 TASK-WSC-906 在 **writeSet 可完成面** 的验收：「我的目录」已移入 `nav-catalog-submenu`，序为全链 → 我的目录 → 我的数据产品 →（ADMIN）目录维护；权威路由 **`/my-catalog`**，`/my-maintenance` 仅 redirect；`/admin/users` 保留；ADMIN 双入口；PROVIDER 无目录维护菜单且深链 `/catalog/maintenance` 结构不可达；USER 仅全链 + 深链负例；`useCanWrite` 与 §3.1 / matrix.yaml UI 单元格一致（含 ADMIN `canWriteProduct`/`canImportProduct` **true**）；四 surface active 与 meta.title 可区分；可收起组 aria/sessionStorage/chevron 保留；侧栏企业只读（REQ-SHELL-010）。`/my-catalog` 为壳层占位，**未**越权实现 907 的 `CatalogMaintenancePage` 业务。

DEV 所称 denyModify 内 V1.4「ADMIN 不可写」断言会红：独立判定为 **范围外已知债**，**不是** 本任务 P1（证据见专节）。开放 **P2×2** 不阻塞进入独立 tester / securityReviewer 门禁。

## Scope 检查（denyModify / writeSet）

| 项 | 结果 | 证据 |
|---|---|---|
| `useCanWrite.ts` / `useCanWrite.spec.ts` | **PASS** | 字面 writeSet；906 **独占全文**；ADMIN 写/导入 true；PROVIDER `canMaintainCatalog` false |
| `WorkbenchLayout.vue` / `WorkbenchLayout.spec.ts` | **PASS** | 字面 writeSet；IA 序、深链 watch、`document.title`、企业只读区；spec **新建** |
| `frontend/src/router/routes.ts` | **PASS** | `/my-catalog` meta/title/`beforeEnter`；`/my-maintenance` **仅** `redirect: '/my-catalog'`；**未删** `/admin/users` |
| **`frontend/src/features/shell/**`** | **PASS（在 writeSet）** | 计划 writeSet 字面含 `frontend/src/features/shell/**`；新增 `navSurfaces.ts`(+spec)、`deepLinkAccess.ts`(+spec)、`routeGuards.ts`、`MyCatalogPlaceholderPage.vue` 均落在该 glob |
| 未改 `CatalogMaintenancePage` / `useCatalogMaintenance` / `maintenance/components/**` | **PASS** | git status 无 M；`/my-catalog` 挂 shell 占位，非维护页业务 |
| 未改 `frontend/src/features/catalog/browse/**` | **PASS（906 交付）** | DEV filesChanged 未列；工作树脏文件归属 TASK-WSC-901/902 |
| 未改 `contracts/**`、`frontend/src/api/**` | **PASS（906 交付）** | DEV 未列；脏树归属 TASK-WSC-903 |
| 未改 `backend/**` | **PASS（906 交付）** | 脏树归属 902/904/905 |
| 未改 `tests/e2e/**`、`product/**`、`planning/**` | **PASS（906 交付）** | 908 / 计划制品 |
| 未改 `frontend/src/styles/**`、`frontend/src/theme/**` | **PASS** | git status 无 M；layout 仅 scoped `.enterprise-id` |
| 未改 `state.yaml` / `events.jsonl` | **PASS** | 本实例只写本 REV；DEV 声明未改控制面 |
| 未改 `router/index.ts` | **PASS** | 不在 writeSet；guard 抽到 `features/shell/routeGuards.ts` 由 routes `beforeEnter` 引用 |

工作树并行脏文件（901/902/903/904/905）不计入 906 越界。

## V1.4「ADMIN 不可写」红测 — 独立判定

DEV 交：`CatalogBrowsePage.spec.ts` TASK-WSC-603「product write/import UI only PROVIDER」与 `useProductImport.spec.ts`「仅 PROVIDER 可挂载弹层」在 `canWriteProduct('ADMIN')` / `canImportProduct('ADMIN')` **expected false was true**。审查对照计划后的独立结论：

**范围外已知债，不是本任务 P1 回归缺口。**

| 计划条款 | 含义 |
|---|---|
| 906 acceptance | `useCanWrite` 与 §3.1 一致；ADMIN `canWriteProduct`/`canImportProduct` **必须 true** |
| 906 denyModify | **禁止**改 `frontend/src/features/catalog/browse/**` |
| 906 writeSet | **不含** `useProductImport.spec.ts` / import composable |
| 907 writeSet | 具名 industry 回归 spec（`ImportFieldMapping.spec.ts` 等），**非**本 V1.4 权限断言 |
| SNAP-008 / matrix.yaml | ADMIN `productWriteUI` / `productImportUI` = **visible** |

实地代码：

- `useCanWrite.ts` L20–27：ADMIN+PROVIDER → true，与 §3.1 / `contracts/rbac/matrix.yaml` 2.3.3 一致。
- `CatalogBrowsePage.spec.ts` L16–22（denyModify）：仍断言 ADMIN **false**（V1.4/TASK-WSC-603）。
- `useProductImport.spec.ts` L389–396：仍断言 ADMIN **false**（标注 V1.4）。

906 若为让全量 Vitest 变绿而改 browse/import spec，将违反 denyModify / writeSet，构成 **范围越界**。906 若把 ADMIN 写改回 false，将违反 §3.1 与本任务 acceptance，构成 **P0 需求回退**。

正确闭合：后续可写 browse/import spec 的任务（或独立债项）把断言改为 ADMIN **true**。本 REV 记 P2，**不**因此 REQUEST_CHANGES。

## 907 交接（非本任务缺口）

| 项 | 906 | 907 |
|---|---|---|
| `/my-catalog` 路由/meta/ADMIN 双入口 | **已建立**；不得删除 | 可扩页级 guard；接入 `CatalogMaintenancePage` + `scope=myCatalog` |
| `/my-catalog` 页业务 | `MyCatalogPlaceholderPage.vue`（h1=meta.title） | **独占** 维护 UX |
| `/catalog/maintenance` 页头 h1 | **未改**页面（仍为「目录维护」）；**meta.title** / `document.title` =「目录维护（全量）」 | 可选将页头与 meta 对齐 |
| ADMIN 产品写 UI 断言 | composable 已 true | 不得在 906 改 browse/import spec |

`CatalogMaintenancePage.vue` L78 `<h1>目录维护</h1>` 与 meta「目录维护（全量）」不完全同文：页头改动落在 907 独占文件。四 surface **可区分**已由 meta.title + 占位页 h1 + 侧栏 `isNavActive` 满足 906 acceptance；不升格为本任务 P1。

## Acceptance 对照

| acceptance 项 | 结果 | 证据 |
|---|---|---|
| IA：我的目录在 `nav-catalog-submenu` **内**；序全链 → 我的目录 → 我的数据产品 →（ADMIN）目录维护 | **PASS** | `WorkbenchLayout.vue` L203–248；HEAD 组外 `/my-maintenance` 已移入组内；`906-nav-my-catalog-submenu` |
| 路由统一 `/my-catalog`；void 前 `/my-maintenance` 仅 redirect | **PASS** | `routes.ts` L32–41；无 `name: 'my-maintenance'` |
| 不得删除 `/admin/users` | **PASS** | `routes.ts` L43–48 + layout `nav-users` |
| 四 surface active；meta.title「目录维护（全量）」vs「我的目录（本企业/本人）」 | **PASS** | `navSurfaces.ts` 维护/我的目录优先于 `/catalog` 前缀；`document.title` watch；占位页 h1；页头「（全量）」交 907 |
| ADMIN：全链 + 目录维护 + 我的目录 + 我的数据产品 + 用户管理 | **PASS** | `canMaintainCatalog`/`canSeeMyCatalog`/`canSeeMyProducts` ADMIN true；`userManageVisible` 仍 `role === 'ADMIN'`（602 字面量，供既有 spec 扫描） |
| PROVIDER：全链 + 我的目录 + 我的数据产品；不可见目录维护；深链维护不可达 | **PASS** | `canMaintainCatalog('PROVIDER')===false`；`v-if="catalogMaintenanceVisible"`；`isDeepLinkAllowed('/catalog/maintenance')===false`；`beforeEnter` + layout watch |
| USER：仅全链；其余菜单隐藏 + 深链结构不可达 | **PASS** | useCanWrite USER 全 false；`deepLinkAccess.spec.ts` 覆盖 `/catalog/maintenance` `/my-catalog` `/my-products` `/admin/users` |
| `useCanWrite` 与 §3.1 一致（ADMIN 写/导入 true） | **PASS** | 见下表 |
| 可收起组 aria / sessionStorage / chevron | **PASS** | `:aria-expanded`、`aria-controls="nav-catalog-submenu"`、`wsc.nav.catalogGroupOpen`、chevron `.open`、`v-show`、收起时 `catalogGroupActive` 仍高亮组头 |
| 侧栏企业名称/标识只读；无切换/编辑 | **PASS** | `sidebar-enterprise` + `data-enterprise-readonly`；`enterpriseName` + `session.enterpriseId`；无 RoleSwitcher / 企业切换 / 企业管理页 |

## `useCanWrite` 与 §3.1 / matrix.yaml 逐 cell（UI）

| 能力键 | ADMIN | PROVIDER | USER | 代码 |
|---|---|---|---|---|
| `catalogMaintenanceUI` | visible | **hidden** | hidden | `canMaintainCatalog` 仅 ADMIN |
| `myCatalogUI` | visible | visible | hidden | `canSeeMyCatalog`；`canSeeMyMaintenance` 同义别名 |
| `myProductsUI` | visible | visible | hidden | `canSeeMyProducts` |
| `productWriteUI` | **visible** | visible | hidden | `canWriteProduct` ADMIN\|\|PROVIDER |
| `productImportUI` | **visible** | visible | hidden | `canImportProduct` ADMIN\|\|PROVIDER |
| `categoryMaintainUI` | visible | hidden | hidden | `canMaintainCategory` 仅 ADMIN（未改语义） |
| `userManageUI` | visible | hidden | hidden | `canManageUsers` 仅 ADMIN |

API 单元格（`*Api` / HTTP 200/403）归 905/907，本任务只对齐 UI 门控。隐藏 ≠ 授权：深链另有 `isDeepLinkAllowed` + `requireDeepLinkAccess`；服务端仍由 905 interceptor 把关。

## §4.1 三角色

| 角色 | 正例 | 负例 | 代码 |
|---|---|---|---|
| ADMIN | 全链 + 目录维护全量 + 我的目录 + 我的数据产品 + 用户管理 | 无企业管理页 | 双 `RouterLink` + `/admin/users`；layout 无「企业管理」 |
| PROVIDER | 全链 + 我的目录 + 我的数据产品 | **无**目录维护菜单；深链 `/catalog/maintenance` 不可达 | `catalogMaintenanceVisible` false；`isDeepLinkAllowed` false → replace `/catalog` |
| USER | 仅全链 | 我的目录/我的产品/维护/用户管理 **菜单隐藏 + 深链不可达** | 四路径 `beforeEnter` + watch |

座序图 / myCatalog 结果集本企业 vs 全平台：**非** 906 writeSet（902/907）。

## 测试覆盖（testScope）

| 项 | 结果 |
|---|---|
| §4.1 三角色菜单正例+负例 | **有** `WorkbenchLayout.spec.ts` + `useCanWrite.spec.ts` + `deepLinkAccess.spec.ts` |
| USER 负例：菜单 + 深链 `/catalog/maintenance` `/my-catalog` `/my-products` `/admin/users` | **有**（谓词 + `beforeEnter: requireDeepLinkAccess` 源码断言） |
| PROVIDER 深链 `/catalog/maintenance` 不可达 | **有** `deepLinkAccess.spec.ts` |
| 四 surface active；页头/meta.title 区分 | **有** `navSurfaces.spec.ts` + layout spec 读 routes/placeholder |
| 可收起组非回归（aria/sessionStorage/chevron） | **有** layout spec 源码扫描 |
| 企业信息区渲染 | **有** layout spec（testid / readonly / 无切换） |
| §0.2 命名用例 `906-nav-my-catalog-submenu`、`906-provider-no-maintenance-menu` | **有** `WorkbenchLayout.spec.ts` `it(...)` 标题 |

**未**代替 tester 宣称 PASS/VERIFIED。本实例未重跑 Vitest。layout spec 以源码扫描为主（与仓内 602/603 风格一致）；谓词行为由 `deepLinkAccess.spec.ts` / `navSurfaces.spec.ts` 覆盖。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-906-001 | P2 | OPEN | `frontend/src/features/catalog/browse/CatalogBrowsePage.spec.ts` L16–22 仍期望 `canWriteProduct('ADMIN')===false` / `canImportProduct('ADMIN')===false`（TASK-WSC-603 V1.4）。路径在 906 **denyModify** `browse/**` | 将该用例改为 ADMIN 写/导入 **true**（或迁入可写任务的具名 spec），避免全量 Vitest 红 | REQ-SHELL-009、REQ-RBAC-002 |
| FIND-WSC-906-002 | P2 | OPEN | `frontend/src/features/catalog/import/composables/useProductImport.spec.ts` L389–396「仅 PROVIDER 可挂载弹层」仍期望 `canImportProduct('ADMIN')===false`。不在 906 writeSet | 将 ADMIN 期望改为 **true**，与 overlay `v-if` 及 §3.1 `productImportUI` 对齐 | REQ-RBAC-002 |

## 架构与可维护性备注（非阻塞）

- `navSurfaces` / `deepLinkAccess` / `routeGuards` 从 layout 抽出，避免 `router/index.ts` 越权；`nestUnderLayout` 子路由仍执行 `beforeEnter`，`to.path` 为绝对路径，与 `pathIs` 一致。
- `canSeeMyMaintenance` 保留为 `canSeeMyCatalog` 别名，降低预落地残留 import 断裂风险。
- `onMineSurface` 计算属性现仅满足既有 browse spec 源码扫描（`CatalogBrowsePage.spec.ts` 要求 layout 含该标识），模板改走 `isNavActive`；属遗留，不升格。
- `/my-products` `meta.requiresProvider: true` 为 V1.4 残留字面量；**无**消费方（`router/index.ts` 不读该键）。实际门控已是 ADMIN+PROVIDER 的 `requireDeepLinkAccess`。勿当作「仅 PROVIDER」权威。
- `CatalogMaintenancePage` `onMounted` 仍用 `catalogMaintenanceVisible`（仅 ADMIN）拦 `/catalog/maintenance`：与 906 PROVIDER 深链拒绝一致；907 复用该页接 `/my-catalog` 时须改用 myCatalog 可见性，避免误伤 PROVIDER。

## 决策依据

- P0=0、P1=0 → **APPROVE**，可进入独立 **tester** 与 **securityReviewer**（`auth-model-change`）门禁。
- 本 REV **不**代安全批准；**不**宣称测试门禁通过。
- V1.4 ADMIN 不可写断言、maintenance 页头「（全量）」文案 **不得**在 906 返工改 denyModify / 907 独占文件。
