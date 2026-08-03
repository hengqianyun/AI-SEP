# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-401-1
taskId: TASK-WSC-401
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-006
actorInstance: tester-wsc-401
basedOn:
  - planning/tasks/TASK-WSC-401.md
  - planning/tasks/DEV-TASK-WSC-401.md
  - planning/tasks/REV-TASK-WSC-401.md
  - ai/agents/tester.md
reviewDecision: APPROVE
reviewRound: R1
reviewP0P1: 0/0
executedAt: 2026-08-03T10:30:00+08:00
status: PASSED
exitCode: 0
mustDifferFrom:
  - developer-wsc-401
  - code-reviewer-wsc-401
```

## 结论摘要

**PASSED** — 独立 `tester-wsc-401` 在 REV-TASK-WSC-401 **R1 APPROVE**（P0/P1=0）后实跑：前端 Vitest **21/21 PASSED** + `vue-tsc --noEmit` **EXIT=0**；后端 `listProducts_sortedByUpdatedAtDesc` **1/1 PASSED**；抽样门户 `/` `/workspace` `/docs` public、`打开接入端`→`/login`、create 自动编码、`updatedAt` DESC 均通过。抽样前发现本地 Vite/后端 JVM 陈旧，已重启后复验（非业务代码变更）。未自称任务 VERIFIED；未改业务源码 / REV / DEV / `state.yaml` / `events.jsonl`。

- evidenceId：`EVID-TASK-WSC-401-1`
- 审查前提：REV-TASK-WSC-401 **R1 APPROVE**

命令日志：`temp/vitest-wsc401.out`；`temp/typecheck-wsc401.out`；`temp/mvn-sort-wsc401.out`；`temp/sample-public-wsc401.out`；`temp/sample-sort-wsc401-after-restart.out`；`temp/vite-diagnose-wsc401.out`；`temp/backend-restart-wsc401.out`

## Layers

| name | command | result | notes |
| --- | --- | --- | --- |
| frontend-vitest-task-401 | `pnpm exec vitest run src/features/portal/portal.spec.ts src/router/router.portal.spec.ts src/features/catalog/editor/utils/productCode.spec.ts src/features/catalog/editor/composables/useProductEditor.spec.ts src/features/overview/OverviewPage.spec.ts --reporter=default`（cwd: `frontend`） | PASSED | Test Files **5**；Tests **21**；EXIT=**0** |
| frontend-typecheck | `pnpm exec vue-tsc --noEmit -p tsconfig.json --pretty false`（cwd: `frontend`） | PASSED | EXIT=**0**；无 diagnostics |
| backend-sort-integration | `mvn -pl app/data-chain-service -am test -Dtest=CatalogBrowseIntegrationTest#listProducts_sortedByUpdatedAtDesc`（cwd: `backend`） | PASSED | Tests run:**1**；Failures:**0**；BUILD SUCCESS；EXIT=**0** |
| sample-portal-public | Browser + HTTP：`/` `/workspace` `/docs` | PASSED | 均为 public 静态门户（mock）；未进 Workbench |
| sample-open-console-login | `/workspace`「打开接入端」→ `/login`；admin/demo 登录 → `/overview` | PASSED | DOM `data-testid=open-console-link` **href=/login**；登录页可提交 |
| sample-product-code | `/catalog/products/new` 自动编码 | PASSED | 初始 `DATA-SET-*` +「自动生成」只读；切 API → `API-SVC-*` |
| sample-updatedAt-sort | `GET /api/v1/catalog/products?...&l3CategoryId=cat-l3-emr-desense` | PASSED | 重启后端后首条 **MED-PG-0018**；相邻 `updatedAt` DESC 成立 |

### Vitest 原始输出（摘要）

```text
RUN  v2.1.9 C:/WorkSpace/AI-SEP/frontend

 ✓ src/features/portal/portal.spec.ts (3 tests)
 ✓ src/router/router.portal.spec.ts (1 test)
 ✓ src/features/overview/OverviewPage.spec.ts (1 test)
 ✓ src/features/catalog/editor/utils/productCode.spec.ts (2 tests)
 ✓ src/features/catalog/editor/composables/useProductEditor.spec.ts (14 tests)

 Test Files  5 passed (5)
      Tests  21 passed (21)
EXIT:0
```

