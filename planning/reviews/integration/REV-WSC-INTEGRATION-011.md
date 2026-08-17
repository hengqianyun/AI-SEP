# REV-WSC-INTEGRATION-011 — WSC V1.6 全量束集成审查（PLAN-WSC-8.3）

```yaml
reviewId: REV-WSC-INTEGRATION-011
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
snapshotStatus: APPROVED
actorInstance: integration-reviewer-wsc-011
mustDifferFrom:
  - developer-wsc-901
  - developer-wsc-902
  - developer-wsc-903
  - developer-wsc-903-r2
  - developer-wsc-903-sec-r1
  - developer-wsc-904
  - developer-wsc-904-mig-r1
  - developer-wsc-905
  - developer-wsc-906
  - developer-wsc-907
  - developer-wsc-907-r1
  - developer-wsc-908
  - code-reviewer-wsc-901
  - code-reviewer-wsc-902
  - code-reviewer-wsc-903
  - code-reviewer-wsc-903-r2
  - code-reviewer-wsc-904
  - code-reviewer-wsc-905
  - code-reviewer-wsc-906
  - code-reviewer-wsc-907
  - code-reviewer-wsc-907-r2
  - code-reviewer-wsc-908
  - tester-wsc-901
  - tester-wsc-902
  - tester-wsc-903
  - tester-wsc-904
  - tester-wsc-905
  - tester-wsc-906
  - tester-wsc-907
  - tester-wsc-908
  - security-reviewer-wsc-903
  - security-reviewer-wsc-903-r2
  - security-reviewer-wsc-905
  - security-reviewer-wsc-906
  - migration-reviewer-wsc-904
  - migration-reviewer-wsc-904-r2
decision: APPROVE
reviewedAt: 2026-08-17T16:05:00+08:00
contracts: wsc-contracts@2.3.3
contractsTargetState: wsc-contracts@2.3.2
functionalBaseline: SNAP-WSC-006 (wsc-contracts@2.3.2)
uxBaseline: SNAP-WSC-003
basedOn:
  - planning/approved/PLAN-WSC-8.3.md
  - ai/runs/RUN-WSC-011/state.yaml
  - ai/runs/RUN-WSC-011/events.jsonl
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-901.md .. DEV-TASK-WSC-908.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-901.md .. REV-TASK-WSC-908.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-903-R2.md
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-907-R2.md
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-903.md
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-903-R2.md
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-905.md
  - ai/runs/RUN-WSC-011/reviews/SEC-REV-TASK-WSC-906.md
  - ai/runs/RUN-WSC-011/reviews/MIG-REV-TASK-WSC-904.md
  - ai/runs/RUN-WSC-011/reviews/MIG-REV-TASK-WSC-904-R2.md
  - ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-901.md .. TESTRUN-TASK-WSC-908.md
  - ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-908.md  # evidenceId TESTRUN-WSC-E2E-V16
  - ai/runs/RUN-WSC-011/tests/e2e-908-v16.txt
  - ai/runs/RUN-WSC-011/tests/e2e-908-v14.txt
  - contracts/VERSION
  - contracts/rbac/matrix.yaml
  - contracts/openapi/openapi.yaml
  - contracts/ui/state-matrix.md
  - frontend/src/api/client.ts
  - product/requirements/SNAP-WSC-008.md
  - planning/reviews/integration/REV-WSC-INTEGRATION-008.md  # 格式对照
skillRef: ai/skills/integration-review/SKILL.md
skillStatus: draft
protocolNote: |
  技能 integration-review 仍为 draft（冒烟清单 _待填_）。本审查未伪造已激活冒烟命令清单，未重跑 Playwright。
  结论以 PLAN-WSC-8.3 §0/§3.1/§5/§6/§9 与已落盘 DEV/REV/TESTRUN/SEC/MIG 为准。
  本角色不得代批 SNAP、不得代写 maintainer APPROVE_RELEASE、不得改 state.yaml / events.jsonl。
```

## 结论

