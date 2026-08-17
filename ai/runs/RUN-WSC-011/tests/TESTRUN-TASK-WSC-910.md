# 测试证据 — TASK-WSC-910

```yaml
testrunId: TESTRUN-TASK-WSC-910
taskId: TASK-WSC-910
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
wave: HOTFIX
kind: HOTFIX
actorInstance: tester-wsc-910
mustDifferFrom:
  - developer-wsc-910
  - developer-wsc-910-r1
  - code-reviewer-wsc-910
  - code-reviewer-wsc-910-r2
basedOn:
  - ai/runs/RUN-WSC-011/HOTFIX-TASK-WSC-910.md（验收权威）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-910.md（只读对照，不采信为 PASS）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-910.md（R1 REQUEST_CHANGES；FIND-WSC-910-001）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-910-R2.md（R2 APPROVE；P0=0、P1=0；FIND-WSC-910-001 CLOSED；FIND-WSC-910-002 P2 OPEN）
  - ai/agents/tester.md
  - ai/skills/tester/SKILL.md
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-17T17:19:57+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-CAT-007, REQ-CAT-013, REQ-CAT-017]
riskTags: [hotfix, blocksRelease, pagination-size]
note: |
  独立 tester-wsc-910 实跑 HOTFIX 前端门禁。未改源码、审查正文、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  本任务无 auth-model-change，未做 securityReview，YAML 不写 securityReviewDecision。
  FIND-WSC-910-002（P2，OPEN）不记 910 FAIL。
  未改后端/契约，未跑 mvn。未跑 Playwright / tests/e2e/**（908 独占）；未把未跑 E2E 标为 PASS 替代。
  verbose txt 控制台编码导致部分中文乱码；用例标题与 status 以 vitest-910.json 为准。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-910` 依 `HOTFIX-TASK-WSC-910.md` 验收与本轮门禁清单实际执行：目录维护 composable + UX **2** 套件 **16** 测全部通过（exitCode=0）。具名用例 `910-page-size-changer`（Ux + composable，含档位 / page=1 / 清选择 / 第 1 页改 size 仍重拉）、`910-page-size-changer: antdv dual emit from page>1 keeps page=1`（同栈连打，最终 `{ page: 1, pageSize: 20 }`）、`907-my-catalog-scope-split`、`FIND-WSC-907-001` 均已实跑并通过。

审查前提：R1 `REV-TASK-WSC-910.md` REQUEST_CHANGES；R2 `REV-TASK-WSC-910-R2.md` **APPROVE（P0=0、P1=0）**；`FIND-WSC-910-001` CLOSED。本报告不代标 VERIFIED。未采信 DEV 自测为 PASS。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | 结果 | 证据 |
|---|---|---|---|---:|---|---|
| 1 | frontend-unit（维护分页 pageSize + 907 回归） | `pnpm --dir frontend exec vitest run src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts src/features/catalog/maintenance/components/CatalogMaintenanceUx.spec.ts --reporter=verbose --reporter=json --outputFile=C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-011\tests\vitest-910.json` | repo root | **0** | **PASSED** — Test Files **2** passed；Tests **16** passed（0 failed）；JSON `success=true` `numPassedTests=16` | `vitest-910.txt`；`vitest-910.json` |

**failedCommandCount = 0**（仅计 #1 门禁命令）。无环境阻塞；未把未执行项记为 PASS。

环境：Vitest v2.1.9；Windows / PowerShell；仓库根 `C:\WorkSpace\AI-SEP`。

未跑：后端 `mvn`（本 hotfix 未改后端/契约）；Playwright / `tests/e2e/**`（908 独占；hotfix denyModify 默认不改 v14/v16 spec）。未把未跑 E2E 标为 PASS 替代。

## 前端分套件（门禁 #1）

| 文件 | tests | failures | 结果 |
|---|---:|---:|---|
| `CatalogMaintenanceUx.spec.ts` | 4 | 0 | PASS |
| `useCatalogMaintenance.spec.ts` | 12 | 0 | PASS |
| **合计** | **16** | **0** | **PASS** |

JSON：`numTotalTests=16`、`numPassedTests=16`、`numFailedTests=0`、`success=true`。Start at 17:19:57；Duration 1.27s。

