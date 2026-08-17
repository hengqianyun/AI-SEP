# 测试证据 — TASK-WSC-908

```yaml
testrunId: TESTRUN-TASK-WSC-908
evidenceId: TESTRUN-WSC-E2E-V16
taskId: TASK-WSC-908
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
snapshotId: SNAP-WSC-008
wave: E
actorInstance: tester-wsc-908
mustDifferFrom:
  - developer-wsc-908
  - code-reviewer-wsc-908
basedOn:
  - ai/agents/tester.md
  - ai/skills/tester/SKILL.md
  - planning/approved/PLAN-WSC-8.3.md（§5 TASK-WSC-908 testScope；§6.1 九场景）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-908.md（仅参考命令，本轮重跑）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-908.md（APPROVE；P0=0、P1=0；P2 FIND-001/002 OPEN）
reviewDecision: APPROVE
reviewP0: 0
reviewP1: 0
executedAt: 2026-08-17T15:35:59+08:00
decision: PASS
exitCode: 0
failedCommandCount: 0
requirements: [REQ-CAT-014, REQ-CAT-015, REQ-CAT-016, REQ-CAT-017, REQ-CAT-018, REQ-CAT-019, REQ-SHELL-009, REQ-RBAC-002, REQ-USER-002]
riskTags: [e2e-gate, release-evidence]
specFile: tests/e2e/specs/p0-wsc-v1.6.spec.ts
reportDir: tests/e2e/reports/p0-wsc-v1.6/
commands:
  - id: C1
    command: pnpm --dir tests/e2e run test:p0-v16
    cwd: C:\WorkSpace\AI-SEP
    env: { E2E_RUN: "1", E2E_BASE_URL: "http://127.0.0.1:5173" }
    exitCode: 0
    result: PASS
    note: "10 passed (28.0s)；junit tests=10 failures=0；§6.1 九场景（6 拆 6a/6b）全绿"
  - id: C2
    command: pnpm --dir tests/e2e run test:p0-v14
    cwd: C:\WorkSpace\AI-SEP
    env: { E2E_RUN: "1", E2E_BASE_URL: "http://127.0.0.1:5173" }
    exitCode: 1
    result: PASS_WITH_KNOWN_DELTAS
    note: "可执行并跑完；6 passed / 9 failed (2.9m)；9 失败均为 intentional V1.6 delta / 预存 UX，不计入 failedCommandCount"
  - id: C3
    command: pnpm --dir tests/e2e run test:p0-v15
    result: N/A
    note: "工作树无 p0-wsc-v1.5.spec.ts / 无 test:p0-v15 script；未伪造"
note: |
  独立 tester-wsc-908 实跑 C1/C2。未改业务源码、审查正文、state.yaml、events.jsonl；未 git commit；未自行标 VERIFIED。
  无 auth-model-change：本 YAML 不写 securityReviewDecision。
  未把 DEV 自测 10/10 或 gitignore 旧 EVIDENCE 当本轮 PASS。未因 P2 FIND-001/002 记 FAIL。
```

## 结论摘要

**PASS** — 独立 `tester-wsc-908` 在 CR **APPROVE（P0=0、P1=0）** 后真实执行 PLAN-WSC-8.3 §5 TASK-WSC-908 `testScope`：

| # | command | exitCode | result |
|---|---|---|---|
| C1 | `pnpm --dir tests/e2e run test:p0-v16` | **0** | **PASS** — **10/10**；`evidenceId: TESTRUN-WSC-E2E-V16` |
| C2 | `pnpm --dir tests/e2e run test:p0-v14` | **1** | **可执行** + `PASS_WITH_KNOWN_DELTAS`（9 条 intentional；不记 908 FAIL） |
| C3 | `test:p0-v15` | — | **N/A**（无 spec / 无 script） |

**failedCommandCount = 0**（只计 C1）。本报告 **不标 VERIFIED**。

## 环境

| 项 | 本轮实测 |
|---|---|
| frontend | `http://127.0.0.1:5173` HTTP **200**（既有 Vite `pnpm exec vite --port 5173 --host 127.0.0.1`，本轮未重启） |
| backend | `http://127.0.0.1:8080/actuator/health` → `{"status":"UP"}` HTTP **200** |
| jar | `backend/app/data-chain-service/target/data-chain-service-0.0.1-SNAPSHOT.jar` mtime **2026-08-17 14:58:02**；进程 `--spring.profiles.active=dev`，启动日志 Flyway **v7** |
| 本轮是否重启 jar | **否**。独立探测：ADMIN `POST /api/v1/auth/session` **200**（role=ADMIN）；产品 POST **非 403**（本轮探测码格式非法 → 400 `ERR_PRODUCT_CODE_FORMAT`；C2 冻结用例 5 收到 **200**）。**不像**旧包 ADMIN 写 403 |
| `E2E_BASE_URL` | `http://127.0.0.1:5173` |
| `E2E_RUN` | `1`（C1/C2 均设；v16 spec 无 skip，v14 spec 无此变量会 skip） |

