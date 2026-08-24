/**
 * 订购页 composable — 产品信息展示、须知勾选、备注、提交。
 * REQ-WSC-ORDER-002/003/004/006：订购入口、须知、链内创建。
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { getProduct, type Product } from '@/api/catalog'
import { useAuthStore } from '@/features/auth/store/authStore'
import { createOrder, getCurrentNotice, type OrderNotice } from '@/api/orders'
import { showCreateSuccessToast } from '../utils/toast'

export type SubscribeState = 'idle' | 'loading' | 'submitting' | 'success' | 'error'

export function useOrderSubscribe() {
  const route = useRoute()
  const auth = useAuthStore()
  const { session } = storeToRefs(auth)

  const productId = computed(() => String(route.params.productId || ''))

  const product = ref<Product | null>(null)
  const notice = ref<OrderNotice | null>(null)
  const remark = ref('')
  const noticeAccepted = ref(false)
  const state = ref<SubscribeState>('idle')
  const error = ref<string | null>(null)

  /** 当前订购人信息 */
  const demandUserName = computed(() => session.value?.displayName ?? '')
  const demandEnterpriseName = computed(() => session.value?.enterpriseName ?? '')

  /** 提交按钮是否可用 */
  const canSubmit = computed(() => {
    if (!noticeAccepted.value) return false
    if (!remark.value.trim()) return false
    if (state.value === 'submitting') return false
    return true
  })

  async function load() {
    if (!productId.value) return
    state.value = 'loading'
    error.value = null
    try {
      const [productRes, noticeRes] = await Promise.all([
        getProduct(productId.value),
        getCurrentNotice(),
      ])
      product.value = productRes.data
      notice.value = noticeRes.data
      state.value = 'idle'
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '加载失败'
    }
  }

  async function submit() {
    if (!canSubmit.value) return
    state.value = 'submitting'
    error.value = null
    try {
      await createOrder({
        productId: productId.value,
        remark: remark.value.trim(),
        noticeAccepted: noticeAccepted.value,
      })
      state.value = 'success'
      showCreateSuccessToast()
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '创建失败'
      // 失败不出现成功 toast
    }
  }

  onMounted(() => {
    void load()
  })

  return {
    productId,
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
  }
}
