# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC2-R1-parallelPlanner
planId: PLAN-WSC-2.0
round: 1
role: parallelPlanner
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  拓扑无环；W3 互斥 OK。W4「106 可与 105 并行」在波次表与 dependsOn 叙述上易误解启动门闩。
```

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PP-R1-001 | P1 | §6 写「W4：105；106 可与 105 并行」，但 106 仅 dependsOn 103，可在 104 未完成时与 105 抢合入窗口；波次表未写清 106 的最早启动条件与合入序。 | 波次表写明：106 在 103 VERIFIED 后可启动，**不得与 103 并行**；与 105 并行当且仅当 writeSet 无交集（已满足）；建议合入序 `106 → 105` 或完成先后均可但禁止同 PR 混写。 | REQ-CAT-007, REQ-CAT-004 |
