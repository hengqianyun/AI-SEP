/**
 * 订单详情 composable — 只读分区、快照、时间线、动作操作。
 * REQ-WSC-ORDER-001/009/010/011/012：详情展示、角色动作、取消二次确认。
 * REQ-WSC-ORDER-FE-001：写成功 toast。
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import {
  getOrder,
  confirmOrder,
  cancelOrder,
  type OrderDetail,
  type OrderStatus,
} from '@/api/orders'
import { useAuthStore } from '@/features/auth/store/authStore'
import type { Role } from '@/api/auth'
import {
  showConfirmOrderToast,
  showCancelOrderToast,
} from '../utils/toast'

export type OrderDetailState = 'idle' | 'loading' | 'error'

/** 子视图模式：详情、合约确认、合约上传 */
export type SubView = 'detail' | 'confirm' | 'upload'

/** 判断当前角色是否可对指定状态的订单执行某动作 */
export function canConfirmOrder(status: OrderStatus, role: Role): boolean {
  if (status !== 'PENDING_CONFIRM') return false
  return role === 'PROVIDER' || role === 'ADMIN'
}

export function canUploadContract(status: OrderStatus, role: Role): boolean {
  if (status !== 'PENDING_UPLOAD') return false
  return role === 'USER'
}

export function canConfirmContract(status: OrderStatus, role: Role): boolean {
  if (status !== 'PENDING_CONTRACT_CONFIRM') return false
  return role === 'PROVIDER' || role === 'ADMIN'
}

export function canCancel(status: OrderStatus): boolean {
  return status === 'PENDING_CONFIRM'
    || status === 'PENDING_UPLOAD'
    || status === 'PENDING_CONTRACT_CONFIRM'
}

/** 合约已达成后无支付/交付主按钮 */
export function isTerminal(status: OrderStatus): boolean {
  return status === 'CONTRACT_REACHED' || status === 'CANCELLED'
}

export function useOrderDetail() {
  const route = useRoute()
  const auth = useAuthStore()
  const { session, role } = storeToRefs(auth)

  const orderId = computed(() => String(route.params.orderId || ''))

  const order = ref<OrderDetail | null>(null)
  const state = ref<OrderDetailState>('idle')
  const error = ref<string | null>(null)

  /** 当前子视图 */
  const subView = ref<SubView>('detail')

  /** 动作操作中的 loading 状态 */
  const actionLoading = ref(false)

  /** 取消确认对话框是否可见 */
  const cancelDialogVisible = ref(false)

  /** ADMIN 代操作标识 */
  const isProxyAction = computed(() => role.value === 'ADMIN')

  /** PROVIDER 仅操作本企业订单 */
  const isOwnEnterprise = computed(() => {
    if (!order.value || !session.value?.enterpriseId) return false
    return order.value.participant.providerEnterpriseId === session.value.enterpriseId
  })

  /** 角色按钮可见性矩阵 */
  const actions = computed(() => {
    const s = order.value?.status
    const r = role.value
    if (!s || !r) return { confirmOrder: false, uploadContract: false, confirmContract: false, cancel: false }
    return {
      confirmOrder: canConfirmOrder(s, r) && (r === 'ADMIN' || isOwnEnterprise.value),
      uploadContract: canUploadContract(s, r),
      confirmContract: canConfirmContract(s, r) && (r === 'ADMIN' || isOwnEnterprise.value),
      cancel: canCancel(s),
    }
  })

  async function load() {
    if (!orderId.value) return
    state.value = 'loading'
    error.value = null
    try {
      const res = await getOrder(orderId.value)
      order.value = res.data
      state.value = 'idle'
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '加载失败'
    }
  }

  /** 确认订单 */
  async function handleConfirmOrder() {
    if (!orderId.value || !actions.value.confirmOrder || actionLoading.value) return
    actionLoading.value = true
    error.value = null
    try {
      await confirmOrder(orderId.value)
      showConfirmOrderToast()
      await load()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '确认失败'
    } finally {
      actionLoading.value = false
    }
  }

  /** 打开取消确认对话框 */
  function openCancelDialog() {
    cancelDialogVisible.value = true
  }

  /** 关闭取消确认对话框 */
  function closeCancelDialog() {
    cancelDialogVisible.value = false
  }

  /** 确认取消订单（须通过对话框二次确认） */
  async function handleCancelOrder() {
    if (!orderId.value || !actions.value.cancel || actionLoading.value) return
    actionLoading.value = true
    error.value = null
    try {
      await cancelOrder(orderId.value)
      cancelDialogVisible.value = false
      showCancelOrderToast()
      await load()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '取消失败'
    } finally {
      actionLoading.value = false
    }
  }

  /** 切换到合约确认子视图 */
  function goToContractConfirm() {
    subView.value = 'confirm'
  }

  /** 切换到合约上传子视图 */
  function goToContractUpload() {
    subView.value = 'upload'
  }

  /** 返回详情子视图 */
  function goBackToDetail() {
    subView.value = 'detail'
    void load()
  }

  onMounted(() => {
    void load()
  })

  return {
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
    load,
    handleConfirmOrder,
    openCancelDialog,
    closeCancelDialog,
    handleCancelOrder,
    goToContractConfirm,
    goToContractUpload,
    goBackToDetail,
  }
}
