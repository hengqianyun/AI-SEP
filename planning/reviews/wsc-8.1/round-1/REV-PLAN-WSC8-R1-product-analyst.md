# 规划评审 — PLAN-WSC-8.1 Round 1（productAnalyst）

```yaml
reviewId: REV-PLAN-WSC8-R1-product-analyst
planId: PLAN-WSC-8.1
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-008
actorInstance: product-analyst-wsc-011-r1
decision: APPROVE
summary: |
  相对 SNAP-WSC-008（status=APPROVED）：十条 REQ（9×P0 + 1×P1）均有主任务与 E2E 映射；
  In/Out Scope、poLockedDefaults（浏览去卡片/常显高级区/Cascader、座序图 L1 无企业筛、
  mine 本企业、ADMIN 双入口全量+本企业、PROVIDER 仅我的目录、无企管页、
  INDUSTRY_CATEGORY_OPTIONS 非浏览复用、全链无企业过滤）与 SNAP/PRD 一致；
  §4 RBAC/菜单三角色矩阵与 SNAP 角色表对齐；波次 A/B 内容与 SNAP 交付波次等价
  （计划以 C/D/E 拆分导航、我的目录页与 E2E，不削弱 REQ 覆盖）；继承 REQ 不回退
  声明与 §6.1 回归场景齐全。本角色无开放 ISSUE → APPROVE。
```

## 评审范围与方法

- **权威**：`SNAP-WSC-008`（`status: APPROVED`，PO 2026-08-17）+ `product/prd/wsc-v1.6-catalog-filter-ux.md`
- **对象**：`planning/proposals/PLAN-WSC-8.1.md`（CANDIDATE / DRAFT）；**不以**工作树预落地代码为批准依据
- **焦点**：P0 REQ 任务覆盖、In/Out Scope、RBAC/菜单（ADMIN 目录维护全量 + 我的目录本企业；PROVIDER 仅我的目录）、波次与 SNAP 对齐、PO 锁定默认值

## 覆盖结论（相对 SNAP-WSC-008）

