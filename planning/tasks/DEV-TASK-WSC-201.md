# DEV-TASK-WSC-201

```yaml
taskId: TASK-WSC-201
actorInstance: developer-wsc-201
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
status: READY_FOR_REVIEW
completedAt: 2026-08-02T13:56:00+08:00
reqs:
  - REQ-UX-001
  - REQ-UX-002
  - REQ-UX-008
  - REQ-UX-010
```

## 摘要

按 CSS-token-first 落地可追踪设计令牌与全局基线，并将工作台壳层（侧栏品牌区 / 导航图标与 hover·active / 主区浅灰内边距 / 底部企业信息卡 / 角色切换深色菜单）对齐原型 `wsc-v1.1`。未挂载全量 AntDV；写入口仍为结构条件渲染（`v-if` + `useCanWrite`），未用 CSS 隐藏替代权限。交付截图 #02 草稿与差距草稿行。

## 令牌映射（§2.1 → 实现）

| 令牌 | 原型值 | 落点 |
|---|---|---|
| `--sidebar-bg` | `#0b1120` | `frontend/src/styles/tokens.css` |
| `--sidebar-width` | `232px` | 同上 |
| `--main-bg` | `#f4f6f9` | 同上 |
| `--card-bg` | `#ffffff` | 同上 |
| `--text-primary/secondary/tertiary` | `#1f2937` / `#6b7280` / `#9ca3af` | 同上 |
| `--border-color` | `#e5e7eb` | 同上 |
| `--blue` / `--blue-light` 等语义色 | 原型 light 对 | 同上 |
| `--shadow-sm` / `--shadow` / `--shadow-lg` | 原型三级阴影 | 同上 |
| 卡片圆角 / 菜单圆角 / 14px 中文字体栈 | 12px / 8px / PingFang 栈 | `--radius-card` / `--radius-menu` / `--font-*` |

全局挂载：`frontend/src/main.ts` → `import './styles/index.css'`（tokens + base）。

## 变更文件列表

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/styles/tokens.css` | REQ-UX-001 | §2.1 最小令牌集 + 壳层辅助变量 |
| `frontend/src/styles/base.css` | REQ-UX-001 | 全局字体/背景/盒模型；`.wsc-card` 共享表面 |
| `frontend/src/styles/index.css` | REQ-UX-001 | 样式入口聚合 |
| `frontend/src/main.ts` | REQ-UX-001 | 挂载全局样式 |
| `frontend/src/App.vue` | REQ-UX-001 | 移除内联冲突 `:root`，交由 tokens |
| `frontend/src/layouts/WorkbenchLayout.vue` | REQ-UX-002 / REQ-UX-010 | 固定侧栏 232px/`#0b1120`、Logo+品牌、菜单图标/圆角/hover·active、企业卡、主区内边距；桌面主区 `margin-left` 防遮挡 |
| `frontend/src/features/shell/components/RoleSwitcher.vue` | REQ-UX-008 / REQ-UX-002 | 自研深色上拉菜单；active 蓝强调；仍调 `auth.switchRole` |
| `frontend/src/features/shell/components/RoleSwitcher.spec.ts` | REQ-UX-008 | 断言非 native `<select>`、保留 switchRole |
| `frontend/src/features/shell/components/WriteEntryDemo.vue` | REQ-UX-002 | 令牌化卡片/按钮质感；**保留** `v-if` 写入口可见性 |
| `frontend/src/views/PlaceholderView.vue` | REQ-UX-002 | 占位页卡片/未开放提示对齐壳层令牌；策略不变 |
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/02-shell-sidebar.png` | REQ-UX-002 | 截图 #02 草稿（fixture 脱敏） |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-201.md` | REQ-UX-001/002 | §3.1 schema 差距草稿行 |

未改：`package.json` / `vite.config.*` / `pnpm-lock.yaml`（无需为挂载样式升级依赖）。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-UX-001** | `tokens.css` 覆盖 §2.1；base 字号/字体栈；卡片 12px+边框阴影可走查 |
| **REQ-UX-002** | 侧栏宽/色、品牌区、菜单图标与 active、主区浅灰+padding、企业卡、占位提示质感；写入口 `v-if` 行为继承 SNAP-WSC-002 |
| **REQ-UX-008** | 角色切换：深色菜单 + active 蓝强调（登录页留给 202） |
| **REQ-UX-010** | 侧栏 fixed + 主区 `margin-left: var(--sidebar-width)`，避免侧栏遮挡主区；窄屏降级为纵向堆叠 |

## 已知边界 / 后续任务

- 登录页品牌对齐 → TASK-WSC-202。
- 正式截图目录 / `WALKTHROUGH.md` / 正式差距清单 → TASK-WSC-206。
- 未引入 AntDV ConfigProvider；后继若经替换清单引入，可由 201-HOTFIX 补挂载。
- 草稿截图为与实现令牌一致的 fixture HTML 视口拍屏（演示企业名），非联调后端会话态。

## 自测命令与结果

```text
cd frontend
pnpm typecheck
pnpm build
pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/features/shell/components/RoleSwitcher.spec.ts
```

| 命令 | 结果 |
|---|---|
| `pnpm typecheck` | **PASSED** |
| `pnpm build` | **PASSED**（vite build success） |
| vitest `useCanWrite.spec.ts` | **PASSED**（3 tests，只读未改该文件） |
| vitest `RoleSwitcher.spec.ts` | **PASSED**（1 test） |

制品存在性：

| 路径 | 结果 |
|---|---|
| `ai/runs/RUN-WSC-003/ux-walkthrough/drafts/02-shell-sidebar.png` | **存在** |
| `ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-201.md` | **存在**（含 #02 schema 行） |

## SCOPE / denyModify

- 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`**/sql/**`、`frontend/src/router/**`
- 未改 overview/catalog/chain/auth 业务包（auth 仅只读跑测）
- 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md`
- 未改 `product/**`、`planning/approved/**`、`ai/rules/**`、`tests/e2e/**`
- 本 DEV 报告按派发要求写入 `planning/tasks/`
- 未兼任 codeReviewer；未自行标记 VERIFIED；未改 `state.yaml` / `events.jsonl`
