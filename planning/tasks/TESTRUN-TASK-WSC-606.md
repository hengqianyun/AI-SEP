# 测试证据

```yaml
evidenceId: TESTRUN-TASK-WSC-606
releaseEvidenceId: TESTRUN-WSC-E2E-V14
taskId: TASK-WSC-606
planId: PLAN-WSC-5.2
actorInstance: tester-wsc-606-r1
retest: false
kind: HOTFIX
contracts: wsc-contracts@2.2.0（additive industryCategory query；未 bump info.version）
basedOn:
  - ai/runs/RUN-WSC-008/HOTFIX-TASK-WSC-606.md
  - planning/tasks/REV-TASK-WSC-606.md（round 2 APPROVE；P0=0 P1=0）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-606.md（Round 2）
  - planning/tasks/TESTRUN-TASK-WSC-605.md（格式参考）
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-606.md
reviewRound: 2
reviewP0: 0
reviewP1: 0
specFile: tests/e2e/specs/p0-wsc-v1.4.spec.ts
reportDir: tests/e2e/reports/p0-wsc-v1.4/
executedAt: 2026-08-06T16:40:00+08:00
env:
  E2E_RUN: "1"
  E2E_BASE_URL: "http://127.0.0.1:5173"
  backend: "http://127.0.0.1:8080 health UP"
result: PASS
failedCommandCount: 1
bugsClosed: []
bugsOpen:
  - BUG-WSC-606-TESTRUN-001
mustDifferFrom:
  - developer-wsc-606
  - developer-wsc-606-r2
  - code-reviewer-wsc-606-r1
  - code-reviewer-wsc-606-r2
```

## 结论摘要

**PASS** — 独立 `tester-wsc-606-r1` 在 DEV Round 2 + REV Round 2 **APPROVE**（P0=0、P1=0）后，按 HOTFIX acceptance **真实执行** 测试：

1. Frontend Vitest（UsersAdmin + useCatalogBrowse + CatalogBrowsePage）→ **exitCode=0**；**33 passed / 0 failed**。
2. Backend：建议全类 `CatalogBrowseIntegrationTest` → **exitCode=1**（8/13 FAIL，缺产品种子）→ 记 **FAIL** 并开 **BUG-WSC-606-TESTRUN-001（P2，不阻塞本门禁）**；scoped `listProducts_filterByIndustryCategory_gbtMenlei` → **exitCode=0**（覆盖 acceptance #4）。
3. E2E `pnpm run test:p0-v14` → **exitCode=0**；**15 passed / 0 failed**；发布证据包 **`TESTRUN-WSC-E2E-V14`** 落于 `tests/e2e/reports/p0-wsc-v1.4/`。

- 审查前提：REV-TASK-WSC-606 Round 2 **APPROVE**，P0=0，P1=0（开放仅 P2/P3，不阻塞）
- developer 自测 **不算** 本门禁；本轮为独立 tester 重跑
- **未**将环境阻塞记为 PASS；命令真实执行并记录 exitCode
- **未**修改业务实现源码（frontend/backend/contracts）、`state.yaml`、`events.jsonl`；**未**自行宣称 VERIFIED
- E2E：**PASS**

证据目录：`ai/runs/RUN-WSC-008/tester-wsc-606-r1/`

## commands

