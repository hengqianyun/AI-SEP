# 测试证据

```yaml
testrunId: TESTRUN-TASK-WSC-705
taskId: TASK-WSC-705
planId: PLAN-WSC-6.2
actorInstance: tester-wsc-705-r1
decision: PASS
commands:
  - id: C1
    command: 'pnpm exec vitest run src/features/catalog/browse/CatalogBrowsePage.spec.ts src/features/catalog/browse/composables/useCatalogBrowse.spec.ts'
    cwd: frontend
    exitCode: 0
    result: PASS
    note: "Test Files 2 passed; Tests 52 passed (CatalogBrowsePage 25 + useCatalogBrowse 27); Duration ~1.07s"
summary: "acceptance / testScope 主项全部由 Vitest 绿测 + 源码抽查覆盖；REV 开放 P2×2/P3×1 已记录不据此 FAIL；未测 CirculationSeatMap"
reviewRef: planning/tasks/REV-TASK-WSC-705.md
reviewDecision: APPROVE
reviewRound: 1
reviewP0: 0
reviewP1: 0
reviewOpenP2: 2
reviewOpenP3: 1
contracts: wsc-contracts@2.3.0（只读消费）
requirements: [REQ-CAT-011, REQ-CAT-012, REQ-UX-001, REQ-UX-004]
executedAt: 2026-08-12T11:06:39+08:00
mustDifferFrom:
  - developer（本任务实现者）
  - code-reviewer-wsc-705-r1
basedOn:
  - ai/agents/tester.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-705 acceptance·testScope）
  - planning/tasks/REV-TASK-WSC-705.md（APPROVE；P0=0 P1=0；FIND-001/002 P2 OPEN；FIND-003 P3 OPEN）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-705.md
scopeNote: |
  本门禁覆盖 CatalogBrowsePage + useCatalogBrowse + specs + BrowseFilterBar。
  明确不测 / 不改 CirculationSeatMap（denyModify；页仅挂载）。
```

## 结论

**PASS** — 独立 `tester-wsc-705-r1` 在 REV Round 1 **APPROVE**（P0=0、P1=0）后真实执行 testScope：

| # | command | cwd | exitCode | result |
|---|---|---|---|---|
| C1 | `pnpm exec vitest run …/CatalogBrowsePage.spec.ts …/useCatalogBrowse.spec.ts` | `frontend` | **0** | **PASS** — 2 files / 52 tests |

developer / codeReviewer 自测或审查侧冒烟 **不算** 本门禁；本轮为独立重跑。未修改生产业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED；未 commit。

## acceptance / testScope 勾选（证据）

| # | 项 | 结果 | 证据 |
|---|---|---|---|
| 1 | 筛选区为 ant-design-vue `Input`/`Select`（真实组件） | **PASS** | C1 `CatalogBrowsePage` Ant 烟测：`from 'ant-design-vue'` + `<Input`/`<Select`；无 native `<select` / `type="search"`；实地 `BrowseFilterBar.vue` 同构 |
| 2 | §1.5 Ant→token 最小映射（可勾选） | **PASS** | C1 §1.5 断言：`BROWSE_FILTER_THEME` / `colorPrimary #3b82f6` / `colorBorder #e5e7eb` / `fontSize: 14` / `data-browse-filter-theme` + CSS var 深覆盖 |
| 3 | 企业名称 query 键 `supplierName`；**无** `enterpriseName`；与 `q` 分离 | **PASS** | C1 composable `mine=%s`：`query.supplierName='申能股份'` + `q`；`not.toHaveProperty('enterpriseName')`；blank 省略键仍无 `enterpriseName`；页/FilterBar 源码烟测同口径 |
| 4 | 701 文案与高级筛选默认收起**不回退** | **PASS** | C1：`advancedOpen=ref(false)` + `v-show` 包住 `BrowseFilterBar`；业务视图/大类/行业类别与「全部*」文案断言仍绿（catalog+mine） |
| 5 | 未分类钉在已加载列表末；**无**破坏性前端 sort | **PASS** | C1 `groupProductsByL3 pins 未分类…`：末段「未分类数据」；桶内保序 `['a1','a2']`；负例结果 ≠ `updatedAt` 降序；源码 `useCatalogBrowse.ts` **无** `.sort(` |
| 6 | `/catalog` 与 `/my-products` 一致（双 mode） | **PASS** | C1 `it.each(['catalog','mine'])` 文案/收起/Ant/`supplierName`；composable `mine=false|true` 真实 `applyFilters` query 参数化 |
| 7 | 范围外：CirculationSeatMap | **记录** | 本门禁未跑 SeatMap spec；页测仅断言仍挂载、不内联改实现（与 denyModify 一致） |

## 开放 findings（REV，不阻塞）

| id | sev | 本轮处理 |
|---|---|---|
| FIND-WSC-705-R1-001 | P2 | **记录** — Ant 烟测以源码扫描为主，未 `mount(BrowseFilterBar)` 断言 DOM；与仓内风格一致，不阻塞 |
| FIND-WSC-705-R1-002 | P2 | **记录** — 页侧 `it.each(mode)` 对同一源码重复断言；composable 双 mine 真实 query 已补偿 |
| FIND-WSC-705-R1-003 | P3 | **记录** — 页内遗留 native filter CSS 死规则 |

## 命令输出摘录

### C1（exit 0）

```
RUN  v2.1.9 C:/WorkSpace/AI-SEP/frontend

 ✓ src/features/catalog/browse/CatalogBrowsePage.spec.ts (25 tests) 12ms
 ✓ src/features/catalog/browse/composables/useCatalogBrowse.spec.ts (27 tests) 30ms

 Test Files  2 passed (2)
      Tests  52 passed (52)
   Start at  11:06:27
   Duration  1.07s
```

## 证据红线

- 未将环境阻塞记为 PASS
- 未伪造 Vitest exitCode
- 未因 REV 开放 P2/P3 单独判 FAIL（acceptance 主项已由 C1 覆盖）
- 未修改业务代码 / REV / `ai/runs/**/state.yaml` / `events.jsonl`
- `actorInstance=tester-wsc-705-r1` ≠ developer ≠ `code-reviewer-wsc-705-r1`
