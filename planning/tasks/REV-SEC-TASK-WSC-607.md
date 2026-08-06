# 安全审查（实现态 · 目录维护 RBAC 扩大 / create_by 隔离）

```yaml
reviewId: REV-SEC-TASK-WSC-607
taskId: TASK-WSC-607
planId: PLAN-WSC-5.2
round: 1
kind: HOTFIX
role: securityReviewer
actorInstance: security-reviewer-wsc-607-r1
decision: APPROVE
p0: 0
p1: 0
riskTagsReviewed:
  - rbac-breaking
  - data-isolation
contracts: wsc-contracts@2.2.0（matrix/OpenAPI 叙述同步；info.version 未 bump）
reviewedAt: 2026-08-06T17:20:00+08:00
basedOn:
  - ai/agents/security-reviewer.md
  - ai/workflow/policies.yaml (riskTriggers.securityReviewer)
  - ai/runs/RUN-WSC-008/HOTFIX-TASK-WSC-607.md
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-607.md
  - planning/tasks/REV-TASK-WSC-607.md（codeReview APPROVE；securityReviewSuggested）
  - planning/tasks/TESTRUN-TASK-WSC-607.md（PASS；E2E SKIPPED）
  - planning/tasks/REV-SEC-TASK-WSC-603.md（格式对照）
  - 实现面只读：contracts/rbac/matrix.yaml；openapi maintenance 叙述；
    RbacMatrix、WriteAuthorizationInterceptor、SecurityWebConfig、
    CatalogMaintenanceController/Service、CatalogBrowseSeedStore（isOwnedBy/productsOwnedBy/upsert）、
    CategoryAdminController（路径分类）、useCanWrite(+spec)、WorkbenchLayout、
    CatalogMaintenancePage、RbacMatrixTest、CatalogMaintenanceIntegrationTest、
    CategoryAdminIntegrationTest#nonAdmin_cannotReachCategoryWrite
mustDifferFrom:
  - developer-wsc-607
  - code-reviewer-wsc-607-r1
  - tester-wsc-607-r1
scopeNote: |
  本审查覆盖 TASK-WSC-607 riskTags：rbac-breaking / data-isolation
  （PROVIDER 扩大目录维护写面 + ownCreateBy；USER 禁止；ADMIN 全量；分类树仍 ADMIN）。
  未触碰 sql/migration → 不代 migrationReviewer。
  未修改被审业务代码；未写 state.yaml / events.jsonl；未标 VERIFIED。
```

## 结论

**APPROVE** — 相对 SNAP-WSC-005 将目录维护写面扩大至 PROVIDER 后：角色门禁（矩阵 / `RbacMatrix` / 拦截器 / `useCanWrite`）一致；PROVIDER list/update/batch 强制 `create_by=本人`，空归属与异主不可冒领（403），缺失 404；USER 仍 `ERR_MAINTENANCE_FORBIDDEN`；ADMIN 全量未削弱；分类树写（`canWriteCategory` / CATEGORY 资源 / UI 按钮）未误开放给 PROVIDER。自动化正负例（RbacMatrix + Maintenance IT + Vitest）与 TESTRUN PASS 可复核。**P0=0，P1=0**。开放项为 P3 残余加固，不阻塞本任务 securityReviewer 门禁。未标 VERIFIED。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-SEC-WSC-607-R1-001 | P3 | OPEN | `CatalogMaintenanceService` 对归属校验用「非 PROVIDER 即跳过」（`actorRole != PROVIDER`）；list 亦仅在 `== PROVIDER` 时过滤。主控为拦截器已挡 USER/未登录，属纵深防御缺口：若拦截器失效则 fail-open 为全量 | Service 改为 ADMIN 白名单全量，其余角色显式拒绝或仅 PROVIDER 走 mine | REQ-RBAC-001；data-isolation |
| FIND-SEC-WSC-607-R1-002 | P3 | OPEN | 前端路由 `/catalog/admin/categories` 仅登录门禁，无角色 meta；PROVIDER 可直链进入页壳，但写 API 仍 403（`CategoryAdminIntegrationTest#nonAdmin_cannotReachCategoryWrite`）；维护页按钮已 `categoryMaintainVisible` | 可选：路由 meta + beforeEach 对齐 ADMIN；不改变 API 结论 | REQ-RBAC-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 审查清单（对照焦点）

### 1. PROVIDER 扩大目录维护写面仍强制 ownCreateBy（含空归属不可冒领）

