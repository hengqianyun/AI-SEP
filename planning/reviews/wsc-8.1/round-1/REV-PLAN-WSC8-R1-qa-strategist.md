# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC8-R1-qaStrategist
planId: PLAN-WSC-8.1
round: 1
role: qaStrategist
actorInstance: qa-strategist-wsc-011-r1
snapshotIdAtReview: SNAP-WSC-008
decision: REQUEST_CHANGES
summary: |
  八任务均有 acceptance + testScope + riskTags；908 独占 E2E 写集；§6.1 九场景强制、
  evidenceId/命令/报告落点已钉死——相对 PLAN-WSC-5.1 回退项已闭合。§4.1 三角色矩阵、
  浏览 Cascader、座序图 L1、ADMIN 双入口在 acceptance/E2E 层方向正确。但：(1) USER 角色
  菜单/深链负例未机械落入 906 testScope 与 §6.1；(2) REQ-CAT-018 座序图跨企业 parity
  仅覆盖「无企业筛 UI」，缺 902/§6.1 强制断言；(3) 905 enterprise scope 负例未分项
  钉死 USER/PROVIDER/ADMIN 跨 scope 403。存在未关闭 P1 → REQUEST_CHANGES。未执行任何
  测试；不宣称实现或 E2E 已通过；不伪造 APPROVE。
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-WSC8-R1-001 | P1 | SNAP §4 / 本计划 §4.1 要求 USER 对「我的目录 / 我的数据产品 / 目录维护 / 用户管理」均不可达。906 `testScope` 深链负例**仅**列 `PROVIDER /catalog/maintenance`；§6.1 场景 6 仅覆盖 PROVIDER（无目录维护、深链维护不可达），**未**含 USER 菜单隐藏与深链结构不可达。对比 PLAN-WSC-6.2 §6.1 #8 显式「PROVIDER/USER 打开 `/catalog/maintenance` 结构不可达」，本版 V1.6 新增 `/my-catalog` 等路由后 USER 负例缺口更大，tester 按字面可漏测仍标 VERIFIED。 | 906 `testScope` 用表（或等价清单）列出 §4.1 **USER 负例单元格**，每格含菜单不可见 + 深链结构不可达，最低：`/catalog/maintenance`、`/my-catalog`（或全计划统一路径）、`/my-products`、`/admin/users`。§6.1 扩场景 6 或增独立场景，**强制** USER 菜单 + 深链负例；908 勾选表同步。 | REQ-SHELL-009, REQ-RBAC-002, REQ-CAT-017 |
| ISSUE-QA-WSC8-R1-002 | P1 | SNAP REQ-CAT-018 验收：「全链**列表与座序图统计**为全平台口径；用户企业不影响全链结果」。§6.1 场景 3 覆盖**列表** cross-enterprise 一致；场景 2 仅「无企业筛 UI / 选 L1 过滤卡片」，**未**要求不同 enterprise 用户座序图 L2 分布/卡片/总数一致。902 `testScope` 有「无 enterprise 控件 / 响应无 enterprise 过滤字段」，但**无**两用户 cross-enterprise fixture 断言；与 PO 锁定「座序图无企业过滤」的发布风险未闭环。 | 902 `testScope` 增集成或 Vitest：至少两不同 enterprise 用户（或同一 USER 与 ADMIN 不同企）对同一 L1 下拉，L2 卡片集合与 `GET /catalog/l2-distribution` 结果**一致**；§6.1 场景 2 或 3 增补座序图 cross-enterprise parity **强制**勾选行。 | REQ-CAT-015, REQ-CAT-018 |
| ISSUE-QA-WSC8-R1-003 | P1 | §4.1 三角色负例表完整，905 `acceptance` 含 ADMIN 跨企业写 403、PROVIDER 不可维护他人产品、全链无企业 filter。但 905 `testScope` 仅写「三角色 mine/全链 scope；ADMIN 写本企业 200、跨企业 403；全链不受 enterprise 影响」——**未**分项钉死 USER 产品写/导入/mine **403**、PROVIDER 维护 API **403**、PROVIDER 跨 `create_by` 写 **403**、mine 列表跨企业产品**不可见**负例。与 PLAN-WSC-5.1 ISSUE-QA-WSC5-R1-003 / PLAN-WSC-6.2 ISSUE-QA-WSC6-R1-003 同类缺口。 | 905 `testScope` 用表列出本任务负责的 §4 单元格，每格含**正例+负例**期望。最低必含：USER — mine 列表/产品写/导入 **403**；PROVIDER — 他人 `create_by` 产品写 **403**、目录维护 API **403**；ADMIN — 本企业写 **200**、跨企业写 **403**；全链 — 两 enterprise 用户同一 query 结果集**相同**（与 §6.1 场景 3 对齐）。 | REQ-RBAC-002, REQ-CAT-016, REQ-CAT-018 |
| ISSUE-QA-WSC8-R1-004 | P1 | REQ-CAT-017 / REQ-SHELL-009 核心：ADMIN **同时**有「目录维护」（**全量**）与「我的目录」（**本企业**），两入口路由/范围独立。906 `acceptance` 与 §6.1 场景 5/7 在 E2E 层有菜单与维护 UX 分离描述，但 906/907 `testScope` **未**机械要求：ADMIN 深链 `/catalog/maintenance` 可见全平台数据 vs `/my-catalog` 仅本企业（列表计数或 seed 产品 ID 差集可观测）。tester 可仅验菜单文案而漏 scope 分叉。 | 907 `testScope`（或 906 + 907 成对）增可勾选行：固定 seed 下 ADMIN 打开目录维护见**全平台** N 条、我的目录见**本企业** M 条（M≤N；含跨企业产品仅出现在前者）；§6.1 场景 5 或 7 增「ADMIN 双入口深链 scope 分离」**强制**勾选。908 场景须覆盖。 | REQ-CAT-017, REQ-SHELL-009, REQ-RBAC-002 |
| ISSUE-QA-WSC8-R1-005 | P2 | 901 `acceptance` 要求 Cascader「选 L1 传 l1；选 L2 传 l2；**清空为不限**」。`testScope` 仅「Cascader 映射 l1/l2」，**未**含清空不传 l1/l2、仅选 L1 不传 l2 边界。浏览 Cascader 为本版 Wave A 关键路径，边界漏测易在 901 VERIFIED 后遗留。 | 901 `testScope` 增：(1) Cascader 清空 → 请求 query **无** l1/l2；(2) 仅选 L1 → 有 l1 **无** l2；(3) `/catalog` 与 `/my-products` 参数化同一断言（已有 mode 声明，补 Cascader 边界两行）。 | REQ-CAT-014 |
| ISSUE-QA-WSC8-R1-006 | P2 | §0.2 预落地对账表（901 卡片/toggle、902 L1 下拉、906 `/my-maintenance` 占位等）与 §1.4「不以代码已在代替测试证据」声明正确，但 901–907 各任务 `testScope` **无**条款要求：对账表声称保留的 P0 行为须由**本任务命名自动化用例**证明。tester 可接受 prose 对账说明即 VERIFIED。 | 各任务 `testScope` 增一行：对本任务 §0.2 对账表 P0 行为，列出命名 Vitest/JUnit 用例（或显式引用 §4.1 单元格 / §6.1 场景号）；交付说明附可定位证据，**禁止**仅 prose「已对账」。 | REQ-CAT-014, REQ-SHELL-009, REQ-CAT-015 |
| ISSUE-QA-WSC8-R1-007 | P2 | OQ-V16-003（座序图选 L1 时 `totalProducts` 是否与 L1 子集同口径）在 902 `acceptance` 为「建议验收勾选」，**未**入 `testScope`；§6.1 场景 2 未点名。实现分叉时无自动化关闭条件。 | 902 `testScope` 增：选具体 L1 后 `totalProducts` 与 L1 下卡片计数**同口径**（或与 API 返回 total 一致）；acceptance 建议项改为 testScope 可勾选行。 | REQ-CAT-015 |
| ISSUE-QA-WSC8-R1-008 | P2 | PLAN-WSC-6.2 ISSUE-QA-WSC6-R1-001 已钉死 `supplierName` vs 会话 `enterpriseName` 分叉负例（query **无** `enterpriseName`）。本版 901 `testScope` 仅「supplierName 仍发出」；905 回归 supplierName/未分类但未继承 enterpriseName 键负例。V1.5 不回退声明存在回归漏测风险。 | 901 或 905 `testScope` 增：浏览 query 键**无** `enterpriseName`；fixture 会话 enterprise 与产品 supplierName 分叉时仅 supplierName 检索生效、**0 误命中**。§6.1 场景 3 或 9 可勾选。 | REQ-CAT-012, REQ-CAT-018 |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 901–908 均有 acceptance、testScope、riskTags；§6.2 要求独立 tester。完整性/机械度缺口见 ISSUE-001..004。 |
| E2E §6.1 场景 | 九场景**强制**；908 独占 `tests/e2e/**`；`evidenceId: TESTRUN-WSC-E2E-V16`、命令 `test:p0-v16`、报告落点、v14 共存已钉死——优于 PLAN-WSC-5.1。USER 负例与座序图 cross-enterprise 见 ISSUE-001/002。 |
| 浏览 Cascader（901） | l1/l2 映射、双 surface 参数化、去 industryCategory 方向正确；边界见 ISSUE-005。 |
| 座序图 L1（902） | 下拉/Top5/hover/无 enterprise 控件/API 负例可支撑 REQ-CAT-015；REQ-CAT-018 可视化路径见 ISSUE-002。 |
| Enterprise scope 负例 | 905 acceptance + §6.1 场景 3/4 方向正确；分项自动化与座序图 parity 见 ISSUE-002/003。 |
| ADMIN 双菜单深链 | §4.1、906 acceptance、§6.1 场景 5/7 覆盖菜单层；深链 scope 分离见 ISSUE-004。 |
| 三角色矩阵 | §4.1 表完整；906 引用 §4.1 菜单测；深链/ scope 分项未全机械 → ISSUE-001/003/004。 |
| 预落地对账 | §0.2 表与 void RUN 说明正确；自动化证据绑定弱 → ISSUE-006。 |
| 写集/E2E 互斥 | 908 独占 e2e；901∥902 互斥；禁止 903∥907——无并行写冲突。 |
| 环境 vs 质量 | SNAP 已 APPROVED；本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 契约/单测 | 集成 | Vitest UI | E2E（§6.1） | 缺口 |
|---|---|---|---|---|---|
| REQ-CAT-014 | — | — | 901 无卡片/toggle/industryCategory；Cascader | 场景 1 | Cascader 边界 ISSUE-005 |
| REQ-CAT-015 | 903 `l1` 契约 | 902 `?l1=` | 902 下拉/Top5/hover | 场景 2 | cross-enterprise parity ISSUE-002；totalProducts ISSUE-007 |
| REQ-CAT-016 | 903 mine 语义 | 905 mine 本企业 | 905 useCanWrite | 场景 4 | USER 403 分项 ISSUE-003 |
| REQ-CAT-017 | 903 myCatalog scope | 907 maintenance scope | 907 scope/Cascader | 场景 7 | ADMIN 双入口深链 scope ISSUE-004 |
| REQ-CAT-018 | OpenAPI 描述 | 905 全链无 filter；902 无 enterprise param | — | 场景 3 | 座序图 parity ISSUE-002 |
| REQ-CAT-019 | — | — | 901 浏览无字段；907 editor/import | 场景 8 | 无（计划层） |
| REQ-SHELL-009 | matrix（903） | — | 906 §4.1 菜单 | 场景 5–6 | USER 深链 ISSUE-001；ADMIN scope ISSUE-004 |
| REQ-SHELL-010 | state-matrix（903） | — | 906 企业信息区 | — | P1 不阻塞 A/B；可接受 |
| REQ-USER-002 | 903 enterprise 字段 | 904 会话/多种 ADMIN | 904 用户 CRUD | 场景 4（间接） | 无（计划层） |
| REQ-RBAC-002 | matrix（903） | 905 scope/403 | 906/907 门控 | 场景 4–6 | 负例分项 ISSUE-001/003 |

## 说明

- 输入：`planning/proposals/PLAN-WSC-8.1.md`；需求权威：`product/requirements/SNAP-WSC-008.md`（APPROVED）。
- 角色装载：`ai/agents/qa-strategist.md`；模板：`ai/schemas/templates/planning-review.md`。
- 对照先例：`planning/reviews/wsc-5.1/round-1/REV-PLAN-WSC5-R1-qa-strategist.md`（E2E 所有权）；`planning/archived/v1.5/PLAN-WSC-6.2.md`（三角色/深链/supplierName 负例）。
- **未**修改计划、源码或 Run `state.yaml` / `events.jsonl`。
- **未**执行 Vitest / JUnit / Playwright；**未**代替 developer/tester 宣称通过。
- 决策：`REQUEST_CHANGES`（P1 未关闭：ISSUE-QA-WSC8-R1-001..004）；验收标准本身可测，故非 `BLOCK`。
- 不代批；ISSUE 关闭须本角色后续轮次对照计划正文字面确认。
