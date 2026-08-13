# 测试证据

```yaml
testrunId: TESTRUN-TASK-WSC-704
taskId: TASK-WSC-704
planId: PLAN-WSC-6.2
actorInstance: tester-wsc-704-r1
decision: PASS
commands:
  - id: C1
    command: 'mvn "-Dtest=ListProductsFilterSortIntegrationTest,RbacMatrixTest" test'
    cwd: backend/app/data-chain-service
    exitCode: 0
    result: PASS
    note: "Tests run: 10 (ListProductsFilterSort 4 + RbacMatrix 6), Failures: 0, Errors: 0"
  - id: C2
    command: 'mvn "-Dtest=com.shdata.datachain.model.CatalogBrowseIntegrationTest" test'
    cwd: backend/app/data-chain-service
    exitCode: 1
    result: DOCUMENTED_NON_BLOCKING
    note: "13 run / 8 fail：依赖已移除的演示产品种子（MED-EMR-0001 等）；H2 仅 ensureSeedCategoriesIfEmpty，无产品。非 704 filter/sort 逻辑回归；testScope 主证据由 C1 覆盖"
summary: "acceptance 主项全部由 ListProductsFilterSort+RbacMatrix 绿测覆盖；CatalogBrowseIntegrationTest 夹具陈旧记非阻塞；REV 开放 P2×2/P3×1 已记录不据此 FAIL"
reviewRef: planning/tasks/REV-TASK-WSC-704.md
reviewDecision: APPROVE
reviewRound: 1
reviewP0: 0
reviewP1: 0
reviewOpenP2: 2
reviewOpenP3: 1
contracts: wsc-contracts@2.3.0（只读消费）
requirements: [REQ-CAT-012, REQ-RBAC-001]
executedAt: 2026-08-12T10:56:42+08:00
mustDifferFrom:
  - developer（本任务实现者）
  - code-reviewer-wsc-704-r1
basedOn:
  - ai/agents/tester.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-704 acceptance·testScope）
  - planning/tasks/REV-TASK-WSC-704.md（APPROVE；P0=0 P1=0；FIND-001/002 P2 OPEN；FIND-003 P3 OPEN）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-704.md
```

## 结论

**PASS** — 独立 `tester-wsc-704-r1` 在 REV Round 1 **APPROVE**（P0=0、P1=0）后真实执行 testScope：

| # | command | cwd | exitCode | result |
|---|---|---|---|---|
| C1 | `mvn "-Dtest=ListProductsFilterSortIntegrationTest,RbacMatrixTest" test` | `backend/app/data-chain-service` | **0** | **PASS** — 10 tests, 0 failures |
| C2 | `mvn "-Dtest=com.shdata.datachain.model.CatalogBrowseIntegrationTest" test` | 同上 | **1** | **DOCUMENTED** — 8 failures 均为空产品库（total=0 / 404）；源文件路径 `catalog/browse/` 但 `package com.shdata.datachain.model`；不单独据此 FAIL |

developer / codeReviewer 自测或审查侧冒烟 **不算** 本门禁；本轮为独立重跑。未修改生产业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。

## acceptance / testScope 勾选（证据）

| # | 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | `supplierName` 模糊命中产品字段（大小写不敏感） | **PASS** | C1 `supplierName_fuzzyHitMiss_andCombineWithQ`：命中「华康」/「华康数据科技」/大小写变体；未命中「不存在的供应商XYZ」 |
| 2 | **不**按会话 `enterpriseName` 命中（分叉负例） | **PASS** | C1 `supplierName_doesNotMatchSessionEnterpriseName`：会话「演示企业」作 `supplierName` → total=0；产品字段「华康」可命中 |
| 3 | `q` 仅名/编码；与 `supplierName` 可组合、语义分离 | **PASS** | 同 C1 首测：组合命中 + 互斥未命中夹具 |
| 4 | 排序：null/blank L3 殿后；有 L3 在前；同组 `updatedAt` desc；固定 pageSize 跨页 | **PASS** | C1 `sort_uncategorizedLast_updatedAtDesc_crossPage`（FIND-001：跨页混合页夹具偏弱，核心全量序/页1 仅有 L3 已断言，**不阻塞**） |
| 5 | `mine=true` 共用过滤+排序 | **PASS** | C1 `mineTrue_obeysSupplierNameFilterAndUncategorizedSort` |
| 6 | `RbacMatrix.canMaintainCatalog` 仅 ADMIN | **PASS** | C1 `catalogMaintenance_adminOnly_v15`：ADMIN=true；PROVIDER/USER=false |
| 7 | 既有筛选不回归 | **PASS（逻辑）/ 记录（旧套件）** | Service 仍保留 l1/l2/l3/industry/dataSource/type 等 filter 链；C1 自建夹具下 list 路径绿。C2 旧套件依赖已不存在的演示产品种子 → 非阻塞记录（见下） |

## 开放 findings（REV，不阻塞）

| id | sev | 本轮处理 |
|---|---|---|
| FIND-WSC-704-R1-001 | P2 | **记录** — 跨页「L3→未分类」混合页夹具弱；核心排序断言已绿 |
| FIND-WSC-704-R1-002 | P2 | **记录** — `classify_importPathsPrecedeProducts` 删除；非 704 acceptance |
| FIND-WSC-704-R1-003 | P3 | **记录** — `isUncategorizedL3` Javadoc `@param` 微瑕 |

## 命令输出摘录

### C1（exit 0）

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 22.143 s
       - in com.shdata.datachain.catalog.browse.ListProductsFilterSortIntegrationTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s
       - in com.shdata.datachain.rbac.RbacMatrixTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### C2（exit 1；非阻塞）

```
[ERROR] CatalogBrowseIntegrationTest.listProducts_filterByL1AndProductType:75
        JSON path "$.data.total" expected:<1> but was:<0>
（同类 7 条 + getProduct 404；H2 无 MED-EMR-0001 / FIN-CRD-0003 等演示产品）
[INFO] Tests run: 13, Failures: 8, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

根因：`CatalogBrowseSeedStore` 在 H2（Flyway off）仅补分类树，**不再**写入演示产品；C2 套件仍断言旧种子，属夹具债，非本任务新增 `supplierName`/排序/`canMaintainCatalog` 行为错误。

## 证据红线

- 未将环境阻塞记为 PASS
- 未伪造 Maven exitCode
- 未因 REV 开放 P2/P3 或 C2 陈旧夹具单独判 FAIL（按用户说明 + acceptance 主项已由 C1 覆盖）
- 未修改业务代码 / REV / `ai/runs/**/state.yaml` / `events.jsonl`
- `actorInstance=tester-wsc-704-r1` ≠ developer ≠ `code-reviewer-wsc-704-r1`
