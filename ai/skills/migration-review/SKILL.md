---
id: skill.migration-review
version: 1.0.0
status: active
owner: tech-lead
compatibleRoles: [migrationReviewer]
appliesTo:
  paths:
    - backend/app/*/src/main/resources/sql/migration/**
    - backend/app/*/src/main/resources/sql/init/**
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-ARCHITECTURE, RULE-PROJECT-LAYOUT]
pilot: WSC
---

# Migration Review Skill（WSC / data-chain）

## 工具与路径

- 迁移工具：Flyway（随 Spring Boot 2.7.18 应用启动执行）
- 空库初始化：`backend/app/<service>/src/main/resources/sql/init/`
  - `00_create_database.sql`
  - `01_flyway_baseline.sql`（baseline 版本 `0`）
- 业务迁移：`backend/app/<service>/src/main/resources/sql/migration/`
- 权威说明：业务表与初始数据统一由 Flyway 管理（DEC-WSC-005）
- 遗留路径：`backend/src/main/resources/db/migration/**` 仅 V1.0；**新脚本不得写入**

## 回滚方式

- 默认：**备份恢复**和/或 **forward-fix** 迁移
- 禁止依赖不可靠的 Flyway down 脚本作为唯一回滚手段
- 破坏性变更须在迁移头注释或任务包写明回滚/前进修复步骤与验证

## 审查清单

- [ ] 命名与版本序号不冲突；可重复诊断失败原因
- [ ] 空库路径：先 `sql/init` baseline，再启动应用应用 `sql/migration`
- [ ] 破坏性变更（删列/改类型/删表）有回滚或前进修复剧本
- [ ] 回填与 DDL 分离；大数据可分批
- [ ] 单主库假设成立；无多数据源旁路 DDL
- [ ] 未改写已应用的历史迁移文件

## 禁止通过条件

- 无回滚/前进修复说明的破坏性变更
- 直接改已应用的历史迁移文件
- 在应用代码中手写 DDL 绕过 Flyway
- 向遗留 `db/migration/` 追加新版本脚本冒充合规

## 本地验证建议

1. 空库执行 `sql/init` 两脚本
2. `mvn -pl backend/app/<service> -am spring-boot:run`（或项目等价启动）
3. 确认 Flyway 历史与业务表符合契约；必要时对照 OpenAPI/schema 验收
