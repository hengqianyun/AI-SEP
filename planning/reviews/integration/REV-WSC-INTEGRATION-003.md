# REV-WSC-INTEGRATION-003 — WSC V1.2-UX 全量并行束集成审查

```yaml
reviewId: REV-WSC-INTEGRATION-003
runId: RUN-WSC-003
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
actorInstance: integration-reviewer-wsc-003
decision: APPROVE
reviewedAt: 2026-08-02T16:48:00+08:00
contracts: wsc-contracts@2.0.0
functionalBaseline: SNAP-WSC-002
basedOn:
  - planning/approved/PLAN-WSC-3.1.md
  - ai/runs/RUN-WSC-003/state.yaml
  - ai/runs/RUN-WSC-003/events.jsonl
  - planning/tasks/TASK-WSC-201.md .. TASK-WSC-206.md
  - planning/tasks/DEV-TASK-WSC-201.md .. DEV-TASK-WSC-206.md
  - planning/tasks/REV-TASK-WSC-201.md .. REV-TASK-WSC-206.md
  - planning/tasks/TESTRUN-TASK-WSC-201.md .. TESTRUN-TASK-WSC-206.md
  - planning/tasks/TESTRUN-TASK-WSC-206.md  # evidenceId TESTRUN-WSC-E2E-V12-UX
  - ai/runs/RUN-WSC-003/ux-gap-checklist.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/WALKTHROUGH.md
skillRef: ai/skills/integration-review/SKILL.md
skillStatus: draft
```

## 结论

跨任务写集互斥成立；W2（202∥203）、W3（204∥205）无路径/语义冲突；**禁止 203∥204** 已由调度满足（203 `TASK_VERIFIED` @14:20 → W2 关闭 @14:22 → 204 派发 @14:27）。201 独占 `styles/**`/`layouts/**`/`shell/**`/`App.vue`/`main.ts`；202..205 只读消费令牌；206 **未**改 `frontend/src/features/**`。功能基线契约 `wsc-contracts@2.0.0` 与 `frontend/src/api/**` / `backend/**` 全 Run denyModify 未破坏。§3.4 导入四态与 §3.5 展示白名单在 204 Vitest + 206 E2E 场景 4 证据链可核验。201..206 均 VERIFIED；`TESTRUN-WSC-E2E-V12-UX` PASSED（14/14）；差距清单 P0 open=0；PO 已确认走查并接受载体 B（`GAP-UX-006-01`）。本轮**无跨任务 P0/P1**。

**decision: APPROVE** — 允许进入发布门禁预检 / `RELEASE_REVIEW`。§8 未满足项（maintainer 人类批准）列于下文，**不**由本角色代批。

> 技能 `integration-review` 仍为 `draft`，本审查**未**伪造已激活冒烟命令清单；结论以计划 §2.3/§5/§8 与已落盘证据为准。

---

## 1. 并行束与写集检查表

| 波次 | 任务 | 计划写集 | 落盘路径抽查 | 互斥/冲突 | 结果 |
|---|---|---|---|---|---|
| W1 | 201 | `styles/**` ∥ `theme/**` ∥ `App/main` ∥ `layouts/**` ∥ `shell/**` | DEV/REV：`tokens.css`/`base.css`/`WorkbenchLayout`/`RoleSwitcher`；草稿 `02-shell-sidebar.png` | 单任务；独占全局样式 | PASS |
| W2 | 202 | `overview/**` ∥ `auth/views/**` | LoginView + OverviewPage；草稿 01+03 | 与 203 无交集；绝对 deny `auth/store|composables|api` | PASS |
| W2 | 203 | `catalog/browse/**` | `CatalogBrowsePage`；草稿 04；只读挂载 ImportDialog | 与 202 互斥；deny `import/**` 实现 | PASS |
| W3 | 204 | `maintenance/**` ∥ `admin/**` ∥ `import/**` | CatalogMaintenance / CategoryAdmin / ImportDialog；草稿 05–07 | 与 205 无交集；deny browse/styles | PASS |
| W3 | 205 | `detail/**` ∥ `editor/**` ∥ `chain/**` | 详情/编辑/上链；草稿 08–10 | 与 204 互斥 | PASS |
| W4 | 206 | 正式 ux 制品 + `tests/e2e/**` | checklist / screenshots / WALKTHROUGH；`p0-wsc-v1.1.spec.ts` + `fixtures/auth.ts` + playwright 报告目录 | deny `features/**`；无需 HOTFIX | PASS |

