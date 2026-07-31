# 任务包 TASK-WSC-102

```yaml
taskId: TASK-WSC-102
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: VERIFIED
wave: W2
verifiedAt: 2026-07-31T16:03:00+08:00
evidenceId: EVID-TASK-WSC-102-1
evidence: planning/tasks/TESTRUN-TASK-WSC-102.md
goal: |
  实现/保持 §3.3 会话鉴权；管理员侧栏「目录维护」；占位菜单未开放；
  三角色写入口含批量导入可见性；写 API 403 对齐 §4；
  角色变更后目录维护与导入入口立即更新且旧会话写 API 拒绝。
reqs:
  - REQ-SHELL-001
  - REQ-RBAC-001
dependsOn:
  - TASK-WSC-101
allowModify: writeSet（见下）
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/sql/**"
  - frontend/src/features/catalog/**
  - frontend/src/features/overview/**
  - frontend/src/features/chain/**
  - backend/app/*/src/main/java/**/catalog/**
  - backend/app/*/src/main/java/**/overview/**
  - backend/app/*/src/main/java/**/chain/**
  - ai/rules/**
  - product/**
  - planning/**
  - "**/.env"
  - "**/application-local.yml"
  - "**/application-prod.yml"
readSet:
  - contracts/**
  - frontend/src/api/**（只读）
  - product/requirements/SNAP-WSC-002.md
  - planning/approved/PLAN-WSC-2.2.md（§3.3 / §4）
  - planning/tasks/TASK-WSC-101.md
writeSet:
  - frontend/src/features/shell/**
  - frontend/src/features/auth/**
  - frontend/src/layouts/**
  - backend/app/*/src/main/java/**/rbac/**
  - backend/app/*/src/main/java/**/security/**
  - 对应单元/集成测试（与上述路径对称的 **/src/test/** 或 frontend/**/__tests__/**）
riskTags: []
actorInstance: developer-wsc-102
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-2.2.md` § TASK-WSC-102 `acceptance` / `testScope` 与 §3.3、§4。

摘要：

- §4 RBAC 矩阵壳层侧通过
- 角色切换后目录维护与导入入口立即更新
- 登出或角色/权限变更后，分类维护/目录维护/导入等写 API 拒绝（ISSUE-SEC-R1-001）
- testScope：§4 矩阵（壳层+过滤器）；非管理员不可见目录维护；至少一条「登出或角色变更后写 API 拒绝」可测用例

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-31T15:16:00+08:00
- identity: orchestrator（人类确认「继续」= `CONFIRM_TASK_DISPATCH` W2）
- dependsOnVerified: TASK-WSC-101（EVID-TASK-WSC-101-4）
- approvedPlan: planning/approved/PLAN-WSC-2.2.md
