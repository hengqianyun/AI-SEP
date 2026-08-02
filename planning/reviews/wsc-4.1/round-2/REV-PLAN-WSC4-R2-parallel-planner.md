# Round 2 Review — Parallel Planner

```yaml
reviewId: REV-PLAN-WSC4-R2-parallelPlanner
planId: PLAN-WSC-4.1
round: 2
role: parallelPlanner
actorInstance: parallel-planner-wsc-004-r2
snapshotIdAtReview: SNAP-WSC-004
decision: APPROVE
summary: |
  对照 PLAN-WSC-4.1 §5/§6/§1.7：DAG 无环（301→302∥303→304）；dependsOn 与
  mermaid 一致。唯一同波并行对 302∥303 的 writeSet 字面前缀（catalog/editor/**
  ∥ catalog/detail/**）可证明无交集；仓库现状无 editor↔detail 编译期互引。
  §1.7 选定方案 (a)：全任务显式 denyModify frontend/src/components/**，且无任
  何 writeSet 纳入该路径；W2 并行保持、不因共享组件改为串行。契约/api/后端
  import+typeSpecific/sql 唯一属 301；E2E 唯一属 304；fixtures/import/** 串行
  增补。本角色 R1（对 4.0）无开放 ISSUE。本角色 APPROVE；不代批他角色。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-4.1.md` |
| 快照 | SNAP-WSC-004 |
| 角色装载 | `ai/agents/parallel-planner.md` |
| 实例 | `parallel-planner-wsc-004-r2` |
| R1 对照 | `planning/reviews/wsc-4.0/round-1/REV-PLAN-WSC4-R1-parallel-planner.md`（本角色对 PLAN-WSC-4.0 已 `APPROVE`，开放 ISSUE **0**） |
| 谱系 | `basedOn`/`lineageFrom: PLAN-WSC-4.0`；本 planId 共识轮次 frontmatter `round: 1`（本文件为 RUN-WSC-004 修订候选上的独立评审落盘，路径 `wsc-4.1/round-2`） |
| 本角色权限 | 只读计划/产品/目录结构；本文件为评审落盘；**未**改计划/源码/`state` |

## 相对 4.0 的并行规划相关修订（抽查）

| 来源 | 本计划吸收 | 对本角色可调度性影响 |
|---|---|---|
| ISSUE-SA-R1-002 → §1.7 / §3.5 / 301..304 `denyModify` | 选定 **(a)**：302/303 **各自实现** OpenAPI UI；**禁止**本 Run 写 `frontend/src/components/**`；W2 `302 ∥ 303` **保持** | **增强可调度性声明**：消除「无合法共享组件写路径」歧义；并行对不降级为串行；与本角色 R1 残余观察 #1（共享目录无写任务）对齐并闭合 |
| ISSUE-SA-R1-001 → §3.3 方案 B | 双端解析 + 共享 fixture 关键字段；冲突以客户端 endpoints 为准 | **不改变** DAG/写集：fixture 仍由 301 拥有、302 只读对照；无新增并行写路径 |
| 其余 QA/API ISSUE | 主要扩 301 `testScope` / 304 E2E 钉死项 | **不改变** 波次与写集互斥 |

本角色 R1 **无**待确认 `closeWhen` 条目。

## 评审范围

| 维度 | 结论 |
|---|---|
| DAG 无环 | 通过 |
| dependsOn ↔ mermaid 一致 | 通过 |
| 302 ∥ 303 writeSet 互斥可证明 | 通过 |
| 302 ∥ 303 不稳定读交叉 | 通过（无交叉依赖） |
| 禁写 `frontend/src/components/**` | 通过（§1.7 + §3.5 + 301..304 均 deny；无 writeSet 纳入） |
| 禁止并行对（301∥后继；302∥304；303∥304） | 通过（dependsOn + §6 表） |
| 契约 / `frontend/src/api/**` / 后端 import+typeSpecific / sql 唯一写 | 通过（301） |
| E2E / 本轮 e2e specs 唯一写 | 通过（304） |
| styles / theme / shell / layouts / router 无写任务 | 通过 |
| 任务包可调度字段完备（本 plan 正文） | 通过（301..304 均内联） |
| mergeAfter / 合入序 | 可接受 — §6 波次 + 并行对合入约束足够调度；无任务级 `mergeAfter` 不阻塞 |

## 1. DAG 无环校验

边集合（§5 `dependsOn` + §6 mermaid）：

| from | to |
|---|---|
| TASK-WSC-301 | TASK-WSC-302 |
| TASK-WSC-301 | TASK-WSC-303 |
| TASK-WSC-302 | TASK-WSC-304 |
| TASK-WSC-303 | TASK-WSC-304 |

