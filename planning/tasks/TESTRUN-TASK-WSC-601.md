# 测试证据

```yaml
evidenceId: TESTRUN-TASK-WSC-601
taskId: TASK-WSC-601
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
actorInstance: tester-wsc-601
contracts: wsc-contracts@2.2.0
basedOn:
  - planning/approved/PLAN-WSC-5.2.md (§3.1 / TASK-WSC-601 acceptance + testScope)
  - planning/tasks/REV-TASK-WSC-601.md
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-601.md
  - contracts/VERSION
  - contracts/openapi/openapi.yaml
  - contracts/errors/codes.yaml
  - contracts/rbac/matrix.yaml
  - contracts/ui/state-matrix.md
  - contracts/req-coverage.md
  - frontend/src/api/**
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-601.md
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-05T16:02:52+08:00
result: PASS
failedCommandCount: 1
```

## 结论摘要

**PASS** — 独立 `tester-wsc-601` 按 PLAN-WSC-5.2 TASK-WSC-601 `testScope` 实跑：

1. `node tests/contracts/check-contracts.mjs` → **exitCode=1**（VERSION expected 1.1.0, got 2.2.0；脚本钉死滞后，**记该命令 FAIL**，不作伪 PASS）。
2. 可命名 P0 脚本断言 `assert-601-p0.mjs` → **64 PASS / 0 FAIL，exitCode=0**，覆盖 §3.1 硬冻结 + 2.1.0 回归 + client 对齐。
3. matrix × §4 三角色交叉表 → PASS（`03-matrix-vs-section4.md`）。
4. 勾选：`ERR_ROLE_SWITCH_DISABLED` ∈ codes + OpenAPI enum；`LoginRequest` 无 `role`；`AdminUser.required` 六字段齐全 → 均由脚本断言 PASS。
5. `cd frontend && pnpm run typecheck` → **exitCode=0**。
6. 2.1.0 关键 schema/错误码回归 grep → PASS（`05-regression-21.txt`）。
7. 对账表 601 行 P0 行为均有文件路径定位（`06-preland-reconcile-paths.md`）。

