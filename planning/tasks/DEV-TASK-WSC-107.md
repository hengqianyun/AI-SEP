# DEV-TASK-WSC-107

```yaml
taskId: TASK-WSC-107
actorInstance: developer-wsc-107
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T22:28:00+08:00
reqs:
  - REQ-CAT-008
```

## 摘要

在 `backend/app/data-chain-service`（物理目录 `catalog/import/**`，Java 包 `com.shdata.datachain.catalog.productimport`，因 `import` 为关键字）与 `frontend/src/features/catalog/import/**`、`tests/fixtures/import/**` 落地 REQ-CAT-008：xlsx/csv 同步导入、模板下载、≤10MB 请求级拒绝、§3.6 四态、部分成功计数+错误报告、临时文件清理、报告白名单与 GET 鉴权、成功行可检索并经 `ProductEditorService` 上链。未改 `contracts/**`、`frontend/src/api/**`、browse/editor/admin/maintenance/chain 实现目录。

## 变更文件列表

### 后端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `.../catalog/import/ProductImportController.java` | REQ-CAT-008 | template / POST import / GET report |
| `.../catalog/import/ProductImportService.java` | REQ-CAT-008 | 同步导入；10MB；行级结果；临时文件清理钩子 |
| `.../catalog/import/ImportFieldMapper.java` | REQ-CAT-008 | 列映射；L3 解析；缺三级 → `ERR_CATEGORY_LEAF_REQUIRED` |
| `.../catalog/import/ImportFileCodec.java` | REQ-CAT-008 | csv/xlsx 解析与模板写出（无 POI） |
| `.../catalog/import/ImportReportStore.java` | REQ-CAT-008 | TTL≤24h；白名单序列化 |
| `.../catalog/import/ImportTemplateColumns.java` | REQ-CAT-008 | ISSUE-PA-R1-004 最小列集 |
| `.../catalog/import/ImportMultipartConfig.java` | REQ-CAT-008 | 容器上限 12MB，业务层判 10MB |
| `.../catalog/import/package-info.java` | — | 目录/包名约定；DI 只读调用 editor |
| `.../test/.../catalog/import/ProductImportIntegrationTest.java` | REQ-CAT-008 | fixtures + 权限 + 上链 + 白名单 |

### 前端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/.../import/ImportDialog.vue` | REQ-CAT-008 | `#import-modal` 弹窗；四态 UI；权限门控 |
| `frontend/.../import/composables/useProductImport.ts` | REQ-CAT-008 | 四态分类；客户端超限；报告拉取；关闭回调 |
| `frontend/.../import/composables/useProductImport.spec.ts` | REQ-CAT-008 | 四态/超限/部分成功/关闭回目录表面 |
| `frontend/.../import/index.ts` | — | 导出 |
| `frontend/.../import/INTEGRATION.md` | ISSUE-UX-R1-002 | browse deny 下的挂载说明 |

### Fixtures

| 路径 | 期望 |
|---|---|
| `tests/fixtures/import/full-success.csv` | failureCount=0；可无 reportId |
| `tests/fixtures/import/partial-success.csv` | ≥1 成功且 ≥1 失败；reportId |
| `tests/fixtures/import/code-conflict.csv` | 编码冲突行级失败 |
| `tests/fixtures/import/category-mismatch.csv` | 缺三级/非 L3 → 行级失败 |
| `tests/fixtures/import/required-field-missing.csv` | 必填缺失可识别 |
| `tests/fixtures/import/oversize.README.txt` | 运行时 >10MB；`ERR_IMPORT_FILE_TOO_LARGE` |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-008** | 模板列齐全；≤10MB 请求级拒绝；同步 `ImportResult`；部分成功计数+报告；成功行经 editor 写入并可检索；存证字段经 chain API 可读（DEC-001/002 由 editor 端口保证）；报告 GET：管理员/提供方 200，普通用户/未认证 403/401 |

## 验收点对照

| 验收点 | 证据 |
|---|---|
| §4 权限 | 集成测：普通用户 POST import 403；USER/未认证 GET report 403/401；ADMIN/PROVIDER 可导入 |
| 弹窗入口（browse deny） | `ImportDialog.vue` + `INTEGRATION.md`；关闭触发 `onClosed`（单测）；壳层已有 `/catalog?import=1` 约定 |
| ≤10MB | 业务层 `MAX_BYTES`；集成测 oversize → `ERR_IMPORT_FILE_TOO_LARGE`；前端客户端预检 |
| 同步四态 | `classifyImportResult` + UI `data-result-state`；单测覆盖 ①②③④ |
| 部分成功计数+报告 | `partialSuccess_reportDownloadable_whitelistOnly` |
| 临时文件清理 | `tempFilesCleanedCount` 钩子断言（集成测 fullSuccess） |
| 报告字段白名单 | 行仅 `rowNumber/productCode/reasonCode/reasonMessage` |
| 报告 GET 403 | `reportGet_forbiddenForUserAndUnauthenticated` |
| 成功行可检索上链 | `fullSuccess_searchableAndChained`（q 检索 + versions + snapshot） |

## 已知边界 / 后续

- **browse 挂载**：本任务 denyModify `catalog/browse/**`，目录页未直接改写；需后续可写 browse 的任务按 `INTEGRATION.md` 挂载 `ImportDialog`（关闭清除 `?import=1`）。
- 通过 DI 只读调用 `ProductEditorService` / 既有链端口；**未修改** editor/chain/browse 实现。
- `oversize` fixture 由测试运行时生成字节数组（见 README），不入库超大文件。

## 自测命令与结果

### 后端

```text
mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.productimport.ProductImportIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `ProductImportIntegrationTest` | 8 | **PASSED**（Failures/Errors/Skipped=0） |
| 合计 | 8 | **BUILD SUCCESS** |

覆盖：模板列；全成功可检索+上链+临时文件清理；部分成功报告白名单；编码冲突/分类不匹配/必填缺失；超限与非法格式请求级拒绝；报告 GET 403/401；普通用户导入 403。

### 前端

```text
cd frontend
pnpm exec vitest run src/features/catalog/import/composables/useProductImport.spec.ts
```

| 命令 | 结果 |
|---|---|
| vitest（useProductImport） | **PASSED**（6 tests） |

覆盖：四态分类；关闭回 idle/`onClosed`；客户端超限 `file_rejected`；部分成功双计数+报告入口；服务端文件级拒绝；模板列最小集。

## SCOPE / denyModify

- 未修改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`
- **未触碰** browse / detail / editor / admin / maintenance / chain 实现目录
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer；未自行标记 VERIFIED
