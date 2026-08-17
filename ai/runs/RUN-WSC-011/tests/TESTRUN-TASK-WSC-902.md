# 测试证据 — TASK-WSC-902

```yaml
testrunId: TESTRUN-TASK-WSC-902
taskId: TASK-WSC-902
planId: PLAN-WSC-8.3
runId: RUN-WSC-011
actorInstance: tester-wsc-902
mustDifferFrom: [developer-wsc-902, code-reviewer-wsc-902]
basedOn:
  - planning/approved/PLAN-WSC-8.3.md §5 TASK-WSC-902 testScope
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-902.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-902.md
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-17T11:03:00+08:00
decision: PASS
exitCode: 0
requirements: [REQ-CAT-015, REQ-CAT-018]
```

## 结论摘要

**PASS** — 独立 tester-wsc-902 按 PLAN-WSC-8.3 §5 testScope 实际复跑：后端 `L2DistributionServiceTest` + `L2DistributionIntegrationTest` 共 **13** 测 **BUILD SUCCESS**（exitCode=0）；前端 `CirculationSeatMap.spec.ts` **13/13** 通过（exitCode=0）。§0.2 命名用例 `902-seatmap-l1-dropdown`、`902-no-enterprise-filter` 均在本轮 Vitest 输出中执行并通过；cross-enterprise parity 由后端 `crossEnterpriseParity_adminAndUserSameFullChainResult` 行为断言覆盖，前端 `cross-enterprise parity: full-chain fetch has no session/enterprise params` 静态断言亦通过。未修改业务源码、`state.yaml`、`events.jsonl`；未自行宣称 VERIFIED。

## 执行命令与结果

| 层级 | 命令 | exitCode | 结果 | 证据摘要 |
|---|---|---:|---|---|
| backend-unit | `mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=L2DistributionServiceTest,L2DistributionIntegrationTest" test` | 0 | **PASSED** | Tests run: 13, Failures: 0, Errors: 0, Skipped: 0; BUILD SUCCESS |
| frontend-vitest | `pnpm exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts`（cwd: `frontend`） | 0 | **PASSED** | Test Files 1 passed; Tests 13 passed |

### 后端明细（L2DistributionIntegrationTest）

| 方法 | testScope 映射 | 结果 |
|---|---|---|
| `l1CategoryId_filtersToMatchingL1Only` | `?l1CategoryId=` 过滤集成测 | PASS |
| `l1CategoryId_totalProductsMatchesL1Subset` | `totalProducts` 与 L1 子集同口径（OQ-V16-003 / ISSUE-QA-WSC8-R1-007） | PASS |
| `crossEnterpriseParity_adminAndUserSameFullChainResult` | cross-enterprise parity（ISSUE-QA-WSC8-R1-002） | PASS |
| `noEnterpriseScopeParam_responseUnchanged` | 负例：enterprise 参数不改变分布 | PASS |

### 前端明细（CirculationSeatMap.spec.ts — TASK-WSC-902 describe）

| 用例名 | testScope 映射 | 结果 |
|---|---|---|
| `902-seatmap-l1-dropdown: title row + L1 Select default 全部 + l1CategoryId query` | §0.2 命名用例 | PASS |
| `902-no-enterprise-filter: no enterprise UI or query params` | §0.2 命名用例 + 负例无 enterprise 字段 | PASS |
| `does not couple to browse Cascader state` | 不与 browse Cascader 联动 | PASS |
| `L1 switch triggers reload with loading preserved` | L1 切换 loading | PASS |
| `totalProducts uses API total for selected L1 subset` | `totalProducts` 与 L1 卡片一致 | PASS |
| `cross-enterprise parity: full-chain fetch has no session/enterprise params` | FE cross-enterprise parity（静态；行为由 BE 集成测补强） | PASS |
| `token mapping for L1 Select (§1.5)` | token 映射断言 | PASS |

604 回归 describe（6 测）亦全部 PASS，未回退 V1.5 行为。

## testScope 对照（PLAN-WSC-8.3 §5 TASK-WSC-902）

| # | 要求 | 结果 | 证据 |
|---|---|---|---|
| 1 | 后端 `?l1CategoryId=` 过滤集成测 | **PASS** | `l1CategoryId_filtersToMatchingL1Only` |
| 2 | 无 enterprise scope 参数 | **PASS** | `noEnterpriseScopeParam_responseUnchanged` + `902-no-enterprise-filter` |
| 3 | Vitest：下拉切换 / Top5 / hover / loading | **PASS** | 902 describe + 604 describe 13 测全绿 |
| 4 | Vitest：**cross-enterprise parity** | **PASS** | BE `crossEnterpriseParity_*` + FE 静态 parity 用例 |
| 5 | Vitest：`totalProducts` 与 L1 卡片一致 | **PASS** | FE `totalProducts uses API total…` + BE `l1CategoryId_totalProductsMatchesL1Subset` |
| 6 | Vitest：token 映射 | **PASS** | `token mapping for L1 Select` |
| 7 | 负例：无 enterprise 过滤字段 | **PASS** | `902-no-enterprise-filter` + BE enterprise 参数负例 |
| 8 | §0.2 命名用例 | **PASS** | `902-seatmap-l1-dropdown`、`902-no-enterprise-filter` |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-CAT-015 | 座序图 L1 下拉、卡片联动、无企业筛、与 Cascader 独立 | **PASS** |
| REQ-CAT-018 | 全链统计不按用户企业过滤（BE parity + FE 无 enterprise 参数） | **PASS** |

## 已知限制（不阻塞 PASS）

| 来源 | 说明 |
|---|---|
| REV FIND-WSC-902-003 (P3) | FE cross-enterprise 为源码静态断言；行为 parity 已由 BE 集成测覆盖；908 E2E 可补强 |
| REV FIND-WSC-902-004 (P3) | 902 用例以 readFileSync 源码断言为主，未 mount Select 交互 |
| REV FIND-WSC-902-001/002 (P2) | 临时 `apiRequest` 带参、L1 切换竞态 — 属 903/可选改进，非本任务 testScope 失败面 |
| testScope | UI E2E / Playwright 属 TASK-WSC-908，本任务未执行、未伪造 PASS |

## 缺陷

无 — 本轮无 FAIL，未开立 `BUG-*`。

## 证据红线

- 环境阻塞未记为 PASS（H2 MockMvc + Vitest 本地实跑）
- 未修改业务代码或 REV 结论
- 未更新 `state.yaml` / `events.jsonl`
- 未自行宣称 VERIFIED（仅 TESTRUN PASS）
- `actorInstance=tester-wsc-902`，与 developer / code-reviewer 隔离
