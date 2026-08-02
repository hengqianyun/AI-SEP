# UX 差距草稿 — TASK-WSC-205

```yaml
taskId: TASK-WSC-205
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
actorInstance: developer-wsc-205
schema: PLAN-WSC-3.1 §3.1
note: |
  分波草稿；正式合并仅 TASK-WSC-206。
  截图使用测试 fixture / 脱敏演示数据，无真实 PII；敏感标识截断展示。
```

## 差距行

| id | screenshot / pathKey | dimension | severity | status | evidence |
|---|---|---|---|---|---|
| GAP-UX-007-01 | 08 / `08-product-detail.png` | IA | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/08-product-detail.png`：页头卡（返回区+标题+主次操作）、字段分组卡片、只读格子层次；三级路径蓝强调可读；信用代码截断；令牌消费自 `frontend/src/styles/tokens.css`（201）；实现 `ProductDetailPage.vue` |
| GAP-UX-007-02 | 09 / `09-product-editor.png` | 控件层级 | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/09-product-editor.png`：页头取消/提交主次按钮；表单分区标题分隔；标签/控件间距；路径预览；实现 `ProductEditorPage.vue`；普通用户编辑入口仍为 `v-if`/`router.replace` 结构不可达 |
| GAP-UX-007-03 | 10 / `10-chain.png` | 布局 | P0 | closed | 草稿截图 `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/10-chain.png`：版本列表卡+快照卡双栏；active 蓝底；三级路径可读；metadataHash/ownerDID 截断不扩大明文；实现 `ChainPage.vue` |
| GAP-UX-009-01 | 08+09+10 | 空加载反馈 | P1 | closed | 详情/编辑/上链空态与加载/错误态引用共享令牌（`--text-*`/`--red`/`--card-bg`/`--radius-*`）；页级反馈（编辑 success/error）用 `--green-light`/`--red-light` |

## 备注

- 正式截图目录 / `WALKTHROUGH.md` / 正式差距清单 → TASK-WSC-206。
- 桌面破坏性视口抽检（1280×800 / 1440×900）由 TASK-WSC-206 走查勾选。
- 草稿截图为 fixture HTML 视口拍屏（角标「FIXTURE · 脱敏演示数据 · TASK-WSC-205」），非联调后端会话态。
