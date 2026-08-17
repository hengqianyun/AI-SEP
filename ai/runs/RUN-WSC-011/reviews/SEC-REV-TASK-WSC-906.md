# 安全审查报告 — TASK-WSC-906

```yaml
reviewId: SEC-REV-TASK-WSC-906
taskId: TASK-WSC-906
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: securityReviewer
actorInstance: security-reviewer-wsc-906
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T13:55:00+08:00
trigger: auth-model-change
basedOn:
  - ai/agents/security-reviewer.md
  - ai/skills/security-review/SKILL.md
  - ai/rules/global/RULE-GLOBAL-SECRETS.md
  - planning/approved/PLAN-WSC-8.3.md（§3.1、§4.1、§5 TASK-WSC-906；前端隐藏 ≠ 授权）
  - product/requirements/SNAP-WSC-008.md（REQ-SHELL-009 / REQ-SHELL-010）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-906.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-906.md（codeReview APPROVE；不代本角色批准）
  - 实地：useCanWrite.ts、WorkbenchLayout.vue、routes.ts、deepLinkAccess.ts、navSurfaces.ts、routeGuards.ts、MyCatalogPlaceholderPage.vue
  - 只读对照：contracts/rbac/matrix.yaml
mustDifferFrom:
  - developer-wsc-906
  - code-reviewer-wsc-906
relatedReqs:
  - REQ-SHELL-009
  - REQ-SHELL-010
riskTags: [shell-nav, rbac-visibility, auth-model-change]
note: |
  实现态审查。结论来自实地读壳层/门控/composable，不采信 DEV 自检或 codeReview APPROVE。
  本任务是 FE 壳层/门控，不是后端授权（905 已独立 SEC-REV；本角色不复用其结论当本任务证据）。
  未改被审源码 / state.yaml / events.jsonl；未 git commit；未标 VERIFIED；不代替 tester。
```

## 结论

**APPROVE** — **P0=0，P1=0，P2=2**。

906 壳层门控与 §3.1 / §4.1 / REQ-SHELL-009/010 一致：**隐藏不是唯一控制**。USER 对 `/catalog/maintenance`、`/my-catalog`、`/my-products`、`/admin/users` 为菜单 `v-if` + `routes.ts` `beforeEnter` + layout `watch` 三重结构不可达；PROVIDER 不可达 `/catalog/maintenance`，**可见且可达** `/my-catalog`；ADMIN 双入口路由/meta.title 可区分；无企业管理页、无企业切换；侧栏企业名称/标识只读且仅展示会话字段；`useCanWrite` UI 单元格含 ADMIN 产品写/导入 **true**，注释明确服务端仍须鉴权。`/my-maintenance` 仅为 redirect，落地仍走 `/my-catalog` 的 `beforeEnter`，不能当越权入口。未发现密钥入库、`v-html` XSS 面，或把客户端 `enterpriseId` 当授权依据的 UI。

**未代 tester 宣称测试通过**；未标 VERIFIED。codeReview APPROVE **不**覆盖本安全面。

## 触发与范围

| 项 | 内容 |
|---|---|
| 触发 | `auth-model-change`（另含 rbac-visibility） |
| 本任务写集 | `useCanWrite.ts`(+spec)、`WorkbenchLayout.vue`(+spec)、`routes.ts`、`frontend/src/features/shell/**` |
| 不在本任务 | 后端 interceptor/矩阵（905）、`CatalogMaintenancePage` 业务（907）、契约（903） |
| 本审查可批范围 | 菜单隐藏、深链、路由 guard、composable、侧栏企业只读、与 matrix UI 单元格一致性 |
| 本审查不可批 | tester 门禁、905 API 403 运行时、907 myCatalog 结果集、人类剩余风险接受 |

## 规划期 SEC 约束对照

| 约束 | 结果 | 证据 |
|---|---|---|
| 前端隐藏 ≠ 授权（§4 总则 / ISSUE-QA-WSC8-R1-001） | **PASS**（任务点名四路径） | `isDeepLinkAllowed` + `requireDeepLinkAccess` `beforeEnter` + layout `watch` 立即 replace `/catalog`；非仅 `v-if` |
| USER 深链负例：维护 / 我的目录 / 我的产品 / 用户管理 | **PASS** | `deepLinkAccess.ts` L28–31；`routes.ts` 四处 `beforeEnter`；layout L90–97 |
| PROVIDER `/catalog/maintenance` 不可达；可见 `/my-catalog` | **PASS** | `canMaintainCatalog('PROVIDER')===false`；`canSeeMyCatalog===true`；谓词对维护 false、对我的目录 true |
| ADMIN 双入口可达且 title 可区分；无企业管理/切换 | **PASS** | meta「目录维护（全量）」vs「我的目录（本企业/本人）」；layout 无「企业管理」、无 RoleSwitcher、无 switchEnterprise |
| `useCanWrite` 对齐 §3.1（含 ADMIN 写 UI true） | **PASS** | `canWriteProduct`/`canImportProduct` ADMIN\|\|PROVIDER；注释「Hide is not authorize」 |
| SHELL-010 企业只读；无客户端 enterpriseId 授权 UI | **PASS** | `sidebar-enterprise` + `data-enterprise-readonly`；插值会话 `enterpriseName`/`enterpriseId`；门控只读 `role` |
| `/my-maintenance` 不得成为越权入口 | **PASS** | `redirect: '/my-catalog'` 且无 component / 无 name；目标路由带 `beforeEnter` |
| 无密钥入库；企业名 XSS | **PASS** | 写集无口令/令牌；企业名 `{{ }}` 文本插值，无 `v-html` |

