# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC3-R2-api-data-designer
planId: PLAN-WSC-3.1
round: 2
role: apiDataDesigner
actorInstance: api-data-designer-wsc-003-r2
snapshotIdAtReview: SNAP-WSC-003
contractsBaselineAtReview: wsc-contracts@2.0.0
decision: ABSTAIN
basedOnReview: planning/reviews/wsc-3.0/round-1/REV-PLAN-WSC3-R1-api-data-designer.md
summary: |
  PLAN-WSC-3.1 相对 PLAN-WSC-3.0 吸收 UX/SA/QA/PE/PP/SEC Round 1 ISSUE 意图，
  仍为 SNAP-WSC-003 前端 UX-only 增量：contractsBaseline 保持 wsc-contracts@2.0.0
  只读继承；Out of Scope / §1.3 / §2.3 / 全任务 denyModify 均禁止改 contracts/**、
  OpenAPI、frontend/src/api/**、backend/**；§3.4/§3.5 为既有导入字段与错误码的
  前端展示约束，非新开契约写。R1 ABSTAIN 适用性未改变。本角色无新契约/数据模型/
  迁移决策可审，再次 ABSTAIN。不裁定 @2.0.0 契约对错，亦不要求本计划改 OpenAPI。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-3.1.md`；R1：`REV-PLAN-WSC3-R1-api-data-designer.md`（`ABSTAIN`）；`contractsBaseline: wsc-contracts@2.0.0` |
| 聚焦 | 3.1 修订后是否仍为 UX-only；是否误开契约/OpenAPI/Schema 写；ABSTAIN 适用性是否延续 |
| 不裁定 | `@2.0.0` OpenAPI/错误码/RBAC/Schema 本身是否完备；页面视觉/文案；威胁建模；他角色 ISSUE 的 closeWhen |

## 相对 R1：有无新契约决策

| 检查项 | PLAN-WSC-3.1 证据 | 判定 |
|---|---|---|
| 谱系仍为 UX 增量 | `basedOn`/`lineageFrom: PLAN-WSC-3.0`；`sourcePrd: product/prd/wsc-v1.2-ux.md`；In Scope 为令牌/壳层/页面呈现/走查/E2E | 无新 API/数据范围 |
| 契约基线未改 | front-matter `contractsBaseline: wsc-contracts@2.0.0`；§0.1「行为与 API **只读继承**；本 Run 默认不改」 | 未升级/改写契约版本 |
| §0 ISSUE 映射 | 17 条均为 SA/QA/PP/PE/UX/SEC 的控件基座、证据写权、并行约束、载体 B、权限/PII 展示等；**无**契约/OpenAPI/迁移类 ISSUE | 本角色 R1 无 ISSUE 待 closeWhen |
| §3.4 导入四态 | 内联 `successCount`/`failureCount`/文件级拒绝与 `ERR_IMPORT_FILE_TOO_LARGE` 等映射；声明「后端/契约仍 denyModify」语境下的 **UI 最低要求** | 消费既有契约字段/错误码，非新开 OpenAPI |
| §3.5 展示白名单 | 允许行号/产品编码/原因码/计数/`reportId`；禁止信用代码/ownerDID/明文哈希回显 | 前端展示约束；明确「本计划不改 API」 |
| 任务写集 | 201..206 `writeSet` 均为 `frontend/**`（及 UX 证据/E2E）；无一写入 `contracts/**` | 未误开契约写任务 |

**结论**：3.1 相对 3.0 **未**引入须本角色冻结或裁定的新契约/数据模型/迁移决策；亦 **未** 误开契约写。

## 核对：是否错误要求改契约

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| Out of Scope | §1.1 deny：`contracts/**`、OpenAPI、RBAC 矩阵、后端写路径、Flyway/SQL、领域模型 | 通过 — 明确不改 OpenAPI/契约 |
| 硬门禁 | §1.3：`wsc-contracts@2.0.0` **只读**；`frontend/src/api/**` 只读；backend 全任务 `denyModify`；禁止静默改契约版本 | 通过 |
| 写所有权 | §2.3：`contracts/**`、`frontend/src/api/**`、`backend/**` → **无写任务** / 全任务只读 deny | 通过 |
| 任务 denyModify | TASK-WSC-201..206 均含 `contracts/**`；`frontend/src/api/**`；`backend/**`（206 另禁新增业务 API 调用/改 RBAC） | 通过 |
| 扩 scope 闸门 | §1.1：须 PO + 新快照/DEC；默认不可调度；§8 门禁要求 diff 不含 `contracts/**`/`backend/**` | 通过 — 未把契约变更塞进本计划任务 |

**结论（计划级）**：本计划 **未** 错误要求修改 OpenAPI 或 `contracts/**`；契约结论仍冻结在既有 `wsc-contracts@2.0.0`（本角色不重开该基线对错）。故 **不适用** `REQUEST_CHANGES`。

## 核对：前端仅消费既有 API

| 任务 | 证据 | 判定 |
|---|---|---|
| 201 | `readSet`：`frontend/src/api/**`（只读）；`denyModify`：`contracts/**` / `api/**` / `backend/**` | 通过 |
| 202 | objective：「数据契约与图表库默认不改」；`readSet`：`frontend/src/api/overview.ts`（只读）；deny 同上 | 通过 |
| 203–205 | 行为/写路径继承 SNAP-WSC-002 REQ；204 引用 §3.4/§3.5 为展示层验收；`denyModify` 含契约与 api | 通过 — 无新端点/无改 client |
| 206 | deny：「禁止新增业务 API 调用或改 RBAC 逻辑」；仅 E2E/证据路径可写 | 通过 |

仓库侧只读确认：`contracts/openapi/openapi.yaml` info.version `2.0.0` 与计划 `contractsBaseline` 一致；本评审 **不** 对路径字段或错误码提出变更要求。

## ABSTAIN 适用性

| 条件 | 是否满足 |
|---|---|
| 计划类型为 UX-only / 视觉增量，不新开 API、数据模型、迁移 | 是 — 与 R1 相同；3.1 修订未扩大功能/契约 In Scope |
| 无本角色须冻结或裁定的新契约/Schema 决策 | 是 — 全任务无 `contracts/**` 写集；无 OpenAPI 任务；§3.4/§3.5 非契约开写 |
| 计划已写清契约只读 + 前端只消费既有 API，无本领域未决冲突待本角色关闭 | 是 — 见上两节 |
| 本角色 R1 无待关闭 ISSUE | 是 — R1 异议表为空；无需 closeWhen 核验 |
| ABSTAIN ≠ 默认批准功能/UX/发布 | 是 — 仅声明 API/数据设计领域对本候选计划不适用；他角色门禁仍须独立决策 |

`abstainReason`: PLAN-WSC-3.1 仍为 SNAP-WSC-003 前端 UX-only 增量（相对 PLAN-WSC-3.0 的 ISSUE 吸收修订）。API/数据设计权威为只读继承的 `wsc-contracts@2.0.0`，无新契约、数据模型或迁移工作落入本角色负责范围。计划继续将 OpenAPI/`contracts/**`/`frontend/src/api/**`/`backend/**` 置于 Out of Scope 与全任务 `denyModify`，§3.4/§3.5 仅为既有导入契约上的前端展示验收，未误开契约写。故 `apiDataDesigner` 对 PLAN-WSC-3.1 Round 2 **再次 ABSTAIN**（适用性与 R1 一致）。若后续 PO 批准扩 scope 改契约，须新快照/DEC 后再调度本角色实质评审（届时不得沿用本次 ABSTAIN）。

## 异议

（无。本领域不适用；不提出契约变更类 ISSUE，避免越权改既有 `@2.0.0` 结论。本角色 R1 无 ISSUE 需在本轮 closeWhen 确认。）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |
