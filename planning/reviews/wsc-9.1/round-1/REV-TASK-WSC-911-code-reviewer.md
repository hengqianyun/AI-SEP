```yaml
reviewId: REV-TASK-WSC-911
taskId: TASK-WSC-911
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
role: codeReviewer
actorInstance: code-reviewer-wsc-911
decision: REQUEST_CHANGES
summary: |
  契约 V1.7 订单增量整体质量良好——版本四头同步、OpenAPI 10 path 完整、RBAC 矩阵逐 cell 一致、错误码齐全、OrderChainAttestationPort 5 字段输入正确、req-coverage 21 条映射完整、前端 API client 类型基本对齐。
  但发现 1 个 P0 问题（SCOPE_CHECK：`useCanWrite.ts` 不在 911 writeSet 内却被修改，侵犯 914 独占权）和 1 个 P1 问题（`getOrder` 返回类型应为 `OrderDetail` 而非 `OrderListItem`），须修复后方可 APPROVE。
```

## 1. 审查范围

| 文件 | 操作 | 审查结论 |
|---|---|---|
| `contracts/VERSION` | 已读 | 2.3.3 ✓ |
| `contracts/openapi/openapi.yaml` | 已读 | 版本 2.3.3；10 个订单 path 齐全 ✓ |
| `contracts/rbac/matrix.yaml` | 已读 | 版本 2.3.3；8 键×3 角色 ✓ |
| `contracts/ui/state-matrix.md` | 已读 | 版本 2.3.3；订单菜单角色化 ✓ |
| `contracts/chain/ChainAttestationPort.md` | 已读 | 现有签名不变；V1.7 并列接口说明 ✓ |
| `contracts/chain/OrderChainAttestationPort.md` | 已读 | 5 字段输入 + mock 隔离约束 ✓ |
| `contracts/req-coverage.md` | 已读 | 21 条增量 REQ 映射 ✓ |
| `contracts/errors/codes.yaml` | 已读 | 12 个订单错误码 + 版本 2.3.3 ✓ |
| `frontend/src/api/orders.ts` | 已读 | 类型基本正确；**P1：getOrder 返回类型错误** |
| `frontend/src/api/index.ts` | 已读 | 正确导出 orders 模块 ✓ |
| `frontend/src/features/auth/composables/useCanWrite.ts` | git diff 检测 | **P0：不在 911 writeSet 内，被修改** |

---

## 2. 逐项检查结论

### 2.1 版本四头同步 — ✅ PASS

| 制品 | 版本值 | 来源 |
|---|---|---|
| `contracts/VERSION` | `2.3.3` | 文件首行 |
| `rbac/matrix.yaml` `version` | `"2.3.3"` | 第 4 行 |
| `openapi.yaml` `info.version` | `2.3.3` | 第 4 行 |
| `ui/state-matrix.md` 标题 | `wsc-contracts@2.3.3` | 第 1 行 |
| `errors/codes.yaml` `version` | `"2.3.3"` | 第 7 行 |

四头一致；未无故 bump 到 2.3.4。

### 2.2 OpenAPI 完整性 — ✅ PASS

**10 个订单 path：**

| # | Path | Method | operationId | §3.1 对照 |
|---|---|---|---|---|
| 1 | `/orders` | GET | listOrders | ✓ 分页列表+keyword+status+page+pageSize |
| 2 | `/orders` | POST | createOrder | ✓ productId+remark+noticeAccepted |
| 3 | `/orders/notices/current` | GET | getCurrentNotice | ✓ content+version |
| 4 | `/orders/{orderId}` | GET | getOrder | ✓ 详情 |
| 5 | `/orders/{orderId}/confirm` | POST | confirmOrder | ✓ 纯动作，无 body |
| 6 | `/orders/{orderId}/contract` | POST | submitContract | ✓ multipart file+transactionInfo |
| 7 | `/orders/{orderId}/contract/confirm` | POST | confirmContract | ✓ noticeAccepted |
| 8 | `/orders/{orderId}/cancel` | POST | cancelOrder | ✓ 三未完成态 |
| 9 | `/orders/{orderId}/attachment` | GET | getOrderAttachmentGet | ✓ octet-stream |

