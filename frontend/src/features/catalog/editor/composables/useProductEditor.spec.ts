import { describe, expect, it, vi } from 'vitest'
import { useProductEditor } from './useProductEditor'

vi.mock('@/api/catalog', () => ({
  listCategories: vi.fn(async () => ({
    data: {
      items: [
        { id: 'cat-l1-health', name: '医疗卫生', level: 'L1', parentId: null },
        { id: 'cat-l2-emr', name: '电子病历', level: 'L2', parentId: 'cat-l1-health' },
        { id: 'cat-l3-emr-desense', name: '脱敏病历', level: 'L3', parentId: 'cat-l2-emr' },
        { id: 'cat-l3-emr-struct', name: '结构化病历', level: 'L3', parentId: 'cat-l2-emr' },
      ],
    },
  })),
  getProduct: vi.fn(),
  createProduct: vi.fn(async (body: { l3CategoryId?: string }) => ({
    data: { id: 'prod-new', productCode: 'GEN-NEW-0001', l3CategoryId: body.l3CategoryId },
  })),
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

  it('blocks submit without L3 category', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productCode = 'GEN-NEW-0001'
    ed.form.productName = '缺三级'
    ed.form.productType = 'OTHER'
    ed.form.l1CategoryId = 'cat-l1-health'
    ed.form.l2CategoryId = 'cat-l2-emr'
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.feedback.value).toContain('三级分类')
  })

  it('OTHER type does not require typeSpecific; cascades L3 and success feedback', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productCode = 'GEN-NEW-0001'
    ed.form.productName = '其他新产品'
    ed.form.productType = 'OTHER'
    ed.form.l1CategoryId = 'cat-l1-health'
    ed.form.l2CategoryId = 'cat-l2-emr'
    ed.form.l3CategoryId = 'cat-l3-emr-desense'
    expect(ed.selectedPathLabel.value).toBe('医疗卫生 / 电子病历 / 脱敏病历')
    expect(ed.l3Categories.value.map((c) => c.id)).toEqual([
      'cat-l3-emr-desense',
      'cat-l3-emr-struct',
    ])
    const id = await ed.submit()
    expect(id).toBe('prod-new')
    expect(ed.state.value).toBe('success')
    expect(ed.feedback.value).toContain('第 1 版')
  })

  it('changing L1 clears L2/L3', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.l1CategoryId = 'cat-l1-health'
    ed.form.l2CategoryId = 'cat-l2-emr'
    ed.form.l3CategoryId = 'cat-l3-emr-desense'
    ed.form.l1CategoryId = 'other-l1'
    ed.onL1Change()
    expect(ed.form.l2CategoryId).toBe('')
    expect(ed.form.l3CategoryId).toBe('')
  })
})
