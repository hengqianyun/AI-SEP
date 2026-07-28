# 任务包 TASK-WSC-001

```yaml
taskId: TASK-WSC-001
planId: PLAN-WSC-1.1
snapshotId: SNAP-WSC-001
status: DISPATCHED
wave: W1
goal: |
  创建可构建的前后端绿地工程，并一次性冻结 wsc-contracts@1.1.0：
  OpenAPI、RBAC、Flyway（含版本化目录快照）、路由、存证接口、
  OVW 流、错误码、OQ-004、认证 scheme、frontend/src/api、
  UI 状态矩阵引用、敏感字段表、ops 回滚 runbook、Playwright 脚手架。
  禁止实现业务 feature 模块（overview/catalog/chain/auth/shell 业务逻辑）。
reqs:
  - REQ-SHELL-001
  - REQ-RBAC-001
  - REQ-OVW-001
  - REQ-OVW-002
  - REQ-OVW-003
  - REQ-OVW-004
  - REQ-OVW-005
  - REQ-CAT-001
  - REQ-CAT-002
  - REQ-CAT-003
  - REQ-CAT-004
  - REQ-CAT-005
  - REQ-CAT-006
  - REQ-CHAIN-001
allowModify:
  - frontend/**
  - backend/**
  - contracts/**
  - tests/**
  - ops/runbooks/**
  - package.json
  - pnpm-workspace.yaml
  - .nvmrc
  - .gitignore
denyModify:
  - ai/rules/**
  - product/**
  - planning/**
  - "**/.env"
  - "**/application-local.yml"
  - "**/application-prod.yml"
readSet:
  - product/requirements/SNAP-WSC-001.md
  - product/prd/wsc-v1.0.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
  - design/decisions/DEC-WSC-003.md
  - ai/rules/organization/RULE-ORG-STACK.md
  - ai/rules/project/RULE-PROJECT-LAYOUT.md
  - ai/rules/project/modules.yaml
  - planning/approved/PLAN-WSC-1.1.md
writeSet:
  - frontend/
  - frontend/src/api/**
  - backend/
  - contracts/**
  - backend/src/main/resources/db/migration/**
  - frontend/src/router/**
  - tests/
  - ops/runbooks/
dependsOn: []
mergeAfter: []
riskTags:
  - schema-migration
frozenContracts:
  - wsc-contracts@1.1.0
actorInstance: developer-wsc-001
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-1.1.md` § TASK-WSC-001 `acceptance` / `testScope` / §3.1–3.7。

## 派发记录

- runId: RUN-WSC-001
- dispatchedAt: 2026-07-28T16:42:00+08:00
- identity: orchestrator（人类确认「继续」后派发）
