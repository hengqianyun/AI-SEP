---
id: skill.backend-developer
version: 1.2.0
status: active
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: [backend/**]
relatedRules: [RULE-ORG-STACK, RULE-ORG-CODING, RULE-PROJECT-LAYOUT, RULE-PROJECT-ARCHITECTURE]
relatedSkills: [skill.migration-review]
pilot: WSC
sourceOfTruth:
  coding: backend/CLAUDE.md
  ops: backend/README.md
---

# Backend Developer Skill（WSC / data-chain）

## 适用范围

- 服务根：`backend/`（独立仓 `data-chain-backend`，见 `ai/rules/project/repos.yaml`）
- 服务模块：`backend/app/data-chain-service`（唯一业务模块）
- 启动类：`com.shdata.datachain.DataChainApplication`
- 技术栈：以 `RULE-ORG-STACK` 为准（Java 17 / Spring Boot **2.7.18** / MySQL / Flyway / Redis / JPA / QueryDSL / Knife4j；DEC-WSC-005）
- 构建工具：Maven；父 POM：`backend/pom.xml`
- 编码与包结构硬约束：`RULE-ORG-CODING`、`RULE-PROJECT-LAYOUT`（权威原文 `backend/CLAUDE.md`）
- 工作目录：仓库根目录或明确注明的 `-f` / `-pl` 路径

## 前置条件

- JDK 17；本地联调需 **MySQL** 与 **Redis** 可达（`dev` profile 可无外部依赖冒烟）
- 依赖解析：`mvn -f backend/pom.xml -q -DskipTests dependency:resolve`
- 本地覆盖用 `application-local.yml` / 环境变量；**不得提交真实密钥**
- OpenAPI 契约以 `contracts/openapi/` 为准；Knife4j 仅作运行时文档

## 项目结构（权威）

```text
backend
├── pom.xml
└── app
    └── data-chain-service
        ├── pom.xml
        └── src
            ├── main
            │   ├── java/com/shdata/datachain
            │   │   ├── controller/{域}/{子域}/
            │   │   ├── service/{域}/{子域}/
            │   │   ├── repository/
            │   │   ├── entity/
            │   │   ├── model/
            │   │   ├── config/
            │   │   ├── common/          # entity/exception/response/security/port/...
            │   │   └── DataChainApplication.java
            │   └── resources
            │       ├── application.yml
            │       └── sql
            │           ├── init         # 空库：建库 + Flyway baseline
            │           └── migration    # 唯一业务 schema 入口
            └── test
```

仅配置 **一个 MySQL 主数据源**；禁止多数据源 / 读写分离 / 动态数据源。  
**禁止**新建业务域顶层包；域写在 `controller|service` 子路径下。

## 初始化数据库

全新空库依次执行：

```sql
source backend/app/data-chain-service/src/main/resources/sql/init/00_create_database.sql;
source backend/app/data-chain-service/src/main/resources/sql/init/01_flyway_baseline.sql;
```

随后启动应用：Flyway 识别 baseline，自动执行 `sql/migration`。细则见 `skill.migration-review` 与 `backend/CLAUDE.md`「数据库变更规则」。

## 配置（环境变量覆盖）

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `SERVER_PORT` | `8080` | 服务端口 |
| `DB_HOST` | `localhost` | MySQL 地址 |
| `DB_PORT` | `3306` | MySQL 端口 |
| `DB_NAME` | `data_chain` | 数据库名（须与 init 脚本一致） |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | `root` | 仅本地默认；勿提交真实密钥 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `REDIS_DATABASE` | `0` | Redis 库编号 |
| `WSC_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | 前端 Origin |

前端：`VITE_API_BASE_URL=http://<本服务>/api/v1`。

## 实现工作流

1. 读取任务包、REQ、冻结 API/数据契约与适用 Rule（含 `RULE-ORG-CODING`）。
2. 确认修改路径均在 `allowModify`；新代码写入 `backend/app/data-chain-service/**`。
3. **对照包结构**选定目标路径（`controller|service|repository|entity|model|common|config`）；禁止创建业务域顶层包。
4. 大段代码按铁律：伪代码 `//TODO` → 注释 → 实现；方法补全 Javadoc；Service 维护 `Tips` 清单。
5. Controller 只编排；业务在 Service；禁止 Controller 直访 Repository。
6. 上链逻辑仅通过 `common.port` 存证端口（DEC-WSC-002）。
7. 涉及 schema：先写 `sql/migration` 新版本脚本（及必要时说明），遵守审计字段与 `del_flag`；完成迁移验证。
8. 补充测试后实施最小变更；`mvn compile` / 相关测试；移动包后须 `test-compile`。
9. 执行验证矩阵并报告实际结果。

## 验证矩阵

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 安装/解析依赖 | `mvn -f backend/pom.xml -q -DskipTests dependency:resolve` | 首次/pom 变化 | 退出码 0 |
| 编译 | `mvn -f backend/pom.xml -DskipTests compile` | 每任务 | 退出码 0 |
| 测试编译 | `mvn -f backend/pom.xml test-compile` | 移动包/改签名 | 退出码 0 |
| 相关单测 | `mvn -f backend/pom.xml -Dtest=<Class> test` | 每任务 | 退出码 0 |
| 全量单测 | `mvn -f backend/pom.xml test` | 共享模块/合并前 | 退出码 0 |
| 构建 | `mvn -pl app/data-chain-service -am -DskipTests package`（cwd: `backend`） | 每任务 | 退出码 0 |
| 本地联调 | `mvn -pl app/data-chain-service -am spring-boot:run`（cwd: `backend`） | 联调/冒烟 | 见下方端点 |
| dev 冒烟 jar | `java -jar app/data-chain-service/target/data-chain-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev` | 无外部 MySQL/Redis | 进程起来且探活 OK |
| 迁移 | 空库执行 `sql/init` 后启动（Flyway 应用 `sql/migration`） | schema/数据变更 | 启动成功且表结构符合契约 |

## 本地启动后探活

- 健康检查：`GET http://localhost:8080/health`
- Actuator：`http://localhost:8080/actuator/health`
- Knife4j：`http://localhost:8080/doc.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 目录与实现约定

- 包结构与写边界：`RULE-PROJECT-LAYOUT`
- 编码行为（Javadoc / Tips / 伪代码流程 / 不可妥协项）：`RULE-ORG-CODING`、`backend/CLAUDE.md`
- 迁移命名、表名、审计、`del_flag`：`skill.migration-review`、`backend/CLAUDE.md`
- 写 API 必须鉴权；角色校验与 REQ-RBAC-001 一致
- V1.0 遗留扁平 `backend/src/**` + `db/migration/**`：仅热修；新功能禁止写入（DEC-WSC-005）

## 禁止事项

- 不得引入第二套 Web 框架、ORM 或迁移工具（无 ADR）
- 不得配置多数据源 / 读写分离 / 动态数据源
- 不得业务层直连区块链 SDK
- 不得创建业务域顶层包或绕过 Flyway 改 schema
- 不得提交 `target/`、真实本地配置、真实密钥
- 不得扩大 `allowModify`
- 不得以「简化/性能」为由违反 `backend/CLAUDE.md` 硬约束