未把环境阻塞记为 PASS。前端/后端已可用，故未 BLOCKED。

## 执行命令与结果

| # | 层级 | 命令 | 工作目录 | exitCode | passed / failed | 证据 |
|---|---|---|---|---:|---|---|
| C1 | E2E 发布门禁 | `pnpm --dir tests/e2e run test:p0-v16` | 仓库根 | **0** | **10 / 0**（28.0s） | `e2e-908-v16.txt`；`tests/e2e/reports/p0-wsc-v1.6/junit.xml`（tests=10 failures=0） |
| C2 | E2E 强制回归 | `pnpm --dir tests/e2e run test:p0-v14` | 仓库根 | **1** | **6 / 9**（2.9m） | `e2e-908-v14.txt`；`tests/e2e/reports/p0-wsc-v1.4/junit.xml`（tests=15 failures=9） |
| C3 | 建议回归 | `test:p0-v15` | — | — | **N/A** | `tests/e2e/package.json` 无该 script；glob 无 v1.5 spec |

## §6.1 九场景勾选（C1 本轮实测）

| # | 场景 | 用例 | 结果 |
|---|---|---|---|
| 1 | 浏览筛选 V1.6：无卡片；高级区常显；无 industryCategory；Cascader `l1CategoryId`/`l2CategoryId`；抽样 `/my-products` | `§6.1-1` | **PASS** |
| 2 | 座序图 L1：标题右下拉；默认全部=Top5；选 L1 过滤；无企业筛；两用户 cross-enterprise 一致 | `§6.1-2` | **PASS** |
| 3 | 全链无企业过滤；supplierName/未分类不回退；query 无 `enterpriseName` | `§6.1-3` | **PASS** |
| 4 | 我的数据产品本企业：ADMIN+PROVIDER 可见；ADMIN 可增改导；同企异 create_by 可见 | `§6.1-4` | **PASS** |
| 5 | 导航 ADMIN 双入口：目录维护全量 + `/my-catalog` 本企业；深链 N vs M；用户管理不删 | `§6.1-5` | **PASS** |
| 6 | PROVIDER 仅我的目录 + USER 隔离（菜单隐藏 + 四深链不可达） | `§6.1-6a` + `§6.1-6b` | **PASS** |
| 7 | 我的目录维护 UX：Pagination/Cascader/本页全选/统一维护；本企业 vs 全量分离 | `§6.1-7` | **PASS** |
| 8 | 行业类别非浏览：编辑/导入仍含行业类别；浏览 query 无 industryCategory | `§6.1-8` | **PASS** |
| 9 | V1.5/V1.4 回归抽样：supplierName；未分类垫底；座序图 hover；`test:p0-v14` 可跑 | `§6.1-9` + C2 可执行 | **PASS** |

C1 JUnit 十例均无 failure 节点。场景 6 拆 6a/6b，合计 10 tests。

## C2 失败矩阵（独立分类；未抄 DEV 表）

来源：本轮 `tests/e2e/reports/p0-wsc-v1.4/junit.xml`（timestamp `2026-08-17T07:37:04.742Z`）。**禁止改** `p0-wsc-v1.4.spec.ts`。

| 冻结用例 | 冻结断言 | 本轮观察 | 分类 | 对照 |
|---|---|---|---|---|
| `2a) ADMIN 用户管理 CRUD 抽样` | soft-delete 后表不含 `e2e_u_*` | L150 `not.toContainText` 失败；`e2e_u_228088` 仍「已启用」 | **intentional / 预存 UX** | 点击 `user-soft-delete` 未确认弹框；非 908 writeSet；非环境 blocker |
| `2b) provider 不可达用户管理` | 深链仍挂 `users-admin-page` + `users-forbidden` | `users-admin-page` 未找到 | **intentional V1.6 delta** | REQ-SHELL-009 深链结构不可达；v16 `§6.1-6b` |
| `2b) user 不可达用户管理` | 同上 | 同上 | **intentional V1.6 delta** | 同上 |
| `3) 公共目录无增改导` ×3（admin/provider/user） | heading `数据目录` visible | heading 未找到 | **intentional V1.6 delta** | REQ-CAT-014 无卡片；v16 `§6.1-1` |
| `4) PROVIDER 我的产品…` | `nav-catalog-maintenance` visible | 元素不存在 | **intentional V1.6 delta** | REQ-SHELL-009 PROVIDER 无维护菜单；v16 `§6.1-6a` |
| `4x) ADMIN/USER 无「我的产品」` | ADMIN `nav-my-products` count=0 | Received: **1** | **intentional V1.6 delta** | REQ-CAT-016 ADMIN 可见我的数据产品；v16 `§6.1-4` |
| `5) ADMIN 产品写 403` | POST status 403 | Received: **200** | **intentional V1.6 delta** | REQ-CAT-016 / §3.1 ADMIN 可写；v16 `§6.1-4` |
| `6) 座序图 hover` | chip hover opacity | **无 failure 节点** | **PASS** | 场景 9 抽样；v16 `§6.1-9` 同源 hover **PASS** |
| `7a` / `7b` | 导入四态 + OpenAPI | **无 failure 节点** | **PASS** | 非 V1.6 冲掉 |

