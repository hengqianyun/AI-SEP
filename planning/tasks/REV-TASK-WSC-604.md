# 代码审查

```yaml
reviewId: REV-TASK-WSC-604
taskId: TASK-WSC-604
planId: PLAN-WSC-5.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-604-r1
decision: APPROVE
p0: 0
p1: 0
closedFindings: []
openFindings:
  - FIND-WSC-604-R1-001
  - FIND-WSC-604-R1-002
contracts: wsc-contracts@2.2.0（只读对照；非本任务 writeSet）
riskTags: [visualization, perf-aggregation]
reviewedAt: 2026-08-06T11:04:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-5.2.md（TASK-WSC-604 writeSet/denyModify/acceptance/testScope；§0.2 604 行；§1.5 UX-003；§6.2）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-604.md
  - 实地：L2DistributionController/Service、CirculationSeatMap.vue + spec、L2*Test、CatalogBrowseSeedStore#l2Distribution（只读）、CatalogBrowsePage showSeatMap 挂载（denyModify）、CatalogBrowseController（无 l2 残留）、openapi/api getL2Distribution（只读）
mustDifferFrom: developer-wsc-604
spotCheck: |
  mvn -pl app/data-chain-service -Dtest=L2DistributionServiceTest,L2DistributionIntegrationTest test → 7/0 BUILD SUCCESS
  pnpm exec vitest run .../CirculationSeatMap.spec.ts → 6 passed
  未代 tester PASS；未标 VERIFIED；未做 security/migration 审查（riskTags 不含触发项）
```

## Round 1 结论

**APPROVE** — **P0=0，P1=0**。字面 scope-check 通过：交付 `filesChanged` 均落在 TASK-WSC-604 `writeSet`；`denyModify`（含 `CatalogBrowsePage.vue`、`contracts/**`、`frontend/src/api/**`、`CatalogBrowseController`/`Service`、`**/sql/**`、`state.yaml`/`events.jsonl`）未见本任务触碰。acceptance 关键点（真实 `totalProducts`、items 降序、前端 Top5、比例座位、hover/leave、空/loading/error、仅 catalog 挂载约定、无点击筛选、L2 不在 Browse Controller）源码与命名自动化证据可复核。开放 findings 均为 P3，不阻塞进入独立 tester。

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-604-R1-001 | P3 | **OPEN** | `CirculationSeatMap.spec.ts` 六例均为源码/路由字符串断言，未 mount 组件、未对 opacity/`mouseleave` 做运行时断言；覆盖面可测但弱于 testScope「可测」精神 | 可选：Vitest mount + mock `getL2Distribution`，断言 Top5 DOM、dim opacity、leave 恢复、空/loading/error testid | REQ-CAT-009；testScope Vitest |
| FIND-WSC-604-R1-002 | P3 | **OPEN** | `L2DistributionService` 纯委托 `CatalogBrowseSeedStore#l2Distribution()`；`L2DistributionServiceTest` 降序/总数用例依赖 mock 已满足契约的返回值，不验证服务层自身排序。真行为由 Integration + SeedStore 覆盖，不升格 | 可选：服务层显式排序/契约断言；或单测改测 store；保持现状并依赖 Integration 亦可 | REQ-CAT-009；testScope 后端 |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — L2 Controller/Service、CirculationSeatMap(+spec)、`**/catalog/browse/**L2*` 测试 |
| denyModify 未触碰（含 CatalogBrowsePage / contracts / api / Browse Controller·Service / sql / state·events） | **PASS**（交付声明 + 实地：Page 仅只读消费 `showSeatMap`；Browse Controller 无 `/l2-distribution` 映射） |
| API：`totalProducts` = 真实总数（非 Top5 之和） | **PASS** — SeedStore `all.size()`；Integration 种子 7（含无 L2）且 `total > top5Sum` |
| API：`items` 按 count 降序 | **PASS** — SeedStore `sorted` + Integration 断言 `[emr:3, imaging:2, credit:1]` |
| UI：Top5 + 比例座位 + hover 熄灭 + mouseleave 恢复 | **PASS** — `.slice(0,5)`；`(count/sum)*GRID`；`DIM_OPACITY` + `@mouseenter`/`@mouseleave` |
| 空态 / loading / error 可读不崩 | **PASS** — `seat-map-empty` / `loading` / `error` testid |
| 挂载：仅 `/catalog` `showSeatMap=true`；mine false | **PASS** — routes + Page `v-if="seatMapVisible"`（603 约定；604 只读） |
| 无点击筛选 affordance | **PASS** — 无 `<button>` / 无 `@click`；`role="presentation"`；`cursor: default`；cell `pointer-events: none` |
| L2 不在 CatalogBrowseController 残留 | **PASS** — `prelandReconcile_604_l2EndpointOwnedByL2DistributionController_notBrowse` |
| 预落地对账自动化证据（命名用例） | **PASS** — Integration `prelandReconcile_604_*`；spec `prelandReconcile_604` + mount contract |
| §1.5 UX-003 座序图层次（card / 无第二仪表盘） | **PASS** — `wsc-surface` + `circulation-seat-map`；目录内挂载 |
| DistributionSliceRecord / 契约 / api | **PASS** — 未改（L2 用 Map；只读消费 `getL2Distribution`） |
| 自测声明可复核 | **PASS** — 审查者抽测后端 7/0、前端 6/6，与交付一致 |
| riskTags visualization/perf-aggregation | **记录** — 未代做 security/migration/tester；**未**标 VERIFIED |
| `mustDifferFrom` developer-wsc-604 | **PASS**（`code-reviewer-wsc-604-r1`） |
| P0 / P1 | **无** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester / securityReviewer / migrationReviewer。
- **decision: APPROVE**（P0=0、P1=0；开放 finding 均为 P3）。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 0 | — |
| P3 | 2 | FIND-001 字符串级 Vitest；FIND-002 服务层透传单测弱 |
