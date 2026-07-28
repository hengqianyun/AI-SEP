# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-R1-parallelPlanner
planId: PLAN-WSC-1.0
round: 1
role: parallelPlanner
snapshotIdAtReview: SNAP-WSC-001
decision: APPROVE
summary: |
  对 PLAN-WSC-1.0 做可调度性评审：DAG 无环；W3 同波 writeSet（overview∥catalog 浏览∥chain）无路径交集；
  依赖边完整且 005 显式 dependsOn [004, 006]；共享契约/初版 Flyway/根路由唯一所有者均为 TASK-WSC-001。
  波次划分与 RULE-PROJECT-LAYOUT / modules.yaml（SHELL/RBAC/OVW/CAT/CHAIN）边界一致；
  005 与 006 因存证同事务语义冲突已强制串行，符合并行判定规则。计划可调度，批准进入后续共识。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | 通过 |
| 同波 writeSet 不相交 | 通过（W3） |
| 依赖完整性 | 通过 |
| 契约唯一所有权 | 通过（001） |
| 005 → 004+006 | 通过 |
| 与 modules.yaml 对齐 | 通过 |
| 语义冲突串行 | 通过（005∥006 禁止） |

## 1. DAG 无环校验

边集合：

| from | to |
|---|---|
| TASK-WSC-001 | TASK-WSC-002 |
| TASK-WSC-002 | TASK-WSC-003 |
| TASK-WSC-002 | TASK-WSC-004 |
| TASK-WSC-002 | TASK-WSC-006 |
| TASK-WSC-004 | TASK-WSC-005 |
| TASK-WSC-006 | TASK-WSC-005 |

拓扑序：`001 → 002 → {003, 004, 006} → 005`。全部边由早层指向晚层，**无回边、无环**。

## 2. 依赖核对

| taskId | dependsOn（计划声明） | 判定 |
|---|---|---|
| TASK-WSC-001 | `[]` | 通过 — 契约前置无上游 |
| TASK-WSC-002 | `[TASK-WSC-001]` | 通过 |
| TASK-WSC-003 | `[TASK-WSC-002]` | 通过 |
| TASK-WSC-004 | `[TASK-WSC-002]` | 通过 |
| TASK-WSC-006 | `[TASK-WSC-002]` | 通过 |
| TASK-WSC-005 | `[TASK-WSC-004, TASK-WSC-006]` | 通过 — **显式依赖 004+006** |

专项：**TASK-WSC-005 依赖 TASK-WSC-004 与 TASK-WSC-006** 已在任务包与 §5.1 图中双重声明；与 REQ-CAT-005 对存证适配的运行时依赖一致。

## 3. 同波 writeSet 校验

| 波次 | 任务 | writeSet 核心路径 | 交集 |
|---|---|---|---|
| W1 | 001 | `frontend/` 脚手架、`backend/` 脚手架、`contracts/**`、初版 migration、根路由、`tests/` 框架 | 单任务 |
| W2 | 002 | `features/auth/**`、`features/shell/**`、`layouts/**`、`**/rbac/**`、`**/security/**` | 单任务 |
| W3 | 003 ∥ 004 ∥ 006 | `features/overview/**`+`**/overview/**` ∥ `features/catalog/**`+`**/catalog/**`（浏览）∥ `features/chain/**`+`**/chain/**` | **空集** |
| W4 | 005 | `features/catalog/**`+`**/catalog/**`（详情/写/分类） | 单任务；依赖 004/006 已 VERIFIED |
| W5 | 证据收敛 | 禁止改业务源码（缺陷回退除外） | N/A |

W3 模块码互斥：OVW ∥ CAT ∥ CHAIN，与 `modules.yaml` 一致。

说明：004 与 005 路径均属 CAT，但分属 W3/W4 且 005 dependsOn 004，**非同波冲突**；004 `allowModify` 已限制不得填充写逻辑，与 005 串行交接可接受。

## 4. 契约唯一所有权

| 共享制品 | 唯一写任务 | 后继权限 | 判定 |
|---|---|---|---|
| `contracts/openapi/**` | TASK-WSC-001 | 只读 | 通过 |
| `contracts/rbac/**` | TASK-WSC-001 | 只读 | 通过 |
| `contracts/chain/**` | TASK-WSC-001 | 只读 | 通过 |
| Flyway 初版 | TASK-WSC-001 | 只读；增量须新任务串行 | 通过 |
| `frontend/src/router/**` 根清单 | TASK-WSC-001 | 只读（扩展点除外须声明） | 通过 |
| 工程脚手架 | TASK-WSC-001 | 不得擅自改约定 | 通过 |

002..006 均 `denyModify: contracts/**` 与 `**/db/migration/**`（005 增量迁移须 Orchestrator 串行批准），符合「公共契约由独立前置任务拥有」。

## 5. 并行判定补充

| 规则 | 证据 | 判定 |
|---|---|---|
| 依赖满足方可并行 | W3 启动条件 = 002 VERIFIED | 通过 |
| 写集不相交 | §3 | 通过 |
| 不稳定读集 | W3 均以 001 冻结契约/schema 为权威；006 对 catalog 实现标注「若需关联」，可按契约消费，不构成对 004 写中制品的硬依赖 | 通过 |
| 共享注册表/迁移串行 | 初版仅 001；增量禁止并行波次 | 通过 |
| 语义冲突强制串行 | 005 与 006 同事务耦合 → 不同波；005 不得改 `**/chain/**` | 通过 |

合并顺序：由 DAG + §5.2 波次决定；W3 建议合入序 `003 → 004 → 006`，且不得与 005 并行合入。不以完成速度为准。

## 6. 任务完备性（可调度字段）

每个实现任务具备：`dependsOn`、`readSet`、`writeSet`、`allowModify`/`denyModify`、`acceptance`、`testScope`。审查/测试按波次门禁执行，不另列 TASK，可接受。

## ISSUE 表

（无）本轮并行规划维度无必须关闭的异议。

## 决策

`APPROVE` — 计划可调度；DAG/写集/依赖/契约所有权/005→004+006 均通过校验。
