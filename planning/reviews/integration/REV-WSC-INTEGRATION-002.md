# REV-WSC-INTEGRATION-002 — WSC V1.1 全量并行束集成审查

```yaml
runId: RUN-WSC-002
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
reviewId: REV-WSC-INTEGRATION-002
actorInstance: integration-reviewer-wsc-002
decision: APPROVE
reviewedAt: 2026-07-31T23:35:00+08:00
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/approved/PLAN-WSC-2.2.md
  - ai/runs/RUN-WSC-002/state.yaml
  - planning/tasks/TASK-WSC-101.md .. TASK-WSC-107.md
  - planning/tasks/TASK-WSC-107-FIX1.md
  - planning/tasks/REV-TASK-WSC-101.md .. REV-TASK-WSC-107.md
  - planning/tasks/REV-TASK-WSC-101-FIX1.md
  - planning/tasks/REV-TASK-WSC-107-FIX1.md
  - planning/tasks/TESTRUN-TASK-WSC-101.md .. TESTRUN-TASK-WSC-107.md
  - planning/tasks/TESTRUN-WSC-E2E-V11.md
  - planning/tasks/BUG-WSC-E2E-001.md
  - planning/tasks/DEV-TASK-WSC-107-FIX1.md
skillRef: ai/skills/integration-review/SKILL.md
skillStatus: draft
```

## 结论

跨任务写集互斥成立；W3（103∥104）、W4（105∥106）及条件并行 104∥106 均无路径/语义冲突；合并顺序符合 DAG（101→102→103∥104→105∥106→107→E2E）；共享契约 `wsc-contracts@2.0.0` / `frontend/src/api/**` 仍由 101 冻结、后继只读消费；跨模块语义抽查（admin↔browse↔editor↔chain↔maintenance↔import，含 FIX1 browse 挂载）未发现冲突；101..107 VERIFIED + E2E V11 r2 PASSED + BUG-WSC-E2E-001 CLOSED 证据链齐全。本轮**无跨任务 P0/P1**。

**decision: APPROVE** — 允许进入发布门禁预检 / `RELEASE_REVIEW`。§8 未满足项列于下文，作为进入人类发布批准前的阻塞，**不**由本角色代 `maintainer` 批准。

> 技能 `integration-review` 仍为 `draft`，本审查**未**伪造已激活冒烟命令清单；结论以计划 §5/§6/§8 与已落盘证据为准。

---

## 1. 并行束与写集检查表

| 波次 | 任务 | 计划写集 | 落盘路径抽查 | 互斥/冲突 | 结果 |
|---|---|---|---|---|---|
| W1 | 101 | 脚手架 + `contracts/**` + `frontend/src/api/**` + Flyway + OVW 最小适配 | `contracts/VERSION=2.0.0`；`CONTRACT_VERSION='2.0.0'`；sql/init+migration | 单任务 | PASS |
| W2 | 102 | `auth/**` ∥ `shell/**` ∥ `layouts/**` ∥ `rbac/**` ∥ `security/**` | REV/DEV 声明未改 contracts/api/sql/catalog 业务包 | 单任务 | PASS |
| W3 | 103 | `catalog/admin/**` | admin 前后端 + CategoryAdmin 测存在 | 与 browse 无路径交集 | PASS |
| W3 | 104 | `catalog/browse/**` | browse 前后端 + CatalogBrowse 测存在 | 与 admin 互斥 | PASS |
| W4 | 105 | `detail/**` ∥ `editor/**` ∥ `chain/**` | detail/editor/chain 前后端存在 | 与 maintenance 无交集 | PASS |
| W4 | 106 | `catalog/maintenance/**` | maintenance 前后端存在；SCOPE_AMEND routes/StubWrite | 与 detail/editor/chain 互斥 | PASS |
| W5 | 107 | `catalog/import/**` + fixtures | import FE + backend `productimport` 包 + fixtures | 初交付未改 browse（denyModify） | PASS |
| W5 | 107-FIX1 | `browse/**` + `import/**`（显式任务包） | `CatalogBrowsePage` 挂载 `ImportDialog`；报告下载 | 编排批准的 FIX 写集；非未声明冲突 | PASS |
| W5 | E2E | 仅 `tests/e2e/**` + 报告 | `tests/e2e/reports/p0-wsc-v1.1/index.html`；TESTRUN 记 fixture 路径微调 | 未改业务源码（证据自述） | PASS |

