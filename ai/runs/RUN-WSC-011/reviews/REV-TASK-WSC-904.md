# 代码审查报告 — TASK-WSC-904

```yaml
reviewId: REV-TASK-WSC-904
taskId: TASK-WSC-904
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-904
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 3
reviewedAt: 2026-08-17T12:35:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-8.3.md（§3.2、§5 TASK-WSC-904 writeSet/denyModify/acceptance/testScope）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-904.md
  - product/requirements/SNAP-WSC-008.md（REQ-USER-002、OQ-V16-002、Out of Scope 企业管理页）
  - 实地 diff：Flyway V7、Enterprise/SysUser 实体、SessionPrincipal、SessionContextSupport、AuthController、AdminUser*、UserAccountService、users FE、security 测试
mustDifferFrom: developer-wsc-904
riskTags: [schema-migration, handles-pii, enterprise-model]
note: |
  命中 schema-migration：本 REV 仅 codeReview 门禁；**不得**代 migrationReviewer 批准迁移。
  未代 tester 宣称 VERIFIED；未改源码 / state.yaml / events.jsonl。
```

## 结论

**APPROVE** — **P0=0，P1=0**。实现满足 PLAN-WSC-8.3 §5 TASK-WSC-904 与 REQ-USER-002：最小企业实体 + `sys_user.enterprise_id` 逻辑归属、会话 `enterpriseId`/`enterpriseName` 单一真源、一企多 ADMIN、创建用户可指定/继承、无企业管理页、orphan 默认进 UNASSIGNED 且与 DEMO 不同 id。`AuthController` 改走 `SessionContextSupport.toMap` 属 `controller/security` **同包增量**，在 writeSet 内。denyModify 的 contracts/api/catalog/layouts 无本任务增量。开放 **P2×3** 不阻塞进入 tester / migrationReviewer 门禁。

## Scope 检查（denyModify / writeSet）

| 项 | 结果 | 证据 |
|---|---|---|
| Flyway 仅 V7 新增；未改 V1–V6 | **PASS** | `V7__create_sys_enterprise_and_user_enterprise_id.sql` 为 untracked 新文件 |
| 实体 / repository / UserAccountService / AdminUser* / SessionPrincipal / SessionContextSupport | **PASS** | 与计划 writeSet 及「计划字面名 → 既有 AdminUserCreate/Update/AdminUser」映射一致 |
| **AuthController** 同包增量 | **PASS** | 计划写 `AdminUserController.java（及同包增量）`；`controller.security.AuthController` 仅将 `SessionViews.toMap` 换成 `SessionContextSupport.toMap`（login/current），为实现会话单一真源所必需 |
| `SessionAuthIntegrationTest` 包名对齐 | **PASS** | writeSet 含 `src/test/java/**/security/**`；仅 `package` 从 `model` 改为 `security`，未改 catalog 断言 |
| 未改 `contracts/**`、`frontend/src/api/**` | **PASS（904 交付）** | DEV filesChanged 未列；工作树中该路径脏文件归属 TASK-WSC-903 |
| 未改 catalog controller/service/model | **PASS（904 交付）** | 904 增量无 catalog；`L2Distribution*` 脏树归属 TASK-WSC-902 |
| 未改 `frontend/src/layouts/**`、catalog FE、e2e、planning、product | **PASS** | users 页 spec 只读断言 layout/routes，未改文件 |
| 未改 `state.yaml` / `events.jsonl` | **PASS** | 本实例只写本 REV |
| 无企业管理页 / 企业 CRUD UI | **PASS** | `UsersAdminPage.vue` 仅展示/指定用户归属；无 create/delete/list enterprise；spec 负例 |

工作树并行脏文件（901/902/903）不计入 904 越界。

## Acceptance 对照

| acceptance 项 | 结果 | 证据 |
|---|---|---|
| 用户含 `enterpriseId` 归属 | **PASS** | `SysUserEntity.enterpriseId`；AdminUser 响应序列化 `enterpriseId` |
| 同企业可有多 ADMIN | **PASS** | 空库种子 `admin`+`admin2` 同 DEMO；`seed_multipleAdminsSameEnterprise` |
| 登录会话可解析 `enterpriseId`+`enterpriseName`（单一真源） | **PASS** | `toPrincipal` 取 `enterprise_id`；名称走企业表；`AuthController` → `SessionContextSupport.toMap`；`@DisplayName("904-session-enterprise-id")` |
| `enterprise_name` 兼容/回填/deprecate 且不双源 | **PASS** | V7 脚本头 + DEV 策略表；写入 `applyEnterprise` 同步缓存列；会话/授权不信名称 |
| ADMIN 创建用户可指定/继承企业 | **PASS** | 缺省 `actorEnterpriseId`；指定 `enterpriseId`；不新建企业 |
| 无企业管理页 | **PASS** | 见 Scope；SNAP Out of Scope 对齐 |
| OQ-V16-002：orphan 默认 ≠ 他企写范围 | **PASS（904 层）** | DEMO 与 UNASSIGNED 不同 id；空名/未知名 → UNASSIGNED；`orphanDefault_doesNotShareDemoEnterpriseId`。写拦截归 905 消费本字段 |

