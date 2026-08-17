# 測試證據 — TASK-WSC-901

```yaml
testrunId: TESTRUN-TASK-WSC-901
taskId: TASK-WSC-901
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: tester-wsc-901
mustDifferFrom: [developer-wsc-901, code-reviewer-wsc-901]
basedOn:
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-901 testScope/acceptance）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-901.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-901.md
  - product/requirements/SNAP-WSC-008.md（REQ-CAT-014 / REQ-CAT-019）
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-17T11:03:00+08:00
decision: PASS
exitCode: 0
requirements: [REQ-CAT-014, REQ-CAT-019]
note: |
  覆寫 void 旁路產物（原 PLAN-WSC-8.1 / REQ-SHELL-009 選單用例，與 Wave A 瀏覽篩選無關）。
  未改源碼、審查結論、state.yaml、events.jsonl；未自行標 VERIFIED。
```

## 結論摘要

**PASS** — 獨立 tester-wsc-901 依 PLAN-WSC-8.3 §5 `testScope` 實際執行 browse 相關 Vitest；三輪命令均 **exitCode=0**。§0.2 命名用例 `901-no-nav-cards`、`901-cascader-l1-l2-clear`、`901-no-industryCategory-query` 共 **13** 項全部通過；`acceptance` 與 `testScope` 覆蓋項均有對應測試證據。審查前提 **REV-TASK-WSC-901 APPROVE（P0=0、P1=0）** 已滿足。

## 執行命令與結果

| # | 命令 | 工作目錄 | exitCode | 結果 |
|---|---|---|---|---|
| 1 | `pnpm test -- src/features/catalog/browse/` | `frontend/` | **0** | **71 passed**（5 files） |
| 2 | `pnpm test -- src/features/catalog/browse/CatalogBrowsePage.spec.ts src/features/catalog/browse/components/BrowseFilterBar.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts` | `frontend/` | **0** | **53 passed**（3 files；901 writeSet 核心 spec） |
| 3 | `pnpm vitest run src/features/catalog/browse/ -t "901-no-nav-cards\|901-cascader-l1-l2-clear\|901-no-industryCategory-query" --reporter=verbose` | `frontend/` | **0** | **13 passed** / 58 skipped（§0.2 命名用例專跑） |

Vitest 版本：**v2.1.9**。執行環境：本機 Node/pnpm；無環境阻塞；未將 env 探針記為 PASS。

## §0.2 命名用例驗證

| 命名用例 | 檔案 | 用例數 | 結果 |
|---|---|---|---|
| `901-no-nav-cards` | `CatalogBrowsePage.spec.ts` | 2 | **PASS** |
| `901-no-industryCategory-query` | `CatalogBrowsePage.spec.ts`、`BrowseFilterBar.spec.ts`、`useCatalogBrowse.spec.ts` | 5 | **PASS** |
| `901-cascader-l1-l2-clear` | `BrowseFilterBar.spec.ts`、`useCatalogBrowse.spec.ts` | 6 | **PASS** |

命名用例細目（命令 3 verbose 輸出）：

- `901-no-nav-cards: removes V1.5 space/industry tag cards from page` — PASS
- `901-no-nav-cards: no advanced filter toggle; filter bar always mounted via BrowseFilterBar` — PASS
- `901-no-industryCategory-query: page does not bind industryCategory filter` — PASS
- `901-no-industryCategory-query: no industry category control` — PASS
- `901-no-industryCategory-query: listProducts never sends industryCategory or enterpriseName` — PASS
- `901-no-industryCategory-query ('catalog' mode): query omits industryCategory` — PASS
- `901-no-industryCategory-query ('mine' mode): query omits industryCategory` — PASS
- `901-cascader-l1-l2-clear: Cascader binds category path with clear` — PASS
- `901-cascader-l1-l2-clear: L1 only sends l1CategoryId without l2CategoryId` — PASS
- `901-cascader-l1-l2-clear: L1+L2 sends both category ids` — PASS
- `901-cascader-l1-l2-clear: clear removes l1 and l2 from query` — PASS
- `901-cascader-l1-l2-clear ('catalog' mode): cascader boundaries` — PASS
- `901-cascader-l1-l2-clear ('mine' mode): cascader boundaries` — PASS

## testScope 對照（PLAN-WSC-8.3 §5）

