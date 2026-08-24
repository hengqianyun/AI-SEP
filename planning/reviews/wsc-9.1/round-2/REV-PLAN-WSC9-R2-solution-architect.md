# Round 2 Review — Solution Architect

```yaml
reviewId: REV-PLAN-WSC9-R2-solution-architect
planId: PLAN-WSC-9.2
round: 2
role: solutionArchitect
snapshotIdAtReview: SNAP-WSC-009
actorInstance: solution-architect-wsc-012-r2
decision: APPROVE
summary: |
  对照 PLAN-WSC-9.2 Round 2 修订稿与 R1 评审（APPROVE + 4 条非阻塞备註）：
  R1 四项架构决策均未削弱——911 独占契约增量（含新增 OrderChainAttestationPort.md）、
  912 独占 Flyway 迁移、后端包路径按类型分层无业务域顶层包、写集互斥经 DAG 无环验证。
  R2 吸收 ISSUE-API-001 将 ChainAttestationPort 冻结为并列方案 B（OrderChainAttestationPort），
  接口设计合理（orderId/status/actorRole/actorUserId/snapshotJson），与现有端口签名不冲突；
  ISSUE-SEC-001 指定 UUID v4 订单号存储于 VARCHAR(36)，id 仍为 BIGINT AUTO_INCREMENT 主键，
  不引入 B-tree 碎片或索引性能问题；Flyway 表设计（t_order_*）维持审计四件套 + del_flag + 无 FK；
  DAG 与写集互斥未因修订而改变。R1 四条非阻塞备註中两条已实质改善，两条保留（P3）。
```

## 评审范围与方法

本次 R2 复评聚焦 R1 APPROVE 后 PLAN-WSC-9.1 → PLAN-WSC-9.2 的修订增量，逐一核对以下维度：

| 维度 | 方法 | 结论 |
|---|---|---|
| R1 架构决策是否削弱 | 逐项对照 R1 七条通过结论 | **通过** — 四项核心决策（包结构、契约唯一写、Flyway 唯一写、写集互斥）均未改变 |
| 911/912 独占写集隔离 | writeSet/denyModify 字面对照 | **通过** — 911 独占 `contracts/**` + `frontend/src/api/**`；912 独占 `**/sql/**`；互斥声明未变 |
| OrderChainAttestationPort（ISSUE-API-001） | §3.1 接口定义审阅 | **通过** — 并列方案 B 设计合理，详见下方 |
| Flyway 表设计 | §3.2 vs CLAUDE.md 数据库规则 | **通过** — 审计四件套 + del_flag + 无 FK + t_ 前缀，详见下方 |
| UUID v4 订单号（ISSUE-SEC-001） | §1.2 + §3.2 + 912/913 acceptance | **通过** — 不引入性能或索引问题，详见下方 |
| DAG/写集互斥 | §6 依赖图 + §0.3 文件互斥 | **通过** — 与 R1 完全一致，无修订 |

## 架构抽查（证据）

### 1. R1 架构决策未被削弱

逐项对照 R1 评审结论：

| R1 通过项 | R2 状态 | 证据 |
|---|---|---|
| 后端包结构按类型分层 | **未变** | 913/916 writeSet 仍为 `controller/order/**`、`service/order/**`、`entity/**`、`repository/**`、`model/**`、`common/port/**`；§0.2 包结构约束原文保留 |
| 契约唯一写（911） | **增强** | 911 新增 `OrderChainAttestationPort.md` 输出；§3.1 冻结内容新增 multipart 字段名/schema、列表九组字段枚举、confirm 无 body、须知版本等（吸收 ISSUE-API-001..006） |
| Flyway 唯一写（912） | **未变** | 912 writeSet 仍仅 `sql/migration/**`；denyModify 禁止 `backend/**/java/**` |
| 写集互斥 + DAG | **未变** | §6 DAG 边集与 R1 完全一致；§0.3 文件互斥表无新增冲突路径 |
| 微服务边界 | **未变** | 订单/目录 controller/service deny 互斥保留 |
| mock 上链隔离 | **增强** | §1.2 新增 ISSUE-SEC-004 硬性条款："mock 实现禁止读取/暴露真实链节点配置"；913/916 acceptance 增补 mock 安全默认值要求 |
| 预落地对账 | **增强** | §0.2 补充 ISSUE-API-006 说明（contractsTarget 2.3.2 为语义基线，工作树 2.3.3，911 唯一管理增量） |

