# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC4-R1-api-data-designer
planId: PLAN-WSC-4.0
round: 1
role: apiDataDesigner
actorInstance: api-data-designer-wsc-004-r1
snapshotIdAtReview: SNAP-WSC-004
contractsBaselineAtReview: wsc-contracts@2.0.0
contractsTargetAtReview: wsc-contracts@2.1.0
decision: REQUEST_CHANGES
summary: |
  PLAN-WSC-4.0 对契约升版 2.1.0 的主线大体可调度：typeSpecific.api 形状与 SNAP
  建议一致；ERR_IMPORT_TEMPLATE_UNSUPPORTED 请求级切开与 FORMAT 边界清晰；旧
  endpoint 读兼容 / 写新结构 / 优先无破坏 JSON 迁移合理。阻塞 APPROVE：现网
  OpenAPI 已命名冻结 ImportTemplateColumns（V1.1 最小列集含产品编码），计划 §3.1
  仅要求 GET 模板字节对齐 v0729，未显式废止并重冻该 schema/列名行，301 易留下
  「文档仍写 V1.1、字节已是 v0729」的契约漂移，旧模板拒绝亦缺少可引用的权威列集。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-4.0.md`；`product/requirements/SNAP-WSC-004.md`（APPROVED）；只读 `contracts/**` @`2.0.0` |
| 聚焦 | 升版 `wsc-contracts@2.1.0`：`ImportTemplateColumns`、`typeSpecific.api`、导入错误码、兼容/迁移策略是否计划级可验收 |
| 不裁定 | 页面交互/文案；威胁建模结论；他角色 ISSUE；DAG/写集工程细节（除非直接破坏契约所有权） |

## 对照核对（四焦点）

### 1. `ImportTemplateColumns` / 导入模板契约面 — **缺口（P1）**

| 检查项 | 计划证据 | 现网 `@2.0.0` | 判定 |
|---|---|---|---|
| 权威模板资产 | front-matter / 301 `readSet`：`product-import-template-v0729.xlsx` | — | 通过（资产指针） |
| GET 模板字节 | §3.1「与权威 xlsx 列组一致（分组行 + 列名行）」；301 acceptance | OpenAPI `GET .../import/template` 仍为通用 binary | 方向通过；**未**绑定到命名 schema |
| 命名 schema 升版 | §3.1 冻结表 **未出现** `ImportTemplateColumns` | `ImportTemplateColumns` 描述/example 仍为 V1.1 最小列（含 **产品编码**、数据来源/个信等；无接口定义/数据规模等） | **缺口** |
| 旧模板判定权威列集 | §3.2「表头不是 v0729 权威列集」 | 实现侧 `ImportTemplateColumns.COLUMNS` 与 OpenAPI 同步为 V1.1 | 判定依赖「权威列集」须在契约冻结；计划未写清 schema 废止/替换 |
| 无编码列 | §1.2 / 301：服务端按 DEC-WSC-001 自动生成 | 现 schema example **含**产品编码列 | 业务口径对；契约面未要求去掉旧列叙述 |

**结论**：行为意图（发 v0729、拒旧模板）正确，但 **契约制品 `ImportTemplateColumns` 未纳入 2.1.0 一次性冻结清单**，相对 SNAP 列映射（A–P / 列名行）与现网命名组件冲突，**计划级尚不可验收**。

### 2. `typeSpecific.api` — **通过（计划级）**

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| 形状 | §3.3 yaml：`swaggerFileContent`、`endpoints[]`（method/path/summary/description/parameters/responses/optional schemas）、`fieldDescription`/`dataSample`/`timeRange`/`regionScope`、兼容 `endpoint` | **通过** — 与 SNAP「建议形状」一致 |
| dataset / report / other | §3.1 增量字段；OTHER 允许 `contentDescription`（OQ-004 修订） | **通过** — 须在 301 改写现网「OTHER 无专属」叙述（见 P2） |
| 读写真源 | 写以 swagger+endpoints 为准；读旧仅 `endpoint` 不报错；可派生兼容字段 | **通过** |
| 导入「接口定义」 | 原文 + 解析 endpoints；非空解析失败 → 行失败（OQ-V13-009） | **通过** |
| 所有权 | 301 独占 `contracts/**` + 后端 typeSpecific；302/303 只读消费 | **通过** |
| 现网松散 schema | `@2.0.0` `TypeSpecificFields.api` 为 `additionalProperties: true` | 可接受由 301 按 §3.1/§3.3 **收紧为显式属性**；计划已列字段清单，不单开 P1 |

### 3. 错误码 — **通过（计划级）**

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| 新码 | `ERR_IMPORT_TEMPLATE_UNSUPPORTED`；纳入 `importSemantics.requestLevel` | **通过** |
| 与 FORMAT 切开 | 无法解析/非允许扩展名 → `ERR_IMPORT_FORMAT_INVALID`；可解析但列集不匹配 → `TEMPLATE_UNSUPPORTED` | **通过** — 继承 R2 请求级 vs 行级纪律 |
| 四态 | §3.4 态 ④ 映射含新码；禁止伪装行级 | **通过** |
| 既有码不回退 | FILE_TOO_LARGE / FORMAT_INVALID / ROW_INVALID（仅报告）/ REPORT_NOT_FOUND；白名单；TTL≤24h | **通过** |
| 同步 Job | 不引入异步 Job | **通过** |
| OpenAPI/UI 同步 | 301 writeSet：`errors/codes.yaml`；「相关 ui/state 文档若需同步」 | 主路径通过；「若需」偏软 → **P2** 要求显式同步 |

### 4. 兼容与迁移策略 — **通过（计划级）**

| 检查项 | 计划证据 | 判定 |
|---|---|---|
| 版本号 | `2.0.0` → `2.1.0`（OQ-V13-003）；301 唯一升级 | **通过** — 增量兼容意图正确 |
| 旧 `api.endpoint` | 读兼容；写优先新结构；可派生（OQ-V13-007） | **通过** |
| 旧导入模板 | 文件级拒绝（破坏旧模板客户端，PO 已关闭） | **通过（有意破坏面）** — 须靠错误码 + 文档可发现 |
| 数据迁移 | §3.6 优先无破坏 JSON 键扩展；必要时 301 独占 `sql/migration/**` 一版 | **通过** |
| Browse 分页 | 继续 `page`/`pageSize`/`total`；禁止本版改分页模型 | **通过** — 未重开 ISSUE-API-R1-003 |
| 枚举 | `UpdateFrequency` + `NO_UPDATE` | **通过**（名可在 301 冻结为等价标识） |
| 回滚 | §8：拒合入 / 回退 VERSION/DTO | **通过（计划级）** |

## 异议

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-WSC4-R1-001 | P1 | 现网 `contracts/openapi/openapi.yaml` `ImportTemplateColumns`（及实现镜像列集）冻结 **V1.1 最小列**（含产品编码等）。PLAN-WSC-4.0 §3.1 仅写「GET 模板字节与权威 v0729 列组一致」，**未**要求废止/重写该命名 schema，也未把 SNAP 列名行（如产品名称（必填）…数据内容描述；**无**产品编码列）列为 2.1.0 冻结内容。后果：301 可能只换下载字节，契约文档仍宣称 V1.1 列；§3.2「权威列集」缺少 OpenAPI 可引用真源，旧模板拒绝与 client/文档易分叉。 | 在 §3.1（契约冻结表）**显式**增加一行：`ImportTemplateColumns`（及等价描述）以 v0729 **列名行**为准重冻；写明废止 V1.1 最小列 example/描述；标明模板无产品编码列、编码由服务端生成（DEC-WSC-001）；301 `acceptance`/`testScope` 含：OpenAPI 该 schema 与权威 xlsx 列名行一致 + 契约 lint/对照。可选：注明判定用「列名行」（第 2 行），分组行不参与列名集合等值。 | REQ-CAT-008 |
| ISSUE-API-WSC4-R1-002 | P2 | 新请求级码已写入 §3.1/§3.2/§3.4，但 301 writeSet 对 `contracts/ui/state-matrix.md` / OpenAPI `POST /catalog/products/import` description 与 400 examples 使用「若需同步」软措辞。遗漏则 UI 矩阵与 OpenAPI 示例仍只列 FILE_TOO_LARGE/FORMAT_INVALID，前端/测试对态 ④ 映射不完整。 | §3.1 或 301 acceptance **硬性**要求：`errors/codes.yaml` + `importSemantics.requestLevel` + OpenAPI import 操作描述/400 example + `ui/state-matrix` 态 ④ 均列入 `ERR_IMPORT_TEMPLATE_UNSUPPORTED`。 | REQ-CAT-008 |
| ISSUE-API-WSC4-R1-003 | P2 | `@2.0.0` `info.description` / `ProductType` / `TypeSpecificFields` 仍写 OQ-004「OTHER 无类型专属字段」。计划 §3.1 已允许 `typeSpecific.other.contentDescription`，但未点名须改写上述冲突叙述；301 若只加属性不改 description，契约自相矛盾。 | §3.1 增一句：2.1.0 修订 OTHER/`TypeSpecificFields` 描述，允许 `other`（及 report）的 `contentDescription`+共用时间/地域；删除「OTHER 必须空对象」硬表述（与 SNAP OQ-004 修订一致）。301 acceptance 可测。 | REQ-CAT-004, REQ-CAT-005, REQ-API-001 |

## 不阻塞项（记录）

- `typeSpecific.api` 业务形状、错误码分层、旧 `endpoint` 兼容与 §3.6 迁移优先级：**计划级可验收**（实现细节交 301 OpenAPI 收紧属性）。
- 301 独占 `contracts/**` / `frontend/src/api/**`：所有权正确；后继只读消费符合兼容纪律。
- Browse 分页与导入同步模型：**未回退** `@2.0.0` 已冻结结论。

## 决策

`decision: REQUEST_CHANGES`

关闭 **ISSUE-API-WSC4-R1-001**（P1）前，本角色 **不** APPROVE。P2（002/003）建议在同轮修订中一并吸收，以免 Round 2 仅清文档债。

本评审 **未** 修改计划正文、源码或 Run `state`/`events`。
