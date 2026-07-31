# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-105-R1
taskId: TASK-WSC-105
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-105
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T17:42:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-105.md
  - planning/tasks/DEV-TASK-WSC-105.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。交付落在 `writeSet`：`frontend/src/features/catalog/{detail,editor}/**`、`frontend/src/features/chain/**` 与 `backend/.../catalog/{detail,editor}/**`、`backend/.../chain/**`（含对称测试）。对照 DEV 清单未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`、`catalog/maintenance/**`（工作区并存的 TASK-WSC-106 maintenance / 既有契约与 api 改动不归本轮）。详情三级路径、编辑 L1/L2/L3 级联与强制 `l3CategoryId`、存证版本递增、快照 `categoryPath`/`categoryPathParts`、OQ-004、DEC-001/002、普通用户写 403 均有实现与测试支撑。审查复跑后端 11 测与 vitest 9 测均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-105-R1-001 | P2 | `ProductEditorService` 对 OTHER 携带 `typeSpecific` 返回 `ERR_PRODUCT_CODE_FORMAT`（与编码格式语义同码）；集成测亦断言该码 | 后续契约扩展独立码（如 `ERR_TYPE_SPECIFIC_FORBIDDEN`）或在 `codes.yaml` 注明复用；客户端勿仅凭码名推断原因 | REQ-CAT-005, OQ-004 |
| FIND-WSC-105-R1-002 | P2 | `attestationFailure_noHalfProduct_noOrphanChain` 断言产品未写入成立，但孤儿链校验使用固定假 id `prod-should-not-exist`，未对 `nextProductId()` 实际分配 id 查 `chainStore` | 失败路径捕获已分配 `productId` 并断言 `chainStore.listByProductId(id)` 为空 | REQ-CAT-005, DEC-WSC-002 |
| FIND-WSC-105-R1-003 | P2 | `ProductEditorPage` 对 create/edit 共用组件且无 `RouterView` key；`useProductEditor(mode)` 在 setup 时固化 mode，路由在 `/new`↔`/:id/edit` 复用实例时 `watch(mode)` 会重载表单但 submit 仍走首次 mode | 将 mode 改为响应式参数，或给 `RouterView`/页面加 `key=route.name` | REQ-CAT-005 |
| FIND-WSC-105-R1-004 | P3 | 普通用户写禁仅集成覆盖 POST；PUT 依赖同一 `WriteAuthorizationInterceptor` PRODUCT 分支，无对称用例 | 可选补 PUT `/products/{id}` 的 USER→403 | REQ-CAT-005, §4 |
| FIND-WSC-105-R1-005 | P3 | `frontend/src/api/chain.ts` 未展开 `categoryPathParts`（denyModify）；feature 层以 `CatalogSnapshotView` 兜底，合理但类型与契约有漂移 | 后续允许改 api 层时同步 OpenAPI 类型 | REQ-CHAIN-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet`（detail/editor/chain + 对应测试） | PASS（对照 DEV 变更清单） |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**` | PASS（本任务交付面；工作区另有契约/api 改动不归本轮） |
| 未触碰 `catalog/maintenance/**` | PASS（并行 TASK-WSC-106；未计入本任务） |
| 未改 browse/admin/import/overview、`ai/rules/**`、`product/**`、`planning/approved/**` | PASS（本任务交付面） |

### 2. REQ / DEC / OQ

| 项 | 证据 | 结果 |
|---|---|---|
| REQ-CAT-004 详情三级路径 | `ProductDetailPage`「分类路径（三级）」；`useProductDetail.spec` | PASS |
| REQ-CAT-004 普通用户无编辑入口 | 编辑按钮受 `productWriteVisible` | PASS |
| REQ-CAT-005 三级级联 + 强制 L3 | `ProductEditorPage` cascade；`buildBody` 缺 L3 拦截；后端 `ERR_CATEGORY_LEAF_REQUIRED` | PASS |
| REQ-CAT-005 新建 v1 / 编辑递增 | 集成测 `latestVersionNo` 1→2；旧快照仍可读 | PASS |
| REQ-CAT-005 / DEC-001 编码格式与冲突 | `PRODUCT_CODE` 正则；`ERR_PRODUCT_CODE_FORMAT` / `ERR_PRODUCT_CODE_CONFLICT` | PASS |
| OQ-004 OTHER 无 typeSpecific | 前端不传；后端拒带 payload；UI 提示 | PASS |
| DEC-002 上链经适配层 | `ChainAttestationPort` / `SimulatedChainAttestationPort`；失败无半成品产品 | PASS |
| REQ-CHAIN-001 快照三级路径 + parts | `buildSnapshot` + 种子 `InMemoryChainStore`；Chain 页展示；集成断言 | PASS |
| 普通用户无产品写（403） | `ordinaryUser_productWriteForbidden` → `ERR_FORBIDDEN`；拦截器非仅前端 | PASS |

### 3. 架构 / 安全

| 项 | 结果 |
|---|---|
| Controller → Service → SeedStore / Chain 适配层（未穿透无关层） | PASS |
| 写权限依赖 `WriteAuthorizationInterceptor`（PRODUCT）+ UI 非唯一防护 | PASS |
| 无本任务密钥/连接串入仓；无本任务 SQL/契约改动 | PASS |
| 上链经 `ChainAttestationPort`，非直连链 SDK | PASS |

### 4. 测试覆盖（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| L3 挂载 / 缺叶 / OQ-004 | PASS — `ProductEditorIntegrationTest` + vitest editor |
| 版本递增与旧快照 | PASS — update 集成测 |
| 适配失败无半成品 | PASS — `attestationFailure_*`（孤儿链断言偏弱，见 FIND-002） |
| 快照按 versionId + 三级路径 | PASS — `ChainApiIntegrationTest#getSnapshot_byVersionId` |
| 普通用户 403 | PASS — POST 集成测 |
| 审查复跑 | 后端 11 tests PASSED；vitest 9 tests PASSED |

## 残余风险（交 tester）

- 工作区并存 TASK-WSC-106 maintenance、以及既有 `contracts/**` / `frontend/src/api/**` 改动；取证以 DEV 清单与 detail/editor/chain 为 105 基线。
- 详情 GET 仍由 browse 提供；若并行 browse 种子路径形状变化，需回归详情 `categoryPath`。
- create/edit 同组件复用时 mode 固化属边缘路径；主验收按独立进入 `/new` 与 `/:id/edit` 即可。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 2 |
