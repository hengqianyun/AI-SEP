# DEV-TASK-WSC-604

```yaml
taskId: TASK-WSC-604
runId: RUN-WSC-008
actorInstance: developer-wsc-604
planId: PLAN-WSC-5.2
status: READY_FOR_REVIEW
completedAt: 2026-08-06T11:00:00+08:00
reqs:
  - REQ-CAT-009
  - REQ-CAT-001
```

> 交付说明落于 `ai/runs/RUN-WSC-008/`（避免 `planning/**` denyModify）。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。

## 对账结论（预落地 → §0.2 604 行 + §1.5 座序图）

| 项 | 预落地状态 | 本任务动作 |
|---|---|---|
| L2 与 CatalogBrowse 同文件 | 603 已迁出空壳 | **填充** `L2DistributionController` / `L2DistributionService` 真实现 |
| `CirculationSeatMap.vue` | 预落地草稿（直调 `apiRequest`、button 图例、缺空态 testid） | 对齐契约：`getL2Distribution`；Top5；比例座位；hover/leave；空/loading/error；无点击筛选 |
| `CatalogBrowsePage` 挂载 | 603 约定 `showSeatMap` + `v-if` | **只读消费**；本任务未改该页 |
| `DistributionSliceRecord` | overview 用 name/value | **未改**（L2 复用 store Map code/name/count） |
| OpenAPI / `frontend/src/api` | 601 已冻结 | **只读** |

无未决 ISSUE。

## filesChanged

### Backend
- `backend/.../controller/catalog/browse/L2DistributionController.java` — `@RestController` + `GET /api/v1/catalog/l2-distribution`
- `backend/.../service/catalog/browse/L2DistributionService.java` — 委托 `CatalogBrowseSeedStore#l2Distribution()`（真实总数 + count 降序 items）
- `backend/.../test/.../catalog/browse/L2DistributionServiceTest.java` — 透传/降序/空目录
- `backend/.../test/.../catalog/browse/L2DistributionIntegrationTest.java` — 401、空目录、种子计数与总数、**预落地对账** `prelandReconcile_604_*`

### Frontend
- `frontend/src/features/catalog/browse/components/CirculationSeatMap.vue` — 座序图实现体
- `frontend/src/features/catalog/browse/components/CirculationSeatMap.spec.ts` — Top5/hover/空态/无筛选/挂载约定/对账

未改：`contracts/**`、`frontend/src/api/**`、`CatalogBrowsePage.vue`、browse Controller/Service、security、sql、e2e、`state.yaml`、`events.jsonl`。

## acceptance 勾选

- [x] API：`totalProducts` = 真实产品总数（非 Top5/items 之和冒充）；`items` 按 count 降序
- [x] UI：L2 Top5；座位按占比；hover 熄灭其它（`DIM_OPACITY`）+ mouseleave 恢复
- [x] `totalProducts=0` / items 空：可读空态（`seat-map-empty`），不崩布局
- [x] loading（`seat-map-loading`）可读；失败（`seat-map-error`）不崩页
- [x] 挂载：仅 `/catalog` `showSeatMap=true`；`/my-products` `showSeatMap=false`（603 约定 + spec 对账）
- [x] 无点击筛选 affordance（无 `<button>` / 无 `@click`；`role="presentation"`；`cursor: default`）
- [x] 预落地座序图对账；L2 不在 `CatalogBrowseController` 残留（`prelandReconcile_604_l2EndpointOwnedByL2DistributionController_notBrowse`）

## 自测命令与结果

```text
mvn -f backend/pom.xml -pl app/data-chain-service "-Dtest=L2DistributionServiceTest,L2DistributionIntegrationTest" test
→ Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
→ BUILD SUCCESS

cd frontend && pnpm exec vitest run src/features/catalog/browse/components/CirculationSeatMap.spec.ts
→ Test Files  1 passed (1)
→ Tests  6 passed (6)
```

### 预落地对账自动化证据定位

| 用例名 | 文件 | 断言 |
|---|---|---|
| `prelandReconcile_604_l2EndpointOwnedByL2DistributionController_notBrowse` | `L2DistributionIntegrationTest.java` | handler 在 L2Controller，不在 Browse |
| `prelandReconcile_604: directory card layer + seat-map root testid` | `CirculationSeatMap.spec.ts` | `circulation-seat-map` + `wsc-surface` |
| `mount contract: only catalog showSeatMap=true; mine false` | `CirculationSeatMap.spec.ts` | routes + CatalogBrowsePage `v-if` |

## showSeatMap 消费（只读，来自 DEV-TASK-WSC-603）

| 项 | 约定 |
|---|---|
| `/catalog` | `showSeatMap: true` → 渲染本组件 |
| `/my-products` | `showSeatMap: false` → 不渲染 |
| 挂载点 | `<CirculationSeatMap v-if="seatMapVisible" data-testid="catalog-seat-map-slot" />` |

## scope

- writeSet 内：L2 Controller/Service、CirculationSeatMap + spec、L2* 测试
- **未**改 DistributionSliceRecord（无需）
- denyModify 路径均未触碰
