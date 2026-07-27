---
id: skill.parallel-planner
version: 0.1.0
status: draft
owner: tech-lead
compatibleRoles: [parallelPlanner]
---

# Parallel Planner Skill

## 模块边界（写集提示）

见 `modules.yaml` 与 RULE-PROJECT-LAYOUT。下列路径通常**不能**与消费者任务并行写入：

- _待填：共享契约、公共 schema、路由注册表等_

## 拆分启发式

1. 先契约任务，后实现任务
2. 读写集声明到文件或目录粒度
3. 有语义冲突则串行或拆分共享任务

## 合并顺序

- 遵循任务包 `mergeAfter`；不以完成速度为准
