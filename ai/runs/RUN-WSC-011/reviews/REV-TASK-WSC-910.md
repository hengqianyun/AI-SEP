# 代码审查报告 — TASK-WSC-910

```yaml
reviewId: REV-TASK-WSC-910
taskId: TASK-WSC-910
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 1
role: codeReviewer
actorInstance: code-reviewer-wsc-910
mustDifferFrom: developer-wsc-910
decision: REQUEST_CHANGES
p0Count: 0
p1Count: 1
p2Count: 1
reviewedAt: 2026-08-17T16:55:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/skills/code-review/SKILL.md
  - ai/runs/RUN-WSC-011/HOTFIX-TASK-WSC-910.md（验收权威）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-910.md（只读对照，不采信为 PASS）
  - 实地：CatalogMaintenancePage.vue、useCatalogMaintenance.ts(+spec)、CatalogMaintenanceUx.spec.ts
  - 实地：ant-design-vue@4 vc-pagination changePageSize 双 emit 顺序
  - denyModify 对照：**/sql/**、tests/e2e/playwright.config.ts、p0-wsc-v1.4.spec.ts、RbacMatrix/WriteAuthorizationInterceptor、contracts/VERSION、浏览 loadMore、座序图企业筛、industryCategory 浏览筛
riskTags: [hotfix, blocksRelease, pagination-size]
kind: HOTFIX
note: |
  无 auth-model-change / schema-migration：本 REV 不代 securityReviewer / migrationReviewer。
  未代 tester 宣称 PASS / VERIFIED。未改被审源码 / state.yaml / events.jsonl / DEV。
```

## 结论

**REQUEST_CHANGES** — **P0=0，P1=1**。对照 `HOTFIX-TASK-WSC-910.md` 五项验收：两入口同一 `Pagination` 已打开 size changer、档位 10/20/50/100、默认 10、仅翻页保留 size + quick jumper、`@change` 与 `@showSizeChange` 均接到 `applyPagination`。907 同实例 scope、双入口、筛 Tab 清选择、无跨页全选、FIND-WSC-907-001 getter **未回退**。字面 writeSet 内；未改浏览 `loadMore` / 后端契约 / RBAC。

阻塞项：**第 2 页（或更大页码）改 pageSize 时，ant-design-vue 会同步先 `showSizeChange` 再 `change`，且 `current` 在新 size 下仍合法时不会回到 1。** 页侧 `onShowSizeChange` 虽强制 `applyPagination(1, size)`，紧随的 `onPageChange(current, size)` 因 size 已写入而走 `goPage(current)`，最终请求 `page` 不是 1。验收第 3 条「回到第 1 页」在主路径不成立。现有 910 测只调 `setPageSize` / 单次 `applyPagination`，未模拟双事件。

P0/P1 未清零，不得进入独立 tester 门禁。DEV 自测 **不等于** tester PASS。

## 五项验收（独立核验）

### 1. 两入口同一分页可见 size changer

| 项 | 实地 | 结果 |
|---|---|---|
| 双入口同一页 | `routes.ts`：`/catalog/maintenance` `scope: 'full'`；`/my-catalog` `scope: 'myCatalog'`；均为 `CatalogMaintenancePage.vue` | **通过** |
| size changer 打开 | `:show-size-changer="true"`；无 `"false"` | **通过** |
| 绑定档位 | `:page-size-options="PAGE_SIZE_OPTION_LABELS"` | **通过** |

### 2. 档位 10/20/50/100；默认 10

| 项 | 实地 | 结果 |
|---|---|---|
| 四档 | `PAGE_SIZE_OPTIONS = [10, 20, 50, 100]`；`clampPageSize` 只落到四档；≤ 服务端 maximum 100 | **通过** |
| 默认 10 | `DEFAULT_PAGE_SIZE = 10`；`pageSize = ref(DEFAULT_PAGE_SIZE)`；列表请求显式带 `pageSize`（不依赖 OpenAPI/后端 default 20） | **通过** |
| 服务端回显 | `CatalogMaintenanceService.list` 将请求值钳到 1–100 后写入响应 `pageSize`；前端 `loadEntries` 回写 `res.data.pageSize` | **通过（本任务未改后端，既有行为）** |

### 3. 改 size：新 pageSize + page=1 + 清勾选（不得只走 `goPage(1)`）

