# OrderChainAttestationPort（wsc-contracts@2.3.3）

> 对齐 PLAN-WSC-9.2 §3.1 / ISSUE-API-001。
> 与现有 `ChainAttestationPort`（面向产品目录快照）**并列共存**。
> 业务层仅依赖本接口；禁止直连真实链 SDK。
> mock 实现**禁止**读取/暴露真实链节点配置（URL/私钥/证书）。

## 职责

将**订单状态变更事件**提交至可替换的存证适配层，返回 mock 存证字段。
每次订单状态变更（创建/确认订单/提交合约/确认合约/取消）均调用本接口。

## 输入（OrderAttestationRequest）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `orderId` | string | 是 | 订单 ID（UUID v4 格式） |
| `status` | string | 是 | 当前状态枚举值（PENDING_CONFIRM/PENDING_UPLOAD/PENDING_CONTRACT_CONFIRM/CONTRACT_REACHED/CANCELLED） |
| `actorRole` | string | 是 | 操作人角色（ADMIN/PROVIDER/USER） |
| `actorUserId` | string | 是 | 操作人用户 ID |
| `snapshotJson` | string | 是 | 订单快照摘要（JSON 字符串，含订单核心字段） |

## 输出（OrderAttestationResult）

| 字段 | 类型 | 说明 |
|---|---|---|
| `chainHash` | string | 模拟哈希值（不含真实链格式；mock 实现用 UUID 或随机 hex） |
| `blockHeight` | integer | 模拟区块高度（mock 实现用自增计数） |
| `chainNode` | string | 模拟节点标识（mock 实现用固定占位符，**禁止**真实链节点 URL） |
| `timestamp` | string (ISO-8601) | 存证时间戳 |

## Java 端口签名（脚手架对齐）

```java
package com.shdata.datachain.common.port;

public interface OrderChainAttestationPort {
  OrderAttestationResult attestOrder(OrderAttestationRequest request);

  record OrderAttestationRequest(
      String orderId,
      String status,
      String actorRole,
      String actorUserId,
      String snapshotJson
  ) {}

  record OrderAttestationResult(
      String chainHash,
      long blockHeight,
      String chainNode,
      java.time.Instant timestamp
  ) {}
}
```

## 约束

- V1 实现为 **SimulatedOrderChainAttestationAdapter**（mock）；字段语义与可观测性不变。
- mock 适配器**禁止**读取/暴露真实链节点配置（URL/私钥/证书）；mock 模式下链相关配置项应有安全默认值（如空字符串或 localhost 占位）。
- mock 记录的哈希/高度/节点字段为模拟值，不匹配真实链浏览器可验证格式。
- 适配失败时调用方**不得阻断**订单主流程（PO 决策：mock；实现假设：捕获适配异常仍写本地 mock 行或降级占位记录，状态仍成功）。
- 列表 `chainCount` = 详情 `chainLogs.length`（= 时间线记录条数）。
