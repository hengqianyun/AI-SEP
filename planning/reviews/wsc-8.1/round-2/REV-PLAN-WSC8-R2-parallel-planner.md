# Round 2 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC8-R2-parallelPlanner
planId: PLAN-WSC-8.2
round: 2
role: parallelPlanner
actorInstance: parallel-planner-wsc-011-r2
snapshotIdAtReview: SNAP-WSC-008
decision: APPROVE
summary: |
  对照 PLAN-WSC-8.1 R1 本角色 ISSUE-PP-WSC8-R1-001：PLAN-WSC-8.2 已按 closeWhen 方案 (A) 落实
  文件级互斥——905 writeSet 不含 useCanWrite.ts/.spec.ts 且 denyModify 显式禁止；906 独占 composable
  全文；§0.3 所有权表与 §6 并行表同步修正（905∥906 禁止并行、删除「写集无交」假并行表述）。
  DAG 无环；dependsOn ↔ mermaid / §6 五波一致；Wave A 901∥902 写集字面互斥；903∥907 禁并行成立；
  contracts/sql/e2e 唯一所有权清晰；字面 scope-check 可调度。本角色关闭 ISSUE-PP-WSC8-R1-001；
  不代关他人 ISSUE；不写 ai/runs。
closedIssues:
  - ISSUE-PP-WSC8-R1-001
