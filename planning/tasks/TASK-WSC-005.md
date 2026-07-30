# 任务包 TASK-WSC-005

```yaml
taskId: TASK-WSC-005
planId: PLAN-WSC-1.1
snapshotId: SNAP-WSC-001
status: VERIFIED
wave: W4
goal: |
  产品详情只读分组；管理员/提供方新增编辑（DEC-WSC-001，提交后上架并调用存证适配产生版本）；
  管理员行业分类维护（DEC-WSC-003）；落实 OQ-004 与 PII/审计。
reqs: [REQ-CAT-004, REQ-CAT-005, REQ-CAT-006]
allowModify:
  - frontend/src/features/catalog/detail/**
  - frontend/src/features/catalog/editor/**
  - frontend/src/features/catalog/admin/**
  - backend/src/main/java/**/catalog/detail/**
  - backend/src/main/java/**/catalog/editor/**
  - backend/src/main/java/**/catalog/admin/**
  - backend/src/test/java/**/catalog/detail/**
  - backend/src/test/java/**/catalog/editor/**
  - backend/src/test/java/**/catalog/admin/**
denyModify:
  - contracts/**
  - frontend/src/api/**
  - "**/db/migration/**"
  - backend/src/main/java/**/chain/**
  - frontend/src/features/catalog/browse/**
  - backend/src/main/java/**/catalog/browse/**
  - frontend/src/features/overview/**
  - frontend/src/features/auth/**
  - frontend/src/features/shell/**
  - backend/src/main/java/**/overview/**
  - ai/**
  - product/**
  - planning/**
dependsOn: [TASK-WSC-004, TASK-WSC-006]
actorInstance: developer-wsc-005
codeReviewerMustDiffer: true
testerMustDiffer: true
```

## SCOPE_AMEND（编排批准，最小必要）

| 路径 | 原因 |
|---|---|
| `backend/.../security/StubWriteController.java` | 移除写占位，避免与真实 POST/PUT/DELETE 映射冲突 |
| `backend/.../catalog/browse/CatalogBrowseSeedStore.java` | 增加可变 API，使写路径与浏览共用内存目录 |
| `backend/.../catalog/browse/CatalogProduct.java` | 扩展详情/编辑所需字段（仍由 browse GET 序列化） |
| `backend/.../catalog/browse/CatalogBrowseService.java` | 序列化扩展字段；不改筛选语义 |
| `backend/.../chain/InMemoryChainStore.java` | 增加 `appendVersion`，供 005 注入写入；不改只读 API |
| `frontend/src/router/routes.ts` | 仅挂载 detail/editor/admin 组件；不改 browse/overview/chain 组件绑定 |
| `frontend/src/features/shell/components/WriteEntryDemo.vue` | 写入口跳转到新增/分类维护页（RBAC 可见性已存在） |
| `backend/.../security/AuthAuditLogger.java` | 增加产品提交/分类删除拒绝审计事件（§6.1） |

通过 DI 调用 `ChainAttestationPort`；**不得**改 `SimulatedChainAttestationPort` / `ChainController` 业务逻辑。

## 验收 / testScope

见 PLAN-WSC-1.1 TASK-WSC-005。同任务 review→test **串行**（MEM-ORCH-WSC-002）。

派发：2026-07-29，RUN-WSC-001 W4。
