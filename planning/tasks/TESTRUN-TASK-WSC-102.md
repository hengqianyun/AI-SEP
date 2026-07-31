# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-102-1
taskId: TASK-WSC-102
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-102
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/TASK-WSC-102.md
  - planning/tasks/DEV-TASK-WSC-102.md
  - planning/tasks/REV-TASK-WSC-102.md
  - planning/approved/PLAN-WSC-2.2.md
reviewDecision: APPROVE
executedAt: 2026-07-31T16:02:10+08:00
status: PASSED
```

## 结论摘要

**PASSED** — 独立 tester-wsc-102 按 testScope 复跑：后端 RbacMatrixTest + SessionAuthIntegrationTest 共 12 测 BUILD SUCCESS；前端 useCanWrite.spec.ts 3 测 + typecheck + build 退出码 0。登出/角色变更后写 API 拒绝由集成测 logout_thenImportWriteApiRejected (401) 与 roleSwitch_oldSessionImportRejected (旧会话拒绝 + 新 USER 403) 覆盖并复跑通过。壳层「非管理员不可见目录维护」以 useCanWrite/canMaintainCatalog 单测为证据；WorkbenchLayout UI E2E 不在本任务 writeSet/testScope 可执行 E2E 范围内 -> SKIPPED（不伪造 PASS）。

- 模块：backend/app/data-chain-service (com.shdata.datachain)
- 审查：REV-TASK-WSC-102 APPROVE, P0/P1=0
- 本机 3306/6379 端口探针仅信息记录，不计入 PASSED；集成测 H2+排除 Redis，无需 live MySQL/Redis
- 未修改业务代码或审查报告；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-002/tester-wsc-102/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| backend-rbac-unit | mvn -f backend/pom.xml -Dtest=RbacMatrixTest,SessionAuthIntegrationTest test | PASSED | Tests run: 6 — 04-mvn-rbac-session.txt, 04-mvn-exit.txt (exitCode=0) |
| backend-session-integration | same run / SessionAuthIntegrationTest | PASSED | Tests run: 6; total 12 / BUILD SUCCESS — 04-mvn-rbac-session.txt, 03-session-test-methods.txt |
| session-denial-proof | logout_thenImportWriteApiRejected + roleSwitch_oldSessionImportRejected | PASSED | logout->401; roleSwitch old reject / new USER 403 — 03-session-test-methods.txt, 03-session-denial-log.txt (MockMvc+H2) |
| frontend-useCanWrite | pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts | PASSED | 3 tests — 05-vitest-useCanWrite.txt, 05-vitest-exit.txt |
| frontend-typecheck | pnpm typecheck | PASSED | exitCode=0 — 05-typecheck.txt |
| frontend-build | pnpm build | PASSED | exitCode=0 — 05-frontend-build.txt |
| shell-catalog-maintenance-visibility | unit canMaintainCatalog + static WorkbenchLayout v-if | PASSED (unit) | 05-vitest-cases.txt, 06-shell-visibility-static.txt |
| shell-ui-e2e | (none in scope) | SKIPPED | no UI E2E in writeSet/testScope — 06-e2e-probe.txt; not faked PASS |
| env-mysql-redis-probe | local port probe (info) | NOT SCORED | 00-ports-probe.txt; not marked PASSED |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | BE unit/integration §4 matrix + session | PASSED (12/12) |
| 2 | FE useCanWrite vitest + typecheck/build | PASSED |
| 3 | logout/role-change write API denial | PASSED (integration re-run) |
| 4 | non-admin hide catalog maintenance | PASSED (unit); UI E2E SKIPPED |

## 证据红线

- env/port probe not marked PASSED
- no self VERIFIED claim (TESTRUN only)
- no business code or REV edits
- no passwords / application-local.yml printed
- actorInstance=tester-wsc-102 (differs from developer-wsc-102 / code-reviewer-wsc-102)
