# 接入端工作台（WSC）需求快照 V1.7 — 交易订单（链内一事一议简易流程）

```yaml
snapshotId: SNAP-WSC-009
sourcePrd: product/prd/wsc-v1.7-transaction-orders.md
sourceVersion: V1.7
runId: RUN-WSC-012
status: APPROVED
createdAt: 2026-08-19
approvedAt: 2026-08-19
approvedBy: productOwner
actorInstance: product-analyst-wsc-012-r1
requirementCount: 21
contractsTarget: wsc-contracts@2.3.2
previousSnapshot: SNAP-WSC-008
uxBaseline:
  snapshotId: SNAP-WSC-003
functionalBaseline:
  snapshotId: SNAP-WSC-008
  contracts: wsc-contracts@2.3.2
poLockedDefaults:
  successMetric: order-traceability
  scopeEndsAt: contract-reached
  inChainCreateOnly: true
  noExternalPushCreate: true
  unifiedNotice: platform-wide
  contractBody: signed-attachment-primary
  unifiedContractConfirmPage: true
  attachment: word-pdf-20mb-single-unlimited-retention
  attachmentSecurity: whitelist-plus-virus-scan
  downloadByRole: true
  currency: CNY-2dp
  taxInclusiveEqualsExclusive: true
  keepBothTaxFields: true
  noRejectReturnTimeoutStates: true
  chainAttestation: mock
  privacy: mask-by-role
  list: pagination-only
  pagination: jumper-and-pageSize
  pageSizeDefaultTiers: [10, 20, 50, 100]
  stateIrreversible: true
  cancel: demand-provider-admin-on-three-open-states
  adminCanActAsProvider: true
  providerScope: current-enterprise-as-provider
  userScope: own-as-demand
  adminListScope: all
  productSnapshot: at-order-time
  digitalContract: master-data-plus-new-version-on-change
  noNotifySla: true
  frontendRule: RULE-GLOBAL-FRONTEND
  waves: [A, B, C]
changeFromPrevious:
  - reason: 新增交易订单模块（链内一事一议简易流程，止于合约达成）；开放原壳层交易订单占位；继承 V1.6 目录/壳层/用户企业能力且不得削弱
  - added: [REQ-WSC-ORDER-001, REQ-WSC-ORDER-002, REQ-WSC-ORDER-003, REQ-WSC-ORDER-004, REQ-WSC-ORDER-005, REQ-WSC-ORDER-006, REQ-WSC-ORDER-007, REQ-WSC-ORDER-008, REQ-WSC-ORDER-009, REQ-WSC-ORDER-010, REQ-WSC-ORDER-011, REQ-WSC-ORDER-012, REQ-WSC-ORDER-013, REQ-WSC-ORDER-014, REQ-WSC-ORDER-015, REQ-WSC-ORDER-016, REQ-WSC-ORDER-017, REQ-WSC-ORDER-FE-001, REQ-WSC-ORDER-FE-002, REQ-WSC-ORDER-FE-003, REQ-SHELL-011]
  - revised: [REQ-SHELL-001]
  - superseded-behavior:
      REQ-SHELL-001: "侧栏「交易订单」点击提示本版本未开放 → 由 REQ-SHELL-011 + REQ-WSC-ORDER-001 取代为角色化真实列表"
  - inherited: [REQ-CAT-001..008, REQ-CAT-012..019, REQ-SHELL-009, REQ-SHELL-010, REQ-USER-002, REQ-RBAC-002, REQ-OVW-001..005, REQ-CHAIN-001, REQ-API-001, REQ-UX-001..011]
```

> **权威声明**：本文件为 RUN-WSC-012 需求快照（`snapshotId: SNAP-WSC-009`），`status: APPROVED`。
>
> **不改写旧结论**：SNAP-WSC-001..008 正文结论保持不变；本快照以增量 REQ 表达 V1.7 范围。
>
> **PO 输入**：2026-08-19 产品确认稿 `temp/product/prd/wsc-v1.7-transaction-orders-draft.md`（补充项 1～20）。Demo 仅作交互参考，正式规则以 PO 填写为准。
>
> **前端**：写成功 toast、重要操作二次确认、分页跳页与 pageSize 以 `RULE-GLOBAL-FRONTEND` 为准，不另定第三套规则。

## 业务目标

见 `product/prd/wsc-v1.7-transaction-orders.md`。

