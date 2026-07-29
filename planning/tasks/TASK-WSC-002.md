# 任务包 TASK-WSC-002

```yaml
taskId: TASK-WSC-002
planId: PLAN-WSC-1.1
snapshotId: SNAP-WSC-001
status: DISPATCHED
wave: W2
goal: |
  实现 §3.2 会话鉴权；三角色写入口/写 API 403；固定侧栏与未开放菜单；
  角色变更后写入口立即更新且旧凭证写 API 拒绝；最低审计事件（登录失败、403）。
reqs:
  - REQ-RBAC-001
  - REQ-SHELL-001
allowModify:
  - frontend/src/features/auth/**
  - frontend/src/features/shell/**
  - frontend/src/layouts/**
  - frontend/src/App.vue
  - frontend/src/main.ts
  - frontend/src/router/index.ts
  - backend/src/main/java/**/rbac/**
  - backend/src/main/java/**/security/**
  - backend/src/test/java/**/rbac/**
  - backend/src/test/java/**/security/**
  - frontend/src/features/auth/**/*.spec.ts
  - frontend/src/features/shell/**/*.spec.ts
denyModify:
  - contracts/**
  - frontend/src/api/**
  - frontend/src/router/routes.ts
  - "**/db/migration/**"
  - frontend/src/features/overview/**
  - frontend/src/features/catalog/**
  - frontend/src/features/chain/**
  - backend/src/main/java/**/overview/**
  - backend/src/main/java/**/catalog/**
  - backend/src/main/java/**/chain/**
  - backend/pom.xml
  - ai/**
  - product/**
  - planning/**
readSet:
  - contracts/**
  - contracts/rbac/**
  - frontend/src/api/**
  - frontend/src/router/**
  - product/requirements/SNAP-WSC-001.md
  - planning/approved/PLAN-WSC-1.1.md
  - design/decisions/DEC-WSC-004.md
writeSet:
  - frontend/src/features/auth/**
  - frontend/src/features/shell/**
  - frontend/src/layouts/**
  - backend/src/main/java/**/rbac/**
  - backend/src/main/java/**/security/**
dependsOn:
  - TASK-WSC-001
riskTags: []
frozenContracts:
  - wsc-contracts@1.1.0
actorInstance: developer-wsc-002
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-1.1.md` TASK-WSC-002 与 §3.2、§4.1、§6.1。

## Orchestrator 范围修订（SCOPE_AMEND）

为使壳层可验收，允许修改 `App.vue` / `main.ts` / `router/index.ts`（路由守卫接线）。
**禁止**修改 `routes.ts`（001 契约路由清单）与 `frontend/src/api/**`、`backend/pom.xml`。
鉴权实现采用 Spring MVC 会话 + Interceptor（不新增 spring-security 依赖，避免改 pom）。

## 派发记录

- runId: RUN-WSC-001
- dispatchedAt: 2026-07-28T17:32:00+08:00
- after: TASK-WSC-001 VERIFIED；JDK 17 commit 59f5948
