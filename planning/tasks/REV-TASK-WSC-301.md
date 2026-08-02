# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-301-R1
taskId: TASK-WSC-301
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-301
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
contracts: wsc-contracts@2.1.0
reviewedAt: 2026-08-02T17:46:00+08:00
basedOn:
  - planning/tasks/DEV-TASK-WSC-301.md
  - planning/approved/PLAN-WSC-4.1.md
  - product/requirements/SNAP-WSC-004.md
  - contracts/VERSION
  - contracts/openapi/openapi.yaml
  - contracts/errors/codes.yaml
  - contracts/ui/state-matrix.md
  - frontend/src/api/catalog.ts
  - backend/.../catalog/import/**
  - backend/.../catalog/editor/ProductEditorService.java
  - backend/.../test/.../ProductImportIntegrationTest.java
  - backend/.../test/.../ProductEditorIntegrationTest.java
  - tests/fixtures/import/**
mustDifferFrom: developer-wsc-301
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。`wsc-contracts@2.1.0` 已冻结（VERSION / OpenAPI `ImportTemplateColumns`+`typeSpecific.api` / `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 四处硬同步 / `NO_UPDATE` / OTHER `contentDescription`）；client `frontend/src/api/**` 可消费新 DTO；后端 v0729 导入（文件级旧模板拒绝、自动编码、OpenAPI 单元格派生、四态/报告/RBAC）与编辑侧方案 B 写冲突（客户端 `endpoints` 为准）均有实现与集成测支撑。权威 / fixture / classpath 三份 `product-import-template-v0729.xlsx` SHA256 一致。审查复跑 `ProductImportIntegrationTest`（10）+ `ProductEditorIntegrationTest`（7）共 17 项 **BUILD SUCCESS**。正式 VERIFIED 仍须独立 tester 执行完整 `testScope`（含契约 lint 等）。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-301-R1-001 | P2 | 工作区另有 `frontend/src/features/**`、`tests/e2e/**` 脏改动，以及 writeSet 外 `D .../security/StubWriteController.java`。DEV-301 声明未触碰 features/e2e/state·events；本审查将 301 交付面限定为 contracts / `frontend/src/api/**` / `catalog/import/**` / `catalog/editor/**` / fixtures / 对称测试。StubWrite 删除疑似既有导入落地残留，非本报告认定的 301 范围越界 | 编排/提交时按任务 writeSet 隔离暂存区；勿将 prior-run 脏文件并入 301 提交；若本 Run 确需删 StubWrite，应在后续允许改 security 的说明中补记 | PLAN-WSC-4.1 § TASK-WSC-301 writeSet/denyModify |
| FIND-WSC-301-R1-002 | P2 | §3.3.1 禁止「非空原文成功行仅存 swagger 而空 endpoints」。`ImportOpenApiParser` 对含 `openapi`/`swagger` 但无/空 `paths` 的文本返回空列表且导入可成功（非 `ParseException`） | 非空原文且派生 `endpoints` 为空时改为行失败，或在计划/交付说明显式允许「空 paths」边界并补测 | REQ-API-001, PLAN-WSC-4.1 §3.3.1 |
| FIND-WSC-301-R1-003 | P3 | 物理目录 `catalog/import/`，Java 包名 `productimport`（关键字规避；`package-info` 已说明） | 可选对齐目录/包名或固化模块约定 | — |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| 301 交付面 ⊆ writeSet（contracts、`frontend/src/api/**`、`catalog/import/**`、`catalog/editor/**`、resources/import、对称 test、`tests/fixtures/import/**`） | PASS |
| 未改 `frontend/src/features/**`、`frontend/src/components/**`、`tests/e2e/**`、browse/admin/maintenance/overview/chain 实现、`**/sql/migration/**` 新脚本（本任务交付面） | PASS（见 FIND-001 工作区脏文件隔离说明） |
| 未改 `ai/runs/**/state.yaml` / `events.jsonl`（本审查未写） | PASS |
| `mustDifferFrom: developer-wsc-301` | PASS（本实例 `code-reviewer-wsc-301`） |

### 2. 契约重冻（2.1.0）

| 项 | 证据 | 结果 |
|---|---|---|
| `VERSION`=2.1.0 | `contracts/VERSION` | PASS |
| `ImportTemplateColumns` 按 v0729 列名行重冻；废止 V1.1 最小列；无产品编码列 | OpenAPI schema example + Java `ImportTemplateColumns.COLUMNS` + client `IMPORT_TEMPLATE_COLUMNS` | PASS |
| `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 四处硬同步 | `codes.yaml` + `importSemantics.requestLevel` + OpenAPI import 400 example + `state-matrix` 态 ④ | PASS |
| `typeSpecific.api`：`swaggerFileContent` + `endpoints[]`；兼容旧 `endpoint` | OpenAPI `TypeSpecificApi` + editor/import 实现 | PASS |
| OTHER 允许 `contentDescription`；无「必须空对象」 | OpenAPI 描述 + editor `create_other_withContentDescription` | PASS |
| `UpdateFrequency` 含 `NO_UPDATE` | OpenAPI enum + client + 导入/编辑测 | PASS |

