# DEV-TASK-WSC-302

```yaml
taskId: TASK-WSC-302
actorInstance: developer-wsc-302
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
contracts: wsc-contracts@2.1.0
status: READY_FOR_REVIEW
completedAt: 2026-08-02T18:05:00+08:00
reqs:
  - REQ-CAT-005
  - REQ-API-001
  - REQ-RBAC-001
  - REQ-UX-007
```

## 摘要

产品编辑页按类型动态展示扩展数据描述区；API 类型自研 OpenAPI 侧栏+端点详情+Swagger 导入回填（方案 B 前端解析，无 `frontend/src/components/**`）；提交写入 2.1.0 `typeSpecific`；非空坏 Swagger 阻断提交；共享 fixture 关键字段与 301 一致。未改 detail/import/contracts/backend/api/state/events。

## 变更文件列表（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/editor/ProductEditorPage.vue` | REQ-CAT-005 / UX-007 | 按类型扩展区；更新频率含 `NO_UPDATE`；挂载 OpenAPI 面板；写权结构不可达不变 |
| `frontend/src/features/catalog/editor/components/OpenApiEditorPanel.vue` | REQ-API-001 | 侧栏端点列表/增删；详情参数/响应/schema；Swagger 粘贴/上传回填；归档原文 |
| `frontend/src/features/catalog/editor/composables/useProductEditor.ts` | REQ-CAT-005 / API-001 | typeSpecific 读写；Swagger 回填；非空解析失败阻断；旧 `endpoint` 兼容 |
| `frontend/src/features/catalog/editor/utils/parseOpenApi.ts` | REQ-API-001 | 前端 JSON/YAML OpenAPI→endpoints（对齐 301 关键字段） |
| `frontend/src/features/catalog/editor/utils/openapiSharedExpectations.ts` | REQ-API-001 | 共享 fixture 关键字段常量 |
| `frontend/src/features/catalog/editor/utils/parseOpenApi.spec.ts` | testScope | 共享 YAML/JSON + 坏文本 |
| `frontend/src/features/catalog/editor/composables/useProductEditor.spec.ts` | testScope | 端点增删、Swagger 回填、阻断提交、类型切换、payload 抽样、旧 endpoint |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-005** | 类型动态扩展区；提交 `typeSpecific`（api/dataset/report/other）；上链反馈文案不回退 |
| **REQ-API-001** | 侧栏+详情+Swagger 回填保留原文；方案 B 前端解析；关键字段与 301 fixture 一致 |
| **REQ-RBAC-001** | 无写权仍 `router.replace('/catalog')` 结构不可达（非纯 CSS） |
| **REQ-UX-007** | 消费既有令牌；主次按钮/分区卡片；不强制 Element Plus；不写全局 `components/**` |

## 共享 fixture 对照（允许差异：无）

| 字段 | 期望（301 导入 = 302 回填） |
|---|---|
| method | `POST` |
| path | `/enterprise/security/verify` |
| summary | `企业安全信息核验` |

来源：`tests/fixtures/import/openapi-shared-minimal.yaml`（只读）。

## 自测命令与结果

```text
cd frontend
pnpm exec vitest run src/features/catalog/editor
# Test Files  2 passed (2)
# Tests  16 passed (16)

npx vue-tsc --noEmit -p tsconfig.json --pretty false
# EXIT:0
```

## 已知边界

- YAML 解析为 editor 内最小子集（无新增 npm 依赖）；复杂 YAML 边缘以 JSON 粘贴为兜底。
- 详情只读 OpenAPI UI → TASK-WSC-303；导入弹窗/E2E → TASK-WSC-304。
- 空 `paths` 的合法 openapi 文档：解析成功且 endpoints=[]（与 301 一致）；非空坏文本阻断提交。

## 审查入口

status: **READY_FOR_REVIEW** — 请独立 codeReviewer / tester 按 PLAN-WSC-4.1 § TASK-WSC-302 acceptance / testScope 验收。
