# HOTFIX 候选 — TASK-WSC-910（目录分页 pageSize 选择）

```yaml
taskId: TASK-WSC-910
runId: RUN-WSC-011
kind: HOTFIX
blocksRelease: true
status: IN_DEVELOPMENT
source: PO chat 2026-08-17
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
```

## 业务意图（PO）

发布门禁 `RELEASE_REVIEW` 期间提出：

1. 目录分页组件增加 **pageSize 选择**

## 预落地对照（Orchestrator 只读定位，供确认）

| # | 现象 | 现况 | 建议验收 |
|---|---|---|---|
| 1 | 目录分页无法改每页条数 | `CatalogMaintenancePage.vue` 使用 ant-design-vue `Pagination`，`:show-size-changer="false"`；`onPageChange` 只收页码，调用 `goPage`；`useCatalogMaintenance` 固定 `pageSize = 10`。浏览页 `/catalog` 为触底 `loadMore`，**没有**分页组件 | 目录维护分页（目录维护全量 + 我的目录，同一 `CatalogMaintenancePage`）打开每页条数选择；切换后按新 pageSize 重新拉第 1 页；选项不超过服务端上限 100 |

> 按「**目录维护 / 我的目录** 底部 Pagination」理解。若实际指浏览页触底加载也要改每页条数，确认时请改口。

## acceptance

1. `/catalog/maintenance` 与 `/my-catalog`：分页条可见 **pageSize 选择**（ant-design-vue `show-size-changer` 或等价 UI）
2. 可选档建议 **10 / 20 / 50 / 100**（须 ≤ OpenAPI/服务端 `pageSize` maximum **100**）；默认可保持现前端 **10**
3. 变更 pageSize：请求带上新 `pageSize`，**回到第 1 页**，清本页勾选；`total` / 页码与新 pageSize 一致
4. 仅翻页、不改 pageSize 时行为与现网一致（含 `show-quick-jumper`）
5. 自动化：UI 含 size changer；选择非默认档后 `listMaintenanceEntries` 的 query 含对应 `pageSize`，且 `page=1`

## 建议 writeSet

- `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue`（`show-size-changer`；`@change` / `@showSizeChange` 同时处理 page 与 pageSize）
- `frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts`（`setPageSize` / 扩展 `goPage`）
- 对应 spec：`useCatalogMaintenance.spec.ts`、`CatalogMaintenanceUx.spec.ts`

服务端 `pageSize` 已支持 1–100，**默认不必改后端/契约**。

## denyModify

- `tests/e2e/playwright.config.ts`、`p0-wsc-v1.4.spec.ts`（除非选择器被本 hotfix 打断，默认不改 908 规格）
- `**/sql/**`
- `ai/runs/**/state.yaml`、`events.jsonl`（仅 Orchestrator）
- 无企业管理页；座序图**不得**加企业筛；浏览**不得**加回 `industryCategory` 筛
- 未改口则 **不改** 浏览页触底 `loadMore` 模型
- 不改 RBAC / 拦截器授权模型；不 bump `wsc-contracts`

## 门禁

developer → codeReviewer → tester → VERIFIED 后恢复 `human/APPROVE_RELEASE`

默认本 hotfix **不**改授权模型，不强制 `securityReviewer`。