### 2. 911/912 独占写集隔离

- **911**：writeSet = `contracts/**` + `frontend/src/api/**`（含新增 `chain/OrderChainAttestationPort.md`）。denyModify 禁止 `backend/**`、`frontend/src/features/**`、`**/sql/**`。
- **912**：writeSet = `backend/.../sql/migration/**`。denyModify 禁止 `contracts/**`、`frontend/**`、`backend/**/java/**`。
- **交集**：无。911 不碰 SQL，912 不碰契约/frontend。
- **913/916**：新增 `OrderChainAttestationPort.java` 接口定义 + mock 适配器（`common/port/**`），但仅消费 911 已冻结的接口签名，不改契约文件。

### 3. OrderChainAttestationPort 接口设计（ISSUE-API-001 吸收）

§3.1 冻结的并列方案 B：

- **接口**：`OrderChainAttestationPort.attestOrder(OrderAttestationRequest)`
- **请求字段**：`orderId`（string）、`status`（string，状态枚举值）、`actorRole`（string，ADMIN/PROVIDER/USER）、`actorUserId`（string）、`snapshotJson`（string，订单快照摘要）
- **与现有端口关系**：并列共存。现有 `ChainAttestationPort` 面向产品目录快照，签名不变；`OrderChainAttestationPort` 面向订单状态变更事件。
- **注入**：913/916 注入 `OrderChainAttestationPort`；mock 适配器返回模拟哈希/高度/时间。

**评估**：接口设计合理。`snapshotJson` 作为 string 字段避免了订单领域模型与链端口的强耦合；`actorRole` + `actorUserId` 满足 ISSUE-SEC-003 ADMIN 代操作审计需求（时间线可从此字段提取操作人身份）。并列方案不破坏现有产品目录上链路径，符合开闭原则。

### 4. Flyway 表设计（§3.2）

- **表名**：`t_order_header`、`t_order_line`、`t_order_event`、`t_order_chain_log`、`t_order_attachment`、`t_order_contract_version` — 全部 `t_` 前缀 + 单数。
- **审计四件套**：每张表含 `create_time`/`create_by`/`update_time`/`update_by`。
- **逻辑删除**：`del_flag TINYINT NOT NULL DEFAULT 0`。
- **无 FK**：关联用业务列（订单号、用户 ID 字符串、enterprise_id 字符串）。
- **订单号**：`t_order_header.order_no VARCHAR(36) NOT NULL`，存储 UUID v4。
- **产品增量**：`allow_simple_order TINYINT NOT NULL DEFAULT 1`。

**与 CLAUDE.md 一致性**：全部符合。幂等策略（`CREATE TABLE IF NOT EXISTS`）、字符集（utf8mb4）、排序（utf8mb4_unicode_ci）、引擎（InnoDB）在 912 acceptance 或 migrationReviewer 门禁中保障。

### 5. UUID v4 订单号性能与索引分析（ISSUE-SEC-001）

- **主键**：`id BIGINT UNSIGNED AUTO_INCREMENT` — 自增主键，B-tree 友好，无碎片。
- **订单号**：`order_no VARCHAR(36) NOT NULL` — 非主键，作为业务标识列。
- **查询场景**：列表 API 按 `keyword` 模糊匹配订单号（`GET /orders?keyword=...`）；详情 API 按 `orderId` 精确查询。
- **索引建议**：`order_no` 应建唯一索引 `uk_t_order_header_order_no`（精确查询 + 唯一性约束）；模糊查询走 LIKE 前缀或全文，UUID v4 无序性不影响非主键列的索引效率。
- **结论**：UUID v4 存储于 VARCHAR(36) 不引入 B-tree 碎片（因非主键）；id 自增主键保障插入性能；order_no 唯一索引保障查询效率。无阻断级性能风险。

### 6. DAG/写集互斥（R2 未修订）

§6 DAG 边集与 R1 完全一致，无新增边或删除边。§0.3 文件互斥表无新增共享制品或写任务。允许的并行对（912∥914、913∥914）路径仍不相交；禁止的并行对（913∥916、914∥915、915∥917、917∥918）仍有串行 dependsOn 保障。

