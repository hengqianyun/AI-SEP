# 任务包 TASK-WSC-402（HOTFIX）

```yaml
taskId: TASK-WSC-402
runId: RUN-WSC-006
status: VERIFIED
wave: HOTFIX
kind: HOTFIX
goal: |
  产品详情页与新增/编辑页高分辨率适配：去掉过窄 max-width，流体铺满主区，
  与总览/目录页宽屏利用一致，避免右侧大片空挡。
reqs: [REQ-CAT-004, REQ-CAT-005, REQ-UX-007]
dependsOn: [TASK-WSC-401]
blocksRelease: true
actorInstance: developer-wsc-402
```

## 现象

- `ProductDetailPage.vue`：`.detail-page-body { max-width: 1100px }`
- `ProductEditorPage.vue`：`.editor-body { max-width: 900px }` / `.wide { max-width: 1100px }`
- 宽屏下内容居中偏窄，右侧空挡过大（同 401 总览问题）。

## 验收

- 详情、新增、编辑在常见宽屏下主内容流体铺满（合理 padding），无「中间一条、右侧空洞」
- API 编辑宽布局仍可用（OpenAPI 面板不挤爆）
- 小屏既有 `@media` 不回退
- Vitest 不破坏；可补布局相关断言或说明

## writeSet

- `frontend/src/features/catalog/detail/**`
- `frontend/src/features/catalog/editor/**`（仅布局/样式；勿回退 401 编码生成）
- 对应 `*.spec.ts`
- `planning/tasks/DEV-TASK-WSC-402.md`

## denyModify

- `backend/**`；`contracts/**`；`frontend/src/api/**`
- `portal/**`；`browse/**`；`overview/**`（除非共享 token）
- `product/**`；`planning/approved/**`；`ai/runs/**/state.yaml`

## 派发

- 2026-08-03T10:40:00+08:00 PO：详情/编辑未做分辨率适配；暂缓发布
