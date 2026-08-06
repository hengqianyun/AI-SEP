# 测试证据

```yaml
evidenceId: TESTRUN-TASK-WSC-603
taskId: TASK-WSC-603
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
actorInstance: tester-wsc-603-r2
retest: true
contracts: wsc-contracts@2.2.0（只读对照）
basedOn:
  - planning/approved/PLAN-WSC-5.2.md (§4 / TASK-WSC-603 acceptance + testScope)
  - planning/tasks/TESTRUN-TASK-WSC-603.md（round 1 FAIL；BUG-WSC-603-TESTRUN-001）
  - planning/tasks/REV-TASK-WSC-603.md（round 3 APPROVE；P0=0 P1=0）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-603.md（Round 3 BUG 修复）
reviewDecision: APPROVE
reviewRef: planning/tasks/REV-TASK-WSC-603.md
reviewRound: 3
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-06T10:48:30+08:00
result: PASS
failedCommandCount: 0
bugsClosed:
  - BUG-WSC-603-TESTRUN-001
bugsOpen: []
```

## 结论摘要

**PASS**（重测）— 独立 `tester-wsc-603-r2` 在 developer-wsc-603-r3 修复 + code-reviewer-wsc-603-r3 APPROVE 后，按 PLAN-WSC-5.2 TASK-WSC-603 `testScope` 实跑：

1. 后端：`ProductEditorIntegrationTest` + `CreateByOwnershipIntegrationTest` + `ProductImportIntegrationTest` + `RbacMatrixTest` → **exitCode=0**（Tests run: **27**, Failures: **0**）。其中 **ProductEditor 7/0**（上一轮 4 Failures 已消失）。
2. Vitest：browse / import-entry / useCanWrite / useProductImport → **exitCode=0**（5 files / 53 tests）。
3. §4 单元格表 → 命名方法定位齐全（`03-section4-cells.md`）。
4. 预落地对账 603 行 P0 → 命名路径齐全（`04-preland-reconcile-paths.md`）。
5. **BUG-WSC-603-TESTRUN-001：CLOSED** — OpenAPI 编辑不回退 acceptance 对应套件全绿。

- 审查前提：REV-TASK-WSC-603 Round 3 **APPROVE**，P0=0，P1=0
- **未**将环境阻塞记为 PASS；命令均真实执行并记录 exitCode
- 未修改业务实现源码、`state.yaml`、`events.jsonl`；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-008/tester-wsc-603-r2/`

## commands

| # | command | cwd | exitCode | result | evidence |
|---|---|---|---|---|---|
| 1 | `mvn -pl app/data-chain-service -am "-Dtest=ProductEditorIntegrationTest,CreateByOwnershipIntegrationTest,ProductImportIntegrationTest,RbacMatrixTest" test` | `backend` | **0** | **PASS** | `01-backend.txt` / `01-backend-exit.txt` — CreateBy 3 + ProductEditor **7** + ProductImport 10 + RbacMatrix 7 = **27**，Failures: 0；BUILD SUCCESS |
| 2 | `pnpm exec vitest run src/features/catalog/browse/composables/useCatalogBrowse.spec.ts src/features/catalog/browse/CatalogBrowsePage.spec.ts src/features/catalog/browse/composables/useCatalogImportEntry.spec.ts src/features/auth/composables/useCanWrite.spec.ts src/features/catalog/import/composables/useProductImport.spec.ts` | `frontend` | **0** | **PASS** | `02-vitest.txt` / `02-vitest-exit.txt` — 5 files / 53 tests passed |
| 3 | §4 单元格表证据定位 | n/a（read + bind #1/#2） | n/a | **PASS** | `03-section4-cells.md` |
| 4 | 预落地对账 603 行 P0 路径 | n/a | n/a | **PASS** | `04-preland-reconcile-paths.md` |
| 5 | BUG 关闭确认 | n/a（bind #1 ProductEditor） | n/a | **CLOSED** | `05-bug-status.md` |

**failedCommandCount = 0**

## testScope 对照 checklist

| # | testScope 项 | 结果 | 证据 |
|---|---|---|---|
| 1a | `mine=true` 过滤仅本人 | **PASS** | `CreateByOwnershipIntegrationTest#mineTrue_listsOnlyOwnCreateBy` |
| 1b | 空/异主 create_by 负例（对照本人 200） | **PASS** | `#provider_updateOwn_200_emptyAndOther_403` |
| 1c | 产品写矩阵 ADMIN 403 / PROVIDER 本人 / USER 403 | **PASS** | CreateByOwnership + RbacMatrixTest |
| 2a | Vitest 公共目录无写入口 | **PASS** | `CatalogBrowsePage.spec.ts` writeVisible |
| 2b | 我的产品入口 / 返回 | **PASS** | CatalogBrowsePage + useCatalogImportEntry → `/my-products` |
| 2c | `useCanWrite` 三角色 | **PASS** | `useCanWrite.spec.ts` 3 tests |
| 2d | 导入宿主；active 互斥 | **PASS** | import-entry + layout `onMineSurface` / `nav-my-products` |
| 2e | mine 切换重拉（FIND-001） | **PASS** | `useCatalogBrowse.spec.ts` mine reactivity |
| 3 | 导入四态回归（宿主迁移后） | **PASS** | `useProductImport.spec.ts` + `ProductImportIntegrationTest`（10） |
| 4 | §4 单元格表 | **PASS** | `03-section4-cells.md` |
| 5 | 预落地对账自动化命名路径 | **PASS** | `04-preland-reconcile-paths.md` |
| A | acceptance：OpenAPI 编辑不回退 | **PASS** | `ProductEditorIntegrationTest` 7/0（相对 R1 FAIL 已绿） |

