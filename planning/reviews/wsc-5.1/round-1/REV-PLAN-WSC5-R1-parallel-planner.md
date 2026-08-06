# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC5-R1-parallelPlanner
planId: PLAN-WSC-5.1
round: 1
role: parallelPlanner
actorInstance: parallel-planner-wsc-008-r1
snapshotIdAtReview: SNAP-WSC-005
decision: REQUEST_CHANGES
summary: |
  DAG 无环（601→602→603→604）；dependsOn 与 mermaid/§6 波次一致；声明无同波
  并行对，故无「假并行」写冲突。串行四波方向正确（browse/layout/RBAC/迁移交叉
  迫使串行）。但多处 writeSet/denyModify 依赖**方法级/片段级散文**切割同文件
  所有权（602↔603 的 RbacMatrix/common.security；603↔604 的 browse Controller /
  CatalogBrowsePage），与计划 §1.4「字面路径 scope-check」及本角色完成条件冲突，
  预落地混合改动会放大 VERIFIED 交接歧义。须修订为可机械判定的文件级独占写集
  后再对本角色复评。不代批他角色。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-5.1.md` |
| 快照 | `product/requirements/SNAP-WSC-005.md`（`IN_REVIEW`；派发门禁已在计划文首声明） |
| 角色装载 | `ai/agents/parallel-planner.md` |
| 模板 | `ai/schemas/templates/planning-review.md` |
| 实例 | `parallel-planner-wsc-008-r1` |
| 本角色权限 | 只读计划/产品/目录结构；本文件为评审落盘；**未**改计划/源码/`ai/runs/**` |

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | **通过** |
| dependsOn ↔ mermaid / §6 一致 | **通过** |
| 同波并行写集互斥 | **不适用（无同波并行对）** — 声明「禁止任意两两并行」成立 |
| 不稳定读交叉（并行窗口） | **通过** — 无并行窗口 |
| 共享迁移/安全注册表串行 | **方向通过** — 四波串行；但所有权散文见 ISSUE |
| 契约 / `frontend/src/api/**` 唯一写 | **通过**（601） |
| 字面 writeSet / scope-check 可调度 | **未通过** — 见 ISSUE-PP-WSC5-R1-001 / 002 |
| 预落地是否破坏并行假设 | **未破坏「无并行」假设**；但放大同文件散文切割风险 — 见 §5 |
| mergeAfter | **可接受** — §6 波次 + VERIFIED 链足够；无任务级 `mergeAfter` 不单独升格 |

## 1. DAG 无环校验

边集合（§5 `dependsOn` + §6 mermaid）：

| from | to |
|---|---|
| TASK-WSC-601 | TASK-WSC-602 |
| TASK-WSC-602 | TASK-WSC-603 |
| TASK-WSC-603 | TASK-WSC-604 |

拓扑序：`601 → 602 → 603 → 604`。全部边由早层指向晚层，**无回边、无环**。

## 2. 依赖核对

| taskId | dependsOn（§5） | 与 mermaid / §6 | 判定 |
|---|---|---|---|
| TASK-WSC-601 | `[]` | 源点；W1 | 通过 |
| TASK-WSC-602 | `[TASK-WSC-601]` | 一致；W2 需 601 VERIFIED | 通过 |
| TASK-WSC-603 | `[TASK-WSC-602]` | 一致；W3 需 602 VERIFIED（传递含 601） | 通过 |
| TASK-WSC-604 | `[TASK-WSC-603]` | 一致；W4 需 603 VERIFIED | 通过 |

专项：

- **601 独占公共契约前置**：`contracts/**` + `frontend/src/api/**`，符合「公共契约由独立前置任务拥有」。
- **603 dependsOn 602**：真登录 / `userId` / 会话角色稳定后再做 `create_by` 与产品写矩阵，语义合理。
- **604 dependsOn 603**：browse 页挂载座序图与「我的产品无图」同属 browse 面，串行避免同文件争用，合理。
- **无缺失边**：604 未直连 601，但经 603 传递，可调度。

## 3. 并行写集与禁并行声明

### 3.1 声明并行对

计划 §1.4 / §6：**串行四波；任意 601..604 两两禁止并行**。同波均为单任务。

| 对 | 计划声明 | 本角色判定 |
|---|---|---|
| 任意两两 ∥ | 禁止 | **成立** — `dependsOn` 链强制全序 |
| 601 ∥ 后继 | 禁止至 601 VERIFIED | **成立** |

**无同波并行对 ⇒ 不存在「写集相交却声称可并行」的假并行。** 就此点不升格 ISSUE。

### 3.2 串行共享路径（路径交集存在，须文件级可交接）

下列路径在**相邻任务** writeSet 中字面重叠或父子包含。串行本身允许交接，但交接边界须可机械判定：

| 路径 / 前缀 | 写序 | 计划现口径 | 判定 |
|---|---|---|---|
| `contracts/**`、`frontend/src/api/**` | 仅 601 | 后继 deny | **通过** |
| `**/sql/migration/**` | 602 →（条件）603 | 迁移串行正确；602 全 glob + 603 条件 glob | 残余：建议 603 钉死「仅新增 V* create_by」文件名模式 — **不单独升格**（见观察） |
| `WorkbenchLayout.vue`、`router/routes.ts` | 602 → 603 | 「仅改 my-products 片段」散文 | 串行可接受；片段级 — **观察** |
| `frontend/src/features/auth/**` ⊇ `useCanWrite.ts` | 602 宽写 ⊃ 603 点写 | 602 未 denyModify `useCanWrite` | 见 ISSUE-002 相关 |
| `common/security/**` / `RbacMatrix` / `WriteAuthorizationInterceptor` | 602 散文「部分写」↔ 603 显式写 | **不可机械 scope-check** | **ISSUE-001** |
| `controller/catalog/browse/**`、`service/catalog/browse/**`、`CatalogBrowsePage.vue` | 603 → 604 | 同文件方法/挂载片段散文切割 | **ISSUE-002** |

### 3.3 全局所有权矩阵（可闭合项）

| 资源 | 唯一写任务 | 判定 |
|---|---|---|
| `contracts/**`、`frontend/src/api/**` | 601 | 通过 |
| `sys_user` 迁移 / 用户 CRUD / RoleSwitcher | 602（意图） | 意图通过；security 矩阵例外见 ISSUE-001 |
| 我的产品 / 公共目录去写 / `create_by` 产品路径 | 603（意图） | 意图通过；browse 与 604 切割见 ISSUE-002 |
| `CirculationSeatMap.vue` | 604 | 通过（603 deny 该文件） |
| `frontend/src/styles/**` / `theme/**` | **无写任务** | 通过 |
| `ai/runs/**/state.yaml` / `events.jsonl` | **无写任务** | 通过 |

## 4. 波次可调度性

| 波次 | 任务 | 启动条件 | 判定 |
|---|---|---|---|
| W1 | 601 | SNAP 门禁（正式批准 **或** 委员会确认范围）+ 本计划 APPROVED 后派发 | 条件写明；**门禁未满足前不可派发** — 调度描述正确 |
| W2 | 602 | 601 VERIFIED | 通过 |
| W3 | 603 | 602 VERIFIED | 通过 |
| W4 | 604 | 603 VERIFIED | 通过 |

合入序服从全序 DAG，不以完成速度为准 — **通过**。  
无任务级 `mergeAfter` — 与既有 WSC 本角色口径一致，**不阻塞**。

任务字段（dependsOn / readSet / writeSet / denyModify / acceptance / testScope）四任务均内联 — **完备性通过**；问题在边界**可机械性**，非缺失字段。

## 5. 预落地与并行假设

| 检查 | 结论 |
|---|---|
| 计划是否假设可并行加速 | **否** — 显式串行四波 |
| §0.2 预落地对账是否要求按任务写集验收 | **是** — 不以「代码已在」代替证据 |
| 预落地是否迫使必须并行 | **否** |
| 预落地是否破坏可调度性 | **不破坏 DAG**；但工作树已可能在同一文件混入 602+603+604 职责，若继续用「同文件部分改」散文，VERIFIED 交接与字面 scope-check **更易失败** → 强化 ISSUE-001/002 的 closeWhen，**不**另立预落地 ISSUE |

## 6. 残余观察（非 ISSUE / 不单独阻塞）

1. **`**/sql/migration/**` 双任务 glob**：602 独占 `sys_user`、603 条件追加 `create_by` 的意图正确且已串行；建议 603 writeSet 改为可匹配的新增脚本命名约束，避免改写已 VERIFIED 的 V6。
2. **`WorkbenchLayout` / `routes` 片段散文**：全序串行下可交接；若修订写集时可顺带改为「602 denyModify 我的产品菜单键；603 denyModify 用户管理菜单键」之类路径外的属性级说明或拆分 composable。
3. **E2E 无独占写任务**：各任务 deny `tests/e2e/**`；§6.1 建议证据包 — 发布门禁依赖后续 hotfix/扩展波次时须另开任务写集，本角色不阻塞候选串行核。
4. **SNAP `IN_REVIEW`**：派发硬门禁，非 DAG 缺陷。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PP-WSC5-R1-001 | P1 | PLAN-WSC-5.1 §5 TASK-WSC-602 `writeSet` 含 `common/security/**`，并附散文：「RBAC 产品写规则以 603 为准时可只读，若预落地已改矩阵则对账后允许本任务写矩阵中与用户管理/登录相关部分，产品写最终以 603 acceptance 为准」。同计划 603 又显式写入 `RbacMatrix.java`、`WriteAuthorizationInterceptor.java`。同一路径被两任务声称可写，边界依赖自然语言「部分」，**无法**按 §1.4 做字面 scope-check；亦违反本角色「共享注册表须清晰串行交接 / 公共可变点单一所有者」口径。预落地已可能混改该矩阵，放大 602 VERIFIED 后仍留产品写漏洞、或 603 回滚 602 登录相关改动的风险。 | 修订 602/603 写集为**文件级互斥**，去掉「部分写矩阵」散文。任选其一并保持 dependsOn 串行：**(A)** 602 `writeSet` 仅列 `PasswordHasher` / `AuthAuditLogger`（及会话辅助，若有）等具名文件，并对 `RbacMatrix.java`、`WriteAuthorizationInterceptor.java` **denyModify**；603 **独占**上述两文件（及产品写矩阵测例）。**(B)** 602 独占整个 `common/security/**`（含矩阵中用户管理/登录码），603 **denyModify** `common/security/**`，产品写规则改由 602 acceptance 钉死并与 SNAP §4 对齐（则 603 不得再声称写 RbacMatrix）。关闭后方可对本角色再批。 | REQ-RBAC-001, REQ-USER-001, REQ-SHELL-001 |
| ISSUE-PP-WSC5-R1-002 | P1 | （1）603 `writeSet` 含 `frontend/src/features/catalog/browse/**`（仅散文禁止改 `CirculationSeatMap.vue`），字面包含 `CatalogBrowsePage.vue`；604 又显式写同一 `CatalogBrowsePage.vue`（「仅座序图挂载」散文）。（2）603 `denyModify` 对 `controller/catalog/browse/**` 写方法级例外：「允许为挂 mine 改同一 Controller，但不得改 L2 聚合；若冲突则…留给 604」。604 `writeSet` 亦含 `controller/catalog/browse/**` 与 `service/catalog/browse/**`。方法/挂载片段切割**不可**机械 scope-check，与 §1.4 及 WSC-3.0 本角色对散文写集的 P1 先例同型。另：602 `writeSet` `frontend/src/features/auth/**` 字面覆盖 603 的 `useCanWrite.ts`，602 未 denyModify 该文件，预落地对账时两任务可争同一 composable。 | 修订为**文件级**独占或可匹配前缀，去掉同文件方法/片段散文。须同时满足：**(1)** `CatalogBrowsePage.vue`：要么 603 denyModify 该文件、仅 604 写挂载/`showSeatMap`（603 的 mine/去写经其它文件或布局完成）；要么 603 独占该页、604 只写 `CirculationSeatMap.vue` 且通过只读 props/slot 约定由 603 在 W3 预留挂载点（计划写明冻结面）。**(2)** L2 API：将 `l2-distribution` 处理器/聚合抽到 604 独占类路径（或 603 denyModify 含 L2 的既有 Controller 文件，mine 放在不与 L2 同文件的路径）。**(3)** 602 `denyModify` 显式包含 `frontend/src/features/auth/composables/useCanWrite.ts`（及 603 产品写可见性相关 spec），或从 602 `writeSet` 缩小 `auth/**` 为不含该 composable 的具名列表。关闭后方可对本角色再批。 | REQ-CAT-009, REQ-CAT-010, REQ-CAT-001, REQ-RBAC-001 |

## 决策

`REQUEST_CHANGES` — DAG **无环**且与 dependsOn/波次一致；**无同波并行对**，假并行写冲突不成立；契约唯一所有权与迁移串行方向正确；预落地**未**推翻「全串行」假设。但 **ISSUE-PP-WSC5-R1-001（P1）** 与 **ISSUE-PP-WSC5-R1-002（P1）** 表明多处同文件散文切割使 Orchestrator **无法**字面 scope-check，预落下 VERIFIED 交接不可靠。请 Plan Editor 按 closeWhen 修订候选计划后，再调度本角色 Round 复评。

本角色 **不** 代批其他委员会角色，**不** 代关他人 ISSUE，**不** 将本文件视为计划已 `APPROVED`，**不** 写入 `ai/runs/**`。

开放 ISSUE（本角色本轮）：**2**（P1×2，P2×0）。
