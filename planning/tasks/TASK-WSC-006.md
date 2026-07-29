# 任务包 TASK-WSC-006

```yaml
taskId: TASK-WSC-006
planId: PLAN-WSC-1.1
snapshotId: SNAP-WSC-001
status: DISPATCHED
wave: W3
goal: |
  可替换模拟存证适配层；按产品展示上链版本列表与选中版本目录快照；
  字段 metadataHash/ownerDID/timestamp/certificate.owner；空态与敏感标识复制。
reqs: [REQ-CHAIN-001]
allowModify:
  - frontend/src/features/chain/**
  - backend/src/main/java/**/chain/**
  - backend/src/test/java/**/chain/**
  - frontend/src/router/routes.ts
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/db/migration/**"
  - backend/src/main/java/**/catalog/**
  - frontend/src/features/catalog/**
  - frontend/src/features/overview/**
  - frontend/src/features/auth/**
  - frontend/src/features/shell/**
  - backend/src/main/java/**/overview/**
  - backend/src/main/java/**/rbac/**
  - ai/**
  - product/**
  - planning/**
dependsOn: [TASK-WSC-002]
actorInstance: developer-wsc-006
codeReviewerMustDiffer: true
```

## SCOPE_AMEND

允许仅修改 `routes.ts` 中 **chain** 路由的 `component`。

## 验收 / testScope

见 PLAN-WSC-1.1 TASK-WSC-006 与 DEC-WSC-002。实现 `ChainAttestationPort` 的模拟实现供后续 005 注入。

派发：2026-07-29，RUN-WSC-001 W3。
