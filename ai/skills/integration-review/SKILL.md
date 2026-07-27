---
id: skill.integration-review
version: 0.1.0
status: draft
owner: tech-lead
compatibleRoles: [integrationReviewer]
---

# Integration Reviewer Skill（实例化时填写）

> 本文件由项目的**集成审查负责人**（可与 QA/前端协作）根据真实冒烟与契约测试入口制定，不由 AI 猜测集成命令。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草并行束清单、冒烟与契约测试入口 | 集成审查负责人 |
| 前端入口冒烟命令与路径 | 前端 + QA（如有前端） |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

冒烟/契约入口未试跑前须保持 `draft`。

## 填写完成标准

- 并行束检查覆盖跨任务交互，而非重复单任务自测
- 冒烟与契约命令可复制执行，或明确 N/A
- `BLOCKED` 不得记为通过；失败可归因到任务或环境
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 集成审查负责人完成填写
- [ ] QA 复核通过标准
- [ ] 文件 Owner 批准激活

## 并行束合并后检查

- [ ] 跨任务行为与契约一致
- [ ] 合并顺序符合 DAG / mergeAfter
- [ ] 无未声明写冲突残留
- [ ] 冒烟路径：_待填命令或清单_

## 契约测试入口

- _待填_