### 后端排序集成测（摘要）

```text
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
EXIT=0
# CatalogBrowseIntegrationTest#listProducts_sortedByUpdatedAtDesc
```

## Vitest 覆盖摘要

| 套件 | 用例数 | 结果 |
|---|---|---|
| `portal.spec.ts` | 3 | PASSED（home/workspace→login/docs smoke） |
| `router.portal.spec.ts` | 1 | PASSED（public meta；根不 redirect overview） |
| `productCode.spec.ts` | 2 | PASSED（DEC-WSC-001 映射 + 正则） |
| `useProductEditor.spec.ts` | 14 | PASSED（含 create 自动编码 / edit 保持） |
| `OverviewPage.spec.ts` | 1 | PASSED（`width:100%`；无 `max-width:1200px`） |
| **合计** | **21** | **21 PASSED** |

## 抽样核对

| # | 检查项 | 结果 | 证据 |
|---|---|---|---|
| 1 | `/` 静态门户 public | PASSED | Browser：标题「以可信链接促进数据要素高效流通」；PortalLayout；URL 保持 `/` |
| 2 | `/workspace` 静态简介 public | PASSED | Browser：`portal-workspace` 文案；「打开接入端」可见 |
| 3 | `/docs` 静态文档简介 public | PASSED | Browser：「标准接入文档中心」；URL `/docs` |
| 4 | 「打开接入端」→ `/login` | PASSED | DOM `href=/login`（`open-console-link`）；未链 `/workspace/console` |
| 5 | 登录 | PASSED | `/login` admin/demo → `/overview`（Workbench） |
| 6 | create 自动编码 | PASSED | `/catalog/products/new`：`DATA-SET-298405` 只读+「自动生成」；类型→API 后 DOM=`API-SVC-401476` |
| 7 | 列表 `updatedAt` DESC | PASSED | 重启后 API：首条 `MED-PG-0018` @ `2026-06-02T08:00:00Z` … `MED-PG-0009` @ `2026-06-01T23:00:00Z`；`DESC_ok=True` |

### 环境注记（不记为代码 FAIL）

- 初次 live API 抽样命中 **陈旧** Spring Boot（PID 5440，2026-08-02 启动），响应无 `updatedAt` → 已安全重启 `local` profile 后复验 **PASS**。
- 初次 Vite 抽样命中 **陈旧** Vite（未加载当前 `@` / plugin-vue）→ 已重启 `pnpm exec vite --port 5173 --host 127.0.0.1` 后复验 **PASS**。
- 未将陈旧进程环境阻塞记作代码缺陷；集成测与重启后抽样均通过。

## acceptance 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | `/` `/workspace` `/docs` 为静态门户页（迁移 mock） | PASSED |
| 2 | 「打开接入端」→ `/login` | PASSED |
| 3 | 新增产品编码自动生成（DEC-WSC-001） | PASSED（Vitest + UI 抽样） |
| 4 | 宽屏无大片空挡（overview 流体宽度） | PASSED（OverviewPage.spec：无 max-width 1200px） |
| 5 | 产品列表按修改时间新→旧 | PASSED（集成测 + live API） |
| 6 | Vitest / 后端测覆盖排序或编码生成 | PASSED（21 + 1） |
| 7 | 产出 `TESTRUN-TASK-WSC-401.md`（EVID-TASK-WSC-401-1） | **PASSED**（本文件；勿自称 VERIFIED） |

## REQ 追踪

| REQ / DEC | 证据层 | 结果 |
|---|---|---|
| REQ-SHELL-001 | portal/router Vitest + 门户抽样 | PASSED |
| REQ-CAT-001 / DEC-WSC-001 | productCode + useProductEditor Vitest + create UI | PASSED |
| REQ-CAT-005 | Maven sort 集成测 + live DESC | PASSED |
| REQ-UX-004 | OverviewPage.spec 流体宽度 | PASSED |

## 证据红线

- 未将环境阻塞记为 PASS（陈旧进程已重启后复验）
- 未未执行却声称覆盖
- 未自行宣称 VERIFIED
- 未改业务源码、REV、DEV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-401`（≠ developer-wsc-401 / ≠ code-reviewer-wsc-401）
- 无本轮新开阻塞缺陷