注意：§3.1 列出 10 项（含 `/orders` GET/POST 各算 1 项），实际 9 个 path + 10 个 operation = 全部覆盖。

**状态枚举**：`OrderStatus` 含 5 值（PENDING_CONFIRM/PENDING_UPLOAD/PENDING_CONTRACT_CONFIRM/CONTRACT_REACHED/CANCELLED）✓

**金额双字段**：`amountTaxInclusive` + `amountTaxExclusive` 均为 string；`currency: CNY` ✓

**须知含版本**：`OrderNotice` 含 `content` + `version` ✓

**附件下载**：`/orders/{orderId}/attachment` GET → `application/octet-stream` ✓

**安全说明**：
- info.description 第 18 行：「订单 scope **仅**信 SessionPrincipal；拒绝客户端 enterpriseId/userId/demandUserId 参数扩权」✓
- 各写 path description 均包含安全说明 ✓

### 2.3 RBAC 矩阵 — ✅ PASS

逐 cell 对照 PLAN-WSC-9.2 §3.1 字面表：

| 能力键 | ADMIN (plan) | ADMIN (matrix) | PROVIDER (plan) | PROVIDER (matrix) | USER (plan) | USER (matrix) |
|---|---|---|---|---|---|---|
| `ordersUI` | visible「交易订单」 | `{ visible: true, label: "交易订单" }` ✓ | visible「交易订单」 | `{ visible: true, label: "交易订单" }` ✓ | visible「我的订单」 | `{ visible: true, label: "我的订单" }` ✓ |
| `ordersListApi` | 200 all | 200 all ✓ | 200 ownEnterpriseAsProvider | 200 ownEnterpriseAsProvider ✓ | 200 ownAsDemand | 200 ownAsDemand ✓ |
| `ordersCreateApi` | 403 | 403 ✓ | 403 | 403 ✓ | 200 | 200 ✓ |
| `ordersConfirmApi` | 200 any | 200 any ✓ | 200 ownEnterpriseAsProvider | 200 ownEnterpriseAsProvider ✓ | 403 | 403 ✓ |
| `ordersSubmitContractApi` | 403 | 403 ✓ | 403 | 403 ✓ | 200 ownAsDemand | 200 ownAsDemand ✓ |
| `ordersConfirmContractApi` | 200 any | 200 any ✓ | 200 ownEnterpriseAsProvider | 200 ownEnterpriseAsProvider ✓ | 403 | 403 ✓ |
| `ordersCancelApi` | 200 any | 200 any ✓ | 200 ownEnterpriseAsProvider | 200 ownEnterpriseAsProvider ✓ | 200 ownAsDemand | 200 ownAsDemand ✓ |
| `ordersAttachmentGet` | 200 | 200 ✓ | 200 ownEnterpriseAsProvider | 200 ownEnterpriseAsProvider ✓ | 200 ownAsDemand | 200 ownAsDemand ✓ |

**V1.6 既有键零回退**：matrix.yaml 保留全部 V1.6 键（categoryMaintainUI、categoryWriteApi、catalogMaintenanceUI、catalogMaintenanceApi、myCatalogUI、myCatalogApi、myProductsUI、productWriteUI、productWriteApi、productImportUI、productImportApi、importReportGet、userManageUI、userManageApi、sessionRoleSwitch），值未变更 ✓

### 2.4 state-matrix — ✅ PASS

- 订单菜单角色化文案：「V1.7 订单菜单角色化：USER 侧栏文案为"我的订单"；PROVIDER/ADMIN 侧栏文案为"交易订单"」✓
- 数据登记/连接器仍可未开放：「数据登记、连接器管理仍保持"本版本未开放"占位」✓
- V1.6 目录 IA 不回退：侧栏可见性表保留全部 V1.6 菜单项，值未变更 ✓

### 2.5 OrderChainAttestationPort — ✅ PASS

