# DEV-TASK-WSC-303

```yaml
taskId: TASK-WSC-303
actorInstance: developer-wsc-303
planId: PLAN-WSC-4.1
snapshotId: SNAP-WSC-004
runId: RUN-WSC-004
status: READY_FOR_REVIEW
completedAt: 2026-08-02T18:00:00+08:00
reqs:
  - REQ-CAT-004
  - REQ-API-001
  - REQ-RBAC-001
  - REQ-UX-007
```

## 摘要

产品详情在既有分组之上，按类型只读展示扩展数据描述；API 类型自研只读 OpenAPI 面板（侧栏端点 + 详情 + 文档信息），兼容旧 `endpoint`；ADMIN/PROVIDER 保留编辑入口、USER 结构不可达。全部落在 `frontend/src/features/catalog/detail/**`，未写 `components/**` / `editor/**`。

## 变更文件列表（writeSet）

| 路径 | REQ | 说明 |
|---|---|---|
| `frontend/src/features/catalog/detail/ProductDetailPage.vue` | CAT-004 / RBAC-001 / UX-007 | 接入类型只读区；编辑入口经 `isDetailEditEntryVisible` |
| `frontend/src/features/catalog/detail/components/TypeSpecificReadonly.vue` | CAT-004 | 按类型展示扩展字段；API 挂载只读 OpenAPI |
| `frontend/src/features/catalog/detail/components/OpenApiReadonlyPanel.vue` | API-001 / CAT-004 | 自研只读：legacy endpoint、swagger 文档信息、endpoints 列表/详情；无写控件 |
| `frontend/src/features/catalog/detail/utils/typeSpecificView.ts` | CAT-004 / API-001 / RBAC-001 | 字段抽取、API 视图派生、编辑入口可见性 |
| `frontend/src/features/catalog/detail/utils/typeSpecificView.spec.ts` | testScope | 四类型字段、旧 endpoint、角色入口、端点标签 |
| `frontend/src/features/catalog/detail/composables/useProductDetail.spec.ts` | testScope | 回归 + OTHER 扩展 + legacy API 加载 |

既有未改：`SensitiveId.vue`、`truncateSensitive.ts`、`useProductDetail.ts`（load 逻辑不变）。

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CAT-004** | 基础/供应商/产权/标签/简介/场景分组保留；API/DATASET/REPORT/OTHER 扩展字段只读；API endpoints + 文档信息 |
| **REQ-API-001** | 详情侧只读等价：method/path/summary/描述/参数/响应/可选 schema；无增删/导入写控件 |
| **REQ-RBAC-001** | `isDetailEditEntryVisible`：ADMIN/PROVIDER 显示「编辑」；USER 不渲染入口 |
| **REQ-UX-007** | 沿用既有 detail 卡片/令牌 scoped 样式；不写入 `frontend/src/components/**` |

## 验收点对照（PLAN-WSC-4.1 § TASK-WSC-303）

| 验收点 | 证据 |
|---|---|
| 既有分组保留 | `ProductDetailPage.vue` 基础信息/供应商/产权/标签/简介/场景未删 |
| API 只读 endpoints + 文档信息；无写控件 | `OpenApiReadonlyPanel.vue`；无 input/增删/Swagger 导入 |
| DATASET/REPORT/OTHER 扩展字段 | `buildTypeSpecificFields` + Vitest |
| 旧仅 `endpoint` 可读不报错 | `buildApiDetailView` + `useProductDetail` api-legacy 用例 |
| ADMIN/PROVIDER 有编辑入口；USER 无 | `isDetailEditEntryVisible` 单测；模板 `v-if="editEntryVisible"` |
| 不写 `components/**` | 本任务仅 `detail/**` |

## 自测命令与结果

```text
pnpm exec vitest run src/features/catalog/detail --reporter=default
```

| 文件 | Tests | 结果 |
|---|---|---|
| `useProductDetail.spec.ts` | 3 | **PASSED** |
| `typeSpecificView.spec.ts` | 8 | **PASSED** |
| 合计 | 11 | **全部通过** |

`vue-tsc --noEmit`：当前仓库报错位于 `frontend/src/features/catalog/editor/**`（并行 TASK-WSC-302 范围），**非本任务 writeSet**；detail 路径无新增诊断。

## SCOPE / denyModify

- **未修改** `editor/**`、`import/**`、`contracts/**`、`backend/**`、`frontend/src/api/**`、`frontend/src/components/**`
- **未修改** `ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`
- 本 DEV 报告按派发要求写入 `planning/tasks/DEV-TASK-WSC-303.md`
- 未兼任 codeReviewer；未自行标记 VERIFIED

## 已知边界 / 后续

- OpenAPI 只读 UI 自研于 detail，与 302 编辑 UI 允许信息层级对齐、像素级可不一致（§1.7）。
- E2E 覆盖归 TASK-WSC-304。
