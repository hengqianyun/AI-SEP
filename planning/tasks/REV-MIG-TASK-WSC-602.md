# 迁移审查（实现态 · sys_user / V6）

```yaml
reviewId: REV-MIG-TASK-WSC-602
taskId: TASK-WSC-602
planId: PLAN-WSC-5.2
round: 1
role: migrationReviewer
actorInstance: migration-reviewer-wsc-602-r1
decision: APPROVE
p0: 0
p1: 0
migrationFilesReviewed:
  - backend/app/data-chain-service/src/main/resources/sql/migration/V6__create_sys_user.sql
entityReviewed:
  - backend/app/data-chain-service/src/main/java/com/shdata/datachain/entity/SysUserEntity.java
  - backend/app/data-chain-service/src/main/java/com/shdata/datachain/common/entity/BaseEntity.java
reviewedAt: 2026-08-05T17:40:00+08:00
basedOn:
  - ai/agents/migration-reviewer.md
  - ai/skills/migration-review/SKILL.md
  - ai/workflow/policies.yaml (riskTriggers.migrationReviewer → schema-migration)
  - planning/approved/PLAN-WSC-5.2.md (§3.3 / §8 / TASK-WSC-602)
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-602.md
  - backend/CLAUDE.md「数据库变更规则」（skill 权威原文）
mustDifferFrom:
  - developer-wsc-602
  - developer-wsc-602-r2
scopeNote: |
  只读审查 sql/migration 下 sys_user 相关脚本与实体对齐、幂等、路径合规、种子策略与回滚/前向兼容说明。
  未改 SQL/业务代码；未写 state/events；未标 VERIFIED。
  安全面（会话吊销、Cookie Secure 等）归 securityReviewer；本报告仅记与 schema/种子相关的交叉项。
```

## 结论

**APPROVE** — `V6__create_sys_user.sql` 落在合规路径 `sql/migration/`（未向 `db/migration/` 追加）；`CREATE TABLE IF NOT EXISTS` 可重复；含 `username` 唯一、`role`、`password_hash`、审计四件套与 `del_flag`；无删列/盲数据迁移；种子由应用侧 BCrypt 写入、脚本不落明文口令，策略可接受；与 `SysUserEntity`/`BaseEntity` 列对齐；变更性质为纯新增表，前向兼容，回滚可按 PLAN §8「回滚 V6+」+ 备份恢复/前进修复理解。**P0=0，P1=0**。开放项均为 P2 残余说明，不阻塞 migrationReviewer 门禁。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-MIG-WSC-602-R1-001 | P2 | `uk_sys_user_username` 覆盖全表行（含 `del_flag=1`）。软删后同名无法重建；若演示库将全部用户软删，应用侧 `ensureSeedUsers` 因非空软删行仍占 UK 亦无法重种。属账号生命周期/schema 设计权衡，非破坏性迁移缺陷 | 文档化「软删占用用户名」；或后续前进迁移改为部分唯一/应用层策略（须新 `V{n}__`，禁止改本 V6） | REQ-USER-001；§3.3 |
| FIND-MIG-WSC-602-R1-002 | P2 | 迁移头注释覆盖幂等与种子策略，但未显式写「回滚步骤」（如：备份恢复；或前进 `V{n}__` 废弃表前迁出依赖并吊销会话）。PLAN §8 已有「回滚 V6+；吊销会话」；对本纯 `CREATE TABLE` 非破坏性脚本不构成禁止通过条件 | 在任务交付说明或后续迁移头补一行 forward-fix / 备份恢复验证步骤即可 | PLAN §8；skill 回滚约定 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 审查清单（对照焦点）

### 1. 可重复执行 / 幂等

| 项 | 证据 | 结果 |
|---|---|---|
| `CREATE TABLE IF NOT EXISTS` | `V6__create_sys_user.sql` L5 | **PASS** |
| 无依赖二次执行会失败的裸 `CREATE`/`ALTER` | 脚本仅建表；无 `DROP`/`ALTER`/`UPDATE` 盲迁 | **PASS** |
| 版本序号不冲突 | 既有 `V1` / `V5` / 本 `V6`；`sys_user` 仅出现于 V6 | **PASS** |
| 未改写已发布历史脚本（本任务面） | 602 交付面为新增/对账 V6；未改 V1/V5 内容（本审查只读确认） | **PASS** |

