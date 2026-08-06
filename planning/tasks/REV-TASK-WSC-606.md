# 代码审查

```yaml
reviewId: REV-TASK-WSC-606
taskId: TASK-WSC-606
planId: PLAN-WSC-5.2
round: 2
role: codeReviewer
actorInstance: code-reviewer-wsc-606-r2
decision: APPROVE
p0: 0
p1: 0
closedFindings:
  - FIND-WSC-606-R1-001
openFindings:
  - FIND-WSC-606-R1-002
  - FIND-WSC-606-R1-003
  - FIND-WSC-606-R1-004
contracts: wsc-contracts@2.2.0（additive `industryCategory` query；未 bump info.version）
riskTags: [ux-hotfix, release-evidence, e2e-selector-fixed]
reviewedAt: 2026-08-06T16:12:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/tasks/REV-TASK-WSC-606.md（Round 1 REQUEST_CHANGES）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-606.md（Round 2）
  - 实地：tests/e2e/specs/p0-wsc-v1.4.spec.ts 场景 2a；tests/e2e/helpers/v14-selectors.ts
  - 对照（只读）：frontend/src/features/users/UsersAdminPage.vue testid
mustDifferFrom:
  - developer-wsc-606
  - developer-wsc-606-r2
spotCheck: |
  FIND-001 closeWhen：场景 2a 先 openUserCreateDialog（user-open-create→弹框填表）再 user-create；
  改角色经 editUserRoleViaDialog（user-edit-role→弹框内 user-row-role→user-edit-role-save）；
  行内断言 user-row-role-label。testid 与 UsersAdminPage 只读对照一致。
  R2 scope：仅 e2e spec/helpers（+开发自跑报告路径）+ DEV 更新；UsersAdmin 本轮未改（OK）。
  开发自跑 15/0 记为冒烟对照，未代 tester PASS；未标 VERIFIED；未写 state/events。
  P0=0 P1=0；开放仅 P2×1 P3×2 → APPROVE。
```

## Round 2 结论

**APPROVE** — **P0=0，P1=0**。复审 **FIND-WSC-606-R1-001**：DEV Round 2 已更新 V1.4 场景 2a 与 helper，与 606 弹框化 UX / UsersAdmin testid 对齐；`closeWhen` 满足，**CLOSED**。R2 变更范围限于 e2e + DEV 说明（UsersAdmin 未改，既有 testid 足够）。开发自跑 `test:p0-v14` **15 passed / exitCode=0** 仅作冒烟记录，**正式门禁仍须独立 tester**。开放 findings 仅剩 P2/P3（FIND-002/003/004），不阻塞进入测试门禁。

## Findings（Round 2 状态）

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-606-R1-001 | P1 | **CLOSED** | R2 实地：`openUserCreateDialog` 先 `user-open-create` 再在 `user-create-dialog` 填 `user-username`/`user-password`/`user-display-name`/`user-role`；`editUserRoleViaDialog` 先 `user-edit-role` 再弹框内 `user-row-role` + `user-edit-role-save`；行内 `user-row-role-label`。与 `UsersAdminPage.vue` testid 一致。DEV 自跑 15/0 记冒烟，不代 tester | ~~允许本任务或配套 HOTFIX 更新场景 2a…~~ **已满足：本任务 R2 更新 2a 并自跑** | HOTFIX acceptance #2；§6.1 #2a；blocksRelease |
| FIND-WSC-606-R1-002 | P2 | **OPEN** | 种子/存量 `business_category` 缩写与 GB/T 全文门类不完全一致时，精确匹配可能筛不到旧数据（R1 遗留；R2 未改） | 可选：种子规范化迁移，或过滤层兼容别名；不阻塞本 hotfix | DEC-WSC-006；acceptance #4 |
| FIND-WSC-606-R1-003 | P3 | **OPEN** | openapi additive query、未 bump `info.version` 2.2.0（R1 遗留；可接受） | 若门禁日后要求契约次版本递增，再 bump；否则可保持并关闭 | contracts |
| FIND-WSC-606-R1-004 | P3 | **OPEN** | UsersAdmin Vitest 仍为源码字符串断言（R1 遗留；R2 未改页） | 可选：mount + 弹框交互 DOM 断言；不阻塞 | acceptance #2；testScope |

