# DEV-TASK-WSC-203

```yaml
taskId: TASK-WSC-203
actorInstance: developer-wsc-203
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
status: READY_FOR_REVIEW
completedAt: 2026-08-02T14:12:00+08:00
reqs:
  - REQ-UX-004
  - REQ-UX-009
```

## 摘要

按 CSS-token-first 将数据目录浏览页视觉对齐原型 `wsc-v1.1`：消费 TASK-WSC-201 令牌；空间/行业标签 active 蓝底、子类分节左侧色条+计数、筛选条与列表行、列表/预览分栏、滚动加载「加载中…」与空态可读。行为仍走既有 `useCatalogBrowse` / `useCatalogImportEntry`（REQ-CAT-001 筛选与滚动加载不变）。未改 `import/**` 弹窗。交付截图 #04 草稿与差距草稿行。

## 视觉对照要点（原型 → 实现）

| 维度 | 原型/验收 | 落点 |
|---|---|---|
| 标签 active | 蓝底 `#eff6ff` + 蓝字 + `#bfdbfe` 边 | `.tag.active` → `var(--blue-light)` / `var(--blue)` |
| 子类分节 | 左侧色条 + 计数 | `.section-title::before` + `N 项` |
| 筛选条 | 白卡片、标签+控件列、搜索 | `.filter-bar.wsc-surface` + `.filter-group` |
| 列表行 | 序号/编码/名称/上链/类型；选中蓝底 | `.product-item` / `.selected` |
| 预览分栏 | 列表 + ~380px 预览卡 | `.bi-pane` grid |
| 加载中 | 列表底「加载中…」不破坏布局 | `.state.load-more` + spinner；滚动仍由 `onListScroll` |
| 空态 | 可读文案 + 令牌色 | `.state.empty` + `CATALOG_EMPTY_MESSAGE` |
| 导入入口 | 页头主按钮视觉 | `.import-btn`；`ImportDialog` 只读挂载 |

## 变更文件列表

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` | REQ-UX-004 / REQ-UX-009 | 令牌化页头/标签/筛选/分节/列表行/预览/空态加载；导入按钮视觉；保留全部既有 testid 与行为 |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/04-catalog-browse.png` | REQ-UX-004 | 截图 #04 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-203.md` | REQ-UX-004/009 | §3.1 schema 差距草稿行（覆盖 #04） |

未改 composables / labels / import 弹窗实现；未引入 AntDV。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-UX-004** | 页头、空间/行业 active 蓝底、子类色条+计数、筛选条、列表行、预览分栏、加载中呈现；中文标签「空间/行业/产品列表/产品预览/批量导入产品」与原型一致 |
| **REQ-UX-009** | 空态/初始加载/加载更多引用 `--text-*` / `--blue` / `--red-light` 等共享令牌；不遮挡列表滚动与筛选操作 |

## 已知边界 / 后续任务

- 导入弹窗视觉与四态 → TASK-WSC-204。
- 正式截图目录 / `WALKTHROUGH.md` / 正式差距清单 → TASK-WSC-206。
- 草稿截图为与实现令牌一致的 fixture HTML 视口拍屏（演示企业名 / DEMO-* 编码），非联调后端会话态。
- 并行 TASK-WSC-202（overview/auth views）未触碰。

## 自测命令与结果

```text
cd frontend
pnpm typecheck
pnpm exec vitest run src/features/catalog/browse
```

| 命令 | 结果 |
|---|---|
| `pnpm typecheck` | **PASSED**（exit 0） |
| vitest browse（`useCatalogBrowse` + `useCatalogImportEntry`） | **PASSED**（12 tests） |

制品存在性：

| 路径 | 结果 |
|---|---|
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/04-catalog-browse.png` | **存在** |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-203.md` | **存在**（含 #04 schema 行） |

## SCOPE / denyModify

- 未改 `frontend/src/styles/**`、`theme/**`、`layouts/**`、`shell/**`、`router/**`
- 未改 `catalog/admin|maintenance|import|detail|editor`、`overview/**`、`chain/**`、`auth/**`
- 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**`
- 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md`
- 未改 `product/**`、`planning/approved/**`、`ai/rules/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer/tester；未自行标记 VERIFIED；未改 `state.yaml` / `events.jsonl`
