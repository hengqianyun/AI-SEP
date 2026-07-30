# Round 1 Review — Plan Editor

```yaml
reviewId: REV-PLAN-WSC2-R1-planEditor
planId: PLAN-WSC-2.1
round: 1
role: planEditor
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-002 与 planEditor 批准门禁：REQ 映射与 DAG 骨架基本成立，
  §3.1/101/107/波次表相对 2.0 的增量修订可读。但作为 activePlanId=PLAN-WSC-2.1
  的唯一派发权威，TASK-WSC-102/103/105/106 及 §1.3/§4/§7/§8 仅写「同 2.0」，
  无法在本 planId 制品内完成字面 writeSet scope-check 与发布门禁核对；
  101 writeSet 仍含非字面「搬迁清单」条目，与 §1.4「字面路径」硬门禁冲突。
  另：frontmatter round:2 / §0·§10「供 Round 2 确认 closeWhen」与当前 Run
  对 PLAN-WSC-2.1 的 Round 1 共识语义不一致。不代替他角色关闭 §0 所列既有 ISSUE。
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PE-R1-002 | P1 | §5：`TASK-WSC-102/103/105/106` 正文仅为「同 2.0」；§1.3/§2/§4/§7/§8 同。§1.4 要求 Orchestrator 按各任务 `writeSet` **字面路径** scope-check；planEditor 门禁要求每任务具备依赖、文件边界、读写集、测试范围。仅引用 `PLAN-WSC-2.0` 时，对 `PLAN-WSC-2.1` 单文件无法机械核对 W3/W4 并行写集互斥与任务验收。 | 将 102/103/105/106 的 `dependsOn`/`writeSet`/`denyModify`/`acceptance`/`testScope`（及执行/发布所必需的 §1.3、§4、§8 表）内联进 `PLAN-WSC-2.1`，或声明 `PLAN-WSC-2.0` 为不可变规范性附件（固定路径 + 内容指纹）并列出纳入引用的章节清单，使本 planId 下门禁可执行。 | — |
| ISSUE-PE-R1-003 | P1 | `TASK-WSC-101` `writeSet` 含条目「搬迁清单允许的源→目标（须在 DEV 报告列出）：既有 `backend/src/main/java/com/wsc/**` → …」——叙述性允许集，非字面可检路径，与同计划 §1.4「字面路径」及「101 writeSet 可机械 scope-check」（§10）不一致。 | 将迁入/迁出源路径以字面 glob 列入 `writeSet`（或单独 `migrateAllowlist` 表且声明纳入 scope-check）；删除 writeSet 内纯叙述子弹；DEV 报告仅作举证，不得替代计划边界。 | — |
| ISSUE-PE-R1-004 | medium | YAML `round: 2`；候选声明与 §0/§10 写「Round 1 异议吸收稿 / 供 Round 2 确认 closeWhen / 吸收 12 条 ISSUE」，而 `RUN-WSC-002` 当前对 `PLAN-WSC-2.1` 执行 Round 1 隔离评审。易把谱系修订说明误当作本轮共识已关闭。 | 区分「相对 2.0 的修订谱系」与「本 planId 共识轮次」：校正 frontmatter `round`（或增加 `lineageFrom`/`consensusRound`）；§0 标明所列 ISSUE 仅为相对 2.0 的修订索引，**不**代替本轮独立评审与原提出者 closeWhen；§10 去掉易误导的「已吸收 Round 1 全部 ISSUE」完成态表述（或改为谱系核对项）。 | — |

## 范围说明（本角色）

- 已检：任务边界可执行性、验收/testScope 是否在本计划可核、DAG/波次门禁、与 SNAP 覆盖关系（§9）。
- 未做：代替 PA/SA/QA/API/UX/SEC/PP 关闭或确认 §0 表内既有 ISSUE；未改计划正文。
