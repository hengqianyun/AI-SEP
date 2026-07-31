# 任务包 TASK-WSC-104

```yaml
taskId: TASK-WSC-104
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: VERIFIED
wave: W3
parallelWith:
  - TASK-WSC-103
verifiedAt: 2026-07-31T17:07:00+08:00
evidenceId: EVID-TASK-WSC-104-1
evidence: planning/tasks/TESTRUN-TASK-WSC-104.md
goal: |
  顶部空间/行业标签；子类分组列表；预览与三级路径；筛选对齐三级；
  滚动加载更多（page/pageSize/total）。
reqs:
  - REQ-CAT-001
  - REQ-CAT-002
  - REQ-CAT-003
dependsOn:
  - TASK-WSC-102
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/sql/**"
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/catalog/maintenance/**
  - frontend/src/features/catalog/import/**
  - backend/app/*/src/main/java/**/catalog/admin/**
  - backend/app/*/src/main/java/**/catalog/detail/**
  - backend/app/*/src/main/java/**/catalog/editor/**
  - backend/app/*/src/main/java/**/catalog/maintenance/**
  - backend/app/*/src/main/java/**/catalog/import/**
  - ai/rules/**
  - product/**
  - planning/**
readSet:
  - contracts/**（只读）
  - frontend/src/api/**（只读）
  - product/requirements/SNAP-WSC-002.md
  - planning/approved/PLAN-WSC-2.2.md
writeSet:
  - frontend/src/features/catalog/browse/**
  - backend/app/*/src/main/java/**/catalog/browse/**
  - 对应测试
riskTags: []
actorInstance: developer-wsc-104
codeReviewerMustDiffer: true
mergeConstraint: 禁止与 TASK-WSC-103 混写同一 PR
```

## 验收要点

见 `planning/approved/PLAN-WSC-2.2.md` § TASK-WSC-104。

- 标签切换更新产品集；分节字段符合 SNAP
- 滚动加载有 loading 且保留滚动位置与筛选；分页字段 `page`/`pageSize`/`total`
- testScope：标签切换；筛选空态；滚动加载；预览权限入口；筛选 → load-more 仍满足筛选

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-31T16:04:00+08:00
- wave: W3 并行（∥ TASK-WSC-103）
- dependsOnVerified: TASK-WSC-102