### §3.1 矩阵逐 cell（本任务 UI）

| 能力键 | ADMIN | PROVIDER | USER | 实地代码 | 结果 |
|---|---|---|---|---|---|
| catalogMaintenanceUI | visible | **hidden** | hidden | `canMaintainCatalog` 仅 ADMIN；菜单 `v-if` + 深链拒绝 | **PASS** |
| myCatalogUI | visible | visible | hidden | `canSeeMyCatalog`；USER 深链 false | **PASS** |
| myProductsUI | visible | visible | hidden | `canSeeMyProducts`；USER 深链 false | **PASS** |
| productWriteUI / ImportUI | **visible** | visible | hidden | `canWriteProduct`/`canImportProduct` ADMIN\|\|PROVIDER | **PASS** |
| userManageUI | visible | hidden | hidden | layout `role === 'ADMIN'`（与 `canManageUsers` 同语义）；深链拒绝 | **PASS** |
| categoryMaintainUI | visible | hidden | hidden | composable 仅 ADMIN；**路由深链见 FIND-001** | **PASS（函数）** |
| \*Api HTTP 200/403 | — | — | — | 905 | 不在本任务 |

## 鉴权与授权（必查）

### 1. 隐藏 ≠ 授权（结构不可达）

| 路径 | USER | PROVIDER | ADMIN | 控制层 |
|---|---|---|---|---|
| `/catalog` | 可达 | 可达 | 可达 | 无门控（全链只读） |
| `/catalog/maintenance` | **不可达** | **不可达** | 可达 | beforeEnter + watch + 页 onMounted |
| `/my-catalog` | **不可达** | 可达 | 可达 | beforeEnter + watch |
| `/my-products` | **不可达** | 可达 | 可达 | beforeEnter + watch |
| `/admin/users` | **不可达** | **不可达** | 可达 | beforeEnter + watch；页 `allowed` + `load()` 早退 |
| `/my-maintenance` | 无页面 | 无页面 | 无页面 | 仅 redirect → `/my-catalog` 再过 guard |

`isDeepLinkAllowed` 未知/空角色对受控路径 **fail-closed**（`can*` 全 false）。`router/index.ts` `beforeEach` 先 `await fetchSession()`，子路由 `beforeEnter` 读到的是会话角色，不是未加载空窗。

layout `watch([route.path, role])` `{ immediate: true }`：角色后变（若发生）会再拦一次，不只靠首次导航。

### 2. `/my-maintenance` redirect

`routes.ts` L32–35：仅 `path` + `redirect: '/my-catalog'`，**无** `component`、**无** `name: 'my-maintenance'`。Vue Router 4 守卫打在**目标**路由；USER 最终仍被 `requireDeepLinkAccess` 打回 `/catalog`。`isDeepLinkAllowed('/my-maintenance')` 虽为默认 true（非受控字面路径），该路由本身无特权视图，不能当越权入口。

### 3. 客户端 enterprise 不作授权真源

| 面 | 行为 | 结果 |
|---|---|---|
| `useCanWrite` / `isDeepLinkAllowed` | 只读 `role` | **PASS** |
| 侧栏 | 展示 `session.enterpriseName` / `session.enterpriseId` | **PASS**（只读） |
| 无 query/body/localStorage 改企业以开菜单 | layout 无输入、无切换 | **PASS** |
| `authStore` | 会话仅来自 `getSession`/`createSession` | **PASS**（906 未改 store；对照只读） |

前端门控注释与代码均不暗示可绕过 905 interceptor。ADMIN 写按钮可见 = UI 单元格，**不是**跨企写授权。

### 4. ADMIN 双入口 / 无企管

- 权威路径 `/my-catalog` vs `/catalog/maintenance`；`navSurfaces` 先判 myCatalog/maintenance 再 `/catalog` 前缀，active 不串。
- `document.title` 跟 `meta.title`；占位页 h1 同文。维护页 DOM h1 仍「目录维护」（907 独占文件）不构成本任务越权。
- 无 `/admin/enterprises` 类路由；无「企业管理」文案；`RoleSwitcher.vue` **未挂载**（既有只读组件，layout 用 `roleLabel`）。

