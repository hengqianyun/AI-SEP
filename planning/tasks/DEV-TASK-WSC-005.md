# DEV-TASK-WSC-005

```yaml
taskId: TASK-WSC-005
actorInstance: developer-wsc-005
status: READY_FOR_REVIEW
completedAt: 2026-07-29T17:55:00+08:00
reqs: [REQ-CAT-004, REQ-CAT-005, REQ-CAT-006]
```

## 摘要

实现产品详情只读分组、新增/编辑（DEC-WSC-001 + 存证适配同事务）、管理员行业分类维护（DEC-WSC-003）、OQ-004「其他数据产品」无专属字段、§6.1 审计事件。

## SCOPE_AMEND 落地

| 路径 | 变更 |
|---|---|
| 删除 `StubWriteController` | 真实写控制器接管 POST/PUT/DELETE |
| `CatalogBrowseSeedStore` / `CatalogProduct` / `CatalogBrowseService` | 共享可变目录 + 详情字段序列化 |
| `InMemoryChainStore.appendVersion` | 005 注入写入上链版本 |
| `AuthAuditLogger` | PRODUCT_SUBMIT / CATEGORY_DELETE_REJECTED |
| `routes.ts` | 挂载 detail/editor/admin |
| `WriteEntryDemo.vue` | 跳转新增/分类维护 |

未改：`contracts/**`、`frontend/src/api/**`、`SimulatedChainAttestationPort` / `ChainController` 业务逻辑、browse UI。

## 主要交付

### 后端

- `catalog/editor`：`ProductEditorService` / `ProductEditorController`
- `catalog/admin`：`CategoryAdminService` / `CategoryAdminController`
- `catalog/detail/package-info.java`（GET 仍由 browse 提供）
- 测试：`ProductEditorIntegrationTest`、`CategoryAdminIntegrationTest`；更新 `SessionAuthIntegrationTest` 有效请求体

### 前端

- `catalog/detail/ProductDetailPage.vue` + composable/spec
- `catalog/editor/ProductEditorPage.vue` + composable/spec
- `catalog/admin/CategoryAdminPage.vue` + composable/spec

## 自测

```text
mvn test
pnpm exec vitest run src/features/catalog
```

后端全量与 catalog vitest 均通过（2026-07-29）。

## REQ 映射

| REQ | 要点 |
|---|---|
| REQ-CAT-004 | 详情分组完整；OTHER 无专属区；敏感信用代码截断+复制；有写权限显示编辑 |
| REQ-CAT-005 | 编码格式/冲突；新建 v1 四字段；编辑递增；适配失败无半成品；标签回车；取消不落库 |
| REQ-CAT-006 | 左右栏维护；待保存提示；挂载禁删 reason/productCount；非管理员不可达 |
