# 測試證據 — TASK-WSC-903

```yaml
testrunId: TESTRUN-TASK-WSC-903
taskId: TASK-WSC-903
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: tester-wsc-903
mustDifferFrom:
  - developer-wsc-903
  - developer-wsc-903-r2
  - developer-wsc-903-sec-fix
  - code-reviewer-wsc-903-r2
  - security-reviewer-wsc-903-r2
basedOn:
  - planning/approved/PLAN-WSC-8.3.md（§3.1 矩陣表 + §5 TASK-WSC-903 testScope/acceptance）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-903.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-903-R2.md（APPROVE；P0/P1=0）
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-903-R2.md（APPROVE）
  - product/requirements/SNAP-WSC-008.md
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
securityReviewDecision: APPROVE
executedAt: 2026-08-17T12:50:00+08:00
decision: PASS
exitCode: 0
failedCommandCount: 1
requirements: [REQ-USER-002, REQ-RBAC-002, REQ-CAT-016, REQ-CAT-017, REQ-CAT-018, REQ-SHELL-009, REQ-CAT-015]
note: |
  獨立 tester-wsc-903 實跑 testScope。未改源碼、審查結論、state.yaml、events.jsonl；未 git commit；未自行標 VERIFIED。
  check-contracts.mjs 因寫集外釘死 1.1.0 而 exit 1（如實記錄 FAIL，不作偽 PASS）；§3.1 語義由 assert-903-p0.mjs 覆蓋。
  全倉 typecheck exit 2 僅 WorkbenchLayout.vue（906 寫集）；api/** 隔離 typecheck exit 0，不記為 903 FAIL。
```

## 結論摘要

**PASS** — 獨立 `tester-wsc-903` 依 PLAN-WSC-8.3 §5 TASK-WSC-903 `testScope` 實際執行：四頭 semver 均為 **2.3.3**；OpenAPI V1.6 增量（enterpriseId、mine 本企業、maintenance `scope=full|myCatalog`、`l2-distribution?l1CategoryId=`、`createBy` 非 mine 過濾依據）可檢；`matrix.yaml` 與 §3.1 **逐 cell** 交叉（關鍵 cell 全部字面一致）；`frontend/src/api/**` 隔離 typecheck **exit 0**；預落地 VERSION/matrix/OpenAPI 漂移已閉合。

審查前提：codeReview R2 **APPROVE（P0=0、P1=0）**；securityReview R2 **APPROVE**。本報告不代標 VERIFIED。

## 執行命令與結果

| # | 命令 | 工作目錄 | exitCode | 結果 | 證據 |
|---|---|---|---|---|---|
| 1 | `node tests/contracts/check-contracts.mjs` | repo root | **1** | **FAIL**（腳本釘死 VERSION expected 1.1.0，got 2.3.3） | `check-contracts.txt` |
| 2 | `Get-Command spectral, redocly, swagger-cli, openapi-generator-cli` | repo root | n/a | **未安裝**（無匹配命令） | 本表 |
| 3 | `python -c "import yaml"` | repo root | **1** | **FAIL**（`ModuleNotFoundError: No module named 'yaml'`） | 本表 |
| 4 | `npx --yes @stoplight/spectral-cli --version` | repo root | **無可用 exit** | **未完成**（npx 無回傳；倉內無 spectral 依賴） | 本表；**未**記為 PASS |
| 5 | `node ai/runs/RUN-WSC-011/tests/assert-903-p0.mjs` | repo root | **0** | **PASS 107 / FAIL 0 / KNOWN_P2 1** | `assert-903-p0-output.txt` |
| 6 | `pnpm run typecheck` | `frontend/` | **2** | **FAIL** — 僅 `src/layouts/WorkbenchLayout.vue` 5 條 TS2300/TS2451 | `typecheck-full.txt` |
| 7 | `pnpm exec vue-tsc --noEmit -p ../ai/runs/RUN-WSC-011/tests/tsconfig.api-only.json --pretty false` | `frontend/` | **0** | **PASS**（無診斷；僅 include `src/api/**` + `vite-env.d.ts`） | `typecheck-api-only.txt` |

