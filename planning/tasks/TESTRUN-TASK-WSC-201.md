# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-201-1
taskId: TASK-WSC-201
planId: PLAN-WSC-3.1
actorInstance: tester-wsc-201
basedOn:
  - planning/tasks/TASK-WSC-201.md
  - planning/tasks/DEV-TASK-WSC-201.md
  - planning/tasks/REV-TASK-WSC-201.md
  - planning/approved/PLAN-WSC-3.1.md
reviewDecision: APPROVE
executedAt: 2026-08-02T14:03:49+08:00
status: PASSED
```

## 结论摘要

**PASSED** — 独立 `tester-wsc-201` 按 PLAN-WSC-3.1 / TASK-WSC-201 `testScope` 真实复跑：`pnpm typecheck`、`pnpm build`、vitest（`useCanWrite` 3 + `RoleSwitcher` 1）均 exitCode=0；草稿截图 `02-shell-sidebar.png` 存在（35770 bytes）；差距草稿 `ux-gap-drafts/TASK-WSC-201.md` §3.1 schema 列齐全且覆盖截图 #02。对照 CR P2（FIND-WSC-201-R1-001）：截图为 **fixture HTML 视口拍屏**（角标「FIXTURE · 脱敏演示数据 · TASK-WSC-201」），非联调 Vue 真壳层；本轮未启动应用做像素对照 →「文件存在 + schema 可检」PASSED，「与真壳层像素一致」PARTIAL/SKIPPED（不因 fixture alone 伪造视觉像素 PASS）。E2E（`tests/e2e`）不在本任务 testScope → SKIPPED。

- 模块：frontend（data-chain-static@0.1.0）
- 审查：REV-TASK-WSC-201 APPROVE，P0/P1=0
- 未修改业务源码或审查报告；未自行宣称 VERIFIED；未改 `state.yaml` / `events.jsonl`

证据目录：`ai/runs/RUN-WSC-003/tester-wsc-201/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| frontend-typecheck | `pnpm typecheck`（cwd: frontend） | PASSED | exitCode=0 — 01-typecheck.txt, 01-typecheck-exit.txt |
| frontend-build | `pnpm build`（cwd: frontend） | PASSED | exitCode=0；vite build success — 02-build.txt, 02-build-exit.txt |
| frontend-vitest-readonly | `pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/features/shell/components/RoleSwitcher.spec.ts` | PASSED | 2 files / 4 tests（useCanWrite 3 + RoleSwitcher 1）— 03-vitest.txt, 03-vitest-exit.txt |
| draft-screenshot-02 | 存在性检查 `ux-walkthrough/drafts/02-shell-sidebar.png` | PASSED | EXISTS\|35770\|2026-08-02 13:55:39 — 04-screenshot-exists.txt |
| gap-draft-schema | 检 `ux-gap-drafts/TASK-WSC-201.md` §3.1 列 + 覆盖 #02 | PASSED | GAP-UX-002-01；列 id/screenshot\|pathKey/dimension/severity/status/evidence — 05-gap-draft-schema.txt |
| visual-pixel-vs-live-shell | 真开应用对照 WorkbenchLayout 像素一致 | PARTIAL / SKIPPED | CR P2 fixture；未联调会话态拍屏对照 — 07-cr-p2-fixture-note.txt；不伪造 PASSED |
| e2e | tests/e2e | SKIPPED | 不在本任务 testScope — 06-e2e-skipped.txt |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | 前端 `pnpm typecheck` | PASSED（exit 0） |
| 2 | 前端 `pnpm build` | PASSED（exit 0） |
| 3 | 只读 vitest：`useCanWrite.spec.ts`（及存在的 `RoleSwitcher.spec.ts`） | PASSED（4/4） |
| 4 | 草稿截图存在：`drafts/02-shell-sidebar.png` | PASSED（35770 bytes） |
| 5 | 差距草稿可检（§3.1 schema；至少覆盖 #02） | PASSED（GAP-UX-002-01） |
| 6 | 对照 CR P2：fixture vs 真壳层；像素一致未对照则 PARTIAL/SKIPPED | 文件+schema PASSED；像素对照 PARTIAL/SKIPPED（fixture，见备注） |
| — | E2E（tests/e2e） | SKIPPED（范围外） |

## 备注（CR P2 / 视觉）

- 截图性质：**fixture**（DEV 声明 + 图内角标「FIXTURE · 脱敏演示数据 · TASK-WSC-201」；主区文案「总览 (fixture)」），非真壳层联调会话态。
- 偏差：本轮未启动 frontend/dev 或浏览器对照 `WorkbenchLayout` 实机像素；按指令不得因 fixture alone 标「与真壳层像素一致」PASSED。偏差是否「完全不像」未做实机判定 → 不升级为视觉 FAIL；正式像素/视口走查归 TASK-WSC-206。
- 计划 §3.1 允许 fixture/脱敏作为分波草稿证据；本任务视觉相关 acceptance 以「草稿存在 + schema 可检」计 PASSED。

## 证据红线

- 未将环境阻塞记为 PASS
- 未未执行却声称覆盖；命令均真实执行并留 exitCode
- 未自行宣称任务 VERIFIED（仅 TESTRUN）
- 未改业务源码、未改 REV 结论、未改 `state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-201`（区别于 `developer-wsc-201` / `code-reviewer-wsc-201`）
- 无真实 PII；截图为脱敏 fixture
- E2E 未伪造 PASS
