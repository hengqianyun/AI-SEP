<script setup lang="ts">
/**
 * 合约附件上传组件 — 附件上传、交易信息表单、即时校验、扫描失败提示。
 * REQ-WSC-ORDER-013/014：附件白名单+大小校验、上传进度、交易信息。
 */
import { ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { ALLOWED_EXTENSIONS, formatFileSize } from './composables/useUploadContract'
import { useUploadContract } from './composables/useUploadContract'

const props = defineProps<{
  orderId: string
}>()

const emit = defineEmits<{
  done: []
}>()

const {
  order,
  state,
  error,
  selectedFile,
  uploadProgress,
  scanError,
  fileValidationError,
  replaceConfirmVisible,
  orderLines,
  amountTaxInclusive,
  amountTaxExclusive,
  canSubmit,
  handleFileSelect,
  handleRemoveFile,
  openReplaceConfirm,
  closeReplaceConfirm,
  confirmReplace,
  addOrderLine,
  removeOrderLine,
  calculateSubtotal,
  handleSubmit,
  goBack,
} = useUploadContract(props.orderId, () => emit('done'))

const acceptTypes = ALLOWED_EXTENSIONS.join(',')
const pendingReplaceFile = ref<File | null>(null)

function onFileInput(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    if (selectedFile.value) {
      openReplaceConfirm()
      pendingReplaceFile.value = file
    } else {
      handleFileSelect(file)
    }
  }
  input.value = ''
}

function onReplaceConfirm() {
  confirmReplace()
  if (pendingReplaceFile.value) {
    handleFileSelect(pendingReplaceFile.value)
    pendingReplaceFile.value = null
  }
}

function onReplaceCancel() {
  closeReplaceConfirm()
  pendingReplaceFile.value = null
}
</script>

<template>
  <div class="upload-page" data-testid="contract-upload-page">
    <header class="page-header wsc-card">
      <div class="header-left">
        <button type="button" class="back-btn" aria-label="返回详情" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
        <h1 class="page-title">提交合约</h1>
      </div>
    </header>

    <div v-if="state === 'loading'" class="state-card wsc-card" data-testid="upload-loading">
      加载中…
    </div>
    <div v-else-if="state === 'error' && !order" class="state-card wsc-card error" data-testid="upload-error">
      <p>{{ error }}</p>
    </div>

    <template v-else-if="order">
      <!-- 附件上传区 -->
      <div class="section-card wsc-card" data-testid="upload-attachment-section">
        <h2 class="section-title">签署附件</h2>
        <p class="section-hint">支持 Word（.doc/.docx）和 PDF 格式，单文件不超过 20MB。</p>

        <!-- 已选文件 -->
        <div v-if="selectedFile" class="file-info" data-testid="upload-file-info">
          <div class="file-detail">
            <span class="file-name" data-testid="upload-file-name">{{ selectedFile.name }}</span>
            <span class="file-size" data-testid="upload-file-size">{{ formatFileSize(selectedFile.size) }}</span>
          </div>
          <div class="file-actions">
            <button
              type="button"
              class="file-action-btn"
              data-testid="upload-file-replace"
              @click="openReplaceConfirm"
            >
              替换
            </button>
            <button
              type="button"
              class="file-action-btn danger"
              data-testid="upload-file-remove"
              @click="handleRemoveFile"
            >
              删除
            </button>
          </div>
        </div>

        <!-- 上传进度 -->
        <div v-if="state === 'uploading'" class="progress-bar" data-testid="upload-progress">
          <div class="progress-fill" :style="{ width: `${uploadProgress}%` }" />
          <span class="progress-text">{{ uploadProgress }}%</span>
        </div>

        <!-- 文件选择 -->
        <label v-if="!selectedFile" class="file-input-label" data-testid="upload-file-input-label">
          <input
            type="file"
            :accept="acceptTypes"
            class="file-input-hidden"
            data-testid="upload-file-input"
            @change="onFileInput"
          />
          选择文件
        </label>

        <!-- 前端即时校验错误 -->
        <p v-if="fileValidationError" class="error-msg" data-testid="upload-validation-error">
          {{ fileValidationError }}
        </p>

        <!-- 后端扫描失败 inline 错误 -->
        <p v-if="scanError" class="error-msg" data-testid="upload-scan-error">
          {{ scanError }}
        </p>
      </div>

      <!-- 交易信息区 -->
      <div class="section-card wsc-card" data-testid="upload-transaction-section">
        <h2 class="section-title">交易信息</h2>

        <div class="form-grid">
          <div class="form-item">
            <label class="form-label">含税金额 *</label>
            <input
              v-model="amountTaxInclusive"
              type="text"
              class="form-input"
              placeholder="请输入含税金额"
              data-testid="upload-amount-inclusive"
            />
          </div>
          <div class="form-item">
            <label class="form-label">不含税金额</label>
            <input
              v-model="amountTaxExclusive"
              type="text"
              class="form-input"
              placeholder="请输入不含税金额（默认同含税）"
              data-testid="upload-amount-exclusive"
            />
          </div>
        </div>

        <!-- 订单明细 -->
        <h3 class="sub-title">订单明细</h3>
        <table class="lines-table" data-testid="upload-order-lines">
          <thead>
            <tr>
              <th>单位</th>
              <th>数量</th>
              <th>单价</th>
              <th>小计</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(line, idx) in orderLines" :key="idx">
              <td>
                <input v-model="line.unit" type="text" class="table-input" :data-testid="`line-unit-${idx}`" />
              </td>
              <td>
                <input v-model.number="line.quantity" type="number" min="1" class="table-input" :data-testid="`line-qty-${idx}`" />
              </td>
              <td>
                <input v-model="line.unitPrice" type="text" class="table-input" :data-testid="`line-price-${idx}`" />
              </td>
              <td>
                <span class="subtotal" :data-testid="`line-subtotal-${idx}`">{{ calculateSubtotal(line) }}</span>
              </td>
              <td>
                <button
                  v-if="orderLines.length > 1"
                  type="button"
                  class="line-remove-btn"
                  :data-testid="`line-remove-${idx}`"
                  @click="removeOrderLine(idx)"
                >
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <button type="button" class="add-line-btn" data-testid="upload-add-line" @click="addOrderLine">
          + 添加明细
        </button>
      </div>

      <!-- 通用错误 -->
      <p v-if="error" class="error-msg" data-testid="upload-submit-error">{{ error }}</p>

      <!-- 提交 -->
      <div class="submit-bar">
        <button
          type="button"
          class="btn primary"
          :disabled="!canSubmit"
          data-testid="upload-submit"
          @click="handleSubmit"
        >
          {{ state === 'uploading' ? '提交中…' : '提交合约' }}
        </button>
      </div>
    </template>

    <!-- 替换文件二次确认对话框 -->
    <Modal
      :open="replaceConfirmVisible"
      title="确认替换文件"
      :closable="true"
      :mask-closable="false"
      :footer="null"
      data-testid="replace-confirm-dialog"
      @cancel="onReplaceCancel"
    >
      <p>当前已有已选文件，替换后原文件将被移除。确认替换？</p>
      <div class="dialog-actions">
        <button type="button" class="btn-cancel" data-testid="replace-cancel-btn" @click="onReplaceCancel">
          取消
        </button>
        <button type="button" class="btn-primary" data-testid="replace-confirm-btn" @click="onReplaceConfirm">
          确认替换
        </button>
      </div>
    </Modal>
  </div>
