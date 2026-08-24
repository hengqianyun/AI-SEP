# 任务包 — TASK-WSC-918：数字合约版本 / 外部跳转非建单 / 脱敏对齐

<!-- R2 revised: ISSUE-QA-003, ISSUE-QA-001 -->

```yaml
taskId: TASK-WSC-918
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: C
status: PENDING
requirements: [REQ-WSC-ORDER-017, REQ-WSC-ORDER-005, REQ-WSC-ORDER-008, REQ-WSC-ORDER-009]
dependsOn: [TASK-WSC-916, TASK-WSC-917]
```

## 1. 任务目标

下单生成数字合约初版；确认/提交/达成/取消等变更出新版本并保留历史；详情展示当前版本；无主数据空值不阻断。订购页外部跳转：有地址打开、无地址提示、**列表不新增订单**。脱敏与附件下载按 §2.2 假设对齐目录 SensitiveId

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `backend/app/data-chain-service/src/main/java/com/shdata/datachain/service/order/**` | 修改 | 版本生成；**仍不得**写 sql |
| `backend/app/data-chain-service/src/test/java/com/shdata/datachain/order/**` | 新增/修改 | 版本与脱敏测试 |
| `frontend/src/features/order/**` | 修改 | 版本区、外部跳转、脱敏展示 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md`（ORDER-017/005/008） | 需求 |
| 既有 `SensitiveId.vue` | 只读参考 |
| 订单 BE/FE | 913/915/916/917 产出 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `**/sql/**` | Flyway 归 912 |
| `contracts/**` | 契约归 911 |
| `frontend/src/api/**` | API client 归 911 |
| `WorkbenchLayout.vue` | 壳层归 914 |
| `useCanWrite.ts` | 权限归 914 |
| `routes.ts` | 跳转不新建路由 |
| `frontend/src/features/catalog/browse/**` | 浏览归 8.3 |
| `ProductDetailPage.vue` | 跳转在 subscribe 页 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] 创建后详情可见版本 1（或等价当前版本）；后续状态变更版本递增且历史可查
- [ ] 无主数据字段空/占位，仍能下单
- [ ] 外部跳转不调用 `POST /orders`；无地址提示未配置
- [ ] 列表/详情姓名、用户名、单位、信用代码、连接器、合约相关按角色脱敏（§2.2）

## 6. 测试范围（testScope）

- 后端：版本历史条数随变更增加；下单不阻断
- Vitest：外部跳转无 POST；**无地址时展示「未配置」或等价提示文案（testid 可断言）**
- Vitest：USER/PROVIDER/ADMIN 脱敏差异（可 mock）
- Vitest：**详情页版本区可见版本号、版本列表条数随状态变更递增、历史版本可展开查看**（命名用例 `918-contract-version-ui`）
- 命名用例：`918-contract-versions`、`918-external-jump-no-create`、`918-contract-version-ui`

## 7. 风险标签（riskTags）

- `pii-masking`
- `digital-contract-version`
