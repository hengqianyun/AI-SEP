# 任务包 — TASK-WSC-915：订购入口 / 须知 / 创建 UI / 列表分页字段 / 只读详情 / toast

```yaml
taskId: TASK-WSC-915
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: A
status: PENDING
requirements: [REQ-WSC-ORDER-002, REQ-WSC-ORDER-003, REQ-WSC-ORDER-004, REQ-WSC-ORDER-006, REQ-WSC-ORDER-007, REQ-WSC-ORDER-008, REQ-WSC-ORDER-009, REQ-WSC-ORDER-FE-001, REQ-WSC-ORDER-FE-003, REQ-WSC-ORDER-001]
dependsOn: [TASK-WSC-913, TASK-WSC-914]
```

## 1. 任务目标

产品详情订购 CTA（USER）；订购页展示产品与订购人信息、统一须知勾选、链内提交；列表九组字段+查看；Ant Pagination jumper+pageSize；详情只读分区+快照；创建成功 toast；空态「暂无订单数据」。Wave B 动作按钮可占位隐藏

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `frontend/src/features/order/**` | 新增 | 列表、订购、只读详情、composable、spec、toast 辅助若放在 `features/order/utils/toast.ts` |
| `frontend/src/features/catalog/detail/ProductDetailPage.vue` | 修改 | **仅**增加 USER 订购入口，不改快照/编辑/上链按钮语义 |
| `frontend/src/router/routes.ts` | 修改 | 在 914 基础上增 `/orders/subscribe/:productId`、`/orders/:orderId`；**不得**删 `/orders` 与 V1.6 路由 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md`（对应 REQ） | 需求 |
| `RULE-GLOBAL-FRONTEND` | 前端全局规则 |
| `frontend/src/api/**` | 只读，911 生成物 |
| `useCanWrite.ts` | 只读，914 产出 |
| 维护页 Pagination 实现 | 只读参考 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `frontend/src/layouts/WorkbenchLayout.vue` | 壳层归 914 |
| `frontend/src/features/auth/composables/useCanWrite.ts` | 权限归 914 |
| `frontend/src/features/catalog/browse/**` | 浏览归 8.3 |
| `frontend/src/features/catalog/maintenance/**`、`editor/**`、`import/**` | 维护归 8.3（detail 仅 ProductDetailPage 例外） |
| `contracts/**` | 契约归 911 |
| `frontend/src/api/**` | API client 归 911 |
| `backend/**` | 后端归 912/913/916 |
| `**/sql/**` | Flyway 归 912 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |
| `frontend/src/styles/**`、`frontend/src/theme/**` | 样式只读 |

## 5. 验收标准（acceptance）

- [ ] 订购入口展示：产品名称、编码、类型、提供方、来源平台、访问地址、当前订购人及所属单位
- [ ] 未勾选须知 / 备注为空不能提交；不允许简易流程时不可链内建单（按钮禁用或提交 4xx 有错误提示）
- [ ] 创建成功 toast；失败无成功 toast
- [ ] 列表九组字段（与 §3.1 `GET /orders` 200 响应字段枚举一致）；查看进详情；标题含订单号；可返回列表；产品区为快照
- [ ] 分页：可跳页、可改 pageSize 且回第 1 页；档位 10/20/50/100；缺省 10
- [ ] 无匹配「暂无订单数据」
- [ ] 敏感字段可先按 §2.2 假设做最小截断（C 波 918 对齐）

## 6. 测试范围（testScope）

- Vitest：须知未勾选不发 POST；备注空不发
- Vitest：pageSize 变更 → page=1；jumper 发对应 page
- Vitest：创建成功调用 toast 一次；失败 0 次
- Vitest：列表字段 testid 齐全（九组字段对应 testid）
- 命名用例：`915-subscribe-notice`、`915-list-pagination-jumper`

## 7. 风险标签（riskTags）

- `ux-order-create`
- `frontend-toast-pagination`
