# Round 2 Review — API/Data Designer

```yaml
reviewId: REV-PLAN-WSC9-R2-api-data-designer
planId: PLAN-WSC-9.2
round: 2
role: apiDataDesigner
actorInstance: api-data-designer-wsc-012-r2
snapshotIdAtReview: SNAP-WSC-009
contractsBaselineAtReview: wsc-contracts@2.3.3
contractsTargetAtReview: wsc-contracts@2.3.3
decision: APPROVE
summary: |
  R1 六条 ISSUE 全部正确吸收并关闭：ISSUE-001（P0）ChainAttestationPort 已冻结为方案 B 并列接口
  OrderChainAttestationPort，payload 五字段枚举明确；ISSUE-002 multipart part 名与 transactionInfo
  JSON schema 已硬冻结，orderLines 提交时序明确；ISSUE-003 列表九组字段与分页 envelope 已枚举；
  ISSUE-004 confirm 端点已明确为纯动作无 body；ISSUE-005 须知响应已含 version 字段且服务端自动填充；
  ISSUE-006 contractsTarget 采取保持 YAML 头 2.3.2 + body 补充说明方案，歧义已消除。修订未引入新
  API 设计问题。批准前最小闭合集（001+002+003）满足。decision=APPROVE。
```

## 评审范围（本角色）

| 维度 | 本轮动作 |
|---|---|
| 权威输入 | `planning/proposals/PLAN-WSC-9.2.md` §3 / §4 / §5；`product/requirements/SNAP-WSC-009.md` |
| 历史对照 | `planning/reviews/wsc-9.1/round-1/REV-PLAN-WSC9-R1-api-data-designer.md`（R1 六条 ISSUE）；`planning/reviews/wsc-9.1/round-1/SUMMARY.md` |
| 聚焦 | R1 六条 ISSUE 吸收验证；修订是否引入新 API 设计问题 |
| 不裁定 | 页面交互/文案；威胁建模；他角色 ISSUE；DAG/写集工程细节 |
| 禁区 | 不代批他角色；不写 `ai/runs/**`；不改 `contracts/**` / 计划 / state / events；不 commit |

## R1 ISSUE 逐条复评

### ISSUE-001（P0）— ChainAttestationPort 订单事件请求类型 — **CLOSED**

| 维度 | R1 closeWhen 要求 | PLAN-WSC-9.2 证据 | 判定 |
|---|---|---|---|
| 方案选定 | §3.1 二选一并写死 | §3.1 冻结表明确「**并列方案**：新建 `OrderChainAttestationPort` 接口（`attestOrder(OrderAttestationRequest)`），与现有 `ChainAttestationPort`（面向产品目录快照）共存」 | **方案 B 已冻结** |
| payload 字段枚举 | orderId、status、snapshot 摘要等 | §3.1 冻结 `OrderAttestationRequest` 字段：`orderId`（string）、`status`（string，状态枚举值）、`actorRole`（string，ADMIN/PROVIDER/USER）、`actorUserId`（string）、`snapshotJson`（string，订单快照摘要） | **五字段明确** |
| 911 acceptance | ChainAttestationPort.md 更新 + 订单 attestation payload 字段枚举 | 911 acceptance：「`ChainAttestationPort.md` 更新（现有签名不变）+ 新增 `OrderChainAttestationPort.md`（§3.1 冻结的并列接口 + payload 字段枚举）」 | **通过** |
| 913/916 注入 | 注入对应端口 | §3.1：「913/916 注入 `OrderChainAttestationPort`」；913 readSet 含 `OrderChainAttestationPort`（只读接口定义）；916 readSet 含 `OrderChainAttestationPort` | **通过** |
| mock 适配 | mock 适配返回字段校验 | 913 writeSet 含 `OrderChainAttestationPort.java` + `SimulatedOrderChainAttestationAdapter` mock 实现；913 acceptance：「mock 上链使用 `OrderChainAttestationPort`；mock 记录哈希/高度/节点为模拟值」 | **通过** |

**结论**：方案 B（并列新接口）已完全冻结，payload 字段枚举明确，911/913/916 acceptance 与 testScope 均已对齐。**P0 阻塞项已消除。**

---

### ISSUE-002（P1）— multipart 合约上传字段名与交易信息 JSON schema — **CLOSED**

