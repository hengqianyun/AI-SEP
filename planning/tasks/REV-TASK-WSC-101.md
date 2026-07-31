# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-101-R1
taskId: TASK-WSC-101
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-101
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.0.0
reviewedAt: 2026-07-30T17:58:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-101.md
  - planning/tasks/DEV-TASK-WSC-101.md
  - planning/approved/PLAN-WSC-2.2.md
  - product/requirements/SNAP-WSC-002.md
  - design/decisions/DEC-WSC-001.md
  - design/decisions/DEC-WSC-002.md
  - design/decisions/DEC-WSC-003.md
  - design/decisions/DEC-WSC-004.md
  - design/decisions/DEC-WSC-005.md
```

## 结论

**APPROVE** — 本轮 **P0=0，P1=0**。变更落在 `writeSet` ∪ `migrateAllowlist` 内：未实现 catalog/shell/auth/chain **业务页**；overview 仅契约/可编译启动最小适配、无新业务指标。`wsc-contracts@2.0.0` 覆盖 PLAN-WSC-2.2 §3.1–3.5 关键条目（三级 CRUD、维护≡产品、同步导入 DTO、Browse `page`/`pageSize`/`total`、错误码、会话 scheme、报告白名单/TTL/鉴权矩阵）。Flyway 权威为 `sql/init`+`sql/migration`；旧扁平 `backend/src/**` 与 `db/migration` 已物理退役且有退役说明。Boot 2.7.18 / MySQL / Redis 骨架对齐 DEC-WSC-005。审查复跑 `mvn -f backend/pom.xml -DskipTests compile` → **BUILD SUCCESS**；契约机检因 `tests/contracts/check-contracts.mjs` 仍断言 `1.1.0`（文件不在 writeSet）失败，已改为静态核对契约制品，**不以环境/机检版本钉死放过 §3.1 缺口**。空库 MySQL BLOCKED 记录合理。正式 VERIFIED 仍须独立 tester 执行 `testScope`。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-101-R1-001 | P2 | `tests/contracts/check-contracts.mjs` 仍断言 `VERSION === 1.1.0` 且仅检查 V1.0 四件套；`node tests/contracts/check-contracts.mjs` → FAILED（`got 2.0.0`）。文件不在 TASK-WSC-101 writeSet；DEV 已记 BLOCKED。契约本体经静态审查通过 | 独立 chore/后续任务将检查器升级为断言 `2.0.0` 与 IMPORT/LEAF/MAINTENANCE/REPORT 等新增码，并扩展 17 REQ | — |
| FIND-WSC-101-R1-002 | P2 | `frontend/src/api/catalog.ts` `ProductWrite` 仍 **必填** `l2CategoryId`（兼容 denyModify 的既有编辑页）；OpenAPI 权威为 `l3CategoryId`。DEV 已知限制；不构成契约冻结缺口 | TASK-WSC-105（或约定任务）切换编辑写入并去掉必填 `l2CategoryId` | REQ-CAT-005 |
| FIND-WSC-101-R1-003 | P2 | 空库 MySQL `sql/init`→启动 Flyway 本机 BLOCKED（3306/6379 不可达）；dev profile H2 + overview smoke 已由 DEV 记录。审查不以该环境阻塞放宽契约；tester 须在 MySQL+Redis 下补 `testScope` 空库项 | 本地/CI 起 MySQL 8 + Redis，按 `sql/init` baseline 后默认 profile 启动并验 `/health` | — |
| FIND-WSC-101-R1-004 | P3 | OpenAPI `ImportTemplateColumns` 定义了模板列最小集 example，但 `GET /catalog/products/import/template` 未 `$ref` 该 schema（列为 description + 前端 `IMPORT_TEMPLATE_COLUMNS` 常量） | 可选：在 template 操作 description 或关联 schema 显式 `$ref`，避免实现方漏列 | REQ-CAT-008 |
| FIND-WSC-101-R1-005 | P3 | `components/responses/Forbidden` 示例仅 `ERR_FORBIDDEN`；维护路径依赖同一 ref，而 `rbac/matrix.yaml` 对 PROVIDER 维护写期望 `ERR_MAINTENANCE_FORBIDDEN`（Error schema enum 已含该码） | 为 maintenance 403 增独立 example，或在 Forbidden 下增加 maintenance 示例 | REQ-RBAC-001, REQ-CAT-007 |
| FIND-WSC-101-R1-006 | P3 | `mvn compile` 对 `catalog/detail/package-info.class` 有 Windows `NoSuchFileException` 警告，Reactor 仍 SUCCESS | 确认 detail 包目录/编译插件行为，消除噪音警告 | — |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / 禁止业务 feature

| 项 | 结果 |
|---|---|
| 变更 ⊆ `writeSet` ∪ `migrateAllowlist` | PASS（含 contracts / api / overview / sql / ops runbooks / 多模块 POM；migrate 迁入 catalog/chain/rbac/security） |
| 未改 `ai/rules/**`、`product/**`、`planning/approved/**` | PASS（本审查范围内） |
| 未触碰 `frontend/src/features/{catalog,shell,auth,chain}/**`；无 `catalog/maintenance` 业务页 | PASS |
| overview 仅最小适配（`useOverviewData` 契约版本注释；无新指标字段） | PASS |
| 未向 `**/db/migration/**` 追加新脚本 | PASS（旧扁平树已删除；权威仅 `sql/migration`） |

### 2. 契约齐全（wsc-contracts@2.0.0 / §3.1–3.5）

| 项 | 证据 | 结果 |
|---|---|---|
| VERSION | `contracts/VERSION` = `2.0.0`；OpenAPI `info.version: 2.0.0` | PASS |
| 维护≡产品 | `/catalog/maintenance/entries/{productId}`；`MaintenanceEntry.id`≡产品；schema 注释 | PASS |
| 三级 CRUD | categories L1/L2/L3；`CategoryWrite.level` enum；产品 `l3CategoryId` | PASS |
| Browse 分页 | query `page`/`pageSize`；`ProductPage`/`MaintenancePage` required `items,page,pageSize,total`；无 offset/limit 替代 | PASS |
| 同步导入 | multipart POST；`successCount`/`failureCount`/`reportId`；全成功/部分/全行失败/请求级示例；无异步 job | PASS |
| 模板列最小集 + 三级语义 | 11 列 example；行业分类须 L3 id 或「空间/行业/子类」；仅二级 → 行级失败示例 | PASS |
| 错误码 | `ERR_IMPORT_FILE_TOO_LARGE` / `FORMAT_INVALID` / `ROW_INVALID`(报告内) / `LEAF_REQUIRED` / `MAINTENANCE_FORBIDDEN` / `REPORT_NOT_FOUND` + V1.0 四件套 | PASS |
| 会话 scheme | `SessionCookie`(`WSC_SESSION`) + `BearerAuth`（可吊销说明）；全局 security | PASS |
| §3.5 报告 | TTL≤24h；白名单字段；403/404；`sensitive-fields.md`；`rbac` USER GET 403 | PASS |
| 快照三级路径 | `categoryPath` / `categoryPathParts`；ChainAttestationPort V1.1 说明 | PASS |
| 17 REQ | `contracts/req-coverage.md` | PASS |
| 契约机检 | `check-contracts.mjs` 未升级（见 FIND-001）；静态审查替代 | PASS（静态）/ 机检 BLOCKED |

### 3. Flyway / 布局（DEC-WSC-005）

| 项 | 证据 | 结果 |
|---|---|---|
| 权威路径 | `sql/init/{00,01}` + `sql/migration/V202607301600__init_wsc_v11.sql`；`flyway.locations: classpath:sql/migration` | PASS |
| L3 + 产品挂三级 + 维护状态 + import_error_report | migration SQL | PASS |
| 旧扁平退役 | `backend/src` **不存在**；`LEGACY-FLAT-RETIRED.md` + runbook | PASS |
| 未混用双权威 | 无新 `db/migration` 脚本 | PASS |

### 4. Boot / 栈骨架

| 项 | 证据 | 结果 |
|---|---|---|
| Boot 2.7.18 / Java 17 | parent `pom.xml` | PASS |
| MySQL + Redis + JPA + Flyway + Knife4j | `wsc-service/pom.xml` + `application.yml` | PASS |
| javax（Boot 2） | Controllers/Security 使用 `javax.servlet` | PASS |
| `/health`、`/doc.html` | `HealthConfig` + knife4j；DEV 自测 PASSED；审查未代跑进程 | PASS（配置与 DEV 记录） |
| 审查复跑 compile | `mvn -f backend/pom.xml -DskipTests compile` → BUILD SUCCESS（49 sources） | PASS |

### 5. frontend api / overview / 密钥

| 项 | 结果 |
|---|---|
| `CONTRACT_VERSION = '2.0.0'`；经 `client.ts` + `credentials: 'include'`；无硬编码绝对后端 URL | PASS |
| catalog client：maintenance/import/Browse 分页字段对齐 | PASS |
| 无真实 `.env` / `application-local.yml` / `application-prod.yml` 入仓；密码为 `${DB_PASSWORD:...}` 占位 | PASS |

### 6. DEV 自测 BLOCKED

| 项 | 结果 |
|---|---|
| MySQL 空库 BLOCKED 原因与解除条件 | PASS（合理；不阻塞本轮 APPROVE） |
| 不以环境阻塞放过契约缺口 | 已执行 §3.1 静态核对 + compile 复跑 |

## 残余风险（交 tester）

- 须在 MySQL 8 + Redis 下执行空库 `sql/init`→Flyway 与默认 profile 启动；dev/H2 不能替代该 `testScope` 项。
- OpenAPI lint、契约机检升级、overview 只读 smoke 的独立证据由 tester 产出；本审查不代替宣称 VERIFIED。
- 迁入的 catalog 内存实现仍为 V1.0（L2）；2.0 业务落地属 103+，不在本任务验收为完整 catalog 行为。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过 / VERIFIED。
- **decision: APPROVE**（进入独立 tester 门禁）。

## 计数

| 级别 | 数量 |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 3 |
