# Round 1 Review — Security / Operations（PLAN-WSC-9.1）

```yaml
reviewId: REV-PLAN-WSC9-R1-security-operations
planId: PLAN-WSC-9.1
round: 1
role: securityOperations
snapshotIdAtReview: SNAP-WSC-009
actorInstance: security-operations-wsc-012-r1
decision: REQUEST_CHANGES
summary: |
  对照 SNAP-WSC-009 与 PLAN-WSC-9.1：三角色 RBAC 矩阵（§3.1/§4）与 SNAP 角色表一致；ADMIN 代操作
  限定「可代 PROVIDER 确认订单/确认合约/取消，不可代上传」；附件白名单 + 20MB + 扫描端口 + 按角色下载；
  状态不可回退（逆向 POST → 4xx）；Flyway 审计四件套 + del_flag；前端隐藏≠授权等方向正确。
  阻断 APPROVE 的缺口：
  ① 订单号生成规则缺失，无不可预测性与信息防泄露约束；
  ② 客户端 enterpriseId/userId 参数 IDOR 风险未在 913/916 acceptance 中以负例强制覆盖；
  ③ ADMIN 代操作审计：update_by 需记录 ADMIN 身份而非订单关联方，时间线需记录代操作人，acceptance/testScope 未明确；
  ④ mock 上链隔离未硬性禁止暴露真实链节点配置/私钥。
  无证据 BLOCK 业务目标；不代批其他角色；不伪造高风险接受；不写 plan/state/events。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-9.1.md`（`CANDIDATE` / `DRAFT` / Round 1） |
| 快照 | `product/requirements/SNAP-WSC-009.md`（`status: APPROVED`；PO 2026-08-19） |
| 功能基线 | `SNAP-WSC-008` / `PLAN-WSC-8.3` / `wsc-contracts@2.3.2` |
| 本角色焦点 | 订单 API 权限一致性、ADMIN 代操作审计、角色越权防护、附件安全、状态不可回退、mock 上链隔离、脱敏、Flyway 安全、订单号安全、前端 OWASP、审计日志 |
| 本实例 | `security-operations-wsc-012-r1` |

## 评审范围与方法

| 维度 | 方法 | 结论 |
|---|---|---|
| 订单 API 权限（USER/PROVIDER/ADMIN） | 对照 SNAP-WSC-009 角色权限表 vs §3.1 RBAC 矩阵 | **通过** — 矩阵逐 cell 与 SNAP 一致 |
| ADMIN 代操作限制 | §3.1 矩阵 `ordersConfirmApi`/`ordersConfirmContractApi`/`ordersCancelApi` ADMIN=200 any；§1.2 假设「不可代上传」 | **通过方向** — 审计细节缺口见 ISSUE-003 |
| 角色范围防越权 | §3.1「scope 仅信 SessionPrincipal」+ §4 测试矩阵 + 913 acceptance「enterprise 参数不扩大可见面」 | **部分缺口** — 客户端参数 IDOR 负例未硬绑定（ISSUE-002） |
| 附件安全 | §1.2 假设 + §2.2 OQ-V17-002 端口 + §3.1 `ordersAttachmentGet` 按角色 | **通过** — 白名单 + 20MB + 扫描端口 + 按角色下载齐全 |
| 状态不可回退 | §1.2「不可回退」+ 916 acceptance「逆向 POST → 4xx，状态不变」 | **通过** |
| mock 上链隔离 | §1.2 + §8「复用 Simulated 端口」 | **缺口** — 未硬性禁止暴露真实链配置（ISSUE-004） |
| 脱敏规则 | §2.2 OQ-V17-001 实现假设 + 918 目标 | **通过方向** — OPEN non-blocking，假设合理 |
| Flyway 脚本安全 | §3.2 迁移协议 + 912 acceptance | **通过** — 审计四件套 + del_flag + 无 FK + 幂等 |
| 订单号生成安全性 | 全文检索 | **缺口** — 无生成规则（ISSUE-001） |
| 前端 OWASP Top 10 | §1.3 技术栈 + §1.5 UX 规则 | **通过方向** — Vue 3 + TS + Ant Design Vue，无额外风险面 |
| 审计日志 | §3.2 审计四件套 + 916 时间线 + mock 上链 | **通过方向** — ADMIN 代操作审计细化见 ISSUE-003 |

