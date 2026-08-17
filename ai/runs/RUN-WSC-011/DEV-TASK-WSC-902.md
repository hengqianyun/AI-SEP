# DEV-TASK-WSC-902

```yaml
taskId: TASK-WSC-902
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
actorInstance: developer-wsc-902
status: READY_FOR_REVIEW
completedAt: 2026-08-17T10:50:00+08:00
reqs:
  - REQ-CAT-015
  - REQ-CAT-018
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## 对账结论（预落地 → §0.2 902 行）

| 项 | 预落地状态 | 本任务动作 |
|---|---|---|
| `CirculationSeatMap.vue` | 无 L1 下拉；仅全量 `getL2Distribution()` | **新增** 标题行右侧 L1 Select（默认「全部」）；独立 state；选 L1 时 `?l1CategoryId=` |
| `L2DistributionController/Service` | 604 全量分布 | **扩展** 可选 `l1CategoryId` query；全链无企业 scope |
| browse Cascader / CatalogBrowsePage | 901 负责 | **只读**；本任务 **未改** |
| `frontend/src/api/catalog.ts` | `getL2Distribution()` 无参 | **只读**；903 前组件内 `apiRequest` 带 query |

无未决 ISSUE。

## filesChanged

### Backend
- `backend/.../controller/catalog/browse/L2DistributionController.java` — 接受可选 `@RequestParam l1CategoryId`
- `backend/.../service/catalog/browse/L2DistributionService.java` — 无 L1 委托 store；有 L1 按产品 `l1CategoryId` 子树聚合 L2 + `totalProducts`
- `backend/.../test/.../L2DistributionServiceTest.java` — L1 过滤单元测 + blank 回退
- `backend/.../test/.../L2DistributionIntegrationTest.java` — `l1CategoryId` 集成、total 同口径、cross-enterprise parity、enterprise 参数负例

### Frontend
- `frontend/src/features/catalog/browse/components/CirculationSeatMap.vue` — 标题 flex（h2 左 / L1 Select 右）；`listCategories` 拉 L1；`watch(selectedL1Id)` 重载；ConfigProvider token 映射
- `frontend/src/features/catalog/browse/components/CirculationSeatMap.spec.ts` — 604 回归 + 902 命名用例 `902-seatmap-l1-dropdown`、`902-no-enterprise-filter` 等

未改：`CatalogBrowsePage.vue`、`useCatalogBrowse.ts`、browse 其它组件、`contracts/**`、`frontend/src/api/**`、CatalogBrowseController/Service、sql、e2e、planning/product、`state.yaml`、`events.jsonl`。

## acceptance 勾选

- [x] 标题行：h2 左、L1 控件右（`seat-map-title-row` / `seat-map-l1-select`）
- [x] 下拉含「全部」+ 全部 L1；默认「全部」= V1.5 Top5 行为
- [x] 选 L1 后卡片/图例/`l2-distribution?l1CategoryId=` 同 L1；切换 loading 不崩布局
- [x] **不与** browse Cascader 双向同步（独立 `selectedL1Id`；无 props/inject/useCatalogBrowse）
- [x] 选具体 L1 时 `totalProducts` 与 API L1 子集同口径（OQ-V16-003）
- [x] **无**企业筛选控件；API **不按**登录用户企业过滤（parity 集成测）
- [x] 仅公共目录挂载（604 约定未改）
- [x] hover/leave/空态/loading 不回退 V1.5
- [x] L1 Select token 映射（`data-seat-map-filter-theme="ant-token-mapped"`）

## 自测命令与结果

```text
mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=L2DistributionServiceTest,L2DistributionIntegrationTest" test
→ Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
→ BUILD SUCCESS

pnpm exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts
  (cwd: frontend)
→ Test Files  1 passed (1)
→ Tests  13 passed (13)
```

### 预落地 / testScope 自动化证据定位

| 用例名 | 文件 | 断言 |
|---|---|---|
| `902-seatmap-l1-dropdown` | `CirculationSeatMap.spec.ts` | title-row、L1 Select、`l1CategoryId` query、watch 重载 |
| `902-no-enterprise-filter` | `CirculationSeatMap.spec.ts` | 无 enterprise UI/query |
| `l1CategoryId_filtersToMatchingL1Only` | `L2DistributionIntegrationTest.java` | health L1 仅 emr/imaging |
| `l1CategoryId_totalProductsMatchesL1Subset` | `L2DistributionIntegrationTest.java` | total=2 含无 L2 产品 |
| `crossEnterpriseParity_adminAndUserSameFullChainResult` | `L2DistributionIntegrationTest.java` | user/admin 同 data |
| `noEnterpriseScopeParam_responseUnchanged` | `L2DistributionIntegrationTest.java` | enterprise 参数不改变分布 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| REQ-CAT-015 | 座序图 L1 下拉 + 卡片联动；无企业筛 |
| REQ-CAT-018 | 全链统计不按用户企业过滤（BE parity + FE 无 enterprise 参数） |

## scope

- writeSet 内 6 文件均已变更
- denyModify 路径均未触碰
- `getL2Distribution()` API 生成留 **TASK-WSC-903**；本任务 FE 用 `apiRequest` 临时带参
