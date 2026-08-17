# DEV-TASK-WSC-908

```yaml
taskId: TASK-WSC-908
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
actorInstance: developer-wsc-908
status: READY_FOR_REVIEW
completedAt: 2026-08-17T15:25:00+08:00
reqs:
  - REQ-CAT-014
  - REQ-CAT-015
  - REQ-CAT-016
  - REQ-CAT-017
  - REQ-CAT-018
  - REQ-CAT-019
  - REQ-SHELL-009
  - REQ-RBAC-002
  - REQ-USER-002
blocksRelease: true
riskTags: [e2e-gate, release-evidence]
wave: E
```

> 交付说明落于 `ai/runs/RUN-WSC-011/`。未改 `state.yaml` / `events.jsonl`。**未标 VERIFIED**。
> 本实例不代写 REV，不 git commit。无 `auth-model-change` / `schema-migration`：未写 SEC/MIG 审查。

## objective

Wave E：独占 Playwright E2E，覆盖 PLAN-WSC-8.3 §6.1 九个强制场景；产出可审计证据包 `TESTRUN-WSC-E2E-V16`；**保留** V1.4 回归可执行；**不改**任何业务 feature 源码。

## 预落地对账

| 项 | 预落地 | 对账动作 |
|---|---|---|
| V1.4 config / spec | `playwright.config.ts` + `p0-wsc-v1.4.spec.ts` 冻结 | **未改**（denyModify）；`test:p0-v14` 保留且可启动 |
| V1.5 spec/config | 工作树**没有** `playwright.v15.config.ts` / `p0-wsc-v1.5.spec.ts` | **未新建** v15；**未伪造** `test:p0-v15`（避免「仍可执行」变红） |
| V1.6 命令 | 独立 config + `test:p0-v16` | **已做**：script 一字不差 `playwright test specs/p0-wsc-v1.6.spec.ts --config=playwright.v16.config.ts` |
| 路由 | `/my-catalog` 权威；`/my-maintenance` 仅 redirect | E2E 深链字面 `/my-catalog`；redirect 可断言 |
| ADMIN 双入口 | 目录维护全量 + 我的目录本企业 | 场景 5：N vs M + meta/title + 用户管理不删 |
| PROVIDER | 仅我的目录、无目录维护 | 场景 6a |
| USER | 菜单隐藏 + 四深链不可达 | 场景 6b |
| 种子 | `admin`/`admin2`/`provider` 同 DEMO | 跨企用户经用户管理 UI 绑定「未归属默认企业」（未改 backend seed） |

建议回归 `test:p0-v15`：**N/A**。强制回归仍是 `test:p0-v14`。

## filesChanged

| 路径 | 说明 | REQ |
|---|---|---|
| `tests/e2e/specs/p0-wsc-v1.6.spec.ts` | 新建；§6.1 九场景强制用例（6 拆 6a/6b）；头注释 `evidenceId: TESTRUN-WSC-E2E-V16` | §6.1 全表 |
| `tests/e2e/playwright.v16.config.ts` | 新建；`testMatch` 仅 v1.6 spec；HTML+JUnit → `./reports/p0-wsc-v1.6/`；`open: 'never'` | 证据包 |
| `tests/e2e/package.json` | **增补** `test:p0-v16`；保留 `test` / `test:p0` / `test:p0-v13` / `test:p0-v14` | 共存 |
| `tests/e2e/helpers/wsc-v16-fixtures.ts` | 登录/跨企用户/Cascader/座序图/query 审计 | 场景 2–6 |
| `tests/e2e/reports/p0-wsc-v1.6/index.html` | Playwright HTML | 证据包 |
| `tests/e2e/reports/p0-wsc-v1.6/junit.xml` | JUnit tests=10 failures=0 | 证据包 |
| `tests/e2e/reports/p0-wsc-v1.6/EVIDENCE.md` | evidenceId / 场景勾选 / 命令 / exitCode | 证据包 |
| `tests/e2e/reports/p0-wsc-v1.6/run-console.log` | 自测控制台摘录 | 证据包 |
| `ai/runs/RUN-WSC-011/DEV-TASK-WSC-908.md` | 本交付说明 | — |

## REQ 映射

| REQ | 实现要点 | 证据 |
|---|---|---|
| REQ-CAT-014 | 浏览 Cascader l1/l2；无卡片；高级区常显 | `§6.1-1` |
| REQ-CAT-015 | 座序图 L1；默认全部=Top5；无企业筛 | `§6.1-2` |
| REQ-CAT-016 | mine 本企业；ADMIN 可写可导入 | `§6.1-4` |
| REQ-CAT-017 | `/my-catalog`；维护 UX；scope 分离 | `§6.1-5` `§6.1-7` |
| REQ-CAT-018 | 全链无企业过滤；cross-enterprise 座序图/列表一致 | `§6.1-2` `§6.1-3` |
| REQ-CAT-019 | 浏览无 industryCategory；编辑/导入仍有行业类别 | `§6.1-1` `§6.1-8` |
| REQ-SHELL-009 | ADMIN 双入口；PROVIDER 仅我的目录；USER 深链 | `§6.1-5` `§6.1-6a` `§6.1-6b` |
| REQ-RBAC-002 | 菜单与深链/API scope 一致 | `§6.1-4`–`6` |
| REQ-USER-002 | 跨企用户经 UI 绑定已有企业；会话企业只读 | `§6.1-2` `§6.1-3` `§6.1-5` |

