# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC4-R2-api-data-designer
planId: PLAN-WSC-4.1
round: 2
role: apiDataDesigner
actorInstance: api-data-designer-wsc-004-r2
snapshotIdAtReview: SNAP-WSC-004
contractsBaselineAtReview: wsc-contracts@2.0.0
contractsTargetAtReview: wsc-contracts@2.1.0
decision: APPROVE
basedOnReview: planning/reviews/wsc-4.0/round-1/REV-PLAN-WSC4-R1-api-data-designer.md
summary: |
  作为 ISSUE-API-WSC4-R1-001/002/003 原提出者，对照 PLAN-WSC-4.1 逐条核验
  closeWhen：ImportTemplateColumns 已按 v0729 列名行写入 §3.1 并废止 V1.1 最小列
  example，301 acceptance/testScope 含列名行对照与契约 lint；TEMPLATE_UNSUPPORTED
  已硬同步 codes.yaml + importSemantics + OpenAPI import 400 + state-matrix 态 ④；
  OTHER/TypeSpecific 描述修订与 301 可测 acceptance 已点名。三条均可关闭；本轮无新
  增契约/数据异议。本角色 APPROVE。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-4.1.md`；R1：`REV-PLAN-WSC4-R1-api-data-designer.md`；`SNAP-WSC-004`（APPROVED） |
| 聚焦 | 本角色 R1 ISSUE（001..003）的 `closeWhen` 是否被 4.1 吸收且计划级可验收 |
| 不裁定 | 他角色 ISSUE；页面交互/文案；威胁建模；DAG/写集工程细节（除非直接破坏契约所有权） |

## closeWhen 核验（ISSUE-API-WSC4-R1-*）

| id | sev | closeWhen 要点 | PLAN-WSC-4.1 证据 | 判定 |
|---|---|---|---|---|
| ISSUE-API-WSC4-R1-001 | P1 | §3.1 **显式**增加 `ImportTemplateColumns` 以 v0729 **列名行**重冻；废止 V1.1 最小列 example/描述；标明无产品编码列、服务端 DEC-WSC-001 生成；301 `acceptance`/`testScope`：OpenAPI 该 schema 与权威 xlsx 列名行一致 + 契约 lint/对照；可选：判定用列名行（第 2 行），分组行不参与列名集合等值 | §0 映射本 ISSUE；§3.1 冻结表专行：列名行（第 2 行）重冻、废止 V1.1 最小列 example（含产品编码等）、权威列名集合对齐 SNAP（产品名称（必填）…数据内容描述）、**无**产品编码列、DEC-WSC-001、分组行不参与等值判定；GET 模板字节与重冻 schema 一致；301 writeSet 必须含重冻；acceptance 含重冻/废止/无编码列 + GET 与列名行一致；testScope「模板 GET」+「契约 lint / OpenAPI 校验」含 `ImportTemplateColumns` 与列名行对照 | **closeWhen 满足，可关闭** |
| ISSUE-API-WSC4-R1-002 | P2 | §3.1 或 301 acceptance **硬性**要求：`errors/codes.yaml` + `importSemantics.requestLevel` + OpenAPI import 操作描述/400 example + `ui/state-matrix` 态 ④ 均列入 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` | §3.1「TEMPLATE_UNSUPPORTED 硬同步」行：**必须**同步上述四处；**禁止**「若需同步」软措辞；301 acceptance 同四处硬同步；testScope「TEMPLATE_UNSUPPORTED 四处硬同步可检」；writeSet 含 `errors/codes.yaml`、`ui/state-matrix.md`、相关 importSemantics | **closeWhen 满足，可关闭** |
| ISSUE-API-WSC4-R1-003 | P2 | §3.1：2.1.0 修订 OTHER/`TypeSpecificFields` 描述，允许 `other`（及 report）的 `contentDescription`+共用时间/地域；删除「OTHER 必须空对象」硬表述；301 acceptance 可测 | §3.1「OTHER / TypeSpecific 描述」行点名修订 `info.description` / `ProductType` / `TypeSpecificFields`；report/other 字段含 `contentDescription`；§1.2/§2.2 OQ-004 修订口径；301 acceptance「描述已允许 `contentDescription`；无『OTHER 必须空对象』硬表述」；testScope 四类型 round-trip 含 OTHER `contentDescription` | **closeWhen 满足，可关闭** |

**原提出者确认**：ISSUE-API-WSC4-R1-001、002、003 对 `PLAN-WSC-4.1` **均可关闭**。

## 契约面抽查（closeWhen 之外，无回退）

| 项 | 结论 |
|---|---|
| `contractsTarget` | `wsc-contracts@2.1.0`；301 唯一升级 — 通过 |
| `typeSpecific.api` | §3.3 形状与 SNAP 建议一致；冲突/派生属 SA 域，本角色不重开 | 计划级可验收 |
| 导入错误分层 | TEMPLATE_UNSUPPORTED 与 FORMAT_INVALID 切开；态 ④ 含新码 — 通过 |
| 旧 `endpoint` / §3.6 迁移 | 读兼容；优先无破坏 JSON 扩展 — 通过 |
| Browse 分页 | 继续 `page`/`pageSize`/`total`；禁止本版改分页 — 通过（未重开历史 ISSUE-API-R1-003） |
| 列名行集合 vs SNAP | §3.1 所列权威列名与 SNAP-WSC-004 共用/类型列名一致；无产品编码列 — 通过 |

## 异议

（无。Round 1 本角色三条 ISSUE 均已满足 closeWhen；本轮不新开 ISSUE。）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 决策

`decision: APPROVE`

本角色 R1 阻塞项 ISSUE-API-WSC4-R1-001（P1）及建议同轮吸收的 002/003（P2）在 PLAN-WSC-4.1 均已满足 closeWhen。契约升版 2.1.0 的计划级冻结清单（含模板列 schema、错误码硬同步、OTHER 描述修订）可指导 TASK-WSC-301 验收。不代替他角色关闭或批准。

本评审 **未** 修改计划正文、源码或 Run `state`/`events`。
