import { describe, expect, it, vi, beforeEach } from 'vitest'
import { nextTick, ref, toValue } from 'vue'
import { ApiError } from '@/api/client'
import type { MaintenanceScope } from '@/api/catalog'
import {
  DEFAULT_PAGE_SIZE,
  PAGE_SELECT_ALL_LABEL,
  PAGE_SIZE_OPTIONS,
  UNIFIED_MAINTAIN_LABEL,
  batchSelectedCopy,
  buildMaintenanceCascaderOptions,
  canEnterMaintenancePage,
  categoryIdsFromPath,
  clampPageSize,
  pathFromCategoryIds,
  useCatalogMaintenance,
} from './useCatalogMaintenance'

const listCategories = vi.fn()
const listMaintenanceEntries = vi.fn()
const apiRequest = vi.fn()

vi.mock('@/api/catalog', () => ({
  listCategories: (...args: unknown[]) => listCategories(...args),
  listMaintenanceEntries: (...args: unknown[]) => listMaintenanceEntries(...args),
}))

vi.mock('@/api/client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/client')>()
  return {
    ...actual,
    apiRequest: (...args: unknown[]) => apiRequest(...args),
  }
})

const sampleCategories = [
  { id: 'l1', name: '空间', level: 'L1' as const },
  { id: 'l2', name: '行业', level: 'L2' as const, parentId: 'l1' },
  { id: 'l3', name: '子类', level: 'L3' as const, parentId: 'l2' },
]

function mockList(items = [{ id: 'p1' }, { id: 'p2' }], extras?: { total?: number }) {
  listMaintenanceEntries.mockImplementation(async (params: { page?: number; pageSize?: number } = {}) => ({
    data: {
      items: items.map((it, i) => ({
        id: it.id,
        productCode: `C${i + 1}`,
        productName: `条目${it.id}`,
        maintenanceStatus: 'PENDING',
        l3CategoryId: null,
        categoryPath: null,
      })),
      page: params.page ?? 1,
      pageSize: params.pageSize ?? DEFAULT_PAGE_SIZE,
      total: extras?.total ?? items.length,
    },
  }))
}