通过用例（6）：真登录三角色 ×3、座序图 hover、导入 7a、OpenAPI 7b。

**无**非 delta blocker（场景 9 hover/supplierName 在 v16 本轮 PASS；C2 命令可启动）。故 **不**记 908 FAIL。

## REQ 追踪

| REQ | 验证要点 | 本轮 |
|---|---|---|
| REQ-CAT-014 | 无卡片；Cascader l1/l2；高级区常显 | **PASS**（C1 `§6.1-1`） |
| REQ-CAT-015 | 座序图 L1；默认全部=Top5；无企业筛 | **PASS**（C1 `§6.1-2`；hover `§6.1-9`） |
| REQ-CAT-016 | mine 本企业；ADMIN 可写可导入 | **PASS**（C1 `§6.1-4`） |
| REQ-CAT-017 | `/my-catalog`；维护 UX；scope 分离 | **PASS**（C1 `§6.1-5` `§6.1-7`） |
| REQ-CAT-018 | 全链无企业过滤；cross-enterprise 一致 | **PASS**（C1 `§6.1-2` `§6.1-3`） |
| REQ-CAT-019 | 浏览无 industryCategory；编辑/导入仍有 | **PASS**（C1 `§6.1-1` `§6.1-8`） |
| REQ-SHELL-009 | ADMIN 双入口；PROVIDER 仅我的目录；USER 深链 | **PASS**（C1 `§6.1-5` `§6.1-6a` `§6.1-6b`） |
| REQ-RBAC-002 | 菜单与深链/API scope 一致 | **PASS**（C1 `§6.1-4`–`6`） |
| REQ-USER-002 | 跨企用户经 UI 绑定；会话企业只读 | **PASS**（C1 `§6.1-2` `§6.1-3` `§6.1-5`） |

## 开放 P2（不因此 FAIL）

| id | 本轮 |
|---|---|
| FIND-WSC-908-001 | gitignore 排除报告目录。本轮 **已独立重跑** C1 并落正式 TESTRUN；**未**采信 DEV 10/10 |
| FIND-WSC-908-002 | L2 返回值未强制、未分类条件断言、跨企编码未定位。本轮 C1 `§6.1-1/3/5/9` **均为 PASS**，未构成 P0 场景失败 |

未因开放 P2 记 FAIL，未开 `BUG-*`。

## 缺陷

无。C1 未失败，未在 `ai/runs/RUN-WSC-011/` 写 `BUG-*`。

## 证据红线（自检）

- [x] 实际执行 testScope（C1/C2 命令 + 真实 exitCode），非仅读 DEV/REV
- [x] 环境阻塞 / 未执行 **未**记为 PASS
- [x] 未把 DEV junit 10/10 或 gitignore 旧 EVIDENCE 当本轮 PASS
- [x] 未因 P2 FIND-001/002 记 FAIL
- [x] C2 仅 intentional delta → 不记 908 FAIL；`failedCommandCount` 只计 C1
- [x] 未改 `frontend/src/**`、`backend/**`、`contracts/**`、`tests/e2e/specs/**`、`playwright.config.ts`、`playwright.v16.config.ts`、`package.json`、helpers
- [x] 未改审查正文 / `state.yaml` / `events.jsonl`
- [x] 未 git commit
- [x] `actorInstance=tester-wsc-908`，与 developer-wsc-908 / code-reviewer-wsc-908 隔离
- [x] **未自行标 VERIFIED**
- [x] 无 `securityReviewDecision`（本任务无 auth-model-change）
- [x] C3 `test:p0-v15` 如实 N/A，未伪造

## 证据路径

- 本报告：`ai/runs/RUN-WSC-011/tests/TESTRUN-TASK-WSC-908.md`
- V1.6 报告目录：`tests/e2e/reports/p0-wsc-v1.6/`（`junit.xml` / `index.html` / `EVIDENCE.md`）
- C1 摘录：`ai/runs/RUN-WSC-011/tests/e2e-908-v16.txt`
- C2 摘录：`ai/runs/RUN-WSC-011/tests/e2e-908-v14.txt`
- V1.4 JUnit：`tests/e2e/reports/p0-wsc-v1.4/junit.xml`
- 任务包 / 开发 / 审查：`planning/approved/PLAN-WSC-8.3.md` §5 TASK-WSC-908 / §6.1；`DEV-TASK-WSC-908.md`；`reviews/REV-TASK-WSC-908.md`