## 风险对照

| 风险点 | 计划措施 | 判定 |
|---|---|---|
| USER 看到他人需求方订单 | §3.1 `ordersListApi` USER=200 ownAsDemand | **通过** |
| PROVIDER 看到他企提供方订单 | §3.1 `ordersListApi` PROVIDER=200 ownEnterpriseAsProvider | **通过** |
| ADMIN 代 USER 上传合约 | §3.1 `ordersSubmitContractApi` ADMIN=403；§1.2「不可代需求方上传」 | **通过** |
| PROVIDER 代他企确认订单 | §3.1 `ordersConfirmApi` PROVIDER=200 ownEnterpriseAsProvider + 916 acceptance「PROVIDER 他企确认→403」 | **通过** |
| 客户端篡改 enterpriseId 扩大可见面 | §3.1「scope 仅信 SessionPrincipal；拒绝客户端 enterprise/user 作授权」 | **缺口** — 913/916 acceptance 未硬绑定 IDOR 负例（ISSUE-002） |
| 订单号泄露业务信息或可预测 | 未提及 | **缺口**（ISSUE-001） |
| ADMIN 代操作审计不可追溯 | §3.2 审计四件套 + 916 时间线 | **缺口** — 代操作人身份未明确（ISSUE-003） |
| mock 上链暴露真实链节点/私钥 | §8「复用 Simulated 端口；验收不以链网可用为门禁」 | **缺口** — 未硬性禁止（ISSUE-004） |
| 附件类型绕过（仅 MIME 不查扩展名） | §2.2「白名单+病毒扫描」；SNAP「Word/PDF」 | **通过** — 白名单含 MIME/类型约束 |
| 扫描失败附件入库 | §2.2「扫描失败或未扫描→附件不合格」 | **通过** |
| 状态回退（后端 API 绕过前端） | 916 acceptance「逆向 POST → 4xx，状态不变」 | **通过** |
| Flyway 脚本 SQL 注入 | §3.2 参数化约定 + 912 幂等 | **通过** |
| 前端 XSS/CSRF | Vue 3 默认转义 + Ant Design Vue 组件 | **通过方向** — 标准框架防护 |

