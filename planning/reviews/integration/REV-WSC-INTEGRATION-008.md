# REV-WSC-INTEGRATION-008 — WSC V1.4 全量串行束集成审查

```yaml
reviewId: REV-WSC-INTEGRATION-008
runId: RUN-WSC-008
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
snapshotStatus: IN_REVIEW
actorInstance: integration-reviewer-wsc-008
decision: APPROVE
reviewedAt: 2026-08-06T14:45:00+08:00
contracts: wsc-contracts@2.2.0
functionalBaseline: SNAP-WSC-004 (wsc-contracts@2.1.0)
uxBaseline: SNAP-WSC-003
basedOn:
  - planning/approved/PLAN-WSC-5.2.md
  - ai/runs/RUN-WSC-008/state.yaml
  - ai/runs/RUN-WSC-008/events.jsonl
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-601.md .. DEV-TASK-WSC-605.md
  - planning/tasks/REV-TASK-WSC-601.md .. REV-TASK-WSC-605.md
  - planning/tasks/TESTRUN-TASK-WSC-601.md .. TESTRUN-TASK-WSC-605.md
  - planning/tasks/REV-SEC-TASK-WSC-601.md
  - planning/tasks/REV-SEC-TASK-WSC-602.md
  - planning/tasks/REV-SEC-TASK-WSC-603.md
  - planning/tasks/REV-MIG-TASK-WSC-602.md
  - planning/tasks/TESTRUN-TASK-WSC-605.md  # releaseEvidenceId TESTRUN-WSC-E2E-V14
  - contracts/VERSION
  - tests/e2e/reports/p0-wsc-v1.4/
  - product/requirements/SNAP-WSC-005.md
  - planning/reviews/integration/REV-WSC-INTEGRATION-004.md  # 格式对照
skillRef: ai/skills/integration-review/SKILL.md
skillStatus: draft
protocolNote: |
  protocolRepair 曾作废主会话伪造的 SNAPSHOT_APPROVED / PLAN-WSC-5.1；
  SNAP-WSC-005 仍为 IN_REVIEW（approvedAt: null）。本审查不得代批 SNAP / maintainer 发布。
```

## 结论

全量串行五波（W1..W5 = TASK-WSC-601..605）写集互斥成立；**无同波并行对**；`dependsOn` 与 events **语义序**一致（601→602→603→604→605，后波均在前任务 `TASK_VERIFIED` 之后按 append 序启动）。共享契约 `wsc-contracts@2.2.0` 由 **601 独占**升版/对账；602..605 只读消费（各 DEV/REV denyModify 声明可检）。跨任务语义（RBAC / 我的产品 / 公共目录只读 / 座序图挂载 / E2E）证据链连贯：`TESTRUN-WSC-E2E-V14` PASSED（15/15；报告 `tests/e2e/reports/p0-wsc-v1.4/`）。各任务 VERIFIED 门禁齐全（含 601/602/603 security；602 migration；604 无 security/migration **合理**——riskTags 仅 visualization/perf-aggregation；605 E2E）。本轮**无跨任务 P0/P1**。

**decision: APPROVE** — 允许进入发布门禁预检 / `RELEASE_REVIEW`。§9 未满足项（**SNAP-WSC-005 正式批准**、**maintainer APPROVE_RELEASE**）列于下文，**不**由本角色代批。

> 技能 `integration-review` 仍为 `draft`，本审查**未**伪造已激活冒烟命令清单；结论以计划 §2.3/§3/§6/§9 与已落盘证据为准。

---

## 1. 串行束与写集检查表

