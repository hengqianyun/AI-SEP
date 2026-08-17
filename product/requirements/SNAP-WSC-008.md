# 接入端工作台（WSC）需求快照 V1.6 — 目录筛选 / 座序图联动 / 我的目录 / 企业多管理员

```yaml
snapshotId: SNAP-WSC-008
sourcePrd: product/prd/wsc-v1.6-catalog-filter-ux.md
sourceVersion: V1.6
runId: RUN-WSC-011
status: APPROVED
createdAt: 2026-08-17
approvedAt: 2026-08-17
approvedBy: productOwner
actorInstance: product-analyst-wsc-011-r1
requirementCount: 10
contractsTarget: wsc-contracts@2.3.2
previousSnapshot: SNAP-WSC-006
uxBaseline:
  snapshotId: SNAP-WSC-003
functionalBaseline:
  snapshotId: SNAP-WSC-006
  contracts: wsc-contracts@2.3.2
poLockedDefaults:
  browseRemoveFilterCards: true
  advancedFilterAlwaysVisible: true
  browseRemoveIndustryCategoryFilter: true
  browseCategoryControl: cascader-l1-l2
  seatMapL1Dropdown: true
  seatMapNoEnterpriseFilter: true
  mineScope: enterprise
  myCatalogMenu: ADMIN+PROVIDER
  catalogMaintenanceMenu: ADMIN-only-full-scope
  providerNoCatalogMaintenance: true
  fullChainNoEnterpriseFilter: true
  noEnterpriseAdminPage: true
  industryCategoryEnumReuse: INDUSTRY_CATEGORY_OPTIONS
  waves: [A, B, C]
changeFromPrevious:
  - reason: V1.6 浏览筛选去卡片与常显高级区、Cascader l1/l2；座序图 L1 下拉；用户企业归属；我的目录菜单；ADMIN 本企业 mine 写；目录维护仅 ADMIN 全量、PROVIDER 改我的目录
  - added: [REQ-CAT-014, REQ-CAT-015, REQ-CAT-017, REQ-CAT-018, REQ-CAT-019, REQ-SHELL-009, REQ-SHELL-010, REQ-USER-002, REQ-RBAC-002]
  - revised: [REQ-CAT-009, REQ-CAT-010, REQ-CAT-011, REQ-CAT-016, REQ-SHELL-001, REQ-RBAC-001, REQ-USER-001]
  - superseded-behavior:
      REQ-CAT-011: "高级筛选默认收起 + toggle + 浏览含 industryCategory → 由 REQ-CAT-014 取代"
      REQ-CAT-010: "mine 仅 PROVIDER + create_by 本人 → 由 REQ-CAT-016 取代"
      REQ-SHELL-001: "侧栏含目录维护 ADMIN+PROVIDER → 由 REQ-SHELL-009 取代（目录维护仅 ADMIN 全量；PROVIDER 改我的目录）"
  - inherited: [REQ-CAT-001..008, REQ-CAT-012, REQ-CAT-013, REQ-OVW-001..005, REQ-CHAIN-001, REQ-API-001, REQ-UX-001..011]
```

> **权威声明**：本文件为 RUN-WSC-011 需求快照（`snapshotId: SNAP-WSC-008`），`status: DRAFT`。
>
> **不改写旧结论**：SNAP-WSC-001..006 正文结论保持不变；本快照以增量/修订 REQ 表达 V1.6 范围。
>
> **PO 输入**：2026-08-17 RUN-WSC-011 `HUMAN_INPUT`（productOwner V1.6 intake）。

## 业务目标

见 `product/prd/wsc-v1.6-catalog-filter-ux.md`。

成功标准（可观察）：

- 浏览：无业务视图/业务大类卡片；高级筛选常显；无行业类别筛选项；Cascader 联动 `l1`/`l2`（REQ-CAT-014）。
- 座序图：标题右 L1 下拉默认「全部」；卡片随 L1 过滤；无企业筛选（REQ-CAT-015）。
- 企业/RBAC：用户归属企业、一企多 ADMIN；ADMIN+PROVIDER「我的数据产品」本企业增改导（REQ-CAT-016、REQ-USER-002、REQ-RBAC-002）。
- 导航：ADMIN 同时有「目录维护」（全量）与「我的目录」（本企业）；PROVIDER 仅「我的目录」、无「目录维护」（REQ-CAT-017、REQ-SHELL-009）。
- 全链目录全量无企业过滤；编辑/导入仍用 `INDUSTRY_CATEGORY_OPTIONS`（REQ-CAT-018、REQ-CAT-019）。
- V1.5 不回退：REQ-CAT-012 企业名搜与未分类置底、REQ-CAT-013 维护页 Pagination/Cascader/全选、座序图核心交互。

