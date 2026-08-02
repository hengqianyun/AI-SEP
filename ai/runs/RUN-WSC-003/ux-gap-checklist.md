# UX 正式差距清单 — RUN-WSC-003

```yaml
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
taskId: TASK-WSC-206
actorInstance: developer-wsc-206
schema: PLAN-WSC-3.1 §3.1
reqs:
  - REQ-UX-011
  - REQ-UX-009
  - REQ-UX-010
mergedFrom:
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-201.md
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-202.md
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-203.md
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-204.md
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-205.md
screenshotsDir: ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/
walkthrough: ai/runs/RUN-WSC-003/ux-walkthrough/WALKTHROUGH.md
piiNote: |
  正式截图均来自分波 drafts（测试 fixture / 脱敏演示数据）；禁止真实 PII 入库。
  见各截图角标或 WALKTHROUGH 脱敏声明。
```

## 汇总

| 项 | 结果 |
|---|---|
| 截图覆盖 01–10 | 每项 ≥1 行（见下表） |
| P0 open | **0** |
| P0 closed | 10（均有 evidence） |
| P1 open（保留偏差） | 1（`GAP-UX-006-01` 载体 B，非 P0，可勾选） |
| P1 closed | 1（`GAP-UX-009-01`） |
| 视口抽检行 | `GAP-UX-010-01` closed → WALKTHROUGH |

## 差距行（§3.1 schema）

| id | screenshot / pathKey | dimension | severity | status | evidence |
|---|---|---|---|---|---|
| GAP-UX-001-01 | 01 / `01-login.png` | 令牌 | P0 | closed | 正式截图 `ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/01-login.png`；登录卡令牌/品牌与壳层一致；WALKTHROUGH §01 |
| GAP-UX-002-01 | 02 / `02-shell-sidebar.png` | 令牌 | P0 | closed | 正式截图 `…/screenshots/02-shell-sidebar.png`；侧栏品牌/菜单/企业卡/角色区；WALKTHROUGH §02 |
| GAP-UX-003-01 | 03 / `03-overview.png` | 令牌 | P0 | closed | 正式截图 `…/screenshots/03-overview.png`；三列指标卡与图表卡层次；WALKTHROUGH §03 |
| GAP-UX-004-01 | 04 / `04-catalog-browse.png` | 布局 | P0 | closed | 正式截图 `…/screenshots/04-catalog-browse.png`；标签/筛选/列表预览分栏；WALKTHROUGH §04 |
| GAP-UX-005-01 | 05 / `05-catalog-maintenance.png` | 控件层级 | P0 | closed | 正式截图 `…/screenshots/05-catalog-maintenance.png`；状态页签/筛选/批量条/列表卡片；WALKTHROUGH §05 |
| GAP-UX-006-01 | 06 / `06-category-admin-modal.png` | IA | P1 | open | **载体 B 偏差（非 P0，可勾选）**：独立路由 `/catalog/admin/categories` 页内等价 modal 壳（约 960px）；原型为目录页 `#category-modal` overlay。本 Run 按 PLAN-WSC-3.1 §1.5 保留；正式截图 `…/screenshots/06-category-admin-modal.png`；WALKTHROUGH §06 / 载体 B 勾选 |
| GAP-UX-006-02 | 07 / `07-import-modal.png` | 控件层级 | P0 | closed | 正式截图 `…/screenshots/07-import-modal.png`；约 520px 弹窗、上传/模板/§3.4 态②双计数+报告；无信用代码/ownerDID/明文哈希；WALKTHROUGH §07 |
| GAP-UX-007-01 | 08 / `08-product-detail.png` | IA | P0 | closed | 正式截图 `…/screenshots/08-product-detail.png`；页头卡+分组只读格子+路径可读；WALKTHROUGH §08 |
| GAP-UX-007-02 | 09 / `09-product-editor.png` | 控件层级 | P0 | closed | 正式截图 `…/screenshots/09-product-editor.png`；页头主次按钮+表单分区；WALKTHROUGH §09 |
| GAP-UX-007-03 | 10 / `10-chain.png` | 布局 | P0 | closed | 正式截图 `…/screenshots/10-chain.png`；版本列表+快照双栏；敏感截断；WALKTHROUGH §10 |
| GAP-UX-009-01 | 08+09+10（主路径抽检） | 空加载反馈 | P1 | closed | 详情/编辑/上链空态·加载·错误与页级反馈消费共享令牌；合并自 TASK-WSC-205 草稿；WALKTHROUGH §空态加载反馈 |
| GAP-UX-010-01 | 02+05+06+07（桌面破坏性） | 布局 | P1 | closed | 视口 1280×800 与 1440×900：侧栏不遮主区、卡片不重叠、modal 不溢出；WALKTHROUGH §视口抽检 |

## 载体 B（GAP-UX-006-01）勾选

- [x] 已识别为 **P1 / open**（非 P0）
- [x] 正式截图 #06 已归档
- [x] 走查记录可勾选（见 `WALKTHROUGH.md`）
- [ ] PO/UX 是否接受保留载体 B（人类签署栏，见 WALKTHROUGH）

## 合并说明

- 分波草稿 P0 项均已关闭并改链正式 `screenshots/` + `WALKTHROUGH.md`。
- 无新增「完全不像原型」P0；若后续 E2E/`TESTRUN-WSC-E2E-V12-UX` 发现功能回归，由独立 tester 开缺陷，不在本清单伪关闭。
