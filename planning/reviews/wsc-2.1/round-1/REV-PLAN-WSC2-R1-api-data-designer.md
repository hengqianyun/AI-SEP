# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC2-R1-apiDataDesigner
planId: PLAN-WSC-2.1
round: 1
role: apiDataDesigner
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  相对 PLAN-WSC-2.0，§3.1 已吸收导入同步 API（multipart POST + successCount/failureCount/reportId + GET 模板/错误报告、明确不采用异步 job）与资源模型「维护条目 ≡ 产品」，错误码清单与 101 唯一写契约方向正确，可支撑 CAT-007/008 契约化。
  仍阻塞 APPROVE：browse 分页在既有 wsc-contracts@1.1.0 已冻结为 page/pageSize/total，本计划却以「或」重新打开 offset/limit 与 hasMore，存在破坏性兼容风险；ERR_IMPORT_ROW_INVALID 与 OQ-V11-004 部分成功的 HTTP/报告语义未切开，易导致 101/107 与前端对「整单失败 vs 行失败」各写一套。
```

## 评审范围（本角色）

| 维度 | 结论 |
|---|---|
| OpenAPI / 契约冻结所有权 | 通过框架 — 101 独占 `contracts/**`；版本 `wsc-contracts@2.0.0` |
| 资源模型（维护 ≡ 产品） | 通过 — 与 OQ-V11-003 暂定口径一致；不得拆两套实体 |
| 批量导入同步 API | 大体通过 — 同步形状已写；错误码/HTTP 语义仍缺（见 ISSUE） |
| Browse 分页 | **缺口** — 未继承 V1.0 已冻结字段，反而放宽「或」 |
| 错误码目录 | 部分通过 — 新增码列出；行级 vs 请求级边界未冻结 |
| 兼容 / 迁移 | 可接受框架 — 允许破坏性 schema + 备份演练；后继只读契约 |

## 对照核对

| 项 | 计划陈述 | 判定 |
|---|---|---|
| 维护条目 ≡ 产品 | §3.1 资源模型；101 acceptance | 通过 |
| 导入同步 + 结果 DTO | POST multipart；successCount/failureCount/reportId；无 job 轮询 | 通过（形状） |
| GET 模板 / GET 错误报告 | §3.1 已写 | 通过（路径细节可交 101 OpenAPI） |
| Browse 分页 | page+size **或** offset+limit；total **或** hasMore | **缺口** — 与现网契约冲突 |
| 错误码新增 | FILE_TOO_LARGE / ROW_INVALID / LEAF_REQUIRED / MAINTENANCE_FORBIDDEN | ROW_INVALID 语义不清 |
| 三级路径 / 存证快照 | OpenAPI + 存证含三级路径 | 通过（计划级） |
| DEC-WSC-001 编码 | 继承 V1.0 错误码（CONFLICT/FORMAT） | 通过（隐含继承） |
| DEC-WSC-002 存证 | 成功行上链；快照三级路径 | 通过（计划级） |
| 报告制品生命周期 | §3.5 仅覆盖原始上传临时文件 | 次要缺口（P2） |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-R1-003 | P1 | 现有 `contracts/openapi/openapi.yaml`（wsc-contracts@1.1.0）`GET /catalog/products` 已冻结 `page`/`pageSize`，`ProductPage` 必含 `items/page/pageSize/total`。PLAN-WSC-2.1 §3.1 Browse 分页写「page+size（或 offset+limit…）」及「total 或 hasMore」，等于在升级到 `@2.0.0` 时重新打开已冻结约定，滚动加载与 104/维护列表易与生成 client 分叉。 | §3.1 **写死继承** V1.0：查询 `page`+`pageSize`、响应 `items`+`page`+`pageSize`+`total`；声明滚动加载=递增 `page`（筛选条件不变）；禁止本版改用 offset/limit 或仅 `hasMore` 替代 `total`（除非另立 DEC 并写明破坏性迁移）。101 acceptance 显式引用上述字段名。 | REQ-CAT-001, REQ-CAT-007 |
| ISSUE-API-R1-004 | P1 | §3.1 同时要求 OQ-V11-004 部分成功（结果 DTO 含 success/failure 计数 + reportId）与新增 `ERR_IMPORT_ROW_INVALID`，但未规定：行级失败是报告内 reason 码还是整单 HTTP envelope code；部分成功时 HTTP/`code` 是否仍成功（`"0"`）。`ERR_IMPORT_FILE_TOO_LARGE` 为请求级清晰，ROW_INVALID 边界不清 → 101/107/前端会对「可下载报告的 200」与「整单 4xx」各自假设。 | §3.1（或错误码表）切开：**请求级**（整单拒绝，无部分写入）至少含过大文件、无法解析/非 xlsx\|csv → 对应 HTTP 错误码（可保留/补充 FORMAT 类码）；**行级**失败只进入错误报告（行号+编码+原因码/文案），导入 API 在已接受文件并完成处理后返回成功 envelope + `successCount`/`failureCount`/`reportId`（failureCount>0 时 reportId 必填）。明确 `ERR_IMPORT_ROW_INVALID` 仅作报告内原因码（或删除该 HTTP 码、改名 report reason）。101 acceptance 含两种示例（整单拒绝 vs 部分成功）。 | REQ-CAT-008 |
| ISSUE-API-R1-005 | P2 | `GET` 错误报告依赖 `reportId`，§3.5 只约束原始上传临时文件删除/TTL，未约定报告制品保留时长、过期响应码；同步模型下报告仍须短时可下载。 | §3.1 或 §3.5 补一句：错误报告 TTL（建议对齐上传 ≤24h）及过期/未知 reportId 的错误码（如 404/`ERR_IMPORT_REPORT_NOT_FOUND`）；101 契约或 runbook 可引用。 | REQ-CAT-008 |

## 对已吸收 ISSUE 的独立看法（非 close 裁定）

| 原 ISSUE | 本版是否满足原 closeWhen 意图 |
|---|---|
| ISSUE-API-R1-001（同步导入+DTO） | **是** — 同步形状与字段已写入 §3.1；残余见 ISSUE-API-R1-004 |
| ISSUE-API-R1-002（browse 分页冻结） | **否** — 仍留「或」且未对齐 V1.0 字段名；见 ISSUE-API-R1-003 |

> 正式 closeWhen 确认属 Round 2 原提出者动作；上表仅供编排参考。
