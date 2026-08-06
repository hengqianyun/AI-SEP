# 规划评审 — UX/UI（PLAN-WSC-5.1 Round 1）

```yaml
reviewId: REV-PLAN-WSC5-R1-uxUiPlanner
planId: PLAN-WSC-5.1
round: 1
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-005
actorInstance: ux-ui-planner-wsc-008-r1
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-005 与 UX 基线 SNAP-WSC-003：侧栏角色菜单可见性、切角色下线、
  公共目录 vs 我的产品入口分离、座序图 Top5/hover/真实总数及令牌不重开的意图均已写入
  §1.5/§4 与 602–604 acceptance，方向正确。但仍有四处体验验收未冻住，阻碍本角色 APPROVE：
  ① 角色区由可切换下拉改为只读时，未冻结替换形态及对 REQ-UX-008 交互段的书面 supersede；
  ② 双目录表面（页头文案/active/增改导返回点）与 state-matrix 导入宿主未钉死；
  ③ 座序图缺 loading/empty/leave 恢复与目录内挂载位，易伤 REQ-UX-004；
  ④ 新增用户管理页无令牌/层次验收，有 Ant 默认表堆砌回退风险。
  故 REQUEST_CHANGES；不代替其他角色批准，不关闭他人 ISSUE。
```

## 评审基线

| 项 | 值 |
|---|---|
| 角色 | `uxUiPlanner`（`ai/agents/ux-ui-planner.md`） |
| 计划 | `planning/proposals/PLAN-WSC-5.1.md`（`CANDIDATE` / `DRAFT`，round 1） |
| 需求权威 | `product/requirements/SNAP-WSC-005.md`（`IN_REVIEW`） |
| UX 基线 | `product/requirements/SNAP-WSC-003.md`（`REQ-UX-001..011` 继承不回退） |
| 契约 UI 状态 | `contracts/ui/state-matrix.md`（当前仍标 `wsc-contracts@2.1.0` 导入宿主=目录页） |
| 预落地抽样（对账证据，非批准依据） | `WorkbenchLayout.vue` 侧栏已有我的产品/用户管理/`role-line`；`RoleSwitcher.vue` 仍为可点 listbox；`CatalogBrowsePage` 已有 `showSeatMap=!isMine` |

## 对照结论（本轮焦点）

