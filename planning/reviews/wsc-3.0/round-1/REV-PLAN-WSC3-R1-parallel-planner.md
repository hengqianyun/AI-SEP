# Round 1 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC3-R1-parallelPlanner
planId: PLAN-WSC-3.0
round: 1
role: parallelPlanner
actorInstance: parallel-planner-wsc-003-r1
snapshotIdAtReview: SNAP-WSC-003
decision: REQUEST_CHANGES
summary: |
  对照 PLAN-WSC-3.0 §4/§5：DAG 无环；dependsOn 与 mermaid 一致；声明并行对
  202∥203、204∥205 的 writeSet 字面路径前缀可证明互斥。契约/api/backend/sql
  全任务 deny，无共享注册表/迁移并行写。但 TASK-WSC-206 写集含非路径条件句
  （feature 内 data-testid/aria「最小触碰」），与 §1.4「字面路径 scope-check」
  不可机械对齐；另 §5「四互斥最大并行」隐含 203∥204，而 browse 编译期依赖
  ImportDialog（204 writeSet），构成不稳定读交叉。关闭下列 ISSUE 后再批本角色。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | 通过 |
| dependsOn ↔ mermaid 一致 | 通过 |
| 202 ∥ 203 writeSet 互斥可证明 | 通过 |
| 204 ∥ 205 writeSet 互斥可证明 | 通过 |
| 四任务最大并行（含 203∥204）不稳定读 | **未通过** — 见 ISSUE-PP-WSC3-R1-002 |
| 206 最小触碰可调度 / 字面写集 | **未通过** — 见 ISSUE-PP-WSC3-R1-001 |
| 契约/api/backend/sql 无写任务 | 通过（全 deny） |
| styles/theme/shell 唯一写 | 通过（201） |
| 任务包可调度字段完备（本 plan 正文） | 通过（201..206 均内联） |
| mergeAfter / 合入序 | 部分可接受 — 波次 + 并行对合入约束已有；206 例外写路径未闭合前不足 |

## 1. DAG 无环校验

边集合（§4 `dependsOn` + §5 mermaid）：

| from | to |
|---|---|
| TASK-WSC-201 | TASK-WSC-202 |
| TASK-WSC-201 | TASK-WSC-203 |
| TASK-WSC-201 | TASK-WSC-204 |
| TASK-WSC-201 | TASK-WSC-205 |
| TASK-WSC-202 | TASK-WSC-206 |
| TASK-WSC-203 | TASK-WSC-206 |
| TASK-WSC-204 | TASK-WSC-206 |
| TASK-WSC-205 | TASK-WSC-206 |

拓扑序：`201 → {202, 203, 204, 205} → 206`。全部边由早层指向晚层，**无回边、无环**。

## 2. 依赖核对

| taskId | dependsOn（§4 正文） | 与 mermaid | 判定 |
|---|---|---|---|
| TASK-WSC-201 | `[]` | 源点 | 通过 |
| TASK-WSC-202 | `[TASK-WSC-201]` | 一致 | 通过 |
| TASK-WSC-203 | `[TASK-WSC-201]` | 一致 | 通过 |
| TASK-WSC-204 | `[TASK-WSC-201]` | 一致 | 通过 |
| TASK-WSC-205 | `[TASK-WSC-201]` | 一致 | 通过 |
| TASK-WSC-206 | `[TASK-WSC-201..205]` | 四入边一致 | 通过 |

专项：201 独占令牌/主题/壳层后再派页面任务，符合公共样式前置；206 汇合全部实现任务后做证据与 E2E，符合发布闸门。

## 3. 并行写集互斥证明

### 3.1 202 ∥ 203（W2）

| 任务 | writeSet 核心字面前缀（§4） |
|---|---|
| 202 | `frontend/src/features/overview/**`；`frontend/src/features/auth/views/**` |
| 203 | `frontend/src/features/catalog/browse/**` |

路径前缀交集：**空集**。相互 `denyModify` 覆盖对方核心目录。仓库现状下 `overview` / `auth/views` / `catalog/browse` 为分立目录，无共享父文件落在双方 writeSet。**可证明互斥。**

### 3.2 204 ∥ 205（W3）

| 任务 | writeSet 核心字面前缀（§4） |
|---|---|
| 204 | `frontend/src/features/catalog/maintenance/**`；`.../admin/**`；`.../import/**` |
| 205 | `frontend/src/features/catalog/detail/**`；`.../editor/**`；`frontend/src/features/chain/**` |

路径前缀交集：**空集**。相互 `denyModify` 覆盖 browse 与对方子树。**可证明互斥。**

### 3.3 四任务最大并行（计划声明）

| 任务 | 核心写前缀 |
|---|---|
| 202 | overview；auth/views |
| 203 | catalog/browse |
| 204 | catalog/{maintenance,admin,import} |
| 205 | catalog/{detail,editor}；chain |

四者两两路径交集为空（写集层面）。但 **203 对 204 存在编译期读边**：`CatalogBrowsePage.vue` import `@/features/catalog/import/ImportDialog.vue`（位于 204 writeSet）。在「W3 可与 W2 重叠」且「允许最大并行」下，203∥204 违反本角色「不稳定读集不交叉」判定（见 ISSUE-PP-WSC3-R1-002）。**写集互斥 ≠ 可并行。**

### 3.4 单波 / 串行写所有权

| 资源 | 唯一写任务 | 判定 |
|---|---|---|
| `frontend/src/styles/**`、`theme/**`、`layouts/**`、`features/shell/**`、`App.vue`、`main.ts` | 201 | 通过 |
| `tests/e2e/**` | 206 | 通过（201..205 deny） |
| `contracts/**`、`frontend/src/api/**`、`backend/**`、`**/sql/**` | 无 | 通过 |

