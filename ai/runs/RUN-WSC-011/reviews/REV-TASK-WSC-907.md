# 代码审查报告 — TASK-WSC-907

```yaml
reviewId: REV-TASK-WSC-907
taskId: TASK-WSC-907
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-907
decision: REQUEST_CHANGES
p0Count: 0
p1Count: 1
p2Count: 1
reviewedAt: 2026-08-17T14:25:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-8.3.md（§0.3、§3.1/§3.2、§5 TASK-WSC-907 writeSet/denyModify/acceptance/testScope）
  - product/requirements/SNAP-WSC-008.md（REQ-CAT-017、REQ-CAT-013、REQ-CAT-019）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-907.md
  - 上游交接：DEV-TASK-WSC-905.md（ADMIN myCatalog 结果集）、DEV-TASK-WSC-906.md（双入口 meta / 占位页）
  - 实地 diff：CatalogMaintenancePage、useCatalogMaintenance(+spec)、maintenance/components/**、routes.ts、CatalogMaintenanceController/Service、MyCatalogScopeSplitIntegrationTest、三份 industry 具名 spec
mustDifferFrom: developer-wsc-907
riskTags: [maintenance-reuse, scope-isolation, enum-regression]
note: |
  本任务无 auth-model-change / schema-migration 触发标签；本 REV 不代 securityReviewer / migrationReviewer。
  未代 tester 宣称 VERIFIED；未改源码 / state.yaml / events.jsonl。
```

## 结论

**REQUEST_CHANGES** — **P0=0，P1=1，P2=1**。后端 ADMIN `scope=myCatalog` 本企业过滤、PROVIDER 本人 `create_by`、N vs M 具名集成测试、906 双入口 meta.title / redirect / `/admin/users`、REQ-CAT-013 UX 控件、REQ-CAT-019 仅具名 spec、未改 interceptor / browse / api 生成物，均对照计划通过。

阻塞项：`/my-catalog` 与 `/catalog/maintenance` **共用** `CatalogMaintenancePage`，layout `RouterView` **无 key**；composable 把 `scope` 当 setup 闭包常量。Vue Router 对「同一组件挂两条路由」会复用实例，页头会随 props 变成「我的目录（本企业/本人）」，list/PUT/POST 仍可能打上一次的 `scope=full`（或相反）。这直接违反 REQ-CAT-017「两入口路由/范围独立」与 acceptance「API scope 与 UI 一致」。

P0/P1 未清零，不得进入独立 tester 门禁。

## Scope 检查（denyModify / writeSet）

| 项 | 结果 | 证据 |
|---|---|---|
| `CatalogMaintenancePage.vue` / `useCatalogMaintenance.ts`(+spec) | **PASS（字面 writeSet）** | 参数化 `scope`；页级 `canEnterMaintenancePage`；Cascader / 本页全选 / 清选择 |
| `frontend/.../maintenance/components/**` | **PASS** | 新建 Cascader + UX spec；均落在 glob |
| `frontend/src/router/routes.ts` | **PASS（906 基础上扩页）** | `/my-catalog` 接维护页 `scope=myCatalog`；全量入口 `scope=full`；**未删改** 906 meta.title |
| industry 具名 spec（editor/import/detail） | **PASS** | **仅**新增 spec；未改 editor/import/detail **实现** |
| maintenance Controller / Service | **PASS** | 907 独占 BE 结果集；传 `SessionPrincipal` + query `scope` |
| `MyCatalogScopeSplitIntegrationTest` | **PASS** | writeSet `src/test/java/**/catalog/maintenance/**` |
| 未改 `WorkbenchLayout.vue` / `useCanWrite.ts` | **PASS（907 交付）** | 脏树归属 TASK-WSC-906；本任务 filesChanged 未列 |
| 未改 `frontend/src/features/catalog/browse/**` | **PASS** | 脏树归属 901/902；浏览页无 `industryCategory` 筛 |
| 未改 `WriteAuthorizationInterceptor.java` | **PASS** | 脏树归属 905；907 只读消费 |
| 未改 `contracts/**`、`frontend/src/api/**` | **PASS** | 脏树归属 903；PUT/POST 用 `apiRequest` 附 `?scope=`（见独立判定） |
| 未改 `L2Distribution*`、`CatalogBrowseController` / `CatalogBrowseService` | **PASS（907 交付）** | DEV 未列；脏树归属 902/905 |
| 未改 `tests/e2e/**`、`product/**`、`planning/**`、`**/sql/**` | **PASS（907 交付）** | 908 / 计划制品 / 904 V7 |
| 未改 `state.yaml` / `events.jsonl` | **PASS** | 本实例只写本 REV；DEV 声明未改控制面 |
| 未删 `MyCatalogPlaceholderPage.vue` | **PASS** | 文件仍在；906 `WorkbenchLayout.spec.ts` 仍读该文件标题 |

