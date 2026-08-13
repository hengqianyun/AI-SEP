# 代码审查（Round 2）

```yaml
reviewId: REV-TASK-WSC-707-R2
taskId: TASK-WSC-707
planId: PLAN-WSC-6.2
round: 2
role: codeReviewer
actorInstance: code-reviewer-wsc-707-r2
decision: APPROVE
p0: 0
p1: 0
p2: 1
p3: 0
closedFindings:
  - FIND-WSC-707-R1-001
  - FIND-WSC-707-R1-002
  - FIND-WSC-707-R1-003
  - FIND-WSC-707-R1-004
  - FIND-WSC-707-R1-005
openFindings:
  - FIND-WSC-707-R2-001
priorReview: planning/tasks/REV-TASK-WSC-707.md
contracts: wsc-contracts@2.3.0（707 只读；hotfix 升 2.3.1 不属本任务写集）
riskTags: [e2e-gate, release-evidence]
reviewedAt: 2026-08-12T16:50:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/tasks/REV-TASK-WSC-707.md（Round 1 REQUEST_CHANGES；FIND-001..005）
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-707 writeSet·denyModify·acceptance·testScope；§6.1；§9 item 8）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-707.md
  - planning/tasks/REV-TASK-WSC-HOTFIX-USER-001.md（APPROVE）
  - planning/tasks/TESTRUN-TASK-WSC-HOTFIX-USER-001.md（PASS；v14 2a PASS）
  - 实地：tests/e2e/specs/p0-wsc-v1.5.spec.ts、helpers/wsc-v15-fixtures.ts、playwright.v15.config.ts、package.json
  - 实地：playwright.config.ts、specs/p0-wsc-v1.4.spec.ts（denyModify）
  - 证据：tests/e2e/reports/p0-wsc-v1.5/{EVIDENCE.md,junit.xml,run-console.log,run-v14-console.log}
  - 证据：tests/e2e/reports/p0-wsc-v1.4/run-hotfix-user-001-2a.log
mustDifferFrom:
  - developer（本任务实现者；本实例未兼任）
  - code-reviewer-wsc-707-r1
  - tester-hotfix-user-001-r1
  - code-reviewer-hotfix-user-001-r1
spotCheck: |
  writeSet / denyModify / test:p0-v15 一字不差 / §6.1 1–8 覆盖：仍 PASS（与 R1 一致；本轮未改业务源码）。
  FIND-001：TESTRUN-TASK-WSC-HOTFIX-USER-001 C3（-g 2a）exit 0 → CLOSE。
  FIND-002：逐条矩阵已有；heading=改名、#4=收起+维护门控为 intentional；soft-delete 非故意增量，
  且 hotfix 后 2a 复跑 PASS → 不再作基线失败门槛。
  FIND-003：场景 7 断言筛 Cascader 后 batch-bar 消失 + 引用 706 unit。
  FIND-004：writeEvidenceHeader 合并保留 MATRIX_MARKER 段。
  FIND-005：无未用 loginAs；场景 4 调用 installUncategorizedPageFixture。
  残余：EVIDENCE/DEFAULT 矩阵 soft-delete 行仍写 BLOCKER escalate（与 TESTRUN 绿矛盾）→ R2-001 P2。
  未代 tester PASS；未标 VERIFIED；未写 state/events；未改业务代码。
```

## Round 2 结论

**APPROVE** — **P0=0，P1=0**。Round 1 全部 P1 已清零：FIND-001 由独立 hotfix TESTRUN **v14 2a PASS** 关闭；FIND-002 逐条失败矩阵可审计，故意增量归类正确，soft-delete 在 hotfix 后不再构成基线失败。FIND-003..005 已关闭。开放 **P2×1**（证据矩阵 soft-delete 行文案过期）**不阻塞**进入独立 tester（门禁命令仍为 `test:p0-v15`）。

## Round 1 Findings 处置

