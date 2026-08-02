# Round 2 Review — Plan Editor（完备性 / closeWhen）

```yaml
reviewId: REV-PLAN-WSC3-R2-plan-editor
actorInstance: plan-editor-wsc-003-r2-rev
planId: PLAN-WSC-3.1
round: 2
role: planEditor
snapshotIdAtReview: SNAP-WSC-003
decision: APPROVE
summary: |
  作为 ISSUE-PE-WSC3-R1-001..003 原提出者（评审实例，非本轮起草者），对照
  PLAN-WSC-3.1 核验 closeWhen：206 writeSet 已删叙述性跨 feature testid/aria 例外，
  仅字面路径 + denyModify frontend/src/features/** + 串行 20x-HOTFIX 回流；§3.4
  内联四态可验收表且 204 readSet/acceptance/testScope 引用本节而非外挂 2.2 §3.6；
  202 对 auth/store|composables|api 为绝对 denyModify（方案 a）。本角色三条 ISSUE
  closeWhen 均已满足；本轮无新异议。不代替他角色关闭或批准；不伪造全员同意。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| R1 本角色 ISSUE closeWhen | **通过** — 001/002/003 均在 PLAN-WSC-3.1 有可检修订 |
| 任务字段内联完备度 | 通过 — 201..206 均含 dependsOn/writeSet/denyModify/acceptance/testScope/riskTags 全文 |
| 字面 writeSet / scope-check 可调度性 | 通过 — 206 无散文扩写例外；§1.4 字面路径硬门禁可执行 |
| 禁止跨 plan「同某节」任务验收外挂 | 通过 — 204 四态锚定本计划 §3.4 |
| 版本 / round 元数据 | 通过 — `planId: PLAN-WSC-3.1`/`round:1`/`lineageFrom: PLAN-WSC-3.0`/`DRAFT`/`CANDIDATE`；§0 声明关闭须原提出者确认 |
| 伪造全员同意 | 无 — §9 未勾选委员会 APPROVE；候选声明明确禁止 |

**对照基线**：`planning/reviews/wsc-3.0/round-1/REV-PLAN-WSC3-R1-plan-editor.md` 异议表 + planEditor 批准门禁（依赖/文件边界/读写集/测试范围）；本轮仅评 `PLAN-WSC-3.1` 对本角色 R1 ISSUE 的完备修订，不关闭他角色异议。

## R1 ISSUE closeWhen 核对（仅本角色）

| id | severity | closeWhen 要点 | PLAN-WSC-3.1 证据位置 | 本轮判定 |
|---|---|---|---|---|
| ISSUE-PE-WSC3-R1-001 | P1 | 删除 206 writeSet 叙述性跨 feature testid/aria 例外，或改为字面 glob/allowlist；必须改选择器则回流 20x / 串行 HOTFIX；DEV 报告不得扩大 writeSet | §4 TASK-WSC-206：`writeSet` 均为字面路径（证据/E2E/报告）；`denyModify` 含 `frontend/src/features/**` 并写明禁止改 feature 源码（含 data-testid/aria）；选择器适配优先 `tests/e2e/**`，必要 DOM 属性改走串行 `TASK-WSC-20x-HOTFIX`（writeSet=该 feature 字面路径）。§1.4 / §5.1 同步禁止散文例外 | **closeWhen 已满足** |
| ISSUE-PE-WSC3-R1-002 | P1 | 本 planId 内联导入四态可验收定义；204 acceptance（及必要时 testScope）字面引用本计划内联表；删除对「PLAN-WSC-2.2 §3.6」的唯一依赖；四态完整不可三态冒充 | §3.4 内联 ①全成功/②部分成功/③全失败/④文件级拒绝（判定+UI 最低要求）；声明不得以外挂 2.2 §3.6 为唯一依据。204 `readSet` 引用「本计划 §3.4 / §3.5」；`acceptance` 字面要求 §3.4 四态均可区分并枚举四名；`testScope` 含导入四态相关 | **closeWhen 已满足** |
| ISSUE-PE-WSC3-R1-003 | medium | 二选一写死：（a）auth store/composables/api 绝对 deny，登录视觉仅限 `auth/views/**`；或（b）字面移入 writeSet 并补可测负例；删除「除非」软例外 | TASK-WSC-202：`writeSet` 仅 `auth/views/**`（及 overview/草稿证据）；`denyModify` 对 `auth/store/**`、`composables/**`、`api/**` 标注 **绝对禁止**，登录视觉仅限 `auth/views/**`；全文无「除非…视觉硬依赖」条件例外 → 方案 (a) | **closeWhen 已满足** |

## 结构抽查（closeWhen 之外）

| 检查项 | 证据 |
|---|---|
| 201..206 字段齐全 | §4 各任务块字面列出五字段 + riskTags |
| DAG 无环 / 并行写集 | §5 mermaid + 并行对表；显式禁止 203∥204 |
| REQ 覆盖 | §6：REQ-UX-001..011 主任务映射齐全 |
| 风险/发布门禁 | §7 / §8 可自本文件核对 |
| 候选边界 | YAML `CANDIDATE`/`DRAFT`；§0/§9 不伪造独立评审与 ISSUE 已关闭 |

## 非异议说明（刻意不升格）

- §3.2「继承 PLAN-WSC-2.2 §6.1 **意图**」后已内联六条回归场景；属意图溯源，非任务字段外挂（与 R1 非异议口径一致）。
- 206 `writeSet`「同目录为扩展断言所必需的 helper/fixture…须在交付说明列出」限定在 `tests/e2e/specs/` 语境下的附属字面文件，不同于已删除的跨 feature 叙述例外；本次不升格。
- 201 构建配置「仅当必需」条件与 R1 非异议口径一致；不升格。
- 他角色 R1 ISSUE（SA/QA/PP/UX/SEC 等）的 closeWhen 确认权属原提出者；本角色不代关。

## ISSUE 表（本轮新增）

（无）本角色 R1 ISSUE 的 closeWhen 均已在 PLAN-WSC-3.1 体现；本轮无新异议。

## 决策

`APPROVE` — 本角色剩余开放 ISSUE 数 = **0**（R1 三条 closeWhen 已确认满足）。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威。