Wave A–E（TASK-WSC-901..908）写集互斥与串行交接成立；**唯一并行对**为 901∥902（Wave A）。events **append 序**满足 DAG：901∥902 → 903 → 904 → 905 → 906 → 907 → 908；后继 HUMAN DISPATCH 均在 `dependsOn` 任务 `TASK_VERIFIED` **之后**。禁止并行对（903∥907、905∥906、908∥feature）未发生。共享契约由 **903 独占**冻结为 **`wsc-contracts@2.3.3`**（PLAN §3.1 允许 WORKTREE 已为 2.3.3 时合并增量且不再 bump；四头同步）。跨任务语义（interceptor 200/403 vs 907 结果集、906 双入口壳 → 907 接页且保留 meta、`/my-catalog`、scope 枚举）证据链连贯。E2E：独立 `tester-wsc-908` **PASS**；`evidenceId: TESTRUN-WSC-E2E-V16`；§6.1 九场景（6 拆 6a/6b，10/10）；`test:p0-v14` 可执行。各任务 VERIFIED 门禁齐全（903/905/906 security；904 migration；907/908 无 auth-model-change / schema-migration **合理**）。本轮**无跨任务 P0/P1**。开放 P2 汇总于 §6，**不**阻塞进入 `RELEASE_REVIEW`。

**decision: APPROVE** — 允许进入 `RELEASE_REVIEW`。§9 未满足项（**maintainer `human/APPROVE_RELEASE`**）列于下文，**不**由本角色代批。

> 技能 `integration-review` 仍为 `draft`，本审查**未**伪造已激活冒烟命令清单；结论以计划 §6/§9 与已落盘证据为准。本审查**未**重跑 Playwright。

---

## 1. 波次束与写集检查表

| 波次 | 任务 | 计划写集（摘要） | 落盘路径抽查 | 互斥/冲突 | 结果 |
|---|---|---|---|---|---|
| A | 901 | CatalogBrowsePage / useCatalogBrowse / browse components（除座序图） | REV-901：筛选区 + spec + labels；**未改** CirculationSeatMap | 与 902 并行；独占 browse 筛选 | PASS |
| A | 902 | CirculationSeatMap + L2Distribution* | DEV/REV-902：**未改** CatalogBrowsePage / useCatalogBrowse / props | 与 901 并行允许；deny browse 页 | PASS |
| B | 903 | `contracts/**` + `frontend/src/api/**` | VERSION/matrix/OpenAPI/state-matrix **2.3.3**；client `CONTRACT_VERSION` | 独占契约/API；**未与 907 并行** | PASS |
| B | 904 | Flyway V7 + 企业实体/会话/用户 | V7；SysUser/Enterprise；SessionPrincipal；**deny** catalog model/controller | 独占 sql/enterprise | PASS |
| B | 905 | RbacMatrix / Interceptor / Browse list / editor·import 写 | interceptor scope-aware；mine 本企业；**deny** useCanWrite | 独占 interceptor/browse RBAC；**未与 906 并行** | PASS |
| C | 906 | WorkbenchLayout / useCanWrite / routes 导航 | 双入口菜单；`/my-catalog` meta；ADMIN 写 true | 独占壳层/composable/导航面 | PASS |
| D | 907 | maintenance 页+composable + routes 页级 + industry spec + BE scope | CatalogMaintenancePage；N vs M；**未改** interceptor / layout / api | 独占维护页+结果集；保留 906 meta | PASS |
| E | 908 | `tests/e2e/**`（V1.6） | `p0-wsc-v1.6.spec.ts`；`playwright.v16.config.ts`；**未改** feature | 独占 e2e；deny 生产 feature | PASS |

### 1.1 合并顺序（PLAN §6；以 events **append 序**为准）

