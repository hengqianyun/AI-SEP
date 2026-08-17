# DEV-TASK-WSC-901

```yaml
taskId: TASK-WSC-901
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: developer-wsc-901
status: READY_FOR_REVIEW
completedAt: 2026-08-17T10:50:00+08:00
reqs:
  - REQ-CAT-014
  - REQ-CAT-019
blocksRelease: false
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## objective

Wave A 浏览筛选重构（REQ-CAT-014 / REQ-CAT-019）：移除列表区业务视图/业务大类标签卡片；高级筛选常显、无 toggle；去除浏览 `industryCategory`；以 Cascader 联动 `l1CategoryId`/`l2CategoryId`；保留 V1.5 其余筛选项与 `supplierName`；`/catalog` 与 `/my-products` 同源筛选；座序图组件未改（归 902）。

## 预落地对账（§0.2）

| 迹象 | 对账结论 |
|---|---|
| 工作树 browse 仍为 V1.5 标签栏 + 内联 `industryCategory` select | **已重构**：移除 `nav-card`/tag-bar；启用 `BrowseFilterBar` + Cascader |
| 存在未接入页面的 `BrowseFilterBar.vue`（含 industry 控件） | **已改写**：去除 industry；接入 `CatalogBrowsePage` |
| `labels.ts` 缺 Cascader 常量 | **已补齐** `BROWSE_CATEGORY_CASCADER_*` |
| git diff browse 为空（已 commit 基线） | 以 SNAP-008 + PLAN acceptance 为准实现并补测 |

## filesChanged

| 路径 | 说明 |
|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | 移除 V1.5 空间/行业标签卡片与内联筛选；挂载 `BrowseFilterBar`；`@category-change` → `applyCategoryPath` |
| `frontend/src/features/catalog/browse/components/BrowseFilterBar.vue` | Cascader l1/l2（change-on-select + allow-clear）；Ant Select/Input；ConfigProvider token 映射；独立 `supplierName` 字段；分类 loading/empty/error 提示 |
| `frontend/src/features/catalog/browse/components/BrowseFilterBar.spec.ts` | **新建** §0.2 命名用例 + token/Cascader 结构断言 |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` | 删除 `industryCategory` 字段与 query；新增 `categoryPathFromFilters` / `applyCategoryPath` / `allL2Categories`；`resetFilters` 全量重置含分类 |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` | 更新默认 filter 断言；新增 `901-no-industryCategory-query` / `901-cascader-l1-l2-clear`（catalog + mine 参数化） |
| `frontend/src/features/catalog/browse/CatalogBrowsePage.spec.ts` | 新增 `901-no-nav-cards` 等；移除 nav-card CSS 期望 |
| `frontend/src/features/catalog/browse/utils/labels.ts` | Cascader / supplierName 文案常量 |

**denyModify 遵守**：未改 `CirculationSeatMap*`、`useCanWrite.ts`、`WorkbenchLayout.vue`、backend、contracts、api、e2e、planning/product/state/events。

## REQ 映射

| REQ | 实现要点 | 证据 |
|---|---|---|
| REQ-CAT-014 | 无列表区卡片；筛选常显；Cascader l1/l2；双表面同源 | `901-no-nav-cards`；`BrowseFilterBar` Cascader；`CatalogBrowsePage` 共用 composable |
| REQ-CAT-019 | 浏览不传 `industryCategory`；编辑/导入枚举 untouched | `901-no-industryCategory-query`；未改 editor/import |

## acceptance 自检

| 项 | 结果 |
|---|---|
| 无业务视图/业务大类卡片与 active 标签行 | PASS（DOM testid 已移除） |
| 无「高级筛选」toggle；面板默认可见 | PASS |
| 高级区无 industryCategory；query 无 industryCategory / enterpriseName | PASS（Vitest） |
| Cascader：L1 → 仅 l1CategoryId；L2 → 两者；清空 → 皆无 | PASS（`901-cascader-l1-l2-clear`） |
| Ant→token 映射 | PASS（`data-browse-filter-theme="ant-token-mapped"` + ConfigProvider） |
| `/catalog` 与 `/my-products` 同源 | PASS（mine 参数化 Cascader/query 测试） |
| supplierName 仍发出 | PASS |
| 座序图仍挂载 | PASS（`CirculationSeatMap` 未改；page 仍 `v-if="seatMapVisible"`） |

## 自测命令与结果

```bash
cd frontend
pnpm test -- src/features/catalog/browse/
```

| 项 | 值 |
|---|---|
| 命令 | `pnpm test -- src/features/catalog/browse/` |
| 结果 | **71 passed**（5 files） |
| exitCode | **0** |
| 命名用例 | `901-no-nav-cards`、`901-cascader-l1-l2-clear`、`901-no-industryCategory-query` |

补充：`npx vue-tsc --noEmit` 在 browse 写集无新增错误；仓库内 `WorkbenchLayout.vue` 既有重复标识符为预存问题（本任务 denyModify，未触碰）。

## 已知限制 / 升级

- Cascader 选项依赖 `listCategories`；错误态展示重试仍由页级 `catalog-categories-error` banner 承担（筛栏内亦有 inline error hint）。
- 正式通过须独立 **codeReviewer** + **tester** 按 PLAN `testScope` 复验；developer 未标 VERIFIED。
