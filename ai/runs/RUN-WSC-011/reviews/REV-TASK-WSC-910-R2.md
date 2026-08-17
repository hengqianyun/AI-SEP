# 代码审查报告 — TASK-WSC-910 Round 2

```yaml
reviewId: REV-TASK-WSC-910-R2
taskId: TASK-WSC-910
runId: RUN-WSC-011
planId: PLAN-WSC-8.3
round: 2
role: codeReviewer
actorInstance: code-reviewer-wsc-910-r2
decision: APPROVE
p0Count: 0
p1Count: 0
p2Count: 1
reviewedAt: 2026-08-17T17:20:00+08:00
basedOn:
  - ai/agents/code-reviewer.md
  - ai/skills/code-review/SKILL.md
  - ai/runs/RUN-WSC-011/HOTFIX-TASK-WSC-910.md（五项验收权威）
  - ai/runs/RUN-WSC-011/reviews/REV-TASK-WSC-910.md（Round 1 REQUEST_CHANGES；FIND-WSC-910-001 closeWhen 权威）
  - ai/runs/RUN-WSC-011/DEV-TASK-WSC-910.md（actorInstance=developer-wsc-910-r1；只读对照，不采信为 PASS）
  - 实地：useCatalogMaintenance.ts(+spec)、CatalogMaintenancePage.vue、CatalogMaintenanceUx.spec.ts
  - 实地：ant-design-vue@4 vc-pagination changePageSize 双 emit 顺序
mustDifferFrom:
  - developer-wsc-910
  - developer-wsc-910-r1
  - code-reviewer-wsc-910
closedFinding: FIND-WSC-910-001
closedIssues:
  - FIND-WSC-910-001
openIssues:
  - FIND-WSC-910-002
riskTags: [hotfix, blocksRelease, pagination-size]
kind: HOTFIX
note: |
  本 REV 仅 codeReview Round 2 复审门禁；不得把 R1 REQUEST_CHANGES 当复审结论。
  未代 tester 宣称 PASS/VERIFIED；未改源码 / state.yaml / events.jsonl / R1 REV。
  FIND-002 仍为 P2，不因此 REQUEST_CHANGES。无 auth-model-change / schema-migration。
```

## 结论

**APPROVE** — **P0=0，P1=0**。Round 1 唯一阻塞项 **FIND-WSC-910-001** 已按 closeWhen 三项修复并关闭：`applyPagination` 在 size 变化时强制 `page=1` 并置同栈抑制标志，紧随的 page-only `goPage(current)` 被挡住；自动化按库顺序同步连打（中间无 await）；第 1 页改 size 仍走 `loadEntries`，微任务后仅翻页仍委托 `goPage` 且保留 size。五项 hotfix 验收仍成立。907 同实例 scope、双入口、筛 Tab 清选择、无跨页全选、FIND-WSC-907-001 getter **未回退**。本轮无新 P0/P1。开放 **P2×1**（FIND-WSC-910-002）不阻塞进入独立 tester 门禁。

**未代 tester 宣称通过**；未标 VERIFIED。DEV 自测 **不等于** tester PASS。未改 `REV-TASK-WSC-910.md`。

## FIND-WSC-910-001 closeWhen 核验

R1 closeWhen 权威（不得抄 DEV）：

1. 第 >1 页改 size 后只应留下一次列表请求，且 `page=1`、新 `pageSize`、`selectedCount===0`
2. 自动化须按库顺序**连打**（中间不要 await）：`showSizeChange(current, size)` 再 `change(current, size)`，`current>1` 且新 size 下该页仍合法
3. 第 1 页改 size 仍须重拉（不得只靠 `goPage(1)`）；仅翻页仍走 `goPage` 且保留 size

| closeWhen 项 | 结果 | 证据 |
|---|---|---|
| ① 第 >1 页改 size：一次请求、`page=1`、新 size、清勾选 | **PASS** | `applyPagination` size 分支写 `pageSize`、`page=1`、`clearSelection`、`suppressPageOnlyAfterSizeChange=true` 后 `loadEntries()`。同栈第二次 size 已相等则命中抑制分支 `Promise.resolve()`，**不**走 `goPage(current)`。`loadEntries` 在 `await listMaintenanceEntries` 前同步读 `page`/`pageSize`，故唯一请求为 `{ page: 1, pageSize: 20 }` |
| ② 自动化按库顺序同步连打 | **PASS（制品存在）** | `useCatalogMaintenance.spec.ts` 具名 `antdv dual emit from page>1 keeps page=1`：`total=120`、`goPage(2)` 后勾选，**中间不 await** 连打 `applyPagination(1, 20)` 再 `applyPagination(2, 20)`（对应页侧 `onShowSizeChange` → `applyPagination(1, size)` 与 `onPageChange` → `applyPagination(current, size)`）。`current=2>1`；新 size=20 下 `ceil(120/20)=6`，第 2 页仍合法。断言 `pageSize===20` 的 list 调用长度为 1 且 `{ page: 1, pageSize: 20 }`，`selectedCount===0` |
| ③ 第 1 页改 size 仍重拉；仅翻页走 `goPage` 且保留 size | **PASS** | 第 1 页：`goPage(1)` 在 `next===page` 时 no-op；size 分支绕开并 `loadEntries()`。仅翻页：微任务清标志后 size 相等走 `goPage`。spec「size change on page 1 still reloads; page-only keeps size」先证 `goPage(1)` 不增调用，再 `applyPagination(1, 20)` 重拉，await 后再 `applyPagination(2, 20)` 仍 `pageSize: 20` |

