# Round 2 Review — Security / Operations（PLAN-WSC-9.2）

```yaml
reviewId: REV-PLAN-WSC9-R2-security-operations
planId: PLAN-WSC-9.2
round: 2
role: securityOperations
snapshotIdAtReview: SNAP-WSC-009
actorInstance: security-operations-wsc-012-r2
decision: APPROVE
summary: |
  逐条复评 R1 四条 ISSUE：ISSUE-SEC-001（P1 订单号安全性）已关闭——§1.2 指定 UUID v4、§3.2 明确字段类型与 service 层生成逻辑、913 acceptance/testScope 含格式负例与递增规律断言。
  ISSUE-SEC-002（P1 IDOR 负例）已关闭——§3.1 安全说明冻结 OpenAPI description、913 acceptance 覆盖 userId/demandUserId 参数负例、916 acceptance 覆盖确认/取消篡改 enterprise/user 参数负例。
  ISSUE-SEC-003（P1 ADMIN 代操作审计）已关闭——916 acceptance 明确 update_by 记录 ADMIN 用户标识、时间线 operator 记录 ADMIN 身份与角色、916 testScope 含集成断言、913 acceptance 确认 create_by = 创建会话 USER。
  ISSUE-SEC-004（P2 mock 上链隔离）已关闭——§1.2 硬性禁止读取真实链配置+安全默认值+模拟值约束、§3.2 数据迁移协议再次约束、913/916 acceptance 与 testScope 均含 mock 记录断言。
  R2 修订未引入新的安全风险。
```

## 评审基线

| 项 | 值 |
|---|---|
| 计划 | `planning/proposals/PLAN-WSC-9.2.md`（`CANDIDATE` / `DRAFT` / Round 2） |
| 快照 | `product/requirements/SNAP-WSC-009.md`（`status: APPROVED`；PO 2026-08-19） |
| 功能基线 | `SNAP-WSC-008` / `PLAN-WSC-8.3` / `wsc-contracts@2.3.2` |
| 前一轮 | `REV-PLAN-WSC9-R1-security-operations`（`decision: REQUEST_CHANGES`；4 ISSUE） |
| 本角色焦点 | 订单号生成安全性、客户端 IDOR 负例、ADMIN 代操作审计、mock 上链隔离、修订增量安全风险 |
| 本实例 | `security-operations-wsc-012-r2` |

## 复评方法

逐条对照 R1 `closeWhen` 条件，检查 PLAN-WSC-9.2 是否在修订章节/任务 acceptance/testScope 中完整吸收；同时审查 R2 修订增量是否引入新安全风险。

## ISSUE 逐条复评

### ISSUE-SEC-WSC9-R1-001（P1）：订单号生成规则 — **已关闭**

**R1 closeWhen：**
1. §1.2 假设或 §3.2 数据迁移协议增加订单号生成安全口径：不可预测（UUID v4）、不含可推断业务语义字段
2. 913 acceptance 增加订单号格式负例
3. 913 testScope 至少一条集成断言

**PLAN-WSC-9.2 吸收证据：**

| closeWhen 条件 | 修订位置 | 内容摘录 | 判定 |
|---|---|---|---|
| 不可预测 + 无业务语义 | §1.2 假设 | "**UUID v4**（不可预测、不含可推断业务语义的字段如企业 ID/产品编码/用户 ID）；不同订单的订单号无可观察递增规律" | **满足** |
| 字段类型 + service 层逻辑 | §3.2 数据迁移协议 | "`t_order_header.order_no` 字段类型 `VARCHAR(36) NOT NULL`，存储 UUID v4 格式；生成逻辑在 service 层，**不含**企业 ID/产品编码/用户 ID 等可推断业务语义的字段" | **满足** |
| 913 acceptance 负例 | 913 acceptance | "成功：订单号（**UUID v4 格式**，不含企业 ID/产品编码/用户 ID 子串）" | **满足** |
| 913 testScope 断言 | 913 testScope | "安全：订单号格式验证（UUID v4 正则，不含 productId/enterpriseId 子串，不同订单无可观察递增规律）" | **满足** |
| 命名用例可追溯 | 913 testScope | `913-order-id-uuid` | **满足** |
| 风险表记录 | §8 风险与回滚 | "订单号可预测/信息泄露 → §1.2 指定 UUID v4；913 负例断言 → 热修生成逻辑" | **满足** |

**结论：closeWhen 三项全部满足，ISSUE 关闭。**

---

### ISSUE-SEC-WSC9-R1-002（P1）：IDOR 负例覆盖 — **已关闭**

**R1 closeWhen：**
1. 913 acceptance 扩展负例：客户端传 userId/demandUserId 参数不扩大可见面
2. 916 acceptance 增加：确认/取消请求携带篡改 enterpriseId 或 userId 参数 → 行为一致，越权 → 403
3. §3.1 OpenAPI 订单 path description 或 security note 增加安全说明

