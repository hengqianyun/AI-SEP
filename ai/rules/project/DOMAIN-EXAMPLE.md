# RULE-PROJECT-DOMAIN 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认业务域。以下不变量与规则 ID 均是假设值，复制时必须按真实产品确认。

```markdown
---
id: RULE-PROJECT-DOMAIN
version: 1.0.0
status: active
owner: productOwner
override: allowed
appliesTo:
  roles: ["productAnalyst", "apiDataDesigner", "developer", "qaStrategist"]
reviewBy: 2026-07-20
---

# RULE-PROJECT-DOMAIN

## 不变量（违反即 P0/BLOCK）

### INV-ORDER-001 订单金额不可为负
- 约束：任意时刻订单 `totalAmount >= 0`，明细行 `lineTotal >= 0`
- 适用范围：订单创建、改价、取消退款计算
- 违反后果：财务与对账错误，属 P0
- 检查证据：API 响应字段、DB CHECK 约束、单元测试 `OrderAmountTest`
- 例外：无
- Owner：productOwner

### INV-ORDER-002 已取消订单不可再流转
- 约束：状态为 `CANCELLED` 后禁止任何状态迁移（除审计备注）
- 适用范围：订单状态机
- 违反后果：履约与库存混乱，属 P0
- 检查证据：状态变更 API 返回 `ORDER_001`、集成测试 `OrderCancellationIT`
- 例外：无；历史数据修正须 maintainer + productOwner 工单
- Owner：productOwner

## 业务规则索引

| ID | 摘要 | 触发条件（一句话） | 详细文档 |
|---|---|---|---|
| BR-ORDER-001 | 下单时锁定售价快照 | 用户提交订单 | `product/business-rules/BR-ORDER-001-price-snapshot.md` |
| BR-ORDER-002 | 待付款超时自动取消 | 创建后 30 分钟未支付 | `product/business-rules/BR-ORDER-002-payment-timeout.md` |
| BR-CATALOG-001 | 下架商品不可新下单 | 商品状态 `OFF_SHELF` | `product/business-rules/BR-CATALOG-001-off-shelf.md` |

### 业务规则正文骨架示例（BR-ORDER-001）

### BR-ORDER-001 下单价格快照
- 触发条件：用户点击「提交订单」且购物车校验通过
- 输入/前置状态：购物车行含 `skuId`、数量；商品当前售价可读
- 规则：订单行 `unitPrice` 写入提交时刻售价；后续调价不影响已生成订单
- 输出/状态变化：生成 `PENDING_PAYMENT` 订单
- 优先级/冲突顺序：优先于 BR-CATALOG-001（下架仅阻止新单，不改历史快照）
- 例外与审批：无
- 来源：PRD v1.0 REQ-ORDER-001
- 关联不变量：INV-ORDER-001

## 与术语表

见 `product/glossary.md`。需求与代码命名须与术语表一致；冲突时以术语表 + 已批准 DEC 为准。
```