工作树并行脏文件（901–906）不计入 907 越界。

## PUT/POST 附 `?scope=` 而未改生成 api — 独立判定

生成 client：`listMaintenanceEntries` **已有** `scope`（903）；`updateMaintenanceEntry` / `batchUpdateMaintenanceEntries` **无** query `scope`。OpenAPI PUT/POST 描述写「scope 规则同 list」，但 **未** 声明 query 参数；907 **denyModify** `frontend/src/api/**`。

**范围内正确 workaround，不是范围越界，也不是本任务 P1。**

实地：`useCatalogMaintenance.ts` `saveEdit` / `saveBatch` 走 `apiRequest('.../entries/...?scope=' + scope)`；列表仍用生成 `listMaintenanceEntries({ scope })`。拦截器按 query `scope` 分流，PROVIDER 缺省/full → 403，必须带 `myCatalog`。记 P2 残差（生成函数仍无 scope），**不**要求 907 改 api。

## 906 双入口交接

| 项 | 结果 | 证据 |
|---|---|---|
| meta.title「目录维护（全量）」vs「我的目录（本企业/本人）」 | **PASS（字面未删改）** | `routes.ts` L42 / L81 |
| `/my-maintenance` 仅 redirect | **PASS** | `routes.ts` L33–35；无 `name: 'my-maintenance'` |
| `/admin/users` 保留 | **PASS** | `routes.ts` L45–50 |
| `/my-catalog` 从占位接到维护页 | **PASS（接入）** | `component` → `CatalogMaintenancePage`；`props: { scope: 'myCatalog' }`；`beforeEnter: requireDeepLinkAccess` 保留 |
| 页头与 meta 对齐 | **PASS** | `CatalogMaintenancePage.vue` `pageTitle` 与 meta 同文 |
| 占位页文件 | **PASS** | 未删；906 spec 仍绿所需 |

## 905 交接（ADMIN myCatalog 结果集）

| 项 | 结果 | 证据 |
|---|---|---|
| ADMIN `scope=myCatalog` 本企业（create_by→enterprise_id） | **PASS（BE）** | `sourceProducts`：`enterpriseScope.matchesMineEnterprise`；同企 `admin2`/`provider` 产品计入 M |
| ADMIN `scope=full` 全平台 | **PASS** | 非 myCatalog 走 `catalog.products()`；跨企 `foreignCode` 仅 full |
| PROVIDER 本人 create_by | **PASS** | `productsOwnedBy(actor.userId())`；907 测试断言不含 `adminCode`/跨企 |
| 未改 interceptor | **PASS** | PROVIDER `full` 仍 403 `ERR_MAINTENANCE_FORBIDDEN`（拦截器） |
| 忽略客户端 `enterpriseId` | **PASS** | Controller 无该形参；测试 `enterpriseId=999` 仍不含跨企 |
| 写路径同样校验 | **PASS** | `requireWritable`：ADMIN myCatalog 跨企 PUT 403；full 200 |

## Acceptance / N vs M / REQ

| acceptance 项 | 结果 | 证据 |
|---|---|---|
| ADMIN `/catalog/maintenance` `scope=full` 全平台 | **PASS（BE；FE 首屏）** | `MyCatalogScopeSplitIntegrationTest` N 含跨企；路由 `props: { scope: 'full' }` |
| ADMIN `/my-catalog` `scope=myCatalog` 本企业 | **BE PASS；FE 双入口切换 P1** | BE：M≤N、跨企仅 full；FE 见 FIND-001 |
| PROVIDER `/my-catalog` 本人；无目录维护入口 | **PASS** | BE 本人列表；菜单仍由 906 `canMaintainCatalog` 隐藏（907 未改 layout） |
| UX：Pagination jumper / Cascader / 本页全选 / 统一维护 / 筛 Tab 翻页清选择 | **PASS（源码+Vitest 存在）** | 页 `show-quick-jumper`；`MaintenanceCategoryCascader`；`PAGE_SELECT_ALL_LABEL` / `UNIFIED_MAINTAIN_LABEL`；`clearSelection` |
| 编辑/导入/详情仍 `industryCategory` + `INDUSTRY_CATEGORY_OPTIONS` | **PASS（具名 spec 存在）** | 三份 `907-industry-category-regression`；**未改**实现 |
| 浏览仍无该筛 | **PASS** | 907 未改 browse；`CatalogBrowsePage.vue` 无 `industryCategory` |
| USER 深链 `/my-catalog` 不可达 | **PASS（结构）** | 保留 906 `beforeEnter` + 页级 `canEnterMaintenancePage('myCatalog','USER')===false` |
| API scope 与 UI 一致 | **FAIL（P1）** | 见 FIND-001：组件复用时闭包 `scope` 不跟随页头 |

