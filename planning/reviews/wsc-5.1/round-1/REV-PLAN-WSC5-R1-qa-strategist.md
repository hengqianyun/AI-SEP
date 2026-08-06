# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC5-R1-qaStrategist
planId: PLAN-WSC-5.1
round: 1
role: qaStrategist
actorInstance: qa-strategist-wsc-008-r1
snapshotIdAtReview: SNAP-WSC-005
decision: REQUEST_CHANGES
summary: |
  四任务均有 acceptance + testScope + riskTags；§4 RBAC 矩阵与 §0.2 预落地对账方向正确；
  §9 发布门禁点名真登录/三角色/座序图/E2E。但：(1) §6.1 E2E 为「建议」且无任务拥有
  tests/e2e 写集，与 §9 强制证据冲突；(2) 预落地对账仅要求说明，未把 P0 负例钉入
  各任务可自动化 testScope；(3) §4 三角色负例未机械落入 602/603 testScope 勾选表。
  存在未关闭 P1 → REQUEST_CHANGES。未执行任何测试；不宣称实现或 E2E 已通过；不伪造 APPROVE。
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-WSC5-R1-001 | P1 | §6.1 七场景与 `evidenceId: TESTRUN-WSC-E2E-V14` 均为「建议」；604 `testScope` 写「手工或 Playwright 抽样…**非强制阻塞**若 601..603 单测已覆盖 P0」。同时 601–603 `denyModify` `tests/e2e/**`，604 `writeSet` **未**含 `tests/e2e/**`。发布门禁 §9.4–§9.8 却要求真登录/RBAC/座序图自动化证据及「§6.1 E2E（或等价证据包）通过」。结果：任务可在无 E2E 写集/无钉死证据下落 VERIFIED，发布门禁又依赖无主场景包——相对 PLAN-WSC-4.1（304 独占 E2E + 强制 evidenceId/命令/报告落点）回退。 | 指定唯一任务（建议 **604** 或独立 E2E 任务）`writeSet` 含 `tests/e2e/**`（及必要 `playwright.config.ts`）；将 §6.1 场景 1–7 标为**发布门禁强制**（或明文「等价证据包」最低清单：覆盖真登录三角色、用户管理负例、公共目录无写、我的产品、ADMIN 产品写 403、座序图公共/mine、V1.3 导入抽样）；钉死非建议的 `evidenceId`、规格文件名、报告目录、可执行命令。604 删除「E2E 非强制阻塞」与 §9 冲突的表述。 | REQ-SHELL-001, REQ-RBAC-001, REQ-USER-001, REQ-CAT-010, REQ-CAT-009, REQ-CAT-001 |
| ISSUE-QA-WSC5-R1-002 | P1 | 本计划核心是协议修复+预落地对账。§0.2 / §1.4 / §6.2 / §9.9 正确强调「不以代码已在代替测试证据」「预落地 diff 进 review 证据」「对账说明齐全」。但 601–604 `acceptance` 仅「预落地…已对账」；各任务 `testScope` **无**条款要求：对账表声称保留的 P0 行为必须由**本任务自动化用例**证明仍成立。tester 按字面可只跑现有模糊条目 + 接受 prose 对账说明即 VERIFIED，与 §0.3「不得以手工看起来行代替 P0 自动化」及文首对账强制声明冲突。 | 各任务 `testScope` 增补可勾选行：(1) 对本任务 §0.2 对账表中的 P0 行为，列出命名自动化用例（或显式引用 §4 矩阵单元格 / 本任务已有负例条）；(2) 交付/对账说明须附可定位证据（测试类/spec 名或 TESTRUN id），**禁止**仅 prose「已对账」。可选：要求独立 tester 报告含「预落地保留 / 缺口已补 / 偏离已修」三态勾选。 | REQ-SHELL-001, REQ-RBAC-001, REQ-USER-001, REQ-CAT-009, REQ-CAT-010, REQ-CAT-001 |
| ISSUE-QA-WSC5-R1-003 | P1 | SNAP / §4 要求三角色矩阵可测且「隐藏≠授权」。602 `testScope` 写「非 ADMIN 403」未分 **PROVIDER 与 USER**；切角色禁用未显式写「三角色会话均禁用」。603 `testScope` 列「ADMIN 产品写 403」「导入权限矩阵」，未分项钉死 **USER** 产品写/导入 403、**ADMIN** 导入 403、我的产品菜单对 ADMIN/USER **结构不可达**。SHELL 侧栏三态（用户管理 / 我的产品 / 目录维护）无跨任务勾选表，易漏负例却仍标 VERIFIED。 | 在 602 / 603 `testScope` 用表（或等价清单）列出本任务负责的 §4 单元格，每格含**正例+负例**期望。最低必含：602 — 用户管理 API/UI：ADMIN 200/可达；PROVIDER、USER **均** 403 + UI 不可达；`POST …/session/role`（或等价）三角色均禁用 + 前端结构不可达。603 — 产品写/导入：PROVIDER 本人 200；ADMIN、USER **均** 403；跨用户 403；我的产品菜单仅 PROVIDER；公共目录三角色均无增改导入口（结构不可达）；ADMIN 分类/维护仍 200。 | REQ-SHELL-001, REQ-RBAC-001, REQ-USER-001, REQ-CAT-010 |
| ISSUE-QA-WSC5-R1-004 | P2 | 603 `acceptance` 要求导入四态 / OpenAPI 编辑相对 PLAN-WSC-4.1 **不回退**；`testScope` 仅「导入宿主」「导入权限矩阵」，**未**要求既有 import 四态 Vitest / OpenAPI 编辑路径在宿主迁至我的产品后仍绿。§6.1 场景 7 依赖 ISSUE-001 的 E2E 所有权。宿主迁移是本版高回归风险点，计划层证据落点偏弱。 | 603 `testScope` 增补：宿主迁移后须执行（或显式声明依赖）既有 import 四态相关自动化套件且通过；并含「增改导入口仅从我的产品可达 / 公共目录不可达」的 Vitest 或集成断言。若完整四态留 E2E，须在 ISSUE-001 关闭后的强制场景 7 中点名，且 603 波次内至少保留入口/权限级自动化。 | REQ-CAT-010, REQ-CAT-001, REQ-RBAC-001 |
| ISSUE-QA-WSC5-R1-005 | P2 | §8 风险「create_by 历史数据为空 → 不可被 PROVIDER 冒领编辑」有缓解口径，但 603 `acceptance` / `testScope` **未**要求空归属/异主产品的负例自动化。数据隔离 P0（REQ-CAT-010）在脏数据场景下可漏测。 | 603 `testScope`（或 acceptance+testScope 成对）增加：`create_by` 为空或非当前用户时，PROVIDER 编辑/导入该产品 → **403**（或计划钉死的稳定拒绝码）；并注明与「本人 200」对照的种子/fixture。 | REQ-CAT-010, REQ-RBAC-001 |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 601–604 均有 acceptance、testScope、riskTags；§6.2 要求独立 tester。完整性/机械度缺口见 ISSUE-001..003。 |
| 验收可观测性（P0） | SNAP 六条 P0 均有 §7 任务映射。登录/CRUD/软删、mine 过滤、L2 totalProducts≠冒充 Top5 之和、hover 态 class 等在 acceptance 层可观测；证据落点受 ISSUE 阻塞批准。 |
| 三角色负例 | §4 矩阵完整且正确；任务 testScope 未逐格机械化 → ISSUE-003。 |
| 预落地对账 | §0.2 表与「不以代码代替测试」声明正确；缺自动化证据绑定 → ISSUE-002。 |
| E2E 分层 | §6.1 场景覆盖面正确（登录/用户/去写/我的产品/ADMIN 403/座序图/V1.3）；所有权与强制力不足 → ISSUE-001。 |
| 契约 601 | lint / matrix 清单 / typecheck / 2.1.0 回归方向可接受；错误码硬同步在 acceptance，清单交叉检查可为人审+后继实现测。不单列 P1。 |
| 座序图 604 | 后端总数/空目录 + Vitest Top5/hover/`showSeatMap` 可支撑大部分 CAT-009；公共 vs mine 挂载可测。发布级端到端仍依赖 ISSUE-001。 |
| 写集/E2E | 串行 DAG 无并行写冲突；E2E 路径无主 → ISSUE-001。 |
| 环境 vs 质量 | SNAP 仍 `IN_REVIEW` 为派发门禁，**非**本评质量通过条件；本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 契约/单测 | 集成（602/603/604） | Vitest UI | E2E（§6.1） | 缺口 |
|---|---|---|---|---|---|
| REQ-SHELL-001 | matrix / state-matrix（601） | 切角色禁用（602） | RoleSwitcher 只读；侧栏菜单 | 场景 1 | 菜单三态负例未入 testScope 表；E2E 无主 |
| REQ-RBAC-001 | matrix.yaml（601） | ADMIN/跨用户 403；mine（603） | useCanWrite；公共目录无写 | 场景 3–5 | USER/ADMIN 导入分项；E2E 无主 |
| REQ-USER-001 | `/admin/users`（601） | 登录/CRUD/软删/非 ADMIN 403（602） | UsersAdminPage | 场景 1–2 | PROVIDER vs USER 分列；预落地证据 |
| REQ-CAT-010 | `mine`（601） | mine 过滤；本人写（603） | 我的产品入口/宿主 | 场景 4 | 空 create_by；四态回归落点 |
| REQ-CAT-009 | l2-distribution（601） | 计数/总数（604） | Top5/hover/showSeatMap | 场景 6 | E2E 非强制 |
| REQ-CAT-001 | — | 浏览不削弱（603 抽样） | 公共只读 | 场景 3、7 | 依赖去写负例 + E2E |

## 说明

- 输入：`planning/proposals/PLAN-WSC-5.1.md`；需求权威只读：`product/requirements/SNAP-WSC-005.md`（`IN_REVIEW`）。
- 角色装载：`ai/agents/qa-strategist.md`；模板：`ai/schemas/templates/planning-review.md`。
- 对照先例：`planning/reviews/wsc-4.0/round-1/REV-PLAN-WSC4-R1-qa-strategist.md`（E2E/fixture 机械化标准）。
- **未**修改计划、源码或 Run `state.yaml` / `events.jsonl`。
- **未**执行 Vitest / JUnit / Playwright；**未**代替 developer/tester 宣称通过。
- 决策：`REQUEST_CHANGES`（P1 未关闭：ISSUE-QA-WSC5-R1-001..003）；验收标准本身可测，故非 `BLOCK`。
- 不代批；ISSUE 关闭须本角色后续轮次对照计划正文字面确认。
