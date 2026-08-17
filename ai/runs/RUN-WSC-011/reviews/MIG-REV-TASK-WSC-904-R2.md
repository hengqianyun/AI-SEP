# 迁移审查报告 — TASK-WSC-904 Round 2

```yaml
reviewId: MIG-REV-TASK-WSC-904-R2
taskId: TASK-WSC-904
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 2
role: migrationReviewer
actorInstance: migration-reviewer-wsc-904-r2
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T12:52:00+08:00
trigger: schema-migration
basedOn:
  - ai/agents/migration-reviewer.md
  - ai/skills/migration-review/SKILL.md
  - backend/CLAUDE.md（数据库变更规则：Flyway 唯一入口、已发布脚本不可变、审计四件套、del_flag、幂等、表名、批量 UPDATE 须带 update_time/update_by）
  - planning/approved/PLAN-WSC-8.3.md（§3.2、§5 TASK-WSC-904）
  - ai/runs/RUN-WSC-011/reviews/MIG-REV-TASK-WSC-904.md（Round 1 REQUEST_CHANGES；FIND-MIG-WSC-904-001 closeWhen 原文）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-904.md（迁移审查修复节 FIND-MIG-WSC-904-001）
  - 实地：sql/migration/V7；EnterpriseMigrationSmokeTest；EnterpriseFlywaySmokeTest；git 对 V1/V5/V6 无 diff
mustDifferFrom:
  - migration-reviewer-wsc-904
  - developer-wsc-904
  - developer-wsc-904-mig-r1
  - developer-wsc-904-mig-fix
  - code-reviewer-wsc-904
closedIssues:
  - FIND-MIG-WSC-904-001
  - FIND-MIG-WSC-904-003
openIssues:
  - FIND-MIG-WSC-904-002
  - FIND-MIG-WSC-904-004
relatedReqs:
  - REQ-USER-002
riskTags: [schema-migration, handles-pii, enterprise-model]
note: |
  本轮仅复审 FIND-MIG-WSC-904-001 对未发布 V7 的修复（脚本头回滚手册、覆盖前备份表、三句 UPDATE 审计）。
  命中 schema-migration。不改源码、state.yaml、events.jsonl、R1 MIG REV。
  不代替 tester 宣称通过；不标 VERIFIED；不 git commit。
```

## 结论

**APPROVE** — **P0=0，P1=0**。Round 1 阻塞项 **FIND-MIG-WSC-904-001** 已按 closeWhen ①②③ 关闭：未发布 V7 脚本头写明前置备份、回滚触发 A/B/C、DROP INDEX/COLUMN/TABLE，以及「未匹配原名覆盖后不可恢复」；覆盖未匹配非空名称之前写入可查询备份表 `sys_user_v7_enterprise_name_backup`；三句 `UPDATE sys_user` 均显式 `SET update_time = CURRENT_TIMESTAMP(3), update_by = 'system'`。本轮无新 P0/P1。

顺带关闭 **FIND-MIG-WSC-904-003**（P2）：已有库 smoke 已断言 `old_unknown` 覆盖为「未归属默认企业」且备份表保留「未知公司」；空库 smoke 已删除不实「实体 validate 通过」注释。开放 **P2×2**（002 / 004）不阻塞本迁移门禁。

已发布 **V1 / V5 / V6 无 diff**（`git diff` 为空；工作树仅 `?? V7__...sql`）；`sql/init/` 未改。

**未代 tester 宣称测试通过**；未标 VERIFIED。本结论不批准合入生产发布门禁的 tester 步骤。

## FIND-MIG-WSC-904-001 closeWhen 核验

| closeWhen 项 | 结果 | 证据 |
|---|---|---|
| ① 脚本头：前置备份、回滚触发、DROP INDEX/COLUMN/TABLE、「未匹配原名覆盖后不可恢复」 | **PASS** | 头注释 L11–13、L15–40：备份表列清单（至少 `id, username, enterprise_name, enterprise_id`）；触发条件 A（原名丢失不可接受）/ B（validate 或回填不符）/ C（整段撤回 V7）；撤回 DDL 含 `DROP INDEX idx_sys_user_enterprise_id`、`DROP COLUMN enterprise_id`、`DROP TABLE` 备份表与 `sys_enterprise`；L13 与 L40 明确「未匹配原名覆盖后不可恢复」（除非先从备份表还原且备份表尚未删除） |
| ② 覆盖未匹配非空名称之前可查询备份表 | **PASS** | L107–136：第三段 UPDATE **之前** `CREATE TABLE IF NOT EXISTS sys_user_v7_enterprise_name_backup` + `INSERT ... SELECT`（仅 `enterprise_id<>0` 且非空名且与企业表规范名不一致的行，`NOT EXISTS` 幂等）。备份表含 closeWhen 要求的四列 + 审计四件套 + `del_flag`。L138–145 才覆盖缓存列 |
| ③ 三句 `UPDATE sys_user` 均带 `update_time` + `update_by` | **PASS** | 第一段 L81–96、第二段 L98–105、第三段 L138–145：均 `SET update_time = CURRENT_TIMESTAMP(3), update_by = 'system'`（与脚本头「操作者约定」一致）。满足 CLAUDE.md §6 批量更新硬约束 |