成功标准（可观察）：

- **订单可追踪率**：平台内创建的订单从创建到当前状态，详情时间线完整；创建/确认订单/提交合约/确认合约/取消均有 mock 上链记录（REQ-WSC-ORDER-016、010、006、012）。
- 角色范围：USER「我的订单」仅本人需求方订单；PROVIDER「交易订单」仅本企业作为提供方；ADMIN 全部订单且可代提供方确认订单、确认合约、取消（REQ-WSC-ORDER-001、011，REQ-SHELL-011）。
- 主流程：待确认订单 → 待上传合约 → 待确认合约 → 合约已达成；状态不可回退；本期止于合约达成（REQ-WSC-ORDER-010、015）。
- 未完成三态三方可取消，须二次确认，进入已取消且不回退（REQ-WSC-ORDER-012、FE-002）。
- 产品详情始终为下单快照；签署附件为主（Word/PDF，≤20MB，一份）；统一合约确认页；平台统一须知（REQ-WSC-ORDER-004、009、013、015）。
- 列表仅分页（跳页 + pageSize）；写成功 toast（REQ-WSC-ORDER-007、FE-001、FE-003）。
- **不回退** V1.6：目录筛选、座序图、我的数据产品、我的目录、目录维护、用户企业归属与壳层目录菜单。

## 角色与权限摘要

| 角色 | 订单菜单 | 列表范围 | 确认订单 / 确认合约 | 上传合约与交易信息 | 取消（未完成三态） | V1.6 目录/壳层 |
|---|---|---|---|---|---|---|
| USER | 我的订单 | 本人作为需求方 | 否 | 待上传合约时，仅本人 | 可 | 继承 SNAP-WSC-008 |
| PROVIDER | 交易订单 | 当前企业作为提供方 | 仅本企业订单 | 否 | 可（本企业订单） | 继承；不可代其他企业 |
| ADMIN | 交易订单 | 平台全部 | **可代提供方**（任意企业订单） | 否 | 可代取消 | 继承 |

## 优先级政策

- `P0`：本版成功标准与链内主路径；缺失则不得按 V1.7 发布交易订单。
- `P1`：REQ-WSC-ORDER-005 外部跳转（非建单）、REQ-WSC-ORDER-017 数字合约版本展示；不单独阻塞主流程可追踪验收。

## 需求清单

