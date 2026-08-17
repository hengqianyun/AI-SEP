# 迁移审查报告 — TASK-WSC-904

```yaml
reviewId: MIG-REV-TASK-WSC-904
taskId: TASK-WSC-904
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: migrationReviewer
actorInstance: migration-reviewer-wsc-904
decision: REQUEST_CHANGES
p0Count: 0
p1Count: 1
p2Count: 3
reviewedAt: 2026-08-17T12:40:00+08:00
trigger: schema-migration
basedOn:
  - ai/agents/migration-reviewer.md
  - ai/skills/migration-review/SKILL.md
  - backend/CLAUDE.md（数据库变更规则：Flyway 唯一入口、已发布脚本不可变、审计四件套、del_flag、幂等、表名、批量 UPDATE 须带 update_time/update_by）
  - planning/approved/PLAN-WSC-8.3.md（§3.2、§5 TASK-WSC-904 / TASK-WSC-905 denyModify `**/sql/**`、OQ-V16-002）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-904.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-904.md（codeReview APPROVE；明确不代本角色批准迁移）
  - 实地：sql/migration/V1、V5、V6、V7；sql/init 未改；EnterpriseEntity / SysUserEntity；UserAccountService.backfillEnterpriseIds；EnterpriseFlywaySmokeTest / EnterpriseMigrationSmokeTest
mustDifferFrom:
  - developer-wsc-904
  - code-reviewer-wsc-904
relatedReqs:
  - REQ-USER-002
openIssues:
  - FIND-MIG-WSC-904-001
  - FIND-MIG-WSC-904-002
  - FIND-MIG-WSC-904-003
  - FIND-MIG-WSC-904-004
riskTags: [schema-migration, handles-pii, enterprise-model]
note: |
  命中 schema-migration。本报告只写迁移正确性 / 顺序 / 兼容 / 回滚；不改源码、state.yaml、events.jsonl。
  不代替 tester 宣称通过；不标 VERIFIED；不 git commit。
  codeReview APPROVE 不覆盖本迁移门禁。
```

## 结论

**REQUEST_CHANGES** — **P0=0，P1=1，P2=3**。

V7 作为**未发布**新脚本，版本号、命名、表名、审计四件套、`del_flag`、无跨表物理 FK、904 独占序列、OQ-V16-002 种子与 DEMO 分 id，均符合 `backend/CLAUDE.md` 与 PLAN §3.2 / §5。已发布 **V1 / V5 / V6 无 diff**；`sql/init/` 未改。

阻塞项是：**第三段 `UPDATE` 不可逆覆盖未匹配的 `enterprise_name`，且脚本头 / DEV 无回滚或前进修复剧本**（含备份与 `update_by` 审计）。违反 migration-review skill「禁止通过：无回滚/前进修复说明的破坏性变更」，以及审查重点中的回滚可行性与 orphan 可审计。P1 清零前不得对本迁移门禁 `APPROVE`。

**未代 tester 宣称测试通过**；未标 VERIFIED。

## 迁移文件与序列

| 项 | 结果 | 证据 |
|---|---|---|
| 新增脚本路径 / 命名 | **PASS** | `backend/app/data-chain-service/src/main/resources/sql/migration/V7__create_sys_enterprise_and_user_enterprise_id.sql`：`V` 大写、双下划线、全小写动宾 |
| 版本递增、无序号冲突 | **PASS** | 目录现有 `V1`、`V5`、`V6`（历史缺口非本任务）；下一版本为 **V7**；无 V8+ 争用 |
| 已发布脚本不可变 | **PASS** | `git diff` 对 V1/V5/V6 为空；`git status` 仅 `?? V7__...sql` |
| 未写入遗留 `db/migration/` | **PASS** | 仓库无该路径新文件 |
| `sql/init/` 未塞业务 schema | **PASS** | 904 filesChanged 未列 init；工作树 init 无改 |
| Flyway 唯一入口 | **PASS** | 无应用内手写 DDL；`UserAccountService` 仅 JPA 种子/回填行数据；生产 `ddl-auto=validate` |
| 904 独占 vs 905 | **PASS** | PLAN §0.3 / §3.2：904 独占 `sql/migration/**`；905 `denyModify` 含 `**/sql/**`；无并行版本号冲突 |

## 清单对照（skill + CLAUDE.md）

| 检查项 | 结果 | 说明 |
|---|---|---|
| 新建表 `sys_` 前缀、单数、utf8mb4 / InnoDB | **PASS** | `sys_enterprise` |
| 主键 `id BIGINT UNSIGNED AUTO_INCREMENT` | **PASS** | V7 L15 |
| 禁止跨表物理 FK | **PASS** | `enterprise_id` 注释为逻辑归属；计划字面 FK 按逻辑实现（与 codeReview 一致，不升 P0） |
| 唯一键 / 普通索引命名 | **PASS** | `uk_sys_enterprise_code`、`idx_sys_enterprise_name`、`idx_sys_user_enterprise_id` |
| 审计四件套 + `del_flag` | **PASS** | 新表列齐全；`sys_user` 沿用 V6 |
| `CREATE TABLE IF NOT EXISTS` + 种子 `INSERT ... WHERE NOT EXISTS` | **PASS** | DEMO / UNASSIGNED 按 `code` 幂等插入 |
| ALTER / CREATE INDEX 幂等 | **见 P2-002** | 无 `IF NOT EXISTS`；头注释依赖 Flyway 成功一次；与已发布 V5 同类 |
| 实体与脚本同步 | **PASS** | `EnterpriseEntity` / `SysUserEntity.enterpriseId` 对齐；无漏列 |
| 回填与 DDL 分离、可分批 | **见 P2-002** | 同脚本混合；`sys_user` 体量小，不升 P1 |
| 破坏性变更回滚剧本 | **FAIL → P1-001** | 未匹配名覆盖无备份、无 DROP 回滚步骤 |
| 空库路径 | **PASS（脚本）** | V1→V6→V7 后 Java 种子；`EnterpriseFlywaySmokeTest` 覆盖企业行 + 列存在 + admin∈DEMO |
| 已有库（含既有 `enterprise_name`） | **部分** | `EnterpriseMigrationSmokeTest`：演示名→DEMO，空名/未知名→UNASSIGNED；**未断言未知原名保留或已备份**（P2-003） |
| 锁表 / 长事务 | **PASS（低风险）** | MySQL 8 加列+默认值多为 INSTANT；三句全表 UPDATE 作用于用户表（非产品目录）。须在变更窗口执行，无需停机窗口升级 |
| 单主库、无旁路 DDL | **PASS** | |

## `enterprise_name` 策略（ISSUE-SA-R1-006）

| 阶段 | 脚本/DEV 口径 | 本角色判定 |
|---|---|---|
| 兼容 | 保留 `sys_user.enterprise_name` NOT NULL | **PASS** |
| 回填 | 名称匹配非 UNASSIGNED；失败或空名 → `code=UNASSIGNED` | **PASS（映射）** |
| 单一真源 | 会话/授权只信 `enterprise_id`；缓存列用企业表 `name` 覆盖 | **策略有文档**；覆盖未匹配原名见 P1-001 |
| deprecate | V1.6 不删列；后续版本可删 | **PASS** |
| 双源冲突 | 写入同步 id+规范名 | 运行期 `applyEnterprise` 与 V7 第三段一致；**未匹配行以销毁原名为代价消除双源** |

## OQ-V16-002（orphan 默认企业种子可审计）

| 项 | 结果 | 证据 |
|---|---|---|
| 种子企业 DEMO ≠ UNASSIGNED | **PASS** | 两行 `INSERT ... WHERE NOT EXISTS`；`code` 唯一；`unassigned_default` 仅 UNASSIGNED=1；`create_by/update_by='system'` |
| orphan 不并入 DEMO | **PASS（映射）** | 第一段 UPDATE 排除 `unassigned_default=1`；剩余 `enterprise_id=0` 进 UNASSIGNED |
| 不得因默认策略获他企写权限 | **PASS（904 层）** | 分 id 即可被 905 按 `enterpriseId` 隔离；本任务不改 catalog 写拦截 |
| 回填操作可审计到用户行 | **FAIL → P1-001** | 三句 `UPDATE sys_user` **未** `SET update_by`（亦未显式 `update_time`）；`update_time` 或许可靠 `ON UPDATE CURRENT_TIMESTAMP` 刷新，**`update_by` 仍为原值**，无法从用户行识别「V7/system 回填」 |

Java `backfillEnterpriseIds` 为启动补偿（非 DDL），与 Flyway 双路径一致，不构成本门禁通过条件。

## 空库 vs 已有库

- **空库**：Flyway 建 `sys_enterprise` 并种子 → `ALTER` 加列默认 0 → 回填（此时用户表可能仍空）→ 应用 `ensureSeedUsers` 写入 admin/admin2 等同 DEMO。与 DEV 预落地对账一致。
- **已有库**：V6 仅有 `enterprise_name` 的行经 V7 回填。`「演示企业」→ DEMO`；空名/未知名 → UNASSIGNED。H2 直连 smoke **未走 Flyway 版本表**，不证明 MySQL `validate-on-migrate` + `ddl-auto=validate`（P2-003）。
- **哨兵 0**：列 `DEFAULT 0` 在回填后仍保留；漏回填行会带着 0 进入运行期（codeReview FIND-WSC-904-002 已标 P2，本角色不重复升格）。

## 回滚与锁

当前 **无** 可复制回滚步骤。扩展性 DDL（建表/加列/加索引）本身可前进修复为：

```sql
-- 仅示意缺口；本审查要求写入脚本头后方可关闭 P1
DROP INDEX `idx_sys_user_enterprise_id` ON `sys_user`;
ALTER TABLE `sys_user` DROP COLUMN `enterprise_id`;
DROP TABLE IF EXISTS `sys_enterprise`;
```

第三段 UPDATE 若已执行，未匹配的原 `enterprise_name`（如 smoke 中的「未知公司」）**不能**由上述 DDL 回滚恢复。故不能把本迁移当成纯加列而批准。

锁表风险低，不单独开 finding。

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-MIG-WSC-904-001 | P1 | OPEN | V7 L69–74 将已匹配及未匹配行的 `enterprise_name` 覆盖为企业表规范名；未匹配原名（smoke 插入「未知公司」）被销毁且无备份表。脚本头 L1–9 与 DEV「enterprise_name 列策略」只写兼容/回填/deprecate，**无**备份、回滚 SQL、验证步骤。三句 UPDATE（L47–74）均未显式 `update_time`/`update_by`，违反 CLAUDE.md §6 批量更新硬约束，用户行无法审计为迁移回填。skill 禁止在无回滚/前进修复说明时通过破坏性变更 | ① V7 **尚未发布，允许改本文件**：脚本头写明前置备份（至少 `id,username,enterprise_name,enterprise_id`）、回滚触发条件、上列 DROP INDEX/COLUMN/TABLE、以及「未匹配原名覆盖后不可恢复」。② 覆盖未匹配非空名称**之前**写入可查询备份（同脚本 `CREATE TABLE IF NOT EXISTS` 备份表 + `INSERT ... SELECT`，或跳过覆盖并在策略中声明 UNASSIGNED 行缓存列可暂与企业表名不一致直至补录）。③ 三句 `UPDATE sys_user` 均 `SET update_time = CURRENT_TIMESTAMP(3), update_by = 'system'`（或脚本约定操作者） | REQ-USER-002 / OQ-V16-002 |
| FIND-MIG-WSC-904-002 | P2 | OPEN | `ALTER TABLE ... ADD COLUMN` 与 `CREATE INDEX` 无守卫，与回填 DML 同版本。MySQL DDL 隐式提交后若 UPDATE 失败，Flyway 重跑会 Duplicate column。与已发布 V5 同类；`sys_user` 行数预期很小 | 可选拆 V8 只做回填；或在失败手册中写明「列已存在则 `flyway repair` 后只跑补偿 DML / 启动 `backfillEnterpriseIds`」。不阻塞本 P2 单独存在时的重审，但 P1 仍须先关 | REQ-USER-002 |
| FIND-MIG-WSC-904-003 | P2 | OPEN | `EnterpriseFlywaySmokeTest` 注释写「实体 validate 通过」，实际 `ddl-auto=none`。`EnterpriseMigrationSmokeTest` 用 JDBC 执行 V6+V7，不断言 `old_unknown` 的 `enterprise_name` 是保留、备份还是已覆盖为「未归属默认企业」 | 空库 smoke 使用与生产一致的 `validate`（或删除不实注释）；已有库 smoke 显式断言未知原名的处置，并与脚本头策略一致 | REQ-USER-002 |
| FIND-MIG-WSC-904-004 | P2 | OPEN | `unassigned_default=1` 仅靠种子与 Java `requireEnterprise` 保证一行，无部分唯一约束；软删后再插可能出现多行占位，`MIN(id)` / `findFirst` 不确定 | 后续版本可用生成列/唯一键约束「未删除且 unassigned_default=1 至多一行」，或文档约定禁止第二行 | OQ-V16-002 |

## 决策依据

- P1 未清零 → **REQUEST_CHANGES**。修复范围仅限**未发布的 V7 脚本头 + 回填 UPDATE / 备份表**（及对应 smoke 断言）；**禁止改 V1–V6**。
- P2×3 不单独阻塞，但 P1 closeWhen 建议一并处理 003 的未知原名断言，避免策略与测试分叉。
- 本结论 **不** 批准合入生产迁移；**不** 宣称 tester 门禁通过；905 仍须 deny sql。

## 衔接

| 下游 | 条件 |
|---|---|
| 开发返工 | 关闭 FIND-MIG-WSC-904-001 后由**另一** `migrationReviewer` 实例复审（本 actor 不得既改脚本又批准） |
| tester | 迁移门禁未通过前不得 `VERIFIED`（PLAN §6.2） |
| Integration / 发布 | 须本角色（或后续 round）`APPROVE` 且回滚步骤可检查 |
