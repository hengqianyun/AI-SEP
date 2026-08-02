# TESTRUN-WSC-E2E-V11

```yaml
taskId: W5-E2E-V11
actorInstance: tester-wsc-e2e-v11-r2
result: PASSED
evidenceId: TESTRUN-WSC-E2E-V11
round: r2
supersedes: tester-wsc-e2e-v11 FAILED (2026-07-31T23:16:06+08:00)
testedAt: 2026-07-31T23:30:50+08:00
planRef: PLAN-WSC-2.2 §6.1
snapshotId: SNAP-WSC-002
planId: PLAN-WSC-2.2
exitCode: 0
fixId: FIX1
bugId: BUG-WSC-E2E-001
mustDifferFrom:
  - developer-wsc-e2e-fix1
  - code-reviewer-wsc-e2e-fix1
  - tester-wsc-e2e-v11
```

## 前提

- 独立 tester 实例 `tester-wsc-e2e-v11-r2`（≠ developer-wsc-e2e-fix1 / code-reviewer-wsc-e2e-fix1 / tester-wsc-e2e-v11）
- 前端 `http://127.0.0.1:5173`（200）
- 后端 `http://127.0.0.1:8080`（`/actuator/health` = 200；复跑前重启以恢复 maintenance PENDING 种子）
- FIX1 已 APPROVE：`planning/tasks/REV-TASK-WSC-107-FIX1.md`
- 主机统一为 `127.0.0.1`

## 命令（权威终跑）

```text
cd tests/e2e
$env:E2E_RUN = "1"
$env:E2E_BASE_URL = "http://127.0.0.1:5173"
pnpm exec playwright test p0-wsc-v1.1.spec.ts --config=playwright.config.ts --workers=1
```

- exitCode: **0**
- 结果：`11 passed`（16.7s）

## 报告

| 项 | 路径 |
|---|---|
| HTML | `tests/e2e/reports/p0-wsc-v1.1/index.html` |
| 原始输出（终跑） | `ai/runs/RUN-WSC-002/tester-wsc-e2e-v11-r2/02-playwright-v11-r2.txt` |
| exitCode | `ai/runs/RUN-WSC-002/tester-wsc-e2e-v11-r2/02-playwright-exit.txt` |
| 初跑（FAILED，路径/种子） | `ai/runs/RUN-WSC-002/tester-wsc-e2e-v11-r2/01-playwright-v11-r2.txt` |

## §6.1 场景覆盖

| # | 场景 | 对齐 | 结果 | 备注 |
|---|---|---|---|---|
| 1 | V1.0 P0 主路径回归 | 登录/角色；OVW；目录筛选；详情；上链；新增产生版本 | **PASSED** | 1a–1f 全部 ok |
| 2 | 三级分类维护 | CAT-006 | **PASSED** | 选择 L1/L2 后新增 L3，列表可见新子类 |
| 3 | 目录维护：待关联→已维护 | CAT-007 | **PASSED** | pending 单条关联三级后 maintained 可见（终跑前重启后端恢复种子） |
| 4 | 批量导入 partial-success | CAT-008 | **PASSED** | `#import-modal` 可见；成功 1 / 失败 1；`.report-meta` 含行数（FIX1 关闭 BUG-WSC-E2E-001） |
| 5 | 标签切换 + 滚动加载抽样 | CAT-001/002 | **PASSED** | 空间/行业标签 + 筛选保留 + 列表滚动 |
| 6 | 上链快照三级路径 | CHAIN-001 | **PASSED** | `categoryPath` / `categoryPathParts` L1/L2/L3 可见 |

## 场景 4（FIX1 复验要点）

| 项 | 内容 |
|---|---|
| 入口 | 壳层「批量导入」→ `/catalog?import=1` |
| 弹窗 | `#import-modal` 可见；标题「批量导入产品」 |
| 计数 | `import-counts`：成功 1 / 失败 1；`data-result-state=partial_success` |
| 报告 | `import-report-link` 可见；`.report-meta` 含「行」（prefetch 可观测） |
| 对照 | 先前 r1 FAILED：browse 未挂载 ImportDialog；本轮 FIX1 后通过 |

## 规格变更（仅 `tests/e2e`，本轮最小修正）

| 变更 | 原因 |
|---|---|
| `specs/p0-wsc-v1.1.spec.ts` `FIXTURE_PARTIAL`：`../../../fixtures/...` → `../../fixtures/import/partial-success.csv` | 先前路径指向仓库根 `fixtures/`（ENOENT）；正确落点为 `tests/fixtures/import/`。r1 因弹窗未挂载未触达上传，路径缺陷潜伏 |

## 发布门禁对照（§8 / §6.1）

| 项 | 状态 |
|---|---|
| §6.1 E2E 全部成功 | **满足**（6 场景 / 11 tests PASSED） |
| P0 缺陷为 0 | **满足**（BUG-WSC-E2E-001 → CLOSED） |
| 本证据可否支撑发布门禁 E2E 项 | **是**（仍须 maintainer 人类发布批准等其它门禁） |

## 历史失败摘要（superseded）

| 轮次 | actor | result | 要点 |
|---|---|---|---|
| r1 | tester-wsc-e2e-v11 | **FAILED** | 场景 4：`#import-modal` 不可见（browse 未挂载）；exitCode 1；`10 passed / 1 failed` |
| r2 初跑 | tester-wsc-e2e-v11-r2 | 中间失败 | 场景 3：PENDING 种子耗尽「暂无条目」；场景 4：fixture 路径 ENOENT（弹窗已可见） |
| r2 终跑 | tester-wsc-e2e-v11-r2 | **PASSED** | 重启后端 + 修正 fixture 路径后 `11 passed`；exitCode 0 |

## 结论

**result: PASSED** — §6.1 六场景全部成功；P0=0。FIX1 关闭 BUG-WSC-E2E-001（场景 4 `#import-modal` + partial-success 计数 + 错误报告可观测）。独立 tester 复跑完成。
