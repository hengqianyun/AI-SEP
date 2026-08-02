# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-304-R5
taskId: TASK-WSC-304
round: 5
decision: APPROVE
actorInstance: code-reviewer-wsc-304
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
contracts: wsc-contracts@2.1.0 (read-only for this task)
reviewedAt: 2026-08-02T19:20:00+08:00
priorRound: REV-CODE-TASK-WSC-304-R4
basedOn:
  - planning/tasks/TASK-WSC-304.md
  - planning/tasks/DEV-TASK-WSC-304.md
  - planning/tasks/BUG-WSC-304-005.md
  - planning/approved/PLAN-WSC-4.1.md
  - frontend/src/features/catalog/import/composables/useProductImport.ts
mustDifferFrom: developer-wsc-304
```

## Round 5 结论

**APPROVE** — 本轮 **P0=0，P1=0**。BUG-WSC-304-005（TS6133）：`useProductImport.ts` 已去掉未使用的 `IMPORT_REQUEST_ERROR_HINTS` 本地 import，保留 `export { … } from './importGuards'` 再导出。变更 ⊆ writeSet（仅该文件 + DEV）。审查复跑 `vue-tsc --noEmit` **EXIT=0**；import Vitest **16 PASSED**。无新 P0/P1。进入独立 tester 复测门禁（typecheck / 可与 TESTRUN 一并）；**不**代标 E2E / VERIFIED。

## Round 5 Findings

| id | severity | status | evidence | closeWhen |
|---|---|---|---|---|
| FIND-WSC-304-R5-001 | P3 | OPEN | BUG-005 yaml 仍 `status: OPEN`；代码侧 TS6133 已消 | tester 复跑 typecheck EXIT=0 后关闭 BUG-005 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## Round 5 核对

| 项 | 结果 |
|---|---|
| writeSet：仅 `useProductImport.ts`（+ DEV） | PASS |
| 未改 browse/backend/contracts/api/editor/detail | PASS |
| `IMPORT_REQUEST_ERROR_HINTS` 仅再导出、无本地绑定 | PASS |
| `vue-tsc --noEmit -p tsconfig.json` | **EXIT=0** |
| Vitest import | **16 PASSED** |

### 审查复跑证据（R5）

```text
cd frontend
pnpm exec vue-tsc --noEmit -p tsconfig.json --pretty false   # EXIT=0
pnpm exec vitest run src/features/catalog/import --reporter=default  # 16 passed
```

### 隔离声明（R5）

- 审查者未修改被审业务代码、DEV/BUG、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 E2E/VERIFIED；未调度 tester。
- **decision: APPROVE**（进入 tester 复测门禁）。

### 计数（R5 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 0（本轮新增） |
| P3 | 1（BUG-005 yaml 待 tester 关闭） |

---

## Round 4（历史；APPROVE）

```yaml
reviewId: REV-CODE-TASK-WSC-304-R4
round: 4
decision: APPROVE
reviewedAt: 2026-08-02T19:15:00+08:00
```

## Round 4 结论（摘要）

**APPROVE** — 本轮 **P0=0，P1=0**。BUG-WSC-304-004 修复落在 writeSet：`importGuards.ts` 拆出纯函数；`index.ts` barrel **仅**导出 `ImportDialog`；`ImportDialog` / 单测仍直接引用 `./composables/useProductImport`（`export function useProductImport` 仍在）。模块图无 `index ↔ Dialog ↔ composable` 双路径再导出循环。本任务交付面**未改** `browse/**`（browse 仍直接 `import ImportDialog from '.../ImportDialog.vue'`；工作区 browse 脏文件属前序 FIX1/挂载，非本轮 004 清单）。审查复跑 Vitest **16 PASSED**。进入独立 tester 复测门禁；**不**代标 E2E / `TESTRUN-WSC-E2E-V13` 通过；001/002 行为与 301 live 仍交复测。

## Round 4 Findings

| id | severity | status | evidence | closeWhen |
|---|---|---|---|---|
| FIND-WSC-304-R4-001 | P3 | OPEN | BUG-004 yaml 仍 `status: OPEN`；开发侧已修但 closeWhen 含 tester 复跑场景 1b/2/3/6 | 独立 tester 复跑通过后关闭 BUG-004 |
| （承继 R3）FIND-R3-001/002 | P2 | OPEN | API/xlsx 与 partial-success 仍依赖 301 live | 部署后复测 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## Round 4 核对清单

| 项 | 结果 |
|---|---|
| writeSet：`import/**`（含新建 `importGuards.ts`、收窄 `index.ts`） | PASS |
| 未改 `browse/**`（本轮 BUG-004 交付） | PASS（DEV 声明 + browse 仍直引 `.vue`；并行脏文件不归本轮） |
| barrel 仅 `ImportDialog`；composable 具名导出保留 | PASS |
| 循环依赖 | PASS：`importGuards` → api only；`useProductImport` → guards；Dialog → composable；无 barrel re-export composable |
| Vitest | **16 PASSED** |

### 审查复跑证据（R4）

```text
cd frontend
pnpm exec vitest run src/features/catalog/import --reporter=default
# ✓ useProductImport.spec.ts (16 tests)  — PASSED
```

### 隔离声明（R4）

- 审查者未修改被审业务代码、DEV/BUG、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；**不**代标 E2E PASSED / VERIFIED；未调度 tester。
- **decision: APPROVE**（进入 tester 复测门禁）。

### 计数（R4 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2（承继 R3 环境/API 残余；不阻塞本轮） |
| P3 | 1（BUG-004 yaml 待 tester 关闭） |

---

## Round 3（历史；APPROVE）

```yaml
reviewId: REV-CODE-TASK-WSC-304-R3
round: 3
decision: APPROVE
reviewedAt: 2026-08-02T18:35:00+08:00
```

## Round 3 结论（摘要）

**APPROVE** — 本轮 **P0=0，P1=0**。针对 `TESTRUN-WSC-E2E-V13` FAILED 后的 writeSet 内修复可进入**复测门禁**。

| BUG | CR 判定 |
|---|---|
| **BUG-003** | 规格编码已改为三段 `E2E-{API\|DET}-{NNNN}`（`makeProductCode`；NNNN≥4），符合 `{DOMAIN}-{FEATURE}-{NNNN}`；**本任务代码侧可关闭** |
| **BUG-001** | CSV 表头预检 ⊆ `import/**` writeSet；集合精确对齐 `IMPORT_TEMPLATE_COLUMNS` / 后端 `matchesV0729`；属 **UX 对齐 / 防御**，非伪造成功、非掩盖契约语义。xlsx / 直打 API 仍依赖 301 live（P2 残余） |
| **BUG-002** | **同意 DEV**：根因属 live 后端未加载 301 v0729 导入（模板 GET 仍 V1.1；partial 报告文案要求「产品编码」），**非 304 UI 映射缺陷**。环境阻塞**不**构成对 304 代码的 REQUEST_CHANGES |

**重要注明：** 本 APPROVE **不**宣称 E2E / `TESTRUN-WSC-E2E-V13` 通过，亦**不**关闭 tester 侧 BUG yaml。场景 2（强制 partial-success）及 BUG-001 的 API closeWhen **仍依赖**部署/重启含 TASK-WSC-301 的 `data-chain-service`。审查复跑 Vitest **16 PASSED**。未调度 tester；未改业务代码 / state / events。

## Round 3 Findings

| id | severity | status | evidence | closeWhen |
|---|---|---|---|---|
| FIND-WSC-304-R3-001 | P2 | OPEN | CSV 预检仅覆盖 `.csv`/`text/csv`；xlsx 旧模板与直打 API 仍依赖后端 `ERR_IMPORT_TEMPLATE_UNSUPPORTED`（DEV 已声明残余） | 301 live 部署后：GET template=v0729；POST legacy → 400+TEMPLATE_UNSUPPORTED；tester 复跑场景 3 并可关闭 BUG-001 API closeWhen |
| FIND-WSC-304-R3-002 | P2 | OPEN | BUG-002 / 场景 2 在 V1.1 live 上无法 partial_success；仓内 301 集成测与源码正确 → 环境/部署门禁 | HOTFIX 部署 301 后独立 tester 复跑场景 2 PASSED |
| FIND-WSC-304-R3-003 | P3 | OPEN | BUG-001/002/003 yaml 仍 `status: OPEN`；DEV 已部分处置但未回写 bug 文件 | 开发/编排在复测后按 closeWhen 更新 bug 状态 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**（进入复测；E2E 仍依赖 301 live）。

## Round 3 核对清单

### 1. BUG-003 三段编码

| 项 | 结果 |
|---|---|
| 禁止四段 `E2E-V13-API-*` | PASS（场景 4/5 改用 `makeProductCode`） |
| 形态 `E2E-API-{nnnn}` / `E2E-DET-{nnnn}`，数字段 ≥4 | PASS（`Date.now().slice(-6)`） |
| 落点 ⊆ `tests/e2e/specs/**` | PASS |

### 2. BUG-001 UI CSV 预检 vs 后端权威

| 项 | 结果 |
|---|---|
| 路径 ⊆ writeSet `frontend/.../import/**` | PASS |
| 与后端语义 | PASS：`isV0729HeaderRow` ≡ 列集合精确匹配 `IMPORT_TEMPLATE_COLUMNS`（同 `matchesV0729`）；`looksLikeImportHeaderRow` 避免无表头误杀 |
| 是否「掩盖后端」 | **否（不构成 P0/P1）**：预检结果与契约意图一致（旧模板→态④）；不产生假成功；失败时不调用 `importProducts`。属客户端 UX 对齐，**不替代**后端权威拒绝 |
| Vitest 预检负例 | PASS（`CSV legacy headers preflight`；16 tests） |

### 3. BUG-002 环境归因

| 项 | 结果 |
|---|---|
| 同意非本任务代码缺陷 | **是**（live V1.1 行解析 vs 仓内 301） |
| 因环境拒绝 CR APPROVE？ | **否**（按复审指令：不因环境阻塞批准进入测试门禁） |

### 4. writeSet / Vitest

| 项 | 结果 |
|---|---|
| 本轮 304 声明变更 ⊆ writeSet | PASS（`useProductImport(.spec).ts`、`index.ts`、`p0-wsc-v1.3.spec.ts`、DEV） |
| 未改 backend/contracts/api/editor/detail/browse/auth.ts | PASS（本任务交付面；工作区并行脏文件属 301/302/303 等） |
| Vitest | **16 PASSED** |

### 审查复跑证据（R3）

```text
cd frontend
pnpm exec vitest run src/features/catalog/import --reporter=default
# ✓ useProductImport.spec.ts (16 tests)  — PASSED
```

### 隔离声明（R3）

- 审查者未修改被审业务代码、DEV/BUG/TESTRUN、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；**不**代标 E2E PASSED / 任务 VERIFIED；未调度 tester。
- **decision: APPROVE**（复测门禁；E2E 仍依赖 301 live 部署）。

### 计数（R3 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 2（环境/API 残余；不阻塞本轮代码 APPROVE） |
| P3 | 1（bug yaml 状态未同步） |

---

## Round 2（历史；APPROVE）

```yaml
reviewId: REV-CODE-TASK-WSC-304-R2
round: 2
decision: APPROVE
reviewedAt: 2026-08-02T18:25:00+08:00
```

## Round 2 结论（摘要）

**APPROVE** — 本轮 **P0=0，P1=0**。FIND-WSC-304-R1-001 已按方案 b 关闭：`git diff -- tests/e2e/fixtures/auth.ts` 为空（工作区相对索引无本任务改动；仍为三参 `switchRole` + `.role-switch select`）；`p0-wsc-v1.3.spec.ts` 仅 `import { loginAs }`，场景 1/6 使用文件内联 `expectCurrentRole` / `switchRoleInline`。DEV-304 变更表与 PLAN writeSet 字面一致，并显式声明未改 `auth.ts`。审查复跑 import Vitest **15 PASSED**。正式 Playwright（`TESTRUN-WSC-E2E-V13`）仍须独立 tester；本审查不代标 E2E 通过 / VERIFIED。

## Round 2 Findings 闭环

| id | severity | R2 状态 | 证据 |
|---|---|---|---|
| FIND-WSC-304-R1-001 | P1 | **CLOSED** | `auth.ts` 无 diff；规格内联助手；DEV 变更表声明「未改 auth.ts」+ 仅只读 `loginAs` |
| FIND-WSC-304-R1-002 | P2 | **CLOSED** | Vitest 新增 `all_success submit`（title/detail/无报告链）；套件 15 PASSED |
| FIND-WSC-304-R1-003 | P2 | **CLOSED** | `_trial-run-out*.txt` 已不存在；`p0-wsc-v1.1.spec.ts` 由 DEV 标明属前序 206、非本任务交付 |
| FIND-WSC-304-R1-004 | P3 | OPEN（不阻塞） | 可选 ImportDialog 挂载测；交 tester §6.1 DOM 覆盖 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## Round 2 复审核对

| 项 | 结果 |
|---|---|
| `tests/e2e/fixtures/auth.ts` 相对索引无本任务改动 | PASS（`git diff` 空；`git status` 无该路径） |
| 场景 1/6 不依赖共享 fixture 越界新 API | PASS（仅 `loginAs`；`switchRoleInline` / 内联 `expectCurrentRole`） |
| DEV 变更表 ⊆ PLAN writeSet | PASS（含内联助手说明；明确排除 `auth.ts`） |
| 禁写 contracts/api/backend/editor/detail/browse/components | PASS（本任务交付面） |
| Vitest import feature | PASS（15 tests） |

### 审查复跑证据（R2）

```text
cd frontend
pnpm exec vitest run src/features/catalog/import --reporter=default
# ✓ useProductImport.spec.ts (15 tests)  — PASSED
```

### 隔离声明（R2）

- 审查者未修改被审业务代码、DEV 报告、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 E2E 通过 / VERIFIED；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

### 计数（R2 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 0 |
| P3 | 1（FIND-004，不阻塞） |

---

## Round 1（历史；REQUEST_CHANGES）

```yaml
reviewId: REV-CODE-TASK-WSC-304-R1
taskId: TASK-WSC-304
round: 1
decision: REQUEST_CHANGES
actorInstance: code-reviewer-wsc-304
reviewedAt: 2026-08-02T18:20:00+08:00
```

## 结论（R1）

**REQUEST_CHANGES** — 本轮 **P0=0，P1=1**。导入 feature 本体（v0729 文案、四态、旧模板→态④、`ERR_IMPORT_TEMPLATE_UNSUPPORTED`、部分成功双计数+报告、白名单投影、RBAC 结构隐藏）与 Vitest / §6.1 规格内容整体达标；审查复跑 `pnpm exec vitest run src/features/catalog/import` **14 PASSED**，`vue-tsc --noEmit` **exit 0**。但 `tests/e2e/fixtures/auth.ts` 相对 HEAD 有改动且 **不在** PLAN § TASK-WSC-304 `writeSet`、亦未列入 DEV-304 变更表，而 `p0-wsc-v1.3.spec.ts` 直接依赖其新 API——字面 scope-check / 可独立合入复现失败，故不得批准进入 tester 门禁。正式 Playwright（`TESTRUN-WSC-E2E-V13`）仍须独立 tester；本审查不代标 E2E 通过。

## Findings（R1；已由 R2 闭环）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-304-R1-001 | P1 | `git diff`：`tests/e2e/fixtures/auth.ts` 改为 RoleSwitcher/`expectCurrentRole`/`switchRole(page, role)` 二参签名。PLAN-WSC-4.1 § TASK-WSC-304 `writeSet` 仅枚举 `import/**`、`tests/fixtures/import/**`、`tests/e2e/specs/**`、`package.json`、`playwright.config.ts`、`reports/p0-wsc-v1.3/**`——**不含** `tests/e2e/fixtures/**`。DEV-304 变更表亦未声明。`p0-wsc-v1.3.spec.ts` L5–10/151–153/331+ 直接 `import { expectCurrentRole, switchRole } from '../fixtures/auth'`。仅合入 DEV 声明 writeSet 时，场景 1/6 无法独立复现 | (a) 计划扩权/HOTFIX 将 `tests/e2e/fixtures/auth.ts`（或 `fixtures/**`）纳入 304 writeSet，并补录 DEV 变更表；或 (b) 不改 writeSet 外文件，在 `p0-wsc-v1.3.spec.ts` 内联角色切换助手并回退对共享 fixture 未声明变更的依赖。完成后 scope-check 字面通过 | PLAN-WSC-4.1 § TASK-WSC-304 writeSet；§6.1 场景 1/6 |
| FIND-WSC-304-R1-002 | P2 | Vitest 对态① `all_success` 仅经 `classifyImportResult` 断言；`submit()` 异步路径覆盖了 partial / all_row_failure / file_rejected（含 TEMPLATE_UNSUPPORTED 与 FORMAT 分界），缺全成功 submit 结果区文案断言 | 可选补一条 `successCount>0 ∧ failureCount=0` 的 submit 用例（title/detail/无报告链） | REQ-CAT-008；§3.4 态① |
| FIND-WSC-304-R1-003 | P2 | 工作区另有未跟踪噪声：`tests/e2e/_trial-run-out*.txt`；以及未列入 DEV-304 的 `tests/e2e/specs/p0-wsc-v1.1.spec.ts`（若属本任务扩展则应补录，若属前序脏文件则合入时隔离） | 清理 trial 输出；按任务 writeSet 隔离暂存；补全 DEV 清单或排除非本任务文件 | PLAN-WSC-4.1 §6.2 |
| FIND-WSC-304-R1-004 | P3 | 四态/白名单/权限测均走 composable 纯函数与 `useProductImport`；未 mount `ImportDialog.vue` 做 DOM 抽样（testid/四色图标） | 可选补组件挂载测；或交独立 tester 的 §6.1 E2E 覆盖结果区 DOM | REQ-CAT-008 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=1** → **REQUEST_CHANGES**。

## 检查清单（R1 当时）

### 1. 范围 / writeSet / denyModify

| 项 | 结果 |
|---|---|
| 304 声明交付 ⊆ `writeSet`（import + fixtures/import + e2e specs/config/reports） | **FAIL**（FIND-001：`tests/e2e/fixtures/auth.ts`；R2 已关闭） |
| 未写 `contracts/**`、`frontend/src/api/**`、`backend/**`、`**/sql/**` | PASS（本任务交付面只读消费 `@/api/catalog`） |
| 未写 `frontend/src/components/**`、`editor/**`、`detail/**`、`browse/**` | PASS（browse 挂载属前序 FIX1；本任务未改 browse） |
| 未写 `admin/**`、`maintenance/**`、`styles/**`、`theme/**`、`layouts/**`、`shell/**`、`router/**`、`product/**` | PASS |
| `mustDifferFrom: developer-wsc-304` | PASS（本实例 `code-reviewer-wsc-304`） |

### 2. REQ-CAT-008 / §3.4：v0729 + 四态 + 旧模板

| 项 | 证据 | 结果 |
|---|---|---|
| 模板下载为 v0729 | `ImportDialog`「下载 v0729 导入模板」+ `data-testid=import-template-xlsx`；hint 文案；列常量来自只读 `IMPORT_TEMPLATE_COLUMNS` | PASS |
| 四态可区分 | `ImportResultState` + 结果区四标题/四图标 class；Vitest classify + partial/all_row_failure/file_rejected | PASS（态① submit 路径见 FIND-002） |
| 旧模板 → 态④ + 错误码映射 | `mapImportRequestError` + submit catch → `file_rejected`；Vitest legacy / FORMAT 分界；无报告链、无双计数伪装 | PASS |
| 部分成功：两计数 + 报告入口 | `resultDetail` / `import-counts` / `showReportLink`；Vitest + E2E 场景 2（强制 partial，不可跳过） | PASS |
| 报告白名单不扩大 | `projectWhitelistErrorRows` / `buildErrorReportCsv` / `containsForbiddenImportDisplay` 负例（信用代码/ownerDID/哈希） | PASS |

### 3. REQ-RBAC-001

| 项 | 证据 | 结果 |
|---|---|---|
| USER 无入口（结构不可达） | `v-if="open && productImportVisible"`；`shouldRenderImportOverlay` + `canImportProduct('USER')===false` Vitest；E2E 场景 6 规格 | PASS（E2E 执行交 tester） |
| ADMIN/PROVIDER 可达 | 同上 Vitest true；E2E 场景 6 | PASS（规格已写） |
| 非纯 CSS 藏权限 | 结构 `v-if` | PASS |

### 4. REQ-API-001（304：导入列/E2E 对照）

| 项 | 证据 | 结果 |
|---|---|---|
| E2E 场景 4 OpenAPI 编辑 | `p0-wsc-v1.3.spec.ts` Swagger 回填→保存→再开 | PASS（规格；执行交 tester） |
| E2E 场景 5 详情只读 | endpoints / 文档信息 / 无写控件 | PASS（规格；执行交 tester） |

### 5. testScope / §6.1 E2E 落盘

| 项 | 结果 |
|---|---|
| Vitest：四态（含 template-unsupported vs 态③）、白名单负例、权限 | PASS（复跑 14 PASSED） |
| 规格文件 `tests/e2e/specs/p0-wsc-v1.3.spec.ts` | PASS（场景 1–6；场景 2 强制 partial-success） |
| `playwright.config.ts` 报告 → `tests/e2e/reports/p0-wsc-v1.3/` | PASS（html + junit；`.gitkeep` 存在） |
| `test:p0-v13` script | PASS |
| evidenceId `TESTRUN-WSC-E2E-V13` 声明 | PASS（规格头注释 + DEV） |
| 正式 E2E 执行 | **不在本审查范围**（独立 tester；开发侧未标通过） |

### 6. UX 不回退 PLAN-WSC-3.1 导入弹窗

| 项 | 结果 |
|---|---|
| 保留 overlay / modal / drop-zone / 结果四色图标 scoped 样式；令牌变量（`--card-bg`/`--blue`/…） | PASS（代码层面；未见强制 Element Plus 或质感回退） |
| 增量主要为 v0729 文案与 testid | PASS |

## 审查复跑证据（R1）

```text
cd frontend
pnpm exec vitest run src/features/catalog/import --reporter=default
# ✓ useProductImport.spec.ts (14 tests)  — PASSED

pnpm exec vue-tsc --noEmit -p tsconfig.app.json
# exit 0（无 catalog/import 诊断）
```

未执行正式 Playwright（留给独立 tester / `TESTRUN-WSC-E2E-V13`）。

## 隔离声明（R1）

- 审查者未修改被审业务代码、DEV 报告、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代写伪造 APPROVE；未调度 tester。
- **decision: REQUEST_CHANGES**（R1；见文首 Round 2 APPROVE）。

## 计数（R1）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 1 |
| P2 | 2 |
| P3 | 1 |
