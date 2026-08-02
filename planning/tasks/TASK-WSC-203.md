# 任务包 TASK-WSC-203

```yaml
taskId: TASK-WSC-203
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: VERIFIED
wave: W2
verifiedAt: 2026-08-02T14:20:00+08:00
evidenceId: EVID-TASK-WSC-203-1
evidence: planning/tasks/TESTRUN-TASK-WSC-203.md
goal: |
  数据目录浏览：页头、空间/行业标签 active、子类分组标题、筛选条、列表行、
  预览分栏与滚动加载「加载中」对齐原型；行为继承 REQ-CAT-001。交付草稿截图 04 与差距草稿。
reqs:
  - REQ-UX-004
  - REQ-UX-009
allowModify: writeSet（见下）
denyModify:
  - frontend/src/styles/**
  - frontend/src/theme/**
  - frontend/src/layouts/**
  - frontend/src/features/shell/**
  - frontend/src/router/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/maintenance/**
  - frontend/src/features/catalog/import/**
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/overview/**
  - frontend/src/features/chain/**
  - frontend/src/features/auth/**
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
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/import/ImportDialog（只读消费；实现归 204）
writeSet:
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/browse/**/*.spec.ts
  - frontend/src/features/catalog/browse/**/__tests__/**
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-203.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/04-catalog-browse.png
dependsOn:
  - TASK-WSC-201
mergeAfter: []
riskTags: []
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
actorInstance: developer-wsc-203
codeReviewerMustDiffer: true
parallelWith:
  - TASK-WSC-202
```

## 验收要点

见 `planning/approved/PLAN-WSC-3.1.md` § TASK-WSC-203。

摘要：

- 标签 active 蓝底；子类分节左侧色条+计数；筛选条与列表行接近原型
- 预览区与列表分栏清晰；加载中不破坏布局且保留滚动位置
- 无结果空态可读；空态/加载引用共享令牌
- 导入入口按钮视觉可调，**不得**改 `import/**` 弹窗（归 204）
- 草稿 `04-catalog-browse.png` + `ux-gap-drafts/TASK-WSC-203.md`
- browse 既有 Vitest + typecheck

## 派发记录

- runId: RUN-WSC-003
- wave: W2（∥ TASK-WSC-202）
- dispatchedAt: 2026-08-02T14:05:30+08:00
- identity: orchestrator（人类确认「派发 W2」）
- approvedPlan: planning/approved/PLAN-WSC-3.1.md
- dependsOnVerified: [TASK-WSC-201]
