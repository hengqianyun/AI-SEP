# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-306-R1
taskId: TASK-WSC-306
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-306
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-005
reviewedAt: 2026-08-02T21:36:55+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/tasks/TASK-WSC-306.md
  - planning/tasks/DEV-TASK-WSC-306.md
  - frontend/src/features/catalog/browse/CatalogBrowsePage.vue
  - frontend/src/features/catalog/browse/CatalogBrowsePage.spec.ts
  - frontend/src/layouts/WorkbenchLayout.vue
  - frontend/src/features/auth/composables/useCanWrite.ts (read-only)
mustDifferFrom: developer-wsc-306
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。

HOTFIX 四项验收在实现与 writeSet 内均成立：

1. **高分辨率**：`.catalog-browse` 保留视口 `height`/`max-height` + `overflow:hidden`；去掉页底留白；`.bi-pane` 改为比例列 `minmax(0, 1.55fr) minmax(380px, 1fr)` 且 `flex:1`，列表 pane 自滚与触底/`fillUntilScrollable` 约束未破坏。
2. **新增产品**：页头 `header-actions` 内 `v-if="productWriteVisible"` + `catalog-open-create` → `goCreate` → `/catalog/products/new`；与 `productImportVisible` 导入按钮并排；USER 经 `canWriteProduct`/`canImportProduct` 皆不可见；`goCreate` 二次守卫。
3. **写入口卡片**：`WorkbenchLayout` 移除 `WriteEntryDemo` import 与挂载（非 CSS 隐藏）；侧栏/目录写路径不依赖该卡。
4. **sticky 无露缝**：`.pane.list` 顶 padding 置 0（`padding: 0 20px 20px`），顶内边距移入 sticky `.list-head`（`top:0`、不透明 `background`、`z-index:2`）。

变更 ⊆ writeSet（browse + WorkbenchLayout + DEV + 对应 spec）；未改 backend/contracts/api/components/editor|detail|import|product、`state.yaml`、`events`。审查复跑 browse Vitest **27 PASSED**。进入独立 tester 门禁；**不**代标人工/E2E / VERIFIED。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-306-R1-001 | P2 | OPEN（不阻塞） | `CatalogBrowsePage.spec.ts` 以源码字符串断言权限/`sticky`/布局/卸载；无角色切换挂载测与视觉露缝断言 | 可选挂载测，或交独立 tester 人工核对 | TASK-WSC-306 testScope |
| FIND-WSC-306-R1-002 | P3 | OPEN（不阻塞） | `WriteEntryDemo.vue` 仍留存；`import/INTEGRATION.md` 仍提及壳层演示入口（import 在 denyModify，本任务未改） | 后续清理死代码或更新文档 | REQ-UX-004 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## 核对

| 项 | 结果 |
|---|---|
| writeSet ⊆ browse/** + WorkbenchLayout + DEV + spec | PASS |
| 未改 denyModify（backend/contracts/api/components/editor|detail|import|product/state/events） | PASS |
| `productWriteVisible` 门控新增按钮；导入仍 `productImportVisible` | PASS |
| USER：`canWriteProduct`/`canImportProduct` = false | PASS（composable + 源码 v-if） |
| 路由 `/catalog/products/new` + `goCreate` 守卫 | PASS |
| `WorkbenchLayout` 无 `WriteEntryDemo` | PASS |
| sticky：list pane 无顶 padding；head sticky + 顶 padding + 底色 + z-index | PASS |
| 高屏：视口高度约束保留；bi-pane 比例列 + flex 填满 | PASS |
| Vitest `src/features/catalog/browse` | **27 PASSED** |

### 审查复跑证据

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse --reporter=default
# ✓ CatalogBrowsePage.spec.ts (4)
# ✓ useCatalogImportEntry.spec.ts (4)
# ✓ useCatalogBrowse.spec.ts (19)
# Test Files  3 passed | Tests  27 passed
```

### 隔离声明

- 审查者 `code-reviewer-wsc-306` ≠ `developer-wsc-306`；未修改被审业务代码、DEV/TASK、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 E2E/VERIFIED；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

### 计数（open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 1（FIND-001，不阻塞） |
| P3 | 1（FIND-002，不阻塞） |