| id | priority | change | summary | acceptance | wave |
|---|---|---|---|---|---|
| REQ-WSC-ORDER-001 | P0 | 新增 | 角色化订单菜单与数据范围 | USER「我的订单」仅本人需求方订单；PROVIDER 仅本企业提供方订单且不可代其他企业；ADMIN 可看全部并可代确认订单、代确认合约、代取消；无权限深链不可达；原「未开放」占位对订单不再出现 | A |
| REQ-WSC-ORDER-002 | P0 | 新增 | 产品订购入口 | 订购入口展示产品名称、编码、类型、提供方、来源平台、访问地址、当前订购人及所属单位 | A |
| REQ-WSC-ORDER-003 | P0 | 新增 | 订购方式本期边界 | 产品允许简易流程时可链内建本地订单；不允许则不可链内建单；外部跳转不建单、不接收外部推送 | A |
| REQ-WSC-ORDER-004 | P0 | 新增 | 订购须知确认 | 未勾选平台统一须知不能提交链内订购；可查看须知正文 | A |
| REQ-WSC-ORDER-005 | P1 | 新增 | 外部平台跳转（非建单） | 有地址则打开；无地址提示未配置；列表不因此新增订单 | C |
| REQ-WSC-ORDER-006 | P0 | 新增 | 链内订单创建与快照 | 备注为空不能提交；成功后有订单号、待确认订单、时间线与 mock 上链；详情产品为下单快照；成功 toast | A |
| REQ-WSC-ORDER-007 | P0 | 新增 | 列表查询与分页 | 关键词与状态筛选生效；空态「暂无订单数据」；可跳页、可改 pageSize 且改后回第 1 页；符合 RULE-GLOBAL-FRONTEND §3 | A |
| REQ-WSC-ORDER-008 | P0 | 新增 | 列表字段与查看 | 九组字段 + 查看进入对应详情；姓名/用户名/单位/信用代码/连接器/合约等按角色脱敏 | A |
| REQ-WSC-ORDER-009 | P0 | 新增 | 订单详情分区 | 标题含订单号；可返回列表；产品区为快照；可见订单、参与方、附件、时间线、mock 上链、数字合约版本（受脱敏与下载限制） | A |
| REQ-WSC-ORDER-010 | P0 | 新增 | 链内主状态流转 | 确认订单→待上传合约→提交附件与交易信息→待确认合约→统一确认页→合约已达成；不可回退；达成后无支付/交付主流程 | B |
| REQ-WSC-ORDER-011 | P0 | 新增 | 状态动作权限 | 确认订单/确认合约：PROVIDER 本企业、ADMIN 可代；上传仅需求方本人；取消为未完成三态三方；无权限不显示按钮且越权被拒 | B |
| REQ-WSC-ORDER-012 | P0 | 新增 | 取消旁路 | 三未完成态可取消；须二次确认；取消后已取消且不回退；时间线与 mock 上链有取消记录；达成/已取消不可再取消 | B |
| REQ-WSC-ORDER-013 | P0 | 新增 | 签署附件与交易信息 | 一份 Word/PDF≤20MB；白名单+病毒扫描；无合格附件不能进待确认合约；在线文本不能替代附件；按角色下载；存储无期限 | B |
| REQ-WSC-ORDER-014 | P0 | 新增 | 计费与金额 | 人民币两位小数；单价>0；缺单位或数量不能提交；明细与小计正确；含税未税本期同价且保留两字段 | B |
| REQ-WSC-ORDER-015 | P0 | 新增 | 统一合约确认页 | 同一页展示交易信息、统一须知、签署附件；未勾选不能确认；确认后合约已达成；无独立第二套标准合约确认路径 | B |
| REQ-WSC-ORDER-016 | P0 | 新增 | 时间线与 mock 上链 | 创建及各状态变更均写时间线与 mock 上链；列表上链次数与详情记录数一致；不依赖真实链网、不以真链成败阻断状态 | B |
| REQ-WSC-ORDER-017 | P1 | 新增 | 数字合约版本 | 按下单主数据生成初版；相关变更出新版本并保留历史；详情可见当前版本；无主数据空值不阻断下单 | C |
| REQ-WSC-ORDER-FE-001 | P0 | 新增 | 写成功 toast | 创建/确认订单/提交合约/确认合约/取消成功有 toast；失败无成功 toast；只读成功不必 toast（RULE-GLOBAL-FRONTEND §1） | A |
| REQ-WSC-ORDER-FE-002 | P0 | 新增 | 重要操作二次确认 | 取消须先确认再请求；未确认状态不变；后续删除/禁用同样二次确认（RULE-GLOBAL-FRONTEND §2） | B |
| REQ-WSC-ORDER-FE-003 | P0 | 新增 | 分页跳页与 pageSize | 可输入页码跳转；可更换 pageSize 并重拉且回第 1 页；仅翻页不改 size（RULE-GLOBAL-FRONTEND §3） | A |
| REQ-SHELL-011 | P0 | 修订 | 开放交易订单/我的订单导航 | USER 见我的订单；PROVIDER/ADMIN 见交易订单；V1.6 数据目录收纳与我的目录/目录维护/我的数据产品可见性不回退 | A |

### 继承声明（不逐条重写验收）

以下 REQ **行为继承** SNAP-WSC-008 / 既有基线，本版 **不得削弱**：

- `REQ-CAT-001..008`：浏览结构、详情、编辑、导入、分类维护等
- `REQ-CAT-012`：`supplierName` 模糊搜、未分类 API 全局置底
- `REQ-CAT-013`：维护页 Pagination / Cascader / 本页全选
- `REQ-CAT-014..019`：V1.6 浏览筛选、座序图 L1、mine 本企业、我的目录、全链无企业过滤、行业类别枚举复用
- `REQ-SHELL-009`、`REQ-SHELL-010`：目录双入口导航、侧栏企业只读
- `REQ-USER-002`、`REQ-RBAC-002`：用户企业归属、写权限企业范围
- `REQ-OVW-001..005`、`REQ-CHAIN-001`、`REQ-API-001`
- `REQ-UX-001..011`（SNAP-WSC-003）UX 令牌不回退

### 被取代的既有行为（实现勿双轨）

| 原 REQ | 既有行为 | V1.7 取代方 |
|---|---|---|
| REQ-SHELL-001 §交易订单占位 | 侧栏可见但点击提示本版本未开放 | REQ-SHELL-011 + REQ-WSC-ORDER-001 |

