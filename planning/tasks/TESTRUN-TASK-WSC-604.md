# 测试证据

```yaml
evidenceId: TESTRUN-TASK-WSC-604
taskId: TASK-WSC-604
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
actorInstance: tester-wsc-604-r1
retest: false
contracts: wsc-contracts@2.2.0（只读对照）
basedOn:
  - planning/approved/PLAN-WSC-5.2.md（TASK-WSC-604 acceptance + testScope；§0.2 604 行）
  - planning/tasks/REV-TASK-WSC-604.md（round 1 APPROVE；P0=0 P1=0）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-604.md
  - planning/tasks/TESTRUN-TASK-WSC-603.md（格式参考）
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-604.md
reviewRound: 1
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-06T11:07:30+08:00
result: PASS
failedCommandCount: 0
bugsClosed: []
bugsOpen: []
```

## 结论摘要

**PASS** — 独立 `tester-wsc-604-r1` 在 developer-wsc-604 交付 + code-reviewer-wsc-604-r1 APPROVE（P0=0、P1=0）后，按 PLAN-WSC-5.2 TASK-WSC-604 `testScope` 实跑：

1. 后端：`L2DistributionServiceTest` + `L2DistributionIntegrationTest` → **exitCode=0**（Tests run: **7**, Failures: **0**）。含空目录、真实总数 vs Top 之和、items 降序、预落地 `prelandReconcile_604_*`。
2. Vitest：`CirculationSeatMap.spec.ts` → **exitCode=0**（1 file / **6** tests）。含 Top5/占比、hover+leave、空/loading/error、无筛选 affordance、挂载约定、对账 testid。
3. acceptance/testScope checklist → 命名方法定位齐全（`03-acceptance-testscope.md`）。
4. 预落地对账 604 行 P0 → 命名路径齐全（`04-preland-reconcile-paths.md`）。

- 审查前提：REV-TASK-WSC-604 Round 1 **APPROVE**，P0=0，P1=0
- **未**将环境阻塞记为 PASS；命令均真实执行并记录 exitCode
- 未修改业务实现源码、`state.yaml`、`events.jsonl`；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-008/tester-wsc-604-r1/`

## commands

| # | command | cwd | exitCode | result | evidence |
|---|---|---|---|---|---|
| 1 | `mvn -pl app/data-chain-service -am "-Dtest=L2DistributionServiceTest,L2DistributionIntegrationTest" test` | `backend` | **0** | **PASS** | `01-backend.txt` / `01-backend-exit.txt` — Integration **4** + Service **3** = **7**，Failures: 0；BUILD SUCCESS |
| 2 | `pnpm exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts` | `frontend` | **0** | **PASS** | `02-vitest.txt` / `02-vitest-exit.txt` — 1 file / 6 tests passed |
| 3 | acceptance / testScope checklist 证据定位 | n/a（read + bind #1/#2） | n/a | **PASS** | `03-acceptance-testscope.md` |
| 4 | 预落地对账 604 行 P0 路径 | n/a | n/a | **PASS** | `04-preland-reconcile-paths.md` |

**failedCommandCount = 0**

## testScope 对照 checklist

| # | testScope 项 | 结果 | 证据 |
|---|---|---|---|
| 1a | 后端：distribution 计数与种子一致 | **PASS** | `L2DistributionIntegrationTest#seededProducts_totalProductsReal_andItemsSortedDesc`（7 产品；emr/imaging/credit 计数） |
| 1b | 后端：总数断言（真实总数 > Top items 之和） | **PASS** | 同上 `total > top5Sum`；ServiceTest `#l2Distribution_passesThroughTotalProductsAndItems` |
| 1c | 后端：空目录安全 | **PASS** | Integration `#emptyCatalog_returnsZeroTotalAndEmptyItems`；Service `#l2Distribution_emptyCatalog_safe` |
| 2a | Vitest：Top5 截断 + 比例座位 | **PASS** | `Top5 truncate + proportion seats + real total via getL2Distribution` |
| 2b | Vitest：hover 熄灭 + leave 恢复 | **PASS** | `hover dims others (opacity cap) + mouseleave restores` |
| 2c | Vitest：空态 / loading | **PASS** | `empty + loading + error states are readable` |
| 2d | Vitest：无筛选 affordance | **PASS** | `no click-filter affordance` |
| 2e | Vitest：mine 模式不渲染座序图（showSeatMap 约定） | **PASS** | `mount contract: only catalog showSeatMap=true; mine false` |
| 3 | 预落地对账自动化命名路径（604 行 P0） | **PASS** | `04-preland-reconcile-paths.md` |

## acceptance 摘要

| 项 | 结果 |
|---|---|
| API totalProducts 真实总数 + items 降序 | PASS（#1） |
| UI Top5 + 比例 + hover/leave | PASS（#2） |
| 空 / loading / error | PASS（#1+#2） |
| 仅 catalog 挂载；mine 不显示 | PASS（#2） |
| 无点击筛选 | PASS（#2） |
| L2 不在 Browse Controller；预落地对账 | PASS（#1+#2） |

## REQ 追踪

| REQ | 覆盖方式 | 结果 |
|---|---|---|
| REQ-CAT-009 | L2 API 真实总数/降序；CirculationSeatMap Top5/hover/leave/空态/挂载 | PASS（后端 7 + Vitest 6） |
| REQ-CAT-001 | 公共目录只读浏览不削弱（座序图只读挂载约定；未触 Browse Page 写集外改动） | PASS（mount contract + 审查 denyModify） |

## BUG

| id | severity | status | summary |
|---|---|---|---|
| （无） | — | — | 本轮无 FAIL；未开 BUG 单 |

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未伪造 mvn / vitest exit 0；命令如实记录
- 未修改业务实现源码、REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-604-r1`（≠ developer-wsc-604 / ≠ code-reviewer-wsc-604-r1）
- 未自行宣称 VERIFIED

## 计数

| 指标 | 值 |
|---|---|
| result | **PASS** |
| failedCommandCount | **0** |
| retest | **false** |
| 后端 Tests run | 7 / Failures 0 |
| Vitest | 1 file / 6 tests |
| bugsOpen | （无） |
