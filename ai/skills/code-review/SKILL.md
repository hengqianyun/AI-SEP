---
id: skill.code-review
version: 0.1.0
status: draft
owner: tech-lead
inputSchema: task-package@1
outputSchema: code-review@1
compatibleRoles: [codeReviewer]
---

# Code Review Skill（实例化时填写）

> 本文件由项目的**审查负责人或 Tech Lead**根据真实仓库制定，合并前端与后端审查项，不由 AI 猜测检查细则。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草前后端必查项、阻塞与非阻塞边界 | 审查负责人 / Tech Lead |
| 确认与 Coding / Architecture Rule 一致 | Tech Lead |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

若前后端检查项尚未按仓库实例化，本文件必须保持 `draft`；Orchestrator 不得当作已激活审查规范装载。

## 填写完成标准

- 通用、前端、后端检查项均已填写，或明确 `N/A` 及原因
- 阻塞条件可客观判定；纯风格项不得升为 P0/P1（除非已写入 Rule）
- 输出模板与批准门禁明确
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 审查负责人完成填写
- [ ] Tech Lead 确认与适用 Rule 无冲突
- [ ] `context-map.yaml` 已按需装载本 Skill
- [ ] 文件 Owner 批准激活

## 必查清单（项目）

1. 正确性：是否满足任务目标与 REQ
2. 范围：是否越出 allowModify / 偷扩需求
3. 架构：是否违反 RULE-PROJECT-ARCHITECTURE
4. 安全：密钥、注入、鉴权、数据暴露
5. 契约：API/数据是否与批准版本一致
6. 测试：必要层级是否存在且可执行

## 不可作为 P0/P1 阻塞的风格项

- _待填（与 RULE-ORG-CODING 对齐）_

## 输出

使用 `ai/schemas/templates/code-review.md`；P0/P1 清零前不得批准。
