# DEV-TASK-WSC-006

```yaml
taskId: TASK-WSC-006
actorInstance: developer-wsc-006
status: READY_FOR_REVIEW
completedAt: 2026-07-29T16:03:00+08:00
reqs:
  - REQ-CHAIN-001
planId: PLAN-WSC-1.1
decisions:
  - DEC-WSC-002
```

## 摘要

实现可替换模拟存证适配 `SimulatedChainAttestationPort`（Spring Bean，供 005 注入）、内存种子上链版本/快照只读 API，以及 `features/chain` 上链信息页（左版本列表、右快照、存证+权属字段、§3.6 截断复制、§3.5 loading/empty/error）。

## 口径说明

| 项 | 口径 |
|---|---|
| 未知 / 无记录 `productId` | **HTTP 200 + `items: []`**（不 404） |
| 未知 `versionId` 快照 | **HTTP 404** + `code: "404"` |
| 鉴权 | 两个 GET 均需登录会话，否则 401 |
| 列表排序 | `versionNo` **降序**（最新优先） |
| 种子产品 | `prod-demo-chain-001`（3 个版本 + snapshot_json） |

## SCOPE 例外

| 项 | 说明 |
|---|---|
| `frontend/src/router/routes.ts` | **SCOPE_AMEND**：仅将 `name: 'chain'` 的 `component` 改为懒加载 `ChainPage.vue`；未改其他路由。 |
| `contracts/**` / `frontend/src/api/**` / migration / catalog / overview | **未修改**（只读消费 `api/chain.ts`）。 |
| `planning/**` | 仅本 DEV 报告。 |

## 文件列表

### 后端（新增/更新）

| 路径 | 说明 |
|---|---|
| `backend/.../chain/SimulatedChainAttestationPort.java` | 模拟适配；生成 metadataHash / ownerDID / timestamp / certificate.owner |
| `backend/.../chain/InMemoryChainStore.java` | 内存 seed + 按产品列表 / 按 versionId 读快照 |
| `backend/.../chain/ChainController.java` | GET versions / GET snapshot；需登录 |
| `backend/.../chain/package-info.java` | 包说明更新 |
| `backend/.../chain/ChainAttestationPort.java` | 既有接口，未改签名 |
| `backend/src/test/java/.../chain/SimulatedChainAttestationPortTest.java` | 适配单元 |
| `backend/src/test/java/.../chain/ChainApiIntegrationTest.java` | 列表倒序、快照、空态、401、Bean 暴露 |

### 前端（新增/更新）

| 路径 | 说明 |
|---|---|
| `frontend/src/features/chain/ChainPage.vue` | 上链页主视图 |
| `frontend/src/features/chain/components/SensitiveId.vue` | §3.6 截断 + 复制 |
| `frontend/src/features/chain/composables/useChainPage.ts` | 列表/快照状态机 |
| `frontend/src/features/chain/utils/truncateSensitive.ts` | 截断工具 |
| `frontend/src/features/chain/composables/useChainPage.spec.ts` | 空态/重试断言 |
| `frontend/src/features/chain/utils/truncateSensitive.spec.ts` | 截断断言 |
| `frontend/src/router/routes.ts` | SCOPE_AMEND：chain 组件挂载 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-CHAIN-001** | 按产品版本列表 + 按 versionId 完整快照；字段含存证上链与权属证书；模拟适配无真实链 SDK |
| **DEC-WSC-002** | `SimulatedChainAttestationPort` 输出四字段；`@Component` 实现 `ChainAttestationPort` Bean |

## 自测命令与结果

### 后端

```text
mvn "-Dtest=com.wsc.chain.SimulatedChainAttestationPortTest,com.wsc.chain.ChainApiIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `com.wsc.chain.SimulatedChainAttestationPortTest` | 3 | PASS |
| `com.wsc.chain.ChainApiIntegrationTest` | 6 | PASS |
| **合计** | **9** | **BUILD SUCCESS** |

覆盖：适配四字段；Port 为 Spring Bean；列表最新优先；按 versionId 读快照；未知产品空列表；未知版本 404；未登录 401。

### 前端

```text
pnpm exec vitest run src/features/chain
pnpm typecheck
```

| 命令 | 结果 |
|---|---|
| vitest `src/features/chain` | **5 passed**（截断 3 + 空态/error 2） |
| `pnpm typecheck` | **未因本任务失败**；当前失败点在并行任务 `features/overview/OverviewPage.vue`（本任务 writeSet 外，未改） |

## 联调提示

- 种子页：`/chain/products/prod-demo-chain-001`（需先登录）
- 空态页：`/chain/products/any-unknown-id`
- 前端默认相对路径 `/api/v1`；跨端口设 `VITE_API_BASE_URL=http://localhost:8080/api/v1`
