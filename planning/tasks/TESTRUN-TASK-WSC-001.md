# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-001-1
taskId: TASK-WSC-001
planId: PLAN-WSC-1.1
actorInstance: tester-wsc-001
contracts: wsc-contracts@1.1.0
basedOn:
  - planning/tasks/TASK-WSC-001.md
  - planning/tasks/DEV-TASK-WSC-001.md
  - planning/tasks/REV-TASK-WSC-001.md
reviewDecision: APPROVE
executedAt: 2026-07-28T17:22:00+08:00
status: BLOCKED
```

## 总体结论

**BLOCKED**（发布前须解除）— 契约机检与前端 typecheck/build 均 **PASSED**；后端 `mvn compile` 因本机 **JDK 17 ≠ 要求 Java 21** 记 **BLOCKED**，**不得**记为 PASSED。  
等价表述：`PASSED_WITH_BLOCKERS`（blocker = JDK 21 未就绪 → 后端 compile / Flyway 空库应用 / 运行时契约测试不可执行）。

独立审查前提：`REV-TASK-WSC-001` = **APPROVE**（P0/P1 = 0）。本 tester 未兼任 developer/codeReviewer。

## Layers

| name | command | result | relatedReqs | notes |
|---|---|---|---|---|
| contract-check | `node tests/contracts/check-contracts.mjs` | PASSED | REQ-SHELL-001..REQ-CHAIN-001（14 REQ） | exit 0；错误码四件套、REQ 覆盖、RBAC 角色、OVW 流类型、VERSION 1.1.0、OpenAPI 含 SessionCookie/BearerAuth 与 OQ-004/OTHER |
| openapi-lint（轻量） | 同上（无独立 spectral/redocly 配置） | PASSED | 契约最小集 | 以机检覆盖「错误码目录与示例一致性」；未跑第三方 OpenAPI lint 工具 |
| frontend-typecheck | `pnpm --filter frontend typecheck` | PASSED | REQ-SHELL-001 | exit 0；`vue-tsc --noEmit` |
| frontend-build | `pnpm --filter frontend build` | PASSED | REQ-SHELL-001 | exit 0；vite production build 成功 |
| backend-compile | `mvn -f backend/pom.xml -DskipTests compile` | BLOCKED | 脚手架 | exit 1；`javac release 21` → 「不支持发行版本 21」；本机 `java 17.0.11`，`JAVA_HOME=C:\Program Files\Java\jdk-17` |
| flyway-file-assert | 读 `V202607281600__init_wsc.sql` | PASSED | REQ-CAT-004..006, REQ-CHAIN-001 | 含 `chain_catalog_snapshot`；`CONSTRAINT uq_data_product_code UNIQUE (product_code)` |
| flyway-empty-db-apply | （需 JDK 21 + DB） | BLOCKED | REQ-CAT-*, REQ-CHAIN-001 | 环境不可用；未启动应用/未对空库执行 migrate |
| snapshot-rw-contract | get-by-versionId 运行时 | BLOCKED | REQ-CHAIN-001 | 无业务实现且后端不可 compile；仅契约/SQL 静态存在 |
| oq004-dto-negative | 多余专属字段忽略/拒绝 | SKIPPED | REQ-CAT-005, OQ-004 | 本任务无 DTO/服务实现；静态：SQL 可空 type_* + OpenAPI/codes 口径已落盘 |
| rbac-matrix-exists | 文件存在性 | PASSED | REQ-RBAC-001 | `contracts/rbac/matrix.yaml` 存在；含 ADMIN/PROVIDER/USER × §4.1 |
| error-codes-foursome | 读 `contracts/errors/codes.yaml` | PASSED | REQ-CAT-005/006, REQ-RBAC-001 | 四码齐全：FORMAT / CONFLICT / CATEGORY_HAS_PRODUCTS / FORBIDDEN |
| contracts-version | 读 `contracts/VERSION` | PASSED | — | 内容 `1.1.0` |
| e2e-p0 | Playwright | SKIPPED | — | 本任务无业务 UI；按指令不跑真实后端 E2E。`tests/e2e` 脚手架与 `node_modules` 已存在（可选） |

## 环境快照

| 项 | 值 |
|---|---|
| cwd | `C:\WorkSpace\AI-SEP` |
| Node / pnpm | workspace 前端命令可用 |
| Java | `17.0.11`（Oracle） |
| JAVA_HOME | `C:\Program Files\Java\jdk-17` |
| Maven | `3.6.3`（绑定上述 JDK 17） |
| 后端要求 | Spring Boot 3.3.x + **Java 21**（`pom.xml` / RULE-ORG-STACK） |

## 阻塞时

- blockedBy: 本机仅安装 JDK 17，Maven compiler 目标 release 21 失败（错误：不支持发行版本 21）。因此后端 compile、Flyway 空库应用、版本快照运行时读写、依赖后端进程的契约负例均无法在本环境真实执行。
- 解除条件:
  1. 安装 **JDK 21**，并将 `JAVA_HOME`（及 PATH）指向该 JDK；`java -version` / `mvn -version` 均显示 21。
  2. 复跑：`mvn -f backend/pom.xml -DskipTests compile` → 期望 exit 0。
  3. （testScope 补齐）在 H2 或 PostgreSQL 16 上空库执行 Flyway migrate；按契约对 versionId 快照可读做最小冒烟；可选启动后确认 health。
  4. 解除后由独立 tester 更新本证据状态为 PASSED（或新开 `EVID-TASK-WSC-001-2`），方可作为发布/VERIFIED 门禁依据。

## 文件断言明细（已执行）

| 断言 | 结果 |
|---|---|
| `contracts/VERSION` = `1.1.0` | PASSED |
| 错误码四件套文件 `contracts/errors/codes.yaml` 存在且含四码 | PASSED |
| OpenAPI / chain port / OVW stream / UI matrix / sensitive-fields / req-coverage 存在 | PASSED |
| `contracts/rbac/matrix.yaml` 存在 | PASSED |
| Flyway SQL 含 `chain_catalog_snapshot` | PASSED |
| Flyway SQL 含 `product_code` unique（`uq_data_product_code`） | PASSED |

## 决策权声明

- 结论基于本机真实命令 exitCode 与可读文件断言；未把环境阻塞记为 PASSED。
- 未修改被测业务代码；未改写 `REV-*` 审查结论。
- 总体：`BLOCKED` — 契约与前端层已绿，**发布前必须解除 JDK 21 blocker 并复跑后端相关 testScope**。
