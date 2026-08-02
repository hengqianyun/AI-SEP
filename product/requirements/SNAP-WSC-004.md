# 接入端工作台（WSC）需求快照 V1.3 — OpenAPI 与 v0729 导入

```yaml
snapshotId: SNAP-WSC-004
sourcePrd: product/prd/wsc-v1.3-openapi-import.md
sourceVersion: V1.3
runId: RUN-WSC-004
status: APPROVED
createdAt: 2026-08-02
approvedAt: 2026-08-02
actorInstance: product-analyst-wsc-004
requirementCount: 5
contractsTarget: wsc-contracts@2.1.0
templateAsset: product/assets/import/product-import-template-v0729.xlsx
previousSnapshot: SNAP-WSC-002
uxBaseline:
  snapshotId: SNAP-WSC-003
  planId: PLAN-WSC-3.1
functionalBaseline:
  snapshotId: SNAP-WSC-002
  planId: PLAN-WSC-2.2
  contracts: wsc-contracts@2.0.0
poScopeConfirm:
  surfaces: [editor, detail, import]
  stack: full
  defaults:
    legacyTemplate: file-level-reject
    contracts: "2.1.0"
    openapiStorage: swaggerText+endpoints
poSnapshotConfirm:
  utterance: Implement the plan（含建议默认关闭 OQ-V13-005/006/008；OQ-V11-003 暂定同一可关联条目）
  decidedAt: 2026-08-02T17:10:00+08:00
changeFromPrevious:
  - reason: 承接上游 v0729 导入模板与数据接口 OpenAPI 结构化登记；补齐详情/编辑数据描述字段
  - action: 由 product/prd/wsc-v1.3-openapi-import.md 解析；对照 v0729 模板与 SNAP-WSC-002 REQ-CAT-004/005/008；UX 不回退 SNAP-WSC-003
  - added: [REQ-API-001]
  - revised: [REQ-CAT-004, REQ-CAT-005, REQ-CAT-008]
  - inherited: [REQ-SHELL-001, REQ-RBAC-001, REQ-OVW-001..005, REQ-CAT-001..003, REQ-CAT-006..007, REQ-CHAIN-001]
  - inheritedUxNoRegression: [REQ-UX-001..011]
  - closedOpenQuestions: [OQ-V13-001, OQ-V13-002, OQ-V13-003, OQ-V13-004, OQ-V13-005, OQ-V13-006, OQ-V13-007, OQ-V13-008, OQ-V13-009, OQ-V11-003]
  - blockingOpenQuestions: []
  - revisedOpenQuestions: [OQ-004]
  - retainedOpenQuestions: [OQ-001, OQ-V11-004]
```

> **权威声明**：本文件为 RUN-WSC-004 需求快照（`snapshotId: SNAP-WSC-004`），当前 **APPROVED**。后续评审与计划须引用本 `snapshotId`；需求变更须新建快照。
>
> **不改写旧结论**：SNAP-WSC-001 / SNAP-WSC-002 / SNAP-WSC-003 正文结论保持不变；本快照以增量/修订 REQ 表达 V1.3 范围。
>
> **基线关系**：功能与契约演进基线 = SNAP-WSC-002（`previousSnapshot`）；UX 视觉与权限结构基线 = SNAP-WSC-003 / PLAN-WSC-3.1（**不回退**，不重做令牌体系）。契约升版**目标** = `wsc-contracts@2.1.0`（本快照声明；VERSION 文件由契约任务改）。

## 业务目标

在 V1.1 功能与 V1.2-UX 表现基线之上，使工作台能够：

1. 按上游 **v0729** 模板批量导入含类型分区数据描述的产品；
2. 对「数据接口」产品进行 **OpenAPI 结构**的编辑、只读展示与 Swagger 导入回填；
3. 在详情/编辑中完整呈现扩展数据描述字段。

成功标准（可观察）：

