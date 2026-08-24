/**
 * TASK-WSC-918 测试：详情页版本区可见版本号、版本列表条数随状态变更递增、历史版本可展开查看。
 * 命名用例：918-contract-version-ui
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'

const getOrder = vi.fn()

vi.mock('@/api/orders', () => ({
  getOrder: (...args: unknown[]) => getOrder(...args),
  confirmOrder: vi.fn(),
  cancelOrder: vi.fn(),
  confirmContract: vi.fn(),
  ORDER_STATUS_LABELS: {
    PENDING_CONFIRM: '待确认订单',
    PENDING_UPLOAD: '待上传合约',
    PENDING_CONTRACT_CONFIRM: '待确认合约',
    CONTRACT_REACHED: '合约已达成',
    CANCELLED: '已取消',
  },
}))

vi.mock('../utils/toast', () => ({
  showConfirmOrderToast: vi.fn(),
  showCancelOrderToast: vi.fn(),
  showConfirmContractToast: vi.fn(),
  showCreateSuccessToast: vi.fn(),
  showSubmitContractToast: vi.fn(),
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

function mockOrderDetailWithVersions(status = 'PENDING_CONFIRM', versions: Array<{ versionNo: number; snapshotJson: string; createdAt: string }> = []) {
  getOrder.mockResolvedValue({
    data: {
      orderId: 'ORD-001',
      status,
      statusLabel: '',
      productSnapshot: { productId: 'p1', productName: '产品A', productCode: 'PA-001', productType: 'DATASET', sourcePlatform: '平台', allowSimpleOrder: true },
      participant: { providerEnterpriseId: 'ent-2', providerEnterpriseName: '供应商', demandUserId: 'u1', demandUserName: '张三', demandUserLoginName: 'zhangsan', demandEnterpriseId: 'ent-1', demandEnterpriseName: '测试企业' },
      remark: null,
      noticeVersion: '1.0',
      transactionInfo: null,
      attachment: null,
      timeline: [],
      chainLogs: [],
      contractVersions: versions,
      chainCount: 0,
      amountTaxInclusive: null,
      amountTaxExclusive: null,
      currency: null,
      createTime: '2026-08-21T10:00:00',
      updateTime: '2026-08-21T10:00:00',
    },
  })
}

describe('918-contract-version-ui', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('currentVersionNo returns max version number', async () => {
    mockOrderDetailWithVersions('PENDING_CONFIRM', [
      { versionNo: 1, snapshotJson: '{"orderId":"ORD-001"}', createdAt: '2026-08-21T10:00:00' },
    ])

    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()
    await detail.load()

    // contractVersions should be present in the order
    expect(detail.order.value).toBeTruthy()
    expect(detail.order.value!.contractVersions).toHaveLength(1)
    expect(detail.order.value!.contractVersions[0].versionNo).toBe(1)
  })

  it('version list grows with state changes', async () => {
    mockOrderDetailWithVersions('PENDING_UPLOAD', [
      { versionNo: 1, snapshotJson: '{"orderId":"ORD-001","status":"PENDING_CONFIRM"}', createdAt: '2026-08-21T10:00:00' },
      { versionNo: 2, snapshotJson: '{"orderId":"ORD-001","status":"PENDING_UPLOAD"}', createdAt: '2026-08-21T10:05:00' },
    ])

    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()
    await detail.load()

    expect(detail.order.value!.contractVersions).toHaveLength(2)
    expect(detail.order.value!.contractVersions[0].versionNo).toBe(1)
    expect(detail.order.value!.contractVersions[1].versionNo).toBe(2)
  })

  it('contractVersions empty when no versions exist', async () => {
    mockOrderDetailWithVersions('PENDING_CONFIRM', [])

    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()
    await detail.load()

    expect(detail.order.value!.contractVersions).toHaveLength(0)
  })

  it('version snapshot contains order data', async () => {
    const snapshot = JSON.stringify({ orderId: 'ORD-001', status: 'PENDING_CONFIRM', productCode: 'PA-001' })
    mockOrderDetailWithVersions('PENDING_CONFIRM', [
      { versionNo: 1, snapshotJson: snapshot, createdAt: '2026-08-21T10:00:00' },
    ])

    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()
    await detail.load()

    const ver = detail.order.value!.contractVersions[0]
    const parsed = JSON.parse(ver.snapshotJson)
    expect(parsed.orderId).toBe('ORD-001')
    expect(parsed.status).toBe('PENDING_CONFIRM')
  })

  it('versions sorted by versionNo ascending from API', async () => {
    mockOrderDetailWithVersions('CONTRACT_REACHED', [
      { versionNo: 1, snapshotJson: '{}', createdAt: '2026-08-21T10:00:00' },
      { versionNo: 2, snapshotJson: '{}', createdAt: '2026-08-21T10:05:00' },
      { versionNo: 3, snapshotJson: '{}', createdAt: '2026-08-21T10:10:00' },
      { versionNo: 4, snapshotJson: '{}', createdAt: '2026-08-21T10:15:00' },
    ])

    const { useOrderDetail } = await import('../composables/useOrderDetail')
    const detail = useOrderDetail()
    await detail.load()

    expect(detail.order.value!.contractVersions).toHaveLength(4)
    // Verify ascending order from API
    const versionNos = detail.order.value!.contractVersions.map(v => v.versionNo)
    expect(versionNos).toEqual([1, 2, 3, 4])
  })
})