| 规则 | 证据 | 结果 |
|---|---|---|
| A：901∥902 并行；VERIFIED 后开 903 | append：DISPATCH 901+902 → `TASK_VERIFIED` [901,902] @11:05:02 → NEXT 903 `dependsOnVerified:[901,902]` → HUMAN → DISPATCH 903 | PASS |
| B：903 在 901+902 VERIFIED 后 | 903 DISPATCH 在 901/902 `TASK_VERIFIED` 之后；`TASK_VERIFIED` 903 @11:34:01 | PASS |
| B：904 在 903 VERIFIED 后 | NEXT 904 `dependsOnVerified:[903]` → HUMAN @12:20 → DISPATCH 904；`TASK_VERIFIED` 904 @12:52:01 | PASS |
| B：905 在 903+904 VERIFIED 后 | NEXT 905 `dependsOnVerified:[903,904]` → HUMAN @12:55 → DISPATCH 905；`TASK_VERIFIED` 905 @13:25:01 | PASS |
| C：906 在 903+905 VERIFIED 后 | NEXT 906 `dependsOnVerified:[903,905]` → HUMAN @13:26 → DISPATCH 906；**905 已 VERIFIED**；`TASK_VERIFIED` 906 @13:56:01 | PASS |
| D：907 在 902+905+906 VERIFIED 后 | NEXT 907 `dependsOnVerified:[902,905,906]` → HUMAN @13:56:30 → DISPATCH 907；`TASK_VERIFIED` 907 @14:42:01 | PASS |
| E：908 在 901+902+905+906+907 VERIFIED 后 | NEXT 908 `dependsOnVerified:[901,902,905,906,907]` → HUMAN @14:43 → DISPATCH 908；`TASK_VERIFIED` 908 @15:43:01 | PASS |
| **禁止 903∥907** | 903 VERIFIED @11:34；907 DISPATCH @13:56:32 | PASS |
| **禁止 905∥906** | 905 VERIFIED @13:25:01；906 DISPATCH @13:26:02 | PASS |
| **禁止 908∥feature** | 908 在 907 `TASK_VERIFIED` 之后单任务串行 | PASS |
| 禁止以 Worker 完成速度替代 DAG | 无「未 VERIFIED 即启后继」的 **append** 证据 | PASS |

> **时间戳 vs append**：Wave A `HUMAN_DECISION` 的 `ts`（10:44）早于规划 R3 append（11:15），但 jsonl **行序**为先计划批准再派发 A。state 中 908 HUMAN `at: 14:41` 早于 907 `TASK_VERIFIED`（14:42），但 events **append** 为 907 VERIFIED → 再 HUMAN/DISPATCH 908。以 append / `dependsOnVerified` 语义序为准（见 OBS-INT-V16-004）。

### 1.2 §0.3 文件级互斥裁决

| 冲突面 | 规则 | 抽查 | 结果 |
|---|---|---|---|
| CatalogBrowsePage / useCatalogBrowse | 901 独占；902 **不得**改 | DEV-902 / REV-902：未改 page/composable/props；独立 `selectedL1Id` | PASS |
| CirculationSeatMap / L2Distribution | 902 独占；901 deny | REV-901：SeatMap 变更归属 902 | PASS |
| contracts / `frontend/src/api/**` | 903 独占 | 后继 DEV/REV denyModify；907 未为 P2 改 api | PASS |
| `**/sql/**` / 企业实体 | 904 独占 | REV-904：仅 V7；deny catalog | PASS |
| RbacMatrix / Interceptor / Browse list | 905 独占 | 907 REV：未改 Interceptor；906 deny backend | PASS |
| useCanWrite.ts | **906 独占全文**；905 deny | DEV-905 声明不改；906 对齐 ADMIN 写 true | PASS |
| WorkbenchLayout | 906 独占；907 deny | REV-907-R2：未给 FIND-001 改 layout | PASS |
| routes.ts 导航/meta | 906 → 907 串行；907 **不得**删 906 双入口 | 实地：`/my-catalog` title「我的目录（本企业/本人）」；`/catalog/maintenance`「目录维护（全量）」；`/admin/users` 仍在；`/my-maintenance` → `/my-catalog` | PASS |
| CatalogMaintenancePage / BE scope | 907 独占 | 907 writeSet；905 交接结果集给 907 | PASS |
| `tests/e2e/**` | 908 独占 | 901..907 deny；REV-908：未改 frontend/backend/contracts | PASS |
| `frontend/src/styles/**` / theme | **无写任务** | 全任务只读 | PASS |
| `ai/runs/**/state.yaml` | **无写任务** | 本审查未写 | PASS |