- 审查前提：REV-TASK-WSC-601 **APPROVE**，P0=0，P1=0
- **未**将 check-contracts 工具链滞后记为环境阻塞 PASS；该命令如实 FAIL，语义由命名断言覆盖（与 REV FIND-WSC-601-R1-001 一致，不升格为本任务产品 BUG）
- 未修改 contracts/frontend 业务源码、`state.yaml`、`events.jsonl`；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-008/tester-wsc-601/`

## commands

| # | command | cwd | exitCode | result | evidence |
|---|---|---|---|---|---|
| 1 | `node tests/contracts/check-contracts.mjs` | repo root | **1** | **FAIL** | `01-check-contracts.txt` / `01-check-contracts-exit.txt` — `VERSION expected 1.1.0, got 2.2.0` |
| 2 | `node ai/runs/RUN-WSC-008/tester-wsc-601/assert-601-p0.mjs` | repo root | **0** | **PASS** | `02-p0-assert.txt` / `02-p0-assert-exit.txt` — 64/0 |
| 3 | matrix × §4 文档交叉检查 | n/a（read） | n/a | **PASS** | `03-matrix-vs-section4.md` |
| 4 | `pnpm run typecheck` | `frontend` | **0** | **PASS** | `04-typecheck.txt` / `04-typecheck-exit.txt` — `vue-tsc --noEmit` |
| 5 | 2.1.0 回归 grep | repo root | n/a | **PASS** | `05-regression-21.txt` |
| 6 | 对账表 601 P0 路径定位 | n/a | n/a | **PASS** | `06-preland-reconcile-paths.md`（绑定 #2 断言名） |

**failedCommandCount = 1**（仅 #1 官方契约脚本；补偿断言 #2 已证明 §3.1）。

## testScope 对照 checklist

| # | testScope 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | 契约/OpenAPI 校验（check-contracts） | 命令 **FAIL**（VERSION 钉死 1.1.0）；§3.1 由命名脚本断言 **PASS** | #1 FAIL + #2 PASS |
| 2 | matrix × §4 三角色关键能力交叉 | **PASS** | `03-matrix-vs-section4.md` |
| 3a | `ERR_ROLE_SWITCH_DISABLED` ∈ codes.yaml + OpenAPI enum | **PASS** | assert：codes + enum；openapi L1054；codes L82+ |
| 3b | `LoginRequest` 无 role | **PASS** | assert；openapi L1090–1098；`frontend/src/api/auth.ts` |
| 3c | `AdminUser` required 齐全 | **PASS** | assert：userId/username/displayName/role/enterpriseName/deleted |
| 4 | `pnpm run typecheck` | **PASS** exitCode=0 | #4 |
| 5 | 回归：2.1.0 关键 schema/错误码仍在 | **PASS** | TypeSpecificApi；ERR_IMPORT_*；reportFieldsWhitelist；NO_UPDATE |
| 6 | 预落地对账自动化（601 行 P0 路径） | **PASS** | `06-preland-reconcile-paths.md` |

## 勾选断言明细（§3.1 / acceptance）

| 勾选项 | 结果 | 路径 |
|---|---|---|
| VERSION / info.version = 2.2.0 | PASS | `contracts/VERSION`；openapi L4 |
| matrix 与 §4 一致（ADMIN 产品写/导入 hidden+403 等） | PASS | `contracts/rbac/matrix.yaml` + 交叉表 |
| `/admin/users` 命名 AdminUser*；软删 PUT deleted；无 password/hash 响应字段 | PASS | openapi AdminUser*；admin.ts |
| `/catalog/l2-distribution`；products `mine` | PASS | openapi L291+、L475 |
| `POST /auth/session/role` → 410 + ERR_ROLE_SWITCH_DISABLED | PASS | openapi L87–116 |
| 用户错误码 CONFLICT/NOT_FOUND + enum | PASS | codes.yaml；openapi enum |
| 叙述无 ADMIN 产品写；createProduct 仅 PROVIDER | PASS | openapi createProduct summary/description |
| state-matrix 2.2.0；导入宿主=我的产品；角色只读 | PASS | `contracts/ui/state-matrix.md` |
| 2.1.0 typeSpecific / 导入四态码 / 报告白名单不回退 | PASS | `05-regression-21.txt` + assert |
| frontend/src/api typecheck | PASS | exitCode=0 |

## matrix × §4 摘要

完整表见 `03-matrix-vs-section4.md`。要点：ADMIN 产品写/导入 hidden+403；PROVIDER 写/导入+myProducts；USER 只读；userManage 仅 ADMIN；三角色 `sessionRoleSwitch` 均为 410 + `ERR_ROLE_SWITCH_DISABLED`。

## REQ 追踪

| REQ | 覆盖方式 | 结果 |
|---|---|---|
| REQ-RBAC-001 | matrix 2.2.0 三角色 × 产品写/导入/userManage；OpenAPI 叙述仅 PROVIDER | PASS |
| REQ-SHELL-001 | 切角色 410 + 码硬同步；LoginRequest 无 role；state-matrix 角色只读 / 菜单面 | PASS |
| REQ-CAT-009 | `/catalog/l2-distribution` + `L2Distribution` schema；client `getL2Distribution` | PASS |
| REQ-USER-001 | `/admin/users` + AdminUser required；ERR_USER_*；client admin.ts | PASS |
| REQ-CAT-010 | `mine` + `createBy`；公共目录只读叙述；导入宿主=我的产品 | PASS |

## 对 check-contracts FAIL 的处理（非 BUG）

| 项 | 说明 |
|---|---|
| 命令 | `node tests/contracts/check-contracts.mjs` exitCode=**1** |
| 原因 | 脚本硬编码 `VERSION expected 1.1.0`；当前 `contracts/VERSION`=`2.2.0` |
| 范围 | 脚本在 `tests/contracts/**`，**不在** TASK-WSC-601 writeSet（REV FIND-001 / DEV 已声明） |
| 补偿 | `assert-601-p0.mjs` 64 项命名断言证明 §3.1；不得把工具链滞后伪称为 PASS，亦**不**因脚本滞后对本任务契约交付记 FAIL |
| 交回条件（工具链专任，非 601 产品退回） | 另开任务将 check-contracts 期望升至当前主版本并纳入 CI |

## BUG

无。本轮 **不** 因 #1 脚本滞后开产品 BUG 交回 developer-wsc-601。

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未伪造 check-contracts exit 0
- 未修改业务实现（contracts/frontend 源码）、REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-601`（≠ developer-wsc-601 / ≠ code-reviewer-wsc-601-r1）
- 未自行宣称 VERIFIED

## 计数

| 指标 | 值 |
|---|---|
| result | **PASS** |
| failedCommandCount | **1**（check-contracts only） |
| P0 脚本断言 | 64 PASS / 0 FAIL |
| typecheck exitCode | 0 |
| BUG | 0 |
