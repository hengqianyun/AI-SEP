# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-202-R1
taskId: TASK-WSC-202
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-202
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
reviewedAt: 2026-08-02T14:15:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-202.md
  - planning/tasks/DEV-TASK-WSC-202.md
  - planning/approved/PLAN-WSC-3.1.md
  - product/requirements/SNAP-WSC-003.md
  - design/prototypes/wsc-v1.1/index.html
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet`：`frontend/src/features/overview/**`（`OverviewPage.vue` + spec）、`frontend/src/features/auth/views/**`（`LoginView.vue` + spec）、草稿截图 `01-login.png` / `03-overview.png` 与 `ux-gap-drafts/TASK-WSC-202.md`。未改 `auth/store|composables|api`、`styles/**`、`theme/**`、`catalog/**`、`router/**`、`contracts/**`、`api/**`、`backend/**`、e2e 与正式 ux 制品。登录与总览为 CSS-token-first（消费 201 `--blue`/`--card-bg`/`--radius-card`/`--shadow*`/`--sidebar-*` 等），未引入 AntDV。OVW 指标仍为 `assetTotal` / `activeEnterprises` / `todayAttestations`，`useOverviewData` 与 `api/overview` 无 diff。审查复跑 `pnpm typecheck` 与 vitest（LoginView 1 + OverviewPage 1 + useOverviewData 1）均 **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-202-R1-001 | P2 | 草稿截图 `01-login.png` / `03-overview.png` 为 fixture HTML 视口拍屏（角标「FIXTURE · 脱敏演示数据 · TASK-WSC-202」），非联调 Vue 会话态；DEV 已声明。计划 §3.1 允许 fixture/脱敏 | tester 对照真应用登录页与总览走查 #01/#03 要点；偏差若非「完全不像」记入差距行，P0 不得开放 | REQ-UX-003, REQ-UX-008, REQ-UX-011 |
| FIND-WSC-202-R1-002 | P3 | `LoginView.spec.ts` / `OverviewPage.spec.ts` 仅静态读源码断言令牌与 testid，未挂载组件验证登录提交或加载/空态渲染 | 可选：补组件测或交 206 E2E 覆盖登录→总览 smoke | REQ-UX-003, REQ-UX-008 |
| FIND-WSC-202-R1-003 | P3 | `OverviewPage.vue` 局部硬编码次级色（如 `#f9fafb`/`#f3f4f6`/`#2563eb`）；主品牌面已用令牌，非第二套全局色板 | 可选：回 201 补中性面令牌后替换，或 206 走查确认可接受 | REQ-UX-001, REQ-UX-003 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| 本任务交付 ⊆ `writeSet` | PASS（overview 页+spec、auth/views+spec、drafts 01+03、gap-draft） |
| 未改 `auth/store/**`、`auth/composables/**`、`auth/api/**` | PASS（git status 无上述路径本任务改动；登录仅 `auth/views/**`） |
| 未改 `styles/**`、`theme/**`、`App.vue`、`main.ts`、`layouts/**`、`shell/**`、`router/**` | PASS（相对本任务 DEV 清单；工作区另有 201/203 脏文件不归本轮 202） |
| 未改 `catalog/**`、`chain/**`、`contracts/**`、`frontend/src/api/**`、`backend/**`、`tests/e2e/**` | PASS |
| 未改正式 `ux-gap-checklist.md` / `screenshots/**` / `WALKTHROUGH.md` | PASS |
| 无 SCOPE_VIOLATION | PASS |

### 2. 令牌消费 / §2.2 CSS-token-first

| 项 | 证据 | 结果 |
|---|---|---|
| 登录主色/卡片/圆角阴影/输入 focus | `LoginView.vue`：`--blue`、`--card-bg`、`--radius-card`、`--shadow-lg`、`--blue-light` focus 环 | PASS |
| 品牌区与壳层一致 | Logo `--sidebar-bg` + `--sidebar-accent`；文案「可信数据空间 / 接入端工作台」 | PASS |
| 总览指标卡色块 | `.stat-card-icon.blue/green/orange` → `--*-light` / 语义色；数值 `28px` | PASS |
| 图表卡 header / 圆角阴影 | `.card-header` + `--radius-card` / `--shadow` | PASS |
| 空态/加载/错误令牌 | `--text-secondary/tertiary`；错误 `--red` / 登录 `--red-light` | PASS |
| 未引入 AntDV / 无替换清单义务 | 源码无 `ant-design-vue`；DEV 声明未引入 | PASS |
| 未私建第二套全局色板 | 页面 scoped 消费 201 令牌；未改 `styles/**` | PASS |

### 3. 登录验收（REQ-UX-008 / 009）

| 项 | 证据 | 结果 |
|---|---|---|
| 与壳层品牌一致，非割裂默认皮肤 | 去掉旧 `#1f4b7a`/`#fff`/`#e2e6ec` 硬编码皮肤；改消费令牌 | PASS |
| 登录行为未改 | `<script>` 仍 `auth.login` → `router.replace(redirect \|\| '/overview')`；未改 store/api | PASS |
| 提交中态可读 | `submitting` →「登录中…」+ disabled | PASS |
| data-testid 保留/增强 | `login-form` / `login-username` / `login-password` / `login-submit` / `login-error` | PASS |

### 4. 总览验收（REQ-UX-003 / 009）+ OVW 行为未扩

| 项 | 证据 | 结果 |
|---|---|---|
| 三列指标卡层次接近原型 | 标题/蓝绿橙图标色块/28px 数值/描述；对照原型 `.stat-card-*` | PASS |
| 图表卡 header、圆角阴影、留白 | 趋势/TOP/动态流/分布均有 `.card-header` + card 令牌 | PASS |
| 图表 CSS 级收敛、未换库 | 自研柱条/条形；未改图表库依赖 | PASS |
| 指标口径 REQ-OVW-001..005 | 仍绑定 `assetTotal` / `activeEnterprises` / `todayAttestations`；五段加载态与 testid 保留 | PASS |
| 未改 composable / API 契约 | `useOverviewData.ts`、`frontend/src/api/overview.ts` 无 diff | PASS |
| 空态/加载可读 | metrics loading/empty/error；图表各段 empty/loading 文案 + 令牌色 | PASS |

### 5. §3.1 分波视觉证据 / 草稿 schema

| 项 | 结果 |
|---|---|
| `ux-walkthrough/drafts/01-login.png` 存在 | PASS（fixture 脱敏；演示账号文案） |
| `ux-walkthrough/drafts/03-overview.png` 存在 | PASS（三列指标+趋势/TOP；fixture 脱敏演示产品名） |
| `ux-gap-drafts/TASK-WSC-202.md` schema 行可检 | PASS（`GAP-UX-001-01` / `GAP-UX-003-01`：id / screenshot / dimension / severity / status / evidence） |
| 正式清单/正式截图/WALKTHROUGH 未越权写入 | PASS |

### 6. 审查复跑（支撑验收，不代替 tester）

| 项 | 结果 |
|---|---|
| `pnpm typecheck` | PASS（exit 0） |
| vitest `LoginView.spec.ts` | PASS（1） |
| vitest `OverviewPage.spec.ts` | PASS（1） |
| vitest `useOverviewData.spec.ts` | PASS（1，只读未改该文件） |

## 残余风险（交 tester）

- 草稿截图为 fixture，tester 须对真 Vue 登录页与总览核对 #01/#03 验收要点，勿仅凭 fixture 图关门禁视觉项。
- 工作区另有 `styles/**`/`shell/**`（201）、`catalog/**`（203）、`backend/**`/`tests/e2e/**` 等非本任务脏改动；tester 取证时以 DEV 变更列表与 writeSet 为准，避免误归因。
- 登录成功进总览的联调 smoke 与桌面破坏性视口抽检归独立 tester / TASK-WSC-206。
- 静态源码 Vitest 不覆盖交互态；E2E/手工走查补齐。

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
