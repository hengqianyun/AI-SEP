/**
 * 统一合约确认页 composable — 交易信息、平台统一须知、签署附件、确认合约。
 * REQ-WSC-ORDER-013/015/FE-001：统一确认页、须知勾选、确认成功 toast。
 */
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import {
  getOrder,
  getCurrentNotice,
  confirmContract,
  getOrderAttachmentUrl,
  type OrderDetail,
  type OrderNotice,
} from '@/api/orders'
import { useAuthStore } from '@/features/auth/store/authStore'
import { showConfirmContractToast } from '../utils/toast'

export type ConfirmState = 'idle' | 'loading' | 'submitting' | 'error'

export function useContractConfirm(orderId: string, onDone: () => void) {
  const auth = useAuthStore()
  const { role } = storeToRefs(auth)

  const order = ref<OrderDetail | null>(null)
  const notice = ref<OrderNotice | null>(null)
  const noticeAccepted = ref(false)
  const state = ref<ConfirmState>('idle')
  const error = ref<string | null>(null)

  /** ADMIN 代 PROVIDER 确认时的操作人身份标识 */
  const isProxyAction = computed(() => role.value === 'ADMIN')

  /** 确认按钮是否可用：须知勾选 + 非提交中 */
  const canSubmit = computed(() => {
    if (!noticeAccepted.value) return false
    if (state.value === 'submitting') return false
    return true
  })

  /** 附件下载地址 */
  const attachmentUrl = computed(() => {
    if (!orderId) return ''
    return getOrderAttachmentUrl(orderId)
  })

  /** 附件文件名 */
  const attachmentFileName = computed(() => {
    return order.value?.attachment?.fileName || '签署附件'
  })

  async function load() {
    if (!orderId) return
    state.value = 'loading'
    error.value = null
    try {
      const [orderRes, noticeRes] = await Promise.all([
        getOrder(orderId),
        getCurrentNotice(),
      ])
      order.value = orderRes.data
      notice.value = noticeRes.data
      state.value = 'idle'
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '加载失败'
    }
  }

  /** 确认合约 */
  async function handleSubmit() {
    if (!canSubmit.value || !orderId) return
    state.value = 'submitting'
    error.value = null
    try {
      await confirmContract(orderId, { noticeAccepted: noticeAccepted.value })
      showConfirmContractToast()
      onDone()
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '确认失败'
    }
  }

  function goBack() {
    onDone()
  }

  onMounted(() => {
    void load()
  })

  return {
    order,
    notice,
    noticeAccepted,
    state,
    error,
    isProxyAction,
    canSubmit,
    attachmentUrl,
    attachmentFileName,
    load,
    handleSubmit,
    goBack,
  }
}
