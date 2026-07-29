# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-003-2
taskId: TASK-WSC-003
planId: PLAN-WSC-1.1
actorInstance: tester-wsc-003-reconfirm
contracts: wsc-contracts@1.1.0
supersedes: EVID-TASK-WSC-003-1
basedOn:
  - planning/tasks/TASK-WSC-003.md
  - planning/tasks/DEV-TASK-WSC-003.md
  - planning/tasks/REV-TASK-WSC-003.md
reviewDecision: APPROVE
executedAt: 2026-07-29T16:46:48+08:00
status: PASSED
```

## 总体结论

**PASSED** — `REV-TASK-WSC-003` 已 **APPROVE**；后端 Overview 5 测 + 前端 overview vitest 通过（复跑合批 22 测全绿含本任务 5）。首次 EVID-1 因并行时审查未落盘记 BLOCKED，现已解除。

## Layers

| name | command | result | notes |
|---|---|---|---|
| review-gate | 读 `planning/tasks/REV-TASK-WSC-003.md` | PASSED | decision: APPROVE；P0/P1=0 |
| backend-overview | `mvn -f backend/pom.xml "-Dtest=com.wsc.overview.OverviewApiIntegrationTest" test` | PASSED | Tests run: **5**, Failures: 0, Errors: 0；BUILD SUCCESS；exit 0 |
| frontend-overview | `pnpm --filter frontend exec vitest run src/features/overview` | PASSED | Test Files 1 passed；Tests **1** passed；exit 0 |
| static-stream-types | 读集成测 + `useOverviewData` / Page | PASSED | 三类类型见下 |
| static-empty | 读集成测空态 + Page `*-empty` testid | PASSED | 见下 |

## 后端明细

| 类 | Tests | Failures | Errors | 结果 |
|---|---|---|---|---|
| `com.wsc.overview.OverviewApiIntegrationTest` | 5 | 0 | 0 | PASSED |

覆盖抽样：seed metrics；`clear()` 后 metrics/stream/trend 友好空态（0/空数组）；`fixtureThreeStreamTypes` 断言 `TRADE_ORDER`/`DATA_REGISTER`/`CATALOG_REGISTER` 倒序；TOP/distribution；未登录 401。

## 前端明细

| 规格 | Tests | 结果 |
|---|---|---|
| `src/features/overview/composables/useOverviewData.spec.ts` | 1 | PASSED |

`STREAM_TYPE_LABEL`：目录登记 / 数据登记 / 交易订单。

## 静态核对

### 1. 三类流类型

| 项 | 证据 | 结果 |
|---|---|---|
| 枚举三类 | 集成测 `jsonPath` 含 `CATALOG_REGISTER` / `DATA_REGISTER` / `TRADE_ORDER` | PASSED |
| 中文标签 | `STREAM_TYPE_LABEL` + vitest | PASSED |
| UI type 样式 | `OverviewPage.vue` `[data-type=…]` | PASSED |

### 2. 空态

| 项 | 证据 | 结果 |
|---|---|---|
| API 空库 | `metricsAndStream_emptyStore_returnFriendlyZeros` | PASSED |
| UI §3.5 empty | `metrics-empty` / `trend-empty` / `top-empty` / `stream-empty` / `distribution-empty` | PASSED |
| composable | `LoadState` 含 `empty`；指标全 0 / 列表空 → empty | PASSED |

## 环境快照

| 项 | 值 |
|---|---|
| Java | 17.0.11 |
| Maven | 仓库 `backend/pom.xml`；Surefire 3.2.5 |
| Node / Vitest | Vitest v2.1.9 |
| cwd | `C:\WorkSpace\AI-SEP` |
| 日志 | `temp/mvn-wsc003-test.out`、`temp/vitest-wsc003.out` |

## 阻塞说明

- **blockedBy**: 缺少独立 codeReviewer 产出 `REV-TASK-WSC-003.md` 且 `decision: APPROVE`
- **解除条件**: 审查 APPROVE（或等价关闭 P0/P1）后，由独立 tester 复跑或确认本证据命令层仍有效，方可改标 PASSED
- 命令全绿 **≠** PASSED

## 决策权声明

- tester 未修改业务源码；仅产出本证据文件。
- 未把未执行的 E2E 记为 PASSED。
