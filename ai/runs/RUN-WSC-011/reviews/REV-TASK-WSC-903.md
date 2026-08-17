# 代码审查报告 — TASK-WSC-903

```yaml
reviewId: REV-TASK-WSC-903
taskId: TASK-WSC-903
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-903
decision: REQUEST_CHANGES
p0Count: 0
p1Count: 1
p2Count: 3
reviewedAt: 2026-08-17T11:20:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-8.3.md（§3.1、§5 TASK-WSC-903 writeSet/denyModify/acceptance/testScope）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-903.md
  - product/requirements/SNAP-WSC-008.md（V1.6 十条 REQ）
  - 实地 diff：`contracts/**`、`frontend/src/api/**`
mustDifferFrom: developer-wsc-903
riskTags: [contracts-bump, rbac-breaking, enterprise-scope, auth-model-change]
note: |
  命中 auth-model-change：本 REV 仅 codeReview 门禁；**不得**代 securityReviewer 批准安全面。
  计划 header 写 wsc-contracts@2.3.2；§3.1 允许 WORKTREE 已为 2.3.3 时合并 V1.6 增量且不再 bump——交付 2.3.3 四头同步合规。
```

## 结论

**REQUEST_CHANGES** — **P0=0，P1=1**。契约四头同步、§3.1 矩阵逐 cell、maintenance scope 硬冻结、mine 本企业叙述（主路径）、l1CategoryId、state-matrix V1.6 侧栏、req-coverage 十条 REQ、denyModify 边界均满足；`frontend/src/api/**` 对齐 2.3.3 且未引入新 TS 错误。发现 OpenAPI `Product.createBy` 仍残留 create_by-only 叙述（与 REQ-CAT-016 / ISSUE-API-WSC8-R1-003 冲突），须修复后复审。**未代 tester 宣称通过**；未标 VERIFIED；未改 `state.yaml` / `events.jsonl`。

## 版本四头对账（2.3.2 → 2.3.3 合并说明）

| 头 | 计划口径 | 交付 | 结果 |
|---|---|---|---|
| `contracts/VERSION` | 2.3.2 或 WORKTREE 对账 2.3.3 | **2.3.3** | **PASS** |
| `rbac/matrix.yaml` version | 同上 | **2.3.3** | **PASS** |
| OpenAPI `info.version` | 预落地 2.2.0 → 闭合 | **2.3.3** | **PASS** |
| `ui/state-matrix.md` 版本头 | 预落地 2.2.0 → 闭合 | **wsc-contracts@2.3.3** | **PASS** |
| `frontend/src/api/client.ts` `CONTRACT_VERSION` | 对齐四头 | **2.3.3** | **PASS** |
| `errors/codes.yaml` / `sensitive-fields.md` | 随 bump 同步 | **2.3.3** | **PASS** |

说明：PLAN §3.1 字面「保持 2.3.3 且不再 bump」与 DEV §0.2 预落地对账一致；计划 front-matter `contractsTarget: 2.3.2` 为批准时目标 semver，§3.1 例外条款覆盖 WORKTREE 2.3.3 场景。

## Scope 检查（denyModify / writeSet）

| 项 | 结果 | 证据 |
|---|---|---|
| writeSet 仅 `contracts/**` + `frontend/src/api/**` | **PASS** | git diff 903 交付路径限定上述目录 |
| 未改 `frontend/src/features/**` | **PASS** | 903 diff 无 features 路径 |
| 未改 `frontend/src/layouts/**` / `router/**` | **PASS** | 903 diff 无 layout/router |
| 未改 `backend/**` / `**/sql/**` | **PASS** | 903 diff 无 backend |
| 未改 `tests/e2e/**` / `planning/**` / `product/**` | **PASS** | 903 diff 无上述路径 |
| 未改 `state.yaml` / `events.jsonl` | **PASS** | 审查实例仅写本 REV |

## §3.1 矩阵逐 cell 交叉检查

| 能力键 | ADMIN | PROVIDER | USER | matrix.yaml | 结果 |
|---|---|---|---|---|---|
| `catalogMaintenanceUI` | visible | hidden | hidden | 一致 | **PASS** |
| `catalogMaintenanceApi`（scope=full） | 200 | 403 | 403 | 一致（PROVIDER `ERR_MAINTENANCE_FORBIDDEN`） | **PASS** |
| `myCatalogUI` | visible | visible | hidden | 一致 | **PASS** |
| `myCatalogApi`（scope=myCatalog） | 200 ownEnterprise | 200 ownCreateBy | 403 | 一致 | **PASS** |
| `myProductsUI` | visible | visible | hidden | 一致 | **PASS** |
| `productWriteUI` / `ImportUI` | visible | visible | hidden | 一致 | **PASS** |
| `productWriteApi` / `ImportApi` | 200 ownEnterprise | 200 ownCreateBy | 403 | 一致 | **PASS** |

## Acceptance 对照

| acceptance 项 | 结果 | 证据 |
|---|---|---|
| 四头同步 2.3.3 | **PASS** | 见上表 |
| OpenAPI enterpriseId+enterpriseName | **PASS** | `Session`/`AdminUser` schema required；示例含 `ent-demo-1` |
| mine = 本企业（主叙述） | **PARTIAL** | `listProducts` description/parameter 已改；`Product.createBy` 仍写 create_by-only（→ P1） |
| maintenance `scope=full\|myCatalog` required enum | **PASS** | `listMaintenanceEntries` parameters + descriptions |
| 全链无企业 filter + 安全说明 | **PASS** | info description + listProducts/l2-distribution 叙述 |
| `l2-distribution?l1CategoryId=` | **PASS** | OpenAPI parameter + `getL2Distribution({ l1CategoryId })` |
| matrix 与 §3.1 逐 cell | **PASS** | 见上表 |
| state-matrix 我的目录 + ADMIN 双入口 + PROVIDER 无维护 | **PASS** | 侧栏表 + ADMIN 双入口注释 |
| V1.5 supplierName/未分类/mine/L2 不回退 | **PASS** | supplierName query 保留；l1CategoryId 增而不删 |
| req-coverage 十条 V1.6 REQ | **PASS** | §V1.6 表 10 行 |
| api client 对齐 2.3.3 | **PASS** | 全模块版本头 + 类型/export 增量 |
| 预落地漂移闭合（2.3.3/2.3.2/2.2.0） | **PASS** | matrix 2.3.2→2.3.3；OpenAPI/state-matrix 2.2.0→2.3.3 |

## 独立复验

```bash
# 四头版本（PowerShell 抽样）
contracts/VERSION → 2.3.3
contracts/rbac/matrix.yaml version → "2.3.3"
contracts/openapi/openapi.yaml info.version → 2.3.3
contracts/ui/state-matrix.md 首行 → wsc-contracts@2.3.3

# 前端 typecheck
cd frontend; pnpm run typecheck
→ EXIT 2 — 预存 WorkbenchLayout.vue 重复标识符（906 writeSet；与 901/902 REV 同口径）
→ 错误路径均非 frontend/src/api/**；903 api 变更未引入新 TS 错误

# 契约 lint（写集外）
node tests/contracts/check-contracts.mjs
→ FAIL：脚本硬编码 VERSION expected 1.1.0（滞后；非 903 writeSet）
```

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-903-R1-001 | **P1** | OPEN | `contracts/openapi/openapi.yaml` L1703 `Product.createBy.description` 仍为「mine=true **过滤依据**」，暗示 create_by-only，与 §3.1 / ISSUE-API-WSC8-R1-003「禁止 create_by-only 叙述」及 REQ-CAT-016 本企业语义冲突；同文件 L519 已正确写本企业 | 将该 description 改为 create_by 归属/审计字段说明，删除 mine 过滤依据表述；或明确「本企业判定见 listProducts mine 参数（enterpriseId 比较）」 | REQ-CAT-016 |
| FIND-WSC-903-R1-002 | P2 | OPEN | `frontend/src/api/auth.ts` 文件头写 Session「必填 enterpriseId+enterpriseName」，但 `Session` 类型两者均为 optional（注释 904 落地前）| OpenAPI 已 required；904 落地后 client 类型改 required，或 JSDoc 与类型一致 | REQ-USER-002 |
| FIND-WSC-903-R1-003 | P2 | OPEN | `ListMaintenanceEntriesQuery.scope` 为 optional，OpenAPI `listMaintenanceEntries` 的 `scope` 为 **required: true** | 907 接入前可保留过渡注释；建议 type 层标注 `@deprecated omit until 907` 或拆分为显式 overload | REQ-CAT-017 |
| FIND-WSC-903-R1-004 | P2 | OPEN | 全仓 `pnpm run typecheck` EXIT 2（WorkbenchLayout.vue 重复标识符，906 范围）| 906 修复后全仓 typecheck 可绿；不阻塞 903 契约/client 交付 | — |

## 架构与可维护性备注（非阻塞）

- `MaintenanceScope`、`ListProductsQuery`、`L2DistributionQuery` 抽取清晰，与 OpenAPI 参数对齐。
- `getL2Distribution` query 构建与 902 BE 契约一致；902 后续可改用带参调用。
- matrix notes 与 OpenAPI info description 安全说明一致（SessionPrincipal 单一真源、拒绝客户端 enterpriseId 授权）。
- `auth-model-change` 命中：enterprise scope / ADMIN 产品写 / maintenance scope 等安全验收须独立 **securityReviewer**，本 REV 不代批。

## 决策依据

- P1=1（create_by-only 残留叙述）→ **REQUEST_CHANGES**；修复 FIND-WSC-903-R1-001 后发起 round 2 复审。
- P2 项不阻塞契约冻结主体；typecheck 预存错误归 906。
- 修复后仍须：**securityReviewer**（auth-model-change）+ **tester**（testScope 契约对账/typecheck）。
