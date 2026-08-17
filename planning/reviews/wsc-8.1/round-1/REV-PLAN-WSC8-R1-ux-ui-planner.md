# 规划评审 — UX/UI（PLAN-WSC-8.1 Round 1）

```yaml
reviewId: REV-PLAN-WSC8-R1-uxUiPlanner
planId: PLAN-WSC-8.1
round: 1
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-008
actorInstance: ux-ui-planner-wsc-011-r1
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-008 与 UX 基线 SNAP-WSC-003 / PLAN-WSC-6.2 §1.5：浏览去卡片、高级区常显、
  Cascader l1/l2、座序图 L1 下拉、ADMIN 双维护入口、PROVIDER 仅我的目录、数据目录可收起、
  令牌不回退等 PO 锁定意图在 §1/§4/901–902/906 中方向正确。但仍有多处体验验收未冻住，
  阻碍本角色 APPROVE：① 浏览 Cascader 交互/文案/状态与 REQ-UX-004 标签栏 supersede 未写入
  901 acceptance；② 901/902 缺 §1.5 Ant→token 可勾选断言（含新 Cascader 与座序图 L1 控件）；
  ③ 906 未钉死「我的目录」归位数据目录子菜单、菜单序、双维护 active/页头区分及路径统一；
  ④ 902 缺 L1 下拉布局/切换 loading 与 OQ-V16-003 totalProducts 口径冻结。故 REQUEST_CHANGES。
```

## 评审基线

| 项 | 值 |
|---|---|
| 角色 | `uxUiPlanner`（`ai/agents/ux-ui-planner.md`） |
| 计划 | `planning/proposals/PLAN-WSC-8.1.md`（`CANDIDATE` / `DRAFT`，round 1） |
| 需求权威 | `product/requirements/SNAP-WSC-008.md`（`APPROVED`） |
| UX 基线 | `product/requirements/SNAP-WSC-003.md`（`REQ-UX-001..011` 继承不回退） |
| 功能 UX 冻结面 | `planning/archived/v1.5/PLAN-WSC-6.2.md` §1.5（Ant→token 最小映射集） |
| 预落地抽样（对账证据，非批准依据） | `CatalogBrowsePage.vue` 仍含 `nav-card` 空间/行业 tag 行与 native 筛选；`BrowseFilterBar.vue` 已存在 Ant+industryCategory 但未挂载；`CirculationSeatMap.vue` 无 L1 下拉；`WorkbenchLayout.vue` 含可收起「数据目录」但「我的目录」挂于组外且路由为 `/my-maintenance` |

## 对照结论（本轮焦点）

