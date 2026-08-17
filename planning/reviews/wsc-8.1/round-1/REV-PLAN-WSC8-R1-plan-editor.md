# Round 1 Review — Plan Editor（计划结构完整性）

```yaml
reviewId: REV-PLAN-WSC8-R1-planEditor
planId: PLAN-WSC-8.1
round: 1
role: planEditor
snapshotIdAtReview: SNAP-WSC-008
actorInstance: plan-editor-wsc-011-r1-review
decision: REQUEST_CHANGES
summary: |
  作为隔离评审实例（≠起草者 plan-editor-wsc-011-r1），对照 planEditor 批准门禁
  与历史 PE 字面 writeSet 口径核对 PLAN-WSC-8.1：候选声明 / status=DRAFT /
  planType=CANDIDATE / §0.2 预落地对账节齐全；901..908 均全文内联 requirements、
  dependsOn、readSet、writeSet、denyModify、acceptance、testScope、riskTags；
  DAG 无环；§7 十条 REQ 映射与 SNAP-WSC-008 一致；§9 发布门禁十项结构完整；
  TASK-WSC-908 字面拥有 tests/e2e 写集，§6.1/§9 E2E 门禁可对齐（较 WSC-5.1 R1 已改善）。
  但 905 useCanWrite 片段级写权限、901/902/905 浏览表面跨任务软例外、907 条件性
  writeSet、§6 并行表 905∥906「写集无交」与事实不符、我的目录路由路径未冻结，
  均妨碍 §1.4 字面 scope-check 与 E2E 机械调度。本角色开放 5 条异议；不代关他角色 ISSUE；
  不改写计划 status。
```

## 评审范围

| 维度 | 结论 |
|---|---|
| 任务字段内联完备度 | **通过** — 901..908 均具备 requirements / dependsOn / readSet / writeSet / denyModify / acceptance / testScope / riskTags |
| 字面 writeSet / scope-check 可调度性 | **未通过** — 905 片段级 useCanWrite；907 条件性/叙述性条目 |
| denyModify 可机械判定 | **未通过** — 901/902/905 浏览表面跨任务软例外 |
| 验收 / 测试范围 | **部分通过** — 各任务有 acceptance/testScope；908 E2E 写集已对齐 §9；路由路径未定影响 §6.1 机械执行 |
| §9 发布门禁 | **通过（结构项）** — 十项齐全；P0 九条 + 908 绑定明确；P1 REQ-SHELL-010 不阻塞发布与 SNAP 一致 |
| SNAP-WSC-008 一致性 | **通过（映射项）** — 十条 REQ、PO 锁定项、Out of Scope、波次语义一致；路径字面未冻结见异议 |
| 候选声明 / DRAFT·CANDIDATE | **通过** — 文首候选声明 + YAML；§10 未勾选委员会 APPROVE |
| 预落地对账节 | **通过** — §0.2 波次表 + §0.3 文件互斥总览 |
| ISSUE 编号 | **本评审新建** — `ISSUE-PE-WSC8-R1-001`..`005` |
| 伪造全员同意 / 擅标 APPROVED | **无** |

**对照基线**：`ai/agents/plan-editor.md` 计划批准门禁；历史 `ISSUE-PE-WSC5-R1-001/002/003` closeWhen 精神（字面路径、禁叙述性允许集、禁 deny/write 软例外扩界）。本轮仅评 `planning/proposals/PLAN-WSC-8.1.md` **结构完整性**；**不**关闭他角色异议；**不**将计划 `status` 改为 `APPROVED`。

## 完整性自检（对照计划批准门禁 · 结构项）

| 门禁项 | 证据 | 结论 |
|---|---|---|
| 所有 P0/P1 REQ 均有验收标准 | SNAP 十条 REQ 均有 acceptance；§5 主任务 + §7 映射可追溯 | 通过（结构项） |
| API / 数据 / UI 契约无未决冲突（计划结构） | §3.1 冻结 `wsc-contracts@2.3.2`；903 唯一写任务；blocking OQ=无（§2.2） | 通过（结构项；契约细目属他角色） |
| 每任务有依赖、文件边界、读写集、测试范围 | §5：901..908 字段齐全；边界可调度性见异议 | **有缺陷** — 见 ISSUE-001..005 |
| DAG 无环；并行写集无交集 | §6 mermaid 无环；901∥902 互斥声明正确；§6 并行表 905∥906 行有误（ISSUE-004） | **部分通过** |
| 安全 / 迁移 / 部署 / 回滚风险已处理或明确接受 | §3.2 迁移 904 独占；§8 风险回滚；904→migrationReviewer（§6.2） | 通过（结构项） |
| 所有必需角色输出 APPROVE | Round 1 收集阶段；§10 未勾选 | **不适用本角色单点关闭** — 保持 CANDIDATE |

