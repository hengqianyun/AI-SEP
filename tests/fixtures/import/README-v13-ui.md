# Import fixtures — UI / E2E (TASK-WSC-304)

| 文件 | 用途 |
|---|---|
| `product-import-template-v0729.xlsx` | 权威模板字节（模板下载对照） |
| `partial-success.csv` | v0729 混行（E2E 亦可运行时生成唯一名称） |
| `full-success.csv` | v0729 全成功（推荐另跑，非强制） |
| `all-fail.csv` | 态③ 行级全失败 |
| `legacy-template.csv` | V1.1 旧表头 → `ERR_IMPORT_TEMPLATE_UNSUPPORTED` |
| `format-invalid.bin` | 非允许格式 → `ERR_IMPORT_FORMAT_INVALID` |
| `openapi-shared-minimal.yaml` | OpenAPI 编辑/详情/导入共享 fixture |

E2E 规格：`tests/e2e/specs/p0-wsc-v1.3.spec.ts`；报告：`tests/e2e/reports/p0-wsc-v1.3/`。

## Live 后端注意（BUG-WSC-304-001/002）

强制 partial-success / API 级 legacy 拒绝要求 **已部署 TASK-WSC-301** 的导入实现。  
若 `GET /catalog/products/import/template` 仍返回 V1.1 列（含「产品编码」），则场景 2 会 `all_row_failure`。  
前端 CSV 表头预检可独立拒绝 legacy（态④）；xlsx/API 直传仍依赖后端。
