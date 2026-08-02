# 任务包 TASK-WSC-107-FIX1

```yaml
taskId: TASK-WSC-107
fixId: FIX1
bugId: BUG-WSC-E2E-001
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: DISPATCHED
wave: W5
goal: |
  关闭 BUG-WSC-E2E-001：目录浏览页按 INTEGRATION.md 挂载 ImportDialog
  （?import=1 + 关闭清 query）；补齐错误报告可下载/打开（FIND-002），
  使 §6.1 场景 4 E2E 可过。
reqs:
  - REQ-CAT-008
dependsOn:
  - TASK-WSC-107
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/sql/**"
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/maintenance/**
  - backend/**
  - ai/rules/**
  - product/**
  - planning/**
  - tests/e2e/**
allowModify: |
  仅 writeSet；最小改动关闭 BUG closeWhen
readSet:
  - planning/tasks/BUG-WSC-E2E-001.md
  - planning/tasks/TESTRUN-WSC-E2E-V11.md
  - planning/tasks/REV-TASK-WSC-107.md
  - planning/tasks/DEV-TASK-WSC-107.md
  - frontend/src/features/catalog/import/INTEGRATION.md
  - planning/approved/PLAN-WSC-2.2.md
  - contracts/**（只读）
writeSet:
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/import/**
  - 对应前端单测
riskTags:
  - handles-pii
actorInstance: developer-wsc-e2e-fix1
codeReviewerMustDiffer: true
```

## 验收要点（closeWhen）

1. `CatalogBrowsePage` 挂载 `ImportDialog`；`?import=1` 打开；关闭清除 query 回目录表面
2. 错误报告 UI 可实际下载或打开（对齐 FIND-WSC-107-R1-002）
3. 自测：相关 vitest；说明如何手测/供 E2E 复跑场景 4
4. 不标 BUG CLOSED / 不标 VERIFIED（关闭权在独立 tester 复跑 E2E）

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-31T23:17:30+08:00
- confirmedBy: human/CONFIRM_FIX_DISPATCH (productOwner)
- bugId: BUG-WSC-E2E-001
