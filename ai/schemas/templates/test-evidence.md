# 测试证据

```yaml
evidenceId: EVID-{taskId}-{N}
taskId: TASK-...
status: PASSED | FAILED | BLOCKED
```

## Layers

| name | command | result | relatedReqs | notes |
|---|---|---|---|---|
| unit | | PASSED/FAILED/SKIPPED/BLOCKED | | |

## 阻塞时

- blockedBy: 环境 / 依赖 / 权限说明与解除条件
