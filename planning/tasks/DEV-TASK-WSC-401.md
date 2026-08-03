# DEV-TASK-WSC-401

```yaml
taskId: TASK-WSC-401
runId: RUN-WSC-006
actorInstance: developer-wsc-401
status: READY_FOR_REVIEW
completedAt: 2026-08-03T09:47:00+08:00
reqs:
  - REQ-SHELL-001
  - REQ-CAT-001
  - REQ-CAT-005
  - DEC-WSC-001
  - REQ-UX-004
```

## 摘要

迁入门户静态页（`/`、`/workspace`、`/docs`）与 PortalLayout 公开路由；重构路由使根路径不再 redirect 至 `/overview`。新增产品 create 模式编码自动生成（DEC-WSC-001）。总览页改为流体宽度。后端 `CatalogProduct` 增加 `updatedAt`，列表分页前按修改时间倒序。

## DOMAIN / FEATURE 映射（DEC-WSC-001）

| productType | DOMAIN | FEATURE | 示例前缀 |
|---|---|---|---|
| API | API | SVC | `API-SVC-{NNNN+}` |
| DATASET | DATA | SET | `DATA-SET-{NNNN+}` |
| REPORT | RPT | DOC | `RPT-DOC-{NNNN+}` |
| OTHER | GEN | MISC | `GEN-MISC-{NNNN+}` |

NNNN 段：毫秒时间戳 + 随机数拼接后取末 4–6 位数字，满足 `/^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$/`。

## writeSet 变更

| 路径 | 说明 |
|---|---|
| `frontend/src/features/portal/WorkspaceIntroPage.vue` | 接入端简介；「打开接入端」→ `/login` |
| `frontend/src/features/portal/DocsPage.vue` | 文档中心最小简介 |
| `frontend/src/features/portal/portal.spec.ts` | 门户 smoke |
| `frontend/src/router/index.ts` | PortalLayout 公开路由 + WorkbenchLayout 鉴权 |
| `frontend/src/router/routes.ts` | 移除 `/` → `/overview` redirect |
| `frontend/src/router/router.portal.spec.ts` | 路由 public meta smoke |
| `frontend/src/features/catalog/editor/utils/productCode.ts` | 编码生成 |
| `frontend/src/features/catalog/editor/utils/productCode.spec.ts` | 编码单测 |
| `frontend/src/features/catalog/editor/composables/useProductEditor.ts` | create 自动编码 + `onProductTypeChange` |
| `frontend/src/features/catalog/editor/ProductEditorPage.vue` | 只读编码 +「自动生成」提示 |
| `frontend/src/features/overview/OverviewPage.vue` | `width: 100%`，移除 max-width 1200px |
| `frontend/src/api/catalog.ts` | `Product.updatedAt?` |
| `backend/.../catalog/browse/CatalogProduct.java` | `Instant updatedAt` |
| `backend/.../catalog/browse/CatalogBrowseSeedStore.java` | 种子 staggered updatedAt |
| `backend/.../catalog/browse/CatalogBrowseService.java` | 排序 + 响应字段 |
| `backend/.../catalog/editor/ProductEditorService.java` | 写路径刷新 updatedAt |
| `backend/.../catalog/maintenance/CatalogMaintenanceService.java` | 关联/待关联 updatedAt |
| `backend/.../catalog/browse/CatalogBrowseIntegrationTest.java` | 排序集成测 |

未改：`contracts/**`、`product/**`、`planning/approved/**`、`ai/runs/**`。

## REQ 映射

| REQ | 实现 |
|---|---|
| **REQ-SHELL-001** | `/` `/workspace` `/docs` 静态门户 + PortalLayout；公开 meta |
| **REQ-CAT-001** | create 模式产品编码自动生成（类型切换重生成） |
| **REQ-CAT-005** | 列表 `updatedAt` DESC 排序；写路径刷新 |
| **DEC-WSC-001** | `{DOMAIN}-{FEATURE}-{NNNN+}` 映射表 + 校验正则 |
| **REQ-UX-004** | 总览 `width: 100%`；门户宽屏容器已有 fluid CSS |

## 自测命令与结果

```text
cd frontend
pnpm exec vitest run src/features/portal/portal.spec.ts src/router/router.portal.spec.ts src/features/catalog/editor/utils/productCode.spec.ts src/features/catalog/editor/composables/useProductEditor.spec.ts src/features/overview/OverviewPage.spec.ts
```

| 命令 | 结果 |
|---|---|
| vitest portal + router + productCode + useProductEditor + OverviewPage | **PASSED**（21 tests, exit 0） |
| `mvn -pl app/data-chain-service -am test -Dtest=CatalogBrowseIntegrationTest#listProducts_sortedByUpdatedAtDesc` | **PASSED**（exit 0） |

## denyModify 遵守

- 未改 `ai/runs/**/state.yaml`、`events.jsonl`
- 未改 `contracts/**`、`product/**`、`planning/approved/**`
- 未兼任 reviewer/tester；未自行标记 VERIFIED