## SNAP-WSC-008 一致性核对

| 检查项 | SNAP-WSC-008 | PLAN-WSC-8.1 | 结论 |
|---|---|---|---|
| REQ 数量与 ID | 10 条（9×P0 + 1×P1） | §7 映射十条；YAML `requirementCount: 10` | 一致 |
| PO 锁定：ADMIN 双入口 | catalogMaintenanceMenu ADMIN-only-full-scope；myCatalogMenu ADMIN+PROVIDER | §1.1 / §4 / 906 acceptance | 一致 |
| PO 锁定：PROVIDER 无目录维护 | providerNoCatalogMaintenance: true | §1.1 Out of Scope；906/907 acceptance | 一致 |
| 浏览无 industryCategory | browseRemoveIndustryCategoryFilter | 901 / REQ-CAT-019 分工 | 一致 |
| 座序图无企业筛 | seatMapNoEnterpriseFilter | 902 / 905 / §6.1 场景 2 负例 | 一致 |
| mine 本企业 + ADMIN 可写 | mineScope: enterprise | 905 / REQ-CAT-016 | 一致 |
| 我的目录 scope | ADMIN 本企业 / PROVIDER create_by | 907 objective + acceptance | 一致 |
| 全链无企业过滤 | fullChainNoEnterpriseFilter | 902 + 905 + §6.1 场景 3 | 一致 |
| 波次 A/B/C 语义 | SNAP §交付波次 | 计划拆 A/B/C/D/E 调度粒度；语义不冲突 | 一致 |
| contractsTarget | wsc-contracts@2.3.2 | §3.1 / YAML 一致 | 一致 |
| 路由字面路径 | REQ-CAT-017 两入口独立 | 906/907 写「/my-catalog **或** 统一路径」— **未冻结** | **见 ISSUE-005** |

## 结构抽查（通过项）

| 检查项 | 证据 |
|---|---|
| 901..908 均含 `requirements` | §5 各任务块首字段 |
| 901..908 均含 `dependsOn` | 与 §6 DAG 一致（如 903→901+902；908→901/902/905/906/907） |
| 901..908 均含 `denyModify` | 含 `ai/runs/**/state.yaml` / `events.jsonl`；908 deny 默认 v1.4 config |
| 901..908 均含 `acceptance` / `testScope` / `riskTags` | §5 内联完整 |
| 文件级互斥总览 | §0.3：CatalogBrowsePage→901；CirculationSeatMap→902；contracts→903；maintenance→907；e2e→908 |
| E2E 写集与 §9 对齐 | TASK-WSC-908 writeSet 含 `tests/e2e/**`；§9.8 绑定 evidenceId + test:p0-v16（较 WSC-5.1 R1 ISSUE-003 类缺陷已闭合） |
| 禁止 903∥907 | §6 并行表 + 907 dependsOn 903（经 905/906 串行） | 
| REQ 主任务分配 | §7 与 §5 requirements 交叉一致 |

### 任务字段矩阵

| taskId | requirements | writeSet | denyModify | acceptance | testScope | riskTags |
|---|---|---|---|---|---|---|
| TASK-WSC-901 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-902 | ✓ | ✓（字面） | ✓（含软例外，见 ISSUE-002） | ✓ | ✓ | ✓ |
| TASK-WSC-903 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-904 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-905 | ✓ | ✓（含片段散文，见 ISSUE-001） | ✓（含软例外，见 ISSUE-002） | ✓ | ✓ | ✓ |
| TASK-WSC-906 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-907 | ✓ | **条件/叙述**（见 ISSUE-003） | ✓ | ✓ | ✓ | ✓ |
| TASK-WSC-908 | ✓ | ✓（字面） | ✓ | ✓ | ✓ | ✓ |

## §9 发布门禁核对

