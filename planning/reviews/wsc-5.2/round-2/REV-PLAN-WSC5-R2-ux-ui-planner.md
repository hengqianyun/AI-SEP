# 规划评审 — UX/UI（PLAN-WSC-5.2 Round 2）

```yaml
reviewId: REV-PLAN-WSC5-R2-uxUiPlanner
planId: PLAN-WSC-5.2
round: 2
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-005
actorInstance: ux-ui-planner-wsc-008-r2
decision: APPROVE
summary: |
  对照 SNAP-WSC-005 与 Round 1 ISSUE-UX-WSC5-R1-001..004 的 closeWhen：PLAN-WSC-5.2
  已在 §1.5 冻结四条 UX 面，并下沉到 601/602/603/604 acceptance 与 testScope。
  ① 角色只读唯一展示面（无 listbox / 禁 disabled 假下拉 / REQ-UX-008 切换段 supersede）；
  ② 双目录页头/active/返回点 + state-matrix 导入宿主→我的产品；
  ③ 座序图 hover+leave、空态/loading、挂载位、无筛选 affordance；
  ④ UsersAdminPage 令牌层次与 loading/empty/error。
  四条 ISSUE 均满足 closeWhen，本角色关闭并 APPROVE。不代替他角色批准，不写 ai/runs/**。
closedIssues:
  - ISSUE-UX-WSC5-R1-001
  - ISSUE-UX-WSC5-R1-002
  - ISSUE-UX-WSC5-R1-003
  - ISSUE-UX-WSC5-R1-004
openIssues: []
```

## 评审基线

| 项 | 值 |
|---|---|
| 角色 | `uxUiPlanner`（`ai/agents/ux-ui-planner.md`） |
| 计划 | `planning/proposals/PLAN-WSC-5.2.md`（`CANDIDATE` / `DRAFT`，round 2） |
| 需求权威 | `product/requirements/SNAP-WSC-005.md`（`IN_REVIEW`） |
| UX 基线 | SNAP-WSC-003 / PLAN-WSC-3.1（令牌/壳层不回退） |
| R1 本品 ISSUE | `planning/reviews/wsc-5.1/round-1/REV-PLAN-WSC5-R1-ux-ui-planner.md` |
| 本实例 | `ux-ui-planner-wsc-008-r2` |

## Round 1 ISSUE 核验

| ISSUE id | sev | closeWhen 要点 | PLAN-WSC-5.2 证据 | 结论 |
|---|---|---|---|---|
| ISSUE-UX-WSC5-R1-001 | P1 | (A) footer 唯一只读无 listbox；(B) 侧栏令牌/企业卡；(C) RoleSwitcher 结构不可达；(D) REQ-UX-008 切换段 supersede | §1.5「角色只读（UX-001）」全文；(§0 映射)；602 `acceptance` 只读唯一展示 + 无 listbox/chevron；602 `testScope` 三角色「只读无切换」+ Vitest RoleSwitcher 无 listbox；§0.3 对账提示 | **关闭** |
| ISSUE-UX-WSC5-R1-002 | P1 | 双表面文案/页头空态；active 互斥；写/导入回 `/my-products`；公共无增改导；state-matrix 导入宿主 | §1.5「双目录表面（UX-002）」；§3.1 / 601 acceptance：`state-matrix` 导入宿主=我的产品、版本头 2.2.0；603 acceptance 双表面+返回点；603 testScope 导入宿主/active 互斥；§6.1 场景 4 返回仍在我的产品 | **关闭** |
| ISSUE-UX-WSC5-R1-003 | P1 | hover+leave；空态；loading；挂载位；无筛选 affordance；仅公共目录 | §1.5「座序图（UX-003）」；604 acceptance：Top5/比例/hover+leave/空态/loading/挂载仅 `/catalog`/无筛选 affordance/失败不崩；604 testScope 对应 Vitest | **关闭** |
| ISSUE-UX-WSC5-R1-004 | P2 | UsersAdminPage 令牌层次；loading/empty/error；非 ADMIN 不可达 | §1.5「用户管理页（UX-004）」；602 acceptance UsersAdminPage 页头+列表卡 + 空载错；602 testScope UsersAdminPage 权限与空载态 | **关闭** |

## 异议

（本轮无新开 ISSUE。）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 关闭清单

| id | 动作 | 理由 |
|---|---|---|
| ISSUE-UX-WSC5-R1-001 | **closed** | closeWhen (A)(B)(C)(D) 已写入 §1.5 + 602 acceptance/testScope |
| ISSUE-UX-WSC5-R1-002 | **closed** | closeWhen 双表面/返回/导入宿主已写入 §1.5 + 601/603 + E2E 场景 4 |
| ISSUE-UX-WSC5-R1-003 | **closed** | closeWhen 状态机与挂载位已写入 §1.5 + 604 acceptance/testScope |
| ISSUE-UX-WSC5-R1-004 | **closed** | closeWhen 用户管理页质感已写入 §1.5 + 602 acceptance |

**closedIssueCount: 4** · **openIssueCount: 0**

## 非阻塞观察（不升 ISSUE）

- SNAP 仍 `IN_REVIEW`：本评审按正文范围确认计划体验可调度性；不代替快照正式批准。
- §1.5 / Out of Scope「不重做 UX 令牌 / 禁止第二套 UI 框架」与 UX 基线一致，维持通过。
- 实现期须对账预落地 `RoleSwitcher` 残留与导入返回路径；验收以本计划 acceptance 为准，本角色不预判实现质量。

## 决策

`APPROVE` — Round 1 本品四条 ISSUE 均满足 closeWhen 并关闭；无新开异议。  
本实例 **不** 代写计划补丁、**不** 伪造他角色 APPROVE、**不** 写入 `ai/runs/**`、**不** 关闭他人 ISSUE。
