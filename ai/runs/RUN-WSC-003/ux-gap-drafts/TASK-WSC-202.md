# UX 差距草稿 — TASK-WSC-202

```yaml
taskId: TASK-WSC-202
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
actorInstance: developer-wsc-202
schema: PLAN-WSC-3.1 §3.1
note: |
  分波草稿；正式合并仅 TASK-WSC-206。
  截图使用测试 fixture / 脱敏演示数据，无真实 PII。
```

## 差距行

| id | screenshot / pathKey | dimension | severity | status | evidence |
|---|---|---|---|---|---|
| GAP-UX-001-01 | 01 / `01-login.png` | 令牌 | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/01-login.png`：登录卡 `--card-bg`/`--radius-card`/`--shadow-lg`，主按钮 `--blue`，品牌 Logo `--sidebar-bg`+`--sidebar-accent`，与壳层品牌一致；实现见 `frontend/src/features/auth/views/LoginView.vue` |
| GAP-UX-003-01 | 03 / `03-overview.png` | 令牌 | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/03-overview.png`：三列指标卡标题/蓝绿橙图标色块/大号数值/描述层次；图表卡 header、圆角阴影与留白；空态/加载引用共享令牌；实现见 `frontend/src/features/overview/OverviewPage.vue` |

## 备注

- 原型 HTML 无独立登录页；登录视觉以壳层 201 令牌 + 品牌区为对照。
- 图表仍为自研柱条/条形（未换库、未引入 AntDV）。
- 桌面破坏性视口抽检（1280×800 / 1440×900）由 TASK-WSC-206 走查勾选。
