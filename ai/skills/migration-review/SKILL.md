---
id: skill.migration-review
version: 1.1.0
status: active
owner: tech-lead
compatibleRoles: [migrationReviewer]
appliesTo:
  paths:
    - backend/app/*/src/main/resources/sql/migration/**
    - backend/app/*/src/main/resources/sql/init/**
relatedRules: [RULE-ORG-STACK, RULE-PROJECT-ARCHITECTURE, RULE-PROJECT-LAYOUT, RULE-ORG-CODING]
pilot: WSC
sourceOfTruth:
  backend: backend/CLAUDE.md
---

# Migration Review Skill（WSC / data-chain）

> 数据库变更细则权威原文：`backend/CLAUDE.md`「数据库变更规则」。

## 工具与路径

- 迁移工具：Flyway（随 Spring Boot 2.7.18 应用启动执行）
- 空库初始化：`backend/app/data-chain-service/src/main/resources/sql/init/`
  - `00_create_database.sql`
  - `01_flyway_baseline.sql`（baseline 版本 `0`）
- 业务迁移：`backend/app/data-chain-service/src/main/resources/sql/migration/`
- JPA：`spring.jpa.hibernate.ddl-auto=validate`（只校验、不生成）
- 遗留路径：`backend/src/main/resources/db/migration/**` 仅 V1.0；**新脚本不得写入**
- `sql/init/` 仅冷启动（建库 + baseline），**不在其中维护业务 schema**

## 核心原则（硬约束）

1. 所有 DDL / 必要初始化 DML **只通过 Flyway**；禁止直连手工改表；禁止依赖 Hibernate 建表。
2. 影响 schema 的 Java 变更必须同步新增版本脚本；禁止只改实体漏迁移。
3. **已发布 `V{n}__` 脚本不可变**；修正/回滚只能追加更高版本。

## 脚本命名

- 路径：`.../sql/migration/`
- 格式：`V{版本号}__{动词}_{对象}.sql`（版本号与描述之间 **两个** 下划线；`V` 大写）
- 描述：全小写、下划线分词、动宾结构
  - 正例：`V3__create_sys_user_table.sql`、`V4__add_status_to_t_order_header.sql`
  - 反例：`V3.sql`、`V3__update.sql`、`v3__xxx.sql`

## 表名规则

一律小写、下划线分词、单数。

| 类型 | 前缀 | 格式 | 示例 |
|---|---|---|---|
| 系统表 | `sys_` | `sys_{名称}` | `sys_user`、`sys_role`、`sys_dict` |
| 业务表 | `t_` | `t_{业务模块}_{实例(可选)}` | `t_order`、`t_order_header`、`t_order_detail` |

- 现有 `demo` 等早期表示例可保留；**新建表一律遵守本规则**。
- 字符集 `utf8mb4` / `utf8mb4_unicode_ci`；引擎 `InnoDB`。

## 列与约束

- 主键：`id`（`BIGINT UNSIGNED AUTO_INCREMENT`）
- **禁止跨表外键**；唯一键 `uk_{表}_{字段}`；普通索引 `idx_{表}_{字段}`
- **审计四件套（业务/系统表必备）**：
  - `create_time` `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`
  - `create_by` `VARCHAR(64) NOT NULL DEFAULT ''`
  - `update_time` `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)`
  - `update_by` `VARCHAR(64) NOT NULL DEFAULT ''`
- **逻辑删除（必备）**：`del_flag TINYINT NOT NULL DEFAULT 0`（0=未删除，1=已删除）
- **关系短表例外**：仅关联外键、靠 `uk_` 幂等、无独立业务属性时，可省略 `create_by` / `update_by` / `del_flag`，建议保留 `create_time`
- 旧表示例沿用 `created_at` / `updated_at` 的已发布脚本不可改；新建表按四件套 + `del_flag`

## 编写约定

- 幂等可重入：`CREATE TABLE IF NOT EXISTS`；初始化用 `INSERT ... WHERE NOT EXISTS` 或 `ON DUPLICATE KEY UPDATE`
- 一脚本一主题（一张新表或一组紧密相关字段变更）
- 条件允许时启动应用验证 Flyway；至少确认实体与 schema 对齐、`validate` 可通过

## 审计自动填充与逻辑删除（硬约束）

- 插入写 `create_time` / `create_by`；更新写 `update_time` / `update_by`（`@PrePersist` / `@PreUpdate` 或统一切面；用户取自安全上下文）
- **任何 UPDATE 必须刷新** `update_time` 与 `update_by`；批量更新须在语句中显式带上
- 删除一律逻辑删除（`del_flag=1` 并刷新审计）；**禁止业务路径物理 DELETE**（短表豁免除外）
- 默认查询带 `del_flag=0`；实体宜配合 `@Where(clause = "del_flag = 0")` 等机制

## 回滚方式

- 默认：**备份恢复**和/或 **forward-fix** 迁移
- 禁止依赖不可靠的 Flyway down 脚本作为唯一回滚手段
- 破坏性变更须在迁移头注释或任务包写明回滚/前进修复步骤与验证

## 审查清单

- [ ] 命名符合 `V{n}__verb_object.sql`；版本序号不冲突
- [ ] 未改写已应用的历史迁移文件
- [ ] 空库路径：先 `sql/init` baseline，再启动应用应用 `sql/migration`
- [ ] 新建表前缀 `sys_` / `t_` 正确；主键/索引命名合规
- [ ] 新建业务/系统表含审计四件套 + `del_flag`（或符合短表例外并注明）
- [ ] 对应 JPA 实体字段与脚本同步；无跨表外键
- [ ] 破坏性变更有回滚或前进修复剧本
- [ ] 回填与 DDL 分离；大数据可分批
- [ ] 单主库假设成立；无多数据源旁路 DDL；无应用内手写 DDL

## 禁止通过条件

- 无回滚/前进修复说明的破坏性变更
- 直接改已应用的历史迁移文件
- 在应用代码中手写 DDL 绕过 Flyway，或依赖 `ddl-auto` 非 `validate`
- 新建表缺少审计四件套 / `del_flag` 且无短表例外说明
- 向遗留 `db/migration/` 追加新版本脚本冒充合规

## 本地验证建议

1. 空库执行 `sql/init` 两脚本
2. `mvn -pl app/data-chain-service -am spring-boot:run`（cwd: `backend`）
3. 确认 Flyway 历史与业务表符合契约；对照实体与 OpenAPI/schema 验收