describe('useCatalogMaintenance', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listCategories.mockResolvedValue({ data: { items: sampleCategories } })
    mockList([{ id: 'p1' }])
    apiRequest.mockResolvedValue({
      data: {
        id: 'p1',
        productCode: 'C1',
        productName: '待关联样例',
        maintenanceStatus: 'MAINTAINED',
        l3CategoryId: 'l3',
        categoryPath: '空间 / 行业 / 子类',
      },
    })
  })

  it('loads list with page/pageSize/total and status tabs', async () => {
    const m = useCatalogMaintenance()
    await m.init()
    expect(m.state.value).toBe('ready')
    expect(m.entries.value).toHaveLength(1)
    expect(m.total.value).toBe(1)
    expect(listMaintenanceEntries).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'ALL', page: 1, pageSize: 10, scope: 'full' }),
    )

    await m.setStatusTab('PENDING')
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ status: 'PENDING', page: 1, scope: 'full' }),
    )
  })

  it('907-my-catalog-scope-split: full vs myCatalog list query', async () => {
    const full = useCatalogMaintenance('full')
    await full.init()
    expect(listMaintenanceEntries).toHaveBeenCalledWith(
      expect.objectContaining({ scope: 'full' }),
    )

    const mine = useCatalogMaintenance('myCatalog')
    await mine.init()
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ scope: 'myCatalog' }),
    )
    expect(toValue(full.scope)).toBe('full')
    expect(toValue(mine.scope)).toBe('myCatalog')
    expect(canEnterMaintenancePage('full', 'ADMIN')).toBe(true)
    expect(canEnterMaintenancePage('full', 'PROVIDER')).toBe(false)
    expect(canEnterMaintenancePage('myCatalog', 'ADMIN')).toBe(true)
    expect(canEnterMaintenancePage('myCatalog', 'PROVIDER')).toBe(true)
    expect(canEnterMaintenancePage('myCatalog', 'USER')).toBe(false)
    expect(canEnterMaintenancePage('full', 'USER')).toBe(false)
  })

  it('907-my-catalog-scope-split: same instance full → myCatalog list and write URLs', async () => {
    const scope = ref<MaintenanceScope>('full')
    const m = useCatalogMaintenance(scope)
    await m.init()
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ scope: 'full' }),
    )
    m.toggleSelect('p1', true)
    expect(m.selectedCount.value).toBe(1)

    scope.value = 'myCatalog'
    await nextTick()
    await vi.waitFor(() =>
      expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
        expect.objectContaining({ scope: 'myCatalog' }),
      ),
    )
    expect(toValue(m.scope)).toBe('myCatalog')
    expect(m.selectedCount.value).toBe(0)

    m.startEdit(m.entries.value[0])
    m.applyEditPath(['l1', 'l2', 'l3'])
    await m.saveEdit()
    expect(apiRequest).toHaveBeenCalledWith(
      '/catalog/maintenance/entries/p1?scope=myCatalog',
      expect.objectContaining({ method: 'PUT' }),
    )

    apiRequest.mockResolvedValue({
      data: { successCount: 1, failureCount: 0, failures: [] },
    })
    m.toggleSelect('p1', true)
    m.applyBatchPath(['l1', 'l2', 'l3'])
    await m.saveBatch()
    expect(apiRequest).toHaveBeenCalledWith(
      '/catalog/maintenance/entries/batch?scope=myCatalog',
      expect.objectContaining({ method: 'POST' }),
    )
  })

  it('Cascader path: L1 only / L1+L2 / L3 / clear', async () => {
    expect(categoryIdsFromPath(['l1'])).toEqual({
      l1CategoryId: 'l1',
      l2CategoryId: '',
      l3CategoryId: '',
    })
    expect(categoryIdsFromPath(['l1', 'l2'])).toEqual({
      l1CategoryId: 'l1',
      l2CategoryId: 'l2',
      l3CategoryId: '',
    })
    expect(categoryIdsFromPath(['l1', 'l2', 'l3'])).toEqual({
      l1CategoryId: 'l1',
      l2CategoryId: 'l2',
      l3CategoryId: 'l3',
    })
    expect(categoryIdsFromPath([])).toEqual({
      l1CategoryId: '',
      l2CategoryId: '',
      l3CategoryId: '',
    })
    expect(pathFromCategoryIds('l1', '', '')).toEqual(['l1'])
    const tree = buildMaintenanceCascaderOptions(sampleCategories)
    expect(tree[0].value).toBe('l1')
    expect(tree[0].children?.[0].value).toBe('l2')
    expect(tree[0].children?.[0].children?.[0].value).toBe('l3')

    const m = useCatalogMaintenance('myCatalog')
    await m.init()
    await m.applyFilterPath(['l1'])
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({
        scope: 'myCatalog',
        l1CategoryId: 'l1',
      }),
    )
    const last = listMaintenanceEntries.mock.calls.at(-1)?.[0] as Record<string, unknown>
    expect(last.l2CategoryId).toBeUndefined()
    expect(last.l3CategoryId).toBeUndefined()

    await m.applyFilterPath(['l1', 'l2', 'l3'])
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({
        l1CategoryId: 'l1',
        l2CategoryId: 'l2',
        l3CategoryId: 'l3',
      }),
    )

    await m.applyFilterPath([])
    const cleared = listMaintenanceEntries.mock.calls.at(-1)?.[0] as Record<string, unknown>
    expect(cleared.l1CategoryId).toBeUndefined()
    expect(cleared.l2CategoryId).toBeUndefined()
    expect(cleared.l3CategoryId).toBeUndefined()
  })

  it('本页全选 / 取消；筛/Tab/翻页清选择', async () => {
    mockList([{ id: 'p1' }, { id: 'p2' }])
    const m = useCatalogMaintenance()
    await m.init()
    m.toggleSelectAllOnPage(true)
    expect(m.selectedCount.value).toBe(2)
    expect(m.allPageSelected.value).toBe(true)
    m.toggleSelectAllOnPage(false)
    expect(m.selectedCount.value).toBe(0)
    expect(PAGE_SELECT_ALL_LABEL).toBe('本页全选')
    expect(UNIFIED_MAINTAIN_LABEL).toBe('统一维护')
    expect(batchSelectedCopy(2)).toBe('已选择 2 条（本页）')

    m.toggleSelectAllOnPage(true)
    await m.setStatusTab('PENDING')
    expect(m.selectedCount.value).toBe(0)

    m.toggleSelectAllOnPage(true)
    await m.applyFilterPath(['l1'])
    expect(m.selectedCount.value).toBe(0)

    listMaintenanceEntries.mockResolvedValueOnce({
      data: { items: [], page: 2, pageSize: DEFAULT_PAGE_SIZE, total: 12 },
    })
    m.total.value = 12
    m.toggleSelectAllOnPage(true)
    await m.goPage(2)
    expect(m.selectedCount.value).toBe(0)
  })

  it('910-page-size-changer: setPageSize 20/50/100 queries page=1 and clears selection', async () => {
    mockList([{ id: 'p1' }, { id: 'p2' }], { total: 120 })
    const m = useCatalogMaintenance()
    await m.init()
    expect(m.pageSize.value).toBe(DEFAULT_PAGE_SIZE)
    expect(listMaintenanceEntries).toHaveBeenCalledWith(
      expect.objectContaining({ page: 1, pageSize: DEFAULT_PAGE_SIZE }),
    )

    m.total.value = 120
    await m.goPage(2)
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 2, pageSize: DEFAULT_PAGE_SIZE }),
    )
    m.toggleSelectAllOnPage(true)
    expect(m.selectedCount.value).toBeGreaterThan(0)

    const callsBefore = listMaintenanceEntries.mock.calls.length
    await m.goPage(2)
    expect(listMaintenanceEntries.mock.calls.length).toBe(callsBefore)

    for (const size of [20, 50, 100] as const) {
      m.toggleSelectAllOnPage(true)
      expect(m.selectedCount.value).toBeGreaterThan(0)
      await m.setPageSize(size)
      expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 1, pageSize: size }),
      )
      expect(m.page.value).toBe(1)
      expect(m.pageSize.value).toBe(size)
      expect(m.selectedCount.value).toBe(0)
    }
  })

  it('910-page-size-changer: antdv dual emit from page>1 keeps page=1', async () => {
    mockList([{ id: 'p1' }, { id: 'p2' }], { total: 120 })
    const m = useCatalogMaintenance()
    await m.init()
    m.total.value = 120
    await m.goPage(2)
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 2, pageSize: DEFAULT_PAGE_SIZE }),
    )
    m.toggleSelectAllOnPage(true)
    expect(m.selectedCount.value).toBeGreaterThan(0)

    const callsBefore = listMaintenanceEntries.mock.calls.length
    // 页 handler 同栈：showSizeChange(current, size) 再 change(current, size)；中间不 await
    const showSizeChange = m.applyPagination(1, 20)
    const change = m.applyPagination(2, 20)
    await Promise.all([showSizeChange, change])

    const size20Calls = listMaintenanceEntries.mock.calls
      .slice(callsBefore)
      .map((c) => c[0] as { page: number; pageSize: number })
      .filter((q) => q.pageSize === 20)
    expect(size20Calls).toHaveLength(1)
    expect(size20Calls[0]).toEqual(expect.objectContaining({ page: 1, pageSize: 20 }))
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1, pageSize: 20 }),
    )
    expect(m.page.value).toBe(1)
    expect(m.pageSize.value).toBe(20)
    expect(m.selectedCount.value).toBe(0)
  })

  it('910-page-size-changer: applyPagination size change on page 1 still reloads; page-only keeps size', async () => {
    mockList([{ id: 'p1' }], { total: 40 })
    const m = useCatalogMaintenance()
    await m.init()
    expect(m.page.value).toBe(1)

    const callsOnPage1 = listMaintenanceEntries.mock.calls.length
    await m.goPage(1)
    expect(listMaintenanceEntries.mock.calls.length).toBe(callsOnPage1)

    m.toggleSelect('p1', true)
    await m.applyPagination(1, 20)
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1, pageSize: 20 }),
    )
    expect(m.selectedCount.value).toBe(0)

    m.total.value = 40
    await m.applyPagination(2, 20)
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 2, pageSize: 20 }),
    )
  })

  it('910-page-size-changer: clampPageSize snaps to 10/20/50/100', async () => {
    expect(PAGE_SIZE_OPTIONS).toEqual([10, 20, 50, 100])
    expect(clampPageSize(Number.NaN)).toBe(10)
    expect(clampPageSize(0)).toBe(10)
    expect(clampPageSize(15)).toBe(10)
    expect(clampPageSize(16)).toBe(20)
    expect(clampPageSize(999)).toBe(100)
    expect(clampPageSize(50)).toBe(50)

    const m = useCatalogMaintenance()
    await m.init()
    await m.setPageSize(999)
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1, pageSize: 100 }),
    )
    await m.setPageSize(3)
    expect(listMaintenanceEntries).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1, pageSize: 10 }),
    )
  })

  it('single save associates l3 then reloads with scope', async () => {
    const m = useCatalogMaintenance('myCatalog')
    await m.init()
    m.startEdit(m.entries.value[0])
    m.applyEditPath(['l1', 'l2', 'l3'])
    await m.saveEdit()
    expect(apiRequest).toHaveBeenCalledWith(
      '/catalog/maintenance/entries/p1?scope=myCatalog',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ l3CategoryId: 'l3' }),
      }),
    )
    expect(m.editingId.value).toBeNull()
    expect(listMaintenanceEntries.mock.calls.length).toBeGreaterThanOrEqual(2)
  })

  it('统一维护 uses selected ids and batch l3 with scope', async () => {
    apiRequest.mockResolvedValue({
      data: { successCount: 2, failureCount: 0, failures: [] },
    })
    const m = useCatalogMaintenance('full')
    await m.init()
    m.toggleSelect('p1', true)
    m.toggleSelect('p2', true)
    expect(m.selectedCount.value).toBe(2)
    m.applyBatchPath(['l1', 'l2', 'l3'])
    await m.saveBatch()
    expect(apiRequest).toHaveBeenCalledWith(
      '/catalog/maintenance/entries/batch?scope=full',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          productIds: ['p1', 'p2'],
          l3CategoryId: 'l3',
        }),
      }),
    )
    expect(m.selectedCount.value).toBe(0)
    expect(m.feedback.value).toContain('成功 2')
  })

  it('surfaces ApiError on save failure', async () => {
    apiRequest.mockRejectedValue(
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
