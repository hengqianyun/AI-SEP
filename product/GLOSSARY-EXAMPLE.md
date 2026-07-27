# acme-orders 术语表已填写示例

> 仅展示填写粒度，不代表真实产品域。复制后保存为 `product/glossary.md` 并按业务确认。

```markdown
# acme-orders 术语表

| 术语 | 英文/代码标识 | 定义 | 边界/不包含 | 易混淆术语 | 维护人 |
|---|---|---|---|---|---|
| 订单 | Order | 用户一次提交购买意图的聚合根，含行项目与状态 | 不包含支付渠道侧交易单 | 购物车（未提交）、支付单 | productOwner |
| 订单行 | OrderLine | 订单内单个 SKU 的数量与价格快照 | 不包含运费模板配置本身 | 购物车行 | productOwner |
| 商品 | Product | 可售卖的可视化商品信息（标题、描述、主图） | 不包含库存与价格生效规则 | SKU | productOwner |
| SKU | Sku | 可下单的最小库存单位，含售价与库存 | 不包含组合促销包 | Product、SPU | productOwner |
| 待付款 | PENDING_PAYMENT | 订单已创建、尚未完成模拟支付的状态 | 不包含支付网关处理中 | 已付款 | productOwner |
| 模拟支付 | MockPayment | V1 内置的伪支付确认，不产生真实扣款 | 不是生产支付 | 退款、对账 | productOwner |

## 待确认术语

- 运费：V1 是否固定免运费；确认人：productOwner；期限：2026-08-01
```
