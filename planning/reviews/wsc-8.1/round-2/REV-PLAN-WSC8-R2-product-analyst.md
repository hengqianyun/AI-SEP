# 规划评审 — PLAN-WSC-8.2 Round 2（productAnalyst）

```yaml
reviewId: REV-PLAN-WSC8-R2-product-analyst
planId: PLAN-WSC-8.2
round: 2
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-008
actorInstance: product-analyst-wsc-011-r2
decision: APPROVE
summary: |
  相对 SNAP-WSC-008（status=APPROVED）：十条 REQ（9×P0 + 1×P1）均有主任务与 E2E 映射；
  In/Out Scope、poLockedDefaults、§4 RBAC/菜单三角色矩阵与 SNAP 角色表一致；R2 修订
  （/my-catalog 路由冻结、maintenance scope=full|myCatalog、mine 本企业语义、905/906 互斥）
  为技术/可测澄清，未引入产品范围回归。对照 R1 覆盖结论全部维持；本角色 R1 无开放
  ISSUE → 无 closeWhen 待关；无新增异议 → APPROVE。
closedIssues: []
openIssues: []
```

## 评审范围与方法

- **权威**：`SNAP-WSC-008`（`status: APPROVED`，PO 2026-08-17）+ `product/prd/wsc-v1.6-catalog-filter-ux.md`
- **对象**：`planning/proposals/PLAN-WSC-8.2.md`（CANDIDATE / DRAFT；`basedOn: PLAN-WSC-8.1`）；**不以**工作树预落地代码为批准依据
- **焦点**：对照 R1 本角色覆盖结论复检；核验 R2 修订是否引入 SNAP 范围回归；PO 锁定默认值与 RBAC/菜单口径
- **不做**：代关他角色 ISSUE；代批他角色结论；改写 SNAP

## R1 结论复检

| R1 检查项（REV-PLAN-WSC8-R1-product-analyst） | R2（PLAN-WSC-8.2）判定 |
|---|---|
| 十条 REQ 均有主任务 + **908** E2E | **维持** — §7 映射完整；§6.1 九场景覆盖全部 P0 |
| P1 `REQ-SHELL-010` 归 **906**，不阻塞 A/B | **维持** — 906 acceptance/testScope 含侧栏企业只读；未纳入 P0 E2E，符合 SNAP 优先级政策 |
| In/Out Scope 与 SNAP / `poLockedDefaults` 对齐 | **维持** — §1.1 In Scope、Out of Scope 与 SNAP §Out of Scope 一致；文首 PO-CONFIRM-011-001  thirteen 项默认值均有对应章节 |
| §4 RBAC/菜单与 SNAP「角色与权限摘要」一致 | **维持并加强** — §4 / §4.1 / §3.1 matrix 字面表：ADMIN 目录维护**全量** + 我的目录**本企业**；PROVIDER **仅**我的目录（create_by）、**无**目录维护；USER 仅全链 |
| PROVIDER 双 scope：mine=**本企业**、myCatalog=**create_by 本人** | **维持** — §3.2 推导规则 + 905 acceptance（mine 本企业）+ 907 acceptance（myCatalog scope 分离）；§6.1 #4/#6/#7 可测 |
| 波次 A/B/C 内容等价（计划 A–E 编排拆分） | **维持** — A→901∥902；B→903→904→905；C→906；D→907（SNAP-C 的 CAT-017/013）；E→908 |
| 基线不回退（SNAP-006 / SNAP-003、REQ-CAT-012/013） | **维持** — §1.5、§6.1 #9、各任务 deny 与 V1.5 回归声明齐全 |
| R1 非阻塞备注：`/my-maintenance` vs `/my-catalog` | **已改善** — §0.2 路由冻结 `ROUTE_MY_CATALOG=/my-catalog`；906 acceptance 要求迁移 void 前路径 |
| R1 非阻塞备注：OQ-V16-003 `totalProducts` 口径 | **已改善** — 902 acceptance/testScope 强制与 L1 卡片同口径；§2.2 仍标 non-blocking |

**R1 本角色 ISSUE**：Round 1 无开放 ISSUE（`decision: APPROVE`，异议节为空）→ 本轮无 `closeWhen` 待核验。

## 覆盖结论（相对 SNAP-WSC-008）