## R1 非阻塞备註跟踪

| R1 id | R1 说明 | R2 状态 | 说明 |
|---|---|---|---|
| NOTE-SA-R1-001 | 913/916 `model/**` glob 过宽，可能覆盖目录 DTO | **保留（P3）** | R2 未修订 writeSet glob 范围。串行执行（913→916）下实际风险低，但 Orchestrator 字面 scope-check 时 `model/**` 仍可能误判。建议后续修订或实现时收紧为显式订单 model 列表。 |
| NOTE-SA-R1-002 | contractsTarget 元数据 2.3.2 vs 工作树 2.3.3 不一致 | **已改善** | R2 在 §0.1 补充说明："工作树当前为 2.3.3，由 911 唯一管理增量"；修订说明表 ISSUE-API-006 明确元数据保留 2.3.2 为语义基线。表面不一致已消除。 |
| NOTE-SA-R1-003 | §3.2 未显式列出索引策略 | **保留（P3）** | R2 未在 §3.2 或 912 acceptance 增补索引清单。912 migrationReviewer 可在实现时补齐（建议至少 `uk_t_order_header_order_no`、`idx_t_order_header_demand_user`、`idx_t_order_header_provider_enterprise`、`idx_t_order_header_status`）。 |
| NOTE-SA-R1-004 | 913/916/918 共享测试路径可能混淆 | **保留（P3）** | R2 未修订测试路径细分。串行模式（913→916→918）已提供时序保障，实现时 codeReviewer 可要求后续任务不删除前序测试。 |

## 异议

**无阻断级异议。** R2 修订稿吸收了 R1 全部 16 条 ISSUE 的修订意图，架构维度无新增风险。

## 非阻塞备註

| id | severity | 说明 | 建议 |
|---|---|---|---|
| NOTE-SA-R2-001 | P3 | 承接 R1 NOTE-SA-R1-001：913/916 `model/**` glob 仍覆盖全部 model 目录。R2 新增了 `OrderChainAttestationPort` 相关 model（如 `OrderAttestationRequest`），但 glob 未收紧。 | 913/916 实现时将 model writeSet 收紧为显式文件列表（`OrderCreateRequest`、`OrderListResponse`、`OrderDetailResponse`、`OrderStatus`、`OrderAttestationRequest` 等），或在 denyModify 增 `model/catalog/**`。非阻塞。 |
| NOTE-SA-R2-002 | P3 | 承接 R1 NOTE-SA-R1-003：§3.2 建议 6 张表未列出索引策略。R2 新增 `order_no VARCHAR(36)` UUID 存储，唯一索引更显必要。 | 912 migrationReviewer 门禁中补充索引检查：`uk_t_order_header_order_no`（唯一）、`idx_t_order_header_demand_user`、`idx_t_order_header_provider_enterprise`、`idx_t_order_header_status`。非阻塞。 |

## 决策

`APPROVE` — 从解决方案架构维度，PLAN-WSC-9.2 Round 2 修订稿在 R1 基础上：

1. **架构决策未削弱**：R1 七条通过结论（包结构、契约唯一写、Flyway 唯一写、写集互斥、微服务边界、mock 上链隔离、预落地对账）全部保留或增强。
2. **OrderChainAttestationPort 设计合理**（ISSUE-API-001）：并列方案 B 不破坏现有产品目录上链路径；接口字段满足订单状态变更事件与 ADMIN 代操作审计需求。
3. **UUID v4 订单号无性能风险**（ISSUE-SEC-001）：`order_no` 非主键，id 仍为 BIGINT AUTO_INCREMENT；order_no 唯一索引保障查询效率。
4. **Flyway 表设计合规**：审计四件套 + del_flag + 无 FK + t_ 前缀，与 CLAUDE.md 一致。
5. **DAG/写集互斥未改变**：依赖图与文件互斥表与 R1 完全一致。

2 条非阻塞备註（NOTE-SA-R2-001..002）均为 P3 级实现细节建议，不构成阻断条件。

本文件仅代表 `solutionArchitect` / `solution-architect-wsc-012-r2`；**不**代写其他委员会角色结论，**不**伪造他角色 APPROVE；**不**修改 plan / state / events。