### 1.3 共享制品所有权（§0.3 / §3）

| 制品 | 唯一写任务 | 后继行为 | 结果 |
|---|---|---|---|
| `contracts/**` / `frontend/src/api/**` | 903 | 904..908 只读消费 2.3.3 | PASS |
| sys_user / 企业 / 会话 enterprise | 904 | 905+ 只读消费 principal | PASS |
| Interceptor / RbacMatrix / mine 本企业 | 905 | 907 只读拦截器；消费 200/403 | PASS |
| WorkbenchLayout / useCanWrite / 导航 meta | 906 | 907 扩页级、不删双入口 | PASS |
| maintenance 页 + 结果集 + industry spec | 907 | 908 只读测 | PASS |
| `tests/e2e/**` | 908 | 901..907 deny | PASS |

---

## 2. 共享契约抽查（三头）

| 头 | 期望（state / PLAN 头） | 抽查 | 结果 |
|---|---|---|---|
| state `contractsTarget` | **2.3.2** | `ai/runs/RUN-WSC-011/state.yaml`：`wsc-contracts@2.3.2` | 标签仍 2.3.2 |
| PLAN `contractsTarget` / §9.3 | **2.3.2**（§3.1 允许对账至 2.3.3） | PLAN-WSC-8.3 YAML + §3.1 | 头字段 2.3.2；对账例外成立 |
| 903 冻结 | 2.3.2 **或** 对账 2.3.3 且不再升 | DEV/TESTRUN-903：四头 **2.3.3**；未再 bump | **冻结 = 2.3.3** |
| `contracts/VERSION` | 与 903 冻结同一 semver | 文件内容 `2.3.3` | PASS（对齐冻结） |
| matrix.yaml `version` | 同一 semver | `"2.3.3"` | PASS |
| OpenAPI `info.version` | 同一 semver | `2.3.3` | PASS |
| `ui/state-matrix.md` 版本头 | 同一 semver | `wsc-contracts@2.3.3` | PASS |
| client `CONTRACT_VERSION` | 同一 semver | `frontend/src/api/client.ts` = `'2.3.3'` | PASS |

**分叉判定**：state / PLAN 头仍写 `2.3.2`，树内权威冻结为 **2.3.3**。PLAN §3.1 明文：「若 WORKTREE 已为 2.3.3：**合并 V1.6 增量至 2.3.3 且不再升**」。四头同步、matrix 逐 cell、E2E 均对 **2.3.3** 可测。§9.3「V1.6 增量无未决冲突」**可测**，故 **不升 P1**。记 OBS-INT-V16-005（发布标签应对齐 2.3.3）。

### 2.1 §3.1 矩阵 / scope 枚举（903 ↔ 905/907）

| 契约项 | 抽查 | 结果 |
|---|---|---|
| maintenance `scope=full\|myCatalog` | OpenAPI enum；`RbacMatrix.MAINTENANCE_SCOPE_*`；907 `isMyCatalogScope` 字面 `myCatalog` | **一致** |
| ADMIN `catalogMaintenanceApi` full **200** | 905 interceptor + 907 full 全平台 | **一致** |
| PROVIDER `scope=full` **403**；`myCatalog` **200** ownCreateBy | 905 TESTRUN `905-provider-maintenance-403`；907 BE 本人 | **一致** |
| ADMIN `myCatalogApi` **200 ownEnterprise** | 905：interceptor 200（结果集交 907）；907：`adminDualEntry_fullN_myCatalogM` M≤N | **交接闭合** |
| `mine=true` 本企业（非 create_by-only） | 903 OpenAPI；905 `create_by → enterprise_id` | **一致** |
| 全链无企业过滤；浏览无 `industryCategory` | 902/905/901 + E2E #1/#3 | **一致** |
| `l2-distribution?l1CategoryId=` | 902 BE + 903 OpenAPI/client | **一致** |
| 903..908 对 contracts/api | denyModify / 只读（907 用 `apiRequest?scope=` 规避生成 PUT 无 scope） | PASS（P2 见 FIND-907-002） |

