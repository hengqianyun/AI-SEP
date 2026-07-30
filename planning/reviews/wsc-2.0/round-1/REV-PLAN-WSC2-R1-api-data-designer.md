# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC2-R1-apiDataDesigner
planId: PLAN-WSC-2.0
round: 1
role: apiDataDesigner
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  契约升级方向正确。缺口：批量导入是同步响应还是异步 job、错误报告下载端点未写入 §3.1；滚动加载分页/cursor 契约未冻结。
```

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-R1-001 | P1 | §3.1 仅写「批量导入（上传/模板/结果）」，未规定：同步完成 vs jobId 轮询；错误报告 `GET` 路径；部分成功响应字段（successCount/failureCount/reportId）。 | §3.1 冻结导入 API 最小契约（推荐 V1.1 **同步**完成+结果 DTO，或异步则含 job 状态枚举）；101 acceptance 含示例。 | REQ-CAT-008 |
| ISSUE-API-R1-002 | P1 | 104 滚动加载未对应 OpenAPI 分页/cursor 字段；易前后端各写一套。 | §3.1 冻结 browse 列表分页约定（offset/limit 或 cursor）；104 只读消费。 | REQ-CAT-001 |