</template>

<style scoped>
.upload-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 40px;
  color: var(--text-primary);
}

.wsc-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
}

.page-header {
  padding: 16px 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
}

.back-btn:hover {
  background: #f9fafb;
  color: var(--text-primary);
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.state-card {
  padding: 28px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.state-card.error {
  color: #b91c1c;
}

.state-card.error p {
  margin: 0;
}

.section-card {
  padding: 20px 24px;
}

.section-title {
  margin: 0 0 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-hint {
  margin: 0 0 14px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  margin-bottom: 12px;
}

.file-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.file-size {
  font-size: 11px;
  color: var(--text-tertiary);
}

.file-actions {
  display: flex;
  gap: 8px;
}

.file-action-btn {
  height: 28px;
  padding: 0 10px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  color: var(--text-primary);
}

.file-action-btn:hover {
  background: #f9fafb;
}

.file-action-btn.danger {
  color: #ef4444;
  border-color: #fecaca;
}

.file-action-btn.danger:hover {
  background: #fef2f2;
}

.progress-bar {
  position: relative;
  height: 24px;
  background: #f3f4f6;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: var(--blue);
  border-radius: 12px;
  transition: width 0.3s;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 11px;
  font-weight: 600;
  color: var(--text-primary);
}

.file-input-label {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 20px;
  border-radius: var(--radius-sm);
  border: 1px dashed var(--border-color);
  background: #f9fafb;
  font-size: 13px;
  font-weight: 500;
  color: var(--blue);
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.file-input-label:hover {
  border-color: var(--blue);
  background: #eff6ff;
}

.file-input-hidden {
  display: none;
}

.error-msg {
  margin: 8px 0 0;
  padding: 8px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: #b91c1c;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
}

.form-input {
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font: inherit;
  font-size: 13px;
  color: var(--text-primary);
}

.form-input::placeholder {
  color: var(--text-tertiary);
}

.sub-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.lines-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  margin-bottom: 10px;
}

.lines-table th {
  text-align: left;
  padding: 8px 8px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  font-size: 12px;
}

.lines-table td {
  padding: 6px 8px;
  border-bottom: 1px solid var(--border-color);
}

.table-input {
  width: 100%;
  box-sizing: border-box;
  height: 30px;
  padding: 0 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font: inherit;
  font-size: 13px;
  color: var(--text-primary);
}

.subtotal {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.line-remove-btn {
  height: 26px;
  padding: 0 8px;
  border-radius: 4px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  font: inherit;
  font-size: 11px;
  color: #ef4444;
  cursor: pointer;
}

.line-remove-btn:hover {
  background: #fee2e2;
}

.add-line-btn {
  height: 32px;
  padding: 0 14px;
  border-radius: 4px;
  border: 1px dashed var(--border-color);
  background: transparent;
  font: inherit;
  font-size: 13px;
  color: var(--blue);
  cursor: pointer;
}

.add-line-btn:hover {
  border-color: var(--blue);
  background: #eff6ff;
}

.submit-bar {
  display: flex;
  justify-content: flex-end;
  padding: 0 4px;
}

.btn {
  height: 36px;
  padding: 0 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-primary);
  transition: background 0.15s, border-color 0.15s;
}

.btn.primary {
  border-color: var(--blue);
  background: var(--blue);
  color: #fff;
}

.btn.primary:hover {
  background: #2563eb;
  border-color: #2563eb;
}

.btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}

.btn-cancel {
  height: 34px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-primary);
}

.btn-cancel:hover {
  background: #f9fafb;
}

.btn-primary {
  height: 34px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--blue);
  background: var(--blue);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: #fff;
}

.btn-primary:hover {
  background: #2563eb;
  border-color: #2563eb;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
