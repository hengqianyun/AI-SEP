---
decisionId: DEC-WSC-006
title: 产品「行业分类」为 GB/T 4754 门类字段，非目录树节点
status: ACCEPTED
decidedAt: 2026-08-03
runId: RUN-WSC-007
supersedes: []
---

# DEC-WSC-006 产品行业分类语义

## 背景

v0729 导入模板样例行使用「建筑业」等 GB/T 4754—2017 **门类**名称；现网将「行业分类」解析为目录 L3 挂载点，导致 `ERR_CATEGORY_LEAF_REQUIRED`。PO 澄清：该字段**不是**分类树节点。

## 决策

1. 产品字段 `industryCategory`：取自导入模板 C 列 dataValidation 的 20 个门类枚举（中文原文）。
2. 新增/编辑/导入的枚举下拉（产品类型、行业分类、交付方式、地域范围、更新频率、数据形态）**对齐** v0729 模板对应列的下拉选项。
3. **方案 A**：编辑与导入**不再强制**选择目录 L1/L2/L3 挂载；`l3CategoryId` 可空/弱化。目录树维护能力保留，但不作为本字段语义。
4. 导入不得再因「仅二级/非叶子」拒绝合法门类枚举值。

## 后果

- 契约 `ProductWrite` 必填从 `l3CategoryId` 调整为含 `industryCategory`；版本升补丁/次版本由实现任务完成。
- 目录浏览对无挂载产品须有可预期展示，避免静默丢失。

## 补充澄清（2026-08-03）

PO：导入/无挂载产品须归入浏览分节 **「未分类数据」**；**不得**将 `industryCategory`（GB/T 门类）作为目录节点或分节标题。`industryCategory` 仅作产品字段展示/筛选值。
