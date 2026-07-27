---
id: skill.migration-review
version: 0.1.0
status: draft
owner: tech-lead
compatibleRoles: [migrationReviewer]
---

# Migration Reviewer Skill（实例化时填写）

> 本文件由项目的**迁移/数据评审负责人**根据真实迁移工具、路径与回滚方式制定，不由 AI 猜测数据栈。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草工具路径、必查项、回滚剧本与数据分级 | 迁移/数据评审负责人 |
| 安全与脱敏规则会签 | Security/Ops（如适用） |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

回滚与分级未可执行前须保持 `draft`。

## 填写完成标准

- 迁移工具、文件路径、回滚方式明确
- 回滚剧本含触发阈值、步骤、验证与责任人
- 数据分级不含真实密钥或生产样本
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 迁移评审负责人完成填写
- [ ] 回滚剧本可执行或有明确 N/A
- [ ] 文件 Owner 批准激活

## 工具与路径

- 迁移工具：_待填_
- 迁移文件路径：_待填_
- 回滚方式：_待填_

## 必查

- [ ] 向前兼容 / 锁表风险
- [ ] 回滚剧本可执行
- [ ] 数据分级与脱敏
- [ ] 是否需停机窗口
