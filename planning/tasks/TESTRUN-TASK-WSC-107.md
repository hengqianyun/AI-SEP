# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-107-1
taskId: TASK-WSC-107
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-107
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/TASK-WSC-107.md
  - planning/tasks/DEV-TASK-WSC-107.md
  - planning/tasks/REV-TASK-WSC-107.md
  - planning/approved/PLAN-WSC-2.2.md
reviewDecision: APPROVE
executedAt: 2026-07-31T22:47:55+08:00
status: PASSED
```

## 结论摘要

**PASSED** — 独立 `tester-wsc-107` 按 testScope 实跑：后端 `ProductImportIntegrationTest` **8** 测 BUILD SUCCESS（exitCode=0）；前端 vitest `useProductImport.spec.ts` **6** 测 + `pnpm typecheck` 退出码 0。覆盖：fixtures full/partial/code-conflict/category-mismatch/required-field-missing/oversize；模板列齐全；成功行可检索上链；错误报告白名单；临时文件清理；报告 GET USER 403 / 未认证 401；普通用户导入 403；FE 四态与关闭回目录信号。browse 未挂载入口 E2E → **SKIPPED**（对照 REV P2 FIND-001，不伪造成 PASS）。波次 E2E `TESTRUN-WSC-E2E-V11` **不在本任务范围** → **SKIPPED**。

- 模块：`backend/.../catalog/import`（Java 包 `productimport`）；`frontend/src/features/catalog/import/**`；`tests/fixtures/import/**`
- 审查：REV-TASK-WSC-107 APPROVE，P0/P1=0
- 集成测：H2 + MockMvc；未将 env/live DB 探针记为 PASSED
- 未修改业务代码或审查报告；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-002/tester-wsc-107/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| backend-product-import-integration | `mvn -f backend/pom.xml "-Dtest=com.shdata.datachain.catalog.productimport.ProductImportIntegrationTest" test` | PASSED | Tests run: 8, Failures: 0, Errors: 0, Skipped: 0; BUILD SUCCESS EXIT=0 → `01-mvn-product-import.txt`, `01-mvn-exit.txt`, `01-mvn-summary.txt`, `01-backend-test-methods.txt` |
| fixture-full-success | same run / `fullSuccess_searchableAndChained` | PASSED | full-success.csv；可检索 + 上链 + tempFilesCleaned |
| fixture-partial-success | same run / `partialSuccess_reportDownloadable_whitelistOnly` | PASSED | 双计数 + reportId + 白名单四字段 |
| fixture-code-conflict | same run / `codeConflict_andCategoryMismatch_andRequiredMissing` | PASSED | 编码冲突行级失败 |
| fixture-category-mismatch | same run（同上） | PASSED | 缺三级 / L3 行级失败 |
| fixture-required-field-missing | same run（同上） | PASSED | 必填缺失可识别 |
| fixture-oversize | same run / `oversize_requestLevelReject` | PASSED | ERR_IMPORT_FILE_TOO_LARGE（运行时 >10MB） |
| template-columns | same run / `template_hasAllRequiredColumns` | PASSED | 模板列齐全（ISSUE-PA-R1-004） |
| chain-searchable | same run / `fullSuccess_searchableAndChained` | PASSED | browse q 检索 + versions/snapshot |
| report-whitelist-temp-cleanup | same / partial + fullSuccess | PASSED | 白名单负例字段约束；temp 清理钩子 |
| report-get-auth | same / `reportGet_forbiddenForUserAndUnauthenticated` | PASSED | USER 403；未认证 401（REV FIND-003 口径） |
| ordinary-user-import-forbidden | same / `ordinaryUser_importForbidden` | PASSED | POST import 403 |
| frontend-vitest-useProductImport | `pnpm exec vitest run src/features/catalog/import/composables/useProductImport.spec.ts` | PASSED | 6 tests；EXIT=0 → `02-vitest-useProductImport.txt`, `02-vitest-exit.txt` |
| frontend-typecheck | `pnpm typecheck`（cwd: frontend） | PASSED | EXIT=0 → `03-typecheck.txt`, `03-typecheck-exit.txt` |
| browse-entry-e2e | （本任务不可执行 E2E） | SKIPPED | denyModify browse；REV FIND-001；→ `04-browse-entry-e2e-skipped.txt`；FE 仅单测关闭回目录 |
| wave-e2e-v11 | TESTRUN-WSC-E2E-V11 | SKIPPED | 不在 TASK-WSC-107 范围 → `05-wave-e2e-v11-skipped.txt` |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | fixture `full-success` | PASSED — `fullSuccess_searchableAndChained` |
| 2 | fixture `partial-success` | PASSED — `partialSuccess_reportDownloadable_whitelistOnly` |
| 3 | fixture `code-conflict` | PASSED — `codeConflict_andCategoryMismatch_andRequiredMissing` |
| 4 | fixture `category-mismatch` | PASSED — 同上方法内覆盖 |
| 5 | fixture `required-field-missing` | PASSED — 同上方法内覆盖 |
| 6 | fixture `oversize` | PASSED — `oversize_requestLevelReject` |
| 7 | 模板列齐全 | PASSED — `template_hasAllRequiredColumns` + FE 列集单测 |
| 8 | CHAIN / 上链可检索 | PASSED — `fullSuccess_searchableAndChained`（q + versions + snapshot） |
| 9 | 错误报告白名单 | PASSED — partialSuccess 行键仅 rowNumber/productCode/reasonCode/reasonMessage |
| 10 | 临时文件清理 | PASSED — fullSuccess `tempFilesCleanedCount` 钩子 |
| 11 | 报告 GET 403（及未认证行为） | PASSED — USER 403；未认证 401（与 REV FIND-003 一致，不伪称字面匿名 403） |
| 12 | 入口关闭回目录（FE 单测） | PASSED — vitest 关闭 → idle / `onClosed` |
| 13 | browse 未挂载入口可达 E2E | SKIPPED — FIND-001；不得伪造 PASS |
| 14 | 波次 E2E TESTRUN-WSC-E2E-V11 | SKIPPED — 注明不在本任务范围 |

## 证据红线

- 未将环境阻塞 / 未执行项记为 PASS
- 未自行宣称 VERIFIED（仅产出 TESTRUN）
- 未修改业务源码、REV 报告或 `state.yaml`
- actorInstance=`tester-wsc-107`（≠ developer-wsc-107 / ≠ code-reviewer-wsc-107）
- 波次 E2E 未冒充本任务 PASS
- browse 入口 E2E SKIPPED，对照 REV P2 FIND-001
