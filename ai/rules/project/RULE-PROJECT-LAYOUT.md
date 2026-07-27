---
id: RULE-PROJECT-LAYOUT
version: 0.1.0
status: draft
owner: tech-lead
override: allowed
appliesTo:
  roles: ["parallelPlanner", "developer", "codeReviewer", "orchestrator"]
reviewBy: TBD
---

# 目录与模块地图

> 填写请复制同目录 [`LAYOUT-TEMPLATE.md`](./LAYOUT-TEMPLATE.md)。
> **人类必填**：与真实仓库结构对齐。

## 顶层目录

| 路径 | 职责 | 默认可写角色 |
|---|---|---|
| `product/` | 需求与业务 | productAnalyst, planEditor（制品） |
| `design/` | 设计决策与 UI | uxUiPlanner |
| `planning/` | 计划、任务、评审 | 规划委员会 |
| `frontend/` | 前端实现 | developer（任务 allowModify 内） |
| `backend/` | 后端实现 | developer（任务 allowModify 内） |
| `tests/` | 测试与证据 | developer, tester |
| `ai/` | Agent 契约与控制面 | orchestrator；规则变更须人类审批 |

## 默认 denyModify（示例，请按项目改）

- `ai/rules/global/**`（仅平台）
- `ai/workflow/definition.yaml`（仅流程变更审批后）
- 生产密钥与环境私密配置路径：_待填_

## 共享契约路径（并行时须独立前置任务拥有）

- _待填：如 `planning/api-design/`、`packages/contracts/`_