### 2. 列与约束（username / role / hash / 软删 / 审计）

| 项 | 证据 | 结果 |
|---|---|---|
| 表名 `sys_user`（系统表前缀） | L5；对齐命名规范 | **PASS** |
| 主键 `id BIGINT UNSIGNED AUTO_INCREMENT` | L6 + `PRIMARY KEY` | **PASS** |
| `username` + `uk_sys_user_username` | L7、L18 | **PASS** |
| `password_hash` `VARCHAR(100) NOT NULL` | L8（BCrypt 足够） | **PASS** |
| `role` `VARCHAR(32) NOT NULL` | L10 | **PASS** |
| 审计四件套 | `create_time`/`create_by`/`update_time`/`update_by` L12–15 | **PASS** |
| `del_flag TINYINT NOT NULL DEFAULT 0` | L16 | **PASS** |
| 索引命名 `uk_` / `idx_` | L18–19 | **PASS** |
| 无跨表外键 | 全文无 FK | **PASS** |
| InnoDB + utf8mb4 / unicode_ci | L20 | **PASS** |

### 3. 路径合规

| 项 | 证据 | 结果 |
|---|---|---|
| 业务迁移在 `sql/migration/` | `…/resources/sql/migration/V6__create_sys_user.sql` | **PASS** |
| 未向遗留 `db/migration/` 追加 | 仓库 `backend/**/db/migration/**` 无匹配；602 denyModify 亦禁止该路径 | **PASS** |
| 未在 `sql/init/` 维护业务 schema | `sys_user` 仅在 migration V6 | **PASS** |
| 命名 `V{n}__verb_object.sql` | `V6__create_sys_user.sql`；与 PLAN §3.3 预落地文件名一致 | **PASS** |

### 4. 破坏性变更 / 种子策略

| 项 | 证据 | 结果 |
|---|---|---|
| 无删列 / 无盲数据迁移 | 纯 `CREATE TABLE` | **PASS**（非破坏性） |
| SQL 无明文口令种子 | 头注释声明种子由应用写入；脚本无 `INSERT` | **PASS** |
| 应用侧 BCrypt 种子可接受 | `UserAccountService.DEFAULT_DEMO_PASSWORD` + `PasswordHasher`；DEV 对账「种子由应用 BCrypt」；对齐 PLAN acceptance「不得明文落盘生产密钥」 | **ACCEPTABLE** |
| 种子与迁移职责分离 | DDL 在 Flyway；DML 种子在 `ApplicationRunner`（非 SQL 明文） | **PASS** |

### 5. 实体对齐（只读）

| SQL 列 | 实体 | 结果 |
|---|---|---|
| `id` | `BaseEntity.id` | **对齐** |
| `username` / `password_hash` / `display_name` / `role` / `enterprise_name` | `SysUserEntity` 对应 `@Column` | **对齐** |
| 审计四件套 + `del_flag` | `BaseEntity` | **对齐** |
| JPA `ddl-auto=validate` 预期 | 列名/可空/长度一致，无脚本外幽灵列 | **可预期通过 validate**（本实例未启动应用实测） |

### 6. 回滚 / 前向兼容

| 项 | 证据 | 结果 |
|---|---|---|
| 前向兼容 | 新增空表，不改既有 `t_*`/`demo` schema；旧客户端无强依赖该表 | **PASS** |
| 回滚策略 | 默认备份恢复 / forward-fix；PLAN §8「回滚 V6+；吊销会话」；脚本本身可安全遗留空表 | **足够（非破坏性）**；完整操作剧本见 FIND-MIG-…-002（P2） |
| 禁止依赖 Flyway down 作为唯一手段 | 交付面未引入 down 脚本依赖 | **PASS** |

## 决策摘要

| 字段 | 值 |
|---|---|
| decision | `APPROVE` |
| p0 | 0 |
| p1 | 0 |
| findingCount | 2（均为 P2） |
| migrationFilesReviewed | `V6__create_sys_user.sql` |

未标 `VERIFIED`；未写 `ai/runs/**/state.yaml` / `events.jsonl`；未修改任何 SQL 或业务代码。
