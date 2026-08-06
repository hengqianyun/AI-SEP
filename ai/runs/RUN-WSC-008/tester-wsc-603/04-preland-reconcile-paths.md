# 预落地对账自动化路径（TASK-WSC-603 行 P0）

对齐 PLAN §0.2 603 行 + testScope「预落地对账自动化」。

| P0 行为 | 命名测试类 / spec 路径 | 方法 / 用例 |
|---|---|---|
| `RbacMatrix` 产品写仅 PROVIDER | `backend/.../rbac/RbacMatrixTest.java`（package `com.shdata.datachain.common.security`） | `productWrite_providerOnly_v14`；`productImport_providerOnly_v14` |
| browse `mine` + `create_by` 本人列表 | `backend/.../catalog/browse/CreateByOwnershipIntegrationTest.java` | `mineTrue_listsOnlyOwnCreateBy` |
| 空/异主 create_by → PROVIDER update **403**；本人 200 | 同上 | `provider_updateOwn_200_emptyAndOther_403` |
| ADMIN/USER 产品写 403 | 同上 + `SessionAuthIntegrationTest` | `admin_productWriteForbidden_user_productWriteForbidden`；`rbacMatrix_admin_*` / `rbacMatrix_user_*` |
| `/my-products` + `useCanWrite` | `frontend/.../useCanWrite.spec.ts`；`CatalogBrowsePage.spec.ts` | 三角色 `canSeeMyProducts` / `canWriteProduct` / `canImportProduct`；路由+`nav-my-products` |
| 公共目录去写 | `CatalogBrowsePage.spec.ts` | `writeVisible=isMine && productWriteVisible` |
| 导入宿主迁至我的产品 | `useCatalogImportEntry.spec.ts`；`useProductImport.spec.ts` | `onImportClosed` → `/my-products`；PROVIDER 可开弹层；ADMIN/USER 不可 |
| 双表面 mine 随 mode 响应（FIND-001） | `useCatalogBrowse.spec.ts`；`CatalogBrowsePage.spec.ts` | `catalog↔my-products` query 带/不带 `mine=true`；禁止 `mine: isMine.value` 快照 |
| 导入四态不回退 | `useProductImport.spec.ts`；`ProductImportIntegrationTest` | 四态 classify + 集成 10 tests |
| OpenAPI/编辑不回退（acceptance） | `ProductEditorIntegrationTest` | **本轮 FAIL**（4 cases → `ERR_CATEGORY_LEAF_REQUIRED` / 400 vs 期望 200/5xx） |

核心 P0 路径（除 ProductEditor）均存在且 `01b`/`02` exitCode=0；ProductEditor 见 `01c` exitCode=1。
