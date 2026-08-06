# 规划评审 — PLAN-WSC-5.2 Round 2（productAnalyst）

```yaml
reviewId: REV-PLAN-WSC5-R2-productAnalyst
planId: PLAN-WSC-5.2
round: 2
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-005
actorInstance: product-analyst-wsc-008-r2
decision: APPROVE
summary: |
  相对 SNAP-WSC-005 正文：六条 P0 REQ 均有主任务映射；In/Out Scope、RBAC/菜单重划、
  座序图/用户/我的产品口径与 SNAP/PRD 对齐；基线不削弱与预落地对账、SNAP IN_REVIEW
  派发门禁仍清晰（本评审不代批 SNAP）。
  R1 ISSUE-PA-001 closeWhen 已在 PLAN-WSC-5.2 的 TASK-WSC-603 acceptance/testScope
  与 §8 空归属策略中满足 → 本角色关闭 ISSUE-PA-001；无新增异议 → APPROVE。
closedIssues:
  - ISSUE-PA-001
openIssues: []
```

## 评审范围与方法

- **权威**：`SNAP-WSC-005`（status=`IN_REVIEW`）+ `product/prd/wsc-v1.4-seatmap-users-mine.md`
- **对象**：`planning/proposals/PLAN-WSC-5.2.md`（CANDIDATE / DRAFT；`basedOn: PLAN-WSC-5.1`）；**不以**工作树预落地代码为批准依据
- **焦点**：核验本角色 R1 ISSUE（尤其 ISSUE-PA-001）的 closeWhen；核对 SNAP 六条 P0 覆盖与产品范围口径
- **不做**：代关他角色 ISSUE；代批 SNAP；撰写他角色结论

## R1 ISSUE 关闭核验（本角色）

| id | severity | closeWhen（R1） | 5.2 证据 | 本轮 |
|---|---|---|---|---|
| ISSUE-PA-001 | P1 | TASK-WSC-603 `acceptance` 与 `testScope` 显式：`create_by` 为空或非当前用户时 PROVIDER 编辑/更新/导入归属失败（403 或等价）；含自动化负例；与 §8 空归属策略一致 | **acceptance**：空/缺失/非当前用户 → PROVIDER 编辑/更新/导入 **403**（或契约稳定拒绝码），不可冒领。**testScope**：单元格表「本人 200；他人/空归属 403」；后端「空/异主 create_by 负例」。§1.2 假设钉死空/缺失不可冒领；§8 风险行与 603 负例一致；§0 映射表指向上述位置 | **CLOSED** |

## 覆盖结论（相对 SNAP）

| 检查项 | 结论 |
|---|---|
| P0 REQ→任务 | §7 六条均有主任务：SHELL→602/603(+605)；RBAC→601+603(+605)；CAT-009→601+604(+605)；USER-001→601+602(+605)；CAT-010→601+603(+605)；CAT-001→603/604 回归(+605) |
| In Scope | 契约 2.2.0、sys_user、我的产品、ADMIN 保留分类/目录维护、座序图公共目录、强制 E2E — 与 SNAP/PO 锁定一致 |
| Out of Scope | Downloads React、座序图点击筛选、OAuth/多租户等与 SNAP 一致；计划额外 deny 不扩大 SNAP 范围 |
| RBAC/菜单 | §4 三角色矩阵与 SNAP 角色表一致；切角色下线、产品写仅 PROVIDER、ADMIN 产品写 403、我的产品仅 PROVIDER |
| 座序图 CAT-009 | 604：Top5、比例座位、hover/leave、真实 totalProducts、仅公共目录、无点击筛选 |
| 用户 USER-001 | 602：sys_user、hash 登录、ADMIN CRUD、软删、非 ADMIN 403 |
| 我的产品 CAT-010 | 603：PROVIDER 可见、无座序图、增改导、mine/`create_by`（**含空归属拒绝**）、公共目录去写 |
| 基线不回退 | 功能 SNAP-004/PLAN-4.1；UX SNAP-003/PLAN-3.1；导入四态/报告白名单不削弱 |
| SNAP 门禁 | 文首与 §9 区分计划共识与快照正式批准；**SNAP 正式批准仍开放**，本角色不代批 |

## 异议

（无。本角色 R1 唯一 ISSUE 已关闭；本轮未新开 ISSUE。）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## closedIssues / openIssues

| 集合 | ids |
|---|---|
| closedIssues | ISSUE-PA-001 |
| openIssues | （空） |

> 仅关闭本角色提出的 ISSUE。他角色 ISSUE（SA/QA/PP/PE/UX/API/SEC 等）**不**由本实例关闭。

## 非阻塞备注（不构成独立 ISSUE）

- SNAP-WSC-005 仍为 `IN_REVIEW`：委员会共识后，派发/发布前仍须满足计划 §9 快照门禁（正式批准或委员会书面确认范围等同 SNAP 正文）。
- 实现波次证据包建议继续勾选三角色×菜单，属执行层质量提示，不升格为本轮计划异议。

## 决策

`APPROVE` — ISSUE-PA-001 已关闭；本角色对本 planId（PLAN-WSC-5.2）无开放异议；SNAP 六条 P0 产品覆盖充分。