| 波次 | 任务 | 计划写集（摘要） | 落盘路径抽查 | 互斥/冲突 | 结果 |
|---|---|---|---|---|---|
| W1 | 601 | `contracts/**` ∥ `frontend/src/api/**` | VERSION 2.2.0、matrix、OpenAPI、admin/auth/catalog client | 单任务；独占契约/API | PASS |
| W2 | 602 | sys_user 迁移 + auth/users + RoleSwitcher 下线；壳层 `admin-users` 白名单 | V6 migration、AdminUser*、SessionAuth、UsersAdminPage；**deny** RbacMatrix/useCanWrite | 单任务；与 603 文件级互斥（§2.3） | PASS |
| W3 | 603 | 我的产品 + 去写 + create_by + `RbacMatrix`/`WriteAuthorizationInterceptor`/`useCanWrite`/`CatalogBrowsePage` | mine 过滤、公共目录无写、导入宿主 `/my-products`；L2 **空壳迁出** | 单任务；deny L2 实现体 / CirculationSeatMap | PASS |
| W4 | 604 | `L2DistributionController/Service` + `CirculationSeatMap.vue` | L2 独立类；座序图 Top5/hover；Browse Page **未改**（只读 `showSeatMap`） | 单任务；deny CatalogBrowsePage / contracts | PASS |
| W5 | 605 | `tests/e2e/**`（V1.4） | `p0-wsc-v1.4.spec.ts`；报告 `p0-wsc-v1.4/`；未改 frontend/backend/contracts | 单任务串行；deny 生产 feature | PASS |

### 1.1 合并顺序（PLAN §6）

| 规则 | 证据 | 结果 |
|---|---|---|
| W1：601 单独；VERIFIED 后启 W2 | events：601 `TASK_VERIFIED` @16:15:01 → PO 确认 W2 → 602 DISPATCH @16:34:01；`dependsOnVerified:[601]` | PASS |
| W2：602 在 601 VERIFIED 后 | 602 gates 含 security+migration；`TASK_VERIFIED` @18:00:01 → W3 | PASS |
| W3：603 在 602 VERIFIED 后 | 603 DISPATCH @18:00:04；`dependsOnVerified:[601,602]` | PASS |
| W4：604 在 603 VERIFIED 后 | **append 序**：603 `TASK_VERIFIED` @11:05:01 → NEXT W4 → HUMAN/DISPATCH 604（见 OBS 时间戳交错） | PASS（语义序） |
| W5：605 在 604 VERIFIED 后 | 604 `TASK_VERIFIED` @11:20:01 → W5；605 DISPATCH @14:05；`dependsOnVerified:[601..604]` | PASS |
| **禁止任意两任务同波并行** | 五波均为单任务；无并行 DISPATCH | PASS |
| 禁止以 Worker 完成速度替代 DAG | 无「未 VERIFIED 即启后继」的 append 证据 | PASS |

### 1.2 §2.3 文件级互斥裁决

| 冲突面 | 规则 | 抽查 | 结果 |
|---|---|---|---|
| L2 vs mine 后端 | 604 独占 L2*；603 deny；Browse 无 l2 映射 | `L2DistributionController` 存在；`CatalogBrowseController` 仅文档指向 L2；无 `/l2-distribution` 映射 | PASS |
| RbacMatrix / Interceptor | 603 独占；602 deny | 602 R2：不依赖/不改 useCanWrite；603 拥有矩阵实现 + REV-SEC-603 | PASS |
| CatalogBrowsePage | 603 独占；604 仅 CirculationSeatMap | 604 REV：Page 未改；`showSeatMap` 挂载约定 | PASS |
| useCanWrite | 603 独占；602 deny | 602 FIND-002 CLOSED（内联 ADMIN） | PASS |
| 壳层/路由白名单 | 602=`admin-users`；603=`my-products` 收口且不删 602 | 603 TESTRUN：壳层断言保留 nav-users / 角色只读 | PASS |
| E2E | 605 独占 `tests/e2e/**` | 601..604 deny；605 REV scope PASS | PASS |

### 1.3 共享制品所有权（§3.2）

| 制品 | 唯一写任务 | 后继行为 | 结果 |
|---|---|---|---|
| `contracts/**` / `frontend/src/api/**` | 601 | 602..605 只读消费 2.2.0 | PASS |
| sys_user / 登录 / 用户 API / 切角色下线 | 602 | 603+ 只读消费会话 | PASS |
| `RbacMatrix` / Interceptor / `useCanWrite` / 我的产品 / BrowsePage | 603 | 604 deny Page；605 只读测 | PASS |
| L2 Controllers + CirculationSeatMap | 604 | 603 deny 实现体 | PASS |
| `tests/e2e/**` | 605 | 601..604 deny | PASS |
| `frontend/src/styles/**` / theme | **无写任务** | 全任务只读 | PASS |
| `ai/runs/**/state.yaml` | **无写任务** | 本审查未写 | PASS |

