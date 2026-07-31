import { describe, expect, it, vi, beforeEach } from 'vitest'
import { ApiError } from '@/api/client'
import { useCatalogMaintenance } from './useCatalogMaintenance'

const listCategories = vi.fn()
const listMaintenanceEntries = vi.fn()
const updateMaintenanceEntry = vi.fn()
const batchUpdateMaintenanceEntries = vi.fn()

vi.mock('@/api/catalog', () => ({
  listCategories: (...args: unknown[]) => listCategories(...args),
  listMaintenanceEntries: (...args: unknown[]) => listMaintenanceEntries(...args),
  updateMaintenanceEntry: (...args: unknown[]) => updateMaintenanceEntry(...args),
  batchUpdateMaintenanceEntries: (...args: unknown[]) => batchUpdateMaintenanceEntries(...args),
}))

describe('useCatalogMaintenance', () => {
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
    listMaintenanceEntries.mockResolvedValue({
      data: {
        items: [
          {
            id: 'p1',
            productCode: 'C1',
            productName: '待关联样例',
            maintenanceStatus: 'PENDING',
            l3CategoryId: null,
            categoryPath: null,
          },
        ],
        page: 1,
        pageSize: 10,
        total: 1,
      },
    })
    updateMaintenanceEntry.mockResolvedValue({
      data: {
        id: 'p1',
        productCode: 'C1',
        productName: '待关联样例',
        maintenanceStatus: 'MAINTAINED',
        l3CategoryId: 'l3',
        categoryPath: '空间 / 行业 / 子类',
      },
    })
    batchUpdateMaintenanceEntries.mockResolvedValue({
      data: { successCount: 2, failureCount: 0, failures: [] },
    })
  })

  it('loads list with page/pageSize/total and status tabs', async () => {
    const m = useCatalogMaintenance()
    await m.init()
    expect(m.state.value).toBe('ready')
    expect(m.entries.value).toHaveLength(1)
    expect(m.total.value).toBe(1)
    expect(listMaintenanceEntries).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'ALL', page: 1, pageSize: 10 }),
    )

    await m.setStatusTab('PENDING')
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ status: 'PENDING', page: 1 }),
    )
  })

  it('single save associates l3 then reloads', async () => {
    const m = useCatalogMaintenance()
    await m.init()
    m.startEdit(m.entries.value[0])
    m.editL3Id.value = 'l3'
    await m.saveEdit()
    expect(updateMaintenanceEntry).toHaveBeenCalledWith('p1', { l3CategoryId: 'l3' })
    expect(m.editingId.value).toBeNull()
    expect(listMaintenanceEntries.mock.calls.length).toBeGreaterThanOrEqual(2)
  })

  it('batch save uses selected ids and batch l3', async () => {
    const m = useCatalogMaintenance()
    await m.init()
    m.toggleSelect('p1', true)
    m.toggleSelect('p2', true)
    expect(m.selectedCount.value).toBe(2)
    m.batchL3Id.value = 'l3'
    await m.saveBatch()
    expect(batchUpdateMaintenanceEntries).toHaveBeenCalledWith({
      productIds: expect.arrayContaining(['p1', 'p2']),
      l3CategoryId: 'l3',
    })
    expect(m.selectedCount.value).toBe(0)
    expect(m.feedback.value).toContain('成功 2')
  })

  it('surfaces ApiError on save failure', async () => {
    updateMaintenanceEntry.mockRejectedValue(
      new ApiError('产品须挂载三级分类节点', 'ERR_CATEGORY_LEAF_REQUIRED', 'c1'),
    )
    const m = useCatalogMaintenance()
    await m.init()
    m.startEdit(m.entries.value[0])
    m.editL3Id.value = 'l2'
    await m.saveEdit()
    expect(m.state.value).toBe('error')
    expect(m.feedback.value).toContain('三级')
  })
})
