# DEV-TASK-WSC-101

```yaml
taskId: TASK-WSC-101
actorInstance: developer-wsc-101
planId: PLAN-WSC-2.2
snapshotId: SNAP-WSC-002
contracts: wsc-contracts@2.0.0
status: READY_FOR_REVIEW
completedAt: 2026-07-30T17:45:00+08:00
```

## 摘要

后端迁至 `backend/app/wsc-service/`（Boot 2.7.18 / MySQL+Redis 配置基线 / Flyway `sql/init`+`sql/migration`）；冻结 `wsc-contracts@2.0.0`（三级分类、目录维护≡产品、同步导入、Browse `page`/`pageSize`/`total`、会话 scheme 继承）；生成/对齐 `frontend/src/api/**`；overview 前后端最小适配可编译启动；文档化 V1.0→V1.1 schema 策略。未实现 catalog/shell/auth/chain **业务页**；未实现导入/维护后端业务逻辑（契约+表结构已冻结）。

## 变更文件列表

### backend/（多模块骨架）
- `backend/pom.xml`（parent Boot 2.7.18）
- `backend/LEGACY-FLAT-RETIRED.md`
- `backend/app/wsc-service/pom.xml`
- `backend/app/wsc-service/src/main/java/com/wsc/WscApplication.java`
- `backend/app/wsc-service/src/main/java/com/wsc/config/{HealthConfig,OpenApiConfig}.java`
- `backend/app/wsc-service/src/main/java/com/wsc/common/WscConstants.java`
- `backend/app/wsc-service/src/main/java/com/wsc/overview/**`（迁入 + package-info 适配说明）
- `backend/app/wsc-service/src/main/java/com/wsc/{catalog,chain,rbac,security}/**`（migrateAllowlist 迁入；javax；无新业务实现）
- `backend/app/wsc-service/src/main/resources/application.yml`
- `backend/app/wsc-service/src/main/resources/application-local.example.yml`
- `backend/app/wsc-service/src/main/resources/sql/README.md`
- `backend/app/wsc-service/src/main/resources/sql/init/00_create_database.sql`
- `backend/app/wsc-service/src/main/resources/sql/init/01_flyway_baseline.sql`
- `backend/app/wsc-service/src/main/resources/sql/migration/V202607301600__init_wsc_v11.sql`
- `backend/app/wsc-service/src/test/java/com/wsc/**`（迁入；排除 Redis 自动配置）
- **退役删除**：原扁平 `backend/src/**`（含 `db/migration/V202607281600__init_wsc.sql`）

### contracts/（wsc-contracts@2.0.0）
- `contracts/VERSION` → `2.0.0`
- `contracts/openapi/openapi.yaml`（L3 CRUD；maintenance；import 同步 DTO；请求级/行级示例；Browse 分页字段；快照三级路径）
- `contracts/errors/codes.yaml`（新增 IMPORT/LEAF/MAINTENANCE/REPORT 码）
- `contracts/rbac/matrix.yaml`（目录维护/导入/报告 GET）
- `contracts/req-coverage.md`（17 REQ）
- `contracts/overview/stream-events.yaml`
- `contracts/ui/state-matrix.md`（导入四态）
- `contracts/security/sensitive-fields.md`（报告白名单）
- `contracts/chain/ChainAttestationPort.md`

### frontend/
- `frontend/src/api/{client,catalog,overview,auth,chain,index}.ts`（2.0.0）
- `frontend/src/features/overview/composables/useOverviewData.ts`（契约版本注释；无新指标）
- `frontend/src/router/routes.ts`（注释增量）

### ops/
- `ops/runbooks/wsc-v11-schema-migration.md`（新建）
- `ops/runbooks/rollback.md`（对齐 MySQL / sql/migration）

## REQ 映射（本任务底座覆盖）

