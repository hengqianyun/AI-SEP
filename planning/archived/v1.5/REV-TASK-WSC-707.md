# 代码审查

```yaml
reviewId: REV-TASK-WSC-707
taskId: TASK-WSC-707
planId: PLAN-WSC-6.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-707-r1
decision: REQUEST_CHANGES
p0: 0
p1: 2
p2: 2
p3: 1
closedFindings: []
openFindings:
  - FIND-WSC-707-R1-001
  - FIND-WSC-707-R1-002
  - FIND-WSC-707-R1-003
  - FIND-WSC-707-R1-004
  - FIND-WSC-707-R1-005
contracts: wsc-contracts@2.3.0（只读；本任务未改 contracts/api）
riskTags: [e2e-gate, release-evidence]
reviewedAt: 2026-08-12T13:40:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-6.2.md（TASK-WSC-707 writeSet·denyModify·acceptance·testScope；§6.1；§9 item 8）
  - ai/runs/RUN-WSC-009/dev/DEV-TASK-WSC-707.md
  - 实地：tests/e2e/specs/p0-wsc-v1.5.spec.ts、playwright.v15.config.ts、package.json、helpers/wsc-v15-fixtures.ts
  - 实地：playwright.config.ts（默认仍指向 v1.4）、specs/p0-wsc-v1.4.spec.ts（denyModify，未改）
  - 证据：tests/e2e/reports/p0-wsc-v1.5/{EVIDENCE.md,run-console.log,run-v14-console.log,junit.xml}
mustDifferFrom:
  - developer（本任务实现者；本实例未兼任）
spotCheck: |
  writeSet：p0-wsc-v1.5.spec.ts、playwright.v15.config.ts、package.json(+test:p0-v15)、
  wsc-v15-fixtures.ts、reports/p0-wsc-v1.5/**。
  denyModify：playwright.config.ts 仍 testMatch v1.4 + reports/p0-wsc-v1.4；未改 v1.4.spec；
  本任务归因未写 frontend features/layouts/router 等业务源码。
  test:p0-v15 script 一字不差；§6.1 场景 1–8 均有对应用例；evidenceId TESTRUN-WSC-E2E-V15 在
  EVIDENCE.md / fixtures / spec 头注释可见；junit 12/0。
  test:p0-v14 可执行：10 passed / 5 failed。其中 heading「数据目录」、高级筛选收起致
  catalog-search 不可见、PROVIDER 仍期望 nav-catalog-maintenance — 属 V1.5 故意产品增量 vs
  冻结 v1.4 期望（denyModify 禁止本任务改 v1.4.spec）。但 2a ADMIN 用户 soft-delete 后表内
  仍含用户名，非 V1.5 增量，且 EVIDENCE 把 5 失败一律归因「产品增量」——不满足 testScope
  「至少 RBAC/基线相关场景通过」与 §9.7 用户管理不回退之证据要求。
  未代 tester PASS；未标 VERIFIED；未写 state/events；未改业务代码。
```

## Round 1 结论

**REQUEST_CHANGES** — **P0=0，P1=2**。V1.5 规格、独立 config、script 共存与 §6.1 场景覆盖源码可复核；**不得**在 P1 未清零时批准进入独立 tester 门禁。

**门禁口径（本轮裁定）**：

