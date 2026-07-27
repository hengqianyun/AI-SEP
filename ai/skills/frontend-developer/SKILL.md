---
id: skill.frontend-developer
version: 0.1.0
status: draft
owner: frontend-lead
inputSchema: task-package@1
compatibleRoles: [developer]
---

# Frontend Developer Skill（实例化时填写）

> 本文件由项目的**前端负责人或前端团队**根据真实仓库制定，不由 AI 猜测技术栈。
>
> - 填写模板：[`FRONTEND-TEMPLATE.md`](./FRONTEND-TEMPLATE.md)
> - Vue 3 已填写示例：[`FRONTEND-EXAMPLE-VUE.md`](./FRONTEND-EXAMPLE-VUE.md)（仅展示粒度，不代表默认技术栈）

## 责任与审批

| 环节 | 责任人 |
|---|---|
| 起草技术栈做法、目录约定与命令 | 前端负责人 / 前端团队 |
| 确认可执行命令与测试触发条件 | 前端团队 + 测试人员 |
| 检查与 Org/Project Rule、架构约束一致 | Tech Lead |
| 将 `status` 从 `draft` 改为 `active` | 文件 Owner 或 Tech Lead |

若项目尚未确定前端技术栈，本文件必须保持 `draft`；Orchestrator 不得装载，Developer 不得自行选择 Vue、React 或其他方案。

## 填写完成标准

- 已写明适用前端路径、包管理器、运行时版本来源与工作目录
- 安装、格式、Lint、类型检查、单测、构建、启动等适用命令可直接执行
- 页面、路由、组件、状态、API、样式和测试约定指向真实路径或现有范例
- “必须”“禁止”可由 Code Reviewer / Tester 客观检查
- 不适用项明确写 `N/A` 及原因
- 不存在 `_待填_`、`REPLACE_ME` 或未解释的示例值

## 激活前检查

- [ ] 前端负责人完成填写并实际执行命令
- [ ] 测试人员确认测试命令与证据要求
- [ ] Tech Lead 确认与适用 Rule 无冲突
- [ ] `context-map.yaml` 已按真实前端路径装载 `skill.frontend-developer`
- [ ] 文件 Owner 批准激活