## §4 单元格摘要

| 能力 | ADMIN | PROVIDER | USER | 证据 |
|---|---|---|---|---|
| 产品写 API | **403** | 本人 **200**；他人/空 **403** | **403** | CreateByOwnership + RbacMatrix |
| 导入 API | **403** | **200** | **403** | ProductImport + RbacMatrix |
| 我的产品菜单 | **不可达** | 可见 | **不可达** | useCanWrite + CatalogBrowsePage |
| 公共目录增改导 | **结构不可达** | **结构不可达** | **结构不可达** | CatalogBrowsePage writeVisible |
| 分类/目录维护 | **200** | 403 | 403 | RbacMatrixTest category/maintenance |

## REQ 追踪

| REQ | 覆盖方式 | 结果 |
|---|---|---|
| REQ-CAT-010 | mine 列表；我的产品入口；公共目录去写；导入宿主 | PASS（Vitest+集成） |
| REQ-RBAC-001 | 产品写/导入三角色矩阵 | PASS（后端矩阵） |
| REQ-SHELL-001 | my-products 白名单；不回退 admin-users / 角色只读 | PASS（CatalogBrowsePage.spec 壳层断言） |
| REQ-CAT-001 | browse helpers + mine 切换不削弱列表 | PASS（useCatalogBrowse） |
| OpenAPI 编辑不回退（acceptance） | ProductEditorIntegrationTest | **PASS** |

## BUG

| id | severity | status | summary | evidence |
|---|---|---|---|---|
| BUG-WSC-603-TESTRUN-001 | P1 | **CLOSED** | 上一轮 `ProductEditorIntegrationTest` 4 例因空库缺 L3 种子返回 400 `ERR_CATEGORY_LEAF_REQUIRED`。R3 修复后本轮 7/0 exitCode=0；正式关闭。 | `01-backend.txt`；`05-bug-status.md` |

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未伪造 mvn / vitest exit 0；命令如实记录
- 未修改业务实现源码、REV、`state.yaml` / `events.jsonl`
- actorInstance=`tester-wsc-603-r2`（≠ developer-wsc-603-r3 / ≠ code-reviewer-wsc-603-r3）
- `retest: true`；未自行宣称 VERIFIED

## 计数

| 指标 | 值 |
|---|---|
| result | **PASS** |
| failedCommandCount | **0** |
| retest | **true** |
| 后端 Tests run | 27 / Failures 0 |
| ProductEditor | 7 / Failures 0 |
| Vitest | 5 files / 53 tests |
| bugsClosed | BUG-WSC-603-TESTRUN-001 |
| bugsOpen | （无） |
