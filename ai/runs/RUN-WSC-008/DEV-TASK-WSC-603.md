# DEV-TASK-WSC-603 — 交付说明

- **actorInstance**: `developer-wsc-603-r3`（Round 3：BUG-WSC-603-TESTRUN-001；Round 2=`developer-wsc-603-r2`；Round 1=`developer-wsc-603`）
- **taskId**: `TASK-WSC-603`
- **requirements**: `REQ-CAT-010`, `REQ-RBAC-001`, `REQ-SHELL-001`, `REQ-CAT-001`
- **status**: Round 3 已修复 tester 交回 BUG；提交再测（**未**标 VERIFIED；**未**写 `state.yaml` / `events.jsonl`）
- **riskTags**: `[rbac-breaking, data-isolation, import-host-move, auth-model-change]`（**未**触碰 sql/migration）

## Round 3 — BUG 修复（BUG-WSC-603-TESTRUN-001）

| id | severity | 回应 | 实现摘要 |
|---|---|---|---|
| BUG-WSC-603-TESTRUN-001 | P1 | **已修复**（developer 侧；关闭/VERIFIED 权在 tester） | H2 测境 `flyway.enabled=false` 时分类表空 → `findCategory(cat-l3-*)` 失败 → create 400 `ERR_CATEGORY_LEAF_REQUIRED`。`CatalogBrowseSeedStore` 实现 `ApplicationRunner`：空库补齐与 V5/旧内存 seed 一致的 11 条 L1/L2/L3（含 `cat-l3-emr-desense` / `cat-l3-emr-struct`）。附带：OTHER `typeSpecific` 无独立列、mapper 写入 `type_api_json` 回读成 `api` → SeedStore 读路径 `restoreOtherTypeSpecific` 按 `productType=OTHER` 归一为 `other`，避免 OpenAPI 编辑回读回退 |

### 自测（Round 3）

```text
# cwd: backend — exit 0；Tests run: 20, Failures: 0
mvn -pl app/data-chain-service -am "-Dtest=ProductEditorIntegrationTest,CreateByOwnershipIntegrationTest,ProductImportIntegrationTest" test
```

| 套件 | Tests | Failures |
|---|---|---|
| `CreateByOwnershipIntegrationTest` | 3 | 0 |
| `ProductEditorIntegrationTest` | 7 | 0 |
| `ProductImportIntegrationTest` | 10 | 0 |

## Round 2 — 审查回应（FIND-WSC-603-R1-001）

| finding | severity | 回应 | 实现摘要 |
|---|---|---|---|
| FIND-WSC-603-R1-001 | P0 | **已修复**（developer 侧；关闭权在 codeReviewer） | 方案 1：`useCatalogBrowse` 接受 `MaybeRefOrGetter<boolean>`；`buildQuery` 经 `toValue` 读当前 mine；`watch` mine 变化重拉列表。`CatalogBrowsePage` 传 `mine: isMine`（computed），不再 `isMine.value` 快照。自动化：catalog↔mine 切换后 `listProducts` 带/不带 `mine=true` |

**未**在本轮宣称关闭 FIND-002/003/004（P2/P3；非本步必改）。

## 对账结论（§0.2 / 603 行）

| 预落地迹象 | 对账结果 |
|---|---|
| `RbacMatrix` 产品写仅 PROVIDER | **保留并对齐** matrix 2.2.0；ADMIN/USER 产品写/导入 false |
| browse `mine` + `create_by` | **保留并强化**：`mine=true` → `productsOwnedBy`；空/异主 update → **403**（`isOwnedBy`）；R2 前端 mine 随 mode 响应 |
| `/my-products` + `useCanWrite` | **602 R2 曾移除壳层 my-products** → 本任务收口补齐路由/菜单；`useCanWrite` 已含 PROVIDER 矩阵 |
| 公共目录去写 | **确认**：写入口 `writeVisible = isMine && productWriteVisible` |
| L2 与 mine 同文件 | **已迁出**：`CatalogBrowseController/Service` 删除 L2；空壳 `L2DistributionController/Service` 交 604 |
| 导入宿主仍挂 `/catalog` | **已修**：关闭回 `/my-products`；editor 成功/取消回我的产品 |
| 壳层 602 用户管理/角色只读 | **未回退**：保留 `nav-users`、内联 `role==='ADMIN'`、`session-role-label` |

## showSeatMap 约定（供 604 只读消费）

