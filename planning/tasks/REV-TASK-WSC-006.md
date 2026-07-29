# 代码审查

```yaml
reviewId: REV-CODE-TASK-WSC-006-R1
taskId: TASK-WSC-006
round: 1
decision: APPROVE
actorInstance: code-reviewer-wsc-006
planId: PLAN-WSC-1.1
contracts: wsc-contracts@1.1.0
reviewedAt: 2026-07-29T16:25:00+08:00
basedOn:
  - planning/tasks/TASK-WSC-006.md
  - planning/tasks/DEV-TASK-WSC-006.md
  - planning/approved/PLAN-WSC-1.1.md
  - design/decisions/DEC-WSC-002.md
```

## 结论

**APPROVE** — 本轮无 P0/P1。`SimulatedChainAttestationPort` 以 `@Component` 实现 `ChainAttestationPort` Spring Bean，输出 **metadataHash / ownerDID / timestamp / certificate.owner**（DEC-WSC-002）；无真实链 SDK。只读 versions/snapshot API 需登录；未知产品 200+空列表、未知 versionId 404；列表 `versionNo` 降序；UI 展示四字段+§3.6 截断复制+§3.5 空/错态。写集落在 `chain/**` + chain 路由 SCOPE_AMEND。正式 VERIFIED 仍须独立 tester。

## Findings

| id | severity | evidence | closeWhen | relatedReqs |
|---|---|---|---|---|
| FIND-WSC-006-R1-001 | P2 | 快照区 `snapshot-error` 仅展示文案，无显式「重试」按钮（列表区有重试）；§3.5 error 可观测但交互不对称 | 快照错误补重试或文档化「切换版本/刷新列表即重试」 | REQ-CHAIN-001, §3.5 |
| FIND-WSC-006-R1-002 | P2 | `ChainPage` 原生 HTML；未用 ant-design-vue | UI 统一或书面冻结 | REQ-CHAIN-001 |
| FIND-WSC-006-R1-003 | P3 | DEV 称 typecheck 失败点在并行 overview（writeSet 外）；本任务 vitest 已绿 | 全仓 typecheck 绿后再标发布 | 工程卫生 |
| FIND-WSC-006-R1-004 | P3 | 列表项 `certificate` 额外带 `timestamp`（OpenAPI 可选）；无害 | 保持或与契约示例对齐 | 契约 |

P0/P1 未清零不得 `APPROVE`。本轮 **P0=0，P1=0**。

## 检查清单（对照重点）

### 1. allowModify / denyModify 边界

| 项 | 结果 |
|---|---|
| 变更 ⊆ `frontend/src/features/chain/**`、`backend/**/chain/**`、测试 | PASS |
| 未改 contracts/api/migration/catalog/overview/rbac | PASS（`ChainAttestationPort` 接口签名未改） |
| `routes.ts` 仅 chain `component` → `ChainPage`（SCOPE_AMEND） | PASS（并行文件另含他任务接线，不归本越界证据） |

### 2. DEC-WSC-002 字段 + Port Bean

| 项 | 证据 | 结果 |
|---|---|---|
| 四字段齐全 | `SimulatedChainAttestationPort.attest` → hash/DID/Instant/`Certificate(owner)` | PASS |
| 可替换模拟、无真实链 SDK | SHA-256 本地哈希 + `did:wsc:sim:` | PASS |
| Spring Bean 暴露 | `@Component` + 集成测 `portIsExposedAsSpringBean` instanceof Simulated… | PASS |
| 单元断言四字段 | `SimulatedChainAttestationPortTest` | PASS |
| 列表/快照可读四字段 | `ChainController.toVersionView`；种子 snapshot.attestation | PASS |

### 3. 安全（读接口需登录）

| 项 | 证据 | 结果 |
|---|---|---|
| GET versions / snapshot `requireLogin` | `ChainController` | PASS |
| 未登录 401 | `listVersions_requiresLogin` | PASS |

### 4. 空态 / 契约行为

| 项 | 证据 | 结果 |
|---|---|---|
| 未知 productId → 200 + `items:[]` | `listVersions_unknownProduct_returnsEmptyList` | PASS |
| 未知 versionId → 404 | `getSnapshot_unknownVersion_returns404` | PASS |
| 最新优先 | versionNo 3→2→1 | PASS |
| UI 空态文案 | `chainEmptyMessage` + `list-empty` / `snapshot-empty` | PASS |
| §3.6 SensitiveId | metadataHash/ownerDID 截断+复制单测 | PASS |

### 5. 自测证据（审查侧抽样）

| 项 | 结果 |
|---|---|
| DEV：PortTest 3 + ChainApiIntegrationTest 6 = 9 PASS | 已声明 |
| DEV：vitest `src/features/chain` 5 PASS | 已声明 |

## 残余风险（交 tester）

- 005 注入 Port 的事务/失败回滚不在本任务范围，联测归 005。
- 种子页路径 `/chain/products/prod-demo-chain-001` 需登录后联调。
- 快照 error 无按钮重试，建议 TESTRUN 确认可切换版本恢复。

## 决策权声明

- 审查者未修改被审业务代码。
- 未兼任 developer；未代替 tester 宣称 testScope 通过。
- **decision: APPROVE**（进入独立 tester 门禁）。
