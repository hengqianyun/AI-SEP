# DEV-TASK-WSC-906

```yaml
taskId: TASK-WSC-906
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
actorInstance: developer-wsc-906
status: READY_FOR_REVIEW
completedAt: 2026-08-17T13:34:00+08:00
reqs:
  - REQ-SHELL-009
  - REQ-SHELL-010
blocksRelease: false
riskTags: [shell-nav, rbac-visibility, prelanding-reconcile, auth-model-change]
wave: C
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。**未标 VERIFIED**。
> 本任务命中 `auth-model-change`，须经独立 **codeReviewer** + **securityReviewer**；本实例不代写审查。

## objective

Wave C：数据目录可收起子菜单（全链 → 我的目录 → 我的数据产品 → 仅 ADMIN 目录维护）；ADMIN 双入口；PROVIDER 无目录维护；统一路由 `/my-catalog`；侧栏企业只读（P1）；独占 `useCanWrite.ts` 全文对齐 §3.1 矩阵。不实现 `/my-catalog` 页面业务（907 独占 CatalogMaintenancePage / useCatalogMaintenance）。

## 预落地对账（§0.2）

| 项 | 预落地 | 对账动作 |
|---|---|---|
| `useCanWrite.ts` `canSeeMyMaintenance` | ADMIN+PROVIDER 可见「我的目录」；`canWriteProduct`/`canImportProduct` **仅 PROVIDER**；ADMIN 产品写 **false**（偏离 §3.1） | **保留** myCatalog 可见性；**修正** ADMIN `canWriteProduct`/`canImportProduct` = **true**；导出 `canSeeMyCatalog`（`canSeeMyMaintenance` 同义别名） |
| `WorkbenchLayout.vue`「我的目录」`/my-maintenance` | 菜单在 `nav-catalog-submenu` **组外**；序偏离；中文乱码；`myMaintenanceVisible` 重复声明 | **移入** `nav-catalog-submenu`；序：全链 → 我的目录 → 我的数据产品 →（ADMIN）目录维护；路由改为 `/my-catalog`；修复 UTF-8 文案；去掉重复声明 |
| `routes.ts` `/my-maintenance` | 权威路径为 void 前占位，挂 CatalogMaintenancePage | **统一** `/my-catalog`；`/my-maintenance` **仅 redirect**，不得作权威路径；页组件用 shell 占位（907 再接维护页）；meta.title 区分双入口 |
| 深链门控 | 仅 layout watch 拦 `/catalog/maintenance` | **扩展** USER：`/catalog/maintenance` `/my-catalog` `/my-products` `/admin/users`；PROVIDER：`/catalog/maintenance`；`routes.ts` `beforeEnter` + layout watch |
| 企业信息区 SHELL-010 | footer 仅企业名/角色，无 testid，无企业标识 | **补** 只读名称 + `enterpriseId`；无切换/编辑；无企业管理页 |
| 可收起组 | aria-expanded / aria-controls / sessionStorage / chevron 已有 | **保留** 非回归 |

命名用例：`906-nav-my-catalog-submenu`、`906-provider-no-maintenance-menu`（`WorkbenchLayout.spec.ts`）。

## filesChanged

| 路径 | 说明 | REQ |
|---|---|---|
| `frontend/src/features/auth/composables/useCanWrite.ts` | 独占全文对齐 §3.1：ADMIN 产品写/导入 true；PROVIDER 无 catalogMaintenanceUI；myCatalogUI ADMIN+PROVIDER | REQ-SHELL-009、REQ-RBAC-002 门控面 |
| `frontend/src/features/auth/composables/useCanWrite.spec.ts` | 三角色矩阵单元格含 ADMIN write/import true | REQ-SHELL-009 |
| `frontend/src/layouts/WorkbenchLayout.vue` | 子菜单 IA/序/active；深链 watch；document.title；企业只读区 | REQ-SHELL-009、REQ-SHELL-010 |
| `frontend/src/layouts/WorkbenchLayout.spec.ts` | **新建** §4.1 / 四 surface / aria / 企业区 / 命名用例 | REQ-SHELL-009、REQ-SHELL-010 |
| `frontend/src/router/routes.ts` | `/my-catalog` meta/title/guard；`/my-maintenance` redirect；维护 title「目录维护（全量）」；保留 `/admin/users` | REQ-SHELL-009 |
| `frontend/src/features/shell/navSurfaces.ts` | 四 surface active 判定 | REQ-SHELL-009 |
| `frontend/src/features/shell/navSurfaces.spec.ts` | 四 surface 互斥 + 组头高亮 | REQ-SHELL-009 |
| `frontend/src/features/shell/deepLinkAccess.ts` | 深链结构可达谓词 | REQ-SHELL-009 |
| `frontend/src/features/shell/deepLinkAccess.spec.ts` | USER/PROVIDER 负例 | REQ-SHELL-009 |
| `frontend/src/features/shell/routeGuards.ts` | `beforeEnter`（`router/index.ts` 不在 writeSet） | REQ-SHELL-009 |
| `frontend/src/features/shell/MyCatalogPlaceholderPage.vue` | `/my-catalog` 页头占位；**无**维护业务 | REQ-SHELL-009 |

## 交 907

| 项 | 本任务 | 907 |
|---|---|---|
| `/my-catalog` 路由/meta/ADMIN 双入口语义 | **已建立**；不得删除 | 可扩页级 guard；接入 CatalogMaintenancePage + `scope=myCatalog` |
| `/my-catalog` 页业务 / 维护 UX | shell 占位（title 可区分） | **独占** CatalogMaintenancePage / useCatalogMaintenance |
| `/catalog/maintenance` 页头 h1 | 未改页面（仍为「目录维护」）；**meta.title** =「目录维护（全量）」 | 可选将页头与 meta 对齐 |
| ADMIN 产品写 UI（browse/import spec） | composable 已 true | 若需，更新 denyModify 外的 V1.4 断言（本任务不得改 browse/import） |

## 自测命令与结果

```bash
# cwd: 仓库根
pnpm --dir frontend exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/layouts/WorkbenchLayout.spec.ts src/features/shell/navSurfaces.spec.ts src/features/shell/deepLinkAccess.spec.ts
# exitCode: 0
# Test Files  4 passed (4)
# Tests       20 passed (20)
```

命名用例：`906-nav-my-catalog-submenu`、`906-provider-no-maintenance-menu`。

## denyModify 自检

未改：`frontend/src/features/catalog/browse/**`、`frontend/src/features/catalog/maintenance/**`、`contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**`、`product/**`、`planning/**`、`ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`、`frontend/src/styles/**`、`frontend/src/theme/**`。未 git commit。未标 VERIFIED。

## 范围外已知红测（未改；denyModify）

| 测试 | 现象 | 原因 |
|---|---|---|
| `CatalogBrowsePage.spec.ts` TASK-WSC-603「product write/import UI only PROVIDER」 | `canWriteProduct('ADMIN')` expected false was **true** | V1.4/V1.5 断言；SNAP-008 / §3.1 要求 ADMIN **true**；路径在 browse denyModify |
| `useProductImport.spec.ts`「仅 PROVIDER 可挂载弹层」 | `canImportProduct('ADMIN')` expected false was **true** | 同上；路径在 import denyModify |

既有扫描布局的 spec **通过**：`RoleSwitcher.spec.ts`、`UsersAdminPage.spec.ts`、`router.portal.spec.ts`；`CatalogBrowsePage.spec.ts` 其余 15 项（含 dual surface / 901 对账）通过。

## acceptance 自检

| 项 | 结果 |
|---|---|
| IA：我的目录在 `nav-catalog-submenu` 内；序全链 → 我的目录 → 我的数据产品 →（ADMIN）目录维护 | PASS（`906-nav-my-catalog-submenu`） |
| 路由统一 `/my-catalog`；`/my-maintenance` 仅重定向 | PASS |
| 四 surface active；meta.title「目录维护（全量）」vs「我的目录（本企业/本人）」 | PASS |
| ADMIN：全链 + 目录维护 + 我的目录 + 我的数据产品 + 用户管理 | PASS |
| PROVIDER：全链 + 我的目录 + 我的数据产品；不可见目录维护；深链维护不可达 | PASS（`906-provider-no-maintenance-menu`） |
| USER：仅全链；其余菜单隐藏 + 深链结构不可达 | PASS |
| `useCanWrite` 与 §3.1 一致（ADMIN 写/导入 true） | PASS |
| 可收起组 aria / sessionStorage / chevron | PASS |
| 侧栏企业只读；无切换/编辑；无企业管理页 | PASS |

本实例 **不** 标 VERIFIED。须独立 codeReviewer **且** securityReviewer。
