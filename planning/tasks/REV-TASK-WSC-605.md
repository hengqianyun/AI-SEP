# 代码审查

```yaml
reviewId: REV-TASK-WSC-605
taskId: TASK-WSC-605
planId: PLAN-WSC-5.2
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-605-r1
decision: APPROVE
p0: 0
p1: 0
closedFindings: []
openFindings:
  - FIND-WSC-605-R1-001
  - FIND-WSC-605-R1-002
  - FIND-WSC-605-R1-003
contracts: wsc-contracts@2.2.0（只读；denyModify；本任务未触碰）
riskTags: [e2e-gate, release-evidence]
reviewedAt: 2026-08-06T14:35:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - planning/approved/PLAN-WSC-5.2.md（TASK-WSC-605 writeSet/denyModify/acceptance/testScope；§6.1 七场景）
  - ai/runs/RUN-WSC-008/DEV-TASK-WSC-605.md
  - 实地：p0-wsc-v1.4.spec.ts、helpers/v14-selectors.ts、fixtures/auth.ts、playwright.config.ts、package.json、reports/p0-wsc-v1.4/{EVIDENCE.md,index.html,junit.xml}
mustDifferFrom: developer-wsc-605
spotCheck: |
  字面 scope：605 filesChanged ⊆ writeSet（specs/helpers/auth/package/config/reports）
  denyModify：本任务归因未改 frontend/backend/contracts/sql/product/planning/state/events
  junit.xml：15 tests / 0 failures；evidenceId TESTRUN-WSC-E2E-V14 见 EVIDENCE.md
  未代 tester PASS；未标 VERIFIED；未写 state/events
```

## Round 1 结论

**APPROVE** — **P0=0，P1=0**。字面 scope-check 通过：交付变更均落在 TASK-WSC-605 `writeSet`（含声明的 helper/fixture）；`denyModify`（frontend features/layouts/router/styles/theme/components/api、backend、contracts、sql、product、planning、state/events）未见本任务触碰，亦无生产 DOM/feature 热修。§6.1 场景 1–7 在 `p0-wsc-v1.4.spec.ts` 均有非空强制用例（含三角色登录/只读角色、用户管理 CRUD 抽样与负例、公共目录无写、我的产品增与回流、ADMIN 写 403、座序图 Top5+总数+hover、导入四态与 OpenAPI 不回退）。`evidenceId: TESTRUN-WSC-E2E-V14`、报告落点 `tests/e2e/reports/p0-wsc-v1.4/`、命令 `test:p0-v14` / 等价 playwright 声明齐全；本地存在 HTML/JUnit/EVIDENCE（目录被 `.gitignore` 忽略属既有约定）。开放 findings 为 P2/P3，不阻塞进入独立 tester。

## Findings

| id | severity | status | evidence / note | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-605-R1-001 | P2 | **OPEN** | 场景 4 用例名写「增改导」，本例实质覆盖「增」+ 导入按钮可见 + 列表本人 + 无座序图 + 返回；「改」依赖 `7b`、「导」依赖 `7a`。同一证据包内行为有覆盖，但场景 4 字面要点未在单测内闭环 | 可选：在 `4)` 增补一次编辑保存或一次导入成功路径；或改用例名/DEV 勾选表明示跨用例举证 | REQ-CAT-010；§6.1 #4 |
| FIND-WSC-605-R1-002 | P2 | **OPEN** | `playwright.config.ts` 设 `testMatch: **/p0-wsc-v1.4.spec.ts`，与本轮独占证据包一致，但同 config 的 `test:p0-v13` 将无法匹配旧规格 | 可选：v14 专用 config，或 script 显式 `--grep`/`--config` 分流；不阻塞本任务 acceptance | testScope；package.json |
| FIND-WSC-605-R1-003 | P3 | **OPEN** | `fixtures/auth.ts` 仍保留 `switchRole`（依赖已下线的 `.role-switch select`）；V1.4 规格未调用。属遗留 API，易误导后续作者 | 可选：标注 `@deprecated` 或迁出/删除并确认旧规格不再依赖共享导出 | REQ-SHELL-001 |

## 检查清单（Round 1）

| 项 | 结果 |
|---|---|
| 字面 writeSet scope-check | **PASS** — `specs/p0-wsc-v1.4.spec.ts`、`helpers/v14-selectors.ts`、`fixtures/auth.ts`、`package.json`、`playwright.config.ts`、`reports/p0-wsc-v1.4/**` |
| denyModify 未触碰（frontend feature/layout/router/…、backend、contracts、sql、product、planning、state·events） | **PASS**（605 归因交付仅 `tests/e2e/**`；未改生产 DOM） |
| §6.1 场景 1 真登录三角色 / 只读角色 / 无切角色 | **PASS** — `1)×3` + `expectReadonlyRole`（无 select/dropdown/listbox） |
| §6.1 场景 2 ADMIN 用户管理 CRUD；PROVIDER/USER 不可达 | **PASS** — `2a` 创建/改角色/软删；`2b`×2 禁达 |
| §6.1 场景 3 公共目录三角色无增改导 | **PASS** — `3)×3` + `expectPublicCatalogNoWrite` |
| §6.1 场景 4 我的产品增改导 / 列表本人 / 无座序图 / 返回 | **PASS（有 P2 备注）** — 增/列表/无图/返回扎实；改/导跨 `7b`/`7a` |
| §6.1 场景 5 ADMIN 产品写 403；分类/维护可用 | **PASS** — POST 403 + 编辑器重定向 + category-admin / maintenance |
| §6.1 场景 6 座序图 Top5+总数+hover；我的产品无图 | **PASS** — chip≤5、total、hover opacity、mine 无 slot |
| §6.1 场景 7 V1.3 导入四态 + OpenAPI 不回退 | **PASS** — `7a` 四态；`7b` 保存后再开保留 |
| evidenceId `TESTRUN-WSC-E2E-V14` | **PASS** — 规格头注释 + `EVIDENCE.md` |
| 报告落点 `tests/e2e/reports/p0-wsc-v1.4/` | **PASS** — config html/junit；磁盘有 `index.html` / `junit.xml` / `EVIDENCE.md`（gitignore 忽略） |
| 命令写入报告 | **PASS** — `pnpm run test:p0-v14` 及等价 playwright 命令 |
| auth fixture 变更合理且仍属 `tests/e2e` | **PASS** — `logoutIfNeeded` + `loginAs` 会话守卫适配；writeSet 允许必需 fixture |
| 开发自测 ≠ 独立 tester；未标 VERIFIED | **记录** — junit 15/0 仅作开发冒烟对照 |
| `mustDifferFrom` developer-wsc-605 | **PASS**（`code-reviewer-wsc-605-r1`） |
| P0 / P1 | **无** |

## 决策权声明

- 审查者未修改被审业务代码；未写 `state.yaml` / `events.jsonl`；未标 VERIFIED。
- 未兼任 developer / tester。
- **decision: APPROVE**（P0=0、P1=0；开放 finding 为 P2×2、P3×1）。

## 计数（Round 1）

| 级别 | 数量（开放） | 说明 |
|---|---|---|
| P0 | 0 | — |
| P1 | 0 | — |
| P2 | 2 | FIND-001 场景4 改/导跨用例；FIND-002 testMatch 锁 v1.4 |
| P3 | 1 | FIND-003 遗留 switchRole |
