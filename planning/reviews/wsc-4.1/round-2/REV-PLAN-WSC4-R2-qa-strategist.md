# Round 2 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC4-R2-qaStrategist
planId: PLAN-WSC-4.1
round: 2
role: qaStrategist
actorInstance: qa-strategist-wsc-004-r2
snapshotIdAtReview: SNAP-WSC-004
decision: APPROVE
summary: |
  相对 PLAN-WSC-4.0 Round 1 本角色五条 ISSUE（ISSUE-QA-WSC4-R1-001..005）的 closeWhen
  均已在 PLAN-WSC-4.1 字面满足，可关闭。301 testScope fixture 表含 all-fail（态③）、
  legacy-template / format-invalid 分界与零写入、模板 GET、四类型 typeSpecific round-trip、
  NO_UPDATE；§6.1 / 304 强制 partial-success，并钉死 evidenceId / 命令 / 报告落点与
  playwright.config 调整。本领域无未关闭异议、无新增 P1；未执行任何测试；不宣称实现
  或 E2E 已通过；本 APPROVE 仅表示计划层可测性门禁对本角色已闭合。
```

## Round 1 ISSUE closeWhen 确认

| id | severity | closeWhen 结论 | 证据（PLAN-WSC-4.1） |
|---|---|---|---|
| ISSUE-QA-WSC4-R1-001 | P1 | **关闭** | TASK-WSC-301 `testScope` fixture 表显式行 **`all-fail`（态③）**：`successCount=0` ∧ `failureCount>0` ∧ `reportId` 必填；**非**请求级 `ERR_IMPORT_*` 文件拒绝；报告可下载且白名单字段可读。`writeSet` 含 `all-fail` fixture。304 Vitest 声明「四态 UI 分支…与态③对照」（可选对照满足）。发布门禁 §9.3 亦点名态③。 |
| ISSUE-QA-WSC4-R1-002 | P1 | **关闭** | 301 `testScope` 表：`legacy-template`（可解析、列集≠v0729，须覆盖 V1.1 最小列集或既有 fixture 升级语义之一 → HTTP 错误 + `ERR_IMPORT_TEMPLATE_UNSUPPORTED` + **零写入** + **非**行级/部分成功 envelope）；`format-invalid` → `ERR_IMPORT_FORMAT_INVALID` 且**不得**映射 TEMPLATE_UNSUPPORTED。§6.1 场景 3 / 304 `readSet` 显式引用 301 `legacy-template`（或报告声明等价路径）。 |
| ISSUE-QA-WSC4-R1-003 | P1 | **关闭** | 301 `testScope` 可勾选项齐全：(1) **模板 GET** — 列组（分组行+列名行）或字节抽样与 `product/assets/import/product-import-template-v0729.xlsx` 一致，且与 `ImportTemplateColumns` 列名行一致；(2) **typeSpecific round-trip** — API / DATASET / REPORT / OTHER 各至少 1 组（含 OTHER `contentDescription`）；(3) **`NO_UPDATE`** 可写入并回读。 |
| ISSUE-QA-WSC4-R1-004 | P2 | **关闭** | §6.1 场景 2：**强制** `partial-success`（两计数 + 报告入口）；推荐另跑 full-success，但 partial **不可跳过**。§1.2 / 304 `objective` / `acceptance` / §9.6 同口径。满足 closeWhen 首选（强制 partial；非 OR）。 |
| ISSUE-QA-WSC4-R1-005 | P2 | **关闭** | §6.1（304 `testScope`「见 §6.1」）：**`evidenceId: TESTRUN-WSC-E2E-V13`**（强制，非建议）；命令 `pnpm exec playwright test --config=playwright.config.ts`（或等价 script，须写入报告）；规格须在报告声明实际文件名（建议 `p0-wsc-v1.3.spec.ts`）；报告落点 **`tests/e2e/reports/p0-wsc-v1.3/`**；`playwright.config.ts` **必须**指向该目录（304 `writeSet` 已许可并强制调整）；禁止静默复用 `p0-wsc-v1.2-ux`。 |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| （无） | — | Round 2 本领域无新增异议；R1 五条均可关闭。 | — | — |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 301–304 均有 acceptance + testScope + riskTags；§6.2 要求独立 tester 执行 testScope。R1 完整性缺口已闭合。 |
| 验收可观测性（P0） | SNAP 五条 P0 均有任务映射（§7）。CAT-008 四态/旧模板、模板对齐、扩展字段、NO_UPDATE、E2E partial/证据钉死均可按字面勾选。 |
| 导入四态 | §3.4 判定式清晰；301 含 full / partial / **all-fail** / 请求级（legacy / format / oversize）；304 Vitest 四态含 template-unsupported 与态③对照。 |
| 旧模板拒绝 | §3.2 + 301 fixture 分界 + 304 E2E 场景 3 引用同一负例语义；可调度。 |
| E2E 分层 | 304 独占 `tests/e2e/**`；§6.1 场景 1–6 + 机械条款（evidenceId/命令/报告/config）齐全；失败不得发布。 |
| OpenAPI / 扩展描述 | 302/303 Vitest 覆盖编辑与只读；301 含共享 OpenAPI fixture 关键字段、写冲突、坏接口定义行失败——本轮无新 P1。 |
| RBAC | §4 矩阵 + 301 USER 403 + 304 E2E 场景 6；可接受。 |
| Fixture 所有权 | 301 与 304 均可写 `tests/fixtures/import/**`，DAG 串行（304 dependsOn 301），无并行写冲突。 |
| 环境 vs 质量 | 本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 单测/契约 | 集成（301） | Vitest UI | E2E（304） | 缺口 |
|---|---|---|---|---|---|
| REQ-CAT-008 | 错误码 requestLevel；ImportTemplateColumns | full/partial/**all-fail**/legacy/format/oversize；模板 GET | 四态含 template-unsupported / 态③ | 场景 2–3（强制 partial）；证据钉死 | 无（计划层） |
| REQ-CAT-004 | — | typeSpecific 读/兼容 | 303 只读抽样 | 场景 5 | 无（计划层） |
| REQ-CAT-005 | — | typeSpecific 写 + NO_UPDATE + 写冲突 | 302 编辑/Swagger | 场景 4 | 无（计划层） |
| REQ-API-001 | OpenAPI 形状校验 | 坏接口定义→行失败；共享 fixture 关键字段 | 302/303 | 场景 4–5 | 无（计划层） |
| REQ-RBAC-001 | — | USER 写/报告 403 | 302/303/304 隐藏 | 场景 6 | 可接受 |

## 说明

- 输入：`planning/proposals/PLAN-WSC-4.1.md`；R1 异议源：`planning/reviews/wsc-4.0/round-1/REV-PLAN-WSC4-R1-qa-strategist.md`。
- 需求权威只读：`product/requirements/SNAP-WSC-004.md`（APPROVED）。
- **未**修改计划、源码或 Run `state.yaml` / `events.jsonl`。
- **未**执行 Vitest / JUnit / Playwright；**未**代替 developer/tester 宣称通过。
- 决策：`APPROVE`（本角色 R1 ISSUE-QA-WSC4-R1-001..005 全部关闭且无新增 P1）；关闭结论均有计划正文与任务字段字面证据，非伪造。
- ISSUE 正式关闭仍须原提出者（本角色本轮）确认；本文件即该确认落盘。
