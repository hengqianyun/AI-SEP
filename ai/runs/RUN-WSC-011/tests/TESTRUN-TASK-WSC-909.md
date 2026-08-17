# 测试证据 — TASK-WSC-909

```yaml
testrunId: TESTRUN-TASK-WSC-909
taskId: TASK-WSC-909
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
wave: HOTFIX
kind: HOTFIX
actorInstance: tester-wsc-909
mustDifferFrom:
  - developer-wsc-909
  - code-reviewer-wsc-909
basedOn:
  - ai/runs/RUN-WSC-011/HOTFIX-TASK-WSC-909.md（验收权威）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-909.md（只读对照，不采信为 PASS）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-909.md（APPROVE；P0=0、P1=0；P2 FIND-001/002 OPEN）
  - ai/agents/tester.md
  - ai/skills/tester/SKILL.md
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-17T16:30:18+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-CAT-009, REQ-CAT-012, REQ-CAT-015]
riskTags: [hotfix, blocksRelease, ux-count-audit]
note: |
  独立 tester-wsc-909 实跑 HOTFIX 三项门禁。未改源码、审查正文、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  本任务无 auth-model-change，未做 securityReview，YAML 不写 securityReviewDecision。
  CatalogBrowsePage.spec.ts 全文件观察红（ADMIN 不可写）属 FIND-WSC-909-001 / 906 已知债，不记 909 FAIL。
  未跑 Playwright / tests/e2e/**（908 独占）；未把未跑 E2E 标为 PASS 替代。
  JVM shutdownHook 在 Results 之后打印 H2「Database is already closed」，不影响 Tests run / BUILD SUCCESS。
  verbose txt 控制台编码导致部分中文乱码；用例标题与 status 以 vitest-909.json / Surefire XML 为准。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-909` 依 `HOTFIX-TASK-WSC-909.md` 验收与本轮门禁清单实际执行：前端座序图 + L3 count 整文件 **2** 套件 **49** 测全部通过（exitCode=0）；前端审计具名 `909-audit` **1** 测通过、16 skipped（exitCode=0）；后端 2 类共 **12** 测 **BUILD SUCCESS**（exitCode=0）。具名用例 `909-seatmap-title-center`、`902-seatmap-l1-dropdown`、`902-no-enterprise-filter`、`909-l3-count-full-total`、`l3Counts_fullTotal_independentOfPageSize`、`audit_createBy_updateBy_ignoresClientBody_andPreservesCreator`、`909-audit-createBy-updateBy` 均已实跑并通过。

审查前提：codeReview **APPROVE（P0=0、P1=0）**。本报告不代标 VERIFIED。未采信 DEV 自测为 PASS。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | 结果 | 证据 |
|---|---|---|---|---:|---|---|
| 1 | frontend-unit（座序图 + L3 count 整文件） | `pnpm --dir frontend exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts --reporter=verbose --reporter=json --outputFile=C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-011\tests\vitest-909.json` | repo root | **0** | **PASSED** — Test Files **2** passed；Tests **49** passed（0 failed）；JSON `success=true` `numPassedTests=49` | `vitest-909.txt`；`vitest-909.json` |
| 2 | frontend-unit（审计具名，勿把全文件当门禁） | `pnpm --dir frontend exec vitest run src/features/catalog/browse/CatalogBrowsePage.spec.ts -t "909-audit"` | repo root | **0** | **PASSED** — Tests **1** passed \| **16** skipped（17） | `vitest-909-audit.txt` |
| 3 | backend-integration | `mvn -f backend/pom.xml -pl app/data-chain-service --batch-mode "-Dtest=ProductEditorIntegrationTest,ListProductsFilterSortIntegrationTest" test` | repo root | **0** | **PASSED** — Tests run: **12**, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS（Total time 28.501 s） | `backend-909.txt`；`surefire-909-summary.txt` |

**failedCommandCount = 0**（仅计 #1、#2、#3 门禁命令）。无环境阻塞；未把未执行项记为 PASS。

环境：Vitest v2.1.9；Java 17.0.11；Maven 3.6.3；H2 in-memory（测试自带，无外部 MySQL/Redis）。

未跑：Playwright / `tests/e2e/**`（908 独占；hotfix denyModify 默认不改 v14/v16 spec）。未把未跑 E2E 标为 PASS 替代。

## 前端分套件（门禁 #1）

| 文件 | tests | failures | 结果 |
|---|---:|---:|---|
| `CirculationSeatMap.spec.ts` | 14 | 0 | PASS |
| `useCatalogBrowse.spec.ts` | 35 | 0 | PASS |
| **合计** | **49** | **0** | **PASS** |

JSON：`numTotalTests=49`、`numPassedTests=49`、`numFailedTests=0`、`success=true`。Start at 16:27:13；Duration 1.25s。

### 命名用例（门禁必须见到）

