# 代码审查

```yaml
reviewId: REV-TASK-WSC-703
taskId: TASK-WSC-703
planId: PLAN-WSC-6.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-703-r1
decision: APPROVE
p0: 0
p1: 0
p2: 1
p3: 0
closedFindings: []
openFindings:
  - FIND-WSC-703-R1-001
contracts: wsc-contracts@2.3.0
riskTags: [contracts-bump, rbac-visibility]
reviewedAt: 2026-08-12T10:45:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-6.2.md（§3.1 / §4 / TASK-WSC-703 writeSet·denyModify·acceptance·testScope）
  - product/requirements/SNAP-WSC-006.md（REQ-CAT-011/012/013、SHELL/RBAC 维护 ADMIN-only）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-703.md
  - 实地：contracts/VERSION、openapi.yaml、rbac/matrix.yaml、req-coverage.md、ui/state-matrix.md、errors/codes.yaml、security/sensitive-fields.md
  - 实地：frontend/src/api/**（CONTRACT_VERSION、ListProductsQuery.supplierName）
mustDifferFrom:
  - developer（本任务实现者；本实例未兼任）
spotCheck: |
  writeSet 字面 ⊆ contracts/** + frontend/src/api/**；未改 package.json / pnpm-lock。
  denyModify：features/layouts/router/styles/theme/components、backend、sql、e2e、product、planning、ai/rules、state/events — 本任务 diff 未触碰。
  VERSION=2.3.0 与 OpenAPI info.version / matrix/req-coverage/state-matrix/errors/sensitive 版本头一致。
  listProducts：supplierName 模糊 contains；q=产品名/编码不回退；排序 L3 前/未分类殿后/updatedAt desc/跨页全局。
  matrix：PROVIDER/USER maintenance hidden+403 ERR_MAINTENANCE_FORBIDDEN；ADMIN visible/200；notes ADMIN-only。
  2.2.0 能力抽样：mine、/admin/users、l2-distribution、industryCategory、导入四态、产品写仅 PROVIDER — 未回退。
  tsc --noEmit exit 0（审查侧复跑）。
  check-contracts.mjs 仍 expect 1.1.0（写集外、升版前已陈旧）→ P2，不阻塞。
  未代 tester PASS；未标 VERIFIED；未写 state/events；未改业务代码。
```

## Round 1 结论

**APPROVE** — **P0=0，P1=0**。TASK-WSC-703（契约 `wsc-contracts@2.3.0` + `frontend/src/api/**` + 维护矩阵 ADMIN-only 纠偏）字面 scope / `denyModify` 合规；§3.1 硬冻结与计划 acceptance 源码可复核。开放 finding 仅 P2×1（仓内契约检查脚本仍钉死 `VERSION=1.1.0`，**在 writeSet 外**且升版前已陈旧），**不阻塞**进入独立 tester 门禁。

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-703-R1-001 | P2 | **OPEN** | `tests/contracts/check-contracts.mjs` 硬编码 `VERSION expected 1.1.0`；对本树跑脚本 → `got 2.3.0` FAIL。路径 **不在** TASK-WSC-703 `writeSet`；在基线已为 `2.2.0` 时该脚本亦已无法绿，属预存债而非本任务引入回归。acceptance 要求的 VERSION/OpenAPI/`supplierName`/排序/matrix 可由人工勾选 + OpenAPI 字面复核；**不升格 P1、不据此 REQUEST_CHANGES** | 后续任务（或编排扩写集）将脚本 expected 对齐当前 `contracts/VERSION`（及必要 OpenAPI 断言）后关闭；可选纳入 Wave 收尾 | TASK-WSC-703 testScope「契约 lint」；工程底座 |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — 仅 `contracts/**`（7 文件）+ `frontend/src/api/**`（7 文件）；未动 package/lock |
| denyModify：features / layouts / router / styles / theme / components / backend / sql / e2e / product / planning / ai/rules / state·events | **PASS（703 归因）** — 本任务 diff 未写入 |
| `contracts/VERSION` = `2.3.0`；OpenAPI `info.version` 一致 | **PASS** |
| `listProducts` + `supplierName`（模糊 contains）；`q` 产品名/编码不回退 | **PASS** — OpenAPI description + parameters；client `ListProductsQuery` 同语义 |
| 排序硬描述：有 L3 在前、未分类殿后、次键 `updatedAt` desc、跨页全局 | **PASS** — operation description + 200 response description |
| `matrix.yaml`：PROVIDER hidden+403；ADMIN visible/200；USER hidden+403；notes ADMIN-only | **PASS** — diff 仅纠偏 PROVIDER 维护行 + notes；ADMIN/USER 维护期望正确 |
| 2.2.0 其余能力不回退（mine / 用户管理 / L2 / 导入四态 / industryCategory / 产品写仅 PROVIDER） | **PASS** — OpenAPI/matrix/state-matrix 抽样保留 |
| `frontend/src/api/**` typecheck | **PASS** — 审查侧 `pnpm exec tsc --noEmit -p tsconfig.json` → exit 0；`CONTRACT_VERSION='2.3.0'`；既有模块导出保留 |
| req-coverage / state-matrix V1.5 + 维护 ADMIN-only | **PASS** — CAT-011/012/013、SHELL/RBAC V1.5 行；侧栏维护 ADMIN-only；浏览企业名/未分类/高级筛选 |
| errors / sensitive-fields 版本头对齐 2.3.0 | **PASS** |
| 契约自动 lint 脚本 | **记录 P2** — `check-contracts.mjs` 写集外陈旧 expect（FIND-001）；人工勾选通过 |
| 开发自测 ≠ 独立 tester | **记录** — DEV 自测与审查复跑 tsc 不代 tester PASS |
| 未改业务代码 / 未写 state·events / 未标 VERIFIED | **PASS** |
| `mustDifferFrom` 实现者 | **PASS**（`code-reviewer-wsc-703-r1`） |
| P0 / P1 | **P0=0；P1=0** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester PASS。
- **decision: APPROVE**（P0=0、P1=0；开放 P2×1 可延期，判定为写集外预存债）。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 1 | FIND-001：`check-contracts.mjs` 仍 expect 1.1.0（写集外） |
| P3 | 0 | — |
