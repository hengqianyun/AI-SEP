# 代码审查报告 — TASK-WSC-908

```yaml
reviewId: REV-TASK-WSC-908
taskId: TASK-WSC-908
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-908
mustDifferFrom: developer-wsc-908
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 2
reviewedAt: 2026-08-17T15:38:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/skills/code-review/SKILL.md
  - planning/approved/PLAN-WSC-8.3.md（§0.2 路由冻结、§1.2 PO 口径、§5 TASK-WSC-908 writeSet/denyModify/acceptance/testScope、§6.1 九场景）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-908.md
  - 实地：tests/e2e/specs/p0-wsc-v1.6.spec.ts、tests/e2e/playwright.v16.config.ts、tests/e2e/package.json、tests/e2e/helpers/wsc-v16-fixtures.ts
  - denyModify 对照：tests/e2e/playwright.config.ts、tests/e2e/specs/p0-wsc-v1.4.spec.ts（git diff 空）
  - 工作树可见（gitignore）：tests/e2e/reports/p0-wsc-v1.6/**、tests/e2e/reports/p0-wsc-v1.4/junit.xml
riskTags: [e2e-gate, release-evidence]
note: |
  无 auth-model-change / schema-migration：本 REV 不代 securityReviewer / migrationReviewer；不因缺 SEC/MIG 而 REQUEST_CHANGES。
  未代 tester 宣称 test:p0-v16 正式门禁 PASS / VERIFIED。未改被审源码 / state.yaml / events.jsonl。
```

## 结论

**APPROVE** — **P0=0，P1=0**。TASK-WSC-908 交付落在字面 writeSet：新建 V1.6 spec + `playwright.v16.config.ts` + `wsc-v16-fixtures.ts`，`package.json` **仅增补** `test:p0-v16`（命令一字不差），**保留** `test:p0-v14`；未改 v14 spec/config、未新建 v15 spec/config、未伪造 `test:p0-v15`、未改 frontend/backend/contracts/sql。PLAN-WSC-8.3 §6.1 九个强制场景均有非空用例（场景 6 拆 6a/6b）；深链权威路径字面 **`/my-catalog`**；`evidenceId: TESTRUN-WSC-E2E-V16` 在 spec 头注释；config `open: 'never'`；报告落点 `tests/e2e/reports/p0-wsc-v1.6/`。

工作树可见 DEV 自测 HTML/JUnit/`EVIDENCE.md`（10 tests / 0 failures），但路径被根 `.gitignore` 的 `tests/e2e/reports/` 排除，**不得**当作独立 tester 正式门禁 PASS。开放 **P2×2** 不阻塞进入独立 tester。

## Scope 检查（denyModify / writeSet）

本实例对 `tests/e2e/` 的 git status（相对 HEAD）：

| 路径 | 状态 | writeSet |
|---|---|---|
| `tests/e2e/package.json` | `M` | **是**（仅增 `test:p0-v16`） |
| `tests/e2e/specs/p0-wsc-v1.6.spec.ts` | `??` | **是** |
| `tests/e2e/playwright.v16.config.ts` | `??` | **是** |
| `tests/e2e/helpers/wsc-v16-fixtures.ts` | `??` | **是** |
| `tests/e2e/reports/p0-wsc-v1.6/**` | 工作树可见、**gitignore** | writeSet 含该 glob；不入库 |

