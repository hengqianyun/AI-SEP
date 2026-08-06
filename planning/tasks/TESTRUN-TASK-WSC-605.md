# 测试证据

```yaml
evidenceId: TESTRUN-TASK-WSC-605
releaseEvidenceId: TESTRUN-WSC-E2E-V14
taskId: TASK-WSC-605
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
actorInstance: tester-wsc-605-r1
retest: false
contracts: wsc-contracts@2.2.0（只读；本任务未触碰）
basedOn:
  - planning/approved/PLAN-WSC-5.2.md（TASK-WSC-605 acceptance + testScope；§6.1 七场景）
  - planning/tasks/REV-TASK-WSC-605.md（round 1 APPROVE；P0=0 P1=0）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-605.md
  - planning/tasks/TESTRUN-TASK-WSC-604.md（格式参考）
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-605.md
reviewRound: 1
reviewP0: 0
reviewP1: 0
specFile: tests/e2e/specs/p0-wsc-v1.4.spec.ts
reportDir: tests/e2e/reports/p0-wsc-v1.4/
executedAt: 2026-08-06T14:34:57+08:00
env:
  E2E_RUN: "1"
  E2E_BASE_URL: "http://127.0.0.1:5173"
  backend: "http://127.0.0.1:8080 (Listen)"
result: PASS
failedCommandCount: 0
bugsClosed: []
bugsOpen: []
mustDifferFrom:
  - developer-wsc-605
  - code-reviewer-wsc-605-r1
```

## 结论摘要

**PASS** — 独立 `tester-wsc-605-r1` 在 developer-wsc-605 交付 + code-reviewer-wsc-605-r1 APPROVE（P0=0、P1=0）后，按 PLAN-WSC-5.2 TASK-WSC-605 `testScope` **真实重跑** Playwright E2E：

1. `pnpm run test:p0-v14`（cwd=`tests/e2e`；`E2E_RUN=1`；`E2E_BASE_URL=http://127.0.0.1:5173`）→ **exitCode=0**；**15 passed / 0 failed**（31.4s）。
2. 发布证据包 `evidenceId: TESTRUN-WSC-E2E-V14` 落于 `tests/e2e/reports/p0-wsc-v1.4/`（HTML / JUnit / EVIDENCE.md）。
3. §6.1 场景 1–7 全部勾选 **PASS**（见下表与 `02-section4-scenarios.md`）。

- 审查前提：REV-TASK-WSC-605 Round 1 **APPROVE**，P0=0，P1=0（开放 findings 仅为 P2/P3，不阻塞）
- developer 自测 **不算** 本门禁；本轮为独立 tester 重跑
- **未**将环境阻塞记为 PASS；命令真实执行并记录 exitCode
- **未**修改业务实现源码（frontend/backend/contracts）、`state.yaml`、`events.jsonl`；**未**自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-008/tester-wsc-605-r1/`

## commands

| # | command | cwd | env | exitCode | result | evidence |
|---|---|---|---|---|---|---|
| 1 | `pnpm run test:p0-v14`（≡ `playwright test specs/p0-wsc-v1.4.spec.ts --config=playwright.config.ts`） | `tests/e2e` | `E2E_RUN=1`；`E2E_BASE_URL=http://127.0.0.1:5173` | **0** | **PASS** | `01-playwright-p0-v14.txt` / `01-playwright-p0-v14-exit.txt` — **15 passed**；JUnit tests=15 failures=0 |
| 2 | §6.1 七场景勾选对账 | n/a（bind #1 + junit） | n/a | n/a | **PASS** | `02-section4-scenarios.md` |
| 3 | JUnit / 报告目录核对 | n/a | n/a | n/a | **PASS** | `03-junit-summary.md`；`reports/p0-wsc-v1.4/{index.html,junit.xml,EVIDENCE.md}` |

**failedCommandCount = 0**

## §6.1 七场景勾选

