# DEV-TASK-WSC-306

```yaml
taskId: TASK-WSC-306
actorInstance: developer-wsc-306
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-005
status: READY_FOR_REVIEW
completedAt: 2026-08-02T21:35:00+08:00
reqs:
  - REQ-CAT-001
  - REQ-CAT-005
  - REQ-RBAC-001
  - REQ-UX-004
```

## 摘要

HOTFIX：数据目录高分辨率填满主区（列表+预览比例撑满、去掉页底留白与写入口卡片占位）；页头在 `productWriteVisible` 时增加「新增产品」→ `/catalog/products/new`（与导入分组，USER 不可见）；`WorkbenchLayout` **卸载** `WriteEntryDemo`（非 CSS 隐藏）；列表 sticky「产品列表」消除 `.pane` 顶 padding 露缝。Vitest 覆盖权限按钮与布局可测点。状态 **READY_FOR_REVIEW**。

## 变更（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | REQ-CAT-001 / REQ-CAT-005 / REQ-RBAC-001 / REQ-UX-004 | 页头 `catalog-open-create`；`header-actions` 与导入并排；宽屏 `bi-pane` 比例列；保留视口高度约束；`.pane.list` 去顶 padding，`.list-head` sticky 自带顶内边距+不透明底+z-index |
| `frontend/src/features/catalog/browse/CatalogBrowsePage.spec.ts` | REQ-RBAC-001 / REQ-UX-004 | 新增按钮权限接线；sticky 无露缝 CSS；高分布局；布局无 WriteEntryDemo |
| `frontend/src/layouts/WorkbenchLayout.vue` | REQ-UX-004 / REQ-SHELL | 移除 `WriteEntryDemo` import 与挂载 |
| `planning/tasks/DEV-TASK-WSC-306.md` | — | 本报告 |

未改：`WriteEntryDemo.vue` 本体（布局不再挂载即可）；backend / contracts / api / state / events / REV。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-001** | 目录浏览主区列表+预览在宽/高屏更好占满视口；触底滚动容器高度约束保留 |
| **REQ-CAT-005** | 页头「新增产品」→ 新建页路由 |
| **REQ-RBAC-001** | `v-if="productWriteVisible"`；USER 无新增/导入写按钮（导入仍 `productImportVisible`） |
| **REQ-UX-004** | sticky 标题吸附无上方漏行；卸除写入口演示卡片；pane 自滚约束不破坏 |

## 自测

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse --reporter=default
```

| 文件 | Tests | 结果 |
|---|---|---|
| `CatalogBrowsePage.spec.ts` | 4 | **PASSED** |
| `useCatalogBrowse.spec.ts` | 19 | **PASSED** |
| `useCatalogImportEntry.spec.ts` | 4 | **PASSED** |
| 合计 | 27 | **PASSED** |

人工建议（tester）：宽屏 `/catalog` 列表+预览无明显大片留白；ADMIN/PROVIDER 见「新增产品」+「批量导入」、USER 皆无；滚动列表时「产品列表」贴顶无露行；工作台底部无写入口演示卡。

## SCOPE / denyModify

- 仅改 writeSet 内 browse + WorkbenchLayout + 本 DEV
- 未改 REV / state / events / contracts / api / backend / editor|detail|import|product
- 未兼任 codeReviewer / tester；未标 VERIFIED