- v0729 导入主路径可用；旧模板文件级拒绝且错误码明确；
- 编辑/详情/导入三面覆盖 OpenAPI 与扩展描述；成功导入/编辑产生上链版本；
- RBAC、导入四态、报告白名单、UX 主路径质感不回退；
- P0 缺陷为 0。

## 角色与权限摘要

与 SNAP-WSC-002 **相同**（矩阵不回退）：

| 角色 | 可观察能力 |
|---|---|
| 管理员 | 浏览；分类维护；目录维护；新增/编辑/批量导入；OpenAPI 与数据描述写 |
| 提供方 | 浏览；新增/编辑/批量导入；OpenAPI 与数据描述写；无分类/目录维护 |
| 普通用户 | 只读（含详情 OpenAPI/数据描述）；无写入口 |

## 优先级政策

- `P0`：本版成功标准相关能力；缺失则不得按 V1.3 发布。
- 本快照所列修订/新增 REQ 均为 **P0**。

## 需求清单

状态：`IN_REVIEW`（快照 DRAFT）。验收以本表 + 列映射 + typeSpecific 形状 + 引用 DEC 为准。

| id | priority | change | summary | acceptance | decisionRefs |
|---|---|---|---|---|---|
| REQ-CAT-008 | P0 | 修订 | v0729 批量导入；旧模板文件级拒绝 | 模板列与权威 xlsx 一致；≤10MB；旧模板 → 请求级明确错误码（建议 `ERR_IMPORT_TEMPLATE_UNSUPPORTED`）；四态/报告白名单不回退；成功行可检索+上链；无产品编码列时服务端按 DEC-WSC-001 自动生成；默认同步完成态 | DEC-WSC-001, DEC-WSC-002 |
| REQ-CAT-004 | P0 | 修订 | 详情只读：扩展数据描述 + OpenAPI 结构 | 既有分组保留；按类型展示扩展字段；API 展示 endpoints + 文档信息；旧 `api.endpoint` 可读；有权显示编辑入口 | DEC-WSC-001 |
| REQ-CAT-005 | P0 | 修订 | 编辑：扩展数据描述 + OpenAPI 写入 | 类型切换动态区；提交写入 typeSpecific；API 写 swaggerFileContent+endpoints；上链版本规则不变；UX 不回退 REQ-UX-007 | DEC-WSC-001, DEC-WSC-002 |
| REQ-API-001 | P0 | 新增 | OpenAPI 编辑/只读/Swagger 回填 | 侧栏端点列表+增删；端点摘要/方法/路径/参数/响应/可选 schema；Swagger 导入回填 endpoints 且保留原文；详情只读等价；导入「接口定义」解析失败且非空→行失败 | — |
| REQ-RBAC-001 | P0 | 继承 | 三角色写权限不回退 | 导入/编辑仍仅 ADMIN+PROVIDER；USER 403；前端隐藏不等于授权 | — |

### 继承声明（不逐条重写验收）

以下 REQ **行为继承** SNAP-WSC-002，本版不得削弱：`REQ-SHELL-001`、`REQ-OVW-001..005`、`REQ-CAT-001..003`、`REQ-CAT-006`、`REQ-CAT-007`、`REQ-CHAIN-001`。  
UX：`REQ-UX-001..011`（SNAP-WSC-003）**不回退**；本版可在既有令牌内增加 OpenAPI 编辑控件，**不**重做令牌体系。

导入结果四态与报告约束（继承，不回退）：

| 约束 | 口径 |
|---|---|
| 四态 | 全成功 / 部分成功 / 全失败（行级）/ 文件级拒绝 |
| 请求级错误 | `ERR_IMPORT_FILE_TOO_LARGE`、`ERR_IMPORT_FORMAT_INVALID`、**新增**旧模板类错误码 |
| 行级 | `ERR_IMPORT_ROW_INVALID` 仅报告内 |
| 报告白名单 | `rowNumber, productCode, reasonCode, reasonMessage` |
| Job | **不**引入独立异步导入 Job（OQ-V13-004 CLOSED） |

## 列 → 字段映射表（v0729）

权威表头来自 `product-import-template-v0729.xlsx`（Sheet1）。  
第 1 行为分组（合并单元格）；第 2 行为列名；第 3 行为填写说明；第 4 行起为数据。

### 分组

| 列范围 | 分组 |
|---|---|
| A–E | 产品基础信息 |
| F–H | 数据描述：共用 |
| I–K | 数据描述A：数据接口（类型 A = 数据接口 / `API`） |
| L–O | 数据描述B：数据集（类型 B = 数据集 / `DATASET`） |
| P | 数据描述C：数据报告或其他（类型 C = 数据报告 `REPORT` / 其他 `OTHER`） |

### 共用 / 基础列

| 列 | 模板列名 | 必填（模板） | 目标字段 | 说明 |
|---|---|---|---|---|
| A | 产品名称（必填） | 是 | `productName` | |
| B | 产品类型（必填） | 是 | `productType` | 中文枚举 → `API` / `DATASET` / `REPORT` / `OTHER` |
| C | 行业分类（必填） | 是 | `l3CategoryId`（解析） | 模板语义为 GB/T 4754 门类；**映射策略见 OQ-V13-006（blocking）** |
| D | 产品简介（必填） | 是 | `summary` | |
| E | 交付方式 | 否 | `deliveryMethod` | API / 文件传输 / 数据沙箱 / 隐私保护计算 |
| F | 时间范围 | 否 | 见类型块 `timeRange` | 格式 `YYYY/MM/DD - YYYY/MM/DD`；可无结束日期 |
| G | 地域范围 | 否 | 见类型块 `regionScope` | 国际…区县级以下 |
| H | 更新频率 | 否 | `updateFrequency` | 含 **不更新**（契约 2.1.0 扩枚举） |
| — | （模板无）产品编码 | — | `productCode` | **服务端自动生成**（OQ-V13-005 CLOSED） |
| — | （模板无）数据来源/个信/公共数据/计费/价格等 | — | 对应可选字段 | 导入不强制；保持空/默认；手工编辑仍可填 |

### 类型 A — 数据接口（`productType=API`）

| 列 | 模板列名 | 目标字段 | 说明 |
|---|---|---|---|
| F | 时间范围 | `typeSpecific.api.timeRange` | 共用列写入 api 块 |
| G | 地域范围 | `typeSpecific.api.regionScope` | |
| I | 接口定义 | `typeSpecific.api.swaggerFileContent` + 解析 → `typeSpecific.api.endpoints[]` | 兼容 OpenAPI 嵌套文本；解析失败且非空 → 行失败 |
| J | 字段描述 | `typeSpecific.api.fieldDescription` | Tab 分隔：字段名 / 类型 / 说明（允许原文存贮） |
| K | 数据样例 | `typeSpecific.api.dataSample` | 任意文本/类 JSON |

A 类型行应忽略 B/C 专属列（L–P）或允许空；非空异型列 → 行失败或忽略（实现默认：**忽略空，非空异型列警告或行失败**；建议 **行失败** 以便数据洁净，见 OQ 若需放宽）。

### 类型 B — 数据集（`productType=DATASET`）

| 列 | 模板列名 | 目标字段 | 说明 |
|---|---|---|---|
| F | 时间范围 | `typeSpecific.dataset.timeRange` | |
| G | 地域范围 | `typeSpecific.dataset.regionScope` | |
| L | 数据规模 | `typeSpecific.dataset.dataScale` | 如 xx条、xxGB |
| M | 数据形态 | `typeSpecific.dataset.dataForm` | 图片/文本/视频/音频/表格/其他 |
| N | 字段描述 | `typeSpecific.dataset.fieldDescription` | 同 A 的 Tab 文本约定 |
| O | 数据样例 | `typeSpecific.dataset.dataSample` | |

### 类型 C — 数据报告或其他（`REPORT` / `OTHER`）

| 列 | 模板列名 | 目标字段 | 说明 |
|---|---|---|---|
| F | 时间范围 | `typeSpecific.report.timeRange` 或 `typeSpecific.other.timeRange` | 按产品类型二选一 |
| G | 地域范围 | `typeSpecific.report.regionScope` 或 `typeSpecific.other.regionScope` | |
| P | 数据内容描述 | `typeSpecific.report.contentDescription` 或 `typeSpecific.other.contentDescription` | 修订 OQ-004：OTHER 允许该字段 |

## typeSpecific.api 建议形状

> 业务形状建议，供契约/实现冻结；**非**本角色改 contracts。

```yaml
typeSpecific:
  api:
    swaggerFileContent: string   # 原始 OpenAPI/Swagger 文本；可空
    endpoints:                   # 解析或手工维护的端点列表
      - id: string
        method: string           # GET|POST|PUT|DELETE|PATCH 等
        path: string
        summary: string
        description: string
        parameters:              # 输入参数
          - name: string
            type: string
            required: boolean
            description: string
        responses:               # 输出/响应项
          - code: string
            description: string
        responseBodySchema: string   # 可选
        requestBodySchema: string    # 可选
    fieldDescription: string     # 模板「字段描述」原文（可选）
    dataSample: string           # 模板「数据样例」
    timeRange: string
    regionScope: string
    endpoint: string             # 兼容旧字段（见下）
```

### 旧 `apiEndpoint` / `typeSpecific.api.endpoint` 兼容策略

| 场景 | 策略 |
|---|---|
| 读旧数据（仅有 `endpoint`） | 详情/编辑**可读**；`endpoints` 可为空数组；UI 不报错 |
| 写新数据（2.1.0） | **以** `swaggerFileContent` + `endpoints[]` **为准** |
| 兼容字段 | 允许保留/派生 `endpoint`（例如首端点 `method path` 或 servers URL 摘要），供旧客户端只读；不得作为唯一真源 |
| 导入 | 不依赖旧单字段；由「接口定义」生成新结构 |

## 关键枚举约束（增量）

继承 SNAP-WSC-002 表。增量：

| 维度 | 取值增量 |
|---|---|
| 更新频率 | **不更新**（`NO_UPDATE` 或契约等价枚举名，由 2.1.0 冻结） |
| 数据形态 | 图片、文本、视频、音频、表格、其他 |

## 非功能约束

- 页面中文；关键操作成功/失败反馈。
- 导入文件级 vs 行级错误语义分离；报告白名单与 TTL 不回退。
- OpenAPI 原文可存可回读；解析失败原因可读。
- 默认同步导入；不引入独立异步 Job。
- UX 令牌/壳层/主路径质感不回退 SNAP-WSC-003。
- 后端栈：Java 17 / Spring Boot 2.7.18 / MySQL / Redis / Flyway（DEC-WSC-004/005）。
- 契约目标：`wsc-contracts@2.1.0`。

## Out of Scope / 非目标

- 重做 UX 令牌体系或另起视觉主题
- 引入独立异步导入 Job（除非实现期论证必须并经批准）
- 数据登记 / 交易订单 / 连接器完整业务流
- 真实链共识、移动端 App、公开注册门户、多租户组织树、支付结算
- 改写 SNAP-WSC-001/002/003 历史结论
- 本快照角色任务内修改源码 / contracts VERSION / Run state/events

## 开放问题（OQ）

