# DEV-TASK-WSC-104

```yaml
taskId: TASK-WSC-104
actorInstance: developer-wsc-104
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T16:20:00+08:00
reqs:
  - REQ-CAT-001
  - REQ-CAT-002
  - REQ-CAT-003
```

## 摘要

在 `backend/app/data-chain-service`（`com.shdata.datachain.catalog.browse`）与 `frontend/src/features/catalog/browse` 落地三级目录浏览：顶部空间/行业标签、子类分节列表、预览三级路径、行业筛可选 L2/L3、滚动加载更多（契约字段 `page`/`pageSize`/`total`；筛选条件在 load-more 中保留）。未改 `contracts/**`、`frontend/src/api/**`、`catalog/admin/**`。

## 变更文件列表

### 后端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `.../catalog/browse/CatalogProduct.java` | REQ-CAT-001 | 增 `l3CategoryId`；保留无 l3 兼容构造供 editor |
| `.../catalog/browse/CatalogCategory.java` | REQ-CAT-001 | 注释对齐 L1/L2/L3 |
| `.../catalog/browse/CatalogBrowseSeedStore.java` | REQ-CAT-001 | L3 种子；产品挂三级；路径/挂载计数；分页样例数据 |
| `.../catalog/browse/CatalogBrowseService.java` | REQ-CAT-001/002 | `l3CategoryId` 筛选；L2 含子 L3；`pathLabels`；`page/pageSize/total` |
| `.../catalog/browse/CatalogBrowseController.java` | REQ-CAT-001/002 | 查询参数增加 `l3CategoryId` |
| `.../catalog/browse/package-info.java` | — | 对齐 TASK-WSC-104 |
| `.../test/.../CatalogBrowseIntegrationTest.java` | REQ-CAT-001/002 | 三级/筛选空态/分页与筛选保留（H2） |

### 前端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | REQ-CAT-001/002/003 | 顶部空间/行业标签；子类分节；滚动 load-more；预览路径与三分入口 |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` | REQ-CAT-001/002 | 筛选对齐三级；append 分页；`groupProductsByL3` |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` | REQ-CAT-001/002 | 分节/行业筛解析/空态等单测 |

> 未改 `SensitiveId.vue` / `labels.ts` / `truncateSensitive.ts`（既有能力复用）。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-001** | 顶部空间(L1)/行业(L2)标签切换产品集；列表按 L3 分节（标题+数量）；行字段含序号/编码/名称/上链次数/类型；预览含三级 `categoryPath`；滚动加载有 loading，筛选与已加载列表保留 |
| **REQ-CAT-002** | 来源/类型/公共数据/交付/关键词同前；行业筛可选 L2 或 L3；无结果空态 |
| **REQ-CAT-003** | 预览「详情 / 上链 / 编辑」；编辑仅 `productWriteVisible`（普通用户无编辑） |

## 已知边界 / 后续任务

- `CatalogProduct` 无 l3 兼容构造供 TASK-WSC-105 editor 过渡；browse 种子已挂 L3。
- 分类维护 CRUD（含 L3）属 TASK-WSC-103；本任务未触碰 `catalog/admin/**`。
- 详情/编辑三级选择器属 TASK-WSC-105。

## 自测命令与结果

### 后端

```text
mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.browse.CatalogBrowseIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `com.shdata.datachain.catalog.browse.CatalogBrowseIntegrationTest` | 11 | **PASSED** / **BUILD SUCCESS** |

覆盖：登录门禁；L1/L2/L3 分类；L1/L2/L3 筛选；空态；关键词；三级路径预览；`page`/`pageSize`/`total`；page2 仍满足 `l3CategoryId` 筛选。集成测试使用 H2 + 排除 Redis。

### 前端

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse/composables/useCatalogBrowse.spec.ts
pnpm typecheck
pnpm lint
pnpm build
```

| 命令 | 结果 |
|---|---|
| vitest browse composable | **PASSED**（8 tests） |
| `pnpm typecheck` | **PASSED** |
| `pnpm lint` | **PASSED**（placeholder exit 0） |
| `pnpm build` | **PASSED** |

## SCOPE / denyModify

- 未修改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`
- **未触碰** `frontend/src/features/catalog/admin/**`、`backend/.../catalog/admin/**`（及 detail/editor/maintenance/import）
- 未与 TASK-WSC-103 混写 admin；仅 browse writeSet
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer；未自行标记 VERIFIED