| # | §9 条目 | 计划内锚点 | 结论 |
|---|---|---|---|
| 1 | SNAP 已 APPROVED + 委员会 APPROVE 后发布 | 候选声明；§9.1 | 结构完整 |
| 2 | 全部 P0 REQ VERIFIED（含 908） | §7 九条 P0；908 dependsOn | 结构完整 |
| 3 | contracts@2.3.2 增量无冲突 | §3.1；903 唯一写 | 结构完整 |
| 4 | Wave A 可观察通过 | 901/902 acceptance | 结构完整 |
| 5 | Wave B 自动化证据 | 904/905 testScope | 结构完整 |
| 6 | Wave C/D 导航与维护 UX | 906/907 acceptance | 结构完整（路由字面见 ISSUE-005） |
| 7 | UX/RBAC 无 P0 回退 | §1.5；§6.1 场景 9 | 结构完整 |
| 8 | §6.1 E2E 强制 + test:p0-v16 + v1.4 共存 | 908 writeSet + §6.1 证据表 | 结构完整 |
| 9 | 角色隔离 + scope-check + 预落地对账 | §1.4；§6.2；§0.2 | 结构完整（scope-check 可执行性见 ISSUE） |
| 10 | maintainer 人类发布批准 | §9.10 | 结构完整 |

> **注**：P1 REQ-SHELL-010 未列入 §9.2「全部 P0」门槛，与 SNAP §优先级政策「P1 不单独阻塞 A/B」一致；不升格为异议。

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-PE-WSC8-R1-001 | P1 | `TASK-WSC-905` `writeSet` 含 `frontend/src/features/auth/composables/useCanWrite.ts` 并附散文：「**仅** `canWriteProduct`/`canImportProduct` ADMIN=true 与 903 矩阵对齐**片段**；**完整**导航门控归 906」。同文件 `TASK-WSC-906` 亦整文件列入 writeSet。虽 DAG 串行（905→906），但 §1.4 要求 Orchestrator 按各任务 `writeSet` **字面路径** scope-check；**无法**机械判定 905 diff 是否仅触及声明片段、未改导航可见性字段。同类缺陷见已关闭 `ISSUE-PE-WSC5-R1-002`（方法/片段级 carve-out）。 | 二选一写死：（a）**905 绝对 denyModify** `useCanWrite.ts`（及 spec），产品写/导入矩阵对齐仅在后端 + 903 矩阵验收；**906 独占** `useCanWrite.ts` 全部 FE 门控；或（b）905 保留整文件 writeSet 但 `acceptance`/`testScope` 钉死可测负例（不得改 `canSeeMyCatalog`/`canMaintainCatalog` 等导航字段），删除「仅片段」散文。 | REQ-RBAC-002, REQ-CAT-016 |
| ISSUE-PE-WSC8-R1-002 | P1 | 浏览表面跨任务边界未机械化：（1）Wave A **901∥902 并行**；902 `denyModify` `CatalogBrowsePage.vue`，但 `denyModify` 又写「若必须传 props 开 ISSUE 并入 901 或最小 mount 补丁由 reviewer 裁定」——并行启动时挂载/props 契约未定义，Orchestrator 无法预判 902 是否越界。（2）905 `denyModify` 对 `CatalogBrowsePage.vue`（归 901）保留「905 可改 mine 列表消费逻辑**若在同 composable 须开 ISSUE**」——`useCatalogBrowse.ts` 在 **901** writeSet、不在 905 writeSet，属 deny 内的软扩界。同类见 `ISSUE-PE-WSC5-R1-002`。 | （a）在 §0.3 或 902 `acceptance` **写死**：902 仅改 `CirculationSeatMap*` + L2 API，**不得**改 `CatalogBrowsePage`/`useCatalogBrowse`；若需 props，**901 先 VERIFIED** 或 props 契约写入 901 writeSet/acceptance 后再开 902。（b）删除 905 denyModify 中 composable 软例外；mine 列表 FE 消费若需改 composable，**明确归属 901**（dependsOn 调整）或 **905 字面列入** `useCatalogBrowse.ts` 且与 901 串行。 | REQ-CAT-014, REQ-CAT-015, REQ-CAT-016 |
| ISSUE-PE-WSC8-R1-003 | P1 | `TASK-WSC-907` `writeSet` 含叙述性/条件条目：（1）`frontend/.../editor/**`、`import/**`、`detail/**` 标注「**仅** `INDUSTRY_CATEGORY_OPTIONS` 回归断言相关」——glob 下任意文件可写，scope-check 无法核验改动是否「仅回归」。（2）`backend/.../controller/catalog/maintenance/**`（**若需** myCatalog scope 参数）——「若需」非字面路径集合，调度前边界未定。与 `ISSUE-PE-WSC5-R1-001` 叙述性 writeSet 同类。 | 写死字面边界：回归面改为具名 spec 路径（如 `*.spec.ts` 中 industry category 用例）或具名 SFC 文件列表；backend 要么 **无条件** 列入 maintenance controller/service 路径，要么拆独立子任务；删除「若需」「仅回归相关」散文。DEV 交付说明不得扩大 writeSet。 | REQ-CAT-017, REQ-CAT-013, REQ-CAT-019 |
| ISSUE-PE-WSC8-R1-004 | medium | §6 **并行写集互斥声明**表行「905 ∥ 906 \| 写集无交且 903 已 VERIFIED \| **允许**」与 §5 矛盾：两任务 **均** 将 `frontend/src/features/auth/composables/useCanWrite.ts`（及 spec）列入 writeSet。§6 DAG 已 905→906 串行，此行既误导并行调度，又错误声明「写集无交」。 | 修正并行表：（a）改为「**禁止并行**（906 dependsOn 905）」并删除「写集无交」；或（b）若保留「允许」语义，须先按 ISSUE-001 拆分 useCanWrite 所有权使写集 literally 不交。 | REQ-RBAC-002, REQ-SHELL-009 |
| ISSUE-PE-WSC8-R1-005 | P1 | 「我的目录」路由路径未冻结：`TASK-WSC-906` acceptance 写「`/my-catalog` **或 PO 确认路径，全计划统一**」；907 writeSet 同「`/my-catalog` 或统一路径」；§0.2 预落地迹象为 `/my-maintenance`。§6.1 场景 5–6、908 E2E 需 literal 深链/菜单断言，当前计划无法 mechanical 绑定单一路径，与 SNAP REQ-CAT-017「两入口路由/范围独立」的可测性不足。 | 在 906/907 `writeSet`（`routes.ts`）、`acceptance`、§6.1 场景 5–6 与 908 `writeSet` spec 中 **统一单一字面 path**（如 `/my-catalog` 或 `/my-maintenance` 二选一并全计划替换）；§0.2 对账表同步；删除「或 PO 确认路径」开放式表述。 | REQ-CAT-017, REQ-SHELL-009 |

