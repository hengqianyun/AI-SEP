# Round 1 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC9-R1-api-data-designer
planId: PLAN-WSC-9.1
round: 1
role: apiDataDesigner
actorInstance: api-data-designer-wsc-012-r1
snapshotIdAtReview: SNAP-WSC-009
contractsBaselineAtReview: wsc-contracts@2.3.3
contractsTargetAtReview: wsc-contracts@2.3.3
decision: REQUEST_CHANGES
summary: |
  PLAN-WSC-9.1 对 V1.7 订单增量的 API 端点规划（list/detail/create/confirm/contract/cancel/notice/attachment）、
  RBAC 矩阵与 SNAP-WSC-009 角色表一致、911 独占契约写集与 912 独占 Flyway 的分工合理、
  state-matrix 订单菜单角色化方向正确、DAG 无环且串行写集互斥。阻塞 APPROVE：
  ① ChainAttestationPort 订单事件请求类型未冻结（§3.1「可扩展或并列」二选一未定）；
  ② multipart 合约上传端点的字段名与交易信息 JSON schema 未冻结；
  ③ 订单列表响应字段（九组）未在契约冻结区枚举；
  ④ 计费明细（t_order_line）提交入口未明确挂载到哪个端点；
  ⑤ 金额 DECIMAL 精度未指定。开放 6 条 ISSUE；decision=REQUEST_CHANGES。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-9.1.md` §3 / §4 / §5；`product/requirements/SNAP-WSC-009.md`；`product/prd/wsc-v1.7-transaction-orders.md` |
| 只读对照 | `contracts/VERSION`（2.3.3）、`contracts/rbac/matrix.yaml`（2.3.3）、`contracts/openapi/openapi.yaml`（2.3.3）、`contracts/ui/state-matrix.md`、`contracts/chain/ChainAttestationPort.md`、`contracts/errors/codes.yaml`、`contracts/security/sensitive-fields.md` |
| 聚焦 | 订单 API 端点完整性；OpenAPI 路径/query/body/response 冻结缺口；matrix 新增单元格与 SNAP 对齐；state-matrix 订单行；数据模型字段设计；mock 上链请求结构；脱敏 API 层可行性；分页参数设计；VERSION bump 逻辑 |
| 不裁定 | 页面交互/文案；威胁建模（仅提出数据暴露风险）；他角色 ISSUE；DAG/写集工程细节（除非直接破坏契约冻结） |
| 禁区 | 不代批他角色；不写 `ai/runs/**`；不改 `contracts/**` / 计划 / state / events；不 commit |

## 对照核对

### 1. 契约版本与 911 独占所有权 — **通过**

| 检查项 | 计划证据 | 只读现状 | 判定 |
|---|---|---|---|
| `contractsTarget` | YAML 头 `wsc-contracts@2.3.2`；§0.2/§3.1 明确工作树 2.3.3 | `VERSION` = 2.3.3；`matrix.yaml` version = 2.3.3；`openapi.yaml` info.version = 2.3.3 | 四头已同步为 2.3.3；计划 body 解释充分；**头字段与 body 不一致为小瑕疵**（ISSUE-006 non-blocking） |
| 911 独占写集 | §0.3 / TASK-911 writeSet；禁止他任务 bump | — | **通过** |
| 一次性冻结 | §3.1「一次性冻结后续写接口形状，避免二次 bump」；911 acceptance 含 B/C 全部 path | — | **通过**（方向正确） |
| V1.6 不回退 | §3.1「既有不回退」列表；matrix「V1.6 全部既有键保持 2.3.3 原文」 | matrix 2.3.3 已含完整 V1.6 单元格 | **通过** |

### 2. API 端点设计 — **部分通过**

| 端点 | 计划 §3.1 描述 | SNAP 对齐 | 判定 |
|---|---|---|---|
| `GET /orders` | 分页列表；query: keyword/status/page/pageSize；scope 仅信 SessionPrincipal | REQ-001/007/008 ✓ | **通过** |
| `POST /orders` | body: productId/remark/noticeAccepted=true；成功返回订单号+PENDING_CONFIRM | REQ-003/004/006 ✓ | **通过** |
| `GET /orders/{orderId}` | 详情全分区；越权 403 | REQ-009 ✓ | **通过** |
| `POST /orders/{orderId}/confirm` | PROVIDER 本企业或 ADMIN；PENDING_CONFIRM→PENDING_UPLOAD | REQ-010/011 ✓ | **body 未指定**（ISSUE-004） |
| `POST /orders/{orderId}/contract` | multipart：签署附件+交易信息 JSON；仅需求方；PENDING_UPLOAD→PENDING_CONTRACT_CONFIRM | REQ-010/013/014 ✓ | **字段名与 JSON schema 未冻结**（ISSUE-002） |
| `POST /orders/{orderId}/contract/confirm` | body: noticeAccepted=true；PROVIDER 或 ADMIN；→CONTRACT_REACHED | REQ-010/015 ✓ | **通过** |
| `POST /orders/{orderId}/cancel` | 三未完成态；三方可 | REQ-012 ✓ | **通过** |
| `GET /orders/notices/current` | 平台统一须知正文 | REQ-004 ✓ | **响应未含版本标识**（ISSUE-005 non-blocking） |
| `GET /orders/{orderId}/attachment` | 按角色下载；越权 403 | REQ-013 ✓ | **通过** |

### 3. RBAC 矩阵 — **通过**

| 能力键 | SNAP-WSC-009 角色表 | 计划 §3.1 矩阵 | 判定 |
|---|---|---|---|
| `ordersUI` | USER 我的订单；PROVIDER/ADMIN 交易订单 | visible，文案按角色 | **一致** |
| `ordersListApi` | USER 本人需求方；PROVIDER 本企业提供方；ADMIN 全部 | 200 ownAsDemand / 200 ownEnterpriseAsProvider / 200 all | **一致** |
| `ordersCreateApi` | 仅 USER（产品允许时） | USER 200；ADMIN/PROVIDER 403 | **一致** |
| `ordersConfirmApi` | PROVIDER 本企业；ADMIN 可代；USER 否 | PROVIDER 200 own / ADMIN 200 any / USER 403 | **一致** |
| `ordersSubmitContractApi` | 仅 USER 本人 | USER 200 ownAsDemand；其余 403 | **一致** |
| `ordersConfirmContractApi` | PROVIDER 本企业；ADMIN 可代 | PROVIDER 200 own / ADMIN 200 any / USER 403 | **一致** |
| `ordersCancelApi` | 三方（未完成态） | 三角色均 200（scope 各异） | **一致** |
| `ordersAttachmentGet` | 按角色下载 | ADMIN 200 / PROVIDER 200 own / USER 200 ownAsDemand | **一致** |
| V1.6 既有键 | 不回退 | 保持 2.3.3 原文 | **通过** |

### 4. state-matrix 订单增量 — **部分通过**

| 检查项 | 计划描述 | 判定 |
|---|---|---|
| 订单菜单角色化文案 | §3.1 state-matrix：USER 我的订单；PROVIDER/ADMIN 交易订单 | **通过** |
| 数据登记/连接器仍可未开放 | §3.1 明确保留 | **通过** |
| V1.6 目录 IA 不回退 | §3.1 明确 | **通过** |
| 订单 UI 状态 loading/empty/error/success | **未在 §3.1 或 state-matrix 冻结区提及** | **缺口**（ISSUE-003 non-blocking） |

### 5. 数据模型（t_order_*） — **部分通过**

| 检查项 | 计划 §3.2 | SNAP / PRD | 判定 |
|---|---|---|---|
| 表名 `t_` 前缀 + 审计四件套 + del_flag | 明确 | CLAUDE.md 规则 ✓ | **通过** |
| 无跨表外键 | 明确 | — | **通过** |
| `t_order_header` 字段 | 列举含订单号/状态/需求方/提供方/产品快照/备注/须知版本/含税/未税/币种/上链次数 | SNAP-014/016 ✓ | **通过** |
| `t_order_line` | 计费明细（单位/数量/单价/小计） | PRD「在线计费信息仍须填写」 | **提交入口未明确**（ISSUE-001） |
| 金额精度 | 「人民币两位小数」 | PRD「两位小数」 | **DECIMAL(M,N) 未指定**（ISSUE-001） |
| `t_order_attachment` | 文件名/类型/大小/存储键/扫描结果 | REQ-013 ✓ | **通过** |
| `t_order_contract_version` | 版本号与 JSON 快照 | REQ-017 ✓ | **通过** |
| `allow_simple_order` | TINYINT DEFAULT 1 | §1.2 假设 | **通过** |

### 6. mock 上链与 ChainAttestationPort — **未通过**

| 检查项 | 计划描述 | 只读现状 | 判定 |
|---|---|---|---|
| 订单事件 mock | §3.1：「允许订单事件以 mock 适配调用（可扩展 request 类型或并列 OrderAttestation 说明）」 | `ChainAttestationPort.md`：签名仅 `AttestationRequest(productCode, versionNo, catalogSnapshotJson)` | **二选一未定**（ISSUE-001 P0） |
| 订单 attestation payload | §3.1 未列字段 | — | **未冻结** |
| mock 适配失败不阻断 | §1.2 / 913 acceptance 明确 | — | **通过** |
| `t_order_chain_log` | 哈希/高度/时间等，来源模拟适配器 | — | **通过（表级）** |

### 7. 分页 API 设计 — **部分通过**

| 检查项 | 计划描述 | 判定 |
|---|---|---|
| query 参数 | page/pageSize；档位 10/20/50/100 | **通过** |
| 改 pageSize 回第 1 页 | §1.5 / 915 acceptance | **通过** |
| 响应 envelope | **未指定**（total/list/page/pageSize/pages） | **缺口**（ISSUE-003） |
| 非法 pageSize | 913 acceptance：「分页非法 pageSize 4xx」 | **通过** |

### 8. 脱敏 API 层实现 — **通过（with caveats）**

| 检查项 | 计划描述 | 判定 |
|---|---|---|
| 脱敏方向 | §2.2 OQ-V17-001 实现假设：ADMIN 完整可读；PROVIDER 部分截断；USER 仅本人明文 | **方向正确** |
| 附件下载权限 | 需求方本人/本企业 PROVIDER/ADMIN 可下载；越权 403 | **通过** |
| 实现假设标记 | OQ-V17-001 标 OPEN non-blocking | **通过**（不假装已关） |
| SensitiveId 对齐 | 918 对齐目录 SensitiveId | **通过（波次合理）** |

### 9. 与 V1.6 API 兼容性 — **通过**

| 检查项 | 判定 |
|---|---|
| 新增 `/orders` tag | 不影响既有 auth/overview/catalog/chain/admin tag |
| 新增路径不冲突 | `/orders` 与既有 `/catalog/**`、`/auth/**`、`/admin/**` 无重叠 |
| 既有端点不改 | §3.1 明确「既有不回退」；V1.6 单元格保持 2.3.3 原文 |
| 新增错误码 | 不与既有 ERR_* 冲突（911 需在 codes.yaml 增补订单相关码） |

### 10. front-end client 生成（TASK-WSC-911） — **部分通过**

| 检查项 | 判定 |
|---|---|
| 911 生成 `frontend/src/api/**` | 任务定义明确 |
| typecheck 验收 | 911 acceptance 含「client 生成后可 typecheck」 |
| OpenAPI 规范正确性 | **依赖上述 ISSUE 闭合后方可完整**（路径/query 枚举/body schema/response schema） |

### 11. VERSION bump 逻辑 — **通过（小瑕疵）**

| 检查项 | 判定 |
|---|---|
| 保持 2.3.3 | §3.1 明确 |
| 禁止他任务 bump | §0.3 / §1.4 明确 |
| 四头同步验收 | 911 acceptance 含 VERSION/matrix/openapi/state-matrix 同一 semver |
| 计划 YAML 头 `contractsTarget: 2.3.2` vs body 2.3.3 | **小瑕疵**：头字段与 body 不一致，但 body 解释充分（ISSUE-006 non-blocking） |

## 无异议项（记录）

- 911 **唯一**拥有 `contracts/**` + `frontend/src/api/**`；912 独占 Flyway；913→916 串行订单 BE — 分工合理。
- DAG 无环；允许的并行写集（912∥914、913∥914）路径不相交。
- 状态机四态+旁路与 SNAP 一致；不可回退；无拒绝/退回/超时。
- ADMIN 代操作范围（确认订单/确认合约/取消，不可代上传）与 SNAP-011 一致。
- PROVIDER 企业隔离（`ownEnterpriseAsProvider`）与 SNAP-001 一致。
- `allow_simple_order` 默认 1 假设已标 OPEN，不假装 PO 已关。
- 订单错误码可复用 `ERR_FORBIDDEN`（403）；新增订单专属码（如 `ERR_ORDER_STATE_INVALID`、`ERR_ATTACHMENT_INVALID` 等）归 911 在 `codes.yaml` 增补，方向无异议。
- 919 E2E 独占 Playwright + 独立 v17 config + 保留 v16/v14 — 与 V1.6 回归共存合理。

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-API-WSC9-R1-001 | **P0** | §3.1 冻结表写 `ChainAttestationPort.md: 允许订单事件以 mock 适配调用（可扩展 request 类型或并列 OrderAttestation 说明）`；当前端口签名仅 `AttestationRequest(productCode, versionNo, catalogSnapshotJson)` 面向产品目录快照。911 若不确定扩展现有 `AttestationRequest` 还是新建 `OrderAttestationRequest(orderId, status, snapshotJson)` 并列接口，将导致 OpenAPI 与 `ChainAttestationPort.md` 无法冻结、913/916 mock 适配器实现双轨。 | §3.1 **二选一并写死**：（a）**扩展** `AttestationRequest` 增加 `requestType` 枚举（`CATALOG`/`ORDER`）+ `orderSnapshotJson` 可选字段，原 catalog 调用不受影响；或（b）**并列**新接口 `OrderChainAttestationPort.attestOrder(OrderAttestationRequest)` 与现有 `ChainAttestationPort` 共存，913/916 注入对应端口。911 acceptance 含 `ChainAttestationPort.md` 更新 + 订单 attestation payload 字段枚举（orderId、status、timestamp、actorRole、actorUserId、snapshot 摘要等）；913/916 testScope 含 mock 适配返回字段校验。 | REQ-WSC-ORDER-016, REQ-CHAIN-001 |
| ISSUE-API-WSC9-R1-002 | **P1** | §3.1 冻结表写 `POST /orders/{orderId}/contract: multipart：签署附件 + 交易信息 JSON`，但未指定：① multipart part 名称（如 `file` vs `attachment`、`transactionInfo` vs `data`）；② 交易信息 JSON 顶层字段（是否含 `orderLines[]` 计费明细数组、每行 `unit`/`quantity`/`unitPrice`/`subtotal`）；③ `t_order_line` 计费明细行由哪个端点写入（创建时 `POST /orders` 仅有 productId/remark/noticeAccepted，无计费字段）。PRD §金额：「在线计费信息仍须填写；单价须大于零；缺少单位或数量不能提交；明细与小计须正确」——这些字段的提交时序与端点归属未冻结。 | §3.1 **硬冻结**：`POST /orders/{orderId}/contract` multipart 结构为 `file`（签署附件，required）+ `transactionInfo`（JSON string，含 `orderLines[{unit, quantity, unitPrice, subtotal}]`、`amountTaxInclusive`、`amountTaxExclusive`、`currency`）。明确 `orderLines` 仅在 contract 阶段提交，`POST /orders` 创建时不含计费。911 OpenAPI requestBody 写死 part name + schema；916 acceptance 含 `orderLines` 校验（单价>0、缺单位/数量 4xx、小计正确）。 | REQ-WSC-ORDER-013, REQ-WSC-ORDER-014, REQ-WSC-ORDER-010 |
| ISSUE-API-WSC9-R1-003 | **P1** | §3.1 冻结表仅写「分页列表」，未列举 `GET /orders` 响应字段。PRD 列表九组字段已明确（订单号、产品信息[名称/编码/类型/来源平台]、提供方、需求方[姓名/用户名/单位]、金额、状态、上链次数、下单时间、操作），但 911 无从得知哪些字段进列表响应 vs 仅详情可见。分页响应 envelope（`total`/`list`/`page`/`pageSize`/`pages`）亦未冻结。 | §3.1 **增补** `GET /orders` 200 响应字段列表（引用 PRD 九组字段或直接枚举）+ 分页 envelope 结构（建议复用 V1.6 维护页约定：`{ total, list, page, pageSize }`）。911 OpenAPI 写死 response schema；915 acceptance 含列表字段 testid 齐全。 | REQ-WSC-ORDER-007, REQ-WSC-ORDER-008 |
| ISSUE-API-WSC9-R1-004 | P1 | §3.1 冻结表 `POST /orders/{orderId}/confirm` 仅写「确认订单；PROVIDER 本企业或 ADMIN；PENDING_CONFIRM → PENDING_UPLOAD」，**未指定 request body**。同版 `POST /orders/{orderId}/contract/confirm` 有 `noticeAccepted=true` body。若 confirm 无 body（纯动作），911 OpenAPI 应写 `requestBody: {}` 或省略；若有校验字段（如确认须知），则须冻结。当前不一致易导致 913/916 实现分歧。 | §3.1 **明确**：`POST /orders/{orderId}/confirm` 为纯动作端点，**无 request body**（或空 JSON 对象）；911 OpenAPI 写死。若后续需要确认须知等字段，在此处冻结。 | REQ-WSC-ORDER-010, REQ-WSC-ORDER-011 |
| ISSUE-API-WSC9-R1-005 | P2 | `GET /orders/notices/current` 返回平台统一须知正文。`t_order_header` 含「须知版本」字段（§3.2），但 API 响应未指定是否返回版本标识（如 `version`/`noticeVersion`）。若响应不含版本，前端无法在确认时回传接受的版本号；若 `t_order_header` 的 `notice_version` 仅由服务端写入当前版本，则无问题但需明确。 | §3.1 **补充**：`GET /orders/notices/current` 响应含 `{ content: string, version: string }` 或仅 `{ content: string }`（服务端隐式记录当前版本）。911 OpenAPI 写死。若选后者，912/913 确认 `notice_version` 由服务端自动填充。 | REQ-WSC-ORDER-004 |
| ISSUE-API-WSC9-R1-006 | P2 | 计划 YAML 头 `contractsTarget: wsc-contracts@2.3.2`，但 §0.2/§3.1 明确工作树为 2.3.3 且「默认不再升 semver」。四头（VERSION/matrix/openapi/state-matrix）已同步为 2.3.3。头字段与 body 不一致为小瑕疵，不影响执行但易造成审计歧义。 | **建议** round 2 修订时将 YAML 头 `contractsTarget` 改为 `wsc-contracts@2.3.3` 以与 body 一致。非阻塞。 | — |

## 决策

`decision: REQUEST_CHANGES`

- **openIssueCount**: 6（P0×1，P1×3，P2×2）
- **closedIssueCount**: 0

本评审 **仅**输出 apiDataDesigner 视角 ISSUE；**未**修改计划正文、`contracts/**`、Run `state`/`events`；**未**代关 planEditor / productAnalyst 或其他角色 ISSUE；**未** commit。

**批准前最小闭合集（本角色）**：ISSUE-001（ChainAttestationPort 订单请求类型，P0）+ ISSUE-002（multipart 合约上传结构，P1）+ ISSUE-003（列表响应字段与分页 envelope，P1）为阻塞项；004/005/006 建议同轮吸收以免 911 返工。
