# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC3-R1-solutionArchitect
planId: PLAN-WSC-3.0
round: 1
role: solutionArchitect
actorInstance: solution-architect-wsc-003-r1
snapshotIdAtReview: SNAP-WSC-003
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-003、原型 README、DEC-WSC-005 与 RULE-PROJECT-LAYOUT，评估 PLAN-WSC-3.0 的前端架构边界：
  contracts/backend 默认 deny 在路径级可执行（writeSet 未纳入 + 全任务 denyModify + §8.4 发布门禁）；
  201..205 写集按 feature 子路径互斥、DAG 无环、令牌所有权归 201 的模型成立。
  但存在两处阻塞调度的架构缺口：(1) §2.2 AntDV「优先覆写」与仓库现状（依赖已装、frontend/src 零引用、UI 为自研 DOM/CSS）脱节，未定义控件基座与并行引入规则，易导致半 Ant 半自研分叉；
  (2) TASK-206 对 feature 内 data-testid 的散文例外未进入字面 writeSet，与 §1.4「按 writeSet 字面路径 scope-check」冲突。
  本轮 REQUEST_CHANGES；不代写他角色结论。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 前端令牌/全局样式重构边界 | 条件通过 — §2.3 / TASK-201 独占 `styles/**`+`theme/**`+挂载点；后继只读变量 + HOTFIX 回流路径清晰。须与 AntDV 基座策略一并闭合（见 ISSUE-SA-R1-001） |
