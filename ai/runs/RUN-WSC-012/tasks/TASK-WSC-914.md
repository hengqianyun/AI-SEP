# 任务包 — TASK-WSC-914：开放订单导航 + FE 门控（独占 useCanWrite）

```yaml
taskId: TASK-WSC-914
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: A
status: PENDING
requirements: [REQ-SHELL-011, REQ-WSC-ORDER-001]
dependsOn: [TASK-WSC-911]
```

## 1. 任务目标

从 `navClosed` 移除订单占位；USER 侧栏「我的订单」、PROVIDER/ADMIN「交易订单」进入 `/orders`；深链门控；**独占** `useCanWrite.ts` 增订单导出且 **不削弱** V1.6 目录函数

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `frontend/src/layouts/WorkbenchLayout.vue` | 修改 | 移除订单占位，按角色显示菜单文案 |
| `frontend/src/layouts/WorkbenchLayout.spec.ts` | 新增/修改 | 布局测试 |
| `frontend/src/features/auth/composables/useCanWrite.ts` | 修改 | 新增 `canSeeOrders` 等导出 |
| `frontend/src/features/auth/composables/useCanWrite.spec.ts` | 新增/修改 | 权限测试 |
| `frontend/src/router/routes.ts` | 修改 | 增 `/orders` 列表占位页或空壳；meta.title 可按角色在页内设置 |
| `frontend/src/features/shell/deepLinkAccess.ts` | 修改 | 深链门控 |
| `frontend/src/features/shell/navSurfaces.ts` | 修改 | 订单路由常量 `ROUTE_ORDERS`；**不得**破坏四目录 surface |
| `frontend/src/features/shell/**` 相关 spec | 新增/修改 | 壳层测试 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md`（REQ-SHELL-011 / ORDER-001） | 需求 |
| `frontend/src/api/**` | 只读，911 生成物 |
| `contracts/rbac/matrix.yaml` | 只读，RBAC 矩阵 |
| `WorkbenchLayout.vue`、`useCanWrite.ts`、`routes.ts`、`deepLinkAccess.ts`、`navSurfaces.ts` | 当前实现 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `frontend/src/features/catalog/**` | 目录归 8.3 |
| `frontend/src/features/order/**` | 页实现归 915 |
| `contracts/**` | 契约归 911 |
| `frontend/src/api/**` | API client 归 911 |
| `backend/**` | 后端归 912/913/916 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |
| `frontend/src/styles/**`、`frontend/src/theme/**` | 样式只读 |

## 5. 验收标准（acceptance）

- [ ] USER：可见「我的订单」；不可见「交易订单」文案作为自己的菜单标签；点击进入 `/orders`；**不再**出现订单「本版本未开放」
- [ ] PROVIDER/ADMIN：可见「交易订单」；进入 `/orders`
- [ ] 数据登记、连接器管理仍未开放
- [ ] V1.6：数据目录收纳、我的目录、目录维护（仅 ADMIN）、我的数据产品、用户管理 **可见性不回退**
- [ ] `useCanWrite`：新增 `canSeeOrders`（三角色 true）等；既有 `canSeeMyCatalog`/`canMaintainCatalog` 行为不变
- [ ] 未登录处理与既有壳层一致

## 6. 测试范围（testScope）

- Vitest 三角色菜单文案与 `navClosed` 不再含 orders 占位
- Vitest：USER 仍不可达 `/catalog/maintenance`、`/my-catalog`、`/my-products`、`/admin/users`
- Vitest：PROVIDER 仍不可达目录维护
- 命名用例：`914-orders-nav-open`、`914-v16-catalog-nav-regression`

## 7. 风险标签（riskTags）

- `shell-nav`
- `rbac-visibility`
- `auth-model-change`
