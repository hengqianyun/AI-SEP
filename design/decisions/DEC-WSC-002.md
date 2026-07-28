---
decisionId: DEC-WSC-002
title: V1 上链采用模拟存证适配层
status: APPROVED
date: 2026-07-28
owner: productOwner
source: WSC 试点默认裁决（plan Phase 0）
closes: [OQ-003]
affects: [REQ-CHAIN-001, REQ-CAT-005]
snapshotRefs: []
supersedes: []
---

# DEC-WSC-002 V1 上链采用模拟存证适配层

## 背景

OQ-003：是否对接真实存证链未定；真实链接入超出试点最小范围。

## 决策

- V1 使用**可替换的模拟存证适配层**生成存证记录。
- 每条记录至少包含：`metadataHash`、`ownerDID`、`timestamp`、`certificate.owner`。
- 业务层只依赖适配层接口，禁止直连具体链 SDK。
- 验收可用模拟哈希/DID；UI 须完整可读展示上述字段。

## 未选择方案

| 方案 | 未采用原因 |
|---|---|
| V1 直连真实区块链节点 | 运维与合规成本高，阻塞试点交付 |
| 不上链仅本地日志 | 无法满足上链信息页与版本溯源验收 |

## 影响

- 技术影响：`backend/.../chain/` 提供 Adapter 接口；后续真实链实现替换适配器即可
- 重评触发：Tech Lead 批准真实链对接 ADR
