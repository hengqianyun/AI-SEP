# DEV-TASK-WSC-909

```yaml
taskId: TASK-WSC-909
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
actorInstance: developer-wsc-909
status: READY_FOR_REVIEW
kind: HOTFIX
blocksRelease: true
completedAt: 2026-08-17T16:20:00+08:00
reqs:
  - REQ-CAT-009
  - REQ-CAT-012
  - REQ-CAT-015
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## 对账结论（hotfix 三项）

| # | PO 验收 | 本任务动作 |
|---|---|---|
| 1 | 座序图 h2「数据流通链」相对卡片水平居中；L1 可留右 | `.title-row` 改为相对定位：h2 `left:50%` + `translateX(-50%)`；L1 Select `flex-end` + `z-index`。覆盖 902「h2 左 / L1 右」。无企业筛；不与 Cascader 联动 |
| 2 | L3 count = 当前筛选全集，不随分页变大 | 列表响应新增 `l3Counts`（服务端按同一套筛选聚合）；前端 `groupProductsByL3` 用该 map，不再用已加载 `list.length` |
| 3 | 创建人/操作人留档 + 界面可见 | 写路径：新建写 `create_by`+`update_by`；后续改只刷 `update_by`（既有 `upsertProduct`）；忽略客户端 body 审计键。读路径：列表/详情/编辑响应补 `createBy`/`updateBy`/`createByName`/`updateByName`；预览只读展示 |

无未决 ISSUE。PO 第 3 项按候选包「产品新增/编辑/导入审计 + 界面可见」实现，未改口。

## filesChanged

### Backend
- `backend/.../controller/catalog/browse/CatalogBrowseController.java` — 列表返回说明含 `l3Counts`
- `backend/.../service/catalog/browse/CatalogBrowseService.java` — 筛选全集聚合 `l3Counts`；`productToMap` 附加审计字段
- `backend/.../repository/CatalogBrowseSeedStore.java` — `findProductAudit` / `appendAuditFields` / `actorLabel`；三参构造注入 `SysUserRepository`（保留两参供单测）
- `backend/.../service/catalog/editor/ProductEditorService.java` — `sanitizeWriteBody` 丢掉客户端审计键；`toMap` 附加审计
- `backend/.../test/.../ListProductsFilterSortIntegrationTest.java` — `l3Counts_fullTotal_independentOfPageSize`
- `backend/.../test/.../catalog/editor/ProductEditorIntegrationTest.java` — `audit_createBy_updateBy_ignoresClientBody_andPreservesCreator`

### Frontend
- `frontend/src/features/catalog/browse/components/CirculationSeatMap.vue` — 标题居中布局
- `frontend/src/features/catalog/browse/components/CirculationSeatMap.spec.ts` — 902 命名用例仍绿（去掉 space-between）；新增 `909-seatmap-title-center`
- `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` — `l3Counts` 驱动分节 count
- `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` — 改写「count 绑死已加载子集」；新增 `909-l3-count-full-total`
- `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` — 预览只读「创建人」「操作人」
- `frontend/src/features/catalog/browse/CatalogBrowsePage.spec.ts` — `909-audit-createBy-updateBy`
- `frontend/src/api/catalog.ts` — `Product.updateBy` / `createByName` / `updateByName`；`ProductPage.l3Counts`

### Contracts
- `contracts/openapi/openapi.yaml` — Product 最小增量 `updateBy`/`createByName`/`updateByName`；ProductPage 可选 `l3Counts`。**未 bump** `wsc-contracts`（仍 2.3.3；纯加字段）

### 未改（理由）
- `CatalogProduct` / `CatalogEntityMapper`：给 record 加字段会牵动 `CatalogMaintenanceService` 构造（不在 writeSet）。审计从实体列读取，不入库新列。
- `ProductImportService`：成功行已走 `ProductEditorService.create`，审计与新建同路径。
- `contracts/VERSION`：加字段不 bump。

## acceptance 自检

- [x] 座序图 h2 相对卡片水平居中；L1 下拉仍在右侧且不挤偏标题；**无**企业筛；不与 browse Cascader 联动
- [x] 902 命名用例 `902-seatmap-l1-dropdown` / `902-no-enterprise-filter` 仍绿（布局断言改为 909 居中）
- [x] 各 L3（含「未分类数据」）count = 当前筛选条件下该分类全部产品数；触底 loadMore **不得**让 count 变大
- [x] 创建写入 `create_by`+`update_by`（会话用户）；后续改刷新 `update_by`，**不**改 `create_by`；客户端 body 无法覆盖
- [x] 预览只读展示创建人/操作人（显示名优先，回退 userId）
- [x] 无新 Flyway / `**/sql/**`

## 自测命令与结果

```text
pnpm exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts
  (cwd: frontend)
→ Test Files  2 passed (2)
→ Tests  49 passed (49)
→ exitCode 0

pnpm exec vitest run src/features/catalog/browse/CatalogBrowsePage.spec.ts -t "909-audit"
  (cwd: frontend)
→ Tests  1 passed | 16 skipped
→ exitCode 0

mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=ProductEditorIntegrationTest,ListProductsFilterSortIntegrationTest" test
→ Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
→ BUILD SUCCESS
→ exitCode 0
```

说明：`CatalogBrowsePage.spec.ts` 全文件另有 1 条既有失败（`canWriteProduct('ADMIN')` 仍期望 false，与 SNAP-WSC-008 ADMIN 可写本企业不一致）。**非本 hotfix 引入**，未改 RBAC 断言。909 命名用例单独跑为绿。

### 具名用例

| 用例名 | 文件 | 断言 |
|---|---|---|
| `909-seatmap-title-center` | `CirculationSeatMap.spec.ts` | h2 `left:50%` / `translateX(-50%)`；L1 仍在；无 space-between |
| `902-seatmap-l1-dropdown` | 同上 | title-row、L1 Select、`l1CategoryId` query（布局已按 909 覆盖） |
| `902-no-enterprise-filter` | 同上 | 无 enterprise UI/query |
| `909-l3-count-full-total` | `useCatalogBrowse.spec.ts` | 服务端 totals 不随 page1/page2 已加载条数变大；loadMore 后 count 不变 |
| `l3Counts_fullTotal_independentOfPageSize` | `ListProductsFilterSortIntegrationTest.java` | pageSize=2 时 L3=3、未分类=2，翻页不变 |
| `909-audit-createBy-updateBy` | `CatalogBrowsePage.spec.ts` | 预览创建人/操作人只读 |
| `audit_createBy_updateBy_ignoresClientBody_andPreservesCreator` | `ProductEditorIntegrationTest.java` | 伪造 body 无效；创建双方均为提供方；ADMIN 更新后 createBy 不变、updateBy 为管理员 |

## denyModify 自检

- [x] 未改 `tests/e2e/playwright.config.ts`、`tests/e2e/specs/p0-wsc-v1.4.spec.ts`
- [x] 未改 `**/sql/**`
- [x] 未改 `ai/runs/**/state.yaml`、`events.jsonl`
- [x] 无企业管理页；座序图无企业筛；浏览无 `industryCategory` 筛
- [x] 未改 RBAC / 拦截器授权模型

## scope

- writeSet 内文件已变更；`CatalogProduct`/`CatalogEntityMapper` 按上表理由未改
- 导入写路径复用 editor create，未另改 import 授权
- 未标 VERIFIED
