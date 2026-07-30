---
id: skill.backend-developer
version: 1.1.0
status: active
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: [backend/**]
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-LAYOUT, RULE-PROJECT-ARCHITECTURE]
pilot: WSC
---

# Backend Developer Skill（WSC / data-chain 骨架）

## 适用范围

- 服务根：`backend/`（Maven 多模块；结构参考组织 `data-chain`）
- 服务模块：`backend/app/<service-module>/`（骨架示例名 `data-chain-service`；本项目可用 `wsc-service`）
- 技术栈：以 `RULE-ORG-STACK` 为准（Java 17 / Spring Boot 2.7.18 / MySQL / Flyway / Redis / JPA / QueryDSL / Knife4j；DEC-WSC-005）
- 构建工具：Maven；父 POM：`backend/pom.xml`
- 工作目录：仓库根目录或明确注明的 `-f` / `-pl` 路径

## 前置条件

- JDK 17；本地 **MySQL** 与 **Redis** 可达
- 依赖解析：`mvn -f backend/pom.xml -q -DskipTests dependency:resolve`
- 从示例配置创建本地覆盖；不得提交真实密钥
- OpenAPI 契约以 `contracts/openapi/` 为准（契约任务完成后）；Knife4j 仅作运行时文档

## 项目结构（权威）

```text
backend
├── pom.xml
└── app
    └── <service-module>
        ├── pom.xml
        └── src
            ├── main
            │   ├── java/...                 # 建议包根与模块一致
            │   └── resources
            │       ├── application.yml
            │       └── sql
            │           ├── init             # 空库：建库 + Flyway baseline
            │           └── migration        # 业务表与初始数据
            └── test
```

仅配置 **一个 MySQL 主数据源**；禁止多数据源 / 读写分离 / 动态数据源。

## 初始化数据库

全新空库依次执行服务模块 `sql/init/`（路径以实际模块为准，下例用 `<service>`）：

```sql
source backend/app/<service>/src/main/resources/sql/init/00_create_database.sql;
source backend/app/<service>/src/main/resources/sql/init/01_flyway_baseline.sql;
```

随后启动应用：Flyway 识别 baseline（版本 `0`），自动执行 `sql/migration` 下版本脚本，完成业务表与初始数据。

> 业务表结构与初始数据统一由 Flyway 管理；细则见服务模块内 `sql/README.md`（若有）及 `skill.migration-review`。

## 配置（环境变量覆盖）

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `SERVER_PORT` | `8080` | 服务端口 |
| `DB_HOST` | `localhost` | MySQL 地址 |
| `DB_PORT` | `3306` | MySQL 端口 |
| `DB_NAME` | `data_chain` | 数据库名（项目可改为 `wsc` 等，须与 init 脚本一致） |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | `root` | 数据库密码（仅本地默认；勿提交真实密钥） |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `REDIS_DATABASE` | `0` | Redis 库编号 |

## 实现工作流

1. 读取任务包、REQ、冻结 API/数据契约与适用 Rule。
2. 确认修改路径均在 `allowModify`（新代码写入 `backend/app/**`，勿向遗留 `db/migration` 追加）。
3. 分层：`web` → `application` → `domain` → `infrastructure`；禁止 Controller 直接访问 Repository。
4. 上链逻辑仅通过 `chain` 适配层接口（见 DEC-WSC-002）。
5. 涉及 schema：先写 `sql/migration`（及必要时 `sql/init`），完成迁移验证/回滚说明。
6. 补充测试后实施最小变更；执行验证矩阵并报告实际结果。

## 验证矩阵

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 安装/解析依赖 | `mvn -f backend/pom.xml -q -DskipTests dependency:resolve` | 首次/pom 变化 | 退出码 0 |
| 编译 | `mvn -f backend/pom.xml -DskipTests compile` | 每任务 | 退出码 0 |
| 相关单测 | `mvn -f backend/pom.xml -Dtest=<Class> test` | 每任务 | 退出码 0 |
| 全量单测 | `mvn -f backend/pom.xml test` | 共享模块/合并前 | 退出码 0 |
| 构建 | `mvn -f backend/pom.xml clean package` | 每任务 | 退出码 0 |
| 本地启动 | `mvn -pl backend/app/<service> -am spring-boot:run` | 联调/冒烟 | 见下方端点 |
| 迁移 | 空库执行 `sql/init` 后启动（Flyway 应用 `sql/migration`） | schema/数据变更 | 启动成功且表结构符合契约 |

## 本地启动后探活

- 健康检查：`GET http://localhost:8080/health`
- Knife4j：`http://localhost:8080/doc.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

骨架 Demo（仅脚手架参考，非 WSC 业务 API）：

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/demos` | 新增 Demo |
| `GET` | `/api/demos/{id}` | 查询详情 |
| `GET` | `/api/demos` | 分页查询（`keyword`、`page`、`size`） |
| `PUT` | `/api/demos/{id}` | 更新 |
| `DELETE` | `/api/demos/{id}` | 删除 |

## 目录与实现约定

- Feature 包：`rbac` / `overview` / `catalog` / `chain`（及任务规定的包）
- 迁移命名：遵循 Flyway 版本脚本约定；破坏性变更须在文件头或任务包写回滚/前进修复说明
- 写 API 必须鉴权；角色校验与 REQ-RBAC-001 一致
- V1.0 遗留扁平 `backend/src/**` + `db/migration/**`：仅维护热修；新功能在多模块骨架落地后开发（DEC-WSC-005）

## 禁止事项

- 不得引入第二套 Web 框架、ORM 或迁移工具（无 ADR）
- 不得配置多数据源 / 读写分离 / 动态数据源
- 不得业务层直连区块链 SDK
- 不得提交 `target/`、真实本地配置、真实密钥
- 不得扩大 `allowModify`