残差（不阻塞）：`tests/contracts/check-contracts.mjs` 仍钉死 expected **1.1.0**（TESTRUN-903 如实 FAIL；由 `assert-903-p0.mjs` 107/0 覆盖语义）。

---

## 3. 跨任务语义抽查结论

| 切面 | 任务对 | 抽查要点 | 证据 | 结论 |
|---|---|---|---|---|
| 契约 → 实现 | 903↔904/905/906/907 | 2.3.3 schema/码由后继消费，不重写契约 | 各 denyModify + VERSION 抽查 | **一致** |
| 会话 enterprise | 904↔905/906 | principal `enterpriseId`+`enterpriseName` 单一真源 | TESTRUN-904；905 仅信 SessionPrincipal | **一致** |
| Interceptor 200 vs 结果集 | 905↔907 | 905：PROVIDER full 403 / myCatalog 200；ADMIN 两 scope 200。907：ADMIN myCatalog **本企业**、PROVIDER **ownCreateBy** | SEC-REV-905 FIND-002 Owner=907；TESTRUN-907 N vs M **PASS** | **交接闭合**（非冲突） |
| 906 壳 → 907 页 | 906↔907 | 双入口菜单 + `/my-catalog` 壳；907 接 CatalogMaintenancePage；**不删** 906 meta.title / `/admin/users` / redirect | routes 实地；TESTRUN-907：WorkbenchLayout.spec 8/8 仍绿 | **一致** |
| useCanWrite 独占 | 905↔906 | 905 仅 BE；906 ADMIN `canWriteProduct/Import` true | DEV-905 deny；DEV/REV/SEC-906 | **一致** |
| 901∥902 筛选 vs 座序图 | 901↔902 | Cascader 与 L1 Select **不联动**；无企业筛 | REV-902 decouple；E2E #1/#2 | **一致** |
| 浏览无 industry vs 编辑回归 | 901↔907 | 浏览不传字段；editor/import/detail 复用枚举 | TESTRUN-901 query 负例；TESTRUN-907 industry spec；E2E #8 | **一致** |
| PO 锁：ADMIN 双入口 | 906↔907↔908 | 目录维护全量 + 我的目录本企业 | 菜单 testid；E2E #5/#7 | **一致** |
| PO 锁：PROVIDER 仅我的目录 | 906↔905↔908 | 无维护菜单/深链；API full 403 | E2E #6a；905 403 | **一致** |
| PO 锁：无企业管理页；座序图无企业筛；全链无企业过滤；路由 `/my-catalog` | 多任务 | 无企管路由；SeatMap 无企业控件；全链 parity | REV-904 无 CRUD UI；902/E2E #2/#3；routes `/my-catalog` | **一致** |
| V1.4 回归可执行 | 908 | `test:p0-v14` 可跑；失败为 V1.6 intentional delta | TESTRUN-908 C2 `PASS_WITH_KNOWN_DELTAS` | **一致**（非 908 FAIL） |

未发现「路径无冲突但语义冲突」的跨任务 P0/P1。FIND-SEC-WSC-905-002（ADMIN myCatalog 仍全平台）由 907 结果集实现 **闭合交接**；905 报告内该 id 仍标 OPEN 属单任务台账，**不**构成集成未清 P1。

---

## 4. 证据链核对

### 4.1 任务门禁矩阵