**failedCommandCount = 1**（僅 #1 官方契約腳本；#3/#4 為工具鏈探針失敗/未完成，不作 OpenAPI CLI PASS；#6 為 906 預存錯誤，見下）。

## testScope 對照（PLAN-WSC-8.3 §5 TASK-WSC-903）

| # | testScope 要求 | 結果 | 證據 |
|---|---|---|---|
| 1 | 契約 lint / OpenAPI 校驗 | 官方腳本 **FAIL**（1.1.0 釘死）；倉內無 spectral/redocly；**未**偽稱 CLI lint PASS。語義與結構由 #5 **107 PASS** 覆蓋（`openapi: 3.0.3`、paths、V1.6 增量） | #1 FAIL + #2–4 探針 + #5 PASS |
| 2 | 四頭版本同步同一 semver | **PASS** — 皆 **2.3.3** | 見下節 |
| 3 | matrix 與 §3.1 逐 cell 交叉 | **PASS**（關鍵 cell 字面一致；1 格已知 P2 見下） | #5 + 本報告矩陣表 |
| 4 | OpenAPI：enterpriseId、mine 本企業、maintenance scope、l1CategoryId、createBy 非 mine 過濾 | **PASS** | #5 |
| 5 | client `frontend/src/api/**` typecheck | **PASS**（隔離 exit 0）。全倉 exit 2 **不在** `api/**` | #6 / #7 |
| 6 | 對賬：預落地 VERSION/matrix/OpenAPI 漂移閉合 | **PASS** | #5：matrix 非 2.3.2；OpenAPI info 非 2.2.0；VERSION 保持 2.3.3 未再 bump |

## 四頭版本同步

| 頭 | 路徑 | 實測值 | 結果 |
|---|---|---|---|
| VERSION | `contracts/VERSION` | `2.3.3` | **PASS** |
| matrix | `contracts/rbac/matrix.yaml` `version` | `"2.3.3"` | **PASS** |
| OpenAPI | `contracts/openapi/openapi.yaml` `info.version` | `2.3.3` | **PASS** |
| state-matrix | `contracts/ui/state-matrix.md` 版本頭 | `wsc-contracts@2.3.3` | **PASS** |

附帶（非四頭、已對齊）：`contracts/errors/codes.yaml` `version: "2.3.3"`；`frontend/src/api/client.ts` `CONTRACT_VERSION = '2.3.3'`；api 各模組頭註解 `@2.3.3`。`contracts/overview/stream-events.yaml` 仍為歷史 `2.0.0`（非 903 四頭）。

§3.1 對賬例外：WORKTREE 已為 2.3.3 → **合併 V1.6 增量至 2.3.3 且不再升**。實測未再 bump。

## OpenAPI V1.6 增量勾選

| 項 | 結果 | 落點 |
|---|---|---|
| Session / AdminUser **必填** `enterpriseId` + `enterpriseName` | **PASS** | OpenAPI `Session.required` / `AdminUser.required` |
| AdminUserCreate 含 `enterpriseId` | **PASS** | OpenAPI `AdminUserCreate` |
| `mine=true` → **本企業**（非 create_by-only） | **PASS** | info description L9；listProducts L519 / L563 |
| maintenance query **`scope=full\|myCatalog`**（enum、required） | **PASS** | `/catalog/maintenance/entries` L800–806 |
| PROVIDER `scope=full` → 403 敘述 | **PASS** | L794–795 |
| `GET /catalog/l2-distribution?l1CategoryId=`（可選；不按登入企業過濾） | **PASS** | L296–310 |
| 安全說明：僅信 SessionPrincipal；拒絕/忽略客戶端 enterpriseId 授權 | **PASS** | L14；list/maintenance 各 description |
| `Product.createBy` **非** mine 過濾依據 | **PASS** | L1703：審計/歸屬；指向 listProducts `mine`（enterpriseId 比較，非 create_by-only） |
| 全檔無「mine=true 过滤依据」 | **PASS** | OpenAPI + `frontend/src/api/**` |
| V1.5 `supplierName` 不回退 | **PASS** | listProducts `supplierName` query |

## matrix.yaml × §3.1 逐 cell

計劃字面表（PLAN-WSC-8.3 §3.1）對 `contracts/rbac/matrix.yaml`：

