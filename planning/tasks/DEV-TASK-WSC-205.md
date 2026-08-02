# DEV-TASK-WSC-205

```yaml
taskId: TASK-WSC-205
actorInstance: developer-wsc-205
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
status: READY_FOR_REVIEW
completedAt: 2026-08-02T14:35:00+08:00
reqs:
  - REQ-UX-007
  - REQ-UX-009
```

## 摘要

按 CSS-token-first 对齐原型详情 / 编辑 / 上链视觉：详情页头卡 + 字段分组卡片与只读格子；编辑表单分区与页头/页脚主次按钮；上链版本列表卡 + 快照卡双栏；三级分类路径蓝强调可读。写路径与敏感截断行为不回退（编辑仍 `v-if` + `router.replace` 结构不可达；hash/DID/信用代码截断展示）。交付草稿截图 08+09+10 与差距草稿。

## 变更文件列表

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/detail/ProductDetailPage.vue` | REQ-UX-007 / 009 | 页头卡、分组卡片、只读格子、路径可读、空/加载/错误令牌态；编辑入口 `v-if="productWriteVisible"` |
| `frontend/src/features/catalog/detail/components/SensitiveId.vue` | REQ-UX-007 | 令牌化截断展示样式；截断逻辑不变 |
| `frontend/src/features/catalog/editor/ProductEditorPage.vue` | REQ-UX-007 / 009 | 页头取消/提交主次按钮、表单分区、路径预览、页级反馈令牌色；无写权仍 `replace('/catalog')` |
| `frontend/src/features/chain/ChainPage.vue` | REQ-UX-007 / 009 | 页头卡；双栏版本列表+快照；active 蓝底；三级路径可读；空/加载/错误令牌态 |
| `frontend/src/features/chain/components/SensitiveId.vue` | REQ-UX-007 | 令牌化截断展示样式；截断逻辑不变 |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/08-product-detail.png` | REQ-UX-007 | 截图 #08 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/09-product-editor.png` | REQ-UX-007 | 截图 #09 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/10-chain.png` | REQ-UX-007 | 截图 #10 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-205.md` | REQ-UX-007/009 | §3.1 schema 差距草稿行（覆盖 #08/#09/#10） |

未改 composables / truncateSensitive 纯逻辑与 API 契约；未引入 AntDV。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-UX-007** | 详情分组卡片与只读层次；编辑分区+主次按钮；上链双栏；三级路径样式；写入口结构条件渲染 |
| **REQ-UX-009** | 空态/加载/错误与编辑页级 success/error 反馈消费 201 共享令牌 |

## 已知边界 / 后续任务

- 正式截图目录 / `WALKTHROUGH.md` / 正式差距清单 → TASK-WSC-206。
- 草稿截图为 fixture HTML 视口拍屏（角标「FIXTURE · 脱敏演示数据 · TASK-WSC-205」），非联调后端会话态。
- 桌面破坏性视口抽检由 206 勾选。

## 自测命令与结果

```text
cd frontend
pnpm typecheck
pnpm exec vitest run src/features/catalog/detail src/features/catalog/editor src/features/chain
```

| 命令 | 结果 |
|---|---|
| `pnpm typecheck` | **PASSED**（exit 0，`vue-tsc --noEmit`） |
| vitest detail/editor/chain | **PASSED**（4 files / 12 tests） |

制品存在性：

| 路径 | 结果 |
|---|---|
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/08-product-detail.png` | **存在**（~44KB） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/09-product-editor.png` | **存在**（~38KB） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/10-chain.png` | **存在**（~35KB） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-205.md` | **存在**（含 #08/#09/#10 schema 行） |

## SCOPE / denyModify

- 未改 `frontend/src/styles/**`、`router/**`、`browse/**`、`admin/**`、`maintenance/**`、`import/**`、`overview/**`、`auth/**`
- 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**`（截图临时依赖已回退，不保留 e2e 包变更）
- 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md`
- 未改 `product/**`、`planning/approved/**`、`ai/rules/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer；未自行标记 VERIFIED；未改 `state.yaml` / `events.jsonl`
