# 测试证据

```yaml
testrunId: TESTRUN-TASK-WSC-703
taskId: TASK-WSC-703
planId: PLAN-WSC-6.2
actorInstance: tester-wsc-703-r1
decision: PASS
commands:
  - id: C1
    command: node tests/contracts/check-contracts.mjs
    cwd: repo-root
    exitCode: 1
    result: DOCUMENTED_NON_BLOCKING
    note: "脚本仍硬编码 VERSION expected 1.1.0（写集外 / REV FIND-WSC-703-R1-001 P2）；got 2.3.0。不单独据此 FAIL 全门禁。"
  - id: C2
    command: "手工/文件断言：VERSION、OpenAPI supplierName+排序、matrix 三角色维护行"
    cwd: repo-root
    exitCode: 0
    result: PASS
  - id: C3
    command: pnpm exec tsc --noEmit -p tsconfig.json
    cwd: frontend
    exitCode: 0
    result: PASS
summary: "acceptance 可勾选项全部通过；tsc exit 0；check-contracts 仅因陈旧 pin 1.1.0 失败（P2 写集外，不阻塞）"
reviewRef: planning/tasks/REV-TASK-WSC-703.md
reviewDecision: APPROVE
reviewRound: 1
reviewP0: 0
reviewP1: 0
reviewOpenP2: 1
contracts: wsc-contracts@2.3.0
requirements: [REQ-CAT-012, REQ-CAT-011, REQ-CAT-013, REQ-SHELL-001, REQ-RBAC-001]
executedAt: 2026-08-12T10:40:00+08:00
mustDifferFrom:
  - developer（本任务实现者）
  - code-reviewer-wsc-703-r1
basedOn:
  - ai/agents/tester.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-703 acceptance·testScope；§3.1；§4）
  - planning/tasks/REV-TASK-WSC-703.md（APPROVE；P0=0 P1=0；FIND-001 P2 OPEN）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-703.md
```

## 结论

**PASS** — 独立 `tester-wsc-703-r1` 在 REV Round 1 **APPROVE**（P0=0、P1=0）后真实执行 testScope：

| # | command | cwd | exitCode | result |
|---|---|---|---|---|
| C1 | `node tests/contracts/check-contracts.mjs` | repo-root | **1** | **DOCUMENTED** — `VERSION expected 1.1.0, got 2.3.0`；路径 `tests/contracts/check-contracts.mjs` **不在** writeSet；对齐 REV FIND-001 / 用户说明：**不单独据此 FAIL** |
| C2 | 文件断言（VERSION / OpenAPI / matrix） | repo-root | **0** | **PASS** — 见下方勾选表 |
| C3 | `pnpm exec tsc --noEmit -p tsconfig.json` | `frontend` | **0** | **PASS** |

developer / codeReviewer 自测或审查侧冒烟 **不算** 本门禁；本轮为独立重跑。未修改生产业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。

## acceptance / testScope 勾选（证据）

| # | 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | `contracts/VERSION` = `2.3.0`；OpenAPI `info.version` 一致 | **PASS** | `contracts/VERSION` 内容 `2.3.0`；`openapi.yaml` L4 `version: 2.3.0`；errors/codes、matrix、state-matrix、sensitive-fields、req-coverage 版本头均为 2.3.0 |
| 2 | `listProducts` 含 `supplierName`（模糊 contains）；`q` 不回退 | **PASS** | `openapi.yaml` L542–547：`name: supplierName` +「大小写不敏感 contains」+ 与 `q` 语义分离；L538–540：`q`「产品名/编码…不匹配供应商名称」；L505 操作描述同语义 |
| 3 | 排序硬描述：有 L3 在前、未分类殿后、`updatedAt` desc、跨页全局 | **PASS** | L506–508 operation description；L560–561 200 response description（跨页全局顺序） |
| 4 | matrix ADMIN/PROVIDER/USER 维护 UI+API 与 §4 一致 | **PASS** | ADMIN L16–17 `visible` / `200`；PROVIDER L31–32 `hidden` / `403 ERR_MAINTENANCE_FORBIDDEN`；USER L46–47 同 PROVIDER；notes L63「目录维护 UI+API 仅 ADMIN」 |
| 5 | req-coverage / state-matrix V1.5 + 维护 ADMIN-only | **PASS** | req-coverage：CAT-011/012/013、RBAC V1.5 行；state-matrix 版本头 2.3.0 + 企业名搜/`supplierName`/未分类垫底 + 侧栏维护 ADMIN-only |
| 6 | `frontend/src/api/**` typecheck；`CONTRACT_VERSION` / `ListProductsQuery.supplierName` | **PASS** | C3 exit 0；`client.ts` `CONTRACT_VERSION = '2.3.0'`；`catalog.ts` `ListProductsQuery.supplierName?: string` |
| 7 | 契约 lint 自动脚本 | **记录非阻塞** | C1 FAIL 仅陈旧 pin（FIND-001）；人工 OpenAPI/VERSION/matrix 勾选已替代覆盖 testScope「契约 lint / OpenAPI 校验」意图 |

## 命令输出摘录

### C1 `node tests/contracts/check-contracts.mjs`

```
CONTRACT CHECK FAILED
 - VERSION expected 1.1.0, got 2.3.0
```

（exitCode=1；与 DEV / REV 预告一致；写集外预存债。）

### C3 `pnpm exec tsc --noEmit -p tsconfig.json`（cwd=`frontend`）

```
EXIT:0
```

## 证据红线

- 未将环境阻塞记为 PASS
- 未伪造 tsc / 脚本 exitCode
- 未因写集外陈旧 `check-contracts.mjs` pin 单独判 FAIL（按任务 Notes / REV P2）
- 未修改 `contracts/**` / `frontend/src/api/**` 被测制品（仅写本证据文件）
- 未修改 REV / `ai/runs/**/state.yaml` / `events.jsonl`
- `actorInstance=tester-wsc-703-r1` ≠ developer ≠ `code-reviewer-wsc-703-r1`