**PLAN-WSC-9.2 吸收证据：**

| closeWhen 条件 | 修订位置 | 内容摘录 | 判定 |
|---|---|---|---|
| §3.1 安全说明 | §3.1 安全说明行 | "订单 scope **仅**信 SessionPrincipal；拒绝客户端 enterprise/user 作授权；授权范围**仅**从服务端会话推导，客户端 `enterpriseId`/`userId`/`demandUserId` 参数**不**作为授权依据（OpenAPI description 写死）" | **满足** |
| §3.1 GET /orders 表格 | §3.1 契约表 | "范围**仅**信 SessionPrincipal：**禁止**客户端传 `enterpriseId`/`userId` 扩权" | **满足** |
| 913 acceptance | 913 acceptance | "篡改 query `enterpriseId`/`userId`/`demandUserId` **不扩大**可见面" | **满足** |
| 913 testScope | 913 testScope | "安全：IDOR 他单 403；enterprise 参数负例；**`userId`/`demandUserId` 参数负例**（传参不扩大可见面）" | **满足** |
| 913 命名用例 | 913 testScope | `913-idor-negative-params` | **满足** |
| 916 acceptance | 916 acceptance | "确认订单/确认合约/取消请求携带篡改 `enterpriseId` 或 `userId` 参数 → 行为与不携带一致（仍按 SessionPrincipal 授权），越权 → 403" | **满足** |
| 916 testScope | 916 testScope | "安全：确认/取消请求携带篡改 enterprise/user 参数 → 负例" | **满足** |
| 916 命名用例 | 916 testScope | `916-idor-negative-params` | **满足** |

**结论：closeWhen 三项全部满足，覆盖面超出 R1 要求（913/916 双重覆盖 + OpenAPI 冻结），ISSUE 关闭。**

---

### ISSUE-SEC-WSC9-R1-003（P1）：ADMIN 代操作审计 — **已关闭**

**R1 closeWhen：**
1. 916 acceptance：update_by 记录 ADMIN 用户标识（非被代操作方）；时间线 operator 记录 ADMIN 身份与角色
2. 916 testScope：ADMIN 代 PROVIDER 确认后，t_order_header.update_by = ADMIN 用户名、时间线最新事件 operator = ADMIN
3. 913 acceptance：create_by = 创建会话 USER

**PLAN-WSC-9.2 吸收证据：**

| closeWhen 条件 | 修订位置 | 内容摘录 | 判定 |
|---|---|---|---|
| 916 acceptance 审计条款 | 916 acceptance | "ADMIN 代操作审计：`update_by` 记录**代操作 ADMIN 的用户标识**（非被代操作方的需求方/提供方用户）；时间线事件 `operator` 字段记录 ADMIN 身份与角色；`create_by` = 创建会话 USER" | **满足** |
| 916 testScope 集成断言 | 916 testScope | "安全：ADMIN 代 PROVIDER 确认后，`t_order_header.update_by` = ADMIN 用户名、时间线最新事件 operator = ADMIN" | **满足** |
| 916 命名用例 | 916 testScope | `916-admin-proxy-provider` | **满足** |
| 913 acceptance create_by | 913 acceptance | "`create_by` = 创建会话 USER 用户标识" | **满足** |

**结论：closeWhen 三项全部满足，ADMIN 代操作审计可追溯，ISSUE 关闭。**

---

### ISSUE-SEC-WSC9-R1-004（P2）：mock 上链隔离 — **已关闭**

**R1 closeWhen：**
1. §1.2 或 §3.2：mock 上链实现禁止读取真实链节点配置（URL/私钥/证书）；mock 模式下链配置项应有安全默认值
2. 916 testScope 或 913 testScope：mock 上链记录的哈希/高度/节点字段为模拟值，不匹配真实链浏览器可验证格式

**PLAN-WSC-9.2 吸收证据：**

| closeWhen 条件 | 修订位置 | 内容摘录 | 判定 |
|---|---|---|---|
| §1.2 硬性禁止 + 安全默认值 + 模拟值 | §1.2 假设 | "mock 上链实现**禁止**读取/暴露真实链节点配置（URL/私钥/证书）；mock 模式下链相关配置项应有安全默认值（如空字符串或 localhost 占位）；mock 记录的哈希/高度/节点字段为模拟值，不匹配真实链浏览器可验证格式" | **满足** |
| §3.2 数据迁移协议约束 | §3.2 | "mock 实现**禁止**写入真实链节点 URL/私钥/证书" | **满足** |
| 913 acceptance | 913 acceptance | "mock 上链：使用 `OrderChainAttestationPort`；mock 记录哈希/高度/节点为模拟值；mock 实现**禁止**读取真实链节点配置（URL/私钥/证书）；配置项在 mock 模式下应有安全默认值" | **满足** |
| 913 testScope | 913 testScope | "安全：mock 上链记录不含真实链节点信息" | **满足** |
| 916 acceptance | 916 acceptance | "mock 上链使用 `OrderChainAttestationPort`；mock 记录不含真实链节点信息" | **满足** |
| 916 testScope | 916 testScope | "安全：mock 上链记录不含真实链节点信息" | **满足** |
| §8 风险表 | §8 | "mock 上链暴露真实链配置 → §1.2 硬性禁止；mock 适配器安全默认值 → 移除真实链配置引用" | **满足** |

