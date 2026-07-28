---
id: skill.backend-developer
version: 1.0.0
status: active
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: [backend/**]
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-LAYOUT]
pilot: WSC
---

# Backend Developer Skill（WSC 试点）

## 适用范围

- 服务：`backend/`
- 技术栈：以 `RULE-ORG-STACK` 为准（Java 21 / Spring Boot 3.3 / PostgreSQL / Flyway）
- 构建工具：Maven Wrapper；清单：`backend/pom.xml`
- 工作目录：仓库根目录

## 前置条件

- 执行 `./mvnw -f backend/pom.xml -q -DskipTests dependency:resolve`
- 从 `backend/src/main/resources/application-local.example.yml` 创建本地配置；不得提交真实密钥
- 本地 PostgreSQL 可达；连接信息仅来自本地配置或环境变量
- OpenAPI 契约以 `contracts/openapi/` 为准（契约任务完成后）

## 实现工作流

1. 读取任务包、REQ、冻结 API/数据契约与适用 Rule。
2. 确认修改路径均在 `allowModify`。
3. 分层：`web` → `application` → `domain` → `infrastructure`；禁止 Controller 直接访问 Repository。
4. 上链逻辑仅通过 `chain` 适配层接口（见 DEC-WSC-002）。
5. 涉及 schema 时先写 Flyway 脚本并完成迁移验证/回滚说明。
6. 补充测试后实施最小变更；执行验证矩阵并报告实际结果。

## 验证矩阵

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 安装/解析依赖 | `./mvnw -f backend/pom.xml -q -DskipTests dependency:resolve` | 首次/pom 变化 | 退出码 0 |
| 编译 | `./mvnw -f backend/pom.xml -DskipTests compile` | 每任务 | 退出码 0 |
| 相关单测 | `./mvnw -f backend/pom.xml -Dtest=<Class> test` | 每任务 | 退出码 0 |
| 全量单测 | `./mvnw -f backend/pom.xml test` | 共享模块/合并前 | 退出码 0 |
| 集成测试 | `./mvnw -f backend/pom.xml -Pintegration verify` | API/数据访问变化 | 退出码 0 |
| 构建 | `./mvnw -f backend/pom.xml -DskipTests package` | 每任务 | 退出码 0 |
| 迁移 up | `./mvnw -f backend/pom.xml flyway:migrate` | schema/数据变更 | 退出码 0 |
| 迁移验证 | `./mvnw -f backend/pom.xml flyway:validate` | 有迁移任务 | validate 通过 |
| 本地启动 | `./mvnw -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local` | 联调/冒烟 | `/actuator/health` 为 UP |

## 目录与实现约定

- 源码根建议：`backend/src/main/java/com/wsc/`
- Feature 包：`rbac` / `overview` / `catalog` / `chain`
- 迁移：`backend/src/main/resources/db/migration/`，命名 `VYYYYMMDDHHMM__<desc>.sql`
- 写 API 必须鉴权；角色校验与 REQ-RBAC-001 一致

## 禁止事项

- 不得引入第二套 Web 框架、ORM 或迁移工具（无 ADR）
- 不得业务层直连区块链 SDK
- 不得提交 `target/`、真实本地配置、真实密钥
- 不得扩大 `allowModify`
