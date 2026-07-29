# DEV-TASK-WSC-003

```yaml
taskId: TASK-WSC-003
actorInstance: developer-wsc-003
status: READY_FOR_REVIEW
completedAt: 2026-07-29T16:12:00+08:00
reqs:
  - REQ-OVW-001
  - REQ-OVW-002
  - REQ-OVW-003
  - REQ-OVW-004
  - REQ-OVW-005
```

## 摘要

实现总览只读 API（metrics / trend / top / stream / distribution）与 `OverviewPage` UI：内存 seed/fixture 供给三类动态流事件，空库返回 0/空数组友好态；前端 §3.5 loading/empty/error（可重试）、中文桌面布局。未改 `routes.ts`（由编排器接线）；未改 contracts / api / migration / 其他 feature。

## SCOPE

| 项 | 说明 |
|---|---|
| `frontend/src/router/routes.ts` | **未修改**（用户硬边界；编排器稍后接线） |
| `contracts/**` / `frontend/src/api/**` / migration | **未修改**；只读消费 `overview.ts` 与 OpenAPI |
| `security/**` / `rbac/**` / 其他 feature | **未修改**；会话校验复用 `AuthController.currentPrincipal` + `SessionKeys` |
| `planning/**` | 仅本 DEV 报告 |

## 文件列表

### 后端 `com.wsc.overview`

| 路径 | REQ | 说明 |
|---|---|---|
| `OverviewController.java` | OVW-001..005 | `/api/v1/overview/*`；需登录会话 |
| `OverviewService.java` | OVW-001..005 | 投影组装；流按 `occurredAt` 倒序；相对时间文案 |
| `OverviewDataStore.java` | OVW-004 | 内存 seed；`clear`/`resetToSeed`/`fixtureThreeStreamTypes` |
| `OverviewSnapshot.java` 及 records/enum | — | DTO/投影模型 |
| `package-info.java` | — | 包说明 |
| `OverviewApiIntegrationTest.java` | testScope | fixture 三类流+倒序；空态；metrics；401 |

### 前端 `features/overview`

| 路径 | REQ | 说明 |
|---|---|---|
| `OverviewPage.vue` | OVW-001..005 | 指标卡、CSS 柱趋势、TOP10、动态流、行业/地域分布；§3.5 |
| `composables/useOverviewData.ts` | — | 分区加载与 empty/error 判定；只读调用 `@/api/overview` |
| `composables/useOverviewData.spec.ts` | — | 三类流中文标签 |
| `index.ts` | — | 导出 `OverviewPage` 供路由接线 |

## REQ 映射

| REQ | 实现要点 |
|---|---|
| **REQ-OVW-001** | `assetTotal` / `activeEnterprises` / `todayAttestations` + `updatedAt`；空态展示 0 |
| **REQ-OVW-002** | 近 N 天趋势柱（可切 7/14/30/60）；全 0 空态；ResizeObserver 触发重绘 key |
| **REQ-OVW-003** | TOP 按 count 降序，limit≤10 |
| **REQ-OVW-004** | 三类 `CATALOG_REGISTER`/`DATA_REGISTER`/`TRADE_ORDER`；字段齐全；倒序；fixture 不依赖业务 API |
| **REQ-OVW-005** | 行业占比条 + 地域对比条；口径说明为 V1 投影/fixture |

## 自测命令与结果

### 后端

```text
mvn "-Dtest=com.wsc.overview.OverviewApiIntegrationTest" test
```

| 类 | Tests | 结果 |
|---|---|---|
| `com.wsc.overview.OverviewApiIntegrationTest` | 5 | **PASS** |
| **合计** | **5** | **BUILD SUCCESS** |

覆盖：seed metrics；空库 metrics/stream/trend 友好空态；fixture 三类类型 + `occurredAt` 倒序；TOP 排序与 distribution；未登录 401。

### 前端

```text
pnpm typecheck
pnpm exec vitest run src/features/overview/composables/useOverviewData.spec.ts
```

| 命令 | 结果 |
|---|---|
| `pnpm typecheck` | **exit 0** |
| vitest `useOverviewData.spec.ts` | **1 passed** |

## 联调备注

- 页面导出：`frontend/src/features/overview` → `OverviewPage`；需在 `routes.ts` 将 `/overview` 指向该组件后可见。
- 默认 seed 已填充演示数据；集成测试可 `OverviewDataStore.clear()` 验证空态。
- API 基路径与 Cookie 会话同 TASK-WSC-002（`credentials: 'include'`）。