---

## 2. 共享契约抽查（2.2.0）

| 制品 | 期望 | 抽查 | 结果 |
|---|---|---|---|
| `contracts/VERSION` | **2.2.0** | 文件内容 `2.2.0` | PASS |
| `CONTRACT_VERSION` client | 2.2.0 | `frontend/src/api/client.ts` | PASS |
| RBAC matrix | ADMIN 产品写/导入 hidden+403；PROVIDER 写+myProducts；USER 只读；userManage 仅 ADMIN | REV/TESTRUN-601 + REV-SEC-601/603 | PASS |
| 切角色 | `POST /auth/session/role` → 410 + `ERR_ROLE_SWITCH_DISABLED`；LoginRequest 无 role | 601 assert + 602 集成测 + E2E #1 | PASS |
| `/admin/users` | 命名 AdminUser*；软删 PUT deleted；响应禁 password/hash | 601 + 602 TESTRUN | PASS |
| `mine` + L2 API | `GET products?mine`；`GET /catalog/l2-distribution` | 601 OpenAPI/client；603/604 实现 | PASS |
| 叙述对齐 | 无 ADMIN 可产品写；state-matrix 导入宿主=我的产品；版本头 2.2.0 | REV-601 | PASS |
| 2.1.0 不回退 | typeSpecific / 导入四态 / 报告白名单 | TESTRUN-601 回归 + E2E #7 | PASS |
| 602..605 对 contracts/api | denyModify / 只读 | 各 DEV/REV 声明未改 | PASS |

残差（不阻塞）：`tests/contracts/check-contracts.mjs` 仍钉死 expected 1.1.0（TESTRUN-601 如实 FAIL；由 `assert-601-p0.mjs` 64/0 覆盖语义）——工具链滞后，非契约交付冲突。

---

## 3. 跨任务语义抽查结论

| 切面 | 任务对 | 抽查要点 | 证据 | 结论 |
|---|---|---|---|---|
| 契约 → 实现 | 601↔602/603/604 | 2.2.0 schema/码由后继实现消费，不重写契约 | 各 denyModify + VERSION 抽查 | **一致** |
| 真登录 / 切角色禁用 | 602↔605 | 会话绑定角色；UI 无自由切角色；410 | TESTRUN-602；E2E #1 | **一致** |
| 用户管理仅 ADMIN | 601↔602↔605 | API+UI；PROVIDER/USER 不可达 | TESTRUN-602；E2E #2 | **一致** |
| 产品写仅 PROVIDER | 601 matrix↔603↔605 | RbacMatrix + Interceptor；ADMIN 写 403；分类/维护保留 | REV-SEC-603；E2E #3/#5 | **一致** |
| 我的产品 / create_by | 603↔605 | mine 列表；空/异主 403；导入宿主回 my-products | TESTRUN-603；E2E #4 | **一致** |
| 公共目录只读 | 603↔605 | 结构无增改导（三角色） | Vitest writeVisible；E2E #3 | **一致** |
| 座序图挂载 | 603↔604↔605 | Page 预留 `showSeatMap`；604 实现组件；仅 catalog；mine 无图 | CatalogBrowsePage + CirculationSeatMap；E2E #6 | **一致** |
| L2 文件归属 | 603↔604 | L2 不在 Browse Controller | prelandReconcile_604；Controller 注释 | **一致** |
| 壳层串行收口 | 602↔603 | 603 加 my-products 不回退 admin-users / 角色只读 | 603 TESTRUN 壳层断言 | **一致** |
| V1.3 回归 | 603/605 | 导入四态 + OpenAPI 编辑不回退 | ProductEditor/Import 测；E2E #7 | **一致** |

未发现「路径无冲突但语义冲突」的跨任务 P0/P1。

---

## 4. 证据链核对

### 4.1 任务门禁矩阵