| 条款 | 字面 | 裁定 |
|---|---|---|
| acceptance 共存 | `test:p0-v14` **仍可执行** + 默认 config 仍指向 v1.4 报告目录 | **满足**（脚本/config 未冲掉） |
| testScope | 强制跑 v14，**至少 RBAC/基线相关场景通过** | **未满足** — 用户管理 CRUD soft-delete 失败，且非 V1.5 故意增量 |
| §9 item 8「仍可跑通」 | 与 acceptance「仍可执行」+ testScope「至少…通过」联读；**不**解释为「v1.4 全套 15/15 必须绿」（否则与 denyModify 冻结 v1.4.spec + V1.5 故意改文案/收起/维护 ADMIN-only 自相矛盾） | 故意增量失败可用**逐条失败矩阵**作豁免证据；**禁止**笼统「全是产品增量」后静默 APPROVE |
| 是否要求本任务改 v1.4 期望 | denyModify 禁止改 `p0-wsc-v1.4.spec.ts` | **不**因故意增量对 707 提「改 v1.4.spec」；豁免靠证据文档。soft-delete 须复跑洗净或升级业务 hotfix（写集外） |

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-707-R1-001 | P1 | **OPEN** | `test:p0-v14` 失败用例 **2a ADMIN 用户管理 CRUD**：soft-delete 后 `users-table` 仍含 `e2e_u_*`（见 `run-v14-console.log`）。**不是** V1.5 文案/收起/维护门控增量。与 testScope「至少 RBAC/基线通过」、§9.7「用户管理不回退」冲突 | 干净环境复跑证明偶发并附新日志；或编排器开业务 hotfix 修 soft-delete/确认流后附绿证据。不得仅用「产品增量」一句话关闭 | REQ-RBAC-001；§9.7；testScope v14 |
| FIND-WSC-707-R1-002 | P1 | **OPEN** | `EVIDENCE.md`「V1.4 coexistence note」将 5 失败一律归因「public catalog heading removed; PROVIDER maintenance ADMIN-only」——**漏写 soft-delete**；heading 实为改名「全链数据目录」非「removed」；失败 #4 首错是高级筛选默认收起致 `catalog-search` 不可见，其后才触及 PROVIDER 维护期望。共存豁免证据不可审计 | 按失败用例写**逐条矩阵**（用例 id / 实际断言 / 归类=故意增量|基线回归|环境偶发 / 对应 REQ 或 SNAP）；故意增量方可豁免；001 清零后本 finding 方可关 | §9 item 8；acceptance 交付说明；ISSUE-QA-WSC6-R1-003 |
| FIND-WSC-707-R1-003 | P2 | **OPEN** | §6.1 场景 7 要求「筛/Tab 清空选择」；v1.5 spec 覆盖 Tab 清选择 + 无跨页文案，**未**断言筛选 Cascader/路径变更后清空选择 | 补筛选项清空 selectedIds 的 E2E 断言（或注明由 706 单测覆盖并引用）；不阻塞 001/002 | REQ-CAT-013；§6.1 #7 |
| FIND-WSC-707-R1-004 | P2 | **OPEN** | `writeEvidenceHeader()` 每次 `beforeAll` 覆写 `EVIDENCE.md` 为短头，易冲掉手工共存矩阵；与可审计交付冲突 | 改为 append/合并模板，或生成后由固定 post-step 写入矩阵段 | 证据可维护性 |
| FIND-WSC-707-R1-005 | P3 | **OPEN** | `wsc-v15-fixtures.ts` 导入未用的 `loginAs`；`installUncategorizedPageFixture` 已导出但场景 4 内联重复夹具 | 清理未用导入/复用 helper | 可维护性 |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — 仅 e2e v15 规格/config/script/helper/报告 |
| denyModify：`playwright.config.ts` 未改成 v1.5-only | **PASS** — 仍 v1.4 testMatch + `reports/p0-wsc-v1.4` |
| denyModify：`p0-wsc-v1.4.spec.ts` | **PASS** — 无 diff |
| denyModify：业务 feature/layouts/router/backend/contracts/sql | **PASS（707 归因）** — 本任务未写入；他任务脏文件不计入 |
| `test:p0-v15` script 一字不差 | **PASS** — `playwright test specs/p0-wsc-v1.5.spec.ts --config=playwright.v15.config.ts`；`test:p0-v14` 保留 |
| §6.1 场景 1–8 规格覆盖 | **PASS（有 P2 备注）** — 1..8 均有对应用例；#7 筛清空 → FIND-003 |
| `evidenceId: TESTRUN-WSC-E2E-V15` | **PASS** — EVIDENCE.md / fixtures 常量 / junit 套件叙述 |
| v15 开发自测 12 passed | **记录** — `run-console.log` / junit failures=0；**不**代 tester PASS |
| v14 共存可执行 | **PASS** — script+config 可跑；exit 1 因断言失败，非命令缺失 |
| v14「至少 RBAC/基线通过」+ 豁免文档 | **FAIL** — FIND-001 / FIND-002 |
| §9 item 8 | **FAIL（当前证据）** — 故意增量可豁免，但 soft-delete + 笼统归因导致门禁证据不合格 |
| 开发自测 ≠ 独立 tester | **记录** — 未代 tester；未标 VERIFIED |
| 未写 state·events / 未改业务代码 | **PASS** |
| `mustDifferFrom` 实现者 | **PASS**（`code-reviewer-wsc-707-r1`） |
| P0 / P1 | **P0=0；P1=2** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester；未代 tester PASS。
- **decision: REQUEST_CHANGES**（P1 未清零）。
- **不**要求本任务违反 denyModify 去改 `p0-wsc-v1.4.spec.ts` 以适应 V1.5 故意增量；豁免路径是准确的失败矩阵文档。soft-delete 须复跑或业务侧 hotfix 证据。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 2 | FIND-001 soft-delete 基线失败；FIND-002 EVIDENCE 归因不可审计 |
| P2 | 2 | FIND-003 场景7筛清空；FIND-004 EVIDENCE 覆写 |
| P3 | 1 | FIND-005 helper 卫生 |