| 项 | 实地 | 结果 |
|---|---|---|
| 单次 `applyPagination` / `setPageSize` | size 变化分支：写 `pageSize`、`page=1`、`clearSelection`、`loadEntries()`；**不**走 `goPage(1)` | **单次调用通过** |
| 第 1 页改 size | `goPage(1)` 在 `next === page` 时 no-op；size 分支绕开，spec 有覆盖 | **通过** |
| **第 >1 页改 size（UI 双事件）** | antdv `changePageSize`：`showSizeChange(current, size)` 随后 `change(current, size)`；`current` 仅在超出新总页数时下调，**不会重置为 1**。`onShowSizeChange` → page=1 后，`onPageChange` 见 size 已相等 → `goPage(current)` 再拉旧页 | **不通过 → FIND-WSC-910-001** |

### 4. 仅翻页保留 size + quick jumper

| 项 | 实地 | 结果 |
|---|---|---|
| quick jumper | `:show-quick-jumper="true"` 保留 | **通过** |
| 仅翻页 | size 未变时 `applyPagination` 委托既有 `goPage`；spec：`applyPagination(2, 20)` query 仍 `pageSize: 20` | **通过** |

### 5. 自动化非空

| 项 | 实地 | 结果 |
|---|---|---|
| UX spec `910-page-size-changer` | 断言 `show-size-changer`、options、两事件均调 `applyPagination`；**源码字符串匹配，未挂载 Pagination、未模拟双 emit** | **存在但未覆盖主路径** |
| composable spec | 20/50/100、`page=1`、清选择、第 1 页改 size 仍重拉、钳位；**未按 antdv 顺序连打两次** | **存在但未覆盖主路径** |

## `@change` / `@showSizeChange` 是否接到同一逻辑

两处均调用 `applyPagination`，无分叉实现。**漏刷新风险已消除；双触发未去重。** antdv 4 `vc-pagination/Pagination.js` `changePageSize`（约 L233–261）在同一同步栈：

1. `__emit('showSizeChange', current, size)`
2. `__emit('change', current, size)`

页侧：

- `onShowSizeChange(_current, size)` → `applyPagination(1, size)`（忽略库给出的 current，强制 1）
- `onPageChange(nextPage, nextSize)` → `applyPagination(nextPage, nextSize)`（使用库给出的 current）

`applyPagination` 只在 `clamp(nextSize) !== pageSize.value` 时重置到第 1 页；第二次调用 size 已相等，必然 `goPage(nextPage)`。模板 `void` 不 await，第一次 `loadEntries` 在 `await listMaintenanceEntries` 前已读到 `page=1`，第二次同步把 `page` 写成 `current` 再发请求。后完成的响应覆盖状态 → 停留在旧页码。

## 907 不得回退

| 项 | 实地 | 结果 |
|---|---|---|
| 同实例 `full → myCatalog` | `useCatalogMaintenance(MaybeRefOrGetter)`；`watch(currentScope)`：`page=1` + `clearSelection` + `cancelEdit` + `init`；spec 仍断言写 URL `?scope=myCatalog` | **通过** |
| 双入口 | 路由 props + 页头文案；UX spec `907-my-catalog-scope-split` 仍在 | **通过** |
| 筛 / Tab 清选择 | `setStatusTab` / `applyFilterPath` 仍 `clearSelection`；spec「本页全选 / 取消；筛/Tab/翻页清选择」仍在 | **通过** |
| 无跨页全选 | `toggleSelectAllOnPage` 只遍历当前 `entries`；页源无「跨页全选」「已全选全部」 | **通过** |
| FIND-WSC-907-001 | `useCatalogMaintenance(() => props.scope)`；`watch(() => props.scope)` 重跑 `canEnterMaintenancePage`；UX spec 仍禁 `useCatalogMaintenance(props.scope)` | **通过** |

## Scope 检查（writeSet / denyModify）

相对本 hotfix 建议 writeSet：维护页 + composable + 两份 spec。工作树其它脏文件归属本 Run 901–909，**不**计 910 越界。

