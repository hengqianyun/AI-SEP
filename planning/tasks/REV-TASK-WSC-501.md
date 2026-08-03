# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-501-R2
taskId: TASK-WSC-501
round: 2
decision: APPROVE
actorInstance: code-reviewer-wsc-501
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.2.0
runId: RUN-WSC-007
reviewedAt: 2026-08-03T12:32:00+08:00
priorRound: REV-CODE-TASK-WSC-501-R1
basedOn:
  - planning/tasks/TASK-WSC-501.md
  - planning/tasks/DEV-TASK-WSC-501.md
  - planning/tasks/REV-TASK-WSC-501.md (R1)
  - design/decisions/DEC-WSC-006.md
  - contracts/VERSION
  - contracts/openapi/openapi.yaml
  - backend/.../security/SessionAuthIntegrationTest.java
mustDifferFrom: developer-wsc-501
```

## Round 2 结论

**APPROVE** — 本轮 **P0=0，P1=0**。R1 必修项已闭合：`SessionAuthIntegrationTest` admin/provider `POST /catalog/products` 已补合法 `industryCategory`（「卫生和社会工作」）并去掉无效 `l2CategoryId`；OpenAPI 导入错误报告示例 `reasonMessage` 已改为门类枚举非法文案。审查复跑 `SessionAuthIntegrationTest`：**Tests run: 6, Failures: 0** → **BUILD SUCCESS**。P3 死样式仍 OPEN（不阻塞）。进入独立 tester 门禁；**不**代标 VERIFIED。

## Round 2 Findings

| id | severity | R2 状态 | 证据 |
|---|---|---|---|
| FIND-WSC-501-R1-001 | P1 | **CLOSED** | `SessionAuthIntegrationTest` L62/L114 body 含 `industryCategory":"卫生和社会工作"`；无 `l2CategoryId`；复跑 admin/provider 产品写路径 `PRODUCT_SUBMIT … result=SUCCESS`；套件 6/0/0 |
| FIND-WSC-501-R1-002 | P2 | **CLOSED** | `contracts/openapi/openapi.yaml` L731：`reasonMessage: 行业分类非法，须为 GB/T 4754 门类枚举：电子病历`（不再写「仅二级标签，缺少三级」） |
| FIND-WSC-501-R1-003 | P3 | OPEN（不阻塞） | `ProductEditorPage.vue` `.cascade-row` 死样式仍可择机删除 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0** → **APPROVE**。

## Round 2 核对

| 项 | 结果 |
|---|---|
| FIND-001：产品写补 `industryCategory` | PASS |
| FIND-002：OpenAPI 示例文案对齐门类语义 | PASS |
| SessionAuth 复跑 | **PASS**（6 tests，Failures: 0） |
| 未改业务代码 / state / events（本审查） | PASS |
| 正式 VERIFIED | 须独立 tester（本审查不代标） |

### 审查复跑证据（R2）

```text
cd backend/app/data-chain-service
mvn -Dtest=SessionAuthIntegrationTest test
# Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
# Finished at: 2026-08-03T12:31:26+08:00
```

### 隔离声明（R2）

- 审查者未修改被审业务代码、DEV/TASK、`state.yaml`、`events.jsonl`。
- 未兼任 developer / tester；未代标 VERIFIED；未调度 tester。
- **decision: APPROVE**（进入独立 tester 门禁）。

### 计数（R2 open）

| severity | open |
|---|---|
| P0 | 0 |
| P1 | 0 |
| P2 | 0 |
| P3 | 1（FIND-003，不阻塞） |

---

## Round 1（历史；REQUEST_CHANGES）

```yaml
reviewId: REV-CODE-TASK-WSC-501-R1
taskId: TASK-WSC-501
round: 1
decision: REQUEST_CHANGES
actorInstance: code-reviewer-wsc-501
planId: PLAN-WSC-2.2
contracts: wsc-contracts@2.2.0
reviewedAt: 2026-08-03T12:23:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-501.md
  - planning/tasks/DEV-TASK-WSC-501.md
  - design/decisions/DEC-WSC-006.md
  - contracts/VERSION
  - contracts/openapi/openapi.yaml
```

## 结论（R1）

**REQUEST_CHANGES** — 本轮 **P0=0，P1=1**。核心语义（`industryCategory` 门类字段、导入不再因合法门类抛 `ERR_CATEGORY_LEAF_REQUIRED`、编辑去强制 L1/L2/L3、契约 2.2.0 / ProductWrite、浏览无挂载分桶）实现与 DEV 说明基本对齐；但模块内 `SessionAuthIntegrationTest` 产品写请求体未补 `industryCategory`，审查复跑得 **400 `ERR_VALIDATION`**（期望 200），属必修复回归。正式 VERIFIED 仍须独立 tester 执行 testScope。

