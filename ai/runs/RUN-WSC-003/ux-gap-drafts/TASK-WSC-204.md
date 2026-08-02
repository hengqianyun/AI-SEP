# UX 差距草稿 — TASK-WSC-204

```yaml
taskId: TASK-WSC-204
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
actorInstance: developer-wsc-204
schema: PLAN-WSC-3.1 §3.1
note: |
  分波草稿；正式合并仅 TASK-WSC-206。
  截图使用测试 fixture / 脱敏演示数据（DEMO-*），无真实 PII。
```

## 差距行

| id | screenshot / pathKey | dimension | severity | status | evidence |
|---|---|---|---|---|---|
| GAP-UX-005-01 | 05 / `05-catalog-maintenance.png` | 控件层级 | P0 | closed | 草稿截图：状态页签（全部/已维护/待关联）segmented active；筛选下拉 +「维护三级分类」；批量条蓝底；列表/分页在白卡片内；令牌消费自 `frontend/src/styles/tokens.css`（201） |
| GAP-UX-006-01 | 06 / `06-category-admin-modal.png` | IA | P1 | open | **载体 B 偏差（非 P0）**：实现为独立路由 `/catalog/admin/categories` 页内等价 modal 壳（约 960px、遮罩/header/分栏/footer 右对齐）；原型为目录页 `#category-modal` overlay。本 Run 按 §1.5 不强制改为 browse 宿主；走查可勾选 |
| GAP-UX-006-02 | 07 / `07-import-modal.png` | 控件层级 | P0 | closed | 草稿截图：导入弹窗约 520px；上传区/模板入口；结果区展示 §3.4 态②（部分成功双计数 + 报告入口）；无信用代码/ownerDID/明文哈希 |

## 备注

- 导入四态 ①③④ 逻辑由 Vitest `useProductImport` 覆盖；草稿 #07 主示上传步 + 态②。
- 权限：维护/分类/导入入口均为 `v-if` + 路由守卫结构不可达，未用纯 CSS 藏权限。
- 桌面破坏性视口抽检（1280×800 / 1440×900）由 TASK-WSC-206 走查勾选。
