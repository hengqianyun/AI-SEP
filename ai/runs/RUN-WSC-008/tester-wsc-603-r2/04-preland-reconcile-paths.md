# 预落地对账自动化路径（TASK-WSC-603 行 P0）— retest r2

对齐 PLAN §0.2 603 行 + testScope「预落地对账自动化」。

| P0 行为 | 命名测试类 / spec 路径 | 方法 / 用例 | 本轮 |
|---|---|---|---|
| `RbacMatrix` 产品写仅 PROVIDER | `backend/.../rbac/RbacMatrixTest.java` | `productWrite_providerOnly_v14`；`productImport_providerOnly_v14` | PASS（7/0） |
| browse `mine` + `create_by` 本人列表 | `CreateByOwnershipIntegrationTest.java` | `mineTrue_listsOnlyOwnCreateBy` | PASS（3/0） |
| 空/异主 create_by → PROVIDER update **403**；本人 200 | 同上 | `provider_updateOwn_200_emptyAndOther_403` | PASS |
| ADMIN/USER 产品写 403 | 同上 + RbacMatrix | `admin_productWriteForbidden_user_productWriteForbidden` | PASS |
| `/my-products` + `useCanWrite` | `useCanWrite.spec.ts`；`CatalogBrowsePage.spec.ts` | 三角色 + `nav-my-products` | PASS |
| 公共目录去写 | `CatalogBrowsePage.spec.ts` | `writeVisible=isMine && productWriteVisible` | PASS |
| 导入宿主迁至我的产品 | `useCatalogImportEntry.spec.ts`；`useProductImport.spec.ts` | → `/my-products`；PROVIDER 可开 | PASS |
| 双表面 mine 随 mode 响应（FIND-001） | `useCatalogBrowse.spec.ts`；`CatalogBrowsePage.spec.ts` | catalog↔mine 重拉 | PASS |
| 导入四态不回退 | `useProductImport.spec.ts`；`ProductImportIntegrationTest` | 四态 + 集成 10 | PASS |
| OpenAPI/编辑不回退（acceptance） | `ProductEditorIntegrationTest` | 7 tests | **PASS**（相对 R1 FAIL 已绿） |

全部命名路径存在且本轮 `01-backend` / `02-vitest` exitCode=0。
