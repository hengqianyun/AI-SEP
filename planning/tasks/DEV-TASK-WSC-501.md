# DEV-TASK-WSC-501

```yaml
taskId: TASK-WSC-501
runId: RUN-WSC-007
actorInstance: developer-wsc-501
decision: DEC-WSC-006
status: READY_FOR_REVIEW
completedAt: 2026-08-03T12:19:00+08:00
r2FixedAt: 2026-08-03T12:28:00+08:00
reqs:
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-CAT-008
```

## 摘要

按 DEC-WSC-006 方案 A：产品「行业分类」改为 GB/T 4754 门类字段 `industryCategory`（中文原文枚举），不再解析为目录 L3。契约升至 `2.2.0`；导入/编辑不再强制三级级联；合法门类（如「建筑业」）不再抛 `ERR_CATEGORY_LEAF_REQUIRED`。浏览对无挂载产品按 `industryCategory` 分桶，否则「未分类」。

## R2 修复（对 REV-CODE-TASK-WSC-501-R1 REQUEST_CHANGES）

| Finding | 修复 |
|---|---|
| FIND-001 P1 | `SessionAuthIntegrationTest` admin/provider `POST /catalog/products` body 补合法 `industryCategory`（「卫生和社会工作」），去掉无效 `l2CategoryId`；顺带对齐真实维护 id / multipart 导入探测（原 stub 路径已失效，产品写通过后才会暴露） |
| FIND-002 P2 | OpenAPI 导入错误报告示例 `reasonMessage` 改为「行业分类非法，须为 GB/T 4754 门类枚举：电子病历」 |
| FIND-003 P3 | 未改（死样式，非本轮必修） |

未自行标记 VERIFIED；待独立 codeReviewer / tester。

## writeSet 变更

| 路径 | 说明 |
|---|---|
| `contracts/VERSION` / `openapi/openapi.yaml` / `errors/codes.yaml` / `req-coverage.md` | ProductWrite 必填改 `industryCategory`；`l3CategoryId` 可空；导入文档去「须解析到三级」；版本 2.2.0；R2 修正错误报告示例文案 |
| `backend/.../catalog/IndustryCategories.java` | 20 个门类枚举常量 |
| `backend/.../browse/CatalogProduct.java` | 增 `industryCategory` |
| `backend/.../editor/ProductEditorService.java` | 必填门类；L3 可选；非法显式 L3 仍 `ERR_CATEGORY_LEAF_REQUIRED` |
| `backend/.../import/ImportFieldMapper.java` | 门类枚举校验 → `industryCategory`；不再 resolveL3 |
| `backend/.../browse/CatalogBrowseService.java` | 列表/详情透出 `industryCategory` |
| `backend/.../maintenance/CatalogMaintenanceService.java` | 关联 L3 时保留 `industryCategory` |
| `backend/.../security/SessionAuthIntegrationTest.java` | R2：产品写补 `industryCategory`；维护/导入矩阵探测对齐真实 API |
| `frontend/src/api/catalog.ts` | 类型与 `INDUSTRY_CATEGORY_OPTIONS` |
| `frontend/.../editor/**` | 去掉必填三级级联；行业分类/交付/地域下拉对齐模板 |
| `frontend/.../detail/ProductDetailPage.vue` | 展示 `industryCategory` |
| `frontend/.../browse/useCatalogBrowse.ts` | 无 l3 → 按 industryCategory / 未分类分桶 |
| `tests/fixtures/import/**` | category-mismatch / full-success 等改门类语义 |
| 相关 Java/Vitest 测试 | 同步更新 |

## 浏览策略（验收 #4）

**选择：按 `industryCategory` 分桶；无门类则「未分类」。**  
有 `l3CategoryId` 的产品仍按三级子类分节（兼容既有目录树浏览）。

## REQ 映射

| REQ | 实现 |
|---|---|
| **REQ-CAT-004** | 详情可读 `industryCategory`；挂载路径改为可选展示 |
| **REQ-CAT-005** | ProductWrite 必填门类；创建/编辑不强制 L3 |
| **REQ-CAT-008** | 导入校验门类枚举；非法 → 行级 `ERR_IMPORT_ROW_INVALID` |

## 自测命令与结果

### R1

```text
cd backend/app/data-chain-service
mvn -Dtest=ProductEditorIntegrationTest,ProductImportIntegrationTest test

cd frontend
pnpm exec vitest run src/features/catalog/editor/composables/useProductEditor.spec.ts src/features/catalog/editor/ProductEditorPage.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts src/features/catalog/detail/composables/useProductDetail.spec.ts src/features/catalog/detail/ProductDetailPage.spec.ts
```

| 命令 | 结果 |
|---|---|
| ProductEditorIntegrationTest | **PASSED**（7 tests） |
| ProductImportIntegrationTest | **PASSED**（10 tests） |
| vitest editor/browse/detail | **PASSED**（35 tests） |

### R2

```text
cd backend/app/data-chain-service
mvn -Dtest=SessionAuthIntegrationTest test
```

| 命令 | 结果 |
|---|---|
| SessionAuthIntegrationTest | **PASSED**（6 tests，Failures: 0）— BUILD SUCCESS |

## denyModify 遵守

- 未改 `ai/runs/**/state.yaml`、`events.jsonl`
- 未改 `planning/approved/**`
- 未大改无关 portal / UX 布局
- 未兼任 codeReviewer/tester；未自行标记 VERIFIED
