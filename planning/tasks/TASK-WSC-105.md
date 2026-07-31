# 任务包 TASK-WSC-105

```yaml
taskId: TASK-WSC-105
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: DISPATCHED
wave: W4
parallelWith:
  - TASK-WSC-106
goal: |
  详情/编辑分类选择器为三级；提交仍产生存证版本；上链快照展示三级路径；
  回归 OQ-004/DEC-001/002。
reqs:
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-CHAIN-001
dependsOn:
  - TASK-WSC-103
  - TASK-WSC-104
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/sql/**"
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/maintenance/**
  - frontend/src/features/catalog/import/**
  - backend/app/*/src/main/java/**/catalog/browse/**
  - backend/app/*/src/main/java/**/catalog/admin/**
  - backend/app/*/src/main/java/**/catalog/maintenance/**
  - backend/app/*/src/main/java/**/catalog/import/**
  - frontend/src/features/overview/**
  - backend/app/*/src/main/java/**/overview/**
  - ai/rules/**
  - product/**
  - planning/**
readSet:
  - contracts/**（只读）
  - frontend/src/api/**（只读）
  - product/requirements/SNAP-WSC-002.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
  - planning/approved/PLAN-WSC-2.2.md
writeSet:
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/chain/**
  - backend/app/*/src/main/java/**/catalog/detail/**
  - backend/app/*/src/main/java/**/catalog/editor/**
  - backend/app/*/src/main/java/**/chain/**
  - 对应测试
riskTags:
  - handles-pii
actorInstance: developer-wsc-105
codeReviewerMustDiffer: true
mergeConstraint: 禁止与 TASK-WSC-106 混写同一 PR
```

## 验收要点

见 `planning/approved/PLAN-WSC-2.2.md` § TASK-WSC-105。

- 三级路径展示/选择；新建 v1 / 编辑递增；CHAIN 快照含三级；普通用户无编辑
- testScope：编码冲突；版本递增；适配失败无半成品；快照按 versionId；§4 产品写矩阵回归

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-31T17:11:00+08:00
- wave: W4 并行（∥ TASK-WSC-106）
- dependsOnVerified: TASK-WSC-103, TASK-WSC-104
