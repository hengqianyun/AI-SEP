# PLAN-WSC-9.1 Round 1 评审摘要

```yaml
planId: PLAN-WSC-9.1
round: 1
snapshotId: SNAP-WSC-009
consensus: NOT_REACHED
reviewsComplete: 8
reviewsTotal: 8
approved: 5
requestedChanges: 3
blocked: 0
abstained: 0
```

## 角色决策总览

| 角色 | 实例 | 决策 | ISSUE 数 |
|---|---|---|---|
| productAnalyst | product-analyst-wsc-012-r1 | APPROVE | 0 |
| solutionArchitect | solution-architect-wsc-012-r1 | APPROVE | 0 |
| qaStrategist | qa-strategist-wsc-012-r1 | REQUEST_CHANGES | 3 (1×P1 + 2×P2) |
| parallelPlanner | parallel-planner-wsc-012-r1 | APPROVE | 0 |
| planEditor | plan-editor-wsc-012-r1 | APPROVE | 0 |
| uxUiPlanner | ux-ui-planner-wsc-012-r1 | REQUEST_CHANGES | 3 (3×P1) |
| apiDataDesigner | api-data-designer-wsc-012-r1 | REQUEST_CHANGES | 6 (1×P0 + 3×P1 + 2×P2) |
| securityOperations | security-operations-wsc-012-r1 | REQUEST_CHANGES | 4 (3×P1 + 1×P2) |

## ISSUE 清单（按严重度排序）

| id | 来源角色 | 严重度 | 问题 | closeWhen |
|---|---|---|---|---|
| ISSUE-001 (API) | apiDataDesigner | **P0** | ChainAttestationPort 订单事件请求类型未冻结（「可扩展或并列」二选一未定） | 计划 §3.1 或对应任务明确请求类型定义 |
| ISSUE-002 (API) | apiDataDesigner | P1 | multipart 合约上传字段名与交易信息 JSON schema 未冻结；t_order_line 提交入口未明确 | 计划对应任务冻结字段名/schema |
| ISSUE-003 (API) | apiDataDesigner | P1 | GET /orders 响应九组字段未枚举；分页 envelope 结构未冻结 | 计划对应任务枚举字段/envelope |
| ISSUE-004 (API) | apiDataDesigner | P1 | POST /orders/{orderId}/confirm request body 未指定 | 计划对应任务指定 body |
| ISSUE-001 (UX) | uxUiPlanner | P1 | 统一合约确认页：冻结信息区布局、勾选与按钮 disabled 状态、ADMIN 代确认身份标识 | TASK-WSC-917 acceptance 增补 |
| ISSUE-002 (UX) | uxUiPlanner | P1 | 附件上传交互：冻结上传中进度、三种失败场景反馈、已上传文件操作 | TASK-WSC-917 acceptance 增补 |
| ISSUE-003 (UX) | uxUiPlanner | P1 | 取消对话框：冻结标题/正文（含不可回退警告）、danger 按钮样式、代操作身份 | TASK-WSC-917 acceptance 增补 |
| ISSUE-SEC-001 | securityOperations | P1 | 订单号生成规则缺失，无不可预测性与信息防泄露约束 | 计划对应任务指定生成规则 |
| ISSUE-SEC-002 | securityOperations | P1 | 客户端 enterpriseId/userId 参数 IDOR 风险未在 acceptance 中以负例强制覆盖 | 任务 acceptance 增 IDOR 负例 |
| ISSUE-SEC-003 | securityOperations | P1 | ADMIN 代操作审计：update_by 需记录 ADMIN 身份，时间线需记录代操作人 | 任务 acceptance 增审计条款 |
| ISSUE-QA-001 | qaStrategist | P1 | REQ-WSC-ORDER-017 数字合约版本历史 UI 可见性，§6.1 E2E 无对应场景，TASK-WSC-919 未含 017 | E2E 场景补充或 918 testScope 增断言 |
| ISSUE-SEC-004 | securityOperations | P2 | mock 上链隔离未硬性禁止暴露真实链节点配置/私钥 | 明确禁止条款 |
| ISSUE-QA-002 | qaStrategist | P2 | mock 上链 chainCount 一致性贯穿全状态路径，TASK-WSC-916 testScope 未列出逐态核对 | 916 testScope 补充 |
| ISSUE-QA-003 | qaStrategist | P2 | TASK-WSC-918 外部跳转 testScope「无 URL 提示」措辞含糊 | 改为正向断言 |
| ISSUE-005 (API) | apiDataDesigner | P2 | GET /orders/notices/current 响应是否含版本标识未明确 | 计划补充 |
| ISSUE-006 (API) | apiDataDesigner | P2 | 计划 YAML 头 contractsTarget: 2.3.2 与 body 2.3.3 不一致 | 修正元数据 |

## 批准前最小闭合集（apiDataDesigner）

ISSUE-001（P0）+ ISSUE-002 + ISSUE-003。

## 下一步

共识未达成（5/8 APPROVE，需全部批准）。需 planEditor 修订吸收后进入 Round 2。
