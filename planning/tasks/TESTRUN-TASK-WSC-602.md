# 测试证据

```yaml
evidenceId: TESTRUN-TASK-WSC-602
taskId: TASK-WSC-602
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
actorInstance: tester-wsc-602
contracts: wsc-contracts@2.2.0（只读对照）
basedOn:
  - planning/approved/PLAN-WSC-5.2.md (§4 / TASK-WSC-602 acceptance + testScope)
  - planning/tasks/REV-TASK-WSC-602.md（round 2 APPROVE；P0=0 P1=0）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-602.md
  - planning/tasks/TESTRUN-TASK-WSC-601.md（格式参考）
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-602.md
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-05T17:31:55+08:00
result: PASS
failedCommandCount: 0
```

## 结论摘要

**PASS** — 独立 `tester-wsc-602` 按 PLAN-WSC-5.2 TASK-WSC-602 `testScope` 实跑：

1. 后端集成 `SysUserAuthSecurityIntegrationTest` + `SessionAuthIntegrationTest` → **exitCode=0**（Tests run: 17, Failures: 0）：登录成功/失败；三角色切角色 410+`ERR_ROLE_SWITCH_DISABLED`；ADMIN CRUD；软删不可登录；响应无 password/hash；非 ADMIN 403。
2. Vitest：`authStore` / `RoleSwitcher` / `UsersAdminPage` → **exitCode=0**（3 files / 10 tests）。
3. §4 单元格表（ADMIN/PROVIDER/USER × 用户管理 API/UI、session/role、角色区 UI）→ 命名方法定位齐全（`03-section4-cells.md`）。
4. 预落地对账 602 行 P0 → 均有命名测试类/spec 路径（`04-preland-reconcile-paths.md`）。
5. 审计无明文口令 → 审查清单勾选 PASS（`05-audit-checklist.md`）。

- 审查前提：REV-TASK-WSC-602 Round 2 **APPROVE**，P0=0，P1=0
- **未**将环境阻塞记为 PASS；命令均真实执行并记录 exitCode
- 未修改业务实现源码、`state.yaml`、`events.jsonl`；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-008/tester-wsc-602/`

## commands

| # | command | cwd | exitCode | result | evidence |
|---|---|---|---|---|---|
| 1 | `mvn -pl app/data-chain-service -am "-Dtest=SysUserAuthSecurityIntegrationTest,SessionAuthIntegrationTest" test` | `backend` | **0** | **PASS** | `01-backend-security.txt` / `01-backend-security-exit.txt` — Tests run: 17 (SessionAuth 6 + SysUser 11), Failures: 0；BUILD SUCCESS |
| 2 | `pnpm exec vitest run src/features/auth/store/authStore.spec.ts src/features/shell/components/RoleSwitcher.spec.ts src/features/users/UsersAdminPage.spec.ts` | `frontend` | **0** | **PASS** | `02-vitest.txt` / `02-vitest-exit.txt` — 3 files / 10 tests passed |
| 3 | §4 单元格表证据定位 | n/a（read + bind #1/#2） | n/a | **PASS** | `03-section4-cells.md` |
| 4 | 预落地对账 602 行 P0 路径 | n/a | n/a | **PASS** | `04-preland-reconcile-paths.md` |
| 5 | 审计无明文口令清单 | n/a（源码+日志审查） | n/a | **PASS** | `05-audit-checklist.md` |

**failedCommandCount = 0**

## testScope 对照 checklist

| # | testScope 项 | 结果 | 证据 |
|---|---|---|---|
| 1a | 登录成功/失败；会话角色=账号角色 | **PASS** | `login_success_bindsAccountRole`；`login_wrongPassword_returns401`；`login_providerAndUser_bindOwnRoles`；`authStore.spec` |
| 1b | 切角色禁用（三角色 410+码） | **PASS** | `sessionRoleSwitch_disabledForAllRoles`；authStore `switchRole` |
| 1c | ADMIN CRUD；软删不可登录 | **PASS** | `adminUsers_crudAndSoftDelete_noPasswordLeak` |
| 1d | 响应无 password/hash | **PASS** | 同上 + list/create/update 断言 |
| 1e | 审计无明文口令 | **PASS** | `05-audit-checklist.md`（AuthAuditLogger + 实跑日志） |
| 1f | 非 ADMIN API 403 | **PASS** | `adminUsers_nonAdmin_forbidden` |
| 2 | Vitest authStore / RoleSwitcher / UsersAdminPage | **PASS** | #2 exitCode=0 |
| 3 | §4 单元格表 | **PASS** | `03-section4-cells.md` |
| 4 | 预落地对账自动化命名路径 | **PASS** | `04-preland-reconcile-paths.md` |

## §4 单元格摘要

| 能力 | ADMIN | PROVIDER | USER | 证据 |
|---|---|---|---|---|
| 用户管理 API | 200 | **403** | **403** | SysUser `#adminUsers_crud…` / `#adminUsers_nonAdmin_forbidden` |
| 用户管理 UI | 可达 | **结构不可达** | **结构不可达** | `UsersAdminPage.spec.ts` + Layout `userManageVisible` |
| `POST …/session/role` | **410+码** | **410+码** | **410+码** | `#sessionRoleSwitch_disabledForAllRoles` |
| 角色区 UI | 只读无切换 | 只读无切换 | 只读无切换 | `RoleSwitcher.spec.ts` + `session-role-label` |

## REQ 追踪

| REQ | 覆盖方式 | 结果 |
|---|---|---|
| REQ-USER-001 | 真登录；ADMIN CRUD/软删；错误码；响应禁 password/hash | PASS |
| REQ-SHELL-001 | 切角色 410；角色只读唯一展示面；无 listbox；壳层 admin-users 白名单 | PASS |
| REQ-RBAC-001 | 用户管理 API/UI 仅 ADMIN；PROVIDER/USER 403 / 结构不可达 | PASS |

## BUG

无。

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未伪造 mvn / vitest exit 0
- 未修改业务实现源码、REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-602`（≠ developer-wsc-602 / ≠ code-reviewer-wsc-602-r2）
- 未自行宣称 VERIFIED

## 计数

| 指标 | 值 |
|---|---|
| result | **PASS** |
| failedCommandCount | **0** |
| 后端 Tests run | 17 / Failures 0 |
| Vitest | 3 files / 10 tests |
| BUG | 0 |
