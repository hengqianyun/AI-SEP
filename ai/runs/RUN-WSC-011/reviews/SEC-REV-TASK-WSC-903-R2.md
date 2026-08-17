# 安全审查报告 — TASK-WSC-903 Round 2

```yaml
reviewId: SEC-REV-TASK-WSC-903-R2
taskId: TASK-WSC-903
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 2
role: securityReviewer
actorInstance: security-reviewer-wsc-903-r2
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T12:35:00+08:00
trigger: auth-model-change
basedOn:
  - ai/agents/security-reviewer.md
  - ai/skills/security-review/SKILL.md
  - ai/rules/global/RULE-GLOBAL-SECRETS.md
  - planning/approved/PLAN-WSC-8.3.md（§3.1、§5 TASK-WSC-903、§6.2）
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-903.md（Round 1 REQUEST_CHANGES；FIND-SEC-WSC-903-001 closeWhen）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-903.md（安全审查修复节 FIND-SEC-WSC-903-001）
  - 实地：frontend/src/api/catalog.ts Product.createBy JSDoc；frontend/src/api/** 同类残留扫描
  - 对照：contracts/openapi/openapi.yaml Product.createBy.description（L1703）与 listProducts mine（L9 / L519 / L563）
mustDifferFrom:
  - developer-wsc-903-sec-r1
  - developer-wsc-903
  - code-reviewer-wsc-903
closedIssues:
  - FIND-SEC-WSC-903-001
openIssues:
  - FIND-SEC-WSC-903-002
  - FIND-SEC-WSC-903-003
relatedReqs:
  - REQ-USER-002
  - REQ-RBAC-002
  - REQ-CAT-016
  - REQ-CAT-017
  - REQ-CAT-018
  - REQ-SHELL-009
riskTags: [contracts-bump, rbac-breaking, enterprise-scope, auth-model-change]
note: |
  本轮仅复审 FIND-SEC-WSC-903-001 修复（client JSDoc 与 OpenAPI L1703 同口径）。
  本任务为契约/client 冻结，无后端授权实现。不代替 905 实现态复审，不代替 tester 宣称通过。
  未改源码 / state.yaml / events.jsonl / R1 SEC REV；未 git commit；未标 VERIFIED。
```

## 结论

**APPROVE** — **P0=0，P1=0**。Round 1 唯一阻塞项 **FIND-SEC-WSC-903-001** 已按 closeWhen 关闭：`frontend/src/api/catalog.ts` `Product.createBy` JSDoc 已删除「mine=true 过滤依据」，改为与 OpenAPI L1703 同口径（审计/归属字段，非本企业 scope 判定依据；mine 用 enterpriseId 比较，非 create_by-only）。`frontend/src/api/**` 无同类残留。本轮无新 P0/P1。开放 **P2×2**（FIND-SEC-WSC-903-002 / 003，非本轮修复范围）不阻塞本安全门禁。

**未代 tester 宣称测试通过**；未标 VERIFIED。905 实现态仍须独立 `securityReviewer`（同 `auth-model-change`）。codeReview R2 APPROVE **不**覆盖本安全面；本结论仅基于本角色实地核验。

## FIND-SEC-WSC-903-001 closeWhen 核验

| closeWhen 项 | 结果 | 证据 |
|---|---|---|
| 删除「mine=true 过滤依据」 | **PASS** | `frontend/src/api/**` 全文无「mine=true 过滤依据」；`frontend/` 全树无该短语。R1 证据点 `catalog.ts` L148 旧文案已不存在 |
| 与 OpenAPI L1703 同口径：审计/归属，非本企业 scope 依据 | **PASS** | client L148：「创建者 userId（审计/归属字段，非本企业 scope 判定依据）」；OpenAPI L1703 同句。createBy **不是** mine 过滤键 |
| mine 用 enterpriseId 比较，非 create_by-only | **PASS** | client L148：「本企业产品范围见 listProducts 的 mine 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）」；对照 OpenAPI L1703「`GET /catalog/products` 的 `mine` 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）」及 L519 / L563 / PLAN §3.1 / §3.2 |

**实地 excerpt**（`frontend/src/api/catalog.ts` L148–149）：

```ts
/** 创建者 userId（审计/归属字段，非本企业 scope 判定依据）。本企业产品范围见 listProducts 的 mine 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）。空/缺失不可被 PROVIDER 冒领 */
createBy?: string | null
```

**对照 OpenAPI**（`contracts/openapi/openapi.yaml` L1700–1703）：

```yaml
createBy:
  type: string
  nullable: true
  description: 创建者 userId（审计/归属字段，非本企业 scope 判定依据）。本企业产品范围见 `GET /catalog/products` 的 `mine` 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）。空/缺失不可被 PROVIDER 冒领
```

client 以 `listProducts` 指代 `GET /catalog/products`，语义等价，符合生成 client 口径。空/缺失不可被 PROVIDER 冒领与 L1703 / PLAN §3.2 一致。

对照 PLAN §3.1「`mine=true` → 本企业；不得再写 create_by-only」、§3.2「同 enterpriseId 即本企业（不要求 create_by=当前用户）」、REQ-CAT-016：**一致**。905/906 若只读 `frontend/src/api/**`，不再被引导实现 create_by-only。

## 同类残留扫描（`frontend/src/api/**`）

