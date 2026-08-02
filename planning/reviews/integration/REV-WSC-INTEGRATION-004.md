# REV-WSC-INTEGRATION-004 — WSC V1.3 全量并行束集成审查

```yaml
reviewId: REV-WSC-INTEGRATION-004
runId: RUN-WSC-004
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
actorInstance: integration-reviewer-wsc-004
decision: APPROVE
reviewedAt: 2026-08-02T19:25:00+08:00
contracts: wsc-contracts@2.1.0
functionalBaseline: SNAP-WSC-002 (wsc-contracts@2.0.0)
uxBaseline: SNAP-WSC-003
basedOn:
  - planning/approved/PLAN-WSC-4.1.md
  - ai/runs/RUN-WSC-004/state.yaml
  - ai/runs/RUN-WSC-004/events.jsonl
  - planning/tasks/TASK-WSC-301.md .. TASK-WSC-304.md
  - planning/tasks/DEV-TASK-WSC-301.md .. DEV-TASK-WSC-304.md
  - planning/tasks/REV-TASK-WSC-301.md .. REV-TASK-WSC-304.md
  - planning/tasks/TESTRUN-TASK-WSC-301.md .. TESTRUN-TASK-WSC-304.md
  - planning/tasks/TESTRUN-TASK-WSC-304.md  # evidenceId TESTRUN-WSC-E2E-V13
  - planning/tasks/BUG-WSC-304-001.md .. BUG-WSC-304-005.md
  - contracts/VERSION
  - tests/e2e/reports/p0-wsc-v1.3/
  - planning/reviews/integration/REV-WSC-INTEGRATION-003.md  # 格式对照
skillRef: ai/skills/integration-review/SKILL.md
skillStatus: draft
```

## 结论

跨任务写集互斥成立；W2（302∥303）`editor/**` ∥ `detail/**` 无路径/语义冲突；**禁止**写 `frontend/src/components/**` 已由各任务 DEV/REV 声明且仓库无该目录写入。W3（304）仅在 301+302+303 均 `TASK_VERIFIED` 后串行启动（`WAVE_CLOSED` W2 @18:09:50 → 304 DISPATCHED @18:10:00）。契约目标 `wsc-contracts@2.1.0` 由 301 独占升级；`ImportTemplateColumns` 重冻与 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 四处硬同步可检；OpenAPI 方案 B（双端解析 + 写冲突以客户端 endpoints 为准 + 共享 fixture 关键字段一致）跨 301/302/303 一致。导入四态（含态③ all-fail、态④ TEMPLATE_UNSUPPORTED）在 301 集成测 + 304 Vitest/E2E 证据链可核验；`TESTRUN-WSC-E2E-V13` PASSED（10/10）；BUG-001..005 均 CLOSED。本轮**无跨任务 P0/P1**。

**decision: APPROVE** — 允许进入发布门禁预检 / `RELEASE_REVIEW`。§9 未满足项（maintainer 人类批准）列于下文，**不**由本角色代批。

> 技能 `integration-review` 仍为 `draft`，本审查**未**伪造已激活冒烟命令清单；结论以计划 §3/§6/§9 与已落盘证据为准。

---

## 1. 并行束与写集检查表

| 波次 | 任务 | 计划写集 | 落盘路径抽查 | 互斥/冲突 | 结果 |
|---|---|---|---|---|---|
| W1 | 301 | `contracts/**` ∥ `frontend/src/api/**` ∥ backend import/editor typeSpecific ∥ `tests/fixtures/import/**` | DEV/REV：VERSION 2.1.0、ImportTemplateColumns、ProductImport*、client DTO、fixtures | 单任务；独占契约/API/后端导入 | PASS |
| W2 | 302 | `catalog/editor/**` | OpenApiEditorPanel / useProductEditor / parseOpenApi；Vitest 16 | 与 303 无交集；deny detail/import/components | PASS |
| W2 | 303 | `catalog/detail/**` | OpenApiReadonlyPanel / TypeSpecificReadonly；Vitest 11 | 与 302 互斥；deny editor/import/components | PASS |
| W3 | 304 | `catalog/import/**` ∥ `tests/e2e/**`（V1.3）∥ 前端 import fixtures | ImportDialog / useProductImport / importGuards；`p0-wsc-v1.3.spec.ts`；报告 `p0-wsc-v1.3/` | 单任务串行；deny editor/detail/contracts/backend | PASS |