| contracts / backend 默认 deny 可执行性 | 通过（路径级）— 全任务 `denyModify` 含 `contracts/**`、`frontend/src/api/**`、`backend/**`、`**/sql/**`；无任务 writeSet 纳入；§8.4 要求 Run diff 不含契约/后端。语义级「行为不回退」仍依赖各任务 acceptance + CR，不构成本角色路径 deny 失效 |
| 任务写集与模块边界 | 部分通过 — 201..205 与 LAYOUT 的 shell/overview/catalog/*/chain 子路径对齐且并行对互斥可检；206 testid 例外破坏字面 scope-check（ISSUE-SA-R1-002）；`router/**` 未写入 201 亦未显式冻结声明（见观察） |
| AntDV 覆写策略风险 | 不通过 — §2.2 / OQ-UX-002 假定 AntDV 为主控件层，与当前实现不符（ISSUE-SA-R1-001） |
| DEC-WSC-005 对齐 | 通过 — 本计划不触碰后端骨架/SQL；deny backend 与 DEC「禁止静默混用迁移权威」无冲突 |
| DAG / 并行写集 | 通过 — `201 → {202∥203∥204∥205} → 206` 无环；并行对写集无交集 |

## 架构抽查（证据）

### 令牌重构边界

- 计划 §2.1 令牌最小集与原型 `design/prototypes/wsc-v1.1/index.html` `:root` 一致；视觉权威与 SNAP-WSC-003 / 原型 README 对齐。
- 共享样式唯一写任务 = TASK-WSC-201；202..205 `denyModify` `styles/**`/`theme/**`/`App.vue`/`main.ts`/`layouts/**`/`shell/**`；缺口回流 201-HOTFIX — **可调度**。
- 现状：`App.vue` 内联 `:root`（非原型色板）；仓库无 `frontend/src/styles/**`、`theme/**`。由 201 新建并迁移挂载点，与 writeSet 一致。

### contracts / backend deny

- 路径闸门：字面 writeSet 均不包含 `contracts/**` / `backend/**` / `frontend/src/api/**`；与 SNAP-WSC-003 Out of Scope「默认不改契约/后端」一致。
- 扩 scope 须 PO+新快照/DEC（§1.1）— 符合「不可静默扩大」。
- DEC-WSC-005 仅约束后端栈；本 UX Run 全任务 `denyModify backend/**`，不引入第二套迁移权威。

### 写集与模块边界

| 任务 | writeSet 核心 | 与仓库路径 |
|---|---|---|
| 201 | styles/theme/App/main/layouts/shell/PlaceholderView（+必要构建配置） | layouts/shell/views 已存在；styles/theme 待建 |
| 202 | overview/** + auth/views/** | 存在；鉴权逻辑默认 deny store/composables/api |
| 203 | catalog/browse/** | 存在；ImportDialog 仅引用 import/**（写归 204） |
| 204 | maintenance/** ∥ admin/** ∥ import/** | 存在；与 203/205 无交集 |
| 205 | detail/** ∥ editor/** ∥ chain/** | 存在 |
| 206 | Run 证据 + tests/e2e/** +（叙述）feature testid | testid 例外未字面化 → ISSUE-SA-R1-002 |

并行：`202∥203`、`204∥205`（及四任务最大并行）写集互斥可自本计划单文件核对。

### AntDV / 控件基座

- `frontend/package.json` 声明 `ant-design-vue@^4.2.6` 与 `@ant-design/icons-vue`。
- `frontend/src/**` 检索：无 `ant-design-vue` / `ConfigProvider` / `a-*` 组件引用；`main.ts` 未 `app.use(Antd)`；主路径页（如 Overview、CatalogBrowse、ImportDialog）为自研 markup + scoped/页面 CSS。
- 因此 §2.2「优先 ConfigProvider/主题 token 覆写」在当前架构下**没有可覆写的 AntDV 控件树**；若不先定义基座策略，并行页任务可能各自引入 `a-table`/`a-modal` 或继续加深自研 CSS，放大计划 §7「半 Ant 半原型」风险。

### LAYOUT 观察（非独立 ISSUE）

- `RULE-PROJECT-LAYOUT` SHELL 可写含 `frontend/src/router/**`；本计划 201 writeSet 未含 router，亦未在全任务 `denyModify` 中显式冻结。按 §1.4 字面 writeSet scope-check，router 实际被冻结（无人可写）。若 UX 不改路由可接受，建议在 §2.3 或 201 `denyModify` 旁注「本 Run router 只读冻结」，避免与 LAYOUT 壳层所有权误读。
- `styles/**` / `theme/**` 为本计划新增共享边界，LAYOUT 表尚未列出；不阻塞本计划，建议后续由 techLead 补 LAYOUT（非本轮关闭条件）。

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | PLAN-WSC-3.0 §2.2 / OQ-UX-002 将「ConfigProvider + AntDV 主题/局部覆写」列为默认路径，并禁止未映射令牌的默认皮肤冒充对齐；但仓库 `frontend/src` 当前零 AntDV 引用，UI 为自研 DOM/CSS（例：`ImportDialog.vue` 自研 overlay、`OverviewPage.vue`/`CatalogBrowsePage.vue` 无 antd 导入），仅 `package.json` 声明依赖。策略与实现基座不匹配，且未约束 202..205 是否/何时引入 AntDV 控件，与 §7「半 Ant 半原型」风险直接相关，也削弱 201 作为唯一全局主题所有权的可执行性。 | 在计划 §2（建议 §2.2）增加**控件基座与 AntDV 边界**明文，三选一并贯彻到 201 acceptance / 202..205 deny 或交付约束：**(A) CSS-token-first**：以自研 DOM + §2.1 令牌为默认；AntDV 仅当任务交付「替换清单」列明后局部引入，且必须消费 201 令牌/theme，禁止未挂载主题的裸用；**(B) AntDV-adoption**：201 负责挂载 `ConfigProvider`/全局样式与允许使用的控件基线，后继任务仅在该基线内覆写/使用，禁止并行私自引入第二套控件习惯；**(C) 混合但分所有权**：列出「允许 AntDV 的控件白名单 + 唯一引入任务」。关闭时须能从单文件计划字面执行 scope/CR 检查。 | REQ-UX-001, REQ-UX-002, OQ-UX-002 |
| ISSUE-SA-R1-002 | P1 | §1.4 / §5.1 要求 Orchestrator 按各任务 `writeSet` **字面路径**做 scope-check；TASK-WSC-206 `writeSet` 字面仅含 `ai/runs/RUN-WSC-003/ux-*`、`tests/e2e/**`（及报告路径），却用散文允许「最小范围触碰各 feature 内 `data-testid` / aria」。该例外不在字面 writeSet 内，会导致：要么 scope-check 拒合入（门禁假死），要么检查器放行叙述例外（写边界不可证明）。与 201..205 互斥写集模型冲突。 | 删除或改写 206 叙述性跨 feature 写例外，改为可检方案之一：**(a)** 选择器变更回流对应 20x 任务（或 Orchestrator 插入串行 `TASK-WSC-20x-HOTFIX`，writeSet=该 feature 路径）；**(b)** 若必须由 206 改 testid，则在 206 `writeSet` 增加**显式路径 glob/文件清单**（可先占位「交付前锁定文件列表」），并 `denyModify` 视觉大改（styles/theme 仍禁）；禁止仅靠散文绕过字面 scope-check。 | REQ-UX-011, OQ-UX-004 |

## 决策

`REQUEST_CHANGES` — 解决方案架构维度未关闭 ISSUE：**2**（均为 P1）。

路径级 contracts/backend deny、令牌所有权归 201、并行写集互斥与 DAG **可接受**；在 ISSUE-SA-R1-001 / 002 关闭前，不建议按本候选稿派发开发波次。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-003-r1`；不代写其他委员会角色结论。
