# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-305-R2
taskId: TASK-WSC-305
round: 2
decision: APPROVE
actorInstance: code-reviewer-wsc-305
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
reviewedAt: 2026-08-02T19:31:00+08:00
priorRound: REV-CODE-TASK-WSC-305-R1
basedOn:
  - planning/tasks/TASK-WSC-305.md
  - planning/tasks/DEV-TASK-WSC-305.md
  - planning/tasks/REV-TASK-WSC-305.md (R1)
  - product/requirements/SNAP-WSC-004.md
  - frontend/src/features/catalog/browse/CatalogBrowsePage.vue
  - frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts
  - frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts
mustDifferFrom: developer-wsc-305
```

## Round 2 结论

**APPROVE** — 本轮 **P0=0，P1=0**。FIND-WSC-305-R1-001 已闭环：`needsMoreContentToScroll` / `shouldAutoLoadMore` / `fillUntilScrollable`（停滞停止 + `MAX_AUTO_FILL_PAGES=20`）+ 页面 `ensureListFilled`（`fillInFlight`、init 后、watch、`ResizeObserver`）。布局封顶与触底 `loadMore`、无页码器、加载文案仍成立。变更 ⊆ `browse/**` writeSet。审查复跑 Vitest **23 PASSED**。进入独立 tester 门禁；**不**代标人工/E2E / VERIFIED。

## Round 2 Findings

| id | severity | R2 状态 | 证据 |
|---|---|---|---|
| FIND-WSC-305-R1-001 | P1 | **CLOSED** | `fillUntilScrollable` + `ensureListFilled`；Vitest：未溢出续载 / !hasMore / 停滞 / maxPages |
| FIND-WSC-305-R1-002 | P2 | OPEN（不阻塞） | 仍无页面挂载测；纯函数 + fill 循环已覆盖；接线交 tester |
| FIND-WSC-305-R1-003 | P3 | **CLOSED** | `listScrollEl` 用于度量与 ResizeObserver |
| FIND-WSC-305-R1-004 | P3 | **CLOSED** | DEV 变更表已枚举 import-entry 等 browse 交付文件 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## Round 2 核对

| 项 | 结果 |
|---|---|
| writeSet ⊆ `frontend/src/features/catalog/browse/**`（+ DEV） | PASS |
| 未改 contracts/api/backend/layouts/shell/router/e2e/other features | PASS |
| 布局：pane 自滚动 + `@scroll` 触底 | PASS |
| 未溢出且 hasMore → 自动续载 | PASS（FIND-001） |
| 防并发 / 停滞 / 上限 | PASS（`fillInFlight`；loaded 不增 break；`maxPages`） |
| 无页码器；已加载 x/共 y；加载中/已全部 | PASS |
| Vitest browse | **23 PASSED**（`useCatalogBrowse` 19 + import entry 4） |

### 审查复跑证据（R2）

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse --reporter=default
# ✓ useCatalogImportEntry.spec.ts (4)
# ✓ useCatalogBrowse.spec.ts (19)
# Test Files  2 passed | Tests  23 passed
```

### 隔离声明（R2）

- 审查者未修改被审业务代码、DEV/TASK、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 E2E/VERIFIED；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

### 计数（R2 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 1（FIND-002，不阻塞） |
| P3 | 0 |

---

## Round 1（历史；REQUEST_CHANGES）

```yaml
reviewId: REV-CODE-TASK-WSC-305-R1
taskId: TASK-WSC-305
round: 1
decision: REQUEST_CHANGES
actorInstance: code-reviewer-wsc-305
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
reviewedAt: 2026-08-02T19:26:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-305.md
  - planning/tasks/DEV-TASK-WSC-305.md
  - product/requirements/SNAP-WSC-004.md
  - product/requirements/SNAP-WSC-002.md (REQ-CAT-001)
  - product/requirements/SNAP-WSC-003.md (REQ-UX-004)
  - frontend/src/features/catalog/browse/CatalogBrowsePage.vue
  - frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts
  - frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts
mustDifferFrom: developer-wsc-305
```

## 结论（R1）

**REQUEST_CHANGES** — 本轮 **P0=0，P1=1**。

布局根因修复方向正确：`.catalog-browse` 由仅 `min-height` 改为视口高度约束 + `overflow:hidden`，`.bi-pane`/` .pane` 使用 `min-height:0` + `overflow:auto`，使 `@scroll` / `onListScroll` 落在列表 pane 上；抽出 `isNearScrollBottom`；无页码器；「已加载 x / 共 y」「加载中…」「已加载全部」保留；writeSet 未越界；审查复跑 browse Vitest **18 PASSED**。

但验收要求「桌面常见分辨率下…滚近底部自动请求下一页」在 `pageSize=10` + 约束后的较高列表 pane 下存在高概率 **首屏内容撑不满 pane → 无滚动 → `loadMore` 永不触发** 的缺口（无 sentinel / 无「未溢出且 hasMore 则继续拉取」），故不得 APPROVE。

## Findings（R1；R2 已闭环 P1/P3）

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-305-R1-001 | P1 | OPEN→R2 CLOSED | `pageSize` 默认 10；1080p 列表 pane 常不溢出 → `@scroll` 不触发 | 未溢出且 hasMore 自动续载 + Vitest | REQ-CAT-001, REQ-UX-004 |
| FIND-WSC-305-R1-002 | P2 | OPEN | 无页面级 `@scroll` 挂载测 | 可选挂载测或交 tester | TASK-WSC-305 testScope |
| FIND-WSC-305-R1-003 | P3 | OPEN→R2 CLOSED | `listScrollEl` 仅绑 ref | 删除或用于度量 | 可维护性 |
| FIND-WSC-305-R1-004 | P3 | OPEN→R2 CLOSED | DEV 未完整枚举 import-entry | DEV 补录清单 | SCOPE |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=1** → **REQUEST_CHANGES**（见文首 R2 APPROVE）。

## 核对清单（R1 当时）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| HOTFIX 声明改动 ⊆ `frontend/src/features/catalog/browse/**`（+ DEV） | PASS |
| 未改 `contracts/**`、`frontend/src/api/**`、`backend/**` | PASS |
| 未改 layouts/shell/router/e2e/other catalog features | PASS |
| `mustDifferFrom: developer-wsc-305` | PASS |

### 2–5（R1 摘要）

布局自滚 / 触底接线 / 无页码 / 文案：PASS；首屏未溢出续载：**FAIL**（FIND-001）；Vitest 当时 **18 PASSED**，缺 fill 覆盖。

### 审查复跑证据（R1）

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse --reporter=default
# Tests  18 passed
```

### 隔离声明（R1）

- 审查者未修改被审业务代码、DEV/TASK、`state.yaml`、`events.jsonl`。
- **decision: REQUEST_CHANGES**（R1；见文首 Round 2 APPROVE）。

### 计数（R1 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 1 |
| P2 | 1 |
| P3 | 2 |
