# 任务包 TASK-WSC-205

```yaml
taskId: TASK-WSC-205
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: VERIFIED
wave: W3
verifiedAt: 2026-08-02T15:16:30+08:00
evidenceId: EVID-TASK-WSC-205-1
evidence: planning/tasks/TESTRUN-TASK-WSC-205.md
goal: |
  详情字段分组卡片、编辑表单分区与主次按钮、上链版本列表卡+快照卡双栏层次对齐原型；
  三级分类路径可读；写路径行为不改。交付草稿截图 08+09+10 与差距草稿。
reqs:
  - REQ-UX-007
  - REQ-UX-009
allowModify: writeSet（见下）
denyModify:
  - frontend/src/styles/**
  - frontend/src/theme/**
  - frontend/src/layouts/**
  - frontend/src/features/shell/**
  - frontend/src/router/**
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/maintenance/**
  - frontend/src/features/catalog/import/**
  - frontend/src/features/overview/**
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
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/chain/**
writeSet:
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/chain/**
  - frontend/src/features/catalog/detail/**/*.spec.ts
  - frontend/src/features/catalog/editor/**/*.spec.ts
  - frontend/src/features/chain/**/*.spec.ts
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-205.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/08-product-detail.png
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/09-product-editor.png
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/10-chain.png
dependsOn:
  - TASK-WSC-201
mergeAfter: []
riskTags:
  - handles-pii
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
actorInstance: developer-wsc-205
codeReviewerMustDiffer: true
parallelWith:
  - TASK-WSC-204
```

## 验收要点

见 `planning/approved/PLAN-WSC-3.1.md` § TASK-WSC-205。

摘要：

- 详情：页头、字段分组卡片、只读层次
- 编辑：表单分区、主次按钮；普通用户无编辑入口（结构不可达）
- 上链：版本列表卡 + 快照卡双栏；三级路径可读
- 敏感字段截断不回退；空态/页级反馈用共享令牌
- 草稿 08+09+10 + 差距草稿；detail/editor/chain Vitest + typecheck

## 派发记录

- runId: RUN-WSC-003
- wave: W3（∥ TASK-WSC-204）
- dispatchedAt: 2026-08-02T14:27:30+08:00
- identity: orchestrator（人类确认「派发 W3」）
- approvedPlan: planning/approved/PLAN-WSC-3.1.md