| 能力鍵 | ADMIN（計劃 / matrix） | PROVIDER（計劃 / matrix） | USER（計劃 / matrix） |
|---|---|---|---|
| `catalogMaintenanceUI` | visible / visible **PASS** | hidden / hidden **PASS** | hidden / hidden **PASS** |
| `catalogMaintenanceApi`（scope=full） | 200 / `{expect:200, on:full, scope:full}` **PASS** | 403 / `{expect:403, scope:full}` **PASS** | 403 / `{expect:403, scope:full}` **PASS** |
| `myCatalogUI` | visible / visible **PASS** | visible / visible **PASS** | hidden / hidden **PASS** |
| `myCatalogApi`（scope=myCatalog） | 200 ownEnterprise / 同左 **PASS** | 200 ownCreateBy / 同左 **PASS** | 403 / `{expect:403, scope:myCatalog}` **PASS** |
| `myProductsUI` | visible / visible **PASS** | visible / visible **PASS** | hidden / hidden **PASS** |
| `productWriteUI` | visible / visible **PASS** | visible / visible **PASS** | hidden / hidden **PASS** |
| `productImportUI` | visible / visible **PASS** | visible / visible **PASS** | hidden / hidden **PASS** |
| `productWriteApi` | 200 ownEnterprise / 同左 **PASS** | 200 ownCreateBy / 同左 **PASS** | 403 / 403 **PASS** |
| `productImportApi` | 200 ownEnterprise / 同左 **PASS** | **200 ownCreateBy** / `{expect:200, on:valid}` **KNOWN_P2** | 403 / 403 **PASS** |

**KNOWN_P2**：`PROVIDER.productImportApi.on=valid` 與 §3.1 字面 `ownCreateBy` 不一致。已由 `SEC-REV-TASK-WSC-903-R2` 記為 **FIND-SEC-WSC-903-003（P2，OPEN）**，且該輪 **APPROVE** 聲明不阻塞 903。HTTP `expect: 200` 仍與計劃一致；本 tester **不**升格 FAIL、**不**另開產品 BUG 重複該 FIND。905 仍須 ownCreateBy 負例。

state-matrix：側欄「我的目錄」`/my-catalog`；ADMIN 我的產品可見；PROVIDER 目錄維護 **隱藏**；ADMIN 雙入口說明 — **PASS**。

req-coverage：SNAP-008 十條 REQ（CAT-014..019、SHELL-009/010、USER-002、RBAC-002）均在 — **PASS**。

## client `frontend/src/api/**`