| # | command | cwd | env | exitCode | result | evidence |
|---|---|---|---|---|---|---|
| 1 | `pnpm exec vitest run src/features/users/UsersAdminPage.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts src/features/catalog/browse/CatalogBrowsePage.spec.ts` | `frontend` | — | **0** | **PASS** | `01-vitest.txt` / `01-vitest-exit.txt` — **33 passed** |
| 2a | `mvn -pl app/data-chain-service -am "-Dtest=CatalogBrowseIntegrationTest" test` | `backend` | — | **1** | **FAIL** | `02-backend.txt` / `02-backend-exit.txt` — 13 run / **8 Failures**（缺产品种子）→ `BUG-WSC-606-TESTRUN-001` |
| 2b | `mvn -pl app/data-chain-service -am "-Dtest=CatalogBrowseIntegrationTest#listProducts_filterByIndustryCategory_gbtMenlei" test` | `backend` | — | **0** | **PASS** | `02b-backend-industryCategory.txt` / `02b-backend-industryCategory-exit.txt` — **1/0**（acceptance #4） |
| 3 | `pnpm run test:p0-v14`（≡ playwright `p0-wsc-v1.4.spec.ts`） | `tests/e2e` | `E2E_RUN=1`；`E2E_BASE_URL=http://127.0.0.1:5173` | **0** | **PASS** | `03-playwright-p0-v14.txt` / `03-playwright-p0-v14-exit.txt` — **15 passed**；JUnit 15/0 |
| 4 | acceptance 勾选对账 | n/a | n/a | n/a | **PASS** | `04-acceptance.md` |
| 5 | §6.1 七场景勾选 | n/a（bind #3） | n/a | n/a | **PASS** | `05-section4-scenarios.md` |
| 6 | JUnit / 报告目录核对 | n/a | n/a | n/a | **PASS** | `06-junit-summary.md`；`reports/p0-wsc-v1.4/{index.html,junit.xml,EVIDENCE.md}` |

**failedCommandCount = 1**（仅 #2a 全类集成测；#2b scoped industryCategory 已覆盖 HOTFIX acceptance #4；E2E live 目录绿）。

## HOTFIX acceptance 勾选

| # | 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | 数据目录可滚动；触底分页合理 | **PASS** | Vitest CatalogBrowsePage；E2E 场景 3/6 |
| 2a–2d | 用户管理弹框创建 / 编辑角色 / wsc-surface / 去副标题 | **PASS** | E2E `2a`；Vitest UsersAdmin |
| 3 | 目录维护去除「← 返回目录」 | **PASS** | 源码只读对照（DEV 交付；本轮未改实现） |
| 4 | 行业分类 GB/T + `industryCategory` 筛选 | **PASS** | Vitest useCatalogBrowse；#2b 集成测 |

## §6.1 七场景勾选（E2E）

| # | 场景 | 结果 |
|---|---|---|
| 1–7 | 见 `05-section4-scenarios.md` | **全部 PASS（15/0）** |

## 发布证据包引用

| 字段 | 值 |
|---|---|
| 发布 `evidenceId` | **TESTRUN-WSC-E2E-V14** |
| 任务级 `evidenceId` | **TESTRUN-TASK-WSC-606** |
| 报告目录 | `tests/e2e/reports/p0-wsc-v1.4/` |
| HTML | `tests/e2e/reports/p0-wsc-v1.4/index.html` |
| JUnit | `tests/e2e/reports/p0-wsc-v1.4/junit.xml`（15/0） |
| EVIDENCE | `tests/e2e/reports/p0-wsc-v1.4/EVIDENCE.md` |

## BUG

| id | severity | status | summary |
|---|---|---|---|
| BUG-WSC-606-TESTRUN-001 | P2 | **OPEN** | `CatalogBrowseIntegrationTest` 全类 8 失败：H2 create-drop + flyway=false 无产品种子；不阻塞 606（industryCategory 单测 + E2E 已绿） |

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未伪造 vitest / maven / playwright exit；命令如实记录
- 未修改 frontend/backend/contracts 业务实现源码
- 未修改 REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-606-r1`（≠ developer-wsc-606* / ≠ code-reviewer-wsc-606*）
- 未自行宣称 VERIFIED
- developer 自测仅作对照；本门禁以本轮重跑为准

## 计数

| 指标 | 值 |
|---|---|
| result | **PASS** |
| failedCommandCount | **1** |
| E2E | **PASS**（15/0；TESTRUN-WSC-E2E-V14） |
| Vitest | 33 passed / 0 failed |
| Backend scoped industryCategory | 1 passed / 0 failed |
| Backend full CatalogBrowseIntegrationTest | 13 run / 8 failed（BUG OPEN P2） |
| bugsOpen | BUG-WSC-606-TESTRUN-001（P2，不阻塞） |
| P0 缺陷 | **0** |