| 焦点 | 计划覆盖 | 结论 |
|---|---|---|
| 浏览去卡片 + 高级常显 + Cascader | 901 objective/acceptance；§1.2 PO 锁定 | **意图通过**；Cascader 交互/文案/空载与 REQ-UX-004 supersede 未冻（ISSUE-001） |
| 浏览去 industryCategory | 901 + 907 回归分工 | **通过** |
| 座序图 L1 下拉 | 902；§6.1 #2 | **意图通过**；布局/token/切换态/totalProducts 口径未冻（ISSUE-002） |
| ADMIN 双入口 + PROVIDER 仅我的目录 | §4 / 906 / 907；PO-CONFIRM-011-001 | **矩阵够用**；侧栏 IA 归位与双维护 surface 区分未冻（ISSUE-003） |
| 数据目录可收起 | §1.2 / REQ-SHELL-009 继承 | **声明继承**；906 缺非回归验收锚点（ISSUE-004） |
| Ant token 不回退 | §1.5 继承 6.2 §1.5 | **声明正确**；901/902 缺可勾选 token 断言（ISSUE-002/005） |
| `/catalog` vs `/my-products` 筛选同源 | 901 acceptance；§1.5 双表面 | **意图通过**；mine 空态文案仍指向 create_by（观察项，不单独开单） |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-UX-WSC8-R1-001 | P1 | SNAP `REQ-CAT-014` / PRD §1.4–1.5：列表区移除业务视图/业务大类卡片，高级区常显，以 **Cascader** 联动 l1/l2，文案仍为「业务视图/业务大类」。PLAN 901 覆盖移除卡片、无 toggle、Cascader 映射 query，但**未冻结**可测交互：① Cascader 标签/placeholder（如「业务视图 / 业务大类」）与 `labels.ts` 常量；② 仅选 L1 → 只发 `l1`、选到 L2 → 发 `l1`+`l2`、清空 → 两者皆空；③ 分类树 loading/error/空选项可读反馈（REQ-UX-009）；④ 移除 tag 行后须书面 **supersede** `REQ-UX-004` 中「空间/行业标签栏 active 蓝底」段——以 Cascader + 常显 filter-bar 为准，避免实现者与走查仍按 V1.5 标签栏验收。预落地：`nav-card`/`catalog-space-tags`/`catalog-industry-tags` 仍在页内。 | 在 §1.5 和/或 TASK-WSC-901 `acceptance` + `testScope` 显式冻结：**(A)** 无列表区卡片/testid；**(B)** 无 advanced toggle、filter 区默认可见；**(C)** Cascader 文案/placeholder/allowClear/部分选中 query 映射；**(D)** 分类选项 loading/empty/error；**(E)** 一句 supersede：`REQ-UX-004` 标签栏由 `REQ-CAT-014` 取代。Vitest 参数化 catalog + mine。 | REQ-CAT-014, REQ-CAT-019, REQ-UX-004, REQ-UX-009 |
| ISSUE-UX-WSC8-R1-002 | P1 | SNAP `REQ-CAT-015`：座序图标题**右侧** L1 下拉（含「全部」默认），卡片随 L1 过滤，无企业筛；继承 V1.5 hover/leave/空态/loading。PLAN 902 覆盖主句与 V1.5 不回退，但缺：① 标题行布局（h2 左、下拉右、`flex` 对齐）与控件类型（Ant `Select` 或等价）；② L1 切换时 loading/错误不崩布局；③ **独立状态**：浏览 Cascader 与座序图 L1 **不联动**（各自过滤列表 vs 卡片），须在 acceptance 写清以免 901∥902 并行时误做双向绑定；④ OQ-V16-003：`totalProducts` 选 L1 时改为该 L1 子集计数（建议与卡片同口径）——当前仅「建议验收勾选」，P0 体验易漂移。预落地：`CirculationSeatMap.vue` title-block 无下拉。 | 在 TASK-WSC-902 `acceptance` + `testScope` 冻结：标题右 L1 控件布局/testid；默认「全部」= V1.5 Top5；选 L1 后卡片/图例/filter API 同 L1；切换 loading；**不与** browse Cascader 双向同步；`totalProducts` 口径（采纳 OQ-V16-003 建议或 PO 书面例外）。负例：无 enterprise 控件。 | REQ-CAT-015, REQ-CAT-018, REQ-UX-004, REQ-UX-009 |
| ISSUE-UX-WSC8-R1-003 | P1 | SNAP `REQ-SHELL-009` / `REQ-CAT-017`：「数据目录」**可收起**子菜单；子项含全链、**我的目录**（ADMIN+PROVIDER）、我的数据产品；**ADMIN 另有**「目录维护」（全量）；PROVIDER **无**目录维护。PLAN 906 写导航重排与 §4.1 三角色用例，但未冻：① **IA 归位**：「我的目录」须为 `nav-catalog-submenu` **内**子项，不得挂在一级组外（预落地 `/my-maintenance` 在组外）；② **菜单顺序**建议冻结：全链 → 我的目录 → 我的数据产品 →（仅 ADMIN）目录维护，或 PO 确认序；③ **路径统一**：全计划单一最终路径（`/my-catalog` 或 PO 确认名），906/907 acceptance 写死，对账 void 前占位；④ **active 互斥**：`catalogGroupActive` 含 my-catalog 路由；`/catalog/maintenance` vs `/my-catalog` 与全链 `/catalog`、mine `/my-products` 四 surface 的侧栏 active/页头标题可区分（ADMIN 双维护入口防混淆）；⑤ 页级 guard 与菜单可见性一致。 | 在 TASK-WSC-906 `acceptance` + `testScope` + 建议 §6.1 #5/#6 增补：子菜单结构/testid；菜单序；统一路由名；四 surface active 规则；ADMIN 见「目录维护+我的目录」、PROVIDER 仅「我的目录」且维护深链不可达；页头或 meta.title 可区分「目录维护（全量）」与「我的目录（本企业/本人）」。 | REQ-SHELL-009, REQ-CAT-017, REQ-RBAC-002, REQ-UX-002 |
| ISSUE-UX-WSC8-R1-004 | P2 | SNAP `REQ-SHELL-009` 继承 V1.5：「数据目录」一级可展开/收起。PLAN §1.2 提及继承，906 acceptance 偏角色可见性，**未**钉非回归：① `aria-expanded` / `aria-controls`；② `sessionStorage` 持久化展开态（预落地 `CATALOG_GROUP_OPEN_KEY`）；③ chevron 旋转与 `v-show` 子菜单；④ 收起时子项不可达但激活路由仍高亮组头。 | 在 TASK-WSC-906 `acceptance` 或 `testScope` 增加可收起组非回归条款（可复用 V1.5 WorkbenchLayout spec 模式）。 | REQ-SHELL-009, REQ-UX-002 |
| ISSUE-UX-WSC8-R1-005 | P1 | PLAN §1.5 继承 PLAN-WSC-6.2 **Ant→token 最小映射集**，并声明 Cascader/L1 下拉须映射、禁止未映射默认皮肤。但 TASK-WSC-901/902 的 `acceptance`/`testScope` **缺少** V1.5 705/706 同等「可勾选：令牌/ConfigProvider 映射断言或 `data-*-theme` 键」；新引入 **Cascader** 与座序图 **L1 Select** 是高风险回退点。预落地 `BrowseFilterBar.vue` 使用硬编码 hex token 非常量 `--blue`/`--border-color`/`--card-bg`。 | 在 §1.5 或 901/902 `acceptance` + `testScope` 增补：**(A)** 浏览 Cascader、其余 Ant 筛选项、座序图 L1 控件均经 `ConfigProvider` 或 scoped 消费既有 CSS 变量；**(B)** 禁止未映射 Ant 默认蓝/灰主导筛选条与座序图标题行；**(C)** 可勾选 DOM 标记或 Vitest 断言（如 `data-browse-filter-theme="ant-token-mapped"` 类模式）。 | REQ-UX-001, REQ-UX-004, REQ-UX-005, REQ-CAT-014, REQ-CAT-015 |

## 非阻塞观察（不升 ISSUE）

- §1.5「双目录表面 `/catalog` 与 `/my-products` 筛选同源」与 901 参数化测试意图一致，**通过**。
- 座序图点击不联动列表、无企业筛 UI 已在 Out of Scope / 902 负例，**通过**。
- REQ-SHELL-010 企业只读展示标 P1 且不阻塞 A/B，与 SNAP 优先级政策一致，**通过**。
- mine 空态文案（`MINE_EMPTY_MESSAGE` 仍写「本人创建」）随 REQ-CAT-016 本企业 scope 可能需 901/905 交付时更新——建议 developer 对账，本轮不单开 ISSUE。
- 901∥902 文件互斥与座序图挂载容器「不得破坏」已声明，利于保留 V1.5 滚动折叠座序图槽位，**通过**。

## 决策

`REQUEST_CHANGES` — 关闭上表 ISSUE-001、002、003、005（P1）前本角色不改为 `APPROVE`；ISSUE-004（P2）建议同轮修订一并关闭。  
本实例 **不** 代写计划补丁、**不** 伪造他角色 APPROVE、**不** 写入 `ai/runs/**`、**不** 修改 `state.yaml` / `events.jsonl`、**不** commit。
