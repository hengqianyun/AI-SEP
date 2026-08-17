# Round 2 Review — Plan Editor（计划结构完整性）

```yaml
reviewId: REV-PLAN-WSC8-R2-planEditor
planId: PLAN-WSC-8.2
round: 2
role: planEditor
snapshotIdAtReview: SNAP-WSC-008
runId: RUN-WSC-011
actorInstance: plan-editor-wsc-011-r2-review
decision: REQUEST_CHANGES
closedIssues:
  - ISSUE-PE-WSC8-R1-001
  - ISSUE-PE-WSC8-R1-002
  - ISSUE-PE-WSC8-R1-003
  - ISSUE-PE-WSC8-R1-004
  - ISSUE-PE-WSC8-R1-005
openIssues:
  - ISSUE-PE-WSC8-R2-001
summary: |
  作为隔离评审实例（≠起草者 plan-editor-wsc-011-r2），对照 planEditor 批准门禁与
  Round 1 本角色五条 closeWhen 复检 PLAN-WSC-8.2：候选声明 / DRAFT·CANDIDATE /
  §0.2 预落地对账 / §0.3 文件互斥 / 901..908 任务字段内联 / DAG 无环 / §7 十条
  REQ / §9 发布门禁十项结构完整；R1 五条 PE 异议 closeWhen 均已实质落地（905
  denyModify useCanWrite、906 独占 composable、902/905 浏览边界机械化、907 具名
  spec + 无条件 maintenance BE、§6 905→906 禁止并行、路由常量 /my-catalog）。
  但 TASK-WSC-907 denyModify 将 WriteAuthorizationInterceptor 误写为
  frontend 路径，Orchestrator 无法对 905 独占 Java 拦截器做字面 scope-check。
  本角色 R1 5/5 关闭；新开 1 条 P2 异议。不代关他角色 ISSUE；不改写计划 status。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **通过** — 901..908 均具备 requirements / dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags |
| 字面 writeSet / scope-check 可调度性 | **未通过** — 907 denyModify 拦截器路径前缀错误（ISSUE-PE-WSC8-R2-001） |
| denyModify 可机械判定 | **部分通过** — R1 软例外已清除；907 一条路径字面错误 |
| 验收 / 测试范围 | **通过** — 各任务 acceptance/testScope 齐全；§6.1 E2E 与 `/my-catalog` 字面常量对齐 |
| §9 发布门禁 | **通过（结构项）** — 十项齐全；P0 九条 + 908 绑定明确 |
| SNAP-WSC-008 一致性 | **通过** — 十条 REQ、PO 锁定项、Out of Scope、波次语义一致 |
| 候选声明 / DRAFT·CANDIDATE | **通过** — 文首候选声明 + YAML；§10 未勾选委员会 APPROVE |
| 预落地对账节 | **通过** — §0.2 波次表 + 路由冻结 + §0.3 文件互斥总览 |
| R1 本角色 ISSUE 关闭 | **5/5 关闭** — 见下方 closeWhen 核验 |
| 伪造全员同意 / 擅标 APPROVED | **无** |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁；Round 1 `REV-PLAN-WSC8-R1-planEditor.md` 五条 closeWhen；历史 `ISSUE-PE-WSC5-R1-001/002/003` 字面路径精神。本轮仅评 `planning/proposals/PLAN-WSC-8.2.md` **结构完整性**；**不**关闭他角色异议；**不**将计划 `status` 改为 `APPROVED`。

## 完整性自检（对照计划批准门禁 · 结构项）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | SNAP 十条 REQ；§5 主任务 + §7 映射可追溯 | 通过 |
| API / 数据 / UI 契约无未决冲突（计划结构） | §3.1 硬冻结；903 唯一写任务；blocking OQ=无 | 通过（结构项；契约细目属他角色） |
| 每任务有依赖、文件边界、读写集、测试范围 | §5 字段齐全；907 denyModify 一条路径错误 | **有缺陷** — ISSUE-PE-WSC8-R2-001 |
| DAG 无环；并行写集无交集 | §6 mermaid 无环；901∥902 允许；905∥906 **禁止并行**；903∥907 禁止 | 通过 |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.2 迁移 904 独占；§6.2 migrationReviewer + securityReviewer | 通过 |
| 所有必需角色输出 APPROVE | Round 2 收集中；§10 未勾选 | **不适用本角色单点关闭** |

## R1 ISSUE closeWhen 核验（本角色）

| id | sev | closeWhen 要求 | PLAN-WSC-8.2 证据 | 本轮 |
|---|---|---|---|---|
| ISSUE-PE-WSC8-R1-001 | P1 | 905 **denyModify** `useCanWrite.ts`；906 **独占** composable 全文 | §0.3 — 906 独占 `useCanWrite.ts`+spec；905 writeSet **不含** composable；905 `denyModify` 含 `useCanWrite.ts`（归 906）；§1.4 / §6 — **禁止 905∥906** | **关闭** |
| ISSUE-PE-WSC8-R1-002 | P1 | 902 不得改 CatalogBrowsePage/props；905 删除 composable 软例外 | §0.3 L137 — 902 **仅** CirculationSeatMap；902 `denyModify` 字面列 `CatalogBrowsePage.vue` / `useCatalogBrowse.ts`；905 `denyModify` `browse/**`（归 901）**无** composable 软扩界 | **关闭** |
| ISSUE-PE-WSC8-R1-003 | P1 | 907 回归面具名 spec；maintenance BE **无条件**列入 | 907 `writeSet` 具名 `IndustryCategoryField.spec.ts` / `ImportFieldMapping.spec.ts` / `CatalogProductDetail.spec.ts`；BE 无条件 `CatalogMaintenanceController` + `CatalogMaintenanceService`；删除 R1「若需 myCatalog scope」散文 | **关闭**（残余「或等价/若含 industry」为命名弹性，testScope 已钉 industry 回归） |
| ISSUE-PE-WSC8-R1-004 | medium | §6 并行表 905→906 **禁止并行**；删除「写集无交」 | §6 并行表 L670 — **禁止并行**（906 `dependsOn` 905；useCanWrite 906 独占） | **关闭** |
| ISSUE-PE-WSC8-R1-005 | P1 | 全计划 **单一字面** `/my-catalog` | §0.2 `ROUTE_MY_CATALOG=/my-catalog`；906/907 acceptance、§6.1 场景 5–6、908 证据表统一；void 前 `/my-maintenance` 仅作对账说明 | **关闭** |

## 结构抽查（Round 2 通过项）

| 检查项 | 证据 |
|---|---|
| 901..908 均含完整任务字段 | §5 各任务块 |
| §0.3 文件级互斥总览 | CatalogBrowse→901；SeatMap→902；contracts→903；sql→904；RBAC/interceptor→905；useCanWrite→906；maintenance→907；e2e→908 |
| E2E 写集与 §9 对齐 | TASK-WSC-908 writeSet 字面列 `p0-wsc-v1.6.spec.ts` / `playwright.v16.config.ts`；§9.8 + §6.1 证据表 |
| 905/906 useCanWrite 互斥 + 串行 | §0.3 注释 + §6 DAG + 并行表 — 较 R1「片段级 writeSet + 允许并行」已闭合 |
| 906→907 `routes.ts` 串行拥有权 | §0.3 L127/L135；907 `dependsOn` 906 — 可接受串行增量（同 SA R2 判定） |
| REQ 主任务分配 | §7 与 §5 requirements 交叉一致 |
| 修订说明映射 R1 ISSUE | 文首「修订说明 / 待 R2 复评」表含 PE-002..004 及合并项 |

### 任务字段矩阵

| taskId | requirements | writeSet | denyModify | acceptance | testScope | riskTags |
|---|---|---|---|---|---|---|
| TASK-WSC-901 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-902 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-903 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-904 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-905 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-906 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-907 | ✓ | ✓（字面） | **路径错误**（见 R2-001） | ✓ | ✓ | ✓ |
| TASK-WSC-908 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |

## §9 发布门禁核对

| # | §9 条目 | 计划内锚点 | 结论 |
|---|---|---|---|
| 1 | SNAP 已 APPROVED + 委员会 APPROVE 后发布 | 候选声明；§9.1 | 结构完整 |
| 2 | 全部 P0 REQ VERIFIED（含 908） | §7 九条 P0；908 dependsOn | 结构完整 |
| 3 | contracts@2.3.2 增量无冲突 | §3.1；903 唯一写 | 结构完整 |
| 4 | Wave A 可观察通过 | 901/902 acceptance | 结构完整 |
| 5 | Wave B 自动化证据 | 904/905 testScope；§6.2 双 reviewer | 结构完整 |
| 6 | Wave C/D 导航与维护 UX | 906/907 acceptance；`/my-catalog` 已冻结 | 结构完整 |
| 7 | UX/RBAC 无 P0 回退 | §1.5；§6.1 场景 9 | 结构完整 |
| 8 | §6.1 E2E 强制 + test:p0-v16 + v1.4 共存 | 908 writeSet + §6.1 证据表 | 结构完整 |
| 9 | 角色隔离 + scope-check + 预落地对账 | §1.4；§6.2；§0.2 | **907 denyModify 路径错误** 削弱机械校验 |
| 10 | maintainer 人类发布批准 | §9.10 | 结构完整 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PE-WSC8-R2-001 | P2 | `TASK-WSC-907` `denyModify` 含 `frontend/src/common/security/WriteAuthorizationInterceptor.java`（归 905）。该 Java 类实际位于 `backend/app/data-chain-service/src/main/java/com/shdata/datachain/common/security/WriteAuthorizationInterceptor.java`（见 TASK-WSC-905 `writeSet` L465 与 §0.3 L129「907 **不得**改 WriteAuthorizationInterceptor」）。错误前缀导致 §1.4「按各任务 `writeSet`/`denyModify` **字面路径** scope-check」对 907 无法机械拦截对该 BE 文件的越界 diff；虽 §0.3 叙述正确，但任务级 denyModify 与 905 writeSet 不对齐。 | 将 907 `denyModify` 中该条改为与 905 `writeSet` **同一 backend 字面路径**（或等价 repo 根相对路径 `backend/.../WriteAuthorizationInterceptor.java`）；删除错误的 `frontend/src/common/security/` 条目；可选在 §0.3 同步全路径以避免省略号歧义。 | REQ-RBAC-002, REQ-CAT-017 |

## 非异议说明（刻意不升格）

- 907 industry 回归 spec 写「或等价具名 spec / 若含 industry」：主路径已具名且 testScope 钉死 industry 回归；较 R1 `editor/**` glob 已实质改善，不重复开 ISSUE-PE-WSC8-R1-003。
- 906 `writeSet` `frontend/src/features/shell/**`（若独立）：可选扩展；企业展示亦可落在已列 `WorkbenchLayout.vue`。
- 903 `package.json`/`pnpm-lock.yaml`「仅当 client 生成必需」：路径字面已列，与 WSC 系列口径一致。
- 906→907 串行共用 `routes.ts`：§0.3 串行拥有权明确；非并行写集冲突。
- 908 `helpers`「若需」、904 `SessionContextSupport`「或等价」：可选/命名弹性，不升格。
- 他角色 Round 2 若另有 REQUEST_CHANGES，closeWhen 确认权属原提出者；本角色**不代关**。

## 决策

`REQUEST_CHANGES` — Round 1 本角色 ISSUE **5/5 关闭**；Round 2 新开 ISSUE 数 = **1**（P2×1）。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；**不得**将 `status` 标为 `APPROVED` 或写入 `planning/approved/`；本评审**不**写 `ai/runs/**`、**不** commit。
