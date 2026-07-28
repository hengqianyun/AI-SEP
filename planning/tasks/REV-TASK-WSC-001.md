# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-001-R1
taskId: TASK-WSC-001
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-001
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
reviewedAt: 2026-07-28T17:25:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-001.md
  - planning/tasks/DEV-TASK-WSC-001.md
  - planning/approved/PLAN-WSC-1.1.md
```

## 结论

**APPROVE** — 本轮无 P0/P1。变更落在 `allowModify` 内，未实现 overview/catalog/chain/auth/shell 业务 feature；`wsc-contracts@1.1.0` 关键契约齐备且与计划 §3.1–3.7 / 错误码四件套 / 认证 scheme 对齐。后端 `mvn compile` 因本机 JDK 17 vs 要求 21 而 BLOCKED，DEV 已合理记录解除条件；**不以环境阻塞放过契约缺口**——契约侧经静态审查与 `node tests/contracts/check-contracts.mjs` **PASSED**。正式 VERIFIED 仍须独立 tester 执行 `testScope`（含 Flyway 空库应用等）。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-001-R1-001 | P2 | `contracts/openapi/openapi.yaml` `createProduct` 400 示例 `otherExtraFields` 将 OQ-004「OTHER 携带专属字段」映射为 `ERR_PRODUCT_CODE_FORMAT`，与编码格式语义混用；四件套本身齐全且描述区已写明 ignore/reject | 示例改用明确 400 文案策略（仍可无新码），或在 `codes.yaml`/OpenAPI 注明 OQ-004 拒绝复用该码的约定，避免实现分歧 | REQ-CAT-005, OQ-004 |
| FIND-WSC-001-R1-002 | P2 | `frontend/src/api/**` 为手写对齐 OpenAPI，非 codegen；已声明 `CONTRACT_VERSION = '1.1.0'`（`client.ts`）满足可追踪，但与 acceptance「由 OpenAPI 生成」字面不完全一致 | 补充生成脚本/流水线，或在契约/DEV 中书面冻结「手写对齐 + 版本常量」为 001 验收口径 | — |
| FIND-WSC-001-R1-003 | P2 | DEV：`mvn -f backend/pom.xml -DskipTests compile` **BLOCKED**（pom `java.version=21`，环境仅 JDK 17）。记录含解除步骤；属环境门禁，非契约缺失。审查未因此放宽契约检查 | 安装 JDK 21 后复跑 compile（及 tester：Flyway 空库应用 / H2 或 PG 启动冒烟） | — |
| FIND-WSC-001-R1-004 | P3 | `frontend` `lint` 脚本为 placeholder `exit 0`，无 eslint 配置 | 后续工程化任务再补 lint；不阻塞本轮脚手架 | — |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. 范围 / 禁止业务 feature

| 项 | 结果 |
|---|---|
| 变更路径 ⊆ allowModify（frontend/backend/contracts/tests/ops/runbooks + 根 package/.nvmrc/.gitignore） | PASS |
| 未改 `ai/rules/**`、`product/**`、`planning/approved/**`、真实 `.env` / `application-local.yml` | PASS（本审查范围内） |
| `frontend/src/features/**` 仅 README，无业务页 | PASS |
| backend 无 `@RestController`/`@Service`/`@Entity`；feature 包仅 `package-info` + `ChainAttestationPort` 接口 | PASS |
| 路由均为 `PlaceholderView` 占位（含未开放「本版本未开放」） | PASS |

### 2. 契约齐全（wsc-contracts@1.1.0）

| 制品 | 路径 | 结果 |
|---|---|---|
| VERSION | `contracts/VERSION` = `1.1.0`；OpenAPI `info.version: 1.1.0` | PASS |
| OpenAPI | `contracts/openapi/openapi.yaml` | PASS |
| RBAC | `contracts/rbac/matrix.yaml`（ADMIN/PROVIDER/USER × §4.1） | PASS |
| 错误码四件套 | `ERR_PRODUCT_CODE_FORMAT` / `CONFLICT` / `CATEGORY_HAS_PRODUCTS` / `FORBIDDEN` + envelope | PASS |
| Chain port | `contracts/chain/ChainAttestationPort.md` + Java 接口对齐 DEC-WSC-002 | PASS |
| OVW stream | `contracts/overview/stream-events.yaml` 三类类型 + 最小字段 + fixture/projection | PASS |
| req-coverage | 14 REQ 均映射 | PASS |
| 敏感字段 | `contracts/security/sensitive-fields.md`（§3.7） | PASS |
| UI 矩阵 | `contracts/ui/state-matrix.md`（§3.5） | PASS |
| 契约机检 | `node tests/contracts/check-contracts.mjs` → PASSED（审查复跑） | PASS |

### 3. Flyway 初版

| 项 | 证据 | 结果 |
|---|---|---|
| 版本化目录快照 | `chain_catalog_snapshot`（`version_id` UNIQUE FK → `chain_version`） | PASS |
| `product_code` unique | `CONSTRAINT uq_data_product_code UNIQUE (product_code)` | PASS |
| OQ-004 可空专属字段 | `type_dataset_json` / `type_report_json` / `type_api_json` 可空；注释 OTHER 不用 | PASS |
| OVW 投影表 | `overview_stream_event` + 三类 CHECK | PASS |

### 4. 认证 security scheme / 错误码

| 项 | 结果 |
|---|---|
| `SessionCookie`（Cookie `WSC_SESSION`）+ `BearerAuth`（可吊销会话说明）；全局 security + §3.2 语义 | PASS |
| 403 示例 `ERR_FORBIDDEN`；分类删除 409 含 `reason`/`productCount` | PASS |

### 5. frontend/src/api 与路由

| 项 | 结果 |
|---|---|
| `CONTRACT_VERSION` / 模块头注释声明 1.1.0；经 `client.ts` `fetch` + `credentials: 'include'`，无硬编码绝对后端 URL | PASS |
| 路由仅占位，无 feature 业务实现 | PASS |

### 6. 后端 compile BLOCKED

| 项 | 结果 |
|---|---|
| pom 正确锁定 Java 17 / Spring Boot 3.3.x（RULE-ORG-STACK + DEC-WSC-004） | PASS（复测后） |
| DEV 记录 BLOCKED 原因与解除条件 | PASS（合理） |
| 不因 BLOCKED 跳过契约审查 | 已执行静态+机检 |

## 残余风险（交 tester / 环境）

- 本机未验证 `mvn compile` / Flyway 空库应用 / 进程启动；须在 JDK 21 下完成 TASK-WSC-001 `testScope` 中后端相关项。
- OpenAPI lint、OQ-004 DTO 负例、快照 get-by-versionId 契约测试属 tester 范围，本审查不代替宣称通过。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称测试通过。
- **decision: APPROVE**（进入独立 tester 门禁）。
