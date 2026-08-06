# SQL 脚本说明

数据库脚本分为两类：`init/` 用于人工搭建全新数据库，`migration/` 是 Flyway 版本迁移脚本，由应用启动时自动执行。

## init/（人工建库）

仅在「部署一个全新的空数据库」时按顺序手工执行，适合运维或本地初始化：

1. `00_create_database.sql` — 创建 `data_chain` 数据库。
2. `01_flyway_baseline.sql` — 创建 Flyway 版本历史表并预置 baseline（version = 0）。

执行后数据库处于「已 baseline 到 v0」状态，业务表尚不存在。随后启动应用，Flyway 自动执行 `migration/` 中的迁移脚本。

## migration/（Flyway 版本迁移）

> 已发布版本脚本禁止修改，只能追加更高版本。

| 脚本 | 说明 |
|------|------|
| `V1__init_all_schema.sql` | 项目基线：demo 示例表 + 种子数据 + 6 张 WSC 业务表（t_ 前缀） |

### V1 包含的表

| 表名 | 说明 |
|------|------|
| `demo` | Demo 示例表（早期遗留，沿用 created_at/updated_at） |
| `t_industry_category` | 行业分类（三级树） |
| `t_data_product` | 数据产品 |
| `t_chain_version` | 上链版本记录 |
| `t_chain_catalog_snapshot` | 上链目录快照 |
| `t_overview_stream_event` | 总览流事件 |
| `t_import_error_report` | 导入错误报告（TTL 24h） |

### 已有数据库升级说明

如果 dev/本地数据库已执行过旧版 Flyway 脚本（V1/V2/V202607301600/V4），合并为 V1 后 Flyway 会因 checksum 不匹配启动失败。**清理方法**：

```sql
-- 1. 删除旧 Flyway 历史记录
DROP TABLE IF EXISTS `flyway_schema_history`;

-- 2. 重新执行 baseline（或用 sql/init/01_flyway_baseline.sql）
SOURCE sql/init/01_flyway_baseline.sql;
```

然后重启应用，Flyway 会从 V1 重新执行。

## 相关配置（application.yml）

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