| # | 场景 | 结果 | 用例 |
|---|---|---|---|
| 1 | 真登录三角色；角色绑定；无自由切角色；只读角色展示 | **PASS** | `1)×3` |
| 2 | ADMIN 用户管理 CRUD；PROVIDER/USER 不可达 | **PASS** | `2a`；`2b)×2` |
| 3 | 公共目录三角色无增改导 | **PASS** | `3)×3` |
| 4 | PROVIDER 我的产品增改导；列表本人；无座序图；返回仍在我的产品 | **PASS** | `4)`；`4x` |
| 5 | ADMIN 产品写 403；分类/维护仍可用 | **PASS** | `5)` |
| 6 | 公共目录座序图 Top5+总数+hover；我的产品无图 | **PASS** | `6)` |
| 7 | V1.3 回归：导入四态 + OpenAPI 编辑不回退 | **PASS** | `7a`；`7b` |

## testScope 对照 checklist

| # | testScope 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | 在 `tests/e2e` 执行 `pnpm run test:p0-v14`（或等价 playwright）并写入报告 | **PASS** | command #1；本文件 commands |
| 2 | 规格文件名写入报告 | **PASS** | `specs/p0-wsc-v1.4.spec.ts` |
| 3 | 七场景勾选（§6.1） | **PASS** | 上表；`02-section4-scenarios.md` |
| 4 | `evidenceId: TESTRUN-WSC-E2E-V14` | **PASS** | `reports/p0-wsc-v1.4/EVIDENCE.md`；规格头注释 |
| 5 | 报告落点 `tests/e2e/reports/p0-wsc-v1.4/` | **PASS** | index.html / junit.xml / EVIDENCE.md |
| 6 | 独立 tester；P0 缺陷为 0 | **PASS** | actorInstance；bugsOpen 空 |

## acceptance 摘要

| 项 | 结果 |
|---|---|
| §6.1 场景 1–7 全部强制通过 | PASS（15/0） |
| evidenceId TESTRUN-WSC-E2E-V14 | PASS |
| 报告于 `tests/e2e/reports/p0-wsc-v1.4/` | PASS |
| 独立 tester；P0 缺陷为 0 | PASS |

## REQ 追踪

| REQ | 覆盖方式 | 结果 |
|---|---|---|
| REQ-SHELL-001 | 真登录三角色；只读角色；无切角色 | PASS（场景 1） |
| REQ-RBAC-001 | 用户管理可达性；公共目录无写；ADMIN 产品写 403 | PASS（场景 2/3/5） |
| REQ-USER-001 | ADMIN 用户 CRUD 抽样 | PASS（场景 2） |
| REQ-CAT-009 | 座序图 Top5+总数+hover；我的产品无图 | PASS（场景 6） |
| REQ-CAT-010 | 我的产品增改导 / 列表本人 / 返回 | PASS（场景 4；改/导亦见 7b/7a） |
| REQ-CAT-001 | 公共目录只读浏览；无增改导 | PASS（场景 3） |

## 发布证据包引用

| 字段 | 值 |
|---|---|
| 发布 `evidenceId` | **TESTRUN-WSC-E2E-V14** |
| 任务级 `evidenceId` | **TESTRUN-TASK-WSC-605** |
| 报告目录 | `tests/e2e/reports/p0-wsc-v1.4/` |
| HTML | `tests/e2e/reports/p0-wsc-v1.4/index.html` |
| JUnit | `tests/e2e/reports/p0-wsc-v1.4/junit.xml`（15/0） |
| EVIDENCE | `tests/e2e/reports/p0-wsc-v1.4/EVIDENCE.md` |

## BUG

| id | severity | status | summary |
|---|---|---|---|
| （无） | — | — | 本轮 PASS；P0 缺陷为 0；未开 BUG 单 |

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未伪造 playwright exit 0；命令如实记录
- 未修改 frontend/backend/contracts 业务实现源码
- 未修改 REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-605-r1`（≠ developer-wsc-605 / ≠ code-reviewer-wsc-605-r1）
- 未自行宣称 VERIFIED
- developer 自测 junit 仅作对照；本门禁以本轮重跑为准

## 计数

| 指标 | 值 |
|---|---|
| result | **PASS** |
| failedCommandCount | **0** |
| retest | **false** |
| Playwright | 15 passed / 0 failed |
| §6.1 场景 | 7 / 7 PASS |
| bugsOpen | （无） |
| P0 缺陷 | **0** |
