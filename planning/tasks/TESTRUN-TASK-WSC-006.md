# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-006-2
taskId: TASK-WSC-006
planId: PLAN-WSC-1.1
actorInstance: tester-wsc-006-reconfirm
contracts: wsc-contracts@1.1.0
supersedes: EVID-TASK-WSC-006-1
basedOn:
  - planning/tasks/TASK-WSC-006.md
  - planning/tasks/DEV-TASK-WSC-006.md
  - planning/tasks/REV-TASK-WSC-006.md
  - design/decisions/DEC-WSC-002.md
reviewDecision: APPROVE
executedAt: 2026-07-29T16:46:48+08:00
status: PASSED
```

## 总体结论

**PASSED** — `REV-TASK-WSC-006` 已 **APPROVE**；后端 chain 9 测 + 前端 5 测通过。首次因并行审查未落盘记 BLOCKED，现已解除。

## Layers

| name | command | result | notes |
|---|---|---|---|
| review-gate | 读 `planning/tasks/REV-TASK-WSC-006.md` | PASSED | decision: APPROVE；P0/P1=0 |
| backend-chain | `mvn -f backend/pom.xml "-Dtest=com.wsc.chain.SimulatedChainAttestationPortTest,com.wsc.chain.ChainApiIntegrationTest" test` | PASSED | Tests run: **9**（Port 3 + API 6）；Failures: 0；BUILD SUCCESS；exit 0 |
| frontend-chain | `pnpm --filter frontend exec vitest run src/features/chain` | PASSED | Test Files **2** passed；Tests **5** passed；exit 0 |
| static-four-fields | 读适配单测 + API 集成 + ChainPage | PASSED | metadataHash / ownerDID / timestamp / certificate.owner |
| static-truncate-copy | 读 `truncateSensitive` 测 + `SensitiveId.vue` | PASSED | 截断有测；复制按钮有代码/testid |

## 后端明细

| 类 | Tests | Failures | Errors | 结果 |
|---|---|---|---|---|
| `com.wsc.chain.SimulatedChainAttestationPortTest` | 3 | 0 | 0 | PASSED |
| `com.wsc.chain.ChainApiIntegrationTest` | 6 | 0 | 0 | PASSED |
| **合计** | **9** | 0 | 0 | PASSED |

四字段抽样：`attest_returnsRequiredFields`；列表/快照 API `jsonPath` 断言 `metadataHash` / `ownerDID` / `timestamp` / `certificate.owner`。

## 前端明细

| 规格 | Tests | 结果 |
|---|---|---|
| `src/features/chain/composables/useChainPage.spec.ts` | 2 | PASSED |
| `src/features/chain/utils/truncateSensitive.spec.ts` | 3 | PASSED |
| **合计** | **5** | PASSED |

空态文案 / 可重试；长 hash/DID 截断含省略号；短串与空串边界。

## 静态核对

### 1. 四字段

| 字段 | 证据 | 结果 |
|---|---|---|
| metadataHash | Port 单测 + API + `ChainPage` `SensitiveId` | PASSED |
| ownerDID | 同上 | PASSED |
| timestamp | Port 固定时钟 + Page 明文展示 | PASSED |
| certificate.owner | API `certificate.owner` + Page `certOwner` | PASSED |

### 2. 截断复制（§3.6）

| 项 | 证据 | 结果 |
|---|---|---|
| 截断 | `truncateSensitive.spec.ts` 断言长串含 `…` 且更短 | PASSED |
| 复制 | `SensitiveId.vue`：`navigator.clipboard.writeText(props.value)`；`data-testid="sensitive-copy"` | PASSED（代码+testid；无独立 clipboard mock 单测） |

## 环境快照

| 项 | 值 |
|---|---|
| Java | 17.0.11 |
| Vitest | v2.1.9 |
| cwd | `C:\WorkSpace\AI-SEP` |
| 日志 | `temp/mvn-wsc006-test.out`、`temp/vitest-wsc006.out` |

## 阻塞说明

- **blockedBy**: 缺少 `REV-TASK-WSC-006.md` 且 `decision: APPROVE`
- **解除条件**: 审查 APPROVE 后复确认命令层，方可改标 PASSED
- 命令全绿 **≠** PASSED

## 决策权声明

- tester 未修改业务源码；仅产出本证据文件。
- 未把未执行的 E2E 记为 PASSED。
