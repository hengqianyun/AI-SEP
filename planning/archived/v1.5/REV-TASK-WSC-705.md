# 代码审查

```yaml
reviewId: REV-TASK-WSC-705
taskId: TASK-WSC-705
planId: PLAN-WSC-6.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-705-r1
decision: APPROVE
p0: 0
p1: 0
p2: 2
p3: 1
closedFindings: []
openFindings:
  - FIND-WSC-705-R1-001
  - FIND-WSC-705-R1-002
  - FIND-WSC-705-R1-003
contracts: wsc-contracts@2.3.0（只读消费；本任务未改 contracts/api）
riskTags: [antd-migration, search-wire]
reviewedAt: 2026-08-12T11:05:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-705 writeSet·denyModify·acceptance·testScope；§1.5）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-705.md
  - 实地：CatalogBrowsePage.vue、useCatalogBrowse.ts(+spec)、CatalogBrowsePage.spec.ts
  - 实地：browse/components/BrowseFilterBar.vue（新建）
  - 对照：CirculationSeatMap.vue(+spec) 无 diff；SensitiveId.vue 无 diff
  - tokens.css（--blue / --border-color / --card-bg / --radius-sm / 14px）只读对账
mustDifferFrom:
  - developer（本任务实现者；本实例未兼任）
spotCheck: |
  writeSet：CatalogBrowsePage.vue、useCatalogBrowse.ts(+spec)、CatalogBrowsePage.spec.ts、
  components/BrowseFilterBar.vue（⊆ components/**）。
  labels.ts WT 脏但注释/内容属 TASK-WSC-701（701 writeSet）；DEV-705 未宣称；705 仅读常量 → 不归 705 越界。
  denyModify（本任务归因）：CirculationSeatMap(+spec) 无 diff；未写 maintenance、api、contracts、
  backend、e2e、styles/theme、WorkbenchLayout、useCanWrite、state/events。
  WT 另有 703/706 等脏文件，不归 705。
  Ant：BrowseFilterBar 真实 import ConfigProvider/Input/Select；无 native <select>/type=search。
  §1.5：BROWSE_FILTER_THEME 字面值对齐 tokens（#3b82f6/#e5e7eb/#fff/6/14）+ :deep CSS var 覆盖。
  supplierName：filters + buildQuery；无 enterpriseName query；q 分离。
  701：文案常量绑定保留；advancedOpen=ref(false)；v-show + BrowseFilterBar。
  未分类：groupProductsByL3 殿后；桶内不 sort；无 products.sort。
  catalog+mine：页 mode + composable mine 参数化；spec it.each。
  审查侧复跑 vitest：2 files / 52 passed（非 tester 门禁）。
  未代 tester PASS；未标 VERIFIED；未写 state/events；未改业务代码。
```

## Round 1 结论

**APPROVE** — **P0=0，P1=0**。TASK-WSC-705（Wave B：Ant Input/Select + `supplierName` + 未分类钉底展示）字面 scope / `denyModify` 合规；acceptance（真实 Ant 控件、§1.5 token 映射、企业名 query=`supplierName`、无 `enterpriseName`、与 `q` 分离、701 文案/收起不回退、未分类视觉殿后且无破坏性 FE sort、catalog+mine 同源）源码可复核。开放 findings 为 P2×2 + P3×1，**不阻塞**进入独立 tester 门禁。

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-705-R1-001 | P2 | **OPEN** | `CatalogBrowsePage.spec`「Ant 挂载烟测」以源码扫描为主（import/`<Input`/`<Select`），未 `mount(BrowseFilterBar)` 断言真实 Ant DOM / clear / 选值。与仓内 browse 页测风格一致；静态可复核真实 ant-design-vue 引用 | 可选：挂载 FilterBar，断言 `.ant-input` / `.ant-select` 与 `data-browse-filter-theme`；不阻塞 | REQ-CAT-011；testScope「Ant 控件挂载烟测」 |
| FIND-WSC-705-R1-002 | P2 | **OPEN** | `it.each(['catalog','mine'])` 对同一 `pageSrc`/`filterBarSrc` 重复断言，未按 mode 挂载双表面验证 Ant/`supplierName` 行为分叉（composable 侧 `mine=%s` 已有真实 query 断言补偿） | 可选：双 mode 挂载页或缩小为单条静态断言 + 保留 composable 参数化；不阻塞 | REQ-UX-004；testScope catalog+mine |
| FIND-WSC-705-R1-003 | P3 | **OPEN** | `CatalogBrowsePage.vue` 仍残留 `.filter-bar select` / `.filter-bar .search` 样式块（注释称已迁 BrowseFilterBar）；死 CSS，不影响运行 | 删除或迁出遗留 native 规则 | 可维护性 |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — 页/composable/两 spec + `BrowseFilterBar.vue`；SeatMap/SensitiveId 未改 |
| denyModify：CirculationSeatMap(+spec) / maintenance / api / contracts / backend / e2e / styles·theme / layout·canWrite / state·events | **PASS（705 归因）** — 本任务 diff 未写入；WT 并行脏文件不计入 |
| 真实 ant-design-vue Input/Select（非 class 仿造） | **PASS** — `from 'ant-design-vue'` + 模板 `<Input>`/`<Select>`；无 native select/search |
| §1.5 Ant→token 最小映射 | **PASS** — ConfigProvider theme + scoped `:deep` 钉 `--blue`/`--border-color`/`--card-bg`/`--radius-sm`/14px；字面色值与 `tokens.css` 对账一致 |
| 企业名称 query=`supplierName`；无 `enterpriseName`；与 `q` 分离 | **PASS** — FilterBar 绑定 + `buildQuery`；spec 正/负例 |
| 701 文案与高级筛选默认收起不回退 | **PASS** — L1/L2/行业类别/全部*/高级筛选；`advancedOpen=ref(false)` + `v-show` 包住 BrowseFilterBar |
| 未分类钉在已加载列表末；无破坏性 FE sort | **PASS** — `groupProductsByL3` 殿后；桶内保序；无 `.sort(`；负例测钉死 |
| `/catalog` 与 `/my-products` 同源 | **PASS** — 单页 `mode` + `mine: isMine`；筛栏/Ant/`supplierName` 不按 mode 分叉实现 |
| testScope 覆盖（源码/冒烟） | **PASS（有 P2 备注）** — supplierName/无 enterpriseName、Ant 源码烟测、收起、双 mode、未分类负例；挂载深度 → FIND-001；mode 参数化弱 → FIND-002 |
| 开发自测 ≠ 独立 tester | **记录** — DEV 称 52 passed；审查侧复跑 2/52 passed，**不**代 tester PASS |
| 未改业务外代码 / 未写 state·events / 未标 VERIFIED | **PASS** |
| `mustDifferFrom` 实现者 | **PASS**（`code-reviewer-wsc-705-r1`） |
| P0 / P1 | **P0=0；P1=0** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester PASS。
- **decision: APPROVE**（P0=0、P1=0；开放 P2/P3 可延期）。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 2 | FIND-001 Ant 未真实挂载；FIND-002 mode 参数化弱 |
| P3 | 1 | FIND-003 页内遗留 native filter CSS |
