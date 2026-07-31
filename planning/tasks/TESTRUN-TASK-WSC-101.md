# 测试证据（更新）

`yaml
evidenceId: EVID-TASK-WSC-101-4
taskId: TASK-WSC-101
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-101-r6
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/BUG-WSC-101-001.md
  - planning/tasks/DEV-TASK-WSC-101-FIX1.md
  - planning/tasks/REV-TASK-WSC-101-FIX1.md
  - planning/tasks/TESTRUN-TASK-WSC-101.md
  - evidenceId: EVID-TASK-WSC-101-3
reviewDecision: APPROVE
executedAt: 2026-07-31T14:55:00+08:00
status: PASSED
supersedes: EVID-TASK-WSC-101-3
`

## 结论摘要

**PASSED** — FIX1 (lyway-mysql) 复测关闭 BUG-WSC-101-001。空库 sql/init + profile local 启动：Flyway 识别 MySQL 8.0，成功应用 3 个 migration（含 V202607301600__init_wsc_v11）；无 Unsupported Database。/health 与 /doc.html 可达（200）。合同 pin 1.1.0 仍可 DEFERRED（本轮未重跑 pin 门禁）。

- 模块路径：ackend/app/data-chain-service（原 wsc-service 已迁入）；jar 含 lyway-mysql-8.5.13.jar
- MySQL auth PASSED（gitignored pplication-local.yml；密码未落盘）
- Redis option B 58.33.102.13:16379 OPEN；启动成功
- Overview：/api/v1/overview/* 可达但需会话（401）— 非 Flyway 回归阻塞
- 启动：mvn -f backend/app/data-chain-service/pom.xml spring-boot:run -Dspring-boot.run.profiles=local

证据目录：i/runs/RUN-WSC-002/tester-wsc-101-r6/

## Layers

| name | result | notes |
| --- | --- | --- |
| ports | PASSED | 00-ports.txt |
| pom-flyway-mysql | PASSED | 00-pom-flyway.txt, 00-flyway-tree.txt |
| local-yml | PASSED | 00-yml-shape.txt（密码脱敏） |
| mysql-auth | PASSED | 01-mysql-auth.txt |
| mysql-empty-db-init | PASSED | 02-mysql-init.txt, 02-mysql-post-boot.txt |
| spring-boot-local | PASSED | 04-spring-boot-local.txt, 04-flyway-ok.txt |
| health-doc | PASSED | 05-health.txt |
| overview-smoke-local | PASSED | 06-overview-smoke.txt（401 auth-gated） |
| contract-check | DEFERRED | pin 1.1.0 vs 2.0.0 |

## BUG closeWhen

| # | 条件 | 结果 |
|---|---|---|
| 1 | pom 增加与 Boot 一致的 flyway-mysql | PASSED |
| 2 | 空库 init + local 启动 Flyway 成功；/health、/doc.html 可达 | PASSED |
| 3 | 独立 tester 复测非 BLOCKED | PASSED（本证据） |

→ BUG-WSC-101-001 **CLOSED**

## 证据红线

- 未将环境/认证失败标为 PASSED；本轮 auth 与 Flyway 均为真实通过
- 未打印或提交明文密码；未提交 pplication-local.yml
- supersedes EVID-TASK-WSC-101-3
- 机器摘要：i/runs/RUN-WSC-002/tester-wsc-101-r6/summary.json
