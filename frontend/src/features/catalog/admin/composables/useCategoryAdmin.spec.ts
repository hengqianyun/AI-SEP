import { describe, expect, it, vi } from 'vitest'
import { useCategoryAdmin } from './useCategoryAdmin'

vi.mock('@/api/catalog', () => ({
  listCategories: vi.fn(async () => ({
    data: {
      items: [
        { id: 'l1', name: '一级', level: 'L1' },
        { id: 'l2', name: '二级', level: 'L2', parentId: 'l1' },
      ],
    },
  })),
  createCategory: vi.fn(async () => ({ data: { id: 'n1', name: '新', level: 'L1' } })),
  updateCategory: vi.fn(),
  deleteCategory: vi.fn(async () => {
    throw new Error('分类下仍有 3 个产品')
  }),
}))

describe('useCategoryAdmin', () => {
  it('loads categories and surfaces delete rejection reason', async () => {
    const admin = useCategoryAdmin()
    await admin.load()
    expect(admin.state.value).toBe('ready')
    expect(admin.l1List.value).toHaveLength(1)
    await admin.remove('l2')
    expect(admin.state.value).toBe('error')
    expect(admin.feedback.value).toContain('产品')
  })
})