扫描范围：`auth.ts`、`admin.ts`、`catalog.ts`、`client.ts`、`index.ts`、`overview.ts`、`chain.ts`。检索：`mine=true 过滤依据`、`过滤依据`、`createBy` 作 mine 键、肯定式 `create_by-only`。

| 位置 | 口径 | 结果 |
|---|---|---|
| `catalog.ts` L4 文件头 `mine=本企业` | 与 §3.1 一致 | **PASS** |
| `catalog.ts` L148 `Product.createBy` | 见上；唯一曾失败点，已修 | **PASS** |
| `catalog.ts` L192–193 `ListProductsQuery.mine`：「true → 本企业产品（REQ-CAT-016）」 | 正确；未写成 create_by-only。较短于 OpenAPI L563，不构成绕过叙述 | **PASS** |
| `auth.ts` L13 `Session.enterpriseId`：「mine/myCatalog/写 scope 单一真源」 | 与 ISSUE-SEC-WSC8-R1-003 / OpenAPI L14 一致 | **PASS** |
| `ListProductsQuery` / `ListMaintenanceEntriesQuery` / `L2DistributionQuery` | **无** enterprise 授权参数 | **PASS**（无 IDOR 暗示） |
| 其余 api 文件 | 无 createBy / mine 过滤依据残留 | **PASS** |

「非 create_by-only」仅作为否定句出现（client L148 与 OpenAPI L9/L519/L1703），**不是**残留错误模型。

## Round 1 Findings 处置

| id | sev | R1 | R2 status | 证据 / 说明 |
|---|---|---|---|---|
| FIND-SEC-WSC-903-001 | **P1** | OPEN | **CLOSED** | closeWhen 三项均 PASS；client 与 OpenAPI L1703 同口径；`frontend/src/api/**` 无同类残留 |
| FIND-SEC-WSC-903-002 | P2 | OPEN | **OPEN** | 非本轮修复范围。OpenAPI `updateMaintenanceEntry` / `batchUpdateMaintenanceEntries` 仍无 `scope` 参数。905 **禁止**用客户端 enterprise 补 scope；无参 PUT/batch 须按角色 fail-closed |
| FIND-SEC-WSC-903-003 | P2 | OPEN | **OPEN** | 非本轮修复范围。`matrix.yaml` PROVIDER `productImportApi.on: valid`（约 L41）仍与 §3.1 字面 `ownCreateBy` 不一致。905 测试仍须 ownCreateBy 负例 |

## Round 2 新 Findings

无。本轮未引入新 P0/P1/P2。修复仅改 `Product.createBy` JSDoc，未扩大写集、未新增端点、未改授权参数形状。

## 规划期 SEC 约束（复审增量）

R1 已对 matrix / OpenAPI / state-matrix 权威面给出 PASS。本轮仅复核 R1 失败的 client 叙述及是否因修复引入回归。

| 约束 | R1 | R2 | 证据 |
|---|---|---|---|
| mine=true 本企业 — OpenAPI / matrix | PASS | **PASS**（未回退） | L9 / L519 / L563 / L1703 |
| mine=true 本企业 — **client** | **FAIL** | **PASS** | FIND-SEC-WSC-903-001 CLOSED |
| 无绕过叙述（client） | **FAIL** | **PASS** | createBy 不再被标成 mine 过滤依据 |
| 无 IDOR 暗示（client query） | PASS | **PASS** | 列表/维护/L2 query 仍无 enterprise 授权参数 |
| 无密钥入库 | PASS | **PASS** | 本轮仅 JSDoc；RULE-GLOBAL-SECRETS 无命中 |
| PROVIDER maintenance 403 / ADMIN 写 ownEnterprise | PASS | **PASS**（未改契约） | 非本轮 diff |

## 残留风险（不构成 903 P0/P1；提交人类 Owner 前须 905 复审）

| 项 | 说明 | 接受身份 |
|---|---|---|
| 运行时隔离未实现 | mine 本企业、scope-aware interceptor、篡改 enterprise 负例、空 create_by 403 均归 **905**；本报告不批准运行时安全 | 905 独立 securityReviewer |
| Session client optional | `auth.ts` `enterpriseId?`：904 落地前缺字段时 905 **fail-closed**，禁止当「无企业过滤」 | 904/905 |
| FIND-SEC-WSC-903-002 / 003 | P2 仍开放；905 须按 R1 closeWhen 实现/测试，不得用客户端 enterprise 补洞 | 905 / 后续契约 |
| 剩余风险正式接受 | Agent **不得**关闭未修 P0；本轮无 P0 | securityOperationsOwner |

## 未做事项

- 未跑 tester `testScope`（契约 lint / 矩阵交叉自动化 / typecheck）；**不**宣称测试通过。
- 未审 905 Java interceptor / 906 `useCanWrite`（denyModify / 非本任务）。
- 未改 `state.yaml` / `events.jsonl` / 源码 / `SEC-REV-TASK-WSC-903.md`。
- 未 git commit。

## 决策依据

- P0=0。
- P1=0（FIND-SEC-WSC-903-001 已关闭；无新 P1）。
- P2×2 仍开放，不阻塞本轮 APPROVE（与 R1 声明「本任务可不补」一致）。
- 契约可测面与 client 叙述已无 create_by-only 过滤暗示；**不**代替 tester，**不**代替 905 实现态安全审查。
