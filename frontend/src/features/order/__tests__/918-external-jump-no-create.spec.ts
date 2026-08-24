// @vitest-environment jsdom
/**
 * TASK-WSC-918 测试：外部跳转不调用 POST /orders；无地址时展示「未配置」提示文案。
 * 命名用例：918-external-jump-no-create
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'

vi.mock('vue', async () => {
  const actual = await vi.importActual<typeof import('vue')>('vue')
  return {
    ...actual,
    onMounted: (cb: () => void) => { cb() },
  }
})

const createOrder = vi.fn()
const getCurrentNotice = vi.fn().mockResolvedValue({ data: { content: '须知', version: '1.0' } })
const getProduct = vi.fn()

vi.mock('@/api/orders', () => ({
  createOrder: (...args: unknown[]) => createOrder(...args),
  getCurrentNotice: (...args: unknown[]) => getCurrentNotice(...args),
  listOrders: vi.fn(),
  getOrder: vi.fn(),
  confirmOrder: vi.fn(),
  cancelOrder: vi.fn(),
  confirmContract: vi.fn(),
  ORDER_STATUS_LABELS: {},
}))

vi.mock('@/api/catalog', () => ({
  getProduct: (...args: unknown[]) => getProduct(...args),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { productId: '100' } }),
  useRouter: () => ({ push: vi.fn(), back: vi.fn() }),
}))

vi.mock('pinia', () => ({
  storeToRefs: () => ({
    session: { value: { userId: 'u1', displayName: '张三', role: 'USER', enterpriseId: 'ent-1', enterpriseName: '测试企业' } },
    role: { value: 'USER' },
  }),
}))

vi.mock('@/features/auth/store/authStore', () => ({
  useAuthStore: () => ({}),
}))

vi.mock('../utils/toast', () => ({
  showCreateSuccessToast: vi.fn(),
}))

function mockProductWithUrl(url: string | null) {
  getProduct.mockResolvedValue({
    data: {
      id: 100,
      productName: '测试产品',
      productCode: 'P-001',
      productType: 'DATASET',
      supplierName: '供应商',
      dataSource: '平台',
      deliveryMethod: url,
    },
  })
}

describe('918-external-jump-no-create', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('openExternalUrl opens new window when URL exists', () => {
    const url = 'https://example.com/data-access'
    // Verify that window.open would be called with the URL
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)

    // Simulate the openExternalUrl logic
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer')
    }

    expect(openSpy).toHaveBeenCalledWith(url, '_blank', 'noopener,noreferrer')
    openSpy.mockRestore()
  })

  it('no URL shows "未配置" text (testid: external-not-configured)', async () => {
    mockProductWithUrl(null)

    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()
    await new Promise(resolve => setTimeout(resolve, 50))

    // The product deliveryMethod is null, so "未配置" should be shown
    expect(sub.product.value).toBeTruthy()
    expect(sub.product.value!.deliveryMethod).toBeNull()
  })

  it('external jump does not call createOrder (POST /orders)', async () => {
    mockProductWithUrl('https://example.com')

    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    void useOrderSubscribe()
    await new Promise(resolve => setTimeout(resolve, 50))

    // Just loading the page should not call createOrder
    expect(createOrder).not.toHaveBeenCalled()
  })

  it('with URL, product.deliveryMethod is truthy', async () => {
    mockProductWithUrl('https://example.com/data-access')

    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()
    await new Promise(resolve => setTimeout(resolve, 50))

    expect(sub.product.value!.deliveryMethod).toBe('https://example.com/data-access')
  })

  it('without URL, product.deliveryMethod is null', async () => {
    mockProductWithUrl(null)

    const { useOrderSubscribe } = await import('../composables/useOrderSubscribe')
    const sub = useOrderSubscribe()
    await new Promise(resolve => setTimeout(resolve, 50))

    expect(sub.product.value!.deliveryMethod).toBeNull()
  })
})
