# 接入端工作台 PRD — OpenAPI 结构与 v0729 导入（v1.3）

| 字段 | 值 |
|---|---|
| 状态 | DRAFT |
| 产品负责人 | productOwner（试点一人多身份） |
| 关联快照 | SNAP-WSC-004（DRAFT） |
| 关联 Run | `RUN-WSC-004` |
| 项目代号 | WSC |
| 基于 | `product/prd/wsc-v1.1.md` / SNAP-WSC-002（功能）；SNAP-WSC-003 / PLAN-WSC-3.1（UX 不回退） |
| 模板权威 | [`product/assets/import/product-import-template-v0729.xlsx`](../assets/import/product-import-template-v0729.xlsx) |
| 适用主体 | 可信数据空间接入端运营方 / 数据提供方 |
| 契约升版目标 | `wsc-contracts@2.1.0`（本 PRD/SNAP 声明目标；**不**在本角色任务内改 `VERSION`） |
| 参考交互 | cds-dataspace-ui `ApiDocEditPanel`（侧栏 + 端点编辑 + Swagger 导入）— **交互意图参考，不锁定技术选型** |

## 问题与目标

- **要解决的问题**：
  1. 上游产品批量导入模板已升级为 **v0729**（含按产品类型分区的数据描述列与 OpenAPI 文本），现网仍按 V1.1 最小列集解析，无法承接新模板，且旧模板兼容策略未产品化。
  2. 「数据接口」类产品在编辑/详情侧仍近似单字段 `apiEndpoint`，无法表达多端点、参数/响应与原始 Swagger 文本，运营与提供方难以完成接口产品的结构化登记与核验。
- **成功标准（可观察）**：
  - 管理员/提供方可用 **v0729 模板**完成批量导入；成功行可检索并产生上链版本；失败可下载错误报告。
  - **旧模板（V1.1 列集）整文件被拒绝**，返回明确请求级错误码（非伪装成行级结果）。
  - 产品编辑页可维护 OpenAPI 结构（多端点侧栏 + 端点详情 + Swagger 导入回填）；详情页只读展示等价信息。
  - 契约目标升至 `wsc-contracts@2.1.0`；RBAC、导入四态、错误报告字段白名单、UX 视觉与权限结构相对 SNAP-WSC-002/003 **不回退**。
  - P0 缺陷为 0。

## 相对前序版本的变更摘要

| 类型 | 内容 |
|---|---|
| 修订 | REQ-CAT-008：权威模板切换为 v0729；列集与按类型 A/B/C 分区映射；旧模板文件级拒绝 |
| 修订 | REQ-CAT-004 / REQ-CAT-005：详情/编辑扩展数据描述字段；数据接口改为 OpenAPI 结构展示/编辑 |
| 新增 | REQ-API-001：OpenAPI 结构的编辑、只读与 Swagger 导入回填（交互意图对齐参考 UI） |
| 继承 | 三角色 RBAC、导入结果四态、报告白名单、三级分类/目录维护/总览/上链等功能行为（SNAP-WSC-002） |
| 不回退 | SNAP-WSC-003 / PLAN-WSC-3.1 已验收的视觉令牌、壳层与主路径页面质感；本版不重做 UX 令牌体系 |
| 工程 | 全栈：`contracts` + `backend` + `frontend`；页面面：产品编辑、产品详情只读、批量导入 |

## 范围

### In Scope

- **页面**：产品编辑、产品详情（只读）、批量导入（目录页导入弹窗及模板下载链路）
- **全栈**：`contracts/**`（目标 2.1.0）、`backend/**` 导入与产品读写、`frontend/**` 上述三面
- **导入**：以 v0729 xlsx 为权威模板；支持 xlsx（及既有 csv 策略若仍适用则保持，但列语义以 v0729 为准）；单文件 ≤10MB
- **旧模板**：文件级拒绝 + 明确错误码（PO 已确认默认）
- **OpenAPI 存贮**：原始 swagger 文本 + 解析后 `endpoints[]` 写入 `typeSpecific.api`（PO 已确认默认）
- **详情/编辑**：按产品类型展示/编辑扩展数据描述字段；接口类型对接 OpenAPI 结构 UI

