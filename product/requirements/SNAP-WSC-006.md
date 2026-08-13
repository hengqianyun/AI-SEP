# 接入端工作台（WSC）需求快照 V1.5 — 目录浏览 / 导航 / 维护 UX

```yaml
snapshotId: SNAP-WSC-006
sourcePrd: product/prd/wsc-v1.5-catalog-ux.md
sourceVersion: V1.5
runId: RUN-WSC-009
status: RELEASED
createdAt: 2026-08-11
approvedAt: 2026-08-12
approvedBy: productOwner
releasedAt: 2026-08-13
releasedBy: productOwner
actorInstance: product-analyst-wsc-009-draft
requirementCount: 4
contractsTarget: wsc-contracts@2.3.2
previousSnapshot: SNAP-WSC-005
uxBaseline:
  snapshotId: SNAP-WSC-003
functionalBaseline:
  snapshotId: SNAP-WSC-005
  contracts: wsc-contracts@2.2.0
poLockedDefaults:
  enterpriseNameSearchField: supplierName
  uncategorizedSort: api-global-bottom  # 无 l3CategoryId 殿后；次键 updatedAt
  waves: [A, B, C]
changeFromPrevious:
  - reason: V1.5 目录浏览文案与控件、导航收纳、维护 Pagination/Cascader/全选；企业名搜与未分类全局置底
  - added: [REQ-CAT-011, REQ-CAT-012, REQ-CAT-013]
  - revised: [REQ-SHELL-001]
  - inherited: [REQ-RBAC-001, REQ-CAT-001..010, REQ-USER-001, REQ-OVW-001..005, REQ-CHAIN-001, REQ-API-001, REQ-UX-001..011]
releaseAmend:
  - id: HOTFIX-SHELL-002
    at: 2026-08-12
    contracts: wsc-contracts@2.3.2
    overrides: |
      推翻 V1.5 计划阶段「目录维护仅 ADMIN」纠偏；恢复 V1.4 语义：目录维护 ADMIN+PROVIDER（PROVIDER ownCreateBy）；
      分类维护仍仅 ADMIN；一级「数据目录」须可收起/展开。
```

> **权威声明**：本文件为 RUN-WSC-009 需求快照（`snapshotId: SNAP-WSC-006`），`status: RELEASED`。
>
> **PO 确认记录**（2026-08-12）：口头「批准快照」；identity=`productOwner`；事件 `SNAPSHOT_APPROVED`。
>
> **不改写旧结论**：SNAP-WSC-001..005 正文结论保持不变；本快照以增量/修订 REQ 表达 V1.5 范围。

## 业务目标

见 `product/prd/wsc-v1.5-catalog-ux.md`。

成功标准（可观察）：

- 目录浏览筛选项文案与高级筛选交互符合 1.1–1.4；搜索/下拉为 ant-design-vue（1.5）。
- 企业名称按产品 `supplierName` 模糊命中；未分类在 API 分页中全局垫底（1.6–1.7）。
- 侧栏一级「数据目录」可收起/展开，下含「全链数据目录 / 目录维护 / 我的数据产品」（2.1）；角色可见性按 RELEASE amend（维护 ADMIN+PROVIDER）。
- 目录维护：Pagination 可跳页；Cascader 筛可选父、维护必 L3；本页全选 + 统一维护（3.1–3.3）。
- Wave A 无契约变更；Wave B 契约 ≥2.3.0（RELEASE amend → **2.3.2**）；RBAC/座序图/用户管理不回退、不扩 scope（维护 PROVIDER 范围仍 ownCreateBy）。

## 角色与权限摘要

与 SNAP-WSC-005 **相同**基线，经 **HOTFIX-SHELL-002 RELEASE amend** 明确目录维护可见性（覆盖计划阶段 ADMIN-only 纠偏）：

| 角色 | 可观察能力 |
|---|---|
| 管理员 | 浏览+座序图；分类维护；目录维护（全量）；用户管理；无产品增改导 |
| 提供方 | 浏览+座序图；目录维护（本人 create_by / own 产品）；我的数据产品（本人增改导） |
| 普通用户 | 只读浏览+座序图 |

