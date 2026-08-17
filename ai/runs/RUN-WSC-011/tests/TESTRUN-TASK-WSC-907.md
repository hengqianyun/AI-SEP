# 测试证据 — TASK-WSC-907

```yaml
testrunId: TESTRUN-TASK-WSC-907
taskId: TASK-WSC-907
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
wave: D
actorInstance: tester-wsc-907
mustDifferFrom:
  - developer-wsc-907
  - developer-wsc-907-r1
  - code-reviewer-wsc-907
  - code-reviewer-wsc-907-r2
basedOn:
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-907 testScope/acceptance/writeSet/denyModify）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-907.md（actorInstance=developer-wsc-907-r1）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-907.md（R1 REQUEST_CHANGES；FIND-WSC-907-001）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-907-R2.md（R2 APPROVE；P0=0、P1=0；FIND-001 CLOSED；FIND-002 P2 OPEN）
  - product/requirements/SNAP-WSC-008.md（REQ-CAT-017、REQ-CAT-013、REQ-CAT-019）
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-17T14:38:47+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-CAT-017, REQ-CAT-013, REQ-CAT-019]
riskTags: [maintenance-reuse, scope-isolation, enum-regression]
note: |
  独立 tester-wsc-907 实跑 PLAN testScope 与 DEV 清单。未改源码、审查正文、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  本任务无 auth-model-change，未做 securityReview，YAML 不写 securityReviewDecision。
  FIND-WSC-907-002（P2，生成 PUT/POST client 无 scope）不记 907 FAIL。
  JVM shutdownHook 在 Results 之后打印 H2「Database is already closed」，不影响 Tests run / BUILD SUCCESS。
  verbose txt 控制台编码导致部分中文乱码；用例标题与 status 以 vitest-907.json 为准。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-907` 依 PLAN-WSC-8.3 §5 TASK-WSC-907 `testScope` 实际执行：前端 7 文件共 **23** 测 **全部通过**（exitCode=0）；后端 3 类共 **10** 测 **BUILD SUCCESS**（exitCode=0）。命名用例 `907-my-catalog-scope-split`（FE 双实例 + **同实例** `full → myCatalog` list/PUT/POST + UX 双入口 + BE `@DisplayName`）与 `907-industry-category-regression`（editor / import / detail 三份具名 spec）均已执行并通过。`WorkbenchLayout.spec.ts` **8/8** 仍绿（906 ADMIN 双入口 meta.title / `/my-maintenance` 未削弱）。

审查前提：R1 REQUEST_CHANGES；R2 **APPROVE（P0=0、P1=0）**。本报告不代标 VERIFIED。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | 结果 | 证据 |
|---|---|---|---|---:|---|---|
| 1 | frontend-unit（writeSet + DEV 清单） | `pnpm --dir frontend exec vitest run src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts src/features/catalog/maintenance/components/MaintenanceCategoryCascader.spec.ts src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts src/features/catalog/editor/components/IndustryCategoryField.spec.ts src/features/catalog/import/components/ImportFieldMapping.spec.ts src/features/catalog/detail/CatalogProductDetail.spec.ts src/layouts/WorkbenchLayout.spec.ts --reporter=verbose --reporter=json --outputFile=ai/runs/RUN-WSC-011/tests/vitest-907.json` | repo root | **0** | **PASSED** — Test Files **7** passed；Tests **23** passed（0 failed）；JSON `success=true` `numPassedTests=23` | `vitest-907.txt`；`vitest-907.json` |
| 2 | backend-integration（scope 结果集） | `mvn -f backend/pom.xml -pl app/data-chain-service --batch-mode "-Dtest=MyCatalogScopeSplitIntegrationTest,MaintenanceScopeIntegrationTest,CatalogMaintenanceIntegrationTest" test` | repo root | **0** | **PASSED** — Tests run: **10**, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS（Total time 28.850 s） | `backend-907.txt`；`surefire-907-summary.txt` |

**failedCommandCount = 0**（仅计 #1、#2 门禁命令）。无环境阻塞；未把未执行项记为 PASS。

环境：Vitest v2.1.9；Java 17.0.11；Maven 3.6.3；H2 in-memory（测试自带，无外部 MySQL/Redis）。

未跑：Playwright / `tests/e2e/**`（908 独占）；denyModify 内 V1.4 `CatalogBrowsePage.spec.ts` / `useProductImport.spec.ts` 本轮未观察。未把未跑 E2E 标为 PASS 替代。

## 前端分套件（Vitest 门禁）

