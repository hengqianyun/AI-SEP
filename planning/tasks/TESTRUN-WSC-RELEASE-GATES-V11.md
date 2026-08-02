# 测试证据 — WSC RELEASE 门禁缺口关闭（V1.1 / MySQL 8）

```yaml
taskId: WSC-RELEASE-GATES-V11
actorInstance: tester-wsc-release-gates-v11
result: PASSED
evidenceId: TESTRUN-WSC-RELEASE-GATES-V11
testedAt: 2026-08-02T12:52:30+08:00
flywayEmptyDbApply: PASSED
flywayEmptyDbApplyNote: >-
  本轮在隔离 MySQL 8 演练库复跑 sql/init + Flyway migrate 至 v202607301600（exitCode=0）；
  并引用既有空库证据 EVID-TASK-WSC-101-4（planning/tasks/TESTRUN-TASK-WSC-101.md）。
rollbackDrill: PASSED
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
gitSha: 472494d87c43bb92d8e51fc166ac62aaef68c3c3
backupId: mysqldump-pre-20260802T125222+0800
closesGaps:
  - MYSQL_ROLLBACK_DRILL_EVIDENCE
  - REV-WSC-INTEGRATION-002 / PLAN-WSC-2.2 §8 回滚演练证据（策略 A：mysqldump 备份恢复）
doesNotApprove:
  - MAINTAINER human release approval
```

## 总体结论

**PASSED** — 在 Docker 29.0.1 + `mysql:8.0`（8.0.46）隔离环境完成：空库 `sql/init` → Flyway 应用至 `v202607301600`；按 `ops/runbooks/rollback.md` **策略 A** 执行 `mysqldump` → 探针插入 → `DROP/CREATE DATABASE` → restore；校验探针消失且关键表 / `flyway_schema_history` 恢复。

**未**将环境阻塞记为 PASS；**未**伪造人类发布批准；**未**使用 `flyway undo` / down。

## Layers / Gates

| name | command (摘要) | exitCode | result | notes |
|---|---|---|---|---|
| flyway-empty-db-apply | 演练库：`sql/init` + `flyway/flyway:10-alpine migrate` → `jdbc:mysql://wsc-mysql-release-v11:3306/data_chain`；引用 `EVID-TASK-WSC-101-4` | 0 | PASSED | now at `v202607301600`；见 `ops/evidence/release-v11/02-flyway-*.txt` |
| rollback-drill-A | 容器内 `mysqldump` + `DROP/CREATE` + `mysql < dump` restore | dump=0, restore=0, verify=0 | PASSED | 探针 `probe-release-v11-001` count=0；见 `05-post-restore-verify.txt` |

## 证据路径

- `ops/evidence/release-v11/SUMMARY.md`
- `ops/evidence/release-v11/commands.log`
- `ops/evidence/release-v11/00-env.txt`
- `ops/evidence/release-v11/01-mysql-init.txt`
- `ops/evidence/release-v11/02-flyway-migrate.txt`
- `ops/evidence/release-v11/02-flyway-schema-history.txt`
- `ops/evidence/release-v11/02-show-tables-after-migrate.txt`
- `ops/evidence/release-v11/02-key-tables-check.txt`
- `ops/evidence/release-v11/mysqldump-pre.sql`
- `ops/evidence/release-v11/mysqldump-pre.sha256.txt`
- `ops/evidence/release-v11/03-probe-insert.txt`
- `ops/evidence/release-v11/04-restore-output.txt`
- `ops/evidence/release-v11/05-post-restore-verify.txt`
- 运行副本：`ai/runs/RUN-WSC-002/tester-wsc-release-gates-v11/`（含 `summary.json`）

## 环境快照

| 项 | 值 |
|---|---|
| Docker | 29.0.1 |
| MySQL 镜像 | `mysql:8.0` @ `sha256:7dcddc01f13bab2f15cde676d44d01f61fc9f99fe7785e86196dfc07d358ae2b`（运行时 VERSION=8.0.46） |
| Flyway | `flyway/flyway:10-alpine` @ `sha256:94a81ca7db9a9f24fd8acd7463fa4560cb8f66aae2aa64485c27eb296f5851cf` |
| 迁移脚本 | `backend/app/data-chain-service/src/main/resources/sql/{init,migration}` |
| 库名 | `data_chain`（一次性容器 root 密码，未写入 git 跟踪配置） |
| 清理 | 容器 `wsc-mysql-release-v11` 与 network `wsc-release-v11-net` 已删除 |

## 回滚演练校验（策略 A）

| 检查 | 结果 |
|---|---|
| 备份 ID / SHA256 | `mysqldump-pre-20260802T125222+0800` / `DFBCCC5524C308CB0FBCEBB4D4F230EBA3955F4B1EF43032BE4273A42E5F344B` |
| git SHA / 契约 | `472494d87c43bb92d8e51fc166ac62aaef68c3c3` / `wsc-contracts@2.0.0` |
| `flyway_schema_history` 恢复至含 `202607301600` | 是 |
| 关键表 `industry_category` / `data_product` / `chain_version` / `chain_catalog_snapshot` | 存在 |
| 探针 `probe-release-v11-001` 行数 | **0**（备份在插入前 → 恢复后探针消失 = 回滚生效） |

## 决策权声明

- 本角色为独立 tester；仅对上述技术证据给出 PASSED。
- **未**伪造 / 写入 MAINTAINER 人类发布批准；该项仍须人类门禁。
- 空库 Flyway 门禁以本轮复跑 + `EVID-TASK-WSC-101-4` 共同支撑；本轮焦点为 **MYSQL_ROLLBACK_DRILL_EVIDENCE**。

