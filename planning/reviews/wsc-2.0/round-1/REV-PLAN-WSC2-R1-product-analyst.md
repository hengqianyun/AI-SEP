# Round 1 Review — Product Analyst

```yaml
reviewId: REV-PLAN-WSC2-R1-productAnalyst
planId: PLAN-WSC-2.0
round: 1
role: productAnalyst
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  增量范围与 SNAP-WSC-002 对齐：CAT-007/008 有主任务；三级修订覆盖 SHELL/RBAC/CAT-001/002/006。
  缺口：OQ-V11-003 暂定口径未写入 §3.1 schema/DTO 硬约束；OVW 无主任务时「可编译」验收过软，易漏回归。
```

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PA-R1-001 | medium | §2.2 写明 OQ-V11-003 同一可关联条目，但 §3.1 契约表未要求 OpenAPI/schema 用单一资源模型（或显式 alias），101 可能拆成两套实体。 | §3.1 增加「目录维护条目 ≡ 数据产品」资源模型约束；101 acceptance 含该映射检查。 | REQ-CAT-007, REQ-CAT-005 |
| ISSUE-PA-R1-002 | medium | §9 将 OVW 归「E2E + 可编译」，无 testScope 绑定具体任务；SNAP 成功标准仍要求总览可用。 | 指定 OVW 回归归属（101 或独立热修协议）并在 E2E §6.1 列出 OVW 断言步骤为必跑。 | REQ-OVW-001..005 |
