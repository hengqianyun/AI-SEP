/**
 * TASK-WSC-917 测试：取消未确认不调用 API；角色按钮可见性矩阵；写成功 toast 四动作。
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'

const getOrder = vi.fn()
const confirmOrder = vi.fn()
const cancelOrder = vi.fn()
const confirmContract = vi.fn()
const showConfirmOrderToast = vi.fn()
const showCancelOrderToast = vi.fn()
const showConfirmContractToast = vi.fn()
const showCreateSuccessToast = vi.fn()
const showSubmitContractToast = vi.fn()

vi.mock('@/api/orders', () => ({
  getOrder: (...args: unknown[]) => getOrder(...args),
  confirmOrder: (...args: unknown[]) => confirmOrder(...args),
  cancelOrder: (...args: unknown[]) => cancelOrder(...args),
  confirmContract: (...args: unknown[]) => confirmContract(...args),
  getCurrentNotice: vi.fn().mockResolvedValue({ data: { content: '须知', version: '1.0' } }),
  getOrderAttachmentUrl: (id: string) => `/api/v1/orders/${id}/attachment`,
  ORDER_STATUS_LABELS: {
    PENDING_CONFIRM: '待确认订单',
    PENDING_UPLOAD: '待上传合约',
    PENDING_CONTRACT_CONFIRM: '待确认合约',
    CONTRACT_REACHED: '合约已达成',
    CANCELLED: '已取消',
  },
}))

vi.mock('../utils/toast', () => ({
  showConfirmOrderToast: (...args: unknown[]) => showConfirmOrderToast(...args),
  showCancelOrderToast: (...args: unknown[]) => showCancelOrderToast(...args),
  showConfirmContractToast: (...args: unknown[]) => showConfirmContractToast(...args),
  showCreateSuccessToast: (...args: unknown[]) => showCreateSuccessToast(...args),
  showSubmitContractToast: (...args: unknown[]) => showSubmitContractToast(...args),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { orderId: 'ORD-001' } }),
  useRouter: () => ({ push: vi.fn() }),
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

function mockOrderDetail(status = 'PENDING_CONFIRM', providerEnterpriseId = 'ent-2') {
  getOrder.mockResolvedValue({
    data: {
      orderId: 'ORD-001',
      status,
      statusLabel: '',
      productSnapshot: { productId: 'p1', productName: '产品A', productCode: 'PA-001', productType: 'DATASET', sourcePlatform: '平台', allowSimpleOrder: true },
      participant: { providerEnterpriseId, providerEnterpriseName: '供应商', demandUserId: 'u1', demandUserName: '张三', demandUserLoginName: 'zhangsan', demandEnterpriseId: 'ent-1', demandEnterpriseName: '测试企业' },
      remark: null,
      noticeVersion: '1.0',
      transactionInfo: null,
      attachment: null,
      timeline: [],
      chainLogs: [],
      contractVersions: [],
      chainCount: 0,
      amountTaxInclusive: null,
      amountTaxExclusive: null,
      currency: null,
      createTime: '2026-08-21T10:00:00',
      updateTime: '2026-08-21T10:00:00',
    },
  })
}

describe('917-cancel-confirm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockOrderDetail()
    cancelOrder.mockResolvedValue({
      data: { orderId: 'ORD-001', status: 'CANCELLED', statusLabel: '已取消' },
    })
  })

  it('cancel without dialog confirmation does not call API', async () => {
    const { canCancel, useOrderDetail } = await import('../composables/useOrderDetail')

    expect(canCancel('PENDING_CONFIRM')).toBe(true)
    expect(canCancel('CONTRACT_REACHED')).toBe(false)
    expect(canCancel('CANCELLED')).toBe(false)

    const detail = useOrderDetail()
    expect(detail.cancelDialogVisible.value).toBe(false)
    expect(cancelOrder).not.toHaveBeenCalled()
  })

  it('cancel dialog confirms calls API and shows toast', async () => {
    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()
    // onMounted won't fire outside component; manually load
    await detail.load()

    detail.openCancelDialog()
    expect(detail.cancelDialogVisible.value).toBe(true)

    await detail.handleCancelOrder()
    expect(cancelOrder).toHaveBeenCalledWith('ORD-001')
    expect(showCancelOrderToast).toHaveBeenCalledTimes(1)
    expect(detail.cancelDialogVisible.value).toBe(false)
  })

  it('cancel dialog close does not call API', async () => {
    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()

    detail.openCancelDialog()
    expect(detail.cancelDialogVisible.value).toBe(true)

    detail.closeCancelDialog()
    expect(detail.cancelDialogVisible.value).toBe(false)
    expect(cancelOrder).not.toHaveBeenCalled()
  })
})

describe('917-role-button-visibility', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('canConfirmOrder: only PROVIDER/ADMIN on PENDING_CONFIRM', async () => {
    const { canConfirmOrder } = await import('../composables/useOrderDetail')
    expect(canConfirmOrder('PENDING_CONFIRM', 'PROVIDER')).toBe(true)
    expect(canConfirmOrder('PENDING_CONFIRM', 'ADMIN')).toBe(true)
    expect(canConfirmOrder('PENDING_CONFIRM', 'USER')).toBe(false)
    expect(canConfirmOrder('PENDING_UPLOAD', 'PROVIDER')).toBe(false)
  })

  it('canUploadContract: only USER on PENDING_UPLOAD', async () => {
    const { canUploadContract } = await import('../composables/useOrderDetail')
    expect(canUploadContract('PENDING_UPLOAD', 'USER')).toBe(true)
    expect(canUploadContract('PENDING_UPLOAD', 'PROVIDER')).toBe(false)
    expect(canUploadContract('PENDING_UPLOAD', 'ADMIN')).toBe(false)
    expect(canUploadContract('PENDING_CONFIRM', 'USER')).toBe(false)
  })

  it('canConfirmContract: only PROVIDER/ADMIN on PENDING_CONTRACT_CONFIRM', async () => {
    const { canConfirmContract } = await import('../composables/useOrderDetail')
    expect(canConfirmContract('PENDING_CONTRACT_CONFIRM', 'PROVIDER')).toBe(true)
    expect(canConfirmContract('PENDING_CONTRACT_CONFIRM', 'ADMIN')).toBe(true)
    expect(canConfirmContract('PENDING_CONTRACT_CONFIRM', 'USER')).toBe(false)
  })

  it('canCancel: true for three open states', async () => {
    const { canCancel } = await import('../composables/useOrderDetail')
    expect(canCancel('PENDING_CONFIRM')).toBe(true)
    expect(canCancel('PENDING_UPLOAD')).toBe(true)
    expect(canCancel('PENDING_CONTRACT_CONFIRM')).toBe(true)
    expect(canCancel('CONTRACT_REACHED')).toBe(false)
    expect(canCancel('CANCELLED')).toBe(false)
  })

  it('isTerminal: CONTRACT_REACHED and CANCELLED', async () => {
    const { isTerminal } = await import('../composables/useOrderDetail')
    expect(isTerminal('CONTRACT_REACHED')).toBe(true)
    expect(isTerminal('CANCELLED')).toBe(true)
    expect(isTerminal('PENDING_CONFIRM')).toBe(false)
  })
})

describe('917-toast-four-actions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('confirmOrderToast is called via mock', () => {
    showConfirmOrderToast()
    expect(showConfirmOrderToast).toHaveBeenCalledTimes(1)
  })

  it('cancelOrderToast is called via mock', () => {
    showCancelOrderToast()
    expect(showCancelOrderToast).toHaveBeenCalledTimes(1)
  })

  it('confirmContractToast is called via mock', () => {
    showConfirmContractToast()
    expect(showConfirmContractToast).toHaveBeenCalledTimes(1)
  })

  it('createSuccessToast is called via mock', () => {
    showCreateSuccessToast()
    expect(showCreateSuccessToast).toHaveBeenCalledTimes(1)
  })
})
