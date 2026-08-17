import { beforeEach, describe, expect, it, vi } from 'vitest'
import { computed, nextTick, ref } from 'vue'
import type { Category, Product } from '@/api/catalog'
import { productTypeLabel } from '../utils/labels'
import { truncateSensitiveId } from '../utils/truncateSensitive'

const listProducts = vi.fn()
const listCategories = vi.fn()
const getProduct = vi.fn()

vi.mock('@/api/catalog', () => ({
  listProducts: (...args: unknown[]) => listProducts(...args),
  listCategories: (...args: unknown[]) => listCategories(...args),
  getProduct: (...args: unknown[]) => getProduct(...args),
}))

import {
  catalogEmptyMessage,
  categoryPathFromFilters,
  createDefaultFilters,
  fillUntilScrollable,
  groupProductsByL3,
  isNearScrollBottom,
  isRetryableListState,
  needsMoreContentToScroll,
  shouldAutoLoadMore,
  UNCATEGORIZED_SECTION_KEY,
  useCatalogBrowse,
} from './useCatalogBrowse'

function product(id: string, overrides: Partial<Product> = {}): Product {
  return {
    id,
    productCode: `CODE-${id}`,
    productName: `Product ${id}`,
    productType: 'DATASET',
    l3CategoryId: 'l3a',
    categoryPath: '空间 / 行业 / 脱敏病历',
    chainCount: 0,
    ...overrides,
  } as Product
}

const sampleCategories: Category[] = [
  { id: 'l1-a', name: '视图A', level: 'L1', parentId: undefined },
  { id: 'l2-a1', name: '大类A1', level: 'L2', parentId: 'l1-a' },
  { id: 'l2-a2', name: '大类A2', level: 'L2', parentId: 'l1-a' },
  { id: 'l1-b', name: '视图B', level: 'L1', parentId: undefined },
  { id: 'l2-b1', name: '大类B1', level: 'L2', parentId: 'l1-b' },
]

