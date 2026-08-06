---
id: RULE-PROJECT-ARCHITECTURE
version: 1.1.0
status: active
owner: tech-lead
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "integrationReviewer"]
reviewBy: 2026-11-05
pilot: WSC
sourceOfTruth:
  backend: backend/CLAUDE.md
---

# 项目架构约束（WSC）

> 后端骨架对齐 `data-chain`（DEC-WSC-005）。编码/包结构细则见 `RULE-ORG-CODING`、`RULE-PROJECT-LAYOUT`；权威原文 `backend/CLAUDE.md`。

## 系统边界

- **负责**：接入端工作台 Web；目录/总览/上链信息相关 API；会话角色鉴权；存证适配层（可模拟）。
- **不负责**：真实区块链节点接入、多租户组织树、支付结算、多数据源路由（见 PRD Out of Scope 与 DEC-WSC-005）。

## 模块关系（摘要）

```text
frontend (Vue / data-chain-static)
    │ HTTP / OpenAPI  (/api/v1)
    ▼
backend/app/data-chain-service  (Spring Boot 2.7.18)
    ├── controller → service → repository
    ├── entity / model / config / common
    ├── MySQL（单主数据源，JPA + QueryDSL；ddl-auto=validate）
    ├── Redis（Spring Data Redis）
    ├── Flyway（sql/migration 唯一 schema 入口）
    └── common.port.ChainAttestationPort → 模拟或可替换存证实现
```

## 不可破坏约定

- **单 MySQL 主数据源**：不配置多数据源、读写分离或动态数据源（DEC-WSC-005）。
- **Schema 唯一入口**：DDL/必要初始化 DML 仅经 Flyway（`sql/init` baseline + `sql/migration`）；禁止直连手工改表；禁止 Hibernate 建表（`ddl-auto=validate`）。
- **代码与迁移同步**：凡影响 schema 的实体/字段变更必须同步新增 `V{n}__xxx.sql`；已发布脚本不可改，修正只能追加更高版本。
- **审计与逻辑删除**（新建业务/系统表必备，详见 `skill.migration-review` / `backend/CLAUDE.md`）：
  - 审计四件套：`create_time` / `create_by` / `update_time` / `update_by`
  - `del_flag` 逻辑删除；禁止业务路径物理 `DELETE`（关系短表等明文豁免除外）
  - 插入/更新须自动填充审计字段；查询默认 `del_flag = 0`
- **契约优先**：对外 HTTP API 以 `contracts/openapi/` 为权威；Knife4j 为文档暴露，不得与契约冲突。
- **上链隔离**：业务层仅依赖存证端口/适配接口（`common.port`），禁止直连链 SDK（DEC-WSC-002）。
- **分层**：`controller` → `service` → `repository`；Controller 不得直接访问 Repository；业务逻辑不得堆在 Controller。
- **包结构**：按类型分层顶层包（见 `RULE-PROJECT-LAYOUT`），禁止业务域顶层包。

## ADR 入口

- 目录：`design/decisions/`
- 新增破坏性变更必须先有 ADR，状态 `APPROVED` 后方可进开发任务
- 相关：DEC-WSC-001..005
