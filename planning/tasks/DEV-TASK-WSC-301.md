# DEV-TASK-WSC-301

```yaml
taskId: TASK-WSC-301
actorInstance: developer-wsc-301
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
contracts: wsc-contracts@2.1.0
status: READY_FOR_REVIEW
completedAt: 2026-08-02T17:40:30+08:00
reqs:
  - REQ-CAT-008
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-API-001
  - REQ-RBAC-001
```

## 摘要

本轮核对 writeSet：契约 `2.1.0`、v0729 导入/模板拒绝、`typeSpecific.api`（swaggerFileContent + endpoints）、编辑写冲突方案 B、fixtures 与集成测均已落盘；**未发现需补齐的明显代码缺口**（本轮无源码改动）。自测：`ProductImportIntegrationTest`（10）+ `ProductEditorIntegrationTest`（7）共 17 项 **BUILD SUCCESS**。前端 feature / e2e / state·events 未触碰。

## 变更文件列表（writeSet）

### 契约

| 路径 | REQ | 说明 |
|---|---|---|
| `contracts/VERSION` | 契约 | `2.1.0` |
| `contracts/openapi/openapi.yaml` | REQ-API-001 / CAT-* | `typeSpecific.api`；`ImportTemplateColumns` 重冻；`NO_UPDATE`；OTHER `contentDescription`；import 400 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` example |
| `contracts/errors/codes.yaml` | REQ-CAT-008 | `ERR_IMPORT_TEMPLATE_UNSUPPORTED` + `importSemantics.requestLevel` |
| `contracts/ui/state-matrix.md` | REQ-CAT-008 | 态 ④ 含 TEMPLATE_UNSUPPORTED |

### Client（`frontend/src/api/**`）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/api/catalog.ts` | 契约消费 | `TypeSpecificApi` / dataset / report·other；`NO_UPDATE`；`IMPORT_TEMPLATE_COLUMNS` v0729；导入 DTO |
| `frontend/src/api/{auth,chain,client,index,overview}.ts` | — | 与 2.1.0 client 对齐的既有包文件 |

### 后端导入（路径 `catalog/import/**`，包名 `…catalog.productimport`）

| 路径 | REQ | 说明 |
|---|---|---|
| `…/import/ImportTemplateColumns.java` | REQ-CAT-008 | v0729 列名行；废止 V1.1 最小列 |
| `…/import/ImportFileCodec.java` | REQ-CAT-008 | csv/xlsx；模板镜像 v0729 |
| `…/import/ImportFieldMapper.java` | REQ-CAT-008 / API-001 | 列→body；自动编码；typeSpecific；`NO_UPDATE` |
| `…/import/ImportOpenApiParser.java` | REQ-API-001 | 接口定义单元格 → `endpoints[]` |
| `…/import/ProductImportService.java` | REQ-CAT-008 | 文件级 TEMPLATE_UNSUPPORTED；同步四态；零异步 Job |
| `…/import/ProductImportController.java` | REQ-CAT-008 / RBAC | template / import / report |
| `…/import/ImportReportStore.java` | REQ-CAT-008 | TTL≤24h；白名单四字段 |
| `…/import/ImportMultipartConfig.java` | REQ-CAT-008 | 容器上限 / 业务 10MB |
| `…/import/package-info.java` | — | 目录/包名约定 |
| `…/resources/import/product-import-template-v0729.xlsx` | REQ-CAT-008 | 权威模板镜像 |
| `…/test/…/import/ProductImportIntegrationTest.java` | testScope | fixtures + RBAC + OpenAPI + 四态 |

### 后端编辑

| 路径 | REQ | 说明 |
|---|---|---|
| `…/editor/ProductEditorService.java` | REQ-CAT-005 / API-001 | typeSpecific 扩展；§3.3.3 冲突保留客户端 endpoints |
| `…/test/…/editor/ProductEditorIntegrationTest.java` | testScope | API/DATASET/REPORT/OTHER round-trip；冲突；旧 endpoint；`NO_UPDATE`；USER 403 |

### Fixtures（`tests/fixtures/import/**`）

| 路径 | 期望 |
|---|---|
| `full-success.csv` | 全成功；无编码列；含 `NO_UPDATE` |
| `partial-success.csv` | 混行；两计数>0 + reportId |
| `all-fail.csv` | 态③：success=0 ∧ failure>0 ∧ reportId |
| `legacy-template.csv` | V1.1 列集 → `ERR_IMPORT_TEMPLATE_UNSUPPORTED` + 零写入 |
| `format-invalid.bin` | `ERR_IMPORT_FORMAT_INVALID`（非 TEMPLATE） |
| `openapi-bad-text.csv` | 接口定义坏文本 → 行失败 |
| `openapi-shared-minimal.yaml` + `openapi-shared-import.csv` | 共享 OpenAPI；method/path/summary 可断言 |
| `product-import-template-v0729.xlsx` | 与 product/assets 及 GET 模板对齐 |
| `oversize.README.txt` 等 | 超限/分类/必填等回归辅助 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **契约 2.1.0** | VERSION；OpenAPI；codes；state-matrix；client DTO |
| **REQ-CAT-008** | v0729 解析；旧模板文件级拒绝；四态/报告/10MB/同步完成；成功行可检索上链 |
| **REQ-API-001** | `swaggerFileContent` + `endpoints[]`；导入派生；编辑冲突以客户端 endpoints 为准 |
| **REQ-CAT-004/005** | 后端读写扩展字段（详情/编辑 UI 归 302/303） |
| **REQ-RBAC-001** | USER 导入/产品写/报告 GET 403 |

## 验收点对照（PLAN-WSC-4.1 § TASK-WSC-301）

| 验收点 | 证据 |
|---|---|
| `VERSION`=2.1.0；§3.1 增量 | `contracts/VERSION` + openapi/codes/state-matrix |
| `ImportTemplateColumns` 重冻；无产品编码列 | 契约 example + `ImportTemplateColumns.COLUMNS` + GET 模板测 |
| TEMPLATE_UNSUPPORTED 四处硬同步 | codes.yaml requestLevel；openapi 400 example；state-matrix 态④ |
| OTHER 允许 `contentDescription` | openapi + editor `create_other_withContentDescription` |
| client 可消费新 DTO | `frontend/src/api/catalog.ts` TypeSpecific* / IMPORT_TEMPLATE_COLUMNS |
| 写 swagger+endpoints 可回读；旧 endpoint 可读 | editor 集成测 |
| 写冲突：endpoints ≡ 客户端 | `create_api_endpointsConflict_keepsClientEndpoints` |
| dataset/report/other 扩展读写 | editor round-trip |
| `NO_UPDATE` | 导入 full-success + editor OTHER |
| GET 模板 ≡ 权威 v0729 | `template_matchesV0729Columns_andAuthoritativeXlsx` |
| 旧模板 → TEMPLATE_UNSUPPORTED + 零写入 | `legacyTemplate_requestLevelReject_zeroWrites` |
| v0729 自动编码；坏 OpenAPI 行失败 | fullSuccess + openapiCell_badText |
| 共享 OpenAPI fixture 关键字段 | openapi-shared-*；method/path/summary 断言 |
| 四态 / 报告白名单 / RBAC / 上链 | ProductImportIntegrationTest |
| 同步完成；无异步 Job | ProductImportService 同步返回 |

## 自测命令与结果

```text
mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=ProductImportIntegrationTest,ProductEditorIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `ProductEditorIntegrationTest` | 7 | **PASSED** |
| `ProductImportIntegrationTest` | 10 | **PASSED** |
| 合计 | 17 | **BUILD SUCCESS**（Failures/Errors/Skipped=0） |

原始日志：`ai/runs/RUN-WSC-004/mvn-import-301-dev.txt`（Finished at 2026-08-02T17:39:32+08:00）。

本轮未跑前端 typecheck / vitest（feature UI 归 302–304；api 包已含 2.1.0 DTO）。

## SCOPE / denyModify

- **未修改** `frontend/src/features/**`、`tests/e2e/**`、`ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`
- **未修改** `frontend/src/components/**`、chain 实现包、`product/**`（除只读对照权威 xlsx）
- 本 DEV 报告按派发要求写入 `planning/tasks/DEV-TASK-WSC-301.md`
- 未兼任 codeReviewer；未自行标记 VERIFIED

## 已知边界 / 后续

- 前端编辑 OpenAPI UI、详情只读、导入弹窗文案/E2E 分别归 **TASK-WSC-302 / 303 / 304**。
- 共享 fixture `openapi-shared-minimal.yaml` 供 302 对照 method/path/summary；默认无允许差异清单。
- 既有 `code-conflict.csv` 仍为 V1.1 列语义残留文件；v0729 无编码列后冲突场景由自动生成编码覆盖，本任务 testScope 未再强制该 fixture。
