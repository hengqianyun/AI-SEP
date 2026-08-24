# 任务包 — TASK-WSC-917：确认 / 上传 / 统一确认页 / 取消二次确认 / toast

<!-- R2 revised: ISSUE-UX-001, ISSUE-UX-002, ISSUE-UX-003 -->

```yaml
taskId: TASK-WSC-917
planId: PLAN-WSC-9.2
runId: RUN-WSC-012
snapshotId: SNAP-WSC-009
wave: B
status: PENDING
requirements: [REQ-WSC-ORDER-010, REQ-WSC-ORDER-011, REQ-WSC-ORDER-012, REQ-WSC-ORDER-013, REQ-WSC-ORDER-014, REQ-WSC-ORDER-015, REQ-WSC-ORDER-FE-001, REQ-WSC-ORDER-FE-002, REQ-WSC-ORDER-016]
dependsOn: [TASK-WSC-915, TASK-WSC-916]
```

## 1. 任务目标

在 915 详情上按角色显示动作；统一合约确认页（同一路由或同页步骤，**禁止**第二套「标准合约」独立确认路径）；取消二次确认；各写成功 toast；模板下载与交易信息表单

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|---|---|---|
| `frontend/src/features/order/**` | 修改 | 详情动作、确认页组件、上传表单、cancel confirm、spec |

## 3. 只读集（readSet）

| 文件 | 说明 |
|---|---|
| `product/requirements/SNAP-WSC-009.md`（对应 REQ） | 需求 |
| `RULE-GLOBAL-FRONTEND` | 前端全局规则 |
| `frontend/src/api/**` | 只读，911 生成物 |
| 915 `features/order/**` | 只读基线 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|---|---|
| `frontend/src/layouts/WorkbenchLayout.vue` | 壳层归 914 |
| `frontend/src/features/auth/composables/useCanWrite.ts` | 权限归 914 |
| `frontend/src/router/routes.ts` | 统一确认页挂在 915 已建的 `/orders/:orderId`，本任务**不改**路由表 |
| `frontend/src/features/catalog/**` | 目录归 8.3 |
| `contracts/**` | 契约归 911 |
| `frontend/src/api/**` | API client 归 911 |
| `backend/**` | 后端归 912/913/916 |
| `tests/e2e/**` | E2E 归 919 |
| `product/**` | 需求文档只读 |
| `planning/**` | 计划只读 |
| `ai/runs/**/state.yaml` | 控制面禁止 |
| `ai/runs/**/events.jsonl` | 控制面禁止 |

## 5. 验收标准（acceptance）

- [ ] 无权限按钮不显示；越权不出现成功 toast
- [ ] 统一确认页布局冻结（<!-- R2 revised: ISSUE-UX-001 -->）：纵向分区排列 — **交易信息区**（标题+订单金额/明细）→ **平台统一须知区**（标题+须知正文）→ **签署附件区**（标题+下载按钮 inline），每区有标题分隔；勾选控件紧邻确认按钮上方，**未勾选时确认按钮 disabled（灰态）**；ADMIN 代 PROVIDER 确认时，页内可见操作人身份标识（如「当前操作：管理员代提供方确认」）
- [ ] 附件上传交互冻结（<!-- R2 revised: ISSUE-UX-002 -->）：上传中显示进度（Ant Upload progress 或等价）；前端即时校验：非白名单格式 / 超 20MB 在选择文件后立即提示且不发起上传；后端扫描失败 inline 错误提示（不依赖提交时 4xx）；已上传文件显示文件名+大小，可删除/替换（替换前二次确认）
- [ ] 取消对话框冻结（<!-- R2 revised: ISSUE-UX-003 -->）：标题「确认取消订单」；正文包含「取消后订单将进入已取消状态，此操作不可回退」；确认按钮文案「确认取消」，使用 Ant danger 样式（红色）；ADMIN 代取消时正文体现代操作身份（如「您正在代提供方取消此订单」）
- [ ] 确认订单/提交合约/确认合约/取消成功均 toast
- [ ] 合约已达成后无支付/交付主按钮
- [ ] 附件规则前端预校验与后端一致（类型/大小）

## 6. 测试范围（testScope）

- Vitest：取消未确认不调用 API
- Vitest：角色按钮可见性矩阵
- Vitest：统一确认未勾选不发请求（**button disabled 断言**）
- Vitest：写成功 toast 四动作
- Vitest：**附件上传 — 非白名单格式/超 20MB 前端即时校验**
- Vitest：**取消对话框标题/正文/按钮 class（danger）断言**
- Vitest：**ADMIN 代确认/代取消时页内身份标识可见**
- 命名用例：`917-cancel-confirm`、`917-unified-contract-page`、`917-upload-validation`、`917-cancel-dialog-content`

## 7. 风险标签（riskTags）

- `ux-destructive-confirm`
- `unified-contract-page`