## 角色与权限摘要

| 角色 | 全链目录 | 座序图 | 我的数据产品 | 我的目录 | 目录维护（侧栏） | 分类维护 | 用户管理 |
|---|---|---|---|---|---|---|---|
| ADMIN | 只读全量 | 有 + L1 下拉 | 增改导；**本企业** | 有；**本企业** | **有**；**全量** | 有 | 有 |
| PROVIDER | 只读全量 | 有 + L1 下拉 | 增改导；**本企业** | 有；**本人 create_by** | **否** | 无 | 无 |
| USER | 只读全量 | 有 + L1 下拉 | 不可见 | 不可见 | 不可见 | 无 | 无 |

> **与 V1.5 差异**（PO-CONFIRM-011-001）：V1.5 ADMIN 无 mine 写、mine 仅 PROVIDER 且 create_by 本人、目录维护 ADMIN+PROVIDER 侧栏可见。V1.6 ADMIN 保留「目录维护」全量 + 新增「我的目录」本企业；PROVIDER 仅「我的目录」、不可见「目录维护」（见上表）。

## 优先级政策

- `P0`：本版成功标准相关能力；缺失则不得按 V1.6 发布。
- `P1`：REQ-SHELL-010 企业信息只读展示；可随 Wave C 交付，不单独阻塞 A/B 核心路径。

## 需求清单

| id | priority | change | summary | acceptance | wave |
|---|---|---|---|---|---|
| REQ-CAT-014 | P0 | 新增 | 目录浏览筛选区重构 | 无列表区业务视图/业务大类卡片；无「高级筛选」toggle、面板常显；高级区无行业类别、列表不传 `industryCategory`；Cascader 业务视图→业务大类 → `l1`/`l2`；全链与 mine 浏览同源（mine 无座序图）；其余 Ant 筛选项继承 V1.5 | A |
| REQ-CAT-015 | P0 | 修订 | 座序图 L1 下拉与卡片联动 | 标题右下拉含「全部」+ L1 列表；选 L1 后下方 L2 卡片仅该 L1；默认「全部」同 V1.5 Top5；无企业筛选 UI/API；hover/leave/空态/loading 不回退；仅公共目录挂载 | A+B |
| REQ-CAT-016 | P0 | 修订 | 我的数据产品：本企业 + ADMIN 可写 | ADMIN+PROVIDER 可见且可增改导；列表 **本企业** 产品；公共全链无写入口；无座序图 | B |
| REQ-CAT-017 | P0 | 新增 | 我的目录（维护 UX 复用） | ADMIN+PROVIDER 可见；UX 同 REQ-CAT-013 目录维护页；ADMIN 范围本企业、PROVIDER 范围 create_by 本人；PROVIDER 无「目录维护」；ADMIN 同时有「目录维护」（全量）与「我的目录」（本企业），两入口路由/范围独立 | B+C |
| REQ-CAT-018 | P0 | 新增 | 全链目录全量无企业过滤 | 全链列表与座序图统计为全平台口径；用户企业不影响全链结果；REQ-CAT-012 不回退 | B |
| REQ-CAT-019 | P0 | 新增 | 行业类别枚举非浏览场景复用 | 编辑/导入/详情仍用 `INDUSTRY_CATEGORY_OPTIONS`；浏览筛选无该字段；产品字段可读写 | A+C |
| REQ-SHELL-009 | P0 | 修订 | 导航：ADMIN 双入口；PROVIDER 仅我的目录 | 「数据目录」可收起；子项：全链、**我的目录**（ADMIN+PROVIDER）、我的数据产品（ADMIN+PROVIDER）；**ADMIN** 另有「目录维护」（全量）；**PROVIDER** 无「目录维护」；分类维护/用户管理仍 ADMIN | C |
| REQ-SHELL-010 | P1 | 新增 | 侧栏企业信息只读展示 | 展示当前用户企业名称/标识；无企业切换与编辑 | C |
| REQ-USER-002 | P0 | 修订 | 用户企业归属与多 ADMIN | 用户含企业字段；同企业多名 ADMIN；会话可解析 enterprise 供 scope | B |
| REQ-RBAC-002 | P0 | 修订 | 写权限与企业/本人范围 | ADMIN mine 写不再 403；mine=本企业；我的目录 ADMIN=本企业、PROVIDER=本人；目录维护 ADMIN=全量；API 与菜单一致 | B |

