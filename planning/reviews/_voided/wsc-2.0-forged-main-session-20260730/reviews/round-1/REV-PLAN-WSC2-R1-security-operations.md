# Round 1 Review — Security Operations

```yaml
reviewId: REV-PLAN-WSC2-R1-securityOperations
planId: PLAN-WSC-2.0
round: 1
role: securityOperations
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  RBAC 矩阵含导入/维护正确。缺口：上传文件临时存储与清理、错误报告是否含 PII 行内容未约定。
```

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-R1-001 | P1 | 107 上传 xlsx/csv 无「禁止持久化原始文件超出作业生命周期 / 作业结束后删除」约束；错误报告可能回显行内供应商信息。 | §3.7 或 107 acceptance：原始上传仅临时存储并在完成后删除（或等价 TTL）；错误报告字段白名单（行号、编码、原因码），禁止回显完整信用代码等 §敏感字段。 | REQ-CAT-008 |
