# Round 2 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC5-R2-parallelPlanner
planId: PLAN-WSC-5.2
round: 2
role: parallelPlanner
actorInstance: parallel-planner-wsc-008-r2
snapshotIdAtReview: SNAP-WSC-005
decision: APPROVE
summary: |
  对照 PLAN-WSC-5.1 R1 本角色两条 P1：文件级写集互斥与五波次串行 DAG 均已满足。
  ISSUE-PP-WSC5-R1-001 按方案 (A) 落实（602 deny RbacMatrix/拦截器；603 独占）。
  ISSUE-PP-WSC5-R1-002 三条件齐（CatalogBrowsePage 603 独占；L2 独立类 604 独占；
  602 deny useCanWrite）。DAG 无环 601→602→603→604→605；无同波并行对；
  契约/E2E 唯一所有权清晰。本角色关闭自有两条 ISSUE；不代关他人；不写 ai/runs。
closedIssues:
  - ISSUE-PP-WSC5-R1-001
  - ISSUE-PP-WSC5-R1-002
openIssues: []
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-5.2.md` |
| 谱系 | `basedOn` / `lineageFrom` = PLAN-WSC-5.1 |
| 快照 | SNAP-WSC-005（文首仍声明 `IN_REVIEW`；派发门禁非本角色 DAG 缺陷） |
| 角色装载 | `ai/agents/parallel-planner.md` |
| 模板 | `ai/schemas/templates/planning-review.md` |
| 实例 | `parallel-planner-wsc-008-r2` |
| R1 本角色 | `REV-PLAN-WSC5-R1-parallel-planner.md`（`REQUEST_CHANGES`，开放 ISSUE×2） |
| 本角色权限 | 只读计划/产品；本文件为评审落盘；**未**改计划/源码/`ai/runs/**` |

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环（五任务） | **通过** |
| dependsOn ↔ mermaid / §6 一致 | **通过** |
| 波次数 = 5（串行） | **通过** |
| 同波并行写集互斥 | **不适用（无同波并行对）** |
| 不稳定读交叉（并行窗口） | **通过** — 无并行窗口 |
| 共享迁移/安全注册表串行 | **通过** — 全序 + 文件级独占 |
| 契约 / `frontend/src/api/**` 唯一写 | **通过**（601） |
| `tests/e2e/**` 唯一写 | **通过**（605；R1 观察已吸收） |
| 字面 writeSet / scope-check 可调度 | **通过** — R1 ISSUE closeWhen 满足 |
| ISSUE-PP-WSC5-R1-001 | **关闭** |
| ISSUE-PP-WSC5-R1-002 | **关闭** |

## 1. R1 ISSUE 关闭核验

### 1.1 ISSUE-PP-WSC5-R1-001（P1）— RbacMatrix 文件级互斥

| closeWhen 要件 | PLAN-WSC-5.2 证据 | 判定 |
|---|---|---|
| 去掉「部分写矩阵」散文 | §0 映射；§2.3 方案 (A)；602 `writeSet` **无** `common/security/**` glob，亦无条件「对账后允许写矩阵」散文 | **满足** |
| 方案 (A) 或 (B) | 选定 **(A)**：602 仅具名 `PasswordHasher` / `AuthAuditLogger`（及会话相关列名文件）；`denyModify` 显式含 `RbacMatrix.java`、`WriteAuthorizationInterceptor.java`；603 `writeSet` 独占二者；§3.2 所有权矩阵一致 | **满足** |
| dependsOn 仍串行 | 602 → 603 | **满足** |

**结论：closeWhen 满足 → 关闭 ISSUE-PP-WSC5-R1-001。**

### 1.2 ISSUE-PP-WSC5-R1-002（P1）— CatalogBrowsePage / L2 / useCanWrite

| closeWhen 要件 | PLAN-WSC-5.2 证据 | 判定 |
|---|---|---|
| (1) CatalogBrowsePage 文件级独占 | **603 独占**该页（mine/去写 + 预留 `showSeatMap`）；604 **denyModify** `CatalogBrowsePage.vue`；604 仅写 `CirculationSeatMap.vue`；603 readSet 约定 604 只读消费挂载点 | **满足** |
| (2) L2 独立类路径 | 604 独占 `L2DistributionController.java` / `L2DistributionService.java`；603 **denyModify** 二者；604 **denyModify** `CatalogBrowseController` / `CatalogBrowseService`；预落地同文件残留由 603 acceptance「迁出至 604 文件」收口（验收义务，非双写同文件） | **满足** |
| (3) useCanWrite 边界 | 602 `denyModify` 显式含 `useCanWrite.ts` + `.spec.ts`；602 `writeSet` 收窄为 auth api/store/views + RoleSwitcher 等具名路径（**不含**该 composable）；603 独占写入 | **满足** |

**结论：closeWhen 三条件齐 → 关闭 ISSUE-PP-WSC5-R1-002。**

## 2. DAG 无环与五波次

边集合（§5 `dependsOn` + §6 mermaid）：

| from | to |
|---|---|
| TASK-WSC-601 | TASK-WSC-602 |
| TASK-WSC-602 | TASK-WSC-603 |
| TASK-WSC-603 | TASK-WSC-604 |
| TASK-WSC-604 | TASK-WSC-605 |

拓扑序：`601 → 602 → 603 → 604 → 605`。全部边由早层指向晚层，**无回边、无环**。

| taskId | dependsOn（§5） | 波次（§6） | 判定 |
|---|---|---|---|
| TASK-WSC-601 | `[]` | W1 | 通过 |
| TASK-WSC-602 | `[TASK-WSC-601]` | W2 | 通过 |
| TASK-WSC-603 | `[TASK-WSC-602]` | W3 | 通过 |
| TASK-WSC-604 | `[TASK-WSC-603]` | W4 | 通过 |
| TASK-WSC-605 | `[TASK-WSC-604]` | W5 | 通过 |

`waveCount: 5` / `taskCount: 5` 与上表一致。相对 R1 四波，新增 **605** 串行独占 E2E，不引入并行边。

## 3. 并行写集与禁并行声明

计划 §1.4 / §6：**串行五波；任意 601..605 两两禁止并行**。同波均为单任务。

| 对 | 计划声明 | 本角色判定 |
|---|---|---|
| 任意两两 ∥ | 禁止 | **成立** — `dependsOn` 链强制全序 |
| 601 ∥ 后继 | 禁止至 601 VERIFIED | **成立** |

**无同波并行对 ⇒ 不存在「写集相交却声称可并行」的假并行。**

### 3.1 串行交接（路径交集，文件级可机械判定）

| 路径 / 资源 | 写序 | 5.2 口径 | 判定 |
|---|---|---|---|
| `contracts/**`、`frontend/src/api/**` | 仅 601 | 后继 deny | **通过** |
| `RbacMatrix` / `WriteAuthorizationInterceptor` | 仅 603 | 602 deny | **通过**（001） |
| `useCanWrite.ts`(+spec) | 仅 603 | 602 deny | **通过**（002） |
| `CatalogBrowsePage.vue` | 仅 603 | 604 deny | **通过**（002） |
| `L2Distribution*.java` / `CirculationSeatMap.vue` | 仅 604 | 603 deny | **通过**（002） |
| `tests/e2e/**` | 仅 605 | 601..604 deny | **通过** |
| `**/sql/migration/**` | 602 →（条件）603 | 全序；603 仅缺列时 `V*__*create_by*` | **可调度**（R1 观察，不升格） |
| `WorkbenchLayout.vue` / `routes.ts` | 602 → 603 | §3.2 字面白名单 + 603 收口 | **可调度**（串行；非本角色 R1 ISSUE） |

## 4. 波次可调度性

| 波次 | 任务 | 启动条件 | 判定 |
|---|---|---|---|
| W1 | 601 | SNAP 门禁 + 本计划 APPROVED 后派发 | 条件写明 |
| W2 | 602 | 601 VERIFIED | 通过 |
| W3 | 603 | 602 VERIFIED | 通过 |
| W4 | 604 | 603 VERIFIED | 通过 |
| W5 | 605 | 604 VERIFIED | 通过 |

合入序服从全序 DAG，不以完成速度为准 — **通过**。  
601..605 均内联 dependsOn / readSet / writeSet / denyModify / acceptance / testScope — **完备性通过**。

## 5. 预落地与并行假设

| 检查 | 结论 |
|---|---|
| 是否假设可并行加速 | **否** — 显式串行五波 |
| 预落地是否破坏可调度性 | **否** — 对账进各任务；文件级独占使 VERIFIED 交接可机械 scope-check |
| SNAP `IN_REVIEW` | 派发硬门禁，**非** DAG/写集缺陷 |

## 6. 残余观察（非 ISSUE / 不阻塞本角色 APPROVE）

1. **壳层白名单**仍为同文件串行片段约束（SA-004）；Orchestrator 须按 §3.2 键名核对，非方法级双写散文。
2. **603 条件 migration glob** 与 602 同前缀串行；交付说明须钉死实际新增脚本文件名。
3. **SNAP 门禁**须在派发前满足；与本角色可调度性判定正交。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （本轮无新增） | — | — | — | — |

## 已关闭 ISSUE（本角色）

| id | severity | 关闭依据 |
|---|---|---|
| ISSUE-PP-WSC5-R1-001 | P1 | PLAN-WSC-5.2 方案 (A) 文件级互斥；见 §1.1 |
| ISSUE-PP-WSC5-R1-002 | P1 | CatalogBrowsePage / L2 独立类 / useCanWrite 三条件齐；见 §1.2 |

## 决策

`APPROVE` — DAG **无环**且与 dependsOn / 五波次一致；**无同波并行对**；R1 本角色两条 P1 的 `closeWhen` **均已满足**并关闭；契约与 E2E 唯一写所有权清晰；字面 writeSet 可机械 scope-check。本角色认定 PLAN-WSC-5.2 在**并行/波次可调度性**维度可批。

本角色 **不** 代批其他委员会角色，**不** 代关他人 ISSUE，**不** 将本文件视为计划已全局 `APPROVED`，**不** 写入 `ai/runs/**`。

- closedIssueCount: **2**
- openIssueCount: **0**
