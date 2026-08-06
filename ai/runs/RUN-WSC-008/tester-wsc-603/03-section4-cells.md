# §4 单元格表证据定位（TASK-WSC-603）

对齐 `planning/approved/PLAN-WSC-5.2.md` TASK-WSC-603 `testScope` 单元格表。

| 能力 | ADMIN | PROVIDER | USER | 自动化证据 |
|---|---|---|---|---|
| 产品写 API | **403** | 本人 **200**；他人/空归属 **403** | **403** | `CreateByOwnershipIntegrationTest#admin_productWriteForbidden_user_productWriteForbidden`；`#provider_updateOwn_200_emptyAndOther_403`；`SessionAuthIntegrationTest#rbacMatrix_admin_categoryMaintenanceOk_productImportForbidden`（产品 POST 403）；`#rbacMatrix_provider_maintenanceForbidden_productAndImportOk`（产品 POST 200）；`#rbacMatrix_user_allWritesAndMaintenanceImportForbidden`；`RbacMatrixTest#productWrite_providerOnly_v14` |
| 导入 API | **403** | **200**（规则内） | **403** | `SessionAuthIntegrationTest#rbacMatrix_*`（ADMIN/USER import 403；PROVIDER import+report 200）；`ProductImportIntegrationTest`（PROVIDER 导入成功路径）；`RbacMatrixTest#productImport_providerOnly_v14`；Vitest `useProductImport.spec.ts`「权限隐藏：仅 PROVIDER」 |
| 我的产品菜单 | **不可达** | 可见 | **不可达** | `useCanWrite.spec.ts`（`canSeeMyProducts` 三角色）；`CatalogBrowsePage.spec.ts`（`nav-my-products` + `/my-products` 路由；ADMIN 门禁保留） |
| 公共目录增改导 | **结构不可达** | **结构不可达** | **结构不可达** | `CatalogBrowsePage.spec.ts`：`writeVisible=isMine && productWriteVisible`；`v-if="writeVisible"` 包裹 create；catalog mode 无写入口 |
| 分类/目录维护 | **200** | 403 | 403 | `SessionAuthIntegrationTest#rbacMatrix_admin_categoryMaintenanceOk_productImportForbidden`；`#rbacMatrix_provider_maintenanceForbidden_*`；`#rbacMatrix_user_allWritesAndMaintenanceImportForbidden`；`RbacMatrixTest#categoryWrite_onlyAdmin` / `#catalogMaintenance_onlyAdmin` |

实跑绑定：`01b-backend-core.txt`（CreateByOwnership 3 + RbacMatrix 7 + SessionAuth 6 + ProductImport 10 = 26，Failures 0）；`02-vitest.txt`（5 files / 53 tests）。

附加：`ProductEditorIntegrationTest`（OpenAPI/编辑回归）本轮 **FAIL**（见 `01c-product-editor.txt`），不计入上表单元格通过，计入总体 FAIL。
