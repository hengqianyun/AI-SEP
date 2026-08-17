# 测试证据 — TASK-WSC-904

```yaml
testrunId: TESTRUN-TASK-WSC-904
taskId: TASK-WSC-904
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: tester-wsc-904
mustDifferFrom:
  - developer-wsc-904
  - developer-wsc-904-mig-r1
  - developer-wsc-904-mig-fix
  - code-reviewer-wsc-904
  - migration-reviewer-wsc-904
  - migration-reviewer-wsc-904-r2
basedOn:
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-904 testScope/acceptance）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-904.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-904.md（APPROVE；P0/P1=0）
  - ai/runs/RUN-WSC-011/reviews/MIG-REV-TASK-WSC-904-R2.md（APPROVE；P0/P1=0）
  - product/requirements/SNAP-WSC-008.md（REQ-USER-002、OQ-V16-002）
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
migrationReviewDecision: APPROVE
migrationReviewP0: 0
migrationReviewP1: 0
executedAt: 2026-08-17T12:50:51+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-USER-002]
riskTags: [schema-migration, handles-pii, enterprise-model]
note: |
  独立 tester-wsc-904 实跑 testScope。未改源码、审查结论、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  JVM shutdownHook 在全部断言完成后打印 H2「Database is already closed」，不影响 Tests run / BUILD SUCCESS。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-904` 依 PLAN-WSC-8.3 §5 TASK-WSC-904 `testScope` 实际执行：后端 5 类共 **24** 测 **BUILD SUCCESS**（exitCode=0）；前端 `UsersAdminPage.spec.ts` **9/9** 通过（exitCode=0）。命名用例 `904-session-enterprise-id`（方法 `session_enterprise_id_904`）已执行并通过。覆盖：多 ADMIN 同企业、会话 `enterpriseId`、用户 CRUD 企业字段、orphan 默认企业负例、空库 Flyway、已有库 `enterprise_name` 回填与备份表断言、用户页企业字段增量且无企业 CRUD。

审查前提：codeReview **APPROVE（P0=0、P1=0）**；migrationReview R2 **APPROVE（P0=0、P1=0）**。本报告不代标 VERIFIED。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | 结果 | 证据 |
|---|---|---|---|---:|---|---|
| 1 | backend-integration / migration-smoke | `mvn -f backend/pom.xml -pl app/data-chain-service --batch-mode "-Dtest=EnterpriseUserIntegrationTest,EnterpriseMigrationSmokeTest,EnterpriseFlywaySmokeTest,SysUserAuthSecurityIntegrationTest,SessionAuthIntegrationTest" test` | repo root | **0** | **PASSED** — Tests run: **24**, Failures: 0, Errors: 0, Skipped: 0；BUILD SUCCESS（Total time 33.566 s） | `backend-904.txt`；`surefire-904-summary.txt` |
| 2 | frontend-vitest | `pnpm --dir frontend exec vitest run src/features/users/UsersAdminPage.spec.ts` | `frontend/` | **0** | **PASSED** — Test Files 1 passed；Tests **9 passed**（Vitest v2.1.9） | `vitest-904.txt` |

**failedCommandCount = 0**。无环境阻塞；未把未执行项记为 PASS。

环境：Java 17.0.11、Maven 3.6.3、H2 in-memory（测试自带，无外部 MySQL/Redis）。

## 后端分套件（Surefire）

| 类 | tests | failures | errors | skipped | 结果 |
|---|---:|---:|---:|---:|---|
| `EnterpriseFlywaySmokeTest` | 1 | 0 | 0 | 0 | PASS |
| `EnterpriseMigrationSmokeTest` | 1 | 0 | 0 | 0 | PASS |
| `EnterpriseUserIntegrationTest` | 4 | 0 | 0 | 0 | PASS |
| `SessionAuthIntegrationTest` | 6 | 0 | 0 | 0 | PASS |
| `SysUserAuthSecurityIntegrationTest` | 12 | 0 | 0 | 0 | PASS |
| **合计** | **24** | **0** | **0** | **0** | **PASS** |

### EnterpriseUserIntegrationTest 明细

| 方法 | DisplayName / testScope 映射 | 结果 |
|---|---|---|
| `session_enterprise_id_904` | **§0.2 命名用例 `904-session-enterprise-id`**；登录/当前会话含 `enterpriseId`+`enterpriseName` | **PASS**（time=0.131s） |
| `seed_multipleAdminsSameEnterprise` | 多 ADMIN 同企业（admin+admin2 同 DEMO） | **PASS** |
| `createUser_inheritsOperatorEnterprise_andCanSpecifyEnterpriseId` | 用户 CRUD 企业字段：缺省继承 + 指定 UNASSIGNED | **PASS** |
| `orphanDefault_doesNotShareDemoEnterpriseId` | orphan 默认策略负例：回填后 id=UNASSIGNED ≠ DEMO | **PASS** |

Surefire `testcase name="session_enterprise_id_904"` 无 failure/error 元素。源码 `@DisplayName("904-session-enterprise-id")` 与本轮方法执行对应。

### 迁移 smoke 明细

| 方法 | testScope 映射 | 结果 |
|---|---|---|
| `emptyDatabase_flywayCreatesEnterpriseAndUserFk` | 空库 Flyway：V7 企业表 + `sys_user.enterprise_id`；DEMO ≠ UNASSIGNED | **PASS** |
| `existingDatabase_enterpriseNameBackfill` | 已有库：`enterprise_name` 回填；`old_unknown` 覆盖为「未归属默认企业」；备份表保留「未知公司」；匹配/空名不进备份 | **PASS** |

### 回归套件（DEV 所列等价）

