# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-402-R1
taskId: TASK-WSC-402
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-402
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-006
reviewedAt: 2026-08-03T10:42:30+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/tasks/TASK-WSC-402.md
  - planning/tasks/DEV-TASK-WSC-402.md
  - frontend/src/features/catalog/detail/ProductDetailPage.vue
  - frontend/src/features/catalog/detail/components/TypeSpecificReadonly.vue
  - frontend/src/features/catalog/detail/ProductDetailPage.spec.ts
  - frontend/src/features/catalog/editor/ProductEditorPage.vue
  - frontend/src/features/catalog/editor/ProductEditorPage.spec.ts
  - frontend/src/features/catalog/editor/composables/useProductEditor.ts
  - frontend/src/features/catalog/editor/utils/productCode.ts
mustDifferFrom: developer-wsc-402
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。

验收要点成立：

1. **去掉过窄 max-width**：详情 `.detail-page-body` 不再 `max-width: 1100px`；编辑 `.editor-body` / `.wide` 不再 `900px` / `1100px`。
2. **流体铺满**：详情/编辑根与主区均为 `width: 100%` + `min-width: 0`，与总览 401 方向一致；宽屏详情网格 4 列、编辑表单 3 列。
3. **小屏不回退**：详情保留 `@media (max-width: 900px|640px)`；编辑保留 `@media (max-width: 720px)`（含 cascade 单列）；`TypeSpecificReadonly` 同步保留小屏断点。
4. **OpenAPI 不挤爆**：API 仍挂 `wide`；`.type-block` / `.editor-form-card` / `.form-group` 设 `min-width: 0`；既有 `OpenApiEditorPanel` / `OpenApiReadonlyPanel` 内部已有 overflow/`min-width: 0`。
5. **未破坏 401 编码生成**：create 只读 +「自动生成」；`onProductTypeChange` 仍刷新编码；edit 保持编码；相关 vitest 通过。
6. **writeSet / denyModify**：布局变更落在 `detail/**`、`editor/**` 与对应 `*.spec.ts` + DEV；未改 `backend/**`、`contracts/**`、`frontend/src/api/**`、`portal/**`、`browse/**`、`overview/**`、`product/**`、`planning/approved/**`、`state`/`events`。

审查复跑 Vitest **30 PASSED**。进入独立 tester 门禁；**不**代标 VERIFIED。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-402-R1-001 | P3 | OPEN（不阻塞） | 布局断言为源码字符串 smoke（无挂载/视觉测）；未单独断言 `TypeSpecificReadonly` 宽屏 4 列与 `min-width: 0` | 可选补组件级断言或视觉回归 | REQ-UX-007 |
| FIND-WSC-402-R1-002 | P3 | OPEN（不阻塞） | `.editor-body.wide` 已不再加宽 max-width，仅约束 `.type-block`；语义偏遗留类名 | 可选重命名或注释即可，非功能缺陷 | REQ-CAT-005 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## 核对

| 项 | 结果 |
|---|---|
| 详情去掉 `max-width: 1100px`，主区 `width: 100%` | PASS |
| 编辑去掉 `900/1100` max-width，主区流体 | PASS |
| 宽屏网格扩列（详情 4 / 编辑 3）且小屏 media 保留 | PASS |
| API `wide` + type-block `min-width: 0`，OpenAPI 面板自带 overflow | PASS |
| 401 create 自动编码 UX / edit 不改码 | PASS（页面 + composable + productCode 测） |
| writeSet / denyModify | PASS |
| Vitest 相关套件 | **30 PASSED** |

### 审查复跑证据

```text
cd frontend
pnpm exec vitest run src/features/catalog/detail/ProductDetailPage.spec.ts \
  src/features/catalog/editor/ProductEditorPage.spec.ts \
  src/features/catalog/editor/utils/productCode.spec.ts \
  src/features/catalog/editor/composables/useProductEditor.spec.ts \
  src/features/catalog/detail/composables/useProductDetail.spec.ts \
  src/features/catalog/detail/utils/typeSpecificView.spec.ts --reporter=default
# Test Files  6 passed (6) | Tests  30 passed (30)
```

### 隔离声明

- 审查者 `code-reviewer-wsc-402` ≠ `developer-wsc-402`；未修改被审业务代码、DEV/TASK、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 VERIFIED；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

### 计数（open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 0 |
| P3 | 2（FIND-001/002，不阻塞） |
