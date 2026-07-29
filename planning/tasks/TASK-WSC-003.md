# 任务包 TASK-WSC-003

```yaml
taskId: TASK-WSC-003
planId: PLAN-WSC-1.1
snapshotId: SNAP-WSC-001
status: DISPATCHED
wave: W3
goal: |
  实现总览核心指标、近30天趋势、TOP10、最新上链流（默认10）、行业/地域分布；
  消费 OVW 动态流契约；§3.5 loading/empty/error。
reqs: [REQ-OVW-001, REQ-OVW-002, REQ-OVW-003, REQ-OVW-004, REQ-OVW-005]
allowModify:
  - frontend/src/features/overview/**
  - backend/src/main/java/**/overview/**
  - backend/src/test/java/**/overview/**
  - frontend/src/router/routes.ts
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/db/migration/**"
  - frontend/src/features/auth/**
  - frontend/src/features/shell/**
  - frontend/src/features/catalog/**
  - frontend/src/features/chain/**
  - frontend/src/layouts/**
  - backend/src/main/java/**/rbac/**
  - backend/src/main/java/**/security/**
  - backend/src/main/java/**/catalog/**
  - backend/src/main/java/**/chain/**
  - ai/**
  - product/**
  - planning/**
dependsOn: [TASK-WSC-002]
actorInstance: developer-wsc-003
codeReviewerMustDiffer: true
```

## SCOPE_AMEND

允许仅修改 `frontend/src/router/routes.ts` 中 **overview** 路由的 `component` 指向本 feature 页面；不得改其他路由。

## 验收 / testScope

见 `planning/approved/PLAN-WSC-1.1.md` TASK-WSC-003。

派发：2026-07-29，RUN-WSC-001 W3。
