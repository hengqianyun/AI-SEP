# 代码审查

```yaml
reviewId: REV-TASK-WSC-702
taskId: TASK-WSC-702
planId: PLAN-WSC-6.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-702-r1
decision: APPROVE
p0: 0
p1: 0
p2: 2
p3: 1
contracts: wsc-contracts@2.2.0（本任务无契约变更；只读对照基线）
reviewedAt: 2026-08-12T10:40:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-6.2.md（§1.5 / §4 / §4.1 / TASK-WSC-702）
  - product/requirements/SNAP-WSC-006.md（REQ-SHELL-001 / REQ-RBAC-001）
  - frontend/src/layouts/WorkbenchLayout.vue
  - frontend/src/layouts/WorkbenchLayout.spec.ts
  - frontend/src/router/routes.ts
  - frontend/src/features/auth/composables/useCanWrite.ts
  - frontend/src/features/auth/composables/useCanWrite.spec.ts
mustDifferFrom: developer（Wave A TASK-WSC-702）
note: |
  工作树未见独立 DEV-TASK-WSC-702.md；审查以批准计划 acceptance/testScope + writeSet 字面 diff 为准。
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。TASK-WSC-702 交付面落在字面 `writeSet`：一级「数据目录」常显子菜单（全链 / 目录维护 / 我的数据产品）；`canMaintainCatalog` 纠偏为 **仅 ADMIN**；壳层 `watch` 对非 ADMIN 深链 `/catalog/maintenance` 立即 `replace('/catalog')`（与维护页既有 `onMounted` 门控形成双保险，且维护页通过同一 composable 自动跟随纠偏）；§4.1 角色可见性与用户管理不回退；双表面 active 互斥逻辑正确；`routes.ts` 仅改 `/catalog` meta title 为「全链数据目录」，未删路由。

审查复跑 Vitest：`useCanWrite.spec.ts` + `WorkbenchLayout.spec.ts` → **2 files / 9 tests PASSED**。正式 VERIFIED 仍须独立 tester 执行完整 `testScope`；本报告不代替 TESTRUN。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-702-R1-001 | P2 | `WorkbenchLayout.spec.ts` 对 §4.1 菜单可见性、深链门控以 **源码字符串 / 正则** 断言为主；`resolveActiveNav` 为镜像副本而非挂载 `WorkbenchLayout` 后驱动 router。实现字面正确且 `useCanWrite` 三角色可执行断言齐全，故不升格 P1；仍有镜像漂移与「未真正导航拒绝」的残余风险 | tester 抽测三角色导航+深链，或后续补 mount+router 组件测；镜像与 SFC `isActive` 保持同步即可关闭 | REQ-SHELL-001；REQ-RBAC-001；TASK-WSC-702 testScope |
| FIND-WSC-702-R1-002 | P2 | Run 目录未见 `ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-702.md`（701 已有 DEV 包）。不影响代码正确性与本轮批准，但削弱编排审计链 | developer/编排补交 DEV 包（变更摘要 + 自测命令/结果）后关闭 | PLAN-WSC-6.2 §6.2 |
| FIND-WSC-702-R1-003 | P3 | 深链门控落在壳层 `watch` + 维护页 `onMounted`，未在 `routes.ts`/`router.beforeEach` 增加 `requiresAdmin` 类 meta（对比 `/my-products` 仅有 `requiresProvider` 且当前 `beforeEach` 亦未强制）。与计划「页级/路由结构门控与菜单一致」及既有项目模式相容；非缺陷 | 可选：后续统一路由 meta 守卫；非本任务阻塞 | REQ-RBAC-001 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照 TASK-WSC-702 acceptance）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| 交付 diff ⊆ writeSet（Layout + routes + useCanWrite + 两处 spec） | **PASS**（`git diff --stat`：4 改 + 1 新建 spec） |
| `routes.ts` 仅 title/meta 展示「全链数据目录」；未删除 `/catalog`、`/catalog/maintenance`、`/my-products`、`/admin/users` | **PASS** |
| denyModify：browse/**、maintenance/**、users/**、RoleSwitcher、contracts、api、backend、e2e、styles/theme | **PASS**（本任务 diff 未触碰） |
| 未写 `ai/runs/**/state.yaml` / `events.jsonl`；未改业务实现以外代码 | **PASS**（审查者仅写本 REV） |
| `mustDifferFrom` developer | **PASS**（`code-reviewer-wsc-702-r1`） |

### 2. REQ-SHELL-001 导航收纳

| 项 | 证据 | 结果 |
|---|---|---|
| 一级「数据目录」+ 常显子项 | `nav-catalog-group`；子项常显（验收允许「展开/收起或常显」） | **PASS** |
| 子项文案：全链数据目录 / 目录维护 / 我的数据产品 | Layout 模板 + routes meta title | **PASS** |
| 路由保持 | paths 均在 | **PASS** |

### 3. REQ-RBAC-001 / §4.1 角色矩阵 + ADMIN-only

| 项 | 证据 | 结果 |
|---|---|---|
| `canMaintainCatalog('ADMIN')===true`；PROVIDER/USER/null/undefined===false | `useCanWrite.ts`；spec 三角色用例 | **PASS** |
| ADMIN：全链+维护可见；我的产品隐藏；用户管理仍可见 | `catalogMaintenanceVisible` / `myProductsVisible` / `userManageVisible` + `nav-users` | **PASS** |
| PROVIDER：全链+我的产品；维护菜单 `v-if` 隐藏 | Layout + composable | **PASS** |
| USER：仅全链；维护/我的/用户 `v-if` 不可见 | 同上 | **PASS** |
| 非 ADMIN 深链维护结构门控（非仅菜单隐藏） | Layout `watch(..., { immediate: true })` → `router.replace('/catalog')`；维护页 `onMounted` 读同一 `catalogMaintenanceVisible` | **PASS** |
| 用户管理 / 角色只读展示不回退 | `nav-users`、`session-role-label`；无 RoleSwitcher 挂载 | **PASS** |

### 4. Active 互斥

| 项 | 证据 | 结果 |
|---|---|---|
| `/catalog/**` 浏览 vs `/catalog/maintenance` vs `/my-products`（含 fromMine / `from=mine`） | `onMineSurface` / `onMaintenanceSurface` / `isActive` 互斥分支 | **PASS** |
| 维护路径不高亮「全链」；mine 表面不高亮全链 | 同上 | **PASS** |

### 5. testScope（审查复跑，非代替 tester）

| 项 | 结果 |
|---|---|
| Vitest `useCanWrite` 三角色 `canMaintainCatalog`（含 PROVIDER=false） | **PASS**（可执行断言） |
| §4.1 可见性正例+负例 | **PASS**（逻辑覆盖；证据强度见 FIND-001） |
| active 互斥用例 | **PASS**（镜像函数；见 FIND-001） |
| 深链负例 | **PASS**（源码门控存在；执行级抽测交 tester；见 FIND-001） |

## 复跑测试摘要

| 命令 | 结果 |
|---|---|
| `cd frontend; pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts src/layouts/WorkbenchLayout.spec.ts` | **PASSED** — 2 files / 9 tests（2026-08-12） |

## 残余风险（交 tester）

- 建议手工或 E2E（707 场景 2/8）验证：PROVIDER/USER 直开 `/catalog/maintenance` 被重定向且维护 UI 不停留；ADMIN 可达。
- API 403 不在本任务写集（703/704）；FE 隐藏 ≠ 授权，后端矩阵纠偏仍须后续任务。
- FIND-001：组件级导航测缺失时，以壳层+维护页双门控字面为准做行为抽测。

## 决策权声明

- 审查者未修改被审业务代码；未写 Run `state.yaml` / `events.jsonl`。
- 未兼任 developer；未调度/代替 tester 宣称 VERIFIED。
- **decision: APPROVE**（P0/P1 清零；开放 finding 均为 P2/P3；可进入独立 tester 门禁）。

## 计数

| 级别 | 数量 | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 2 | FIND-001 测试证据形态；FIND-002 缺 DEV 包 |
| P3 | 1 | FIND-003 可选路由 meta 守卫 |