### 1.1 合并顺序（PLAN §5）

| 规则 | 证据 | 结果 |
|---|---|---|
| W2：201 VERIFIED 后 202∥203 | events：201 VERIFIED @14:04:30 → W2 并行派发 @14:05:31 | PASS |
| W2 写集 overview+login-views ∥ browse | DEV/REV 互指 denyModify；无单 PR 混写声明 | PASS |
| **禁止 203∥204**：须 203 VERIFIED（或合入冻结）后再启 204 | 203 VERIFIED @14:20:00；W2 WAVE_CLOSED note「ban 203\|\|204 satisfied」@14:22:31；W3 DISPATCHED dependsOnVerified 含 203 @14:27:31 | PASS |
| W3：204∥205 写集 maint/admin/import ∥ detail/editor/chain | 并行派发；各 DEV 声明未触碰对方写集 | PASS |
| W4：206 在 201..205 均 VERIFIED 后 | WAVE_CLOSED W3 @15:18:31；206 DISPATCHED dependsOnVerified 五任务 @15:19:01 | PASS |
| 禁止以 Worker 完成速度替代 DAG | 波次闸门与 `WAVE_CLOSED` / `dependsOnVerified` 可核验；205 先于 204 完成但未提前启 206 | PASS |

### 1.2 共享样式所有权（§2.3）

| 制品 | 唯一写任务 | 后继行为 | 结果 |
|---|---|---|---|
| `frontend/src/styles/**` / theme / App / main / layouts / shell | 201 | 202..205 DEV/REV 声明只读消费 `var(--*)`；未见第二套全局色板 | PASS |
| `frontend/src/router/**` | 本 Run 冻结（无写任务） | 全任务 denyModify | PASS |
| `contracts/**` / `frontend/src/api/**` / `backend/**` | 无写任务 | 全任务 deny；`contracts/VERSION` 仍为 `2.0.0` | PASS |
| 206 与 features | 206 denyModify `frontend/src/features/**` | DEV/REV：选择器适配仅落 `tests/e2e/**`；无 20x-HOTFIX | PASS |

---

## 2. 共享契约 / 功能基线抽查

| 制品 | 期望 | 抽查 | 结果 |
|---|---|---|---|
| `contracts/VERSION` | 2.0.0（SNAP-WSC-002 冻结） | 文件内容 `2.0.0` | PASS |
| OpenAPI / RBAC / 导入契约 | 本 Run 只读 | 201..206 DEV/REV 均声明未改 `contracts/**` | PASS |
| `frontend/src/api/**` | 只读 | 后继声明未手改 | PASS |
| `backend/**` / `**/sql/**` | 全任务 denyModify | 各任务 scope 声明未改；本审查不代改业务源码 | PASS |

残差（不阻塞本集成 APPROVE）：工作区可能残留他 Run / 本地脏文件，单任务 CR 已要求 tester 以 DEV 变更列表归因为准；集成侧以任务制品声明 + VERSION 为准。

---

## 3. 跨任务语义抽查结论

