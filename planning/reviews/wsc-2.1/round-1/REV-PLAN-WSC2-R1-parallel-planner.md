# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC2-R1-parallelPlanner
planId: PLAN-WSC-2.1
round: 1
role: parallelPlanner
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-002 / RULE-PROJECT-LAYOUT，并在解析 basedOn=PLAN-WSC-2.0 后校验：
  DAG 无环；声明并行对（103∥104、105∥106）writeSet 路径无交集；契约/sql/api 唯一属 101；
  §6 已写清 106 最早启动（103 VERIFIED）且不得与 103 并行（吸收旧 ISSUE-PP-R1-001）。
  但本候选正文对 TASK-WSC-102..106 以「同 2.0」外置 dependsOn/writeSet/denyModify，
  违反本角色完成条件「每个任务具备依赖、文件边界、读写集」及 planId 单源 scope-check；
  另 106 可与未完成的 104 并行，波次表未显式校验 browse∥maintenance。须内联任务 DAG 字段并补并行对后再批。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | 通过（解析 2.0 依赖后） |
| dependsOn ↔ mermaid 一致 | 通过（解析后） |
| 声明同波 writeSet 不相交 | 通过（103∥104；105∥106） |
| 契约/迁移/api 唯一写 | 通过（101） |
| 任务包可调度字段完备（本 plan 正文） | **未通过** — 102..106 外置 |
| 隐含并行对（104∥106）写集声明 | **未通过** — 波次表未列 |
| mergeAfter / 合入序 | 部分 — 仅有波次建议，无任务级 `mergeAfter` |

## 1. DAG 无环校验

边集合（§6 mermaid + 2.1/2.0 `dependsOn` 解析）：

| from | to |
|---|---|
| TASK-WSC-101 | TASK-WSC-102 |
| TASK-WSC-102 | TASK-WSC-103 |
| TASK-WSC-102 | TASK-WSC-104 |
| TASK-WSC-103 | TASK-WSC-105 |
| TASK-WSC-104 | TASK-WSC-105 |
| TASK-WSC-103 | TASK-WSC-106 |
| TASK-WSC-105 | TASK-WSC-107 |

拓扑序：`101 → 102 → {103, 104} → 105 → 107`，且 `103 → 106`（可与 105 同层、可早于 104 完成）。全部边由早层指向晚层，**无回边、无环**。

## 2. 依赖核对

| taskId | dependsOn（权威来源） | 判定 |
|---|---|---|
| TASK-WSC-101 | `[]`（2.1 正文） | 通过 |
| TASK-WSC-102 | `[TASK-WSC-101]`（仅 2.0；2.1 写「同 2.0」） | 语义通过；**声明完备性见 ISSUE-PP-R1-001** |
| TASK-WSC-103 | `[TASK-WSC-102]`（仅 2.0） | 同上 |
| TASK-WSC-104 | `[TASK-WSC-102]`（仅 2.0） | 同上 |
| TASK-WSC-105 | `[TASK-WSC-103, TASK-WSC-104]`（仅 2.0） | 同上；与 mermaid 双入边一致 |
| TASK-WSC-106 | `[TASK-WSC-103]`（2.1 重申） | 通过 |
| TASK-WSC-107 | `[TASK-WSC-105]`（2.1 正文） | 通过 — 分类/浏览经 105 传递依赖 |

专项：107 不依赖 106 合理（导入 ≠ 目录关联）；101 独占契约后再派 102+ 符合「公共契约独立前置」。

## 3. 同波 / 可并行 writeSet 校验

解析 2.0 writeSet（2.1 对 102..106 未内联）后：

| 并行对 | writeSet 核心 | 交集 | 判定 |
|---|---|---|---|
| W3：103 ∥ 104 | `catalog/admin/**` ∥ `catalog/browse/**`（前后端对称） | 空 | 通过 |
| W4：105 ∥ 106 | `detail\|editor/**` + `chain/**` ∥ `maintenance/**` | 空 | 通过 |
| **隐含：104 ∥ 106** | `browse/**` ∥ `maintenance/**` | 空（路径） | 路径可通过，但 **§6 未声明该并行对** → ISSUE-PP-R1-002 |

