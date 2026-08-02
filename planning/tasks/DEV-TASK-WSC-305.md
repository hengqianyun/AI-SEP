# DEV-TASK-WSC-305

```yaml
taskId: TASK-WSC-305
actorInstance: developer-wsc-305
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
status: READY_FOR_REVIEW
completedAt: 2026-08-02T19:30:00+08:00
reqs:
  - REQ-CAT-001
  - REQ-UX-004
basedOnReview: REV-CODE-TASK-WSC-305-R1
```

## 摘要

HOTFIX 目录触底分页：先修窗口滚动根因（视口高度约束 + pane 自滚），再按 **FIND-WSC-305-R1-001** 补齐「`pageSize=10` 撑不满 pane → 无滚动 → `loadMore` 永不触发」：首屏/追加后/`ResizeObserver` 若未溢出且 `hasMore` 则 `fillUntilScrollable` 自动续载（防并发/停滞/上限）。Vitest 覆盖未溢出续载。状态 **READY_FOR_REVIEW**。

## 根因与修复

| 轮次 | 问题 | 修复 |
|---|---|---|
| R0 | `.catalog-browse` 仅 `min-height` → 窗口滚动 | `height/max-height` + `overflow:hidden`；`.bi-pane`/` .pane` `min-height:0` + 列表 `overflow:auto` |
| R1 P1 | `pageSize=10` 常不溢出 → `@scroll` 不触发 | `needsMoreContentToScroll` / `shouldAutoLoadMore` / `fillUntilScrollable`；页面 `ensureListFilled`（watch + ResizeObserver + init 后） |

## 对审查 FIND 回应

| id | severity | 本轮 | 说明 |
|---|---|---|---|
| FIND-WSC-305-R1-001 | P1 | **FIXED** | 未溢出且 hasMore 自动续载至可滚或 !hasMore；`MAX_AUTO_FILL_PAGES=20`；停滞（loaded 不增）停止；`fillInFlight` 防并发 |
| FIND-WSC-305-R1-002 | P2 | 未做页面挂载测 | 可测纯函数 + fill 循环已覆盖；页面接线交 tester |
| FIND-WSC-305-R1-003 | P3 | **FIXED** | `listScrollEl` 用于度量/ResizeObserver |
| FIND-WSC-305-R1-004 | P3 | **FIXED** | 下方变更表完整枚举 browse 交付文件 |

## 变更（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | REQ-CAT-001 / REQ-UX-004 | 布局高度约束；`onListScroll`；`ensureListFilled` + ResizeObserver；导入入口与预览逻辑未改 |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.ts` | REQ-UX-004 | `isNearScrollBottom`；`needsMoreContentToScroll`；`shouldAutoLoadMore`；`fillUntilScrollable` |
| `frontend/src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` | REQ-CAT-001 / REQ-UX-004 | 触底 + loadMore + **未溢出自动续载** |
| `frontend/src/features/catalog/browse/composables/useCatalogImportEntry.ts` | （既有） | 导入入口（本 HOTFIX 未改行为；清单补录） |
| `frontend/src/features/catalog/browse/composables/useCatalogImportEntry.spec.ts` | （既有） | 导入入口回归测 |
| `frontend/src/features/catalog/browse/components/SensitiveId.vue` | （既有） | 未改 |
| `frontend/src/features/catalog/browse/utils/labels.ts` | （既有） | 未改 |
| `frontend/src/features/catalog/browse/utils/truncateSensitive.ts` | （既有） | 未改 |
| `planning/tasks/DEV-TASK-WSC-305.md` | — | 本报告 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-001** | L3 分节；触底/未溢出自动追加；筛选与已加载保留 |
| **REQ-UX-004** | pane 自滚；加载中/已全部；无页码；首屏撑不满也能续页 |

## 自测

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse --reporter=default
```

| 文件 | Tests | 结果 |
|---|---|---|
| `useCatalogBrowse.spec.ts` | 19 | **PASSED** |
| `useCatalogImportEntry.spec.ts` | 4 | **PASSED** |
| 合计 | 23 | **PASSED** |

覆盖：`isNearScrollBottom`；`needsMoreContentToScroll`；`shouldAutoLoadMore` 守门；`fillUntilScrollable` 续载至可滚 / !hasMore / 停滞 / maxPages；`loadMore` append/去重。

人工建议（tester）：1080p+ `/catalog`，确认首屏不足一屏时自动追加至出现滚动条；再触底继续；导入与预览可用。

## SCOPE / denyModify

- 仅改 `frontend/src/features/catalog/browse/**` + 本 DEV
- 未改 REV / state / events / contracts / api / backend / 其他 feature
- 未兼任 codeReviewer / tester；未标 VERIFIED

## 状态

**READY_FOR_REVIEW** — 停止。
