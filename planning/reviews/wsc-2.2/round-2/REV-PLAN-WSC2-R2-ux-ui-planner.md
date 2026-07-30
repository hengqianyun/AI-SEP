# 规划评审

```yaml
reviewId: REV-PLAN-WSC2-R2-uxUiPlanner
planId: PLAN-WSC-2.2
round: 2
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-002
decision: APPROVE
summary: |
  对照 Round 1 本人提出的 ISSUE-UX-R1-001/002，逐条核验 PLAN-WSC-2.2 修订与 closeWhen。
  §3.6 已写明导入结果可验收四态（全成功/部分成功/全失败行级/文件级拒绝），并禁止部分成功仅标「导入失败」；
  107 acceptance/testScope 含四态区分、partial-success 计数+报告、oversize 请求级拒绝。
  §3.1 冻结导入载体为目录页弹窗（对齐原型 #import-modal），入口与关闭返回点明确；107 acceptance/testScope 含入口可达与关闭回目录表面。
  两条 ISSUE 的 closeWhen 均已满足，本角色 APPROVE。无新增异议。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-2.2.md` |
| 快照 | SNAP-WSC-002 |
| R1 对照 | `planning/reviews/wsc-2.1/round-1/REV-PLAN-WSC2-R1-ux-ui-planner.md` |

## closeWhen 核验（ISSUE-UX-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-2.2 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-UX-R1-001 | P2 | 可验收四态：①全成功；②部分成功（两计数可观测+报告入口+可关闭/重传，禁止仅标「导入失败」）；③全失败行级；④文件级拒绝（超限/格式，不伪装行级） | §3.6 导入结果四态表与判定/UI 最低要求逐条对齐；107 acceptance「§3.6 四态可区分」「部分成功：计数可观测 + 错误报告入口」；testScope：`full-success` / `partial-success`（successCount/failureCount + reportId）/ `oversize`→`ERR_IMPORT_FILE_TOO_LARGE` 非行级 | **closeWhen 满足，可关闭** |
| ISSUE-UX-R1-002 | P2 | 声明目录页弹窗（或独立路由则写清路径/入口/返回点）；testScope 入口可达 + 结果态关闭回到约定表面 | §3.1「导入 UI 载体」：目录页弹窗对齐 `#import-modal`；入口=目录「新增」菜单；关闭后回目录浏览表面；107 acceptance「目录页弹窗入口可达；关闭后回到目录浏览」；testScope「入口可达 + 结果态后关闭回到目录表面」 | **closeWhen 满足，可关闭** |

## 本轮 ISSUE 表

无未关闭 ISSUE。R1 两条均确认可关闭；本轮不新增 ISSUE。

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 无异议项（记录）

- 目录维护列表四态、CAT-007 单条/批量交互：R1 已通过；2.2 §3.6/106 全文内联后仍可测，不重开。
- 同步导入无 job 轮询；§3.6「批量导入（提交中）」loading/禁用控件仍适用。
- 本角色仅就 UX 域 R1 异议关闭与体验可执行性给出 APPROVE，不代替必需角色批准。
