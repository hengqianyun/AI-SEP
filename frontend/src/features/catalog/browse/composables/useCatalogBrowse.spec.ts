import { beforeEach, describe, expect, it, vi } from 'vitest'
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
  createDefaultFilters,
  fillUntilScrollable,
  groupProductsByL3,
  isNearScrollBottom,
  isRetryableListState,
  needsMoreContentToScroll,
  resolveIndustryQuery,
  shouldAutoLoadMore,
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

  it('default filters include industryFilterId (L2/L3)', () => {
    const f = createDefaultFilters()
    expect(f.l1CategoryId).toBe('')
    expect(f.l2CategoryId).toBe('')
    expect(f.industryFilterId).toBe('')
    expect(f.productType).toBe('')
    expect(f.q).toBe('')
  })

  it('product type labels cover four types', () => {
    expect(productTypeLabel('DATASET')).toBe('数据集')
    expect(productTypeLabel('REPORT')).toBe('数据报告')
    expect(productTypeLabel('API')).toBe('数据接口')
    expect(productTypeLabel('OTHER')).toBe('其他数据产品')
  })

  it('resolveIndustryQuery maps L2 and L3 filter ids', () => {
    const cats: Category[] = [
      { id: 'l1', name: '空间', level: 'L1' },
      { id: 'l2', name: '行业', level: 'L2', parentId: 'l1' },
      { id: 'l3', name: '子类', level: 'L3', parentId: 'l2' },
    ]
    expect(resolveIndustryQuery('', cats)).toEqual({
      l2CategoryId: undefined,
      l3CategoryId: undefined,
    })
    expect(resolveIndustryQuery('l2', cats)).toEqual({
      l2CategoryId: 'l2',
      l3CategoryId: undefined,
    })
    expect(resolveIndustryQuery('l3', cats)).toEqual({
      l2CategoryId: undefined,
      l3CategoryId: 'l3',
    })
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

  it('groupProductsByL3 preserves filter semantics for load-more append order', () => {
    const cats: Category[] = [{ id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' }]
    const page1 = [{ id: '1', l3CategoryId: 'l3a' }] as Product[]
    const page2 = [
      { id: '1', l3CategoryId: 'l3a' },
      { id: '2', l3CategoryId: 'l3a' },
    ] as Product[]
    expect(groupProductsByL3(page1, cats)[0]!.count).toBe(1)
    expect(groupProductsByL3(page2, cats)[0]!.count).toBe(2)
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
    // remain = 500 - 321 - 100 = 79 < 80 → true; remain = 80 → false
    expect(isNearScrollBottom({ scrollHeight: 500, scrollTop: 321, clientHeight: 100 })).toBe(true)
    expect(isNearScrollBottom({ scrollHeight: 500, scrollTop: 320, clientHeight: 100 })).toBe(false)
  })
})

describe('auto-fill when list does not overflow (FIND-WSC-305-R1-001)', () => {
  it('needsMoreContentToScroll when content shorter than pane', () => {
    expect(needsMoreContentToScroll({ scrollHeight: 400, clientHeight: 740 })).toBe(true)
    // overflow >= threshold → 已可滚，无需续载
    expect(needsMoreContentToScroll({ scrollHeight: 900, clientHeight: 740 })).toBe(false)
    // overflow < threshold still needs fill（900-740 才够；780-740=40 < 80）
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
    let total = 30
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

describe('§3.6 sensitive id truncate (browse)', () => {
  it('truncates long credit code with ellipsis', () => {
    const code = '91310000MA1KXXXX1AEXTRA'
    const shown = truncateSensitiveId(code)
    expect(shown).toContain('…')
    expect(shown.length).toBeLessThan(code.length)
  })
})
