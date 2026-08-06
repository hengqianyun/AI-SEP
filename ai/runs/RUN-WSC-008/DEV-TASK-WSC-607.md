# DEV-TASK-WSC-607 — 交付说明

- **actorInstance**: `developer-wsc-607`
- **taskId**: `TASK-WSC-607`
- **requirements**: `REQ-CAT-007`, `REQ-RBAC-001`（hotfix：PROVIDER 目录维护 · 仅本人 `create_by`）
- **status**: 开发完成，提交审查（**未**标 VERIFIED；**未**写 `state.yaml` / `events.jsonl`）
- **riskTags**: `[rbac-breaking, data-isolation]`（**未**触碰 sql/migration；**未** bump contracts VERSION，仍为 `2.2.0`，OpenAPI info 已注明 hotfix）

## 实现摘要

| 层 | 变更 |
|---|---|
| 契约 | `matrix.yaml`：PROVIDER `catalogMaintenanceUI=visible`，API `on: ownCreateBy`；USER 仍 `ERR_MAINTENANCE_FORBIDDEN`；notes 补充隔离说明。OpenAPI maintenance 叙述同步（VERSION 不 bump） |
| 前端 | `canMaintainCatalog` → ADMIN+PROVIDER；Vitest 正负例；侧栏 `nav-catalog-maintenance`（WorkbenchLayout 仅依赖 `useCanWrite`，分类维护按钮仍 ADMIN-only） |
| 后端 | `RbacMatrix.canMaintainCatalog` 含 PROVIDER（拦截器自动对齐）；`CatalogMaintenanceService` list=`productsOwnedBy`；update/batch 空/异主 → `403 ERR_FORBIDDEN`，不存在 → `404`；ADMIN 全量不变 |
| 测试 | `RbacMatrixTest`；`CatalogMaintenanceIntegrationTest` 正负例；E2E v1.1/v1.3/v1.4 PROVIDER 可见维护 |

## filesChanged

### Contracts
- `contracts/rbac/matrix.yaml`
- `contracts/openapi/openapi.yaml`（叙述；**version 仍 2.2.0**）

### Frontend
- `frontend/src/features/auth/composables/useCanWrite.ts`
- `frontend/src/features/auth/composables/useCanWrite.spec.ts`
- `frontend/src/layouts/WorkbenchLayout.vue`（`data-testid="nav-catalog-maintenance"`）

### Backend
- `backend/.../common/security/RbacMatrix.java`
- `backend/.../controller/catalog/maintenance/CatalogMaintenanceController.java`（注入会话 role/userId）
- `backend/.../service/catalog/maintenance/CatalogMaintenanceService.java`（create_by 隔离，对齐 603）
- `backend/.../test/.../rbac/RbacMatrixTest.java`（package: `common.security`）
- `backend/.../test/.../catalog/maintenance/CatalogMaintenanceIntegrationTest.java`

### E2E（小改）
- `tests/e2e/specs/p0-wsc-v1.1.spec.ts`
- `tests/e2e/specs/p0-wsc-v1.3.spec.ts`
- `tests/e2e/specs/p0-wsc-v1.4.spec.ts`

**未改**：`WriteAuthorizationInterceptor`（角色门禁经 `RbacMatrix` 自动生效）；分类树 ADMIN API；用户管理；座序图/L2；auth 大改；sql；`state.yaml`/`events.jsonl`

## acceptance 勾选

- [x] 侧栏「目录维护」ADMIN + PROVIDER 可见；USER 不可见（`useCanWrite` + WorkbenchLayout）
- [x] PROVIDER list/update/batch 仅 `create_by=本人`；异主/空归属 403；不存在 404
- [x] ADMIN 行为不变（全量 list / associate / batch）
- [x] `matrix.yaml` + OpenAPI 叙述 + `RbacMatrix` + `useCanWrite` + 拦截器（经矩阵）对齐；**未 bump VERSION**（已在 OpenAPI description 注明）
- [x] 自动化：RbacMatrix / Maintenance 集成正负例；Vitest 菜单权限；E2E 抽样；**未**标 VERIFIED

## 自测

```text
# backend — BUILD SUCCESS；Tests run: 13, Failures: 0
mvn -pl app/data-chain-service -am "-Dtest=RbacMatrixTest,CatalogMaintenanceIntegrationTest" test
# cwd: backend

# frontend — 3 passed
npx vitest run src/features/auth/composables/useCanWrite.spec.ts
# cwd: frontend
```

| 套件 | Tests | Failures |
|---|---|---|
| `CatalogMaintenanceIntegrationTest` | 6 | 0 |
| `RbacMatrixTest` | 7 | 0 |
| `useCanWrite.spec.ts` | 3 | 0 |

## scope 声明

- 仅修改 writeSet 字面路径（含 E2E 小改）
- **未**开放分类树 ADMIN API 给 PROVIDER；**未**改用户管理 / 座序图 / L2 / auth 大改 / sql
- **未**写 `state.yaml` / `events.jsonl`
- **未**标 VERIFIED（通过权在 codeReviewer + tester）

---

## Round 2 / residual — SessionAuth 矩阵对齐（developer-wsc-607-r2）

- **actorInstance**: `developer-wsc-607-r2`
- **problem**: `SessionAuthIntegrationTest#rbacMatrix_provider_maintenanceForbidden_productAndImportOk` 仍断言 PROVIDER → `ERR_MAINTENANCE_FORBIDDEN`，与 607（PROVIDER 允许维护、仅 create_by 隔离）冲突；旧断言会误导回归。
- **fix**: 仅改 `SessionAuthIntegrationTest.java`；USER 仍 `ERR_MAINTENANCE_FORBIDDEN`；分类树写仍拒绝 PROVIDER。

### filesChanged

- `backend/app/data-chain-service/src/test/java/com/shdata/datachain/security/SessionAuthIntegrationTest.java`
  - 重命名：`rbacMatrix_provider_maintenanceAllowed_categoryForbidden_productAndImportOk`
  - GET `/catalog/maintenance/entries` → 200 / `code=0`
  - 先建本人产品再 PUT associate → 200 / `code=0`（不再断言 `ERR_MAINTENANCE_FORBIDDEN`）
  - POST categories 仍 `ERR_FORBIDDEN`

### 自测（Round 2）

```text
mvn -pl app/data-chain-service -am "-Dtest=SessionAuthIntegrationTest,RbacMatrixTest,CatalogMaintenanceIntegrationTest" test
# cwd: backend — BUILD SUCCESS
```

| 套件 | Tests | Failures |
|---|---|---|
| `SessionAuthIntegrationTest` | 6 | 0 |
| `CatalogMaintenanceIntegrationTest` | 6 | 0 |
| `RbacMatrixTest` | 7 | 0 |
| **合计** | **19** | **0** |

- **未**写 `state.yaml` / `events.jsonl`；**未**标 VERIFIED
