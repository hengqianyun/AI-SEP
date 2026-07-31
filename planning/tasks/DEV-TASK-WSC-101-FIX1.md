# DEV-TASK-WSC-101-FIX1

```yaml
taskId: TASK-WSC-101
fixId: FIX1
bugId: BUG-WSC-101-001
actorInstance: developer-wsc-101-fix1
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-31T13:30:00+08:00
basedOn:
  - planning/tasks/BUG-WSC-101-001.md
  - planning/tasks/TESTRUN-TASK-WSC-101.md
  - evidenceId: EVID-TASK-WSC-101-3
```

## 摘要

修复 Flyway 8.x 在 MySQL 8 上 `Unsupported Database: MySQL 8.0`：为 `wsc-service` 增加 Boot BOM 管理的 `org.flywaydb:flyway-mysql`（未手写版本号）。未改业务 feature、contracts、denyModify 路径下源码。

## 关联缺陷

| 字段 | 值 |
|---|---|
| bugId | BUG-WSC-101-001 |
| evidence | EVID-TASK-WSC-101-3（tester-wsc-101-r5） |
| rootCause | `flyway-core` 8.5.13 不含 MySQL 方言；需 `flyway-mysql` |
| bug status | `FIXED_PENDING_VERIFY`（关闭权在 tester） |

## 变更文件列表

| 路径 | 说明 | REQ / 关联 |
|---|---|---|
| `backend/app/wsc-service/pom.xml` | 增加 `org.flywaydb:flyway-mysql`（无显式 version，跟 Boot 2.7.18 BOM） | 底座/Flyway 空库可启动；BUG-WSC-101-001 |
| `ops/runbooks/wsc-v11-schema-migration.md` | 空库启动步骤补一句须依赖 `flyway-mysql` | 运维说明 |
| `planning/tasks/BUG-WSC-101-001.md` | status → `FIXED_PENDING_VERIFY` | 缺陷跟踪 |
| `planning/tasks/DEV-TASK-WSC-101-FIX1.md` | 本说明 | — |

## 未修改（边界）

- 未改 `contracts/**`、业务 feature 实现
- 未读/提交 `application-local.yml` 或任何明文密码
- 未标任务 `VERIFIED` / 未将 BUG 标 `CLOSED`
- 未触及 `denyModify` 中的 `ai/rules/**`、`product/**`、feature deny 路径、`.env`、prod yml

## 自测命令与结果

| 命令 | 结果 | 备注 |
|---|---|---|
| `mvn -f backend/pom.xml -DskipTests package` | **PASSED**（exit 0，BUILD SUCCESS，~12s） | 2026-07-31T13:29:19+08:00 |
| local 空库启动 + `/health` | 交 tester | 本实例不代关闭 BUG；环境冒烟见下 |

### local 冒烟

本 FIX1 以 POM + package 自测为主。空库 init → `spring.profiles.active=local` → Flyway migrate → `/health` 复测交给独立 tester（对照 `closeWhen`）。未在本轮读取 `application-local.yml` 密码或启动本机 MySQL 冒烟。

## 提交审查

- status: `READY_FOR_REVIEW`
- codeReviewer 须与 `developer-wsc-101-fix1` 不同实例
- tester 复测通过后方可关闭 `BUG-WSC-101-001`
