# §4 单元格表证据定位（TASK-WSC-603）— retest r2

对齐 `planning/approved/PLAN-WSC-5.2.md` TASK-WSC-603 `testScope` 单元格表。

| 能力 | ADMIN | PROVIDER | USER | 自动化证据 |
|---|---|---|---|---|
| 产品写 API | **403** | 本人 **200**；他人/空归属 **403** | **403** | `CreateByOwnershipIntegrationTest#admin_productWriteForbidden_user_productWriteForbidden`；`#provider_updateOwn_200_emptyAndOther_403`；`RbacMatrixTest#productWrite_providerOnly_v14` |
| 导入 API | **403** | **200**（规则内） | **403** | `ProductImportIntegrationTest`（PROVIDER 导入成功路径）；`RbacMatrixTest#productImport_providerOnly_v14`；Vitest `useProductImport.spec.ts`「权限隐藏：仅 PROVIDER」 |
| 我的产品菜单 | **不可达** | 可见 | **不可达** | `useCanWrite.spec.ts`（`canSeeMyProducts` 三角色）；`CatalogBrowsePage.spec.ts`（`nav-my-products` + `/my-products` 路由） |
| 公共目录增改导 | **结构不可达** | **结构不可达** | **结构不可达** | `CatalogBrowsePage.spec.ts`：`writeVisible=isMine && productWriteVisible`；catalog mode 无写入口 |
| 分类/目录维护 | **200** | 403 | 403 | `RbacMatrixTest#categoryWrite_onlyAdmin` / `#catalogMaintenance_onlyAdmin`（矩阵单元）；既有 SessionAuth 矩阵用例路径仍有效（本轮 surefire 未重跑 SessionAuth，矩阵+CreateBy/Import 已覆盖产品写/导入格） |

实跑绑定：`01-backend.txt`（CreateByOwnership 3 + ProductEditor 7 + ProductImport 10 + RbacMatrix 7 = 27，Failures 0）；`02-vitest.txt`（5 files / 53 tests）。

附加：`ProductEditorIntegrationTest`（OpenAPI/编辑回归）本轮 **PASS**（7/0）。
