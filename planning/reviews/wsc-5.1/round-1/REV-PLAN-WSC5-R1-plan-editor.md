# Round 1 Review — Plan Editor（计划结构完整性）

```yaml
reviewId: REV-PLAN-WSC5-R1-planEditor
planId: PLAN-WSC-5.1
round: 1
role: planEditor
snapshotIdAtReview: SNAP-WSC-005
actorInstance: plan-editor-wsc-008-r1-review
decision: REQUEST_CHANGES
summary: |
  作为隔离评审实例（≠起草者 plan-editor-wsc-008-r1），对照 planEditor 批准门禁
  （依赖/文件边界/读写集/测试范围）与历史 PE 字面 writeSet 口径核对 PLAN-WSC-5.1：
  候选声明 / status=DRAFT / planType=CANDIDATE / §0.2 预落地对账节均在；601..604
  均全文内联 dependsOn/readSet/writeSet/denyModify/acceptance/testScope/riskTags；
  DAG 无环且串行四波；REQ 映射六条 P0；未伪造全员同意或将 status 标 APPROVED。
  但 TASK-WSC-604 writeSet 含无字面路径的叙述性条目；602 common/security 与
  603/604 browse 控制器存在方法级/条件软例外，无法按 §1.4 字面 scope-check；
  §9 发布门禁引用 E2E 而 601..603 deny 且无任务将 tests/e2e/** 列入 writeSet。
  本角色开放 3 条异议；不代替他角色批准或关闭 ISSUE；不改写计划 status。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **部分通过** — 601..604 均具备 dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags；无整任务「同某 plan」外挂 |
| 字面 writeSet / scope-check 可调度性 | **未通过** — 604 叙述性 writeSet；602/603–604 软例外边界 |
| denyModify 可机械判定 | **未通过** — 603 对 browse 同文件 L2 的散文级 carve-out；602 security 条件写权限 |
| 验收 / 测试范围 | **部分通过** — 各任务有 acceptance/testScope；发布门禁 E2E 与任务写集不对齐 |
| 候选声明 / DRAFT·CANDIDATE | **通过** — 文首候选声明 + YAML `status: DRAFT` / `planType: CANDIDATE`；§10 未勾选委员会 APPROVE |
| 预落地对账节 | **通过** — §0.2 波次表 + §0.3 缺口提示；§1.4/§6.2 要求对账进入 review 证据 |
| 伪造全员同意 / 擅标 APPROVED | **无** |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁；历史 `ISSUE-PE-R1-003`、`ISSUE-PE-WSC3-R1-001/003` closeWhen 精神（字面路径、禁叙述性允许集、禁 deny/write 软例外扩界）。本轮仅评 `planning/proposals/PLAN-WSC-5.1.md` **结构完整性**；**不**关闭他角色异议；**不**将计划 `status` 改为 `APPROVED`。

## 完整性自检（对照计划批准门禁 · 结构项）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | SNAP-WSC-005 六条 P0 均有 acceptance；§5 主任务 acceptance/testScope 可追溯；§7 映射齐全；本快照无独立 P1 REQ | 通过（结构项） |
| API / 数据 / UI 契约无未决冲突（计划结构） | §3 冻结 `wsc-contracts@2.2.0`；§3.2 唯一所有权；blocking OQ=无（§2.2）；SNAP 仍 `IN_REVIEW` 已在候选声明/派发门禁写明 | 通过（结构项；契约细目属他角色） |
| 每任务有依赖、文件边界、读写集、测试范围 | §5：601..604 字段齐全；边界可调度性见异议 | **有缺陷** — 见 ISSUE-001..003 |
| DAG 无环；并行写集无交集 | §6 mermaid + 波次表；串行四波；显式禁止 601..604 两两并行 | 通过 |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.3 迁移；§8 风险回滚；§9 发布门禁 | 通过（结构项） |
| 所有必需角色输出 APPROVE | Round 1 收集阶段；§10 未勾选 | **不适用本角色单点关闭** — 保持 CANDIDATE |

## 结构抽查（通过项）

| 检查项 | 证据 |
|---|---|
| 601..604 均含 `dependsOn` | 601=`[]`；602→601；603→602；604→603；与 §6 一致 |
| 601..604 均含 `denyModify` | §5 各任务块字面列出（含 `ai/runs/**/state.yaml` / `events.jsonl`） |
| 601..604 均含 `acceptance` / `testScope` / `riskTags` | §5 内联，非「同某任务」整包外挂 |
| 无整任务「同 4.1 / 同某节」字段 stub | §5 导语「字段全文内联」；任务块完整 |
| 谱系 vs 共识轮次 | YAML `round: 1` + `basedOn`/`lineageFrom: PLAN-WSC-4.1`；§0.1 概念表 |
| 候选边界 | `CANDIDATE`/`DRAFT`；起草 `actorInstance: plan-editor-wsc-008-r1` ≠ 本评审实例 |
| 预落地对账 | §0.2 按 601..604 列迹象与对账要求；完成检查勾选 §0.2 |
| REQ 覆盖 | §7：SHELL/RBAC/CAT-009/USER-001/CAT-010/CAT-001；`requirementCount: 6` 一致 |
| 契约唯一写任务 | §3.2 / 601 writeSet；602..604 deny `contracts/**` 与 `frontend/src/api/**` |

### 任务字段矩阵

| taskId | dependsOn | readSet | writeSet | denyModify | acceptance | testScope | riskTags |
|---|---|---|---|---|---|---|---|
| TASK-WSC-601 | ✓ | ✓ | ✓（主体字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-602 | ✓ | ✓ | ✓（含条件散文，见异议） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-603 | ✓ | ✓ | ✓ | ✓（含软 carve-out，见异议） | ✓ | ✓ | ✓ |
| TASK-WSC-604 | ✓ | ✓ | **部分叙述** | ✓ | ✓ | ✓ | ✓ |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PE-WSC5-R1-001 | P1 | `TASK-WSC-604` `writeSet` 含条目：「必要 repository 只读查询辅助（若需新增方法于既有 store/repository）」——**无字面可检路径**，属叙述性允许集。同计划 §1.4 要求 Orchestrator 按各任务 `writeSet` **字面路径** scope-check；与已关闭的 `ISSUE-PE-R1-003` / `ISSUE-PE-WSC3-R1-001` 同类。交付说明/「若需」不能替代计划边界。 | 删除该叙述项，或改为纳入 scope-check 的字面 glob（列明允许的 repository/store 路径）；若本波次确定无需新增，则从 writeSet 移除并依赖既有 browse service 只读调用。DEV 报告仅举证，不得扩大 writeSet。 | REQ-CAT-009 |
| ISSUE-PE-WSC5-R1-002 | P1 | （1）`TASK-WSC-602` `writeSet` 对 `backend/.../common/security/**` 附条件散文：「RBAC 产品写规则以 603 为准时可只读，若预落地已改矩阵则对账后允许本任务写矩阵中与用户管理/登录相关部分，产品写最终以 603 acceptance 为准」——方法/片段级条件写权限，Orchestrator 无法机械判定 diff 是否越界。（2）`TASK-WSC-603` `denyModify` 对已在本任务 `writeSet` 的 `controller/catalog/browse/**` 写：「允许为挂 mine 而改同一 Controller 类，但不得改 L2 聚合算法（归 604）；若冲突则…」——同文件方法级软 carve-out；同时 **604** 亦将 `controller/catalog/browse/**` 与 `service/catalog/browse/**` 列入 writeSet。串行虽避免并行冲突，但文件级 scope-check **无法**核验「只改了 mine / 未改 L2 算法」。同类缺陷见 `ISSUE-PE-WSC3-R1-003`。 | 二选一（可组合）写死为可机械核对边界：（a）602：将 `RbacMatrix.java` / `WriteAuthorizationInterceptor.java` **移出** 602 writeSet 并绝对 deny，仅保留登录/会话/PasswordHasher 等字面路径；或字面列入且 acceptance 钉死「本任务不得改产品写/导入矩阵行」的可测负例，删除「若预落地…允许」软句。（b）603/604：拆分 browse 所有权为字面文件/方法归属表（例如 603 独占含 `mine` 的 Controller/Service 文件路径，604 独占 `*Distribution*` / L2 专用类路径），或规定 603 **denyModify** 整个 `l2-distribution` 相关类型并由 604 独占 browse distribution 文件；删除「同一 Controller 类但不得改算法」散文。 | REQ-USER-001, REQ-RBAC-001, REQ-CAT-010, REQ-CAT-009 |
| ISSUE-PE-WSC5-R1-003 | medium | §9.8 要求「§6.1 E2E（或等价证据包）通过」；§6.1 列出 7 条 E2E 场景与 `tests/e2e/reports/p0-wsc-v1.4/`。但 TASK-WSC-601..603 `denyModify` 均含 `tests/e2e/**`；TASK-WSC-604 **未**将 `tests/e2e/**`（或报告目录）列入 `writeSet`，仅 testScope 叙述「建议…非强制」。历史已批准计划由独立 E2E 任务（如 206/304）字面拥有 E2E 写集。当前制品下：若执行 Playwright 落盘，**无任务**可通过字面 scope-check 写入 `tests/e2e/**`；若仅靠「等价证据包」，§9 未钉死何任务、何路径算等价，发布门禁不可机械调度。 | 任选其一写死：（a）新增串行 E2E/发布证据任务（或扩展 604）将 `tests/e2e/**`（及报告目录）字面列入 writeSet，并 deny 无关 feature 源码；或（b）修订 §9.8 / §6.1：明确发布门禁仅依赖各任务 `testScope` 自动化证据（列证据路径约定），删除对未赋权 E2E 写集的硬依赖，且不得再暗示「建议 Playwright」而无写集。 | REQ-SHELL-001, REQ-RBAC-001, REQ-CAT-009, REQ-USER-001, REQ-CAT-010 |

## 非异议说明（刻意不升格）

- 601 `writeSet` 中 `package.json`/`pnpm-lock`「仅当 client 生成必需」、603 `sql/migration`「仅当 create_by 缺列」：路径字面已列入，条件为使用约束；与已批准 PLAN-WSC-2.2/3.1/4.x 同类口径，不升格。
- 602/603「对应 `*.spec.ts` / `__tests__/**`」：按同树 colocated 解读；Orchestrator 字面核对应以已列 feature 前缀为准，不得扩成仓库任意路径。残余观察，不升格。
- 602 与 603 串行共用 `WorkbenchLayout.vue` / `routes.ts`：波次串行已声明，属历史可接受串行回流，不单独立 ISSUE（与方法级软 carve-out 区分）。
- 603 acceptance「导入四态相对 PLAN-WSC-4.1 不回退」：属功能基线不削弱声明；本版主交付非重开导入状态机。若 QA 角色要求本 plan 内联四态表，由其提出；本角色不代开。
- SNAP `IN_REVIEW`：候选声明与 §9.1 已作派发门禁，不构成计划结构字段缺失。
- 缺独立 `allowModify` 字段：与已批准 WSC-3.x/4.x「writeSet 即为写边界」口径一致，不升格。
- 他角色 Round 1 若另有 REQUEST_CHANGES，closeWhen 确认权属原提出者；本角色**不代关**。

## 决策

`REQUEST_CHANGES` — 本角色开放 ISSUE 数 = **3**（P1×2，medium×1）。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；**不得**将 `status` 标为 `APPROVED` 或写入 `planning/approved/`；本评审**不**写 `ai/runs/**`。
