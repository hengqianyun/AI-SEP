# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC5-R2-apiDataDesigner
planId: PLAN-WSC-5.2
round: 2
role: apiDataDesigner
actorInstance: api-data-designer-wsc-008-r2
snapshotIdAtReview: SNAP-WSC-005
contractsBaselineAtReview: wsc-contracts@2.1.0
contractsTargetAtReview: wsc-contracts@2.2.0
decision: APPROVE
closedIssues:
  - ISSUE-API-WSC5-R1-001
  - ISSUE-API-WSC5-R1-002
  - ISSUE-API-WSC5-R1-003
openIssues: []
summary: |
  PLAN-WSC-5.2 已吸收 R1 本角色三条 ISSUE 的 closeWhen：§3.1 将切角色禁用、
  AdminUser 命名 schema/软删/禁出 password·hash/冲突与 404 码、OpenAPI/state-matrix
  叙述对齐均改为硬冻结（禁「建议/若有」）；TASK-WSC-601 acceptance/testScope 可测点齐。
  601 独占 contracts+api、mine/l2-distribution/matrix 主线仍可调度。无新增契约异议。
  关闭 ISSUE-API-WSC5-R1-001..003；decision=APPROVE。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-5.2.md`；R1 `REV-PLAN-WSC5-R1-api-data-designer.md` + `SUMMARY.md`；`snapshotIdAtReview: SNAP-WSC-005` |
| 聚焦 | 核验 ISSUE-API-WSC5-R1-001..003 相对 PLAN-WSC-5.2 的 closeWhen；2.2.0 硬冻结与 601 可测验收 |
| 不裁定 | 页面交互；威胁建模；他角色 ISSUE；DAG/写集工程细节（除非破坏契约所有权） |
| 禁区 | 不代批他角色；不写 `ai/runs/**`；不改 `contracts/**`（只评计划） |

## R1 ISSUE closeWhen 核验

### ISSUE-API-WSC5-R1-001（P1）— 切角色硬冻结 — **满足，关闭**

| closeWhen 要点 | PLAN-WSC-5.2 证据 | 判定 |
|---|---|---|
| 禁止「建议」；OpenAPI 必须收录 `POST /auth/session/role` 为始终禁用 | §3.1 标题「硬冻结，禁止『建议/若有』」；**会话切角色（硬）**行：必须收录、始终禁用 | **满足** |
| 固定 HTTP + 稳定码 + example | 同表：HTTP **410**；`code`=`ERR_ROLE_SWITCH_DISABLED`；example 齐全 | **满足** |
| codes.yaml + `ApiResponseError.code` enum | 同表：同码写入 codes.yaml 且进入 enum | **满足** |
| `LoginRequest` 不得再接受客户端选角色 | 同表：**删除** `role`；description 声明角色仅来自 `sys_user` | **满足** |
| 601 acceptance/testScope 可测 | acceptance：410+码、codes/enum 硬同步、`LoginRequest` **无** role；testScope 可勾选同项 | **满足** |

### ISSUE-API-WSC5-R1-002（P1）— AdminUser schema — **满足，关闭**

| closeWhen 要点 | PLAN-WSC-5.2 证据 | 判定 |
|---|---|---|
| 命名响应 schema 必填字段 | §3.1 **用户管理（硬）**：`AdminUser` 必填 userId/username/displayName/role/enterpriseName/deleted（及必要审计字段） | **满足** |
| Create/Update 请求形状 | Create：username+password+role（+可选 displayName）；Update：改角色/资料/软删 | **满足** |
| 软删权威形状二选一写死 | **PUT `deleted: true`**；本版不采用 DELETE | **满足** |
| 响应禁出 password/hash | list/get/create/update **响应禁止** `password` / `passwordHash` | **满足** |
| 错误码硬同步 | 非 ADMIN→`ERR_FORBIDDEN`；冲突→`ERR_USER_USERNAME_CONFLICT`(409)；不存在→`ERR_USER_NOT_FOUND`(404) | **满足** |
| 601 acceptance：路径+命名 schema+码，非仅路径存在 | acceptance：命名 schema 非裸 object；用户错误码硬同步；soft-delete=PUT deleted；禁 password/hash | **满足** |

### ISSUE-API-WSC5-R1-003（P2）— 叙述漂移 — **满足，关闭**

| closeWhen 要点 | PLAN-WSC-5.2 证据 | 判定 |
|---|---|---|
| 废止 ADMIN 可产品写/导入 OpenAPI 叙述 | §3.1 **叙述对齐（硬）** + 601 acceptance「OpenAPI 叙述无『ADMIN 可产品写/导入』」 | **满足** |
| state-matrix：版本头 2.2.0、侧栏/只读/我的产品写入口、导入宿主 | 同表：版本头=`2.2.0`；侧栏可见性/公共目录只读/我的产品写入口/角色只读；**导入宿主=我的数据产品→关闭/成功回我的产品**；601 acceptance 同口径 | **满足** |
| 删除「若有」软措辞；用户码按 002 硬同步 | §3.1 禁「建议/若有」；用户码在 002 行硬列；无「若有」残留于契约冻结表 | **满足** |

## 对照核对（计划级，无新增异议）

| 检查项 | 判定 |
|---|---|
| `contractsTarget` 2.2.0；已是则对账禁再 bump | **通过** |
| 601 独占 `contracts/**` + `frontend/src/api/**`；602..605 denyModify | **通过** |
| `mine=true` → `create_by=当前用户`；601 冻 / 603 实现 | **通过** |
| `l2-distribution`：`totalProducts` + `items[]`；真实总数归 604 | **通过** |
| §4 / matrix 三角色与产品写仅 PROVIDER 声称一致（计划级） | **通过** |
| 2.1.0 导入四态 / typeSpecific / 报告白名单不回退 | **通过** |
| SNAP 仍 `IN_REVIEW`：编排门禁，不单开契约 ISSUE | **记录** |

## 异议

本轮 **无新增** ISSUE。

| id | severity | evidence | closeWhen | relatedReqs | status |
|---|---|---|---|---|---|
| ISSUE-API-WSC5-R1-001 | P1 | （见 R1 REV） | （见上表核验） | REQ-SHELL-001, REQ-RBAC-001, REQ-USER-001 | **CLOSED** |
| ISSUE-API-WSC5-R1-002 | P1 | （见 R1 REV） | （见上表核验） | REQ-USER-001 | **CLOSED** |
| ISSUE-API-WSC5-R1-003 | P2 | （见 R1 REV） | （见上表核验） | REQ-RBAC-001, REQ-CAT-010, REQ-SHELL-001 | **CLOSED** |

## 决策

`decision: APPROVE`

- **closedIssueCount**: 3（ISSUE-API-WSC5-R1-001、002、003）
- **openIssueCount**: 0

本评审 **仅**关闭本角色提出的上述三条 ISSUE；**未**修改计划正文、源码、`contracts/**` 或 Run `state`/`events`；**未**代关他角色 ISSUE。
