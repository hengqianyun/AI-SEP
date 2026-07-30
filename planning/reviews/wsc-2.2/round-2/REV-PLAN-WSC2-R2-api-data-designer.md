# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC2-R2-apiDataDesigner
planId: PLAN-WSC-2.2
round: 2
role: apiDataDesigner
snapshotIdAtReview: SNAP-WSC-002
decision: APPROVE
summary: |
  对照本角色 Round 1（PLAN-WSC-2.1）提出的 ISSUE-API-R1-003/004/005 的 closeWhen，
  逐条核验 PLAN-WSC-2.2 §3.1/§3.5 与 TASK-WSC-101 acceptance：Browse 分页已写死
  page/pageSize/items/total；导入请求级 vs 行级错误语义已切开且 ROW_INVALID 仅作报告内
  原因码；错误报告 TTL≤24h 与 ERR_IMPORT_REPORT_NOT_FOUND 已冻结。三条 ISSUE 均可关闭；
  本轮无新增契约/数据异议。本角色 APPROVE。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `PLAN-WSC-2.2`；`SNAP-WSC-002`；R1 评审 `REV-PLAN-WSC2-R1-apiDataDesigner` |
| 聚焦 | 确认本角色 R1 ISSUE 的 `closeWhen`；独立 decision |
| 不裁定 | 他角色 ISSUE；页面文案/视觉；威胁建模结论 |

## closeWhen 核验（ISSUE-API-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-2.2 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-API-R1-003 | P1 | §3.1 写死继承 V1.0：查询 `page`+`pageSize`；响应 `items`+`page`+`pageSize`+`total`；滚动加载=筛选不变下递增 `page`；禁止 offset/limit 或仅 `hasMore` 替代 `total`；101 acceptance 显式字段名 | §3.1 Browse 分页行已写死上述字段与滚动语义，并禁止本版改用 offset/limit 或仅 `hasMore`（除非另立 DEC）；101 acceptance「Browse 字段名为 `page`/`pageSize`/`total`」；104/106 acceptance 亦引用同约定 | **closeWhen 满足，可关闭** |
| ISSUE-API-R1-004 | P1 | 切开请求级（整单拒绝）vs 行级（仅报告内）；部分成功返回成功 envelope + 计数 + reportId（failureCount>0 时必填）；`ERR_IMPORT_ROW_INVALID` 仅作报告内原因码；101 acceptance 含两种示例 | §3.1 导入错误语义：>10MB / 无法解析 → 请求级 HTTP 错误；行级失败进报告；处理后成功 envelope + `successCount`/`failureCount`/`reportId`（failureCount>0 时 reportId 必填）；`ERR_IMPORT_ROW_INVALID` 明确「仅作报告内原因码」；错误码表标注「报告内」；101 acceptance「请求级 vs 行级错误示例（整单拒绝 vs 部分成功 envelope）」 | **closeWhen 满足，可关闭** |
| ISSUE-API-R1-005 | P2 | §3.1 或 §3.5：错误报告 TTL（对齐上传 ≤24h）及过期/未知 reportId → 404/`ERR_IMPORT_REPORT_NOT_FOUND` | §3.5 错误报告 TTL≤24h；过期/未知 → `404` / `ERR_IMPORT_REPORT_NOT_FOUND`；§3.1 错误码清单已含该码 | **closeWhen 满足，可关闭** |

## 契约冻结摘要（无回退）

| 项 | 结论 |
|---|---|
| 维护条目 ≡ 产品 | 仍冻结；OpenAPI 不得拆两套实体 |
| 导入同步 API | multipart POST + 计数/reportId；无异步 job |
| Browse 分页 | 与 V1.0 字段名对齐，不再「或」放宽 |
| 导入错误分层 | 请求级 / 行级边界可指导 101/107/前端 |
| 报告生命周期 | TTL + 专用错误码可测 |
| 101 独占契约 | `contracts/**` / 生成 client 唯一写仍成立 |

## 异议

（无。Round 1 本角色三条 ISSUE 均已满足 closeWhen；本轮不新开 ISSUE。）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |
