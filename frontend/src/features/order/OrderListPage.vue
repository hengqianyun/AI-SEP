<script setup lang="ts">
/**
 * 订单列表页 — 九组字段、Ant Pagination jumper+pageSize、查看进详情。
 * REQ-WSC-ORDER-007/008/009；RULE-GLOBAL-FRONTEND §3。
 * REQ-WSC-ORDER-008/009：列表/详情按角色脱敏（§2.2 OQ-V17-001）。
 */
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { Pagination } from 'ant-design-vue'
import { ORDER_STATUS_LABELS, type OrderStatus } from '@/api/orders'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useOrderList } from './composables/useOrderList'
import { maskByRole } from './utils/maskSensitive'

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const {
  state,
  error,
  items,
  total,
  page,
  pageSize,
  pageSizeOptions,
  onPageChange,
  onShowSizeChange,
  init,
} = useOrderList()

onMounted(() => {
  void init()
})

function goDetail(orderId: string) {
  void router.push(`/orders/${orderId}`)
}

function statusClass(status: OrderStatus): string {
  if (status === 'CONTRACT_REACHED') return 'status-success'
  if (status === 'CANCELLED') return 'status-cancelled'
  return 'status-pending'
}

function formatTime(val: string): string {
  if (!val) return '—'
  return val.replace('T', ' ').slice(0, 19)
}

/** 按角色对敏感字段脱敏（§2.2 假设） */
function maskValue(value: string | null | undefined): string {
  return maskByRole(value, role.value)
}
</script>

<template>
  <div class="order-list" data-testid="order-list">
    <header class="page-header wsc-card">
      <h1 class="page-title" data-testid="workbench-page-title">交易订单</h1>
    </header>

    <div class="list-body wsc-card">
      <div v-if="state === 'loading'" class="hint" data-testid="list-loading">加载中…</div>
      <div v-else-if="state === 'error'" class="hint error" data-testid="list-error">{{ error }}</div>
      <div v-else-if="!items.length" class="hint empty" data-testid="list-empty">暂无订单数据</div>

      <table v-else class="order-table" data-testid="order-table">
        <thead>
          <tr>
            <th data-testid="col-order-id">订单号</th>
            <th data-testid="col-product-name">产品名称</th>
            <th data-testid="col-product-code">产品编码</th>
            <th data-testid="col-product-type">产品类型</th>
            <th data-testid="col-source-platform">来源平台</th>
            <th data-testid="col-provider">提供方</th>
            <th data-testid="col-demand-user">订购人</th>
            <th data-testid="col-status">状态</th>
            <th data-testid="col-create-time">创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.orderId" :data-testid="`row-${item.orderId}`">
            <td class="col-id" data-testid="cell-order-id">{{ item.orderId }}</td>
            <td data-testid="cell-product-name">{{ item.productName }}</td>
            <td data-testid="cell-product-code">{{ item.productCode }}</td>
            <td data-testid="cell-product-type">{{ item.productType }}</td>
            <td data-testid="cell-source-platform">{{ item.sourcePlatform || '—' }}</td>
            <td data-testid="cell-provider">{{ maskValue(item.providerEnterpriseName) }}</td>
            <td data-testid="cell-demand-user">
              <span>{{ maskValue(item.demandUserName) }}</span>
              <span v-if="item.demandEnterpriseName" class="sub-text">
                {{ maskValue(item.demandEnterpriseName) }}
              </span>
            </td>
            <td>
              <span class="status-badge" :class="statusClass(item.status)" data-testid="cell-status">
                {{ item.statusLabel || ORDER_STATUS_LABELS[item.status] }}
              </span>
            </td>
            <td data-testid="cell-create-time">{{ formatTime(item.createTime) }}</td>
            <td>
              <button
                type="button"
                class="view-btn"
                :data-testid="`view-${item.orderId}`"
                @click="goDetail(item.orderId)"
              >
                查看
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="total > 0" class="pagination" data-testid="pagination">
        <span class="total">共 {{ total }} 条</span>
        <Pagination
          :current="page"
          :page-size="pageSize"
          :total="total"
          :show-quick-jumper="true"
          :show-size-changer="true"
          :page-size-options="pageSizeOptions"
          size="small"
          data-testid="a-pagination"
          @change="onPageChange"
          @showSizeChange="onShowSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 24px;
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

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.list-body {
  padding: 20px;
  min-height: 420px;
  overflow-x: auto;
}

.hint {
  color: var(--text-secondary);
  padding: 32px 0;
  text-align: center;
  font-size: 14px;
}

.hint.error {
  color: #b91c1c;
}

.hint.empty {
  color: var(--text-tertiary);
}

.order-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.order-table th {
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  white-space: nowrap;
  font-size: 12px;
}

.order-table td {
  padding: 12px;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-primary);
  vertical-align: middle;
}

.order-table tr:hover td {
  background: #f9fafb;
}

.col-id {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.sub-text {
  display: block;
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-success {
  background: #dcfce7;
  color: #166534;
}

.status-cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.view-btn {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: var(--blue);
  background: var(--blue-light);
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  cursor: pointer;
  font: inherit;
  transition: background 0.15s;
}

.view-btn:hover {
  background: #dbeafe;
}

.pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 16px;
  font-size: 13px;
}

.total {
  color: var(--text-secondary);
  margin-right: 8px;
}
</style>
