# 任务包 TASK-WSC-201

```yaml
taskId: TASK-WSC-201
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: VERIFIED
wave: W1
verifiedAt: 2026-08-02T14:04:30+08:00
evidenceId: EVID-TASK-WSC-201-1
evidence: planning/tasks/TESTRUN-TASK-WSC-201.md
goal: |
  落地可追踪令牌与全局样式（CSS-token-first）；侧栏品牌区/导航激活态/主区背景/
  底部企业信息卡/角色切换区与占位菜单提示质感对齐原型；挂载全局样式入口；
  不强制全量 AntDV 挂载。交付草稿截图与差距草稿行。
reqs:
  - REQ-UX-001
  - REQ-UX-002
  - REQ-UX-008
  - REQ-UX-010
allowModify: writeSet（见下）
denyModify:
  - contracts/**
  - frontend/src/api/**
  - backend/**
  - "**/sql/**"
  - frontend/src/router/**
  - frontend/src/features/overview/**
  - frontend/src/features/catalog/**
  - frontend/src/features/chain/**
  - frontend/src/features/auth/**
  - product/**
  - planning/**
  - ai/rules/**
  - tests/e2e/**
  - ai/runs/RUN-WSC-003/ux-gap-checklist.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/**
  - ai/runs/RUN-WSC-003/ux-walkthrough/WALKTHROUGH.md
readSet:
  - product/requirements/SNAP-WSC-003.md
  - design/prototypes/wsc-v1.1/index.html
  - planning/approved/PLAN-WSC-3.1.md
  - planning/approved/PLAN-WSC-2.2.md
  - frontend/src/api/**（只读）
  - frontend/src/layouts/**
  - frontend/src/features/shell/**
  - frontend/src/features/auth/composables/useCanWrite*（只读跑测）
  - frontend/src/App.vue
  - frontend/src/main.ts
  - frontend/src/views/PlaceholderView.vue
writeSet:
  - frontend/src/styles/**
  - frontend/src/theme/**
  - frontend/src/App.vue
  - frontend/src/main.ts
  - frontend/src/layouts/**
  - frontend/src/features/shell/**
  - frontend/src/views/PlaceholderView.vue
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-201.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/02-shell-sidebar.png
  - frontend/src/features/shell/**/__tests__/**
  - frontend/src/features/shell/**/*.spec.ts
  - frontend/package.json
  - frontend/vite.config.*
  - frontend/pnpm-lock.yaml
dependsOn: []
mergeAfter: []
riskTags:
  - frontend-theme
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
actorInstance: developer-wsc-201
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-3.1.md` § TASK-WSC-201 `acceptance` / `testScope`，以及 §2.1–§2.3、§3.1、§3.3。

摘要：

- §2.1 最小令牌集可对照原型；卡片圆角/阴影/字号阶梯可走查
- 侧栏宽约 232px、背景接近 `#0b1120`；Logo +「接入端工作台」；菜单图标位/圆角/hover/active
- 主区浅灰背景 + 内边距；底部企业信息卡 + 角色切换深色菜单/active 蓝强调
- 占位菜单提示不破坏壳层风格；角色切换后写入口仍符合 SNAP-WSC-002 REQ-SHELL-001 / REQ-RBAC-001
- 写入口/角色区：**结构**条件渲染保留；禁止纯 CSS 隐藏替代（§1.4）
- CSS-token-first；不得仅以 AntDV 默认主题替代令牌对齐
- 草稿截图 `02-shell-sidebar.png` + `ux-gap-drafts/TASK-WSC-201.md` schema 行可检
- `frontend` build / typecheck；只读执行 `useCanWrite.spec.ts`

## 派发记录

- runId: RUN-WSC-003
- wave: W1
- dispatchedAt: 2026-08-02T13:50:30+08:00
- identity: orchestrator（人类确认「批准派发 W1」= `CONFIRM_TASK_DISPATCH`）
- approvedPlan: planning/approved/PLAN-WSC-3.1.md
