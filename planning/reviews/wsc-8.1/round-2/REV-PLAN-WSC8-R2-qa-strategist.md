# Round 2 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC8-R2-qaStrategist
planId: PLAN-WSC-8.2
round: 2
role: qaStrategist
actorInstance: qa-strategist-wsc-011-r2
snapshotIdAtReview: SNAP-WSC-008
runId: RUN-WSC-011
decision: APPROVE
summary: |
  相对 PLAN-WSC-8.1 Round 1 本角色八条 ISSUE（ISSUE-QA-WSC8-R1-001..008）的 closeWhen
  均已在 PLAN-WSC-8.2 字面满足，可关闭。P1 四项（USER 深链负例、座序图 cross-enterprise
  parity、905 §4 单元格 403 分项、ADMIN 双入口 N vs M scope 分离）均已机械写入对应任务
  testScope 与 §6.1 强制场景；P2 四项（Cascader 边界、§0.2 命名用例、totalProducts 同口径、
  query 无 enterpriseName）亦已闭合。908 独占 E2E；§6.1 九场景强制并钉死
  evidenceId/命令/报告落点。本领域无未关闭 P1、无新增异议；未执行任何测试；不宣称实现或
  E2E 已通过；本 APPROVE 仅表示计划层可测性门禁对本角色已闭合。
closedIssues:
  - ISSUE-QA-WSC8-R1-001
  - ISSUE-QA-WSC8-R1-002
  - ISSUE-QA-WSC8-R1-003
  - ISSUE-QA-WSC8-R1-004
  - ISSUE-QA-WSC8-R1-005
  - ISSUE-QA-WSC8-R1-006
  - ISSUE-QA-WSC8-R1-007
  - ISSUE-QA-WSC8-R1-008
