# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC9-R1-qa-strategist
planId: PLAN-WSC-9.1
round: 1
role: qaStrategist
actorInstance: qa-strategist-wsc-012-r1
snapshotIdAtReview: SNAP-WSC-009
decision: REQUEST_CHANGES
summary: |
  九任务（911–919）均有 acceptance + testScope + riskTags，结构完整度优于 PLAN-WSC-8.1。
  §6.1 九场景强制覆盖主流程、取消、越权、回归；evidenceId/命令/报告落点已钉死。
  21 条 REQ 映射齐全，P0 REQ 均有至少一个任务含可观察验收与 testScope。
  RBAC 矩阵八单元格 × 三角色在 §4、acceptance、E2E 层方向正确。
  但：(1) REQ-WSC-ORDER-017 数字合约版本历史 UI 可见性仅靠集成 testScope「条数随变更增加」
  覆盖，§6.1 E2E 无对应场景，TASK-WSC-919 requirements 未含 017，发布门禁缺 E2E 证据；
  (2) REQ-WSC-ORDER-016 mock 上链 chainCount 一致性贯穿全状态路径，TASK-WSC-916 testScope
  未列出该可勾选项；(3) TASK-WSC-918 acceptance「外部跳转无地址提示」缺集成 testScope 条目。
  存在 P1 → REQUEST_CHANGES。未执行任何测试；不宣称实现或 E2E 已通过；不伪造 APPROVE。