| 任务 | developer | codeReviewer | tester | security | migration | REV | TESTRUN | state.verified |
|---|---|---|---|---|---|---|---|---|
| 901 | developer-wsc-901 | code-reviewer-wsc-901 | tester-wsc-901 | **不要求**（ux-filter；无 auth-model-change） | N/A | APPROVE | PASS | true |
| 902 | developer-wsc-902 | code-reviewer-wsc-902 | tester-wsc-902 | **不要求**（visualization） | N/A | APPROVE | PASS | true |
| 903 | developer-wsc-903（-r2 / -sec-r1） | code-reviewer-wsc-903-r2 | tester-wsc-903 | **SEC-REV R2 APPROVE** | N/A（无 sql） | APPROVE（R2） | PASS（assert 107/0；check-contracts 工具链 FAIL 已披露） | true |
| 904 | developer-wsc-904（-mig-r1） | code-reviewer-wsc-904 | tester-wsc-904 | **不要求**（§6.2 绑定 migration） | **MIG-REV R2 APPROVE** | APPROVE | PASS | true |
| 905 | developer-wsc-905 | code-reviewer-wsc-905 | tester-wsc-905 | **SEC-REV APPROVE** | N/A（deny sql） | APPROVE | PASS | true |
| 906 | developer-wsc-906 | code-reviewer-wsc-906 | tester-wsc-906 | **SEC-REV APPROVE** | N/A | APPROVE | PASS | true |
| 907 | developer-wsc-907（-r1） | code-reviewer-wsc-907-r2 | tester-wsc-907 | **不要求**（riskTags: maintenance-reuse / scope-isolation / enum-regression；授权模型已由 905 SEC 覆盖） | **不要求**（未触 sql） | APPROVE（R2） | PASS | true |
| 908 | developer-wsc-908 | code-reviewer-wsc-908 | tester-wsc-908 | N/A | N/A | APPROVE | PASS + **TESTRUN-WSC-E2E-V16** | true |

角色独立性：各任务 developer ≠ codeReviewer ≠ tester（及适用 security/migration）actorInstance **互不相同**。本 `integration-reviewer-wsc-011` 不在上述集合中。

907/908 无 SEC/MIG：**合理**（计划 §6.2 仅将 `auth-model-change` 绑 903/905/906、`schema-migration` 绑 904）。

### 4.2 E2E V16 + §6.1（只核验落盘，不重跑）

| 项 | 结果 |
|---|---|
| E2E 证据 | `TESTRUN-TASK-WSC-908.md`：`evidenceId: TESTRUN-WSC-E2E-V16`；`decision: PASS`；独立 `tester-wsc-908`；**未**采信 DEV 自测 |
| C1 | `pnpm --dir tests/e2e run test:p0-v16` exit **0**；**10 passed**；摘录 `ai/runs/RUN-WSC-011/tests/e2e-908-v16.txt`（§6.1-1..9，6 拆 6a/6b） |
| C2 v14 共存 | `test:p0-v14` **可执行**；exit 1；6 passed / 9 failed = intentional V1.6 delta / 预存 UX；`failedCommandCount` 不计 C2 |
| C3 v15 | N/A（无 spec / 无 script；未伪造） |
| 报告落点 | 计划 `tests/e2e/reports/p0-wsc-v1.6/`；根 `.gitignore` 排除该目录（FIND-WSC-908-001 P2）；正式门禁以 Run 内 TESTRUN + `e2e-908-v16.txt` 为准 |
| §6.1 强制项 1–9 | 全部 **PASS**（见 TESTRUN-908 勾选表） |
| P0 开放缺陷 | **0**（908 bugsOpen 空；907 FIND-001 CLOSED） |

本审查 **未**执行 Playwright，**未**把 draft 技能冒烟清单写成 PASS。

### 4.3 过程残差（已关闭，供审计）

| 项 | 说明 | 集成判定 |
|---|---|---|
| 903 R1 REQUEST_CHANGES + SEC R1 | create_by-only 叙述 / JSDoc → R2 + SEC-R2 APPROVE | **可接受**：最终门禁齐全 |
| 904 MIG R1 REQUEST_CHANGES | V7 回滚/备份/审计 → MIG-R2 APPROVE | **可接受** |
| 907 R1 REQUEST_CHANGES | 同实例 scope 闭包 P1 → R2 CLOSED + tester PASS | **可接受**：写集内修复；未改 layout/api |
| 905 FIND-SEC-002 Owner=907 | interceptor 200 + 结果集交 907 | **可接受**：907 N vs M 闭合 |

---

## 5. §9 发布门禁技术项预检

