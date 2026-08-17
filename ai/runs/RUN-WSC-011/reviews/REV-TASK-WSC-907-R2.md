# 代码审查报告 — TASK-WSC-907 Round 2

```yaml
reviewId: REV-TASK-WSC-907-R2
taskId: TASK-WSC-907
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 2
role: codeReviewer
actorInstance: code-reviewer-wsc-907-r2
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 1
reviewedAt: 2026-08-17T14:40:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-907.md（Round 1 REQUEST_CHANGES；FIND-WSC-907-001 closeWhen）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-907.md（actorInstance=developer-wsc-907-r1；Round 1 修复节）
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-907 writeSet/denyModify）
  - 实地：useCatalogMaintenance.ts、CatalogMaintenancePage.vue、useCatalogMaintenance.spec.ts、CatalogMaintenanceUx.spec.ts
  - 对照未改：WorkbenchLayout.vue L315 `<RouterView />` 无 key；frontend/src/api/catalog.ts PUT/POST 仍无 query scope
mustDifferFrom:
  - code-reviewer-wsc-907
  - developer-wsc-907
  - developer-wsc-907-r1
closedFinding: FIND-WSC-907-001
closedIssues:
  - FIND-WSC-907-001
openIssues:
  - FIND-WSC-907-002
riskTags: [maintenance-reuse, scope-isolation, enum-regression]
note: |
  本 REV 仅 codeReview Round 2 复审门禁；不得把 R1 结论当复审结论。
  未代 tester 宣称 PASS/VERIFIED；未改源码 / state.yaml / events.jsonl / R1 REV。
  FIND-002 仍为 P2，不因此 REQUEST_CHANGES；907 未为关闭它改 frontend/src/api/**。
```

## 结论

**APPROVE** — **P0=0，P1=0**。Round 1 唯一阻塞项 **FIND-WSC-907-001** 已按 closeWhen 三项修复并关闭：writeSet 内（页/composable）`scope` 响应式驱动 list 与 PUT/POST；`props.scope` 变化时重新 `init`、清选择、重跑页级 guard；同一 composable 实例 `full → myCatalog` 后 list query 与写 URL 均为 `myCatalog`。本轮无新 P0/P1。开放 **P2×1**（FIND-WSC-907-002）不阻塞进入独立 tester 门禁。

**未代 tester 宣称通过**；未标 VERIFIED。未改 `WorkbenchLayout.vue`、未改 `frontend/src/api/**`。

## FIND-WSC-907-001 closeWhen 核验

| closeWhen 项 | 结果 | 证据 |
|---|---|---|
| writeSet 内（页/composable，**禁止改 layout**）：`scope` 响应式驱动 list 与 PUT/POST | **PASS** | composable 接受 `MaybeRefOrGetter`，`currentScope = computed(() => toValue(scopeSource))`；`loadEntries` 传 `scope: currentScope.value`；PUT/POST `?scope=${currentScope.value}`。页传 `() => props.scope`（禁止 `props.scope` 快照）。`WorkbenchLayout.vue` L315 仍为 `<RouterView />` **无** `:key` |
| `props.scope` 变化时重新 init、清选择、重跑页级 guard | **PASS** | composable `watch(currentScope)`：`page=1`、`clearSelection()`、`cancelEdit()`、`void init()`。页 `watch(() => props.scope)` 调用 `canEnterMaintenancePage(next, role)`，失败则 `router.replace('/catalog')` |
| 测试：同一 composable/页面实例 `full → myCatalog` 后，list query 与写 URL 均为 `myCatalog`（不得只测两个独立实例） | **PASS（制品存在）** | `useCatalogMaintenance.spec.ts` 具名用例用**单一** `useCatalogMaintenance(ref)`，`scope.value` 从 `full` 改为 `myCatalog` 后断言 list `scope: 'myCatalog'`、PUT `/entries/p1?scope=myCatalog`、POST `/entries/batch?scope=myCatalog`、选择被清空。另保留两独立实例用例，**不是唯一覆盖** |

**实地 excerpt**（`useCatalogMaintenance.ts`）：

```110:113:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts
export function useCatalogMaintenance(
  scopeSource: MaybeRefOrGetter<MaintenanceScope> = 'full',
) {
  const currentScope = computed(() => toValue(scopeSource))
```

```174:176:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts
      const res = await listMaintenanceEntries({
        scope: currentScope.value,
        status: statusTab.value,
```

```319:321:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts
      await apiRequest<MaintenanceEntry>(
        `/catalog/maintenance/entries/${encodeURIComponent(editingId.value)}?scope=${currentScope.value}`,
        {
```

