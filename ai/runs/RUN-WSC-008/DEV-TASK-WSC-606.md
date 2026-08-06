# DEV-TASK-WSC-606

```yaml
taskId: TASK-WSC-606
runId: RUN-WSC-008
actorInstance: developer-wsc-606-r2
kind: HOTFIX
status: READY_FOR_REVIEW
round: 2
completedAt: 2026-08-06T16:00:00+08:00
basedOn:
  - planning/tasks/REV-TASK-WSC-606.md
  - FIND-WSC-606-R1-001
reqs:
  - REQ-UX-004
  - DEC-WSC-006
blocksRelease: true
```

> 交付说明落于 `ai/runs/RUN-WSC-008/`。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## Round 2 — 回应 FIND-WSC-606-R1-001

```yaml
actorInstance: developer-wsc-606-r2
respondsTo: code-reviewer-wsc-606-r1 REQUEST_CHANGES
findingClosed: FIND-WSC-606-R1-001
```

### objective（R2）

关闭 **FIND-WSC-606-R1-001**：适配 V1.4 E2E 场景 2a 与 606 用户管理弹框化 UX（先开创建弹框再填表；经「编辑角色」进入弹框后再操作 `user-row-role`），并复跑 `test:p0-v14`。

### filesChanged（R2）

| 路径 | 说明 |
|---|---|
| `tests/e2e/specs/p0-wsc-v1.4.spec.ts` | 场景 2a：`openUserCreateDialog` → 填表/`user-create`；`editUserRoleViaDialog` → `user-edit-role` → 弹框内 `user-row-role` + 保存；断言行内 `user-row-role-label` |
| `tests/e2e/helpers/v14-selectors.ts` | 新增 `openUserCreateDialog` / `editUserRoleViaDialog`；导出 `ROLE_LABEL` 供角色文案断言 |
| `tests/e2e/reports/p0-wsc-v1.4/**` | 本轮开发自跑 Playwright 报告（html + junit） |

未改：`UsersAdminPage.vue`（既有 testid 已足够，无需补洞）；其它 frontend feature；backend；contracts；auth；sql；v1.1/v1.3 e2e；`state.yaml` / `events.jsonl`。

### FIND-WSC-606-R1-001 关闭证据

| closeWhen | 证据 |
|---|---|
| 更新场景 2a 选择器步骤 | 创建：`user-open-create` → `user-create-dialog` 内填 `user-username`/`user-password`/`user-display-name`/`user-role` → `user-create`；改角色：`user-edit-role` → `user-edit-role-dialog` 内 `user-row-role` → `user-edit-role-save`；行内断言 `user-row-role-label`（文案「数据提供方」） |
| 复跑 V1.4 P0 | 见下方命令；**exitCode=0**；15 passed（含 2a） |

> 正式门禁仍须独立 tester 重跑；本轮仅为 developer 自跑冒烟，**未标 VERIFIED**。

### 自测命令与结果（R2）

```text
cd tests/e2e
$env:E2E_RUN='1'
pnpm run test:p0-v14
```

| 项 | 值 |
|---|---|
| 命令 | `pnpm run test:p0-v14`（`E2E_RUN=1`；baseURL `http://127.0.0.1:5173`；复用已起前后端 5173/8080） |
| 结果 | **15 passed**（含 `2a) ADMIN 用户管理 CRUD 抽样`） |
| exitCode | **0** |
| 报告 | `tests/e2e/reports/p0-wsc-v1.4/`（html + `junit.xml`） |
| 用时 | ~33.8s |

---

## Round 1 交付（保留）

```yaml
actorInstance: developer-wsc-606
completedAt: 2026-08-06T15:45:00+08:00
```

## objective

V1.4 发布前 UX hotfix：数据目录可滚动、用户管理弹框化与 wsc-surface、目录维护去「返回目录」、行业分类下拉对齐 GB/T `INDUSTRY_CATEGORY_OPTIONS` + 后端 `industryCategory` 过滤。

