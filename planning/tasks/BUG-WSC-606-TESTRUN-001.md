# BUG-WSC-606-TESTRUN-001 — CatalogBrowseIntegrationTest 全量缺产品种子

```yaml
bugId: BUG-WSC-606-TESTRUN-001
taskId: TASK-WSC-606
severity: P2
status: OPEN
reportedBy: tester-wsc-606-r1
evidenceId: TESTRUN-TASK-WSC-606
relatedReqs: [DEC-WSC-006]
relatedPlan: PLAN-WSC-5.2
createdAt: 2026-08-06T16:40:00+08:00
blocksTaskGate: false
```

## 摘要

对 `CatalogBrowseIntegrationTest` **全类**执行时 **8/13 FAIL**（exitCode=1）。失败用例均依赖种子产品（如 `MED-EMR-0001` / `FIN-CRD-0003` / `MED-PG-0018`），在 `@SpringBootTest` 配置 `spring.flyway.enabled=false` + `ddl-auto=create-drop` 下仅有分类种子（`CatalogBrowseSeedStore`），**无产品种子** → `$.data.total` 为 0 或 GET 详情 404。

本任务 **acceptance #4** 对应用例 `listProducts_filterByIndustryCategory_gbtMenlei`（自建产品 + `industryCategory` 过滤）**单独跑 PASS（exitCode=0）**；V1.4 E2E 对 live 库 **15/0 PASS**。故本缺陷记为 **测试夹具/种子债务**，**不阻塞** TASK-WSC-606 本轮门禁。

## 复现

```text
cd backend
mvn -pl app/data-chain-service -am "-Dtest=CatalogBrowseIntegrationTest" test
```

| 项 | 值 |
|---|---|
| exitCode | **1** |
| Tests | 13 run / **8 Failures** / 0 Errors |
| 证据 | `ai/runs/RUN-WSC-008/tester-wsc-606-r1/02-backend.txt` |

### 失败用例（摘录）

| 用例 | 现象 |
|---|---|
| `listProducts_filterByL1AndProductType` | total expected 1 was 0 |
| `listProducts_filterByL3` | total expected 1 was 0 |
| `listProducts_filterByL2_includesChildL3Products` | total expected 1 was 0 |
| `listProducts_keywordCaseInsensitive_onNameAndCode` | total expected 1 was 0 |
| `listProducts_combinedFilters_dataSourceAndPublicData` | total expected 1 was 0 |
| `listProducts_pagination_pagePageSizeTotal_andFilterPreservedOnPage2` | total expected 20 was 0 |
| `listProducts_sortedByUpdatedAtDesc` | 无 `items[0].productCode` |
| `getProduct_previewFieldsAndThreeLevelPath` | Status expected 200 was 404 |

## closeWhen

1. 在集成测 H2 配置下补齐与断言一致的产品种子（或启用等价 fixture / `@Sql`），使 `CatalogBrowseIntegrationTest` 全类绿；**或**
2. 将依赖 Flyway/外部种子的用例改为自建数据（与 `listProducts_filterByIndustryCategory_gbtMenlei` 同模式）；并经独立 tester 复跑全类 exitCode=0。

## 备注

- 非 606 UX 业务回归：industryCategory 过滤与 E2E 场景均已绿。
- 未修改业务实现；仅开缺陷交回开发侧择机修夹具。
