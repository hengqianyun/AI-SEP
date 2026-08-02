# DEV-TASK-WSC-202

```yaml
taskId: TASK-WSC-202
actorInstance: developer-wsc-202
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
status: READY_FOR_REVIEW
completedAt: 2026-08-02T14:12:00+08:00
reqs:
  - REQ-UX-003
  - REQ-UX-008
  - REQ-UX-009
```

## 摘要

按 CSS-token-first 消费 TASK-WSC-201 令牌，对齐登录页品牌与总览三列指标卡/图表卡片视觉（原型 `wsc-v1.1`）。未改数据契约与图表库；未引入 AntDV。交付草稿截图 #01+#03 与差距草稿行。

## 变更文件列表

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/auth/views/LoginView.vue` | REQ-UX-008 / REQ-UX-009 | 登录卡/输入/主按钮消费 `--blue`/`--card-bg`/`--radius-card`/`--sidebar-*`；品牌区与壳层一致；提交中态可读 |
| `frontend/src/features/auth/views/LoginView.spec.ts` | REQ-UX-008 | 断言令牌消费与 `auth.login` 保留 |
| `frontend/src/features/overview/OverviewPage.vue` | REQ-UX-003 / REQ-UX-009 | 三列指标卡（蓝绿橙图标色块+大号数值+描述）；图表卡 header/圆角阴影/留白；空态/加载/错误用令牌色 |
| `frontend/src/features/overview/OverviewPage.spec.ts` | REQ-UX-003 | 断言图标色块、card-header、令牌与 testid；无 AntDV import |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/01-login.png` | REQ-UX-008 | 截图 #01 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/03-overview.png` | REQ-UX-003 | 截图 #03 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-202.md` | REQ-UX-003/008 | §3.1 schema 差距草稿行（覆盖 #01/#03） |

未改：`styles/**`、`theme/**`、`App.vue`、`main.ts`、`layouts/**`、`shell/**`、`router/**`、`auth/store|composables|api/**`、`catalog/**`、`contracts/**`、`api/**`、`backend/**`、`tests/e2e/**`。未引入 AntDV（无替换清单）。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-UX-003** | 三列指标卡：标题/蓝绿橙图标色块/28px 数值/描述；图表卡 header + `--radius-card`/`--shadow`；CSS 级柱条/条形收敛；指标仍为 assetTotal / activeEnterprises / todayAttestations（REQ-OVW 口径） |
| **REQ-UX-008** | 登录页主色 `--blue`、卡片表面与圆角阴影、输入 focus 蓝环、品牌 Logo 用侧栏色；仍 `auth.login` → `/overview` |
| **REQ-UX-009** | 总览/登录加载中文案、空态零值卡与图表空态用 `--text-secondary/tertiary`；错误区 `--red`/`--red-light` |

## 已知边界 / 后续任务

- 正式截图目录 / `WALKTHROUGH.md` / 正式差距清单 → TASK-WSC-206。
- 草稿截图为与实现令牌一致的 fixture HTML 视口拍屏（演示企业名/产品名），非联调后端会话态。
- 原型无独立登录页；登录对照壳层品牌令牌。
- 未改 `useOverviewData` 行为与 API 契约。

## 自测命令与结果

```text
cd frontend
pnpm typecheck
pnpm exec vitest run src/features/overview/composables/useOverviewData.spec.ts src/features/overview/OverviewPage.spec.ts src/features/auth/views/LoginView.spec.ts
```

| 命令 | 结果 |
|---|---|
| `pnpm typecheck` | **PASSED**（exit 0） |
| vitest `useOverviewData.spec.ts` | **PASSED**（1） |
| vitest `OverviewPage.spec.ts` | **PASSED**（1） |
| vitest `LoginView.spec.ts` | **PASSED**（1） |

制品存在性：

| 路径 | 结果 |
|---|---|
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/01-login.png` | **存在** |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/03-overview.png` | **存在** |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-202.md` | **存在**（含 #01/#03 schema 行） |

## SCOPE / denyModify

- 未改 `frontend/src/styles/**`、`theme/**`、`App.vue`、`main.ts`、`layouts/**`、`shell/**`、`router/**`
- 未改 `catalog/**`、`chain/**`、`auth/store/**`、`auth/composables/**`、`auth/api/**`
- 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**`
- 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md`
- 未改 `product/**`、`planning/approved/**`、`ai/rules/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer/tester；未自行标记 VERIFIED；未改 `state.yaml` / `events.jsonl`
- 并行边界：未写入 `catalog/browse/**`（TASK-WSC-203）
