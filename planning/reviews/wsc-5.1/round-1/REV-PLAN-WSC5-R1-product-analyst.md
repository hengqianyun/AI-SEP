# 规划评审 — PLAN-WSC-5.1 Round 1（productAnalyst）

```yaml
reviewId: REV-PLAN-WSC5-R1-productAnalyst
planId: PLAN-WSC-5.1
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-005
actorInstance: product-analyst-wsc-008-r1
decision: REQUEST_CHANGES
summary: |
  相对 SNAP-WSC-005 正文：六条 P0 REQ 均有主任务映射；In/Out Scope、RBAC/菜单重划、
  座序图/用户/我的产品口径与 SNAP/PRD 对齐；基线不削弱声明充分；预落地对账与 SNAP
  IN_REVIEW 派发门禁已写明（本评审不因 SNAP 未正式批准而 BLOCK 计划结构，但发布/派发
  门禁仍开放，不得伪称快照已批）。
  异议：REQ-CAT-010 的 create_by 空归属防冒领仅见于风险表，未落入 TASK-WSC-603
  acceptance/testScope，存在产品隔离验收漏项 → REQUEST_CHANGES。
```

## 评审范围与方法

- **权威**：`SNAP-WSC-005`（status=`IN_REVIEW`）+ `product/prd/wsc-v1.4-seatmap-users-mine.md`
- **对象**：`planning/proposals/PLAN-WSC-5.1.md`（CANDIDATE / DRAFT）；**不以**工作树预落地代码为批准依据
- **焦点**：P0 REQ 任务覆盖、In/Out Scope、RBAC/菜单修订、座序图·用户·我的产品对齐、基线不回退

## 覆盖结论（相对 SNAP）

| 检查项 | 结论 |
|---|---|
| P0 REQ→任务 | §7 六条均有主任务：SHELL→602/603；RBAC→601+602/603；CAT-009→601+604；USER-001→601+602；CAT-010→601+603；CAT-001→603/604 回归 |
| In Scope | 契约 2.2.0、sys_user、我的产品、ADMIN 保留分类/目录维护、座序图公共目录 — 与 SNAP/PO 锁定一致 |
| Out of Scope | Downloads React、座序图点击筛选、OAuth/多租户等与 SNAP/PRD 一致；计划额外 deny（公开注册、另起 UX、异步 Job）不扩大 SNAP 范围 |
| RBAC/菜单 | §4 三角色矩阵与 SNAP 角色表一致；切角色下线、产品写仅 PROVIDER、ADMIN 产品写 403、我的产品仅 PROVIDER |
| 座序图 CAT-009 | 604 acceptance：Top5、比例座位、hover 熄灭、真实 totalProducts、仅公共目录、明确不实现点击筛选 |
| 用户 USER-001 | 602：sys_user、hash 登录、ADMIN CRUD、软删、非 ADMIN 403 |
| 我的产品 CAT-010 | 603：PROVIDER 可见、无座序图、增改导、mine/`create_by`、公共目录去写 |
| 基线不回退 | 功能基线 SNAP-004/PLAN-4.1；UX SNAP-003/PLAN-3.1；导入四态/报告白名单/OpenAPI 不削弱声明齐全 |
| SNAP 门禁 | 文首与 §9 已区分「计划共识」与「快照正式批准」；本角色裁定：计划结构可评，**SNAP 正式批准仍开放** |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PA-001 | P1 | SNAP `REQ-CAT-010` 要求列表/编辑以 `create_by=本人` 隔离。PLAN §8 风险「create_by 历史数据为空」写明须由 603「明确空归属策略（不可被 PROVIDER 冒领编辑）」，但 TASK-WSC-603 `acceptance` 仅有「列表仅 create_by=本人」「改他人产品 403」，`testScope` 无「create_by 为空/缺失」负例；空归属是否可经直链/API 冒领编辑未成可测验收项，产品隔离口径相对 SNAP 不完整。 | 在 PLAN-WSC-5.1 的 TASK-WSC-603 `acceptance` 与 `testScope` 中显式增加：`create_by` 为空或非当前用户时 PROVIDER 编辑/更新/导入归属失败（403 或等价）；含自动化负例；并与 §8 空归属策略表述一致。 | REQ-CAT-010, REQ-RBAC-001 |

## 非阻塞备注（不构成独立 ISSUE）

- SNAP-WSC-005 仍为 `IN_REVIEW`：关闭本轮异议并达成委员会共识后，派发/发布前仍须满足计划 §9 快照门禁（正式批准或委员会书面确认范围等同 SNAP 正文）。本评审 **不** 代批 SNAP。
- REQ-SHELL-001 侧栏「分类维护 / 目录维护」可见性主要依赖 §4 矩阵、601 state-matrix 与 603 抽样回归；建议实现波次证据包勾选三角色×菜单，但不升格为本轮计划必改项。

## 决策

`REQUEST_CHANGES` — 关闭 ISSUE-PA-001 后方可再评本角色对本 planId 的 APPROVE。
