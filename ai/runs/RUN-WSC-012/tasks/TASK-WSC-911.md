# 任务包 — TASK-WSC-911：契约 V1.7 订单增量 + client 生成（唯一 bump/生成）

```yaml
taskId: TASK-WSC-911
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: A
status: PENDING
requirements: [REQ-WSC-ORDER-001, REQ-WSC-ORDER-003, REQ-WSC-ORDER-006, REQ-WSC-ORDER-007, REQ-WSC-ORDER-010, REQ-WSC-ORDER-011, REQ-WSC-ORDER-012, REQ-WSC-ORDER-013, REQ-WSC-ORDER-014, REQ-WSC-ORDER-015, REQ-WSC-ORDER-016, REQ-WSC-ORDER-017, REQ-WSC-ORDER-005, REQ-SHELL-011, REQ-API-001]
dependsOn: []
```

## 1. 任务目标

按 §3.1 **一次性冻结**订单读/建/**全部写状态与附件/确认/取消** 的 OpenAPI 与矩阵，避免 Wave B/C 再改 VERSION；生成 `frontend/src/api/**`；**本计划唯一**契约任务

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `contracts/**`（含 `VERSION`、`openapi/**`、`rbac/matrix.yaml`、`ui/state-matrix.md`、`req-coverage.md`、`errors/codes.yaml`、`chain/ChainAttestationPort.md`、`chain/OrderChainAttestationPort.md`、`security/sensitive-fields.md` 若需订单补充） | 修改 | 契约 doc 全量增量 |
| `frontend/src/api/**` | 生成 | 由契约生成的前端 API client |
| `frontend/package.json`、`frontend/pnpm-lock.yaml` | 条件修改 | **仅当** client 生成必需 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md` | 需求权威快照 |
| `product/prd/wsc-v1.7-transaction-orders.md` | PRD 叙述对照 |
| `planning/proposals/PLAN-WSC-9.2.md`（§3 / §4） | 本计划契约冻结内容与 RBAC 矩阵 |
| 既有 `contracts/**`（含 VERSION 2.3.3、V1.6 matrix） | 对账基线 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `frontend/src/features/**` | 页实现归 914/915/917/918 |
| `frontend/src/layouts/**` | 壳层归 914 |
| `frontend/src/router/**` | 路由归 914/915 |
| `backend/**` | 后端归 912/913/916 |
| `**/sql/**` | Flyway 归 912 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] **版本四头同步**：`contracts/VERSION`、`rbac/matrix.yaml` version、`openapi.yaml` `info.version`、`ui/state-matrix.md` 版本头为**同一 semver**（默认保持 2.3.3）
- [ ] OpenAPI 含 §3.1 全部订单 path 与状态枚举、金额双字段、须知（含版本）、附件下载、安全说明（scope 仅信会话；客户端 enterprise/user 参数不作授权依据）
- [ ] `matrix.yaml` 与 §3.1 **逐 cell 一致**（含 V1.6 单元格**零回退**）
- [ ] `state-matrix.md`：订单菜单角色化文案；数据登记/连接器仍可未开放；V1.6 目录 IA 不回退
- [ ] `ChainAttestationPort.md` 更新（现有签名不变）+ 新增 `OrderChainAttestationPort.md`（§3.1 冻结的并列接口 + payload 字段枚举）
- [ ] `frontend/src/api/**` 生成后可 typecheck
- [ ] req-coverage 含 SNAP-009 21 条增量映射

## 6. 测试范围（testScope）

- 契约 lint / OpenAPI 校验
- matrix 与 §3.1 交叉检查清单（订单键 + V1.6 回归抽样）
- client typecheck
- **对账清单**：禁止无故升到 2.3.4；若必须对齐 bump，在本任务说明原因且仅一次

## 7. 风险标签（riskTags）

- `contracts-bump`
- `rbac-breaking`
- `order-scope`
- `auth-model-change`
