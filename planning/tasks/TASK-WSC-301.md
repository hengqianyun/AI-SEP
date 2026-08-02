# 任务包 TASK-WSC-301

```yaml
taskId: TASK-WSC-301
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
status: VERIFIED
wave: W1
verifiedAt: 2026-08-02T17:57:30+08:00
evidenceId: EVID-TASK-WSC-301-1
evidence: planning/tasks/TESTRUN-TASK-WSC-301.md
goal: |
  冻结 wsc-contracts@2.1.0；生成 client；后端 typeSpecific 扩展与 v0729 导入解析；
  模板下载；旧模板文件级拒绝；§3.3 方案 B 导入侧派生与写冲突规则。
reqs:
  - REQ-CAT-008
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-API-001
  - REQ-RBAC-001
allowModify: writeSet（见 PLAN-WSC-4.1 § TASK-WSC-301）
actorInstance: developer-wsc-301
codeReviewerMustDiffer: true
```

## 验收 / testScope

权威全文见 `planning/approved/PLAN-WSC-4.1.md` § TASK-WSC-301。

## 派发记录

- dispatchedAt: 2026-08-02T18:00:00+08:00
- identity: orchestrator（PO「Implement the plan」含 W1 派发）
