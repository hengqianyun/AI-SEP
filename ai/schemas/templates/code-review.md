# 代码审查

```yaml
reviewId: REV-CODE-{taskId}-R{N}
taskId: TASK-...
round: 1
decision: APPROVE | REQUEST_CHANGES | BLOCK
```

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-... | P0/P1/P2/P3 | | | |

P0/P1 未清零不得 `APPROVE`。