describe('catalog browse helpers (REQ-CAT-001..003 / TASK-WSC-104)', () => {
  it('empty message is explicit and stable (testid catalog-empty)', () => {
    expect(catalogEmptyMessage()).toContain('未找到符合条件的数据产品')
  })

  it('error state is retryable', () => {
    expect(isRetryableListState('error')).toBe(true)
    expect(isRetryableListState('empty')).toBe(false)
    expect(isRetryableListState('loading')).toBe(false)
    expect(isRetryableListState('ready')).toBe(false)
  })

  it('default filters omit industryCategory (REQ-CAT-014 / REQ-CAT-019)', () => {
    const f = createDefaultFilters()
    expect(f.l1CategoryId).toBe('')
    expect(f.l2CategoryId).toBe('')
    expect(f).not.toHaveProperty('industryCategory')
    expect(f.productType).toBe('')
    expect(f.q).toBe('')
    expect(f.supplierName).toBe('')
  })

  it('product type labels cover four types', () => {
    expect(productTypeLabel('DATASET')).toBe('数据集')
    expect(productTypeLabel('REPORT')).toBe('数据报告')
    expect(productTypeLabel('API')).toBe('数据接口')
    expect(productTypeLabel('OTHER')).toBe('其他数据产品')
  })

  it('groupProductsByL3 sections by subcategory with counts', () => {
    const cats: Category[] = [
      { id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' },
      { id: 'l3b', name: '结构化病历', level: 'L3', parentId: 'l2' },
    ]
    const products = [
      { id: '1', l3CategoryId: 'l3a', productName: 'A1' },
      { id: '2', l3CategoryId: 'l3a', productName: 'A2' },
      { id: '3', l3CategoryId: 'l3b', productName: 'B1' },
    ] as Product[]
    const sections = groupProductsByL3(products, cats)
    expect(sections).toHaveLength(2)
    expect(sections[0]).toMatchObject({ l3CategoryId: 'l3a', title: '脱敏病历', count: 2 })
    expect(sections[1]).toMatchObject({ l3CategoryId: 'l3b', title: '结构化病历', count: 1 })
  })

  it('909-l3-count-full-total: section count uses server totals, not loaded subset', () => {
    const cats: Category[] = [
      { id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' },
    ]
    const page1 = [{ id: '1', l3CategoryId: 'l3a' }] as Product[]
    const page2 = [
      { id: '1', l3CategoryId: 'l3a' },
      { id: '2', l3CategoryId: 'l3a' },
    ] as Product[]
    const totals = { l3a: 5, [UNCATEGORIZED_SECTION_KEY]: 3 }
    expect(groupProductsByL3(page1, cats, totals)[0]!.count).toBe(5)
    expect(groupProductsByL3(page2, cats, totals)[0]!.count).toBe(5)
    expect(groupProductsByL3(page2, cats, totals)[0]!.products).toHaveLength(2)
    const uncategorizedPage = [
      { id: 'u1' },
      { id: 'u2' },
    ] as Product[]
    const uncategorized = groupProductsByL3(uncategorizedPage, cats, totals).find(
      (s) => s.title === '未分类数据',
    )
    expect(uncategorized?.count).toBe(3)
    expect(uncategorized?.products).toHaveLength(2)
  })

  it('groupProductsByL3 preserves filter semantics for load-more append order', () => {
    const cats: Category[] = [{ id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' }]
    const page1 = [{ id: '1', l3CategoryId: 'l3a' }] as Product[]
    const page2 = [
      { id: '1', l3CategoryId: 'l3a' },
      { id: '2', l3CategoryId: 'l3a' },
    ] as Product[]
    expect(groupProductsByL3(page1, cats)[0]!.products).toHaveLength(1)
    expect(groupProductsByL3(page2, cats)[0]!.products.map((p) => p.id)).toEqual(['1', '2'])
    expect(groupProductsByL3(page2, cats)[0]!.products.every((p) => p.l3CategoryId === 'l3a')).toBe(
      true,
    )
  })

  it('groupProductsByL3 puts all unmounted products into single 「未分类数据」 section', () => {
    const cats: Category[] = [{ id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' }]
    const products = [
      { id: '1', industryCategory: '建筑业', productName: 'A' },
      { id: '2', industryCategory: '建筑业', productName: 'B' },
      { id: '3', industryCategory: '制造业', productName: 'C' },
      { id: '4', productName: 'D' },
      { id: '5', l3CategoryId: 'l3a', industryCategory: '建筑业', productName: 'E' },
    ] as Product[]
    const sections = groupProductsByL3(products, cats)
    expect(sections).toHaveLength(2)
    expect(sections[0]).toMatchObject({ l3CategoryId: 'l3a', title: '脱敏病历', count: 1 })
    const uncategorized = sections.find((s) => s.title === '未分类数据')
    expect(uncategorized?.count).toBe(4)
    expect(uncategorized?.products.map((p) => p.id)).toEqual(['1', '2', '3', '4'])
    expect(sections.some((s) => s.title === '建筑业' || s.title === '制造业')).toBe(false)
  })
})

describe('categoryPathFromFilters (REQ-CAT-014)', () => {
  it('maps l1-only, l1+l2, and empty', () => {
    expect(categoryPathFromFilters('', '')).toEqual([])
    expect(categoryPathFromFilters('l1-a', '')).toEqual(['l1-a'])
    expect(categoryPathFromFilters('l1-a', 'l2-a1')).toEqual(['l1-a', 'l2-a1'])
  })
})

describe('isNearScrollBottom (REQ-UX-004 / TASK-WSC-305)', () => {
  it('returns true when remaining scroll distance is below threshold', () => {
    expect(
      isNearScrollBottom({ scrollHeight: 1000, scrollTop: 900, clientHeight: 100 }, 80),
    ).toBe(true)
    expect(
      isNearScrollBottom({ scrollHeight: 1000, scrollTop: 920, clientHeight: 100 }, 80),
    ).toBe(true)
  })

  it('returns false when still far from bottom', () => {
    expect(
      isNearScrollBottom({ scrollHeight: 1000, scrollTop: 100, clientHeight: 100 }, 80),
    ).toBe(false)
  })

  it('uses default threshold of 80 (strict less-than)', () => {
    expect(isNearScrollBottom({ scrollHeight: 500, scrollTop: 321, clientHeight: 100 })).toBe(true)
    expect(isNearScrollBottom({ scrollHeight: 500, scrollTop: 320, clientHeight: 100 })).toBe(false)
  })
})

describe('auto-fill when list does not overflow (FIND-WSC-305-R1-001)', () => {
  it('needsMoreContentToScroll when content shorter than pane', () => {
    expect(needsMoreContentToScroll({ scrollHeight: 400, clientHeight: 740 })).toBe(true)
    expect(needsMoreContentToScroll({ scrollHeight: 900, clientHeight: 740 })).toBe(false)
    expect(needsMoreContentToScroll({ scrollHeight: 780, clientHeight: 740 }, 80)).toBe(true)
  })

  it('shouldAutoLoadMore gates on hasMore / loading / ready / overflow', () => {
    expect(
      shouldAutoLoadMore({
        hasMore: true,
        loadingMore: false,
        listReady: true,
        scrollHeight: 400,
        clientHeight: 740,
      }),
    ).toBe(true)
    expect(
      shouldAutoLoadMore({
        hasMore: false,
        loadingMore: false,
        listReady: true,
        scrollHeight: 400,
        clientHeight: 740,
      }),
    ).toBe(false)
    expect(
      shouldAutoLoadMore({
        hasMore: true,
        loadingMore: true,
        listReady: true,
        scrollHeight: 400,
        clientHeight: 740,
      }),
    ).toBe(false)
    expect(
      shouldAutoLoadMore({
        hasMore: true,
        loadingMore: false,
        listReady: false,
        scrollHeight: 400,
        clientHeight: 740,
      }),
    ).toBe(false)
    expect(
      shouldAutoLoadMore({
        hasMore: true,
        loadingMore: false,
        listReady: true,
        scrollHeight: 1200,
        clientHeight: 740,
      }),
    ).toBe(false)
  })

  it('fillUntilScrollable keeps loading until scrollable or !hasMore', async () => {
    let loaded = 10
    const total = 30
    let scrollHeight = 400
    const clientHeight = 740
    const loadMore = vi.fn(async () => {
      loaded += 10
      scrollHeight += 400
    })

    const pages = await fillUntilScrollable({
      getMetrics: () => ({ scrollHeight, clientHeight }),
      hasMore: () => loaded < total,
      loadingMore: () => false,
      listReady: () => true,
      loadMore,
      getLoadedCount: () => loaded,
    })

    expect(pages).toBe(2)
    expect(loadMore).toHaveBeenCalledTimes(2)
    expect(loaded).toBe(30)
    expect(scrollHeight).toBe(1200)
  })

  it('fillUntilScrollable stops when hasMore becomes false before overflow', async () => {
    let loaded = 10
    const total = 15
    let scrollHeight = 400
    const loadMore = vi.fn(async () => {
      loaded = total
      scrollHeight = 550
    })

    const pages = await fillUntilScrollable({
      getMetrics: () => ({ scrollHeight, clientHeight: 740 }),
      hasMore: () => loaded < total,
      loadingMore: () => false,
      listReady: () => true,
      loadMore,
      getLoadedCount: () => loaded,
    })

    expect(pages).toBe(1)
    expect(loadMore).toHaveBeenCalledTimes(1)
    expect(loaded).toBe(15)
  })

  it('fillUntilScrollable stops on stagnation and respects maxPages', async () => {
    const loadMoreStale = vi.fn(async () => {
      /* no growth */
    })
    const stalePages = await fillUntilScrollable({
      getMetrics: () => ({ scrollHeight: 400, clientHeight: 740 }),
      hasMore: () => true,
      loadingMore: () => false,
      listReady: () => true,
      loadMore: loadMoreStale,
      getLoadedCount: () => 10,
      maxPages: 5,
    })
    expect(stalePages).toBe(1)
    expect(loadMoreStale).toHaveBeenCalledTimes(1)

    const loadMoreCap = vi.fn(async () => {
      /* grows but never fills */
    })
    let n = 0
    const capped = await fillUntilScrollable({
      getMetrics: () => ({ scrollHeight: 400, clientHeight: 740 }),
      hasMore: () => true,
      loadingMore: () => false,
      listReady: () => true,
      loadMore: async () => {
        n += 1
        await loadMoreCap()
      },
      getLoadedCount: () => n,
      maxPages: 3,
    })
    expect(capped).toBe(3)
    expect(loadMoreCap).toHaveBeenCalledTimes(3)
  })
})

describe('useCatalogBrowse loadMore / hasMore (REQ-CAT-001 / REQ-UX-004)', () => {
  beforeEach(() => {
    listProducts.mockReset()
    listCategories.mockReset()
    getProduct.mockReset()
    listCategories.mockResolvedValue({ data: { items: [] as Category[] } })
    getProduct.mockImplementation(async (id: string) => ({
      data: product(id),
    }))
  })

  it('hasMore is true when loaded count < total; loadMore appends next page', async () => {
    const page1 = Array.from({ length: 10 }, (_, i) => product(`p${i + 1}`))
    const page2 = [product('p11'), product('p12')]
    listProducts
      .mockResolvedValueOnce({
        data: { items: page1, total: 12, page: 1, pageSize: 10 },
      })
      .mockResolvedValueOnce({
        data: { items: page2, total: 12, page: 2, pageSize: 10 },
      })

    const browse = useCatalogBrowse()
    await browse.loadProducts()
    expect(browse.listState.value).toBe('ready')
    expect(browse.products.value).toHaveLength(10)
    expect(browse.total.value).toBe(12)
    expect(browse.hasMore.value).toBe(true)

    await browse.loadMore()
    expect(browse.products.value).toHaveLength(12)
    expect(browse.products.value.map((p) => p.id)).toEqual([
      ...page1.map((p) => p.id),
      'p11',
      'p12',
    ])
    expect(browse.hasMore.value).toBe(false)
    expect(listProducts).toHaveBeenCalledTimes(2)
    expect(listProducts.mock.calls[1]![0]).toMatchObject({ page: 2, pageSize: 10 })
  })

  it('loadMore is a no-op when !hasMore or already loadingMore', async () => {
    listProducts.mockResolvedValue({
      data: { items: [product('only')], total: 1, page: 1, pageSize: 10 },
    })
    const browse = useCatalogBrowse()
    await browse.loadProducts()
    expect(browse.hasMore.value).toBe(false)

    await browse.loadMore()
    expect(listProducts).toHaveBeenCalledTimes(1)
  })

  it('loadMore dedupes overlapping ids on append', async () => {
    listProducts
      .mockResolvedValueOnce({
        data: { items: [product('a'), product('b')], total: 3, page: 1, pageSize: 2 },
      })
      .mockResolvedValueOnce({
        data: { items: [product('b'), product('c')], total: 3, page: 2, pageSize: 2 },
      })

    const browse = useCatalogBrowse()
    await browse.loadProducts()
    await browse.loadMore()
    expect(browse.products.value.map((p) => p.id)).toEqual(['a', 'b', 'c'])
    expect(browse.hasMore.value).toBe(false)
  })
})

describe('TASK-WSC-909 l3Counts (HOTFIX)', () => {
  beforeEach(() => {
    listProducts.mockReset()
    listCategories.mockReset()
    getProduct.mockReset()
    listCategories.mockResolvedValue({ data: { items: [] as Category[] } })
    getProduct.mockImplementation(async (id: string) => ({
      data: product(id),
    }))
  })

  it('909-l3-count-full-total: loadMore does not inflate section count', async () => {
    const cats: Category[] = [{ id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' }]
    listCategories.mockResolvedValue({ data: { items: cats } })
    const page1 = Array.from({ length: 2 }, (_, i) => product(`p${i + 1}`))
    const page2 = [product('p3'), product('p4')]
    const l3Counts = { l3a: 4 }
    listProducts
      .mockResolvedValueOnce({
        data: { items: page1, total: 4, page: 1, pageSize: 2, l3Counts },
      })
      .mockResolvedValueOnce({
        data: { items: page2, total: 4, page: 2, pageSize: 2, l3Counts },
      })

    const browse = useCatalogBrowse()
    await browse.init()
    expect(browse.products.value).toHaveLength(2)
    expect(browse.sections.value[0]!.count).toBe(4)

    await browse.loadMore()
    expect(browse.products.value).toHaveLength(4)
    expect(browse.sections.value[0]!.count).toBe(4)
  })
})

describe('TASK-WSC-901 browse query (REQ-CAT-014 / REQ-CAT-019)', () => {
  beforeEach(() => {
    listProducts.mockReset()
    listCategories.mockReset()
    getProduct.mockReset()
    listCategories.mockResolvedValue({ data: { items: sampleCategories } })
    getProduct.mockImplementation(async (id: string) => ({
      data: product(id),
    }))
    listProducts.mockResolvedValue({
      data: { items: [product('a')], total: 1, page: 1, pageSize: 10 },
    })
  })

  it('901-no-industryCategory-query: listProducts never sends industryCategory or enterpriseName', async () => {
    const browse = useCatalogBrowse()
    browse.filters.value.supplierName = '测试企业'
    browse.filters.value.l1CategoryId = 'l1-a'
    browse.filters.value.l2CategoryId = 'l2-a1'
    await browse.applyFilters()

    const query = listProducts.mock.calls[0]![0] as Record<string, unknown>
    expect(query).toMatchObject({
      l1CategoryId: 'l1-a',
      l2CategoryId: 'l2-a1',
      supplierName: '测试企业',
      page: 1,
    })
    expect(query).not.toHaveProperty('industryCategory')
    expect(query).not.toHaveProperty('enterpriseName')
  })

  it.each([
    { label: 'catalog', mine: false },
    { label: 'mine', mine: true },
  ])('901-no-industryCategory-query ($label mode): query omits industryCategory', async ({ mine }) => {
    const browse = useCatalogBrowse({ mine })
    browse.filters.value.l1CategoryId = 'l1-a'
    await browse.applyFilters()
    const query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).not.toHaveProperty('industryCategory')
    expect(query).not.toHaveProperty('enterpriseName')
    if (mine) {
      expect(query).toMatchObject({ mine: true, l1CategoryId: 'l1-a' })
    } else {
      expect(query).not.toHaveProperty('mine')
    }
  })

  it('901-cascader-l1-l2-clear: L1 only sends l1CategoryId without l2CategoryId', async () => {
    const browse = useCatalogBrowse()
    browse.applyCategoryPath(['l1-a'])
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalled())

    const query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).toMatchObject({ l1CategoryId: 'l1-a', page: 1 })
    expect(query).not.toHaveProperty('l2CategoryId')
  })

  it('901-cascader-l1-l2-clear: L1+L2 sends both category ids', async () => {
    const browse = useCatalogBrowse()
    browse.applyCategoryPath(['l1-a', 'l2-a1'])
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalled())

    expect(listProducts.mock.calls.at(-1)![0]).toMatchObject({
      l1CategoryId: 'l1-a',
      l2CategoryId: 'l2-a1',
    })
  })

  it('901-cascader-l1-l2-clear: clear removes l1 and l2 from query', async () => {
    const browse = useCatalogBrowse()
    browse.applyCategoryPath(['l1-a', 'l2-a1'])
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalledTimes(1))

    browse.applyCategoryPath([])
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalledTimes(2))

    const query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).not.toHaveProperty('l1CategoryId')
    expect(query).not.toHaveProperty('l2CategoryId')
  })

  it.each([
    { label: 'catalog', mine: false },
    { label: 'mine', mine: true },
  ])('901-cascader-l1-l2-clear ($label mode): cascader boundaries', async ({ mine }) => {
    const browse = useCatalogBrowse({ mine })
    browse.applyCategoryPath(['l1-b'])
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalled())

    let query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).toMatchObject({ l1CategoryId: 'l1-b' })
    expect(query).not.toHaveProperty('l2CategoryId')

    browse.applyCategoryPath(['l1-b', 'l2-b1'])
    await vi.waitFor(() => expect(listProducts.mock.calls.length).toBeGreaterThan(1))
    query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).toMatchObject({ l1CategoryId: 'l1-b', l2CategoryId: 'l2-b1' })

    browse.applyCategoryPath([])
    await vi.waitFor(() => expect(listProducts.mock.calls.length).toBeGreaterThan(2))
    query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).not.toHaveProperty('l1CategoryId')
    expect(query).not.toHaveProperty('l2CategoryId')
  })

  it('supplierName still sent on applyFilters (REQ-CAT-012)', async () => {
    const browse = useCatalogBrowse()
    browse.filters.value.supplierName = 'Acme Corp'
    await browse.applyFilters()
    expect(listProducts.mock.calls[0]![0]).toMatchObject({ supplierName: 'Acme Corp' })
  })

  it('resetFilters clears category path and reloads', async () => {
    const browse = useCatalogBrowse()
    browse.filters.value.l1CategoryId = 'l1-a'
    browse.filters.value.l2CategoryId = 'l2-a1'
    browse.filters.value.supplierName = 'X'
    await browse.resetFilters()

    expect(browse.filters.value.l1CategoryId).toBe('')
    expect(browse.filters.value.l2CategoryId).toBe('')
    expect(browse.filters.value.supplierName).toBe('')
    const query = listProducts.mock.calls.at(-1)![0] as Record<string, unknown>
    expect(query).not.toHaveProperty('l1CategoryId')
    expect(query).not.toHaveProperty('l2CategoryId')
    expect(query).not.toHaveProperty('supplierName')
  })
})

