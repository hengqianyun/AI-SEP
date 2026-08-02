# Round 1 Review — Plan Editor（完备性）

```yaml
reviewId: REV-PLAN-WSC4-R1-plan-editor
actorInstance: plan-editor-wsc-004-r1-rev
planId: PLAN-WSC-4.0
round: 1
role: planEditor
snapshotIdAtReview: SNAP-WSC-004
decision: APPROVE
summary: |
  作为独立评审实例（≠起草者 plan-editor-wsc-004），对照 planEditor 批准门禁
  （依赖/文件边界/读写集/测试范围）与历史 PE 字面 writeSet 口径核对 PLAN-WSC-4.0：
  TASK-WSC-301..304 均全文内联 dependsOn/readSet/writeSet/denyModify/acceptance/
  testScope/riskTags，无整任务「同某 plan/同某节」外挂；§3.4 内联导入四态可验收表；
  §1.4/§6.1 要求字面路径 scope-check，各任务 writeSet 主体为可检 glob；DAG 无环且
  302∥303 写集互斥已声明；REQ 映射与发布/风险门禁可自本文件核对；候选声明未伪造
  全员同意。本轮无本角色异议。不代替他角色关闭或批准其 ISSUE。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **通过** — 301..304 均具备 dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags |
| 字面 writeSet / scope-check 可调度性 | **通过** — 无叙述性跨树扩写例外；条件句（仅当 migration/client 生成必需）路径字面已列，同历史已批准口径 |
| 禁止跨 plan「同某节」任务字段外挂 | **通过** — 任务字段与四态验收锚定本 planId（§3.4 / §5） |
| 计划批准门禁（结构项） | **通过** — 见下表；全员 APPROVE 非本角色单点关闭 |
| 版本 / round 元数据 | **通过** — `planId: PLAN-WSC-4.0` / `round: 1` / `DRAFT` / `CANDIDATE` / `snapshotId: SNAP-WSC-004`；`basedOn`/`lineageFrom` null 与 §0 功能/UX 基线字段区分清楚 |
| 伪造全员同意 | **无** — §10 未勾选委员会 APPROVE；候选声明明确禁止 |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁；历史 `ISSUE-PE-R1-002/003`、`ISSUE-PE-WSC3-R1-001/002/003` closeWhen 精神（字面路径、禁任务字段外挂、禁 deny/write 软例外扩界）。本轮仅评 `planning/proposals/PLAN-WSC-4.0.md` 完备性；**不**关闭他角色 Round 1 ISSUE。

## 完整性自检（对照计划批准门禁）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | SNAP-WSC-004 需求表五条 P0 均含 `acceptance`；本计划 §5 各主任务 `acceptance`/`testScope` 可追溯；§7 映射齐全；本快照无独立 P1 REQ | 通过 |
| API / 数据 / UI 契约无未决冲突（计划结构） | §3 冻结 `wsc-contracts@2.1.0` 形状/错误码/模板语义；§3.5 唯一所有权；blocking OQ 声明为无（§2.2） | 通过（结构项；契约细目异议属他角色） |
| 每任务有依赖、文件边界、读写集、测试范围 | §5：301..304 字段齐全；301 另有 `allowModify`；302/303/304 以 writeSet 为写边界（与历史 WSC-3.x 一致） | 通过 |
| DAG 无环；并行写集无交集 | §6 mermaid + 波次表 + 并行对表；唯一并行对 `editor/**` ∥ `detail/**` | 通过 |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.6 增量迁移；§3.7 导入/报告；§8 风险回滚表；§1.3/§9 发布门禁 | 通过 |
| 所有必需角色输出 APPROVE | Round 1 收集阶段；§10 未勾选 | **不适用本角色单点关闭** — 保持 CANDIDATE |

## 结构抽查（任务包 / 写集 / 门禁条款）

| 检查项 | 证据 |
|---|---|
| 301..304 均含 `dependsOn` | 301=`[]`；302/303→301；304→301+302+303；与 §6 一致 |
| 301..304 均含字面为主的 `writeSet`/`denyModify` | §5；契约/api/后端 import+editor(+条件 detail)/sql 归 301；前端三面互斥；E2E 归 304 |
| 301..304 均含 `acceptance`/`testScope`/`riskTags` | §5 内联，非「同某任务」 |
| 导入四态可验收 | §3.4 内联 ①②③④；304 acceptance/testScope 引用本计划 §3.4；旧模板归态 ④ |
| 304 browse 边界 | `denyModify` browse + 串行 HOTFIX，**禁止**本任务散文扩写（对齐 WSC-3.1 对叙述性例外的修复口径） |
| 执行硬门禁条款 | §1.4：字面 writeSet scope-check；developer≠CR≠tester；301 独占契约/api；并行写集互斥 |
| 每任务审查/测试门禁 | §6.1 四步可调度 |
| 发布门禁条款 | §9 含 REQ VERIFIED、契约、四态/旧模板、OpenAPI 三面、UX 不回退、E2E、三角色、委员会 APPROVE、maintainer 人类批准 |
| REQ 覆盖 | §7：CAT-008/004/005、API-001、RBAC-001；`requirementCount: 5` 一致 |
| 候选边界 | YAML `CANDIDATE`/`DRAFT`；`actorInstance: plan-editor-wsc-004`（起草）；本评审实例为 `…-r1-rev` |

### 任务字段矩阵

| taskId | dependsOn | readSet | writeSet | denyModify | acceptance | testScope | riskTags |
|---|---|---|---|---|---|---|---|
| TASK-WSC-301 | ✓ | ✓ | ✓（字面 glob） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-302 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-303 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓（`[]`） |
| TASK-WSC-304 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

## 非异议说明（刻意不升格）

- 301 `writeSet` 中 sql/migration、`package.json`/`pnpm-lock`「仅当必需」及 detail 包「若…」：路径字面已列入 scope-check 集合，条件为使用约束；与已批准 PLAN-WSC-2.2/3.1 同类口径一致，不升格。
- 302/303/304「对应 `*.spec.ts` / `__tests__/**`」：按同树 colocated 解读（现仓库测例在 feature 前缀内）；Orchestrator 字面核对应以已列 feature 前缀为准，不得扩成仓库任意路径。作残余观察，不升格。
- 304 报告路径「或报告声明的等价落点」：同 PLAN-WSC-3.1 已批准 E2E 报告口径；主路径 `tests/e2e/reports/p0-wsc-v1.3/**` 已字面列出，不升格。
- 301 `contracts/**` 注释「相关 ui/state 文档若需同步」落在已列 `contracts/**` 字面 glob 内，不构成 writeSet 扩界；是否硬性同步 state-matrix/OpenAPI example 属契约角色验收强度，不代开/代关其 ISSUE。
- 他角色 Round 1 已落盘之 REQUEST_CHANGES（契约细目、派生所有权、testScope fixture 钉死等）closeWhen 确认权属原提出者；本角色**不代关**。

## ISSUE 表（本轮）

（无）本角色完备性核对未发现阻碍 `APPROVE` 的结构性缺陷。

## 决策

`APPROVE` — 本角色开放 ISSUE 数 = **0**。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；不得将 `status` 标为 `APPROVED` 或写入 `planning/approved/`。
