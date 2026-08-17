# DEV-TASK-WSC-905

```yaml
taskId: TASK-WSC-905
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: developer-wsc-905
status: READY_FOR_REVIEW
completedAt: 2026-08-17T13:10:00+08:00
reqs:
  - REQ-RBAC-002
  - REQ-CAT-016
  - REQ-CAT-018
blocksRelease: false
riskTags: [rbac-breaking, data-isolation, enterprise-scope, auth-model-change]
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。**未标 VERIFIED**。
> 本任务命中 `auth-model-change`，须经独立 **codeReviewer** + **securityReviewer**；本实例不代写审查。

## objective

Wave B：listProducts `mine=true` → 本企业（§3.2）；全链无企业 filter；ADMIN 产品写/导入 200（本企业）；PROVIDER 不可写他人产品；`RbacMatrix` / `WriteAuthorizationInterceptor` scope-aware（maintenance `full` vs `myCatalog`）。不改 FE `useCanWrite.ts`。

## 预落地对账（§0.2）

| 项 | 预落地 | 对账动作 |
|---|---|---|
| `mine=true` | create_by 本人（V1.5 / TASK-WSC-603） | **改为** create_by → `sys_user.enterprise_id` 与会话 `enterpriseId` 比较；同企异 create_by 可见 |
| `RbacMatrix.canWriteProduct/Import` | 仅 PROVIDER | **ADMIN + PROVIDER**；USER 仍 403 |
| `canMaintainCatalog` | ADMIN+PROVIDER 无 scope | 增 `canMaintainCatalog(role, scope)`：PROVIDER `full`→false，`myCatalog`→true |
| 拦截器 | 只看角色 | 读取 query `scope`；忽略客户端 `enterpriseId` |
| listProducts query | 无 `supplierName`；排序仅 updatedAt | **恢复** supplierName 模糊匹配 + 未分类殿后（REQ-CAT-012 不回退） |
| `useCanWrite.ts` | 906 独占 | **未改** |
| `CatalogMaintenanceController/Service` | 907 独占 BE scope 过滤 | **未改**；interceptor 403/200 本任务完成 |
| matrix.yaml / OpenAPI | 903 已冻结 2.3.3 | **只读** |

**命名用例**：`905-mine-enterprise-scope`、`905-provider-maintenance-403`（`@DisplayName`）。

## filesChanged

| 路径 | 说明 | REQ |
|---|---|---|
| `common/security/RbacMatrix.java` | ADMIN 写/导入；mine API；maintenance scope-aware | REQ-RBAC-002 |
| `common/security/WriteAuthorizationInterceptor.java` | 维护 query `scope`；ADMIN 写/导入放行 | REQ-RBAC-002 |
| `common/security/EnterpriseProductScope.java` | **同包增量**：create_by→enterprise_id；写范围判定 | REQ-CAT-016, REQ-RBAC-002 |
| `controller/catalog/browse/CatalogBrowseController.java` | mine 仅信 SessionPrincipal；USER mine 403；supplierName；忽略企业 query | REQ-CAT-016, REQ-CAT-018 |
| `service/catalog/browse/CatalogBrowseService.java` | mine 本企业过滤；全链不过滤企业；supplierName；未分类殿后 | REQ-CAT-016, REQ-CAT-018 |
| `controller/catalog/editor/ProductEditorController.java` | 传 SessionPrincipal；忽略 body.enterpriseId | REQ-RBAC-002 |
| `service/catalog/editor/ProductEditorService.java` | ADMIN 本企业 / PROVIDER ownCreateBy；空 create_by 403 | REQ-RBAC-002, REQ-CAT-016 |
| `controller/catalog/productimport/ProductImportController.java` | actor 仅会话；忽略 enterpriseId | REQ-RBAC-002 |
| `service/catalog/productimport/ProductImportService.java` | Javadoc：ADMIN 本企业可导入 | REQ-RBAC-002 |
| `src/test/java/.../rbac/RbacMatrixTest.java` | §3.1 矩阵单元格 | REQ-RBAC-002 |
| `src/test/java/.../catalog/browse/EnterpriseScopeCatalogIntegrationTest.java` | mine 本企业、全链 parity、IDOR、ADMIN 写、USER 403 | REQ-CAT-016/018, REQ-RBAC-002 |
| `src/test/java/.../catalog/browse/ListProductsFilterSortIntegrationTest.java` | supplierName + 未分类跨页 | REQ-CAT-018 |
| `src/test/java/.../catalog/browse/CreateByOwnershipIntegrationTest.java` | ADMIN 写 200；USER 403 | REQ-RBAC-002 |
| `src/test/java/.../catalog/browse/CatalogBrowseIntegrationTest.java` | package 与目录对齐（原 `model` 声明导致 ECJ 无法加载） | — |
| `src/test/java/.../catalog/maintenance/MaintenanceScopeIntegrationTest.java` | PROVIDER full 403 / myCatalog 200；ADMIN 双 scope 200 | REQ-RBAC-002 |
| `src/test/java/.../catalog/productimport/ProductImportIntegrationTest.java` | ADMIN 导入 200 + 篡改 enterpriseId 不扩大 | REQ-RBAC-002 |

路径均相对 `backend/app/data-chain-service/src/main/java/com/shdata/datachain/` 或 `src/test/java/com/shdata/datachain/`。

## 交 907（writeSet 内无法补齐的结果集过滤）

既有 `CatalogMaintenanceService.list`：ADMIN **全量**、PROVIDER **create_by=本人**。本任务 **未改** maintenance controller/service。

| 调用 | 本任务 | 907 |
|---|---|---|
| PROVIDER `scope=full` | 拦截器 **403** | — |
| PROVIDER `scope=myCatalog` | 拦截器 **200**；Service 已按 create_by 过滤（测试已断言本人条目） | — |
| ADMIN `scope=full` | 拦截器 **200**；Service 全平台 | 保持 |
| ADMIN `scope=myCatalog` | 拦截器 **200** | **须**按本企业过滤结果集（当前仍为全平台） |

## 自测命令与结果

```bash
# cwd: backend/app/data-chain-service
mvn -q "-Dtest=RbacMatrixTest,EnterpriseScopeCatalogIntegrationTest,MaintenanceScopeIntegrationTest,ListProductsFilterSortIntegrationTest,CreateByOwnershipIntegrationTest,ProductEditorIntegrationTest,ProductImportIntegrationTest,CatalogMaintenanceIntegrationTest" test
# exitCode: 0
# Tests run: 9+5+3+2+3+8+11+6 = 47, Failures: 0, Errors: 0
```

命名用例：`EnterpriseScopeCatalogIntegrationTest` `@DisplayName("905-mine-enterprise-scope")`；`MaintenanceScopeIntegrationTest` `@DisplayName("905-provider-maintenance-403")`。

## denyModify 自检

未改：`contracts/**`、`frontend/src/api/**`、`useCanWrite.ts`、`WorkbenchLayout.vue`、`frontend/src/features/catalog/browse/**`、`frontend/src/features/catalog/maintenance/**`、`L2DistributionController/Service`、`**/sql/**`、`tests/e2e/**`、`product/**`、`planning/**`、`ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`、`CatalogMaintenanceController.java`、`CatalogMaintenanceService.java`。未改已发布 Flyway V1–V7。未 git commit。

## 范围外已知红测（未改）

| 测试 | 现象 | 原因 |
|---|---|---|
| `security/SessionAuthIntegrationTest.rbacMatrix_admin_categoryMaintenanceOk_productImportForbidden` | ADMIN POST 产品 **expected 403 was 200** | V1.5 矩阵；路径在 `**/security/**`，**不在本任务 writeSet** |
| `CatalogBrowseIntegrationTest` 若干筛选项 | 期望 V5 种子产品（MED-EMR-0001 等）total>0，H2 `flyway=false` 下为 0 | 预存在；本任务已用自建夹具覆盖 supplierName/未分类/企业 scope |

## acceptance 自检

| 项 | 结果 |
|---|---|
| ADMIN/PROVIDER `mine=true` 本企业（同企异 create_by 可见；跨企不可见） | PASS（`905-mine-enterprise-scope`） |
| ADMIN 本企业写/导入 200；跨企业写 403 | PASS |
| 全链无登录企业 filter；两 enterprise 同一 query 结果集相同 | PASS |
| supplierName / 未分类跨页不回退；query 无 `enterpriseName` 过滤副作用 | PASS |
| scope 仅信 SessionPrincipal；篡改 enterprise query/body 不扩大可见面 | PASS |
| PROVIDER `scope=full` 403；`myCatalog` 200 | PASS（`905-provider-maintenance-403`） |
| ADMIN `scope=full` / `myCatalog` 均为 200 | PASS（结果集 myCatalog 本企业过滤交 907） |
| 空 create_by：ADMIN/PROVIDER 写 403 | PASS |
| USER mine/写/导入/maintenance 403 | PASS |
| `RbacMatrix` 与 §3.1 / matrix.yaml 一致 | PASS |

本实例 **不** 标 VERIFIED。须独立 codeReviewer **且** securityReviewer。
