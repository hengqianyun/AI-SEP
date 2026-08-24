/**
 * TASK-WSC-918 测试：USER/PROVIDER/ADMIN 脱敏差异（可 mock）。
 * 命名用例：918-contract-versions（脱敏部分）
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'

vi.mock('@/api/orders', () => ({
  getOrder: vi.fn(),
  listOrders: vi.fn(),
  ORDER_STATUS_LABELS: {},
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

vi.mock('../utils/toast', () => ({
  showConfirmOrderToast: vi.fn(),
  showCancelOrderToast: vi.fn(),
  showConfirmContractToast: vi.fn(),
  showCreateSuccessToast: vi.fn(),
  showSubmitContractToast: vi.fn(),
}))

describe('918-masking-by-role', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('truncateSensitiveId', () => {
    it('returns full value when length <= head + tail + 1', async () => {
      const { truncateSensitiveId } = await import('../utils/maskSensitive')
      expect(truncateSensitiveId('short', 10, 6)).toBe('short')
      expect(truncateSensitiveId('12345678901234567', 10, 6)).toBe('12345678901234567')
    })

    it('truncates long values with ellipsis', async () => {
      const { truncateSensitiveId } = await import('../utils/maskSensitive')
      expect(truncateSensitiveId('abcdefghijklmnopqrst', 10, 6)).toBe('abcdefghij…opqrst')
    })

    it('handles null/undefined', async () => {
      const { truncateSensitiveId } = await import('../utils/maskSensitive')
      expect(truncateSensitiveId(null)).toBe('—')
      expect(truncateSensitiveId(undefined)).toBe('—')
      expect(truncateSensitiveId('')).toBe('')
    })

    it('custom head/tail parameters', async () => {
      const { truncateSensitiveId } = await import('../utils/maskSensitive')
      expect(truncateSensitiveId('abcdefghijklmnopqrst', 5, 3)).toBe('abcde…rst')
    })
  })

  describe('maskByRole', () => {
    it('ADMIN sees full value', async () => {
      const { maskByRole } = await import('../utils/maskSensitive')
      expect(maskByRole('很长的企业名称测试', 'ADMIN')).toBe('很长的企业名称测试')
    })

    it('USER gets truncated value', async () => {
      const { maskByRole } = await import('../utils/maskSensitive')
      const longValue = '这是一个很长的用户名用于测试脱敏效果'
      const result = maskByRole(longValue, 'USER')
      expect(result).toContain('…')
      expect(result.length).toBeLessThan(longValue.length)
    })

    it('PROVIDER gets truncated value', async () => {
      const { maskByRole } = await import('../utils/maskSensitive')
      const longValue = '这是一个很长的企业信用代码用于测试脱敏截断效果是否正确处理'
      const result = maskByRole(longValue, 'PROVIDER')
      expect(result).toContain('…')
    })

    it('null/undefined returns "—"', async () => {
      const { maskByRole } = await import('../utils/maskSensitive')
      expect(maskByRole(null, 'USER')).toBe('—')
      expect(maskByRole(undefined, 'PROVIDER')).toBe('—')
    })

    it('short value not truncated even for USER', async () => {
      const { maskByRole } = await import('../utils/maskSensitive')
      expect(maskByRole('短名', 'USER')).toBe('短名')
    })
  })

  describe('maskOrderFields', () => {
    it('ADMIN fields unchanged', async () => {
      const { maskOrderFields } = await import('../utils/maskSensitive')
      const fields = {
        name: '很长的名称用于测试脱敏效果',
        enterprise: '测试企业全称',
      }
      const result = maskOrderFields(fields, 'ADMIN')
      expect(result.name).toBe('很长的名称用于测试脱敏效果')
      expect(result.enterprise).toBe('测试企业全称')
    })

    it('USER fields truncated', async () => {
      const { maskOrderFields } = await import('../utils/maskSensitive')
      const fields = {
        name: '这是一个很长的用户名用于测试脱敏效果',
        enterprise: '测试企业全称',
      }
      const result = maskOrderFields(fields, 'USER')
      expect(result.name).toContain('…')
    })
  })
})
