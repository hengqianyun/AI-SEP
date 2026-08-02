# 任务包 TASK-WSC-204

```yaml
taskId: TASK-WSC-204
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: VERIFIED
wave: W3
verifiedAt: 2026-08-02T15:18:30+08:00
evidenceId: EVID-TASK-WSC-204-1
evidence: planning/tasks/TESTRUN-TASK-WSC-204.md
goal: |
  目录维护页签/筛选/批量条/表格分页层次对齐；分类维护路由页内等价 modal 壳（约 960px）
  与导入弹窗（约 520px）结构、遮罩、footer 主次按钮层级对齐；导入四态与 §3.5 白名单不回退。
reqs:
  - REQ-UX-005
  - REQ-UX-006
  - REQ-UX-009
allowModify: writeSet（见下）
denyModify:
  - frontend/src/styles/**
  - frontend/src/theme/**
  - frontend/src/layouts/**
  - frontend/src/features/shell/**
  - frontend/src/router/**
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/overview/**
  - frontend/src/features/chain/**
  - frontend/src/features/auth/**
  - contracts/**
  - frontend/src/api/**
  - backend/**
  - tests/e2e/**
  - tests/fixtures/import/**
  - ai/runs/RUN-WSC-003/ux-gap-checklist.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/screenshots/**
  - ai/runs/RUN-WSC-003/ux-walkthrough/WALKTHROUGH.md
  - product/**
  - planning/**
  - ai/rules/**
readSet:
  - product/requirements/SNAP-WSC-003.md
  - design/prototypes/wsc-v1.1/index.html
  - planning/approved/PLAN-WSC-3.1.md（§3.4/§3.5 与本任务）
  - planning/approved/PLAN-WSC-2.2.md
  - frontend/src/styles/**（只读令牌）
  - frontend/src/features/catalog/maintenance/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/import/**
writeSet:
  - frontend/src/features/catalog/maintenance/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/import/**
  - frontend/src/features/catalog/maintenance/**/*.spec.ts
  - frontend/src/features/catalog/admin/**/*.spec.ts
  - frontend/src/features/catalog/import/**/*.spec.ts
  - ai/runs/RUN-WSC-003/ux-gap-drafts/TASK-WSC-204.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/05-catalog-maintenance.png
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/06-category-admin-modal.png
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/07-import-modal.png
dependsOn:
  - TASK-WSC-201
mergeAfter: []
riskTags:
  - handles-pii
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
actorInstance: developer-wsc-204
codeReviewerMustDiffer: true
parallelWith:
  - TASK-WSC-205
```

## 验收要点

见 `planning/approved/PLAN-WSC-3.1.md` § TASK-WSC-204 与 §3.4 / §3.5。

摘要：

- 维护页：全部/已维护/待关联 active；筛选/批量条；表格在白卡片内
- 分类维护载体 B：路由页内 modal 壳约 960px；与 overlay 偏差记差距草稿（非 P0）
- 导入弹窗约 520px；§3.4 四态可区分；§3.5 白名单（禁信用代码/ownerDID/明文哈希）
- 权限：结构不可达，禁纯 CSS 替代（§1.4）
- 草稿 05+06+07 + 差距草稿；maintenance/admin/import Vitest + typecheck

## 派发记录

- runId: RUN-WSC-003
- wave: W3（∥ TASK-WSC-205；203 已 VERIFIED，满足禁 203∥204）
- dispatchedAt: 2026-08-02T14:27:30+08:00
- identity: orchestrator（人类确认「派发 W3」）
- approvedPlan: planning/approved/PLAN-WSC-3.1.md
