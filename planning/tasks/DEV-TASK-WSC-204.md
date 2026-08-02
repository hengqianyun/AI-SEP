# DEV-TASK-WSC-204

```yaml
taskId: TASK-WSC-204
actorInstance: developer-wsc-204
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
status: READY_FOR_REVIEW
completedAt: 2026-08-02T14:55:00+08:00
reqs:
  - REQ-UX-005
  - REQ-UX-006
  - REQ-UX-009
```

## 摘要

按 CSS-token-first 对齐目录维护页、分类维护路由页内 modal 壳（载体 B，约 960px）与导入弹窗（约 520px）。维护页签 active、筛选/批量条、白卡片表格层次对齐原型；导入 §3.4 四态可区分且 §3.5 展示白名单不回退（Vitest 负例）。权限仍为结构不可达（`v-if` / 路由替换），未用纯 CSS 藏权限。交付草稿截图 05+06+07 与差距草稿。

## 视觉对照要点（原型 → 实现）

| 维度 | 原型/验收 | 落点 |
|---|---|---|
| 维护页签 active | segmented 灰底 + 白片 active | `.status-tabs` / `.status-tab.active` |
| 筛选 / 批量条 | 右侧筛选 + 蓝底批量条 | `.filter-right` / `.batch-bar.active` |
| 表格层次 | 白卡片内列表+分页 | `.maintenance-body.wsc-surface` |
| 分类载体 B | 路由页内 modal ≈960px | `.category-overlay` + `.category-modal` |
| 导入弹窗 | ≈520px、上传/模板/结果/footer | `ImportDialog` `.import-modal` |
| 四态 | ①②③④ 可区分 | `data-result-state` + 图标色块 |
| §3.5 白名单 | 禁信用代码/ownerDID/明文哈希 | `projectWhitelistErrorRows` + 负例 Vitest |
| 空/加载反馈 | 统一令牌色 | 维护/分类/导入 hint / submitting |

## 变更文件列表

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue` | REQ-UX-005 / 009 | 令牌化页头/页签/筛选/批量条/列表/分页；分类入口 `v-if` |
| `frontend/src/features/catalog/admin/CategoryAdminPage.vue` | REQ-UX-006 / 009 | 载体 B modal 壳（遮罩/960/分栏/footer）；权限结构不可达 |
| `frontend/src/features/catalog/import/ImportDialog.vue` | REQ-UX-006 / 009 | 520px 弹窗、上传区、四态结果视觉、footer 主次按钮 |
| `frontend/src/features/catalog/import/composables/useProductImport.ts` | REQ-UX-006 + §3.5 | 白名单投影 / 禁止字段检测辅助 |
| `frontend/src/features/catalog/import/composables/useProductImport.spec.ts` | §3.4 / §3.5 | 四态回归 + 白名单负例 |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/05-catalog-maintenance.png` | REQ-UX-005 | 截图 #05 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/06-category-admin-modal.png` | REQ-UX-006 | 截图 #06 草稿（路由页内 modal 壳） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/07-import-modal.png` | REQ-UX-006 | 截图 #07 草稿（上传 + 部分成功） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-204.md` | REQ-UX-005/006/009 | §3.1 schema 差距草稿行 |

未改 composable 业务逻辑（maintenance/admin）；未引入 AntDV；未改 browse/styles/router。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-UX-005** | 维护页签 active、筛选与批量条、白卡片列表/分页；非管理员 `v-if`+redirect 不可达 |
| **REQ-UX-006** | 分类 modal 壳 ≈960px；导入 ≈520px；遮罩/圆角/footer 主次按钮；功能规则不改 |
| **REQ-UX-009** | 加载中/空态/提交中/成功失败反馈消费共享令牌色 |

## 已知边界 / 后续任务

- 分类维护载体 B 与原型 browse overlay 偏差：差距行 `GAP-UX-006-01`（P1/open，非 P0）。
- 正式截图目录 / `WALKTHROUGH.md` / 正式差距清单 → TASK-WSC-206。
- 草稿截图为与实现令牌一致的 fixture HTML 视口拍屏（演示企业名 / DEMO-*），非联调后端会话态。
- 并行 TASK-WSC-205（detail/editor/chain）未触碰。

## 自测命令与结果

```text
cd frontend
pnpm typecheck
pnpm exec vitest run src/features/catalog/maintenance src/features/catalog/admin src/features/catalog/import
```

| 命令 | 结果 |
|---|---|
| `pnpm typecheck` | **PASSED**（exit 0） |
| vitest maintenance + admin + import | **PASSED**（17 tests：maintenance 4 + admin 4 + import 9，含 §3.5 白名单负例） |

制品存在性：

| 路径 | 结果 |
|---|---|
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/05-catalog-maintenance.png` | **存在**（45846 bytes） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/06-category-admin-modal.png` | **存在**（41797 bytes） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/07-import-modal.png` | **存在**（41591 bytes） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-204.md` | **存在**（含 #05/#06/#07 schema 行） |

## SCOPE / denyModify

- 未改 `frontend/src/styles/**`、`theme/**`、`layouts/**`、`shell/**`、`router/**`
- 未改 `catalog/browse|detail|editor`、`overview/**`、`chain/**`、`auth/**`
- 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**`、`tests/fixtures/import/**`
- 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md`
- 未改 `product/**`、`planning/approved/**`、`ai/rules/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer/tester；未自行标记 VERIFIED；未改 `state.yaml` / `events.jsonl`
