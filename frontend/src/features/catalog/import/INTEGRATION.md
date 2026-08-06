# 目录页挂载说明（TASK-WSC-107 / FIX1；V1.3 TASK-WSC-304）

`CatalogBrowsePage` 已通过 `useCatalogImportEntry` 挂载 `ImportDialog`（BUG-WSC-E2E-001 FIX1）。

## 组件

- 组件：`frontend/src/features/catalog/import/ImportDialog.vue`（browse 直接导入此文件，勿经 barrel 再导出 composable）
- 逻辑：`composables/useProductImport.ts` + `composables/importGuards.ts`
- 入口：`import/index.ts` **仅** re-export `ImportDialog`（BUG-WSC-304-004：避免 Vite 空模块）
- 浏览挂载：`frontend/src/features/catalog/browse/composables/useCatalogImportEntry.ts`
- **宿主（V1.4）**：仅「我的数据产品」(`/my-products`)；关闭/成功 → `/my-products`

- 权限：`productImportVisible`（仅 PROVIDER；ADMIN/USER 结构不可达，V1.4）

## 约定

1. `?import=1` + PROVIDER → 打开弹窗（`#import-modal`）
2. 关闭 `@closed` → 清除 `import` query，回到 **`/my-products`**（UX-002 / state-matrix 2.2.0）
3. 页头「批量导入产品」按钮（权限内）亦可打开
4. 错误报告链调用 `downloadErrorReport()`（对齐 OpenAPI `getImportErrorReport`，CSV 下载）
5. 模板下载为 **v0729**（`data-testid=import-template-xlsx`）；旧模板 → 态④ + `ERR_IMPORT_TEMPLATE_UNSUPPORTED`
6. 四态：`all_success` / `partial_success` / `all_row_failure` / `file_rejected`

E2E 规格：`tests/e2e/specs/p0-wsc-v1.3.spec.ts`（evidenceId `TESTRUN-WSC-E2E-V13`；正式门禁由独立 tester）。
