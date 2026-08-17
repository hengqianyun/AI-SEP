# 代码审查报告 — TASK-WSC-903 Round 2

```yaml
reviewId: REV-TASK-WSC-903-R2
taskId: TASK-WSC-903
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 2
role: codeReviewer
actorInstance: code-reviewer-wsc-903-r2
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 3
reviewedAt: 2026-08-17T11:40:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-903.md（Round 1 REQUEST_CHANGES；FIND-WSC-903-R1-001 closeWhen）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-903.md（含 Round 1 修复节）
  - planning/approved/PLAN-WSC-8.3.md（§3.1 / §3.2 / §5 TASK-WSC-903）
  - product/requirements/SNAP-WSC-008.md（REQ-CAT-016）
  - 实地：contracts/openapi/openapi.yaml Product.createBy.description（L1700–1703）与 listProducts mine（L519 / L561–563）
mustDifferFrom:
  - developer-wsc-903
  - developer-wsc-903-r2
closedIssues:
  - FIND-WSC-903-R1-001
openIssues:
  - FIND-WSC-903-R1-002
  - FIND-WSC-903-R1-003
  - FIND-WSC-903-R1-004
riskTags: [contracts-bump, rbac-breaking, enterprise-scope, auth-model-change]
note: |
  本 REV 仅 codeReview 复审门禁；命中 auth-model-change，**不得**代 securityReviewer 批准安全面。
  未代 tester 宣称通过；未标 VERIFIED；未改源码 / state.yaml / events.jsonl / R1 REV。
```

## 结论

**APPROVE** — **P0=0，P1=0**。Round 1 唯一阻塞项 **FIND-WSC-903-R1-001** 已按 closeWhen 修复并关闭：`Product.createBy.description` 删除「mine=true 过滤依据 / create_by-only」暗示，改为 create_by 归属/审计说明，并指向 `listProducts` `mine` 本企业语义（对齐 L519 / REQ-CAT-016）。本轮无新 P0/P1。开放 **P2×3**（R1-002/003/004，非本轮修复范围）不阻塞进入 tester 门禁。

**未代 securityReviewer 批准**；未代 tester 宣称通过；未标 VERIFIED。

## FIND-WSC-903-R1-001 closeWhen 核验

| closeWhen 项 | 结果 | 证据 |
|---|---|---|
| 删除 mine=true 过滤依据 / create_by-only 暗示 | **PASS** | 全文件已无「mine=true 过滤依据」；`createBy.description` 明确「非本企业 scope 判定依据」「非 create_by-only」 |
| 改为 create_by 归属/审计说明 | **PASS** | L1703：「创建者 userId（审计/归属字段，非本企业 scope 判定依据）」 |
| 指向 listProducts mine 本企业语义（对齐 L519 / REQ-CAT-016） | **PASS** | L1703：「本企业产品范围见 `GET /catalog/products` 的 `mine` 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）」；L519：「mine=true 时返回**本企业**产品（create_by 关联 sys_user.enterprise_id 与会话 enterpriseId 比较；非 create_by-only）」；L563：`mine` parameter「true 时仅返回**本企业**产品（同 enterpriseId；REQ-CAT-016）」 |

**实地 excerpt**（`contracts/openapi/openapi.yaml` L1700–1703）：

```yaml
createBy:
  type: string
  nullable: true
  description: 创建者 userId（审计/归属字段，非本企业 scope 判定依据）。本企业产品范围见 `GET /catalog/products` 的 `mine` 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）。空/缺失不可被 PROVIDER 冒领
```

对照 PLAN §3.1「不得再写 create_by-only」、§3.2「同 enterpriseId 即本企业（不要求 create_by=当前用户）」、REQ-CAT-016「列表仅含本企业产品（非仅 create_by 本人）」：**一致**。

## Round 1 Findings 处置

| id | sev | R1 | R2 status | 证据 / 说明 |
|---|---|---|---|---|
| FIND-WSC-903-R1-001 | **P1** | OPEN | **CLOSED** | `Product.createBy.description` 已满足 closeWhen 三项；与 L519 / REQ-CAT-016 对齐 |
| FIND-WSC-903-R1-002 | P2 | OPEN | **OPEN** | 非本轮修复范围：`Session` 类型 enterpriseId/enterpriseName 仍 optional（注释 904 落地前） |
| FIND-WSC-903-R1-003 | P2 | OPEN | **OPEN** | 非本轮修复范围：`ListMaintenanceEntriesQuery.scope` optional vs OpenAPI required |
| FIND-WSC-903-R1-004 | P2 | OPEN | **OPEN** | 非本轮修复范围：全仓 typecheck EXIT 2 归 906 `WorkbenchLayout.vue` |

## Round 2 新 Findings

无。本轮未引入新 P0/P1/P2/P3。

## 范围与门禁备注

- 修复范围：仅 `contracts/openapi/openapi.yaml` `Product.createBy` 单字段 description；符合 TASK-WSC-903 writeSet。
- 未 bump 版本（2.3.3 保持）符合 §3.1 WORKTREE 对账例外。
- `auth-model-change` 仍命中：enterprise scope / ADMIN 产品写 / maintenance scope 等安全验收须独立 **securityReviewer**；本 REV **不代批**。
- 进入 tester 后仍须执行 TASK-WSC-903 `testScope`（契约 lint / 矩阵交叉 / client typecheck / 预落地对账）；本 REV **不**宣称测试通过。

## 决策依据

- FIND-WSC-903-R1-001 closeWhen **全部满足** → 计入 `closedIssues`。
- 本轮 **P0=0，P1=0** → **APPROVE**（P2 不阻塞）。
- 后续仍须：独立 **securityReviewer** + 独立 **tester**。
