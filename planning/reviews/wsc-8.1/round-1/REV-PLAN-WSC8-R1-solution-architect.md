# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC8-R1-solutionArchitect
planId: PLAN-WSC-8.1
round: 1
role: solutionArchitect
actorInstance: solution-architect-wsc-011-r1
snapshotIdAtReview: SNAP-WSC-008
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-008（status=APPROVED）与 PLAN-WSC-8.1：Wave A∥901/902 互斥、903 独占
  wsc-contracts@2.3.2 增量、904 独占 Flyway、L2Distribution 独立 Controller（902）与
  browse list scope（905）拆分、907 维护页参数化复用、预落地对账（§0.2）总体架构方向
  与 V1.5 谱系一致，座序图 l1 参数与全链无企业过滤边界清晰。
  阻断调度的是：(1) §6 声明 905∥906「写集无交」但二者同写 useCanWrite.ts，与 §1.4
  字面 scope-check 冲突；(2) 904 model/** glob 过宽；(3) §3.1 Wave B 契约冻结未覆盖
  目录维护 entries 的 full vs myCatalog scope，而 ADMIN 双入口（全量维护 vs 我的目录
  本企业）后端过滤语义必须由 903 契约 + 907 实现对齐。另记录 enterprise 产品范围推导
  策略、904 enterprise_name→enterprise_id 迁移、路由路径统一为 P2。本轮 REQUEST_CHANGES。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务边界 vs SNAP 十条 REQ | 条件通过 — §7 映射完整；901 浏览 / 902 座序图 / 903 契约 / 904 用户企业 / 905 list+RBAC / 906 壳层 / 907 我的目录复用 / 908 E2E 归属合理 |
| 写集冲突 / 互斥 | **不通过** — 905∥906 同写 `useCanWrite.ts` 且 §6 误称无交；904 `model/**` 过宽；见 ISSUE |
| 分层约束（DEC-WSC-005 / CLAUDE.md） | 通过 — §0.2/§1.3 禁止业务域顶层包；Java 写集落在按类型分层路径 |
| 预落地对账 | 通过 — §0.2 列 void 前 901/906/903 迹象；验收以 SNAP+acceptance 为准；deny `ai/runs/**` |
| DAG A→B→C→D→E | 通过 — 无环；903 在 901+902 后冻结契约；904→905 串行；禁止 903∥907 合理 |
| 契约 2.3.2 / 实现边界 | **部分不通过** — 903 独占 contracts/api 清晰；§3.1 缺 maintenance scope 与 myCatalog 参数硬冻结；见 ISSUE-SA-R1-003 |
| 座序图 API（l1 / 无企业过滤） | 通过 — 902 独占 `L2DistributionController/Service`（已自 browse 拆出）；903 补 OpenAPI `?l1=`；905 不改 L2；REQ-CAT-018 负例在 902/908 |
| Flyway / 企业实体 | 条件通过 — 904 独占 `sql/migration/**`；905 deny sql；OQ-V16-001/002 non-blocking；`enterprise_name` 迁移路径见 P2 ISSUE |
| 维护页复用（907） | 条件通过 — 单页参数化 full vs myCatalog 架构合理；依赖 maintenance API scope 契约闭合（ISSUE-SA-R1-003） |
| BE/FE 边界 | 条件通过 — 901 FE-only browse；902 FE+ L2 BE；905 BE scope+矩阵片段；906 FE 导航；907 FE 维护+可选 BE scope；904 BE 用户+FE users 字段 |

## 架构抽查（证据）

### 座序图 API 与 browse 拆分

- 预落地 `L2DistributionController` 已独立 `@RequestMapping("/api/v1/catalog")`，与 `CatalogBrowseController` 分离（WSC-5.1 ISSUE 修复模式延续）。
- PLAN §5 TASK-WSC-902 独占 `L2Distribution*`；905 `denyModify` 同路径；902 `denyModify` `CatalogBrowsePage` 筛选区 — **可字面 scope-check**。
- 当前 OpenAPI `/catalog/l2-distribution` 无 `l1` query（`contracts/openapi/openapi.yaml` L292–308）；902 可先实现 BE，903 对账契约 — 顺序可接受。
- REQ-CAT-018：L2 分布与全链 list **不得**按登录企业过滤 — 902 acceptance 负例 + 908 场景 3 覆盖。

### Wave B 契约 2.3.2

- 树中 `contracts/VERSION` 已为 `2.3.2`；`matrix.yaml` 仍为 V1.5 语义（PROVIDER `catalogMaintenanceUI: visible`、ADMIN 产品写 403）— 903 须按 §3.1/§4 纠偏，禁止静默 bump，与 §0.2 对账一致。
- §3.1 冻结 `GET /catalog/products` mine/scope、enterprise 会话、RBAC 矩阵、`l2-distribution?l1=` — 产品 list scope 有叙述。
- **缺口**：`/catalog/maintenance/entries` 现仅「ADMIN 全量 / PROVIDER create_by」（`CatalogMaintenanceController` L35–36）；V1.6 ADMIN 须 **双轨**（目录维护全量 vs 我的目录本企业），PROVIDER 仅 myCatalog。§3.1 **未**冻结 maintenance scope 参数或等价路径，903 requirements 含 REQ-CAT-017 但 acceptance 未列 — 契约/907 BE 分叉风险。

### 企业 scope 与 Flyway

- `sys_user` 已有 `enterprise_name`（`V6__create_sys_user.sql`）；无独立企业表 / `enterprise_id` FK — 904 最小实体 + Flyway 与 OQ-V16-001 一致。
- 产品 scope 现通过 `CatalogBrowseSeedStore.productsOwnedBy(create_by)`（内存/JPA `t_data_product.create_by`）；**无** `enterprise_id` 产品列。mine=本企业可经 `create_by → sys_user.enterprise_id` 联结实现，905/907 不必改 schema — 但计划 **未**写明推导规则与 908 多样本企业种子要求（P2）。
- 904 独占 Flyway；905/907 `denyModify **/sql/**` — 迁移边界清晰。

### 维护页复用（907）

- 预落地 `/my-maintenance` 与 `/catalog/maintenance` 均挂载 `CatalogMaintenancePage.vue`（`routes.ts` L30–34、L66–69）；907 参数化 scope 复用 REQ-CAT-013 UX 架构正确。
- `useCatalogMaintenance.ts` 当前无 scope 参数，调单一 `listMaintenanceEntries` — 907 须增 scope/route meta 驱动 query；与 ISSUE-SA-R1-003 契约闭合联动。

### 写集交叉（字面）

| 共享路径 | 任务 | 计划声称 | 架构判定 |
|---|---|---|---|
| `useCanWrite.ts` + spec | 905 + 906 | 905「仅 canWriteProduct/Import 片段」；906「完整导航门控」；§6 称 905∥906 写集无交 | **同文件双作者 + §6 叙述错误** → ISSUE-SA-R1-001 |
| `model/**` | 904 | 注释意图 SessionPrincipal/User DTO | **glob 过宽** → ISSUE-SA-R1-002 |
| `router/routes.ts` | 906 → 907 串行 | 906 导航；907 增 my-catalog guard | 串行可容忍；路径 `/my-maintenance` vs `/my-catalog` 待统一 → ISSUE-SA-R1-004（P2） |
| `RbacMatrix` / `WriteAuthorizationInterceptor` | 905 独占 | — | 通过（相对 WSC-5.1 602/603 双写已改进） |
| `L2Distribution*` vs `CatalogBrowse*` | 902 vs 905 | 互斥 deny | 通过 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | PLAN §5 TASK-WSC-905 `writeSet` 含 `frontend/src/features/auth/composables/useCanWrite.ts` 与 `useCanWrite.spec.ts`（散文：仅 `canWriteProduct`/`canImportProduct` ADMIN=true）；TASK-WSC-906 同路径全文。§6 并行表称「905 ∥ 906 … **写集无交** … **允许**」。Orchestrator 字面 scope-check 无法区分「产品写片段」与「导航/full 文件」；并行时 CR 覆盖不可判定，与 §1.4「writeSet 字面路径 scope-check」直接冲突。 | 在计划 §5/§6 **明文三选一**并改写 writeSet/denyModify/并行表：**(A)** 905 **denyModify** `useCanWrite.ts`；ADMIN 产品写可见性仅由 906 在 903 VERIFIED 后一次性对齐 matrix（905 仅 BE + 集成测证明 ADMIN 写 200）。**(B)** 905∥906 **改串行**：905 VERIFIED 后 906 独占 `useCanWrite.ts` 全文（含 ADMIN 产品写与导航）。**(C)** 拆文件：如 `useProductWriteAccess.ts`（905 独占）与 `useNavVisibility.ts`（906 独占），`useCanWrite` 仅 re-export；writeSet **字面列出**各文件。同步修正 §6 并行表「写集无交」表述。 | REQ-RBAC-002, REQ-CAT-016, REQ-SHELL-009 |
| ISSUE-SA-R1-002 | P1 | PLAN §5 TASK-WSC-904 `writeSet` 含 `backend/.../model/**`（注释：SessionPrincipal/User DTO enterprise 字段）。字面 glob 覆盖全部 model（含 `CatalogProduct` 等目录 DTO）。对账名义下可合法修改非用户模型，与 §0.2 包结构及 905/907 边界冲突；复现 WSC-5.1 REV ISSUE-SA-R1-003 模式。 | 将 904 `model/**` **改为显式文件列表**（如 `SessionPrincipal`、`AdminUser*`、企业相关 record/DTO）；或在 904 `denyModify` **字面列出**禁止触碰的 model 路径（含 `CatalogProduct.java` 等）。若 905/907 需新增 scope 相关 DTO，在各自 writeSet 字面列出。 | REQ-USER-002, REQ-CAT-016, REQ-CAT-017 |
| ISSUE-SA-R1-003 | P1 | SNAP REQ-CAT-017 / PRD §3.6：ADMIN「目录维护」= **全平台**；ADMIN「我的目录」= **本企业**；PROVIDER「我的目录」= **create_by 本人**；PROVIDER **无**目录维护。现 `GET /catalog/maintenance/entries` 仅 ADMIN 全量 / PROVIDER create_by（`CatalogMaintenanceController` + `CatalogMaintenanceService.list`）。PLAN §3.1 冻结表列 `GET /catalog/products` scope/mine，**未**列 maintenance entries 的 full vs myCatalog 语义；903 requirements 含 REQ-CAT-017 但 903 acceptance 无 maintenance OpenAPI 行；907 writeSet 含 maintenance controller/service。ADMIN 双入口若共用一个无 scope 的 list API，无法同时满足全量 vs 本企业 — 契约与 907 实现真源漂移。 | 在 §3.1 与 TASK-WSC-903 acceptance **硬冻结** maintenance scope，三选一：**(A)** `GET /catalog/maintenance/entries?scope=full\|myCatalog`（OpenAPI enum + matrix 行：ADMIN full=200；ADMIN myCatalog=本企业；PROVIDER 仅 myCatalog；PROVIDER 调 full/maintenance 403）。**(B)** 独立路径如 `/catalog/my-catalog/entries` 由 903 定义，907 实现。**(C)** 单路径 + 会话角色推导（仅当 OpenAPI **明确**写清 ADMIN 默认 full、myCatalog 路由强制 scope 且可测）。同步更新 `matrix.yaml` `myCatalogUI`/`catalogMaintenanceApi` PROVIDER 403 与 §4 表一致。 | REQ-CAT-017, REQ-RBAC-002, REQ-CAT-016 |
| ISSUE-SA-R1-004 | P2 | PLAN §3.1：`scope=myCatalog` **或等价参数** — 产品 list 与 maintenance 命名未统一。§0.2 预落地路由 `/my-maintenance`，§5 906 acceptance 称「`/my-catalog` 或 PO 确认路径，**全计划统一**」— 契约参数名、路由 path、菜单 key 三元组未在 §3.1 一次冻结，903/906/907/908 可能对账分叉。 | 在 §3.1 或 903 acceptance **固定**：API query/path 名、前端路由 path（建议统一为 `/my-catalog` 并迁移 void 前占位）、矩阵 `myCatalogUI` key；§0.2/906/907/908 引用同一常量叙述。 | REQ-CAT-017, REQ-SHELL-009 |
| ISSUE-SA-R1-005 | P2 | V1.6 mine=本企业 / myCatalog ADMIN=本企业 依赖产品 `create_by` 与用户 `enterprise_id` 联结（`CatalogBrowseSeedStore.productsOwnedBy` + `t_data_product.create_by`；无产品 enterprise 列）。PLAN 905/907 acceptance 未写推导规则；908 场景 3–4 需多企业多样本。904 OQ-V16-002 仅「交付说明」— 缺 905/908 可检钩子。 | 在 §3.2 或 905 acceptance **明文**：enterprise scope = `create_by` 所属用户与 session `enterpriseId` 相同（或 904 若引入产品 enterprise 列则单列 owner）；904/905 testScope 含「同企业多 ADMIN + 跨企业负例」种子/ fixture 要求；不在未分配任务改 seed 的前提下写清谁负责测试数据（904 种子 vs 905 集成测 setup）。 | REQ-CAT-016, REQ-CAT-017, REQ-CAT-018, REQ-USER-002 |
| ISSUE-SA-R1-006 | P2 | `V6__create_sys_user.sql` 已有 `enterprise_name`；§3.2/904 叙述「企业表 + sys_user 外键」但未写 `enterprise_name` 列保留/废弃/回填策略。Flyway 增量若加 `enterprise_id` 而不迁移既有 denormalized 字段，会话 enterprise 解析可能双源。 | 904 acceptance 增加：**(a)** `enterprise_id` FK 与 `enterprise_name` 兼容/回填/deprecate 策略写清；**(b)** 迁移脚本幂等；**(c)** 会话 principal 单一真源（id+name）。 | REQ-USER-002 |

## 无异议项（记录）

- 903 **唯一**写 `contracts/**` + `frontend/src/api/**`；禁止 903∥907 — 契约/client 边界清晰。
- 901 ∥ 902 Wave A：`CatalogBrowsePage` 筛选 vs `CirculationSeatMap` 互斥 deny — 相对 V1.5 并行模式改进。
- 902 可选先 mock `l1` query、903 对账 OpenAPI — FE/BE 解耦可接受。
- 907 维护页参数化复用（非 fork 新页）— 符合 REQ-CAT-013 / REQ-CAT-017。
- Out of Scope（企管页、座序图企业筛、OAuth、多租户）与 SNAP 一致。
- 不代替 securityOperations / qaStrategist / productAnalyst 裁定；不以预落地 void 代码代替委员会共识。
- REQ-SHELL-010（P1）不阻塞 A/B — Wave C 依赖 903 只读 client，架构合理。

## 决策

`REQUEST_CHANGES` — 解决方案架构维度未关闭 ISSUE：**3×P1**（ISSUE-SA-R1-001 / 002 / 003）；**3×P2**（004 / 005 / 006）建议与修订稿一并闭合。

在 ISSUE-SA-R1-001 / 002 / 003 关闭前，不建议按本候选稿字面 writeSet 派发 Wave B/C（`useCanWrite` 双作者、model glob 越界、maintenance scope 契约缺口会导致 scope-check 与 ADMIN 双入口验收不可判定）。P2 项不单独构成 BLOCK，但应在 Round 1 修订中一并消歧以避免 903/906/907/908 对账返工。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-011-r1`；**不**代写其他委员会角色结论，**不**伪造他角色 `APPROVE`；**不**修改 plan/state/events。
