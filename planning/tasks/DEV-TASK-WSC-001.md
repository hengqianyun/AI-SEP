# DEV-TASK-WSC-001

```yaml
taskId: TASK-WSC-001
actorInstance: developer-wsc-001
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
status: READY_FOR_REVIEW
completedAt: 2026-07-28T17:15:00+08:00
```

## 摘要

绿地脚手架与 `wsc-contracts@1.1.0` 已落盘。未实现 overview/catalog/chain/auth/shell 业务 feature 逻辑；仅契约、空壳路由占位、API client 骨架、Flyway 初版、测试框架与回滚 runbook。

## 变更文件列表

### 根
- `package.json`
- `pnpm-workspace.yaml`
- `.nvmrc`
- `.gitignore`
- `pnpm-lock.yaml`（install 生成）

### contracts/（wsc-contracts@1.1.0）
- `contracts/VERSION`
- `contracts/openapi/openapi.yaml`
- `contracts/rbac/matrix.yaml`
- `contracts/errors/codes.yaml`
- `contracts/chain/ChainAttestationPort.md`
- `contracts/overview/stream-events.yaml`
- `contracts/ui/state-matrix.md`
- `contracts/security/sensitive-fields.md`
- `contracts/req-coverage.md`

### backend/
- `backend/pom.xml`
- `backend/src/main/java/com/wsc/WscApplication.java`
- `backend/src/main/java/com/wsc/chain/ChainAttestationPort.java`
- `backend/src/main/java/com/wsc/{rbac,overview,catalog,chain}/package-info.java`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.example.yml`
- `backend/src/main/resources/db/migration/V202607281600__init_wsc.sql`
- `backend/src/test/java/com/wsc/WscApplicationTests.java`

### frontend/
- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/tsconfig.json`
- `frontend/tsconfig.app.json`
- `frontend/tsconfig.node.json`
- `frontend/index.html`
- `frontend/.env.example`
- `frontend/src/main.ts`
- `frontend/src/App.vue`
- `frontend/src/vite-env.d.ts`
- `frontend/src/views/PlaceholderView.vue`
- `frontend/src/router/index.ts`
- `frontend/src/router/routes.ts`
- `frontend/src/api/{client,auth,overview,catalog,chain,index}.ts`
- `frontend/src/features/README.md`

### tests/
- `tests/e2e/package.json`
- `tests/e2e/playwright.config.ts`
- `tests/e2e/specs/p0-wsc.spec.ts`
- `tests/contracts/README.md`
- `tests/contracts/check-contracts.mjs`

### ops/
- `ops/runbooks/rollback.md`
- `ops/runbooks/wsc-v1-rollback.md`（与 rollback.md 内容一致，满足计划 §7.6 路径引用）

## REQ 映射（本任务二级覆盖）

| REQ | 本任务覆盖 |
|---|---|
| REQ-SHELL-001 | 路由占位 + 未开放路由 + UI 状态矩阵引用 |
| REQ-RBAC-001 | `contracts/rbac/matrix.yaml` + OpenAPI 403/`ERR_FORBIDDEN` + securitySchemes |
| REQ-OVW-001..005 | OpenAPI overview 端点 + stream-events 契约 |
| REQ-CAT-001..006 | OpenAPI catalog + 错误码 + OQ-004 + Flyway data_product/category |
| REQ-CHAIN-001 | OpenAPI versions/snapshot + ChainAttestationPort + Flyway chain_* |

详见 `contracts/req-coverage.md`。

## 未实现说明（硬边界）

- 无 auth/shell/overview/catalog/chain 业务页面与服务实现
- `frontend/src/features/**` 仅 README 占位
- backend feature 包仅 `package-info` + `ChainAttestationPort` 接口
- Playwright 为脚手架；P0 E2E 属 W5 tester

## 自测命令与结果

| 命令 | 结果 | 说明 |
|---|---|---|
| `node tests/contracts/check-contracts.mjs` | **PASSED** | 错误码、REQ 覆盖、RBAC、流类型、VERSION |
| `pnpm install` | **PASSED** | workspace frontend + e2e |
| `pnpm --filter frontend typecheck` | **PASSED** | vue-tsc |
| `pnpm --filter frontend build` | **PASSED** | vite production build |
| `pnpm --filter frontend lint` | **PASSED** | scaffold placeholder（无 eslint 配置，exit 0） |
| `mvn -f backend/pom.xml -DskipTests compile` | **PASSED**（复测） | DEC-WSC-004 后 `java.version=17`；JDK 17.0.11 BUILD SUCCESS |

### BLOCKED 解除条件（历史）

原「安装 JDK 21」要求已由 **DEC-WSC-004**（改为 Java 17）取代并关闭。
2. 重新执行：`mvn -f backend/pom.xml -DskipTests compile`
3. （可选启动）复制 `application-local.example.yml` → `application-local.yml`（勿提交），或使用默认 H2 profile：`mvn -f backend/pom.xml spring-boot:run`，确认 `/actuator/health` 为 UP

## 审查提交声明

- 变更均在 `allowModify` 内（另按派发要求写入本 DEV 报告至 `planning/tasks/`）
- 未修改 `planning/approved/**`、`product/**`、`ai/rules/**`
- 未兼任 codeReviewer；正式 VERIFIED 待独立审查与 tester