## filesChanged

| 路径 | 说明 |
|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | 解除页面 `overflow:hidden` 高度锁死；`.bi-pane` 固定视口预算保留列表触底分页；行业下拉改 GB/T |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` | `industryFilterId`→`industryCategory`；query 发 `industryCategory`；移除 L2/L3 树作该筛选项 |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` | 更新默认 filters / 新增 industryCategory query 断言 |
| `frontend/src/features/users/UsersAdminPage.vue` | 创建弹框；编辑角色按钮+弹框；wsc-surface 白卡；去除 sys_user 副标题 |
| `frontend/src/features/users/UsersAdminPage.spec.ts` | 606 弹框/按钮断言；壳层断言对齐 V1.4 my-products |
| `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue` | 去除「← 返回目录」 |
| `backend/.../CatalogBrowseController.java` | GET `/products` 增加 `industryCategory` query |
| `backend/.../CatalogBrowseService.java` | 按产品字段 `industryCategory` 精确过滤 |
| `backend/.../CatalogBrowseIntegrationTest.java` | `listProducts_filterByIndustryCategory_gbtMenlei` |
| `contracts/openapi/openapi.yaml` | 仅增 `industryCategory` query（**未 bump** `info.version` 2.2.0） |

未改：`CirculationSeatMap` / L2Distribution* 实现体；auth/login；sql；e2e；`frontend/src/api/catalog.ts`（`listProducts` 已接受任意 query Record）；`state.yaml` / `events.jsonl`。

## acceptance 勾选

| # | 项 | 结果 |
|---|---|---|
| 1 | 数据目录可滑动；列表触底分页仍合理 | ✅ 页面允许增高滚动；`.bi-pane`/`overflow:auto` 保留触底 + `fillUntilScrollable` |
| 2a | 用户创建改为弹框 | ✅ `user-open-create` → `user-create-dialog` |
| 2b | 操作栏「编辑角色」按钮（非仅行内 select） | ✅ `user-edit-role` → 弹框内 `user-row-role` |
| 2c | 白色卡片 / `wsc-surface` | ✅ 页头 + 列表卡定义 `.wsc-surface` |
| 2d | 去除副标题「创建账号并配置角色（持久化 sys_user）」 | ✅ |
| 3 | 目录维护去除「← 返回目录」 | ✅ |
| 4 | 行业分类下拉 = `INDUSTRY_CATEGORY_OPTIONS`；筛选对齐 `industryCategory` | ✅ 前后端 + openapi query；未 bump VERSION |

## 自测命令与结果

### Frontend Vitest

```text
cd frontend
pnpm exec vitest run src/features/users/UsersAdminPage.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts src/features/catalog/browse/CatalogBrowsePage.spec.ts
```

| 项 | 值 |
|---|---|
| 结果 | **33 passed**（UsersAdmin 4 + useCatalogBrowse 23 + CatalogBrowsePage 6） |
| exitCode | **0** |

### Backend

```text
cd backend
mvn -pl app/data-chain-service "-Dtest=CatalogBrowseIntegrationTest#listProducts_filterByIndustryCategory_gbtMenlei" test -q
```

| 项 | 值 |
|---|---|
| 结果 | **PASS** |
| exitCode | **0** |

## contracts / VERSION

- 仅在 `GET /catalog/products` 增加可选 query `industryCategory`（additive、向后兼容）。
- **未 bump** `contracts/openapi/openapi.yaml` `info.version`（保持 `2.2.0`）。

## scope / 备注（R1；E2E 断裂已由 R2 关闭 FIND-001）

- ~~E2E 场景 2a 选择器与弹框 UX 断裂、本轮未改 e2e~~ → **已由 Round 2 适配并自跑 `test:p0-v14` exitCode=0**。
- 种子库部分产品 `business_category` 仍为缩写（如「卫生」），与 GB/T 全文枚举不完全一致；经编辑器写入的合法门类可被新过滤命中（集成测试已覆盖创建+筛选）。
