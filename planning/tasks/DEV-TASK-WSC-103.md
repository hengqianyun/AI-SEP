# DEV-TASK-WSC-103

```yaml
taskId: TASK-WSC-103
actorInstance: developer-wsc-103
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T16:14:00+08:00
reqs:
  - REQ-CAT-006
```

## 摘要

在 `backend/app/data-chain-service`（`com.shdata.datachain.catalog.admin`）实现三级分类（空间 L1 / 行业 L2 / 子类 L3）写 API：CRUD、有挂载禁止删（`ERR_CATEGORY_HAS_PRODUCTS`，DEC-WSC-003）、有子节点禁止删、全局至少保留一个三级。前端 `catalog/admin` 升级为三分栏维护面板（待保存提示、级联选择刷新、非管理员跳转目录）。未触碰 `catalog/browse/**` 与 TASK-WSC-104 writeSet；挂载计数委托既有 `CatalogBrowseSeedStore#countMountedProducts`（与并行 104 升级后的 L3 种子兼容）。

## 变更文件列表

### 后端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/catalog/admin/CategoryAdminService.java` | REQ-CAT-006 | L1/L2/L3 校验；删挂载/子节点/最后三级 → 409 `ERR_CATEGORY_HAS_PRODUCTS` |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/catalog/admin/CategoryAdminController.java` | REQ-CAT-006 | 注释对齐三级；端点不变 |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/catalog/admin/package-info.java` | — | 包说明对齐 103 |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/catalog/admin/CategoryAdminIntegrationTest.java` | REQ-CAT-006 | CRUD；挂载 L3/L2/L1 负例；非管理员 403 |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/catalog/admin/CategoryAdminServiceTest.java` | REQ-CAT-006 | 子节点禁删；挂载 L3；全局最后三级（mock）；非法 parent |

### 前端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/admin/CategoryAdminPage.vue` | REQ-CAT-006 | 三分栏（空间/行业/子类）；待保存提示；非管理员不可达 |
| `frontend/src/features/catalog/admin/composables/useCategoryAdmin.ts` | REQ-CAT-006 | L3 CRUD；L1→L2→L3 级联选择；删除错误 reason 展示 |
| `frontend/src/features/catalog/admin/composables/useCategoryAdmin.spec.ts` | REQ-CAT-006 | 加载三级、dirty、增 L3、删除拒绝原因 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-006** | 三分栏增改；待保存提示；保存后 `load()` 刷新级联列表；非管理员前端跳转 + 写 API `ERR_FORBIDDEN`；有挂载/子节点禁止删 `ERR_CATEGORY_HAS_PRODUCTS`（含 reason/productCount）；全局至少保留一个三级 |

## 已知边界 / 后续任务

- GET `/catalog/categories` 仍由 browse 提供（只读依赖）；本任务未改 browse。
- 种子 L3/产品挂载由并行 TASK-WSC-104 维护；admin 删除挂载检测调用共享 store。
- 「至少保留一个三级」为全局计数；种子已有多个挂载 L3 时，该分支主要由单测（mock 仅一枚 L3）覆盖。
- 未兼任 codeReviewer；未自行标记 VERIFIED。

## 自测命令与结果

### 后端

```text
mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.admin.CategoryAdminIntegrationTest,com.shdata.datachain.catalog.admin.CategoryAdminServiceTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `CategoryAdminIntegrationTest` | 3 | **PASSED** |
| `CategoryAdminServiceTest` | 5 | **PASSED** |
| **合计** | **8** | **BUILD SUCCESS** |

覆盖：三级 CRUD；挂载 L3/L2/L1 禁删；空树可删；非管理员 403；子节点禁删；全局最后三级；非法 L3 parent。

### 前端

```text
cd frontend
pnpm exec vitest run src/features/catalog/admin/composables/useCategoryAdmin.spec.ts
pnpm typecheck
```

| 命令 | 结果 |
|---|---|
| vitest `useCategoryAdmin.spec.ts` | **PASSED**（4 tests） |
| `pnpm typecheck` | **PASSED** |

## SCOPE / denyModify

- 未修改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`
- **未触碰** `frontend/src/features/catalog/browse/**` 与 `backend/**/catalog/browse/**`
- 未修改 maintenance/import/detail/editor/overview/chain、`ai/rules/**`、`product/**`、`planning/approved/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