describe('§3.6 sensitive id truncate (browse)', () => {
  it('truncates long credit code with ellipsis', () => {
    const code = '91310000MA1KXXXX1AEXTRA'
    const shown = truncateSensitiveId(code)
    expect(shown).toContain('…')
    expect(shown.length).toBeLessThan(code.length)
  })
})

describe('useCatalogBrowse mine reactivity (FIND-WSC-603-R1-001)', () => {
  beforeEach(() => {
    listProducts.mockReset()
    listCategories.mockReset()
    getProduct.mockReset()
    listCategories.mockResolvedValue({ data: { items: [] as Category[] } })
    getProduct.mockImplementation(async (id: string) => ({
      data: product(id),
    }))
    listProducts.mockResolvedValue({
      data: { items: [product('a')], total: 1, page: 1, pageSize: 10 },
    })
  })

  it('catalog↔my-products: listProducts query gains/drops mine=true when mine ref toggles', async () => {
    const mine = ref(false)
    const browse = useCatalogBrowse({ mine })
    await browse.loadProducts()
    expect(listProducts).toHaveBeenCalledTimes(1)
    expect(listProducts.mock.calls[0]![0]).not.toHaveProperty('mine')

    mine.value = true
    await nextTick()
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalledTimes(2))
    expect(listProducts.mock.calls[1]![0]).toMatchObject({ mine: true })

    mine.value = false
    await nextTick()
    await vi.waitFor(() => expect(listProducts).toHaveBeenCalledTimes(3))
    expect(listProducts.mock.calls[2]![0]).not.toHaveProperty('mine')
  })

  it('accepts computed mine (CatalogBrowsePage mode prop path)', async () => {
    const mode = ref<'catalog' | 'mine'>('catalog')
    const isMine = computed(() => mode.value === 'mine')
    const browse = useCatalogBrowse({ mine: isMine })
    await browse.init()
    expect(listProducts.mock.calls.at(-1)![0]).not.toHaveProperty('mine')

    mode.value = 'mine'
    await nextTick()
    await vi.waitFor(() =>
      expect(listProducts.mock.calls.at(-1)![0]).toMatchObject({ mine: true }),
    )

    mode.value = 'catalog'
    await nextTick()
    await vi.waitFor(() =>
      expect(listProducts.mock.calls.at(-1)![0]).not.toHaveProperty('mine'),
    )
  })

  it('plain boolean mine=true still sends mine on load (compat)', async () => {
    const browse = useCatalogBrowse({ mine: true })
    await browse.loadProducts()
    expect(listProducts.mock.calls[0]![0]).toMatchObject({ mine: true })
  })
})