| # | 门禁项 | 状态 | 说明 |
|---|---|---|---|
| 1 | **SNAP-WSC-008 已 APPROVED** | **已满足** | `status: APPROVED`；`approvedAt: 2026-08-17`；state `snapshotStatus: APPROVED`。本审查**不**代批 |
| 2 | 全部 P0 REQ（§7 九条）对应任务 VERIFIED（含 908） | **已满足** | `verifiedTaskIds` = 901..908 |
| 3 | 契约 V1.6 增量无未决冲突；maintenance/enterprise/matrix 可测 | **已满足**（冻结 **2.3.3**） | 四头同步；标签 2.3.2 见 OBS-INT-V16-005，**不**使门禁不可测 |
| 4 | Wave A：浏览筛选 + 座序图 L1 | **已满足** | TESTRUN-901/902 + E2E #1/#2 |
| 5 | Wave B：企业归属 + mine 本企业 + ADMIN 可写 + maintenance scope + 全链无企业 filter；security + migration | **已满足** | 903 SEC-R2；904 MIG-R2；905 SEC；TESTRUN-904/905 |
| 6 | Wave C/D：ADMIN 双入口、PROVIDER 仅我的目录、维护 UX、行业类别非浏览 | **已满足** | TESTRUN-906/907 + E2E #5–8 |
| 7 | UX/RBAC 相对 SNAP-003 / V1.5 无 P0 回退 | **已满足**（制品+E2E 口径） | E2E #9；v14 可执行 |
| 8 | §6.1 E2E：`TESTRUN-WSC-E2E-V16`；P0=0 | **已满足** | TESTRUN-908（独立 tester；本 REV 不重跑） |
| 9 | developer ≠ CR ≠ tester；字面 scope-check；适用 security/migration | **已满足** | 见 §4.1 |
| 10 | maintainer 人类发布批准 | **未满足** | **未见** `ai/runs/RUN-WSC-011/approvals.yaml`；本审查**不得**伪造 |

### 计划批准（支撑项，非代批发布）

| 项 | 状态 |
|---|---|
| `PLAN-WSC-8.3` 经委员会并发布 `planning/approved/` | **已满足**（state `planStatus: APPROVED`；`approvedPlanPath` 可检） |
| SNAP-WSC-008 | **已满足**（与 008 轮次 SNAP 仍 IN_REVIEW 不同） |

### 进入 RELEASE_REVIEW / 发布前缺口（须关闭，非本角色批准）

1. **`maintainer` 人类明确批准**发布包（`human/APPROVE_RELEASE` / `approvals.yaml`）。
2. 可选：将 state `contractsTarget` 标签与冻结 **2.3.3** 对齐（OBS-INT-V16-005；不阻塞本 APPROVE）。

> 集成 APPROVE **不等于** 生产发布批准，**不等于** 代写 `APPROVE_RELEASE`。

---

## 6. Findings

### P0（跨任务）

无。

### P1（跨任务）

无。

### 开放 P2 汇总（不因此 REQUEST_CHANGES）

| id | 来源任务 | 摘要 | 跨任务语义冲突？ |
|---|---|---|---|
| FIND-WSC-901-R1-001/002 | 901 | 副标题旧称；filter theme 硬编码 hex | 否 |
| FIND-WSC-902-001/002 | 902 | SeatMap 仍 `apiRequest` 未换 903 `getL2Distribution`；L1 无 abort | 否（E2E #2 PASS；902 已 VERIFIED 后不可改 api） |
| FIND-WSC-903-R1-002/003 | 903 | Session 类型 optional；list scope TS optional vs OpenAPI required | 否 |
| FIND-SEC-WSC-903-002 | 903 | OpenAPI PUT/POST 无 query `scope` | 否（与 907-002 同残差；907 workaround + 905 fail-closed） |
| FIND-SEC-WSC-903-003 | 903 | matrix PROVIDER import `on: valid` vs 字面 ownCreateBy | 否（905 负例仍 ownCreateBy） |
| FIND-WSC-904-001/002/003 | 904 | SessionViews 残留；enterpriseId `"0"`；smoke 注释 | 否 |
| FIND-MIG-WSC-904-002/004 | 904 | DDL 无守卫；unassigned 无部分唯一 | 否 |
| FIND-WSC-905-001/002 | 905 | V1.5 SessionAuth 仍期望 ADMIN 写 403；provider myCatalog 测缺异主负例（他测已覆盖） | 否 |
| FIND-SEC-WSC-905-001 | 905 | 会话 enterpriseId 空串时 mine 可能退化为全链 | 否（写路径仍 403；不扩大机密面） |
| FIND-SEC-WSC-905-002 | 905 | 原「ADMIN myCatalog 全平台」Owner=907 | **交接已由 907 闭合**；905 台账可保持 OPEN |
| FIND-WSC-906-001/002 | 906 | browse/import spec 仍期望 ADMIN 写 false（denyModify 外） | 否（产品行为 E2E #4 PASS；测试债） |
| FIND-SEC-WSC-906-001/002 | 906 | 分类/编辑深链未进 `isDeepLinkAllowed`；`requiresProvider` 残留字面 | 否（非 §4.1 点名四路径；API 仍 905 403） |
| FIND-WSC-907-002 | 907 | 生成 PUT/POST client 无 `scope`；907 用 `apiRequest?scope=` | 否（与 903-002 同源；未改 api） |
| FIND-WSC-908-001/002 | 908 | gitignore 排除报告目录；L2/未分类/跨企编码断言偏软 | 否（独立 TESTRUN 已重跑 C1；场景非空） |

