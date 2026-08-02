# 任务包 TASK-WSC-304

```yaml
taskId: TASK-WSC-304
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
status: VERIFIED
wave: W3
verifiedAt: 2026-08-02T19:18:00+08:00
evidenceId: EVID-TASK-WSC-304-1
evidence: planning/tasks/TESTRUN-TASK-WSC-304.md
e2eEvidenceId: TESTRUN-WSC-E2E-V13
goal: |
  目录页导入弹窗适配 v0729 模板下载/上传与四态结果；旧模板文件级拒绝可区分；
  Vitest + Playwright E2E（TESTRUN-WSC-E2E-V13）；报告目录 tests/e2e/reports/p0-wsc-v1.3/；
  禁写 editor/detail/contracts/components。
reqs: [REQ-CAT-008, REQ-API-001, REQ-RBAC-001]
dependsOn: [TASK-WSC-301, TASK-WSC-302, TASK-WSC-303]
actorInstance: developer-wsc-304
```

权威：`planning/approved/PLAN-WSC-4.1.md` § TASK-WSC-304 / §6.1。

## 派发记录

- dispatchedAt: 2026-08-02T18:10:00+08:00
- identity: orchestrator（PO「Implement the plan」含 W3 派发）