## ISSUE 表

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| ISSUE-SEC-WSC9-R1-001 | P1 | 全文未定义订单号生成规则。§3.1 仅写「成功后有订单号」；§3.2 表结构仅列字段类型。若采用自增 ID、时间戳拼接或含企业编码的格式，将导致：（1）可预测性——竞争对手可枚举订单量；（2）信息泄露——订单号含企业 ID/产品编码/时间可被外部推断业务节奏。SNAP-WSC-009 REQ-WSC-ORDER-006 验收「成功后有订单号」，隐含不可泄露业务信息的安全预期。 | （1）§1.2 假设或 §3.2 数据迁移协议增加订单号生成安全口径：**不可预测**（推荐 UUID v4 或加密随机前缀 + 自增）、**不含可推断业务语义的字段**（企业 ID、产品编码、用户 ID）；（2）913 `acceptance` 增加：订单号格式不包含需求方/提供方/产品标识，不同订单的订单号无可观察递增规律；（3）913 `testScope` 至少一条集成断言：订单号不匹配 `^\d+$`（或自增模式）、不含 `productId`/`enterpriseId` 子串。 | REQ-WSC-ORDER-006 |
| ISSUE-SEC-WSC9-R1-002 | P1 | §3.1 安全说明写「订单 scope 仅信 SessionPrincipal；拒绝客户端 enterprise/user 作授权」；913 acceptance 含「篡改 query enterpriseId 不扩大可见面」。但：（1）913 acceptance 仅测「enterpriseId」，未覆盖 `userId`/`demandUserId` 等可能暴露的参数；（2）916 acceptance（确认订单/确认合约/取消）**未**包含客户端携带篡改 enterprise/user 参数仍按 SessionPrincipal 授权的负例；（3）§3.1 OpenAPI 冻结内容未写 negative security note（如「忽略客户端 enterpriseId 参数」）。若实现时未在 service 层强制忽略客户端参数，前端传参与后端 scope 之间的 gap 可导致 IDOR。 | （1）913 `acceptance` 扩展负例：客户端传 `userId`/`demandUserId` 参数（若 API 暴露面存在）**不扩大**可见面；（2）916 `acceptance` 增加：确认订单/确认合约/取消请求携带篡改 `enterpriseId` 或 `userId` 参数 → 行为与不携带一致（仍按 SessionPrincipal 授权），越权 → 403；（3）§3.1 OpenAPI 订单 path description 或 security note 增加：「授权范围**仅**从服务端会话推导，客户端 enterprise/user 参数**不**作为授权依据」。 | REQ-WSC-ORDER-001, REQ-WSC-ORDER-011 |
| ISSUE-SEC-WSC9-R1-003 | P1 | §3.2 要求审计四件套（`create_by`/`update_by`），916 时间线 + mock 上链记录状态变更。但 ADMIN 代操作时：（1）`update_by` 应记录**代操作 ADMIN 的身份**（而非被代操作方的需求方/提供方用户），计划未明确；（2）时间线事件应区分「操作人=ADMIN(代)」与「操作人=PROVIDER/USER(本人)」，计划仅写「各状态变更写时间线」，未要求记录代操作人角色与身份；（3）916 `testScope` 含「ADMIN 可代确认/取消」正例，但**未**要求验证 `update_by` = ADMIN 而非 PROVIDER、时间线操作人字段 = ADMIN。ADMIN 代操作是特权行为，审计不可追溯将导致合规与问责缺口。 | （1）916 `acceptance` 增加：ADMIN 代操作确认订单/确认合约/取消时，`update_by` 记录 ADMIN 用户标识（非被代操作方）；时间线事件 `operator` 字段记录 ADMIN 身份与角色；（2）916 `testScope` 增加集成断言：ADMIN 代 PROVIDER 确认后，`t_order_header.update_by` = ADMIN 用户名、时间线最新事件 operator = ADMIN；（3）913 `acceptance`（创建路径）确认 `create_by` = 创建会话 USER。 | REQ-WSC-ORDER-011, REQ-WSC-ORDER-016 |
| ISSUE-SEC-WSC9-R1-004 | P2 | §1.2 假设「上链 mock」+ §8 风险「复用 Simulated 端口；去掉真实 SDK 依赖」。但计划未硬性约束：（1）`SimulatedChainAttestationPort`（或 `OrderAttestation` mock 实现）**不得**读取/暴露真实链节点 URL、私钥、证书等配置；（2）配置项（如 `chain.node.url`、`chain.private.key`）在 mock 模式下应有安全默认值或显式禁用，避免开发/测试环境意外连接真实链；（3）916 `acceptance`（mock 上链）未要求验证 mock 记录中**不含**真实链节点信息。当前风险为 P2（实现细节），但若开发时误接真实链配置，可能导致测试数据污染生产链或密钥泄露。 | （1）§1.2 假设或 §3.2 数据迁移协议补充：mock 上链实现**禁止**读取真实链节点配置（URL/私钥/证书）；mock 模式下链相关配置项应有安全默认值（如空字符串或 localhost 占位）；（2）916 `testScope` 或 913 `testScope` 增加：mock 上链记录的哈希/高度/节点字段为模拟值，不匹配真实链浏览器可验证格式。 | REQ-WSC-ORDER-016 |

## 非阻塞备注（记录，不构成 REQUEST_CHANGES）

