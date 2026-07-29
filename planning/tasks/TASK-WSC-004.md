# 任务包 TASK-WSC-004

```yaml
taskId: TASK-WSC-004
planId: PLAN-WSC-1.1
snapshotId: SNAP-WSC-001
status: DISPATCHED
wave: W3
goal: |
  三栏目录浏览：一级行业索引、二级产品列表、右侧预览；多维筛选与搜索；
  预览入口详情/上链/编辑（按权限）；普通用户无编辑按钮。
reqs: [REQ-CAT-001, REQ-CAT-002, REQ-CAT-003]
allowModify:
  - frontend/src/features/catalog/browse/**
  - backend/src/main/java/**/catalog/browse/**
  - backend/src/test/java/**/catalog/browse/**
  - frontend/src/router/routes.ts
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/db/migration/**"
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/catalog/admin/**
  - backend/src/main/java/**/catalog/detail/**
  - backend/src/main/java/**/catalog/editor/**
  - backend/src/main/java/**/catalog/admin/**
  - frontend/src/features/overview/**
  - frontend/src/features/chain/**
  - frontend/src/features/auth/**
  - frontend/src/features/shell/**
  - backend/src/main/java/**/overview/**
  - backend/src/main/java/**/chain/**
  - ai/**
  - product/**
  - planning/**
dependsOn: [TASK-WSC-002]
actorInstance: developer-wsc-004
codeReviewerMustDiffer: true
```

## SCOPE_AMEND

允许仅修改 `routes.ts` 中 **catalog**（列表页）路由的 `component`；不得实现 detail/editor/admin；不得改 overview/chain 路由。

## 验收 / testScope

见 PLAN-WSC-1.1 TASK-WSC-004。预览「详情/上链/编辑」可 `router.push` 到已有占位路由。

派发：2026-07-29，RUN-WSC-001 W3。
