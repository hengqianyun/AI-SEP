# 测试证据 — TASK-WSC-905

```yaml
testrunId: TESTRUN-TASK-WSC-905
taskId: TASK-WSC-905
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: tester-wsc-905
mustDifferFrom:
  - developer-wsc-905
  - code-reviewer-wsc-905
  - security-reviewer-wsc-905
basedOn:
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-905 testScope/acceptance）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-905.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-905.md（APPROVE；P0/P1=0）
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-905.md（APPROVE；P0/P1=0）
  - product/requirements/SNAP-WSC-008.md（REQ-RBAC-002、REQ-CAT-016、REQ-CAT-018）
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
securityReviewDecision: APPROVE
securityReviewP0: 0
securityReviewP1: 0
executedAt: 2026-08-17T13:22:34+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-RBAC-002, REQ-CAT-016, REQ-CAT-018]
riskTags: [rbac-breaking, data-isolation, enterprise-scope, auth-model-change]
note: |
  独立 tester-wsc-905 实跑 DEV 所列 8 套件。未改源码、审查结论、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  未把 V1.5 SessionAuthIntegrationTest（ADMIN 写 expected 403）纳入本任务必跑；未因 907 独占的 ADMIN myCatalog 结果集过滤记 905 FAIL。
  JVM shutdownHook 在 Results 之后打印 H2「Database is already closed」，不影响 Tests run / BUILD SUCCESS。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-905` 依 PLAN-WSC-8.3 §5 TASK-WSC-905 `testScope` 与 DEV 套件清单实际执行：后端 8 类共 **47** 测 **BUILD SUCCESS**（exitCode=0）。命名用例 `905-mine-enterprise-scope`（方法 `mine_enterpriseScope_sameEnterpriseVisible_crossHidden`）与 `905-provider-maintenance-403`（方法 `provider_scopeFull_403_myCatalog_200`）已执行并通过。

ADMIN `scope=myCatalog`：**实测为拦截器 HTTP 200**（`admin_scopeFullAndMyCatalog_200` 只断言 `status=200` / `code=0`，**未**断言 items 为本企业）。对照 `CatalogMaintenanceService.list`：无 `scope` 形参，ADMIN 走 `catalog.products()` 全量。与 testScope 字面「myCatalog 本企业」冲突，但 905 writeSet **不含** maintenance service（907 独占）；本 tester **不**记 905 FAIL、**不**开 `BUG-*`。本轮**未**把未跑的结果集过滤标为 PASS。

审查前提：codeReview **APPROVE（P0=0、P1=0）**；securityReview **APPROVE（P0=0、P1=0）**。本报告不代标 VERIFIED。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | 结果 | 证据 |
|---|---|---|---|---:|---|---|
| 1 | backend-unit + backend-integration | `mvn -f backend/pom.xml -pl app/data-chain-service --batch-mode "-Dtest=RbacMatrixTest,EnterpriseScopeCatalogIntegrationTest,MaintenanceScopeIntegrationTest,ListProductsFilterSortIntegrationTest,CreateByOwnershipIntegrationTest,ProductEditorIntegrationTest,ProductImportIntegrationTest,CatalogMaintenanceIntegrationTest" test` | repo root | **0** | **PASSED** — Tests run: **47**, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS（Total time 30.385 s） | `backend-905.txt`；`surefire-905-summary.txt` |

**failedCommandCount = 0**。无环境阻塞；未把未执行项记为 PASS。

环境：Java 17.0.11、Maven 3.6.3、H2 in-memory（测试自带，无外部 MySQL/Redis）。

未跑：`SessionAuthIntegrationTest`（V1.5 ADMIN 写/导入 expected 403）。路径在 `**/security/**`，不在 905 writeSet；本轮不作为 905 必跑失败。

## 后端分套件（Surefire）

| 类 | tests | failures | errors | skipped | 结果 |
|---|---:|---:|---:|---:|---|
| `RbacMatrixTest` | 9 | 0 | 0 | 0 | PASS |
| `EnterpriseScopeCatalogIntegrationTest` | 5 | 0 | 0 | 0 | PASS |
| `MaintenanceScopeIntegrationTest` | 3 | 0 | 0 | 0 | PASS |
| `ListProductsFilterSortIntegrationTest` | 2 | 0 | 0 | 0 | PASS |
| `CreateByOwnershipIntegrationTest` | 3 | 0 | 0 | 0 | PASS |
| `ProductEditorIntegrationTest` | 8 | 0 | 0 | 0 | PASS |
| `ProductImportIntegrationTest` | 11 | 0 | 0 | 0 | PASS |
| `CatalogMaintenanceIntegrationTest` | 6 | 0 | 0 | 0 | PASS |
| **合计** | **47** | **0** | **0** | **0** | **PASS** |