> **RELEASE amend（权威）**：目录维护 = ADMIN + PROVIDER（PROVIDER 不得改他人产品；分类维护仍仅 ADMIN）。本 amend 覆盖此前「维护仅 ADMIN」的 SNAP/PRD 纠偏表述。

## 优先级政策

- `P0`：本版成功标准相关能力；缺失则不得按 V1.5 发布。
- 本快照所列修订/新增 REQ 均为 **P0**。

## 需求清单

| id | priority | change | summary | acceptance | wave |
|---|---|---|---|---|---|
| REQ-CAT-011 | P0 | 新增 | 目录浏览：文案 + 高级筛选 + Ant Design 控件（1.1–1.5） | 「空间」→「业务视图」；「行业」→「业务大类」；「行业分类」→「行业类别」（query 名仍 `industryCategory`）；筛选区默认收起，业务大类行提供「高级筛选」按钮；搜索框/下拉使用 ant-design-vue；公共目录与我的数据产品同源浏览页行为一致 | A+B |
| REQ-CAT-012 | P0 | 新增 | 企业名模糊搜 + 未分类 API 全局置底（1.6–1.7） | `listProducts` 支持按产品 **`supplierName`** 模糊检索（契约 ≥2.3.0）；排序：有 `l3CategoryId` 在前，无 L3（未分类）殿后，次键 `updatedAt`；分页跨页仍保持该全局顺序；FE 可将「未分类数据」区钉在列表末 | B |
| REQ-SHELL-001 | P0 | 修订 | 导航收纳「数据目录」（2.1）+ 可收起 | 一级菜单「数据目录」可收起/展开（aria-expanded；收起时隐藏子项，组标签可点展开）；子项：全链数据目录、目录维护（ADMIN+PROVIDER）、我的数据产品（PROVIDER）；USER 维护隐藏且深链不可达；分类维护仍仅 ADMIN | A |
| REQ-CAT-013 | P0 | 新增 | 目录维护：Pagination / Cascader / 全选统一维护（3.1–3.3） | 使用 ant-design-vue Pagination（含页码跳转）；分类 Cascader：筛选允许选父节点并映射 l1/l2/l3；编辑/统一维护必须选到 L3；表头本页全选 +「统一维护」批量入口（跨页全选 Out of Scope） | C |

### 继承声明（不逐条重写验收）

以下 REQ **行为继承** SNAP-WSC-005 / 既有基线，本版不得削弱：

- 功能/RBAC：`REQ-RBAC-001`、`REQ-CAT-001..010`、`REQ-USER-001`、`REQ-OVW-001..005`、`REQ-CHAIN-001`、`REQ-API-001`
- UX：`REQ-UX-001..011`（SNAP-WSC-003）**不回退**；本版可在既有令牌内引入 ant-design-vue 控件，**不**重做令牌体系。

## 交付波次（计划约束）

| Wave | 内容 | 契约 | 备注 |
|---|---|---|---|
| **A** | REQ-CAT-011 文案与高级筛选收起；REQ-SHELL-001 导航收纳 | 无 | 可先验收 UX |
| **B** | REQ-CAT-011 Ant 控件部分；REQ-CAT-012 企业名搜 + 未分类置底 | **≥2.3.0** + BE | 锁定字段 `supplierName`；排序 API 全局垫底 |
| **C** | REQ-CAT-013 维护 Pagination / Cascader / 全选统一维护 | 无 | 跨页全选不做 |

## Out of Scope

- 用户管理功能增改、硬删除用户、密码重置 / OAuth / 多租户
- 跨页全选统一维护
- 座序图交互变更、链存证逻辑、导入模板/列映射变更
- Downloads React 仓改造
- 将会话/企业主体 `enterpriseName` 当作目录产品检索字段（本版明确用产品 `supplierName`）

## 开放问题

| id | 状态 | 说明 |
|---|---|---|
| OQ-V15-001 | CLOSED（Run 创建默认） | 企业名称 = `supplierName` |
| OQ-V15-002 | CLOSED（Run 创建默认） | 未分类置底 = API 分页全局垫底 |

> **PO 待办**：本快照仍为 DRAFT。请以 Product Owner 身份回复「批准快照」或等价确认后，方可记入 `SNAPSHOT_APPROVED` 并进入计划起草。

