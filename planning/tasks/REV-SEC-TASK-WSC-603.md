# 安全审查（实现态 · 产品写 RBAC / create_by 隔离 / 公共目录去写）

```yaml
reviewId: REV-SEC-TASK-WSC-603
taskId: TASK-WSC-603
planId: PLAN-WSC-5.2
round: 1
role: securityReviewer
actorInstance: security-reviewer-wsc-603-r1
decision: APPROVE
p0: 0
p1: 0
riskTagsReviewed:
  - auth-model-change
  - rbac-breaking
  - data-isolation
contracts: wsc-contracts@2.2.0（只读对照）
reviewedAt: 2026-08-06T10:55:00+08:00
basedOn:
  - ai/agents/security-reviewer.md
  - ai/workflow/policies.yaml (riskTriggers.securityReviewer)
  - planning/approved/PLAN-WSC-5.2.md (§4 / §6.2 / TASK-WSC-603)
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-603.md（Round 3）
  - planning/tasks/REV-TASK-WSC-603.md（round 3 APPROVE）
  - planning/tasks/TESTRUN-TASK-WSC-603.md（retest PASS；BUG-WSC-603-TESTRUN-001 CLOSED）
  - 实现面只读：RbacMatrix、WriteAuthorizationInterceptor、SecurityWebConfig、ProductEditorService/Controller、ProductImportService、CatalogBrowseController/Service、CatalogBrowseSeedStore、useCanWrite、CatalogBrowsePage、WorkbenchLayout、routes、AuthController（602 面不回退抽查）、CreateByOwnershipIntegrationTest、RbacMatrixTest、ProductImportIntegrationTest
mustDifferFrom:
  - developer-wsc-603
  - developer-wsc-603-r2
  - developer-wsc-603-r3
  - code-reviewer-wsc-603-r3
  - tester-wsc-603-r2
scopeNote: |
  本审查覆盖 TASK-WSC-603 riskTags：auth-model-change / rbac-breaking / data-isolation
  （及焦点所述公共目录去写、602 安全面不回退、SeedStore ApplicationRunner 分类种子）。
  未触碰 sql/migration → 不代 migrationReviewer。
  未修改被审业务代码；未写 state.yaml / events.jsonl；未标 VERIFIED。
```

## 结论

**APPROVE** — 对照 §4 / TASK-WSC-603 acceptance 与 `auth-model-change` / `rbac-breaking` / `data-isolation`：产品写/导入仅 PROVIDER（拦截器 + `RbacMatrix`；ADMIN/USER API 403）；`create_by` 本人隔离且空/异主更新 403 不可冒领；`mine=true` 仅列本人；公共目录结构无增改导；我的产品仅 PROVIDER；602 真登录/切角色禁用/用户管理菜单未回退；`CatalogBrowseSeedStore` ApplicationRunner 仅在空分类表补齐 L1/L2/L3，无特权账号/产品越权写入。**P0=0，P1=0**。开放项均为 P3 残余加固，不阻塞本任务 securityReviewer 门禁。未标 VERIFIED。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-SEC-WSC-603-R1-001 | P3 | OPEN | `ProductImportIntegrationTest` 有 `ordinaryUser_importForbidden`；ADMIN 导入 POST 403 依赖 `RbacMatrixTest` + 拦截器路径分类，缺命名 HTTP 集成负例 | 增补 `admin_importForbidden`（或等价）MockMvc 用例 | REQ-RBAC-001 |
| FIND-SEC-WSC-603-R1-002 | P3 | OPEN | `CatalogBrowseSeedStore` 作为 `ApplicationRunner` 在**全 profile**空分类表时写入固定树；非 HTTP 攻击面、不造用户/产品，但生产误关 Flyway 时空库会静默种子 | 文档化「仅测境/空库兜底」或 `@Profile`/`wsc.seed.categories` 开关 | REQ-CAT-001；运维 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 审查清单（对照焦点）

### 1. 产品写 / 导入仅 PROVIDER（rbac-breaking / auth-model-change）

| 项 | 证据 | 结果 |
|---|---|---|
| `RbacMatrix.canWriteProduct` / `canImportProduct` 仅 PROVIDER | `RbacMatrix.java`；`RbacMatrixTest` ADMIN/USER false | **PASS** |
| 拦截器 PRODUCT/IMPORT → 403 `ERR_FORBIDDEN` | `WriteAuthorizationInterceptor` classify `/catalog/products`、`/import`；未登录 401 | **PASS** |
| ADMIN/USER 产品写 API 403 | `CreateByOwnershipIntegrationTest#admin_productWriteForbidden_user_productWriteForbidden` | **PASS** |
| USER 导入 403；PROVIDER 导入可达 | `ProductImportIntegrationTest#ordinaryUser_importForbidden`；PROVIDER 成功路径 | **PASS** |
| 契约 matrix 2.2.0 对齐 | `contracts/rbac/matrix.yaml` ADMIN/USER productWrite/Import 403；PROVIDER 可见 | **PASS** |
| 分类/维护仍 ADMIN | `canWriteCategory` / `canMaintainCatalog` 仅 ADMIN；§4 单元格保留 | **PASS** |