### 1.1 合并顺序（PLAN §6）

| 规则 | 证据 | 结果 |
|---|---|---|
| W1：301 单独；VERIFIED 后启 W2 | events：301 `TASK_VERIFIED` @17:57:30 → `WAVE_CLOSED` W1 → W2 并行派发 302∥303 @17:57:33 | PASS |
| W2：301 VERIFIED 后 **302 ∥ 303** | 并行 `DISPATCH` @17:58:00；写集 editor ∥ detail；各 DEV/REV 互指 denyModify | PASS |
| W2 写集无交集；禁单 PR 混写 | REV-302/303：交付面分别限定 editor / detail；FIND 工作区脏文件隔离说明 | PASS |
| **禁止 302∥304 / 303∥304** | 304 未与 W2 并行；`WAVE_CLOSED` W2 @18:09:50 后才派发 | PASS |
| W3：304 须 **301+302+303 均 VERIFIED** | 302 VERIFIED @18:09:45；303 @18:08:45；301 已于 W1；304 DISPATCHED @18:10:00；`dependsOn` 满足 | PASS |
| 禁止以 Worker 完成速度替代 DAG | 303 先于 302 完成测试，但未提前启 304；W2 闸门等 302 亦 VERIFIED | PASS |

### 1.2 共享制品所有权（§3.5 / §1.7）

| 制品 | 唯一写任务 | 后继行为 | 结果 |
|---|---|---|---|
| `contracts/**` / `frontend/src/api/**` | 301 | 302..304 只读消费 2.1.0 | PASS |
| 后端导入 / 产品 typeSpecific 写 | 301 | 304 只读 API；302/303 经 API | PASS |
| `catalog/editor/**` | 302 | 303/304 deny | PASS |
| `catalog/detail/**` | 303 | 302/304 deny | PASS |
| `catalog/import/**` + 本轮 `tests/e2e/**` | 304 | 302/303 deny | PASS |
| `frontend/src/components/**` | **无写任务** | 全任务 deny；仓库**无**该目录 | PASS |
| styles / shell / router | 本 Run 冻结 | 全任务只读（UX 不重开） | PASS |

---

## 2. 共享契约抽查（2.1.0）

| 制品 | 期望 | 抽查 | 结果 |
|---|---|---|---|
| `contracts/VERSION` | **2.1.0** | 文件内容 `2.1.0` | PASS |
| `ImportTemplateColumns` | 按 v0729 列名行重冻；废止 V1.1 最小列；无产品编码列 | REV/TESTRUN-301：OpenAPI + Java COLUMNS + GET 模板测 | PASS |
| `ERR_IMPORT_TEMPLATE_UNSUPPORTED` | 四处硬同步 | codes.yaml 定义 + requestLevel；OpenAPI 400 example；state-matrix 态④ | PASS |
| `typeSpecific.api` | swaggerFileContent + endpoints[]；旧 endpoint 读兼容 | 301 OpenAPI/实现 + 302/303 消费 | PASS |
| OTHER / `NO_UPDATE` | contentDescription 允许；枚举增量 | 301 editor 集成测 | PASS |
| 302/303/304 对 contracts/api | denyModify / 只读 | 各 DEV/REV 声明未改 | PASS |

残差（不阻塞本集成 APPROVE）：各 CR 记载工作区并行脏文件；集成侧以任务制品声明 + VERSION + 字面 writeSet 隔离说明为准。

---

## 3. 跨任务语义抽查结论

