# 任务包 — TASK-WSC-916：状态机 / 权限 / 附件计费 / mock 上链补齐（订单 BE 写扩展）

<!-- R2 revised: ISSUE-SEC-003, ISSUE-SEC-002, ISSUE-QA-002, ISSUE-SEC-004 -->

```yaml
taskId: TASK-WSC-916
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: B
status: PENDING
requirements: [REQ-WSC-ORDER-010, REQ-WSC-ORDER-011, REQ-WSC-ORDER-012, REQ-WSC-ORDER-013, REQ-WSC-ORDER-014, REQ-WSC-ORDER-015, REQ-WSC-ORDER-016]
dependsOn: [TASK-WSC-913]
```

## 1. 任务目标

实现确认订单、提交附件+交易信息、统一确认合约、取消；不可回退；金额 CNY 两位小数双字段同价；附件白名单+扫描端口+单文件 20MB；每次状态变更新增时间线与 mock 上链；列表 chainCount 与详情条数一致；越权 403 且状态不变

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/controller/order/**` | 修改 | 订单写端点 |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/service/order/**` | 修改 | 状态机与业务逻辑 |
| 订单相关 `entity`/`repository`/`model`（附件、明细、事件、链日志） | 新增/修改 | 附件、计费明细、时间线、链日志实体 |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/common/port/**` | 新增 | **仅**新增 `OrderAttachmentScanPort` 及默认实现；**禁止**把产品目录 `attest` 改成真链 |
| `RbacMatrix.java`、`WriteAuthorizationInterceptor.java` | 修改 | 挂载 confirm/contract/cancel |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/order/**` | 新增/修改 | 订单写路径测试 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `contracts/**` OpenAPI 订单写 path | 只读，911 产出 |
| 913 订单服务/实体 | 只读基线 |
| `OrderChainAttestationPort` | 只读接口 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `**/sql/**` | Flyway 归 912 |
| `contracts/**` | 契约归 911 |
| `frontend/**` | 前端归 914/915/917/918 |
| `controller/catalog/**`、`service/catalog/**` | 目录归 8.3 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] 状态：确认订单 → 待上传合约 → 提交合格附件与计费 → 待确认合约 → 勾选须知确认 → 合约已达成；达成后无支付/交付 API
- [ ] 不可回退：逆向 POST → 4xx，状态不变
- [ ] PROVIDER 他企确认/取消 → 403；USER 确认订单/确认合约 → 403；ADMIN 可代确认/取消，**不能**代上传
- [ ] 确认订单/确认合约/取消请求携带篡改 `enterpriseId` 或 `userId` 参数 → 行为与不携带一致（仍按 SessionPrincipal 授权），越权 → 403
- [ ] 无附件/非 Word-PDF/>20MB/扫描失败 → 不能进入待确认合约
- [ ] 单价 ≤0 或缺单位/数量 → 不能提交；明细小计正确；含税=未税=同一金额两位小数
- [ ] 取消：三未完成态成功 → `CANCELLED`；达成/已取消再取消 4xx
- [ ] 创建+确认+提交+达成+取消路径均有时间线与 mock 记录；`chainCount` 一致
- [ ] mock 上链使用 `OrderChainAttestationPort`；mock 记录不含真实链节点信息
- [ ] 在线文本字段即使有值，无附件仍失败
- [ ] ADMIN 代操作审计：`update_by` 记录**代操作 ADMIN 的用户标识**（非被代操作方的需求方/提供方用户）；时间线事件 `operator` 字段记录 ADMIN 身份与角色；`create_by` = 创建会话 USER

## 6. 测试范围（testScope）

- 集成：主路径四步 + 取消三态参数化
- 集成：三角色动作正/负例表（§4）
- 集成：附件类型/大小/扫描失败
- 集成：金额舍入两位；两字段相等
- 集成：**每步状态变更后 `GET /orders/{id}` 的 `chainCount` = 时间线记录条数；列表 `chainCount` 与详情一致**
- 安全：确认/取消请求携带篡改 enterprise/user 参数 → 负例
- 安全：ADMIN 代 PROVIDER 确认后，`t_order_header.update_by` = ADMIN 用户名、时间线最新事件 operator = ADMIN
- 安全：mock 上链记录不含真实链节点信息
- 命名用例：`916-state-machine`、`916-admin-proxy-provider`、`916-cancel-three-states`、`916-chain-count-per-state`、`916-idor-negative-params`

## 7. 风险标签（riskTags）

- `auth-model-change`
- `attachment-scan`
- `state-machine`