```353:353:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts
      }>(`/catalog/maintenance/entries/batch?scope=${currentScope.value}`, {
```

```375:381:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts
  /** 路由复用时 props.scope 变化须重拉列表并清选择（FIND-WSC-907-001） */
  watch(currentScope, () => {
    page.value = 1
    clearSelection()
    cancelEdit()
    void init()
  })
```

**实地 excerpt**（`CatalogMaintenancePage.vue`）：

```30:31:frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue
/** 传 getter，避免 /catalog/maintenance ↔ /my-catalog 复用实例时 scope 快照陈旧（FIND-WSC-907-001） */
const m = useCatalogMaintenance(() => props.scope)
```

```84:91:frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue
watch(
  () => props.scope,
  (next) => {
    if (!canEnterMaintenancePage(next, role.value)) {
      void router.replace('/catalog')
    }
  },
)
```

全文件已无 `useCatalogMaintenance(props.scope)`（setup 快照）。`pageTitle` 仍为 `computed(() => props.scope === ...)`，与 list/写路径现同属响应式 `props.scope`，R1「页头变、API 仍旧 scope」缺口已闭合。

**同实例测试 excerpt**（`useCatalogMaintenance.spec.ts` L112–150）：

- `const scope = ref<MaintenanceScope>('full')` + **一次** `useCatalogMaintenance(scope)`（非两个独立实例）
- `await m.init()` 后 list 为 `scope: 'full'`；勾选 `p1`
- `scope.value = 'myCatalog'` → `waitFor` 最后一次 list 为 `scope: 'myCatalog'`；`selectedCount === 0`
- `saveEdit` → `'/catalog/maintenance/entries/p1?scope=myCatalog'`（PUT）
- `saveBatch` → `'/catalog/maintenance/entries/batch?scope=myCatalog'`（POST）

页级接线另由 `CatalogMaintenanceUx.spec.ts` 源码断言：`useCatalogMaintenance(() => props.scope)` + `watch(() => props.scope` + `canEnterMaintenancePage`。

本实例为核对 closeWhen 测试制品曾执行上述两份 spec（11 passed）；**此为审查取证，不是 tester 门禁，不标 PASS/VERIFIED**。

## 范围：禁止改 layout / 禁止为 P2 改 api

| 项 | 结果 | 证据 |
|---|---|---|
| 未改 `WorkbenchLayout.vue`（本轮 FIND-001 修复） | **PASS** | R1 根因是 layout `RouterView` 无 key；907 **denyModify** layout。实地 L315 仍 `<RouterView />`，**未**加 `:key`。工作树该文件脏差异属 TASK-WSC-906 导航面，非本轮 907-r1 filesChanged |
| 未为 FIND-002 改 `frontend/src/api/**` | **PASS** | `updateMaintenanceEntry` / `batchUpdateMaintenanceEntries` 仍无 query `scope`（`catalog.ts` L343–361）。907 写路径继续 `apiRequest` 附 `?scope=`。api 工作树脏差异属 903 生成 client（list 已有 `scope`），非本轮为关 P2 而改 |

修复落点均在 writeSet：`useCatalogMaintenance.ts`、`CatalogMaintenancePage.vue`、对应 spec、`CatalogMaintenanceUx.spec.ts`。

## Round 1 Findings 处置

| id | sev | R1 | R2 status | 证据 / 说明 |
|---|---|---|---|---|
| FIND-WSC-907-001 | **P1** | OPEN | **CLOSED** | closeWhen 三项均满足；见上表。计入 `closedFinding` / `closedIssues` |
| FIND-WSC-907-002 | P2 | OPEN | **OPEN** | 生成 PUT/POST client 仍无 `scope`；907 workaround 正确。**本轮不必改 api**；不阻塞 APPROVE |

## Round 2 新 Findings

无。本轮未引入新 P0/P1/P2/P3。

非阻塞观察（不升格）：页级 `watch(props.scope)` 在 guard 失败时 `replace`，composable `watch(currentScope)` 仍会 `init()`；PROVIDER 深链 full 仍由 906 `beforeEnter` + 905 拦截器 403 fail-closed。不构成 P1。

## 决策依据

- FIND-WSC-907-001 closeWhen **全部满足** → 计入 `closedFinding` / `closedIssues`。
- 本轮 **P0=0，P1=0** → **APPROVE**（P2 FIND-002 不阻塞）。
- 进入独立 tester 后仍须执行 TASK-WSC-907 `testScope`；本 REV **不**宣称测试通过。
