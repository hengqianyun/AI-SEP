# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC8-R2-apiDataDesigner
planId: PLAN-WSC-8.2
round: 2
role: apiDataDesigner
actorInstance: api-data-designer-wsc-011-r2
snapshotIdAtReview: SNAP-WSC-008
contractsBaselineAtReview: wsc-contracts@2.3.2
contractsTargetAtReview: wsc-contracts@2.3.2
decision: APPROVE
summary: |
  PLAN-WSC-8.2 §3.1 已完整吸收 Round 1 本角色全部 6 条 ISSUE 的 closeWhen 意图：
  maintenance scope 硬冻结于 GET /catalog/maintenance/entries（full|myCatalog）并自 listProducts 解绑；
  SessionPrincipal/AdminUser 必填 enterpriseId+enterpriseName；mine=true 本企业判定写死 §3.2 join 规则；
  RBAC 矩阵字面表含 myCatalogUI/myCatalogApi 与 ADMIN 产品写 200；浏览/l2-distribution 统一
  l1CategoryId/l2CategoryId；903 acceptance 四头版本同步对账清单齐备。预落地契约仍漂移
  （VERSION=2.3.3、OpenAPI info=2.2.0、mine=create_by、matrix V1.5 语义），属 903 实施闭合范围，
  不阻塞计划级 APPROVE。decision=APPROVE；closedIssueCount=6；openIssueCount=0。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-8.2.md` §3.1 / §3.2 / TASK-WSC-903 acceptance；R1 `REV-PLAN-WSC8-R1-api-data-designer.md` |
| 只读对照 | `contracts/VERSION`（2.3.3）、`contracts/rbac/matrix.yaml`（2.3.2 / V1.5 语义）、`contracts/openapi/openapi.yaml`（info 2.2.0）、`SessionPrincipal.java`（仅 enterpriseName） |
| 聚焦 | R1 ISSUE-API-WSC8-R1-001..006 在 §3.1 的闭合验证；903 契约冻结可实施性 |
| 不裁定 | 页面交互/文案；威胁建模；他角色 ISSUE；DAG/写集（除非直接破坏契约） |
| 禁区 | 不代批他角色；不写 `ai/runs/**`；不改 `contracts/**` / plan / state / events；不 commit |

## R1 ISSUE 闭合核对

### ISSUE-API-WSC8-R1-001（P0）— listProducts 误绑 myCatalog — **已闭合**

| closeWhen 要求 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| 删除 listProducts 上 myCatalog/scope 叙述 | §3.1：`GET /catalog/products` 明示 **无** `scope=myCatalog`（myCatalog 走 maintenance API） | **满足** |
| maintenance entries 硬冻结 `scope=full\|myCatalog` | §3.1：`GET /catalog/maintenance/entries` query enum；`full`=ADMIN 全平台、`myCatalog`=ADMIN 本企业 / PROVIDER ownCreateBy；PROVIDER `full`→403；USER→403 | **满足** |
| batch/put 等同 scope 规则 | §3.1 末行「batch/put 等同 scope 规则」；§905/907 acceptance 含 scope-aware 拦截与双入口分离 | **满足** |
| 907 maintenance BE 无条件列入 | §5 TASK-WSC-907 writeSet 含 `CatalogMaintenanceController/Service` | **满足** |

### ISSUE-API-WSC8-R1-002（P1）— 会话/AdminUser enterprise 字段 — **已闭合**

| closeWhen 要求 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| SessionPrincipal + OpenAPI session **必填** enterpriseId + enterpriseName | §3.1 用户/会话行；§3.2「会话 principal **单一真源**为 enterpriseId+enterpriseName」 | **满足** |
| AdminUser Create/Update **含** enterpriseId | §3.1；TASK-WSC-904 writeSet 含 AdminUser* model | **满足** |
| 禁止「或等价」模糊表述 | §3.1 字面写死字段名；§1.2「scope **仅**从 SessionPrincipal.enterpriseId 推导」 | **满足** |

> 预落地 OpenAPI `Session` 仍仅 `enterpriseName`、无 `enterpriseId` — 属 **903/904 实施差**，非计划缺口。

### ISSUE-API-WSC8-R1-003（P1）— mine 本企业规则 — **已闭合**

| closeWhen 要求 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| mine description 硬修订为本企业 | §3.1：`mine=true` → **本企业**产品；**不得**再写 create_by-only | **满足** |
| 写死权威判定规则 | §3.2：`create_by` → `sys_user.enterprise_id` 与会话 `enterpriseId` 比较；同 enterpriseId 即本企业；产品表**不新增** enterprise_id 列 | **满足** |
| 905 testScope 同企异 create_by / 跨企负例 | §5 TASK-WSC-905 testScope 含「同企异 create_by 用户可见、跨企业不可见」 | **满足** |

### ISSUE-API-WSC8-R1-004（P1）— RBAC 矩阵字面表 — **已闭合**

| closeWhen 要求 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| matrix 字面表（myCatalogUI/myCatalogApi、catalogMaintenance、productWrite） | §3.1 **RBAC 矩阵字面表** 9 行能力键 × 三角色 | **满足** |
| PROVIDER catalogMaintenance hidden + API 403 | 表：`catalogMaintenanceUI` PROVIDER **hidden**；`catalogMaintenanceApi` PROVIDER **403** | **满足** |
| ADMIN productWrite visible + 200 ownEnterprise | 表：`productWriteUI/ImportUI` ADMIN **visible**；`productWriteApi/ImportApi` ADMIN **200 ownEnterprise** | **满足** |
| 903 acceptance 逐 cell 一致 + state-matrix | TASK-WSC-903 acceptance：matrix 与 §3.1 逐 cell + state-matrix 侧栏/myCatalog/ADMIN 我的产品 | **满足** |

> 预落地 `matrix.yaml` 仍为 V1.5（PROVIDER maintenance visible、ADMIN 写 403、无 myCatalog 行）— **903 须按 §3.1 纠偏**，计划已冻结目标态。

### ISSUE-API-WSC8-R1-005（P1）— l1/l2 参数命名 — **已闭合**

| closeWhen 要求 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| 保留 l1CategoryId/l2CategoryId（推荐路径） | §3.1 listProducts 与 l2-distribution 均写 **`l1CategoryId`**；§1.2 / TASK-901/902 acceptance 一致 | **满足** |
| 901/902 testScope 一字对齐 | TASK-901：Cascader 传 `l1CategoryId`/`l2CategoryId`；TASK-902：`?l1CategoryId=` | **满足** |

### ISSUE-API-WSC8-R1-006（P2）— 四头版本同步 — **已闭合**

| closeWhen 要求 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| VERSION / matrix / OpenAPI info / state-matrix 同一 semver | §3.1 VERSION 行：保持 2.3.2 或合并至 2.3.3 且不再升；TASK-WSC-903 acceptance **版本四头同步** | **满足** |
| 903 testScope 对账预落地漂移 | TASK-WSC-903 testScope：「VERSION 2.3.3 / matrix 2.3.2 / OpenAPI 2.2.0 漂移须闭合」 | **满足** |

## 增量核对（§3.1 整体）

| 检查项 | 计划 §3.1 | 只读现状 | 判定 |
|---|---|---|---|
| 全链 list 无企业 filter | REQ-CAT-018；安全说明拒绝客户端 enterprise 参数 | OpenAPI 无 enterprise query；BE 无 filter | **计划冻结正确**；903 补 description |
| maintenance scope 与双入口 | scope=full \| myCatalog 硬 enum | maintenance GET 无 scope 参数；描述 ADMIN 全量 / PROVIDER create_by | **计划正确**；903 增量 |
| l2-distribution L1 过滤 | 可选 `l1CategoryId`；无企业 filter | OpenAPI 已有 `l1CategoryId` on listProducts；l2-distribution path 待 902/903 | **通过** |
| 903 独占 contracts/api | §0.3 / TASK-903 writeSet | — | **通过** |
| industryCategory 浏览弃用 | REQ-CAT-019；901 不传 | OpenAPI browse 仍列 industryCategory query | **903 实施**（browse description 标注弃用/保留 schema 供 editor）；不阻塞 APPROVE |

## 无异议项（记录）

- §3.2 本企业 join 规则与「产品表不新增 enterprise_id」降低迁移面，与 904 Flyway 独占分层一致。
- §3.1 安全说明（scope 仅信 SessionPrincipal；拒绝客户端 enterpriseId）与 905/SEC ISSUE 对齐，契约叙述完备。
- supplierName 检索 vs 会话 enterpriseName — 计划未引入 enterpriseName 作产品 filter，与 PLAN-WSC-6.2 一致。
- blocking OQ = 无；企业实体细节落 904，903 已先冻结 API 可见字段与 scope 语义。

## 实施提示（非阻塞；供 TASK-WSC-903 developer）

1. OpenAPI maintenance **三路**（GET list / PUT `{productId}` / POST batch）建议均显式 `scope` query enum 或 operation 级 description 引用同一 enum，避免 FE 仅 list 带 scope 而写路径语义分叉。
2. `matrix.yaml` 新增 `on: ownEnterprise` 时须与 §3.1 字面表及 905 `RbacMatrixTest` 同源；若沿用既有 `on` 枚举，903 acceptance 勾选映射表。
3. browse query `industryCategory` 建议在 OpenAPI 标注「browse 面弃用；editor/import 仍用 schema 字段」，与 REQ-CAT-019 一致。

## ISSUE 表

| id | severity | status | evidence | closeWhen（R1） | R2 判定 |
|---|---|---|---|---|---|
| ISSUE-API-WSC8-R1-001 | P0 | **CLOSED** | §3.1 maintenance scope 硬冻结；listProducts 无 myCatalog | 见 R1 REV | **已吸收** |
| ISSUE-API-WSC8-R1-002 | P1 | **CLOSED** | §3.1 enterpriseId+enterpriseName 必填 | 见 R1 REV | **已吸收** |
| ISSUE-API-WSC8-R1-003 | P1 | **CLOSED** | §3.1 mine 本企业 + §3.2 join 规则 | 见 R1 REV | **已吸收** |
| ISSUE-API-WSC8-R1-004 | P1 | **CLOSED** | §3.1 RBAC 矩阵字面表 | 见 R1 REV | **已吸收** |
| ISSUE-API-WSC8-R1-005 | P1 | **CLOSED** | §3.1 l1CategoryId/l2CategoryId | 见 R1 REV | **已吸收** |
| ISSUE-API-WSC8-R1-006 | P2 | **CLOSED** | §3.1 VERSION + 903 四头同步 acceptance | 见 R1 REV | **已吸收** |

## 决策

`decision: APPROVE`

- **openIssueCount**: 0
- **closedIssueCount**: 6（R1 本角色 ISSUE 全部闭合）
- **newIssueCount**: 0

本评审 **仅**输出 apiDataDesigner 视角结论；**未**修改 `PLAN-WSC-8.2`、`contracts/**`、Run `state`/`events`；**未**代关他角色 ISSUE；**未** commit。

**批准含义（本角色）**：§3.1 Wave B 契约增量冻结内容可交 TASK-WSC-903 实施；预落地契约漂移须在 903 VERIFIED 前按 acceptance 闭合，不属于计划修订阻塞项。