### 1.1 合并顺序

| 规则（PLAN §6） | 证据 | 结果 |
|---|---|---|
| W3 可并行 103∥104；写集 admin ∥ browse | events：102 VERIFIED 后并行派发 103∥104；独立 REV/TESTRUN；均 `verified: true` | PASS |
| W4：105 依赖 103+104；106 仅依赖 103、不得与 103 并行 | events：WAVE_VERIFIED W3 后并行派发 105∥106；106 未与 103 重叠 | PASS |
| 105∥106 写集无交集（detail/editor/chain ∥ maintenance） | 各 DEV/REV denyModify 互指；路径无混写 | PASS |
| **104 ∥ 106**（browse ∥ maintenance）当且仅当写集无交集 | 本轮实际在 104 VERIFIED 后才开 W4；写集仍无交集；未见单 PR 混写 | PASS |
| W5：107 在 105 VERIFIED 后；E2E 在 101..107 均 VERIFIED 后 | state `dependsOnVerified: [105]`；e2eGate r2 在 107 VERIFIED + FIX1 后 | PASS |
| 禁止以 Worker 完成速度替代 DAG | 波次闸门与 `WAVE_VERIFIED`/`WAVE_CLOSED` 记录可核验 | PASS |

### 1.2 SCOPE_AMEND / FIX 观察（非未声明写冲突）

| 项 | 说明 | 集成判定 |
|---|---|---|
| 106 SCOPE_AMEND | 去 `StubWriteController` 维护占位 + `routes` 注册 `/catalog/maintenance` | **已声明最小扩展**；REV-106 APPROVE |
| 107 初交付 browse 未挂载 | denyModify `browse/**` → FIND-001 P2；E2E 场景 4 失败 → BUG-WSC-E2E-001 | 边界已知；由 FIX1 关闭 |
| 107-FIX1 | 任务包显式允许 `browse/**` + `import/**`；挂载 ImportDialog + 报告下载 | **编排批准的缺陷修复写集**；非并行束隐式冲突 |

---

## 2. 共享契约冻结抽查

| 制品 | 期望 | 抽查 | 结果 |
|---|---|---|---|
| `contracts/VERSION` | 2.0.0（101 冻结） | 文件内容 `2.0.0` | PASS |
| OpenAPI / RBAC / errors / 导入 / Browse 分页 | 101 唯一写 | 102..107 及 FIX1 REV/DEV 均声明未改 `contracts/**` | PASS |
| `frontend/src/api/**` | 101 唯一写；后继只读 | `CONTRACT_VERSION = '2.0.0'`；后继声明未手改 | PASS |
| Flyway / `sql/**` | 仅 101；后继 denyModify | 101-FIX1 仅 pom `flyway-mysql` + runbook；业务任务未见 sql 改动声明 | PASS |

残差（不阻塞本集成 APPROVE）：

- `tests/contracts/check-contracts.mjs` 仍钉 `1.1.0`（FIND-WSC-101-R1-001 P2；TESTRUN-101 `contract-check` DEFERRED）。
- `frontend/src/api/chain.ts` 未展开 `categoryPathParts`（FIND-WSC-105 P3）；feature 层兜底。

---

## 3. 跨任务语义抽查结论