| 切面 | 任务对 | 抽查要点 | 证据 | 结论 |
|---|---|---|---|---|
| OpenAPI 方案 B | 301↔302 | 双端解析；归档真源；导入始终派生；写冲突以客户端 endpoints 为准 | 301：`create_api_endpointsConflict_keepsClientEndpoints`；302：保留原文+endpoints 原样提交、坏文本阻断 | **一致** |
| 共享 fixture 关键字段 | 301↔302 | method/path/summary 一致 | 双方：`openapi-shared-minimal.yaml` → POST `/enterprise/security/verify` /「企业安全信息核验」 | **一致** |
| OpenAPI UI 方案 a | 302↔303 | 各自实现于 feature；禁 `components/**` | 编辑可写面板 ∥ 详情只读面板；无共享组件路径 | **一致**（像素复用非硬门禁） |
| 详情只读 ↔ 编辑写 | 302↔303↔E2E | 信息层级对齐 §1.5；旧 endpoint 不崩溃 | REV-302/303；E2E 场景 4/5 PASSED | **一致** |
| 导入四态 §3.4 | 301↔304 | ①全成功/②部分成功/③全失败/④文件级拒绝（含旧模板） | 301：四态 fixtures+集成测；304：classifyImportResult + Vitest + E2E #2/#3 | **一致** |
| TEMPLATE vs FORMAT | 301↔304 | 不得互映 | 301 format-invalid 测；304 Vitest FORMAT 分界 + E2E legacy 态④ | **一致** |
| 报告白名单 | 301↔304 | 不扩大 PII | 301 集成测白名单四字段；304 投影/负例 Vitest | **一致** |
| 权限非 CSS | 301..304↔E2E | 结构不可达 + 三角色 | 各 REV `v-if`/守卫；E2E 场景 6 PASSED | **一致** |
| 空 paths 边界 | 301↔302 | 含 openapi 但无 paths → 空 endpoints 且非解析失败 | FIND-301/302-R1-002（均为 P2，口径对称） | **一致**（已知边界，非跨任务冲突） |

未发现「路径无冲突但语义冲突」的跨任务 P0/P1。

---

## 4. 证据链核对

### 4.1 任务门禁矩阵

| 任务 | developer | codeReviewer | tester | REV | TESTRUN | state.verified |
|---|---|---|---|---|---|---|
| 301 | developer-wsc-301 | code-reviewer-wsc-301 | tester-wsc-301 | APPROVE | PASSED（17 集成测） | true |
| 302 | developer-wsc-302 | code-reviewer-wsc-302 | tester-wsc-302 | APPROVE | PASSED（Vitest 16） | true |
| 303 | developer-wsc-303 | code-reviewer-wsc-303 | tester-wsc-303 | APPROVE | PASSED（Vitest 11） | true |
| 304 | developer-wsc-304 | code-reviewer-wsc-304 | tester-wsc-304 | APPROVE（R5） | PASSED + **TESTRUN-WSC-E2E-V13** | true |

角色独立性：各任务三者 actorInstance **互不相同**，可核验。

### 4.2 E2E V13 + 导入四态

| 项 | 结果 |
|---|---|
| E2E 证据 | `TESTRUN-WSC-E2E-V13` ⊆ `planning/tasks/TESTRUN-TASK-WSC-304.md`；`status: PASSED`；exit 0；**10 passed** |
| 规格 / 报告 | `tests/e2e/specs/p0-wsc-v1.3.spec.ts`；`tests/e2e/reports/p0-wsc-v1.3/`（junit：tests=10, failures=0） |
| §6.1 强制项 | 场景 2 **partial-success** PASSED；场景 3 legacy → TEMPLATE_UNSUPPORTED 态④ PASSED |
| OpenAPI / 详情 / RBAC | 场景 4/5/6 PASSED |
| 301 四态底座 | full / partial / **all-fail** / legacy / format-invalid 集成测 PASSED |
| 304 Vitest 四态 | classify + submit 路径覆盖 ①②③④（态① submit 文案为 R1 P2 观察项） |
| P0 开放缺陷 | **0**（BUG-001..005 均 `CLOSED`） |

### 4.3 304 过程残差（已关闭，供审计）

| 项 | 说明 | 集成判定 |
|---|---|---|
| 多轮 CR/测试 | R1 REQUEST_CHANGES（auth.ts 越界）→ 内联 helpers；中途 live 后端未部署 301 → ops 重启；BUG-004 barrel 循环；BUG-005 typecheck | **可接受**：最终 R5 APPROVE + r4 TESTRUN PASSED；DAG/写集未破坏 |
| CSV 客户端预检 | BUG-001：与 `IMPORT_TEMPLATE_COLUMNS` 对齐；不替代后端权威拒绝 | **可接受**（REV 已定性非掩盖契约） |

---

## 5. §9 发布门禁技术项预检