**结论：closeWhen 两项全部满足，且约束出现在 §1.2、§3.2、913、916 多处（超额覆盖），ISSUE 关闭。**

---

## R2 修订增量安全审查

| 修订项 | 安全影响 | 判定 |
|---|---|---|
| ChainAttestationPort 并列方案（ISSUE-API-001） | 新增 `OrderChainAttestationPort` 与现有端口共存；mock 约束已写入；隔离正面（产品目录与订单存证分离） | **无新风险** |
| multipart 字段冻结（ISSUE-API-002） | 硬冻结 part 名 `file` + `transactionInfo`；`orderLines` 仅 contract 阶段 | **无新风险** — 减少实现歧义 |
| 列表九组字段 + envelope（ISSUE-API-003） | 响应枚举明确 | **无新风险** |
| confirm 无 body（ISSUE-API-004） | 减少攻击面（无 request body = 无可注入参数） | **正面** |
| 须知含版本（ISSUE-API-005） | `notice_version` 服务端自动填充，前端无需回传 | **正面** — 防客户端篡改版本 |
| contractsTarget 2.3.3 说明（ISSUE-API-006） | 版本管理澄清 | **无安全影响** |
| UX 确认页布局/附件交互/取消对话框（ISSUE-UX-001~003） | UX 冻结；未勾选 disabled；danger 样式；ADMIN 代操作身份可见 | **正面** — 减少误操作 |
| 数字合约版本 E2E + UI（ISSUE-QA-001） | 增强可观察性 | **无新风险** |
| chainCount 逐态（ISSUE-QA-002） | 增强审计一致性 | **正面** |
| 外部跳转正向断言（ISSUE-QA-003） | 明确「未配置」文案 testid 可断言 | **无新风险** |

**修订增量未引入新的安全风险。**

## 非阻塞备注（记录，不构成 REQUEST_CHANGES）

| 项 | 说明 |
|---|---|
| R1 非阻塞项继承 | R1 列出的 6 项非阻塞备注（附件 MIME 双重校验、扫描失败提示泛化、脱敏假设、前端 OWASP、V1.6 回归、Flyway 安全）在 R2 中维持原判断，均为方向正确或 non-blocking |
| 附件 MIME + 扩展名白名单 | R2 未进一步细化 MIME 与扩展名双重校验，但 §3.1 已硬冻结附件类型约束（Word/PDF），实现时建议 service 层同时校验 |
| 扫描失败用户提示 | 916 acceptance 仅写「扫描失败」，建议实现时返回通用错误消息 |

## 无异议项（记录）

- §3.1 RBAC 矩阵逐 cell 与 SNAP-WSC-009 角色表一致；V1.6 单元格不回退 — **通过**。
- ADMIN 代操作范围：可代确认订单/确认合约/取消；**不可**代上传合约 — 与 SNAP 一致。
- PROVIDER 企业隔离：916 acceptance「PROVIDER 他企确认/取消 → 403」— **通过**。
- USER 数据范围：913 acceptance USER 不见他人需求方单 — **通过**。
- 附件安全链：白名单 + 20MB + `OrderAttachmentScanPort` + 按角色下载 — **通过**。
- 状态不可回退：916 acceptance「逆向 POST → 4xx，状态不变」— **通过**。
- `OrderChainAttestationPort` 并列方案：隔离正面；mock 适配器返回模拟值；不以真链成败为门禁 — **通过**。
- 审计四件套 + del_flag — §3.2 强制 — **通过**。
- 「前端隐藏 ≠ 授权」+ 深链负例 — **通过**。
- 本评审**不**等同批准 SNAP；**不**代批其他角色。
- 高风险接受未伪造；不代替人类 `securityOperationsOwner` 签署残余风险。

## 决策

**APPROVE** — ISSUE-SEC-WSC9-R1-001（P1 订单号安全性）、ISSUE-SEC-WSC9-R1-002（P1 客户端 IDOR 负例）、ISSUE-SEC-WSC9-R1-003（P1 ADMIN 代操作审计）、ISSUE-SEC-WSC9-R1-004（P2 mock 上链隔离）均按 `closeWhen` 完整吸收。R2 修订增量未引入新安全风险。禁止代批其他角色。