| 切面 | 任务对 | 抽查要点 | 证据 | 结论 |
|---|---|---|---|---|
| 令牌消费 | 201↔202..205 | CSS-token-first；无裸 AntDV 第二皮肤 | 各 REV APPROVE；无 AntDV 替换清单 | **一致** |
| 导入入口 vs 弹窗 | 203↔204 | 203 只读挂载 ImportDialog；弹窗视觉归 204 | REV-203 deny import 实现；REV-204 writeSet import；禁并行已调度 | **一致** |
| 分类维护载体 B | 204↔计划 §1.5 | 独立路由 + 页内 modal 壳；非 browse overlay | GAP-UX-006-01 P1 open；正式截图 06；WALKTHROUGH 勾选 | **一致**（有意偏差） |
| 导入 §3.4 四态 | 204↔206 | ①全成功/②部分成功/③全失败/④文件级拒绝可区分 | 204：`classifyImportResult` + Vitest 9；`data-result-state`；206 E2E 场景 4 partial-success | **一致** |
| 导入 §3.5 白名单 | 204↔206 | 禁信用代码/ownerDID/明文哈希回显 | 204 Vitest `§3.5 whitelist…`；正式截图 07 / WALKTHROUGH；E2E 未扩 PII 证据 | **一致** |
| 权限非 CSS 替代 | 201/204/205↔206 | 结构不可达 + 三角色矩阵 | REV 声明 `v-if`；E2E 6b/6c PASSED | **一致** |
| 详情/编辑/上链 | 205↔E2E | 主路径 + 敏感截断不回退 | TESTRUN-205 Vitest；E2E 1d/1e/6a | **一致** |

未发现「路径无冲突但语义冲突」的跨任务 P0/P1。

---

## 4. 证据链核对

### 4.1 任务门禁矩阵

| 任务 | developer | codeReviewer | tester | REV | TESTRUN | state.verified |
|---|---|---|---|---|---|---|
| 201 | developer-wsc-201 | code-reviewer-wsc-201 | tester-wsc-201 | APPROVE | PASSED | true |
| 202 | developer-wsc-202 | code-reviewer-wsc-202 | tester-wsc-202 | APPROVE | PASSED | true |
| 203 | developer-wsc-203 | code-reviewer-wsc-203 | tester-wsc-203 | APPROVE | PASSED | true |
| 204 | developer-wsc-204 | code-reviewer-wsc-204 | tester-wsc-204 | APPROVE | PASSED | true |
| 205 | developer-wsc-205 | code-reviewer-wsc-205 | tester-wsc-205 | APPROVE | PASSED | true |
| 206 | developer-wsc-206 | code-reviewer-wsc-206 | tester-wsc-206 | APPROVE | PASSED（E2E V12-UX） | true |

角色独立性：各任务三者 actorInstance **互不相同**，可核验。

### 4.2 E2E + 走查 + 差距清单

| 项 | 结果 |
|---|---|
| E2E 证据 | `TESTRUN-WSC-E2E-V12-UX` = `planning/tasks/TESTRUN-TASK-WSC-206.md`；`status: PASSED`；exit 0；**14 passed** |
| 报告 | `tests/e2e/reports/p0-wsc-v1.2-ux/index.html` |
| §3.2 场景 | 含导入 partial-success、三角色写入口矩阵 6b/6c | 
| 差距清单 | `ux-gap-checklist.md`：P0 open=**0**；截图 01–10 覆盖 |
| 走查签字 | `WALKTHROUGH.md` PO/UX 栏已填；events `HUMAN_DECISION` `CONFIRM_UX_WALKTHROUGH` @16:47:00；原话接受载体 B |
| P0 功能缺陷 | 0（TESTRUN 无 BUG 单） |

### 4.3 残余开放项（可接受）

| id | severity | 说明 | 集成判定 |
|---|---|---|---|
| GAP-UX-006-01 | P1 open（计划内） | 载体 B vs 原型 overlay；§1.5 明确非 P0、须可勾选 | **可接受**：正式截图 #06 + WALKTHROUGH 勾选 + PO `acceptedGaps: [GAP-UX-006-01]`。**不**计为本审查跨任务 P1 阻塞项 |

