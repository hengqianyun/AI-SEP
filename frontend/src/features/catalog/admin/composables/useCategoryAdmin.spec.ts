import { describe, expect, it, vi, beforeEach } from 'vitest'
import { ApiError } from '@/api/client'
import { useCategoryAdmin } from './useCategoryAdmin'

const listCategories = vi.fn()
const createCategory = vi.fn()
const updateCategory = vi.fn()
const deleteCategory = vi.fn()

vi.mock('@/api/catalog', () => ({
  listCategories: (...args: unknown[]) => listCategories(...args),
  createCategory: (...args: unknown[]) => createCategory(...args),
  updateCategory: (...args: unknown[]) => updateCategory(...args),
  deleteCategory: (...args: unknown[]) => deleteCategory(...args),
}))

describe('useCategoryAdmin', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listCategories.mockResolvedValue({
      data: {
        items: [
          { id: 'l1', name: '空间', level: 'L1' },
          { id: 'l2', name: '行业', level: 'L2', parentId: 'l1' },
          { id: 'l3', name: '子类', level: 'L3', parentId: 'l2' },
        ],
      },
    })
    createCategory.mockResolvedValue({ data: { id: 'n1', name: '新', level: 'L3' } })
    updateCategory.mockResolvedValue({ data: { id: 'l3', name: '改', level: 'L3' } })
  })

  it('loads three levels and cascades L2/L3 selection', async () => {
    const admin = useCategoryAdmin()
    await admin.load()
    expect(admin.state.value).toBe('ready')
    expect(admin.l1List.value).toHaveLength(1)
    expect(admin.l2List.value).toHaveLength(1)
    expect(admin.l3List.value).toHaveLength(1)
    expect(admin.selectedL1Id.value).toBe('l1')
    expect(admin.selectedL2Id.value).toBe('l2')
  })

  it('surfaces delete rejection reason (mounted / last-l3)', async () => {
    deleteCategory.mockRejectedValue(
      new ApiError('分类下仍有挂载产品，禁止删除', 'ERR_CATEGORY_HAS_PRODUCTS', 'c1', {
        reason: '分类下仍有 3 个产品',
        productCount: 3,
      }),
    )
    const admin = useCategoryAdmin()
    await admin.load()
    await admin.remove('l3')
    expect(admin.state.value).toBe('error')
    expect(admin.feedback.value).toContain('产品')
  })

  it('marks dirty pending-save hint on rename input', async () => {
    const admin = useCategoryAdmin()
    await admin.load()
    expect(admin.dirty.value).toBe(false)
    admin.markDirty()
    expect(admin.dirty.value).toBe(true)
  })

  it('adds L3 under selected L2 then reloads', async () => {
    const admin = useCategoryAdmin()
    await admin.load()
    admin.draftL3Name.value = '新子类'
    await admin.addL3()
    expect(createCategory).toHaveBeenCalledWith({
      name: '新子类',
      level: 'L3',
      parentId: 'l2',
    })
    expect(listCategories).toHaveBeenCalledTimes(2)
  })
})