### 5. 占位页不扩大读面

`MyCatalogPlaceholderPage.vue` 无 API、无 `scope`、无企业参数。PROVIDER/ADMIN 进入该路由看不到跨企数据（907 接维护页前无业务载荷）。

## 密钥、敏感数据、注入、供应链

| 检查 | 结果 |
|---|---|
| 写集密钥/生产连接串 | **无** |
| 口令 | 未入 906 日志/模板；登录仍走既有 auth API |
| XSS（用户可控企业名） | `{{ enterpriseName }}` / `{{ session.enterpriseId }}` / `{{ session.displayName }}` 文本插值；写集 **无** `v-html` / `innerHTML` |
| sessionStorage | 仅 `wsc.nav.catalogGroupOpen` 0/1；不存角色/企业；`v-show` 收起不取消 `v-if` 角色门控 |
| 新依赖/CVE | 本任务未引入新依赖；N/A |
| 角色切换 UI | 未挂载；`POST /auth/session/role` 仍 410（既有契约，非本任务增量） |

## Findings

| id | severity | status | 影响 | 证据 | closeWhen | Owner | relatedReqs |
|---|---|---|---|---|---|---|---|
| FIND-SEC-WSC-906-001 | P2 | OPEN | USER/PROVIDER 深链 `/catalog/admin/categories`、`/catalog/products/new`、`/catalog/products/:id/edit` **不**经 `isDeepLinkAllowed`（默认 true）。分类页有 onMounted+`v-if`；编辑页仅 onMounted replace，**模板无** `v-if="productWriteVisible"`，可能闪现写表单。API 仍 905 403，**非**本任务点名四路径绕过 | `deepLinkAccess.ts` L32 `return true`；`routes.ts` 上述三路由无 `beforeEnter`；`ProductEditorPage.vue` L47–52 vs 模板 L76 无写可见包裹；`CategoryAdminPage.vue` L39–45、L66 | 将分类维护与产品增改纳入 `isDeepLinkAllowed` + `beforeEnter`（或 layout watch 等价）；编辑页模板 fail-closed 不渲染写表单 | developer-wsc-906 热修或后续壳层债 | REQ-SHELL-009、REQ-RBAC-002 |
| FIND-SEC-WSC-906-002 | P2 | OPEN | `/my-products` `meta.requiresProvider: true` 为 V1.4 残留字面量；**无**消费方（`router/index.ts` 不读该键）。现行门控已是 ADMIN+PROVIDER。若后续误把该键当成「仅 PROVIDER」权威，会与 §3.1 `myProductsUI` ADMIN visible 冲突（可用性/错误拒绝，非 USER 提权） | `routes.ts` L30；全仓无 `requiresProvider` 读取 | 删除该键或改为与 `requireDeepLinkAccess` 同语义的注释/meta，避免双真源 | developer-wsc-906 或文档债 | REQ-SHELL-009 |

## 残留风险（不构成 906 P0/P1）

| 项 | 说明 | 接受身份 |
|---|---|---|
| 分类/编辑深链未进 906 谓词 | FIND-001；页级 等价 + 905 API；不在 §4.1 点名四负例 | 后续壳层热修或 907 页级 |
| `typeSpecificView.ts` 仍写「USER/ADMIN 结构不可达」 | 实现已 `canWriteProduct`（ADMIN true）；文件不在 906 writeSet | 详情任务清理注释 |
| 维护页 h1「目录维护」vs meta「（全量）」 | 907 独占 `CatalogMaintenancePage` | 907 |
| 前端门控可被 DevTools 改 Pinia role | 预期；授权真源在 905 会话/拦截器 | 无需接受为缺陷 |
| 剩余风险正式接受 | Agent **不得**关闭未修 P0；本轮无 P0 | securityOperationsOwner |

## 未做事项

- 未跑 tester `testScope` / Vitest / E2E；**不**宣称测试通过或 VERIFIED。
- 未审 905 拦截器实现（只读知悉其已独立 SEC-REV；本结论不依赖其 APPROVE 代替本面证据）。
- 未改 `state.yaml` / `events.jsonl` / 被审源码。

## 决策依据

- P0=0、P1=0 → **APPROVE**（P2 不阻塞进入独立 tester）。
- 不可接受风险未发现 → 不 **BLOCK**。
- codeReview APPROVE **不**替代本角色；本结论在「四路径结构不可达 / PROVIDER 无维护 / ADMIN 双入口 / 企业只读 / 矩阵 UI」上 **独立同向**，证据为 `deepLinkAccess` + `beforeEnter` + layout `watch` + `useCanWrite` 源码，而非 DEV 表格。
- 修复 P2 非本轮强制；P0/P1 若后续复审出现则须 REQUEST_CHANGES。
