# 规划评审 — UX/UI（PLAN-WSC-8.2 Round 2）

```yaml
reviewId: REV-PLAN-WSC8-R2-uxUiPlanner
planId: PLAN-WSC-8.2
round: 2
role: uxUiPlanner
snapshotIdAtReview: SNAP-WSC-008
actorInstance: ux-ui-planner-wsc-011-r2
decision: APPROVE
summary: |
  作为 ISSUE-UX-WSC8-R1-001..005 原提出者，对照 PLAN-WSC-8.2 逐条核验 closeWhen。
  §1.5 REQ-UX-004 supersede、901 Cascader 文案/placeholder/query/loading·empty·error 与
  catalog+mine 参数化 testScope；902 座序图 L1 布局/独立状态/totalProducts 口径/token 断言；
  906 IA 归位/菜单序/路由 /my-catalog/四 surface active·页头区分/可收起组非回归；901/902
  Ant→token 可勾选断言均已冻结。五条 ISSUE closeWhen 均已满足，本角色 APPROVE。
  无新增异议；不代替他角色批准。
```

## 评审基线

| 项 | 值 |
|---|---|
| 角色 | `uxUiPlanner`（`ai/agents/ux-ui-planner.md`） |
| 计划 | `planning/proposals/PLAN-WSC-8.2.md`（`CANDIDATE` / `DRAFT`，round 2） |
| 需求权威 | `product/requirements/SNAP-WSC-008.md`（`APPROVED`） |
| UX 基线 | `product/requirements/SNAP-WSC-003.md`（`REQ-UX-001..011` 继承不回退） |
| 功能 UX 冻结面 | `planning/archived/v1.5/PLAN-WSC-6.2.md` §1.5（Ant→token 最小映射集） |
| R1 对照 | `planning/reviews/wsc-8.1/round-1/REV-PLAN-WSC8-R1-ux-ui-planner.md` |
| 预落地对账（§0.2） | `nav-card`/标签栏待 901 移除；`/my-maintenance` 待 906 迁移 `/my-catalog`；座序图 L1 下拉待 902 |

## closeWhen 核验（ISSUE-UX-WSC8-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-UX-WSC8-R1-001 | P1 | §1.5 和/或 901 冻结：(A) 无列表区卡片/testid；(B) 无 advanced toggle、filter 常显；(C) Cascader 文案/placeholder/allowClear/部分选中 query 映射；(D) 分类 loading/empty/error；(E) supersede `REQ-UX-004` 标签栏；Vitest 参数化 catalog + mine | **(A)(B)** 901 acceptance「无业务视图/业务大类卡片与 active 标签行」「无 toggle、面板默认可见」；testScope「无卡片 DOM/testid；无 advanced toggle」；**(C)** acceptance Cascader placeholder/标签「业务视图/业务大类」、`labels.ts` writeSet；L1/L2/clear query 映射；testScope Cascader 边界 + `901-cascader-l1-l2-clear`；**(D)** acceptance「loading/error/空选项可读反馈」；testScope「分类 loading/empty 态」；**(E)** §1.5「REQ-UX-004 supersede…以 Cascader + 常显 filter-bar 取代」；901 acceptance 同句 supersede；**(mine)** testScope「catalog + mine mode 参数化同一套断言」 | **closeWhen 满足，可关闭** |
| ISSUE-UX-WSC8-R1-002 | P1 | 902 冻结：标题右 L1 布局/testid；默认「全部」= V1.5 Top5；选 L1 后卡片/图例/API 同 L1；切换 loading；**不与** browse Cascader 双向同步；`totalProducts` 口径（OQ-V16-003）；负例无 enterprise 控件 | 902 acceptance「h2 左、L1 控件右（flex）/testid」「全部 + 全部 L1 / 默认全部=Top5」「l2-distribution?l1CategoryId=」「L1 切换 loading 不崩布局」「不与 browse Cascader 双向同步」「totalProducts 与 L1 卡片同口径」「无企业筛选」；§2.2 OQ-V16-003 强制 902 testScope；testScope cross-enterprise parity、`totalProducts` 一致、无 enterprise 负例、`902-seatmap-l1-dropdown` | **closeWhen 满足，可关闭** |
| ISSUE-UX-WSC8-R1-003 | P1 | 906 + §6.1 #5/#6：「我的目录」在 `nav-catalog-submenu` 内；菜单序全链→我的目录→我的数据产品→（ADMIN）目录维护；统一路由；四 surface active；ADMIN 双维护/PROVIDER 无维护；页头或 meta.title 区分全量 vs 本企业/本人 | §0.2 `ROUTE_MY_CATALOG=/my-catalog`；906 acceptance IA/菜单序/四 surface active/页头「目录维护（全量）」vs「我的目录（本企业/本人）」/三角色可见性；testScope §4.1 三角色、USER 深链负例、四 surface active、页头/meta.title；§6.1 #5 ADMIN 双入口 scope 分离 N vs M、#6 PROVIDER 仅我的目录 + USER 深链不可达 | **closeWhen 满足，可关闭** |
| ISSUE-UX-WSC8-R1-004 | P2 | 906 可收起组非回归：`aria-expanded`/`aria-controls`；sessionStorage；chevron；收起时子项不可达但组头高亮 | 906 acceptance 四句齐全；testScope「可收起组非回归（aria/sessionStorage/chevron）」 | **closeWhen 满足，可关闭** |
| ISSUE-UX-WSC8-R1-005 | P1 | §1.5 或 901/902：(A) Cascader/筛选项/L1 经 ConfigProvider 或 scoped CSS 变量；(B) 禁止未映射 Ant 默认蓝/灰；(C) 可勾选 DOM 或 Vitest 断言 | §1.5 Ant→token 最小映射集 + Cascader/L1 Select 须映射；901 acceptance + testScope `data-browse-filter-theme="ant-token-mapped"` 或 ConfigProvider 键；902 acceptance「L1 控件 token 映射」+ testScope token 断言 | **closeWhen 满足，可关闭** |

## 本轮 ISSUE 表

无未关闭 ISSUE。R1 五条均确认可关闭；本轮不新增 ISSUE。

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 非阻塞观察（不升 ISSUE）

- **mine 空态文案**：R1 已记「`MINE_EMPTY_MESSAGE` 仍写本人创建」随 REQ-CAT-016 本企业 scope 可能需 901/905 交付时对账——仍建议 developer 在 901 或后续 HOTFIX 更新，本轮不阻塞 APPROVE。
- **REQ-SHELL-010（P1）**：906 acceptance/testScope 已含侧栏企业只读展示；与 SNAP 优先级「不阻塞 A/B」一致，**通过**。
- **§6.1 E2E**：场景 1–2 覆盖 Cascader 与座序图 L1；token 映射以 901/902 Vitest 断言为主路径，与 V1.5 705/706 模式一致，**通过**。
- 本角色仅就本人 R1 ISSUE 的 closeWhen 与 UX 可验收性给出 `APPROVE`，**不**代替 solutionArchitect / apiDataDesigner / qaStrategist / planEditor / securityOperations 等角色结论，**不**伪造全员共识。

## 决策

`APPROVE` — ISSUE-UX-WSC8-R1-001（P1）、002（P1）、003（P1）、004（P2）、005（P1）的 `closeWhen` 均已在 `PLAN-WSC-8.2` 满足；本角色无剩余异议。  
本实例 **不** 代写计划补丁、**不** 伪造他角色 APPROVE、**不** 写入 `ai/runs/**`、**不** 修改 `state.yaml` / `events.jsonl`、**不** commit。