## 检查清单（Round 2）

| 项 | 结果 |
|---|---|
| FIND-001：先开创建弹框再填表 | **PASS** — `openUserCreateDialog` → `user-open-create` → `user-create-dialog` 内填表 → 调用方点 `user-create` |
| FIND-001：改角色经「编辑角色」按钮 | **PASS** — `editUserRoleViaDialog` → `user-edit-role` → `user-edit-role-dialog` 内 `user-row-role` → `user-edit-role-save`；行内断言 `user-row-role-label` |
| UsersAdmin testid 只读对照 | **PASS** — 上述 testid 均存在于 `UsersAdminPage.vue`；本轮未改该文件（OK） |
| R2 scope（仅 e2e + DEV） | **PASS** — `p0-wsc-v1.4.spec.ts`、`v14-selectors.ts`、DEV-TASK；未扩 frontend/backend/contracts |
| 开发自测 ≠ 独立 tester | **记录** — DEV 称 15/0 exitCode=0；本审查不代 tester PASS、不标 VERIFIED |
| `mustDifferFrom` developer-wsc-606 / developer-wsc-606-r2 | **PASS**（`code-reviewer-wsc-606-r2`） |
| P0 / P1 | **P0=0；P1=0**（FIND-001 CLOSED） |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester PASS。
- **decision: APPROVE**（P0=0、P1=0；开放 P2×1、P3×2 可延期）。

## 计数（Round 2）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | FIND-001 已关闭 |
| P2 | 1 | FIND-002 种子缩写与 GB/T 精确匹配缺口 |
| P3 | 2 | FIND-003 未 bump VERSION；FIND-004 字符串级 Vitest |

---

## Round 1 历史（保留）

```yaml
round: 1
actorInstance: code-reviewer-wsc-606-r1
decision: REQUEST_CHANGES
p0: 0
p1: 1
closedFindings: []
openFindings:
  - FIND-WSC-606-R1-001
  - FIND-WSC-606-R1-002
  - FIND-WSC-606-R1-003
  - FIND-WSC-606-R1-004
reviewedAt: 2026-08-06T15:55:00+08:00
mustDifferFrom: developer-wsc-606
```

### Round 1 结论

**REQUEST_CHANGES** — **P0=0，P1=1**。字面 scope-check 通过：交付变更落在 HOTFIX writeSet；`denyModify`（座序图/L2 实现体、auth/login、sql、e2e、state/events）未见本任务归因触碰。acceptance 四项源码可复核。**阻塞点**：V1.4 `p0-wsc-v1.4.spec.ts` 场景 2a 仍按页内表单 + 行内 `user-row-role` 编写，与 606 弹框化 DOM 断裂 → **P1 FIND-001**。

### Round 1 Findings（当时状态）

| id | severity | status | evidence / note | closeWhen |
|---|---|---|---|---|
| FIND-WSC-606-R1-001 | P1 | OPEN | 场景 2a 直接填 `user-username`、行内 `user-row-role`；与弹框化不兼容 | 更新 2a 选择器步骤并复跑；或 PO 豁免 |
| FIND-WSC-606-R1-002 | P2 | OPEN | 种子缩写 vs GB/T 精确匹配 | 可选迁移/别名 |
| FIND-WSC-606-R1-003 | P3 | OPEN | openapi 未 bump VERSION | 可保持 |
| FIND-WSC-606-R1-004 | P3 | OPEN | UsersAdmin spec 字符串断言 | 可选加强 |

### Round 1 计数

| 级别 | 开放 |
|---|---|
| P0 | 0 |
| P1 | 1（FIND-001） |
| P2 | 1 |
| P3 | 2 |
