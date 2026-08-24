<script setup lang="ts">
/**
 * 统一合约确认组件 — 交易信息、平台统一须知、签署附件、勾选确认。
 * REQ-WSC-ORDER-013/015/FE-001：统一确认页布局冻结；未勾选 disabled；ADMIN 代操作身份标识。
 */
import { useContractConfirm } from './composables/useContractConfirm'

const props = defineProps<{
  orderId: string
}>()

const emit = defineEmits<{
  done: []
}>()

const {
  order,
  notice,
  noticeAccepted,
  state,
  error,
  isProxyAction,
  canSubmit,
  attachmentUrl,
  attachmentFileName,
  handleSubmit,
  goBack,
} = useContractConfirm(props.orderId, () => emit('done'))

function formatAmount(val: string | null | undefined): string {
  if (!val) return '—'
  return `¥ ${val}`
}
</script>

<template>
  <div class="contract-confirm-page" data-testid="contract-confirm-page">
    <header class="page-header wsc-card">
      <div class="header-left">
        <button type="button" class="back-btn" aria-label="返回详情" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
        <h1 class="page-title">合约确认</h1>
      </div>
    </header>

    <!-- ADMIN 代操作身份标识 -->
    <div v-if="isProxyAction" class="proxy-banner wsc-card" data-testid="proxy-action-banner">
      当前操作：管理员代提供方确认
    </div>

    <div v-if="state === 'loading'" class="state-card wsc-card" data-testid="confirm-loading">
      加载中…
    </div>
    <div v-else-if="state === 'error' && !order" class="state-card wsc-card error" data-testid="confirm-error">
      <p>{{ error }}</p>
    </div>

    <template v-else-if="order">
      <!-- 交易信息区 -->
      <div class="section-card wsc-card" data-testid="confirm-transaction-section">
        <h2 class="section-title">交易信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value" data-testid="confirm-order-id">{{ order.orderId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">含税金额</span>
            <span class="info-value" data-testid="confirm-amount">
              {{ formatAmount(order.transactionInfo?.amountTaxInclusive ?? order.amountTaxInclusive) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">不含税金额</span>
            <span class="info-value">
              {{ formatAmount(order.transactionInfo?.amountTaxExclusive ?? order.amountTaxExclusive) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">币种</span>
            <span class="info-value">{{ order.transactionInfo?.currency ?? order.currency ?? 'CNY' }}</span>
          </div>
        </div>
        <!-- 订单明细 -->
        <div v-if="order.transactionInfo?.orderLines?.length" class="order-lines">
          <h3 class="sub-title">订单明细</h3>
          <table class="lines-table" data-testid="confirm-order-lines">
            <thead>
              <tr>
                <th>单位</th>
                <th>数量</th>
                <th>单价</th>
                <th>小计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(line, idx) in order.transactionInfo.orderLines" :key="idx">
                <td>{{ line.unit }}</td>
                <td>{{ line.quantity }}</td>
                <td>{{ line.unitPrice }}</td>
                <td>{{ line.subtotal }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 平台统一须知区 -->
      <div class="section-card wsc-card" data-testid="confirm-notice-section">
        <h2 class="section-title">平台统一须知</h2>
        <div v-if="notice" class="notice-content" data-testid="confirm-notice-content">
          {{ notice.content }}
        </div>
        <div v-else class="notice-content muted">须知内容加载中…</div>
      </div>

      <!-- 签署附件区 -->
      <div class="section-card wsc-card" data-testid="confirm-attachment-section">
        <h2 class="section-title">签署附件</h2>
        <div class="attachment-row">
          <span class="attachment-name" data-testid="confirm-attachment-name">{{ attachmentFileName }}</span>
          <a
            v-if="attachmentUrl"
            :href="attachmentUrl"
            target="_blank"
            class="download-btn"
            data-testid="confirm-attachment-download"
          >
            下载
          </a>
        </div>
      </div>

      <!-- 勾选控件紧邻确认按钮上方 -->
      <div class="confirm-bar">
        <label class="notice-check" data-testid="confirm-checkbox-label">
          <input
            v-model="noticeAccepted"
            type="checkbox"
            data-testid="confirm-checkbox"
          />
          <span>我已阅读并同意上述须知</span>
        </label>
        <p v-if="error" class="error-msg" data-testid="confirm-submit-error">{{ error }}</p>
        <button
          type="button"
          class="btn primary"
          :disabled="!canSubmit"
          data-testid="confirm-submit"
          @click="handleSubmit"
        >
          {{ state === 'submitting' ? '确认中…' : '确认合约' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.contract-confirm-page {
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

.proxy-banner {
  padding: 10px 20px;
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1e40af;
  font-size: 13px;
  font-weight: 500;
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

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.info-label {
  font-size: 11px;
  color: var(--text-tertiary);
}

.info-value {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-word;
}

.sub-title {
  margin: 16px 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.lines-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.lines-table th {
  text-align: left;
  padding: 8px 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  font-size: 12px;
}

.lines-table td {
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-primary);
}

.notice-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  max-height: 200px;
  overflow-y: auto;
}

.notice-content.muted {
  color: var(--text-tertiary);
}

.attachment-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.attachment-name {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.download-btn {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 4px;
  border: 1px solid #bfdbfe;
  background: var(--blue-light);
  color: var(--blue);
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s;
}

.download-btn:hover {
  background: #dbeafe;
}

.confirm-bar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 4px;
}

.notice-check {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
}

.notice-check input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.error-msg {
  margin: 0;
  padding: 8px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: #b91c1c;
}

.btn {
  align-self: flex-end;
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

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