拓扑序：`301 → {302, 303} → 304`。全部边由早层指向晚层，**无回边、无环**。

## 2. 依赖核对

| taskId | dependsOn（§5 正文） | 与 mermaid | 判定 |
|---|---|---|---|
| TASK-WSC-301 | `[]` | 源点 | 通过 |
| TASK-WSC-302 | `[TASK-WSC-301]` | 一致 | 通过 |
| TASK-WSC-303 | `[TASK-WSC-301]` | 一致 | 通过 |
| TASK-WSC-304 | `[TASK-WSC-301, TASK-WSC-302, TASK-WSC-303]` | 三入边一致 | 通过 |

专项：

- **301 独占公共契约前置**：`contracts/**` + `frontend/src/api/**` + 后端 typeSpecific/导入解析，符合「公共契约由独立前置任务拥有」。
- **304 汇合后做导入 UI + E2E**：依赖 302/303 VERIFIED，避免在未稳定的编辑/详情 DOM 上写选择器与主路径证据，合理。
- **302 与 303 互不依赖**：同属 W2，仅共享对 301 的依赖；§1.7 明确不因共享组件改为串行 → **可并行**。

## 3. 并行写集互斥与禁写 `components/**`

### 3.1 禁写 `frontend/src/components/**`（硬门禁复核）

| 检查项 | 证据 | 判定 |
|---|---|---|
| 全局声明 | §1.4 / §1.7：本 Run **任何任务** writeSet/实际改动不得含该路径 | 通过 |
| 所有权矩阵 | §3.5：该路径 **无写任务**；全任务 denyModify | 通过 |
| 301 `denyModify` | 显式含 `frontend/src/components/**` | 通过 |
| 302 `denyModify` | 显式含；objective/acceptance 重申不抽到 `components/**` | 通过 |
| 303 `denyModify` | 显式含；不得改 editor「借用」组件 | 通过 |
| 304 `denyModify` | 显式含 | 通过 |
| 任一任务 `writeSet` 纳入该路径 | **无** | 通过 |
| W2 并行声明 | §6：`302 ∥ 303` 保持；「均不得写 `components/**`」 | 通过 |

仓库抽查：`frontend/src/components/**` 当前无文件（路径未作为既有共享落点）；`catalog/editor` 与 `catalog/detail` 仅依赖 `@/api/**` 与 auth，**无**对 `@/components` 或彼此 feature 的编译期 import。既有分树副本先例（如 `detail/components/SensitiveId.vue` vs browse 侧副本）与方案 (a) 一致。

**结论**：禁写闭合；实现期若违反 → Orchestrator 字面 scope-check 应拒；若需共享控件 → 须新 ISSUE/HOTFIX 扩写集或改 DAG，**不得**在本计划下静默写入。

### 3.2 302 ∥ 303（W2，唯一声明并行对）

| 任务 | writeSet 核心字面前缀（§5） |
|---|---|
| 302 | `frontend/src/features/catalog/editor/**`（及同树测例） |
| 303 | `frontend/src/features/catalog/detail/**`（及同树测例） |

路径前缀交集：**空集**。相互 `denyModify` 覆盖对方核心目录及 `import/**` / `browse/**` 等。  
仓库现状：`editor/` 与 `detail/` 为分立目录；**无** `editor`↔`detail` 编译期互引。**写集互斥可证明；不稳定读不交叉。**

语义：各自实现 OpenAPI UI（方案 a）允许信息层级对齐 SNAP/§1.5、容忍像素级漂移——**不**构成写冲突，亦**不**强制串行。

### 3.3 禁止并行对（计划已声明，本角色复核）

| 对 | 依据 | 判定 |
|---|---|---|
| 301 ∥ 302/303/304 | 后继 `dependsOn` 含 301；W1 单任务 | 禁止成立 |
| 302 ∥ 304 | 304 `dependsOn` 含 302 | 禁止成立 |
| 303 ∥ 304 | 304 `dependsOn` 含 303 | 禁止成立 |

### 3.4 串行共享路径（非并行冲突）

| 路径 | 写任务序 | 判定 |
|---|---|---|
| `tests/fixtures/import/**` | 301 → 304（304 声明增补 UI/E2E fixture；302 只读共享 OpenAPI fixture） | **串行**；304 启动条件含 301 VERIFIED，不构成同波写冲突 |
| `frontend/package.json` / lock | 仅 301（且仅 client 生成必需时） | 后继 deny（writeSet 外）；通过 |
| `**/sql/migration/**` | 仅 301（§3.6 条件） | 302..304 deny；通过 |

### 3.5 单波 / 全局所有权矩阵

