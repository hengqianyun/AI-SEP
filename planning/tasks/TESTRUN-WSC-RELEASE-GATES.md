# 测试证据 — WSC RELEASE 门禁缺口关闭

```yaml
taskId: WSC-RELEASE-GATES
actorInstance: tester-wsc-release-gates
result: PASSED
evidenceId: TESTRUN-WSC-RELEASE-GATES
testedAt: 2026-07-30T11:30:48+08:00
flywayEmptyDbApply: PASSED
rollbackDrill: PASSED
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
gitSha: 4805318cb07d5b753ca13b8b09b25ab87b48e20e
closesGaps:
  - REV-WSC-INTEGRATION-001 §7.3c Flyway 空库干净应用
  - REV-WSC-INTEGRATION-001 §7.3d 回滚演练证据（策略 A 备份恢复）
doesNotApprove:
  - MAINTAINER human release approval
```

## 总体结论

**PASSED** — 在 Docker 29.0.1 + `postgres:16` 上空库执行 Flyway migrate 成功；按 `ops/runbooks/wsc-v1-rollback.md` 策略 A 完成 `pg_dump` → 探针 → `DROP/CREATE DATABASE` → restore，校验探针消失且关键表/`flyway_schema_history` 恢复。

**未**将环境阻塞记为 PASS；**未**伪造人类发布批准；**未**使用 flyway undo/down。

## Layers / Gates

| name | command (摘要) | exitCode | result | notes |
|---|---|---|---|---|
| flyway-empty-db-apply | `docker run ... flyway/flyway:10-alpine ... migrate` via `wsc-release-net` → `jdbc:postgresql://wsc-pg-release:5432/wsc` | 0 | PASSED | `v202607281600`；见 `ops/evidence/release/flyway-schema-history.txt` |
| rollback-drill-A | 容器内 `pg_dump` + `DROP/CREATE` + `psql -f` restore | dump=0, drop=0, create=0, restore=0 | PASSED | 探针 count=0；见 `post-restore-verify.txt` |
| host.docker.internal:5432 flyway | 首次尝试 | 非 0（认证失败） | N/A | 宿主 5432 双监听；改用 docker network，不记 BLOCKED |

## 证据路径

- `ops/evidence/release/SUMMARY.md`
- `ops/evidence/release/commands.log`
- `ops/evidence/release/flyway-schema-history.txt`
- `ops/evidence/release/psql-dt-after-migrate.txt`
- `ops/evidence/release/key-tables-check.txt`
- `ops/evidence/release/pg_dump-pre.sql`
- `ops/evidence/release/pg_dump-pre.sha256.txt`
- `ops/evidence/release/probe-insert.txt`
- `ops/evidence/release/restore-output.txt`
- `ops/evidence/release/psql-dt-after-restore.txt`
- `ops/evidence/release/post-restore-verify.txt`

## 环境快照

| 项 | 值 |
|---|---|
| Docker | 29.0.1 |
| PG 镜像 | postgres:16 @ sha256:33f923b05f64ca54ac4401c01126a6b92afe839a0aa0a52bc5aeb5cc958e5f20 |
| Flyway | flyway/flyway:10-alpine / OSS 10.22.0 |
| 迁移脚本 | `backend/src/main/resources/db/migration/V202607281600__init_wsc.sql` |
| 清理 | 容器 `wsc-pg-release` 与 network `wsc-release-net` 已删除 |

## 决策权声明

- 本角色为独立 tester；仅对上述两项技术证据给出 PASSED。
- 第三项「人类发布批准」不在本证据范围内。
- TESTRUN-TASK-WSC-001 中 `flyway-empty-db-apply=DEFERRED` 由本证据关闭，不回写伪造该历史文件为 PASSED。