| 项 | 结果 | 证据 |
|---|---|---|
| `test:p0-v16` 一字不差 | **PASS** | `"playwright test specs/p0-wsc-v1.6.spec.ts --config=playwright.v16.config.ts"` |
| `test:p0-v14` 仍在 | **PASS** | HEAD 原命令未改 |
| 未伪造 `test:p0-v15` | **PASS** | 工作树无 `p0-wsc-v1.5.spec.ts` / `playwright.v15.config.ts`；scripts 无 `test:p0-v15`（PLAN「保留」在 HEAD 本就不存在该 script 时成立；伪造会把「仍可执行」变红） |
| 未改 `playwright.config.ts` | **PASS** | `git diff` 空；仍为 V1.4 `testMatch` + `open: 'never'` |
| 未改 `p0-wsc-v1.4.spec.ts` | **PASS** | `git diff` 空 |
| 未改 `v14-selectors.ts` / `fixtures/auth.ts` | **PASS** | 仅只读 import |
| 未改 `frontend/src/**`、`backend/**`、`contracts/**`、`**/sql/**` | **PASS（908 交付）** | 908 `tests/e2e/` 脏文件仅上表；901–907 工作树脏文件不计入本任务越界 |
| 未新建 v15 spec/config | **PASS** | glob 0 |
| 未改 `state.yaml` / `events.jsonl` | **PASS** | 本实例只写本 REV |
| 未热修 feature 选择器 | **PASS** | 无 frontend/backend 于 908 diff |

`ai/runs/RUN-WSC-011/DEV-TASK-WSC-908.md` 为 developer 交付说明，不在 denyModify。

## `test:p0-v16` / 证据字段

| 计划字段 | 实地 | 结果 |
|---|---|---|
| `evidenceId: TESTRUN-WSC-E2E-V16` | spec 头注释 L36–37；config 注释 L4 | **PASS** |
| 规格 `tests/e2e/specs/p0-wsc-v1.6.spec.ts` | 新建；`testMatch` 仅该文件 | **PASS** |
| 命令 `pnpm --dir tests/e2e run test:p0-v16` | package.json script | **PASS** |
| config `playwright.v16.config.ts` | `html` `open: 'never'`；JUnit `./reports/p0-wsc-v1.6/junit.xml` | **PASS** |
| 报告落点 `tests/e2e/reports/p0-wsc-v1.6/` | config `outputFolder`；工作树有 `index.html` / `junit.xml` / `EVIDENCE.md` | **可见且 gitignore** |
| 路由 `/my-catalog` | `ROUTE_MY_CATALOG = '/my-catalog'`；goto/URL 断言用该常量 | **PASS** |
| `/my-maintenance` 非权威 | 仅 `ROUTE_MY_MAINTENANCE_REDIRECT` 测 redirect → `/my-catalog` | **PASS** |

本 REV **未**重跑 Playwright。工作树 `junit.xml`：`tests="10" failures="0"`。该文件属 **developer 自测**，正式门禁须独立 tester 再执行 `test:p0-v16`。

## §6.1 九场景对照（强制用例非空）

| # | PLAN 要点 | 用例 | 结果 |
|---|---|---|---|
| 1 | 无卡片；高级区常显；无 industryCategory；Cascader `l1CategoryId`/`l2CategoryId`；抽样 `/my-products` | `§6.1-1`：space/industry tags=0、无 toggle、Cascader 可见、`l1CategoryId` 强制、`/my-products` 同源筛栏无座序图 | **有**（L2 见 P2） |
| 2 | 标题右下拉；默认全部=Top5；选 L1 过滤；无企业筛；cross-enterprise 座序图一致 | `§6.1-2`：`seat-map-l1-select` 含「全部」；chipCount≤5；无 `seat-map-enterprise`；跨企 USER 快照相等 | **有** |
| 3 | 不同企业全链列表一致；supplierName/未分类不回退；query 无 `enterpriseName` | `§6.1-3`：`fetchAllCatalogProductCodes` 跨用户相等；`supplierName=演示`；`assertNoForbiddenBrowseQuery` | **有** |
| 4 | ADMIN+PROVIDER 可见；ADMIN 可增改导；同企异 create_by 可见 | `§6.1-4`：双角色 `nav-my-products`；ADMIN 创建/编辑/打开导入；peer ADMIN 产品可搜到 | **有** |
| 5 | 目录维护全量 + `/my-catalog` 本企业；N vs M；用户管理不删 | `§6.1-5`：双入口 + meta.title；`m≤n` 且跨企后再 `n2>m2`；`/admin/users` 可达；无「企业管理」 | **有** |
| 6 | PROVIDER 无维护、深链不可达、本人 scope；USER 菜单+四深链不可达 | `§6.1-6a` + `§6.1-6b` | **有** |
| 7 | Pagination/Cascader/本页全选/统一维护；本企业 vs 全量分离 | `§6.1-7`：pagination、select-all-page、batch-save「统一维护」、scope `myCatalog` vs `full` | **有** |
| 8 | 编辑/导入仍含行业类别；浏览 query 无 industryCategory | `§6.1-8`：editor `industry-category-select` 含「卫生和社会工作」「建筑业」且 ≥20；CSV 含「行业分类」；browse 禁 `industryCategory` | **有** |
| 9 | supplierName；未分类垫底；座序图 hover；`test:p0-v14` 可跑 | `§6.1-9` 抽样三项 + 冻结 v14 命令仍在 | **有**（未分类见 P2） |