固定 seed N vs M：`MyCatalogScopeSplitIntegrationTest` `@DisplayName("907-my-catalog-scope-split")`：`pageSize=100` 下 `m <= n`；跨企仅 full；同企异 `create_by` 在 myCatalog。H2 `flyway=false` 下种子用户 `admin`/`admin2`/`provider` 同 DEMO 企业（`UserAccountService.ensureSeedUsers`），与 §3.2 一致。

## 测试覆盖（testScope）

| 项 | 结果 |
|---|---|
| 命名用例 `907-my-catalog-scope-split` | **有** FE composable spec + UX spec + BE `@DisplayName` |
| 命名用例 `907-industry-category-regression` | **有** editor / import / detail 三份 `it(...)` 标题 |
| Vitest：myCatalog vs full list query；Cascader/本页全选 | **有** `useCatalogMaintenance.spec.ts`（**两独立实例**，未覆盖同实例切 scope） |
| 后端：ADMIN 本企业；PROVIDER 本人；跨 scope 403 | **有** `MyCatalogScopeSplitIntegrationTest` |
| ADMIN 双入口 N vs M（M≤N；跨企仅 full） | **有** 同上 |

**未**代替 tester 宣称 PASS/VERIFIED。本实例未重跑 Vitest/Maven。DEV 自测声明不计入本 REV 门禁。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-907-001 | P1 | OPEN | `routes.ts` L37–42 与 L76–81 均挂 `CatalogMaintenancePage`（`scope` 分别为 myCatalog / full）。`WorkbenchLayout.vue` L315 `<RouterView />` **无** `:key`（907 **denyModify** layout）。`useCatalogMaintenance(props.scope)` 把 `scope` 收成 setup **闭包常量**（`useCatalogMaintenance.ts` L104、L165–166、L311、L344）；`pageTitle` 为 `computed(() => props.scope === ...)`（`CatalogMaintenancePage.vue` L64–66）会随 props 更新，但 `onMounted` 只 `init()` 一次（L75–81），无 `watch(() => props.scope)`。Vue Router：「同一组件用于两条路由时复用实例」。ADMIN 侧栏从「目录维护」点到「我的目录」：页头可变为「我的目录（本企业/本人）」，list/PUT/POST 仍可能 `scope=full`（反向则全量入口仍打 myCatalog）。 | 在 **writeSet 内**（页/composable，**禁止**改 layout）：`scope` 必须响应式驱动 list 与 PUT/POST；`props.scope` 变化时重新 `init`、清选择、重跑页级 guard。补测试：同一 composable/页面实例 `full → myCatalog` 后，list query 与写 URL 均为 `myCatalog`（不得只测两个独立 `useCatalogMaintenance()` 实例）。 | REQ-CAT-017 |
| FIND-WSC-907-002 | P2 | OPEN | `frontend/src/api/catalog.ts` `updateMaintenanceEntry` / `batchUpdateMaintenanceEntries` 仍不带 `scope`。907 用 `apiRequest` 附 `?scope=` 为 denyModify api 下的正确做法；其他调用方若改走生成函数会漏 scope。 | 后续可写 `frontend/src/api/**` 的契约任务：OpenAPI PUT/POST 补 query `scope` 并重生 client；907 再改回生成函数。 **本轮不必改 api。** | REQ-CAT-017 |

## 架构与可维护性备注（非阻塞）

- `EnterpriseProductScope` 只读复用符合 §3.2；空 `create_by` 不进 ADMIN myCatalog，写路径 403，fail-closed。
- 页级 guard 已按 906 交接改为 `canSeeMyCatalog`（myCatalog）/ `canMaintainCatalog`（full），避免 PROVIDER 被误伤。
- `sourceProducts` 对 USER 未单独 fail-closed（依赖 905 拦截器 403）。隐藏 ≠ 授权仍由 interceptor 把关；不升格。
- Cascader `ConfigProvider` token 映射与 901 浏览条同色值；`data-maint-cascader-theme="ant-token-mapped"`。
- 演示 `ensurePendingSamples` 无 `create_by`，只出现在 `scope=full`，与「本企业」语义一致。
- 占位页可留作 906 spec 扫描源；权威 UI 已是维护页。

## 决策依据

- P1=1 → **REQUEST_CHANGES**。修复 FIND-WSC-907-001 后由 **同一隔离 codeReviewer 角色** 复审；P0/P1 清零方可进入独立 tester。
- 本 REV **不**代 tester 宣称测试通过；**无** security/migration 代批。
- FIND-002 为 P2 残差，不单独阻塞；不得为关闭它而改 `frontend/src/api/**`。
