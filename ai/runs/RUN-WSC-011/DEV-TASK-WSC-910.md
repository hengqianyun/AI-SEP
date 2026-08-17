# DEV-TASK-WSC-910

```yaml
taskId: TASK-WSC-910
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
actorInstance: developer-wsc-910-r1
priorActorInstance: developer-wsc-910
status: READY_FOR_REVIEW
kind: HOTFIX
blocksRelease: true
fixRound: 1
fixedFinding: FIND-WSC-910-001
completedAt: 2026-08-17T17:13:00+08:00
reqs:
  - REQ-CAT-007
  - REQ-CAT-013
  - REQ-CAT-017
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。未改 `REV-TASK-WSC-910.md`。未标 VERIFIED。

## R1 回应（FIND-WSC-910-001）

| Finding | 本轮动作 |
|---|---|
| FIND-WSC-910-001（P1） | **已修**：`applyPagination` 在 size 变化时强制 `page=1` 并置同栈抑制标志；紧随的 page-only `@change(current, size)` 不再走 `goPage(current)`。微任务后标志清除，仅翻页仍委托 `goPage` 且保留 size。第 1 页改 size 仍走 `loadEntries`，不靠 `goPage(1)`。 |
| FIND-WSC-910-002（P2） | **本轮不关**；tester 门禁留给独立 tester。未把 UX spec 改成挂载 Pagination。 |

closeWhen 对照：

1. 第 >1 页改 size：同栈 `applyPagination(1, 20)` 再 `applyPagination(2, 20)` 后，只留下一次 `pageSize=20` 列表请求，且为 `{ page: 1, pageSize: 20 }`，`selectedCount===0`。
2. 自动化按库顺序连打（中间不 await）：`showSizeChange` 对应 `applyPagination(1, size)`，`change` 对应 `applyPagination(current, size)`；场景为 total=120、page=2、10→20。
3. 既有用例仍断言：第 1 页改 size 重拉；仅翻页走 `goPage` 且保留 size。

## 对账结论（hotfix）

| # | PO 验收 | 本任务动作 |
|---|---|---|
| 1 | `/catalog/maintenance` 与 `/my-catalog` 分页条可见 pageSize 选择 | 同一 `CatalogMaintenancePage`：`:show-size-changer="true"`，`:page-size-options="PAGE_SIZE_OPTION_LABELS"`（10/20/50/100） |
| 2 | 可选档 10/20/50/100（≤ 服务端 100）；默认保持前端 10 | `DEFAULT_PAGE_SIZE = 10`；`clampPageSize` 只落到四档 |
| 3 | 变更 pageSize：请求带新 `pageSize`、回到第 1 页、清本页勾选 | size 变化：`page=1` + `clearSelection` + `loadEntries`。**不**走 `goPage(1)`。R1：抑制 antdv 同栈随后 `@change` 的旧 current |
| 4 | 仅翻页、不改 pageSize 时与现网一致（含 quick jumper） | size 未变且非同栈抑制时 `applyPagination` 委托既有 `goPage`；`:show-quick-jumper="true"` 保留 |
| 5 | UI 含 size changer；选非默认档后 query 含对应 `pageSize` 且 `page=1` | UX spec `910-page-size-changer`；composable 测 20/50/100 与 dual-emit |

未改浏览页 `/catalog` 触底 `loadMore`。未改后端 / OpenAPI / `wsc-contracts`。907 同实例 `full → myCatalog`、双入口、筛 Tab 清选择、无跨页全选未回退。

无未决 ISSUE（P2 不在本轮关闭范围）。

## filesChanged

### Frontend
- `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue` — 打开 size changer；`@change` 与 `@showSizeChange` 均接到 `applyPagination`（R1 仅注释说明同栈去重在 composable）
- `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts` — `PAGE_SIZE_OPTIONS` / `clampPageSize` / `setPageSize` / `applyPagination`；R1：size 变化后同栈忽略随后 page-only `goPage`
- `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts` — `910-page-size-changer`（20/50/100、page=1、清选择、钳位）；R1 新增 dual-emit 用例；907 命名用例保留
- `frontend/src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts` — `910-page-size-changer`（size changer 可见）；907 命名用例保留

### 未改（理由）
- 后端 / `contracts/openapi` / `wsc-contracts`：服务端 `pageSize` 已支持 1–100，PO 未改口不必动契约。
- 浏览页触底 `loadMore`：范围仅目录维护 / 我的目录 Pagination。
- `tests/e2e/**`：选择器未打断。
- `REV-TASK-WSC-910.md`：禁止改审查正文。

## acceptance 自检

- [x] `/catalog/maintenance` 与 `/my-catalog` 同一分页条可见 pageSize 选择（`show-size-changer`）
- [x] 可选档 10 / 20 / 50 / 100；默认前端 10
- [x] 变更 pageSize：请求新 `pageSize`、回到第 1 页、清本页勾选（含第 >1 页 antdv 双 emit）
- [x] 仅翻页不改 pageSize：行为与现网一致（含 `show-quick-jumper`）
- [x] 自动化：UI 含 size changer；选 20/50/100 后 `listMaintenanceEntries` query 含对应 `pageSize` 且 `page=1`；dual-emit 连打
- [x] 907 命名用例仍绿（同实例 scope 切换、双入口、筛 Tab 清选择、无跨页全选）

## 自测命令与结果

```text
pnpm --dir frontend exec vitest run src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts
→ Test Files  2 passed (2)
→ Tests  16 passed (16)
→ Duration  1.70s
→ exitCode 0
```

### 具名用例

| 用例名 | 文件 | 断言 |
|---|---|---|
| `910-page-size-changer` | `CatalogMaintenanceUx.spec.ts` | `:show-size-changer="true"`；`page-size-options`；`@change` 与 `@showSizeChange` 共用 `applyPagination`；无 `loadMore` |
| `910-page-size-changer` | `useCatalogMaintenance.spec.ts` | 选 20/50/100 时 query `pageSize` 对应且 `page=1`、清选择；第 1 页改 size 仍重拉；仅翻页保留 size；非法值钳到 10/100 |
| `910-page-size-changer: antdv dual emit from page>1 keeps page=1` | `useCatalogMaintenance.spec.ts` | page=2、size 10→20 同栈连打 `applyPagination(1,20)` 再 `applyPagination(2,20)`；唯一一次 size=20 请求为 `{ page: 1, pageSize: 20 }`；选择已清 |
| `907-my-catalog-scope-split` | 上述两文件 | 双入口路由、full vs myCatalog query、同实例 `full → myCatalog` 写 URL 与清选择 |
| `FIND-WSC-907-001` | `CatalogMaintenanceUx.spec.ts` | 页传 reactive scope getter |

## denyModify 自检

- [x] 未改 `tests/e2e/playwright.config.ts`、`tests/e2e/specs/p0-wsc-v1.4.spec.ts`
- [x] 未改 `**/sql/**`
- [x] 未改 `ai/runs/**/state.yaml`、`events.jsonl`
- [x] 无企业管理页；座序图无企业筛；浏览无 `industryCategory` 筛
- [x] 未改浏览触底 `loadMore`
- [x] 未改 RBAC / 拦截器授权模型；未 bump `wsc-contracts`
- [x] 未改 `REV-TASK-WSC-910.md`

## scope

- writeSet 内文件已变更；无超出
- 未标 VERIFIED
