# Round 1 Review — QA Strategist

```yaml
reviewId: REV-PLAN-WSC4-R1-qaStrategist
planId: PLAN-WSC-4.0
round: 1
role: qaStrategist
actorInstance: qa-strategist-wsc-004-r1
snapshotIdAtReview: SNAP-WSC-004
decision: REQUEST_CHANGES
summary: |
  四任务均有 acceptance + testScope；§3.2/§3.4 对旧模板→态④与 FORMAT/TEMPLATE 分界口径清晰；
  304 Vitest「四态 UI」与 E2E 场景 3（旧模板拒绝）、发布门禁 §9.3 方向正确。
  但 301 testScope 未显式覆盖态③行级全失败，旧模板负例缺可调度 fixture/无写入/分界断言，
  且模板 GET、扩展 typeSpecific、NO_UPDATE 仅在 acceptance 而未入 testScope，
  导致 P0 底座可在无对应自动化证据下标 VERIFIED。存在 P1 → REQUEST_CHANGES。
  未执行任何测试；不宣称实现或 E2E 已通过；不伪造 APPROVE。
```

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-QA-WSC4-R1-001 | P1 | §3.4 / §1.2 / 发布门禁 §9.3 要求导入**四态可区分**；301 `acceptance` 写「四态后端语义不回退」，但 `testScope` 仅列 v0729 `full-success` / `partial-success` + 请求级（旧模板 / oversize / format-invalid）。**态③**（`successCount=0` 且 `failureCount>0` 的行级全失败 + 报告）无命名 fixture/期望。tester 按字面执行无法勾选态③；与 PLAN-WSC-2.2 对混行/计数可测性要求的精神不一致（该计划亦以 fixture 表钉死可观测期望）。304 Vitest「四态 UI」不能代替 301 后端语义回归。 | 在 TASK-WSC-301 `testScope`（建议 fixture 表）增加 **`all-fail`（或等价命名）**：同文件全部行失败 → `successCount=0` ∧ `failureCount>0` ∧ `reportId` 必填 ∧ **非**请求级 `ERR_IMPORT_*` 文件拒绝；报告可下载且白名单字段可读。可选：304 Vitest/E2E 勾选引用同一语义（非必须与 301 同文件名，但须可对照 §3.4 态③）。 | REQ-CAT-008 |
| ISSUE-QA-WSC4-R1-002 | P1 | 旧模板文件级拒绝是本版 P0 增量核心（OQ-V13-001 / §3.2）。301 `testScope` 仅一句「旧模板负例 → `ERR_IMPORT_TEMPLATE_UNSUPPORTED`」；`writeSet` 提及 fixture 但未钉可调度清单。缺口：(1) 无命名负例（建议 `legacy-template`）代表**可解析但列集≠v0729**（须显式覆盖 V1.1 最小列集或既有 `tests/fixtures/import/*.csv` 升级后的判定语义之一）；(2) `acceptance`「无部分写入」未落入 `testScope` 断言；(3) §3.2「FORMAT vs TEMPLATE」分界：`format-invalid` 回归条未写明**不得**映射为 `ERR_IMPORT_TEMPLATE_UNSUPPORTED`。304 E2E 场景 3 依赖可引用的同一负例，否则 UI 态④与后端码可能各测各的。 | 301 `testScope` 用表列出至少：`legacy-template`（可解析、列集不匹配 → HTTP 错误 + `ERR_IMPORT_TEMPLATE_UNSUPPORTED` + **零**该请求产生的产品写入 + **非**行级结果态/非部分成功 envelope）；`format-invalid`（非允许扩展名/无法解析 → `ERR_IMPORT_FORMAT_INVALID`，**不得**为 TEMPLATE_UNSUPPORTED）。304 E2E 场景 3 显式引用 `legacy-template`（或报告声明的等价路径）。 | REQ-CAT-008 |
| ISSUE-QA-WSC4-R1-003 | P1 | 301 `acceptance` 含：GET 模板与权威 v0729 列组一致；dataset/report/other 扩展字段可读写；`UpdateFrequency` 含「不更新」/`NO_UPDATE`。`testScope` 仅覆盖 endpoints+swagger 原文 round-trip、旧 `endpoint` 兼容读、导入相关与 RBAC——**未**把上述三条列入可执行范围。按 §6.1「独立 tester 执行该任务 testScope」门禁，301 可在缺模板对齐/扩展字段/枚举证据下 VERIFIED，削弱 REQ-CAT-004/005/API-001 与契约 2.1.0 底座可测性。 | 301 `testScope` 增补可勾选项：(1) GET 导入模板：列组（分组行+列名行）或字节抽样与 `product/assets/import/product-import-template-v0729.xlsx` 一致；(2) 产品写读 round-trip：API / DATASET / REPORT / OTHER 扩展 `typeSpecific` 字段至少各抽样 1 组（含 OTHER `contentDescription`）；(3) `NO_UPDATE`（或契约冻结名）可写入并回读。 | REQ-CAT-004, REQ-CAT-005, REQ-CAT-008, REQ-API-001 |
| ISSUE-QA-WSC4-R1-004 | P2 | 304 E2E 场景 2 写「成功**或** partial-success」。OR 语义允许整轮 E2E 只跑全成功、跳过混行计数+报告入口的 UI 主路径。后端 301 虽有 partial，但发布门禁依赖 304 E2E；相对 PLAN-WSC-2.2 §6.1 场景 4 强制 partial-success（OQ-V11-004）偏弱。 | 将场景 2 改为至少覆盖 **partial-success**（推荐：full-success 与 partial-success **均**跑）；或明文规定 E2E 只强制其一，并在 304 Vitest 四态勾选表中把另一态标为**关闭发布门禁所必需**的证据行（含报告路径）。 | REQ-CAT-008 |
| ISSUE-QA-WSC4-R1-005 | P2 | 304 `testScope`：证据 id 为「建议」`TESTRUN-WSC-E2E-V13`；命令与报告路径仅「写入测试报告」，计划正文未钉死可执行入口。仓库现状：`tests/e2e/playwright.config.ts` 报告目录仍指向 `reports/p0-wsc-v1.2-ux`；规格基线为 `p0-wsc-v1.1.spec.ts` / `p0-wsc.spec.ts`。独立 tester +「失败不得发布」已写，但机械关门弱于 PLAN-WSC-2.2 §6.1。 | 在 304 `testScope`（或等价 §E2E 小节）钉死：Playwright 命令或 `tests/e2e` package script、本轮规格文件名、报告落点（`tests/e2e/reports/p0-wsc-v1.3/` **或**复用旧目录时强制报告声明）、`evidenceId: TESTRUN-WSC-E2E-V13`（非仅建议）；与 `playwright.config.ts` 调整条款（已有 writeSet 许可）对齐。 | REQ-CAT-008, REQ-API-001, REQ-RBAC-001 |

