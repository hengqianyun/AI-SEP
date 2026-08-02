# Round 2 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC3-R2-qaStrategist
planId: PLAN-WSC-3.1
round: 2
role: qaStrategist
actorInstance: qa-strategist-wsc-003-r2
snapshotIdAtReview: SNAP-WSC-003
decision: APPROVE
summary: |
  相对 PLAN-WSC-3.0 Round 1 本角色五条 ISSUE（ISSUE-QA-WSC3-R1-001..005）的 closeWhen
  均已在 PLAN-WSC-3.1 满足，可关闭。分波视觉证据选定方案 A（草稿截图+差距草稿纳入
  201–205 VERIFIED）；草稿写权落入 writeSet；§3.2 场景 6 / 206 钉死三角色写入口矩阵；
  桌面视口与 201/E2E 机械条款已补齐。本领域无未关闭异议；未执行任何测试；
  不宣称实现或 E2E 已通过；本 APPROVE 仅表示计划层可测性门禁对本角色已闭合。
```

## Round 1 ISSUE closeWhen 确认

| id | severity | closeWhen 结论 | 证据（PLAN-WSC-3.1） |
|---|---|---|---|
| ISSUE-QA-WSC3-R1-001 | P1 | **关闭**（方案 A） | §3.3 显式选定方案 A：201–205 `VERIFIED` 含视觉草稿。§3.1 截图表映射草稿任务；各任务 `acceptance` 要求对应草稿截图+差距草稿已交付；`testScope` 要求草稿截图与 `ux-gap-drafts/TASK-WSC-20x.md` 可检；§5.1：tester 执行含分波视觉草稿的 `testScope`，无草稿不得标视觉 acceptance 通过。 |
| ISSUE-QA-WSC3-R1-002 | P1 | **关闭**（方案 A） | §3.1 声明分波差距草稿与 `ux-walkthrough/drafts/`；201–205 `writeSet` 均含 `ux-gap-drafts/TASK-WSC-20x.md` 与对应草稿截图字面路径；正式清单/`screenshots`/`WALKTHROUGH` 仍仅 206。已删除「可预填但无写权」不可执行表述。 |
| ISSUE-QA-WSC3-R1-003 | P1 | **关闭** | §3.2 场景 6 给出 ADMIN/PROVIDER/USER 写入口可见性矩阵（结构不可达，非仅 CSS）；扩展断言须落入 `p0-wsc-v1.1.spec.ts`（或继任规格并报告声明）。206 `testScope`：可勾选矩阵扩展步骤已在规格中且通过；`acceptance` 要求含场景 6。 |
| ISSUE-QA-WSC3-R1-004 | P2 | **关闭** | §3.1 桌面视口表：1280×800、1440×900；检查项含侧栏不遮挡主区、卡片不重叠、弹窗/modal 壳不溢出视口。206 `acceptance`：两视口破坏性检查已勾选。 |
| ISSUE-QA-WSC3-R1-005 | P2 | **关闭** | 201 `testScope` 钉死 `frontend/src/features/auth/composables/useCanWrite.spec.ts`（仓库存在；只读跑测）。§3.2：门禁规格 `specs/p0-wsc-v1.1.spec.ts`；命令 `pnpm --dir tests/e2e exec playwright test specs/p0-wsc-v1.1.spec.ts`（或 `test:v1.1`）；报告优先 `reports/p0-wsc-v1.2-ux/`，复用 V11 时强制 `evidenceId=TESTRUN-WSC-E2E-V12-UX` + 时间戳子目录。206 `writeSet`/`testScope` 与之对齐。 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 2 本领域无新增异议；R1 五条均可关闭。 | — | — |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 分波视觉 / VERIFIED | 方案 A + §5.1 绑定；页面波次不可在零 UX 草稿下标视觉通过 |
| 草稿写权 / scope-check | 201–205 writeSet 字面路径齐全；与 ISSUE-001/002 一致可调度 |
| 三角色写入口 E2E | §3.2 场景 6 + 206 可勾选；对齐 REQ-UX-011 / REQ-RBAC-001 意图 |
| 桌面破坏性视口 | ≥2 视口 + 检查项可勾选（REQ-UX-010） |
| E2E 命令/报告机械条款 | 规格文件、命令、evidenceId、报告落点已钉死 |
| 任务 testScope | 201–206 均有；视觉层与逻辑层均有可检交付物 |
| OQ-UX-003/004 | 仍成立：人工走查+截图硬门禁；E2E 复用扩展 |

## 说明

- 输入：`planning/proposals/PLAN-WSC-3.1.md`；R1：`planning/reviews/wsc-3.0/round-1/REV-PLAN-WSC3-R1-qa-strategist.md`。
- 对照仓库只读：`frontend/src/features/auth/composables/useCanWrite.spec.ts` 存在；`tests/e2e/package.json` 含 `test:v1.1`；`playwright.config.ts` 当前 html 报告仍为 `p0-wsc-v1.1`（计划允许 206 改 config 或复用并声明 evidenceId）。
- **未**执行 Vitest/Playwright/走查；**未**代替 developer/tester 宣称通过。
- 决策：`APPROVE`（本角色 R1 ISSUE 全部关闭且无新增 P1）；非伪造——关闭结论均有计划正文与任务字段字面证据。