| 切面 | 任务对 | 抽查要点 | 证据 | 结论 |
|---|---|---|---|---|
| 分类挂载禁删 | 103↔104 seed | DEC-WSC-003；`ERR_CATEGORY_HAS_PRODUCTS`；挂载计数委托 browse seed | CategoryAdmin + `CatalogBrowseSeedStore#countMountedProducts`；TESTRUN-103 | **一致** |
| 浏览分页契约 | 104↔101 | `page`/`pageSize`/`total`；筛选保留 load-more | CatalogBrowseIntegrationTest；契约 §3.1 | **一致** |
| 编辑→存证三级路径 | 105↔chain/101 | 新建 v1 / 编辑递增；快照 `categoryPath`/`categoryPathParts`；失败无半成品 | ProductEditor + ChainApi 集成测；TESTRUN-105；E2E 场景 6 | **一致** |
| 目录维护↔种子/叶节点 | 106↔103/104 | 待关联→已维护；非叶 `ERR_CATEGORY_LEAF_REQUIRED`；非管理员 403 | CatalogMaintenanceIntegrationTest；TESTRUN-106；E2E 场景 3 | **一致** |
| 导入→产品写→上链 | 107↔105↔chain | DI 只读调用 `ProductEditorService`；成功行可检索上链；未改 editor/chain 实现 | ProductImportService；`fullSuccess_searchableAndChained`；REV-107 | **一致** |
| 导入入口挂载 | 107-FIX1↔104 | browse 挂载 `#import-modal`；`?import=1` + 关闭清 query | CatalogBrowsePage + ImportDialog；E2E 场景 4 r2；BUG CLOSED | **一致** |
| 会话/RBAC 写拒绝 | 102↔103/105/106/107 | §4 三角色；登出/角色变更后写拒绝；导入/维护/分类 | SessionAuth + 各任务 403 测；TESTRUN-102 | **一致** |
| 导入报告鉴权 | 107↔102 | ADMIN/PROVIDER 可下；USER 403；未认证拒绝（401 平台惯例） | report GET 集成测；FIND-003 P2 口径 | **一致**（安全目标达成） |

未发现「路径无冲突但语义冲突」的跨任务 P0/P1。

---

## 4. 证据链核对

### 4.1 任务门禁矩阵

| 任务 | developer | codeReviewer | tester | REV | TESTRUN | state.verified |
|---|---|---|---|---|---|---|
| 101 | developer-wsc-101（+fix1） | code-reviewer-wsc-101（+fix1） | tester-wsc-101-r6 | APPROVE | PASSED | true |
| 102 | developer-wsc-102 | code-reviewer-wsc-102 | tester-wsc-102 | APPROVE | PASSED | true |
| 103 | developer-wsc-103 | code-reviewer-wsc-103 | tester-wsc-103 | APPROVE | PASSED | true |
| 104 | developer-wsc-104 | code-reviewer-wsc-104 | tester-wsc-104 | APPROVE | PASSED | true |
| 105 | developer-wsc-105 | code-reviewer-wsc-105 | tester-wsc-105 | APPROVE | PASSED | true |
| 106 | developer-wsc-106 | code-reviewer-wsc-106 | tester-wsc-106 | APPROVE | PASSED | true |
| 107 | developer-wsc-107 | code-reviewer-wsc-107 | tester-wsc-107 | APPROVE | PASSED | true |
| 107-FIX1 | developer-wsc-e2e-fix1 | code-reviewer-wsc-e2e-fix1 | （经 E2E r2） | APPROVE | VERIFIED_VIA_E2E_R2 | — |
| W5 E2E | — | — | tester-wsc-e2e-v11-r2 | — | PASSED（exit 0，11 passed） | e2eGate PASSED |

角色独立性：各任务三者 actorInstance **互不相同**，可核验。E2E / FIX1 实例与功能任务 developer/reviewer/tester 亦不相同；r2 tester ≠ r1 failed tester。

### 4.2 W5 E2E + 缺陷关闭

| 项 | 结果 |
|---|---|
| 证据 | `planning/tasks/TESTRUN-WSC-E2E-V11.md`；`result: PASSED`；`round: r2` |
| 报告 | `tests/e2e/reports/p0-wsc-v1.1/index.html` |
| §6.1 六场景 | 全部 PASSED（含 partial-success 导入 + 三级快照） |
| BUG-WSC-E2E-001 | **CLOSED**（FIX1 + r2 复验）；`openBugs: []` |
| P0 缺陷 | 0（state / TESTRUN 声明） |

### 4.3 过程观察（不构成 FAIL）

