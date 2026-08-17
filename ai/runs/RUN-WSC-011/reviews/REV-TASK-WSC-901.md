# 代码审查报告 — TASK-WSC-901

```yaml
reviewId: REV-TASK-WSC-901
taskId: TASK-WSC-901
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-901
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T11:00:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-901 writeSet/denyModify/acceptance/testScope）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-901.md
  - product/requirements/SNAP-WSC-008.md（REQ-CAT-014 / REQ-CAT-019）
  - 实地 diff：`frontend/src/features/catalog/browse/` writeSet 文件
mustDifferFrom: developer-wsc-901
note: |
  作废并覆盖 void 旁路产物 `reviews/REV-TASK-WSC-901.md`（PLAN-WSC-8.1 / 906 菜单内容，与 Wave A 浏览筛选无关）。
  工作树中 `CirculationSeatMap*` 变更归属 TASK-WSC-902（见 DEV-TASK-WSC-902），不在本任务 writeSet；901 denyModify 合规。
```

## 结论

**APPROVE** — **P0=0，P1=0**。实现满足 PLAN-WSC-8.3 §5 TASK-WSC-901 acceptance 与 testScope 核心项；writeSet 内变更对齐 REQ-CAT-014 / REQ-CAT-019；denyModify 未触碰。独立复跑 901 相关 Vitest（53 项）全部通过。开放 2 条 P2（文案与 theme token 可维护性），不阻塞进入 tester 门禁。

**未代 tester 宣称通过**；未标 VERIFIED；未改 `state.yaml` / `events.jsonl`。

## Scope 检查（denyModify / writeSet）

| 项 | 结果 | 证据 |
|---|---|---|
| writeSet 仅 browse 筛选相关 | **PASS** | `CatalogBrowsePage.vue`、`BrowseFilterBar.vue`、`useCatalogBrowse.ts`、对应 spec、`labels.ts` |
| 未改 `CirculationSeatMap*` | **PASS（901 交付）** | DEV-901 未列；diff 中 SeatMap 变更属 902 |
| 未改 `useCanWrite.ts` / `WorkbenchLayout.vue` | **PASS** | git diff writeSet 外无 auth/layout 改动 |
| 未改 backend / contracts / api / e2e / sql | **PASS** | 无 backend、contracts、api 路径 diff |
| 未改 editor / import / detail | **PASS** | denyModify 路径无 diff |
| 未改 state / events | **PASS** | 审查实例仅写本 REV |

## Acceptance 对照

| acceptance 项 | 结果 | 证据 |
|---|---|---|
| 无列表区业务视图/业务大类卡片与 active 标签行 | **PASS** | 移除 `nav-card`/`tag-bar`；`901-no-nav-cards` |
| 无「高级筛选」toggle；面板默认可见 | **PASS** | `BrowseFilterBar` 常显挂载；无 `advancedFilter` |
| 高级区无 industryCategory；query 无 industryCategory / enterpriseName | **PASS** | `buildQuery` 注释 + 测试；`901-no-industryCategory-query` |
| Cascader l1/l2：L1 仅 l1；L2 两者；清空皆无 | **PASS** | `change-on-select` + `applyCategoryPath`；`901-cascader-l1-l2-clear` |
| Cascader loading/error/空选项反馈 | **PASS** | `:loading`、`:disabled` on error、empty/error testid |
| Ant→token 映射 | **PASS** | `ConfigProvider` + `data-browse-filter-theme="ant-token-mapped"` + scoped :deep |
| `/catalog` 与 `/my-products` 同源筛选 | **PASS** | 共用 composable + mine 参数化测试 |
| V1.5 supplierName 与其余 Ant 筛选项不回退 | **PASS** | 独立 `supplierName` Input；Ant Select/Input 保留 |
| mine 仍无座序图 | **PASS** | `seatMapVisible` 逻辑未改 |
| 座序图区域仍挂载 | **PASS** | `<CirculationSeatMap v-if="seatMapVisible" />` 保留 |

## 独立测试复验

```bash
cd frontend
pnpm test -- src/features/catalog/browse/CatalogBrowsePage.spec.ts \
  src/features/catalog/browse/components/BrowseFilterBar.spec.ts \
  src/features/catalog/browse/composables/useCatalogBrowse.spec.ts
```

| 项 | 结果 |
|---|---|
| 用例数 | **53 passed**（3 files） |
| exitCode | **0** |
| §0.2 命名用例 | `901-no-nav-cards`、`901-cascader-l1-l2-clear`、`901-no-industryCategory-query` 均存在且通过 |

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-901-R1-001 | P2 | OPEN | `CatalogBrowsePage.vue` 公共目录 `pageSubtitle` 仍为「按**空间 / 行业**浏览…」，与 REQ-CAT-014 冻结命名「业务视图 / 业务大类」不一致 | 更新副标题文案或确认产品刻意保留旧称 | REQ-CAT-014 |
| FIND-WSC-901-R1-002 | P2 | OPEN | `BrowseFilterBar.vue` 中 `BROWSE_FILTER_THEME.token.colorPrimary` 等使用硬编码 hex（`#3b82f6`），未引用 CSS 变量；主题切换时可能漂移 | 改为读取 `--blue` 等 token 或文档化冻结值 | REQ-CAT-014 §1.5 |

## 架构与可维护性备注（非阻塞）

- 筛选 UI 抽取至 `BrowseFilterBar.vue`，页面仅编排 props/事件，符合分层约定。
- `categoryPathFromFilters` / `applyCategoryPath` 职责清晰；`allL2Categories` 供 Cascader 全树，避免 L1 切换后 L2 选项残缺。
- 分类变更加 `@category-change` 即时 `loadProducts`，与 V1.5 标签卡片点选行为一致；其余字段仍通过「搜索」触发，符合 V1.5 交互。
- `BrowseFilterBar.spec.ts` 以源码断言为主；Cascader 边界由 `useCatalogBrowse.spec.ts` 行为测试覆盖，满足 PLAN testScope 意图。

## 决策依据

- P0/P1 清零 → 批准进入 **tester** 门禁。
- P2 记录供后续 UX/主题 polish，不要求本轮回退。