## Findings（R1）

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-501-R1-001 | P1 | `SessionAuthIntegrationTest` 中 admin/provider `POST /catalog/products` body 仅含 `l2CategoryId`、无 `industryCategory`；审查复跑 `rbacMatrix_admin_*` / `rbacMatrix_provider_*` 得 Status 400、`ERR_VALIDATION`「行业分类为必填项」（Tests run: 23, Failures: 2）。DEV 自测未覆盖该类 | 补齐合法 `industryCategory`（及按需去掉无效 l2-only 语义），使上述两测与相关矩阵写路径重新通过 | REQ-CAT-005, DEC-WSC-006 |
| FIND-WSC-501-R1-002 | P2 | `contracts/openapi/openapi.yaml` 导入错误报告示例仍写 `reasonMessage: 行业分类仅二级标签，缺少三级`（约 L731），与同文件导入 description / ImportTemplateColumns「非三级叶子」语义冲突 | 将示例改为门类枚举非法类文案（如「行业分类非法，须为 GB/T 4754 门类枚举：…」） | REQ-CAT-008, DEC-WSC-006 |
| FIND-WSC-501-R1-003 | P3 | `ProductEditorPage.vue` 残留未使用的 `.cascade-row` 样式；三级级联 UI 已移除 | 删除死样式即可 | REQ-CAT-005 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=1**。

## 检查清单（对照重点）（R1）

### 1. industryCategory 为 GB/T 门类产品字段

| 项 | 证据 | 结果 |
|---|---|---|
| 非树节点 / 合法门类不 `ERR_CATEGORY_LEAF_REQUIRED` | `ImportFieldMapper` → `IndustryCategories.normalizeOrNull` → `industryCategory`；无 resolveL3；`full-success` 建筑业/交通运输断言 `l3CategoryId` null | PASS |
| 非法门类行级失败 | `category-mismatch.csv`（电子病历等）→ `ERR_IMPORT_ROW_INVALID` +「行业分类非法」 | PASS |
| 显式非法 L3 仍保留叶校验 | `ProductEditorService` 仅当 `l3CategoryId` 非空时校验 L3；集成测 `GEN-LEAF-9006` → `ERR_CATEGORY_LEAF_REQUIRED` | PASS |
| 20 门类枚举对齐 TASK/v0729 | `IndustryCategories.ALL` / OpenAPI `IndustryCategory` / FE `INDUSTRY_CATEGORY_OPTIONS` 一致 | PASS |

### 2. 枚举对齐 v0729；编辑不强制 L1/L2/L3

| 项 | 证据 | 结果 |
|---|---|---|
| 产品类型/交付/地域/更新频率/数据形态下拉 | `ProductEditorPage` + `useProductEditor` 选项与 TASK 权威表一致（展示中文；频率/交付存英文码） | PASS |
| 行业分类存中文原文 | 导入/编辑写入中文门类；集成断言「建筑业」 | PASS |
| 不再强制三级级联 | 编辑页无 L1/L2/L3 cascade；`buildBody` 仅强制 `industryCategory`；后端 L3 可空 | PASS |

### 3. 契约 2.2.0 / ProductWrite / 导入文档

| 项 | 证据 | 结果 |
|---|---|---|
| VERSION / OpenAPI / codes / req-coverage | `contracts/VERSION=2.2.0`；`ProductWrite.required` 含 `industryCategory`；`l3CategoryId` nullable | PASS |
| 导入 description 去「须解析到三级」 | import POST description + `ImportTemplateColumns` 已更新 | PASS（示例残留见 FIND-002） |

### 4. 浏览可见 / writeSet / denyModify / 测试

| 项 | 证据 | 结果 |
|---|---|---|
| 无挂载产品浏览可见 | `groupProductsByL3`：无 l3 → `ic:{industryCategory}` /「未分类」；vitest 覆盖；列表 API 无滤时不丢 null-l3 | PASS（策略：按门类分桶，DEV 已说明） |
| writeSet | 变更落在 contracts、catalog backend、frontend catalog/{editor,detail,browse}、api/catalog、fixtures、DEV；含 maintenance 保留 `industryCategory` | PASS |
| denyModify | 未改 `planning/approved/**`；未改本任务职责外的 state/events 编排写（审查未写业务代码/state） | PASS |
| 任务相关测 | Editor 7 + Import 10 随同批复跑通过；SessionAuth 2 失败 → FIND-001 | FAIL（P1） |
| 审查复跑 | `mvn -Dtest=SessionAuthIntegrationTest,ProductEditorIntegrationTest,ProductImportIntegrationTest test` → **BUILD FAILURE**（Failures: 2，均 SessionAuth） | 见 FIND-001 |

## 复审建议（R1 → 已由 R2 处理）

1. 修复 FIND-001（SessionAuth 产品写 body）。
2. 建议顺手改 FIND-002 示例文案。
3. 复审时至少复跑：`SessionAuthIntegrationTest` + `ProductEditorIntegrationTest` + `ProductImportIntegrationTest`，以及 DEV 所列 vitest 集。
