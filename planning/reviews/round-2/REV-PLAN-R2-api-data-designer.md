# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-R2-apiDataDesigner
planId: PLAN-WSC-1.1
round: 2
role: apiDataDesigner
snapshotIdAtReview: SNAP-WSC-001
decision: APPROVE
summary: |
  对照 Round 1 本人提出的 ISSUE-API-R1-001/002/003/004，逐条核验 PLAN-WSC-1.1 修订与 closeWhen。
  版本化目录快照已写入 §3.1 与 TASK-WSC-001 acceptance/testScope（含 REQ-CHAIN-001 覆盖）；
  OVW 动态流契约含类型枚举、最小字段与 V1 fixture 供给并由 003 引用；
  错误码最小集四类已冻结且 002/005 可引用；OQ-004 专属字段恒空/忽略或拒绝已与 005 对齐。
  四条 ISSUE 的 closeWhen 均已满足，本角色 APPROVE。无新增异议。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-1.1.md` |
| 快照 | `product/requirements/SNAP-WSC-001.md` |
| R1 对照 | `planning/reviews/round-1/REV-PLAN-R1-api-data-designer.md` |

## closeWhen 核验（ISSUE-API-R1-*）

| id | severity | closeWhen 要点 | PLAN-WSC-1.1 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-API-R1-001 | P0 | 001 acceptance/testScope：初版 schema+OpenAPI 含版本化目录快照（覆盖 REQ-CAT-004 分组或文档化子集），可按 versionId 读完整快照；契约-REQ 覆盖含 REQ-CHAIN-001 | §3.1 Flyway「上链版本记录 + 版本化目录快照」；001 acceptance「可按 versionId 读取」；testScope「版本快照读写」+「契约-REQ 覆盖检查（含 REQ-CHAIN-001）」；006 按 versionId 读快照联测 | **closeWhen 满足，可关闭** |
| ISSUE-API-R1-002 | P1 | §3 或 001 增补 OVW 动态流契约（类型枚举、最小字段、V1 供给含 fixture）；003 testScope 引用 | §3.1 OVW 动态流契约（CATALOG_REGISTER/DATA_REGISTER/TRADE_ORDER、最小字段、只读投影/seed/fixture、禁止依赖未实现业务 API）；003 objective 消费该契约；testScope fixture 注入三类类型 | **closeWhen 满足，可关闭** |
| ISSUE-API-R1-003 | P1 | 001 冻结错误码目录（格式非法、编码冲突、分类非空禁止删、403）并与 OpenAPI 示例一致；005/002 testScope 可引用同一码表 | §3.1 错误码目录四码 + 统一响应包装；001 acceptance/testScope「错误码目录与示例一致性」；002 acceptance `ERR_FORBIDDEN`、testScope 403 负例；005 acceptance/testScope `ERR_PRODUCT_CODE_*` / `ERR_CATEGORY_HAS_PRODUCTS` | **closeWhen 满足，可关闭** |
| ISSUE-API-R1-004 | P1 | schema/OpenAPI：其他数据产品专属字段不出现或恒空且服务端忽略/拒绝多余载荷；001 与 005 acceptance 对齐 | §3.1 OQ-004 schema/DTO；001 acceptance+OQ-004 DTO 负例；005 acceptance「不展示、不校验类型专属字段区，仅基础信息」+ testScope OQ-004 用例 | **closeWhen 满足，可关闭** |

## 本轮 ISSUE 表

无未关闭 ISSUE。R1 四条均确认可关闭；本轮不新增 ISSUE。

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 无异议项（记录）

- `wsc-contracts@1.1.0` 与 TASK-WSC-001 唯一写、后继只读（含 `frontend/src/api/**`）仍可防止并行契约漂移。
- 005 依赖 006、适配失败整单回滚，目录快照与上链版本同事务半成品风险仍可控。
- 本角色不代替必需角色批准；仅就 API/数据域 R1 异议关闭与契约可冻结性给出 APPROVE。
