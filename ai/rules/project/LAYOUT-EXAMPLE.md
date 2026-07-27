# RULE-PROJECT-LAYOUT 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认目录结构。以下 glob 与角色均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: RULE-PROJECT-LAYOUT
version: 1.0.0
status: active
owner: techLead
override: allowed
appliesTo:
  roles: ["parallelPlanner", "developer", "codeReviewer", "orchestrator"]
reviewBy: 2026-07-20
---

# RULE-PROJECT-LAYOUT

## 顶层目录

| 路径 | 职责 | 默认可写角色 |
|---|---|---|
| `product/` | 需求、术语、业务规则 | productAnalyst, productOwner |
| `design/` | ADR、UI 规范 | solutionArchitect, uxUiPlanner |
| `planning/` | 计划、任务包、评审 | parallelPlanner, planEditor |
| `contracts/openapi/` | API 契约 | apiDataDesigner |
| `frontend/` | Vue 3 客户端 | developer（frontend skill） |
| `backend/` | Spring Boot API | developer（backend skill） |
| `frontend/tests/`、`backend/src/test/` | 测试与证据 | developer, tester |
| `ai/` | Agent 契约与控制面 | 人类审批后由 maintainer |

## 前端布局与写边界

| 模块代码 | 业务名称 | 可修改路径 glob | 只读/共享路径 | 禁止修改路径 | Owner | 冲突升级角色 |
|---|---|---|---|---|---|---|
| ORDER | 订单 | `frontend/src/features/orders/**` | `frontend/src/api/client.ts` | `frontend/src/router/guards/**` | techLead | maintainer |
| CATALOG | 商品目录 | `frontend/src/features/catalog/**` | `frontend/src/styles/tokens.css` | `frontend/vite.config.ts` | techLead | maintainer |

### 前端共享边界
- 共享契约/类型：`frontend/src/types/api/`（OpenAPI 生成）；修改条件：apiDataDesigner 任务 + OpenAPI 已合并
- 共享组件/资源：`frontend/src/components/`、`frontend/src/styles/`；修改条件：跨模块影响须在 PR 声明
- 生成文件：`frontend/src/types/api/generated/`；生成来源：`pnpm --filter frontend generate:api`

## 后端布局与写边界

| 路径/glob | 内容职责 | Owner | 允许写入角色 | 共享/独占 | 并行写规则 |
|---|---|---|---|---|---|
| `backend/src/main/java/com/acme/orders/**` | 订单域 | techLead | developer | 独占 | 单 TASK 单域 |
| `backend/src/main/java/com/acme/catalog/**` | 商品域 | techLead | developer | 独占 | 单 TASK 单域 |
| `backend/src/main/resources/db/migration/**` | Flyway 迁移 | techLead | developer + migrationReviewer | 共享 | 串行；须独立 migration 任务 |
| `backend/src/main/java/com/acme/common/**` | 横切工具 | techLead | developer | 共享 | 变更须 codeReviewer |

## 默认 denyModify

- `ai/rules/**`：仅 maintainer 可改（实例化除外）
- `ai/workflow/policies.yaml`：maintainer + productOwner 双批
- `contracts/openapi/**`：须 apiDataDesigner 或显式契约任务
- 生产密钥：`backend/.env.prod`、`k8s/secrets/**`：禁止 Agent 写入

## 共享契约路径（并行时须独立前置任务拥有）

| 路径 | Owner | 并行写 | 变更协议 |
|---|---|---|---|
| `contracts/openapi/acme-orders-v1.yaml` | apiDataDesigner | forbidden | 先合并 OpenAPI，再派发前后端 TASK |
| `frontend/src/types/api/generated/**` | apiDataDesigner | forbidden | CI 生成，不手改 |

## 多任务写冲突处理

- 同一 `allowModify` glob 不得并行两 TASK；parallelPlanner 须拆分或串行
- 迁移目录同一版本号只允许一个 TASK；冲突升级 maintainer
```