| 维度 | R1 closeWhen 要求 | PLAN-WSC-9.2 证据 | 判定 |
|---|---|---|---|
| part 名称 | `file`（签署附件，required）+ `transactionInfo`（JSON string） | §3.1：「part `file`（签署附件，required，Word/PDF ≤20MB）+ part `transactionInfo`（JSON string，required）」 | **硬冻结** |
| transactionInfo schema | `orderLines[{unit, quantity, unitPrice, subtotal}]`、`amountTaxInclusive`、`amountTaxExclusive`、`currency` | §3.1：「`transactionInfo` schema：`{ orderLines: [{ unit: string, quantity: number, unitPrice: string, subtotal: string }], amountTaxInclusive: string, amountTaxExclusive: string, currency: \"CNY\" }`」 | **JSON schema 完整冻结** |
| orderLines 提交时序 | 仅在 contract 阶段提交，POST /orders 创建时不含计费 | §3.1：「`orderLines` **仅**在 contract 阶段提交，`POST /orders` 创建时不含计费」；§3.2：「`t_order_line`：计费明细；**仅**由 `POST /orders/{orderId}/contract` 写入」 | **明确** |
| 911 OpenAPI | 写死 part name + schema | 911 objective：「按 §3.1 **一次性冻结**订单读/建/全部写状态与附件/确认/取消的 OpenAPI 与矩阵」 | **通过** |
| 916 acceptance | orderLines 校验 | 916 acceptance：「单价 ≤0 或缺单位/数量 → 不能提交；明细小计正确；含税=未税=同一金额两位小数」 | **通过** |

**结论**：multipart part 名、transactionInfo JSON schema、orderLines 提交时序均已硬冻结。911/916 acceptance 对齐。

---

### ISSUE-003（P1）— GET /orders 响应字段枚举与分页 envelope — **CLOSED**

| 维度 | R1 closeWhen 要求 | PLAN-WSC-9.2 证据 | 判定 |
|---|---|---|---|
| 九组字段枚举 | 引用 PRD 或直接枚举 | §3.1 冻结表逐组枚举：①订单号（orderId）②产品信息（productName/productCode/productType/sourcePlatform）③提供方（providerEnterpriseName）④需求方（demandUserName/demandUserLoginName/demandEnterpriseName）⑤金额（amountTaxInclusive/amountTaxExclusive/currency）⑥状态（status/statusLabel）⑦上链次数（chainCount）⑧下单时间（createTime）⑨操作（由 FE 按角色渲染） | **九组完整枚举** |
| 分页 envelope | `{ total, list, page, pageSize }` | §3.1：「分页 envelope：`{ total: number, list: OrderListItem[], page: number, pageSize: number }`」 | **冻结** |
| 911 OpenAPI | 写死 response schema | §3.1：「911 OpenAPI 写死 response schema」 | **通过** |
| 915 acceptance | 列表字段 testid 齐全 | 915 acceptance：「列表九组字段（与 §3.1 `GET /orders` 200 响应字段枚举一致）」；915 testScope：「列表字段 testid 齐全（九组字段对应 testid）」 | **通过** |
| 913 acceptance | 列表响应字段与分页 envelope 对齐 | 913 acceptance：「列表响应字段与分页 envelope 与 §3.1 `GET /orders` 200 响应定义一致」 | **通过** |

**结论**：九组字段与分页 envelope 已完整枚举并冻结，911/913/915 acceptance 均已对齐。

---

### ISSUE-004（P1）— POST /orders/{orderId}/confirm request body — **CLOSED**

| 维度 | R1 closeWhen 要求 | PLAN-WSC-9.2 证据 | 判定 |
|---|---|---|---|
| body 指定 | 明确为纯动作端点，无 request body（或空 JSON 对象） | §3.1：「确认订单；**纯动作端点，无 request body（空 JSON 对象）**；PROVIDER 本企业或 ADMIN；`PENDING_CONFIRM` → `PENDING_UPLOAD`」 | **已明确** |
| 911 OpenAPI | 写死 | 911 objective 覆盖全部写状态 OpenAPI | **通过** |

**结论**：已明确为纯动作端点，无 body。与 `POST /orders/{orderId}/contract/confirm`（有 `noticeAccepted=true` body）的区分清晰。

---

### ISSUE-005（P2）— GET /orders/notices/current 版本标识 — **CLOSED**

| 维度 | R1 closeWhen 要求 | PLAN-WSC-9.2 证据 | 判定 |
|---|---|---|---|
| 响应结构 | 含 `{ content, version }` 或仅 `{ content }` | §3.1：「响应 `{ content: string, version: string }`；`notice_version` 由服务端自动填充至 `t_order_header`，前端无需回传版本号」 | **已选含 version 方案** |
| 服务端自动填充 | 若选含 version，确认 notice_version 写入逻辑 | §3.1 明确「`notice_version` 由服务端自动填充至 `t_order_header`，前端无需回传版本号」；§1.2 假设：「响应含版本标识（§3.1）」 | **通过** |

