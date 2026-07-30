# Round 1 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC2-R1-solutionArchitect
planId: PLAN-WSC-2.0
round: 1
role: solutionArchitect
snapshotIdAtReview: SNAP-WSC-002
decision: REQUEST_CHANGES
summary: |
  DEC-WSC-005 / LAYOUT 多模块路径总体正确；DAG 无环；W3 admin∥browse 互斥清楚。
  主要缺口：TASK-WSC-101 writeSet 为 backend/** 过宽，骨架迁移期间可误改 feature；107 denyModify「除非声明」不可机械 scope-check。
```

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SA-R1-001 | P1 | TASK-WSC-101 `writeSet: backend/**` 允许任意改业务包，与「禁止实现 feature 业务页」仅靠叙述约束，Orchestrator 无法字面判定。 | 将 101 writeSet 收敛为可枚举路径（如 `backend/pom.xml`、`backend/app/*/pom.xml`、`backend/app/*/src/main/resources/**`、脚手架 Java 启动类/配置包、允许迁移期移动清单），并 `denyModify` 显式禁止 `**/features/**` 与未声明的 catalog/overview 业务实现；若必须搬迁代码，列出允许搬迁的源→目标表。 | 全部（底座） |
| ISSUE-SA-R1-002 | P1 | TASK-WSC-107 `denyModify` 含「除非任务声明只读调用」例外，scope check 无法执行。 | 改为：107 仅写 import 路径；通过 DI 调用已有 editor/chain **端口**，`denyModify` 列出禁止修改的实现目录且无叙述例外。 | REQ-CAT-008 |