**同栈抑制是否挡住第二次 `goPage(current)`（实地，非 DEV 转述）**

antdv 4 `vc-pagination/Pagination.js` `changePageSize`（约 L233–261）同一同步栈：

1. `__emit('showSizeChange', current, size)` — `current` 仅当 `current > calculatePage(newSize)` 才下调，**不重置为 1**
2. `__emit('change', current, size)`

页侧（`CatalogMaintenancePage.vue`）两 handler 均 `void` 不 await，故第二次 `applyPagination` 仍在同一 JS 栈、`queueMicrotask` 清标志之前：

```294:310:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.ts
  function applyPagination(nextPage: number, nextSize?: number) {
    const size = clampPageSize(nextSize ?? pageSize.value)
    if (size !== pageSize.value) {
      pageSize.value = size
      page.value = 1
      clearSelection()
      suppressPageOnlyAfterSizeChange = true
      queueMicrotask(() => {
        suppressPageOnlyAfterSizeChange = false
      })
      return loadEntries()
    }
    if (suppressPageOnlyAfterSizeChange) {
      return Promise.resolve()
    }
    return goPage(nextPage)
  }
```

页接线：

```104:112:frontend/src/features/catalog/maintenance/CatalogMaintenancePage.vue
/** ant-design-vue Pagination `@change`：(page, pageSize)。size 刚变时 applyPagination 会忽略紧随的旧 current。 */
function onPageChange(nextPage: number, nextSize: number) {
  void applyPagination(nextPage, nextSize)
}

/** size 变化也可能只走 `@showSizeChange`；强制 page=1，与 @change 共用 applyPagination */
function onShowSizeChange(_current: number, size: number) {
  void applyPagination(1, size)
}
```

例：total=120、page=2、10→20 → `onShowSizeChange` 写入 page=1 + size=20 + 抑制；同栈 `onPageChange(2, 20)` size 已相等且标志仍 true → **不** `goPage(2)`。R1 主路径缺口已闭合。

**spec 是否同步连打而非两次 await**

```287:304:frontend/src/features/catalog/maintenance/composables/useCatalogMaintenance.spec.ts
    const callsBefore = listMaintenanceEntries.mock.calls.length
    // 页 handler 同栈：showSizeChange(current, size) 再 change(current, size)；中间不 await
    const showSizeChange = m.applyPagination(1, 20)
    const change = m.applyPagination(2, 20)
    await Promise.all([showSizeChange, change])

    const size20Calls = listMaintenanceEntries.mock.calls
      .slice(callsBefore)
      .map((c) => c[0] as { page: number; pageSize: number })
      .filter((q) => q.pageSize === 20)
    expect(size20Calls).toHaveLength(1)
    expect(size20Calls[0]).toEqual(expect.objectContaining({ page: 1, pageSize: 20 }))
```

两次 `applyPagination` 在同一同步段先后调用，**中间无 await**；`Promise.all` 只等待已发出的 load。不是 R1 所拒的「两次独立 await」。

**微任务后仅翻页是否仍可用**

`queueMicrotask` 清 `suppressPageOnlyAfterSizeChange`。closeWhen ③ 用例在 `await applyPagination(1, 20)` 之后再 `applyPagination(2, 20)`：await 已让微任务跑完，标志为 false，size 相等 → `goPage(2)`，query 仍 `pageSize: 20`。

本实例为核对 closeWhen 测试制品曾执行上述两份 spec（16 passed / 2 files）；**此为审查取证，不是 tester 门禁，不标 PASS/VERIFIED**。

## 五项验收（复审仍须成立）

### 1. 两入口同一分页可见 size changer

| 项 | 实地 | 结果 |
|---|---|---|
| 双入口同一页 | UX spec `907-my-catalog-scope-split` 仍断言 `/catalog/maintenance` `scope: 'full'` 与 `/my-catalog` `scope: 'myCatalog'` 均挂 `CatalogMaintenancePage.vue` | **通过** |
| size changer 打开 | `:show-size-changer="true"`；无 `"false"` | **通过** |
| 绑定档位 | `:page-size-options="PAGE_SIZE_OPTION_LABELS"` | **通过** |

### 2. 档位 10/20/50/100；默认 10

| 项 | 实地 | 结果 |
|---|---|---|
| 四档 | `PAGE_SIZE_OPTIONS = [10, 20, 50, 100]`；`clampPageSize` 只落到四档；≤ 服务端 maximum 100 | **通过** |
| 默认 10 | `DEFAULT_PAGE_SIZE = 10`；`pageSize = ref(DEFAULT_PAGE_SIZE)`；list 显式带 `pageSize` | **通过** |

