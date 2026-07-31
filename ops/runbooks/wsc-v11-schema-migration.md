# WSC V1.0 → V1.1 Schema 迁移 / 重建策略

> 路径：`ops/runbooks/wsc-v11-schema-migration.md`（TASK-WSC-101 / DEC-WSC-005）  
> 契约：`wsc-contracts@2.0.0`  
> 权威迁移目录：`backend/app/wsc-service/src/main/resources/sql/{init,migration}`  
> **退役**：扁平 `backend/src/**` 与 `backend/src/main/resources/db/migration/**` 不再作为权威。

## 背景差异

| 项 | V1.0（遗留） | V1.1（本 runbook） |
|---|---|---|
| 引擎 | PostgreSQL 16 | MySQL 8（单主库） |
| 迁移路径 | `classpath:db/migration` | `classpath:sql/migration`（+ `sql/init` baseline） |
| 分类 | L1/L2；产品挂 `l2_category_id` | L1/L2/L3；产品挂 `l3_category_id` |
| 维护状态 | 无 | `maintenance_status`（PENDING/MAINTAINED） |
| 导入报告 | 无 | `import_error_report`（TTL≤24h） |
| 缓存 | 无 | Redis（会话/缓存基线） |

## 推荐策略（二选一）

### A. 空库重建（开发 / 试点默认，优先）

适用于无必须保留的生产数据，或可从上游重新导入目录。

1. 备份（若有任何需保留数据）：导出产品/分类 CSV 或 `mysqldump`/`pg_dump`。
2. 停止应用写流量。
3. **丢弃**旧库（或新建空库 `wsc`）：
   ```bash
   mysql -uroot -p < backend/app/wsc-service/src/main/resources/sql/init/00_create_database.sql
   mysql -uroot -p wsc < backend/app/wsc-service/src/main/resources/sql/init/01_flyway_baseline.sql
   ```
4. 启动 `wsc-service`：Flyway 应用 `V202607301600__init_wsc_v11.sql`（Boot 2.7 / Flyway 8.x 须依赖 `org.flywaydb:flyway-mysql`，否则报 `Unsupported Database: MySQL 8.0`）。
5. 校验：`GET /health` UP；`/doc.html` 可达；抽查表 `industry_category`（含 L3）、`data_product.l3_category_id`。
6. 按需重新导入种子/产品（走 V1.1 导入模板，行业分类列须三级）。

### B. 逻辑迁移（有存量目录数据）

> 因引擎从 PostgreSQL → MySQL，**不提供**就地 `ALTER` 跨引擎脚本。流程为「导出走 MySQL 重建」。

1. 在 V1.0 PG 导出：
   - 分类树：映射为 L1/L2；为每个 L2 **人工或规则补建默认 L3**（产品必须挂三级）。
   - 产品：`l2_category_id` → 对应默认 L3 id；编码/字段按 OpenAPI 2.0.0。
2. 按策略 A 建空 MySQL 并启动。
3. 通过应用写 API 或受控 ETL 灌入；禁止手改生产 schema 且无 Flyway 版本号。
4. 抽检：Browse `page`/`pageSize`/`total`；快照 `categoryPath` 为三级。

## 明确禁止

- 向已退役的 `**/db/migration/**` **追加**新版本脚本。
- 同时把 `db/migration` 与 `sql/migration` 当作双权威。
- 依赖 Flyway undo / 手写 down 作为默认回滚（见 `ops/runbooks/rollback.md`）。

## 回滚

- 优先：恢复迁移前数据库备份 + 对应应用/契约版本（`wsc-contracts@1.1.0` + 旧扁平后端，仅紧急）。
- 或：在 V1.1 栈上 forward-fix（独立 `TASK-WSC-10x-MIG` 独占 `sql/migration/**`）。

## 验收检查清单

- [ ] 空库可走 `sql/init` → 启动 → `sql/migration` 成功
- [ ] `/health`、`/doc.html` 可达
- [ ] 无新脚本写入 `db/migration/`
- [ ] 契约版本 `contracts/VERSION` = `2.0.0`
- [ ] 本 runbook 与应用模块路径一致
