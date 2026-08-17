# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC8-R1-parallelPlanner
planId: PLAN-WSC-8.1
round: 1
role: parallelPlanner
actorInstance: parallel-planner-wsc-011-r1
snapshotIdAtReview: SNAP-WSC-008
decision: REQUEST_CHANGES
summary: |
  DAG 无环（901/902→903→904→905→906→907→908）；dependsOn 与 §6 mermaid/波次表一致；
  Wave A 901∥902 写集互斥成立；903∥907 由依赖链强制串行；contracts/sql/e2e 唯一所有权清晰。
  但 TASK-WSC-905 与 TASK-WSC-906 同写 useCanWrite.ts/.spec.ts，边界依赖「片段级散文」
  （905 仅 product write 片段 vs 906 完整导航门控），与 §1.4 字面 scope-check 及
  WSC-5.1 本角色 P1 先例同型，Orchestrator 无法机械判定 VERIFIED 交接。须修订为文件级
  独占写集后再复评。不代批他角色。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-8.1.md` |
| 快照 | `product/requirements/SNAP-WSC-008.md`（PO 2026-08-17 APPROVED） |
| 角色装载 | `ai/agents/parallel-planner.md` |
| 模板 | `ai/schemas/templates/planning-review.md` |
| 实例 | `parallel-planner-wsc-011-r1` |
| 本角色权限 | 只读计划/产品/目录结构；本文件为评审落盘；**未**改计划/源码/`ai/runs/**` |

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | **通过** |
| dependsOn ↔ mermaid / §6 一致 | **通过** |
| Wave A 901∥902 写集互斥 | **通过** |
| 903∥907 禁并行 | **通过** — 依赖链强制串行 |
| 905∥906 并行表声明 | **无假并行** — dependsOn 已串行；写集相交见 ISSUE |
| 不稳定读（api/contracts） | **通过** — 907 消费 api 前 903 经 905/906 传递 VERIFIED |
| contracts / `frontend/src/api/**` 唯一写 | **通过**（903） |
| Flyway / sql 独占 | **通过**（904） |
| `tests/e2e/**` 唯一写 | **通过**（908） |
| 字面 writeSet / scope-check 可调度 | **未通过** — 见 ISSUE-PP-WSC8-R1-001 |
| mergeAfter / 波次 VERIFIED 链 | **可接受** — §6 五波 + dependsOn 足够 |

## 1. DAG 无环校验

边集合（§5 `dependsOn` + §6 mermaid）：

| from | to |
|---|---|
| TASK-WSC-901 | TASK-WSC-903, TASK-WSC-908 |
| TASK-WSC-902 | TASK-WSC-903, TASK-WSC-907, TASK-WSC-908 |
| TASK-WSC-903 | TASK-WSC-904, TASK-WSC-906 |
| TASK-WSC-904 | TASK-WSC-905 |
| TASK-WSC-905 | TASK-WSC-906, TASK-WSC-907, TASK-WSC-908 |
| TASK-WSC-906 | TASK-WSC-907, TASK-WSC-908 |
| TASK-WSC-907 | TASK-WSC-908 |

拓扑序示例：`901/902 → 903 → 904 → 905 → 906 → 907 → 908`（901/902 可并行起算）。全部边由早层指向晚层，**无回边、无环**。

## 2. 依赖核对

| taskId | dependsOn（§5） | 与 mermaid / §6 | 判定 |
|---|---|---|---|
| TASK-WSC-901 | `[]` | Wave A 源点 | 通过 |
| TASK-WSC-902 | `[]` | Wave A 源点 | 通过 |
| TASK-WSC-903 | `[901, 902]` | 一致；Wave B 首任务 | 通过 |
| TASK-WSC-904 | `[903]` | 一致 | 通过 |
| TASK-WSC-905 | `[903, 904]` | 一致 | 通过 |
| TASK-WSC-906 | `[903, 905]` | 一致；Wave C | 通过 |
| TASK-WSC-907 | `[902, 905, 906]` | 一致；Wave D | 通过 |
| TASK-WSC-908 | `[901, 902, 905, 906, 907]` | 一致；Wave E | 通过 |

专项：

- **903 独占契约前置**：`contracts/**` + `frontend/src/api/**`，符合公共契约独立任务口径。
- **907 未直连 903**：经 905/906 传递，且 readSet 要求 api **须 903 VERIFIED**，不稳定读交叉已闭合。
- **908 未直连 903/904**：经 905 传递，调度安全。

## 3. 并行写集与禁并行声明

### 3.1 Wave A：901 ∥ 902

| 资源 | 901 | 902 | 判定 |
|---|---|---|---|
| `CatalogBrowsePage.vue` / `useCatalogBrowse.ts` | write | denyModify | **互斥** |
| `browse/components/**`（除座序图） | write | denyModify（除 CirculationSeatMap*） | **互斥** |
| `CirculationSeatMap.vue` + spec | denyModify | write | **互斥** |
| `L2DistributionController/Service` | deny backend | write | **互斥** |
| `contracts/**` / `frontend/src/api/**` | deny | deny | 仅 903 可写 |

**901∥902 允许** — 写集字面不相交；902 对 `CatalogBrowsePage` 的 props 需求已走 ISSUE 升级路径，不构成写冲突。

### 3.2 903 ∥ 907（禁止）

| 检查 | 结论 |
|---|---|
| 计划声明 | §1.4 / §6：**禁止** |
| 907 `dependsOn` | 含 905、906；二者均依赖 903 |
| 907 readSet | `frontend/src/api/**` **须 903 VERIFIED** |
| 实际调度窗口 | 903 完成并 VERIFIED 后 907 方可启动 |

**禁止成立** — 无假并行。

### 3.3 905 ∥ 906（并行表「允许」vs dependsOn）

计划 §6 并行表称 905∥906「写集无交且允许」，但 **906 `dependsOn` 含 905**，实际为 **串行**（905 VERIFIED 后开 906）。此非「假并行调度」缺陷；但两任务 **字面 writeSet 相交**于 `useCanWrite.ts` / `.spec.ts`，串行交接边界不可机械判定 — 见 ISSUE-001。

### 3.4 全局所有权矩阵

| 资源 | 唯一写任务 / 写序 | 判定 |
|---|---|---|
| `contracts/**`、`frontend/src/api/**` | 903 | 通过 |
| `**/sql/migration/**`、企业实体 | 904 | 通过 |
| `CatalogBrowseController/Service`、`RbacMatrix`、browse RBAC | 905 | 通过 |
| `L2DistributionController/Service` | 902 | 通过 |
| `CatalogBrowsePage` + browse 筛选 composable | 901 | 通过 |
| `CirculationSeatMap.vue` | 902 | 通过 |
| `WorkbenchLayout.vue`、壳层企业展示 | 906 | 通过 |
| `router/routes.ts` | 906 → 907（串行增量） | 通过（串行散文见观察） |
| `CatalogMaintenancePage` + maintenance/** | 907 | 通过 |
| `tests/e2e/**` | 908 | 通过 |
| `frontend/src/styles/**` / `theme/**` | **无写任务** | 通过 |
| `ai/runs/**` | **无写任务** | 通过 |
| `useCanWrite.ts` + spec | 905 **与** 906（片段散文） | **ISSUE-001** |

## 4. 波次可调度性

| 波次 | 任务 | 启动条件 | 判定 |
|---|---|---|---|
| **A** | 901 ∥ 902 | 本计划 APPROVED 后派发 | 通过 |
| **B** | 903 → 904 → 905 | 901+902 VERIFIED 后开 903 | 通过 |
| **C** | 906 | 903+905 VERIFIED | 通过 |
| **D** | 907 | 902+905+906 VERIFIED | 通过 |
| **E** | 908 | 901+902+905+906+907 VERIFIED | 通过 |

合入序服从 DAG + VERIFIED 链，不以完成速度为准 — **通过**。  
八任务均内联 `dependsOn` / `readSet` / `writeSet` / `denyModify` / `acceptance` / `testScope` — **字段完备**；问题在边界**可机械性**。

## 5. 预落地与并行假设

| 检查 | 结论 |
|---|---|
| Wave A 并行假设 | **成立** — browse 与座序图路径已拆分 |
| §0.2 预落地对账 | 按任务 writeSet 验收，不以「代码已在」代测试 |
| 预落地 `/my-maintenance` vs `/my-catalog` | 906 acceptance 统一命名 — 调度语义问题，非并行缺陷 |
| 预落地 matrix 混改 | 903/905 串行纠偏 — 不破坏 DAG |
| `useCanWrite` 预落地 | 可能已含导航+产品写混合改动 — **放大 ISSUE-001** |

## 6. 残余观察（非 ISSUE / 不单独阻塞）

1. **`router/routes.ts` 906→907 串行增量**：906 写导航 meta，907 增 `/my-catalog` 与 guard；全序串行可交接；若修订 ISSUE-001 时可顺带在 907 `denyModify` 中显式禁止删改 906 已建 ADMIN 双入口 meta 键（计划 §0.3 已有散文，可升格为 deny 列表）。
2. **§6 并行表 905∥906「允许」**：与 dependsOn 串行并存，建议 Plan Editor 加注「实际调度串行，此行仅表示 hypothetical 写集相容」以免 Orchestrator 误读 — **不单独升格**。
3. **无任务级 `mergeAfter` 字段**：与 WSC-5.2 本角色 APPROVE 口径一致，波次表足够。
4. **908 未显式 dependsOn 903/904**：经 905 传递已闭合，可选补边增强可读性 — **不阻塞**。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PP-WSC8-R1-001 | P1 | PLAN-WSC-8.1 §5 TASK-WSC-905 `writeSet` 含 `frontend/src/features/auth/composables/useCanWrite.ts` 与 `.spec.ts`，并附散文：「**仅** `canWriteProduct`/`canImportProduct` ADMIN=true 与 903 矩阵对齐片段；**完整**导航门控归 906」。同计划 TASK-WSC-906 又显式写入同一 `useCanWrite.ts` / `.spec.ts`（导航可见性 + §4 矩阵）。两任务对同文件声称「部分写」，**无法**按 §1.4 做字面 scope-check；与 REV-PLAN-WSC5-R1-parallel-planner ISSUE-PP-WSC5-R1-002（602↔603 useCanWrite 片段切割）同型。预落地 §0.2 已可能在 `useCanWrite.ts` 混入导航与产品写改动，905 VERIFIED 后 906 再改同一文件时 scope 争议与回滚风险高。 | 修订为**文件级互斥**，去掉同文件片段散文。推荐方案（与 PLAN-WSC-6.2 702 独占口径对齐）：**(A)** 从 905 `writeSet` **移除** `useCanWrite.ts` 及 `.spec.ts`；905 仅改后端 `RbacMatrix` / `WriteAuthorizationInterceptor` 与相关集成测；906 **独占** `useCanWrite.ts` + spec（一次性对齐 903 矩阵：含 `canWriteProduct`/`canImportProduct` **与** 导航/myCatalog/myMaintenance 门控），且 906 `dependsOn` 仍含 905（后端矩阵 VERIFIED 后再写 FE）。**(B)** 若坚持 905 写 FE 产品写片段：905 **独占**整个 `useCanWrite.ts` + spec，906 **denyModify** 该 composable，906 仅改 `WorkbenchLayout` / `routes.ts` / shell 组件，导航门控经 905 已冻结的 composable 导出（须在 905 acceptance 钉死全部 §4 导航键）。任选 (A) 或 (B)，并在 §0.3 所有权表同步。关闭后方可对本角色再批。 | REQ-RBAC-002, REQ-CAT-016, REQ-SHELL-009 |

## 决策

`REQUEST_CHANGES` — DAG **无环**且 dependsOn/五波次一致；Wave A **901∥902** 写集互斥与 **903∥907** 禁并行均成立；903/904/908 独占资源清晰。但 **ISSUE-PP-WSC8-R1-001（P1）** 表明 `useCanWrite.ts` 在 905/906 间片段散文切割，Orchestrator **无法**字面 scope-check，与 WSC-5.1 本角色未关闭前口径一致。请 Plan Editor 按 closeWhen 修订候选计划后，再调度本角色 Round 复评。

本角色 **不** 代批其他委员会角色，**不** 代关他人 ISSUE，**不** 将本文件视为计划已 `APPROVED`，**不** 写入 `ai/runs/**`。

开放 ISSUE（本角色本轮）：**1**（P1×1，P2×0）。