---

## 5. §8 发布门禁技术项预检

| # | 门禁项 | 状态 | 说明 |
|---|---|---|---|
| 1 | P0 REQ-UX 对应任务 VERIFIED；P1（008/009/010）走查通过或差距关闭 | **已满足** | 201..206 verified；`GAP-UX-009/010` closed；载体 B 为人接受的计划内 P1 |
| 2 | §3.1：P0=0；截图齐全（脱敏）；视口勾选；PO/UX 确认 | **已满足** | checklist + WALKTHROUGH + HUMAN_DECISION |
| 3 | §3.2 E2E 通过；P0 功能缺陷 0 | **已满足** | TESTRUN-WSC-E2E-V12-UX PASSED |
| 4 | `wsc-contracts@2.0.0` 无变更、无未决冲突；diff 不含 contracts/backend | **已满足**（静态/制品口径） | VERSION + 全任务 denyModify 声明 |
| 5 | developer ≠ codeReviewer ≠ tester；writeSet 字面 scope | **已满足** | 文件层 actor 可区分；各 REV scope PASS |
| 6 | 计划经委员会 APPROVE 并已发布 approved | **已满足** | `planning/approved/PLAN-WSC-3.1.md` |
| 7 | maintainer 人类发布批准（`approvals.yaml`） | **未满足** | 未见 `ai/runs/RUN-WSC-003/approvals.yaml`；本审查**不得**伪造 |

### 进入 RELEASE_REVIEW / 发布前阻塞缺口（须关闭，非本角色批准）

1. **`maintainer`（或等价）人类明确批准**发布包（写入 `ai/runs/RUN-WSC-003/approvals.yaml`）。

> 说明：本 UX Run 无 DB 迁移回滚项（计划 §7）；不另列 MySQL 演练为集成后阻塞。

---

## 6. Findings

### P0

无。

### P1

无（跨任务）。计划内残余 `GAP-UX-006-01` 见 §4.3，已人类确认，不阻塞本 APPROVE。

### 观察 / 非阻塞

| id | severity | evidence | closeWhen |
|---|---|---|---|
| OBS-INT-UX-001 | observation | 分波草稿截图多为 fixture HTML（各 CR P2）；正式走查/E2E 已对真应用闭环 | 审计保留 drafts→screenshots 合并链即可 |
| OBS-INT-UX-002 | observation | TESTRUN-206 @16:44 时 PO 签署栏仍空；PO 签字 @16:47 后补齐 | 以 WALKTHROUGH + events 为准 |
| OBS-INT-UX-003 | observation | `ux-gap-checklist.md` 载体 B 勾选栏末项 checkbox 可能未与 WALKTHROUGH 同步勾选 | 可选对齐文档勾选；人类确认已在 WALKTHROUGH/events |
| OBS-INT-UX-004 | observation | `approvals.yaml` 缺失 / 空 | maintainer 人类批准写入 |
| OBS-INT-UX-005 | observation | FIND-204-R1-002：源码注释仍写「§3.6」旧引用 | 可选注释改为 §3.4 |
| OBS-INT-UX-006 | observation | skill `integration-review` 仍 draft | Tech Lead 激活前不伪造冒烟清单 |

---

## 7. 决策

```yaml
reviewId: REV-WSC-INTEGRATION-003
runId: RUN-WSC-003
planId: PLAN-WSC-3.1
decision: APPROVE
actorInstance: integration-reviewer-wsc-003
```

```text
decision: APPROVE
```

- **含义**：并行束集成一致性通过；可进入 `RELEASE_REVIEW` / 发布门禁流程（技术前提已齐）。
- **不含义**：不宣称 V1.2-UX 已可发布；§8 缺口（maintainer 批准）必须由 maintainer 关闭。
- **权限声明**：审查者未修改业务源码、契约、单任务 REV/TESTRUN 或 `ai/runs/**` state/events；仅写入本报告。