### Out of Scope / 非目标

- 重做 UX 设计令牌体系或另起视觉主题（继承 SNAP-WSC-003）
- 引入独立异步导入 Job / 队列（默认不引入；除非实现期论证同步路径无法满足可观测完成态并经 PO/架构批准）
- 数据登记 / 交易订单 / 连接器管理完整业务流（菜单占位策略不变）
- 真实区块链共识、移动端 App、公开注册门户、多租户组织树、支付结算引擎
- 在本角色任务内修改源码 / contracts VERSION / Run `state.yaml` / `events.jsonl`
- 改写 SNAP-WSC-001/002/003 既有结论正文

## 系统角色

与 SNAP-WSC-002 **相同**（本版不改权限矩阵，只扩展有权角色在编辑/详情/导入上的字段与交互能力）：

| 角色 | 能力 |
|---|---|
| 管理员 | 浏览；维护三级分类；目录维护；新增/编辑/**批量导入**；编辑与导入 OpenAPI/数据描述 |
| 提供方 | 浏览；新增/编辑/**批量导入**；编辑与导入 OpenAPI/数据描述；不可分类维护与目录维护 |
| 普通用户 | 只读浏览总览、目录、**详情（含 OpenAPI/数据描述只读）**、上链信息；无写入口 |

> 须登录态鉴权；禁止仅靠前端隐藏写入口（REQ-RBAC-001）。

## 功能需求

### REQ-CAT-008 批量导入产品（v0729）— 修订

- 优先级：P0
- 变更：相对 SNAP-WSC-002 **修订**
- 摘要：管理员与提供方按 **v0729** 模板批量导入产品；旧模板整文件拒绝。
- 验收：
  - 可下载的权威模板与 `product/assets/import/product-import-template-v0729.xlsx` 列组一致（见 SNAP 列映射表）；
  - 上传 `.xlsx` / `.csv`（若保留 csv：列名须与 v0729 表头语义一致）；单文件 ≤10MB，超限 → `ERR_IMPORT_FILE_TOO_LARGE`（请求级）；
  - **旧模板**（V1.1 最小列集等非 v0729 表头）→ **文件级拒绝**，明确错误码（建议 `ERR_IMPORT_TEMPLATE_UNSUPPORTED`，最终码名由契约冻结），**不得**进入部分成功/行级报告伪装；
  - 无法解析/非允许格式 → `ERR_IMPORT_FORMAT_INVALID`（请求级）；
  - 行级校验失败写入错误报告，`ERR_IMPORT_ROW_INVALID` 仅出现在报告内；报告字段白名单不扩大：`rowNumber, productCode, reasonCode, reasonMessage`；
  - 导入结果四态（全成功 / 部分成功 / 全失败行级 / 文件级拒绝）UI 行为不回退；
  - 成功行可在目录检索并产生上链版本（与 REQ-CAT-005 / DEC-WSC-002 一致）；默认**同步**完成态可观测，不引入独立异步 Job；
  - v0729 无「产品编码」列：导入时服务端按 DEC-WSC-001 **自动生成**唯一编码（见 OQ-V13-005，PO 默认关闭）。

### REQ-CAT-004 产品详情只读 — 修订

- 优先级：P0
- 变更：相对 SNAP-WSC-002 **修订**
- 摘要：在既有分组之上，按产品类型只读展示扩展数据描述；数据接口展示 OpenAPI 结构（原始文档摘要 + 端点列表）。
- 验收：
  - 保留 V1.0/V1.1 基础信息、供应商、产权、标签、简介、场景等既有分组能力（三级分类路径展示不变）；
  - **数据接口**：只读展示 `typeSpecific.api` 的端点列表（method/path/summary 等）及可理解的文档信息；兼容仅含旧 `endpoint` 的历史数据可读；
  - **数据集 / 数据报告 / 其他**：按 SNAP 映射展示数据规模、数据形态、字段描述、数据样例、数据内容描述、时间/地域范围等已存字段；
  - 有写权限时提供「编辑」入口；编码规则仍遵循 DEC-WSC-001。

### REQ-CAT-005 新增 / 编辑数据产品 — 修订

- 优先级：P0
- 变更：相对 SNAP-WSC-002 **修订**
- 摘要：编辑器按类型切换扩展数据描述区；数据接口类型进入 OpenAPI 结构编辑（见 REQ-API-001）；提交后仍产生上链版本。
- 验收：
  - 未填产品名称或产品编码（手工新建/编辑）时阻止提交并提示；导入路径编码自动生成除外；
  - 切换产品类型时动态切换类型专属/数据描述区；
  - 新建/编辑成功行为与上链版本规则继承 DEC-WSC-001/002；取消/返回不保存；
  - 数据接口提交体写入 `typeSpecific.api`（含 `swaggerFileContent` 与 `endpoints[]`，见 SNAP 形状）；旧 `endpoint` 兼容策略见 SNAP；
  - UX 布局与控件层级不回退 SNAP-WSC-003 REQ-UX-007 可验收质感（本版可在既有令牌内增量控件，不重做令牌体系）。

### REQ-API-001 OpenAPI 结构编辑 / 只读 / 导入回填 — 新增

- 优先级：P0
- 变更：相对 SNAP-WSC-002 **新增**
- 摘要：为「数据接口」产品提供结构化 API 文档能力：端点列表侧栏、端点详情编辑、Swagger/OpenAPI 文本导入并回填端点。
- 交互意图（参考 `ApiDocEditPanel`，**不**锁定组件库/目录结构）：
  - **侧栏**：端点列表；可新增自定义端点、删除端点；展示 method 标签与摘要/路径级标签；空态提示「暂无接口，请新增或从 Swagger 导入」；
  - **端点编辑区**：摘要、描述、method、path；输入参数表（增删行）；响应/输出项表（增删行）；可选响应体 Schema 文本；
  - **Swagger 导入**：粘贴或上传 OpenAPI/Swagger 文本 → 解析并回填 `endpoints[]`，同时保留原始文本至 `swaggerFileContent`；
  - **详情只读**：同等信息只读呈现（无写控件）。
- 验收：
  - 编辑态可完成：新增/删除端点、编辑参数与响应、Swagger 导入回填后可保存并再次打开不丢失；
  - 导入模板「接口定义」列中的 OpenAPI 文本：入库为 `swaggerFileContent`，并解析生成 `endpoints[]`；解析失败且单元格非空 → 该行失败（报告内原因可读）；
  - 普通用户详情只读可见；无写权限不可改。

### 继承需求（本版不削弱）

| id | 说明 |
|---|---|
| REQ-RBAC-001 | 三角色写权限与 403；导入/编辑仍仅管理员+提供方 |
| REQ-SHELL-001 / REQ-CAT-001..003 / REQ-CAT-006..007 / REQ-OVW-* / REQ-CHAIN-001 | 行为继承 SNAP-WSC-002；展示适配已有三级模型 |
| REQ-UX-001..011 | 视觉与权限结构不回退（SNAP-WSC-003）；本版不重开 UX 令牌重做 |

## 关键枚举增量

在 SNAP-WSC-002 枚举基础上：

| 维度 | 增量 |
|---|---|
| 更新频率 | 增加 **不更新**（v0729 模板可选值；契约 2.1.0 须容纳） |
| 数据形态（数据集） | 图片、文本、视频、音频、表格、其他（模板约束；存于 typeSpecific） |
| 行业分类（导入） | 模板说明为 GB/T 4754—2017 门类名称/代码；须映射到本系统可挂载三级分类（见 OQ） |

## 非功能需求

- 继承 V1.1 / UX NFR：中文界面、关键操作成功/失败反馈、写操作鉴权、敏感字段可读规范。
- 批量导入：明确进度/完成反馈；文件级错误与行级报告语义分离；报告 TTL/白名单不回退。
- OpenAPI 解析：在导入与编辑导入路径上失败原因对用户可读；不得静默丢弃用户提供的 swagger 原文（原文须可存回）。
- 性能：默认同步导入路径；不引入独立异步 Job（见 Out of Scope）。
- 契约：目标 `wsc-contracts@2.1.0`；错误码与 DTO 由后续计划/API 角色冻结，本 PRD 只给业务语义。
- 后端栈基线仍为 DEC-WSC-004 / DEC-WSC-005。

## 开放问题

| id | question | suggestedDefault | owner | blocking | 状态 |
|---|---|---|---|---|---|
| OQ-V13-001 | 旧模板如何处理？ | **文件级拒绝 + 明确错误码** | productOwner | yes | **CLOSED**（PO CONFIRM_SCOPE） |
| OQ-V13-002 | OpenAPI 如何存贮？ | **原始 swagger 文本 + 解析 endpoints[]** | productOwner | yes | **CLOSED**（PO CONFIRM_SCOPE） |
| OQ-V13-003 | 契约升版号？ | **wsc-contracts@2.1.0** | productOwner | yes | **CLOSED**（PO CONFIRM_SCOPE） |
| OQ-V13-004 | 是否引入独立异步导入 Job？ | **否**（保持同步可观测完成态） | productOwner | no | **CLOSED**（建议默认；与 Out of Scope 一致） |
| OQ-V13-005 | v0729 无产品编码列时如何满足 DEC-WSC-001？ | **导入时服务端自动生成**符合 DEC-WSC-001 的唯一编码 | productOwner | yes | **CLOSED**（建议默认，待 PO 确认快照时一并确认） |
| OQ-V13-006 | 「行业分类」门类名如何映射到三级 `l3CategoryId`？ | **按名称匹配**现有 L2/L3 节点（精确/规范名）；无法匹配 → 行失败 | productOwner | yes | **OPEN（blocking）** |
| OQ-V13-007 | 历史 `typeSpecific.api.endpoint` 如何兼容？ | **读兼容**；写优先新结构；可选派生 `endpoint` 兼容字段 | productOwner | no | **CLOSED**（建议默认） |
| OQ-V13-008 | 更新频率「不更新」是否扩枚举？ | **是**，纳入 2.1.0 | productOwner | yes | **CLOSED**（建议默认，待 PO 确认） |
| OQ-V13-009 | 「接口定义」非空但 OpenAPI 解析失败？ | **行级失败**（写入错误报告） | productOwner | no | **CLOSED**（建议默认） |
| OQ-004 | 「其他数据产品」独立类型字段？ | v0729 为 OTHER/报告提供「数据内容描述」；OTHER 允许 `contentDescription` | productOwner | no | **修订口径**（见 SNAP；待 PO 确认） |
| OQ-001 | 占位菜单业务是否纳入后续迭代？ | — | productOwner | no | 保留；仍 Out of Scope |
| OQ-V11-003 | 目录维护条目与数据产品是否同一实体？ | 暂定同一 | productOwner | yes | 保留自 SNAP-WSC-002 |
| OQ-V11-004 | 导入失败行全有全无 vs 部分成功？ | 部分成功+错误报告 | productOwner | no | 保留；本版不改变 |

## 完成检查

- [x] 有范围与非目标
- [x] P0 需求有验收
- [x] 开放问题有 Owner；PO 已确认项标注 CLOSED
- [x] 关联需求快照 SNAP-WSC-004（DRAFT）
- [ ] PO `CONFIRM_SNAPSHOT` 后将本 PRD / SNAP 升为可规划权威（非本起草步）