openIssues: []
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-8.2.md` |
| 谱系 | `basedOn` = PLAN-WSC-8.1；`lineageFrom` = PLAN-WSC-6.2 |
| 快照 | SNAP-WSC-008（PO 2026-08-17 APPROVED） |
| 角色装载 | `ai/agents/parallel-planner.md` |
| 模板 | `ai/schemas/templates/planning-review.md` |
| 实例 | `parallel-planner-wsc-011-r2` |
| R1 本角色 | `planning/reviews/wsc-8.1/round-1/REV-PLAN-WSC8-R1-parallel-planner.md`（`REQUEST_CHANGES`，开放 ISSUE×1） |
| 本角色权限 | 只读计划/产品/目录结构；本文件为评审落盘；**未**改计划/源码/`ai/runs/**` |

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | **通过** |
| dependsOn ↔ mermaid / §6 一致 | **通过** |
| Wave A 901∥902 写集互斥 | **通过** |
| 903∥907 禁并行 | **通过** — 依赖链强制串行 |
| 905∥906 禁并行 | **通过** — dependsOn 串行 + §6 字面声明一致 |
| 不稳定读（api/contracts） | **通过** — 907 readSet 要求 api **须 903 VERIFIED** |
| contracts / `frontend/src/api/**` 唯一写 | **通过**（903） |
| Flyway / sql 独占 | **通过**（904） |
| `tests/e2e/**` 唯一写 | **通过**（908） |
| 字面 writeSet / scope-check 可调度 | **通过** — R1 ISSUE closeWhen 满足 |
| ISSUE-PP-WSC8-R1-001 | **关闭** |

## 1. R1 ISSUE 关闭核验

### ISSUE-PP-WSC8-R1-001（P1）— useCanWrite.ts 文件级互斥

| closeWhen 要件 | PLAN-WSC-8.2 证据 | 判定 |
|---|---|---|
| 去掉同文件「片段散文」切割 | §5 TASK-WSC-905 **无** `useCanWrite.ts` / `.spec.ts` 于 `writeSet`；objective 写明「**不改** FE useCanWrite.ts（归 906）」 | **满足** |
| 方案 (A)：905 仅 BE；906 独占 composable | 905 `writeSet` 仅后端 `RbacMatrix` / `WriteAuthorizationInterceptor` / browse·editor·import 范围；905 `denyModify` 显式含 `frontend/.../useCanWrite.ts`（**归 906 独占**）；906 `writeSet` 独占 `useCanWrite.ts` + `.spec.ts` | **满足** |
| §0.3 所有权表同步 | §0.3：`useCanWrite.ts` → **906 独占全文**；905 **denyModify** | **满足** |
| dependsOn 仍串行 | 906 `dependsOn`: `[903, 905]`；§6 Wave C 启动条件「903+905 VERIFIED」 | **满足** |
| §6 并行表不再误导 | §6 并行声明表：**905∥906 → 禁止并行**（删除 R1「写集无交且允许」行）；修订说明映射 ISSUE-PE-WSC8-R1-004 / PP-001 | **满足** |

**结论：closeWhen 满足 → 关闭 ISSUE-PP-WSC8-R1-001。**

## 2. DAG 无环校验

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

## 3. 依赖核对

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
- **907 未直连 903**：经 905/906 传递；readSet 钉死 api **须 903 VERIFIED**，不稳定读交叉已闭合。
- **908 未直连 903/904**：经 905 传递，调度安全。

## 4. 并行写集与禁并行声明（字面 scope-check）

### 4.1 Wave A：901 ∥ 902（允许）

| 资源 | 901 | 902 | 判定 |
|---|---|---|---|
| `CatalogBrowsePage.vue` / `useCatalogBrowse.ts` | write | denyModify | **互斥** |
| `browse/components/**`（除座序图） | write | denyModify（除 CirculationSeatMap*） | **互斥** |
| `CirculationSeatMap.vue` + spec | denyModify | write | **互斥** |
| `L2DistributionController/Service` | deny backend | write | **互斥** |
| `contracts/**` / `frontend/src/api/**` | deny | deny | 仅 903 可写 |

**901∥902 允许** — 写集字面不相交；902 只读挂载关系、禁止改 browse 页/props（§5 denyModify + ISSUE-PE-WSC8-R1-002 吸收）。

### 4.2 903 ∥ 907（禁止）

| 检查 | 结论 |
|---|---|
| 计划声明 | §1.4 / §6：**禁止** |
| 907 `dependsOn` | 含 905、906；二者均依赖 903 |
| 907 readSet | `frontend/src/api/**` **须 903 VERIFIED** |
| 907 `writeSet` | **无** `contracts/**`；denyModify 含 contracts/api |
| 实际调度窗口 | 903 VERIFIED 后 907 方可启动 |

**禁止成立** — 无假并行。

### 4.3 905 ∥ 906（禁止）

| 检查 | 结论 |
|---|---|
| 计划声明 | §1.4 / §6 并行表：**禁止并行** |
| 906 `dependsOn` | 含 905 → 905 VERIFIED 后开 906 |
| 905 `writeSet` | 仅后端 Java + 相关测试；**不含** `useCanWrite.ts` |
| 906 `writeSet` | 独占 `useCanWrite.ts` + `.spec.ts` |
| 字面写集相交 | **无** — R1 片段散文已消除 |

**禁止成立且写集互斥** — Orchestrator 可机械 scope-check。

### 4.4 全局所有权矩阵

| 资源 | 唯一写任务 / 写序 | 判定 |
|---|---|---|
| `contracts/**`、`frontend/src/api/**` | 903 | 通过 |
| `**/sql/migration/**`、企业实体 | 904 | 通过 |
| `CatalogBrowseController/Service`、`RbacMatrix`、browse RBAC | 905 | 通过 |
| `L2DistributionController/Service` | 902 | 通过 |
| `CatalogBrowsePage` + browse 筛选 composable | 901 | 通过 |
| `CirculationSeatMap.vue` | 902 | 通过 |
| `WorkbenchLayout.vue`、壳层企业展示 | 906 | 通过 |
| `useCanWrite.ts` + spec | **906 独占**；905 denyModify | 通过 |
| `router/routes.ts` | 906 → 907（串行增量） | 通过 |
| `CatalogMaintenancePage` + maintenance/** | 907 | 通过 |
| `tests/e2e/**` | 908 | 通过 |
| `frontend/src/styles/**` / `theme/**` | **无写任务** | 通过 |
| `ai/runs/**` | **无写任务** | 通过 |

## 5. 波次可调度性

| 波次 | 任务 | 启动条件 | 判定 |
|---|---|---|---|
| **A** | 901 ∥ 902 | 本计划 APPROVED 后派发 | 通过 |
| **B** | 903 → 904 → 905 | 901+902 VERIFIED 后开 903 | 通过 |
| **C** | 906 | 903+905 VERIFIED | 通过 |
| **D** | 907 | 902+905+906 VERIFIED | 通过 |
| **E** | 908 | 901+902+905+906+907 VERIFIED | 通过 |

合入序服从 DAG + VERIFIED 链，不以完成速度为准 — **通过**。  
八任务均内联 `dependsOn` / `readSet` / `writeSet` / `denyModify` / `acceptance` / `testScope` — **字段完备**。

## 6. 残余观察（非 ISSUE / 不阻塞本角色 APPROVE）

1. **`router/routes.ts` 906→907 串行增量**：906 写导航/meta；907 增 `/my-catalog` 页级 guard；§0.3 / §5 907 acceptance 已散文约束「不得删改 906 ADMIN 双入口 meta」——全序串行可交接；可选升格为 907 `denyModify` 具名 meta 键（Plan Editor 增强项，非并行缺陷）。
2. **905 acceptance 含 maintenance scope 分项测**：maintenance service 归 907 `writeSet`；905 侧为 interceptor/RbacMatrix 授权层——验收分工清晰，无写集冲突。
3. **908 未显式 dependsOn 903/904**：经 905 传递已闭合，可选补边增强可读性 — **不阻塞**。
4. **无任务级 `mergeAfter` 字段**：与 WSC-5.2 本角色 APPROVE 口径一致，§6 波次表 + dependsOn 足够。

## 异议

| id | severity | evidence | closeWhen | relatedReqs | 状态 |
|---|---|---|---|---|---|
| ISSUE-PP-WSC8-R1-001 | P1 | R1：905/906 同写 `useCanWrite.ts` 片段散文，无法字面 scope-check | 文件级互斥；方案 (A) 或 (B)；§0.3 同步 | REQ-RBAC-002, REQ-CAT-016, REQ-SHELL-009 | **已关闭** — §1 核验 |

## 决策

`APPROVE` — DAG **无环**且 dependsOn/五波次一致；**901∥902** 写集互斥与 **903∥907**、**905∥906** 禁并行均成立且 §6 字面声明与 dependsOn 一致；903/904/908 独占资源清晰；**ISSUE-PP-WSC8-R1-001** 已按 closeWhen 方案 (A) 关闭，字面 scope-check **可调度**。

本角色 **不** 代批其他委员会角色，**不** 代关他人 ISSUE，**不** 将本文件视为计划已 `APPROVED`，**不** 写入 `ai/runs/**`。

开放 ISSUE（本角色本轮）：**0**。