| 资源 | 唯一写任务 | 判定 |
|---|---|---|
| `contracts/**`、`frontend/src/api/**` | 301 | 通过 |
| 后端 `catalog/import/**`、产品 typeSpecific 相关 editor/detail 后端包、条件 migration | 301 | 通过 |
| `frontend/.../editor/**` | 302 | 通过 |
| `frontend/.../detail/**` | 303 | 通过 |
| `frontend/.../import/**`、`tests/e2e/specs/**`（本轮） | 304 | 通过 |
| `frontend/src/styles/**`、`theme/**`、`layouts/**`、`shell/**`、`router/**` | **无** | 通过（UX 令牌不重开） |
| `frontend/src/components/**` | **无** | 通过（§1.7 禁写） |

### 3.6 既有 browse→ImportDialog 读边

仓库 `CatalogBrowsePage.vue` 编译期依赖 `@/features/catalog/import/ImportDialog.vue`（304 writeSet）。本计划 **无** browse 写任务；304 为 W3 单任务且已依赖 301+302+303。**不**复现「页面任务 ∥ 弹窗任务」不稳定读窗口。304 对 browse 的 `denyModify` + HOTFIX 串行扩写口径可接受。

## 4. 波次、启动条件与合入顺序

| 波次 | 计划声明 | 判定 |
|---|---|---|
| W1 | 301；SNAP + 计划 APPROVED 后派发 | 通过 |
| W2 | 302 ∥ 303；301 VERIFIED；写集 editor ∥ detail；均不得写 `components/**` | 通过 |
| W3 | 304；301+302+303 均 VERIFIED | 通过 |

合入约束（已写明、本角色认可）：

- 禁止单 PR 混写多任务路径
- 302∥303：写集无交集；建议先完成者先合入
- 合并顺序服从 DAG/波次，不以完成速度为准

无任务级 `mergeAfter` 字段——在 §6 波次表与并行对表完整时，与历史 WSC 本角色口径一致，**不单独升格 ISSUE**。

## 5. 任务完备性（可调度字段）

| taskId | dependsOn | writeSet | denyModify | acceptance / testScope | 判定 |
|---|---|---|---|---|---|
| 301 | 有 | 有（字面 glob + 条件说明） | 有（含 `components/**`） | 有 | 完备 |
| 302 | 有 | 有 | 有（含 `components/**`） | 有 | 完备 |
| 303 | 有 | 有 | 有（含 `components/**`） | 有 | 完备 |
| 304 | 有 | 有 | 有（含 `components/**`） | 有 | 完备 |

说明：302/303/304 writeSet 中「对应 `*.spec.ts` / `__tests__/**`」宜解读为**落在上一行 feature 前缀树内**的同树测例（现仓库亦为 colocated）。Orchestrator 字面 scope-check 应以 feature 前缀为准，**不得**把「对应」扩成仓库任意路径。此项作残余观察，不升格阻塞 ISSUE。

## 6. 残余观察（非 ISSUE / 不阻塞 APPROVE）

1. **同树测例措辞**：同上「对应 `*.spec.ts`」口径；执行期按 feature 前缀做 scope-check。
2. **`tests/fixtures/import/**` 串行增补**：304 不得破坏 301 已 VERIFIED 的后端/契约负例语义；计划「增补」口径足够。
3. **301 条件写 `package.json` / migration**：条件句可接受（路径字面已列）；后继任务不得借机扩写。
4. ~~**OpenAPI 展示组件复用**~~：已由 §1.7 方案 (a) + 全任务 deny `components/**` 明文闭合（对照本角色对 4.0 的 R1 残余观察 #1）。

## 本轮 ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| — | — | — | — | — |

## 决策

`APPROVE` — PLAN-WSC-4.1 在并行规划维度可调度：DAG 无环；**301 → 302∥303 → 304** 与 dependsOn 一致；唯一并行对 **302∥303 写集互斥可证明**且无不稳定读交叉；**`frontend/src/components/**` 禁写已在 §1.7/§3.5/各任务 denyModify 闭合**且无 writeSet 纳入；共享契约/api/后端/迁移与 E2E 所有权闭合；禁止并行对已由依赖与 §6 表锁定。

本角色 **不** 代批其他委员会角色，**不** 代关其他角色 ISSUE（含 ISSUE-SA-R1-002，关闭须原提出者确认），**不** 将本文件视为计划已 `APPROVED` 或可复制至 `planning/approved/`。

开放 ISSUE（本角色本轮）：**0**（P1×0，P2×0）。本角色对 PLAN-WSC-4.0 的 R1 开放 ISSUE：**0**（无 closeWhen 待确认）。
