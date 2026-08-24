# 任务包 — TASK-WSC-912：订单表 Flyway + 产品简易流程标记（唯一 SQL）

```yaml
taskId: TASK-WSC-912
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: A
status: PENDING
requirements: [REQ-WSC-ORDER-006, REQ-WSC-ORDER-009, REQ-WSC-ORDER-013, REQ-WSC-ORDER-014, REQ-WSC-ORDER-016, REQ-WSC-ORDER-017, REQ-WSC-ORDER-003]
dependsOn: [TASK-WSC-911]
```

## 1. 任务目标

按 §3.2 创建 `t_order_*`（审计四件套 + `del_flag`，无跨表外键）；产品表增加 `allow_simple_order`；幂等可重入

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `backend/app/data-chain-service/src/main/resources/sql/migration/**` | 新增 | **独占** Flyway 迁移脚本 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md` | 需求权威快照 |
| `planning/proposals/PLAN-WSC-9.2.md`（§3.2） | 数据迁移协议 |
| 既有 `sql/migration/V1`..`V9` | 只读，对账最高版本 |
| `backend/CLAUDE.md` | 数据库规则 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `contracts/**` | 契约归 911 |
| `frontend/**` | 前端归 914/915/917/918 |
| `backend/**/java/**` | 实体映射归 913 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] 脚本幂等（`CREATE TABLE IF NOT EXISTS` / 列增量可重复执行策略文档化）
- [ ] 每张新业务表含 `create_time`/`create_by`/`update_time`/`update_by`/`del_flag`
- [ ] **无** `FOREIGN KEY` 子句
- [ ] `t_` 前缀、单数表名；金额两列（含税/未税）
- [ ] 产品 `allow_simple_order` 有默认值；既有行可链内订购（§1.2 假设）
- [ ] `t_order_header.order_no` 类型 `VARCHAR(36) NOT NULL`（UUID v4 存储）
- [ ] JPA `validate` 将在 913 实体对齐后通过；本任务交付迁移 smoke（空库/已有 V9 库）

## 6. 测试范围（testScope）

- 迁移 smoke：在 V9 之上执行 V10（或下一号）成功
- 检查清单：无 FK、四件套、`del_flag`、表名前缀、`order_no` VARCHAR(36)

## 7. 风险标签（riskTags）

- `schema-migration`
- `handles-pii`
