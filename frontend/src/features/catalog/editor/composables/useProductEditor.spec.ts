import { describe, expect, it, vi } from 'vitest'
import { useProductEditor } from './useProductEditor'

vi.mock('@/api/catalog', () => ({
  listCategories: vi.fn(async () => ({
    data: { items: [{ id: 'cat-l2-emr', name: '电子病历', level: 'L2', parentId: 'cat-l1-health' }] },
  })),
  getProduct: vi.fn(),
  createProduct: vi.fn(async () => ({ data: { id: 'prod-new', productCode: 'GEN-NEW-0001' } })),
  updateProduct: vi.fn(),
}))

describe('useProductEditor', () => {
  it('blocks submit without name/code and shows error feedback', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.state.value).toBe('error')
    expect(ed.feedback.value).toContain('产品名称')
  })

  it('OTHER type does not require typeSpecific; success feedback', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productCode = 'GEN-NEW-0001'
    ed.form.productName = '其他新产品'
    ed.form.productType = 'OTHER'
    ed.form.l2CategoryId = 'cat-l2-emr'
    const id = await ed.submit()
    expect(id).toBe('prod-new')
    expect(ed.state.value).toBe('success')
    expect(ed.feedback.value).toContain('第 1 版')
  })
})
