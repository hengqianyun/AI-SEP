# DEV-TASK-WSC-605

```yaml
taskId: TASK-WSC-605
runId: RUN-WSC-008
actorInstance: developer-wsc-605
planId: PLAN-WSC-5.2
status: READY_FOR_REVIEW
completedAt: 2026-08-06T14:20:00+08:00
evidenceId: TESTRUN-WSC-E2E-V14
reqs:
  - REQ-SHELL-001
  - REQ-RBAC-001
  - REQ-USER-001
  - REQ-CAT-009
  - REQ-CAT-010
  - REQ-CAT-001
```

> 交付说明落于 `ai/runs/RUN-WSC-008/`（避免 `planning/**` denyModify）。未改 `state.yaml` / `events.jsonl`。未标 VERIFIED。未改 frontend/backend/contracts。

## objective

独占 Playwright E2E，强制覆盖 PLAN-WSC-5.2 §6.1 场景 1–7；产出可审计证据包 `TESTRUN-WSC-E2E-V14`。

## filesChanged

| 路径 | 说明 |
|---|---|
| `tests/e2e/specs/p0-wsc-v1.4.spec.ts` | V1.4 P0 七场景规格（15 用例） |
| `tests/e2e/helpers/v14-selectors.ts` | 只读角色/公共目录无写/我的产品导入/行业分类/自动编码/座序图根 |
| `tests/e2e/fixtures/auth.ts` | `logoutIfNeeded`；`loginAs` 在已有会话时先退出（适配 V1.4 无切角色） |
| `tests/e2e/playwright.config.ts` | `testMatch=p0-wsc-v1.4.spec.ts`；报告目录 `reports/p0-wsc-v1.4/` |
| `tests/e2e/package.json` | script `test:p0-v14` |
| `tests/e2e/reports/p0-wsc-v1.4/index.html` | Playwright HTML 报告 |
| `tests/e2e/reports/p0-wsc-v1.4/junit.xml` | JUnit |
| `tests/e2e/reports/p0-wsc-v1.4/EVIDENCE.md` | evidenceId + 七场景勾选 + 命令 |

未改：`frontend/**`、`backend/**`、`contracts/**`、`product/**`、`planning/**`、`ai/runs/**/state.yaml`、`events.jsonl`。

## §6.1 七场景勾选

| # | 场景 | 结果 | 用例 |
|---|---|---|---|
| 1 | 真登录三角色；角色绑定；无自由切角色；只读角色展示 | **PASS** | `1) ×3` |
| 2 | ADMIN 用户管理 CRUD；PROVIDER/USER 不可达 | **PASS** | `2a`/`2b` |
| 3 | 公共目录三角色无增改导 | **PASS** | `3) ×3` |
| 4 | PROVIDER 我的产品增改导；列表本人；无座序图；返回仍在我的产品 | **PASS** | `4)`/`4x` |
| 5 | ADMIN 产品写 API 403；分类/维护仍可用 | **PASS** | `5)` |
| 6 | 公共目录座序图 Top5+总数+hover；我的产品无图 | **PASS** | `6)` |
| 7 | V1.3 回归：导入四态 + OpenAPI 编辑不回退 | **PASS** | `7a`/`7b` |

## 自测命令与 exitCode

```text
cd tests/e2e
$env:E2E_RUN='1'
$env:E2E_BASE_URL='http://127.0.0.1:5173'
pnpm run test:p0-v14
```

等价：`pnpm exec playwright test specs/p0-wsc-v1.4.spec.ts --config=playwright.config.ts`

| 项 | 值 |
|---|---|
| 结果 | **15 passed (29.3s)** |
| **exitCode** | **0** |
| BLOCKED | **否** |

前置：frontend `http://127.0.0.1:5173` + backend `http://127.0.0.1:8080` 已启动。

## 报告路径

- 目录：`tests/e2e/reports/p0-wsc-v1.4/`
- `evidenceId`：`TESTRUN-WSC-E2E-V14`（见 `EVIDENCE.md`）
- HTML：`tests/e2e/reports/p0-wsc-v1.4/index.html`
- JUnit：`tests/e2e/reports/p0-wsc-v1.4/junit.xml`

## 选择器（只读消费；未改生产 DOM）

| 用途 | 选择器 |
|---|---|
| 只读角色 | `session-role-label`；断言无 `.role-switch select` / `button.role-dropdown-trigger` / listbox |
| 用户管理 | `nav-users`、`users-admin-page`、`users-forbidden`、`user-username`/`password`/`display-name`/`role`/`create`、`users-table`、`user-row-role`、`user-soft-delete` |
| 公共目录无写 | `catalog-browse`；`catalog-open-create`/`catalog-open-import` count=0；`#import-modal` 无 |
| 我的产品 | `nav-my-products`、heading「我的数据产品」、`catalog-open-create`/`import` |
| 座序图根 | **`catalog-seat-map-slot`**（父组件 fallthrough 覆盖组件内 `circulation-seat-map`）；子：`seat-map-total`/`legend`/`seat-map-chip-*` |
| 行业分类 | `industry-category-select`（替代已下线的 `cat-l1/l2/l3`） |
| 产品编码 | 只读自动生成；`fillCreateProductBasics` 读 inputValue |
| 详情返回 | `aria-label=返回目录` → `/my-products` |
| 登录 | `login-form` / `input[name=username|password]`；`退出登录` |

### 选择器备注（非 BLOCKED）

1. **座序图根 testid**：Vue fallthrough 使根节点仅暴露 `catalog-seat-map-slot`；场景 6 用 slot + 子 testid 完成 Top5/总数/hover 举证，**未**要求生产改 DOM。
2. **产品编码只读**：创建态不可 `fill`；读自动生成编码。
3. **行业分类**：单选 `industry-category-select`，非三级级联。

## scope

- writeSet 内：`tests/e2e/specs/**`、`package.json`、`playwright.config.ts`、`reports/p0-wsc-v1.4/**`、helper `tests/e2e/helpers/v14-selectors.ts`、fixture `tests/e2e/fixtures/auth.ts`
- denyModify 遵守：未触碰 frontend/backend/contracts/sql/product/planning/state/events
- 正式 VERIFIED 由独立 tester / Orchestrator 门禁；本交付仅 `READY_FOR_REVIEW`
