# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC2-R1-qaStrategist
planId: PLAN-WSC-2.0
round: 1
role: qaStrategist
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  各任务有 testScope；E2E 增量场景方向正确。缺口：导入部分成功缺少可重复 fixture 约定；滚动加载缺少稳定性断言；W5 命令/报告路径可更硬。
```

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-R1-001 | P1 | 107 testScope 列失败原因但未规定仓库内 fixture 文件路径与「固定 成功N/失败M」期望，tester 难复现。 | 107 testScope 指定 `tests/fixtures/import/`（或等价）至少：全成功、编码冲突、分类不匹配、超限各一份；断言成功/失败计数。 | REQ-CAT-008 |
| ISSUE-QA-R1-002 | P1 | 104 滚动加载 acceptance 有 loading，但 testScope 未要求「筛选后 load-more 不丢条件」断言。 | 104 testScope 增加：设置筛选 → 触发加载更多 → 结果仍满足筛选；保留 scroll 位置抽样。 | REQ-CAT-001, REQ-CAT-002 |