### 命名用例（§0.2）

| DisplayName | Surefire 方法 | 结果 |
|---|---|---|
| **905-mine-enterprise-scope** | `mine_enterpriseScope_sameEnterpriseVisible_crossHidden` | **PASS**（time=0.395s；无 failure/error 元素） |
| **905-provider-maintenance-403** | `provider_scopeFull_403_myCatalog_200` | **PASS**（time=0.086s；无 failure/error 元素） |

源码 `@DisplayName` 与本轮方法执行对应。Surefire XML 记录方法名而非 DisplayName。

### EnterpriseScopeCatalogIntegrationTest 明细

| 方法 | testScope 映射 | 结果 |
|---|---|---|
| `mine_enterpriseScope_sameEnterpriseVisible_crossHidden` | 命名用例；ADMIN/PROVIDER `mine=true` 本企业（同企异 create_by 可见；跨企不可见） | **PASS** |
| `user_mineWriteImportMaintenance_403` | USER：mine / 产品写 / 导入 / maintenance → 403 | **PASS** |
| `fullChain_crossEnterpriseParity_andIgnoresClientEnterpriseQuery` | 全链两 enterprise 同一 query 结果集相同；篡改 `enterpriseId` 不扩大 mine | **PASS** |
| `admin_writeOwnEnterprise200_crossAndEmpty403_providerForeign403` | ADMIN 本企业写 200（含 query `enterpriseId=999`）；跨企 403；空 create_by 403；PROVIDER 他人 403 | **PASS** |
| `query_hasNoEnterpriseNameFilter` | query 无 `enterpriseName` 过滤副作用 | **PASS** |

### MaintenanceScopeIntegrationTest 明细

| 方法 | testScope 映射 | 结果 |
|---|---|---|
| `provider_scopeFull_403_myCatalog_200` | PROVIDER `scope=full` GET/PUT **403**；`myCatalog` **200** 且 `hasItem` 本人编码 | **PASS** |
| `admin_scopeFullAndMyCatalog_200` | ADMIN 双 scope **HTTP 200**（**未**断言结果集本企业） | **PASS（门禁）** |
| `user_anyScope_403` | USER `full` / `myCatalog` → 403 | **PASS** |

### 其余套件（覆盖 / 回归）

| 类 / 方法 | 映射 | 结果 |
|---|---|---|
| `RbacMatrixTest.catalogMaintenance_scopeAware_matchesSection31` 等 9 | `RbacMatrix` 与 §3.1：ADMIN 写/导入/mine true；USER false；PROVIDER full 维护 false | **PASS** |
| `ListProductsFilterSortIntegrationTest.supplierName_fuzzyContains_separateFromQ` | supplierName 模糊、与 `q` 分离 | **PASS** |
| `ListProductsFilterSortIntegrationTest.sort_uncategorizedLast_updatedAtDesc_crossPage` | 未分类殿后跨页 | **PASS** |
| `CreateByOwnershipIntegrationTest.provider_updateOwn_200_emptyAndOther_403` | PROVIDER 本人写 200；空 create_by / 他人 403 | **PASS** |
| `CreateByOwnershipIntegrationTest.admin_productWriteAllowed_user_productWriteForbidden` | ADMIN 写 200；USER 写 403 | **PASS** |
| `ProductImportIntegrationTest.admin_importOwnEnterprise_200` | ADMIN 导入 200；query `enterpriseId=999999` 不扩大 | **PASS** |
| `ProductImportIntegrationTest.ordinaryUser_importForbidden` | USER 导入 403 | **PASS** |
| `ProductEditorIntegrationTest.ordinaryUser_productWriteForbidden` | USER 产品写 403 | **PASS** |
| `CatalogMaintenanceIntegrationTest.user_forbidden_maintenance` | USER maintenance 403 | **PASS** |
| `CatalogMaintenanceIntegrationTest.provider_listOnlyOwn_and_associateOwn200_other403` | PROVIDER 列表本人；挂载他人 403 | **PASS** |
| 其余 editor/import/maintenance 回归 | 非 905 增量，本轮未回退 | **PASS** |

## testScope 对照（PLAN-WSC-8.3 §5 TASK-WSC-905）

