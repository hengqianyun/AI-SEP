# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-107-R1
taskId: TASK-WSC-107
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-107
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T22:36:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-107.md
  - planning/tasks/DEV-TASK-WSC-107.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
  - contracts/openapi/openapi.yaml
  - contracts/rbac/matrix.yaml
  - contracts/errors/codes.yaml
mustDifferFrom: developer-wsc-107
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。交付落在 `writeSet`：`frontend/src/features/catalog/import/**`、`backend/.../catalog/import/**`（Java 包 `productimport`）与 `tests/fixtures/import/**`。对照 DEV 清单与工作区未跟踪文件，未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`、browse/editor/admin/maintenance/chain 实现目录。REQ-CAT-008：≤10MB 请求级拒绝、同步四态、部分成功+报告白名单、报告鉴权、成功行经 `ProductEditorService` 可检索上链均有实现与测试支撑。目录页未挂载弹窗因 `denyModify browse/**` 记为 **P2 边界**（非 P1 需求遗漏，见 FIND-001）。审查复跑后端 8 测与 vitest 6 测均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-107-R1-001 | P2 | `acceptance` 要求「目录页弹窗入口可达」，但 writeSet 仅 `catalog/import/**` 且 denyModify `browse/**`；`ImportDialog.vue` + `INTEGRATION.md` 已交付，`CatalogBrowsePage` 无挂载；壳层 `WriteEntryDemo` 跳转 `/catalog?import=1` 当前不会打开弹窗。ISSUE-UX-R1-002 在计划中为 P2；任务包允许本波次以组件+集成说明收口 | 后续可写 browse 的任务按 `INTEGRATION.md` 挂载 `ImportDialog`，并清除 `?import=1`；E2E 再验「入口可达」 | REQ-CAT-008, ISSUE-UX-R1-002 |
| FIND-WSC-107-R1-002 | P2 | `ImportDialog.vue` 错误报告链为 `@click.prevent` 且无下载/打开动作；报告虽在 submit 后 prefetch 到 `report` 并展示行数/过期，但 UI「下载入口」不完整 | 绑定报告拉取/下载（或打开只读面板），与 OpenAPI `getImportErrorReport` 对齐 | REQ-CAT-008, ISSUE-SEC-R1-002 |
| FIND-WSC-107-R1-003 | P2 | 计划 §3.5 / ISSUE-SEC-R1-002 字面「匿名 GET → 403」；既有 `WriteAuthorizationInterceptor` 对 `principal==null` 返回 **401**（集成测 `isUnauthorized`）；普通用户 **403** 已覆盖。访问仍被拒绝，与平台会话惯例一致 | 若 PO 坚持匿名亦 403：在允许改 rbac/会话层的任务统一口径并更新契约响应表；否则在计划勘误中注明「未认证=401 / 已认证无权限=403」 | REQ-CAT-008, ISSUE-SEC-R1-002 |
| FIND-WSC-107-R1-004 | P3 | 源码物理目录 `catalog/import/`，Java 包为 `productimport`（绕开关键字）；依赖编译器按 package 落盘，与常规 Maven 目录约定不一致 | 可选：目录改名对齐包名，或在模块 README 固化约定（`package-info` 已说明） | — |
| FIND-WSC-107-R1-005 | P3 | `getReport` 对 PROVIDER 校验 `ownerUserId`，集成测覆盖 ADMIN/PROVIDER 自有报告与 USER 403，缺「PROVIDER 读他人 reportId → 403」负例 | 补一条跨 owner 报告 GET 用例 | ISSUE-SEC-R1-002 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet`（import feature + backend catalog/import + fixtures） | PASS（仅上述路径为新增未跟踪；对照 DEV 清单） |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**` | PASS（本任务交付面；api 层既有 import 客户端只读调用） |
| 未触碰 browse / detail / editor / admin / maintenance / chain 实现目录 | PASS |
| DI 只读调用 `ProductEditorService`，未改其实现 | PASS（`ProductImportService` 构造注入；成功行 `create`） |
| `mustDifferFrom: developer-wsc-107` | PASS（本实例 `code-reviewer-wsc-107`） |

### 2. REQ-CAT-008 / DEC / 验收点

| 项 | 证据 | 结果 |
|---|---|---|
| 模板列最小集（ISSUE-PA-R1-004） | `ImportTemplateColumns`；集成测 `template_hasAllRequiredColumns`；vitest 列集 | PASS |
| ≤10MB 请求级 vs 行级 | 业务层 `MAX_BYTES`；超限 `ERR_IMPORT_FILE_TOO_LARGE` HTTP 400；行级进报告；前端客户端预检 `file_rejected` | PASS |
| 同步 ImportResult 四态 §3.6 | `classifyImportResult` + UI `data-result-state`；①②③④ 单测可分 | PASS |
| 部分成功：双计数 + reportId + 白名单 | `partialSuccess_reportDownloadable_whitelistOnly`；行键仅四字段 | PASS |
| 临时文件清理 | `finally` 删除 temp；`tempFilesCleanedCount` 钩子断言 | PASS |
| 报告 GET：USER 403；未认证拒绝 | `reportGet_forbiddenForUserAndUnauthenticated`（USER 403；未认证 401，见 FIND-003） | PASS（安全目标达成；口径差异 P2） |
| 普通用户 POST import 403 | `ordinaryUser_importForbidden`；拦截器 IMPORT 分支 | PASS |
| 成功行可检索并上链 | `fullSuccess_searchableAndChained`：browse `q` + versions + snapshot 路径 parts | PASS |
| DEC-001 编码格式/冲突 | 经 editor；`code-conflict` → `ERR_PRODUCT_CODE_CONFLICT` | PASS |
| DEC-002 上链经适配层 | DI → editor → `ChainAttestationPort`（测试 Mock）；未改 chain 实现 | PASS |
| 目录页弹窗入口可达 | 组件+INTEGRATION 交付；browse 未挂载 | BOUNDARY → FIND-001（P2） |
| riskTags: handles-pii | 报告白名单无 DID/哈希/全文；服务未打上传全文日志；含「涉及个人信息」列映射 | PASS（记录边界） |

### 3. 架构 / 安全

| 项 | 结果 |
|---|---|
| Controller → Service → ReportStore / FieldMapper / FileCodec；产品写经 editor | PASS |
| 导入/报告鉴权依赖 `WriteAuthorizationInterceptor`（IMPORT / IMPORT_REPORT）+ UI `productImportVisible` 非唯一防护 | PASS |
| 请求级错误不作部分写入伪装；行级失败用成功 envelope + counts | PASS |
| 报告 TTL≤24h；未知/过期 `ERR_IMPORT_REPORT_NOT_FOUND` | PASS |
| 无本任务密钥入仓；无 SQL/契约改动 | PASS |

### 4. 测试覆盖（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| fixtures：full / partial / conflict / category / required / oversize | PASS |
| 模板列；CHAIN 字段；报告白名单；临时文件钩子；USER/未认证报告；USER 导入 403 | PASS |
| 四态 + 关闭 onClosed + 客户端超限 | PASS — vitest |
| 审查复跑 | 后端 8 tests PASSED；vitest 6 tests PASSED |

## 复跑测试摘要

| 命令 | 结果 |
|---|---|
| `mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.productimport.ProductImportIntegrationTest" test` | **PASSED** — Tests run: 8, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS |
| `pnpm exec vitest run src/features/catalog/import/composables/useProductImport.spec.ts`（cwd: frontend） | **PASSED** — 6 tests |

## 残余风险（交 tester）

- 目录页真实入口依赖后续 browse 挂载；壳层「批量导入」目前仅改 query，弹窗不可见直至 FIND-001 关闭。
- E2E（`TESTRUN-WSC-E2E-V11`）不在本任务 developer 范围；须 101..107 均 VERIFIED 后由独立 tester 执行。
- 简易 XLSX 编解码无 POI：复杂 Excel 边缘格式可能触发 `ERR_IMPORT_FORMAT_INVALID`（请求级），属可接受边界。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 2 |