### 3. TEMPLATE_UNSUPPORTED / v0729 解析

| 项 | 证据 | 结果 |
|---|---|---|
| 旧模板（V1.1 列集）→ 请求级 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` + 零写入 | `legacy-template.csv` + `legacyTemplate_requestLevelReject_zeroWrites` | PASS |
| FORMAT ≠ TEMPLATE | `format-invalid.bin` → `ERR_IMPORT_FORMAT_INVALID` | PASS |
| GET 模板 ≡ 权威 v0729 | classpath / fixtures / `product/assets` 三份 xlsx **同一 SHA256**；集成测字节相等 + CSV 列名断言 | PASS |
| 无编码列自动生成；成功行可检索上链 | `fullSuccess_autoCode_searchableAndChained` | PASS |
| 接口定义坏文本 → 行失败 | `openapiCell_badText_rowFail_sharedFixture_endpoints` | PASS |
| 共享 OpenAPI：method/path/summary 可断言 | `openapi-shared-minimal.yaml` + import 测 POST `/enterprise/security/verify` /「企业安全信息核验」 | PASS |
| 异型非空列 → 行失败 | `ImportFieldMapper.rejectHeterogeneousColumns` | PASS |
| 同步完成；无异步 Job | `ProductImportService.importFile` 同步返回 | PASS |

### 4. typeSpecific.api 写冲突（方案 B）

| 项 | 证据 | 结果 |
|---|---|---|
| 产品写：二者均非空且不一致 → 落库 endpoints ≡ 客户端；不静默重算 | `normalizeApiSection` 原样保留客户端 endpoints；`create_api_endpointsConflict_keepsClientEndpoints`（path=/client-path，swagger 含 `/from-swagger`） | PASS |
| 导入例外：始终从单元格派生 | `ImportFieldMapper` + `ImportOpenApiParser`；共享 fixture 测 | PASS |
| dataset/report/other 扩展读写 | editor round-trip（含 OTHER/REPORT `contentDescription`） | PASS |
| 旧仅 `endpoint` 可读 | `create_dataset_report_roundTrip_andLegacyEndpointRead` | PASS |

### 5. testScope 覆盖（支撑验收，不代替 tester）

| fixture / 项 | 覆盖 | 结果 |
|---|---|---|
| full-success | `fullSuccess_autoCode_searchableAndChained` | PASS |
| partial-success | `partialSuccess_andAllFail_rowLevel` | PASS |
| all-fail（态③） | 同上（success=0, failure=2, reportId） | PASS |
| legacy-template | `legacyTemplate_requestLevelReject_zeroWrites` | PASS |
| format-invalid | `formatInvalid_notMappedToTemplateUnsupported` | PASS |
| oversize | `oversize_requestLevelReject` | PASS |
| 模板 GET | `template_matchesV0729Columns_andAuthoritativeXlsx` | PASS |
| 坏 OpenAPI 文本 | `openapiCell_badText_*` | PASS |
| 共享 OpenAPI 原文 | 同上 + `openapi-shared-*` | PASS |
| typeSpecific round-trip + 写冲突 + NO_UPDATE | ProductEditorIntegrationTest | PASS |
| RBAC USER 403（导入/产品写/报告 GET） | import + editor 集成测 | PASS |
| 契约 lint | 本审查做内容硬同步核对；未单独跑 lint 工具 | DEFER → tester |

### 6. 架构 / 安全

| 项 | 结果 |
|---|---|
| Controller → Service → Codec/Mapper/Parser/ReportStore；成功行经 `ProductEditorService`；chain 仅端口调用 | PASS |
| 报告白名单四字段；TTL 存贮；请求级错误无行级 envelope 伪装 | PASS |
| 无本任务密钥入仓；无无故新增 Flyway 迁移 | PASS |

## 复跑测试摘要

| 命令 | 结果 |
|---|---|
| `mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=ProductImportIntegrationTest,ProductEditorIntegrationTest" test` | **PASSED** — Tests run: 17, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS（Finished at 2026-08-02T17:44:33+08:00） |

与 DEV 日志 `ai/runs/RUN-WSC-004/mvn-import-301-dev.txt`（17/0/0/0，SUCCESS）一致。

## 残余风险（交 tester）

- 前端编辑 OpenAPI UI / 详情只读 / 导入弹窗 E2E 归 **302/303/304**；本任务不宣称 UI 验收。
- 共享 fixture 关键三字段一致性的前端对照由 302 验收；301 仅保证导入路径断言。
- FIND-002：空 `paths` OpenAPI 边缘数据洁净。
- 契约自动化 lint / OpenAPI 校验工具链由 tester `testScope` 补跑。

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`。
- 未兼任 developer；未调度 tester；未代替 tester 宣称 VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2 |
| P3 | 1 |
