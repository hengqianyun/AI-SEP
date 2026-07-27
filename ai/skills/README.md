# Skills

> 对应 `design.md` §13。Skill = 某角色在本组织/项目下的**可执行做法**；不改 `ai/agents/` 职责边界。

## 约定

每个 Skill 目录含：

- `SKILL.md`：正文（人类必填技术栈细节）
- 可选 `registry.yaml`：版本元数据（晋升与回滚用）
- `TEMPLATE.md` 或 `*-TEMPLATE.md`：供负责人复制的逐项填写说明（与生效 `SKILL.md` 同目录）
- 可选 `*-EXAMPLE-*.md`：已填写示例；必须标明“仅示例，不是默认技术栈”

`SKILL.md` 是运行时实际装载的生效文件；Template/Example 仅用于人工编写，Orchestrator 不应将示例作为项目规则装载。

首个参考：

- [`frontend-developer/SKILL.md`](./frontend-developer/SKILL.md)
- [`frontend-developer/FRONTEND-TEMPLATE.md`](./frontend-developer/FRONTEND-TEMPLATE.md)
- [`frontend-developer/FRONTEND-EXAMPLE-VUE.md`](./frontend-developer/FRONTEND-EXAMPLE-VUE.md)
- [`backend-developer/SKILL.md`](./backend-developer/SKILL.md)
- [`backend-developer/TEMPLATE.md`](./backend-developer/TEMPLATE.md)
- [`backend-developer/BACKEND-EXAMPLE-SPRING.md`](./backend-developer/BACKEND-EXAMPLE-SPRING.md)

统一假设示例栈（仅粒度参考）：`acme-orders` = Vue3 + Spring Boot；Rule/配置 EXAMPLE 见各 `*-EXAMPLE*` 旁路文件。

## Developer Skill 分层

- `skill.developer`：所有开发任务共用的边界、验证和输出底线
- `skill.frontend-developer`：由前端负责人/团队按真实技术栈制定
- `skill.backend-developer`：由后端负责人/团队按相同模式制定

技术栈 Skill 使用 `compatibleRoles: [developer]`，通过 `context-map.yaml` 按任务路径装载。技术栈未确定时必须保持 `draft`，不得由 Agent 自行选型。

## 状态

模板默认 `status: draft`。填完并将 `status` 改为 `active` 后，Orchestrator 才应装载。

## 与 Rules 的关系

- Rule：约束与红线（做什么/不做什么）
- Skill：步骤、命令、检查清单（怎么做）
