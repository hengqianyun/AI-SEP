# DEV-TASK-WSC-907

```yaml
taskId: TASK-WSC-907
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
actorInstance: developer-wsc-907-r1
status: READY_FOR_REVIEW
completedAt: 2026-08-17T14:20:00+08:00
reqs:
  - REQ-CAT-017
  - REQ-CAT-013
  - REQ-CAT-019
blocksRelease: false
riskTags: [maintenance-reuse, scope-isolation, enum-regression]
wave: D
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。**未标 VERIFIED**。
> 本实例不代写 REV，不 git commit。

## objective

Wave D：`/my-catalog` 接入目录维护 UX；ADMIN `scope=myCatalog` 本企业、PROVIDER 本人 create_by；ADMIN `/catalog/maintenance` `scope=full` 全平台语义不变；编辑/导入/详情行业类别回归；maintenance BE scope 与 903/905 一致。

## 905/906 交接对账

| 交接项 | 上游状态 | 本任务动作 |
|---|---|---|
| 905：ADMIN `scope=myCatalog` 拦截器 200、结果集仍全平台 | 交 907 在 `CatalogMaintenanceService` 按本企业过滤 | **已做**：create_by → `sys_user.enterprise_id` 与会话 `enterpriseId` 比较；跨企仅 `scope=full` 可见 |
| 905：PROVIDER `myCatalog` 已按 create_by；`full` 403 | 拦截器未改 | **复用**；列表仍本人；未改 `WriteAuthorizationInterceptor` |
| 906：`/my-catalog` 壳层占位；ADMIN 双入口 meta.title | 不得删除 | **已接** `CatalogMaintenancePage` + `scope=myCatalog`；保留「我的目录（本企业/本人）」/「目录维护（全量）」；`/my-maintenance` redirect、`/admin/users`、`beforeEnter: requireDeepLinkAccess` 均保留 |
| 906：占位页文件 | 不在 907 writeSet | **未删** `MyCatalogPlaceholderPage.vue`（906 spec 仍读该文件） |
| 903：`listMaintenanceEntries` 已支持 `scope` | 只读 api | 列表走生成 client；PUT/POST 用 `apiRequest` 附 `?scope=`（不改 `frontend/src/api/**`） |

命名用例：`907-my-catalog-scope-split`、`907-industry-category-regression`。

## filesChanged

| 路径 | 说明 | REQ |
|---|---|---|
| `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue` | 参数化 `scope`；页级 guard；页头对齐双入口；Pagination jumper、Cascader、本页全选、统一维护 | REQ-CAT-017, REQ-CAT-013 |
| `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts` | `scope` 写入 list/PUT/POST；Cascader 路径映射；本页全选；筛/Tab/翻页清选择 | REQ-CAT-017, REQ-CAT-013 |
| `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts` | 双 scope、Cascader 边界、本页全选、清选择 | REQ-CAT-017, REQ-CAT-013 |
| `frontend/src/features/catalog/maintenance/components/MaintenanceCategoryCascader.vue` | L1→L2→L3 Cascader；筛可选父、维护须 L3；token 映射 | REQ-CAT-013 |
| `frontend/src/features/catalog/maintenance/components/MaintenanceCategoryCascader.spec.ts` | Cascader/token 结构断言 | REQ-CAT-013 |
| `frontend/src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts` | 路由双入口 + UX 回归源码断言 | REQ-CAT-017, REQ-CAT-013 |
| `frontend/src/router/routes.ts` | `/my-catalog` 接维护页 `scope=myCatalog`；全量入口 `scope=full`；不改 906 meta.title | REQ-CAT-017 |
| `frontend/src/features/catalog/editor/components/IndustryCategoryField.spec.ts` | 编辑仍用 `INDUSTRY_CATEGORY_OPTIONS` | REQ-CAT-019 |
| `frontend/src/features/catalog/import/components/ImportFieldMapping.spec.ts` | 导入模板含「行业分类（必填）」 | REQ-CAT-019 |
| `frontend/src/features/catalog/detail/CatalogProductDetail.spec.ts` | 详情仍展示 `industryCategory` | REQ-CAT-019 |
| `backend/.../controller/catalog/maintenance/CatalogMaintenanceController.java` | 编排：传 `SessionPrincipal` + query `scope`；忽略客户端 enterpriseId | REQ-CAT-017 |
| `backend/.../service/catalog/maintenance/CatalogMaintenanceService.java` | ADMIN myCatalog 本企业过滤；PROVIDER 本人；写路径同样校验 | REQ-CAT-017 |
| `backend/.../src/test/java/.../catalog/maintenance/MyCatalogScopeSplitIntegrationTest.java` | ADMIN N vs M；PROVIDER 本人；跨 scope 403 | REQ-CAT-017 |

## REQ 映射

| REQ | 实现要点 | 证据 |
|---|---|---|
| REQ-CAT-017 | `/my-catalog` 复用维护 UX；ADMIN 本企业 / PROVIDER 本人；全量入口不改语义 | `907-my-catalog-scope-split`（FE+BE） |
| REQ-CAT-013 | Pagination jumper、Cascader、本页全选、统一维护、筛/Tab/翻页清选择 | composable + `CatalogMaintenanceUx.spec.ts` |
| REQ-CAT-019 | 编辑/导入/详情仍用行业类别枚举；未改 browse | `907-industry-category-regression` 三份具名 spec |

## 自测命令与结果

```bash
# cwd: frontend
pnpm exec vitest run src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts src/features/catalog/maintenance/components/MaintenanceCategoryCascader.spec.ts src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts src/features/catalog/editor/components/IndustryCategoryField.spec.ts src/features/catalog/import/components/ImportFieldMapping.spec.ts src/features/catalog/detail/CatalogProductDetail.spec.ts src/layouts/WorkbenchLayout.spec.ts
# exitCode: 0
# Test Files  7 passed (7)
# Tests       21 passed (21)
```

```bash
# cwd: backend/app/data-chain-service
mvn -q "-Dtest=MyCatalogScopeSplitIntegrationTest,MaintenanceScopeIntegrationTest,CatalogMaintenanceIntegrationTest" test
# exitCode: 0
# Tests run: 1+3+6 = 10, Failures: 0, Errors: 0
```

命名用例：`907-my-catalog-scope-split`（Vitest + `@DisplayName`）、`907-industry-category-regression`（editor/import/detail 具名 spec）。

## denyModify 自检

未改：`WorkbenchLayout.vue`、`useCanWrite.ts`、`frontend/src/features/catalog/browse/**`、`WriteAuthorizationInterceptor.java`、`contracts/**`、`frontend/src/api/**`、`L2Distribution*`、`CatalogBrowseController` / `CatalogBrowseService`、`tests/e2e/**`、`product/**`、`planning/**`、`ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`、`**/sql/**`。未 git commit。未标 VERIFIED。

只读消费：`EnterpriseProductScope`、`RbacMatrix`、`useCanWrite` 导出、`listMaintenanceEntries` / `INDUSTRY_CATEGORY_OPTIONS`。

## acceptance 自检

| 项 | 结果 |
|---|---|
| ADMIN `/catalog/maintenance` `scope=full` 全平台 | PASS（BE N 含跨企） |
| ADMIN `/my-catalog` `scope=myCatalog` 本企业 | PASS（BE M≤N；跨企仅 full） |
| PROVIDER `/my-catalog` 本人 create_by；无目录维护入口 | PASS（BE 列表仅本人；菜单仍由 906 隐藏） |
| UX：Pagination / Cascader / 本页全选 / 统一维护 / 筛 Tab 翻页清选择 | PASS（Vitest） |
| 编辑/导入/详情仍展示/提交 `industryCategory` + `INDUSTRY_CATEGORY_OPTIONS` | PASS（具名 spec） |
| 浏览仍无该筛项 | PASS（未改 browse） |
| USER 深链 `/my-catalog` 不可达 | PASS（保留 906 `beforeEnter` + 页级 `canEnterMaintenancePage`） |
| API scope 与 UI 一致 | PASS（list/PUT/POST 均带 `scope`） |
| 未削弱 906 ADMIN 双入口 meta.title / `/my-maintenance` / `/admin/users` | PASS（`WorkbenchLayout.spec.ts` 8 项仍绿） |

本实例 **不** 标 VERIFIED。须独立 codeReviewer。

## Round 1 修复（FIND-WSC-907-001）

| 项 | 内容 |
|---|---|
| finding | FIND-WSC-907-001（P1） |
| 依据 | `ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-907.md` |
| actorInstance | developer-wsc-907-r1（mustDifferFrom code-reviewer-wsc-907） |
| 根因 | `/catalog/maintenance` 与 `/my-catalog` 共用 `CatalogMaintenancePage`；layout `RouterView` 无 key（907 denyModify）。`useCatalogMaintenance(props.scope)` 把 scope 收成 setup 闭包常量；Vue Router 复用实例时页头 computed 已变，list/PUT/POST 仍打旧 scope。 |
| 修复 | 对齐 603 `mine` 响应式：composable 接受 `MaybeRefOrGetter<MaintenanceScope>`，list/PUT/POST 经 `toValue`/`currentScope` 读当前值；`watch(currentScope)` 重置页码、清选择、取消编辑并 `init`。页面传 `() => props.scope`（禁止 `props.scope` 快照）；`watch(() => props.scope)` 重跑 `canEnterMaintenancePage`。 |
| closeWhen 对照 | ✅ scope 响应式驱动 list 与 PUT/POST；✅ props.scope 变化时重新 init、清选择、重跑页级 guard；✅ 同一 composable 实例 `full → myCatalog` 后 list query 与写 URL 均为 `myCatalog` |
| 未改 | `WorkbenchLayout.vue`；`frontend/src/api/**`（FIND-002 P2 不在本轮）；审查报告；state/events |
| 未标 | VERIFIED；未 git commit |

### 改动文件（本轮 writeSet）

| 路径 | 说明 |
|---|---|
| `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts` | `MaybeRefOrGetter` + `currentScope`；watch 重拉/清选择 |
| `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue` | 传 getter；watch props.scope 重跑 guard |
| `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts` | 同实例 `full → myCatalog` list + PUT + POST |
| `frontend/src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts` | 源码断言：非 `props.scope` 快照 + watch guard |

### 测试证据

```bash
# cwd: frontend
pnpm exec vitest run src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts src/features/catalog/maintenance/components/MaintenanceCategoryCascader.spec.ts
# exitCode: 0
# Test Files  3 passed (3)
# Tests       12 passed (12)
```

同实例用例：`907-my-catalog-scope-split: same instance full → myCatalog list and write URLs`（单一 `useCatalogMaintenance(ref)`，`scope.value` 从 `full` 改为 `myCatalog` 后 list `objectContaining({ scope: 'myCatalog' })`，PUT `/entries/p1?scope=myCatalog`，POST `/entries/batch?scope=myCatalog`，选择被清空）。

**REQ**：REQ-CAT-017

**status**：READY_FOR_REVIEW（Round 2 复审）。developer **不**标 VERIFIED。