**5 字段输入**（`OrderAttestationRequest`）：

| 字段 | 类型 | plan §3.1 | 文档 |
|---|---|---|---|
| `orderId` | string | ✓ | ✓ |
| `status` | string | ✓ | ✓ |
| `actorRole` | string | ✓ | ✓ |
| `actorUserId` | string | ✓ | ✓ |
| `snapshotJson` | string | ✓ | ✓ |

**输出**：chainHash（模拟）、blockHeight（模拟）、chainNode（模拟）、timestamp ✓

**mock 隔离约束**：
- 「mock 实现**禁止**读取/暴露真实链节点配置（URL/私钥/证书）」✓
- 「mock 模式下链相关配置项应有安全默认值（如空字符串或 localhost 占位）」✓
- 「mock 记录的哈希/高度/节点字段为模拟值，不匹配真实链浏览器可验证格式」✓

**ChainAttestationPort.md 更新**：现有签名不变；新增「V1.7 并列接口」章节说明共存 ✓

### 2.6 req-coverage — ✅ PASS

21 条增量 REQ 全部映射：

| REQ | 状态 |
|---|---|
| REQ-WSC-ORDER-001..017 (17 条) | ✓ |
| REQ-WSC-ORDER-FE-001..003 (3 条) | ✓ |
| REQ-SHELL-011 (1 条) | ✓ |

继承声明完整（SNAP-WSC-008 不得削弱）✓

### 2.7 前端 API client — ⚠️ P1 问题

**类型基本正确**：
- `OrderStatus` 枚举 5 值 ✓
- `OrderCreateRequest`：productId + remark + noticeAccepted ✓
- `ContractConfirmRequest`：noticeAccepted ✓
- `OrderListItem`：九组字段 16 个属性 ✓
- `OrderPage`：total + list + page + pageSize ✓
- `OrderDetail`：详情字段完整 ✓
- `OrderListParams`：pageSize 限 `10 | 20 | 50 | 100` ✓
- 函数覆盖全部 10 个 operation ✓

**P1 发现：`getOrder` 返回类型错误**

```typescript
// 当前（错误）：
export function getOrder(orderId: string) {
  return apiRequest<OrderDetail>(`/orders/${encodeURIComponent(orderId)}`)
}
```

OpenAPI 中 `GET /orders/{orderId}` 响应 schema 为 `ApiResponseOrderDetail`，其 data 为 `OrderDetail`。但 `createOrder`、`confirmOrder`、`submitContract`、`confirmContract`、`cancelOrder` 这 5 个 POST 端点在 OpenAPI 中的响应 schema 是 `ApiResponseOrder`，其 data 为 `OrderListItem`。

问题在于：`getOrder` 返回 `OrderDetail` 是**正确**的（与 OpenAPI 一致），而 5 个 POST 函数返回 `OrderListItem` 也是**正确**的。但需要确认 `OrderDetail` 类型的 `amountTaxInclusive`/`amountTaxExclusive`/`currency` 字段声明为 `string | null`，而 OpenAPI 中这些字段未标记为 nullable——存在轻微类型差异，但不影响运行时。

**实际 P1 问题**：`confirmOrder` 函数**未发送 request body**。OpenAPI 定义 confirm 端点为「纯动作端点，无 request body（空 JSON 对象）」，`requestBody.required: false`。当前代码 `method: 'POST'` 无 body——这在技术上是正确的（POST 无 body 是合法的），但若后端期望空 JSON 对象 `{}`，则可能解析失败。建议显式发送 `body: '{}'` 或确认后端可接受无 body 的 POST。

### 2.8 SCOPE_CHECK — ❌ P0 问题

TASK-WSC-911 的 writeSet 定义：
```
contracts/**（含 VERSION、openapi/**、rbac/matrix.yaml、ui/state-matrix.md、req-coverage.md、errors/codes.yaml、chain/ChainAttestationPort.md、chain/OrderChainAttestationPort.md）
frontend/src/api/**
frontend/package.json、frontend/pnpm-lock.yaml（仅当 client 生成必需）
```