### 继承声明（不逐条重写验收）

以下 REQ **行为继承** SNAP-WSC-006 / 既有基线，本版 **不得削弱**：

- `REQ-CAT-001..008`：浏览结构、详情、编辑、导入、分类维护等
- `REQ-CAT-012`：`supplierName` 模糊搜、未分类 API 全局置底
- `REQ-CAT-013`：维护页 Pagination / Cascader / 本页全选（能力迁至「我的目录」入口，行为不变）
- `REQ-OVW-001..005`、`REQ-CHAIN-001`、`REQ-API-001`
- `REQ-UX-001..011`（SNAP-WSC-003）UX 令牌不回退

### 被取代的 V1.5 行为（实现勿双轨）

| 原 REQ | V1.5 行为 | V1.6 取代方 |
|---|---|---|
| REQ-CAT-011 | 高级筛选默认收起 + toggle；浏览含 industryCategory | REQ-CAT-014 |
| REQ-CAT-010 | mine 仅 PROVIDER；create_by 本人 | REQ-CAT-016 |
| REQ-SHELL-001 §目录维护 | 侧栏目录维护 ADMIN+PROVIDER | REQ-SHELL-009 + REQ-CAT-017 |

## 交付波次（计划约束）

| Wave | 内容 | 契约 | 备注 |
|---|---|---|---|
| **A** | REQ-CAT-014 浏览筛选 UX；REQ-CAT-015 座序图 L1 下拉（FE 可先接 query）；REQ-CAT-019 浏览侧去 industryCategory | 可选 `GET /catalog/l2-distribution?l1=` | 无企业 scope 亦可验收浏览/座序图 UI |
| **B** | REQ-USER-002、REQ-RBAC-002；REQ-CAT-016/017/018 企业 scope API | **≥2.3.2** 增量（user.enterprise、list scope） | 关键路径 |
| **C** | REQ-SHELL-009/010 导航；REQ-CAT-017 路由与维护页复用；REQ-CAT-013 行为回归 | 无或随 B | 可与 B 部分并行 |

## Out of Scope

- 企业管理页（企业 CRUD、切换、配额）
- 独立超管后台与本阶段超管/企管菜单拆分（ADMIN 保留现有「目录维护」全量入口）
- 超级管理员 vs 企业管理员身份与菜单拆分
- 座序图企业筛选 UI 或按企业过滤
- 座序图点击联动列表筛选
- 浏览页 industryCategory 筛选（产品字段与其他页面保留）
- 跨页全选统一维护
- 用户硬删除、密码重置邮件、OAuth、完整多租户
- Downloads React 仓改造
- 削弱 V1.5：未分类置底、supplierName 搜、座序图存在、mine 无座序图

## 开放问题

| id | 状态 | 说明 |
|---|---|---|
| OQ-V16-001 | OPEN（non-blocking） | 企业实体最小字段与 `sys_user` 关联方式；PO 已锁行为 |
| OQ-V16-002 | OPEN（non-blocking） | 历史无企业用户迁移/默认企业策略 |
| OQ-V16-003 | OPEN（non-blocking） | 座序图选 L1 时 `totalProducts` 是否改为 L1 子集计数（建议与卡片同口径） |
| OQ-V16-004 | CLOSED（PO-CONFIRM-011-001） | ADMIN 侧栏「目录维护」是否保留 → **保留**（全量）+ 新增「我的目录」（本企业）；PROVIDER 仅「我的目录」 |

> **PO 待办**：~~本快照为 DRAFT。请以 Product Owner 身份回复「批准快照」~~ **已批准**（2026-08-17，identity=`productOwner`；事件 `SNAPSHOT_APPROVED`）。
