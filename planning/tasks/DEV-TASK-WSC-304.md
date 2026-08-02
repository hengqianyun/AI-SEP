# DEV-TASK-WSC-304

```yaml
taskId: TASK-WSC-304
actorInstance: developer-wsc-304
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
status: READY_FOR_REVIEW
completedAt: 2026-08-02T19:15:00+08:00
reqs:
  - REQ-CAT-008
  - REQ-API-001
  - REQ-RBAC-001
evidenceIdTarget: TESTRUN-WSC-E2E-V13
e2eSpec: tests/e2e/specs/p0-wsc-v1.3.spec.ts
e2eReportDir: tests/e2e/reports/p0-wsc-v1.3/
```

## 摘要

关闭 **BUG-WSC-304-005**（typecheck TS6133）：拆分 `importGuards` 后 `useProductImport.ts` 多余 import 了未在本文件使用的 `IMPORT_REQUEST_ERROR_HINTS`（再导出不需本地绑定）。已删除该 import；`vue-tsc` **EXIT=0**；import Vitest **16/16 PASSED**。

r3 E2E 已 10/10；本轮仅清 typecheck 阻塞。状态 **READY_FOR_REVIEW**。

## BUG-WSC-304-005

| 项 | 内容 |
|---|---|
| 现象 | `TS6133: 'IMPORT_REQUEST_ERROR_HINTS' is declared but its value is never read` |
| 修复 | `useProductImport.ts`：从 `import { ... }` 去掉 `IMPORT_REQUEST_ERROR_HINTS`；保留 `export { ... } from './importGuards'` |
| 验证 | `pnpm exec vue-tsc --noEmit -p tsconfig.json --pretty false` → **EXIT=0** |

## 自测

```text
cd frontend
pnpm exec vue-tsc --noEmit -p tsconfig.json --pretty false   # EXIT=0
pnpm exec vitest run src/features/catalog/import --reporter=default  # 16 passed
```

## 变更（本轮 writeSet）

| 路径 | 说明 |
|---|---|
| `frontend/src/features/catalog/import/composables/useProductImport.ts` | 去掉未使用 import（BUG-005） |
| `planning/tasks/DEV-TASK-WSC-304.md` | 本报告 |

既有交付（004 barrel/guards、001 CSV 预检、003 三段编码、E2E 规格等）不变。

## SCOPE

- 未改 browse/backend/contracts/api/editor/detail/state/events/REV
- 未标 VERIFIED；未调度 CR/tester

## 状态

**READY_FOR_REVIEW** — 停止。
