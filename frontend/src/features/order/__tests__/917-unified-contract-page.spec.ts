/**
 * TASK-WSC-917 测试：统一确认未勾选不发请求（button disabled 断言）；ADMIN 代确认身份标识。
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'

const getOrder = vi.fn()
const getCurrentNotice = vi.fn()
const confirmContract = vi.fn()

vi.mock('@/api/orders', () => ({
  getOrder: (...args: unknown[]) => getOrder(...args),
  getCurrentNotice: (...args: unknown[]) => getCurrentNotice(...args),
  confirmContract: (...args: unknown[]) => confirmContract(...args),
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
  showConfirmContractToast: vi.fn(),
  showCreateSuccessToast: vi.fn(),
  showConfirmOrderToast: vi.fn(),
  showCancelOrderToast: vi.fn(),
  showSubmitContractToast: vi.fn(),
}))

vi.mock('pinia', () => ({
  storeToRefs: () => ({
    role: { value: 'PROVIDER' },
  }),
}))

vi.mock('@/features/auth/store/authStore', () => ({
  useAuthStore: () => ({}),
}))

function mockOrderData() {
  getOrder.mockResolvedValue({
    data: {
      orderId: 'ORD-001',
      status: 'PENDING_CONTRACT_CONFIRM',
      statusLabel: '待确认合约',
      productSnapshot: { productId: 'p1', productName: '产品A', productCode: 'PA-001', productType: 'DATASET', sourcePlatform: '平台', allowSimpleOrder: true },
      participant: { providerEnterpriseId: 'ent-1', providerEnterpriseName: '供应商', demandUserId: 'u1', demandUserName: '张三', demandUserLoginName: 'zhangsan', demandEnterpriseId: 'ent-2', demandEnterpriseName: '需求方' },
      remark: null,
      noticeVersion: '1.0',
      transactionInfo: {
        orderLines: [{ unit: '次', quantity: 10, unitPrice: '100.00', subtotal: '1000.00' }],
        amountTaxInclusive: '1000.00',
        amountTaxExclusive: '884.96',
        currency: 'CNY',
      },
      attachment: { fileName: '合约.pdf', fileType: 'application/pdf', fileSize: 102400, scanResult: 'PASS' },
      timeline: [],
      chainLogs: [],
      contractVersions: [],
      chainCount: 1,
      amountTaxInclusive: '1000.00',
      amountTaxExclusive: '884.96',
      currency: 'CNY',
      createTime: '2026-08-21T10:00:00',
      updateTime: '2026-08-21T10:00:00',
    },
  })
  getCurrentNotice.mockResolvedValue({
    data: { content: '平台数据使用须知', version: '1.0' },
  })
}

describe('917-unified-contract-page', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockOrderData()
    confirmContract.mockResolvedValue({
      data: { orderId: 'ORD-001', status: 'CONTRACT_REACHED', statusLabel: '合约已达成' },
    })
  })

  it('unchecked → canSubmit is false (button disabled)', async () => {
    const { useContractConfirm } = await import('../composables/useContractConfirm')
    const confirm = useContractConfirm('ORD-001', vi.fn())
    await vi.dynamicImportSettled()

    // noticeAccepted defaults to false
    expect(confirm.noticeAccepted.value).toBe(false)
    expect(confirm.canSubmit.value).toBe(false)
  })

  it('checked → canSubmit is true', async () => {
    const { useContractConfirm } = await import('../composables/useContractConfirm')
    const confirm = useContractConfirm('ORD-001', vi.fn())
    await vi.dynamicImportSettled()

    confirm.noticeAccepted.value = true
    expect(confirm.canSubmit.value).toBe(true)
  })

  it('unchecked → submit does not call API', async () => {
    const onDone = vi.fn()
    const { useContractConfirm } = await import('../composables/useContractConfirm')
    const confirm = useContractConfirm('ORD-001', onDone)
    await vi.dynamicImportSettled()

    // Try to submit without checking
    await confirm.handleSubmit()

    expect(confirmContract).not.toHaveBeenCalled()
    expect(onDone).not.toHaveBeenCalled()
  })

  it('checked → submit calls API and triggers onDone', async () => {
    const onDone = vi.fn()
    const { useContractConfirm } = await import('../composables/useContractConfirm')
    const confirm = useContractConfirm('ORD-001', onDone)
    await vi.dynamicImportSettled()

    confirm.noticeAccepted.value = true
    await confirm.handleSubmit()

    expect(confirmContract).toHaveBeenCalledWith('ORD-001', { noticeAccepted: true })
    expect(onDone).toHaveBeenCalledTimes(1)
  })

  it('ADMIN proxy identity is computed from role', async () => {
    // isProxyAction is a computed that checks role.value === 'ADMIN'
    // In this test file, role is mocked as 'PROVIDER' → isProxyAction is false
    // The ADMIN mock is in 917-cancel-dialog-content.spec.ts which has role: 'ADMIN'
    const { useContractConfirm } = await import('../composables/useContractConfirm')
    const confirm = useContractConfirm('ORD-001', vi.fn())
    await vi.dynamicImportSettled()

    // With PROVIDER role, isProxyAction should be false
    expect(confirm.isProxyAction.value).toBe(false)
  })
})