| title / 方法 | 文件 | 结果 |
|---|---|---|
| **909-seatmap-title-center**: h2 centered on card; L1 stays right and does not shift title | `CirculationSeatMap.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.40ms） |
| **902-seatmap-l1-dropdown**: title row + L1 Select default 全部 + l1CategoryId query | `CirculationSeatMap.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.29ms） |
| **902-no-enterprise-filter**: no enterprise UI or query params | `CirculationSeatMap.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.32ms） |
| **909-l3-count-full-total**: section count uses server totals, not loaded subset | `useCatalogBrowse.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.44ms） |
| **909-l3-count-full-total**: loadMore does not inflate section count | `useCatalogBrowse.spec.ts` | **PASS**（JSON `status=passed`；duration≈1.16ms） |

902 命名用例仍绿（布局已按 909 居中，不再断言 `space-between`）。L3 count 两条具名用例均覆盖「全集 totals」与「loadMore 不增大」。

### 门禁 #2 审计具名

| title | 文件 | 结果 |
|---|---|---|
| **909-audit-createBy-updateBy**: preview shows 创建人/操作人 readonly | `CatalogBrowsePage.spec.ts` | **PASS**（1 passed / 16 skipped；Start at 16:28:27；Duration 837ms） |

只跑 `-t "909-audit"`，未把全文件当 909 门禁。

## 后端分套件（Surefire）

| 类 | tests | failures | errors | skipped | 结果 |
|---|---:|---:|---:|---:|---|
| `ListProductsFilterSortIntegrationTest` | 3 | 0 | 0 | 0 | PASS（time=15.365s） |
| `ProductEditorIntegrationTest` | 9 | 0 | 0 | 0 | PASS（time=6.547s） |
| **合计** | **12** | **0** | **0** | **0** | **PASS** |

### 命名方法（门禁必须见到）

| 方法 | 类 | 结果 |
|---|---|---|
| **l3Counts_fullTotal_independentOfPageSize** | `ListProductsFilterSortIntegrationTest` | **PASS**（Surefire time=0.475s；无 failure/error）。日志见 `CNT-L3A-9091`…`CNT-UNC-9095` PRODUCT_SUBMIT SUCCESS |
| **audit_createBy_updateBy_ignoresClientBody_andPreservesCreator** | `ProductEditorIntegrationTest` | **PASS**（Surefire time=0.261s；无 failure/error）。日志见 `AUD-WSC-9091` 先 `userId=3` 后 `userId=1` PRODUCT_SUBMIT SUCCESS |

源码方法名与本轮 Surefire XML 对应。

### ListProductsFilterSortIntegrationTest 明细

| 方法 | 映射 | 结果 |
|---|---|---|
| `supplierName_fuzzyContains_separateFromQ` | REQ-CAT-012 提供方名称筛（回归） | **PASS**（time=0.966s） |
| `l3Counts_fullTotal_independentOfPageSize` | L3 count = 筛选全集；pageSize=2 时 L3=3、未分类=2，翻页不变 | **PASS**（time=0.475s） |
| `sort_uncategorizedLast_updatedAtDesc_crossPage` | 未分类置底排序（回归） | **PASS**（time=0.453s） |

### ProductEditorIntegrationTest 明细

| 方法 | 映射 | 结果 |
|---|---|---|
| `create_other_withContentDescription_version1` | 新建回归 | **PASS**（time=0.138s） |
| `create_api_endpointsConflict_keepsClientEndpoints` | API 端点回归 | **PASS**（time=0.136s） |
| `create_dataset_report_roundTrip_andLegacyEndpointRead` | 数据集/报告回归 | **PASS**（time=0.156s） |
| `update_incrementsVersion_oldReadable_conflictAndFormat` | 更新版本回归 | **PASS**（time=0.175s） |
| `attestationFailure_noHalfProduct_noOrphanChain` | 上链失败回滚 | **PASS**（time=0.306s） |
| `create_requiresIndustryCategory_andRejectsInvalidL3` | 行业类别/叶子校验 | **PASS**（time=0.141s） |
| `ordinaryUser_productWriteForbidden` | USER 写禁止 | **PASS**（time=0.107s） |
| `delete_own_200_foreign_403_missing_404` | 删除权限 | **PASS**（time=0.189s） |
| `audit_createBy_updateBy_ignoresClientBody_andPreservesCreator` | 伪造 body 无效；创建双方均为提供方；ADMIN 更新后 createBy 不变、updateBy 为管理员 | **PASS**（time=0.261s） |

## acceptance 对照（执行层）

