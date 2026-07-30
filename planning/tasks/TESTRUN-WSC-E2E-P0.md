# TESTRUN-WSC-E2E-P0

```yaml
taskId: W5-P0-E2E
actorInstance: tester-wsc-e2e-p0
result: PASSED
evidenceId: TESTRUN-WSC-E2E-P0
testedAt: 2026-07-30T10:42:42+08:00
planRef: PLAN-WSC-1.1 §5.4
exitCode: 0
```

## 前提

- TASK-WSC-001..006 均已 VERIFIED（独立 review + test）
- 前端 `http://127.0.0.1:5173`（`VITE_API_BASE_URL=http://127.0.0.1:8080/api/v1`）
- 后端 `http://127.0.0.1:8080` 健康（`/actuator/health` = UP）
- 主机统一为 `127.0.0.1`，避免 `localhost`/`127.0.0.1` Cookie 域不一致

## 命令

```text
cd tests/e2e
$env:E2E_RUN = "1"
$env:E2E_BASE_URL = "http://127.0.0.1:5173"
pnpm exec playwright test p0-wsc.spec.ts
```

- exitCode: **0**
- 结果：`6 passed (5.4s)`

## 报告

| 项 | 路径 |
|---|---|
| HTML | `tests/e2e/reports/p0-wsc/index.html` |
| 原始输出 | `temp/playwright-p0-wsc.out` |

## §5.4 场景覆盖

| # | 步骤 | 结果 |
|---|---|---|
| 1 | 登录失败反馈 + 登录成功并切换/确认角色 | PASSED |
| 2 | 打开总览并核对核心指标可见 | PASSED |
| 3 | 进入数据目录，组合筛选/搜索 | PASSED |
| 4 | 打开产品详情 | PASSED |
| 5 | 打开上链信息并查看快照 | PASSED |
| 6 | 新增产品并确认产生链上版本（含成功反馈） | PASSED |

## NFR 抽样

| 项 | 证据 |
|---|---|
| 界面中文 | 步骤 1：登录页「演示账号 / 用户名 / 密码」可见 |
| 失败反馈 | 步骤 1 错误密码 alert；步骤 6 非法编码 `editor-feedback` |
| 成功反馈 | 步骤 6 创建后跳转详情并可在链上页见 `v1` 快照 |

## 规格微调（仅 tests/e2e，非业务源码）

| 变更 | 原因 |
|---|---|
| `getByText(..., { exact: true })` / `toContainText` | 避免 strict mode 多节点命中 |
| `switchRole` 等待 disabled→enabled | 切换请求完成后再断言角色值 |

## 结论

**result: PASSED** — P0 缺陷 0；§5.4 六步全部成功。
