# 代码审查（FIX1）

```yaml
reviewId: REV-CODE-TASK-WSC-101-FIX1-R1
taskId: TASK-WSC-101
fixId: FIX1
bugId: BUG-WSC-101-001
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-101-fix1
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-31T13:58:00+08:00
basedOn:
  - planning/tasks/BUG-WSC-101-001.md
  - planning/tasks/DEV-TASK-WSC-101-FIX1.md
  - planning/tasks/TESTRUN-TASK-WSC-101.md
  - planning/tasks/TASK-WSC-101.md
  - planning/tasks/REV-TASK-WSC-101.md
  - evidenceId: EVID-TASK-WSC-101-3
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。FIX1 针对 EVID-TASK-WSC-101-3 / BUG-WSC-101-001：在 `backend/app/wsc-service/pom.xml` 增加 `org.flywaydb:flyway-mysql`，**未手写 version**；审查复跑 `dependency:tree` 确认与 Boot 2.7.18 BOM / `flyway-core` 同为 **8.5.13**。变更落在 `writeSet`（`backend/app/*/pom.xml`、`ops/runbooks/**`）；未扩大业务 feature / contracts / denyModify 源码。开发侧 closeWhen 第 1 条已满足；空库 migrate + `/health` 仍交独立 tester，本审查不宣称 VERIFIED / 空库通过。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | 本轮无 P0–P3 开单 | — | — |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照审查重点）

### 1. flyway-mysql 依赖

| 项 | 证据 | 结果 |
|---|---|---|
| 已增加 `org.flywaydb:flyway-mysql` | `wsc-service/pom.xml` L39–43 | PASS |
| 无手写冲突 version | 依赖块无 `<version>`；跟 parent Boot BOM | PASS |
| 与 flyway-core / Boot BOM 一致 | Boot `spring-boot-dependencies:2.7.18` 中 `flyway.version=8.5.13` 同时管理 core/mysql；`mvn … dependency:tree -Dincludes=org.flywaydb:*` → `flyway-core:8.5.13` + `flyway-mysql:8.5.13`（BUILD SUCCESS） | PASS |
| 对齐阻塞根因 | tester：`Unsupported Database: MySQL 8.0`；建议即补 flyway-mysql | PASS |

### 2. 范围 / writeSet

| 项 | 结果 |
|---|---|
| 代码/运维变更 ⊆ writeSet | PASS：`backend/app/*/pom.xml`、`ops/runbooks/**` |
| 未改 contracts / 业务 feature 源码 | PASS（DEV 报告与审阅一致） |
| 未触碰 denyModify 业务页 / `.env` / local·prod yml | PASS |
| 未向 `**/db/migration/**` 追加脚本 | PASS |
| planning 仅 BUG/DEV 过程制品 | PASS（非 `planning/approved/**`；不扩大实现范围） |

### 3. BUG closeWhen（开发侧）

| closeWhen | 本审查判定 |
|---|---|
| 1. pom（或父 POM）增加与 Boot 一致的 `flyway-mysql` | **满足（开发侧）** |
| 2. 空库 init → local 启动 Flyway 成功；`/health`（及 `/doc.html`）可达 | **未验** — 交独立 tester；DEV 已正确搁置 |
| 3. 独立 tester 复测证据非 BLOCKED | **未验** — 禁止本角色代关 / 宣称 VERIFIED |

足以关闭开发侧条件并进入 tester 复测门禁；**不足以**单独关闭 BUG。

### 4. 安全 / 密钥

| 项 | 结果 |
|---|---|
| 未入仓真实密码 / `application-local.yml` | PASS（审阅范围内未改） |

## 残余风险（交 tester）

- 须在 MySQL 8 + Redis 下：空库 `sql/init` → `spring.profiles.active=local` 启动 → 确认 Flyway 应用 `V202607301600__init_wsc_v11.sql` 且 `/health`、`/doc.html` 可达。
- 注意 tester 先前记录：jar 内嵌 `application-local.yml` 可能陈旧，启动宜用 `--spring.config.additional-location` 指向源码 resources（环境/打包问题，非本 FIX 范围）。
- 本审查不代替 tester 宣称空库通过或任务 VERIFIED。

## 决策权声明

- 审查者未修改被审业务代码。
- 与 `developer-wsc-101-fix1` 隔离；未代替 tester 宣称测试通过 / VERIFIED / 空库通过。
- **decision: APPROVE**（进入独立 tester 复测门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 0 |
| P3 | 0 |
