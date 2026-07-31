# WSC V1 回滚 Runbook

> 路径：`ops/runbooks/rollback.md`  
> **禁止**依赖不可靠的 Flyway down 脚本获得虚假安全感。  
> V1.1 schema 迁移/重建另见：`ops/runbooks/wsc-v11-schema-migration.md`。

## 适用场景

- Flyway 初版或增量迁移失败 / 损坏数据（`schema-migration`）
- 发布后需紧急回到上一已知良好状态

## 策略（二选一或组合）

### A. 备份恢复（优先）

1. **发布前**：对 **MySQL** 做逻辑备份（`mysqldump`）或快照，记录备份 ID、时间、git SHA、契约版本 `wsc-contracts@2.0.0`。（V1.0 历史为 PostgreSQL / `wsc-contracts@1.1.0`。）
2. **回滚时**：停止写流量（应用只读或下线写入口）。
3. **恢复**：从备份恢复目标库；校验关键表行数与抽样 `chain_version` / `chain_catalog_snapshot`。
4. **应用**：部署与备份对应的应用版本；确认 `GET /health` 为 UP。
5. **证据**：将备份 ID、恢复命令摘要、校验结果落盘到 staging 验收目录（由运维约定路径）。

### B. Forward-fix

1. 评估损坏范围；编写**新的**正向迁移（`VYYYYMMDDHHMM__fix_*.sql`），经独立串行迁移任务批准，写入 `sql/migration/`（禁止写入退役的 `db/migration/`）。
2. 在 staging 演练：migrate → 业务冒烟 → 记录结果。
3. 生产执行同一脚本；禁止手工改生产 schema 且无版本号。
4. 证据：迁移版本号、演练报告、冒烟结果。

## 明确禁止

- 将 `flyway undo` / 手写 down 脚本当作默认可靠回滚手段
- 在日志或备份说明中写入敏感字段明文
- 向 `**/db/migration/**` 追加新版本脚本

## Staging 检查清单（引用）

- [ ] 发布前备份存在且可恢复演练通过（A）或 forward-fix 演练通过（B）
- [ ] 回滚后 `/health` UP；契约版本与应用版本一致
- [ ] 证据路径已记录