## `enterprise_name` 策略（ISSUE-SA-R1-006）

| 阶段 | 代码/脚本口径 | 结果 |
|---|---|---|
| 兼容 | 保留 `sys_user.enterprise_name` NOT NULL | **PASS** |
| 回填 | 按名称匹配非 UNASSIGNED；失败 → `code=UNASSIGNED` | **PASS**（V7 SQL + `backfillEnterpriseIds`） |
| 单一真源 | 会话只输出 `enterprise_id` 对应 id + 企业表 name | **PASS**（活路径） |
| deprecate | V1.6 不删列；注释写明后续可删 | **PASS** |

逻辑 FK（无物理外键）符合 `backend/CLAUDE.md`；计划字面「FK」按逻辑归属实现，不升格为 P0/P1。迁移脚本可重复性/回滚/锁表由 **migrationReviewer** 专审。

## AuthController 范围判定

计划 writeSet 未点名 `AuthController.java`，但写明 `AdminUserController.java（及同包增量）`，且点名 `SessionContextSupport`（或等价会话解析类）。

实地 diff 仅两处响应组装：`POST/GET /api/v1/auth/session` 由已无 `enterpriseId` 的 `SessionViews` 改为 `SessionContextSupport.toMap`。不改登录/登出/角色切换语义。

**判定：范围内。** 若不改 AuthController，会话 JSON 仍缺 `enterpriseId`，acceptance「单一真源」无法闭合。

## 测试覆盖（testScope）

| 项 | 结果 |
|---|---|
| 多 ADMIN 同企业 | **有** `seed_multipleAdminsSameEnterprise` |
| 会话含 enterpriseId；§0.2 `904-session-enterprise-id` | **有** `@DisplayName("904-session-enterprise-id")` |
| 用户 CRUD 企业字段 | **有** 创建继承 + 指定 UNASSIGNED；**无** PUT 改归属专测（P2） |
| 迁移 smoke 空库 / 已有 `enterprise_name` | **有** `EnterpriseFlywaySmokeTest`、`EnterpriseMigrationSmokeTest` |
| orphan 负例 | **有** 回填后 id ≠ DEMO |
| FE 无企业 CRUD + 展示/继承 | **有** spec 第 9 例 |

独立复验（审查证据，**不**代替 tester）：

- 后端 `EnterpriseUserIntegrationTest,EnterpriseMigrationSmokeTest,EnterpriseFlywaySmokeTest,SysUserAuthSecurityIntegrationTest,SessionAuthIntegrationTest` → Maven **exit 0**
- 前端 `UsersAdminPage.spec.ts` → **9 passed**

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-904-001 | P2 | OPEN | `common/support/SessionViews.java` 仍默认 `enterpriseName="演示企业"` 且**不输出** `enterpriseId`；活路径已切到 `SessionContextSupport`，该类无调用方。残留双路径，后续误用会破坏单一真源 | 删除 `SessionViews`，或改为委托 `SessionContextSupport.toMap` 并加废弃注解 | REQ-USER-002 |
| FIND-WSC-904-002 | P2 | OPEN | `toPrincipal` 将 `enterpriseId=0` 序列化为 `"0"`，`toView` 则输出 `""`。回填后运行期不应出现 0；若漏回填，创建用户表单预填 `"0"` 会触发 `enterpriseId 无效` | 登录路径对 `<=0` 解析为 UNASSIGNED（或拒绝登录），与 `toView` 对齐 | OQ-V16-002 |
| FIND-WSC-904-003 | P2 | OPEN | `EnterpriseFlywaySmokeTest` 注释写「实体 validate 通过」，但 `ddl-auto=none` 而非 `validate`；缺 PUT 改 `enterpriseId` 与 orphan **登录会话**断言 | 空库 smoke 改为 `validate`（或删注释）；可选补 PUT/orphan session 用例 | REQ-USER-002 |

## 架构与可维护性备注（非阻塞）

- `applyEnterprise` 同时写 id 与规范名称，避免缓存列漂移。
- 创建未知企业名时不建新企业、回退操作者企业，符合「无企业 CRUD」。
- 既有库不自动插入 `admin2`（`ensureSeedUsers` 仅空库）；能力仍可通过 ADMIN 创建第二名 ADMIN 满足。
- `frontend/src/api/auth.ts` 中 `Session.enterpriseId` 仍为 optional（903 P2 / 904 denyModify），904 FE 用可选链继承，不阻塞本任务。

## 决策依据

- P0=0、P1=0 → **APPROVE**，可进入独立 **tester** 与 **migrationReviewer**（`schema-migration`）门禁。
- 本 REV **不**批准 Flyway 发布/回滚；**不**宣称测试门禁通过。