openIssues: []
```

## Round 1 ISSUE closeWhen 确认

| id | severity | closeWhen 结论 | 证据（PLAN-WSC-8.2） |
|---|---|---|---|
| ISSUE-QA-WSC8-R1-001 | P1 | **关闭** | **906 `testScope`**：Vitest §4.1 三角色菜单正例+负例表；**USER 负例**显式列出菜单不可见 + 深链结构不可达 — `/catalog/maintenance`、`/my-catalog`、`/my-products`、`/admin/users`。PROVIDER 深链 `/catalog/maintenance` 不可达同列。**§6.1 场景 6** 强制：USER 菜单隐藏 + 上述四路由深链不可达；908 场景勾选表同步。 |
| ISSUE-QA-WSC8-R1-002 | P1 | **关闭** | **902 `testScope`**：Vitest **cross-enterprise parity** — 两不同 enterprise 用户同一 L1，L2 卡片集合与 `GET /catalog/l2-distribution` 结果**一致**；后端 `?l1CategoryId=` 集成测无 enterprise scope 参数；负例：请求/响应无 enterprise 过滤字段。**§6.1 场景 2** 强制「**两用户 cross-enterprise 座序图一致**」。 |
| ISSUE-QA-WSC8-R1-003 | P1 | **关闭** | **905 `testScope`**：**§4 单元格分项表**（每格正例+负例）：USER — mine/产品写/导入/maintenance → **403**；PROVIDER — 他人 create_by 写 **403**、`scope=full` maintenance **403**、myCatalog **200** 仅本人；ADMIN — 本企业写 **200**、跨企业写 **403**、full maintenance **200** 全平台、myCatalog **200** 本企业；全链 — 两 enterprise 用户同一 query 结果集**相同**。集成含篡改 enterprise 参数、空 create_by 负例 seed。 |
| ISSUE-QA-WSC8-R1-004 | P1 | **关闭** | **907 `testScope`**：**ADMIN 双入口 scope 分离** — 固定 seed 下 ADMIN 打开 `/catalog/maintenance` 见**全平台 N 条**、`/my-catalog` 见**本企业 M 条**（M≤N；跨企产品仅出现在前者）。**§6.1 场景 5** 强制「**深链 scope 分离**（N vs M）」；场景 7 维护 UX 分离；908 须覆盖。 |
| ISSUE-QA-WSC8-R1-005 | P2 | **关闭** | **901 `testScope`**：Vitest Cascader — 选 L1 → 有 `l1CategoryId` 无 `l2CategoryId`；选 L2 → 两者皆有；**清空 → 两者皆无**；**catalog + mine mode** 参数化同一套断言（含 Cascader 边界）。`acceptance` 同口径。 |
| ISSUE-QA-WSC8-R1-006 | P2 | **关闭** | 各任务 `testScope` 均含 **§0.2 对账**命名用例行：`901-no-nav-cards` / `901-cascader-l1-l2-clear` / `901-no-industryCategory-query`；`902-seatmap-l1-dropdown` / `902-no-enterprise-filter`；`904-session-enterprise-id`；`905-mine-enterprise-scope` / `905-provider-maintenance-403`；`906-nav-my-catalog-submenu` / `906-provider-no-maintenance-menu`；`907-my-catalog-scope-split` / `907-industry-category-regression`。**903** 以契约 lint / matrix 交叉检查 / 对账清单（VERSION/matrix/OpenAPI 漂移）作为等价自动化证据，可接受（契约任务无 FE Vitest）。 |
| ISSUE-QA-WSC8-R1-007 | P2 | **关闭** | **902 `acceptance`**：选具体 L1 时 `totalProducts` 与 L1 下卡片计数**同口径**（由「建议」升格为硬验收）。**902 `testScope`**：Vitest `totalProducts` 与 L1 卡片计数一致。**§2.2 OQ-V16-003** 标注 902 testScope 强制。 |
| ISSUE-QA-WSC8-R1-008 | P2 | **关闭** | **901 `acceptance` + `testScope`**：query **无** `enterpriseName`。**905 `testScope`** 回归：query **无** `enterpriseName`。**§6.1 场景 3** 强制：query **无** `enterpriseName`（与全链 cross-enterprise 一致场景同勾）。 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 2 本领域无新增异议；R1 八条均可关闭。 | — | — |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 901–908 均有 acceptance、testScope、riskTags；§6.2 要求独立 tester + securityReviewer / migrationReviewer。R1 完整性/机械度缺口已闭合。 |
| E2E §6.1 场景 | 九场景**强制**；908 独占 `tests/e2e/**`；`evidenceId: TESTRUN-WSC-E2E-V16`、命令 `test:p0-v16`、报告落点、v14/v15 共存已钉死。USER 负例、座序图 cross-enterprise、ADMIN N vs M 均已入强制场景。 |
| 浏览 Cascader（901） | l1/l2/清空边界、双 surface 参数化、去 industryCategory、无 enterpriseName 负例齐全。 |
| 座序图 L1（902） | 下拉/Top5/hover/cross-enterprise parity/totalProducts 同口径/无 enterprise 控件/API 负例可支撑 REQ-CAT-015/018。 |
| Enterprise scope 负例 | 905 §4 单元格分项表 + §6.1 场景 3/4；篡改 enterprise 参数、空 create_by 负例成对。 |
| ADMIN 双菜单深链 | §4.1、906 acceptance、907 N vs M seed、§6.1 场景 5/7 覆盖菜单层与 scope 分叉。 |
| 三角色矩阵 | §4.1 表完整；906 引用 §4.1 + USER/PROVIDER 深链负例机械化。 |
| 预落地对账 | §0.2 表 + 各任务命名用例/903 对账清单；§1.4「不以代码已在代替测试证据」声明保留。 |
| 写集/E2E 互斥 | 908 独占 e2e；901∥902 互斥；禁止 903∥907、905∥906 串行——无并行写冲突。 |
| 环境 vs 质量 | SNAP 已 APPROVED；本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 契约/单测 | 集成 | Vitest UI | E2E（§6.1） | 缺口 |
|---|---|---|---|---|---|
| REQ-CAT-014 | — | — | 901 无卡片/toggle/industryCategory；Cascader 边界 | 场景 1 | 无（计划层） |
| REQ-CAT-015 | 903 `l1CategoryId` | 902 `?l1=` | 902 下拉/Top5/cross-enterprise/totalProducts | 场景 2 | 无（计划层） |
| REQ-CAT-016 | 903 mine 语义 | 905 mine 本企业 | 906 useCanWrite | 场景 4 | 无（计划层） |
| REQ-CAT-017 | 903 myCatalog scope | 907 maintenance scope | 907 N vs M scope split | 场景 5、7 | 无（计划层） |
| REQ-CAT-018 | OpenAPI 描述 | 905 全链无 filter；902 无 enterprise param | 902 cross-enterprise parity | 场景 2、3 | 无（计划层） |
| REQ-CAT-019 | — | — | 901 浏览无字段；907 industry 回归 spec | 场景 8 | 无（计划层） |
| REQ-SHELL-009 | matrix（903） | — | 906 §4.1 菜单 + USER 深链 | 场景 5–6 | 无（计划层） |
| REQ-SHELL-010 | state-matrix（903） | — | 906 企业信息区 | — | P1 不阻塞 A/B；可接受 |
| REQ-USER-002 | 903 enterprise 字段 | 904 会话/多种 ADMIN | 904 用户 CRUD | 场景 4（间接） | 无（计划层） |
| REQ-RBAC-002 | matrix（903） | 905 scope/403 分项 | 906/907 门控 | 场景 4–6 | 无（计划层） |

## 说明

- 输入：`planning/proposals/PLAN-WSC-8.2.md`；R1 异议源：`planning/reviews/wsc-8.1/round-1/REV-PLAN-WSC8-R1-qa-strategist.md`；需求权威：`product/requirements/SNAP-WSC-008.md`（APPROVED）。
- 角色装载：`ai/agents/qa-strategist.md`；模板：`ai/schemas/templates/planning-review.md`。
- 对照先例：`planning/reviews/wsc-5.2/round-2/REV-PLAN-WSC5-R2-qa-strategist.md`（R2 APPROVE 关闭 R1 ISSUE 格式）；`planning/archived/v1.5/PLAN-WSC-6.2.md`（三角色/深链/supplierName 负例）。
- **未**修改计划、源码或 Run `state.yaml` / `events.jsonl`。
- **未**执行 Vitest / JUnit / Playwright；**未**代替 developer/tester 宣称通过。
- 决策：`APPROVE`（本角色 R1 ISSUE-QA-WSC8-R1-001..008 全部关闭且无新增 P1）；关闭结论均有计划正文与任务字段字面证据，非伪造。
- 只关闭本角色 ISSUE；不代关其他角色 ISSUE。
