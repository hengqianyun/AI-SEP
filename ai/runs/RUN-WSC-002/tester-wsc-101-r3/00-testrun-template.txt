# 测试证据（复测）

```yaml
evidenceId: EVID-TASK-WSC-001-2
taskId: TASK-WSC-001
planId: PLAN-WSC-1.1
actorInstance: tester-wsc-001-retest
contracts: wsc-contracts@1.1.0
supersedes: EVID-TASK-WSC-001-1
basedOn:
  - planning/tasks/TASK-WSC-001.md
  - design/decisions/DEC-WSC-004.md
  - planning/tasks/REV-TASK-WSC-001.md
reviewDecision: APPROVE
executedAt: 2026-07-28T17:30:00+08:00
status: PASSED
```

## 总体结论

**PASSED** — 在 **DEC-WSC-004**（后端基线改为 Java 17）落地后，复跑后端 compile **PASSED**；契约机检与既有前端 typecheck/build 证据仍有效。

残差（不阻塞本脚手架任务 VERIFIED）：空库 Flyway migrate / 运行时快照读写需 PostgreSQL，记入后续环境冒烟（W2+），本证据不将其伪造成 PASSED。

## Layers

| name | command | result | notes |
|---|---|---|---|
| contract-check | `node tests/contracts/check-contracts.mjs` | PASSED | 复跑 exit 0 |
| backend-compile | `mvn -f backend/pom.xml -DskipTests compile` | PASSED | `javac release 17`；JDK 17.0.11；BUILD SUCCESS |
| frontend-typecheck/build | （沿用 EVID-1） | PASSED | 未回归变更 |
| flyway-file-assert | 读 SQL | PASSED | 沿用 EVID-1 |
| flyway-empty-db-apply | migrate 空库 | DEFERRED | 无本地 PG；非 JDK 问题 |
| e2e-p0 | — | SKIPPED | 无业务 UI |

## 环境快照

| 项 | 值 |
|---|---|
| Java | 17.0.11（`C:\Program Files\Java\jdk-17`） |
| Maven | 3.6.3（绑定 JDK 17） |
| 后端要求 | Spring Boot 3.3.x + **Java 17**（DEC-WSC-004 / pom / RULE-ORG-STACK） |

## 决策权声明

- 未把 Flyway 空库未执行记为 PASSED。
- JDK 基线变更由 techLead 人类确认（DEC-WSC-004）。
