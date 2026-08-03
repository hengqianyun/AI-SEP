# DEV-TASK-WSC-502

```yaml
taskId: TASK-WSC-502
runId: RUN-WSC-007
actorInstance: developer-wsc-502
decision: DEC-WSC-006
status: READY_FOR_REVIEW
completedAt: 2026-08-03T13:01:00+08:00
reqs:
  - REQ-CAT-002
  - REQ-CAT-008
```

## 摘要

HOTFIX：无 `l3CategoryId` 的产品（含导入）在目录浏览中全部归入单一分节 **「未分类数据」**；禁止按 `industryCategory`（如「建筑业」）拆成伪节点/分节。`industryCategory` 仍可在产品行/详情展示，不作分节 key。

## writeSet 变更

| 路径 | 说明 |
|---|---|
| `frontend/.../browse/composables/useCatalogBrowse.ts` | `groupProductsByL3`：无 l3 → 统一 `__uncategorized__`，标题「未分类数据」；去掉 `ic:*` 分桶 |
| `frontend/.../browse/composables/useCatalogBrowse.spec.ts` | 原「按 industryCategory 分桶」改为断言统一「未分类数据」 |
| `design/decisions/DEC-WSC-006.md` | 补充澄清已存在（PO：未分类数据；门类不作分节） |

## 后端

`CatalogBrowseService` 仅透出 `industryCategory` 字段，无服务端分节/分组逻辑 → **未改后端**。

## REQ 映射

| REQ | 实现 |
|---|---|
| **REQ-CAT-002** | 浏览分节：有 l3 按三级子类；无挂载单一「未分类数据」 |
| **REQ-CAT-008** | 导入产品无 l3 时进入「未分类数据」，不因门类名伪挂载 |

## 验收对照

1. 无 `l3CategoryId`（含不同 `industryCategory` / 无门类）→ 单一分节，标题「未分类数据」
2. 不生成 `ic:*` 伪节点；「建筑业」等不作分节标题
3. 有 `l3CategoryId` 仍按 L3 子类分节
4. Vitest browse 通过

## 自测命令与结果

```text
cd frontend
pnpm exec vitest run src/features/catalog/browse/composables/useCatalogBrowse.spec.ts
```

| 命令 | 结果 |
|---|---|
| useCatalogBrowse.spec.ts | **PASSED**（20 tests） |

未自行标记 VERIFIED；待独立 codeReviewer / tester。