| 文件 | tests | failures | 结果 |
|---|---:|---:|---|
| `useCatalogMaintenance.spec.ts` | 8 | 0 | PASS |
| `MaintenanceCategoryCascader.spec.ts` | 1 | 0 | PASS |
| `CatalogMaintenanceUx.spec.ts` | 3 | 0 | PASS |
| `IndustryCategoryField.spec.ts` | 1 | 0 | PASS |
| `ImportFieldMapping.spec.ts` | 1 | 0 | PASS |
| `CatalogProductDetail.spec.ts` | 1 | 0 | PASS |
| `WorkbenchLayout.spec.ts` | 8 | 0 | PASS |
| **合计** | **23** | **0** | **PASS** |

JSON：`numTotalTests=23`、`numPassedTests=23`、`numFailedTests=0`、`success=true`。

### 命名用例（§0.2）

| DisplayName / title | 文件 | 结果 |
|---|---|---|
| **907-my-catalog-scope-split: full vs myCatalog list query** | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈2.5ms） |
| **907-my-catalog-scope-split: same instance full → myCatalog list and write URLs** | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈68.5ms） |
| **907-my-catalog-scope-split: dual entry routes keep 906 meta titles and wire maintenance page** | `CatalogMaintenanceUx.spec.ts` | **PASS**（JSON `status=passed`；duration≈8.2ms） |
| **907-industry-category-regression: editor still binds INDUSTRY_CATEGORY_OPTIONS and submits industryCategory** | `IndustryCategoryField.spec.ts` | **PASS**（JSON `status=passed`；duration≈3.6ms） |
| **907-industry-category-regression: import template maps 行业分类 and reuses INDUSTRY_CATEGORY_OPTIONS** | `ImportFieldMapping.spec.ts` | **PASS**（JSON `status=passed`；duration≈3.2ms） |
| **907-industry-category-regression: detail still displays industryCategory from INDUSTRY_CATEGORY_OPTIONS enum** | `CatalogProductDetail.spec.ts` | **PASS**（JSON `status=passed`；duration≈3.5ms） |
| **907-my-catalog-scope-split**（`@DisplayName`） | `MyCatalogScopeSplitIntegrationTest.adminDualEntry_fullN_myCatalogM_providerOwnOnly` | **PASS**（Surefire tests=1 failures=0；time=0.896s；无 failure/error 元素） |

源码 `it('907-...')` / `@DisplayName("907-my-catalog-scope-split")` 与本轮 JSON / Surefire 方法执行对应。

同实例 FIND-001 closeWhen：单一 `useCatalogMaintenance(ref)`，`scope.value` 从 `full` 改为 `myCatalog` 后 list query 与 PUT `/entries/p1?scope=myCatalog`、POST `/entries/batch?scope=myCatalog` 均通过。**不是**只靠两个独立实例。

### WorkbenchLayout.spec.ts（906 双入口不得削弱）

| 用例 title | 结果 |
|---|---|
| 906-nav-my-catalog-submenu: 我的目录在 nav-catalog-submenu 内且序为全链→我的目录→我的数据产品→目录维护 | **PASS** |
| 906-provider-no-maintenance-menu: PROVIDER 无目录维护菜单；有我的目录 | **PASS** |
| §4.1 三角色菜单正例+负例：ADMIN 双入口；PROVIDER 无维护；USER 仅全链 | **PASS** |
| USER 负例：菜单不可见 + 深链结构不可达（含 /my-catalog） | **PASS** |
| 四 surface：侧栏 active 与 meta.title / 页头可区分 | **PASS** |
| 统一路由 /my-catalog；void 前 /my-maintenance 重定向且非权威路径 | **PASS** |
| 可收起组非回归：aria-expanded / aria-controls / sessionStorage / chevron | **PASS** |
| 侧栏企业信息区只读（P1）：名称/标识；无切换/编辑；无企业管理页 | **PASS** |

8/8 `status=passed`。907 未削弱 906 ADMIN 双入口 meta.title / `/my-maintenance`。

### 其余前端用例

| 文件 / 用例 | testScope 映射 | 结果 |
|---|---|---|
| `useCatalogMaintenance` loads list + status tabs | 默认 `scope=full` list | **PASS** |
| Cascader path: L1 only / L1+L2 / L3 / clear | Cascader 路径映射 | **PASS** |
| 本页全选 / 取消；筛/Tab/翻页清选择 | REQ-CAT-013 清选择 | **PASS** |
| single save associates l3 then reloads with scope | PUT `?scope=myCatalog` | **PASS** |
| 统一维护 uses selected ids and batch l3 with scope | POST `?scope=full` | **PASS** |
| surfaces ApiError on save failure | 错误反馈 | **PASS** |
| FIND-WSC-907-001: page passes reactive scope and re-runs guard | 非 `props.scope` 快照 + watch guard | **PASS** |
| Pagination / Cascader / 本页全选 / 统一维护；筛 Tab 清选择；无跨页全选 | REQ-CAT-013 UX | **PASS** |
| Cascader token mapping / allow-clear / change-on-select | Cascader 结构 | **PASS** |