### 命名用例（门禁必须见到）

| title | 文件 | 结果 |
|---|---|---|
| **910-page-size-changer**: Pagination shows size changer 10/20/50/100; change and showSizeChange share applyPagination | `CatalogMaintenanceUx.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.26ms） |
| **910-page-size-changer**: setPageSize 20/50/100 queries page=1 and clears selection | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈1.36ms） |
| **910-page-size-changer**: applyPagination size change on page 1 still reloads; page-only keeps size | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.54ms） |
| **910-page-size-changer**: clampPageSize snaps to 10/20/50/100 | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈1.32ms） |
| **910-page-size-changer: antdv dual emit from page>1 keeps page=1** | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.72ms） |
| **907-my-catalog-scope-split**: dual entry routes keep 906 meta titles and wire maintenance page | `CatalogMaintenanceUx.spec.ts` | **PASS**（JSON `status=passed`；duration≈2.44ms） |
| **907-my-catalog-scope-split**: full vs myCatalog list query | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈1.62ms） |
| **907-my-catalog-scope-split**: same instance full → myCatalog list and write URLs | `useCatalogMaintenance.spec.ts` | **PASS**（JSON `status=passed`；duration≈63.64ms） |
| **FIND-WSC-907-001**: page passes reactive scope and re-runs guard on props.scope | `CatalogMaintenanceUx.spec.ts` | **PASS**（JSON `status=passed`；duration≈0.49ms） |

`FIND-WSC-907-001` 仍存在于 `CatalogMaintenanceUx.spec.ts`，本轮实跑绿。907 命名用例未回退。

## 门禁 #1 全量明细（JSON）

### CatalogMaintenanceUx.spec.ts

| title | 结果 |
|---|---|
| `907-my-catalog-scope-split: dual entry routes keep 906 meta titles and wire maintenance page` | **PASS** |
| `FIND-WSC-907-001: page passes reactive scope and re-runs guard on props.scope` | **PASS** |
| `Pagination / Cascader / 本页全选 / 统一维护；筛 Tab 清选择；无跨页全选` | **PASS** |
| `910-page-size-changer: Pagination shows size changer 10/20/50/100; change and showSizeChange share applyPagination` | **PASS** |

### useCatalogMaintenance.spec.ts

| title | 结果 |
|---|---|
| `loads list with page/pageSize/total and status tabs` | **PASS** |
| `907-my-catalog-scope-split: full vs myCatalog list query` | **PASS** |
| `907-my-catalog-scope-split: same instance full → myCatalog list and write URLs` | **PASS** |
| `Cascader path: L1 only / L1+L2 / L3 / clear` | **PASS** |
| `本页全选 / 取消；筛/Tab/翻页清选择` | **PASS** |
| `910-page-size-changer: setPageSize 20/50/100 queries page=1 and clears selection` | **PASS** |
| `910-page-size-changer: antdv dual emit from page>1 keeps page=1` | **PASS** |
| `910-page-size-changer: applyPagination size change on page 1 still reloads; page-only keeps size` | **PASS** |
| `910-page-size-changer: clampPageSize snaps to 10/20/50/100` | **PASS** |
| `single save associates l3 then reloads with scope` | **PASS** |
| `统一维护 uses selected ids and batch l3 with scope` | **PASS** |
| `surfaces ApiError on save failure` | **PASS** |

## acceptance 对照（执行层）

| HOTFIX acceptance | 结果 | 本轮证据 |
|---|---|---|
| `/catalog/maintenance` 与 `/my-catalog`：分页条可见 pageSize 选择 | **PASS** | UX `910-page-size-changer`（`show-size-changer` + 档位绑定） |
| 可选档 10 / 20 / 50 / 100（≤ 服务端 100）；默认前端 10 | **PASS** | UX 档位断言 + composable `clampPageSize` + `setPageSize` 20/50/100 |
| 变更 pageSize：请求带新 `pageSize`、回到第 1 页、清本页勾选 | **PASS** | `setPageSize` 20/50/100（page=1、清选择）；第 1 页改 size 仍重拉；**antdv dual emit** 同栈连打后唯一 `pageSize=20` 请求为 `{ page: 1, pageSize: 20 }` |
| 仅翻页、不改 pageSize 时与现网一致（含 quick jumper） | **PASS** | `applyPagination` page-only keeps size；UX 回归 Pagination / 筛 Tab / 无跨页全选 |
| 自动化：UI 含 size changer；选非默认档后 query 含对应 `pageSize` 且 `page=1` | **PASS** | 上表全部 `910-page-size-changer*` 具名用例实跑绿 |
| 907 同实例 scope / 双入口 / FIND-WSC-907-001 不得回退 | **PASS** | `907-my-catalog-scope-split`（Ux + composable）+ `FIND-WSC-907-001` |

## REQ 追踪

| REQ | 验证要点 | 结果 |
|---|---|---|
| REQ-CAT-007 | 目录维护分页：改 pageSize 后重拉第 1 页、请求带新 `pageSize`、清勾选 | **PASS**（`setPageSize` + dual-emit + 第 1 页改 size 仍重拉） |
| REQ-CAT-013 | 目录维护 UX：size changer 可见；907 双入口与维护页接线未回退 | **PASS**（UX `910-page-size-changer` + `907-my-catalog-scope-split`） |
| REQ-CAT-017 | 我的目录与全量维护 scope 拆分；FIND-WSC-907-001 getter | **PASS**（`907-my-catalog-scope-split` 同实例切换 + `FIND-WSC-907-001`） |

## 已知非门禁（不计入 failedCommandCount）

| 来源 | 说明 |
|---|---|
| FIND-WSC-910-002（P2，OPEN） | R2 记：DEV 自测不是独立 tester 门禁；UX spec 仍只读 `.vue` 字符串，未挂载 Pagination。本轮已独立实跑门禁具名用例（含 dual-emit），**不**因此记 910 FAIL、**不**开 `BUG-*`。不要求本轮把 UX spec 改成组件挂载。 |
| 未跑 mvn | 本 hotfix 未改后端/契约；不必跑。不得标 PASS 替代。 |
| 未跑 E2E | 908 独占；hotfix denyModify 默认不改 v14/v16 spec。不得标 PASS 替代。 |
| `vitest-910.txt` 中文乱码 | 控制台编码；用例标题与 status 以 `vitest-910.json` 为准。 |

本 tester **不**因开放 P2 另开 `BUG-*`。

## 缺陷

无新开 `BUG-*`。

## 审查前提

| 项 | 值 |
|---|---|
| codeReview R1 | `REV-TASK-WSC-910` **REQUEST_CHANGES**；P0=0、P1=1（FIND-WSC-910-001） |
| codeReview R2 | `REV-TASK-WSC-910-R2` **APPROVE**；P0=0、P1=0；FIND-WSC-910-001 **CLOSED** |
| 开放 P2（不阻塞） | FIND-WSC-910-002 |
| securityReview | **不适用**（无 `auth-model-change`；YAML 不写 `securityReviewDecision`；不因缺 SEC-REV 记 BLOCKED） |

## 证据红线（自检）

- [x] 实际执行门禁命令（命令 + 真实 exitCode），非仅读 DEV/REV
- [x] 具名用例有实测结果行（vitest-910.json `status=passed`）
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未因 FIND-WSC-910-002（P2）记 910 FAIL
- [x] 未把未跑的 E2E / mvn 当作 910 门禁 PASS
- [x] 未改源码 / 审查正文 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-910`，与 developer-wsc-910 / developer-wsc-910-r1 / code-reviewer-wsc-910 / code-reviewer-wsc-910-r2 隔离
- [x] 未自行标任务 VERIFIED
- [x] YAML 无 `securityReviewDecision`
- [x] 门禁未失败，故未开 BUG-*

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-910.md`
- 门禁 #1 verbose：`ai/runs/RUN-WSC-011/tests/vitest-910.txt`
- 门禁 #1 JSON：`ai/runs/RUN-WSC-011/tests/vitest-910.json`
- hotfix / 开发 / 审查：`HOTFIX-TASK-WSC-910.md`、`DEV-TASK-WSC-910.md`、`reviews/REV-TASK-WSC-910.md`、`reviews/REV-TASK-WSC-910-R2.md`
