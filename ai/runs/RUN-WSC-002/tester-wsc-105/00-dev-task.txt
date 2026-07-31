# DEV-TASK-WSC-105

```yaml
taskId: TASK-WSC-105
actorInstance: developer-wsc-105
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T17:31:00+08:00
reqs:
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-CHAIN-001
```

## 摘要

在 `backend/app/data-chain-service`（`com.shdata.datachain.catalog.editor` / `chain`）与 `frontend/src/features/catalog/{detail,editor}`、`frontend/src/features/chain` 落地：详情展示三级路径；编辑页空间/行业/子类级联选择；提交挂 `l3CategoryId` 并产生存证版本；快照含 `categoryPath` + `categoryPathParts`；回归 OQ-004 / DEC-001/002 与 §4 普通用户无产品写。未改 `contracts/**`、`frontend/src/api/**`、`catalog/maintenance/**`。

## 变更文件列表

### 后端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `.../catalog/editor/ProductEditorService.java` | REQ-CAT-005 / CHAIN-001 | 强制 L3；`ERR_CATEGORY_LEAF_REQUIRED`；三级路径；快照 `categoryPathParts`；DEC-001/002 |
| `.../catalog/editor/package-info.java` | — | 对齐 TASK-WSC-105 |
| `.../catalog/detail/package-info.java` | REQ-CAT-004 | 详情包约定（GET 仍由 browse） |
| `.../chain/InMemoryChainStore.java` | REQ-CHAIN-001 | 种子快照三级路径 + parts |
| `.../chain/package-info.java` | — | 对齐 TASK-WSC-105 |
| `.../test/.../ProductEditorIntegrationTest.java` | REQ-CAT-005 | L3 新建/递增/冲突/格式/存证失败回滚/缺叶/普通用户 403 |
| `.../test/.../ChainApiIntegrationTest.java` | REQ-CHAIN-001 | 快照三级路径断言；H2 测试配置对齐 |

### 前端（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/.../editor/composables/useProductEditor.ts` | REQ-CAT-005 | L1/L2/L3 级联；提交 `l3CategoryId`；OQ-004 |
| `frontend/.../editor/ProductEditorPage.vue` | REQ-CAT-005 | 三级选择器 UI；无写权限跳转 |
| `frontend/.../editor/composables/useProductEditor.spec.ts` | REQ-CAT-005 | 缺 L3 / OTHER / 级联清除 |
| `frontend/.../detail/ProductDetailPage.vue` | REQ-CAT-004 | 分类路径（三级）展示；编辑仅 `productWriteVisible` |
| `frontend/.../detail/composables/useProductDetail.spec.ts` | REQ-CAT-004 | 三级路径加载 |
| `frontend/.../chain/composables/useChainPage.ts` | REQ-CHAIN-001 | `categoryPathParts` 视图类型；`formatCategoryPath` |
| `frontend/.../chain/ChainPage.vue` | REQ-CHAIN-001 | 快照三级路径与 parts 展示 |
| `frontend/.../chain/composables/useChainPage.spec.ts` | REQ-CHAIN-001 | 路径格式化单测 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-004** | 详情「分类路径（三级）」只读展示；编辑入口仍受 `productWriteVisible`（普通用户无编辑） |
| **REQ-CAT-005** | 编辑器三级级联；提交 `l3CategoryId`；新建 v1 / 编辑递增；编码格式/冲突；OTHER 无 typeSpecific（OQ-004）；适配失败无半成品 |
| **REQ-CHAIN-001** | 存证快照含三级 `categoryPath` + `categoryPathParts`；按 versionId 可读；种子与新建路径一致 |

## 已知边界 / 后续任务

- GET 产品详情仍由 browse 提供；本任务未改 `catalog/browse/**`。
- `frontend/src/api/chain.ts` 尚未展开 `categoryPathParts` 字段类型（denyModify）；feature 层补充视图类型。
- 目录维护属 TASK-WSC-106；本任务**未触碰** `catalog/maintenance/**`。

## 自测命令与结果

### 后端

```text
mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.editor.ProductEditorIntegrationTest,com.shdata.datachain.chain.ChainApiIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `ProductEditorIntegrationTest` | 5 | **PASSED** |
| `ChainApiIntegrationTest` | 6 | **PASSED** |
| 合计 | 11 | **BUILD SUCCESS** |

覆盖：L3 挂载与路径；OQ-004；版本递增与旧快照可读；编码冲突/格式；`ERR_CATEGORY_LEAF_REQUIRED`；存证失败无半成品；普通用户写 403；种子快照三级路径。

### 前端

```text
cd frontend
pnpm exec vitest run src/features/catalog/editor/composables/useProductEditor.spec.ts src/features/catalog/detail/composables/useProductDetail.spec.ts src/features/chain/composables/useChainPage.spec.ts
pnpm typecheck
pnpm lint
pnpm build
```

| 命令 | 结果 |
|---|---|
| vitest（editor/detail/chain） | **PASSED**（9 tests） |
| `pnpm typecheck` | **PASSED** |
| `pnpm lint` | **PASSED**（placeholder exit 0） |
| `pnpm build` | **PASSED** |

## SCOPE / denyModify

- 未修改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`
- **未触碰** `frontend/src/features/catalog/maintenance/**`、`backend/.../catalog/maintenance/**`（及 browse/admin/import）
- 未与 TASK-WSC-106 混写
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer；未自行标记 VERIFIED