场景 8 抽样项与 `frontend/src/api/catalog.ts` `INDUSTRY_CATEGORY_OPTIONS`（含「卫生和社会工作」「建筑业」，length≥20）一致；908 **未**改该源码。

## PO 锁抽查

| 锁 | 结果 | 证据 |
|---|---|---|
| ADMIN 双入口 | **有** | `nav-catalog-maintenance` + `nav-my-catalog`；title「目录维护（全量）」vs「我的目录（本企业/本人）」 |
| PROVIDER 仅我的目录 | **有** | 6a：`nav-my-catalog` 可见；`nav-catalog-maintenance` count=0；深链 `/catalog/maintenance` → `/catalog` |
| 无企业管理页 | **有** | 5：sidebar「企业管理」count=0 |
| 座序图无企业筛 | **有** | 2：`seat-map-enterprise` count=0 |
| 浏览无 industryCategory | **有** | 1/8 + `assertNoForbiddenBrowseQuery` |
| 全链无企业过滤 | **有** | 3：跨企 list 一致；query 无 `enterpriseName` |
| 路由 `/my-catalog` | **有** | 常量 + 5/6 深链；`/my-maintenance` 仅 redirect |

## V1.4 `test:p0-v14` 失败矩阵 — 独立判定

对照冻结 spec `p0-wsc-v1.4.spec.ts` + 工作树 `reports/p0-wsc-v1.4/junit.xml`（`tests=15 failures=9`，DEV 自测、gitignore）。**禁止改 v14 spec**。本 REV 判定：9 条失败均为相对 SNAP-008 / PLAN-WSC-8.3 的 **intentional V1.6 delta**，**不是** 908 引入的回归 blocker（908 未改业务源码与 v14 制品）。

| 冻结用例 | 冻结断言 | 独立观察（junit） | 判定 | 对照 |
|---|---|---|---|---|
| `2a) ADMIN 用户管理 CRUD 抽样` | soft-delete 后表不含 `e2e_u_*` | L150 `not.toContainText` 失败；行仍「已启用」 | **intentional / 预存 UX**（点击 `user-soft-delete` 未确认弹框；非 908 改用户管理） | 非本任务 writeSet |
| `2b) provider/user 不可达用户管理` | 深链仍挂 `users-admin-page` + `users-forbidden` | `users-admin-page` 未找到 | **intentional V1.6 delta** | REQ-SHELL-009 深链结构不可达（906 `beforeEnter` 重定向 `/catalog`）；v16 `§6.1-6b` 覆盖 |
| `3) 公共目录无增改导` ×3 | heading `数据目录` visible（`expectPublicCatalogNoWrite`） | heading 未找到 | **intentional V1.6 delta** | REQ-CAT-014 无卡片/无标题卡；v16 `§6.1-1` 显式 count=0 |
| `4) PROVIDER 我的产品…` | `nav-catalog-maintenance` visible（TASK-WSC-607） | 元素不存在 | **intentional V1.6 delta** | REQ-SHELL-009 PROVIDER 无目录维护；v16 `§6.1-6a` |
| `4x) ADMIN/USER 无「我的产品」` | ADMIN `nav-my-products` count=0 | Received: 1 | **intentional V1.6 delta** | REQ-CAT-016 ADMIN 可见我的数据产品；v16 `§6.1-4` |
| `5) ADMIN 产品写 403` | POST status 403 | Received: 200 | **intentional V1.6 delta** | REQ-CAT-016 / §3.1 ADMIN 可写；v16 `§6.1-4` |
| `6) 座序图 hover` | chip hover opacity 0.45 / 恢复 1 | **无 failure 节点** | **PASS** | 场景 9 抽样；v16 `§6.1-9` 同源 hover |
| `7a` / `7b` | 导入四态 + OpenAPI | **无 failure 节点** | **PASS** | 非 V1.6 冲掉 |