| 项 | 证据 | 结果 |
|---|---|---|
| 契约 `on: ownCreateBy` | `matrix.yaml` PROVIDER `catalogMaintenanceApi: { expect: 200, on: ownCreateBy }`；notes 明确越权 403/404 | **PASS** |
| list 仅本人 | `CatalogMaintenanceService.list` → `productsOwnedBy(actorUserId)`；空 userId → 空列表（SeedStore） | **PASS** |
| update/batch 归属 | `requireOwnershipIfProvider` → `catalog.isOwnedBy`；空/异主 → 403 `ERR_FORBIDDEN`；不存在 → 404 | **PASS** |
| `isOwnedBy` 语义 | create_by 非空且 `equals(actorUserId)`；blank/null actor 或 blank owner → false | **PASS** |
| 不可经 body 改主 | `applyAssociate` 保留 `src` 业务字段；`upsertProduct` 更新路径 `entity.setCreateBy(old.getCreateBy())` | **PASS** |
| actor 来自会话 | Controller `actorId`/`actorRole` ← `AuthController.currentPrincipal`（非请求体） | **PASS** |
| 自动化负例 | `provider_listOnlyOwn_and_associateOwn200_other403`；`provider_batch_ownOk_foreignInFailures`；TESTRUN backend 6/0 | **PASS** |

### 2. USER 仍禁止；ADMIN 全量不削弱

| 项 | 证据 | 结果 |
|---|---|---|
| USER UI | `canMaintainCatalog('USER')=false`；Vitest；侧栏 `v-if="catalogMaintenanceVisible"` | **PASS** |
| USER API | 拦截器 MAINTENANCE → `ERR_MAINTENANCE_FORBIDDEN`；IT `user_forbidden_maintenance` GET+batch | **PASS** |
| ADMIN UI/API | `canMaintainCatalog(ADMIN)=true`；list 走 `catalog.products()`；既有 ADMIN 单条/批量/分页用例保留 | **PASS** |
| ADMIN 矩阵 | `catalogMaintenanceApi: { expect: 200, on: valid }` 未收窄 | **PASS** |

### 3. 分类树维护未误开放给 PROVIDER

| 项 | 证据 | 结果 |
|---|---|---|
| `RbacMatrix.canWriteCategory` 仅 ADMIN | 源码 + `RbacMatrixTest` PROVIDER/USER false | **PASS** |
| 拦截器 CATEGORY | `classify(/catalog/categories)` → `canWriteCategory`；与 MAINTENANCE 路径分离 | **PASS** |
| 契约 | PROVIDER `categoryMaintainUI: hidden`；`categoryWriteApi: 403 ERR_FORBIDDEN` | **PASS** |
| UI | `canMaintainCategory` 仅 ADMIN；维护页「维护三级分类」`v-if="categoryMaintainVisible"` | **PASS** |
| HTTP 负例（既有） | `CategoryAdminIntegrationTest#nonAdmin_cannotReachCategoryWrite` PROVIDER POST 403 | **PASS** |
| 残余 | 分类管理路由无角色 meta → FIND-SEC-WSC-607-R1-002（P3） | 不阻塞 |

### 4. 矩阵 / 拦截器 / 服务层一致性；越权路径

| 项 | 证据 | 结果 |
|---|---|---|
| 三角色矩阵对齐 | matrix ↔ `RbacMatrix` ↔ `useCanWrite` ↔ 拦截器 `isAllowed(MAINTENANCE)` | **PASS** |
| 拦截器挂载 | `SecurityWebConfig` → `/api/v1/catalog/**`；MAINTENANCE 含 GET | **PASS** |
| 未登录 | principal null → 401（先于角色 403） | **PASS** |
| OpenAPI 叙述 | maintenance list/update/batch 写明 ADMIN 全量 / PROVIDER ownCreateBy / USER 403 | **PASS** |
| 产品写面未回退 | `canWriteProduct`/`canImportProduct` 仍仅 PROVIDER；ADMIN/USER 403 矩阵保留 | **PASS** |
| 用户管理未扩大 | `canManageUsers` 仅 ADMIN；Layout `nav-users` 仍 ADMIN | **PASS** |
| 纵深防御 | Service 非 PROVIDER fail-open → FIND-SEC-WSC-607-R1-001（P3） | 不阻塞 |

## §4 单元格（本任务安全结论 · 目录维护增量）

| 能力 | ADMIN | PROVIDER | USER |
|---|---|---|---|
| 目录维护 UI | 可见 | 可见 | 隐藏 |
| 目录维护 list | 全量 **200** | 仅本人 **200** | **403** `ERR_MAINTENANCE_FORBIDDEN` |
| 目录维护 update/batch | 任意 **200** | 本人 **200**；空/异主 **403**；缺失 **404** | **403** `ERR_MAINTENANCE_FORBIDDEN` |
| 分类树写 UI/API | **200** | UI 隐藏；API **403** | **403** |
| 产品写 / 导入（603 面） | **403** | 本人 / 导入 **200** | **403** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / codeReviewer / tester / migrationReviewer。
- `actorInstance=security-reviewer-wsc-607-r1`（≠ `developer-wsc-607` / ≠ `code-reviewer-wsc-607-r1` / ≠ `tester-wsc-607-r1`）。
- **decision: APPROVE**（P0=0、P1=0；开放 finding 均为 P3）。

## 计数

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 0 | — |
| P3 | 2 | FIND-001 Service 非 PROVIDER fail-open；FIND-002 分类管理路由无角色 meta |
| findingCount | 2 | 开放 findings 合计 |