## 非异议说明（刻意不升格）

- 903 `writeSet` 中 `package.json`/`pnpm-lock.yaml`「仅当 client 生成必需」：路径字面已列入，为使用约束；与 PLAN-WSC-5.2/6.2 同类口径。
- 904 Flyway `sql/migration/**` 独占 + §6.2 migrationReviewer：结构已绑定，migration 细目属 migrationReviewer。
- 908 `test:p0-v15`「若 spec 尚未存在，交付说明记录」：v1.15 共存为建议层，§9.8 强制项已钉 v1.16 + v1.4；不升格（若 QA 要求 v1.15 强制，由其开 ISSUE）。
- 905→906 串行共用 `useCanWrite.ts`：串行回流本身可接受；升格点仅在 **905 片段级 writeSet 表述**（ISSUE-001），非串行本身。
- SNAP §47 正文仍写 `status: DRAFT` 而 YAML 为 `APPROVED`：以 YAML + PO 批准事件为准；不构成计划结构缺陷。
- P1 REQ-SHELL-010 未进 908 `requirements`：与 SNAP「P1 不阻塞 A/B」一致。
- 缺独立 `allowModify` 字段：与已批准 WSC 系列「writeSet 即为写边界」口径一致。
- 他角色 Round 1 若另有 REQUEST_CHANGES，closeWhen 确认权属原提出者；本角色**不代关**。

## 决策

`REQUEST_CHANGES` — 本角色开放 ISSUE 数 = **5**（P1×4，medium×1）。

不伪造他角色 `APPROVE`；不关闭非本角色异议；全员独立共识与 Orchestrator 门禁通过前不得将本计划视为可派发实现权威；**不得**将 `status` 标为 `APPROVED` 或写入 `planning/approved/`；本评审**不**写 `ai/runs/**`、**不** commit。