场景 9 抽样三项在 v16 spec **均有覆盖**：`supplierName` 请求参数、未分类分节位置（条件断言）、hover opacity。v14 命令仍可启动（exit 1 来自冻结断言 vs V1.6 产品语义，不是 908 把 v14 跑挂）。

## 测试覆盖与门禁边界

| 项 | 本 REV |
|---|---|
| §6.1 强制用例存在且非空 | **通过**（规格审查） |
| DEV 自测 `test:p0-v16` exit 0 | **见过**工作树 junit/console；**不等于**正式 tester PASS |
| 正式 `TESTRUN-WSC-E2E-V16` 门禁 | **未宣称**；须独立 tester 再跑 |
| 本实例重跑 E2E | **否** |
| 标 VERIFIED | **禁止且未做** |

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-908-001 | P2 | OPEN | 根 `.gitignore` L36 `tests/e2e/reports/` 排除证据包；`EVIDENCE.md` / HTML / JUnit 仅工作树可见。DEV 自测 10/10 **不是**独立 tester 门禁 | 独立 tester 执行 `pnpm --dir tests/e2e run test:p0-v16` 并落正式 `TESTRUN-*`；不要把 gitignore 自测当 VERIFIED | REQ-CAT-014..019、发布证据 |
| FIND-WSC-908-002 | P2 | OPEN | ① `§6.1-1` 调用 `trySelectFirstBrowseL2` 但不断言返回值，spec 无强制 `l2CategoryId`（helper 在无第二列时 no-op）。② `§6.1-9` / `§6.1-3` 未分类垫底仅在 `titles.includes('未分类数据')` 时断言。③ `§6.1-5` 创建跨企产品 `xCode` 后只比 N>M，未在全量/我的目录列表中定位该编码。场景本身非空，不升 P1 | 若 tester 发现 L2/未分类/跨企产品定位空转，再补强制断言；本轮不阻塞 | REQ-CAT-014、REQ-CAT-015、REQ-CAT-017 |

## 架构与可维护性备注（非阻塞）

- fixtures 只读复用 `v14-selectors` / `fixtures/auth`，避免复制登录与创建弹框，且未改 denyModify 文件。
- 跨企用户经用户管理 UI 绑定「未归属默认企业」并清空 `enterpriseId`，符合「不改 backend seed」。
- `outputDir: ./reports/test-results-v1.6` 与 HTML 报告目录分离，不污染 v14 `test-results-v1.4`。
- 工作树另有 gitignore 的 `reports/p0-wsc-v1.5/**` 残留，**无**对应 spec/script；不视为 908 伪造 v15。

## 决策依据

- P0=0、P1=0 → **APPROVE**，可进入独立 **tester** 执行 `test:p0-v16`（强制）与 `test:p0-v14`（强制回归，允许按上表失败）。
- 本 REV **不**宣称测试门禁通过，**不**标 VERIFIED。
- 无 SEC/MIG 触发；**不**因缺安全/迁移审查而 REQUEST_CHANGES。
- P2 不要求 908 返工改业务源码或改 v14 spec。
