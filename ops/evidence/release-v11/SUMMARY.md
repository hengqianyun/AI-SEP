# WSC RELEASE V1.1 证据摘要 — Flyway 空库复跑 + MySQL 回滚演练 A

| 字段 | 值 |
|---|---|
| evidenceId | TESTRUN-WSC-RELEASE-GATES-V11 |
| actorInstance | tester-wsc-release-gates-v11 |
| testedAt | 2026-08-02T12:52:30+08:00 |
| gitSha | 472494d87c43bb92d8e51fc166ac62aaef68c3c3 |
| contracts | wsc-contracts@2.0.0 |
| backupId | mysqldump-pre-20260802T125222+0800 |
| result | **PASSED**（关闭 MYSQL_ROLLBACK_DRILL_EVIDENCE；不含人类 MAINTAINER 批准） |

## 环境

| 项 | 值 |
|---|---|
| Docker | 29.0.1 |
| MySQL | `mysql:8.0` @ sha256:7dcddc01f13bab2f15cde676d44d01f61fc9f99fe7785e86196dfc07d358ae2b（8.0.46） |
| Flyway | `flyway/flyway:10-alpine` @ sha256:94a81ca7db9a9f24fd8acd7463fa4560cb8f66aae2aa64485c27eb296f5851cf |
| 容器 / 网络 | `wsc-mysql-release-v11` / `wsc-release-v11-net`（演练后已删除） |
| 模块路径 | `backend/app/data-chain-service/src/main/resources/sql/` |

## 1. Flyway 空库应用 — PASSED（复跑 + 引用）

- 引用：`EVID-TASK-WSC-101-4` / `planning/tasks/TESTRUN-TASK-WSC-101.md`
- 本轮复跑：`00_create_database.sql` + `01_flyway_baseline.sql` → Flyway migrate，Successfully applied 3 migrations，now at `v202607301600`（exitCode=0）

## 2. 回滚演练（策略 A）— PASSED

禁止项遵守：未使用 flyway undo/down。

1. `mysqldump --single-transaction` → `mysqldump-pre.sql`（sha256 见同名 .sha256.txt）
2. 插入探针 `industry_category.id=probe-release-v11-001`（count=1）
3. `DROP DATABASE` + `CREATE DATABASE` + restore dump（exitCode=0）
4. 校验：probe_count=0；flyway_schema_history 含 202607301600；关键表存在

## 清理

- `docker rm -f wsc-mysql-release-v11`
- `docker network rm wsc-release-v11-net`
- **未删除**本目录证据文件

## 非本证据范围

- **未**伪造 MAINTAINER 人类发布批准
- **未**修改业务源码、contracts、state.yaml、approvals.yaml

