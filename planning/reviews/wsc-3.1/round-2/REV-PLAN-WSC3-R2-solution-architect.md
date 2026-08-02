# Round 2 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC3-R2-solutionArchitect
planId: PLAN-WSC-3.1
round: 2
role: solutionArchitect
actorInstance: solution-architect-wsc-003-r2
snapshotIdAtReview: SNAP-WSC-003
decision: APPROVE
summary: |
  对照本角色 Round 1（PLAN-WSC-3.0 / REV-PLAN-WSC3-R1-solution-architect）两条 P1 ISSUE 的 closeWhen，
  逐条核验 PLAN-WSC-3.1：§2.2 已选定并贯彻 CSS-token-first（方案 A），201 acceptance 与 202..205
  交付约束可单文件执行；TASK-WSC-206 已删除跨 feature testid 散文例外，denyModify 含
  frontend/src/features/**，选择器变更回流 20x / TASK-WSC-20x-HOTFIX。ISSUE-SA-R1-001/002
  closeWhen 均已满足，本轮关闭；无新架构异议。不代写他角色结论。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 控件基座 / AntDV 边界（R1-001） | 通过 — §2.2 选定 CSS-token-first；OQ-UX-002 / 201 acceptance / 202 替换清单约束可检 |
| 206 写边界字面化（R1-002） | 通过 — writeSet 仅证据 + `tests/e2e/**`；`denyModify` 含 `frontend/src/features/**`；HOTFIX 回流 |
| 令牌/全局样式所有权 | 通过 — §2.3 / 201 独占 styles/theme/App/main/layouts/shell；后继 deny + 201-HOTFIX |
| contracts / backend deny | 通过 — 全任务 deny；无 writeSet 纳入；§8 发布门禁 |
| DAG / 并行写集 | 通过 — 无环；203∥204 显式禁止；其余并行写集互斥 |
| router 冻结（R1 观察） | 观察已吸收 — §1.4 / §2.3 本 Run `router/**` 只读冻结；非独立 ISSUE |

## R1 ISSUE closeWhen 核对（仅本角色）

| id | severity | closeWhen 要点 | 3.1 证据位置 | 本轮判定 |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | §2 明文控件基座与 AntDV 边界；三选一并贯彻到 201 acceptance / 202..205 约束；可单文件 scope/CR | **方案 A**：§2.2「选定：CSS-token-first」；默认自研 DOM+§2.1 令牌；AntDV 仅经「替换清单」且须消费 201 令牌、禁裸默认皮肤；禁止 202..205 第二套控件习惯；§1.2 OQ-UX-002 同步；201 objective/acceptance「符合 §2.2」；202 acceptance「局部引入须有替换清单且消费 201 令牌」 | **closeWhen 已满足 → 关闭** |
| ISSUE-SA-R1-002 | P1 | 删除 206 跨 feature 散文写例外；**(a)** 选择器回流 20x / `TASK-WSC-20x-HOTFIX`，或 **(b)** 206 writeSet 显式路径 | **方案 a**：§4.206 writeSet 字面仅 `ux-*` 证据 + `tests/e2e/**`（及报告/config）；`denyModify` 含 `frontend/src/features/**`（含 data-testid/aria）；须改生产 DOM → 串行 `TASK-WSC-20x-HOTFIX`；§5.1.5 / W4 同口径；无散文扩写例外 | **closeWhen 已满足 → 关闭** |

### 逐条核验说明

**ISSUE-SA-R1-001**  
R1 closeWhen 要求在 §2 增加基座策略三选一。3.1 §2.2 明确选定 **(A) CSS-token-first**，并写明默认基座、AntDV 局部引入门禁、禁止并行第二套习惯、201 不强制全量挂载 AntDV。201 acceptance 要求符合 §2.2；202 要求替换清单+消费 201 令牌。可从单文件计划字面做 CR/scope 检查。**满足，关闭。**

**ISSUE-SA-R1-002**  
R1 closeWhen 要求去掉 206 叙述性跨 feature 写例外，改用可检方案。3.1 206 `writeSet` 无 feature 路径；`denyModify` 明确禁止改任何 `frontend/src/features/**`（含 testid/aria）；选择器适配优先改 `tests/e2e/**`，否则 Orchestrator 插入串行 `TASK-WSC-20x-HOTFIX`（writeSet=该 feature 字面路径）。与 §1.4 字面 scope-check 一致。**满足，关闭。**

## 架构抽查（3.1 相对 3.0）

- 控件基座与仓库现状对齐：零 AntDV 引用时默认自研 + 令牌；避免并行页任务各自引入 `a-table`/`a-modal` 造成半 Ant 半自研分叉（§7 风险行已挂钩 §2.2）。
- 206 写边界可证明：Orchestrator 可对字面 writeSet 拒合入 feature 改动；testid 变更有 HOTFIX 回流，不再依赖散文例外。
- 令牌所有权、contracts/backend deny、DAG 与并行互斥（含禁止 203∥204）在 3.1 仍成立；本轮不新开异议。

## ISSUE 表（本轮）

| id | status | note |
|---|---|---|
| ISSUE-SA-R1-001 | **CLOSED** | closeWhen 由 PLAN-WSC-3.1 §2.2 方案 A + 任务约束满足 |
| ISSUE-SA-R1-002 | **CLOSED** | closeWhen 由 PLAN-WSC-3.1 §4.206 / §5.1 方案 a 满足 |

本轮新增 ISSUE：（无）

## 决策

`APPROVE` — 解决方案架构维度剩余未关闭 ISSUE 数 = **0**（本角色 R1 两条均已关闭）；控件基座与 206 字面写边界可调度执行。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-003-r2`；**不**代写其他委员会角色结论，**不**伪造他角色 `APPROVE`。
