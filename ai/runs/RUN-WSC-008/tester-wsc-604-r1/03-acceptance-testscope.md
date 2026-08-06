# acceptance / testScope checklist 证据定位（TASK-WSC-604）

对齐 PLAN-WSC-5.2 TASK-WSC-604 `acceptance` + `testScope`。本轮命令 #1/#2 均 exitCode=0。

## acceptance

| # | acceptance 项 | 命名证据 | 本轮 |
|---|---|---|---|
| A1 | API：`totalProducts` = 真实总数（非 Top5 之和） | `L2DistributionIntegrationTest#seededProducts_totalProductsReal_andItemsSortedDesc`（total=7 > top5Sum）；`L2DistributionServiceTest#l2Distribution_passesThroughTotalProductsAndItems` | PASS |
| A2 | API：`items` 按 count 降序 | Integration 同上（emr:3 → imaging:2 → credit:1）；ServiceTest `#l2Distribution_itemsSortedByCountDesc` | PASS |
| A3 | UI：L2 Top5；座位按占比 | Vitest `Top5 truncate + proportion seats + real total via getL2Distribution`（`.slice(0, 5)` + `(count/sum)*GRID`） | PASS |
| A4 | hover 熄灭 + mouseleave 恢复 | Vitest `hover dims others (opacity cap) + mouseleave restores`（DIM_OPACITY / @mouseenter / @mouseleave） | PASS |
| A5 | 空态不崩 | Integration `#emptyCatalog_returnsZeroTotalAndEmptyItems`；Service `#l2Distribution_emptyCatalog_safe`；Vitest `empty + loading + error`（`seat-map-empty`） | PASS |
| A6 | loading / error 可读不崩 | Vitest `empty + loading + error states are readable`（`seat-map-loading` / `seat-map-error`） | PASS |
| A7 | 挂载：仅 `/catalog` showSeatMap=true；mine false | Vitest `mount contract: only catalog showSeatMap=true; mine false` | PASS |
| A8 | 无点击筛选 affordance | Vitest `no click-filter affordance`（无 button / 无 @click；role=presentation；cursor:default） | PASS |
| A9 | L2 不在 CatalogBrowseController 残留 | Integration `prelandReconcile_604_l2EndpointOwnedByL2DistributionController_notBrowse` | PASS |
| A10 | 预落地座序图对账 | 见 `04-preland-reconcile-paths.md` | PASS |

## testScope

| # | testScope 项 | 结果 | 证据文件 |
|---|---|---|---|
| 1 | 后端：distribution 计数与种子一致；总数断言；空目录安全 | **PASS** | `01-backend.txt` — Integration 4 + Service 3 = **7**，Failures: 0 |
| 2 | Vitest：Top5；hover+leave；空/loading；无筛选；mine 不渲染约定 | **PASS** | `02-vitest.txt` — 6 passed |
| 3 | 预落地对账自动化（604 行 P0 命名测试） | **PASS** | `04-preland-reconcile-paths.md` |