| id | 级别 | 说明 | 处理 |
|---|---|---|---|
| OBS-INT-V11-001 | 观察 | E2E r1 FAILED → FIX1 → r2 初跑种子/fixture 路径问题 → 终跑 PASSED；过程完整可追溯 | 不阻塞集成 APPROVE |
| OBS-INT-V11-002 | 观察 | 107 初交付因 writeSet 边界未挂 browse，P2 升格为 E2E P0 后由 FIX1 关闭 | 集成侧已核验闭环 |

---

## 5. §8 发布门禁预检

| # | 门禁项 | 状态 | 说明 |
|---|---|---|---|
| 1 | P0 REQ 对应任务 VERIFIED；P1 OVW 回归 | **已满足** | 101..107 + state；E2E 场景 1 含 OVW |
| 2 | P0 缺陷 0；§6.1 E2E 通过 | **已满足** | TESTRUN-E2E-V11 r2 PASSED；BUG-E2E-001 CLOSED |
| 3a | 契约 `wsc-contracts@2.0.0` 无未决冲突 | **已满足**（静态） | VERSION + 后继只读；机检脚本钉 1.1.0 为残差 P2 |
| 3b | Flyway 空库可应用 | **已满足**（本地/证据口径） | TESTRUN-101：`mysql-empty-db-init` PASSED（FIX1 flyway-mysql） |
| 3c | MySQL 回滚演练有证据 | **未满足** | runbook 存在；勾选未勾；未见演练证据落盘 |
| 4 | developer ≠ codeReviewer ≠ tester | **已满足** | 文件层 actor 可区分 |
| 5 | maintainer 人类发布批准 | **未满足** | `ai/runs/RUN-WSC-002/approvals.yaml` → `approvals: []`；本审查**不得**伪造 |
| 6 | 计划 APPROVED | **已满足** | `planning/approved/PLAN-WSC-2.2.md` status APPROVED |

### 进入 RELEASE_REVIEW 前阻塞缺口（须关闭，非本角色批准）

1. **MySQL 回滚演练**证据（备份恢复或 forward-fix，路径按 `ops/runbooks/rollback.md` / `wsc-v1-rollback.md`）。
2. **`maintainer`（或等价）人类明确批准**发布包（写入 `approvals.yaml`）。
3. （建议）将 `check-contracts.mjs` 升级钉死 `2.0.0`（FIND-WSC-101-R1-001），避免发布机检误报。

> 说明：Flyway 空库应用在本 Run 已有 TESTRUN-101 通过证据，**不再**列为集成后发布阻塞；与 RUN-WSC-001 集成审查时的 DEFERRED 状态不同。

---

## 6. Findings

### P0

无。

### P1

无。

### 观察 / 非阻塞

| id | severity | evidence | closeWhen |
|---|---|---|---|
| OBS-INT-V11-001 | observation | E2E r1→FIX1→r2 过程链 | 审计时保留 TESTRUN/BUG/FIX1 制品即可 |
| OBS-INT-V11-002 | observation | runbook 无演练勾选证据 | 按 rollback runbook 完成演练并落盘 |
| OBS-INT-V11-003 | observation | `approvals.yaml` 空 | maintainer 人类批准写入 |
| OBS-INT-V11-004 | observation | `check-contracts.mjs` 仍断言 1.1.0 | chore 升级为 2.0.0 + 新错误码覆盖 |
| OBS-INT-V11-005 | observation | 匿名报告 GET 401 vs 计划字面 403（FIND-107-003） | 计划勘误或会话层统一；不阻塞集成 |
| OBS-INT-V11-006 | observation | Java 包 `productimport` vs 目录 `import/`（FIND-107-004 P3） | 可选对齐或 README 固化 |

---

## 7. 决策

```text
decision: APPROVE
```

- **含义**：并行束集成一致性通过；可进入 `RELEASE_REVIEW` / 发布门禁流程。
- **不含义**：不宣称 V1.1 已可发布；§8 缺口（回滚演练 + maintainer 批准）必须由运维/maintainer 关闭。
- **权限声明**：审查者未修改业务源码、契约、单任务 REV/TESTRUN 或 `ai/runs/**`；仅写入本报告。
