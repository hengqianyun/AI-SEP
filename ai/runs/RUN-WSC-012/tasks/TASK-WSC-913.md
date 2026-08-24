# 任务包 — TASK-WSC-913：订单创建 / 列表 / 详情只读 API + 创建时时间线与 mock 上链

<!-- R2 revised: ISSUE-SEC-001, ISSUE-SEC-002, ISSUE-SEC-004 -->

```yaml
taskId: TASK-WSC-913
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: A
status: PENDING
requirements: [REQ-WSC-ORDER-001, REQ-WSC-ORDER-003, REQ-WSC-ORDER-004, REQ-WSC-ORDER-006, REQ-WSC-ORDER-007, REQ-WSC-ORDER-008, REQ-WSC-ORDER-009, REQ-WSC-ORDER-016, REQ-WSC-ORDER-FE-003]
dependsOn: [TASK-WSC-911, TASK-WSC-912]
```

## 1. 任务目标

实现 GET/POST 列表与创建、GET 详情；角色 scope；产品快照冻结；须知校验；创建写时间线 + mock 上链；分页 page/pageSize；**不实现**确认/上传/取消写（归 916，但 911 已冻结形状）

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/controller/order/**` | 新增 | 订单 Controller |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/service/order/**` | 新增 | 订单 Service |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/entity/**` | 新增 | **仅**订单相关实体及产品实体 `allowSimpleOrder` 字段映射 |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/repository/**` | 新增 | **仅**订单相关 Repository |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/model/**` | 新增 | **仅**订单 DTO/枚举；deny 改 CatalogProduct 除必要时的只读扩展 record **优先**新建 `OrderProductSnapshot` 而不改浏览 record |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/common/security/RbacMatrix.java` | 修改 | **仅增**订单方法，不改 V1.6 方法语义 |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/common/security/WriteAuthorizationInterceptor.java` | 修改 | 挂载订单写路径：本任务仅 `POST /orders` |
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/common/port/OrderChainAttestationPort.java` | 新增 | 接口定义 + `SimulatedOrderChainAttestationAdapter` mock 实现 |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/order/**` | 新增 | 订单相关测试 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `frontend/src/api/**`、`contracts/**` | 只读，911 生成物 |
| 既有 `SessionPrincipal`、`RbacMatrix`、`WriteAuthorizationInterceptor` | 安全框架 |
| `OrderChainAttestationPort` | 911 新增，只读接口定义 |
| 产品实体/仓储 | 只读，取快照与 `allow_simple_order`、create_by→enterprise |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `**/sql/**` | Flyway 归 912 |
| `contracts/**` | 契约归 911 |
| `frontend/**` | 前端归 914/915/917/918 |
| `controller/catalog/**`、`service/catalog/**` | 除只读调用，目录归 8.3 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] USER 创建：备注空/未勾选须知/产品不允许简易流程 → 失败且无订单行
- [ ] 成功：订单号（**UUID v4 格式**，不含企业 ID/产品编码/用户 ID 子串）、`PENDING_CONFIRM`、详情产品为快照 JSON、时间线创建记录、mock 上链至少 1 条；`chainCount=1`
- [ ] 列表：USER 不见他人需求方单；PROVIDER 不见他企提供方单；ADMIN 全可见；keyword（订单号/产品名/编码/需求方姓名/单位）与状态筛选生效；空列表语义由 FE 展示「暂无订单数据」，API total=0
- [ ] 列表响应字段与分页 envelope 与 §3.1 `GET /orders` 200 响应定义一致
- [ ] 分页：`pageSize` 仅允许 10/20/50/100
- [ ] 篡改 query `enterpriseId`/`userId`/`demandUserId` **不扩大**可见面
- [ ] ADMIN/PROVIDER `POST /orders` → 403
- [ ] `create_by` = 创建会话 USER 用户标识
- [ ] mock 上链：使用 `OrderChainAttestationPort`；mock 记录哈希/高度/节点为模拟值；mock 实现**禁止**读取真实链节点配置（URL/私钥/证书）；配置项在 mock 模式下应有安全默认值
- [ ] 创建路径 mock 失败**不得**阻断建单（PO：mock；实现假设：捕获适配异常仍写本地 mock 行或降级占位记录，状态仍成功）——若端口抛错，订单与时间线仍提交，上链区可标记 mocked/degraded，**不以真链成败为门禁**

## 6. 测试范围（testScope）

- 集成：三角色列表正/负例；创建校验；快照不随产品改名漂移（改产品后详情仍旧名）
- 集成：分页非法 pageSize 4xx
- 安全：IDOR 他单 403；enterprise 参数负例；**`userId`/`demandUserId` 参数负例**（传参不扩大可见面）
- 安全：订单号格式验证（UUID v4 正则，不含 productId/enterpriseId 子串，不同订单无可观察递增规律）
- 安全：mock 上链记录不含真实链节点信息
- 命名用例：`913-create-snapshot-and-mock`、`913-role-list-scope`、`913-order-id-uuid`、`913-idor-negative-params`

## 7. 风险标签（riskTags）

- `auth-model-change`
- `data-isolation`
- `order-create`
