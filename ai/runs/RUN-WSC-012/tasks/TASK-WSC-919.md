# 任务包 — TASK-WSC-919：V1.7 P0 E2E 证据包（串行；与 V1.6/V1.4 共存）

<!-- R2 revised: ISSUE-QA-001 -->

```yaml
taskId: TASK-WSC-919
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: D
status: PENDING
requirements: [REQ-WSC-ORDER-001, REQ-WSC-ORDER-006, REQ-WSC-ORDER-007, REQ-WSC-ORDER-010, REQ-WSC-ORDER-011, REQ-WSC-ORDER-012, REQ-WSC-ORDER-015, REQ-WSC-ORDER-016, REQ-WSC-ORDER-017, REQ-SHELL-011, REQ-WSC-ORDER-FE-001, REQ-WSC-ORDER-FE-002, REQ-WSC-ORDER-FE-003]
dependsOn: [TASK-WSC-915, TASK-WSC-917, TASK-WSC-918]
```

## 1. 任务目标

独占 Playwright，覆盖 §6.1；**不改**业务 feature 源码；保留 `test:p0-v16`、`test:p0-v14`

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `tests/e2e/specs/p0-wsc-v1.7.spec.ts` | 新增 | V1.7 P0 E2E 规格 |
| `tests/e2e/playwright.v17.config.ts` | 新增 | V1.7 Playwright 配置 |
| `tests/e2e/package.json` | 修改 | 增补 **`test:p0-v17`**；**保留** `test:p0-v16`、`test:p0-v14` |
| `tests/e2e/reports/p0-wsc-v1.7/**` | 新增 | 测试报告落点 |
| `tests/e2e/helpers/wsc-v17-fixtures.ts` | 条件新增 | 若需 fixture |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md` | 需求权威 |
| `planning/proposals/PLAN-WSC-9.2.md`（§6.1） | P0 E2E 场景表 |
| 既有 `tests/e2e/**` | V1.6/V1.4 参考 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `tests/e2e/playwright.config.ts` | V1.4 配置不变 |
| `tests/e2e/playwright.v16.config.ts`、`tests/e2e/specs/p0-wsc-v1.6.spec.ts` | V1.6 配置与规格不变 |
| `tests/e2e/specs/p0-wsc-v1.4.spec.ts` | V1.4 规格不变 |
| `frontend/src/**` | 业务源码禁止 |
| `backend/**` | 业务源码禁止 |
| `contracts/**` | 契约归 911 |
| `**/sql/**` | Flyway 归 912 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] §6.1 强制场景通过
- [ ] 命令：`pnpm --dir tests/e2e run test:p0-v17`
- [ ] 报告：`tests/e2e/reports/p0-wsc-v1.7/`；`evidenceId: TESTRUN-WSC-E2E-V17`
- [ ] `test:p0-v16`、`test:p0-v14` 仍可执行
- [ ] P0 缺陷为 0

## 6. 测试范围（testScope）

- 强制：`pnpm --dir tests/e2e run test:p0-v17`
- 强制回归：`pnpm --dir tests/e2e run test:p0-v16`
- 建议：`test:p0-v14`

## 7. 风险标签（riskTags）

- `e2e-gate`
- `release-evidence`
