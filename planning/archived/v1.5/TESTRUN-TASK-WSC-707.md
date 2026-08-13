# 测试证据

```yaml
testrunId: TESTRUN-TASK-WSC-707
evidenceId: TESTRUN-WSC-E2E-V15
taskId: TASK-WSC-707
planId: PLAN-WSC-6.2
runId: RUN-WSC-009
snapshotId: SNAP-WSC-006
actorInstance: tester-wsc-707-r1
decision: PASS
retest: false
contracts: wsc-contracts@2.3.0（只读；hotfix 2.3.1 不属本任务写集）
basedOn:
  - ai/agents/tester.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-707 testScope；§6.1；§9 item 8）
  - planning/tasks/REV-TASK-WSC-707-R2.md（APPROVE；P0=0 P1=0；开放 P2 FIND-WSC-707-R2-001）
  - planning/tasks/TESTRUN-TASK-WSC-HOTFIX-USER-001.md（PASS；v14 2a PASS）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-707.md
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-707-R2.md
reviewRound: 2
reviewP0: 0
reviewP1: 0
reviewOpenP2: 1
specFile: tests/e2e/specs/p0-wsc-v1.5.spec.ts
reportDir: tests/e2e/reports/p0-wsc-v1.5/
executedAt: 2026-08-12T17:08:43+08:00
env:
  E2E_RUN: "1"
  E2E_BASE_URL: "http://127.0.0.1:5173"
  backend: "http://127.0.0.1:8080 actuator/health UP"
  frontend: "http://127.0.0.1:5173 UP"
commands:
  - id: C1
    command: pnpm --dir tests/e2e run test:p0-v15
    cwd: tests/e2e
    env: { E2E_RUN: "1", E2E_BASE_URL: "http://127.0.0.1:5173" }
    exitCode: 0
    result: PASS
    note: "12 passed (27.1s)；junit tests=12 failures=0；§6.1 场景 1–8 全绿"
  - id: C2
    command: pnpm --dir tests/e2e run test:p0-v14
    cwd: tests/e2e
    env: { E2E_RUN: "1", E2E_BASE_URL: "http://127.0.0.1:5173" }
    exitCode: 1
    result: PASS_WITH_KNOWN_DELTAS
    note: "可执行；11 passed / 4 failed；RBAC/基线（1×3、2a、2b×2）PASS；2a soft-delete PASS；失败 4 条均为 intentional V1.5 delta（矩阵见 EVIDENCE.md）"
summary: "C1 门禁全绿；C2 共存可跑且 RBAC/2a 基线绿；故意增量有矩阵豁免；soft-delete BLOCKER 已清"
requirements: [REQ-CAT-011, REQ-CAT-012, REQ-SHELL-001, REQ-CAT-013, REQ-RBAC-001]
mustDifferFrom:
  - developer（本任务实现者）
  - code-reviewer-wsc-707-r2
  - code-reviewer-wsc-707-r1
  - tester-hotfix-user-001-r1
```

## 结论

**PASS** — 独立 `tester-wsc-707-r1` 在 REV Round 2 **APPROVE**（P0=0、P1=0）后真实执行 PLAN-WSC-6.2 TASK-WSC-707 `testScope`：

| # | command | exitCode | result |
|---|---|---|---|
| C1 | `pnpm --dir tests/e2e run test:p0-v15` | **0** | **PASS** — **12/12**；`evidenceId: TESTRUN-WSC-E2E-V15` |
| C2 | `pnpm --dir tests/e2e run test:p0-v14` | **1** | **可执行 + 基线/RBAC PASS**；4 失败 = intentional V1.5 delta（§9 item 8 豁免） |

未将环境阻塞记为 PASS；未伪造 exitCode；未改 `state.yaml` / `events.jsonl`；未 commit；未自行标 VERIFIED。

## §6.1 八场景勾选（C1）

