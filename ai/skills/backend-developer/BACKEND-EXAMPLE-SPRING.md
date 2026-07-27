# Backend Developer Skill 已填写示例（Spring Boot 3）

> 仅展示填写粒度，不代表 AI-SEP 默认技术栈。以下路径与命令均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.backend-developer
version: 1.0.0
status: active
owner: backend-platform
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: [backend/**]
relatedRules: [RULE-ORG-STACK, RULE-ORG-CODING, RULE-PROJECT-LAYOUT, RULE-PROJECT-ARCHITECTURE]
---

# Backend Developer Skill

## 适用范围

- 服务：`backend/`
- 技术栈：以 `RULE-ORG-STACK` 为准（Java 21 / Spring Boot 3.3 / PostgreSQL / Flyway）
- 构建工具：Maven Wrapper；清单：`backend/pom.xml`
- 运行时版本：读取根目录 `.sdkmanrc` 与 `backend/pom.xml` 的 `java.version`
- 工作目录：仓库根目录

## 前置条件

- 执行 `./mvnw -f backend/pom.xml -q -DskipTests dependency:resolve`
- 从 `backend/src/main/resources/application-local.example.yml` 创建本地配置；不得提交真实密钥
- 本地 PostgreSQL 可达；连接信息仅来自本地配置或环境变量
- OpenAPI 契约以 `contracts/openapi/` 为准；生成客户端后不得手改生成物

## 实现工作流

1. 读取任务包、REQ、冻结 API/数据契约与适用 Rule。
2. 确认修改路径均在 `allowModify`。
3. 同类实现参考 `backend/src/main/java/com/acme/orders/orders/web/OrderController.java`。
4. 分层：`web` → `application` → `domain` → `infrastructure`；禁止 Controller 直接访问 Repository。
5. 涉及 schema 时先写 Flyway 脚本并完成迁移验证/回滚说明。
6. 补充测试后实施最小变更；执行验证矩阵并报告实际结果。

## 验证矩阵

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 安装/解析依赖 | `./mvnw -f backend/pom.xml -q -DskipTests dependency:resolve` | 首次/pom 变化 | 退出码 0 |
| 格式/静态检查 | `./mvnw -f backend/pom.xml spotless:check` | 每任务 | 退出码 0 |
| 编译 | `./mvnw -f backend/pom.xml -DskipTests compile` | 每任务 | 退出码 0 |
| 相关单测 | `./mvnw -f backend/pom.xml -Dtest=OrderServiceTest test` | 每任务 | 退出码 0 |
| 全量单测 | `./mvnw -f backend/pom.xml test` | 共享模块/合并前 | 退出码 0 |
| 集成测试 | `./mvnw -f backend/pom.xml -Pintegration verify` | API/数据访问变化 | 退出码 0 |
| 构建 | `./mvnw -f backend/pom.xml -DskipTests package` | 每任务 | 退出码 0 |
| 迁移 up | `./mvnw -f backend/pom.xml flyway:migrate` | schema/数据变更 | 退出码 0 |
| 迁移验证/回滚 | `./mvnw -f backend/pom.xml flyway:validate`；回滚见迁移头注释 | 有迁移任务 | validate 通过；回滚步骤可执行 |
| 本地启动 | `./mvnw -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local` | 联调/冒烟 | `/actuator/health` 为 UP |

## 目录与实现约定

### 模块与边界
- 源码根：`backend/src/main/java/com/acme/orders/`
- 业务按 feature 分包：`orders/`、`catalog/`、`payments/`
- 配置样例：`application-local.example.yml`；禁止提交 `application-local.yml`

### API 与契约
- Controller：`.../<feature>/web/`
- 错误体：`{ code, message, correlationId, details }`，见 `RULE-ORG-CODING`
- 鉴权：统一 `SecurityFilterChain`；禁止在 Controller 手写鉴权分支
- 契约变更必须同步 `contracts/openapi/` 并跑 `ci/openapi-diff`

### 数据访问与迁移
- Repository 仅在 `infrastructure`；领域层不依赖 JPA 注解泄漏
- 迁移目录：`backend/src/main/resources/db/migration/`
- 命名：`VYYYYMMDDHHMM__<desc>.sql`；破坏性变更须在文件头写回滚步骤

## 测试约定

- 单测：`*Test.java`，与源码同模块 `src/test/java`
- 集成测：`*IT.java`，使用 Testcontainers PostgreSQL
- Fixture：`backend/src/test/resources/fixtures/`
- 新增/变更 API、数据访问、迁移逻辑必须补测
- 仅注释或日志文案变更可免自动化测试，须记录人工验证

## 禁止事项

- 不得引入第二套 Web 框架、ORM 或迁移工具，除非 ADR 已批准
- 不得在业务代码硬编码下游 URL、密钥或环境名
- 不得跳过 Flyway validate；无回滚说明不得宣称迁移完成
- 不得提交 `target/`、真实本地配置、调试日志或无关格式化
- 不得扩大 `allowModify`

## 输出示例

- 任务：`TASK-ORDER-014`
- 关联需求：`REQ-ORDER-007`
- 修改文件：
  - `backend/src/main/java/com/acme/orders/orders/web/OrderController.java`
  - `backend/src/main/java/com/acme/orders/orders/application/OrderQueryService.java`
  - `backend/src/test/java/com/acme/orders/orders/application/OrderQueryServiceTest.java`
- 实现摘要：订单列表增加状态筛选，查询参数与 OpenAPI 对齐
- 迁移影响：无
- 已执行验证：
  - `./mvnw -f backend/pom.xml spotless:check` → `PASSED`
  - `./mvnw -f backend/pom.xml -Dtest=OrderQueryServiceTest test` → `PASSED`
  - `./mvnw -f backend/pom.xml -DskipTests package` → `PASSED`
- 未执行验证：集成测，由 tester 在含 DB 的环境执行
- 已知风险：无
```
