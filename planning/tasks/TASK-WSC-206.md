# 任务包 TASK-WSC-206

```yaml
taskId: TASK-WSC-206
planId: PLAN-WSC-3.1
snapshotId: SNAP-WSC-003
runId: RUN-WSC-003
status: VERIFIED
wave: W4
verifiedAt: 2026-08-02T16:46:30+08:00
evidenceId: TESTRUN-WSC-E2E-V12-UX
evidence: planning/tasks/TESTRUN-TASK-WSC-206.md
goal: |
  合并分波草稿、对照原型完成主路径差距清单闭环；归档正式截图与走查记录；
  复用扩展既有 E2E 证明无 UX 引入的功能回归；确认无 P0「完全不像原型」页面。
reqs:
  - REQ-UX-011
  - REQ-UX-009
  - REQ-UX-010
allowModify: writeSet（见下；字面路径）
denyModify:
  - contracts/**
  - frontend/src/api/**
  - backend/**
  - "**/sql/**"
  - frontend/src/styles/**
  - frontend/src/theme/**
  - frontend/src/features/**
  - frontend/src/App.vue
  - frontend/src/main.ts
  - frontend/src/layouts/**
  - frontend/src/router/**
  - product/**
  - planning/**
  - ai/rules/**
readSet:
  - product/requirements/SNAP-WSC-003.md
  - design/prototypes/wsc-v1.1/index.html
  - planning/approved/PLAN-WSC-3.1.md（§3.1–§3.5 与本任务）
  - frontend/src/**（只读对照）
  - tests/e2e/**
  - ai/runs/RUN-WSC-003/ux-gap-drafts/**
  - ai/runs/RUN-WSC-003/ux-walkthrough/drafts/**
writeSet:
  - ai/runs/RUN-WSC-003/ux-gap-checklist.md
  - ai/runs/RUN-WSC-003/ux-walkthrough/**
  - tests/e2e/specs/p0-wsc-v1.1.spec.ts
  - tests/e2e/specs/**（仅为本规格扩展断言所必需且须在 DEV 列出）
  - tests/e2e/playwright.config.ts
  - tests/e2e/reports/p0-wsc-v1.2-ux/**
dependsOn:
  - TASK-WSC-201
  - TASK-WSC-202
  - TASK-WSC-203
  - TASK-WSC-204
  - TASK-WSC-205
mergeAfter: []
riskTags:
  - release-gate
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
evidenceIdTarget: TESTRUN-WSC-E2E-V12-UX
actorInstance: developer-wsc-206
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-3.1.md` § TASK-WSC-206 与 §3.1–§3.2。

摘要：

- 正式差距清单 §3.1 schema；截图 01–10 每项至少一行；P0=0 或已关闭有证据；载体 B 偏差若保留则非 P0
- 正式截图 `ux-walkthrough/screenshots/` 齐全（脱敏）；`WALKTHROUGH.md` 含 PO/UX 确认栏（可先留待人类签署）
- 视口 1280×800 与 1440×900 破坏性检查已勾选
- E2E：扩展 `p0-wsc-v1.1.spec.ts`（含三角色矩阵）；选择器适配优先改 e2e，**禁止**改 feature 源码；若必须改生产 DOM → 升级 Orchestrator 插入 HOTFIX
- DEV 侧准备规格与（可选）试跑；正式门禁证据由独立 tester 执行并落盘 `TESTRUN-WSC-E2E-V12-UX`

## 派发记录

- runId: RUN-WSC-003
- wave: W4
- dispatchedAt: 2026-08-02T15:19:00+08:00
- identity: orchestrator（人类确认「派发 W4」）
- approvedPlan: planning/approved/PLAN-WSC-3.1.md
- dependsOnVerified: [TASK-WSC-201 .. TASK-WSC-205]
