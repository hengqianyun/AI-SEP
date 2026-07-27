# DEC-ACME-001 已填写示例（订单状态机）

> 仅展示填写粒度，不代表已批准决策。复制后创建 `design/decisions/DEC-ACME-001.md` 并完成人工批准流程。

```markdown
---
decisionId: DEC-ACME-001
title: 订单状态机与模拟支付
status: APPROVED
date: 2026-07-22
owner: productOwner
source: human-confirmed-2026-07-22
closes: [OQ-001]
affects:
  - REQ-ORDER-001
  - REQ-ORDER-002
  - INV-ORDER-002
  - BR-ORDER-002
snapshotRefs: [SNAP-ACME-001]
supersedes: []
---

# DEC-ACME-001 订单状态机与模拟支付

## 背景

PRD v1.0 需明确订单从创建到完成的最小状态集合。团队曾讨论「提交即已付款」以简化演示，但与运营对账及后续接入真实支付冲突。需在 V1 固定状态机与模拟支付边界。

## 决策

1. V1 状态枚举：`PENDING_PAYMENT` → `PAID` → `SHIPPED` → `COMPLETED`；另设 `CANCELLED` 终止态
2. 创建订单后初始状态为 `PENDING_PAYMENT`
3. 仅用户显式触发「模拟支付」可从 `PENDING_PAYMENT` 进入 `PAID`
4. `CANCELLED` 仅允许从 `PENDING_PAYMENT` 进入；进入后禁止任何迁移（见 INV-ORDER-002）
5. `SHIPPED` / `COMPLETED` 在 V1 可由运营手工推进（无物流集成），须写审计日志

## 未选择方案

| 方案 | 未采用原因 |
|---|---|
| 提交订单即 `PAID` | 无法演练待付款流程，且阻碍未来真实支付接入 |
| 引入支付中 `PAYING` 态 | V1 无真实支付渠道，增加前后端复杂度无收益 |
| 管理员任意跳转任意状态 | 破坏可审计性，与 INV-ORDER-002 冲突 |

## 影响

- 用户/业务影响：运营可清晰区分待付款与已付款；客服话术与筛选器对齐四主态 + 取消
- 后续约束：实现不得跳过 `PAID`；不得自动支付；状态变更须经 domain service 统一入口
- 技术/契约影响：OpenAPI `OrderStatus` enum 与 DB check 约束一致；见 `contracts/openapi/components/OrderStatus.yaml`
- 重评触发：接入真实支付或物流 API 时 Must 新开 DEC

## 反例

- 提交订单后不得自动进入 `PAID`
- 不得对 `CANCELLED` 订单再次支付或发货
- 不得在前端本地改 state 绕过后端校验

## 验收口径

- 新建订单响应 `status=PENDING_PAYMENT`
- 模拟支付 API 对非待付款订单返回 `ORDER_001`
- 取消后同订单支付/发货 API 均拒绝
- 状态时间线按时间升序展示每次合法迁移
```