| # | 门禁项 | 状态 | 说明 |
|---|---|---|---|
| 1 | SNAP-WSC-004 全部 P0 REQ 对应任务 VERIFIED | **已满足** | 301..304 verified；CAT-008/004/005、API-001、RBAC-001 |
| 2 | `wsc-contracts@2.1.0`；ImportTemplateColumns；TEMPLATE_UNSUPPORTED 硬同步可测 | **已满足** | VERSION + TESTRUN-301 |
| 3 | 旧模板拒绝 + v0729 主路径自动化；四态可区分；报告白名单不回退 | **已满足** | 301 集成测 + 304 Vitest/E2E |
| 4 | OpenAPI：编辑回读 + 详情只读 + 导入路径；共享 fixture；写冲突可检 | **已满足** | 301/302/303 + E2E #4/#5 |
| 5 | UX 相对 3.1 无 P0 回退；未写 `components/**`；令牌未重做 | **已满足**（制品口径） | deny + 无目录写入 + E2E 主路径抽样 |
| 6 | §6.1 E2E：`TESTRUN-WSC-E2E-V13`；报告 `p0-wsc-v1.3/`；强制 partial；P0=0 | **已满足** | TESTRUN-304 r4 |
| 7 | developer ≠ codeReviewer ≠ tester；writeSet 字面 scope | **已满足** | 文件层 actor 可区分；各 REV scope PASS（304 越界已在 R2 纠正） |
| 8 | 计划经委员会 APPROVE 并已发布 approved | **已满足** | `planning/approved/PLAN-WSC-4.1.md`；events `PLAN_APPROVED` |
| 9 | maintainer 人类发布批准（`approvals.yaml`） | **未满足** | **未见** `ai/runs/RUN-WSC-004/approvals.yaml`；本审查**不得**伪造 |

### 进入 RELEASE_REVIEW / 发布前阻塞缺口（须关闭，非本角色批准）

1. **`maintainer`（或等价）人类明确批准**发布包（写入 `ai/runs/RUN-WSC-004/approvals.yaml`）。

> 说明：本 Run 未要求本审查代跑 MySQL 迁移演练；301 对 sql 的增量以任务交付说明为准。集成 APPROVE **不等于**发布批准。

---

## 6. Findings

### P0

无。

### P1

无（跨任务）。

### 观察 / 非阻塞

| id | severity | evidence | closeWhen |
|---|---|---|---|
| OBS-INT-V13-001 | observation | skill `integration-review` 仍 draft；冒烟清单 `_待填_` | Tech Lead 激活前不伪造冒烟命令 |
| OBS-INT-V13-002 | observation | FIND-301/302-R1-002：空 `paths` 时双端返回空 endpoints 且非解析失败（对称） | 若产品要求「非空原文须至少一端点」则单独立项；否则固化边界说明 |
| OBS-INT-V13-003 | observation | 302/303 method 标签硬编码色（各 CR P3）；计划允许 UI 漂移非像素门禁 | 可选收束为令牌变量 |
| OBS-INT-V13-004 | observation | 304 首轮 E2E 曾因 live 后端仍为 V1.1 导入而失败；ops 重启后闭环 | 发布包须确认运行中后端已含 301 导入实现 |
| OBS-INT-V13-005 | observation | `approvals.yaml` 缺失 | maintainer 人类批准写入 |
| OBS-INT-V13-006 | observation | events.jsonl 存在时间戳与追加顺序交错（301 制品时间戳早于文件中 PLAN_APPROVED 行之后的片段） | 以 `WAVE_CLOSED` / `TASK_VERIFIED` / `dependsOnVerified` 语义序为准；编排侧可选规范化 append |
| OBS-INT-V13-007 | observation | 304 Vitest 态①全成功 submit 文案覆盖偏弱（FIND-R1-002 P2）；E2E 强制覆盖为 partial | 可选补 Vitest；不阻塞集成 |

---

## 7. 决策

```yaml
reviewId: REV-WSC-INTEGRATION-004
runId: RUN-WSC-004
planId: PLAN-WSC-4.1
decision: APPROVE
actorInstance: integration-reviewer-wsc-004
```

```text
decision: APPROVE
```

- **含义**：并行束集成一致性通过；可进入 `RELEASE_REVIEW` / 发布门禁流程（技术前提已齐）。
- **不含义**：不宣称 V1.3 已可发布；§9 缺口（maintainer 批准）必须由 maintainer 关闭。
- **权限声明**：审查者未修改业务源码、契约、单任务 REV/TESTRUN/BUG、或 `ai/runs/**` state/events；仅写入本报告。
