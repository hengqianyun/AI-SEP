# DEV-TASK-WSC-402

```yaml
taskId: TASK-WSC-402
runId: RUN-WSC-006
actorInstance: developer-wsc-402
status: READY_FOR_REVIEW
completedAt: 2026-08-03T10:41:00+08:00
reqs:
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-UX-007
```

## 摘要

产品详情页与新增/编辑页去掉过窄 `max-width`，主区改为 `width: 100%` 流体铺满（对齐总览 TASK-WSC-401）。宽屏（≥1400px）详情网格增至 4 列、编辑表单增至 3 列；保留既有小屏 `@media`。API 编辑仍挂 `wide`，并对 type-block 设 `min-width: 0`，避免 OpenAPI 面板横向挤爆。未改动 401 产品编码自动生成逻辑。

## writeSet 变更

| 路径 | 说明 |
|---|---|
| `frontend/src/features/catalog/detail/ProductDetailPage.vue` | 移除 `max-width: 1100px`；主区流体宽度；宽屏 4 列 |
| `frontend/src/features/catalog/detail/components/TypeSpecificReadonly.vue` | 类型只读网格宽屏 4 列，与详情对齐 |
| `frontend/src/features/catalog/detail/ProductDetailPage.spec.ts` | 布局断言（无窄 max-width / 保留小屏 media） |
| `frontend/src/features/catalog/editor/ProductEditorPage.vue` | 移除 900/1100 max-width；流体宽度；宽屏 3 列；API `wide` 防挤爆 |
| `frontend/src/features/catalog/editor/ProductEditorPage.spec.ts` | 布局断言 + 401 自动编码 UX 未回退 |

未改：`backend/**`、`contracts/**`、`frontend/src/api/**`、`portal/**`、`browse/**`、`overview/**`、`product/**`、`planning/approved/**`、`ai/runs/**/state.yaml`、`events`。

## REQ 映射

| REQ | 实现 |
|---|---|
| **REQ-CAT-004** | 详情页宽屏流体布局，信息网格可扩列 |
| **REQ-CAT-005** | 新增/编辑页流体布局；API OpenAPI 区可用 |
| **REQ-UX-007** | 去掉过窄 max-width，与总览宽屏利用一致 |

## 自测命令与结果

```text
cd frontend
pnpm exec vitest run src/features/catalog/detail/ProductDetailPage.spec.ts src/features/catalog/editor/ProductEditorPage.spec.ts src/features/catalog/editor/utils/productCode.spec.ts src/features/catalog/editor/composables/useProductEditor.spec.ts src/features/catalog/detail/composables/useProductDetail.spec.ts src/features/catalog/detail/utils/typeSpecificView.spec.ts
```

| 命令 | 结果 |
|---|---|
| vitest detail/editor layout + 401 编码 + 相关 composable/utils | **PASSED**（30 tests, exit 0） |

## denyModify 遵守

- 未改 `backend/**`、`contracts/**`、`frontend/src/api/**`
- 未改 `portal/**`、`browse/**`、`overview/**`、`product/**`
- 未改 `planning/approved/**`、`ai/runs/**/state.yaml`
- 未回退 `productCode` / create 自动编码
- 未兼任 reviewer/tester；未自行标记 VERIFIED