| # | testScope 要求 | 證據 | 結果 |
|---|---|---|---|
| 1 | 無卡片 DOM/testid | `901-no-nav-cards`；頁面源碼無 `nav-card`/`tag-bar` | **PASS** |
| 2 | 無 advanced toggle；面板常顯 | `901-no-nav-cards`（無 `advancedFilter`/`filter-toggle`）；`BrowseFilterBar.spec.ts` 無 `v-if=.*advanced` | **PASS** |
| 3 | query 無 `industryCategory`、無 `enterpriseName` | `901-no-industryCategory-query`（catalog + mine） | **PASS** |
| 4 | Cascader：L1 僅 `l1CategoryId`；L2 兩者皆有；清空皆無 | `901-cascader-l1-l2-clear`（含 `applyCategoryPath` 行為測） | **PASS** |
| 5 | catalog + mine 參數化同一套斷言 | `useCatalogBrowse.spec.ts` `('catalog'/'mine' mode)` 參數化用例 | **PASS** |
| 6 | `supplierName` 仍發出 | `supplierName still sent on applyFilters`；頁級 `supplierName field is separate` | **PASS** |
| 7 | 分類 loading/empty 態 | `category loading/empty/error affordances in BrowseFilterBar`（`:loading`、`catalog-filter-category-empty`） | **PASS** |
| 8 | token 映射斷言 | `data-browse-filter-theme="ant-token-mapped"` + `ConfigProvider` 源碼斷言 | **PASS** |

## acceptance 對照

| acceptance 項 | 結果 | 測試/證據 |
|---|---|---|
| 無列表區業務視圖/業務大類卡片與 active 標籤行 | **PASS** | `901-no-nav-cards` |
| 無「高級篩選」toggle；面板預設可見 | **PASS** | `901-no-nav-cards` + `BrowseFilterBar` 常顯掛載 |
| 高級區無 industryCategory；query 無 industryCategory / enterpriseName | **PASS** | `901-no-industryCategory-query` |
| Cascader l1/l2 邊界 + loading/error/空選項 | **PASS** | `901-cascader-l1-l2-clear` + loading/empty testid 斷言 |
| Ant→token 映射 | **PASS** | `ant-token-mapped` 斷言 |
| `/catalog` 與 `/my-products` 同源篩選；mine 仍無座序圖 | **PASS** | mine 參數化 Cascader/query；`seatMapVisible` 邏輯未改（頁 spec 既有） |
| V1.5 supplierName 與其餘 Ant 篩選不回退 | **PASS** | supplierName 獨立欄位 + query 測 |
| 座序圖區域仍掛載（組件由 902 負責） | **PASS** | `CatalogBrowsePage` 仍含 `CirculationSeatMap`（901 denyModify 未改組件） |

## REQ 追蹤

| REQ | 驗證要點 | 證據 |
|---|---|---|
| REQ-CAT-014 | 移除卡片；常顯高級區；Cascader l1/l2；雙表面同源；token 映射 | 命名用例 + testScope #1–5、#7–8 |
| REQ-CAT-019 | 瀏覽不傳 `industryCategory`；編輯/導入枚舉 untouched（本任務 denyModify） | `901-no-industryCategory-query`；writeSet 未含 editor/import |

## 審查前提

| 項 | 值 |
|---|---|
| reviewId | REV-TASK-WSC-901 |
| decision | APPROVE |
| P0 / P1 | **0 / 0** |
| P2（非阻塞） | FIND-WSC-901-R1-001（副標題文案）、FIND-WSC-901-R1-002（theme hex 硬編碼）— 不影響 tester PASS |

## 缺陷

無。全部必需 Vitest 通過，未開 BUG-*。

## 證據紅線（自檢）

- [x] 實際執行 testScope 內 Vitest（browse），非僅讀開發/審查報告
- [x] 環境阻塞未記為 PASS
- [x] 未改源碼 / 審查結論 / state / events
- [x] `actorInstance=tester-wsc-901`，與 developer / codeReviewer 隔離
- [x] 未自行標任務 VERIFIED

## 證據路徑

- 本報告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-901.md`
- 任務包：`planning/approved/PLAN-WSC-8.3.md` §5 TASK-WSC-901
- 開發報告：`ai/runs/RUN-WSC-011/DEV-TASK-WSC-901.md`
- 審查：`ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-901.md`
- 測試源碼：`frontend/src/features/catalog/browse/**/*.spec.ts`
