# 任务包 TASK-WSC-107

```yaml
taskId: TASK-WSC-107
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
runId: RUN-WSC-002
status: DISPATCHED
wave: W5
goal: |
  xlsx/csv 导入（目录页弹窗）；模板下载；≤10MB；同步结果四态；
  错误报告；成功行可检索并上链。
reqs:
  - REQ-CAT-008
dependsOn:
  - TASK-WSC-105
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/sql/**"
  - frontend/src/features/catalog/browse/**
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/catalog/admin/**
  - frontend/src/features/catalog/maintenance/**
  - backend/app/*/src/main/java/**/catalog/browse/**
  - backend/app/*/src/main/java/**/catalog/detail/**
  - backend/app/*/src/main/java/**/catalog/editor/**
  - backend/app/*/src/main/java/**/catalog/admin/**
  - backend/app/*/src/main/java/**/catalog/maintenance/**
  - backend/app/*/src/main/java/**/chain/**
  - ai/rules/**
  - product/**
  - planning/**
allowModify: |
  仅 writeSet；通过 DI 只读调用已有产品写入/存证端口（不得修改其实现目录）
readSet:
  - contracts/**（只读）
  - frontend/src/api/**（只读）
  - product/requirements/SNAP-WSC-002.md
  - planning/approved/PLAN-WSC-2.2.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
writeSet:
  - frontend/src/features/catalog/import/**
  - backend/app/*/src/main/java/**/catalog/import/**
  - 对应测试与 tests/fixtures/import/**
riskTags:
  - handles-pii
actorInstance: developer-wsc-107
codeReviewerMustDiffer: true
```

## 验收要点

见 `planning/approved/PLAN-WSC-2.2.md` § TASK-WSC-107。

- §4 权限；目录页弹窗入口；关闭后回到目录浏览
- ≤10MB；同步导入结果 DTO；§3.6 四态可区分
- 部分成功：计数 + 错误报告；临时文件清理；报告字段白名单
- 报告下载：登录态 + 管理员/提供方；普通用户/未认证 403
- 成功行可检索并上链

## testScope（摘要）

- fixtures：`full-success` / `partial-success` / `code-conflict` / `category-mismatch` / `required-field-missing` / `oversize`
- 模板列齐全；CHAIN 字段；报告入口
- 错误报告白名单负例；临时文件清理证据；报告 GET 403；入口关闭回目录

> 波次级 E2E（`TESTRUN-WSC-E2E-V11`）在 **101..107 均 VERIFIED** 后由独立 tester 执行，**不在本任务 developer 范围内**。

## 派发记录

- runId: RUN-WSC-002
- dispatchedAt: 2026-07-31T17:58:00+08:00
- wave: W5
- dependsOnVerified: TASK-WSC-105
- confirmedBy: human/CONFIRM_TASK_DISPATCH