| 项 | 约定 |
|---|---|
| 路由 props | `/catalog` → `{ mode: 'catalog', showSeatMap: true }`；`/my-products` → `{ mode: 'mine', showSeatMap: false }` |
| 组件 props | `CatalogBrowsePage`：`mode?: 'catalog'\|'mine'`；`showSeatMap?: boolean`（未传时 catalog=true / mine=false） |
| 挂载点 | `<CirculationSeatMap v-if="seatMapVisible" data-testid="catalog-seat-map-slot" />` |
| 禁止 | 本任务**不**实现座序图内部逻辑；**不**改 `CirculationSeatMap.vue` |
| 604 | 仅实现 `CirculationSeatMap.vue` + L2 API 类体；消费上述 props/挂载点 |

## L2 空壳声明

| 文件 | 状态 |
|---|---|
| `…/controller/catalog/browse/L2DistributionController.java` | **空壳**（无 `@RestController` 实现）；604 独占写实现体 |
| `…/service/catalog/browse/L2DistributionService.java` | **空壳**；可复用 `CatalogBrowseSeedStore#l2Distribution()` |
| `CatalogBrowseController` | **已无** `l2-distribution` 方法 |

## filesChanged

### Round 3（本实例 `developer-wsc-603-r3`）

- `backend/.../repository/CatalogBrowseSeedStore.java`（空库 L1/L2/L3 分类种子；OTHER `typeSpecific` 读路径归一）
- `backend/.../test/.../catalog/editor/ProductEditorIntegrationTest.java`（对账：PROVIDER 登录；链查询按 `productCode`；与 R1/R2 包路径对齐）

### Round 2

- `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts`（`mine` 响应式 + watch 重拉）
- `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts`
- `frontend/src/features/catalog/browse/CatalogBrowsePage.vue`（`mine: isMine`）
- `frontend/src/features/catalog/browse/CatalogBrowsePage.spec.ts`

### Round 1（保留）

#### Backend
- `common/security/RbacMatrix.java`；`WriteAuthorizationInterceptor.java`
- `controller/catalog/browse/CatalogBrowseController.java`；`service/catalog/browse/CatalogBrowseService.java`
- `controller/catalog/browse/L2DistributionController.java`（空壳）；`service/catalog/browse/L2DistributionService.java`（空壳）
- `service/catalog/editor/ProductEditorService.java`；`controller/catalog/editor/ProductEditorController.java`
- `service/catalog/productimport/ProductImportService.java`
- `repository/CatalogBrowseSeedStore.java`（create_by / mine / industryCategory 兜底）
- `test/.../catalog/browse/CreateByOwnershipIntegrationTest.java`；`**/rbac/RbacMatrixTest.java`
- `test/.../catalog/productimport/ProductImportIntegrationTest.java`；`ProductEditorIntegrationTest.java`

#### Frontend（R1）
- browse / editor / detail / import / `useCanWrite` / `WorkbenchLayout` / `routes.ts`（见 Round 1 清单）

## acceptance 勾选

- [x] PROVIDER 可见「我的数据产品」；ADMIN/USER 菜单不可见 + mine 页 onMounted 重定向
- [x] 我的产品：`showSeatMap=false`；可新增/编辑/导入；列表 `mine=true` 仅本人
- [x] 空/缺失/异主 `create_by` → PROVIDER update **403**
- [x] 公共目录：可浏览；无增改导
- [x] 双表面 + mine 随 mode 响应（FIND-001 R2）
- [x] ADMIN/USER 产品写/导入 403；PROVIDER 本人 200
- [x] 导入四态相关 vitest + ProductImportIntegrationTest
- [x] OpenAPI 编辑相对 PLAN-WSC-4.1 **不回退**（R3：`ProductEditorIntegrationTest` 7/7）
- [x] `showSeatMap` 约定；壳层未回退；L2 已迁出

## testScope 证据定位

| 能力 | 证据 |
|---|---|
| 产品写 / create_by / mine | `CreateByOwnershipIntegrationTest`；SessionAuth / RbacMatrix（既有） |
| OpenAPI 编辑不回退 | `ProductEditorIntegrationTest`（R3 自测 7/7） |
| 导入 | `ProductImportIntegrationTest`（10） |
| 前端 mine/去写/入口 | Vitest（R2；本轮未复跑前端） |

## scope 声明

- 仅修改 `writeSet` 字面路径（R3：`CatalogBrowseSeedStore` + `**/catalog/**` 测试）
- **未**改：`contracts/**`、`frontend/src/api/**`、`CirculationSeatMap.vue`、users/**、security controller、PasswordHasher、AuthAuditLogger、admin/maintenance、e2e、L2 实现体、`state.yaml`、`events.jsonl`
- **未**标 VERIFIED