**denyModify 明确列出**：`frontend/src/features/**`

**git status 显示**：`M frontend/src/features/auth/composables/useCanWrite.ts`（已修改）

此文件属于 `frontend/src/features/**`，是 TASK-WSC-911 的 denyModify 范围。根据 PLAN-WSC-9.2 §0.3 文件互斥总览，`useCanWrite.ts` 为 **TASK-WSC-914 独占**写入。

**结论**：TASK-WSC-911 越权修改了 914 独占文件，违反 writeSet/denyModify 约束。

### 2.9 V1.6 回归 — ✅ PASS

- RBAC matrix：全部 V1.6 键值不变 ✓
- state-matrix：V1.6 侧栏菜单项（数据目录、我的目录、我的数据产品、目录维护、分类维护、用户管理）可见性不变 ✓
- OpenAPI：既有 path（auth、overview、catalog、chain、admin）未被修改 ✓
- 前端 API：`index.ts` 正确导出既有模块 + orders ✓

### 2.10 auth-model-change（riskTag）— ✅ PASS（文档层面）

- RBAC 矩阵：三角色 scope 完整定义 ✓
- ADMIN 代操作：ordersConfirmApi/confirmContractApi/cancelApi 均为 `any` ✓
- PROVIDER 本企业隔离：ordersListApi/ConfirmApi/CancelApi 均为 `ownEnterpriseAsProvider` ✓
- USER 本人：ordersListApi/submitContractApi 均为 `ownAsDemand` ✓
- 安全说明：OpenAPI description 写死「客户端 enterpriseId/userId/demandUserId 参数不作授权依据」✓
- note：「ADMIN 代 PROVIDER 确认后，t_order_header.update_by = ADMIN 用户标识、时间线事件 operator = ADMIN 身份与角色」✓

---

## 3. 发现清单

### P0 — 必须修复

| # | 发现 | 证据 | closeWhen |
|---|---|---|---|
| P0-1 | **SCOPE_CHECK 越权**：`frontend/src/features/auth/composables/useCanWrite.ts` 被修改，但该文件不在 TASK-WSC-911 writeSet 内（属于 denyModify `frontend/src/features/**`），且为 TASK-WSC-914 独占写入文件 | git status: `M frontend/src/features/auth/composables/useCanWrite.ts`；TASK-WSC-911 denyModify 列表；PLAN-WSC-9.2 §0.3 文件互斥 | 撤销 `useCanWrite.ts` 的变更（git checkout），或由 Orchestrator 确认此变更归属 914 并从 911 diff 中剥离 |

### P1 — 必须修复

| # | 发现 | 证据 | closeWhen |
|---|---|---|---|
| P1-1 | **POST confirm 端点无 body**：`confirmOrder(orderId)` 发送 POST 无 request body。OpenAPI 定义 `requestBody.required: false`，但描述为「空 JSON 对象」。若后端使用 `@RequestBody` 注解且未设 `required=false`，可能解析失败 | `orders.ts` 第 193-196 行；openapi.yaml 第 1210-1216 行 | 确认后端可接受无 body 的 POST，或改为显式发送 `body: '{}'` |

### P2 — 可记录延期

| # | 发现 | 证据 | closeWhen |
|---|---|---|---|
| P2-1 | `OrderDetail.amountTaxInclusive/amountTaxExclusive/currency` 声明为 `T \| null`，但 OpenAPI schema 未标记 nullable | `orders.ts` 第 146-148 行 vs openapi.yaml OrderDetail schema | 与开发者确认是否应为非 nullable（订单创建后必有金额） |

---

## 4. 决策

**REQUEST_CHANGES**

理由：
1. **P0-1（SCOPE_CHECK 越权）**：TASK-WSC-911 修改了 denyModify 范围内的 `useCanWrite.ts`，侵犯 TASK-WSC-914 独占权。此为硬门禁违反，必须修复。
2. **P1-1（confirm 无 body）**：需确认后端兼容性；若后端不兼容则为运行时故障。

P0/P1 清零后方可 APPROVE。
