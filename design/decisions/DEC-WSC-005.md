---
decisionId: DEC-WSC-005
title: 后端骨架与数据栈对齐 data-chain（Boot 2.7 / MySQL / Redis）
status: APPROVED
date: 2026-07-30
owner: techLead
source: human-confirmed-2026-07-30（用户投递 data-chain 骨架说明为新后端规范）
closes: []
affects: [RULE-ORG-STACK, RULE-PROJECT-LAYOUT, RULE-PROJECT-ARCHITECTURE, skill.backend-developer, skill.migration-review]
snapshotRefs: []
supersedes: [DEC-WSC-004 中「仍使用 Spring Boot 3.3.x」条款；PostgreSQL 主库选型]
---

# DEC-WSC-005 后端骨架与数据栈对齐 data-chain

## 背景

组织提供 `data-chain` 最小后端骨架（结构参考 `daop-service`）作为后续后端规范。现行 WSC 试点规范为 Spring Boot 3.3.x + PostgreSQL 16，与骨架不一致，需固化新基线以免规划/实现继续按旧栈交付。

## 决策

- 后端应用框架改为 **Spring Boot 2.7.18**（Java 17 不变，与 DEC-WSC-004 运行时一致）。
- 主库改为 **MySQL**（单主数据源）；**禁止**多数据源、读写分离、动态数据源，除非新 DEC。
- 缓存基线引入 **Spring Data Redis**。
- 数据访问：**Spring Data JPA + QueryDSL**；Schema 版本化仍用 **Flyway**。
- API 文档：**Knife4j / OpenAPI 3**（`/doc.html`、`/v3/api-docs`）。
- Maven **多模块**：仓库顶层仍为 `backend/`，其下采用 `app/<service-module>/` 结构（骨架中的 `data-chain-service` 对应本项目服务模块）。
- 迁移目录改为服务模块内 `src/main/resources/sql/init/`（空库 baseline）+ `sql/migration/`（业务版本脚本）；不再以 `db/migration/` 为权威路径。
- 配置默认值通过环境变量覆盖（见 backend Skill）。

## 未选择方案

| 方案 | 未采用原因 |
|---|---|
| 继续 Spring Boot 3.3.x + PostgreSQL | 与组织 data-chain / daop-service 骨架不一致 |
| 在业务层引入多数据源或动态数据源 | 骨架明确单主库，增加运维与测试复杂度 |

## 影响

- 用户/业务影响：无直接产品功能变更；联调环境需 MySQL + Redis。
- 后续约束：新任务、新 Run、V1.1+ 规划必须以本 DEC 与更新后的 STACK/LAYOUT/Skill 为准。
- 技术/契约影响：既有 `backend/`（Boot 3.3 / PG / `db/migration`）视为 **V1.0 遗留实现**；迁到新骨架须独立计划任务，禁止静默混用两套迁移目录。
- 重评触发：升级到 Spring Boot 3.x、更换主库引擎、引入第二数据源或去掉 Redis 时须新 DEC。

## 反例

- 在应用代码中手写 DDL 绕过 Flyway。
- 同时维护 `db/migration/` 与 `sql/migration/` 且无明确 sole authority。
- 业务模块直连第二套 ORM 或第二套 Web 框架。

## 验收口径

- `RULE-ORG-STACK`、`RULE-PROJECT-LAYOUT`、backend / migration Skill 与本决策一致。
- 新骨架空库可按 `sql/init` → 启动 Flyway `sql/migration` 完成初始化。
