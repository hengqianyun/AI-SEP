from pathlib import Path
text = """# 测试证据（更新）

```yaml
evidenceId: EVID-TASK-WSC-101-3
taskId: TASK-WSC-101
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-101-r5
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/TASK-WSC-101.md
  - planning/tasks/DEV-TASK-WSC-101.md
  - planning/tasks/REV-TASK-WSC-101.md
  - planning/tasks/TESTRUN-TASK-WSC-101.md
reviewDecision: APPROVE
executedAt: 2026-07-31T13:20:00+08:00
status: BLOCKED
supersedes: EVID-TASK-WSC-101-2
```

## 结论摘要

**BLOCKED** — MySQL auth 与 empty-db init 已通过（清除 EVID-TASK-WSC-101-2 的 1045）；profile `local` 已激活且能连上 MySQL；Flyway 在 migrate 阶段失败：`Unsupported Database: MySQL 8.0`（`flyway-core` 8.5.13 缺少 `flyway-mysql`）。未达到 Started，故 `/health`、`/doc.html`、overview smoke 未执行。不得标 PASSED。

- MySQL 127.0.0.1:3306 OPEN；Redis option B `58.33.102.13:16379` OPEN
- gitignored `application-local.yml`：user `root`，密码未记录；CLI 认证 PASSED（需去掉 YAML 双引号再喂给 MYSQL_PWD）
- 空库：`DROP DATABASE` + `00_create_database.sql` + `01_flyway_baseline.sql` PASSED；boot 后仍仅有 baseline，业务 migration 未应用
- 启动：需 `--spring.config.additional-location=.../src/main/resources/`（jar 内嵌 local yml 过期仍 1045，属打包陈旧，非现网凭据错误）
- `check-contracts.mjs` FAILED pin 1.1.0 vs 2.0.0（DEFERRED）；static 2.0.0 notes PASSED
- 建议开发：在 `wsc-service/pom.xml` 增加与 Boot 管理版本一致的 `org.flywaydb:flyway-mysql`，重建后再跑 local 空库冒烟

证据目录：`ai/runs/RUN-WSC-002/tester-wsc-101-r5/`

## Layers

| name | result | notes |
| --- | --- | --- |
| ports | PASSED | 00-ports.txt |
| local-yml | PASSED | 00-yml-shape.txt（密码已脱敏） |
| mysql-auth | PASSED | 01-mysql-auth.txt, 01-mysql-auth-retry.txt |
| mysql-empty-db-init | PASSED | 02-mysql-init.txt, 02-mysql-post-boot.txt |
| spring-boot-local | BLOCKED | 04-spring-boot-local.txt, 04-flyway-blocker.txt |
| health-doc | BLOCKED | 05-health.txt（未启动） |
| overview-smoke-local | BLOCKED | 06-overview-smoke.txt |
| contract-check | FAILED | 03-contracts.txt（DEFERRED pin） |
| static-contract-notes | PASSED | 03c-static-contract-notes.txt |

## 证据红线

- 未将 Flyway/启动失败标为 PASSED；未将环境认证失败伪造成通过后的业务成功
- 未打印或落盘明文密码；未提交 `application-local.yml`
- supersedes EVID-TASK-WSC-101-2；总体仍为 BLOCKED（新阻塞：flyway-mysql）
- 机器摘要：`ai/runs/RUN-WSC-002/tester-wsc-101-r5/summary.json`
"""
Path("planning/tasks/TESTRUN-TASK-WSC-101.md").write_text(text, encoding="utf-8")
# verify
raw = Path("planning/tasks/TESTRUN-TASK-WSC-101.md").read_text(encoding="utf-8")
assert "evidenceId: EVID-TASK-WSC-101-3" in raw
assert "Unsupported Database" in raw
assert "结论摘要" in raw
print("TESTRUN ok, chars=", len(raw))