| # | testScope / 覆盖要求 | 实跑？ | 结果 | 证据 |
|---|---|---|---|---|
| 1 | USER：mine / 产品写 / 导入 / maintenance → 403 | **是** | **PASS** | `user_mineWriteImportMaintenance_403`；`user_anyScope_403`；`ordinaryUser_productWriteForbidden`；`ordinaryUser_importForbidden`；`user_forbidden_maintenance` |
| 2 | PROVIDER：他人 create_by 写 403 | **是** | **PASS** | `admin_writeOwnEnterprise200_crossAndEmpty403_providerForeign403`；`provider_updateOwn_200_emptyAndOther_403` |
| 3 | PROVIDER：`scope=full` maintenance 403 | **是** | **PASS** | `905-provider-maintenance-403` GET+PUT |
| 4 | PROVIDER：myCatalog 200 仅本人 | **是** | **PASS（正例）** | `905-provider-maintenance-403` `hasItem` 本人；`provider_listOnlyOwn_and_associateOwn200_other403` 含异主负例。`905-provider-maintenance-403` 本方法未插入他人条目（CR FIND-WSC-905-002 P2），不升格本轮 FAIL |
| 5 | ADMIN：本企业写 200；跨企业写 403 | **是** | **PASS** | `admin_writeOwnEnterprise200_crossAndEmpty403_providerForeign403`；`admin_productWriteAllowed_user_productWriteForbidden` |
| 6 | ADMIN：full maintenance 200 全平台 | **是** | **PASS（HTTP 200）** | `admin_scopeFullAndMyCatalog_200` 对 `scope=full` 断言 200。本套件**未**用跨企 seed 断言「全平台 N 条」条数；service 对 ADMIN 仍 `catalog.products()` |
| 7 | ADMIN：myCatalog **HTTP 200** | **是** | **PASS（拦截器 200）** | `admin_scopeFullAndMyCatalog_200` |
| 8 | ADMIN：myCatalog **结果集本企业** | **部分观察，非本任务断言** | **不记 905 PASS / 不记 905 FAIL** | 见专节 |
| 9 | 全链：两 enterprise 同一 query 结果集相同 | **是** | **PASS** | `fullChain_crossEnterpriseParity_andIgnoresClientEnterpriseQuery` |
| 10 | 篡改 enterprise 参数负例 | **是** | **PASS** | mine 伪造 `enterpriseId`；写路径 `enterpriseId=999`；导入 `enterpriseId=999999` |
| 11 | 空 create_by 负例 | **是** | **PASS** | ADMIN PUT 403；PROVIDER PUT 403 |
| 12 | supplierName / 未分类跨页；query 无 `enterpriseName` | **是** | **PASS** | `ListProductsFilterSortIntegrationTest` 两例 + `query_hasNoEnterpriseNameFilter` |
| 13 | §0.2 命名用例 `905-mine-enterprise-scope`、`905-provider-maintenance-403` | **是** | **PASS** | Surefire 方法已执行 |

## ADMIN `scope=myCatalog` 结果集 — 独立实测

计划 testScope / acceptance 字面：「ADMIN `scope=myCatalog` → 本企业」。905 writeSet **不含** `CatalogMaintenanceController` / `CatalogMaintenanceService`（907 独占）。本 tester **未**采信 DEV/CR/SEC 结论代替实测。

| 观察 | 本轮证据 |
|---|---|
| 拦截器是否放行 | **是**。`admin_scopeFullAndMyCatalog_200` 对 `GET .../maintenance/entries?scope=myCatalog` 断言 HTTP **200**、`code=0`。方法已执行（0.085s，无 failure） |
| 套件是否断言结果集为本企业 | **否**。该测试无 `items` / 跨企编码负例 |
| 列表实现是否按本企业过滤 | **否（读实现，非未跑声称覆盖）**。`CatalogMaintenanceController.list` **无** `scope` 形参。`CatalogMaintenanceService.list` L72–76：`PROVIDER` → `productsOwnedBy`；**否则（含 ADMIN）`catalog.products()` 全量**，不读 `scope` |
| 与 testScope 字面 | **冲突**（字面要本企业结果集；905 实测只到拦截器 200） |
| 是否开 `BUG-*` | **否**。缺口在 907 writeSet 内；905 写集内拦截器行为未失败。CR FIND-SEC-WSC-905-002 / FIND-WSC-905 交接口径与本轮观察一致，但不替代本实测 |

**判定**：905 门禁覆盖 **HTTP 200/403**；ADMIN myCatalog **ownEnterprise 结果集** 交 TASK-WSC-907（命名用例 `907-my-catalog-scope-split`）。不得把该缺口记为 905 FAIL，也不得把未断言的结果集标为 PASS。

## acceptance 对照（执行层）

