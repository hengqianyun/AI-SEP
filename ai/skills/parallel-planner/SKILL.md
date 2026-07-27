---
id: skill.parallel-planner
version: 0.1.0
status: draft
owner: tech-lead
compatibleRoles: [parallelPlanner]
---

# Parallel Planner Skill（实例化时填写）

> 本文件由项目的**并行规划负责人或 Tech Lead**根据真实模块路径与共享契约规则制定，不由 AI 猜测写集。
>
> - 填写模板：[`TEMPLATE.md`](./TEMPLATE.md)
> - 已填写示例：[`EXAMPLE.md`](./EXAMPLE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草模块写集、冲突规则与合并顺序 | 并行规划负责人 / Tech Lead |
| 与 modules.yaml / 布局规则对齐 | Tech Lead / Maintainer |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

共享路径与冲突规则未确认前须保持 `draft`。

## 填写完成标准

- 模块可修改/共享/禁止并行路径来自真实仓库
- 写集冲突处理与 `mergeAfter` 规则可执行
- 共享契约 `parallelWrite` 策略明确
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 并行规划负责人完成填写
- [ ] 与 modules.yaml / RULE-PROJECT-LAYOUT 一致
- [ ] 文件 Owner 批准激活

## 模块边界（写集提示）

见 `modules.yaml` 与 RULE-PROJECT-LAYOUT。下列路径通常**不能**与消费者任务并行写入：

- _待填：共享契约、公共 schema、路由注册表等_

## 拆分启发式

1. 先契约任务，后实现任务
2. 读写集声明到文件或目录粒度
3. 有语义冲突则串行或拆分共享任务

## 合并顺序

- 遵循任务包 `mergeAfter`；不以完成速度为准
