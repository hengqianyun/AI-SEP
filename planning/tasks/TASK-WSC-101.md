# 任务包 TASK-WSC-101

```yaml
taskId: TASK-WSC-101
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: VERIFIED
wave: W1
verifiedAt: 2026-07-31T15:15:00+08:00
evidenceId: EVID-TASK-WSC-101-4
evidence: planning/tasks/TESTRUN-TASK-WSC-101.md
goal: |
  将后端迁至 backend/app/<service>/（Boot 2.7.18 / MySQL / Redis / Flyway sql 布局）；
  冻结 wsc-contracts@2.0.0（含 PLAN-WSC-2.2 §3.1 全部条目）；生成 client；
  空库 init→migration 可启动；文档化相对 V1.0 schema 迁移/重建策略；
  overview 前后端最小适配至可编译启动（仅 schema/契约对齐，禁止新业务指标）。
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
  - REQ-CAT-007
  - REQ-CAT-008
  - REQ-CHAIN-001
allowModify: writeSet ∪ migrateAllowlist（见下；禁止实现 catalog/shell/auth/chain 业务页；overview 除外且仅最小适配）
denyModify:
  - ai/rules/**
  - product/**
  - planning/**
  - "**/.env"
  - "**/application-local.yml"
  - "**/application-prod.yml"
  - frontend/src/features/catalog/**
  - frontend/src/features/shell/**
  - frontend/src/features/auth/**
  - frontend/src/features/chain/**
  - "向 **/db/migration/** 追加新版本脚本"
readSet:
  - product/requirements/SNAP-WSC-002.md
  - product/prd/wsc-v1.1.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
  - design/decisions/DEC-WSC-003.md
  - design/decisions/DEC-WSC-004.md
  - design/decisions/DEC-WSC-005.md
  - ai/rules/organization/RULE-ORG-STACK.md
  - ai/rules/project/RULE-PROJECT-LAYOUT.md
  - contracts/**（只读对照既有）
  - planning/approved/PLAN-WSC-2.2.md
  - planning/approved/PLAN-WSC-1.1.md（§3.2 会话 scheme 对照）
writeSet:
  - backend/pom.xml
  - backend/app/*/pom.xml
  - backend/app/*/src/main/resources/**
  - backend/app/*/src/main/java/**/WscApplication.java
  - backend/app/*/src/main/java/**/config/**
  - backend/app/*/src/main/java/**/common/**
  - backend/app/*/src/main/java/**/overview/**
  - backend/app/*/src/test/**
  - frontend/src/features/overview/**
  - contracts/**
  - frontend/src/api/**
  - frontend/src/router/**
  - ops/runbooks/**
  - frontend/package.json
  - frontend/vite.config.*
  - frontend/tsconfig*.json
  - frontend/pnpm-lock.yaml
  - tests/e2e/playwright.config.ts
  - tests/e2e/**/*.ts（仅脚手架；禁止业务断言冒充 VERIFIED）
migrateAllowlist:
  moveFrom: backend/src/main/java/com/wsc/**
  moveTo: backend/app/*/src/main/java/com/wsc/**
  retireFlat: backend/src/**
  retireOldMigrations: backend/src/main/resources/db/migration/**
dependsOn: []
mergeAfter: []
riskTags:
  - schema-migration
frozenContracts:
  - wsc-contracts@2.0.0
actorInstance: developer-wsc-101
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-2.2.md` § TASK-WSC-101 `acceptance` / `testScope` 与 §3.1–3.4。

摘要：

- `/health`、`/doc.html` 可达；空库走 `sql/init`→`sql/migration`
- 契约含三级/维护/导入同步 DTO；Browse 字段名为 `page`/`pageSize`/`total`；维护条目≡产品
- 导入：模板列最小集 + 三级分类列语义；请求级 vs 行级错误示例
- overview 可编译启动；禁止新脚本入 `db/migration/`；旧扁平源码与 `db/migration` 不得再作权威
- 17 REQ 可映射到端点或约束；overview 只读 smoke（metrics + 最新流或空态友好）

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-30T16:35:00+08:00
- identity: orchestrator（人类确认「继续」= `CONFIRM_TASK_DISPATCH` 后派发）
- approvedPlan: planning/approved/PLAN-WSC-2.2.md