### 2. create_by 本人隔离；空/异主不可冒领（data-isolation）

| 项 | 证据 | 结果 |
|---|---|---|
| update 前 `isOwnedBy` | `ProductEditorService.update`：空/缺失/异主 → 403 `ERR_FORBIDDEN` | **PASS** |
| `isOwnedBy` 语义 | create_by 非空且 `equals(actorUserId)`；blank/null → false | **PASS** |
| 新建归属 = 会话 userId | `upsertProduct(..., actorUserId)` 新建写 `createBy`；更新保留旧 `createBy`（不可经 body 改主） | **PASS** |
| actor 来自会话 | `ProductEditorController.actorId` ← `AuthController.currentPrincipal`（非请求体） | **PASS** |
| 自动化负例 | `provider_updateOwn_200_emptyAndOther_403`；TESTRUN retest PASS | **PASS** |
| 导入归属 | 导入仅 `productEditorService.create(..., actorUserId)`；成功行归属导入者；无「冒领既有空主」路径 | **PASS** |

### 3. mine 列表 + 公共目录去写 + 我的产品仅 PROVIDER

| 项 | 证据 | 结果 |
|---|---|---|
| `mine=true` → `productsOwnedBy(当前 userId)` | `CatalogBrowseController` + `CatalogBrowseService`；空 userId → 空列表 | **PASS** |
| mine 过滤自动化 | `CreateByOwnershipIntegrationTest#mineTrue_listsOnlyOwnCreateBy` | **PASS** |
| UI 写入口 | `writeVisible = isMine && productWriteVisible`；公共 mode 无增改导 | **PASS** |
| 我的产品菜单/页 | `canSeeMyProducts` 仅 PROVIDER；Layout `v-if="myProductsVisible"`；非 PROVIDER `onMounted` → `/catalog` | **PASS** |
| 导入宿主 | 关闭/成功回 `/my-products`（`useCatalogImportEntry` / editor `returnTo`） | **PASS** |

### 4. 未削弱 602 安全面（auth-model-change 不回退）

| 项 | 证据 | 结果 |
|---|---|---|
| 切角色仍 410 | `AuthController.switchRole` → `ERR_ROLE_SWITCH_DISABLED`；603 denyModify security controller | **PASS** |
| 用户管理菜单保留 | `WorkbenchLayout` `nav-users` + `session-role-label`；`/admin/users` 路由仍在 | **PASS** |
| 拦截器仍挂载 | `SecurityWebConfig` → `/api/v1/catalog/**`；Cookie `HttpOnly` 未删 | **PASS** |
| PasswordHasher / AuthAuditLogger | 交付声明未改；本审查未发现 603 回退登录 hash | **PASS** |

### 5. SeedStore ApplicationRunner 分类种子

| 项 | 证据 | 结果 |
|---|---|---|
| 触发条件 | `ensureSeedCategoriesIfEmpty`：分类表非空则 no-op（Flyway V5 路径安全） | **PASS** |
| 写入内容 | 仅 11 条 L1/L2/L3 分类；**无**用户/角色/产品/口令种子 | **PASS** |
| 越权写入 | 非 HTTP 端点；进程启动特权写入分类元数据，不开放匿名写 API | **PASS** |
| 不安全默认账号 | 无 | **PASS** |
| 残余 | 全 profile 静默种子 → FIND-SEC-WSC-603-R1-002（P3） | 不阻塞 |

## §4 单元格（本任务安全结论）

| 能力 | ADMIN | PROVIDER | USER |
|---|---|---|---|
| 产品写 API | **403** | 本人 **200**；他人/空 **403** | **403** |
| 导入 API | **403**（矩阵+拦截器；IT 以 USER 代表） | **200** | **403** |
| 我的产品菜单 | 不可达 | 可见 | 不可达 |
| 公共目录增改导 | 结构不可达 | 结构不可达 | 结构不可达 |
| 分类/目录维护 | 200 | 403 | 403 |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / codeReviewer / tester / migrationReviewer。
- `actorInstance=security-reviewer-wsc-603-r1`（≠ developer-wsc-603-r3 / ≠ code-reviewer-wsc-603-r3 / ≠ tester-wsc-603-r2）。
- **decision: APPROVE**（P0=0、P1=0；开放 finding 均为 P3）。

## 计数

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 0 | — |
| P3 | 2 | FIND-001 缺 ADMIN 导入 HTTP 负例；FIND-002 种子 profile 开关 |
| findingCount | 2 | 开放 findings 合计 |
