/**
 * TASK-WSC-915 订购页测试：须知未勾选不发 POST；备注空不发；创建成功调用 toast 一次；失败 0 次。
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'

const createOrder = vi.fn()
const getCurrentNotice = vi.fn()
const getProduct = vi.fn()
const showCreateSuccessToast = vi.fn()

vi.mock('@/api/orders', () => ({
  createOrder: (...args: unknown[]) => createOrder(...args),
  getCurrentNotice: (...args: unknown[]) => getCurrentNotice(...args),
}))

vi.mock('@/api/catalog', () => ({
  getProduct: (...args: unknown[]) => getProduct(...args),
}))

vi.mock('../utils/toast', () => ({
  showCreateSuccessToast: (...args: unknown[]) => showCreateSuccessToast(...args),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { productId: 'prod-1' },
  }),
  useRouter: () => ({ push: vi.fn(), back: vi.fn() }),
}))

vi.mock('pinia', () => ({
  storeToRefs: () => ({
    session: { value: { displayName: '张三', enterpriseName: '测试企业' } },
    role: { value: 'USER' },
  }),
}))

vi.mock('@/features/auth/store/authStore', () => ({
  useAuthStore: () => ({}),
}))

function mockProduct() {
  getProduct.mockResolvedValue({
    data: {
      id: 'prod-1',
      productName: '测试产品',
      productCode: 'TP-001',
      productType: 'DATASET',
      supplierName: '供应商A',
      dataSource: '平台X',
      deliveryMethod: 'API接口',
      chainCount: 0,
    },
  })
}

function mockNotice() {
  getCurrentNotice.mockResolvedValue({
    data: { content: '请遵守平台数据使用规范', version: '1.0' },
  })
}

describe('915-subscribe-notice', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockProduct()
    mockNotice()
    createOrder.mockResolvedValue({ data: { orderId: 'ORD-001' } })
  })

  it('notice not accepted → submit button disabled, no POST', async () => {
    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()

    // wait for onMounted load
    await nextTick()
    await vi.dynamicImportSettled()

    // set remark but NOT noticeAccepted
    sub.remark.value = '备注内容'
    sub.noticeAccepted.value = false

    expect(sub.canSubmit.value).toBe(false)

    await sub.submit()
    expect(createOrder).not.toHaveBeenCalled()
  })

  it('remark empty → submit button disabled, no POST', async () => {
    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()

    await nextTick()
    await vi.dynamicImportSettled()

    sub.noticeAccepted.value = true
    sub.remark.value = ''

    expect(sub.canSubmit.value).toBe(false)

    await sub.submit()
    expect(createOrder).not.toHaveBeenCalled()
  })

  it('success → toast called once', async () => {
    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()

    await nextTick()
    await vi.dynamicImportSettled()

    sub.noticeAccepted.value = true
    sub.remark.value = '测试备注'

    await sub.submit()

    expect(createOrder).toHaveBeenCalledTimes(1)
    expect(createOrder).toHaveBeenCalledWith({
      productId: 'prod-1',
      remark: '测试备注',
      noticeAccepted: true,
    })
    expect(showCreateSuccessToast).toHaveBeenCalledTimes(1)
    expect(sub.state.value).toBe('success')
  })

  it('failure → toast NOT called', async () => {
    createOrder.mockRejectedValueOnce(new Error('创建失败'))

    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()

    await nextTick()
    await vi.dynamicImportSettled()

    sub.noticeAccepted.value = true
    sub.remark.value = '测试备注'

    await sub.submit()

    expect(showCreateSuccessToast).not.toHaveBeenCalled()
    expect(sub.state.value).toBe('error')
    expect(sub.error.value).toBe('创建失败')
  })
})
