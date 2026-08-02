# BUG-WSC-E2E-001 — 目录页未挂载 ImportDialog（E2E 场景 4）

```yaml
bugId: BUG-WSC-E2E-001
taskId: W5-E2E-V11
severity: P0
status: CLOSED
reportedBy: tester-wsc-e2e-v11
evidenceId: TESTRUN-WSC-E2E-V11
relatedReqs: [REQ-CAT-008]
relatedFindings: [FIND-WSC-107-R1-001, FIND-WSC-107-R1-002]
createdAt: 2026-07-31T23:16:30+08:00
fixedAt: 2026-07-31T23:21:00+08:00
fixId: FIX1
fixBy: developer-wsc-e2e-fix1
closedAt: 2026-07-31T23:30:50+08:00
closedBy: tester-wsc-e2e-v11-r2
```

## 现象

壳层「批量导入」进入 `/catalog?import=1` 后，`#import-modal` 不可见；§6.1 场景 4（批量导入 partial-success）E2E 失败。

## 证据

- `planning/tasks/TESTRUN-WSC-E2E-V11.md`（r2 PASSED，supersedes r1 FAILED）
- `ai/runs/RUN-WSC-002/tester-wsc-e2e-v11-r2/02-playwright-v11-r2.txt`
- `ai/runs/RUN-WSC-002/tester-wsc-e2e-v11/04-playwright-v11-final.txt`（r1 失败原始）
- `planning/tasks/REV-TASK-WSC-107.md` FIND-001 / FIND-002
- `planning/tasks/REV-TASK-WSC-107-FIX1.md`（APPROVE）

## FIX1（developer-wsc-e2e-fix1）

- DEV: `planning/tasks/DEV-TASK-WSC-107-FIX1.md`
- 挂载 ImportDialog + 错误报告 CSV 下载
- codeReviewer：`REV-TASK-WSC-107-FIX1` APPROVE（P0=0, P1=0）

## closeWhen

1. 按 `frontend/src/features/catalog/import/INTEGRATION.md` 在目录浏览页挂载 `ImportDialog`（含 `?import=1` 与关闭清 query） — **满足（FIX1）**
2. 错误报告 UI 可实际下载/打开（对齐 FIND-002） — **满足（FIX1 + E2E `.report-meta` 行数可观测）**
3. 独立 tester 复跑 `TESTRUN-WSC-E2E-V11` 场景 4（及完整波次）PASSED；P0 缺陷为 0 — **满足（tester-wsc-e2e-v11-r2，11 passed，exitCode 0）**

## Close record

| 字段 | 值 |
|---|---|
| closedBy | tester-wsc-e2e-v11-r2 |
| closedAt | 2026-07-31T23:30:50+08:00 |
| evidence | TESTRUN-WSC-E2E-V11 round r2 PASSED；`tests/e2e/reports/p0-wsc-v1.1/index.html` |
| verification | §6.1 场景 1–6 全部 PASSED；场景 4 `#import-modal` + partial-success 计数 + 报告链可观测 |
| note | 与 developer-wsc-e2e-fix1 / code-reviewer-wsc-e2e-fix1 / tester-wsc-e2e-v11 实例隔离 |