| 任务 | developer | codeReviewer | tester | security | migration | REV | TESTRUN | state.verified |
|---|---|---|---|---|---|---|---|---|
| 601 | developer-wsc-601 | code-reviewer-wsc-601 | tester-wsc-601 | **REV-SEC APPROVE** | N/A（无 sql） | APPROVE | PASS（assert 64/0；check-contracts 工具链 FAIL 已披露） | true |
| 602 | developer-wsc-602(-r2) | code-reviewer-wsc-602-r2 | tester-wsc-602 | **REV-SEC APPROVE** | **REV-MIG APPROVE** | APPROVE（R2） | PASS | true |
| 603 | developer-wsc-603(-r2/-r3) | code-reviewer-wsc-603-r3 | tester-wsc-603-r2 | **REV-SEC APPROVE** | N/A（未触 sql；DEV 声明） | APPROVE（R3） | PASS（retest；BUG CLOSED） | true |
| 604 | developer-wsc-604 | code-reviewer-wsc-604-r1 | tester-wsc-604-r1 | **不要求**（riskTags: visualization/perf-aggregation） | **不要求** | APPROVE | PASS | true |
| 605 | developer-wsc-605 | code-reviewer-wsc-605-r1 | tester-wsc-605-r1 | N/A | N/A | APPROVE | PASS + **TESTRUN-WSC-E2E-V14** | true |

角色独立性：各任务 developer ≠ codeReviewer ≠ tester（及适用 security/migration）actorInstance **互不相同**，可核验。

### 4.2 E2E V14 + §6.1

| 项 | 结果 |
|---|---|
| E2E 证据 | `releaseEvidenceId: TESTRUN-WSC-E2E-V14` ⊆ `planning/tasks/TESTRUN-TASK-WSC-605.md`；`result: PASS`；exit 0；**15 passed** |
| 规格 / 报告 | `tests/e2e/specs/p0-wsc-v1.4.spec.ts`；`tests/e2e/reports/p0-wsc-v1.4/`（EVIDENCE.md + index.html + junit：tests=15, failures=0） |
| §6.1 强制项 1–7 | 全部 **PASS**（见 EVIDENCE / TESTRUN-605 勾选表） |
| P0 开放缺陷 | **0**（605 bugsOpen 空；603 BUG-TESTRUN-001 CLOSED） |

### 4.3 过程残差（已关闭，供审计）

| 项 | 说明 | 集成判定 |
|---|---|---|
| 602 R1 REQUEST_CHANGES | 壳层 my-products 越界 + useCanWrite 依赖 → R2 内联 ADMIN | **可接受**：最终 R2 APPROVE + 全部门禁 |
| 603 R1/R2/R3 + tester FAIL | mine 响应式 P0；测境空分类种子 BUG → 重测 PASS | **可接受**：写集内修复；security 后 VERIFIED |
| 605 场景 4 改/导跨用例（CR P2） | 同一证据包 7a/7b 覆盖 | **观察**：非跨任务冲突；不阻塞 |

---

## 5. §9 发布门禁技术项预检

| # | 门禁项 | 状态 | 说明 |
|---|---|---|---|
| 1 | **SNAP-WSC-005 已正式批准**（或委员会对本 planId 确认范围后正式批 SNAP） | **未满足** | `product/requirements/SNAP-WSC-005.md`：`status: IN_REVIEW`；`approvedAt: null`；protocolRepair 已作废伪造批准。**不得代批** |
| 2 | 全部 P0 REQ（§7 六条）对应任务 VERIFIED（含 605） | **已满足** | verifiedTaskIds = 601..605 |
| 3 | `wsc-contracts@2.2.0`；matrix 与实现一致；§3.1 硬冻结齐全 | **已满足** | VERSION + 601/603 证据 |
| 4 | 真登录 + 用户管理 + 切角色下线；securityReviewer（602）通过 | **已满足** | TESTRUN-602 + REV-SEC-602 |
| 5 | 公共目录无增改导；create_by 隔离；ADMIN 产品写 403；分类/维护保留 | **已满足** | TESTRUN-603 + REV-SEC-603 + E2E #3/#5 |
| 6 | 座序图 Top5/hover/真实总数/仅公共目录 | **已满足** | TESTRUN-604 + E2E #6 |
| 7 | V1.3 导入/OpenAPI 无 P0 回退；UX 无 P0 回退 | **已满足**（制品+E2E 口径） | E2E #7；603 acceptance |
| 8 | §6.1 E2E：`TESTRUN-WSC-E2E-V14`；报告 `p0-wsc-v1.4/`；P0=0 | **已满足** | TESTRUN-605 |
| 9 | developer ≠ CR ≠ tester；writeSet scope；适用 security/migration | **已满足** | 见 §4.1 |
| 10 | maintainer 人类发布批准（`approvals.yaml`） | **未满足** | **未见** `ai/runs/RUN-WSC-008/approvals.yaml`；本审查**不得**伪造 |

