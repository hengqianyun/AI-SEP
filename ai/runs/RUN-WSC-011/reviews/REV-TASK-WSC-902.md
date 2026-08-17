# 代码审查 — TASK-WSC-902

```yaml
reviewId: REV-TASK-WSC-902
taskId: TASK-WSC-902
planId: PLAN-WSC-8.3
runId: RUN-WSC-011
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-902
decision: APPROVE
p0: 0
p1: 0
reviewedAt: 2026-08-17T10:58:00+08:00
basedOn:
  - planning/approved/PLAN-WSC-8.3.md §5 TASK-WSC-902
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-902.md
  - product/requirements/SNAP-WSC-008.md（REQ-CAT-015 / REQ-CAT-018）
  - 实地：CirculationSeatMap.vue(+spec)、L2DistributionController/Service(+tests)
mustDifferFrom: developer-wsc-902
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。座序图 L1 下拉、全链无企业过滤、与 browse Cascader 独立 state、denyModify 边界均满足 TASK-WSC-902 验收；后端 `?l1CategoryId=` 与 `totalProducts` 同口径有集成测支撑。开放项均为 P2/P3，不阻塞进入独立 tester 门禁。本报告不代替 tester 宣称 VERIFIED。

## 验收核对

| 验收项 | 结果 | 证据 |
|---|---|---|
| 标题行 h2 左 / L1 Select 右 | **PASS** | `seat-map-title-row` + `seat-map-l1-select`；`.title-row { justify-content: space-between }` |
| 下拉含「全部」+ 全部 L1；默认「全部」= V1.5 Top5 | **PASS** | `ALL_L1_VALUE = ''`；`l1Options` 首项「全部」；空值走 `getL2Distribution()` + `.slice(0, 5)` |
| 选 L1 后卡片/图例/API `l1CategoryId` 一致 | **PASS** | `watch(selectedL1Id)` → `fetchL2Distribution`；BE `l2DistributionForL1` 按 `l1CategoryId` 过滤 |
| L1 切换 loading 不崩布局 | **PASS** | `load()` 置 `loading=true`；Select `:loading="loading"`；604 态 testid 保留 |
| **不与** browse Cascader 联动 | **PASS** | 独立 `selectedL1Id`；无 `useCatalogBrowse`/props/inject；spec 断言 decouple |
| `totalProducts` 与 L1 子集同口径（OQ-V16-003） | **PASS** | BE `filtered.size()` 含无 L2 产品；集成测 `l1CategoryId_totalProductsMatchesL1Subset` |
| 无企业筛选 UI / API 不按企业过滤 | **PASS** | FE 无 enterprise 字段/query；BE 忽略 `enterpriseId`/`enterpriseName` 参数；集成测 parity + 负例 |
| 仅公共目录挂载（604 约定） | **PASS** | spec 只读断言 `CatalogBrowsePage` / routes `showSeatMap`；902 未改 page/routes |
| hover/leave/空态/loading 不回退 V1.5 | **PASS** | 604 describe 用例仍 PASS；核心 DIM_OPACITY / hover 逻辑未删 |
| L1 Select token 映射（§1.5） | **PASS** | `ConfigProvider` + `SEAT_MAP_FILTER_THEME` + `data-seat-map-filter-theme="ant-token-mapped"` |
| denyModify 边界 | **PASS** | 902 writeSet 6 文件；`frontend/src/api/**`、`contracts/**`、CatalogBrowse* 无 902 增量 diff；并行 901 脏树与 902 隔离 |
| §0.2 命名用例 | **PASS** | `902-seatmap-l1-dropdown`、`902-no-enterprise-filter` 存在于 spec |

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-902-001 | P2 | **OPEN** | L1 过滤暂用组件内 `apiRequest('/catalog/l2-distribution?l1CategoryId=')`，未走生成 client | TASK-WSC-903 生成 `getL2Distribution` 带参 helper 并替换 | PLAN §903 |
| FIND-WSC-902-002 | P2 | **OPEN** | 快速连续切换 L1 时无 request 序号/abort，理论上后发先至可覆盖新选择 | 可选：load 增序号或 AbortController；或接受低频操作 | REQ-CAT-015 |
| FIND-WSC-902-003 | P3 | **OPEN** | Vitest cross-enterprise 为源码静态断言；行为 parity 由 BE `crossEnterpriseParity_adminAndUserSameFullChainResult` 覆盖 | Wave A 无 enterprise 会话时可接受；908 E2E 可补强 | REQ-CAT-018 |
| FIND-WSC-902-004 | P3 | **OPEN** | 902 用例以 readFileSync 源码断言为主，未 mount 验证 Select 交互 | 与 604 既有风格一致；可选补 component mount 测 | testScope |

P0/P1 已清零 → 本轮可 **APPROVE**。

## 复跑测试（审查者独立）

| 命令 | 结果 |
|---|---|
| `mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=L2DistributionServiceTest,L2DistributionIntegrationTest" test` | **PASSED** — 13 tests，BUILD SUCCESS |
| `pnpm exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts`（frontend） | **PASSED** — 1 file / 13 tests |

与 DEV 自测声明一致；审查者未宣称完整 testScope / E2E VERIFIED。

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester。
- **decision: APPROVE**（P0/P1=0；可进入独立 tester 门禁）。

## 计数

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 2 | FIND-001（903 前临时 apiRequest）、FIND-002（L1 切换竞态） |
| P3 | 2 | FIND-003、FIND-004 |