单波任务（W1/W2/W5 实现段）：无同波写冲突。共享 `contracts/**`、`frontend/src/api/**`、`**/sql/**` 仅 101 可写；102..107 `denyModify`（2.0/2.1）禁止契约与迁移并行写，符合 LAYOUT 串行规则。

语义：105（编辑/上链）与 106（关联维护）路径互斥且分属不同 API 面；OQ-V11-003 同源实体不强制同波串行（与 V1.0「编辑∥存证同事务」不同）。维持 105∥106，但合入禁止混 PR（§6 已写）。

## 4. 波次与启动条件

| 波次 | 计划声明 | 判定 |
|---|---|---|
| W1 | 101；SNAP APPROVED | 通过 |
| W2 | 102；101 VERIFIED | 通过 |
| W3 | 103∥104；102 VERIFIED | 通过 |
| W4 | 105：103+104 VERIFIED；106：仅 103 VERIFIED、不得∥103；105∥106 当 writeSet 无交集 | 通过（旧 PP 异议已吸收） |
| W5 | 107 待 105 VERIFIED；E2E 待 101..107 VERIFIED | 通过 |

缺口：106 最早可与 **仍在 W3 的 104** 重叠，波次桶未写明该窗口的互斥校验与合入约束（见 ISSUE-PP-R1-002）。

## 5. 任务完备性（可调度字段）

| taskId | dependsOn | writeSet | denyModify | 本 plan 正文 |
|---|---|---|---|---|
| 101 | 有 | 有（已收敛） | 有 | 完备 |
| 102 | 无（外置） | 无（外置） | 无（外置） | **不完备** |
| 103 | 无（外置） | 无（外置） | 无（外置） | **不完备** |
| 104 | 无（外置） | 无（外置） | 部分（仅追加 testScope） | **不完备** |
| 105 | 无（外置） | 无（外置） | 无（外置） | **不完备** |
| 106 | 有（重申） | 无（外置） | 无（外置） | **不完备** |
| 107 | 有 | 有 | 有 | 完备 |

`readSet` / `mergeAfter`：101 外大多缺失（2.0 亦然）；合入仅靠 §6 建议。不升格为本轮 P1，但批准前建议随任务包落盘补齐。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PP-R1-001 | P1 | PLAN-WSC-2.1 §5：TASK-WSC-102/103/105 仅「同 2.0」；104/106 未内联 writeSet/denyModify。parallelPlanner 完成条件与 §1.4「按各任务 writeSet 字面路径 scope-check」要求本 `planId` 可机械读取依赖与写集；跨计划引用使 CANDIDATE 非单源，且 101 writeSet 已相对 2.0 变更，易漏检后继边界。 | 在 PLAN-WSC-2.1（或由其引用的 `planning/tasks/TASK-WSC-102..106.md`）**全文内联**各任务 `dependsOn`、`writeSet`、`denyModify`（及既有 acceptance/testScope）；评审者无需打开 PLAN-WSC-2.0 即可复算 DAG 与并行写集。 | REQ-SHELL-001, REQ-RBAC-001, REQ-CAT-001..007 |
| ISSUE-PP-R1-002 | P2 | §6 允许 106 在「仅 103 VERIFIED」后启动，故可与未完成的 104 并行；波次表只校验 103∥104 与 105∥106，未写 104∥106 的 writeSet 互斥与合入约束。 | §6 波次/并行表显式增加：`104 ∥ 106` 当且仅当 writeSet 无交集（browse ∥ maintenance）；合入禁止单 PR 混写两任务路径（可与现有 105∥106 条合并表述）。 | REQ-CAT-001, REQ-CAT-007 |

## 决策

`REQUEST_CHANGES` — DAG/声明并行写集/契约所有权在解析 2.0 后可调度，但本候选对 102..106 的可调度字段外置（P1），且隐含 104∥106 窗口未写入波次互斥（P2）。关闭 ISSUE 并内联后再进入本角色 APPROVE。
