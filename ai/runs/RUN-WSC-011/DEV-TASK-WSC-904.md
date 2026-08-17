# DEV-TASK-WSC-904

```yaml
taskId: TASK-WSC-904
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: developer-wsc-904-mig-fix
status: READY_FOR_REVIEW
completedAt: 2026-08-17T12:44:00+08:00
reqs:
  - REQ-USER-002
blocksRelease: false
riskTags: [schema-migration, handles-pii, enterprise-model]
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。
> 本任务命中 `schema-migration`，须经独立 **codeReviewer** + **migrationReviewer**；本实例不代写审查。

## objective

Wave B：最小企业实体 + `sys_user.enterprise_id` 逻辑归属；登录会话 principal 含 `enterpriseId`/`enterpriseName`（单一真源）；ADMIN 创建用户默认同企业；种子支持一企多 ADMIN；`enterprise_name` 兼容/回填/deprecate 策略可审计。

## 预落地对账（§0.2）

| 项 | 预落地 | 对账动作 |
|---|---|---|
| `sys_user` | 仅 `enterprise_name` 文本列（V6） | **V7** 增 `sys_enterprise` + `enterprise_id`；回填后名称与企业表同步 |
| 会话 Session | 仅 `enterpriseName`，默认「演示企业」 | `SessionPrincipal` + `SessionContextSupport` 输出必填 `enterpriseId`+`enterpriseName` |
| 用户 CRUD | 仅 `enterpriseName` | Create/Update/Response 含 `enterpriseId`；缺省继承操作者企业 |
| 种子 | admin / provider / user 同一文本企业名 | 同一 DEMO 企业 `admin`+`admin2` 两名 ADMIN |
| 企业管理页 | 无 | **保持无**；用户页仅展示/指定归属，无企业 CRUD |
| 契约 / `frontend/src/api/**` | 903 已含 enterpriseId | **只读**，未改 |

## writeSet 路径映射

计划字面名 `AdminUserCreateRequest` / `AdminUserUpdateRequest` / `AdminUserResponse` 对应仓库既有 `AdminUserCreate` / `AdminUserUpdate` / `AdminUser`（与 OpenAPI schema 同名）。未新建平行类型，避免双源。

`SessionContextSupport` 为会话序列化真源；`AuthController` 属 `controller/security` **同包增量**（改用 `SessionContextSupport.toMap`）。未改 `contracts/**`、catalog、layouts、state/events。

## filesChanged

| 路径 | 说明 | REQ |
|---|---|---|
| `backend/.../sql/migration/V7__create_sys_enterprise_and_user_enterprise_id.sql` | 企业表 + `enterprise_id` + 回填；覆盖未匹配原名前备份表；未改 V1–V6 | REQ-USER-002 |
| `entity/EnterpriseEntity.java` | 最小企业实体（code/name/unassignedDefault） | REQ-USER-002 |
| `entity/SysUserEntity.java` | 增 `enterpriseId`；`enterpriseName` 降为缓存列 | REQ-USER-002 |
| `repository/EnterpriseRepository.java` | 企业仓储 | REQ-USER-002 |
| `repository/SysUserRepository.java` | 按企业+角色查询；占位回填查询 | REQ-USER-002 |
| `model/SessionPrincipal.java` | 必填 `enterpriseId` | REQ-USER-002 |
| `model/AdminUser.java` / `AdminUserCreate.java` / `AdminUserUpdate.java` | 企业 id 字段 | REQ-USER-002 |
| `common/security/SessionContextSupport.java` | 会话 Map 单一真源输出 | REQ-USER-002 |
| `service/security/UserAccountService.java` | 种子、继承/指定企业、orphan 回填 | REQ-USER-002 |
| `controller/security/AdminUserController.java` | CRUD 读写 `enterpriseId` | REQ-USER-002 |
| `controller/security/AuthController.java` | 会话响应改走 SessionContextSupport（同包增量） | REQ-USER-002 |
| `src/test/java/**/security/EnterpriseUserIntegrationTest.java` | 会话/多 ADMIN/CRUD/orphan | REQ-USER-002 |
| `src/test/java/**/security/EnterpriseFlywaySmokeTest.java` | 空库 Flyway | REQ-USER-002 |
| `src/test/java/**/security/EnterpriseMigrationSmokeTest.java` | 已有 `enterprise_name` 回填 | REQ-USER-002 |
| `src/test/java/**/security/SessionAuthIntegrationTest.java` | package 与目录对齐（`security`），否则 Maven 无法加载 | — |
| `frontend/src/features/users/UsersAdminPage.vue` | 列表展示 enterpriseId；创建默认继承会话企业 | REQ-USER-002 |
| `frontend/src/features/users/UsersAdminPage.spec.ts` | 无企业 CRUD；展示/继承断言 | REQ-USER-002 |

**denyModify 遵守**：未改 `contracts/**`、`frontend/src/api/**`、catalog model/controller/service、layouts、catalog FE、e2e、planning、state/events。无企业管理页。

## `enterprise_name` 列策略（acceptance）

| 阶段 | 口径 |
|---|---|
| 兼容 | 保留 `sys_user.enterprise_name` NOT NULL；旧客户端与列表仍可读 |
| 回填 | 按名称匹配 `sys_enterprise.name`（优先非 UNASSIGNED）；空名/未知名 → `code=UNASSIGNED` |
| 单一真源 | 会话/授权只信 `enterprise_id`；写入时用企业表 `name` 覆盖缓存列，禁止双源。覆盖未匹配非空原名前写入 `sys_user_v7_enterprise_name_backup`（id,username,enterprise_name,enterprise_id）；DROP DDL 不能恢复原名 |
| deprecate | V1.6 不删列；后续版本可删除。生产 orphan 须补录真实企业，不得把 UNASSIGNED 当正式租户 |

## OQ-V16-002

- 种子企业：`DEMO`（演示企业）与 `UNASSIGNED`（未归属默认企业）**不同 id**。
- orphan（`enterprise_id=0` 且名称无法匹配）只进入 UNASSIGNED，**不会**获得 DEMO 的 mine/myCatalog 写范围（905 按 `enterpriseId` 比较即可隔离）。
- 演示种子仅用于本地；生产须补录，不得依赖默认企业扩大写权限。

## acceptance 自检

| 项 | 结果 |
|---|---|
| Flyway 幂等（版本号 + INSERT WHERE NOT EXISTS；未改已发布 V{n}） | PASS |
| 用户含 `enterpriseId` 归属；同企业多 ADMIN（admin+admin2） | PASS |
| 登录会话可解析 `enterpriseId`+`enterpriseName`（单一真源） | PASS（`904-session-enterprise-id`） |
| `enterprise_name` 回填/deprecate/兼容策略已写入脚本头与本文件 | PASS |
| ADMIN 创建用户可指定/继承企业 | PASS |
| 无企业管理页/企业 CRUD UI | PASS |
| orphan 默认企业可审计且 ≠ DEMO | PASS |

## 自测命令与结果

```bash
# 后端 testScope（data-chain-service）
mvn -q "-Dtest=EnterpriseUserIntegrationTest,EnterpriseMigrationSmokeTest,EnterpriseFlywaySmokeTest,SysUserAuthSecurityIntegrationTest,SessionAuthIntegrationTest" test
# Tests run: 4+1+1+12+6 = 24, Failures: 0, Errors: 0
# 命名用例：EnterpriseUserIntegrationTest.session_enterprise_id_904 @DisplayName("904-session-enterprise-id")

# 前端用户页增量
pnpm --dir frontend exec vitest run src/features/users/UsersAdminPage.spec.ts
# Test Files 1 passed; Tests 9 passed
```

## 迁移审查修复（FIND-MIG-WSC-904-001 / round-2）

回应 `MIG-REV-TASK-WSC-904` **P1**。未改 V1–V6、state/events/REV。不代写复审。status 仍为 `READY_FOR_REVIEW`（禁止 VERIFIED）。

| closeWhen | 落实 |
|---|---|
| ① 脚本头：前置备份、回滚触发、DROP INDEX/COLUMN/TABLE、「未匹配原名覆盖后不可恢复」 | V7 头注释含备份表列清单、触发条件 A/B/C、撤回 DDL、以及无备份则原名不可恢复 |
| ② 覆盖未匹配非空名称前可查询备份 | 同脚本 `CREATE TABLE IF NOT EXISTS sys_user_v7_enterprise_name_backup` + `INSERT ... SELECT`（仅非空且将与企业表规范名不一致的行） |
| ③ 三句 `UPDATE sys_user` 带审计 | 均 `SET update_time = CURRENT_TIMESTAMP(3), update_by = 'system'` |

顺带 **FIND-MIG-WSC-904-003**（P2，不单独阻塞）：已有库 smoke 断言 `old_unknown` 覆盖为「未归属默认企业」且备份表保留「未知公司」；空库 smoke 因 H2 将 V1 TEXT 映射为 VARCHAR、Hibernate 期望 CLOB，**不能**用生产同款 `ddl-auto=validate`，已删除不实「实体 validate 通过」注释并写明原因。

### 本轮自测

```bash
mvn -q "-Dtest=EnterpriseUserIntegrationTest,EnterpriseMigrationSmokeTest,EnterpriseFlywaySmokeTest,SysUserAuthSecurityIntegrationTest,SessionAuthIntegrationTest" test
# Tests run: 4+1+1+12+6 = 24, Failures: 0, Errors: 0
```

复审须由**另一** `migrationReviewer` 实例执行。

## 后续任务提示

- **905** 消费 `SessionPrincipal.enterpriseId` 做 mine/myCatalog/写隔离；本任务不改 catalog API。
- 须独立 **migrationReviewer**（schema-migration）与 **codeReviewer**；通过前不得 VERIFIED。