`SysUserAuthSecurityIntegrationTest`（12）与 `SessionAuthIntegrationTest`（6）全部 PASS：登录绑定、用户 CRUD/软删/硬删、非 ADMIN 403、角色切换禁用、RBAC 矩阵写路径。本轮未发现 904 回归。

## 前端明细（UsersAdminPage.spec.ts）

| 用例 | testScope 映射 | 结果 |
|---|---|---|
| `904: shows enterpriseId, inherits session enterprise on create, no enterprise CRUD UI` | 企业字段增量：列表展示、创建继承会话企业、无企业管理/CRUD | **PASS**（含于 9/9） |
| 其余 8 例（602/606 用户页回归） | 非 904 增量，本轮未回退 | **PASS** |

整文件 **9 passed / 0 failed**。

## testScope 对照（PLAN-WSC-8.3 §5 TASK-WSC-904）

| # | testScope / 覆盖要求 | 结果 | 证据 |
|---|---|---|---|
| 1 | 集成：多 ADMIN 同企业 | **PASS** | `seed_multipleAdminsSameEnterprise` |
| 2 | 集成：会话含 enterpriseId | **PASS** | `session_enterprise_id_904` |
| 3 | 集成：用户 CRUD 企业字段 | **PASS** | `createUser_inheritsOperatorEnterprise_andCanSpecifyEnterpriseId` |
| 4 | 迁移 smoke：空库 | **PASS** | `emptyDatabase_flywayCreatesEnterpriseAndUserFk` |
| 5 | 迁移 smoke：已有库（含既有 `enterprise_name`）回填/备份 | **PASS** | `existingDatabase_enterpriseNameBackfill` |
| 6 | 安全：orphan 默认策略负例（≠ DEMO 写范围） | **PASS** | `orphanDefault_doesNotShareDemoEnterpriseId` |
| 7 | §0.2 命名用例 `904-session-enterprise-id` | **PASS** | 方法已执行；见 Surefire |
| 8 | 前端用户页企业字段增量 | **PASS** | spec 第 9 例 + 9/9 |

## acceptance 对照（执行层）

| acceptance 项 | 结果 | 本轮证据 |
|---|---|---|
| Flyway 可重复执行（空库 smoke 应用到 V7） | **PASS** | `EnterpriseFlywaySmokeTest` |
| 用户含 `enterpriseId` 归属；同企业多 ADMIN | **PASS** | 种子 + 列表断言 admin/admin2 |
| 登录会话可解析 `enterpriseId`+`enterpriseName`（单一真源） | **PASS** | `904-session-enterprise-id` |
| `enterprise_name` 回填/兼容（已有库 + 备份表） | **PASS** | migration smoke：规范名覆盖 + 备份「未知公司」 |
| ADMIN 创建用户可指定/继承企业 | **PASS** | inherit + specify UNASSIGNED |
| 无企业管理页/企业 CRUD UI | **PASS** | FE 负例：无「企业管理」、无 create/delete/listEnterprise |
| OQ-V16-002：orphan 默认 ≠ 他企（DEMO）写范围 | **PASS（904 层）** | 回填后 id ≠ DEMO；catalog 写拦截归 905 |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-USER-002 | 最小企业实体、用户 `enterpriseId`、会话企业字段、一企多 ADMIN、创建继承/指定、无企业 CRUD | **PASS** |
| OQ-V16-002 | orphan → UNASSIGNED，与 DEMO 不同 id | **PASS（904 层）** |

## 已知限制（不阻塞 PASS）

| 来源 | 说明 |
|---|---|
| FIND-WSC-904-001/002/003（P2，OPEN） | codeReview 已记；本轮未升格。003 含：无 PUT 改 `enterpriseId` 专测、无 orphan **登录会话**断言 |
| FIND-MIG-WSC-904-002/004（P2，OPEN） | migrationReview R2 已记；不阻塞本测试门禁 |
| JVM shutdownHook H2 90121 | 出现于 **Results: Tests run: 24 Failures: 0** 之前的 SessionFactory 关闭阶段；**未**计入 Failures/Errors；**未**记为测试 FAIL |

本 tester **不**因开放 P2 另开 `BUG-*`。PUT 改归属与 orphan 登录会话属审查已知缺口，不在本轮失败集。

## 缺陷

无新开 `BUG-*`。

## 审查前提

| 项 | 值 |
|---|---|
| codeReview | `REV-TASK-WSC-904` **APPROVE**；P0=0、P1=0 |
| migrationReview | `MIG-REV-TASK-WSC-904-R2` **APPROVE**；P0=0、P1=0 |
| 开放 P2（不阻塞） | FIND-WSC-904-001/002/003；FIND-MIG-WSC-904-002/004 |

## 证据红线（自检）

- [x] 实际执行 testScope（命令 + exitCode），非仅读 DEV/REV
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未改源码 / 审查结论 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-904`，与 developer / codeReviewer / migrationReviewer 隔离
- [x] 未自行标任务 VERIFIED
- [x] 失败未发生，故未开 BUG-*

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-904.md`
- 后端全量日志：`ai/runs/RUN-WSC-011/tests/backend-904.txt`
- 前端 Vitest 日志：`ai/runs/RUN-WSC-011/tests/vitest-904.txt`
- Surefire 方法清单：`ai/runs/RUN-WSC-011/tests/surefire-904-summary.txt`
- 任务包：`planning/approved/PLAN-WSC-8.3.md` §5 TASK-WSC-904
- 开发 / 审查：`DEV-TASK-WSC-904.md`、`reviews/REV-TASK-WSC-904.md`、`reviews/MIG-REV-TASK-WSC-904-R2.md`