## 自测命令与结果

```bash
# cwd: 仓库根；前端 5173 + 后端 8080 已起
# 自测前发现运行中的 jar 为旧包（ADMIN 产品写仍 403），已用当前源码 mvn -DskipTests package 后重启
# java -jar backend/app/data-chain-service/target/data-chain-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# 未改业务源码。

pnpm --dir tests/e2e run test:p0-v16
# exitCode: 0
# 10 passed (34.5s)
# junit.xml tests=10 failures=0 skipped=0 errors=0
```

```bash
E2E_RUN=1 pnpm --dir tests/e2e run test:p0-v14
# exitCode: 1
# 6 passed / 9 failed (2.9m)
# 命令可启动（「仍可执行」）；未改 v14 spec/config
```

建议回归 `pnpm --dir tests/e2e run test:p0-v15`：**N/A**（无 spec / 无 script）。

## V1.4 共存失败矩阵

> 禁止改 `p0-wsc-v1.4.spec.ts`。场景 9 抽样项 supplierName / 未分类 / hover：v16 `§6.1-9` PASS；v14 `6)` 座序图 hover **PASS**。

| test id | assertion (frozen v1.4) | observed (2026-08-17 developer 自测) | classification | related REQ / note |
|---|---|---|---|---|
| `2a) ADMIN 用户管理 CRUD 抽样` | soft-delete 后表不含 `e2e_u_*` | 行仍在（启用态）；现网删除走确认弹框，v14 未点确认 | **intentional V1.6 delta**（606/用户管理弹框化；非 908 引入） | REQ-USER-002 展示面；v14 未跟弹框 |
| `2b) provider 不可达用户管理` | 深链仍渲染 `users-admin-page` + `users-forbidden` | `beforeEnter` 重定向 `/catalog`，页不挂载 | **intentional V1.6 delta** | REQ-SHELL-009 深链结构不可达 |
| `2b) user 不可达用户管理` | 同上 | 同上，重定向 `/catalog` | **intentional V1.6 delta** | REQ-SHELL-009 |
| `3) 公共目录无增改导 — admin` | heading `数据目录` visible | 公共目录不渲染标题卡 | **intentional V1.6 delta** | REQ-CAT-014 无卡片 |
| `3) 公共目录无增改导 — provider` | 同上 | 同上 | **intentional V1.6 delta** | REQ-CAT-014 |
| `3) 公共目录无增改导 — user` | 同上 | 同上 | **intentional V1.6 delta** | REQ-CAT-014 |
| `4) PROVIDER 我的产品…` | `nav-catalog-maintenance` visible | PROVIDER 无目录维护菜单 | **intentional V1.6 delta** | REQ-SHELL-009 |
| `4x) ADMIN/USER 无「我的产品」` | ADMIN `nav-my-products` count=0 | ADMIN 可见我的数据产品 | **intentional V1.6 delta** | REQ-CAT-016 |
| `5) ADMIN 产品写 403` | POST 产品 403 | POST **200**（ADMIN 本企业可写） | **intentional V1.6 delta** | REQ-CAT-016 / REQ-RBAC-002 |
| `6) 座序图 hover` | chip hover opacity | **PASS** | 场景 9 抽样仍绿 | REQ-CAT-015 |
| `7a/7b` 导入/OpenAPI | 四态 + swagger | **PASS** | 非 V1.6 冲掉 | — |

无环境/回归 blocker（相对「命令可启动」）。9 条失败均可归入 intentional V1.6 delta。

## denyModify 自检

未改：`tests/e2e/playwright.config.ts`、`tests/e2e/specs/p0-wsc-v1.4.spec.ts`、`tests/e2e/fixtures/auth.ts`、`tests/e2e/helpers/v14-selectors.ts`（只读 import）、`frontend/src/**`、`backend/**`、`contracts/**`、`**/sql/**`、`product/**`、`planning/**`、`ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`。未新建 v15 spec/config。未 git commit。未标 VERIFIED。

选择器均可用，未 BLOCKED 交 Orchestrator 热修 frontend/backend。

## acceptance 自检

| 项 | 结果 |
|---|---|
| §6.1 九场景均有非空强制用例 | PASS（1–5、6a/6b、7–9） |
| `pnpm --dir tests/e2e run test:p0-v16` | PASS exit 0；10/10 |
| 报告 `tests/e2e/reports/p0-wsc-v1.6/` HTML+JUnit+EVIDENCE.md | PASS；`evidenceId: TESTRUN-WSC-E2E-V16` 出现在 spec 头注释与 EVIDENCE.md |
| `test:p0-v14` 仍可执行 | PASS（exit 1 + 失败矩阵） |
| `test:p0-v15` | N/A（无 spec，未伪造 script） |
| 未改业务 feature 源码 | PASS |
| 路由字面 `/my-catalog` | PASS |
| 无企业管理页 / 座序图无企业筛 / 浏览无 industryCategory / 全链无企业过滤 | PASS |

本实例 **不** 标 VERIFIED。须独立 codeReviewer；正式门禁须独立 tester 再跑 `test:p0-v16`。
