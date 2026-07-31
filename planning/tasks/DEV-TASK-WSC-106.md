# DEV-TASK-WSC-106

```yaml
taskId: TASK-WSC-106
actorInstance: developer-wsc-106
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T17:27:00+08:00
reqs:
  - REQ-CAT-007
```

## 摘要

在 `backend/app/data-chain-service`（`com.shdata.datachain.catalog.maintenance`）与 `frontend/src/features/catalog/maintenance` 落地目录维护：状态页签（全部/已维护/待关联）、一二三级筛选、分页（`page`/`pageSize`/`total`）、单条维护保存/取消、批量关联栏。非管理员前端重定向、API `ERR_MAINTENANCE_FORBIDDEN`。未改 `contracts/**`、`frontend/src/api/**`；**未触碰** detail/editor/chain。

## 变更文件列表

### 后端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `.../catalog/maintenance/CatalogMaintenanceController.java` | REQ-CAT-007 | GET 列表；PUT 单条；POST batch |
| `.../catalog/maintenance/CatalogMaintenanceService.java` | REQ-CAT-007 | 筛选/分页；挂三级；批量结果；演示 PENDING 种子 upsert |
| `.../catalog/maintenance/package-info.java` | — | 包说明 |
| `.../test/.../CatalogMaintenanceIntegrationTest.java` | REQ-CAT-007 | 待关联→已维护；批量；非管理员 403；分页 |

### 前端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue` | REQ-CAT-007 | 页签/筛选/列表/编辑/批量/分页；非管理员 `replace('/catalog')` |
| `.../composables/useCatalogMaintenance.ts` | REQ-CAT-007 | 列表状态与单条/批量写 |
| `.../composables/useCatalogMaintenance.spec.ts` | REQ-CAT-007 | 页签/单条/批量/错误面单测 |

### SCOPE_AMEND（非 writeSet，启动/可达必需）

| 路径 | 说明 |
|---|---|
| `.../security/StubWriteController.java` | 移除维护三端点占位，避免与真实 Controller 映射冲突；导入 stub 保留给 107 |
| `frontend/src/router/routes.ts` | 仅新增 `/catalog/maintenance` → `CatalogMaintenancePage`（对齐 REV-102 / 侧栏链接） |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-007** | 全部/已维护/待关联；L1/L2/L3 筛；`page`/`pageSize`/`total`；单条选三级保存/取消；批量勾选+路径+批量保存；提供方/普通用户 API 403 + 页不可达 |

## 已知边界 / 后续

- 演示 PENDING 条目由 maintenance `@PostConstruct` upsert，未改 browse seed 源码。
- 未实现 105（detail/editor/chain）；未与 105 混写。
- 未自行 VERIFIED。

## 自测命令与结果

### 后端

```text
mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.maintenance.CatalogMaintenanceIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `CatalogMaintenanceIntegrationTest` | 4 | **PASSED** / **BUILD SUCCESS** |

覆盖：PENDING→MAINTAINED；非叶节点 `ERR_CATEGORY_LEAF_REQUIRED`；批量成功/失败计数；PROVIDER/USER → `ERR_MAINTENANCE_FORBIDDEN`；`page`/`pageSize`/`total`。

### 前端

```text
cd frontend
pnpm exec vitest run src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts
pnpm typecheck
pnpm lint
pnpm build
```

| 命令 | 结果 |
|---|---|
| vitest maintenance composable | **PASSED**（4 tests） |
| `pnpm typecheck` | **PASSED** |
| `pnpm lint` | **PASSED**（placeholder exit 0） |
| `pnpm build` | **PASSED** |

## SCOPE / denyModify

- 未修改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`
- **未触碰** `catalog/detail/**`、`catalog/editor/**`、`catalog/chain/**`、`catalog/browse/**`、`catalog/admin/**`、`catalog/import/**`
- 未兼任 codeReviewer；未自行标记 VERIFIED