| 焦点 | 计划覆盖 | 结论 |
|---|---|---|
| 侧栏菜单按角色 | §4 矩阵；602 用户管理；603 我的产品；占位未开放菜单 deny 越界 | **能力表够用**；菜单序/双目录 active 与增改导返回表面未冻（ISSUE-002） |
| 角色绑定 + 下线切角色 | 602 acceptance：API 禁用 + UI 结构不可达；§0.3 提示 RoleSwitcher 残留 | **安全意图通过**；**只读替换 UX 未验收**（ISSUE-001） |
| 座序图 Top5 / hover | 604：Top5、比例座位、hover 熄灭其它、真实 `totalProducts`、仅公共目录、失败不崩 | **主交互意图通过**；缺空/载/leave/挂载位（ISSUE-003） |
| 我的产品 vs 公共目录 | 603：PROVIDER 可见；无座序图；公共无增改导；导入迁入 | **入口分离意图通过**；页头/返回/导入关闭表面未钉死（ISSUE-002） |
| UX 基线不回退 | §1.5 令牌/壳层/导入四态；禁止另起色板；§9 门禁 | **声明正确**；用户管理新页与座序图块缺质感验收锚点（ISSUE-003/004） |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-UX-WSC5-R1-001 | P1 | SNAP-WSC-005 `REQ-SHELL-001` 要求登录身份绑定角色并下线自由切角色；继承 `REQ-UX-002`/`REQ-UX-008` 仍描述侧栏「角色切换区 / 角色切换下拉」。PLAN §0.3/602 写「RoleSwitcher → 只读」「无可点切换」，但未冻结替换形态：①无 listbox/chevron/可切换 affordance（禁止仅 `disabled` 保留下拉观感）；②只读角色文案仍落在侧栏 footer 企业卡内并消费既有侧栏令牌；③书面声明 `REQ-UX-008` 中「可切换下拉」段由本版 `REQ-SHELL-001` **supersede**，只读展示质感仍须对齐壳层（非默认灰字裸文本）。预落地：`RoleSwitcher.vue` 仍为完整可点菜单，而 `WorkbenchLayout` 已另写 `role-line`——计划未规定以何者为权威验收面，易双控件或半残留下拉。 | 在 §1.5 与/或 TASK-WSC-602 `acceptance`（建议同步 `testScope`）显式冻结：**(A)** 壳层 footer **唯一**角色展示为只读（无下拉、无 `aria-haspopup=listbox`、无选项菜单）；**(B)** 视觉：侧栏令牌内可读角色标签（可保留企业卡层次）；**(C)** 删除或永不挂载可切换 `RoleSwitcher`（结构不可达）；**(D)** 一句 supersede：`REQ-UX-008` 交互切换段以 SHELL-001 为准，质感不回退。Vitest/走查可断言无切换 trigger。 | REQ-SHELL-001, REQ-UX-002, REQ-UX-008, REQ-RBAC-001 |
| ISSUE-UX-WSC5-R1-002 | P1 | SNAP `REQ-SHELL-001`/`REQ-CAT-010`：PROVIDER「我的数据产品」与三角色「数据目录」为两入口；公共目录无增改导；导入迁至我的产品。PLAN 603/§0.3 承认路由可能仍挂 `/catalog/products/*`，acceptance 未冻：①两表面页头标题/空态文案可区分；②侧栏 active：`/my-products/**` 不高亮「数据目录」，反之亦然；③新增/编辑/导入成功或关闭后的**返回表面**为 `/my-products`（非误回公共目录）；④601 更新的 `contracts/ui/state-matrix.md` 须把导入宿主从「目录页弹窗→回目录」改为「我的产品宿主→回我的产品」，并覆盖双表面 loading/empty。现行 `state-matrix.md` 仍写「导入 UI 载体：目录页弹窗…关闭后回到目录浏览表面」（2.1.0），与 V1.4 冲突；计划 §3.1 仅列「侧栏可见性/只读/写入口/角色只读」，未点名修正导入宿主返回点。 | 在 §1.5 或 601 `ui/state-matrix` acceptance + 603 `acceptance`/`testScope` 冻结：菜单文案「数据目录」「我的数据产品」；双表面页头与空态；导航 active 互斥规则；写路径与导入弹窗关闭/成功后回到**我的数据产品**；公共目录结构不可达增改导；state-matrix 导入宿主与返回表面与 SNAP 一致。E2E/Vitest 至少覆盖 PROVIDER：公共无写入口 → 我的产品可写 → 返回仍在我的产品。 | REQ-SHELL-001, REQ-CAT-010, REQ-CAT-001, REQ-RBAC-001, REQ-UX-004, REQ-UX-006, REQ-UX-009 |
| ISSUE-UX-WSC5-R1-003 | P1 | SNAP `REQ-CAT-009`：L2 Top5、比例座位、悬停熄灭其它、真实总数、仅公共目录。PLAN 604 覆盖主句与「加载失败可读」，但未冻交互状态机与目录 IA：①默认态 / hover 熄灭（弱化方式可测，如其它域 opacity 上限）/ `mouseleave`（或等价）恢复；②`totalProducts=0` 或 items 空的可读空态（非空白崩布局）；③loading 与目录列表加载可并存且不破坏 `REQ-UX-004` 标签/列表层次；④挂载位置（目录主路径内一块流通链可视化，§1.5）须写明相对页头/标签栏的区域，避免挤掉标签/分组或引入第二套仪表盘语言；⑤明确不实现点击筛选（已 Out of Scope）时 hover 不得冒充可点击筛选（cursor/role 不误导）。 | 在 TASK-WSC-604 `acceptance`（建议 `testScope`）增加可测条款：Top5 截断；比例座位；hover 熄灭 + leave 恢复；空态文案；loading；失败不崩；仅 `/catalog` 挂载、`/my-products` 不挂；挂载区相对目录页头/标签的布局约束（令牌内 card 层次）；无点击筛选且无筛选 affordance。 | REQ-CAT-009, REQ-CAT-001, REQ-UX-004, REQ-UX-009 |
| ISSUE-UX-WSC5-R1-004 | P2 | SNAP `REQ-USER-001` 新增 ADMIN 用户管理；继承 `REQ-UX-001`/`REQ-UX-005`/`REQ-UX-009` 要求主路径页具备令牌层次与统一空载反馈。PLAN 602 验收偏 CRUD/权限，**未**要求 `UsersAdminPage`：白卡片/页头层次、列表空态与加载、主次按钮、禁止「默认 Ant 表格堆砌」无层次。预落地页已存在，存在 UX 回退窗口。 | 在 TASK-WSC-602 `acceptance` 增补用户管理页最低视觉/状态条款：消费既有 CSS 变量；页头+列表卡层次；loading/empty/error 可读；非 ADMIN 结构不可达。无需重做令牌。 | REQ-USER-001, REQ-UX-001, REQ-UX-005, REQ-UX-009 |

## 非阻塞观察（不升 ISSUE）

- §1.5 / Out of Scope「不重做 UX 令牌 / 禁止第二套 UI 框架」与 SNAP 继承口径一致，**通过**。
- 三角色菜单可见性矩阵（§4）与「隐藏≠授权」写入硬门禁，体验与安全边界对齐，**通过**（实现正确性交 security/qa）。
- 座序图点击筛选 Out of Scope 已声明，避免范围蔓延，**通过**。
- REQ-UX-011 差距清单未要求为本版新建完整原型对照包；建议修订时把「座序图 / 我的产品 / 只读角色 / 用户管理」四条走查键列入 §6.1 或发布门禁抽样——本轮不单独开单（P2 用户页已覆盖部分风险）。
- SNAP 仍 `IN_REVIEW`：本评审按正文范围评审计划可调度性；不代替快照正式批准。

## 决策

`REQUEST_CHANGES` — 关闭上表 ISSUE-001..003（P1）前本角色不改为 `APPROVE`；ISSUE-004（P2）建议同轮修订一并关闭。  
本实例 **不** 代写计划补丁、**不** 伪造他角色 APPROVE、**不** 写入 `ai/runs/**`。
