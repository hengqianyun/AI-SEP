# Round 2 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC5-R2-qaStrategist
planId: PLAN-WSC-5.2
round: 2
role: qaStrategist
actorInstance: qa-strategist-wsc-008-r2
snapshotIdAtReview: SNAP-WSC-005
decision: APPROVE
summary: |
  相对 PLAN-WSC-5.1 Round 1 本角色五条 ISSUE（ISSUE-QA-WSC5-R1-001..005）的 closeWhen
  均已在 PLAN-WSC-5.2 字面满足，可关闭。新增 TASK-WSC-605 串行独占 tests/e2e/**，
  §6.1 场景 1–7 强制并钉死 evidenceId/命令/报告目录；601–604 testScope 含预落地对账
  自动化证据定位；602/603 三角色正负例单元格表齐全；603 导入四态回归与空归属负例
  成对入 acceptance/testScope。本领域无未关闭异议、无新增 P1；未执行任何测试；不宣称
  实现或 E2E 已通过；本 APPROVE 仅表示计划层可测性门禁对本角色已闭合。
closedIssues:
  - ISSUE-QA-WSC5-R1-001
  - ISSUE-QA-WSC5-R1-002
  - ISSUE-QA-WSC5-R1-003
  - ISSUE-QA-WSC5-R1-004
  - ISSUE-QA-WSC5-R1-005
openIssues: []
```

## Round 1 ISSUE closeWhen 确认

| id | severity | closeWhen 结论 | 证据（PLAN-WSC-5.2） |
|---|---|---|---|
| ISSUE-QA-WSC5-R1-001 | P1 | **关闭** | 新增 **TASK-WSC-605**：`writeSet` 独占 `tests/e2e/specs/**`、`playwright.config.ts`、`reports/p0-wsc-v1.4/**`；601–604 `denyModify` `tests/e2e/**`。§6.1 场景 1–7 全部标 **强制**；`evidenceId: TESTRUN-WSC-E2E-V14`（非建议）；命令与报告落点钉死；604 已删除「E2E 非强制」表述。§9.8 发布门禁绑定 605。 |
| ISSUE-QA-WSC5-R1-002 | P1 | **关闭** | §0.2 声明对账表 P0 须由本任务 `testScope` 命名自动化证明。601–604 `testScope` 均含 **「预落地对账自动化」** 勾选行：须有命名测试类/spec；交付说明附证据定位（文件路径 / TESTRUN），**禁止**仅 prose「已对账」。§6.2 / §9.9 同口径。 |
| ISSUE-QA-WSC5-R1-003 | P1 | **关闭** | 602 `testScope` §4 单元格表：用户管理 API/UI（ADMIN 200/可达；PROVIDER、USER **403 + 结构不可达**）；`POST …/session/role` 三角色 **410+码**；角色区只读无切换。603 `testScope` 表：产品写（ADMIN/USER **403**；PROVIDER 本人 **200** / 他人·空归属 **403**）；导入 API（ADMIN/USER **403**；PROVIDER 规则内 200）；我的产品菜单仅 PROVIDER；公共目录增改导三角色 **结构不可达**；ADMIN 分类/维护 **200**。正例+负例齐备。 |
| ISSUE-QA-WSC5-R1-004 | P2 | **关闭** | 603 `testScope`：**导入四态回归** — 宿主迁移后执行既有 import 四态相关自动化套件且通过；增改导仅从我的产品可达。§6.1 场景 7（605）强制「导入四态/OpenAPI 编辑不回退」。满足 closeWhen（603 波次保留入口/权限+四态套件；E2E 场景 7 点名）。 |
| ISSUE-QA-WSC5-R1-005 | P2 | **关闭** | 603 `acceptance`：**空归属** — `create_by` 为空/缺失/非当前用户 → PROVIDER 编辑/更新/导入 **403**。`testScope` 表含「他人/空归属 **403**」；另条「空/异主 create_by 负例（对照本人 200 fixture）」。§8 风险缓解同口径。 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 2 本领域无新增异议；R1 五条均可关闭。 | — | — |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 601–605 均有 acceptance、testScope、riskTags；§6.2 要求独立 tester。R1 完整性缺口已闭合。 |
| 验收可观测性（P0） | SNAP 六条 P0 均有 §7 任务映射 + 605 E2E。登录/CRUD/mine/空归属/L2 总数/hover/leave/三角色负例均可按字面勾选。 |
| 三角色负例 | 602/603 单元格表已机械化；隐藏≠授权可测。 |
| 预落地对账 | §0.2 + 各任务「预落地对账自动化」证据定位齐全。 |
| E2E 分层 | 605 独占 `tests/e2e/**`；§6.1 七场景强制 + evidenceId/命令/报告/config 钉死。 |
| 导入四态 / 空归属 | 603 testScope + 605 场景 7；空归属 acceptance/testScope 成对。 |
| 契约 601 | lint / matrix 清单 / typecheck / 预落地对账自动化可接受；无新增 P1。 |
| 座序图 604 | 后端总数/空目录 + Vitest Top5/hover/leave/`showSeatMap`；E2E 场景 6 强制。 |
| 写集/E2E | 串行五波；605 独占 E2E；无并行写冲突。 |
| 环境 vs 质量 | SNAP 仍 `IN_REVIEW` 为派发门禁，**非**本评质量通过条件；本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 契约/单测 | 集成（602/603/604） | Vitest UI | E2E（605） | 缺口 |
|---|---|---|---|---|---|
| REQ-SHELL-001 | matrix / state-matrix（601） | 切角色禁用（602） | RoleSwitcher 只读；侧栏菜单 | 场景 1–2 | 无（计划层） |
| REQ-RBAC-001 | matrix.yaml（601） | ADMIN/USER 产品写·导入 403；mine（603） | useCanWrite；公共目录无写 | 场景 3–5 | 无（计划层） |
| REQ-USER-001 | `/admin/users`（601） | 登录/CRUD/软删/三角色负例（602） | UsersAdminPage | 场景 1–2 | 无（计划层） |
| REQ-CAT-010 | `mine`（601） | mine 过滤；空/异主 403（603） | 我的产品入口/宿主；四态回归 | 场景 4、7 | 无（计划层） |
| REQ-CAT-009 | l2-distribution（601） | 计数/总数（604） | Top5/hover/leave/showSeatMap | 场景 6 | 无（计划层） |
| REQ-CAT-001 | — | 浏览不削弱（603 抽样） | 公共只读 | 场景 3、7 | 无（计划层） |

## 说明

- 输入：`planning/proposals/PLAN-WSC-5.2.md`；R1 异议源：`planning/reviews/wsc-5.1/round-1/REV-PLAN-WSC5-R1-qa-strategist.md`；汇总：`planning/reviews/wsc-5.1/round-1/SUMMARY.md`。
- 需求权威只读：`product/requirements/SNAP-WSC-005.md`（`IN_REVIEW`）。
- 角色装载：`ai/agents/qa-strategist.md`；模板：`ai/schemas/templates/planning-review.md`。
- **未**修改计划、源码或 Run `state.yaml` / `events.jsonl`。
- **未**执行 Vitest / JUnit / Playwright；**未**代替 developer/tester 宣称通过。
- 决策：`APPROVE`（本角色 R1 ISSUE-QA-WSC5-R1-001..005 全部关闭且无新增 P1）；关闭结论均有计划正文与任务字段字面证据，非伪造。
- 只关闭本角色 ISSUE；不代关其他角色 ISSUE。