## 后端分套件（Surefire）

| 类 | tests | failures | errors | skipped | 结果 |
|---|---:|---:|---:|---:|---|
| `CatalogMaintenanceIntegrationTest` | 6 | 0 | 0 | 0 | PASS |
| `MaintenanceScopeIntegrationTest` | 3 | 0 | 0 | 0 | PASS |
| `MyCatalogScopeSplitIntegrationTest` | 1 | 0 | 0 | 0 | PASS |
| **合计** | **10** | **0** | **0** | **0** | **PASS** |

### MyCatalogScopeSplitIntegrationTest 明细

| 方法 | `@DisplayName` | testScope 映射 | 结果 |
|---|---|---|---|
| `adminDualEntry_fullN_myCatalogM_providerOwnOnly` | **907-my-catalog-scope-split** | ADMIN `full` 全平台 N、`myCatalog` 本企业 M（M≤N；跨企仅 full）；PROVIDER 本人 create_by；`scope=full` 403；ADMIN myCatalog 写跨企 403 / full 200 | **PASS**（time=0.896s） |

### MaintenanceScopeIntegrationTest 明细

| 方法 | testScope 映射 | 结果 |
|---|---|---|
| `provider_scopeFull_403_myCatalog_200` | PROVIDER `scope=full` 403；`myCatalog` 200 | **PASS**（time=0.122s） |
| `admin_scopeFullAndMyCatalog_200` | ADMIN 双 scope HTTP 200 | **PASS**（time=0.136s） |
| `user_anyScope_403` | USER 任意 scope 403 | **PASS**（time=0.106s） |

### CatalogMaintenanceIntegrationTest 明细

| 方法 | 映射 | 结果 |
|---|---|---|
| `pending_to_maintained_singleAssociate` | 单条挂载回归 | **PASS** |
| `batchAssociate_and_leafRequired` | 批量 + 叶子校验 | **PASS** |
| `pagination_pagePageSizeTotal` | 分页 | **PASS** |
| `user_forbidden_maintenance` | USER maintenance 403 | **PASS** |
| `provider_listOnlyOwn_and_associateOwn200_other403` | PROVIDER 列表本人；挂载他人 403 | **PASS** |
| `provider_batch_ownOk_foreignInFailures` | PROVIDER 批量他人失败 | **PASS** |

## testScope 对照（PLAN-WSC-8.3 §5 TASK-WSC-907）

| # | testScope / 覆盖要求 | 实跑？ | 结果 | 证据 |
|---|---|---|---|---|
| 1 | Vitest：myCatalog vs full maintenance scope | **是** | **PASS** | `907-my-catalog-scope-split: full vs myCatalog list query` |
| 2 | 同实例 `full → myCatalog` 后 list 与 PUT/POST URL 均为 `myCatalog`（FIND-001 closeWhen；不得只靠两个独立实例） | **是** | **PASS** | `907-my-catalog-scope-split: same instance full → myCatalog list and write URLs` |
| 3 | Cascader / 本页全选 / 筛 Tab 翻页清选择 | **是** | **PASS** | Cascader path 用例；本页全选用例；UX Pagination/Cascader 用例 |
| 4 | Vitest：editor/import/detail industry category 具名 spec | **是** | **PASS** | 三份 `907-industry-category-regression` |
| 5 | 后端：ADMIN myCatalog **本企业**；PROVIDER 本人 create_by；跨 scope 403 | **是** | **PASS** | `MyCatalogScopeSplitIntegrationTest` + `MaintenanceScopeIntegrationTest` |
| 6 | **ADMIN 双入口 scope 分离**：固定 seed 下 full 全平台 N、myCatalog 本企业 M（M≤N；跨企仅 full） | **是** | **PASS** | `@DisplayName("907-my-catalog-scope-split")` |
| 7 | §0.2 命名用例 `907-my-catalog-scope-split`、`907-industry-category-regression` | **是** | **PASS** | 上表点名行 |
| 8 | WorkbenchLayout.spec.ts：906 ADMIN 双入口 meta.title / `/my-maintenance` 仍绿 | **是** | **PASS** | 8/8 passed |

## acceptance 对照（执行层）

