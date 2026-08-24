<script setup lang="ts">
/**
 * 订单详情页 — 只读分区展示、产品快照、时间线、订单号标题。
 * 角色动作按钮（确认订单/上传合约/确认合约/取消）。
 * 子视图：合约确认页、合约上传页（嵌入详情页，不改路由表）。
 * REQ-WSC-ORDER-001/009/010/011/012/FE-001/FE-002。
 * REQ-WSC-ORDER-017：数字合约版本区。
 * REQ-WSC-ORDER-008/009：详情按角色脱敏（§2.2）。
 */
import { computed, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { ORDER_STATUS_LABELS } from '@/api/orders'
import { useOrderDetail } from './composables/useOrderDetail'
import { maskByRole } from './utils/maskSensitive'
import OrderContractConfirmPage from './OrderContractConfirmPage.vue'
import OrderContractUploadPage from './OrderContractUploadPage.vue'

const {
  orderId,
  order,
  state,
  error,
  subView,
  actionLoading,
  cancelDialogVisible,
  isProxyAction,
  actions,
  role,
  handleConfirmOrder,
  openCancelDialog,
  closeCancelDialog,
  handleCancelOrder,
  goToContractConfirm,
  goToContractUpload,
  goBackToDetail,
} = useOrderDetail()

function formatTime(val: string | null | undefined): string {
  if (!val) return '—'
  return val.replace('T', ' ').slice(0, 19)
}

/** 按角色脱敏 */
function maskValue(value: string | null | undefined): string {
  return maskByRole(value, role.value)
}

// ── 数字合约版本区 ──

/** 展开的版本号集合 */
const expandedVersions = ref<Set<number>>(new Set())

/** 当前版本号（最大版本号） */
const currentVersionNo = computed(() => {
  if (!order.value?.contractVersions?.length) return 0
  return Math.max(...order.value.contractVersions.map(v => v.versionNo))
})

/** 版本列表（按版本号降序，当前版本排在最前） */
const sortedVersions = computed(() => {
  if (!order.value?.contractVersions) return []
  return [...order.value.contractVersions].sort((a, b) => b.versionNo - a.versionNo)
})

function toggleVersionExpand(versionNo: number) {
  if (expandedVersions.value.has(versionNo)) {
    expandedVersions.value.delete(versionNo)
  } else {
    expandedVersions.value.add(versionNo)
  }
}

function isVersionExpanded(versionNo: number): boolean {
  return expandedVersions.value.has(versionNo)
}

function formatVersionSnapshot(json: string): string {
  if (!json) return '—'
  try {
    const obj = JSON.parse(json)
    return JSON.stringify(obj, null, 2)
  } catch {
    return json
  }
}
</script>

<template>
  <div class="order-detail" data-testid="order-detail">
    <!-- 子视图：合约确认 -->
    <OrderContractConfirmPage
      v-if="subView === 'confirm'"
      :order-id="orderId"
      @done="goBackToDetail"
    />

    <!-- 子视图：合约上传 -->
    <OrderContractUploadPage
      v-else-if="subView === 'upload'"
      :order-id="orderId"
      @done="goBackToDetail"
    />

    <!-- 默认详情视图 -->
    <template v-else>
      <header class="page-header wsc-card">
        <div class="header-left">
          <button type="button" class="back-btn" aria-label="返回列表" @click="$router.push('/orders')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12" />
              <polyline points="12 19 5 12 12 5" />
            </svg>
          </button>
          <h1 class="page-title" data-testid="detail-title">
            订单详情 <span class="order-id-tag">{{ orderId }}</span>
          </h1>
        </div>
        <!-- 角色动作按钮 -->
        <div v-if="order" class="header-right" data-testid="detail-actions">
          <button
            v-if="actions.confirmOrder"
            type="button"
            class="action-btn primary"
            :disabled="actionLoading"
            data-testid="action-confirm-order"
            @click="handleConfirmOrder"
          >
            {{ isProxyAction ? '代确认订单' : '确认订单' }}
          </button>
          <button
            v-if="actions.uploadContract"
            type="button"
            class="action-btn primary"
            data-testid="action-upload-contract"
            @click="goToContractUpload"
          >
            提交合约
          </button>
          <button
            v-if="actions.confirmContract"
            type="button"
            class="action-btn primary"
            data-testid="action-confirm-contract"
            @click="goToContractConfirm"
          >
            {{ isProxyAction ? '代确认合约' : '确认合约' }}
          </button>
          <button
            v-if="actions.cancel"
            type="button"
            class="action-btn danger"
            :disabled="actionLoading"
            data-testid="action-cancel"
            @click="openCancelDialog"
          >
            {{ isProxyAction ? '代取消' : '取消' }}
          </button>
        </div>
      </header>

      <div v-if="state === 'loading'" class="state-card wsc-card" data-testid="detail-loading">
        加载中…
      </div>
      <div v-else-if="state === 'error'" class="state-card wsc-card error" data-testid="detail-error">
        <p>{{ error }}</p>
      </div>

      <template v-else-if="order">
        <!-- 基本信息 -->
        <div class="section-card wsc-card" data-testid="detail-basic">
          <h2 class="section-title">基本信息</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单号</span>
              <span class="info-value" data-testid="det-order-id">{{ order.orderId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span class="info-value" data-testid="det-status">
                {{ order.statusLabel || ORDER_STATUS_LABELS[order.status] }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value" data-testid="det-create-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">更新时间</span>
              <span class="info-value" data-testid="det-update-time">{{ formatTime(order.updateTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">备注</span>
              <span class="info-value" data-testid="det-remark">{{ order.remark || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">上链次数</span>
              <span class="info-value" data-testid="det-chain-count">{{ order.chainCount }}</span>
            </div>
          </div>
        </div>

        <!-- 产品快照 -->
        <div class="section-card wsc-card" data-testid="detail-product-snapshot">
          <h2 class="section-title">产品快照</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">产品名称</span>
              <span class="info-value" data-testid="det-product-name">{{ order.productSnapshot.productName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">产品编码</span>
              <span class="info-value" data-testid="det-product-code">{{ order.productSnapshot.productCode }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">产品类型</span>
              <span class="info-value" data-testid="det-product-type">{{ order.productSnapshot.productType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">来源平台</span>
              <span class="info-value" data-testid="det-source-platform">{{ order.productSnapshot.sourcePlatform || '—' }}</span>
            </div>
          </div>
        </div>

        <!-- 参与方 -->
        <div class="section-card wsc-card" data-testid="detail-participant">
          <h2 class="section-title">参与方</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">提供方</span>
              <span class="info-value" data-testid="det-provider">{{ maskValue(order.participant.providerEnterpriseName) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订购人</span>
              <span class="info-value" data-testid="det-demand-user">{{ maskValue(order.participant.demandUserName) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">所属单位</span>
              <span class="info-value" data-testid="det-demand-enterprise">{{ maskValue(order.participant.demandEnterpriseName) }}</span>
            </div>
          </div>
        </div>

        <!-- 交易信息 -->
        <div v-if="order.transactionInfo" class="section-card wsc-card" data-testid="detail-transaction">
          <h2 class="section-title">交易信息</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">含税金额</span>
              <span class="info-value">{{ order.transactionInfo.amountTaxInclusive || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">不含税金额</span>
              <span class="info-value">{{ order.transactionInfo.amountTaxExclusive || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">币种</span>
              <span class="info-value">{{ order.transactionInfo.currency }}</span>
            </div>
          </div>
        </div>

        <!-- 时间线 -->
        <div v-if="order.timeline.length" class="section-card wsc-card" data-testid="detail-timeline">
          <h2 class="section-title">时间线</h2>
          <ul class="timeline-list">
            <li
              v-for="(evt, idx) in order.timeline"
              :key="idx"
              class="timeline-item"
              :data-testid="`timeline-${idx}`"
            >
              <div class="timeline-dot" />
              <div class="timeline-content">
                <div class="timeline-desc">{{ evt.description }}</div>
                <div class="timeline-meta">
                  <span data-testid="timeline-operator">{{ evt.operator || '—' }}</span>
                  <span v-if="evt.operatorRole">（{{ evt.operatorRole }}）</span>
                  <span class="timeline-time">{{ formatTime(evt.occurredAt) }}</span>
                </div>
              </div>
            </li>
          </ul>
        </div>

        <!-- 数字合约版本 -->
        <div class="section-card wsc-card" data-testid="detail-contract-versions">
          <h2 class="section-title">
            数字合约版本
            <span v-if="currentVersionNo > 0" class="version-badge" data-testid="current-version-no">
              v{{ currentVersionNo }}
            </span>
          </h2>
          <div v-if="sortedVersions.length === 0" class="empty-versions" data-testid="empty-versions">
            暂无合约版本
          </div>
          <ul v-else class="version-list">
            <li
              v-for="ver in sortedVersions"
              :key="ver.versionNo"
              class="version-item"
              :data-testid="`version-${ver.versionNo}`"
            >
              <div class="version-header" @click="toggleVersionExpand(ver.versionNo)">
                <span class="version-label">
                  版本 {{ ver.versionNo }}
                  <span v-if="ver.versionNo === currentVersionNo" class="current-tag">当前</span>
                </span>
                <span class="version-time">{{ formatTime(ver.createdAt) }}</span>
                <span class="version-toggle">{{ isVersionExpanded(ver.versionNo) ? '收起' : '展开' }}</span>
              </div>
              <div v-if="isVersionExpanded(ver.versionNo)" class="version-detail" data-testid="version-detail">
                <pre class="version-json">{{ formatVersionSnapshot(ver.snapshotJson) }}</pre>
              </div>
            </li>
          </ul>
        </div>
      </template>

      <!-- 取消确认对话框 -->
      <Modal
        :open="cancelDialogVisible"
        title="确认取消订单"
        :closable="true"
        :mask-closable="false"
        :footer="null"
        data-testid="cancel-dialog"
        @cancel="closeCancelDialog"
      >
        <p data-testid="cancel-dialog-body">
          <template v-if="isProxyAction">您正在代提供方取消此订单。</template>
          取消后订单将进入已取消状态，此操作不可回退。
        </p>
        <div class="dialog-actions">
          <button
            type="button"
            class="btn-cancel"
            data-testid="cancel-dialog-cancel-btn"
            @click="closeCancelDialog"
          >
            再想想
          </button>
          <button
            type="button"
            class="btn-danger"
            :disabled="actionLoading"
            data-testid="cancel-dialog-confirm-btn"
            @click="handleCancelOrder"
          >
            确认取消
          </button>
        </div>
      </Modal>
    </template>
  </div>
</template>

<style scoped>
.order-detail {
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-right {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.order-id-tag {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  font-family: monospace;
  margin-left: 8px;
}

.action-btn {
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
  transition: background 0.15s, border-color 0.15s;
}

.action-btn.primary {
  border-color: var(--blue);
  background: var(--blue);
  color: #fff;
}

.action-btn.primary:hover {
  background: #2563eb;
  border-color: #2563eb;
}

.action-btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.danger {
  border-color: #ef4444;
  background: #ef4444;
  color: #fff;
}

.action-btn.danger:hover {
  background: #dc2626;
  border-color: #dc2626;
}

.action-btn.danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  grid-template-columns: repeat(3, 1fr);
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

.timeline-list {
  list-style: none;
  margin: 0;
  padding: 0;
  position: relative;
}

.timeline-list::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: var(--border-color);
}

.timeline-item {
  display: flex;
  gap: 14px;
  padding: 8px 0;
  position: relative;
}

.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--blue);
  flex-shrink: 0;
  margin-top: 2px;
  z-index: 1;
}

.timeline-content {
  flex: 1;
  min-width: 0;
}

.timeline-desc {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.timeline-meta {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.timeline-time {
  color: var(--text-tertiary);
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

.btn-danger {
  height: 34px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  border: 1px solid #ef4444;
  background: #ef4444;
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: #fff;
}

.btn-danger:hover {
  background: #dc2626;
  border-color: #dc2626;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}

/* 数字合约版本区 */
.version-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 1px 8px;
  border-radius: 10px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
}

.empty-versions {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 12px 0;
}

.version-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.version-item {
  border-bottom: 1px solid var(--border-color);
}

.version-item:last-child {
  border-bottom: none;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 4px;
  cursor: pointer;
  user-select: none;
}

.version-header:hover {
  background: #f9fafb;
}

.version-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.current-tag {
  display: inline-block;
  margin-left: 6px;
  padding: 0 6px;
  border-radius: 4px;
  background: #dcfce7;
  color: #166534;
  font-size: 11px;
  font-weight: 500;
}

.version-time {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-left: auto;
}

.version-toggle {
  font-size: 12px;
  color: var(--blue);
  cursor: pointer;
}

.version-detail {
  padding: 8px 4px 12px;
}

.version-json {
  font-size: 12px;
  font-family: ui-monospace, 'Cascadia Code', 'Consolas', monospace;
  background: #f9fafb;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  padding: 12px;
  margin: 0;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}
</style>
