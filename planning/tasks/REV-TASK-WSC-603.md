# 代码审查

```yaml
reviewId: REV-TASK-WSC-603
taskId: TASK-WSC-603
planId: PLAN-WSC-5.2
round: 3
role: codeReviewer
actorInstance: code-reviewer-wsc-603-r3
decision: APPROVE
p0: 0
p1: 0
closedFindings:
  - FIND-WSC-603-R1-001
openFindings:
  - FIND-WSC-603-R1-002
  - FIND-WSC-603-R1-003
  - FIND-WSC-603-R1-004
contracts: wsc-contracts@2.2.0（只读对照；非本任务 writeSet）
reviewedAt: 2026-08-06T10:38:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-5.2.md（TASK-WSC-603 writeSet/denyModify）
  - planning/tasks/REV-TASK-WSC-603.md（Round 2 APPROVE）
  - planning/tasks/TESTRUN-TASK-WSC-603.md（BUG-WSC-603-TESTRUN-001）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-603.md（Round 3 BUG 修复）
  - 实地：CatalogBrowseSeedStore ApplicationRunner L3 种子、ProductEditorIntegrationTest 夹具、V5 分类树对照
mustDifferFrom: developer-wsc-603-r3
bugFixAssessment: |
  BUG-WSC-603-TESTRUN-001 根因与修复匹配：H2 flyway.enabled=false 时空分类表导致 findCategory(cat-l3-*) 失败 → 400 ERR_CATEGORY_LEAF_REQUIRED。
  CatalogBrowseSeedStore 以 ApplicationRunner 在空库补齐与 V5 一致的 11 条 L1/L2/L3（含 cat-l3-emr-desense/struct 等）；Flyway 已写入时 no-op。
  夹具仍保留合法 L3 挂载与非法 L2 负例，未以降断言规避。
  附带 restoreOtherTypeSpecific 读路径归一属 writeSet 内可接受缓解（mapper 不在 603 writeSet）；不升格 P0/P1。
  审查者实地复跑 ProductEditorIntegrationTest：Tests run 7, Failures 0。
  BUG 正式关闭 / TESTRUN PASS 权在独立 tester 重测；本轮不代标 VERIFIED、不代 tester PASS。
```

## Round 3 结论

**APPROVE** — **P0=0，P1=0**。针对 tester 交回的 **BUG-WSC-603-TESTRUN-001**（P1）复审通过：修复对准测境空分类树根因，范围落在 `CatalogBrowseSeedStore.java` + `**/catalog/**` 测试（均在 TASK-WSC-603 `writeSet`），未触碰 `denyModify`。未发现新 P0/P1。FIND-002..004（P2/P3）保持开放、不阻塞批准。

**BUG 关闭状态**：developer 侧修复合理；**正式关闭待独立 `tester` 重测确认**。本报告不代替 TESTRUN、不标 VERIFIED。

## BUG 修复结论（BUG-WSC-603-TESTRUN-001）

| 项 | 结论 |
|---|---|
| 根因理解 | **正确**：`flyway.enabled=false` + 空分类表 → `ProductEditorService` `findCategory` 失败 → `ERR_CATEGORY_LEAF_REQUIRED`，掩盖 OpenAPI/类型创建/上链失败负例 |
| 修复策略 | **合理**：空库种子对齐 V5（11 条：2 L1 + 4 L2 + 5 L3），正式 Flyway 路径 no-op；非削弱用例断言 |
| 夹具变更 | **可接受**：PROVIDER 登录、链查询按 `productCode` 与 603 RBAC/create_by 一致；合法 L3 仍在请求中；`create_requiresIndustryCategory_andRejectsInvalidL3` 仍用 `cat-l2-emr` 期望 400 |
| `restoreOtherTypeSpecific` | **可接受（非阻塞）**：OTHER 写入 `type_api_json` 回读键名漂移的读路径纠正；利于 acceptance「OpenAPI 编辑不回退」；mapper 真修不在本任务 writeSet |
| writeSet / denyModify | **合规**：仅 SeedStore + catalog 测试；未见 contracts / api / CirculationSeatMap / L2 实现体 / security controller / state·events |
| 新 P0 / P1 | **无** |
| 审查者抽测 | `ProductEditorIntegrationTest` → **7 / 0**（exit 0） |
| BUG 关闭权 | **保留给 tester 重测**；本轮 **不**代标关闭/PASS |

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-603-R1-001 | P0 | **CLOSED**（R2） | mine 响应式 + 自动化；本轮未复开 | ~~已满足~~ | REQ-CAT-010；REQ-CAT-001 |
| FIND-WSC-603-R1-002 | P2 | **OPEN** | L2 空壳交 604；未升格 | 604 接手实现体；或编排确认空壳手递合法后关闭 | §3.2；ISSUE-SA-R1-001 |
| FIND-WSC-603-R1-003 | P2 | **OPEN** | UsersAdminPage.spec 壳层断言过时；未升格 | tester/后续波次修订白名单 | §3.2 SA-004 |
| FIND-WSC-603-R1-004 | P3 | **OPEN** | page spec 字符串级；可选增强 | 可选：mount + router 导航断言 | testScope |

## 检查清单（Round 3 — BUG 复审）

| 项 | 结果 |
|---|---|
| BUG 根因 ↔ 修复匹配 | **PASS**（空库 L3 种子 ↔ `ERR_CATEGORY_LEAF_REQUIRED`） |
| 种子与 V5 对齐（含 `cat-l3-emr-desense`） | **PASS**（11 条 code/name/level/parent 一致） |
| 非法 L3/L2 负例未削弱 | **PASS**（`cat-l2-emr` → 400） |
| R3 改动 ∈ writeSet | **PASS**（SeedStore + ProductEditorIntegrationTest） |
| denyModify 未触碰 | **PASS** |
| 新 P0 / P1 | **无** |
| P2/P3 是否升格 | **否** |
| `mustDifferFrom` developer-wsc-603-r3 | **PASS**（`code-reviewer-wsc-603-r3`） |
| 未代 tester PASS / 未标 VERIFIED | **PASS** |

## Round 2 / Round 1 摘要（历史）

- **R2 APPROVE**：FIND-001 mine 响应式关闭；P0=0 P1=0。
- **R1 REQUEST_CHANGES**：FIND-001 P0（mine 快照）。其余 R1 PASS 项本轮未复开。
- **TESTRUN FAIL**：ProductEditor 4 失败 → BUG-WSC-603-TESTRUN-001 → R3 修复。

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester / securityReviewer / migrationReviewer。
- **decision: APPROVE**（P0=0、P1=0；BUG 修复合理；开放 finding 均为 P2/P3；BUG 正式关闭待 tester 重测）。

## 计数（Round 3）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | FIND-001 已关闭；无新 P0 |
| P1 | 0 | BUG-WSC-603-TESTRUN-001 审查侧认为已修复；关闭待 tester |
| P2 | 2 | FIND-002 L2 空壳；FIND-003 602 spec 过时 |
| P3 | 1 | FIND-004 字符串级 page spec |
