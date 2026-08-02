# 任务包 TASK-WSC-305（HOTFIX）

```yaml
taskId: TASK-WSC-305
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
status: VERIFIED
wave: HOTFIX
verifiedAt: 2026-08-02T19:37:00+08:00
evidenceId: EVID-TASK-WSC-305-1
evidence: planning/tasks/TESTRUN-TASK-WSC-305.md
kind: HOTFIX
goal: |
  目录产品列表触底自动分页可用：列表容器内滚动到底自动 loadMore 追加；
  无页码切换器；「已加载 x / 共 y」与加载中/已全部可读。
  根因多为滚动落在窗口而非列表 pane（布局高度未约束）。
reqs: [REQ-CAT-001, REQ-UX-004]
dependsOn: [TASK-WSC-304]
blocksRelease: true
actorInstance: developer-wsc-305
```

## 背景

PO 选择「暂缓 V1.3 发布」后先修本项。`useCatalogBrowse.loadMore` + `@scroll` 已存在，但现场表现为分页数据到了、无法用组件翻页、触底也不加载——按 REQ-CAT-001 / REQ-UX-004 滚动加载验收。

## writeSet

- `frontend/src/features/catalog/browse/**`
- 对应 `*.spec.ts` / `__tests__/**`

## denyModify

- `contracts/**`；`frontend/src/api/**`；`backend/**`
- `frontend/src/components/**`
- `frontend/src/features/catalog/{import,editor,detail,admin,maintenance}/**`
- `frontend/src/styles/**`；`theme/**`；`layouts/**`；`shell/**`；`router/**`
- `tests/e2e/**`（本 HOTFIX 以 Vitest + 手动/组件测为主；若必须改 E2E 须在 DEV 声明并说明）
- `product/**`；`planning/approved/**`；`ai/runs/**/state.yaml`（除 orchestrator）

## acceptance

- 桌面常见分辨率下，列表 pane 自身可滚动；滚近底部自动请求下一页并追加，不丢已选/滚动位置（继承既有行为）
- 无页码分页器；`hasMore` 时可见加载中；无更多时「已加载全部」
- USER/ADMIN/PROVIDER 浏览路径均可用；不破坏导入入口
- Vitest：覆盖 loadMore / hasMore / 布局或 scroll 触发相关测（按改动面）

## testScope

- Vitest：browse composable + 必要时页面级
- 人工或现有 E2E 抽样：登录后 `/catalog` 多页数据触底追加（tester 门禁）

## 派发记录

- dispatchedAt: 2026-08-02T19:22:00+08:00
- identity: orchestrator（PO 选「2」暂缓发布并 HOTFIX）
