# RULE-PROJECT-ARCHITECTURE 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认架构。以下模块边界与依赖均是假设值，复制时必须按真实系统设计修改。

```markdown
---
id: RULE-PROJECT-ARCHITECTURE
version: 1.0.0
status: active
owner: techLead
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "integrationReviewer"]
reviewBy: 2026-07-20
---

# RULE-PROJECT-ARCHITECTURE

## 系统边界

- 本系统负责：B2C 订单与商品目录的 Web 管理；REST API + Vue 3 SPA；订单状态流转至「已完成」
- 本系统不负责：真实支付通道、物流跟踪、ERP 同步（V1 均 Out of Scope）
- 外部依赖：PostgreSQL（主库）；邮件通知（仅 staging/prod，SMTP 配置在 Vault）；契约见 `contracts/openapi/`
- NFR 基线引用：`product/prd/v1.0.md` §非功能需求；暂无独立 NFR 文件

## 模块关系（摘要）

| 模块/边界 | 职责 | 允许依赖 | 禁止依赖 | 对外契约位置 | Owner |
|---|---|---|---|---|---|
| frontend | SPA、路由、展示 | backend API（HTTP） | 直连 PostgreSQL | N/A（消费 OpenAPI） | techLead |
| backend/orders | 订单聚合、状态机 | catalog（只读 SKU）、common | frontend | `contracts/openapi/paths/orders.yaml` | techLead |
| backend/catalog | 商品/SKU | common | orders | `contracts/openapi/paths/catalog.yaml` | techLead |
| backend/common | 鉴权、错误码、审计 | Spring 框架 | 业务域互引 | `contracts/openapi/components/` | techLead |

```text
frontend ──HTTP──► backend/orders ──只读──► backend/catalog
                      │
                      └──► PostgreSQL
backend/catalog ──► PostgreSQL
```

## 不可破坏约定

- `ARCH-INV-001`：frontend 不得直连数据库
  - 检查方式：ESLint 禁止 `pg`/`jdbc` 依赖；codeReview 清单
  - 例外审批：无
- `ARCH-INV-002`：orders 模块不得写 catalog 表
  - 检查方式：包可见性 + 集成测试无 cross-schema write
  - 例外审批：techLead + ADR
- `ARCH-INV-003`：跨模块调用仅经 HTTP 或 Java 接口（catalog 只读 Service），禁止共享 Entity
  - 检查方式：ArchUnit 规则 `no_cross_domain_entities`
  - 例外审批：techLead
- `ARCH-INV-004`：OpenAPI 为对外契约唯一来源；实现不得偏离未更新契约
  - 检查方式：CI `ci/openapi-diff` + 契约测试
  - 例外审批：apiDataDesigner + maintainer

## ADR 入口

- 目录：`design/decisions/`
- 必须新增决策记录的条件：破坏性 API、跨模块依赖新增、技术栈偏离 RULE-ORG-STACK、订单状态机变更
- 新增破坏性变更必须先有 ADR，状态 `APPROVED` 后方可进开发任务
- 模板：见同目录 `DEC-TEMPLATE.md`
```
