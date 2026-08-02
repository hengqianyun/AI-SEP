# UX 差距草稿 — TASK-WSC-201

```yaml
taskId: TASK-WSC-201
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
actorInstance: developer-wsc-201
schema: PLAN-WSC-3.1 §3.1
note: |
  分波草稿；正式合并仅 TASK-WSC-206。
  截图使用测试 fixture / 脱敏演示企业名，无真实 PII。
```

## 差距行

| id | screenshot / pathKey | dimension | severity | status | evidence |
|---|---|---|---|---|---|
| GAP-UX-002-01 | 02 / `02-shell-sidebar.png` | 令牌 | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/02-shell-sidebar.png`：侧栏 `#0b1120` / 232px、品牌区 Logo+「接入端工作台」、菜单图标/圆角/active、主区浅灰、企业信息卡、角色菜单深色+active 蓝强调；令牌见 `frontend/src/styles/tokens.css`（§2.1 最小集） |

## 备注

- 登录页视觉一致性（REQ-UX-008 登录半边）由 TASK-WSC-202 覆盖；本任务仅壳层角色切换区。
- 桌面破坏性视口抽检（1280×800 / 1440×900）由 TASK-WSC-206 走查勾选。