| HOTFIX acceptance | 结果 | 本轮证据 |
|---|---|---|
| `/catalog` 座序图 h2「数据流通链」相对卡片水平居中；L1 下拉仍可用；无企业筛 | **PASS** | `909-seatmap-title-center` + `902-seatmap-l1-dropdown` + `902-no-enterprise-filter` |
| 各 L3（含未分类）count = 筛选全集；分页/loadMore 不增大 count | **PASS** | FE `909-l3-count-full-total`（subset + loadMore）+ BE `l3Counts_fullTotal_independentOfPageSize` |
| 写路径：创建写 create_by+update_by；后续改刷新 update_by、不改 create_by；客户端不可伪造 | **PASS** | BE `audit_createBy_updateBy_ignoresClientBody_andPreservesCreator` |
| 读路径：预览只读展示创建人、操作人 | **PASS** | FE `909-audit-createBy-updateBy` |
| 自动化：居中 / count 不截断 / 审计可断言 | **PASS** | 上表具名用例全部实跑绿 |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-CAT-009 | 座序图卡片层与标题布局（居中 hotfix 覆盖 902 左对齐） | **PASS**（`909-seatmap-title-center`；604 座序图 6 条仍绿） |
| REQ-CAT-012 | 产品列表 count / 筛选全集；提供方名称筛回归 | **PASS**（`909-l3-count-full-total` + `l3Counts_fullTotal_independentOfPageSize` + `supplierName_fuzzyContains_separateFromQ`） |
| REQ-CAT-015 | 座序图 L1 下拉仍可用；无企业筛 | **PASS**（`902-seatmap-l1-dropdown` + `902-no-enterprise-filter`） |

审计留档写读路径由 HOTFIX 第 3 项覆盖，本轮 BE/FE 具名用例均 PASS；DEV 将三项挂到上述 REQ，本报告按 DEV 列表追踪。

## 已知非门禁（不计入 failedCommandCount）

| 来源 | 说明 |
|---|---|
| FIND-WSC-909-001（P2，OPEN）/ 906 已知债 | `CatalogBrowsePage.spec.ts` 全文件仍期望 `canWriteProduct('ADMIN')===false`。本 tester **独立实跑观察**（exitCode=1；1 failed / 16 passed）。失败断言：`CatalogBrowsePage.spec.ts:17` expected false received true。具名 `909-audit-createBy-updateBy` 在全文件中仍绿。**不**记 909 FAIL、**不**开 `BUG-*`；**也未**把该观察当作 909 门禁 PASS 的替代。 |
| FIND-WSC-909-002（P2，OPEN） | DEV 自测不是独立 tester 门禁；列表 `appendAuditFields` N+1 不要求本轮返工。本轮已独立实跑门禁，**不**因此 FAIL。 |
| 未跑 E2E | 908 独占；hotfix denyModify 默认不改 v14/v16 spec。不得标 PASS 替代。 |
| H2 shutdownHook `Database is already closed` | 出现在 Results / BUILD SUCCESS **之后**；Tests run: 12 Failures: 0。不记失败。 |
| `backend-909.txt` 中 GEN-FAIL / ERR_VALIDATION 等 INFO/ERROR | 属 `ProductEditorIntegrationTest` 负例路径预期日志（如 `GEN-FAIL-9003` attestation FAILURE）；Surefire Failures=0。 |

本 tester **不**因开放 P2 另开 `BUG-*`。

### 观察命令（非门禁）

| # | 命令 | exitCode | 结果 | 证据 |
|---|---|---:|---|---|
| O1 | `pnpm --dir frontend exec vitest run src/features/catalog/browse/CatalogBrowsePage.spec.ts --reporter=verbose` | **1** | **观察失败 1 / 通过 16** — 不计入 909 `failedCommandCount` | `vitest-909-browse-observe.txt` |

## 缺陷

无新开 `BUG-*`。

## 审查前提

| 项 | 值 |
|---|---|
| codeReview | `REV-TASK-WSC-909` **APPROVE**；P0=0、P1=0 |
| 开放 P2（不阻塞） | FIND-WSC-909-001、FIND-WSC-909-002 |
| securityReview | **不适用**（无 `auth-model-change`；YAML 不写 `securityReviewDecision`；不因缺 SEC-REV 记 BLOCKED） |

## 证据红线（自检）

- [x] 实际执行门禁命令（命令 + 真实 exitCode），非仅读 DEV/REV
- [x] 具名用例有实测结果行（JSON / Surefire）
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未因 CatalogBrowsePage 全文件 ADMIN 红测记 909 FAIL
- [x] 未把未跑的 E2E 当作 909 门禁 PASS
- [x] 未改源码 / 审查正文 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-909`，与 developer-wsc-909 / code-reviewer-wsc-909 隔离
- [x] 未自行标任务 VERIFIED
- [x] YAML 无 `securityReviewDecision`
- [x] 门禁未失败，故未开 BUG-*

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-909.md`
- 门禁 #1 verbose：`ai/runs/RUN-WSC-011/tests/vitest-909.txt`
- 门禁 #1 JSON：`ai/runs/RUN-WSC-011/tests/vitest-909.json`
- 门禁 #2 verbose：`ai/runs/RUN-WSC-011/tests/vitest-909-audit.txt`
- 门禁 #3 后端日志：`ai/runs/RUN-WSC-011/tests/backend-909.txt`
- Surefire 摘要：`ai/runs/RUN-WSC-011/tests/surefire-909-summary.txt`
- 观察（非门禁）：`ai/runs/RUN-WSC-011/tests/vitest-909-browse-observe.txt`
- hotfix / 开发 / 审查：`HOTFIX-TASK-WSC-909.md`、`DEV-TASK-WSC-909.md`、`reviews/REV-TASK-WSC-909.md`
