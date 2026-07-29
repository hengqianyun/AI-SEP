# DEV-TASK-WSC-004

```yaml
taskId: TASK-WSC-004
actorInstance: developer-wsc-004
status: READY_FOR_REVIEW
completedAt: 2026-07-29T16:14:00+08:00
reqs:
  - REQ-CAT-001
  - REQ-CAT-002
  - REQ-CAT-003
```

## 摘要

实现目录浏览三栏（L1 索引 / 产品列表 / 预览）、多维筛选与大小写不敏感关键词搜索、预览区详情/上链/编辑入口（编辑仅 ADMIN/PROVIDER）。后端 `com.wsc.catalog.browse` 提供只读 GET 接口 + 内存 seed；与 `StubWriteController` 写占位共存（仅 GET）。

## SCOPE 例外

| 项 | 说明 |
|---|---|
| `frontend/src/router/routes.ts` | **仅**将 `/catalog` 的 `component` 换成 `CatalogBrowsePage.vue`（TASK SCOPE_AMEND）。未改 detail/editor/chain/overview 路由。 |
| `frontend/src/api/**` | **未修改**（只读消费 `catalog.ts`）。 |
| `contracts/**` / migration / detail\|editor\|admin | **未修改**。 |
| `planning/**` | 仅本 DEV 报告。 |

## 文件列表

### 后端（新增）

| 路径 | REQ | 说明 |
|---|---|---|
| `backend/.../catalog/browse/package-info.java` | — | 包说明 |
| `backend/.../catalog/browse/CatalogCategory.java` | REQ-CAT-001 | 分类记录 |
| `backend/.../catalog/browse/CatalogProduct.java` | REQ-CAT-001 | 产品记录 |
| `backend/.../catalog/browse/CatalogBrowseSeedStore.java` | REQ-CAT-001 | 内存 seed：2×L1、各有 L2、5 产品（DATASET/REPORT/API/OTHER） |
| `backend/.../catalog/browse/CatalogBrowseService.java` | REQ-CAT-001/002 | 筛选/分页/keyword |
| `backend/.../catalog/browse/CatalogBrowseController.java` | REQ-CAT-001/002 | GET categories / products / products/{id}；需登录 |
| `backend/.../test/.../catalog/browse/CatalogBrowseIntegrationTest.java` | testScope | 筛选、空列表、keyword、401、404 |

### 前端（新增/更新）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | REQ-CAT-001..003 | 三栏 + 筛选栏 + loading/empty/error |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` | REQ-CAT-001/002 | 列表/预览状态机 |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` | testScope | 空态文案、retry、标签 |
| `frontend/src/features/catalog/browse/components/SensitiveId.vue` | §3.6 | 信用代码截断+复制 |
| `frontend/src/features/catalog/browse/utils/truncateSensitive.ts` | §3.6 | 截断工具 |
| `frontend/src/features/catalog/browse/utils/labels.ts` | REQ-CAT-002 | 枚举中文标签与空态文案 |
| `frontend/src/router/routes.ts` | SCOPE_AMEND | catalog 列表页挂载 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-001** | 左 L1 / 中列表（序号、编码、名称、上链次数、类型）/ 右预览（名称、编码、路径、上链次数、概述、操作） |
| **REQ-CAT-002** | L1/L2、dataSource、productType、involvesPublicData、deliveryMethod、q（名/编码大小写不敏感）；`data-testid=catalog-empty` |
| **REQ-CAT-003** | 详情→`/catalog/products/:id`；上链→`/chain/products/:id`；编辑→`/catalog/products/:id/edit`（`useCanWrite.productWriteVisible`，USER 无编辑按钮） |

## API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/catalog/categories` | L1/L2 列表；需登录 |
| GET | `/api/v1/catalog/products` | 筛选+分页；需登录 |
| GET | `/api/v1/catalog/products/{id}` | 预览/详情只读字段 + `chainCount`；需登录 |

写路径仍由 `StubWriteController`（POST/PUT）占位，路径不冲突。

## 自测命令与结果

### 后端

```text
mvn "-Dtest=com.wsc.catalog.browse.CatalogBrowseIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `com.wsc.catalog.browse.CatalogBrowseIntegrationTest` | 8 | **PASS** |
| **合计** | **8** | **BUILD SUCCESS** |

覆盖：未登录 401；L1+productType 筛选；无结果空列表；keyword 大小写不敏感（编码/名称）；组合筛选；产品预览字段+chainCount；404。

### 前端

```text
pnpm exec vitest run src/features/catalog/browse
pnpm typecheck
```

| 命令 | 结果 |
|---|---|
| vitest `catalog/browse` | **5 passed** |
| `pnpm typecheck` | **exit 0** |

## 备注

- Seed 含供应商信用代码；预览区按 §3.6 截断展示并支持完整复制。
- 筛选失败走 `listState=error` + 重试（`data-testid=catalog-error`）。
- 详情/编辑/上链目标页仍为占位路由（005/006），本任务仅 `router.push`。
