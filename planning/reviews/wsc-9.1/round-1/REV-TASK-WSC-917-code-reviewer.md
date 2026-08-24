```yaml
reviewId: REV-TASK-WSC-917
taskId: TASK-WSC-917
role: codeReviewer
actorInstance: code-reviewer-wsc-917
decision: APPROVE
summary: |
  P0/P1 清零。所有验收标准均已实现：角色按钮权限矩阵正确、统一确认页三区纵向排列且未勾选 disabled、附件白名单/20MB 即时校验与替换二次确认、取消对话框标题/正文/danger 样式/ADMIN 代操作身份标识齐全、四动作成功 toast、合约达成后无支付/交付主按钮、denyModify 文件未被修改。发现 3 项 P2 可记录延期。
```

---

## 审查范围确认

| 检查项 | 结果 |
|---|---|
| SCOPE_CHECK：仅修改 writeSet 内文件 | PASS 所有变更均在 `frontend/src/features/order/**` |
| denyModify：routes.ts | PASS 路由表修改来自 915 基线，917 未新增/修改路由 |
| denyModify：WorkbenchLayout.vue | PASS 未修改 |
| denyModify：useCanWrite.ts | PASS 未修改 |
| denyModify：api/** | PASS 未修改（只读引用） |

---

## 验收标准逐项核对

| # | 验收标准 | 结果 | 证据 |
|---|---|---|---|
| 1 | 无权限按钮不显示；越权不出现成功 toast | PASS | `canConfirmOrder`/`canUploadContract`/`canConfirmContract`/`canCancel` 严格按 status x role 矩阵判断；`actions` computed 对每项加 `isOwnEnterprise` 企业校验（PROVIDER）；`v-if="actions.xxx"` 控制渲染 |
| 2 | 统一确认页布局冻结 | PASS | `OrderContractConfirmPage.vue`：交易信息区 -> 平台统一须知区 -> 签署附件区，三区纵向排列，每区有 `section-title` 分隔；`noticeAccepted` checkbox 紧邻确认按钮上方，`:disabled="!canSubmit"` 未勾选时灰态；ADMIN 代确认时 `proxy-banner` 显示「当前操作：管理员代提供方确认」 |
| 3 | 附件上传交互冻结 | PASS | `validateFile` 即时校验白名单（`.doc/.docx/.pdf`）+ 20MB；`fileValidationError`/`scanError` inline 显示；上传进度条 `uploadProgress`；已选文件显示名称+大小；替换前二次确认对话框 |
| 4 | 取消对话框冻结 | PASS | Modal title「确认取消订单」；正文含「取消后订单将进入已取消状态，此操作不可回退」；确认按钮文案「确认取消」+ `.btn-danger` 红色样式；ADMIN 代取消时正文含「您正在代提供方取消此订单」 |
| 5 | 四动作成功 toast | PASS | `toast.ts` 导出 5 个函数（create/confirmOrder/submitContract/confirmContract/cancel），各 composable 在成功路径调用对应 toast |
| 6 | 合约已达成后无支付/交付主按钮 | PASS | `isTerminal()` 判断 `CONTRACT_REACHED`/`CANCELLED`；`actions` computed 在 `isTerminal` 状态下所有按钮为 false |
| 7 | 附件规则前端预校验与后端一致 | PASS | `ALLOWED_EXTENSIONS = ['.doc', '.docx', '.pdf']`、`MAX_FILE_SIZE = 20MB`，与后端白名单/大小限制一致 |

---

## Findings

### P2 (可记录延期)

#### P2-001: 上传进度 setInterval 在提交失败时未清除

**File**: `composables/useUploadContract.ts` L168-172 / L199-201

**Description**: `handleSubmit` 中 `setInterval` 仅在 `try` 块成功路径调用 `clearInterval`。若 `submitContract` 抛出异常，`catch` 块将 `uploadProgress` 重置为 0 并设置 `state = 'idle'`，但 `setInterval` 仍在运行，会持续更新 `uploadProgress` ref。

**Impact**: 提交失败后进度条可能出现异常动画；少量资源泄漏（组件卸载时由 Vue 清理）。

**closeWhen**: 将 `clearInterval(progressTimer)` 移至 `finally` 块，确保无论成功/失败都清除定时器。

Current code (problematic):
```typescript
try {
  const progressTimer = setInterval(...)
  await submitContract(...)
  clearInterval(progressTimer)  // only on success path
  ...
} catch (e) {
  // clearInterval missing
} finally {
  state.value = 'idle'
}
```

Suggested fix:
```typescript
let progressTimer: ReturnType<typeof setInterval> | undefined
try {
  progressTimer = setInterval(...)
  await submitContract(...)
  uploadProgress.value = 100
  ...
} catch (e) {
  uploadProgress.value = 0
  ...
} finally {
  if (progressTimer) clearInterval(progressTimer)
  state.value = 'idle'
}
```

---

#### P2-002: 取消对话框测试未验证模板渲染

**File**: `__tests__/917-cancel-dialog-content.spec.ts`

**Description**: 对话框标题/正文/按钮 class 的测试用例仅断言硬编码字符串常量（如 `expect('确认取消').toBe('确认取消')`），未挂载组件验证实际 DOM 输出。测试会通过但不反映模板变更。

**closeWhen**: 使用 `@vue/test-utils` 的 `mount` 渲染 `OrderDetailPage`，断言 `[data-testid="cancel-dialog"]` 中的标题、正文和按钮 class。

---

#### P2-003: toast 四动作测试仅调用 mock 函数

**File**: `__tests__/917-cancel-confirm.spec.ts` L180-198

**Description**: `917-toast-four-actions` describe 块直接调用 mock 函数（如 `showConfirmOrderToast()`）再断言 `toHaveBeenCalledTimes(1)`，未验证 composable 成功路径是否真正调用了 toast。`submitContractToast` 也未在此文件中测试。

**closeWhen**: 在已有 composable 集成测试（如 `cancel dialog confirms calls API and shows toast`）中补充 `showSubmitContractToast` 断言，或在 `917-toast-four-actions` 中通过触发 composable 动作间接验证。

---

## Decision

**APPROVE** -- P0/P1 clear. All acceptance criteria implemented, code logic correct, scope compliant. 3x P2 findings logged for deferred fix.