**顺序核验**：回填 `enterprise_id`（段 1–2）→ 备份未匹配非空原名 → 再覆盖 `enterprise_name`。备份发生在破坏性覆盖之前，符合 closeWhen ②，不是事后补表。

**恢复剧本**：头注释给出从备份表还原 `enterprise_name` 的 `UPDATE ... JOIN`（带审计字段），再给出扩展 DDL 撤回。破坏性变更不再「无回滚/前进修复说明」，skill「禁止通过」条件已解除。

## 已发布脚本不可变

| 项 | 结果 | 证据 |
|---|---|---|
| V1 / V5 / V6 | **PASS** | `git diff` 对三文件为空 |
| `sql/init/` | **PASS** | 工作树无改 |
| 仅新增未发布 V7 | **PASS** | `git status` 迁移目录仅 `?? V7__create_sys_enterprise_and_user_enterprise_id.sql` |
| 无遗留 `db/migration/` 新脚本 | **PASS** | 本轮未新增该路径 |

## FIND-MIG-WSC-904-003（P2，本轮一并关闭）

R1 closeWhen：空库 smoke 使用与生产一致的 `validate`（**或删除不实注释**）；已有库 smoke 显式断言未知原名处置，并与脚本头策略一致。

| 项 | 结果 | 证据 |
|---|---|---|
| 删除不实「实体 validate 通过」 | **PASS** | `EnterpriseFlywaySmokeTest` L17–20 改为说明 H2 将 V1 TEXT 映射为 VARCHAR、Hibernate 期望 CLOB，**不能**用生产同款 `ddl-auto=validate`；仍 `ddl-auto=none`。走 closeWhen 允许的「或删除不实注释」分支 |
| 未知原名处置与脚本头一致 | **PASS** | `EnterpriseMigrationSmokeTest` L57–70：`old_unknown.enterprise_name` =「未归属默认企业」；备份表同用户名为「未知公司」；匹配行 / 空名行不进备份 |

本角色**阅读** smoke 源码与脚本对照，**不**把开发自测或本审查阅读等同 tester `VERIFIED`。

## 仍开放（P2，不阻塞）

| id | severity | status | 说明 |
|---|---|---|---|
| FIND-MIG-WSC-904-002 | P2 | OPEN | `ALTER TABLE ... ADD COLUMN` 与 `CREATE INDEX` 仍无守卫，与回填 DML 同版本。头注释 L42–43 已写 MySQL DDL 隐式提交后须 `flyway repair` + 补偿 DML / `backfillEnterpriseIds`。与已发布 V5 同类；不升 P1 |
| FIND-MIG-WSC-904-004 | P2 | OPEN | `unassigned_default=1` 仍无部分唯一约束。非本轮 P1 修复范围 |

本轮未发现新的 P0/P1；未把 002/004 升格。备份表以 `sys_user.id` 为主键（非 `AUTO_INCREMENT`）符合「原行快照、按 id 还原」语义，不单独立项。

## 迁移正确性（复审范围）

| 检查项 | 结果 |
|---|---|
| 版本号 V7、命名、`sys_` 前缀、utf8mb4 / InnoDB | 仍 **PASS**（同 R1） |
| 无跨表物理 FK | 仍 **PASS** |
| 新表审计四件套 + `del_flag`（含备份表） | **PASS** |
| 种子 DEMO ≠ UNASSIGNED；orphan 不并入 DEMO | 仍 **PASS** |
| 批量 UPDATE 审计 | **PASS**（本轮关闭缺口） |
| 破坏性覆盖可查询备份 + 头注释回滚 | **PASS**（本轮关闭缺口） |

## 决策依据

- P1 closeWhen ①②③ 均 PASS，且无新 P0/P1 → **APPROVE**。
- P2×2 保留；003 因 closeWhen 已满足而关闭。
- 修复范围限于未发布 V7 + 对应 smoke 断言；**未改 V1–V6**。
- 本结论 **不** 代替 tester 门禁；PLAN §6.2 仍须独立 tester 后方可 `VERIFIED`。905 仍须 `denyModify **/sql/**`。

## 衔接

| 下游 | 条件 |
|---|---|
| tester | 本迁移门禁本轮 **APPROVE**；tester 须独立跑验收，本角色不代标 VERIFIED |
| Integration / 发布 | 回滚步骤可检查（脚本头 L15–43）；P2-002 失败手册已在头注释 |
| 905 | 不得改 `sql/migration/**` |