## 交付波次（计划约束）

| Wave | 内容 | 契约 | 备注 |
|---|---|---|---|
| **A** | 菜单范围、订购入口/须知/创建、列表分页字段、详情快照只读、写成功 toast | **≥2.3.2** 增量（订单读/建） | 可观察：能下单、能按角色看列表 |
| **B** | 主流转、权限、附件计费、统一确认、取消+二次确认、时间线与 mock 上链 | 同上增量（写状态/附件） | 可追踪率关键路径 |
| **C** | 数字合约版本、外部跳转非建单、脱敏对齐 | 无或随 B | P1 与展示 |

## Out of Scope

- 外部成交回传建单（可保留跳转入口）
- 拒绝、退回、撤回、超时关闭等额外状态
- 通知、待办、催办、处理时限
- 真实上链与上链失败补偿（本期 mock）
- 支付、退款、分账、发票、对账、交付、履约、售后
- 列表导出、批量操作、日期范围筛选
- 合同司法效力、电子签章平台对接细则
- 同一企业内订单共享、协作、转交
- 企业管理页、超管/企管菜单拆分（继承 V1.6）
- 削弱 V1.6 目录/座序图/我的数据产品/我的目录/目录维护/用户企业能力
- 将 Demo 示例金额、随机哈希、固定企业信息当作正式规则

## 开放问题

| id | 状态 | 说明 |
|---|---|---|
| OQ-V17-PO-001 | CLOSED | 管理员可代提供方确认订单、确认合约、取消；仅管理员可代 |
| OQ-V17-PO-002 | CLOSED | 提供方仅看当前企业作为提供方的订单 |
| OQ-V17-PO-003 | CLOSED | 三方可取消（未完成三态）；须二次确认；无需对方同意 |
| OQ-V17-PO-004 | CLOSED | 外部推送建单本期不做；只做平台内创建 |
| OQ-V17-PO-005 | CLOSED | Word/PDF，20MB，一份，无限期，白名单+病毒扫描，按角色下载 |
| OQ-V17-PO-006 | CLOSED | 人民币两位小数；含税未税同价，表留两字段 |
| OQ-V17-PO-007 | CLOSED | 最小流程，不增加拒绝/退回/超时状态 |
| OQ-V17-PO-008 | CLOSED | 上链 mock |
| OQ-V17-PO-009 | CLOSED | 按角色脱敏 |
| OQ-V17-PO-010 | CLOSED | 仅分页（页码输入与 pageSize） |
| OQ-V17-PO-011 | CLOSED | 合并为统一合约确认页 |
| OQ-V17-PO-012 | CLOSED | 签署附件为主 |
| OQ-V17-PO-013 | CLOSED | 每次变更生成数字合约新版本 |
| OQ-V17-PO-014 | CLOSED | 按主数据自动带入 |
| OQ-V17-PO-015 | CLOSED | 始终使用产品快照 |
| OQ-V17-PO-016 | CLOSED | 状态不可回退 |
| OQ-V17-PO-017 | CLOSED | 通知与时限本期不实现 |
| OQ-V17-PO-018 | CLOSED | 本期止于合约达成 |
| OQ-V17-PO-019 | CLOSED | 平台统一须知 |
| OQ-V17-PO-020 | CLOSED | 成功指标为订单可追踪率 |
| OQ-V17-001 | OPEN（non-blocking） | 字段级脱敏掩码及附件下载角色矩阵（与目录敏感字段策略对齐） |
| OQ-V17-002 | OPEN（non-blocking） | 病毒扫描落地方式（产品要求必须扫描；实现选型不在本快照裁定） |
| OQ-V16-001 | OPEN（non-blocking） | 继承 SNAP-WSC-008：企业实体最小字段与 `sys_user` 关联 |
| OQ-V16-002 | OPEN（non-blocking） | 继承 SNAP-WSC-008：历史无企业用户迁移 |
| OQ-V16-003 | OPEN（non-blocking） | 继承 SNAP-WSC-008：座序图 L1 时 `totalProducts` 口径 |

> **PO 待办**：~~本快照为 DRAFT。请以 Product Owner 身份回复「批准快照」~~ **已批准**（2026-08-19，identity=`productOwner`；事件 `SNAPSHOT_APPROVED`）。
