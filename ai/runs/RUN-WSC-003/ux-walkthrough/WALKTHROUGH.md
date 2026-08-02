# UX 走查记录 — RUN-WSC-003 / TASK-WSC-206

```yaml
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
taskId: TASK-WSC-206
actorInstance: developer-wsc-206
prototype: design/prototypes/wsc-v1.1/index.html
screenshotsDir: ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/
gapChecklist: ai/runs/RUN-WSC-003/ux-gap-checklist.md
evidenceIdTarget: TESTRUN-WSC-E2E-V12-UX
```

## 脱敏声明（ISSUE-SEC-UX-R1-003）

- [x] 正式截图 `screenshots/01`–`10` 均由分波 `drafts/` 复制整理，使用**测试 fixture / 脱敏演示数据**（含 DEMO-*、演示企业名、截断敏感标识）。
- [x] **未**将含真实 PII 的截图作为发布证据入库。
- [x] 若后续发现误用真实 PII，须重拍替换后再关门禁。

## 主路径截图清单（§3.1）

| # | 文件 | 页面 | 对照结论 | 差距 id |
|---|---|---|---|---|
| 01 | `screenshots/01-login.png` | 登录 | 登录卡/品牌与壳层令牌一致；非「完全不像原型」 | GAP-UX-001-01 |
| 02 | `screenshots/02-shell-sidebar.png` | 壳层/侧栏 | 品牌区、菜单 active、企业卡、角色区对齐 | GAP-UX-002-01 |
| 03 | `screenshots/03-overview.png` | 总览 | 指标卡/图表卡层次与留白对齐 | GAP-UX-003-01 |
| 04 | `screenshots/04-catalog-browse.png` | 数据目录 | 标签/筛选/列表预览分栏对齐 | GAP-UX-004-01 |
| 05 | `screenshots/05-catalog-maintenance.png` | 目录维护 | 页签/筛选/批量条/列表卡片对齐 | GAP-UX-005-01 |
| 06 | `screenshots/06-category-admin-modal.png` | 分类维护（路由页内 modal 壳） | 视觉可用；**载体 B** 与原型 overlay 宿主不同（P1 open） | GAP-UX-006-01 |
| 07 | `screenshots/07-import-modal.png` | 批量导入弹窗 | ~520px；上传/模板/部分成功双计数+报告；无扩 PII | GAP-UX-006-02 |
| 08 | `screenshots/08-product-detail.png` | 产品详情 | 页头卡+分组只读+路径可读 | GAP-UX-007-01 |
| 09 | `screenshots/09-product-editor.png` | 产品编辑 | 主次按钮+表单分区对齐 | GAP-UX-007-02 |
| 10 | `screenshots/10-chain.png` | 上链信息 | 双栏版本/快照；敏感截断 | GAP-UX-007-03 |

## 载体 B 勾选（GAP-UX-006-01）

- [x] 已记录为 **P1 / open**（非 P0）
- [x] 正式截图 #06 可检索
- [x] 开发走查接受本 Run 保留独立路由 modal 壳（PLAN §1.5）
- [x] PO/UX 人类确认接受保留（见下方签署栏）

## 空态 / 加载 / Toast 主路径抽检（REQ-UX-009）

- [x] 总览：metrics/图表空态与加载使用共享令牌色与间距（对照 #03）
- [x] 目录：列表加载/空态不破坏分栏布局（对照 #04）
- [x] 详情 / 编辑 / 上链：空·加载·错误与页级反馈令牌统一（对照 #08–#10；`GAP-UX-009-01`）
- [x] 导入：提交中与结果态不遮挡「关闭/重传」主操作（对照 #07）

## 桌面视口破坏性检查（REQ-UX-010 / ISSUE-QA-WSC3-R1-004）

开发走查（developer-wsc-206）对照运行中前端 + 正式截图抽检：

### 1280×800

- [x] 侧栏不遮挡主区
- [x] 卡片不重叠
- [x] 弹窗 / modal 壳（分类 #06、导入 #07）不溢出视口

### 1440×900

- [x] 侧栏不遮挡主区
- [x] 卡片不重叠
- [x] 弹窗 / modal 壳不溢出视口

> 可选增强：Playwright 截图抽样（非唯一门禁）；正式功能门禁见独立 tester `TESTRUN-WSC-E2E-V12-UX`。

## 功能回归 E2E（规格侧，非 VERIFIED）

| 项 | 约定 |
|---|---|
| 规格 | `tests/e2e/specs/p0-wsc-v1.1.spec.ts`（含三角色写入口矩阵） |
| fixture | `tests/e2e/fixtures/auth.ts`（角色下拉适配 UX DOM） |
| 命令 | `pnpm --dir tests/e2e exec playwright test specs/p0-wsc-v1.1.spec.ts`（需 `E2E_RUN=1`） |
| 报告落点 | `tests/e2e/reports/p0-wsc-v1.2-ux/` |
| evidenceId | `TESTRUN-WSC-E2E-V12-UX`（**正式执行与落盘由独立 tester**） |

开发可选试跑结果记入 `planning/tasks/DEV-TASK-WSC-206.md`，**不**自称任务 VERIFIED。

## PO / UX 确认栏（人类签署；可留空）

| 角色 | 确认内容 | 签署人 | 日期 | 备注 |
|---|---|---|---|---|
| PO | 主路径无 P0「完全不像原型」；载体 B（GAP-UX-006-01）可接受保留 | productOwner（会话确认） | 2026-08-02 | 原话：「确认走查通过，接受载体 B」 |
| UX | 截图 01–10 与原型同屏对照通过；视口破坏性检查认可 | productOwner 代 UX 确认（同会话） | 2026-08-02 | 与 PO 同批确认；无独立 UX 人类实例 |

签名引用：`ai/runs/RUN-WSC-003/events.jsonl` → `HUMAN_DECISION` `CONFIRM_UX_WALKTHROUGH` @ 2026-08-02T16:47:00+08:00

---

*本文件结构满足 PLAN-WSC-3.1 §3.1；PO/UX 走查签字项已于 2026-08-02 关闭。*