| 项 | 说明 |
|---|---|
| 附件 MIME + 扩展名白名单 | §2.2 仅写「白名单」，建议 916 acceptance 明确：MIME 类型（`application/pdf`、`application/msword`、`application/vnd.openxmlformats-officedocument.wordprocessingml.document`）**与**文件扩展名（`.pdf`/`.doc`/`.docx`）双重校验，防 MIME 伪造 |
| 扫描失败用户提示 | §2.2「扫描失败→附件不合格」，建议实现时返回通用错误消息（如「附件不符合安全要求」），**不**暴露扫描引擎品牌/版本/失败原因细节 |
| 脱敏假设（OQ-V17-001） | §2.2 实现假设方向合理：ADMIN 全明文、PROVIDER 按 SensitiveId 截断、USER 仅本人明文。OPEN non-blocking，不阻断本轮 |
| 前端 OWASP | Vue 3 默认 HTML 转义 + Ant Design Vue 组件库 XSS 防护；`RULE-GLOBAL-FRONTEND` 已覆盖 toast/二次确认；无额外注入面。建议 917 acceptance 确认用户输入（备注/交易信息）在展示时经过转义 |
| V1.6 回归风险 | §0.2 预落地对账 + §1.4 门禁 + 914 acceptance「V1.6 目录可见性不回退」方向正确；919 E2E `test:p0-v16` 回归可接受 |
| Flyway 脚本安全 | §3.2 协议完整：`CREATE TABLE IF NOT EXISTS`、审计四件套、`del_flag`、无 FK、`t_` 前缀、幂等。类型安全（`BIGINT UNSIGNED`、`VARCHAR`、`DATETIME(3)`、`TINYINT`）无注入面 |
| 产品 `allow_simple_order` 默认 1 | §1.2 标明为实现假设；既有产品默认允许链内订购。非安全阻断，但建议上线后评估是否需按产品类型差异化默认值 |

## 无异议项（记录）

- §3.1 RBAC 矩阵逐 cell 与 SNAP-WSC-009 角色表一致；V1.6 单元格不回退 — **通过**。
- ADMIN 代操作范围：可代确认订单/确认合约/取消；**不可**代上传合约 — 与 SNAP `poLockedDefaults.adminCanActAsProvider` 一致。
- PROVIDER 企业隔离：`ordersListApi`=ownEnterpriseAsProvider、`ordersConfirmApi`=ownEnterpriseAsProvider、`ordersCancelApi`=ownEnterpriseAsProvider — 916 acceptance「PROVIDER 他企确认→403」。
- USER 数据范围：`ordersListApi`=ownAsDemand、`ordersCreateApi`=200、`ordersSubmitContractApi`=ownAsDemand、`ordersCancelApi`=ownAsDemand。
- 附件安全链：白名单 + 20MB + `OrderAttachmentScanPort`（可替换真实引擎）+ 无合格附件不进待确认合约 + 按角色下载（越权 403）。
- 状态不可回退：状态机仅正向 + 「逆向 POST → 4xx，状态不变」+ 无拒绝/退回/超时态。
- §0.3 文件互斥 + §6 DAG 无环 + §1.4 `auth-model-change` → securityReviewer + `schema-migration` → migrationReviewer — 门禁方向正确。
- 全任务 `denyModify` `ai/runs/**/state.yaml`、`ai/runs/**/events.jsonl`；文首禁止伪造 REV/APPROVE — 控制面约束可接受。
- 「前端隐藏 ≠ 授权」+ 913/914/919 深链负例 — 防 UI 绕过。
- 审计四件套（create_time/create_by/update_time/update_by）+ del_flag — §3.2 强制，与 backend/CLAUDE.md 一致。
- §3.1 安全说明「订单 scope 仅信 SessionPrincipal；拒绝客户端 enterprise/user 作授权」— 方向正确（需补 acceptance 负例，见 ISSUE-002）。
- 本评审**不**等同批准 SNAP；**不**代批 productAnalyst / solutionArchitect / qaStrategist / parallelPlanner / planEditor / apiDataDesigner / uxUiPlanner / maintainer。
- 高风险接受未伪造；不代替人类 `securityOperationsOwner` 签署残余风险。

## 升级路径

- ISSUE-SEC-WSC9-R1-001..003 若被拒绝修补 → 升级 `securityOperationsOwner`；**不**因此否决 SNAP 业务目标本身。
- 若修补后仍拟在无订单号安全规则下发布 → 本角色下一轮可 `BLOCK` 并升级 Owner（本轮尚未构成 BLOCK）。

## 决策

**REQUEST_CHANGES** — 待 ISSUE-SEC-WSC9-R1-001（P1 订单号安全性）、ISSUE-SEC-WSC9-R1-002（P1 客户端 IDOR 负例）、ISSUE-SEC-WSC9-R1-003（P1 ADMIN 代操作审计）按 `closeWhen` 写入计划后，本角色可在后续 round 重评。ISSUE-SEC-WSC9-R1-004（P2 mock 上链隔离）建议同轮修补，不单独构成 BLOCK。禁止代批其他角色。