### 观察 / 非阻塞

| id | severity | evidence | closeWhen |
|---|---|---|---|
| OBS-INT-V16-001 | observation | skill `integration-review` 仍 draft；冒烟清单 `_待填_` | Tech Lead 激活前不伪造冒烟命令 |
| OBS-INT-V16-002 | observation | 未见 `approvals.yaml` / maintainer APPROVE_RELEASE | maintainer 人类批准写入 |
| OBS-INT-V16-003 | observation | 本审查未重跑 Playwright；E2E 以 `tester-wsc-908` 落盘为准 | 无需本角色补跑 |
| OBS-INT-V16-004 | observation | 部分 HUMAN `ts` / state `at` 早于前任务 VERIFIED 墙钟，但 **jsonl append 序**合规 | 以 append/`dependsOnVerified` 为准；编排侧可选规范化时间戳 |
| OBS-INT-V16-005 | observation | state/PLAN 头 `contractsTarget=2.3.2` vs 903 冻结/`VERSION`=**2.3.3**（§3.1 允许） | 发布包标签对齐 2.3.3；**不**构成门禁不可测 |
| OBS-INT-V16-006 | observation | `check-contracts.mjs` expected 1.1.0 vs 实际 2.3.3 | 工具链跟进；语义已由 assert-903-p0 覆盖 |
| OBS-INT-V16-007 | observation | FIND-WSC-903-R1-004 全仓 typecheck WorkbenchLayout：903 记 906 范围；906 门禁为 Vitest 非全仓 tsc | 可选发布前全仓 typecheck；非跨任务 P1 |
| OBS-INT-V16-008 | observation | 903 DEV YAML `developer-wsc-903-sec-fix` vs events `developer-wsc-903-sec-r1` | 实例命名漂移；mustDifferFrom 仍互异 |

---

## 7. 决策

```yaml
reviewId: REV-WSC-INTEGRATION-011
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
snapshotStatus: APPROVED
decision: APPROVE
actorInstance: integration-reviewer-wsc-011
crossTaskP0: 0
crossTaskP1: 0
contractsFrozen: wsc-contracts@2.3.3
e2eEvidenceId: TESTRUN-WSC-E2E-V16
```

```text
decision: APPROVE
```

- **含义**：全量束（901∥902 → 903 → 904 → 905 → 906 → 907 → 908）集成一致性通过；可进入 `RELEASE_REVIEW` / 发布门禁流程（技术前提已齐，待 maintainer）。
- **不含义**：不宣称 V1.6 已可生产发布；**不**代写 maintainer `APPROVE_RELEASE`；**不**将 Run 标 `COMPLETED`；**不**代 tester 宣称新的 E2E 重跑 PASS。
- **权限声明**：审查者未修改业务源码、契约、单任务 REV/TESTRUN/BUG、或 `ai/runs/**` state/events/approvals；仅写入本报告。
