---
decisionId: DEC-WSC-001
title: 产品编码全局唯一与格式
status: APPROVED
date: 2026-07-28
owner: productOwner
source: WSC 试点默认裁决（plan Phase 0）
closes: [OQ-002]
affects: [REQ-CAT-004, REQ-CAT-005]
snapshotRefs: []
supersedes: []
---

# DEC-WSC-001 产品编码全局唯一与格式

## 背景

PRD 开放问题 OQ-002：产品编码是否强制全局唯一及具体编码规范未定，阻塞登记与校验实现。

## 决策

- V1 产品编码**强制全局唯一**。
- 格式：`{DOMAIN}-{FEATURE}-{NNNN}`，例如 `MED-EMR-0002`。
- `DOMAIN` / `FEATURE` 为大写字母与数字；`NNNN` 为至少 4 位数字（可更长）。
- 新建与编辑提交时后端校验唯一性；冲突返回明确错误码。

## 未选择方案

| 方案 | 未采用原因 |
|---|---|
| 仅前端提示、不强制唯一 | 无法保证目录一致性 |
| UUID 作为用户可见编码 | 可读性差，与原型展示不符 |

## 影响

- 用户/业务影响：登记时须遵守编码格式
- 后续约束：实现不得改为非唯一或静默覆盖
- 重评触发：PO 批准变更编码策略的新 DEC
