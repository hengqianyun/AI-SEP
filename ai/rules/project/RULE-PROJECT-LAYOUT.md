---
id: RULE-PROJECT-LAYOUT
version: 1.0.0
status: active
owner: techLead
override: allowed
appliesTo:
  roles: ["parallelPlanner", "developer", "codeReviewer", "orchestrator"]
reviewBy: 2026-10-28
pilot: WSC
---

# 目录与模块地图（WSC 试点）

## 顶层目录

| 路径 | 职责 | 默认可写角色 |
|---|---|---|
| `product/` | 需求与业务 | productAnalyst, planEditor（制品） |
| `design/` | 设计决策与 UI | uxUiPlanner, solutionArchitect |
| `planning/` | 计划、任务、评审 | 规划委员会 |
| `frontend/` | 前端实现 | developer（任务 allowModify 内） |
| `backend/` | 后端实现 | developer（任务 allowModify 内） |
| `contracts/` | OpenAPI 等共享契约 | apiDataDesigner, developer（契约任务） |
| `tests/` / `e2e/` | 测试与证据 | developer, tester |
| `ai/` | Agent 契约与控制面 | orchestrator；规则变更须人类审批 |

## 前端布局与写边界

| 模块代码 | 业务名称 | 可修改路径 glob | Owner | 冲突升级角色 |
|---|---|---|---|---|
| SHELL | 壳层导航 | `frontend/src/features/shell/**`, `frontend/src/layouts/**`, `frontend/src/router/**` | techLead | techLead |
| RBAC | 鉴权前端 | `frontend/src/features/auth/**` | techLead | techLead |
| OVW | 总览 | `frontend/src/features/overview/**` | techLead | techLead |
| CAT | 数据目录 | `frontend/src/features/catalog/**` | techLead | techLead |
| CHAIN | 上链信息 | `frontend/src/features/chain/**` | techLead | techLead |

### 前端共享边界

- 共享 API client：`frontend/src/api/`；修改须独立任务或与契约同步
- 共享组件：`frontend/src/components/`；跨 feature 变更串行

## 后端布局与写边界

| 路径/glob | 内容职责 | Owner | 允许写入角色 | 并行写规则 |
|---|---|---|---|---|
| `backend/src/main/java/**/rbac/**` | 角色鉴权 | techLead | developer | 与 SHELL 协调 |
| `backend/src/main/java/**/overview/**` | 总览指标 | techLead | developer | 可读 catalog 聚合 |
| `backend/src/main/java/**/catalog/**` | 目录与产品 | techLead | developer | 独占写集 |
| `backend/src/main/java/**/chain/**` | 存证适配 | techLead | developer | 与 CAT 编辑同事务时串行 |
| `backend/src/main/resources/db/migration/**` | Schema 迁移 | techLead | developer | 串行；触发 migrationReviewer |

## 默认 denyModify

- `ai/rules/global/**`：仅平台
- `ai/workflow/definition.yaml`：仅流程变更审批后
- 生产密钥与环境私密配置：`**/.env`、`**/application-local.yml`、`**/application-prod.yml`

## 共享契约路径（并行时须独立前置任务拥有）

| 路径 | Owner | 并行写 | 变更协议 |
|---|---|---|---|
| `contracts/openapi/` | apiDataDesigner | 否 | 变更后 openapi-diff；前后端任务依赖契约任务完成 |