| 項 | 結果 |
|---|---|
| `CONTRACT_VERSION = '2.3.3'` | **PASS** |
| `Session.enterpriseId` / `AdminUser.enterpriseId` | **PASS** |
| `MaintenanceScope = 'full' \| 'myCatalog'`；`listMaintenanceEntries` | **PASS** |
| `getL2Distribution({ l1CategoryId })` | **PASS** |
| `ListProductsQuery.mine`：true → 本企業 | **PASS** |
| `Product.createBy` JSDoc 與 OpenAPI L1703 同口徑（非過濾依據） | **PASS** |
| api/** 無 `mine=true 过滤依据`、無 2.2.0/2.3.2 版本釘 | **PASS** |

### 全倉 typecheck vs api/**（906 預存錯誤隔離）

全倉 `pnpm run typecheck` **exit 2**，診斷 **全部** 位於 `src/layouts/WorkbenchLayout.vue`（重複 `canSeeMyMaintenance` / `myMaintenanceVisible`）。輸出中 **無** `src/api/` 路徑。

PLAN-WSC-8.3 §5：`WorkbenchLayout.vue` 屬 **TASK-WSC-906 writeSet**；903 `denyModify` `frontend/src/layouts/**`。

隔離命令（僅 `src/api/**/*.ts` + `vite-env.d.ts`）**exit 0**。故：

- **不得**把 906 預存錯誤記為 903 FAIL
- **不得**把全倉紅燈當環境阻塞而偽稱 PASS（已證明失敗不在 api/**，且 api 隔離通過）

## 預落地對賬（§0.2 / ISSUE-API-WSC8-R1-006）

| 預落地 | 現況 | 結果 |
|---|---|---|
| `contracts/VERSION` 可能 2.3.3 | **保持 2.3.3**，未再 bump | **閉合** |
| `matrix.yaml` 2.3.2 / V1.5 語義 | version **2.3.3**；§3.1 cell 已糾偏 | **閉合** |
| OpenAPI `info.version` 2.2.0 | **2.3.3** + V1.6 增量 | **閉合** |
| `state-matrix.md` 2.2.0 | **2.3.3** + 我的目錄 / ADMIN 雙入口 | **閉合** |

## REQ 追蹤

| REQ | 驗證要點 | 結果 |
|---|---|---|
| REQ-USER-002 | Session/AdminUser enterpriseId+enterpriseName | **PASS** |
| REQ-RBAC-002 | matrix §3.1；maintenance scope=full\|myCatalog | **PASS**（ImportApi on 見已知 P2） |
| REQ-CAT-016 | mine=本企業；createBy 非 create_by-only 過濾 | **PASS** |
| REQ-CAT-017 | maintenance scope=myCatalog；state-matrix 我的目錄 | **PASS** |
| REQ-CAT-018 | 全鏈/L2 無企業 filter；安全說明 | **PASS** |
| REQ-SHELL-009 | 側欄我的目錄；ADMIN 雙入口；PROVIDER 無目錄維護 | **PASS** |
| REQ-CAT-015 | `l2-distribution?l1CategoryId=` + client | **PASS** |

## 對 check-contracts FAIL 的處理（非 BUG）

| 項 | 說明 |
|---|---|
| 命令 | `node tests/contracts/check-contracts.mjs` exitCode=**1** |
| 原因 | 腳本硬編碼 `VERSION expected 1.1.0`；現樹 `contracts/VERSION`=`2.3.3` |
| 範圍 | `tests/contracts/**` **不在** TASK-WSC-903 writeSet（與 TESTRUN-601 / FIND-WSC-703-R1-001 同口徑） |
| 補償 | `assert-903-p0.mjs` 107 項命名斷言證明 §3.1 / acceptance；**未**把工具鏈滯後偽稱為 PASS |
| 交回 | 另開工具鏈任務將 expected 對齊當前主版本；**不**因本命令對 903 契約交付記 FAIL |

## 缺陷

無新開 `BUG-*`。

- #1 腳本滯後：不開產品 BUG（寫集外）。
- #6 WorkbenchLayout：906 寫集預存錯誤，非 903。
- `PROVIDER.productImportApi.on=valid`：沿用已開放之 **FIND-SEC-WSC-903-003（P2）**，不重複開 BUG。

## 審查前提

| 項 | 值 |
|---|---|
| codeReview | `REV-TASK-WSC-903-R2` **APPROVE**；P0=0、P1=0 |
| securityReview | `SEC-REV-TASK-WSC-903-R2` **APPROVE**；P0=0、P1=0 |
| 開放 P2（不阻塞本門禁） | FIND-WSC-903-R1-002/003/004；FIND-SEC-WSC-903-002/003 |

## 證據紅線（自檢）

- [x] 實際執行 testScope（命令 + 斷言），非僅讀 DEV/REV
- [x] 環境阻塞 / 未完成 CLI **未**記為 PASS
- [x] 未改源碼 / 審查結論 / state.yaml / events.jsonl
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-903`，與 developer / codeReviewer / securityReviewer 隔離
- [x] 未自行標任務 VERIFIED
- [x] 906 預存 typecheck 錯誤未記為 903 FAIL；api/** 隔離通過

## 證據路徑

- 本報告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-903.md`
- 斷言腳本 / 輸出：`ai/runs/RUN-WSC-011/tests/assert-903-p0.mjs`、`assert-903-p0-output.txt`
- 契約腳本：`ai/runs/RUN-WSC-011/tests/check-contracts.txt`
- typecheck：`typecheck-full.txt`、`typecheck-api-only.txt`、`tsconfig.api-only.json`
- 任務包：`planning/approved/PLAN-WSC-8.3.md` §3.1 / §5 TASK-WSC-903
- 開發 / 審查：`DEV-TASK-WSC-903.md`、`reviews/REV-TASK-WSC-903-R2.md`、`reviews/SEC-REV-TASK-WSC-903-R2.md`
