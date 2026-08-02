# DEV-TASK-WSC-206

```yaml
taskId: TASK-WSC-206
actorInstance: developer-wsc-206
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: READY_FOR_REVIEW
completedAt: 2026-08-02T16:00:00+08:00
reqs:
  - REQ-UX-011
  - REQ-UX-009
  - REQ-UX-010
evidenceIdTarget: TESTRUN-WSC-E2E-V12-UX
hotfixRequired: false
```

## 摘要

合并 201–205 分波 UX 差距草稿与草稿截图为正式差距清单 / `screenshots/` / `WALKTHROUGH.md`；扩展 `p0-wsc-v1.1.spec.ts` 与 `fixtures/auth.ts` 以适配 UX DOM（角色下拉、双「提交」、分类按钮文案、空态 `empty`、运行时 partial fixture）；报告目录调至 `reports/p0-wsc-v1.2-ux/`。开发试跑 **14/14 PASSED**（非 VERIFIED）。**未**改任何 `frontend/src/features/**`；**无需 HOTFIX**。

## 变更文件列表

| 路径 | REQ | 说明 |
|---|---|---|
| `ai/runs/RUN-WSC-003/ux-gap-checklist.md` | REQ-UX-011/009/010 | 正式差距清单 §3.1 schema；01–10 覆盖；P0 open=0；`GAP-UX-006-01` 载体 B 为 P1 open 可勾选 |
| `ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/01-login.png` … `10-chain.png` | REQ-UX-011 | 自 `drafts/` 复制整理；fixture/脱敏 |
| `ai/runs/RUN-WSC-003/ux-walkthrough/WALKTHROUGH.md` | REQ-UX-011/009/010 | 脱敏声明；01–10；1280×800 / 1440×900 勾选；PO/UX 确认栏（待人类签署） |
| `tests/e2e/specs/p0-wsc-v1.1.spec.ts` | REQ-UX-011 | §3.2 场景扩展；三角色写入口矩阵 6b/6c；UX 选择器适配 |
| `tests/e2e/fixtures/auth.ts` | REQ-UX-011 | `RoleSwitcher` 下拉适配（原 `.role-switch select`） |
| `tests/e2e/playwright.config.ts` | REQ-UX-011 | 报告 → `reports/p0-wsc-v1.2-ux/`；outputDir `test-results-v1.2-ux` |
| `tests/e2e/reports/p0-wsc-v1.2-ux/**` | REQ-UX-011 | 开发试跑 HTML 报告（可选） |
| `planning/tasks/DEV-TASK-WSC-206.md` | — | 本交付说明（派发要求） |

未改：`contracts/**`、`frontend/src/**`（含 features/styles/theme/App/layouts/router）、`backend/**`、`product/**`、`planning/approved/**`、`ai/rules/**`、`state.yaml` / `events.jsonl`。

## HOTFIX

**不需要。** 全部选择器/文案适配落在 `tests/e2e/**`；未发现必须改生产 DOM/`data-testid`/aria 才能通过的阻塞。载体 B（`GAP-UX-006-01`）按计划保留为非 P0。

## REQ 映射

| REQ | 交付要点 |
|---|---|
| **REQ-UX-011** | 正式差距清单闭环 + 走查截图 + E2E 规格扩展（三角色矩阵） |
| **REQ-UX-009** | 主路径空/加载/反馈抽检勾选（WALKTHROUGH + `GAP-UX-009-01`） |
| **REQ-UX-010** | 1280×800 / 1440×900 破坏性检查勾选（WALKTHROUGH + `GAP-UX-010-01`） |

## E2E 适配要点（相对 V11）

1. 角色切换：原生 `<select>` → `data-testid=role-switcher` 下拉（`switchRole` / `expectCurrentRole`）
2. 总览文案：`核心运营指标` → `/核心运营指标/`（副标题扩展）
3. 编辑提交：页头+页脚双「提交」→ `button[type="submit"]`
4. 分类维护：`新增子类` → `/新增三级分类/`
5. 上链版本：`getByText('v1')` 双命中 → `.chain-snapshot-version`
6. 导入：运行时生成唯一编码 CSV，避免 `IMP-PAR-0001` 历史占用致 `all_row_failure`
7. 目录维护：断言 `maintenance-list` **或** `empty`；若 empty 则提示重启 BE 以触发 `ensurePendingSamples`
8. **新增** `6b`/`6c`：ADMIN/PROVIDER/USER 写入口可见性 + 结构不可达（`router.replace`）

## 自测 / 试跑

### 制品存在性

| 路径 | 结果 |
|---|---|
| `ux-gap-checklist.md` | 存在；P0 open=0；载体 B P1 open |
| `ux-walkthrough/screenshots/01`–`10` | 10 文件齐全 |
| `ux-walkthrough/WALKTHROUGH.md` | 含视口勾选栏 + PO/UX 确认栏 |

### E2E 试跑（开发侧，非正式门禁）

```powershell
cd C:\WorkSpace\AI-SEP\tests\e2e
$env:E2E_RUN='1'
# 注意：勿用会双加载 Playwright 的 `pnpm exec playwright`（本机曾 No tests）
node C:\WorkSpace\AI-SEP\node_modules\.pnpm\@playwright+test@1.62.0\node_modules\@playwright\test\cli.js test specs/p0-wsc-v1.1.spec.ts
```

计划等价声明：`pnpm --dir tests/e2e exec playwright test specs/p0-wsc-v1.1.spec.ts`（环境需保证单一 `@playwright/test` 解析）。

| 项 | 结果 |
|---|---|
| 退出码 | **0** |
| 用例 | **14 passed / 0 failed** |
| 含三角色矩阵 | 6b + 6c(PROVIDER) + 6c(USER) **PASSED** |
| 报告 | `tests/e2e/reports/p0-wsc-v1.2-ux/` |
| evidenceId | 目标 `TESTRUN-WSC-E2E-V12-UX`（**正式落盘由独立 tester**） |

试跑前置：曾耗尽待关联种子时重启 `data-chain-service`（`mvn … spring-boot:run -Dspring-boot.run.profiles=local`），使 `@PostConstruct ensurePendingSamples` 重植 `PEND-000*`。

### 视口抽检

开发浏览器对照运行中前端：1280×800 与 1440×900 下侧栏不遮主区、卡片不重叠（总览）；modal 不溢出依据正式截图 #06/#07 + 运行态壳层勾选。

## SCOPE / denyModify

- 未改 feature 源码 / testid / aria
- 未改 styles/theme、contracts、backend、sql、product、approved plan
- 未兼任 codeReviewer / tester；未标 VERIFIED；未改 Run `state`/`events`

## 后续

- 独立 `codeReviewer`（≠ developer-wsc-206）
- 独立 tester 执行正式 `TESTRUN-WSC-E2E-V12-UX` 并落盘证据
- PO/UX 签署 `WALKTHROUGH.md` 确认栏；确认是否接受保留载体 B
