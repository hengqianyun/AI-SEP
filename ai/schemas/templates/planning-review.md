# 规划评审

```yaml
reviewId: REV-PLAN-R{N}-{role}
planId: PLAN-...
round: 1
role: {roleId}
decision: APPROVE | REQUEST_CHANGES | BLOCK | ABSTAIN
summary: |
  ...
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-... | P0/P1/P2/P3 | | | |

## ABSTAIN 时

- abstainReason: （须说明为何本领域不适用；Orchestrator 验证）