### 3. 改 size：新 pageSize + page=1 + 清勾选（不得只走 `goPage(1)`）

| 项 | 实地 | 结果 |
|---|---|---|
| 单次 `applyPagination` / `setPageSize` | size 变化分支：写 size、`page=1`、`clearSelection`、`loadEntries()`；**不**走 `goPage(1)` | **通过** |
| 第 1 页改 size | `goPage(1)` no-op；size 分支绕开；spec 覆盖 | **通过** |
| **第 >1 页改 size（UI 双事件）** | 同栈抑制挡住随后 `goPage(current)`；dual-emit spec 覆盖 | **通过（R1 缺口已闭）** |

### 4. 仅翻页保留 size + quick jumper

| 项 | 实地 | 结果 |
|---|---|---|
| quick jumper | `:show-quick-jumper="true"` 保留 | **通过** |
| 仅翻页 | 标志清除后 size 未变委托 `goPage`；spec `applyPagination(2, 20)` query 仍 `pageSize: 20` | **通过** |

### 5. 自动化非空

| 项 | 实地 | 结果 |
|---|---|---|
| UX spec `910-page-size-changer` | 断言 `show-size-changer`、options、两事件均调 `applyPagination`（源码字符串；未挂载 Pagination） | **存在** |
| composable spec | 20/50/100、`page=1`、清选择、第 1 页改 size 仍重拉、钳位；**新增** antdv 双 emit 同步连打 | **存在且覆盖 R1 主路径** |

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
| 未改浏览 `loadMore` | **通过** | 维护页无 `loadMore`；`useCatalogBrowse` 仍触底追加 |
| 未改后端 / OpenAPI / `wsc-contracts` | **通过** | `contracts/VERSION` 仍 `2.3.3` |
| 浏览无 `industryCategory` 筛 | **通过** | `BrowseFilterBar.vue` 无 `industryCategory` |
| 座序图无企业筛 | **通过** | `CirculationSeatMap.vue` 无 enterprise |
| 未改 `REV-TASK-WSC-910.md` | **通过** | 本实例未写 R1 正文 |
| 未改 `state.yaml` / `events.jsonl` | **通过** | 本实例未写 |
| 无 auth-model-change / schema-migration | **通过** | 本 REV 不代 securityReviewer / migrationReviewer |

## Round 1 Findings 处置

| id | sev | R1 | R2 status | 证据 / 说明 |
|---|---|---|---|---|
| FIND-WSC-910-001 | **P1** | OPEN | **CLOSED** | closeWhen 三项均满足；见上表。计入 `closedFinding` / `closedIssues` |
| FIND-WSC-910-002 | P2 | OPEN | **OPEN** | DEV 自测 16 passed **不是**独立 tester 门禁。UX spec 仍只读 `.vue` 字符串，未挂载 Pagination。**不**要求本轮改成组件挂载。不阻塞 APPROVE |

## Round 2 新 Findings

无。本轮未引入新 P0/P1/P2/P3。

非阻塞观察（不升格）：抑制窗口仅同栈 + 一个微任务；用户下一次翻页属新宏任务，标志已清。页 handler 本身无额外去重，去重全部在 composable，与 R1「两处均调 `applyPagination`」一致。

## 测试覆盖与门禁边界

| 项 | 本 REV |
|---|---|
| 五项验收自动化存在 | **是**：含 antdv 双 emit + 非第 1 页 |
| DEV 自测 vitest 2 files / 16 tests | **见过 DEV 落盘说明**；本实例另作审查取证跑过同一命令（16 passed）；**不等于**独立 tester PASS |
| 标 VERIFIED | **禁止且未做** |

## Findings

| id | severity | status | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|---|
| FIND-WSC-910-001 | P1 | **CLOSED** | 同栈抑制挡住 `goPage(current)`；dual-emit spec 中间无 await；第 1 页改 size 仍 `loadEntries`；微任务后仅翻页走 `goPage` 且保留 size | 已按 R1 closeWhen 三项满足 | HOTFIX-TASK-WSC-910 acceptance 3 / REQ-CAT-013 |
| FIND-WSC-910-002 | P2 | **OPEN** | 审查取证 vitest 与 DEV 自测均非独立 tester 门禁。UX spec 未挂载 Pagination | 独立 tester 在 P1 关闭后再跑具名用例；不要求把 UX spec 改成组件挂载，除非用来锁双事件 | HOTFIX-TASK-WSC-910 acceptance 5 |

## 决策依据

- FIND-WSC-910-001 closeWhen **全部满足** → 计入 `closedFinding` / `closedIssues`。
- 本轮 **P0=0，P1=0** → **APPROVE**（P2 FIND-002 不阻塞）。
- 进入独立 tester 后仍须执行 HOTFIX-TASK-WSC-910 `acceptance`；本 REV **不**宣称测试通过。
- 无 SEC/MIG 触发；910 未改 RBAC/拦截器授权。
