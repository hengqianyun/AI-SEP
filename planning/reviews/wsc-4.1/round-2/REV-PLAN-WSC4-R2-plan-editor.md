# Round 2 Review — Plan Editor（完备性）

```yaml
reviewId: REV-PLAN-WSC4-R2-plan-editor
actorInstance: plan-editor-wsc-004-r2-rev
planId: PLAN-WSC-4.1
round: 2
role: planEditor
snapshotIdAtReview: SNAP-WSC-004
decision: APPROVE
summary: |
  作为独立评审实例（≠修订者 plan-editor-wsc-004-r2），对照 planEditor 批准门禁
  （依赖/文件边界/读写集/测试范围）与历史 PE 字面 writeSet 口径核对 PLAN-WSC-4.1：
  TASK-WSC-301..304 均全文内联 dependsOn/readSet/writeSet/denyModify/acceptance/
  testScope/riskTags，无整任务「同某 plan/同某节」外挂；§3.4 内联导入四态；§0 映射
  PLAN-WSC-4.0 Round 1 全部 10 条 ISSUE 修订位置且声明关闭须原提出者确认；DAG 无环
  且 302∥303 写集互斥；各任务 denyModify 含 frontend/src/components/**；REQ 映射与
  发布/风险门禁可自本文件核对；候选声明未伪造全员同意。本角色 R1 无开放 ISSUE；
  本轮无新异议。不代替他角色关闭或批准其 ISSUE。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **通过** — 301..304 均具备 dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags |
| 字面 writeSet / scope-check 可调度性 | **通过** — 无叙述性跨树扩写例外；304 browse 边界为 deny + 串行 HOTFIX；条件句（migration/client 生成）路径字面已列 |
| 禁止跨 plan「同某节」任务字段外挂 | **通过** — 任务字段与四态验收锚定本 planId（§3.4 / §5 / §6.1） |
| R1 他角色 ISSUE 映射完整性（结构） | **通过** — §0 表 10/10；注明不表示已关闭 |
| 计划批准门禁（结构项） | **通过** — 见下表；全员 APPROVE 非本角色单点关闭 |
| 版本 / round 元数据 | **通过** — `planId: PLAN-WSC-4.1` / `round: 1`（本 planId 共识轮）/ `DRAFT` / `CANDIDATE` / `snapshotId: SNAP-WSC-004`；`basedOn`/`lineageFrom: PLAN-WSC-4.0`；与 Run `round: 2` 谱系区分见 §0.1 |
| 伪造全员同意 | **无** — §10 未勾选委员会 APPROVE；候选声明明确禁止；§0 不代关 ISSUE |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁；历史 `ISSUE-PE-R1-002/003`、`ISSUE-PE-WSC3-R1-001/002/003` closeWhen 精神；`planning/reviews/wsc-4.0/round-1/REV-PLAN-WSC4-R1-plan-editor.md`（本角色 R1 APPROVE、开放 ISSUE=0）。本轮仅评 `planning/proposals/PLAN-WSC-4.1.md` 完备性；**不**关闭他角色 Round 1 ISSUE。

## 完整性自检（对照计划批准门禁）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | SNAP-WSC-004 五条 P0 均含 `acceptance`；本计划 §5 各主任务 `acceptance`/`testScope` 可追溯；§7 映射齐全；本快照无独立 P1 REQ | 通过 |
| API / 数据 / UI 契约无未决冲突（计划结构） | §3 冻结 `wsc-contracts@2.1.0`（含 ImportTemplateColumns 重冻、TEMPLATE_UNSUPPORTED 硬同步、OTHER 描述修订）；§3.3 方案 B 派生/冲突；§3.5 唯一所有权；blocking OQ 声明为无（§2.2） | 通过（结构项；closeWhen 确认权属原提出者） |
| 每任务有依赖、文件边界、读写集、测试范围 | §5：301..304 字段齐全；301 另有 `allowModify`；302/303/304 以 writeSet 为写边界 | 通过 |
| DAG 无环；并行写集无交集 | §6 mermaid + 波次表 + 并行对表；唯一并行对 `editor/**` ∥ `detail/**`；§1.7 禁止本 Run 写 `components/**` | 通过 |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.6 增量迁移；§3.7 导入/报告；§8 风险回滚表；§1.3/§9 发布门禁 | 通过 |
| 所有必需角色输出 APPROVE | Round 2 收集阶段；§10 未勾选 | **不适用本角色单点关闭** — 保持 CANDIDATE |

## R1 本角色 ISSUE

本角色在 `PLAN-WSC-4.0` Round 1 **无开放 ISSUE**（`REV-PLAN-WSC4-R1-plan-editor` → `APPROVE`）。本轮无 closeWhen 待自证关闭项。

## §0 ISSUE 映射条数抽查（不代关）

| 来源 | 期望 | PLAN-WSC-4.1 §0 | 结构结论 |
|---|---|---|---|
| SUMMARY 开放 ISSUE | 10 | 表列 10；声明「映射条数：10」 | 齐全（吸收意图映射；**非**已关闭） |

他角色 closeWhen（SA/QA/API）确认权属原提出者；本角色仅核验修订后计划结构仍可调度。

## 结构抽查（任务包 / 写集 / 门禁条款）

| 检查项 | 证据 |
|---|---|
| 301..304 均含 `dependsOn` | 301=`[]`；302/303→301；304→301+302+303；与 §6 一致 |
| 301..304 均含字面为主的 `writeSet`/`denyModify` | §5；契约/api/后端 import+产品 typeSpecific(+条件 detail)/sql 归 301；前端三面互斥；E2E 归 304；全任务 deny `frontend/src/components/**` |
| 301..304 均含 `acceptance`/`testScope`/`riskTags` | §5 内联；301 `testScope` 含命名 fixture 表（all-fail / legacy-template / format-invalid 等） |
| 导入四态可验收 | §3.4 内联 ①②③④；304 acceptance/testScope 引用本计划 §3.4；旧模板归态 ④ |
| 304 browse 边界 | `denyModify` browse + 串行 HOTFIX，**禁止**本任务散文扩写 |
| E2E 证据钉死 | §6.1：`evidenceId: TESTRUN-WSC-E2E-V13`；命令；报告 `tests/e2e/reports/p0-wsc-v1.3/`；强制 partial-success |
| 执行硬门禁条款 | §1.4：字面 writeSet scope-check；developer≠CR≠tester；301 独占契约/api；并行写集互斥；禁 `components/**` |
| 每任务审查/测试门禁 | §6.2 四步可调度 |
| 发布门禁条款 | §9 含 REQ VERIFIED、契约、四态/旧模板、OpenAPI 三面、UX 不回退、E2E、三角色、委员会 APPROVE、maintainer 人类批准 |
| REQ 覆盖 | §7：CAT-008/004/005、API-001、RBAC-001；`requirementCount: 5` 一致 |
| 候选边界 | YAML `CANDIDATE`/`DRAFT`；`actorInstance: plan-editor-wsc-004-r2`（修订者）；本评审实例为 `…-r2-rev` |

### 任务字段矩阵

| taskId | dependsOn | readSet | writeSet | denyModify | acceptance | testScope | riskTags |
|---|---|---|---|---|---|---|---|
| TASK-WSC-301 | ✓ | ✓ | ✓（字面 glob） | ✓ | ✓ | ✓（fixture 表） | ✓ |
| TASK-WSC-302 | ✓ | ✓ | ✓ | ✓（含 components） | ✓ | ✓ | ✓ |
| TASK-WSC-303 | ✓ | ✓ | ✓ | ✓（含 components） | ✓ | ✓ | ✓（`[]`） |
| TASK-WSC-304 | ✓ | ✓ | ✓ | ✓（含 components） | ✓ | ✓ | ✓ |

## 非异议说明（刻意不升格）

- 301 `writeSet` 中 sql/migration、`package.json`/`pnpm-lock`「仅当必需」及 detail 包「若…」：路径字面已列入 scope-check 集合，条件为使用约束；与已批准 PLAN-WSC-2.2/3.1 及本角色对 4.0 的 R1 口径一致，不升格。
- 302/303/304「对应 `*.spec.ts` / `__tests__/**`」：按同树 colocated 解读；Orchestrator 字面核对应以已列 feature 前缀为准，不得扩成仓库任意路径。残余观察，不升格。
- 301 与 304 均可写 `tests/fixtures/import/**`：DAG 串行（304 dependsOn 301），无并行写冲突；与 R1 QA 观察一致，不升格为本角色 ISSUE。
- §3.3.5 共享 fixture「建议路径」：关键字段一致验收已落入 301/302 acceptance；路径允许报告声明等价，不构成 writeSet 扩界。
- 他角色 Round 1 ISSUE（SA/QA/API）的 closeWhen 确认权属原提出者；本角色**不代关**。

## ISSUE 表（本轮）

（无）本角色完备性核对未发现阻碍 `APPROVE` 的结构性缺陷。

## 决策

`APPROVE` — 本角色开放 ISSUE 数 = **0**。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；不得将 `status` 标为 `APPROVED` 或写入 `planning/approved/`。