| # | 场景 | 结果 | 用例 |
|---|---|---|---|
| 1 | 浏览文案 + 高级筛选；抽样 `/my-products` | **PASS** | `1)` |
| 2 | 导航收纳 + RBAC 三角色；全链改名；用户管理 | **PASS** | `2a)×3`；`2b)` |
| 3 | 企业名搜 `supplierName`；无 `enterpriseName`；抽样 mine | **PASS** | `3)` |
| 4 | 未分类垫底跨页 | **PASS** | `4)` |
| 5 | Ant Input/Select；令牌层次 | **PASS** | `5)` |
| 6 | 维护 Pagination/Cascader | **PASS** | `6)` |
| 7 | 本页全选 + 统一维护；筛/Tab 清空；无跨页 | **PASS** | `7)` |
| 8 | 维护深链门控 PROVIDER/USER | **PASS** | `8)×2` |

## V1.4 共存（C2）与 §9 item 8

| 分组 | 结果 | 说明 |
|---|---|---|
| 场景 1 真登录三角色 | **PASS** | RBAC 基线 |
| 场景 2a 用户管理 CRUD（含 soft-delete） | **PASS** | hotfix 后全量复跑绿；引用 `TESTRUN-TASK-WSC-HOTFIX-USER-001` |
| 场景 2b 非 ADMIN 不可达用户管理 | **PASS** | RBAC 基线 |
| 场景 3 公共目录 heading「数据目录」×3 | **FAIL → intentional** | 已改名「全链数据目录」 |
| 场景 4 PROVIDER 我的产品搜/维护入口 | **FAIL → intentional** | 高级筛选默认收起 + 维护 ADMIN-only |
| 场景 4x / 5 / 6 / 7a / 7b | **PASS** | 其余基线回归 |

矩阵与 soft-delete **CLEARED** 叙事：`tests/e2e/reports/p0-wsc-v1.5/EVIDENCE.md`（同步刷新 `DEFAULT_COEXISTENCE_MATRIX`，关闭 FIND-WSC-707-R2-001 证据过期项）。

## 命令输出摘录

### C1（exit 0）

```
Running 12 tests using 1 worker
  … ok 1..12 …
  12 passed (27.1s)
V15_EXIT=0
```

日志：`tests/e2e/reports/p0-wsc-v1.5/run-console.log`  
JUnit：`tests/e2e/reports/p0-wsc-v1.5/junit.xml`（tests=12 failures=0）

### C2（exit 1；预期故意增量）

```
  ok  4 … 2a) ADMIN 用户管理 CRUD 抽样 (3.0s)
  …
  4 failed
    3) 公共目录无增改导 — admin|provider|user
    4) PROVIDER 我的产品：增改导 / 无座序图 / 返回仍在我的产品
  11 passed (2.7m)
V14_EXIT=1
```

日志：`tests/e2e/reports/p0-wsc-v1.5/run-v14-console.log`  
V1.4 JUnit：`tests/e2e/reports/p0-wsc-v1.4/junit.xml`（tests=15 failures=3 errors=1）

## testScope / §9 item 8 checklist

| # | 项 | 结果 |
|---|---|---|
| 1 | 强制 `test:p0-v15` 且 §6.1 全过 | **PASS** |
| 2 | 报告落 `reports/p0-wsc-v1.5/`；`evidenceId: TESTRUN-WSC-E2E-V15` | **PASS** |
| 3 | `test:p0-v14` 仍可执行；默认 config 仍指向 v1.4 | **PASS** |
| 4 | RBAC/基线相关场景通过 | **PASS**（1、2a、2b） |
| 5 | soft-delete 2a 在 hotfix 后 PASS | **PASS** |
| 6 | 故意增量失败有矩阵、可豁免 | **PASS** |
| 7 | CR P0/P1=0 前提 | **PASS**（REV-R2 APPROVE） |

## 证据红线

- 未将环境阻塞记为 PASS
- 未用 developer 自测代替本门禁
- 未因 REV 开放 P2 单独判 FAIL（本轮已刷新过期 soft-delete 叙事）
- 未修改业务 frontend/backend/contracts；未写 `ai/runs/**/state.yaml` / `events.jsonl`
- `actorInstance=tester-wsc-707-r1` ≠ developer ≠ code-reviewer-wsc-707-r2