| acceptance 项 | 结果 | 本轮证据 |
|---|---|---|
| ADMIN `/catalog/maintenance`（`scope=full`）：全平台产品 | **PASS** | BE N 含跨企编码 `FRN-MNT-9074` |
| ADMIN `/my-catalog`（`scope=myCatalog`）：本企业产品 | **PASS** | BE M≤N；跨企不在 myCatalog；query `enterpriseId=999` 不扩大 |
| PROVIDER `/my-catalog`：本人 create_by；无目录维护入口 | **PASS** | BE 列表仅本人 + `scope=full` 403；FE `906-provider-no-maintenance-menu` |
| UX：Pagination / Cascader / 本页全选 / 统一维护 / 筛 Tab 翻页清选择 | **PASS** | composable + UX spec |
| 编辑/导入/详情仍展示/提交 `industryCategory` + `INDUSTRY_CATEGORY_OPTIONS` | **PASS** | 三份具名 spec |
| 浏览仍无该筛项 | **未在本任务断言 browse 行为** | 本轮未改、未跑 `browse/**`；不把未跑标 PASS |
| USER 深链 `/my-catalog` 不可达 | **PASS** | WorkbenchLayout USER 负例 + UX `beforeEnter: requireDeepLinkAccess` |
| API scope 与 UI 一致 | **PASS** | 同实例 list/PUT/POST 均带当前 scope |
| 未削弱 906 ADMIN 双入口 meta.title / `/my-maintenance` | **PASS** | WorkbenchLayout 8/8 |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-CAT-017 | `/my-catalog` 复用维护 UX；ADMIN 本企业 / PROVIDER 本人；全量入口不改语义；双入口 N vs M | **PASS** |
| REQ-CAT-013 | Pagination jumper、Cascader、本页全选、统一维护、筛/Tab/翻页清选择 | **PASS** |
| REQ-CAT-019 | 编辑/导入/详情仍用行业类别枚举 | **PASS**（三份具名 spec） |

## 已知非门禁（不计入 failedCommandCount）

| 来源 | 说明 |
|---|---|
| FIND-WSC-907-002（P2，OPEN） | 生成 api PUT/POST 无 query `scope`；907 用 `apiRequest` 附 `?scope=`。本轮门禁用例断言的是 `apiRequest` URL，**不**因此 FAIL。本轮未改 `frontend/src/api/**`。 |
| denyModify 内 V1.4 `CatalogBrowsePage.spec.ts` / `useProductImport.spec.ts` | 仍可能期望 ADMIN 不可写（906 已知债）。本轮**未**跑该观察命令，**不**记 907 FAIL，也未当 907 PASS 替代。 |
| 未跑 E2E | 908 独占；不得标 PASS 替代。 |
| H2 shutdownHook `Database is already closed` | 出现在 Results / BUILD SUCCESS **之后**；Tests run: 10 Failures: 0。不记失败。 |

本 tester **不**因开放 P2 另开 `BUG-*`。

## 缺陷

无新开 `BUG-*`。

## 审查前提

| 项 | 值 |
|---|---|
| codeReview R1 | `REV-TASK-WSC-907` **REQUEST_CHANGES**；P1=1（FIND-WSC-907-001） |
| codeReview R2 | `REV-TASK-WSC-907-R2` **APPROVE**；P0=0、P1=0 |
| 已关闭 | FIND-WSC-907-001（同实例 scope 响应式；本轮 Vitest 复验 PASS） |
| 开放 P2（不阻塞） | FIND-WSC-907-002 |
| securityReview | **不适用**（无 `auth-model-change`；不因缺 SEC-REV 记 BLOCKED） |

## 证据红线（自检）

- [x] 实际执行 testScope（命令 + exitCode），非仅读 DEV/REV
- [x] 命名用例有实测结果行
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未因 FIND-WSC-907-002 记 907 FAIL
- [x] 未把未跑的 E2E / denyModify V1.4 红测当作 907 门禁
- [x] 未改源码 / 审查正文 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-907`，与 developer / code-reviewer 隔离
- [x] 未自行标任务 VERIFIED
- [x] YAML 无 `securityReviewDecision`
- [x] 门禁未失败，故未开 BUG-*

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-907.md`
- 门禁 verbose 日志：`ai/runs/RUN-WSC-011/tests/vitest-907.txt`
- 门禁 JSON：`ai/runs/RUN-WSC-011/tests/vitest-907.json`
- 后端日志：`ai/runs/RUN-WSC-011/tests/backend-907.txt`
- Surefire 摘要：`ai/runs/RUN-WSC-011/tests/surefire-907-summary.txt`
- 任务包：`planning/approved/PLAN-WSC-8.3.md` §5 TASK-WSC-907
- 开发 / 审查：`DEV-TASK-WSC-907.md`、`reviews/REV-TASK-WSC-907.md`、`reviews/REV-TASK-WSC-907-R2.md`
