# RULE-ORG-STACK 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认技术栈。以下路径与版本均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: RULE-ORG-STACK
version: 1.0.0
status: active
owner: architecture-committee
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "apiDataDesigner"]
reviewBy: 2026-07-15
---

# RULE-ORG-STACK

## 前端技术栈

| 类别 | 采用项 | 版本/版本来源 | 适用范围 | 变更批准角色 |
|---|---|---|---|---|
| 运行时 | Node.js | 根目录 `.nvmrc`（当前 `20.14.0`） | `frontend/` | techLead |
| UI 框架 | Vue 3 + TypeScript | `frontend/package.json`（`^3.4`） | `frontend/src/**` | techLead |
| 构建工具 | Vite | `frontend/package.json`（`^5.3`） | `frontend/` | techLead |
| 包管理工具 | pnpm | 根目录 `packageManager` 字段（`pnpm@9.6.0`） | 仓库根、`frontend/` | techLead |
| 测试工具 | Vitest + Vue Test Utils | `frontend/package.json` | 单元/组件测试 | techLead |

### 允许与禁止（前端）
- 允许的补充库及用途：Pinia（跨页状态）、Vue Router 4（路由）、Ant Design Vue（UI 组件）；仅限 `frontend/package.json` 已声明项
- 禁止引入：第二套 UI 框架、jQuery、未经 ADR 的全局 CSS 方案
- 新依赖审批：新增 direct dependency 须 PR 说明用途；Tech Lead 批准；证据落盘 `design/decisions/`
- 版本升级策略：minor/patch 由 CI 绿即可合并；major 须兼容性说明 + Tech Lead 批准

## 后端与数据技术栈

| 类别 | 选型/产品 | 版本或约束 | 用途 | 官方/内部依据 | 禁止或例外 |
|---|---|---|---|---|---|
| 运行时/语言 | Java | 21 LTS（`backend/pom.xml` `<java.version>`） | 服务端 | 组织 Java 基线 v2026-Q2 | 禁止 Java 17 以下 |
| 应用框架 | Spring Boot | 3.3.x（BOM 管理） | REST API、DI、配置 | Spring 官方文档 | 禁止 Spring Boot 2.x |
| 数据存储 | PostgreSQL | 16（`docker-compose.yml` 镜像 tag） | 主业务库 | 组织 DB 标准 | 禁止 MySQL 混用同一 schema |
| 数据访问层 | Spring Data JPA | 随 Boot BOM | 实体与 Repository | 项目 `backend/` 约定 | 禁止业务层直连 JDBC |
| 迁移工具 | Flyway | `backend/pom.xml` 插件版本 | Schema 版本化 | `backend/src/main/resources/db/migration/` | 禁止手工改生产库 |
| 其他基础组件 | N/A | — | 当前无消息队列/缓存 | — | 引入 Redis/Kafka 须 ADR |
| 后端测试工具 | JUnit 5 + Testcontainers | `backend/pom.xml` | 单元/集成测试 | 项目测试约定 | 禁止 Mockito 替代集成边界 |

## 允许

| 层 | 技术 | 版本范围 | 备注 |
|---|---|---|---|
| 语言 | Java | 21 | 后端 |
| 语言 | TypeScript | 5.x | 前端 |
| 前端 | Vue 3 + Vite + pnpm | 见 package.json | `frontend/` |
| 后端 | Spring Boot 3 | 3.3.x | `backend/` |
| 数据 | PostgreSQL + Flyway | PG 16 | 迁移目录见上 |
| 测试 | Vitest / JUnit 5 | 见各 pom/package | — |

## 禁止引入（除非 ADR 批准）

- 第二套 ORM（如 MyBatis-Plus）与 JPA 并存
- 前端全局状态第二方案（如 Redux）
- 未扫描许可证的 npm/Maven 依赖

## 引入新依赖的门槛

- [ ] 说明替代方案
- [ ] 安全与许可证检查（`pnpm audit` / OWASP dependency-check）
- [ ] Tech Lead 或架构委员会批准
- [ ] 证据落盘路径：`design/decisions/DEC-<ID>.md`
```
