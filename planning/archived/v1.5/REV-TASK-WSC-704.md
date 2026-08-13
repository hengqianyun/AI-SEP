# 代码审查

```yaml
reviewId: REV-TASK-WSC-704
taskId: TASK-WSC-704
planId: PLAN-WSC-6.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-704-r1
decision: APPROVE
p0: 0
p1: 0
p2: 2
p3: 1
closedFindings: []
openFindings:
  - FIND-WSC-704-R1-001
  - FIND-WSC-704-R1-002
  - FIND-WSC-704-R1-003
contracts: wsc-contracts@2.3.0（只读消费；本任务未改 contracts）
riskTags: [list-sort, search-semantics, rbac-visibility]
reviewedAt: 2026-08-12T10:52:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-704 writeSet·denyModify·acceptance·testScope）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-704.md
  - contracts/openapi/openapi.yaml listProducts + contracts/rbac/matrix.yaml（2.3.0，只读）
  - 实地：CatalogBrowseController / CatalogBrowseService / RbacMatrix
  - 实地：ListProductsFilterSortIntegrationTest（新建）、CatalogBrowseIntegrationTest、RbacMatrixTest
  - CreateByOwnershipIntegrationTest：无 diff（与 DEV 一致）
mustDifferFrom:
  - developer（本任务实现者；本实例未兼任）
spotCheck: |
  writeSet 字面：Controller/Service/RbacMatrix + CatalogBrowseIntegrationTest + 新建 ListProductsFilterSortIntegrationTest + RbacMatrixTest；CreateByOwnership 未改。
  denyModify（本任务归因）：未触 L2* Controller/Service/Test、maintenance/editor/productimport/admin、WriteAuthorizationInterceptor、sql、contracts、frontend、e2e、product、state/events。
  WT 另有 701/702/703 等脏文件，不归 704。
  supplierName：matchesSupplierName 仅 p.supplierName contains；q 仍仅名/编码；可组合。
  enterpriseName 负例：会话「演示企业」作 supplierName → total=0；产品字段「华康」可命中。
  排序：isUncategorizedL3=null/blank；Comparator 未分类殿后 + updatedAt desc；排序后 subList 分页。
  mine：ownerUserId → productsOwnedBy 后共用 filter+sort。
  RbacMatrix.canMaintainCatalog：ADMIN only（原 ADMIN||PROVIDER）；matrix.yaml PROVIDER maintenance 403 对齐。
  审查侧复跑：mvn -Dtest=ListProductsFilterSortIntegrationTest,RbacMatrixTest test → exit 0（非 tester 门禁）。
  未代 tester PASS；未标 VERIFIED；未写 state/events；未改业务代码。
```

## Round 1 结论

**APPROVE** — **P0=0，P1=0**。TASK-WSC-704（`listProducts` + `supplierName` / 未分类垫底排序 / `RbacMatrix` ADMIN-only）字面 scope 与 `denyModify` 合规；acceptance 与 testScope 源码可复核。开放 findings 为 P2×2 + P3×1，**不阻塞**进入独立 tester 门禁。

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-704-R1-001 | P2 | **OPEN** | `sort_uncategorizedLast_updatedAtDesc_crossPage` 注释写「页2 一条有 L3 + 一条未分类」，但夹具仅 2 条有 L3 + `pageSize=2`，页2 实为两条未分类。全量序与页1 仅有 L3 已断言，核心排序正确；跨页「L3→未分类」边界切片未钉死 | 可选：增 1 条有 L3 或改 pageSize，使某页同时含末条有 L3 与首条未分类；修正注释 | REQ-CAT-012；testScope 跨页边界 |
| FIND-WSC-704-R1-002 | P2 | **OPEN** | `RbacMatrixTest` 包迁至 `rbac` 时删除 `classify_importPathsPrecedeProducts`（`WriteAuthorizationInterceptor.classify` 路径优先级）；仓内无替代用例。本任务未改 Interceptor 源码，且 acceptance 仅要求 `canMaintainCatalog` ADMIN-only | 可选：将 classify 用例迁至独立 `WriteAuthorizationInterceptorTest`（不碰 denyModify 源文件） | 回归安全网；非 704 acceptance 阻塞 |
| FIND-WSC-704-R1-003 | P3 | **OPEN** | `CatalogBrowseService.isUncategorizedL3` Javadoc `@param l3CategoryId` 与形参 `CatalogProduct p` 不一致 | 修正 `@param` 为 `p` | 可维护性 |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — 5 改 + 1 新建；CreateByOwnership 未改 |
| denyModify：L2* / maintenance / editor / import / admin / Interceptor / sql / contracts / frontend / e2e / product / state·events | **PASS（704 归因）** — 本任务 diff 未写入 |
| `supplierName` 模糊命中产品字段；不按会话 `enterpriseName` | **PASS** — Service `matchesSupplierName` + 负例测试 |
| `q` 与 `supplierName` 语义分离、可组合 | **PASS** — `matchesKeyword` 不含供应商；组合命中/互斥未命中夹具 |
| 排序：null/""/blank L3 殿后；有 L3 在前；同组 `updatedAt` desc；分页后切片 | **PASS** — `defaultListComparator` + 跨页/全量序测试（边界混合页见 FIND-001） |
| `mine=true` 共用过滤+排序 | **PASS** — `mineTrue_obeysSupplierNameFilterAndUncategorizedSort` |
| `RbacMatrix.canMaintainCatalog` 仅 ADMIN | **PASS** — 实现 + `catalogMaintenance_adminOnly_v15`；对齐 matrix 2.3.0 |
| 既有筛选路径未抽空 | **PASS** — 原 filter 链保留；`CatalogBrowseIntegrationTest` 仅补次键注释 |
| 包结构 / Javadoc | **PASS（主路径）** — Controller/Service 方法有 Javadoc；FIND-003 为微瑕 |
| 开发自测 ≠ 独立 tester | **记录** — DEV 自测 + 审查复跑 exit 0，不代 tester PASS |
| 未改业务外代码 / 未写 state·events / 未标 VERIFIED | **PASS** |
| `mustDifferFrom` 实现者 | **PASS**（`code-reviewer-wsc-704-r1`） |
| P0 / P1 | **P0=0；P1=0** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester PASS。
- **decision: APPROVE**（P0=0、P1=0；开放 P2/P3 可延期）。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 2 | FIND-001 跨页边界夹具弱；FIND-002 classify 测删除 |
| P3 | 1 | FIND-003 Javadoc `@param` |
