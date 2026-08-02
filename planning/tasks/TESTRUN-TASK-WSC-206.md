# 测试证据

```yaml
evidenceId: TESTRUN-WSC-E2E-V12-UX
taskId: TASK-WSC-206
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
actorInstance: tester-wsc-206
basedOn:
  - planning/tasks/TASK-WSC-206.md
  - planning/tasks/DEV-TASK-WSC-206.md
  - planning/tasks/REV-TASK-WSC-206.md
  - planning/approved/PLAN-WSC-3.1.md
reviewDecision: APPROVE
reviewP0P1: 0/0
executedAt: 2026-08-02T16:44:00+08:00
status: PASSED
exitCode: 0
mustDifferFrom:
  - developer-wsc-206
  - code-reviewer-wsc-206
```

## 结论摘要

**PASSED** — 独立 `tester-wsc-206` 按 TASK-WSC-206 正式门禁真实执行：正式 UX 制品（`ux-gap-checklist.md` §3.1、screenshots 01–10、`WALKTHROUGH.md` 结构）核验通过；E2E `specs/p0-wsc-v1.1.spec.ts`（含三角色矩阵 6b/6c）在 `E2E_RUN=1` 下 **14 passed / exitCode=0**；HTML 报告落于 `tests/e2e/reports/p0-wsc-v1.2-ux/`。人类 PO/UX 签署栏仍为空（待签）——按 testScope **不因此 FAIL**，仅在此注明。未跑全量 `specs/` / 遗留 `p0-wsc.spec.ts`。未自称任务 VERIFIED；未改业务源码 / REV / `state.yaml` / `events.jsonl`。

- evidenceId：`TESTRUN-WSC-E2E-V12-UX`（本 TESTRUN 与 E2E 证据同一 id）
- 环境：前端 `127.0.0.1:5173` → 200；后端 `/actuator/health` → 200 `UP`
- 审查前提：REV-TASK-WSC-206 APPROVE（P0/P1=0）

证据目录：`ai/runs/RUN-WSC-003/tester-wsc-206/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| formal-artifacts-checklist | 核验 `ai/runs/RUN-WSC-003/ux-gap-checklist.md` | PASSED | §3.1 schema 列齐全；截图 01–10 每项 ≥1 行；**P0 open=0**；P1 open=1（`GAP-UX-006-01` 载体 B，非 P0，可勾选）— `02-artifacts.txt` |
| formal-artifacts-screenshots | 存在性 `ux-walkthrough/screenshots/01`–`10` | PASSED | 10 文件均存在（145934…34864 bytes）— `02-artifacts.txt` |
| formal-artifacts-walkthrough | 核验 `WALKTHROUGH.md` 结构 | PASSED | 脱敏声明、视口勾选栏（1280×800 / 1440×900）、PO/UX 确认栏结构均存在 — `02-artifacts.txt` |
| po-ux-human-sign | 人类签署栏 | NOTED / N/A | 签署人/日期单元格仍为空（待签）；按 testScope **不 FAIL** |
| e2e-p0-wsc-v1.1 | 见下方实际命令 | PASSED | **14 passed**（18.6s），exitCode=**0**；含 6b/6c — `01-playwright-v12-ux.txt`, `01-playwright-exit.txt` |
| role-write-matrix | 规格内 ADMIN/PROVIDER/USER 写入口矩阵 | PASSED | `6b` + `6c`（PROVIDER/USER）本轮均 ok |
| full-specs-suite | 全量 `specs/`（含遗留 p0-wsc） | NOT_RUN | 禁止冒充本轮门禁；未执行 |

## E2E 实际命令（权威）

```text
cd C:\WorkSpace\AI-SEP\tests\e2e
$env:E2E_RUN='1'
node C:\WorkSpace\AI-SEP\node_modules\.pnpm\@playwright+test@1.62.0\node_modules\@playwright\test\cli.js test specs/p0-wsc-v1.1.spec.ts
```

| 项 | 值 |
|---|---|
| exitCode | **0** |
| 结果 | **14 passed**（18.6s，1 worker） |
| evidenceId | `TESTRUN-WSC-E2E-V12-UX` |
| HTML 报告 | `tests/e2e/reports/p0-wsc-v1.2-ux/index.html` |
| 原始输出 | `ai/runs/RUN-WSC-003/tester-wsc-206/01-playwright-v12-ux.txt` |
| 环境记录 | `ai/runs/RUN-WSC-003/tester-wsc-206/00-env.txt` |

## E2E 场景覆盖（本轮）

| # | 场景 | 结果 |
|---|---|---|
| 1a–1f | 登录/角色、总览、目录筛选、详情、上链、新增版本 | PASSED |
| 2 | 三级分类维护 | PASSED |
| 3 | 目录维护：待关联→已维护 | PASSED |
| 4 | 批量导入 partial-success | PASSED |
| 5 | 标签切换 + 滚动加载 | PASSED |
| 6a | 上链快照三级路径 | PASSED |
| 6b | 三角色写入口矩阵 ADMIN/PROVIDER/USER | PASSED |
| 6c | 独立会话写入口矩阵 PROVIDER / USER | PASSED |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | 正式制品：checklist §3.1 / 01–10 / P0 open=0；screenshots 01–10；WALKTHROUGH 脱敏/视口/PO·UX 结构 | PASSED（人类签署待签仅注明） |
| 2 | 真实执行 E2E `p0-wsc-v1.1.spec.ts`，evidenceId=`TESTRUN-WSC-E2E-V12-UX`，报告 `p0-wsc-v1.2-ux/` | PASSED（exit 0，14/14） |
| 3 | ADMIN/PROVIDER/USER 写入口矩阵步骤在规格中且本轮通过 | PASSED（6b/6c） |
| 4 | 禁止全量 `specs/` 冒充门禁 | 遵守（NOT_RUN） |

## 备注

- 正式截图来自分波 drafts/fixture 合并（CR P2 / WALKTHROUGH 已声明脱敏）；本轮功能回归以对真前端+后端的 E2E 为准，未另做「完全不像原型」像素人工对照（制品存在性 + E2E 功能门禁已覆盖 testScope）。
- 载体 B（`GAP-UX-006-01`）保持 P1 open 可勾选，非 P0，不阻塞本 TESTRUN PASSED。
- Playwright 通过仓库内唯一 `@playwright/test@1.62.0` 的 `cli.js` 直调，避免 `pnpm exec playwright` 双加载。

## 证据红线

- 未将环境阻塞记为 PASS（本轮环境可用并真实执行）
- 未未执行却声称覆盖；命令与 exitCode 已落盘
- 未自行宣称任务 VERIFIED（仅 TESTRUN）
- 未改业务源码、未改 REV 结论、未改 `state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-206`（≠ `developer-wsc-206` / `code-reviewer-wsc-206`）
- 无 BUG 单（全部通过）
