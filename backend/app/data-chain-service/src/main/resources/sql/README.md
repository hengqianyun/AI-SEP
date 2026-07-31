# SQL 脚本说明

数据库脚本分为两类:`init/` 用于人工搭建全新数据库,`migration/` 是 Flyway 版本迁移脚本,由应用启动时自动执行。业务表结构与初始数据**统一由 `migration/` 驱动**,避免两处脚本重复同步。

## init/(人工建库)

仅在「部署一个全新的空数据库」时按顺序手工执行,适合运维或本地初始化:

1. `00_create_database.sql` — 创建 `data_chain` 数据库。
2. `01_flyway_baseline.sql` — 创建 Flyway 版本历史表 `flyway_schema_history`,并预置 baseline(version = 0)。

执行后,数据库处于「已 baseline 到 v0」的状态,业务表尚不存在。随后启动应用,Flyway 会自动从 V1 起依次执行 `migration/` 中的迁移脚本,完成业务表与初始数据的创建。

> 业务表结构和初始数据**不再**在此维护,统一由 `migration/` 驱动。

## migration/(Flyway 版本迁移)

由应用启动时 Flyway 自动执行,location 配置见 `application.yml` 的 `spring.flyway.locations: classpath:sql/migration`。

- `V1__create_demo_table.sql` — 创建 `demo` 表
- `V2__insert_demo_data.sql` — 写入示例数据

规则:**已发布的版本脚本禁止修改,只能追加更高版本。**

## 相关配置(application.yml)

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:sql/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    validate-migration-naming: true
```
