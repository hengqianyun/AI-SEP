<script setup lang="ts">
/**
 * 订购页 — 展示产品信息、当前订购人、统一须知勾选、备注、提交。
 * REQ-WSC-ORDER-002/003/004/006。
 */
import { useRouter } from 'vue-router'
import { useOrderSubscribe } from './composables/useOrderSubscribe'

const router = useRouter()
const {
  product,
  notice,
  remark,
  noticeAccepted,
  state,
  error,
  demandUserName,
  demandEnterpriseName,
  canSubmit,
  submit,
} = useOrderSubscribe()

function goBack() {
  void router.back()
}

/**
 * 外部跳转（REQ-WSC-ORDER-005）。
 * 有地址时打开新窗口；无地址时展示「未配置」提示（testid 可断言）。
 * 此操作不调用 POST /orders，不新增订单。
 */
function openExternalUrl(url: string) {
  if (url) {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}

async function handleSubmit() {
  await submit()
}
</script>

<template>
  <div class="subscribe-page" data-testid="order-subscribe">
    <header class="page-header wsc-card">
      <div class="header-left">
        <button type="button" class="back-btn" aria-label="返回" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
        <h1 class="page-title">订购数据产品</h1>
      </div>
    </header>

    <div v-if="state === 'loading'" class="state-card wsc-card" data-testid="subscribe-loading">
      加载中…
    </div>
    <div v-else-if="state === 'error' && !product" class="state-card wsc-card error" data-testid="subscribe-error">
      <p>{{ error }}</p>
    </div>

    <template v-else-if="product">
      <!-- 产品信息区 -->
      <div class="section-card wsc-card" data-testid="subscribe-product-info">
        <h2 class="section-title">产品信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">产品名称</span>
            <span class="info-value" data-testid="sub-product-name">{{ product.productName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">产品编码</span>
            <span class="info-value" data-testid="sub-product-code">{{ product.productCode }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">产品类型</span>
            <span class="info-value" data-testid="sub-product-type">{{ product.productType }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">提供方</span>
            <span class="info-value" data-testid="sub-provider">{{ product.supplierName || '—' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">来源平台</span>
            <span class="info-value" data-testid="sub-source-platform">{{ product.dataSource || '—' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">访问地址</span>
            <div class="access-url-row" data-testid="sub-access-url-row">
              <span class="info-value" data-testid="sub-access-url">{{ product.deliveryMethod || '—' }}</span>
              <button
                v-if="product.deliveryMethod"
                type="button"
                class="external-jump-btn"
                data-testid="external-jump-btn"
                @click="openExternalUrl(product.deliveryMethod)"
              >
                前往访问
              </button>
              <span
                v-else
                class="external-not-configured"
                data-testid="external-not-configured"
              >
                未配置
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订购人信息 -->
      <div class="section-card wsc-card" data-testid="subscribe-demand-user">
        <h2 class="section-title">订购人信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">当前订购人</span>
            <span class="info-value" data-testid="sub-demand-user">{{ demandUserName || '—' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所属单位</span>
            <span class="info-value" data-testid="sub-demand-enterprise">{{ demandEnterpriseName || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- 须知勾选 -->
      <div class="section-card wsc-card" data-testid="subscribe-notice">
        <h2 class="section-title">订购须知</h2>
        <div v-if="notice" class="notice-content" data-testid="notice-content">
          {{ notice.content }}
        </div>
        <div v-else class="notice-content muted">须知内容加载中…</div>
        <label class="notice-check" data-testid="notice-checkbox-label">
          <input
            v-model="noticeAccepted"
            type="checkbox"
            data-testid="notice-checkbox"
          />
          <span>我已阅读并同意上述须知</span>
        </label>
      </div>

      <!-- 备注 -->
      <div class="section-card wsc-card">
        <h2 class="section-title">备注</h2>
        <textarea
          v-model="remark"
          class="remark-input"
          placeholder="请输入订购备注（必填）"
          rows="3"
          data-testid="subscribe-remark"
        />
      </div>

      <!-- 错误提示 -->
      <p v-if="error" class="error-msg" data-testid="subscribe-submit-error">{{ error }}</p>

      <!-- 提交 -->
      <div class="submit-bar">
        <button
          type="button"
          class="btn primary"
          :disabled="!canSubmit"
          data-testid="subscribe-submit"
          @click="handleSubmit"
        >
          {{ state === 'submitting' ? '提交中…' : '提交订购' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.subscribe-page {
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

.notice-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  margin-bottom: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.notice-content.muted {
  color: var(--text-tertiary);
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

.remark-input {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  font-size: 13px;
  font-family: inherit;
  resize: vertical;
  color: var(--text-primary);
}

.remark-input::placeholder {
  color: var(--text-tertiary);
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

/* 外部跳转 */
.access-url-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.external-jump-btn {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  background: var(--blue-light);
  color: var(--blue);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  font: inherit;
  transition: background 0.15s;
}

.external-jump-btn:hover {
  background: #dbeafe;
}

.external-not-configured {
  font-size: 12px;
  color: var(--text-tertiary);
  padding: 0 6px;
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