| acceptance 项 | 结果 | 本轮证据 |
|---|---|---|
| ADMIN/PROVIDER `mine=true` 本企业（同企异 create_by 可见；跨企不可见） | **PASS** | `905-mine-enterprise-scope` |
| ADMIN 本企业写/导入 200；跨企业写 403 | **PASS** | 写：`admin_writeOwnEnterprise200_*`；导入：`admin_importOwnEnterprise_200` |
| 全链无登录企业 filter；supplierName/未分类不回退 | **PASS** | `fullChain_crossEnterpriseParity_*`；`ListProductsFilterSortIntegrationTest` |
| scope 仅信 SessionPrincipal；篡改 enterprise 不扩大可见面 | **PASS** | mine 篡改负例；写/导入伪造 `enterpriseId` 仍按会话 |
| PROVIDER `scope=full` 403；`myCatalog` 200（service create_by 过滤） | **PASS** | `905-provider-maintenance-403` + `provider_listOnlyOwn_*` |
| ADMIN `scope=full` / `myCatalog` 均为 200 | **PASS（905 层：双 scope HTTP 200）** | `admin_scopeFullAndMyCatalog_200`；结果集本企业 **交 907** |
| 空 create_by：ADMIN/PROVIDER 写 403 | **PASS（编辑路径）** | ADMIN/PROVIDER PUT 负例。ADMIN myCatalog **维护**空归属随 907 |
| USER mine/写/导入/maintenance 403 | **PASS** | 见覆盖表 #1 |
| `RbacMatrix` 与 §3.1 / matrix.yaml 一致 | **PASS（API 单元格）** | `RbacMatrixTest` 9 例。UI 键归 906 |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-RBAC-002 | ADMIN 本企业写/导入；PROVIDER 不可写他人；USER 403；maintenance scope-aware 200/403 | **PASS（905 写集）** |
| REQ-CAT-016 | `mine=true` 按 create_by→enterprise_id 本企业，非 create_by-only | **PASS** |
| REQ-CAT-018 | 全链无企业 filter；supplierName/未分类不回退；无 `enterpriseName` 副作用 | **PASS** |
| REQ-CAT-017（ADMIN myCatalog 本企业结果集） | 不在 905 写集 | **未在本任务断言**；交 907 |

## 已知限制（不阻塞 PASS）

| 来源 | 说明 |
|---|---|
| FIND-WSC-905-001（P2，OPEN） | V1.5 `SessionAuthIntegrationTest` 仍期望 ADMIN 产品写/导入 403。本轮**未跑**该用例，不计入 905 FAIL |
| FIND-WSC-905-002（P2，OPEN） | `905-provider-maintenance-403` 缺「items 不含他人」负例；`CatalogMaintenanceIntegrationTest.provider_listOnlyOwn_*` 已覆盖异主。不另开 BUG-* |
| FIND-SEC-WSC-905-001（P2，OPEN） | 会话 `enterpriseId` 空串时 mine 可能退化为全链。本轮套件无该负例，未升格 |
| FIND-SEC-WSC-905-002（P2，OPEN） | ADMIN myCatalog 结果集仍全平台；Owner=907。本轮独立观察与此一致 |
| JVM shutdownHook H2 90121 | 出现于 **Results: Tests run: 47 Failures: 0** **之后**；**未**计入 Failures/Errors；**未**记为测试 FAIL |

本 tester **不**因开放 P2 另开 `BUG-*`。

## 缺陷

无新开 `BUG-*`。

## 审查前提

| 项 | 值 |
|---|---|
| codeReview | `REV-TASK-WSC-905` **APPROVE**；P0=0、P1=0 |
| securityReview | `SEC-REV-TASK-WSC-905` **APPROVE**；P0=0、P1=0 |
| 开放 P2（不阻塞） | FIND-WSC-905-001/002；FIND-SEC-WSC-905-001/002 |

## 证据红线（自检）

- [x] 实际执行 testScope（命令 + exitCode），非仅读 DEV/REV
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未把 907 结果集缺口自动记为 905 FAIL
- [x] 未把未断言的 ADMIN myCatalog 本企业结果集记为 PASS
- [x] 未改源码 / 审查结论 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-905`，与 developer / code-reviewer / security-reviewer 隔离
- [x] 未自行标任务 VERIFIED
- [x] 失败未发生，故未开 BUG-*
- [x] 未把 writeSet 外 V1.5 `SessionAuthIntegrationTest` 当作 905 必跑失败

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-905.md`
- 后端全量日志：`ai/runs/RUN-WSC-011/tests/backend-905.txt`
- Surefire 方法清单：`ai/runs/RUN-WSC-011/tests/surefire-905-summary.txt`
- 任务包：`planning/approved/PLAN-WSC-8.3.md` §5 TASK-WSC-905
- 开发 / 审查：`DEV-TASK-WSC-905.md`、`reviews/REV-TASK-WSC-905.md`、`reviews/SEC-REV-TASK-WSC-905.md`
