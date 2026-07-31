# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-103-R1
taskId: TASK-WSC-103
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-103
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T16:26:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-103.md
  - planning/tasks/DEV-TASK-WSC-103.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
  - design/decisions/DEC-WSC-003.md
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet`：`frontend/src/features/catalog/admin/**` 与 `backend/.../catalog/admin/**`（含对称测试）。未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**`、`catalog/browse/**`（本任务交付面；工作区另有并行 TASK-WSC-104 / 既有契约改动不归本轮）。三级 CRUD、挂载禁删（DEC-WSC-003）、有子节点禁删、全局至少保留一个三级、非管理员前端跳转 + 写 API `ERR_FORBIDDEN` 均有实现与测试支撑。审查复跑后端 8 测与 vitest 4 测均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-103-R1-001 | P2 | `CategoryAdminService#delete` 对「挂载产品」「有子分类」「全局最后一枚 L3」均返回 `ERR_CATEGORY_HAS_PRODUCTS`；契约/OpenAPI 示例将该码语义绑在挂载产品。`message`/`data.reason` 已区分，但仅看 `code` 的客户端可能误判 | 后续契约扩展独立码（如 leaf-required / has-children）或在 `codes.yaml` 注明多原因复用；客户端以 `reason` 为准 | REQ-CAT-006, DEC-WSC-003 |
| FIND-WSC-103-R1-002 | P2 | 「至少保留一个三级」集成路径在种子多 L3 下难触发；覆盖依赖 `CategoryAdminServiceTest#lastL3_globally_forbidden`（mock 仅一枚 L3）。DEV 已声明边界 | tester 按单测路径验收；或补可控夹具集成测 | REQ-CAT-006 |
| FIND-WSC-103-R1-003 | P3 | 非管理员 UI 不可达由 `CategoryAdminPage` `onMounted` + `categoryMaintainVisible` 实现；无页面级组件测（后端 403 已覆盖） | 可选：加页面测或 E2E 非管理员直链跳转 | REQ-CAT-006 |
| FIND-WSC-103-R1-004 | P3 | `dirty` 在输入时置位，成功 `saveRename`/`load` 后清零；用户改回原名不会自动取消「待保存」提示 | 可选：与 `renameMap` 对比原始名再算 dirty | REQ-CAT-006 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet`（`catalog/admin/**` + 对应测试） | PASS（对照 DEV 变更清单） |
| 未改 `contracts/**`、`frontend/src/api/**`、`**/sql/**` | PASS（本任务交付面） |
| 未改 `catalog/browse/**` | PASS（只读依赖 `CatalogBrowseSeedStore`；未列入 DEV 变更） |
| 未改 detail/editor/maintenance/import/overview/chain、`ai/rules/**`、`product/**`、`planning/approved/**` | PASS |

### 2. REQ-CAT-006 / DEC-WSC-003

| 项 | 证据 | 结果 |
|---|---|---|
| 三级分栏增改（空间/行业/子类） | `CategoryAdminPage` 三分栏；`useCategoryAdmin` L1/L2/L3 CRUD | PASS |
| 待保存提示 | `dirty` + `data-testid="dirty-hint"` | PASS |
| 保存后级联刷新 | 写成功后 `load()`；`selectL1`→`syncL2Selection` | PASS |
| 有挂载禁止删 + 明确原因 | `countMountedProducts`→409/`ERR_CATEGORY_HAS_PRODUCTS` + `reason`/`productCount`；集成测 L3/L2/L1 | PASS |
| 至少保留一个三级 | 全局 `countCategoriesByLevel("L3")<=1`；单测 mock | PASS |
| 有子节点禁删 | `countDirectChildren`；集成删 L2 有 L3→409；单测 | PASS |
| 非管理员不可达 | 页内 `router.replace('/catalog')`；集成 PROVIDER/USER→403 `ERR_FORBIDDEN` | PASS |

### 3. 架构 / 安全

| 项 | 结果 |
|---|---|
| Controller → Service → SeedStore（未穿透到无关层） | PASS |
| 写权限依赖会话拦截器（CATEGORY）+ UI 非唯一防护 | PASS（集成 403；前端门禁） |
| 无密钥入仓；无本任务 SQL/契约改动 | PASS |

### 4. 测试覆盖（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| CRUD 三级 | PASS — `crud_threeLevels`；前端 add L3 + reload |
| 禁止删除负例（挂载 / 子节点） | PASS — 集成 + `CategoryAdminServiceTest` |
| 非管理员 403 | PASS — `nonAdmin_cannotReachCategoryWrite` |
| 至少保留一个三级 | PASS — `lastL3_globally_forbidden`（单测） |
| 审查复跑 | 后端 8 tests PASSED；vitest 4 tests PASSED |

## 残余风险（交 tester）

- 工作区并存 TASK-WSC-104 browse / 契约等改动；取证时以 `catalog/admin` 与 DEV 清单为 103 基线，勿把 104 回归算进本任务。
- 挂载计数委托 `CatalogBrowseSeedStore#countMountedProducts`（含 L3）；若并行 104 变更种子形状，需回归挂载禁删断言。
- 「最后一枚 L3」以单测为主；E2E/集成勿假设种子仅一枚 L3。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2 |
| P3 | 2 |