| id | sev | R1 | R2 status | 证据 / 说明 |
|---|---|---|---|---|
| FIND-WSC-707-R1-001 | P1 | OPEN | **CLOSED** | `planning/tasks/TESTRUN-TASK-WSC-HOTFIX-USER-001.md`：C3 `playwright … p0-wsc-v1.4.spec.ts -g '2a\)'` **exit 0 / v14_2a: PASS**；日志 `tests/e2e/reports/p0-wsc-v1.4/run-hotfix-user-001-2a.log`（1 passed）。前置：`REV-TASK-WSC-HOTFIX-USER-001.md` **APPROVE**（P0=0 P1=0）。满足 R1 closeWhen「业务 hotfix 后附绿证据」；**不得**仅用「产品增量」关闭——本关闭路径为 hotfix E2E，合规 |
| FIND-WSC-707-R1-002 | P1 | OPEN | **CLOSED** | `EVIDENCE.md`「## V1.4 coexistence failure matrix」逐条：heading→「全链数据目录」= intentional；#4 首错收起致 `catalog-search` + PROVIDER 维护 ADMIN-only = intentional；soft-delete **未**误标为故意增量。hotfix 后 2a 复跑 PASS → soft-delete **不再**是基线失败门槛（§9 item 8 豁免仅需对仍失败的故意增量行）。R1 closeWhen「001 清零后本 finding 方可关」已满足 |
| FIND-WSC-707-R1-003 | P2 | OPEN | **CLOSED** | `p0-wsc-v1.5.spec.ts` 场景 7：筛 Cascader 变更后 `batch-bar` count=0；注释引用 `useCatalogMaintenance.spec.ts`「clears selectedIds on statusTab and filter change」 |
| FIND-WSC-707-R1-004 | P2 | OPEN | **CLOSED** | `writeEvidenceHeader()`：若磁盘已有 `MATRIX_MARKER` 则保留该段再写头，避免 beforeAll 冲掉矩阵 |
| FIND-WSC-707-R1-005 | P3 | OPEN | **CLOSED** | `wsc-v15-fixtures.ts` 无未用 `loginAs`；场景 4 使用导出的 `installUncategorizedPageFixture` |

## Round 2 新 Finding

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-707-R2-001 | P2 | **OPEN** | `EVIDENCE.md` / `DEFAULT_COEXISTENCE_MATRIX` 中 `2a` 行与 Soft-delete investigation 仍写 **baseline regression / BLOCKER escalate / FIND-001 保持 OPEN**，与 `TESTRUN-TASK-WSC-HOTFIX-USER-001` **v14 2a PASS** 及本轮关闭 FIND-001 矛盾；故意增量行仍正确。可审计性降级为「过期 escalate 叙事」，**非** R1 式笼统归因 P1 | 将 soft-delete 行改为 **CLEARED / PASS（re-run）**，引用 `planning/tasks/TESTRUN-TASK-WSC-HOTFIX-USER-001.md`（及 `run-hotfix-user-001-2a.log`）；同步修订 Soft-delete investigation 段；可选同步 `DEFAULT_COEXISTENCE_MATRIX` 默认文案 | §9 item 8 证据新鲜度 |

## 检查清单（Round 2）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — v15 spec / `playwright.v15.config.ts` / `package.json`(+`test:p0-v15`) / `wsc-v15-fixtures.ts` / `reports/p0-wsc-v1.5/**` |
| denyModify：默认 `playwright.config.ts` 仍 v1.4 | **PASS** |
| denyModify：`p0-wsc-v1.4.spec.ts` | **PASS**（707 未改；hotfix 亦未改冻结规格） |
| denyModify：业务 FE/BE/contracts/sql（707 归因） | **PASS** — 本任务未写入；hotfix 写集外不计入 707 越界 |
| `test:p0-v15` script 一字不差 | **PASS** — `playwright test specs/p0-wsc-v1.5.spec.ts --config=playwright.v15.config.ts`；`test:p0-v14` 保留 |
| §6.1 场景 1–8 | **PASS** — 规格均有对应用例；#7 筛清空已补 |
| `evidenceId: TESTRUN-WSC-E2E-V15` | **PASS** |
| v15 开发自测 12/0 | **记录** — `junit.xml` tests=12 failures=0；**不**代 tester PASS |
| FIND-001 / v14 2a 基线 | **PASS（关闭）** — 引 `planning/tasks/TESTRUN-TASK-WSC-HOTFIX-USER-001.md` |
| FIND-002 矩阵 / 故意增量 | **PASS（关闭）** — 故意增量可豁免；soft-delete 已非失败基线 |
| §9 item 8（联读） | **PASS（当前证据）** — v15 门禁仍待独立 tester；v14 共存可执行；故意增量有矩阵；2a 基线由 hotfix TESTRUN 绿 |
| 开发自测 ≠ 独立 tester | **记录** — 未代 707 tester；未标 VERIFIED |
| 未写 state·events / 未改业务代码 | **PASS** |
| `mustDifferFrom` | **PASS**（`code-reviewer-wsc-707-r2`） |
| P0 / P1 | **P0=0；P1=0** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester 宣称 `test:p0-v15` 正式门禁 PASS。
- **decision: APPROVE**（P0=0、P1=0；开放 P2 可延期至 tester 前或并行由 developer 刷新矩阵文案）。
- 正式发布门禁命令仍为：`pnpm --dir tests/e2e run test:p0-v15`（独立 tester）。

## 计数（Round 2）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | R1 FIND-001/002 已关 |
| P2 | 1 | FIND-WSC-707-R2-001 矩阵 soft-delete 叙事过期 |
| P3 | 0 | — |
