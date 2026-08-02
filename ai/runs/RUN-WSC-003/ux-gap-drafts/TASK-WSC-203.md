# UX 差距草稿 — TASK-WSC-203

```yaml
taskId: TASK-WSC-203
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
actorInstance: developer-wsc-203
schema: PLAN-WSC-3.1 §3.1
note: |
  分波草稿；正式合并仅 TASK-WSC-206。
  截图使用测试 fixture / 脱敏演示数据，无真实 PII。
```

## 差距行

| id | screenshot / pathKey | dimension | severity | status | evidence |
|---|---|---|---|---|---|
| GAP-UX-004-01 | 04 / `04-catalog-browse.png` | 布局 | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/04-catalog-browse.png`：页头「数据目录」+ 导入入口；空间/行业标签 active 蓝底（`--blue-light`/`--blue`）；子类分节左侧色条+计数；筛选条卡片层次；列表行与预览分栏；底部「加载中…」不破坏布局；令牌消费自 `frontend/src/styles/tokens.css`（201） |

## 备注

- 导入弹窗视觉归 TASK-WSC-204；本任务仅页内导入入口按钮视觉。
- 空态/加载反馈与正式走查闭环归 TASK-WSC-206 统一抽检。
- 桌面破坏性视口抽检（1280×800 / 1440×900）由 TASK-WSC-206 走查勾选。
