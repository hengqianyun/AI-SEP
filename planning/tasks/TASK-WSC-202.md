# 任务包 TASK-WSC-202

```yaml
taskId: TASK-WSC-202
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: VERIFIED
wave: W2
verifiedAt: 2026-08-02T14:22:30+08:00
evidenceId: EVID-TASK-WSC-202-1
evidence: planning/tasks/TESTRUN-TASK-WSC-202.md
goal: |
  登录页与工作台品牌色一致；总览三列指标卡与图表卡片视觉对齐原型；
  数据契约与图表库默认不改（CSS/配置级收敛）。交付草稿截图 01+03 与差距草稿。
reqs:
  - REQ-UX-003
  - REQ-UX-008
  - REQ-UX-009
allowModify: writeSet（见下）
denyModify:
  - frontend/src/styles/**
  - frontend/src/theme/**
  - frontend/src/App.vue
  - frontend/src/main.ts
  - frontend/src/layouts/**
  - frontend/src/features/shell/**
  - frontend/src/router/**
  - frontend/src/features/catalog/**
  - frontend/src/features/chain/**
  - frontend/src/features/auth/store/**
  - frontend/src/features/auth/composables/**
  - frontend/src/features/auth/api/**
  - contracts/**
  - frontend/src/api/**
  - backend/**
  - tests/e2e/**
  - ai/runs/RUN-WSC-003/ux-gap-checklist.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/**
  - ai/runs/RUN-WSC-003/ux-walkthrough/WALKTHROUGH.md
  - product/**
  - planning/**
  - ai/rules/**
readSet:
  - product/requirements/SNAP-WSC-003.md
  - design/prototypes/wsc-v1.1/index.html
  - planning/approved/PLAN-WSC-3.1.md
  - planning/approved/PLAN-WSC-2.2.md
  - frontend/src/styles/**（只读令牌）
  - frontend/src/api/overview.ts（只读）
  - frontend/src/features/overview/**
  - frontend/src/features/auth/views/**
writeSet:
  - frontend/src/features/overview/**
  - frontend/src/features/auth/views/**
  - frontend/src/features/overview/**/*.spec.ts
  - frontend/src/features/overview/**/__tests__/**
  - frontend/src/features/auth/views/**/*.spec.ts
  - frontend/src/features/auth/views/**/__tests__/**
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-202.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/01-login.png
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/03-overview.png
dependsOn:
  - TASK-WSC-201
mergeAfter: []
riskTags: []
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
actorInstance: developer-wsc-202
codeReviewerMustDiffer: true
parallelWith:
  - TASK-WSC-203
```

## 验收要点

见 `planning/approved/PLAN-WSC-3.1.md` § TASK-WSC-202。

摘要：

- 登录：主色/卡片/输入与按钮层级与壳层品牌一致
- 总览：三列指标卡标题/图标色块/大号数值/描述层次接近原型；图表卡片 header、圆角阴影与留白
- 空态/加载可读且引用共享令牌；功能口径仍满足 SNAP-WSC-002 REQ-OVW-001..005
- 局部 AntDV 须有替换清单且消费 201 令牌（默认 CSS-token-first）
- 草稿 `01-login.png` + `03-overview.png` + `ux-gap-drafts/TASK-WSC-202.md`
- overview / auth views Vitest + typecheck

## 派发记录

- runId: RUN-WSC-003
- wave: W2（∥ TASK-WSC-203）
- dispatchedAt: 2026-08-02T14:05:30+08:00
- identity: orchestrator（人类确认「派发 W2」）
- approvedPlan: planning/approved/PLAN-WSC-3.1.md
- dependsOnVerified: [TASK-WSC-201]
