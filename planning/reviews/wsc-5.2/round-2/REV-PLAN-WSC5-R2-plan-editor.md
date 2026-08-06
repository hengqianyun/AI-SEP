# Round 2 Review — Plan Editor（计划结构完整性）

```yaml
reviewId: REV-PLAN-WSC5-R2-planEditor
planId: PLAN-WSC-5.2
round: 2
role: planEditor
snapshotIdAtReview: SNAP-WSC-005
actorInstance: plan-editor-wsc-008-r2-review
decision: APPROVE
summary: |
  作为隔离评审实例（≠起草者 plan-editor-wsc-008-r2），对照 planEditor 批准门禁
  结构项与 R1 closeWhen，核验 PLAN-WSC-5.2：候选声明 / status=DRAFT /
  planType=CANDIDATE / §0.2 预落地对账 / 601..605 字段全文内联 / DAG 串行五波无环 /
  REQ 六条 P0 映射齐全；未伪造全员同意、未将 status 标 APPROVED。
  ISSUE-PE-WSC5-R1-001：604 writeSet 已删叙述性 repository 条，仅字面路径。
  ISSUE-PE-WSC5-R1-002：602 绝对 deny RbacMatrix/拦截器；603/604 以独立 L2 类文件
  字面独占，无同 Controller 方法级 carve-out。
  ISSUE-PE-WSC5-R1-003：TASK-WSC-605 字面拥有 tests/e2e 规格/配置/报告；§9.8 绑定 605。
  三条 closeWhen 均满足，本角色关闭；新开异议 0。不改写计划 status。
closedIssues:
  - ISSUE-PE-WSC5-R1-001
  - ISSUE-PE-WSC5-R1-002
  - ISSUE-PE-WSC5-R1-003
openIssues: []
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **通过** — 601..605 均具备 dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags |
| 字面 writeSet / scope-check 可调度性 | **通过** — R1 三条 PE 异议对应缺陷已消除（见下） |
| denyModify 可机械判定 | **通过** — 文件级互斥；无方法级软 carve-out |
| 验收 / 测试范围 / E2E 写集 | **通过** — 605 独占 `tests/e2e/**`；§6.1 强制；§9.8 绑定 |
| 候选声明 / DRAFT·CANDIDATE | **通过** — YAML `status: DRAFT` / `planType: CANDIDATE`；§10 未勾选委员会 APPROVE |
| 预落地对账节 | **通过** — §0.2 含 601..605 |
| 伪造全员同意 / 擅标 APPROVED | **无** |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁结构项；`planning/reviews/wsc-5.1/round-1/REV-PLAN-WSC5-R1-plan-editor.md` 三条 closeWhen。本轮仅评 `planning/proposals/PLAN-WSC-5.2.md` **计划结构完整性**；**不**关闭他角色异议；**不**将计划 `status` 改为 `APPROVED`。

## R1 异议关闭核验

| ISSUE id | sev | closeWhen 要点 | PLAN-WSC-5.2 证据 | 结论 |
|---|---|---|---|---|
| ISSUE-PE-WSC5-R1-001 | P1 | 删除 604 叙述性 repository 条，或改为字面路径；否则依赖既有 browse 只读 | §0 映射行；604 `writeSet` 仅 CirculationSeatMap / L2Controller / L2Service / DistributionSliceRecord / L2\* 测试；**无**「必要 repository…」；604 `denyModify` CatalogBrowseController/Service（经既有只读，不扩写 repository） | **关闭** |
| ISSUE-PE-WSC5-R1-002 | P1 | 602：RbacMatrix/拦截器移出 writeSet 并绝对 deny；603/604：字面文件拆分 browse，删同 Controller 方法级散文 | 602 writeSet 仅 PasswordHasher/AuthAuditLogger 等具名安全文件；denyModify 含 `RbacMatrix.java`/`WriteAuthorizationInterceptor.java`。603 独占 CatalogBrowse\* + 矩阵/拦截器；denyModify `L2Distribution*.java`。604 独占 `L2Distribution*.java` + CirculationSeatMap；deny CatalogBrowse\*。§2.3 方案 (A)；§1.4 禁方法级例外 | **关闭** |
| ISSUE-PE-WSC5-R1-003 | medium | 新增串行 E2E 任务字面拥有 `tests/e2e/**`，或修订 §9 去掉未赋权 E2E 硬依赖 | **TASK-WSC-605**：writeSet 含 `tests/e2e/specs/**`、`playwright.config.ts`、`reports/p0-wsc-v1.4/**` 等；601..604 denyModify `tests/e2e/**`；§6.1 场景 1–7 **强制**；§9.8 钉死 evidenceId + 605 报告路径 | **关闭** |

## 完整性自检（对照计划批准门禁 · 结构项）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | §7 六条 P0 → 主任务 acceptance/testScope；本快照无独立 P1 REQ | 通过（结构项） |
| API / 数据 / UI 契约无未决冲突（计划结构） | §3.1 硬冻结；§3.2 唯一所有权；blocking OQ=无；SNAP `IN_REVIEW` 已写派发门禁 | 通过（结构项；契约细目属他角色） |
| 每任务有依赖、文件边界、读写集、测试范围 | §5：601..605 字段齐全；写集可字面 scope-check | **通过** |
| DAG 无环；并行写集无交集 | §6：W1→W5 串行；禁止两两并行 | 通过 |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.3 / §6.2 / §8 / §9 | 通过（结构项） |
| 所有必需角色输出 APPROVE | Round 2 收集中；§10 未勾选 | **不适用本角色单点关闭** — 保持 CANDIDATE |

### 任务字段矩阵

| taskId | dependsOn | readSet | writeSet | denyModify | acceptance | testScope | riskTags |
|---|---|---|---|---|---|---|---|
| TASK-WSC-601 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-602 | ✓ | ✓ | ✓（字面；无 security 条件散文） | ✓（含矩阵/拦截器绝对 deny） | ✓ | ✓ | ✓ |
| TASK-WSC-603 | ✓ | ✓ | ✓（CatalogBrowse 字面文件） | ✓（含 L2 独占文件 deny） | ✓ | ✓ | ✓ |
| TASK-WSC-604 | ✓ | ✓ | ✓（仅字面；无叙述性 repository） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-605 | ✓ | ✓ | ✓（E2E 规格/配置/报告字面） | ✓（禁 feature/backend） | ✓ | ✓ | ✓ |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | — | — | — |

## 非异议说明（刻意不升格）

- 605 writeSet「helper/fixture 须在交付说明列出字面路径」：主边界已字面列入 specs/config/reports；交付时补列 helper 与 R1 对 colocated spec 口径同类，不重开 ISSUE-003。
- 603 `CatalogBrowseSeedStore.java`「及 create_by 相关 repository 字面列于交付说明」：锚定字面起点 + 交付补列，非开放叙述性允许集；非本轮 R1 closeWhen 范围，不升格。
- 601 `package.json`「仅当必需」、603 sql「仅当缺列」：路径字面已列，条件为使用约束，与已批准计划口径一致。
- 602/603 串行共用 WorkbenchLayout/routes + §3.2 白名单：可机械；不升格。
- SNAP `IN_REVIEW`：候选声明与 §9.1 已作门禁，非结构字段缺失。
- 他角色 R1 ISSUE 关闭权属原提出者 Round 2；本角色**不代关**。

## 决策

`APPROVE` — 本角色 R1 开放 ISSUE **全部关闭**（closedIssueCount=3）；本轮新开 openIssues=0。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；**不得**将 `status` 标为 `APPROVED` 或写入 `planning/approved/`；本评审**不**写 `ai/runs/**`。