| id | question | suggestedDefault | owner | blocking | status |
|---|---|---|---|---|---|
| OQ-V13-001 | 旧模板处理策略 | 文件级拒绝 + 明确错误码 | productOwner | yes | **CLOSED**（PO CONFIRM_SCOPE `legacyTemplate=file-level-reject`） |
| OQ-V13-002 | OpenAPI 存贮 | 原始 swagger 文本 + `endpoints[]` | productOwner | yes | **CLOSED**（PO CONFIRM_SCOPE `openapiStorage=swaggerText+endpoints`） |
| OQ-V13-003 | 契约版本 | `wsc-contracts@2.1.0` | productOwner | yes | **CLOSED**（PO CONFIRM_SCOPE `contracts=2.1.0`） |
| OQ-V13-004 | 独立异步导入 Job | 否 | productOwner | no | **CLOSED**（默认） |
| OQ-V13-005 | 模板无编码列 | 导入时服务端按 DEC-WSC-001 自动生成 | productOwner | yes | **CLOSED**（建议默认；待 PO 确认快照） |
| OQ-V13-006 | 行业分类门类 → `l3CategoryId` | 按名称/完整路径匹配 L2/L3（继承 ImportFieldMapper）；失败则行失败 | productOwner | yes | **CLOSED**（PO CONFIRM_SNAPSHOT；延续 V1.1 解析） |
| OQ-V13-007 | 旧 `api.endpoint` 兼容 | 读兼容、写优先新结构、可派生 endpoint | productOwner | no | **CLOSED**（建议默认） |
| OQ-V13-008 | 「不更新」枚举 | 契约 2.1.0 扩枚举 | productOwner | yes | **CLOSED**（建议默认；待 PO 确认快照） |
| OQ-V13-009 | 接口定义解析失败 | 行级失败 | productOwner | no | **CLOSED**（建议默认） |
| OQ-004 | OTHER 类型专属字段 | 允许 `contentDescription`（及共用时间/地域） | productOwner | no | **修订待确认**（相对 V1「仅基础信息」） |
| OQ-001 | 占位菜单后续迭代 | — | productOwner | no | 保留；Out of Scope |
| OQ-V11-003 | 目录维护≡数据产品 | 暂定同一可关联条目 | productOwner | yes | **CLOSED（暂定）**（PO CONFIRM_SNAPSHOT；本 Run 不拆实体） |
| OQ-V11-004 | 部分成功 vs 全有全无 | 部分成功+错误报告 | productOwner | no | 保留；本版不改变 |

### Blocking OQ

本版 **无** 开放 blocking OQ。OQ-V13-005/006/008、OQ-V11-003 已由 PO `CONFIRM_SNAPSHOT` 关闭（见 yaml 头）。

## 决策索引

| DEC | 主题 | 本快照用法 |
|---|---|---|
| DEC-WSC-001 | 产品编码唯一与格式 | CAT-004/005/008；导入自动生成须符合本 DEC |
| DEC-WSC-002 | 模拟存证适配层 | CHAIN / 导入与编辑上链版本 |
| DEC-WSC-003 | 有挂载禁止删分类 | 继承 CAT-006 |
| DEC-WSC-004 | Java 17 LTS | 后端运行时 |
| DEC-WSC-005 | data-chain 栈 | 后端/迁移 |

本快照无新增 DEC；OQ-V13-006 若需非「名称匹配」方案，可能触发新 DEC。

## 参考交互（非技术锁定）

cds-dataspace-ui `ApiDocEditPanel` 交互意图摘要（供 REQ-API-001）：

- 左侧端点列表（method 标签、摘要/路径、增删）
- 右侧端点编辑（摘要、描述、method+path、参数表、响应表、可选 schema）
- 空态引导新增或从 Swagger 导入
- 导入后回填列表并可继续编辑

实现可选任意等价 UI，**不**要求复用该仓库组件或技术栈。

## 源与关联

- PRD：`product/prd/wsc-v1.3-openapi-import.md`
- 模板：`product/assets/import/product-import-template-v0729.xlsx`
- 模板字符串对照：`product/assets/import/product-import-template-v0729-strings.txt`
- 功能基线：`product/requirements/SNAP-WSC-002.md`
- UX 基线：`product/requirements/SNAP-WSC-003.md` / `planning/approved/PLAN-WSC-3.1.md`
- 术语：`product/glossary.md`
- 本快照：`product/requirements/SNAP-WSC-004.md`
