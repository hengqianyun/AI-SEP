---
id: RULE-PROJECT-ARCHITECTURE
version: 1.0.0
status: active
owner: tech-lead
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "integrationReviewer"]
reviewBy: 2026-10-28
pilot: WSC
---

# 项目架构约束（WSC）

> 后端骨架约束对齐 `data-chain`（DEC-WSC-005）。模板见 [`ARCHITECTURE-TEMPLATE.md`](./ARCHITECTURE-TEMPLATE.md)。

## 系统边界

- **负责**：接入端工作台 Web；目录/总览/上链信息相关 API；会话角色鉴权；存证适配层（可模拟）。
- **不负责**：真实区块链节点接入、多租户组织树、支付结算、多数据源路由（见 PRD Out of Scope 与 DEC-WSC-005）。

## 模块关系（摘要）

```text
frontend (Vue)
    │ HTTP / OpenAPI
    ▼
backend/app/<service>  (Spring Boot 2.7.18)
    ├── web / application / domain / infrastructure
    ├── MySQL（单主数据源，JPA + QueryDSL）
    ├── Redis（Spring Data Redis）
    └── ChainAttestationPort → 模拟或可替换存证实现
```

## 不可破坏约定

- **单 MySQL 主数据源**：不配置多数据源、读写分离或动态数据源（DEC-WSC-005）。
- **Schema 唯一入口**：业务表与初始数据仅由 Flyway 管理（`sql/init` baseline + `sql/migration`）；禁止业务代码手写 DDL。
- **契约优先**：对外 HTTP API 以 `contracts/openapi/` 为权威；Knife4j 为文档暴露，不得与契约冲突。
- **上链隔离**：业务层仅依赖存证端口/适配接口，禁止直连链 SDK（DEC-WSC-002）。
- **分层**：`web` → `application` → `domain` → `infrastructure`；Controller 不得直接访问 Repository。

## ADR 入口

- 目录：`design/decisions/`
- 新增破坏性变更必须先有 ADR，状态 `APPROVED` 后方可进开发任务
- 相关：DEC-WSC-001..005