| REQ | 本任务覆盖 |
|---|---|
| REQ-SHELL-001 | 路由保留；UI 状态矩阵；壳层业务实现属 102 |
| REQ-RBAC-001 | `rbac/matrix.yaml` + OpenAPI 403/`ERR_FORBIDDEN`/`ERR_MAINTENANCE_FORBIDDEN` + SessionCookie |
| REQ-OVW-001..005 | OpenAPI overview 端点；后端 overview 可启动；前端 API/composable 对齐；无新指标 |
| REQ-CAT-001..002 | Browse `page`/`pageSize`/`total`；L2/L3 筛选参数契约 |
| REQ-CAT-003..005 | Product `l3CategoryId`；LEAF 错误码；OQ-004 保留 |
| REQ-CAT-006 | 三级 Category CRUD 契约 + Flyway L3 |
| REQ-CAT-007 | `/catalog/maintenance/**`；条目≡产品 |
| REQ-CAT-008 | 导入同步 API + 模板列最小集 + 请求级/行级错误语义 + 报告 TTL/鉴权契约 |
| REQ-CHAIN-001 | 快照 `categoryPath`/`categoryPathParts`；ChainAttestationPort 说明 |

详见 `contracts/req-coverage.md`。

## 未实现说明（硬边界）

- 未实现 catalog/shell/auth/chain **业务页**（denyModify）
- 未实现目录维护/批量导入 **服务端业务逻辑**（仅契约 + `import_error_report` 表；属后续任务）
- overview 无新业务指标；仍以内存 seed 投影为主（与 V1.0 一致）
- 未修改 `tests/contracts/check-contracts.mjs`（不在 writeSet；仍写死 1.1.0）

## 自测命令与结果

| 命令 | 结果 | 说明 |
|---|---|---|
| `mvn -f backend/pom.xml -DskipTests compile` | **PASSED** | Reactor BUILD SUCCESS |
| `mvn -f backend/pom.xml -DskipTests package` | **PASSED** | wsc-service repackage SUCCESS |
| `mvn -f backend/app/wsc-service/pom.xml "-Dtest=OverviewApiIntegrationTest,WscApplicationTests" test` | **PASSED** | Tests run: 6, Failures: 0 |
| `pnpm --filter frontend typecheck` | **PASSED** | vue-tsc |
| `pnpm --filter frontend build` | **PASSED** | vite production build |
| `pnpm --filter frontend lint` | **PASSED** | scaffold placeholder exit 0 |
| `mvn -f backend/app/wsc-service/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev` | **PASSED** | H2 + 排除 Redis；`Started WscApplication` |
| `GET http://localhost:8080/health` | **PASSED** | `{"status":"UP"}` |
| `GET http://localhost:8080/doc.html` | **PASSED** | HTTP 200 |
| `GET /v3/api-docs` | **PASSED** | HTTP 200 |
| overview smoke（login admin/demo → metrics + stream） | **PASSED** | 200 + seed 数据 / 流含 items |
| 空库 MySQL `sql/init`→启动 Flyway | **BLOCKED** | 本机 `localhost:3306` / `6379` 不可达；dev profile 下 H2 已跑通 migration |
| `node tests/contracts/check-contracts.mjs` | **BLOCKED** | 检查器仍断言 VERSION=1.1.0（文件不在 writeSet，未改） |

### BLOCKED 解除条件

1. 安装/启动本地 MySQL 8 + Redis，按 `sql/init` 建库 baseline 后以默认 profile 启动 `wsc-service`。
2. 后续任务或独立 chore 将 `tests/contracts/check-contracts.mjs` 升级为断言 `2.0.0` 与新增错误码。

## 已知限制

- ProductWrite TS 仍保留必填 `l2CategoryId` 以兼容 denyModify 的既有 catalog 编辑页；OpenAPI 权威字段为 `l3CategoryId`（105 任务切换）。
- 默认 profile 依赖 MySQL+Redis；无环境时用 `--spring.profiles.active=dev`。
- 迁入的 catalog/security 包仍为 V1.0 内存实现（L2）；与 2.0 契约对齐的业务落地在 103+。

## 审查提交声明

- 变更均在 `writeSet` ∪ `migrateAllowlist` 内；DEV 报告按派发要求写入 `planning/tasks/`
- 未修改 `ai/rules/**`、`product/**`、`planning/approved/**`
- 未触碰 `frontend/src/features/{catalog,shell,auth,chain}/**`
- 未向 `**/db/migration/**` 追加新脚本；旧扁平树已退役
- 未兼任 codeReviewer；未自行标记 VERIFIED