## 领域核对（非异议摘要）

| 检查项 | 结论 |
|---|---|
| 任务 testScope 有无 | 301–304 均有 acceptance + testScope + riskTags；门禁 §6.1 要求独立 tester 执行 testScope。完整性缺口见 ISSUE-001..003。 |
| 验收可观测性（P0） | SNAP 五条 P0 均有任务映射（§7）。CAT-008 旧模板/四态、API-001 OpenAPI 三面、RBAC 矩阵 §4 口径可测；底座证据落点受 ISSUE-001..003 阻塞批准。 |
| 导入四态 | §3.4 判定式清晰（①②③④）；旧模板归④正确。304 Vitest 声明四态 UI（含 template-unsupported）。后端缺态③显式项 → ISSUE-001。 |
| 旧模板拒绝 | §3.2 错误码 + UI 禁止伪装行级；301/304/§9 均提及。fixture/无写入/FORMAT 分界未机械化 → ISSUE-002。 |
| E2E 分层 | 304 独占 `tests/e2e/**`；场景 1–6 覆盖回归抽样、v0729 导入、旧模板、OpenAPI 编辑、详情只读、三角色矩阵——方向正确。命令/证据钉死与 partial OR → ISSUE-004/005。 |
| OpenAPI / 扩展描述 | 302 Vitest：端点增删、Swagger 回填、类型切换、payload 抽样；303 Vitest：类型只读+旧 endpoint+编辑入口可见性。导入「接口定义」坏文本行失败在 301 testScope 已列，可接受。 |
| RBAC | §4 矩阵 + 301 USER 403（写/报告）+ 304 E2E 场景 6；302「手工或单测」USER 不可达可接受为波次内弱项（由 304 关门），不单列 P1。 |
| Fixture 所有权 | 301 与 304 均可写 `tests/fixtures/import/**`，DAG 串行，无并行写冲突。既有 fixture 多为 V1.1 CSV；须在 ISSUE-002 关闭时明确哪些升为 legacy 负例、哪些重做为 v0729。 |
| 契约/错误码 | 301 含契约 lint + 新码列入 requestLevel；与 `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 可测方向一致。 |
| 环境 vs 质量 | 本评仅为计划可测性；**未**把环境阻塞误判为通过。 |

## REQ → 测试层级映射（审查用）

| REQ | 单测/契约 | 集成（301） | Vitest UI | E2E（304） | 缺口 |
|---|---|---|---|---|---|
| REQ-CAT-008 | 错误码 requestLevel | v0729 成功/部分成功；旧模板；oversize/format | 四态含 template-unsupported；报告白名单 | 场景 2–3、回归 | 态③；legacy fixture 机械断言；E2E partial OR |
| REQ-CAT-004 | — | DTO/读兼容（计划有 acceptance） | 303 只读抽样 | 场景 5 | 扩展字段 round-trip 未入 301 testScope |
| REQ-CAT-005 | — | 写持久化（acceptance） | 302 编辑/Swagger | 场景 4 | 同上 + NO_UPDATE |
| REQ-API-001 | OpenAPI 形状校验 | 坏接口定义→行失败；endpoints round-trip | 302/303 | 场景 4–5 | 模板 GET 未入 testScope |
| REQ-RBAC-001 | — | USER 写/报告 403 | 302/303/304 隐藏 | 场景 6 | 可接受 |

## 说明

- 输入：`planning/proposals/PLAN-WSC-4.0.md`；`product/requirements/SNAP-WSC-004.md`（APPROVED）。
- 对照只读：`planning/approved/PLAN-WSC-2.2.md`（四态/fixture/E2E 先例）；`tests/fixtures/import/**`；`tests/e2e/playwright.config.ts` / `specs/**`。
- **未**修改计划、源码或 Run `state.yaml` / `events.jsonl`。
- **未**执行 Vitest / JUnit / Playwright；**未**代替 developer/tester 宣称通过。
- 决策：`REQUEST_CHANGES`（P1 未关闭）；验收标准本身可测，故非 `BLOCK`。