| 检查项 | 结论 |
|---|---|
| P0 REQ→任务 | §7 九条 P0 均有主任务 + **908** E2E；P1 `REQ-SHELL-010` 归 **906** |
| `REQ-CAT-014` 浏览筛选 | **901**：去卡片、常显高级区、无 `industryCategory`、Cascader l1/l2；双表面 `/catalog`∥`/my-products`；supersede REQ-UX-004；**908** #1 |
| `REQ-CAT-015` 座序图 L1 | **902** UI + BE `l1CategoryId`；**903** 契约；无企业筛；与 browse Cascader 不联动；**908** #2 |
| `REQ-CAT-016` mine 本企业 | **903**+**905**+**906**：mine=本企业、ADMIN 可增改导；同企异 create_by 可见；**908** #4 |
| `REQ-CAT-017` 我的目录 | **903** `scope=myCatalog` + **907** 页级复用 REQ-CAT-013 UX；ADMIN 本企业 / PROVIDER create_by；双入口 scope 分离；**908** #5/#7 |
| `REQ-CAT-018` 全链无企业过滤 | **902**+**905**：座序图与列表全平台口径；cross-enterprise parity；**908** #3 |
| `REQ-CAT-019` 行业类别非浏览 | **901** 浏览去字段 + **907** editor/import/detail 回归 `INDUSTRY_CATEGORY_OPTIONS`；**908** #8 |
| `REQ-SHELL-009` 导航 | **906**：ADMIN 双入口；PROVIDER 无目录维护；USER 深链不可达；**908** #5/#6 |
| `REQ-SHELL-010`（P1） | **906** 侧栏企业只读；Wave C；不纳入 P0 E2E 门禁 |
| `REQ-USER-002` / `REQ-RBAC-002` | **903**+**904**+**905**+**906**+**907**：企业归属、一企多 ADMIN、scope 与菜单/API 一致；**908** 场景 3/4/6 |
| In Scope | 与 SNAP 交付波次及 `poLockedDefaults` 逐项对齐 |
| Out Scope | 企管页、超管菜单拆分、座序图企业筛/点击联动、浏览 industryCategory、跨页全选、OAuth/多租户、Downloads React、削弱 V1.5 — 与 SNAP 一致 |
| RBAC/菜单 | §4 / §4.1 / §3.1 matrix 与 SNAP 角色表一致；隐藏≠授权 + 深链负例在 906/907/908 |
| PO 锁定默认值 | §1.2 / 文首覆盖全部 thirteen 项 `poLockedDefaults` |
| 快照门禁 | SNAP-WSC-008 已 APPROVED；计划 `snapshotId` 引用正确 |

## R2 修订对产品范围影响（回归检查）

| R2 修订主题 | 产品影响 |
|---|---|
| maintenance `scope=full\|myCatalog` 硬冻结（§3.1） | **澄清 REQ-CAT-017**：ADMIN 全量维护 vs 我的目录本企业/本人，与 SNAP 双入口语义一致；**无**范围扩大 |
| 删除 listProducts 上 myCatalog 叙述 | **正确分离**：我的目录走 maintenance API，符合 SNAP「UX 同 REQ-CAT-013」 |
| `/my-catalog` 单一路径冻结 | **对齐 SNAP**「我的目录」入口；消除 R1 非阻塞路径歧义 |
| mine=true → 本企业（§3.2） | **对齐 REQ-CAT-016**；与 V1.5 create_by-only 明确 supersede |
| 905 denyModify useCanWrite / 906 独占 composable | 技术互斥；**不改变** RBAC/菜单产品口径 |
| §6.1 E2E 扩充（USER 深链、ADMIN N vs M、cross-enterprise parity） | **加强可测性**；覆盖 SNAP 已锁行为，非新增范围 |

**结论**：R2 修订均为 R1 跨角色 ISSUE 吸收下的可测/契约/编排澄清，**未发现**相对 SNAP-WSC-008 或 R1 产品覆盖的范围回归、遗漏或削弱。

## 异议

（本角色无开放 ISSUE；本轮不新开 ISSUE。）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## closedIssues / openIssues

| 集合 | ids |
|---|---|
| closedIssues | （空 — R1 本角色无 ISSUE） |
| openIssues | （空） |

> 仅记录本角色 ISSUE 状态。他角色 R1 ISSUE（SA/QA/PP/PE/UX/API/SEC 等）**不**由本实例关闭或代批。

## 非阻塞备注（不构成独立 ISSUE）

- 计划 `waveCount: 5`（A–E）与 SNAP `poLockedDefaults.waves: [A, B, C]` 仍为编排粒度差异，非范围差异（同 R1 备注）。
- `REQ-SHELL-010`（P1）建议在 Wave C staging 清单单独勾选企业信息区只读展示，执行层质量提示。

## 决策

`APPROVE` — 对照 SNAP-WSC-008 与 R1 覆盖结论，PLAN-WSC-8.2 Round 2 产品范围、十条 REQ 映射、RBAC/菜单与 PO 锁定默认值对齐充分；R2 修订未引入范围回归；本角色对本 planId 无 `REQUEST_CHANGES` 异议。
