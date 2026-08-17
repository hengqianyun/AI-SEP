# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC8-R1-apiDataDesigner
planId: PLAN-WSC-8.1
round: 1
role: apiDataDesigner
actorInstance: api-data-designer-wsc-011-r1
snapshotIdAtReview: SNAP-WSC-008
contractsBaselineAtReview: wsc-contracts@2.3.2
contractsTargetAtReview: wsc-contracts@2.3.2
decision: REQUEST_CHANGES
summary: |
  PLAN-WSC-8.1 对 wsc-contracts@2.3.2 Wave B 增量、TASK-WSC-903 独占契约写集、
  mine 本企业 / 全链无企业过滤 / ADMIN 产品写纠偏的主线方向与 SNAP-WSC-008 一致，
  903→904→905 串行亦合理。阻塞 APPROVE：① §3.1 将「我的目录」scope 误绑至
  GET /catalog/products，与 REQ-CAT-017 维护 UX / 907 maintenance API 分叉；
  ② scope / mine 语义、本企业解析规则、myCatalog vs catalogMaintenance 矩阵行、
  l2-distribution 与浏览 query 参数命名仍未硬冻结；③ 预落地契约漂移
  （VERSION=2.3.3、matrix=2.3.2、OpenAPI info=2.2.0、mine=create_by、ADMIN 写 403）
  须在 903 acceptance 写死对账口径。开放 6 条 ISSUE；decision=REQUEST_CHANGES。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-8.1.md` §3 / §4 / TASK-WSC-903..905；`product/requirements/SNAP-WSC-008.md` |
| 只读对照 | `contracts/VERSION`、`contracts/rbac/matrix.yaml`、`contracts/openapi/openapi.yaml`（摘要）、`contracts/ui/state-matrix.md`、`contracts/req-coverage.md`；预落地 `CatalogBrowseController`、`L2DistributionController`、`SessionPrincipal`、`RbacMatrix` |
| 聚焦 | 903 契约 2.3.2 增量；`listProducts` 全链 vs mine 本企业；`l2-distribution` `l1` 参数；用户 enterprise 字段；my-catalog vs catalog-maintenance RBAC 矩阵；OpenAPI / matrix / state-matrix 对齐 |
| 不裁定 | 页面交互/文案；威胁建模；他角色 ISSUE；DAG/写集工程细节（除非直接破坏契约冻结） |
| 禁区 | 不代批他角色；不写 `ai/runs/**`；不改 `contracts/**` / 计划 / state / events；不 commit |

## 对照核对

### 1. 契约版本与 903 独占所有权 — **部分通过**

| 检查项 | 计划证据 | 只读现状 | 判定 |
|---|---|---|---|
| `contractsTarget` | YAML / §3：`wsc-contracts@2.3.2` Wave B 增量 | `contracts/VERSION` = **2.3.3**；`matrix.yaml` header = **2.3.2**；OpenAPI `info.version` = **2.2.0** | 计划 §0.2/§3.1 已对账意图正确；**903 验收须写死三处版本头同步**（见 ISSUE-006） |
| 903 独占写集 | §0.3 / TASK-903 writeSet；禁止他任务 bump | — | **通过** |
| 禁止静默再 bump | §3.1「若树中更高：对账合并…禁止无委员会批准再 bump」 | VERSION 已 2.3.3 | 方向通过；对账清单未列齐（ISSUE-006） |

### 2. `GET /catalog/products` — listProducts scope — **未通过（计划级）**

| 检查项 | SNAP / 计划 | 预落地 OpenAPI + BE | 判定 |
|---|---|---|---|
| 全链（默认） | REQ-CAT-018：全平台；**无**登录企业 filter | 无 enterprise query；实现无企业 filter | **意图一致** |
| `mine=true` | REQ-CAT-016：**本企业**（非仅 create_by） | OpenAPI：`create_by=当前用户`；Controller：`mine` → `ownerUserId` | **语义冲突** — 须 903 修订 + 写清解析规则（ISSUE-003） |
| 「我的目录」scope | §3.1 写在 `listProducts` 上：`scope=myCatalog`… | 我的目录 UX 走 `/catalog/maintenance/**`（907）；browse `listProducts` 无 scope | **端点绑错**（ISSUE-001） |
| ADMIN 产品写 | REQ-RBAC-002：ADMIN mine 写 **200**（本企业） | OpenAPI POST/PUT：`仅 PROVIDER`；matrix：ADMIN `productWriteApi` **403** | **须 903 矩阵 + OpenAPI 同步**（ISSUE-004） |
| 浏览 industryCategory | REQ-CAT-019：浏览**不传** | OpenAPI 仍列 `industryCategory` query | 903 须标注 browse 弃用 / 保留 schema 供 editor（可在 903 acceptance 吸收，不单开 ISSUE） |

### 3. `GET /catalog/l2-distribution` — **未通过（命名）**

| 检查项 | 计划证据 | 只读现状 | 判定 |
|---|---|---|---|
| L1 过滤 | §3.1 / 902 / REQ-CAT-015：可选 **`l1`**；无企业 filter | OpenAPI **无** query 参数；Controller 无 `@RequestParam` | 增量方向正确 |
| 参数命名 | 902 testScope：`?l1=`；901 acceptance：`传 l1` / `传 l2` | 既有 browse 用 **`l1CategoryId` / `l2CategoryId`**（OpenAPI + BE） | **命名未冻结**（ISSUE-005） |
| 全链口径 | REQ-CAT-018 / 902 负例：不按用户企业过滤 | 当前无企业 filter | **通过（现状）** |

### 4. 用户 / 会话 enterprise 字段 — **部分通过**

| 检查项 | 计划 / SNAP | 只读现状 | 判定 |
|---|---|---|---|
| 用户归属企业 | REQ-USER-002；§3.1：id/name；904：`enterpriseId` FK | `SysUserEntity` 仅 **`enterprise_name`** 字符串；OpenAPI `AdminUser` / `SessionPrincipal` 仅 **`enterpriseName`** | OQ-V16-001 非阻塞，但 **903 须冻结会话 + AdminUser 最小形状**（ISSUE-002） |
| 一企多 ADMIN | SNAP / 904 acceptance | 行为已锁；schema 未体现 enterpriseId | 依赖 ISSUE-002 + 904 迁移 |
| 本企业产品解析 | mine / myCatalog 均需 enterprise | **计划未写** product 是否新增 `enterprise_id` 或 **create_by → user.enterprise** join 规则 | **缺口**（ISSUE-003） |

### 5. RBAC 矩阵 — my-catalog vs catalog-maintenance — **未通过**

| 能力 | SNAP-WSC-008 §角色表 | 计划 §3.1 / §4 | `matrix.yaml` @2.3.2 | 判定 |
|---|---|---|---|---|
| 目录维护 UI/API | **仅 ADMIN**；全量 | §4 一致 | PROVIDER **visible** + API **ownCreateBy 200** | **须 V1.6 纠偏**（ISSUE-004） |
| 我的目录 UI/API | ADMIN+PROVIDER；ADMIN 本企业 / PROVIDER create_by | §4 有叙述；§3.1 matrix 行含 `myCatalogUI` | **无** `myCatalogUI` / `myCatalogApi` 行 | **未冻结**（ISSUE-004） |
| 我的数据产品 UI | ADMIN+PROVIDER | §4 一致 | `myProductsUI` ADMIN+PROVIDER visible | **通过** |
| ADMIN 产品写/导入 | 本企业 **200** | §3.1 要求 visible + 200 | ADMIN **hidden + 403** | **须修订**（ISSUE-004） |
| PROVIDER 目录维护 API | **403** | §3.1 明示 | **200 ownCreateBy** | **须修订**（ISSUE-004） |

### 6. OpenAPI / state-matrix / req-coverage 对齐 — **未通过**

| 制品 | 计划 903 acceptance | 只读现状 | 判定 |
|---|---|---|---|
| OpenAPI 叙述 | enterprise、mine/scope、全链无企业 filter、`l2-distribution?l1` | info 仍 V1.4 叙述；mine=create_by；维护 PROVIDER 200 | **漂移** — 903 任务正确，**计划 §3.1 须补硬冻结表**（ISSUE-001..005） |
| `state-matrix.md` | V1.6 导航 / myCatalog / ADMIN 双入口 / 企业展示 | 头 **2.2.0**；侧栏无「我的目录」；ADMIN 我的产品 **隐藏** | 903 负责；计划须列 **必改侧栏行**（ISSUE-004） |
| `req-coverage.md` | SNAP-008 十条映射 | 无 V1.6 REQ 行 | 903 acceptance 已点；**通过（任务级）** |

## 无异议项（记录）

- TASK-WSC-903 **唯一**拥有 `contracts/**` + `frontend/src/api/**`；902 可先 BE、903 对账 OpenAPI — 调度合理。
- 全链列表 / 座序图 **不得**按登录用户企业过滤 — 与 SNAP REQ-CAT-018 一致；902/905 负例方向正确。
- `supplierName` 检索 vs 会话 `enterpriseName` — PLAN-WSC-6.2 已钉；本计划未引入 enterpriseName 作产品 filter，**不新开 ISSUE**。
- 904 独占 Flyway、905 默认 denyModify `**/sql/**` — 数据/契约分层正确。
- blocking OQ = 无 — 企业实体细节可落 904，但 **903 须先冻结 API 可见字段与 scope 语义**。

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-WSC8-R1-001 | **P0** | §3.1 冻结表将「我的目录」写成 `GET /catalog/products` 上「可选 `scope=myCatalog`…」；SNAP REQ-CAT-017 / TASK-WSC-907 明确复用 **目录维护 UX** 与 `/catalog/maintenance/**`（或等价 maintenance 模块），ADMIN 本企业 / PROVIDER create_by 与 **全量目录维护** 为**两入口、两 scope**。将 myCatalog 绑到 browse listProducts 会使 905/907 对同一能力双轨（list vs maintenance），OpenAPI 与 E2E 无法 mechanical 验收。 | §3.1 **删除** listProducts 上的 myCatalog/scope 叙述；**改硬冻结**：`/catalog/maintenance/entries`（及 put/batch **若适用**）增加 **`scope` 或等价 query**（建议字面值写死，如 `scope=full` \| `scope=myCatalog`）：`full` = ADMIN 全量目录维护；`myCatalog` = ADMIN **本企业**、PROVIDER **ownCreateBy**；PROVIDER 调 `full` → 403。TASK-WSC-903 acceptance/testScope 交叉 OpenAPI path + matrix `myCatalogApi`；907 writeSet **无条件**列入 maintenance controller/service（与 planEditor ISSUE-PE-WSC8-R1-003 对齐）。 | REQ-CAT-017, REQ-RBAC-002, REQ-CAT-013 |
| ISSUE-API-WSC8-R1-002 | P1 | §3.1 用户/会话行写「enterprise 归属（id/name **或等价**）」；904 objective 为 `enterpriseId` FK，但 903 **未**要求 OpenAPI / `SessionPrincipal` schema 最小字段集。预落地仅有 `enterpriseName` 字符串，无 `enterpriseId`；905/907 scope 若仅按 name 匹配存在重名/迁移风险。 | §3.1 **硬冻结**（禁「或等价」）：`SessionPrincipal` + OpenAPI `SessionUser`（或现有 session schema）**必填** `enterpriseId` + `enterpriseName`；`AdminUser` / Create / Update **含** `enterpriseId`（create 可默认继承操作者企业）；903 acceptance：OpenAPI + 生成 client typecheck；904 实现须与契约 id 对齐。若 V1.6 暂仅 name，须在 §3.1 **显式写死**「本版 scope 仅按 enterpriseId；name 仅展示」并关闭 name 匹配歧义。 | REQ-USER-002, REQ-RBAC-002 |
| ISSUE-API-WSC8-R1-003 | P1 | REQ-CAT-016 / SNAP `mineScope: enterprise` 要求 `mine=true` → **本企业**产品；预落地 OpenAPI 与 Controller 仍为 **create_by=当前用户**。计划 §3.1 未写 **本企业判定规则**（product.`enterprise_id` vs `create_by`→`sys_user.enterprise_id` join），905/908 无法写稳定集成测。 | §3.1 + TASK-WSC-903 OpenAPI **`mine` query description 硬修订**为「本企业产品列表」；**写死一种权威规则**（推荐：`create_by` 关联用户的企业 id，或 product 冗余 `enterprise_id` 于创建时写入）；903 acceptance 含 description + example；905 testScope 含「同企业异 create_by 用户可见、跨企业不可见」；**不得**再写 create_by-only。 | REQ-CAT-016, REQ-RBAC-002, REQ-USER-002 |
| ISSUE-API-WSC8-R1-004 | P1 | §3.1 RBAC 矩阵（硬）与 §4 声称 V1.6 导航/scope，但 `matrix.yaml` 仍为 V1.5：PROVIDER `catalogMaintenanceUI` visible + API 200 ownCreateBy；ADMIN `productWrite*` hidden+403；**无** `myCatalogUI`/`myCatalogApi`。OpenAPI 维护/产品写叙述与 SNAP ADMIN 本企业可写、PROVIDER 无目录维护 **不一致**。 | §3.1 增 **matrix 字面表**（禁散文）：`catalogMaintenanceUI` ADMIN visible / PROVIDER **hidden** / USER hidden；`catalogMaintenanceApi` ADMIN **200 on full** / PROVIDER **403** / USER 403；**新增** `myCatalogUI` ADMIN+PROVIDER visible / USER hidden；`myCatalogApi` ADMIN **200 on ownEnterprise** / PROVIDER **200 on ownCreateBy** / USER 403；`productWriteUI/ImportUI` ADMIN **visible** / PROVIDER visible / USER hidden；`productWriteApi/ImportApi` ADMIN **200 on ownEnterprise** / PROVIDER 200 ownCreateBy / USER 403。903 acceptance：**matrix.yaml 与 §3.1 逐 cell 一致** + OpenAPI POST/PUT/import description 同步；`state-matrix.md` 侧栏增「我的目录」、ADMIN 我的产品 **可见**、PROVIDER **无**目录维护。905 `RbacMatrixTest` 引用同一表。 | REQ-RBAC-002, REQ-SHELL-009, REQ-CAT-016, REQ-CAT-017 |
| ISSUE-API-WSC8-R1-005 | P1 | 计划 / PRD / 901 acceptance 多处写 query **`l1` / `l2`**；902 写 `GET /catalog/l2-distribution?l1=`；既有冻结契约为 **`l1CategoryId` / `l2CategoryId`**（OpenAPI + `CatalogBrowseController` + 前端 maintenance composable）。903 若不对账，901/902/ client 生成将与 BE 分叉。 | §3.1 **二选一并写死**：（a）**保留** `l1CategoryId`/`l2CategoryId`（推荐，向后兼容），901/902 acceptance 改字面；`l2-distribution` 增 **`l1CategoryId`** optional query；或（b）OpenAPI 正式 alias `l1`/`l2` 与 categoryId **同义** 且 BE 双收。903 acceptance：OpenAPI parameters 名与 901/902 testScope **一字一致**；禁止 901 发 `l1` 而 OpenAPI 仅 `l1CategoryId` 无 alias。 | REQ-CAT-014, REQ-CAT-015 |
| ISSUE-API-WSC8-R1-006 | P2 | 预落地三处版本漂移：`VERSION`=2.3.3、`matrix.yaml` version=2.3.2、OpenAPI `info.version`=2.2.0；计划 §3.1 写保持 2.3.2 但未列 **903 对账清单**。903 developer 易只改 matrix 漏 OpenAPI 头或误 bump。 | TASK-WSC-903 acceptance **增硬检**：`contracts/VERSION`、`rbac/matrix.yaml` version、`openapi.yaml` `info.version`、`ui/state-matrix.md` 版本头 **同一 semver**（2.3.2 或委员会批准的对账版本）；若 WORKTREE 已为 2.3.3，§3.1 写死「合并 V1.6 增量至 2.3.3 **且不**再升」或回退至 2.3.2 的裁决二选一；`contracts lint` / 现有 check 脚本可勾选。 | REQ-API-001（继承） |

## 决策

`decision: REQUEST_CHANGES`

- **openIssueCount**: 6（P0×1，P1×4，P2×1）
- **closedIssueCount**: 0

本评审 **仅**输出 apiDataDesigner 视角 ISSUE；**未**修改计划正文、`contracts/**`、Run `state`/`events`；**未**代关 planEditor / productAnalyst 或其他角色 ISSUE；**未** commit。

**批准前最小闭合集（本角色）**：ISSUE-001（端点/scope 绑错，P0）+ ISSUE-003（mine 本企业规则）+ ISSUE-004（matrix 与 OpenAPI RBAC 行）为阻塞项；002/005/006 建议同轮吸收以免 903 返工。