### 计划批准（支撑项，非代批 SNAP）

| 项 | 状态 |
|---|---|
| `PLAN-WSC-5.2` 经隔离委员会评审并发布 `planning/approved/` | **已满足**（events `PLAN_APPROVED` @14:40:01；注明 SNAP 仍 IN_REVIEW） |

### 进入 RELEASE_REVIEW / 发布前阻塞缺口（须关闭，非本角色批准）

1. **`SNAP-WSC-005` 正式人类/隔离批准**（结束 `IN_REVIEW`；写入合法 `approvedAt`）——**不得**沿用已作废的伪造批准。
2. **`maintainer`（或等价）人类明确批准**发布包（写入 `ai/runs/RUN-WSC-008/approvals.yaml`）。

> 集成 APPROVE **不等于** SNAP 批准，**不等于**生产发布批准。

---

## 6. Findings

### P0（跨任务）

无。

### P1（跨任务）

无。

### 观察 / 非阻塞

| id | severity | evidence | closeWhen |
|---|---|---|---|
| OBS-INT-V14-001 | observation | skill `integration-review` 仍 draft；冒烟清单 `_待填_` | Tech Lead 激活前不伪造冒烟命令 |
| OBS-INT-V14-002 | observation | **SNAP-WSC-005 仍 IN_REVIEW**；protocolRepair 作废伪造批准 | 隔离 productAnalyst/PO 正式批准后方可生产发布 |
| OBS-INT-V14-003 | observation | 未见 `approvals.yaml` / maintainer APPROVE_RELEASE | maintainer 人类批准写入 |
| OBS-INT-V14-004 | observation | events 中 W4 HUMAN_DECISION 时间戳（10:53）早于 603 `TASK_VERIFIED`（11:05），但 **jsonl append 序**为先 VERIFIED 再派发 | 以 append/`dependsOnVerified` 语义序为准；编排侧可选规范化时间戳 |
| OBS-INT-V14-005 | observation | `check-contracts.mjs` 仍 expected 1.1.0 vs 实际 2.2.0 | 工具链跟进 2.2.0；语义已由 assert-601-p0 覆盖 |
| OBS-INT-V14-006 | observation | FIND-WSC-605-R1-001：场景 4「改/导」跨 7a/7b 举证 | 可选单测闭环；不阻塞集成 |
| OBS-INT-V14-007 | observation | 602/603 过程多轮 CR/FAIL→修复 | 最终门禁齐全；供审计 |

---

## 7. 决策

```yaml
reviewId: REV-WSC-INTEGRATION-008
runId: RUN-WSC-008
planId: PLAN-WSC-5.2
snapshotId: SNAP-WSC-005
decision: APPROVE
actorInstance: integration-reviewer-wsc-008
crossTaskP0: 0
crossTaskP1: 0
```

```text
decision: APPROVE
```

- **含义**：全量串行束集成一致性通过；可进入 `RELEASE_REVIEW` / 发布门禁流程（技术前提已齐）。
- **不含义**：不宣称 V1.4 已可生产发布；**不**批准 SNAP-WSC-005；**不**代写 maintainer `APPROVE_RELEASE`；**不**将 Run 标 `COMPLETED`。
- **权限声明**：审查者未修改业务源码、契约、单任务 REV/TESTRUN/BUG、或 `ai/runs/**` state/events/approvals；仅写入本报告。
