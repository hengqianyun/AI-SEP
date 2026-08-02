# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-301-1
taskId: TASK-WSC-301
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
actorInstance: tester-wsc-301
contracts: wsc-contracts@2.1.0
basedOn:
  - planning/tasks/TASK-WSC-301.md
  - planning/tasks/DEV-TASK-WSC-301.md
  - planning/tasks/REV-TASK-WSC-301.md
  - planning/approved/PLAN-WSC-4.1.md
  - contracts/VERSION
  - contracts/errors/codes.yaml
  - contracts/openapi/openapi.yaml
  - contracts/ui/state-matrix.md
  - tests/fixtures/import/**
reviewDecision: APPROVE
executedAt: 2026-08-02T17:50:20+08:00
status: PASSED
```

## 结论摘要

**PASSED** — 独立 `tester-wsc-301` 按 PLAN-WSC-4.1 § TASK-WSC-301 `testScope` 实跑：`ProductImportIntegrationTest` **10** + `ProductEditorIntegrationTest` **7** 共 **17** 测 **BUILD SUCCESS**（exitCode=0）。命名 fixtures（full-success / partial-success / all-fail / legacy-template / format-invalid / openapi-shared*）均存在；`contracts/VERSION`=**2.1.0**；`ERR_IMPORT_TEMPLATE_UNSUPPORTED` 四处硬同步内容可检（codes.yaml 定义 + `importSemantics.requestLevel` + OpenAPI import 400 `templateUnsupported` example + state-matrix 态 ④）。仓内未挂载 spectral/redocly 等 OpenAPI CLI lint；以内容对照完成契约项，**未**将工具缺失记为环境阻塞 PASS。

- 审查前提：REV-TASK-WSC-301 **APPROVE**，P0/P1=0
- 集成测：H2 + MockMvc；未将 live DB / 环境阻塞记为 PASSED
- 未修改业务代码、REV、`state.yaml` / `events.jsonl`；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-004/tester-wsc-301/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| backend-import-editor-integration | `mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=ProductImportIntegrationTest,ProductEditorIntegrationTest" test` | PASSED | Tests run: 17, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS EXIT=0；Finished 2026-08-02T17:50:20+08:00 → `01-mvn-integration.txt`, `01-mvn-exit.txt`, `03-test-methods.txt` |
| fixture-existence | path probe `tests/fixtures/import/*` | PASSED | full-success / partial-success / all-fail / legacy-template / format-invalid / openapi-shared-import + openapi-shared-minimal / openapi-bad-text / v0729 xlsx → `02-fixtures-and-contract-sync.txt` |
| contract-VERSION | read `contracts/VERSION` | PASSED | `2.1.0` |
| contract-TEMPLATE_UNSUPPORTED-x4 | content grep/read | PASSED | codes.yaml（码定义 + requestLevel）+ openapi.yaml（400 example）+ state-matrix 态④；→ `02-fixtures-and-contract-sync.txt` |
| openapi-cli-lint | （仓内无 spectral/redocly/swagger-cli 脚本） | N/A → content PASS | 未伪造 CLI lint 通过；以 ImportTemplateColumns / 400 example / 新码 requestLevel 内容可检代替 |

## testScope 对照

| # | fixture / 项 | 结果 |
|---|---|---|
| 1 | `full-success` | PASSED — fixture 存在；`fullSuccess_autoCode_searchableAndChained`（success=2, failure=0；自动编码；可检索+上链；`NO_UPDATE`） |
| 2 | `partial-success` | PASSED — fixture 存在；`partialSuccess_andAllFail_rowLevel`（双计数>0 + reportId + 白名单四字段） |
| 3 | **`all-fail`**（态③） | PASSED — fixture 存在；同方法（success=0, failure=2, reportId；HTTP 200/`code=0`，非请求级 `ERR_IMPORT_*`） |
| 4 | **`legacy-template`** | PASSED — fixture 存在；`legacyTemplate_requestLevelReject_zeroWrites`（400 + `ERR_IMPORT_TEMPLATE_UNSUPPORTED` + 零写入） |
| 5 | **`format-invalid`** | PASSED — fixture 存在；`formatInvalid_notMappedToTemplateUnsupported`（`ERR_IMPORT_FORMAT_INVALID`） |
| 6 | oversize | PASSED — `oversize_requestLevelReject`（`ERR_IMPORT_FILE_TOO_LARGE`；运行时超限字节） |
| 7 | 模板 GET | PASSED — `template_matchesV0729Columns_andAuthoritativeXlsx`（列名行含 `ImportTemplateColumns`；无「产品编码」；xlsx 与 fixture 权威字节相等） |
| 8 | 接口定义坏文本 | PASSED — `openapiCell_badText_rowFail_sharedFixture_endpoints`（行失败 + reasonMessage） |
| 9 | 共享 OpenAPI 原文 | PASSED — `openapi-shared-import.csv` + `openapi-shared-minimal.yaml`；endpoints method/path/summary 断言 |
| 10 | typeSpecific round-trip | PASSED — editor：OTHER `contentDescription`；DATASET/REPORT；API endpoints+swagger；旧 `endpoint` 可读 |
| 11 | 写冲突 | PASSED — `create_api_endpointsConflict_keepsClientEndpoints`（path=`/client-path` ≡ 客户端） |
| 12 | `NO_UPDATE` | PASSED — import full-success + editor OTHER create |
| 13 | RBAC | PASSED — USER 导入 403；报告 GET 403；产品写 403 |
| 14 | 无编码列 | PASSED — full-success 自动生成编码可检索 + versions |
| 15 | 契约 lint / 四处硬同步 | PASSED — VERSION=2.1.0；四处内容可检；CLI lint 工具链未接线（见 Layers） |

## 契约硬同步核对（TEMPLATE_UNSUPPORTED）

| 落点 | 结果 |
|---|---|
| `contracts/errors/codes.yaml` 码定义 | PASS — `ERR_IMPORT_TEMPLATE_UNSUPPORTED` scope=request |
| `importSemantics.requestLevel` | PASS — 列表含该码 |
| OpenAPI `POST /catalog/products/import` 400 example | PASS — `templateUnsupported` → code=`ERR_IMPORT_TEMPLATE_UNSUPPORTED` |
| `contracts/ui/state-matrix.md` 态 ④ | PASS — 映射含该码；与 FORMAT 分界说明存在 |

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未自行宣称 VERIFIED（仅产出 TESTRUN）
- 未修改业务源码、REV 报告或 `state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-301`（≠ developer-wsc-301 / ≠ code-reviewer-wsc-301）
- OpenAPI CLI lint 未接线：以内容可检完成契约项，不伪称工具链 exit 0
