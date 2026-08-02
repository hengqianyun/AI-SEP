# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-304-1
e2eEvidenceId: TESTRUN-WSC-E2E-V13
taskId: TASK-WSC-304
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
actorInstance: tester-wsc-304
contracts: wsc-contracts@2.1.0
basedOn:
  - planning/tasks/TASK-WSC-304.md
  - planning/tasks/DEV-TASK-WSC-304.md
  - planning/tasks/REV-TASK-WSC-304.md
  - planning/approved/PLAN-WSC-4.1.md
reviewDecision: APPROVE
reviewRound: R5
reviewP0P1: 0/0
executedAt: 2026-08-02T19:16:43+08:00
priorRuns:
  - 2026-08-02T18:24:30+08:00 (r1 FAILED)
  - 2026-08-02T18:48:16+08:00 (r2 FAILED)
  - 2026-08-02T19:12:11+08:00 (r3 FAILED — E2E 10/10, typecheck EXIT=2)
status: PASSED
exitCode: 0
mustDifferFrom:
  - developer-wsc-304
  - code-reviewer-wsc-304
```

## 结论摘要

**PASSED**（r4 门禁）— 独立 `tester-wsc-304` 在 REV R5 APPROVE + BUG-005 修复后确认：`vue-tsc --noEmit` **EXIT=0**；Vitest import **16/16 PASSED**；Playwright **`TESTRUN-WSC-E2E-V13`** §6.1 **10/10 PASSED**（本轮完整重跑，未复用 r3 E2E）。未自称任务 VERIFIED；未改业务源码 / REV / DEV / `state.yaml` / `events.jsonl`。

- evidenceId（任务）：`EVID-TASK-WSC-304-1`
- E2E evidenceId：**`TESTRUN-WSC-E2E-V13`**
- 环境：5173 → 200；8080 health UP
- 审查前提：REV-TASK-WSC-304 **R5 APPROVE**
- BUG：001–005 均 **CLOSED**（本轮关闭 005）

证据目录：`ai/runs/RUN-WSC-004/tester-wsc-304/`（本轮 `r4-*`）  
HTML/JUnit：`tests/e2e/reports/p0-wsc-v1.3/`

## E2E 证据策略（r4）

| 项 | 说明 |
|---|---|
| 可选复用 | r3 已 10/10 PASSED，且本轮变更仅为移除未使用 import（BUG-005） |
| 复用风险 | Vite/运行时缓存、环境漂移、规格与后端状态变化可能导致「未重跑却绿」 |
| **本轮实际** | **完整重跑** `TESTRUN-WSC-E2E-V13`（稳妥路径）；不以 r3 报告代替本轮 exitCode |

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| frontend-typecheck | `pnpm exec vue-tsc --noEmit -p tsconfig.json --pretty false`（cwd: `frontend`） | PASSED | EXIT=**0**；无 diagnostics → `r4-02-typecheck.txt`, `r4-02-typecheck-exit.txt` |
| frontend-vitest-import | `pnpm exec vitest run src/features/catalog/import --reporter=default` | PASSED | Tests **16**；EXIT=**0** → `r4-01-vitest-*` |
| e2e-p0-wsc-v1.3 | 见下方权威命令 | PASSED | **10 passed**（12.4s）；EXIT=**0** → `r4-01-playwright-v13.txt`, `r4-01-playwright-exit.txt` |
| e2e-env | probe 5173 / 8080 | OK | `r4-00-env.txt` |

## E2E 实际命令（权威）

```text
cd C:\WorkSpace\AI-SEP\tests\e2e
set E2E_RUN=1
node C:\WorkSpace\AI-SEP\node_modules\.pnpm\@playwright+test@1.62.0\node_modules\@playwright\test\cli.js test --config=playwright.config.ts specs/p0-wsc-v1.3.spec.ts
```

（等价目标：`pnpm run test:p0-v13`；直调 cli 避免双加载。）

| 项 | 值 |
|---|---|
| exitCode | **0** |
| 结果 | **10 passed** |
| evidenceId | **`TESTRUN-WSC-E2E-V13`** |
| 规格 | `tests/e2e/specs/p0-wsc-v1.3.spec.ts` |
| HTML | `tests/e2e/reports/p0-wsc-v1.3/index.html` |
| JUnit | `tests/e2e/reports/p0-wsc-v1.3/junit.xml` |

## §6.1 场景覆盖（r4）

| # | 场景 | 结果 |
|---|---|---|
| 1a | 登录/角色 + 总览 | PASSED |
| 1b | 目录/详情/上链 | PASSED |
| 1c | PROVIDER 编辑入口 | PASSED |
| **2** | 强制 partial-success | PASSED |
| **3** | legacy-template 态④ | PASSED |
| **4** | OpenAPI 编辑保存再开 | PASSED |
| **5** | 详情只读 | PASSED |
| 6 | 三角色含导入 | PASSED |
| 6x PROVIDER / USER | 独立会话 | PASSED |

## 缺陷状态

| bugId | 状态 |
|---|---|
| BUG-WSC-304-001 | CLOSED（r3） |
| BUG-WSC-304-002 | CLOSED（r3） |
| BUG-WSC-304-003 | CLOSED（r2） |
| BUG-WSC-304-004 | CLOSED（r3） |
| BUG-WSC-304-005 | **CLOSED（r4）** — typecheck EXIT=0 |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | typecheck EXIT=0 | PASSED |
| 2 | Vitest import | PASSED（16/16） |
| 3 | E2E `TESTRUN-WSC-E2E-V13` + 报告 `p0-wsc-v1.3/` | PASSED（本轮重跑 10/10） |
| 4 | 强制 partial-success / legacy / 三角色 | PASSED |
| 5 | 全过 → PASSED；勿自称 VERIFIED | **PASSED**（仅 TESTRUN） |

## 证据红线

- 未将环境阻塞记为 PASS
- 未未执行却声称覆盖；E2E 本轮完整重跑
- 未自行宣称 VERIFIED
- 未改业务源码、REV、DEV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-304`（≠ developer / code-reviewer）
- 无 OPEN 阻塞缺陷
