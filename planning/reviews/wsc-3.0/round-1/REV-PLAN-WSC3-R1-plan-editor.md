# Round 1 Review — Plan Editor（完备性）

```yaml
reviewId: REV-PLAN-WSC3-R1-plan-editor
actorInstance: plan-editor-wsc-003-r1
planId: PLAN-WSC-3.0
round: 1
role: planEditor
snapshotIdAtReview: SNAP-WSC-003
decision: REQUEST_CHANGES
summary: |
  对照 PLAN-WSC-2.2 任务字段完备度与 planEditor 批准门禁（依赖/文件边界/读写集/测试范围）：
  TASK-WSC-201..206 均已全文内联 dependsOn/writeSet/denyModify/acceptance/testScope/riskTags，
  无整任务「同 2.2 / 同某节」外挂；frontmatter round:1 + lineageFrom/basedOn 与 §0 谱系表
  正确区分本 planId 共识轮次与功能基线谱系；候选声明未伪造全员同意。
  但 TASK-WSC-206 writeSet 含叙述性跨 feature 例外（data-testid/aria），无法按 §1.4
  字面路径做机械 scope-check（同级缺陷见历史 ISSUE-PE-R1-003）；TASK-WSC-204
  acceptance 对导入四态外挂 PLAN-WSC-2.2 §3.6 且正文只点名三态；TASK-WSC-202
  denyModify 含「除非…」软例外，边界不可调度。本角色不代替他角色批准。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度（相对 2.2） | 部分通过 — 六任务均具备五字段全文；无整节外挂 |
| 字面 writeSet / scope-check 可调度性 | **未通过** — 206 叙述性例外；202 deny 软例外 |
| 禁止「同某节」偷懒 | **未通过** — 204 acceptance 外挂 2.2 §3.6（且四态枚举不完整） |
| 版本 / round 元数据 | 通过 — `planId`/`round:1`/`lineageFrom`/`snapshotId`/`DRAFT`/`CANDIDATE` 一致 |
| 伪造全员同意 | 无 — §9 未勾选委员会 APPROVE；候选声明明确禁止 |

**对照基线**：`planning/approved/PLAN-WSC-2.2.md` §5（任务字段形态）+ 历史 `ISSUE-PE-R1-002/003/004` closeWhen 精神；本轮仅评 `PLAN-WSC-3.0` 完备性，不关闭他角色异议。

## 结构抽查（通过项）

| 检查项 | 证据 |
|---|---|
| 201..206 均含 `dependsOn` | §4 各任务块字面列出（201=`[]`；202..205→201；206→201..205） |
| 201..206 均含字面为主的 `writeSet`/`denyModify` | §4；并行互斥与 §5 DAG/波次表一致 |
| 201..206 均含 `acceptance`/`testScope`/`riskTags` | §4 内联，非「同某任务」 |
| 无整任务「同 2.2」 | §4 导语声明「字段全文内联」；全文检索无「同 2.0/同 PLAN」任务 stub |
| 谱系 vs 共识轮次 | YAML `round: 1` + `lineageFrom`/`basedOn: PLAN-WSC-2.2`；§0 概念表；§9 未伪造 APPROVE |
| REQ 覆盖表 | §6：REQ-UX-001..011 主任务映射齐全 |
| DAG 无环 / 并行写集声明 | §5 mermaid + 并行对表 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PE-WSC3-R1-001 | P1 | `TASK-WSC-206` `writeSet` 含条目：「若稳定选择器必须微调…允许最小范围触碰各 feature 内 `data-testid` / aria（**须在交付说明列出文件**）」——叙述性允许集，非字面可检路径。同计划 §1.4 要求 Orchestrator 按各任务 `writeSet` **字面路径** scope-check；与 PLAN-WSC-2.2 已关闭的 ISSUE-PE-R1-003 同类缺陷。交付说明不能替代计划边界，且与 202..205 已声明的互斥写集在派发时无法机械核对「最小触碰」范围。 | 删除该叙述性例外，或改为纳入 scope-check 的字面 glob/`testidAllowlist`（列明允许路径模式，并声明仅属性级变更、禁止视觉大改的可检约束）；若必须改选择器则回对应 20x 任务或串行 hotfix 任务独占列出的路径。DEV 报告仅举证，不得扩大 writeSet。 | REQ-UX-011 |
| ISSUE-PE-WSC3-R1-002 | P1 | `TASK-WSC-204` `acceptance`：「成功/失败/部分成功四态仍可区分（PLAN-WSC-2.2 §3.6）」——（1）验收判定外挂他 plan 章节，属「同某节」偷懒，本 planId 单文件无法完成 204 验收调度；（2）正文只点名三态却称「四态」，与 2.2 §3.6 ①全成功/②部分成功/③全失败/④文件级拒绝 不一致，tester 无法仅凭本计划判定完备。同文件 `readSet` 亦仅引用「PLAN-WSC-2.2 §3.6」而未内联可验收表。 | 在 `PLAN-WSC-3.0` 内联导入结果四态可验收定义（可复制/改编 2.2 §3.6 四态表），并令 204 `acceptance`（及必要时 `testScope`）字面引用本计划内联表，删除对「PLAN-WSC-2.2 §3.6」的唯一依赖；四态名称与判定条件完整、禁止三态冒充四态。 | REQ-UX-006, REQ-UX-009 |
| ISSUE-PE-WSC3-R1-003 | medium | `TASK-WSC-202` `denyModify` 对 `frontend/src/features/auth/store/**`、`composables/**`、`api/**` 写「除非登录视觉硬依赖且不改会话语义；默认禁止改鉴权逻辑」——条件例外使 deny 边界不可机械判定，Orchestrator/developer 可争议「是否视觉硬依赖」，与 §1.4 字面门禁及 2.2 任务 deny 的硬枚举风格不一致。 | 二选一写死：（a）上述路径保持绝对 `denyModify`，登录视觉仅限 `writeSet` 已列之 `auth/views/**`；或（b）若确需改 store/composables，将其字面路径移入 `writeSet` 并补充 acceptance/testScope（会话语义不回退的可测负例），删除「除非」软例外。 | REQ-UX-008 |

## 非异议说明（刻意不升格）

- §3.2「继承 PLAN-WSC-2.2 §6.1 **意图**」后已内联六条回归场景最低集；属意图溯源而非任务字段外挂，不单独立 ISSUE。
- §5 并行表「同上」仅指同表上一行「禁止单 PR 混写」，不影响任务包可调度性。
- 同文档内 `acceptance` 引用 §2.1/§3.1/§3.2（本 plan 章节）可接受，不同于跨 plan「同某节」。
- 201 `writeSet`「仅当…必需」的构建配置条件与 2.2 101「若必须」同类；本次不升格，但修订 206 时建议保持「有则列入字面路径、无则不写」口径。

## 决策

`REQUEST_CHANGES` — 本角色开放 ISSUE 数 = **3**（P1×2，medium×1）。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威。