| 项 | 结果 | 证据 |
|---|---|---|
| 未改浏览 `loadMore` | **通过** | 维护页无 `loadMore`；`useCatalogBrowse` 仍触底追加；本任务未改该模型 |
| 未改后端 / OpenAPI / `wsc-contracts` | **通过** | 维护 list 仍 1–100；`contracts/VERSION` 仍 `2.3.3`；`git diff HEAD -- contracts/VERSION` 空 |
| 未改 v14 e2e | **通过** | `playwright.config.ts`、`p0-wsc-v1.4.spec.ts` status 干净 |
| 无新 Flyway / `**/sql/**` | **通过（910）** | 既有未跟踪 `V7__*` 属 904 |
| 未改 RBAC / 拦截器授权 | **通过（910）** | 本任务未改 `RbacMatrix` / `WriteAuthorizationInterceptor` 判定。工作树脏来自 **905/906**。**不**升 P0/P1，**不**代 securityReviewer |
| 无企业管理页；座序图无企业筛；浏览无 `industryCategory` 筛 | **通过** | `CirculationSeatMap.vue` 无 enterprise；`BrowseFilterBar.vue` 无 `industryCategory`；`buildQuery` 仍不传该键 |
| 未改 `state.yaml` / `events.jsonl` | **通过** | 本实例未写 |

## 测试覆盖与门禁边界

| 项 | 本 REV |
|---|---|
| 五项验收自动化存在 | **部分**：UI 开关与 composable 单次调用有断言；**缺** antdv 双 emit + 非第 1 页 |
| DEV 自测 vitest 2 files / 15 tests exit 0 | **见过 DEV 落盘说明**；**不等于**独立 tester PASS |
| 本实例重跑命令 | **否**（以源码、antdv 实现与测试断言独立核验，不替代 tester） |
| 标 VERIFIED | **禁止且未做** |

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-910-001 | P1 | OPEN | `CatalogMaintenancePage.vue` `onShowSizeChange` → `applyPagination(1, size)` 后，antdv 同栈再 `change(current, size)`（`frontend/node_modules/ant-design-vue/es/vc-pagination/Pagination.js` `changePageSize`：仅当 `current > calculatePage(newSize)` 才下调 current，**不回到 1**）。`applyPagination` 第二次 size 已相等则 `goPage(current)`。例：total=120、page=2、10→20 → 最终 `listMaintenanceEntries` 为 `{ page: 2, pageSize: 20 }`，违反验收「回到第 1 页」。composable `910-page-size-changer` 只测 `setPageSize` / 单次 `applyPagination`，不能拦住 | ① 第 >1 页改 size 后只应留下一次列表请求，且 `page=1`、新 `pageSize`、`selectedCount===0`。② 自动化须按库顺序连打 `showSizeChange(current, size)` 再 `change(current, size)`（`current>1` 且新 size 下该页仍合法）。③ 第 1 页改 size 仍须重拉（不得只靠 `goPage(1)`）；仅翻页仍走 `goPage` 且保留 size | HOTFIX-TASK-WSC-910 acceptance 3 / REQ-CAT-013 |
| FIND-WSC-910-002 | P2 | OPEN | DEV 自测 15 passed **不是**独立 tester 门禁。UX spec 仅读 `.vue` 字符串，未挂载 Pagination | 独立 tester 在 P1 关闭后再跑具名用例；不要求把 UX spec 改成组件挂载，除非用来锁双事件 | HOTFIX-TASK-WSC-910 acceptance 5 |

## 架构与可维护性备注（非阻塞）

- `setPageSize` → `applyPagination(1, size)` 与「不可走 `goPage(1)`」注释方向正确；缺陷在页事件去重，不在 composable 单入口语义。
- 修复方向由 developer 自选（例如 size 变化忽略随后 `@change` 的 page、或 `@change` 在检测到 size 变化时始终强制 page=1 且抑制紧随的 page-only `goPage`）；本 REV 不改源码。
- 后端 default 20 与前端默认 10 并存：前端始终带 `pageSize` query，不构成本轮 P1。
- 未改授权模型，**不**因缺 securityReviewer 而加 P0/P1。

## 决策依据

- P1>0 → **REQUEST_CHANGES**，不得进入独立 **tester**。
- 本 REV **不**宣称测试门禁通过，**不**标 VERIFIED。
- 无 SEC/MIG 触发；910 未改 RBAC/拦截器授权。
- P2 不单独阻塞；P1 关闭后由独立 tester 再跑。
