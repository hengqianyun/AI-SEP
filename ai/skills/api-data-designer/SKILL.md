---
id: skill.api-data-designer
version: 0.1.0
status: draft
owner: tech-lead
compatibleRoles: [apiDataDesigner]
---

# API / Data Designer Skill（实例化时填写）

> 本文件由项目的**API/数据设计负责人**根据真实契约与 Schema 路径制定，不由 AI 猜测落盘位置或兼容策略。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草契约路径、兼容判定与数据衔接 | API/数据设计负责人 |
| 与 Coding Rule / 迁移评审对齐 | Tech Lead + 相关 Owner |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

契约路径未在仓库确认前须保持 `draft`。

## 填写完成标准

- API 契约与 Schema 落盘路径真实可写
- 兼容/破坏性变更判定与升级条件明确
- 迁移衔接可引用 migration-review，不含未批准栈假设
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] API/数据负责人完成填写
- [ ] Tech Lead 确认与 Rule 无冲突
- [ ] 文件 Owner 批准激活

## API

- 风格与错误结构：见 RULE-ORG-CODING
- 契约落盘路径：_待填（如 planning/api-design/）_
- 兼容性策略：_待填（破坏性变更流程）_

## 数据

- DB / 迁移工具：_待填_
- 命名与软删约定：_待填_
- 迁移评审触发：与 `policies.yaml` 对齐
