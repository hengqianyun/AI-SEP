# 任务包 TASK-WSC-106

```yaml
taskId: TASK-WSC-106
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: DISPATCHED
wave: W4
parallelWith:
  - TASK-WSC-105
goal: |
  目录维护页：全部/已维护/待关联；一二三级筛选；分页（page/pageSize/total）；
  单条维护；批量关联。
reqs:
  - REQ-CAT-007
dependsOn:
  - TASK-WSC-103
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/sql/**"
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/import/**
  - backend/app/*/src/main/java/**/catalog/browse/**
  - backend/app/*/src/main/java/**/catalog/detail/**
  - backend/app/*/src/main/java/**/catalog/editor/**
  - backend/app/*/src/main/java/**/catalog/admin/**
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
  - frontend/src/features/catalog/maintenance/**
  - backend/app/*/src/main/java/**/catalog/maintenance/**
  - 对应测试
riskTags: []
actorInstance: developer-wsc-106
codeReviewerMustDiffer: true
mergeConstraint: 禁止与 TASK-WSC-105 混写同一 PR
```

## 验收要点

见 `planning/approved/PLAN-WSC-2.2.md` § TASK-WSC-106。

- 状态页签与筛选；单条保存/取消；批量栏数量与批量保存
- 提供方/普通用户不可达；分页字段同 Browse 约定
- testScope：待关联→已维护；批量关联；非管理员 403；分页

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-31T17:11:00+08:00
- wave: W4 并行（∥ TASK-WSC-105；依赖 103 VERIFIED，可与 105 并行）
- dependsOnVerified: TASK-WSC-103