**结论**：响应含 `content` + `version`，`notice_version` 由服务端自动填充，前端无需回传。时序清晰。

---

### ISSUE-006（P2）— contractsTarget 元数据不一致 — **CLOSED**

| 维度 | R1 closeWhen 要求 | PLAN-WSC-9.2 证据 | 判定 |
|---|---|---|---|
| YAML 头 vs body | R1 建议改 YAML 头为 2.3.3 | YAML 头仍为 `contractsTarget: wsc-contracts@2.3.2`；§0.1 表格行增加 `<!-- R2 revised: ISSUE-API-006 -->` 注释，说明：「工作树当前为 2.3.3，由 911 唯一管理增量」；§3.1 说明「保持工作树当前 semver（起草时 2.3.3）」 | **方向不同但合理** |
| 歧义消除 | 头字段与 body 不再造成审计歧义 | YAML 头保持 2.3.2 与 SNAP 声明一致（SNAP 为需求权威）；body 充分解释 2.3.3 工作树现状与增量策略；§1.2 假设表重复确认 | **歧义已消除** |

**结论**：planEditor 采取了不同于 R1 建议的方案——保持 YAML 头与 SNAP 一致（2.3.2），在 body 充分解释工作树 2.3.3 现状。此方案更合理（SNAP 为需求权威，YAML 头应与 SNAP 对齐），歧义已消除。

---

## 修订是否引入新 API 设计问题

| 检查维度 | 判定 |
|---|---|
| §3.1 新增/修改端点完整性 | 全部 9 个订单端点 + 1 个须知端点已覆盖，无遗漏 |
| RBAC 矩阵新增单元格 | 8 个订单能力键与 SNAP 角色表一致，V1.6 单元格未改 |
| 新增端点与既有端点冲突 | `/orders` 与 `/catalog/**`、`/auth/**`、`/admin/**` 无重叠 |
| 状态枚举完整性 | 5 个状态（PENDING_CONFIRM/PENDING_UPLOAD/PENDING_CONTRACT_CONFIRM/CONTRACT_REACHED/CANCELLED）与 SNAP 状态机一致 |
| 金额字段一致性 | 含税/未税双字段、CNY、两位小数 string/decimal，贯穿 §3.1/§3.2 |
| 订单号规范 | UUID v4、VARCHAR(36)、不含业务语义字段，§1.2/§3.2/913 acceptance 一致 |
| mock 上链隔离 | §1.2 硬性禁止读取真实链配置；913/916 acceptance/testScope 含负例 |
| 安全说明 | §3.1 OpenAPI 安全说明冻结：scope 仅信 SessionPrincipal，客户端参数不作授权依据 |
| 新增 ISSUE | **无** |

**结论**：修订未引入新的 API 设计问题。

---

## 无异议项（记录）

- 911 **唯一**拥有 `contracts/**` + `frontend/src/api/**`；912 独占 Flyway；913→916 串行订单 BE — 分工合理。
- DAG 无环；允许的并行写集（912∥914、913∥914）路径不相交。
- 状态机四态+旁路与 SNAP 一致；不可回退；无拒绝/退回/超时。
- ADMIN 代操作范围（确认订单/确认合约/取消，不可代上传）与 SNAP-011 一致。
- PROVIDER 企业隔离（`ownEnterpriseAsProvider`）与 SNAP-001 一致。
- `allow_simple_order` 默认 1 假设已标 OPEN，不假装 PO 已关。
- `OrderChainAttestationPort` 并列方案不影响现有 `ChainAttestationPort` 签名，向后兼容。
- 919 E2E 场景 #10（数字合约版本）标为「建议」而非强制，与 REQ-017 P1 优先级一致。
- `t_order_line` 仅由 `POST /orders/{orderId}/contract` 写入，与 §3.2 数据模型声明一致。

---

## 决策

`decision: APPROVE`

- **R1 issueCount**: 6（P0×1，P1×3，P2×2）
- **R2 closedCount**: 6
- **R2 openCount**: 0
- **newIssueCount**: 0
- **最小闭合集（本角色）**: ISSUE-001（P0）+ ISSUE-002（P1）+ ISSUE-003（P1）→ **已全部满足**

本评审 **仅**输出 apiDataDesigner 视角复评；**未**修改计划正文、`contracts/**`、Run `state`/`events`；**未**代关 planEditor / productAnalyst 或其他角色 ISSUE；**未** commit。