```

## 评审范围与方法

| 项目 | 说明 |
|---|---|
| 输入 | `planning/proposals/PLAN-WSC-9.1.md`（Round 1 DRAFT）；`product/requirements/SNAP-WSC-009.md`（APPROVED）；`ai/rules/global/RULE-GLOBAL-FRONTEND.md`；`ai/agents/qa-strategist.md` |
| 对照先例 | `planning/reviews/wsc-8.1/round-1/REV-PLAN-WSC8-R1-qa-strategist.md`（格式与 ISSUE 命名对齐） |
| 方法 | 逐任务核对 acceptance ↔ testScope ↔ §6.1 E2E ↔ §7 REQ 映射；交叉验证 §4 RBAC 矩阵；检查 RULE-GLOBAL-FRONTEND 三条规则（toast/二次确认/分页）在 testScope 中的自动化覆盖；评估回归与边界场景完整性 |
| 角色范围 | 只读；**未**修改计划、源码或 `ai/runs/**/state.yaml` / `events.jsonl` |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-WSC9-R1-001 | **P1** | REQ-WSC-ORDER-017（P1）验收：「按下单主数据生成初版；相关变更出新版本并保留历史；详情可见当前版本」。TASK-WSC-918 acceptance「创建后详情可见版本 1；后续状态变更版本递增且历史可查」+ testScope「版本历史条数随变更增加；下单不阻断」。§6.1 E2E 九场景**无**数字合约版本验证；TASK-WSC-919 `requirements` 列 12 条 REQ，**未含** REQ-WSC-ORDER-017。结果：版本历史 UI 可见性无 E2E 证据；若集成层版本计数逻辑正确但前端渲染遗漏，§9 发布门禁无法捕获。 | 方案 A：§6.1 增场景 #10「数字合约版本」（强制或建议），含创建后版本 1 可见 + 主路径达成后版本 ≥2 且历史可查；TASK-WSC-919 `requirements` 增 REQ-WSC-ORDER-017。方案 B（若委员会判定 P1 不入 E2E 强制）：TASK-WSC-918 `testScope` 增 Vitest 明细：详情页版本区可见版本号、版本列表条数随状态变更递增、历史版本可展开查看——**必须**有命名用例而非仅后端计数。 | REQ-WSC-ORDER-017, REQ-WSC-ORDER-009 |
| ISSUE-QA-WSC9-R1-002 | P2 | REQ-WSC-ORDER-016 验收：「列表上链次数与详情记录数一致」。TASK-WSC-916 acceptance「创建+确认+提交+达成+取消路径均有时间线与 mock 记录；`chainCount` 一致」。但 TASK-WSC-916 `testScope` 列「主路径四步 + 取消三态参数化；三角色动作正/负例表；附件类型/大小/扫描失败；金额舍入两位」——**未**显式列出「每个状态变更后 `chainCount` = 详情时间线条数」可勾选项。tester 可验主路径通而不逐态核对 chainCount。 | TASK-WSC-916 `testScope` 增一行：「每步状态变更后 `GET /orders/{id}` 的 `chainCount`（或等价字段）= 时间线记录条数；列表 `chainCount` 与详情一致」；或合并入命名用例 `916-state-machine` 的断言清单。 | REQ-WSC-ORDER-016 |
| ISSUE-QA-WSC9-R1-003 | P2 | TASK-WSC-918 acceptance「外部跳转：有地址打开、无地址提示未配置、**列表不新增订单**」。`testScope` 列「Vitest：外部跳转无 POST；无 URL 提示」。但测试描述「无 URL 提示」语义含糊——应为「无外部跳转地址时，提示"未配置"或等价文案」，当前措辞易被理解为「不需要提示」。建议修正为正向断言。 | TASK-WSC-918 `testScope` 中「无 URL 提示」改为：「无地址时展示"未配置"或等价提示文案（testid 可断言）」。 | REQ-WSC-ORDER-005 |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 911–919 均有 acceptance、testScope、riskTags；§6.2 要求独立 tester + codeReviewer。完整性缺口见 ISSUE-001..003。 |
| 21 条 REQ 覆盖 | §7 映射表含全部 21 条增量 REQ + 继承声明。P0（19 条）均有 ≥1 任务 testScope 覆盖；P1（005/017）归 918。**唯一缺口**：017 E2E 层（ISSUE-001）。 |
| E2E §6.1 场景 | 九场景**强制**；919 独占 `tests/e2e/**`；`evidenceId: TESTRUN-WSC-E2E-V17`、命令 `test:p0-v17`、报告落点、`test:p0-v16`/`test:p0-v14` 共存已钉死。数字合约版本未覆盖 → ISSUE-001。 |
| RBAC 矩阵可测性 | §3.1 八能力键 × 三角色逐 cell 与 OpenAPI 须一致（911 acceptance）；§4.1 三角色正/负例表完整。913（列表+创建 scope）、916（动作权限+附件）、914（菜单可见性）在集成/Vitest 层均有命名用例。 |
| 前端 RULE-GLOBAL-FRONTEND | toast（§1）：915 testScope 含「创建成功调用 toast 一次；失败 0 次」；917 testScope 含「写成功 toast 四动作」——可自动化。二次确认（§2）：917 testScope 含「取消未确认不调用 API」+ acceptance「先弹确认」——可自动化。分页（§3）：915 testScope 含「pageSize 变更 → page=1；jumper 发对应 page」——可自动化。**三条均可 Vitest 自动化验证**。 |
| 回归 V1.6 不回退 | 914 testScope 含 `914-v16-catalog-nav-regression`（USER 不可达 `/catalog/maintenance`、`/my-catalog`、`/my-products`、`/admin/users`；PROVIDER 不可达目录维护）；§6.1 场景 9 强制 `test:p0-v16` 回归；acceptance 明确各处「不得削弱」。方向正确。 |
| mock 上链验收 | 913 acceptance「创建写时间线 + mock 上链至少 1 条；mock 失败不得阻断建单」可测试（捕获异常仍写降级记录）。916 acceptance「每次状态变更新增时间线与 mock 上链」可测试。§6.1 场景 7「详情时间线完整；列表上链次数=详情 mock 条数」强制。chainCount 逐态核对 → ISSUE-002。 |
| 附件扫描/上传策略 | OQ-V17-002 端口假设：同步调用、白名单返回通过、未调用不得入库。916 testScope 含「附件类型/大小/扫描失败」集成测试；acceptance 含「无附件/非 Word-PDF/>20MB/扫描失败 → 不能进入待确认合约」。端口可替换，不改表——测试策略清晰。 |
| 规模/压力 | 订单列表分页 pageSize 限定 10/20/50/100；接受「仅分页」无筛选维度爆炸。本期无显式压力测试需求——可接受（新模块，数据量从零开始）。 |
| 并行写集互斥 | 912∥914 允许（sql vs 壳层）；913∥916 禁止（串行订单 BE）；914∥915 禁止（routes 交接）；919 与全部 feature 禁止。DAG 无环。914 独占 useCanWrite.ts。互斥声明完整。 |
| 环境 vs 质量 | SNAP-WSC-009 已 APPROVED；本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 优先级 | 契约/单测 | 集成 | Vitest UI | E2E（§6.1） | 缺口 |
|---|---|---|---|---|---|---|
| ORDER-001 | P0 | 911 matrix | 913 三角色列表 | 914 菜单/useCanWrite | 场景 1, 3 | — |
| ORDER-002 | P0 | — | — | 915 订购入口字段 | 场景 2（间接） | — |
| ORDER-003 | P0 | 911 OpenAPI | 913 简易流程边界 | 915 按钮禁用 | 场景 2 | — |
| ORDER-004 | P0 | — | 913 须知校验 | 915 勾选前置 | 场景 2 | — |
| ORDER-005 | P1 | — | 918 跳转 | 918 跳转/提示 | — | testScope 措辞 ISSUE-003 |
| ORDER-006 | P0 | — | 913 创建+快照+mock | 915 toast | 场景 2, 7 | — |
| ORDER-007 | P0 | — | 913 分页+筛选 | 915 分页 jumper | 场景 4 | — |
| ORDER-008 | P0 | — | — | 915 九组字段 | 场景 3（间接） | 脱敏在 918 对齐 |
| ORDER-009 | P0 | — | — | 915 详情分区 | 场景 2（间接） | — |
| ORDER-010 | P0 | 911 OpenAPI | 916 状态流转 | 917 动作按钮 | 场景 5 | — |
| ORDER-011 | P0 | 911 matrix | 916 三角色正/负例 | 917 按钮可见性 | 场景 5, 8 | — |
| ORDER-012 | P0 | — | 916 取消三态 | 917 二次确认 | 场景 6 | — |
| ORDER-013 | P0 | — | 916 附件类型/大小/扫描 | 917 前端预校验 | 场景 5（间接） | — |
| ORDER-014 | P0 | — | 916 金额舍入/双字段 | — | — | — |
| ORDER-015 | P0 | — | 916 统一确认 | 917 统一确认页 | 场景 5 | — |
| ORDER-016 | P0 | — | 916 时间线+mock | — | 场景 7 | chainCount 逐态 ISSUE-002 |
| ORDER-017 | P1 | — | 918 版本计数 | — | — | **E2E 缺失 ISSUE-001** |
| FE-001 | P0 | — | — | 915/917 toast | 场景 2, 5, 6 | — |
| FE-002 | P0 | — | — | 917 二次确认 | 场景 6 | — |
| FE-003 | P0 | — | — | 915 jumper+pageSize | 场景 4 | — |
| SHELL-011 | P0 | 911 state-matrix | — | 914 菜单/navClosed | 场景 1, 9 | — |
| 继承 CAT/SHELL/… | — | — | — | 914 回归 | 场景 9 | — |

## 非阻塞备注

1. **ADMIN 代操作 E2E 深度**：§6.1 场景 5 提及「ADMIN 可代确认」，但未强制要求 ADMIN 代 PROVIDER 确认合约的独立 E2E 步骤。TASK-WSC-916 集成层 `916-admin-proxy-provider` 已覆盖。可接受，但建议 919 在场景 5 中至少包含 ADMIN 代确认订单的一步操作。
2. **数据量基线**：§6.1 场景 3（角色列表）需至少 2 家企业 × 多条订单 seed 才能验证 ADMIN 看全部、PROVIDER 看本企、USER 看本人的集差。919 fixtures 须设计而非空库单条——建议在 `wsc-v17-fixtures.ts` 中显式声明 seed 矩阵。
3. **TASK-WSC-918 脱敏**：acceptance「按 §2.2 假设对齐目录 SensitiveId」+ testScope「USER/PROVIDER/ADMIN 脱敏差异（可 mock）」——OQ-V17-001 仍 OPEN non-blocking，脱敏细则可被后续 PO 修订。计划层可接受，但 tester 需注意 mock 断言不等于真实脱敏引擎验证。

## 决策

**REQUEST_CHANGES**

- ISSUE-001（P1）：REQ-WSC-ORDER-017 数字合约版本历史 UI 可见性缺 E2E 证据。需委员会决定：入 §6.1 强制/建议场景 + 919 requirements 增 017，或 918 testScope 增显式 Vitest UI 断言。
- ISSUE-002/003（P2）：建议修正，不阻塞审批但提升可测性精确度。
- 未执行 Vitest / JUnit / Playwright；未代替 developer / tester 宣称通过。
- 验收标准本身可测（非 BLOCK），故非 `BLOCK`。
- 不代批；ISSUE 关闭须本角色后续轮次对照计划正文字面确认。
