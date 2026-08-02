# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-201-R1
taskId: TASK-WSC-201
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-201
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
reviewedAt: 2026-08-02T14:00:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-201.md
  - planning/tasks/DEV-TASK-WSC-201.md
  - planning/approved/PLAN-WSC-3.1.md
  - product/requirements/SNAP-WSC-003.md
  - design/prototypes/wsc-v1.1/index.html
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet`：`frontend/src/styles/**`、`main.ts`/`App.vue`、`layouts/WorkbenchLayout.vue`、`features/shell/**`、`PlaceholderView.vue`，以及分波草稿截图与差距草稿；未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`router/**`、`auth/**`、正式 ux 制品与 e2e。§2.1 最小令牌集与原型 `:root` 值一致并已挂载；壳层侧栏/品牌/菜单/主区/企业卡/角色深色菜单为 CSS-token-first 自研实现，未引入裸 AntDV 主题。写入口与目录维护菜单仍为 `v-if` + `useCanWrite` 结构条件渲染。审查复跑 `pnpm typecheck` / `pnpm build` / vitest（`useCanWrite` 3 + `RoleSwitcher` 1）均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`（含草稿存在性与逻辑回归）。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-201-R1-001 | P2 | 草稿截图 `02-shell-sidebar.png` 为 fixture HTML 视口拍屏（角标「FIXTURE · 脱敏演示数据」），非联调 Vue 会话态；DEV 已声明。计划 §3.1 允许 fixture/脱敏，但与真壳层 DOM 可能有细微差 | tester 对照真应用壳层走查 #02 要点；偏差若非「完全不像」记入差距行，P0 不得开放 | REQ-UX-002, REQ-UX-011 |
| FIND-WSC-201-R1-002 | P3 | `RoleSwitcher.spec.ts` 仅静态读源码断言（无 `<select>`、含 `switchRole`），未挂载组件验证菜单开合/active 样式 | 可选：补组件测或交 206 E2E 覆盖角色菜单交互 | REQ-UX-008 |
| FIND-WSC-201-R1-003 | P3 | 品牌区语义标签为 `p`+`h1`，原型为 `h1`（eyebrow）+`h2`（标题）；视觉层次已对齐，无功能影响 | 可选：与原型标题层级对齐以利 a11y | REQ-UX-002 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet` | PASS（styles、App/main、layouts、shell、PlaceholderView、drafts/gap-draft、RoleSwitcher.spec） |
| 未改 `contracts/**`、`frontend/src/api/**`、`backend/**`、`**/sql/**` | PASS（相对 DEV 变更清单） |
| 未改 `frontend/src/router/**`、`features/auth/**`、overview/catalog/chain | PASS（本任务交付面；工作区另有 catalog/e2e/backend 脏文件不归本轮 201） |
| 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md` | PASS |
| 无 SCOPE_VIOLATION | PASS |

### 2. §2.1 令牌最小集（REQ-UX-001）

| 项 | 证据 | 结果 |
|---|---|---|
| `--sidebar-bg` `#0b1120` / `--sidebar-width` `232px` | `tokens.css` | PASS |
| `--main-bg` `#f4f6f9` / `--card-bg` / text 三级 / `--border-color` | 同上 | PASS |
| 语义色 blue/green/orange/red + light | 与原型 `:root` 一致 | PASS |
| `--shadow-sm` / `--shadow` / `--shadow-lg` | 与原型三级阴影一致 | PASS |
| 卡片圆角 12px / 菜单 8px / 14px + PingFang 栈 | `--radius-*` / `--font-*` + `base.css` | PASS |
| 全局挂载 | `main.ts` → `./styles/index.css`；`App.vue` 移除冲突内联 `:root` | PASS |

### 3. 壳层视觉（REQ-UX-002 / 008 / 010）

| 项 | 证据 | 结果 |
|---|---|---|
| 侧栏 fixed 232px / `#0b1120` | `WorkbenchLayout` + tokens | PASS |
| Logo +「接入端工作台」品牌区 | header + logo 容器 + eyebrow/title | PASS |
| 菜单图标 / 圆角 / hover·active | `.menu-item` + SVG 图标位 | PASS |
| 主区浅灰 + padding；`margin-left` 防遮挡 | `.main` 使用 `--main-bg` / `--main-padding` / `--sidebar-width` | PASS |
| 企业信息卡 + 角色深色菜单 / active 蓝 | `.company-card`；`RoleSwitcher` `--role-menu-bg` / `--role-active-bg` / `--blue` | PASS |
| 占位「未开放」不破坏壳层 | `.badge` 用 `--orange`；`PlaceholderView` 令牌化卡片 | PASS |
| REQ-UX-010 桌面破坏性 | fixed 侧栏 + main margin；窄屏堆叠降级（非移动端验收） | PASS（实现合理；视口抽检归 206） |

### 4. CSS-token-first / AntDV（§2.2）

| 项 | 结果 |
|---|---|
| 自研 markup + CSS 变量，非 AntDV 默认主题冒充 | PASS（壳层/styles 无 AntDV ConfigProvider / `a-*` 挂载） |
| 未强制全量 AntDV | PASS（与 acceptance / DEV 边界一致） |

### 5. §1.4 权限结构控制（功能基线）

| 项 | 证据 | 结果 |
|---|---|---|
| 写入口 `v-if` + `useCanWrite` | `WriteEntryDemo.vue` 四入口均为 `v-if` | PASS |
| 目录维护菜单结构条件渲染 | `WorkbenchLayout` `v-if="catalogMaintenanceVisible"` | PASS |
| 未用纯 CSS 藏权限 | 权限控件无 `display:none`/`visibility`/`opacity:0`/`pointer-events` 替代；RoleSwitcher `v-show` 仅菜单开合 UI 态 | PASS |
| 角色切换仍调 `auth.switchRole` | `RoleSwitcher.vue` + spec 断言 | PASS |

### 6. §3.1 / §3.3 分波视觉证据

| 项 | 结果 |
|---|---|
| `ux-walkthrough/drafts/02-shell-sidebar.png` 存在 | PASS（~35KB；fixture 脱敏演示企业名） |
| `ux-gap-drafts/TASK-WSC-201.md` schema 行可检 | PASS（`GAP-UX-002-01`：id / screenshot / dimension / severity / status / evidence） |
| 正式清单/正式截图/WALKTHROUGH 未越权写入 | PASS |

### 7. 审查复跑（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| `pnpm typecheck` | PASS |
| `pnpm build` | PASS（vite build success） |
| vitest `useCanWrite.spec.ts` | PASS（3 tests，只读未改该文件） |
| vitest `RoleSwitcher.spec.ts` | PASS（1 test） |

## 残余风险（交 tester）

- 草稿截图为 fixture，tester 须对真 Vue 壳层核对 #02 验收要点，勿仅凭 fixture 图关门禁视觉项。
- 工作区另有 `frontend/src/features/catalog/**`、`tests/e2e/**`、`backend/**` 等非本任务脏改动；tester 取证时以 DEV 变更列表与 writeSet 为准，避免误归因。
- 登录页品牌（REQ-UX-008 登录半边）与正式走查合并属 202/206，不在本任务 VERIFIED 范围。
- 桌面 1280×800 / 1440×900 破坏性抽检归 TASK-WSC-206。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- 未改 `state.yaml` / `events.jsonl`；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 1 |
| P3 | 2 |
