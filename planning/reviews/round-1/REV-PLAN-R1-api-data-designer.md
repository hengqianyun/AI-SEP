# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-R1-apiDataDesigner
planId: PLAN-WSC-1.0
round: 1
role: apiDataDesigner
snapshotIdAtReview: SNAP-WSC-001
decision: REQUEST_CHANGES
summary: |
  计划以 TASK-WSC-001 一次性冻结 OpenAPI、RBAC、Flyway 初版、路由与 ChainAttestationPort，唯一所有权与 wsc-contracts@1.0.0 标记清晰；
  DEC-WSC-001 唯一索引、DEC-WSC-002 存证字段、DEC-WSC-003 分类删除约束可落入契约；005/006 串行降低同事务半成品风险。
  主要缺口：上链版本「目录快照」持久化模型未写入 TASK-WSC-001 schema 验收；OVW-004 含 Out of Scope 业务类型的数据来源/种子契约未定义；
  编码冲突与分类禁止删除等错误码语义、以及 OQ-004「其他数据产品」无专属字段的 schema/DTO 口径未成为冻结门禁。
  契约可冻结方向正确，但上述未决冲突关闭前不能 APPROVE。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| API 契约前置 | 通过框架 — TASK-WSC-001 拥有 OpenAPI/错误码/分页筛选/403 语义 |
| 数据模型 / 迁移 | 部分缺口 — 初版实体列表过粗，快照与枚举约束需显式验收 |
| 兼容与回滚 | 可接受框架 — 后继只读 contracts；增量 Flyway 串行；缺错误码目录 |
| 与 UI 契约冲突 | 有风险 — OQ-004 与 OVW-004 数据源未闭合 |

## 契约与实体核对

| 项 | 计划陈述 | 判定 |
|---|---|---|
| 产品编码唯一 + 格式 | DEC-WSC-001；001 acceptance「唯一约束一致」 | 需错误码显式化（ISSUE） |
| 存证适配字段 | ChainAttestationPort；DEC-WSC-002 | 通过 |
| 上链版本 + 目录快照 | 001 仅写「上链版本记录」；CHAIN 需右侧完整快照 | **缺口** |
| 行业分类挂载禁止删 | DEC-WSC-003；005 testScope 负例 | 需 API 错误语义写入 001 |
| OVW-004 三类动态类型 | 003 acceptance 含目录/登记/订单类型 | **数据源未定义** |
| 类型专属字段 / OQ-004 | §2.2 声明；schema/OpenAPI 未强制 | **缺口** |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-R1-001 | P0 | REQ-CHAIN-001 要求按版本展示「产品目录上链详情快照」；TASK-WSC-001 Flyway 验收仅列「上链版本记录」，未要求持久化/可检索的目录快照结构（或等价不可变 JSON/列集）及与 OpenAPI 响应字段对齐。冻结后 006/005 可能无法同版本对齐快照契约。 | TASK-WSC-001 acceptance/testScope 明确：初版 schema + OpenAPI 含版本化目录快照（字段覆盖 REQ-CAT-004 分组或文档化子集），创建后可按 versionId 读取完整快照；契约-REQ 覆盖检查含 REQ-CHAIN-001。 | REQ-CHAIN-001, REQ-CAT-004, REQ-CAT-005 |
| ISSUE-API-R1-002 | P1 | REQ-OVW-004 最新流须含「数据登记」「交易订单」类型，但二者完整业务流为 Out of Scope；计划未定义这些事件的数据来源（只读投影表、种子/fixture、模拟写入适配，或明确仅展示文案型占位记录）及对应 DTO。TASK-WSC-003 无法稳定验收口径。 | 在 TASK-WSC-001 OpenAPI/schema 或计划 §3 增补 OVW 动态流数据契约：类型枚举、最小字段、V1 数据供给方式（含测试 fixture）；TASK-WSC-003 testScope 引用该契约。 | REQ-OVW-004 |
| ISSUE-API-R1-003 | P1 | DEC-WSC-001 要求冲突返回明确错误码；DEC-WSC-003 要求删除拒绝带原因；计划 §3 提到错误码但 TASK-WSC-001 acceptance 未列出最小错误码集（格式非法、编码冲突、分类非空禁止删除、403 写拒绝）及统一响应包装字段。 | TASK-WSC-001 冻结错误码目录（至少上述四类）并与 OpenAPI 示例/联调负例一致；005/002 testScope 可引用同一码表。 | REQ-CAT-005, REQ-CAT-006, REQ-RBAC-001 |
| ISSUE-API-R1-004 | P1 | OQ-004：V1「其他数据产品」无独立类型专属字段；Flyway「类型专属字段」与 OpenAPI 未写明该类型 DTO/表约束（专属区可空/禁止提交专属载荷）。与 CAT-004/005 UI 易产生契约漂移。 | 在初版 schema/OpenAPI 注明：产品类型=其他数据产品时专属字段组不出现或恒为空且服务端忽略/拒绝多余专属字段；001 覆盖检查与 005 acceptance 对齐。 | REQ-CAT-004, REQ-CAT-005 |

## 无异议项（记录）

- 契约唯一写任务 TASK-WSC-001 + 后继只读，可防止并行漂移；CI openapi-diff 方向正确。
- 005 依赖 006、同事务失败整单回滚，数据层半成品版本风险可控。
- 禁止业务层直连真实链 SDK，适配接口边界清晰。
- 不涉及静默破坏已发布契约（绿地首版）；增量迁移串行约束可接受。