## 4. 波次、启动条件与合入顺序

| 波次 | 计划声明 | 判定 |
|---|---|---|
| W1 | 201；计划 APPROVED 后派发 | 通过 |
| W2 | 202∥203；201 VERIFIED | 写集通过；可调度 |
| W3 | 204∥205；201 VERIFIED；可与 W2 重叠 | 204∥205 写集通过；与 W2 重叠时须处理 203∥204（ISSUE-002） |
| W4 | 206；201..205 均 VERIFIED | 启动条件通过；206 写集字面边界未闭合（ISSUE-001） |

合入约束（已写明、本角色认可的部分）：

- 禁止单 PR 混写多任务路径；禁止跨任务 squash
- 建议按页面合入
- 合并顺序服从 DAG/波次，不以完成速度为准

缺口：

- 无任务级 `mergeAfter` 字段——在波次表完整时历史可接受，**不单独升格 P1**
- 206 对已 VERIFIED 的 20x 源码「最小触碰」未定义合入后是否触发范围复检 / 如何与字面 scope-check 共存（并入 ISSUE-001）

## 5. TASK-WSC-206 最小触碰规则

| 检查项 | 计划现状 | 判定 |
|---|---|---|
| 证据与 E2E 路径 | `ux-gap-checklist` / `ux-walkthrough/**` / `tests/e2e/**` / reports | 清晰、可字面校验 |
| feature 源码例外 | 「允许最小范围触碰各 feature 内 data-testid / aria」且「须在交付说明列出文件」 | **非路径 glob**；无法仅凭 writeSet 字面做 §1.4 scope-check |
| 禁止视觉大改 | 有原则句，无 diff 形态约束（属性级 vs 模板/样式） | 不可机械执行 |
| denyModify | 禁 styles/theme/api/backend 等；**未**将 `frontend/src/features/**` 标为默认 deny + 例外 | 与条件写句冲突，边界模糊 |

结论：206 作为唯一汇合与发布闸门任务，其「最小触碰」必须变成可调度字段，否则 Orchestrator 只能主观放行或一律拒收 feature diff。

## 6. 任务完备性（可调度字段）

| taskId | dependsOn | writeSet | denyModify | acceptance/testScope | 判定 |
|---|---|---|---|---|---|
| 201 | 有 | 有 | 有 | 有 | 完备 |
| 202 | 有 | 有 | 有 | 有 | 完备 |
| 203 | 有 | 有 | 有 | 有 | 完备 |
| 204 | 有 | 有 | 有 | 有 | 完备 |
| 205 | 有 | 有 | 有 | 有 | 完备 |
| 206 | 有 | 有（含非路径条件句） | 有 | 有 | **写集不可机械 scope-check** |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PP-WSC3-R1-001 | P1 | PLAN-WSC-3.0 §4 TASK-WSC-206 `writeSet` 末条为条件散文（「若稳定选择器必须微调…允许最小范围触碰各 feature 内 data-testid / aria」），不是可匹配路径；与同计划 §1.4「按各任务 writeSet **字面路径**做 scope-check」冲突。`denyModify` 亦未默认禁止 `frontend/src/features/**`。并行规划完成条件要求写集可机械判定。 | 修订 206 写集边界，**二选一**：(A) 删除对 `frontend/src/features/**` 的触碰例外，E2E/选择器适配仅允许改 `tests/e2e/**`（及已列证据路径）；或 (B) 增加可机械匹配的条件写集前缀（如 `frontend/src/features/**/*.{vue,ts}`），并同时写明 `allowModify`/`denyModify`：仅允许增删改 `data-testid` 与 `aria-*` 属性、禁止改 style/class/业务逻辑/模板结构；交付物强制 `touched-files` 清单；§1.4 或 206 正文写明 Orchestrator 校验方式（属性级 diff 门禁或记名人工例外 gate）。关闭后方可对本角色再批。 | REQ-UX-011, REQ-UX-009, REQ-UX-010 |
| ISSUE-PP-WSC3-R1-002 | P2 | §5 声明「202∥203∥204∥205 … 四互斥写集 / 允许最大并行」。写集路径互斥成立，但仓库 `frontend/src/features/catalog/browse/CatalogBrowsePage.vue` 编译期 `import ImportDialog from '@/features/catalog/import/ImportDialog.vue'`，该文件属 204 writeSet。W2/W3 重叠时 203∥204 = 写 ImportDialog ∥ 读 ImportDialog，违反「不稳定读集不交叉」。203 正文仅约束「不得改 import/** 弹窗实现」，未排除与 204 并行。 | 在 §5 并行对/波次表中 **显式**处理 203 与 204，任选其一并保持 dependsOn↔波次一致：(1) 禁止 203∥204（最大并行改为不含该对；W3 与 W2 重叠时排除 203 未完成窗口）；或 (2) 203 `dependsOn` 增加 `TASK-WSC-204`；或 (3) 保留并行但增加合入约束「先合入 204 再合入/验 203」+ 冻结 ImportDialog 对外 props/events 契约（计划正文写明冻结面）。 | REQ-UX-004, REQ-UX-006 |

## 决策

`REQUEST_CHANGES` — DAG 无环；**202∥203、204∥205 写集互斥可证明**；201 独占全局样式、206 独占 E2E/证据主路径的方向正确。但 **206 最小触碰规则尚未可调度（P1）**，且 **最大四并行隐含 203∥204 不稳定读（P2）**。本角色不代批；关闭 ISSUE 并修订候选计划后，再调度本角色 Round 复评。

开放 ISSUE：**2**（P1×1，P2×1）。
