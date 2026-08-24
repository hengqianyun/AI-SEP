# ChainAttestationPort（wsc-contracts@2.0.0）

> 对齐 DEC-WSC-002 / REQ-CHAIN-001 / REQ-CAT-005。  
> 业务层仅依赖本接口；禁止直连真实链 SDK。  
> V1.1：`catalogSnapshot` 须含三级分类路径字段（`categoryPath` / `categoryPathParts`）。

## 职责

将**目录快照**提交至可替换的存证适配层，返回存证与权属证书字段。

## 输入

| 字段 | 类型 | 说明 |
|---|---|---|
| `catalogSnapshot` | object / JSON | 产品目录上链快照（字段覆盖 REQ-CAT-004 分组或文档化子集） |
| `productCode` | string | 产品编码 |
| `versionNo` | integer | 期望版本号（新建为 1，编辑递增） |

## 输出（必填）

| 字段 | 类型 | 说明 |
|---|---|---|
| `metadataHash` | string | 元数据哈希 |
| `ownerDID` | string | 权属 DID |
| `timestamp` | string (ISO-8601) | 存证时间戳 |
| `certificate.owner` | string | 权属证书权属方 |

## Java 端口签名（脚手架对齐）

```java
package com.wsc.chain;

public interface ChainAttestationPort {
  AttestationResult attest(AttestationRequest request);

  record AttestationRequest(String productCode, int versionNo, String catalogSnapshotJson) {}

  record AttestationResult(
      String metadataHash,
      String ownerDID,
      java.time.Instant timestamp,
      Certificate certificate
  ) {
    public record Certificate(String owner) {}
  }
}
```

## 约束

- V1 实现可为模拟适配；字段语义与可观测性不变。
- 适配失败时调用方须整单回滚，不产生半成品产品行或孤儿上链行。

## V1.7 并列接口

> V1.7 新增 `OrderChainAttestationPort`（`contracts/chain/OrderChainAttestationPort.md`），面向**订单**状态变更存证。
> 本接口（`ChainAttestationPort`）继续面向**产品目录快照**存证，签名与行为不变。
> 两接口并列共存，互不干扰。
