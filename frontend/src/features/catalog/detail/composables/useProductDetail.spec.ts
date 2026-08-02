import { describe, expect, it, vi } from 'vitest'
import { useProductDetail } from './useProductDetail'

vi.mock('@/api/catalog', () => ({
  getProduct: vi.fn(async (id: string) => {
    if (id === 'missing') throw new Error('产品不存在')
    if (id === 'api-legacy') {
      return {
        data: {
          id,
          productCode: 'GEN-API-0001',
          productName: '旧接口产品',
          productType: 'API',
          l2CategoryId: 'cat-l2-emr',
          l3CategoryId: 'cat-l3-emr-desense',
          categoryPath: '医疗卫生 / 电子病历 / 脱敏病历',
          chainCount: 1,
          typeSpecific: {
            api: { endpoint: 'GET /legacy/ping' },
          },
        },
      }
    }
    return {
      data: {
        id,
        productCode: 'GEN-MISC-0005',
        productName: '其他',
        productType: 'OTHER',
        l2CategoryId: 'cat-l2-emr',
        l3CategoryId: 'cat-l3-emr-desense',
        categoryPath: '医疗卫生 / 电子病历 / 脱敏病历',
        chainCount: 0,
        typeSpecific: {
          other: { contentDescription: '其他内容', regionScope: '全国' },
        },
      },
    }
  }),
}))

describe('useProductDetail', () => {
  it('loads product into ready state with three-level path', async () => {
    const d = useProductDetail()
    await d.load('prod-1')
    expect(d.state.value).toBe('ready')
    expect(d.product.value?.productType).toBe('OTHER')
    expect(d.product.value?.l3CategoryId).toBe('cat-l3-emr-desense')
    expect(d.product.value?.categoryPath).toContain('脱敏病历')
    expect(d.product.value?.typeSpecific?.other?.contentDescription).toBe('其他内容')
  })

  it('loads API product with legacy endpoint without error', async () => {
    const d = useProductDetail()
    await d.load('api-legacy')
    expect(d.state.value).toBe('ready')
    expect(d.product.value?.productType).toBe('API')
    expect(d.product.value?.typeSpecific?.api?.endpoint).toBe('GET /legacy/ping')
  })

  it('surfaces error feedback', async () => {
    const d = useProductDetail()
    await d.load('missing')
    expect(d.state.value).toBe('error')
    expect(d.error.value).toContain('产品不存在')
  })
})
