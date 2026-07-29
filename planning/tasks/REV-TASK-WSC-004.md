# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-004-R1
taskId: TASK-WSC-004
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-004
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
reviewedAt: 2026-07-29T16:25:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-004.md
  - planning/tasks/DEV-TASK-WSC-004.md
  - planning/approved/PLAN-WSC-1.1.md
```

## 结论

**APPROVE** — 本轮无 P0/P1。目录浏览落在互斥子路径 `catalog/browse/**`；**未**创建或填充 `catalog/detail|editor|admin/**`（前后端均无对应目录）。只读 GET categories/products/products/{id} 需登录；写占位仍由 `StubWriteController`（POST/PUT）共存无冲突。三栏+多维筛选+大小写不敏感搜索+无结果空态（`catalog-empty`）+预览三入口（编辑受 `productWriteVisible`，USER 无按钮）符合 REQ-CAT-001..003。正式 VERIFIED 仍须独立 tester。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-004-R1-001 | P2 | `CatalogBrowsePage` 原生 HTML 三栏/筛选，未用 ant-design-vue（与 002/003 同类栈弱对齐） | UI 统一任务改用栈内组件或书面冻结 | REQ-CAT-001 |
| FIND-WSC-004-R1-002 | P2 | 预览权限可见性依赖 `useCanWrite`；本任务单测未覆盖 USER 无编辑按钮（集成/组件级） | tester 三角色抽样或补 vitest | REQ-CAT-003 |
| FIND-WSC-004-R1-003 | P3 | 工作区 `routes.ts` 同时含 overview/chain 接线；DEV 自述仅改 catalog。并行合并下无法按文件拆分归属；若本 actor 改了非 catalog 行则为越界 | 编排器按 SCOPE_AMEND 分拆提交/校验 | SCOPE_AMEND |
| FIND-WSC-004-R1-004 | P3 | 401 负例仅覆盖 `/categories`；products 同 `requireLogin` | 可选补全 | 安全门禁 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. allowModify / denyModify 与无 detail/editor

| 项 | 结果 |
|---|---|
| 新增仅 `frontend/src/features/catalog/browse/**`、`backend/**/catalog/browse/**`、测试 | PASS |
| **无** `catalog/detail/**`、`catalog/editor/**`、`catalog/admin/**`（前后端 glob 为空） | PASS |
| 未改 contracts/api/migration/overview/chain/security/rbac | PASS |
| `routes.ts` catalog 列表 `component` → `CatalogBrowsePage`；detail/edit/new 仍 `PlaceholderView` | PASS |
| 预览入口仅 `router.push` 占位路由，未实现详情/编辑页 | PASS |

### 2. 契约对齐

| 项 | 证据 | 结果 |
|---|---|---|
| GET 路径与查询参数 | 对齐 OpenAPI `l1/l2/dataSource/productType/involvesPublicData/deliveryMethod/q/page/pageSize` | PASS |
| 列表分页体 | `items/page/pageSize/total` | PASS |
| 预览含 `chainCount`、敏感信用代码 | 集成测 + `SensitiveId` | PASS |
| 与 Stub 写路径方法不冲突 | Browse 仅 GET；Stub 仅 POST/PUT | PASS |

### 3. 安全（读接口需登录）

| 项 | 证据 | 结果 |
|---|---|---|
| 三 GET `requireLogin` | `CatalogBrowseController` | PASS |
| 未登录 401 | `unauthenticated_getCategories_401` | PASS |

### 4. 空态 / §3.5 / 权限入口

| 项 | 证据 | 结果 |
|---|---|---|
| 无结果空列表 | 集成测 `total=0`；UI `data-testid=catalog-empty` + 固定文案 | PASS |
| loading/error+重试 | `catalog-loading` / `catalog-error` | PASS |
| 编辑按钮 `v-if="productWriteVisible"` | USER 无 `catalog-go-edit` | PASS（代码审查；tester 验三角色） |
| §3.6 信用代码截断复制 | `SensitiveId` + truncate 单测 | PASS |

### 5. 自测证据（审查侧抽样）

| 项 | 结果 |
|---|---|
| DEV：`CatalogBrowseIntegrationTest` 8 PASS | 已声明 |
| DEV：vitest catalog/browse 5 PASS；typecheck exit 0 | 已声明 |

## 残余风险（交 tester）

- USER/PROVIDER/ADMIN 预览编辑按钮可见性须在 TESTRUN 实测。
- 筛选失败 error 态依赖前端 mock 或断网联调。
- 并行 `routes.ts` 合并后须确认 detail/edit 路由仍为占位（本审查时仍为 PlaceholderView）。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称 testScope 通过。
- **decision: APPROVE**（进入独立 tester 门禁）。