| 检查项 | 结论 |
|---|---|
| P0 REQ→任务 | §7 九条 P0 均有主任务 + **908** E2E；P1 `REQ-SHELL-010` 归 **906**，符合 SNAP「不阻塞 A/B」 |
| `REQ-CAT-014` 浏览筛选 | **901**：去卡片、常显高级区、无 `industryCategory`、Cascader l1/l2；双表面 `/catalog`∥`/my-products`；**908** #1 |
| `REQ-CAT-015` 座序图 L1 | **902** UI + 可选 BE `l1`；**903** 契约 `l2-distribution?l1=`；无企业筛；**908** #2 |
| `REQ-CAT-016` mine 本企业 | **903**+**905**：mine=本企业、ADMIN 可增改导；acceptance 明示「非仅 create_by 本人」；**908** #4 |
| `REQ-CAT-017` 我的目录 | **903** scope 契约 + **907** 页级复用 REQ-CAT-013 UX；ADMIN 本企业 / PROVIDER create_by；**908** #6/#7 |
| `REQ-CAT-018` 全链无企业过滤 | **902**+**905**：座序图与列表全平台口径；**908** #3 |
| `REQ-CAT-019` 行业类别非浏览 | **901** 浏览去字段 + **907** editor/import/detail 回归 `INDUSTRY_CATEGORY_OPTIONS`；**908** #8 |
| `REQ-SHELL-009` 导航 | **906**：ADMIN 双入口（目录维护全量 + 我的目录本企业）；PROVIDER 无目录维护；**908** #5/#6 |
| `REQ-SHELL-010`（P1） | **906** 侧栏企业只读；Wave C；不纳入 P0 E2E 门禁 — 与 SNAP 优先级政策一致 |
| `REQ-USER-002` / `REQ-RBAC-002` | **903**+**904**+**905**+**906**+**907**：企业归属、一企多 ADMIN、scope 与菜单/API 一致；**908** 场景 3/4/6 |
| In Scope | 与 SNAP §In Scope 及 `poLockedDefaults` 逐项对齐；§1.1 任务表可追溯到上述 REQ |
| Out Scope | 企管页、超管菜单拆分、座序图企业筛/点击联动、浏览 industryCategory、跨页全选、OAuth/多租户、Downloads React、削弱 V1.5 — 与 SNAP Out of Scope 一致 |
| RBAC/菜单 | §4 / §4.1 三角色矩阵与 SNAP「角色与权限摘要」表一致：ADMIN 目录维护**全量**+我的目录**本企业**；PROVIDER **仅**我的目录（create_by）、**无**目录维护；USER 仅全链；隐藏≠授权 + 深链负例在 906/907/908 |
| PO 锁定默认值 | §1.2 / 文首 PO-CONFIRM-011-001 覆盖：`browseRemoveFilterCards`、`advancedFilterAlwaysVisible`、`browseRemoveIndustryCategoryFilter`、`browseCategoryControl: cascader-l1-l2`、`seatMapL1Dropdown`、`seatMapNoEnterpriseFilter`、`mineScope: enterprise`、`myCatalogMenu`、`catalogMaintenanceMenu`、`providerNoCatalogMaintenance`、`fullChainNoEnterpriseFilter`、`noEnterpriseAdminPage`、`industryCategoryEnumReuse` |
| 波次 vs SNAP | SNAP 波次 A/B/C；计划拆为 A/B/C/D/E（D=我的目录页，E=E2E）。**内容等价**：A→901∥902；B→903→904→905（含 SNAP-B 的 USER/RBAC/CAT-016/017 API/CAT-018）；C→906（SHELL-009/010）；SNAP-C 的 CAT-017 路由/013 回归→907；E2E 门禁→908。未遗漏 SNAP 波次内 REQ |
| PROVIDER 双 scope | SNAP 要求 mine=**本企业**、我的目录=**create_by 本人**；905 acceptance/testScope 覆盖 mine 本企业；907 acceptance/testScope 覆盖 myCatalog scope 分离；§8 风险「mine 从 create_by 切到本企业」有 905/908 缓解 — 产品隔离口径完整 |
| 基线不回退 | 功能 SNAP-006 / UX SNAP-003；REQ-CAT-012 supplierName/未分类、REQ-CAT-013 维护 UX、座序图核心、mine 无座序图 — §1.5、§6.1 #9、各任务 deny 与回归声明齐全 |
| 快照门禁 | SNAP-WSC-008 已 APPROVED；本评审以快照正文为权威，计划 `snapshotId` 引用正确 |

## 异议

（本角色无开放 ISSUE。）

## 非阻塞备注（不构成独立 ISSUE）

- 计划 `waveCount: 5`（A–E）与 SNAP `poLockedDefaults.waves: [A, B, C]` 为**编排粒度**差异，非范围差异；委员会若需与 SNAP 字面波次对齐，可在计划元数据或 §1.1 增加「SNAP 波次 ↔ 计划波次」对照表，**不阻碍**本角色 APPROVE。
- 预落地路径 `/my-maintenance` vs 计划倾向 `/my-catalog`：906 acceptance 已要求对账统一命名；实现阶段须全计划单一路径，建议在 codeReview 门禁勾选。
- `REQ-SHELL-010`（P1）未列入 **908** P0 E2E 清单符合 SNAP；建议在 Wave C 交付说明或 staging 清单中单独勾选企业信息区只读展示。
- `OQ-V16-003`（座序图 L1 过滤时 `totalProducts` 口径）已在 902 acceptance 建议勾选；PO 行为已锁「无企业筛」，口径差异不阻塞发布，实现时与卡片同口径即可。

## 决策

`APPROVE` — 相对 SNAP-WSC-008，PLAN-WSC-8.1 Round 1 产品范围、P0 覆盖、RBAC/菜单与 PO 锁定默认值对齐充分；本角色对本 planId 无 `REQUEST_CHANGES` 异议。
