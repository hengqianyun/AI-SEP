import { computed, ref, watch } from 'vue'
import {
  getProduct,
  listCategories,
  listProducts,
  type Category,
  type Product,
} from '@/api/catalog'
import { ApiError } from '@/api/client'
import { CATALOG_EMPTY_MESSAGE, CATALOG_ERROR_MESSAGE } from '../utils/labels'

export type LoadState = 'idle' | 'loading' | 'empty' | 'ready' | 'error'

export type CatalogFilters = {
  l1CategoryId: string
  l2CategoryId: string
  /** 行业筛：可为 L2 或 L3 id（REQ-CAT-002） */
  industryFilterId: string
  dataSource: string
  productType: string
  involvesPublicData: string
  deliveryMethod: string
  q: string
}

export type ProductSection = {
  l3CategoryId: string
  title: string
  count: number
  products: Product[]
}

export function createDefaultFilters(): CatalogFilters {
  return {
    l1CategoryId: '',
    l2CategoryId: '',
    industryFilterId: '',
    dataSource: '',
    productType: '',
    involvesPublicData: '',
    deliveryMethod: '',
    q: '',
  }
}

export function catalogEmptyMessage(): string {
  return CATALOG_EMPTY_MESSAGE
}

export function isRetryableListState(state: LoadState): boolean {
  return state === 'error'
}

/** 将扁平产品列表按三级子类分节（稳定顺序：分类树顺序，未知落末尾）。 */
export function groupProductsByL3(
  products: Product[],
  categories: Category[],
): ProductSection[] {
  const l3Order = categories.filter((c) => c.level === 'L3').map((c) => c.id)
  const l3Name = new Map(categories.filter((c) => c.level === 'L3').map((c) => [c.id, c.name]))
  const buckets = new Map<string, Product[]>()

  for (const p of products) {
    const key = p.l3CategoryId || '__unknown__'
    const list = buckets.get(key) ?? []
    list.push(p)
    buckets.set(key, list)
  }

  const sections: ProductSection[] = []
  for (const id of l3Order) {
    const list = buckets.get(id)
    if (!list || list.length === 0) continue
    sections.push({
      l3CategoryId: id,
      title: l3Name.get(id) ?? id,
      count: list.length,
      products: list,
    })
    buckets.delete(id)
  }
  for (const [id, list] of buckets) {
    const title =
      id === '__unknown__'
        ? '未关联子类'
        : (list[0]?.categoryPath?.split('/').pop()?.trim() ?? id)
    sections.push({ l3CategoryId: id, title, count: list.length, products: list })
  }
  return sections
}

export function resolveIndustryQuery(industryFilterId: string, categories: Category[]) {
  if (!industryFilterId) return { l2CategoryId: undefined as string | undefined, l3CategoryId: undefined as string | undefined }
  const cat = categories.find((c) => c.id === industryFilterId)
  if (!cat) return { l2CategoryId: undefined, l3CategoryId: undefined }
  if (cat.level === 'L3') return { l2CategoryId: undefined, l3CategoryId: cat.id }
  if (cat.level === 'L2') return { l2CategoryId: cat.id, l3CategoryId: undefined }
  return { l2CategoryId: undefined, l3CategoryId: undefined }
}

/** 列表 pane 近底 / 未溢出判定阈值（px）。 */
export const LIST_SCROLL_THRESHOLD = 80

/** 未溢出自动续载的安全上限（页），防止异常 total 死循环。 */
export const MAX_AUTO_FILL_PAGES = 20

/** 列表 pane 触底判定（REQ-UX-004）：剩余可滚距离小于 threshold 时触发 loadMore。 */
export function isNearScrollBottom(
  el: Pick<HTMLElement, 'scrollHeight' | 'scrollTop' | 'clientHeight'>,
  threshold = LIST_SCROLL_THRESHOLD,
): boolean {
  return el.scrollHeight - el.scrollTop - el.clientHeight < threshold
}

/** 内容高度不足以产生可用滚动条（含接近阈值内）。 */
export function needsMoreContentToScroll(
  el: Pick<HTMLElement, 'scrollHeight' | 'clientHeight'>,
  threshold = LIST_SCROLL_THRESHOLD,
): boolean {
  return el.scrollHeight - el.clientHeight < threshold
}

/** 首屏/追加后：有更多且列表未撑满 pane 时继续 loadMore（FIND-WSC-305-R1-001）。 */
export function shouldAutoLoadMore(input: {
  hasMore: boolean
  loadingMore: boolean
  listReady: boolean
  scrollHeight: number
  clientHeight: number
  threshold?: number
}): boolean {
  if (!input.hasMore || input.loadingMore || !input.listReady) return false
  return needsMoreContentToScroll(
    { scrollHeight: input.scrollHeight, clientHeight: input.clientHeight },
    input.threshold ?? LIST_SCROLL_THRESHOLD,
  )
}

/**
 * 循环拉取直至可滚、无更多、停滞或达上限。
 * @returns 实际触发的 loadMore 次数
 */
export async function fillUntilScrollable(opts: {
  getMetrics: () => { scrollHeight: number; clientHeight: number }
  hasMore: () => boolean
  loadingMore: () => boolean
  listReady: () => boolean
  loadMore: () => Promise<void>
  getLoadedCount?: () => number
  maxPages?: number
  threshold?: number
}): Promise<number> {
  const max = opts.maxPages ?? MAX_AUTO_FILL_PAGES
  let pages = 0
  while (pages < max) {
    if (
      !shouldAutoLoadMore({
        hasMore: opts.hasMore(),
        loadingMore: opts.loadingMore(),
        listReady: opts.listReady(),
        ...opts.getMetrics(),
        threshold: opts.threshold,
      })
    ) {
      break
    }
    const before = opts.getLoadedCount?.()
    await opts.loadMore()
    pages += 1
    if (before !== undefined && opts.getLoadedCount?.() === before) {
      break
    }
  }
  return pages
}

export function useCatalogBrowse() {
  const categories = ref<Category[]>([])
  const products = ref<Product[]>([])
  const selectedProductId = ref<string | null>(null)
  const preview = ref<Product | null>(null)

  const listState = ref<LoadState>('idle')
  const categoriesState = ref<LoadState>('idle')
  const previewState = ref<LoadState>('idle')
  const loadingMore = ref(false)
  const listError = ref<string | null>(null)
  const categoriesError = ref<string | null>(null)
  const previewError = ref<string | null>(null)

  const filters = ref<CatalogFilters>(createDefaultFilters())
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const l1Categories = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2Categories = computed(() => {
    const l1 = filters.value.l1CategoryId
    return categories.value.filter((c) => c.level === 'L2' && (!l1 || c.parentId === l1))
  })
  const l3Categories = computed(() => {
    const l2Ids = new Set(l2Categories.value.map((c) => c.id))
    return categories.value.filter((c) => c.level === 'L3' && l2Ids.has(c.parentId ?? ''))
  })

  /** 行业筛选项：当前可见 L2 + 其下 L3 */
  const industryFilterOptions = computed(() => {
    const opts: { id: string; label: string; level: 'L2' | 'L3' }[] = []
    for (const l2 of l2Categories.value) {
      opts.push({ id: l2.id, label: l2.name, level: 'L2' })
      for (const l3 of categories.value.filter((c) => c.level === 'L3' && c.parentId === l2.id)) {
        opts.push({ id: l3.id, label: `　${l3.name}`, level: 'L3' })
      }
    }
    return opts
  })

  const sections = computed(() => groupProductsByL3(products.value, categories.value))
  const hasMore = computed(() => products.value.length < total.value)

  function buildQuery(pageNo: number) {
    const f = filters.value
    const industry = resolveIndustryQuery(f.industryFilterId, categories.value)
    const query: Record<string, string | number | boolean | undefined> = {
      page: pageNo,
      pageSize: pageSize.value,
    }
    if (f.l1CategoryId) query.l1CategoryId = f.l1CategoryId
    if (f.l2CategoryId) query.l2CategoryId = f.l2CategoryId
    if (industry.l2CategoryId) query.l2CategoryId = industry.l2CategoryId
    if (industry.l3CategoryId) query.l3CategoryId = industry.l3CategoryId
    if (f.dataSource) query.dataSource = f.dataSource
    if (f.productType) query.productType = f.productType
    if (f.deliveryMethod) query.deliveryMethod = f.deliveryMethod
    if (f.involvesPublicData === 'true') query.involvesPublicData = true
    if (f.involvesPublicData === 'false') query.involvesPublicData = false
    if (f.q.trim()) query.q = f.q.trim()
    return query
  }

  async function loadCategories() {
    categoriesState.value = 'loading'
    categoriesError.value = null
    try {
      const res = await listCategories()
      categories.value = res.data?.items ?? []
      categoriesState.value = categories.value.length === 0 ? 'empty' : 'ready'
    } catch (e) {
      categoriesState.value = 'error'
      categoriesError.value = e instanceof ApiError ? e.message : CATALOG_ERROR_MESSAGE
    }
  }

  async function loadProducts(opts: { append?: boolean } = {}) {
    const append = opts.append === true
    if (append) {
      if (loadingMore.value || !hasMore.value) return
      loadingMore.value = true
    } else {
      listState.value = 'loading'
      listError.value = null
      page.value = 1
    }

    const pageNo = append ? page.value + 1 : 1
    try {
      const res = await listProducts(buildQuery(pageNo))
      const items = res.data?.items ?? []
      total.value = res.data?.total ?? 0
      page.value = res.data?.page ?? pageNo
      pageSize.value = res.data?.pageSize ?? pageSize.value

      if (append) {
        const seen = new Set(products.value.map((p) => p.id))
        products.value = [...products.value, ...items.filter((p) => !seen.has(p.id))]
      } else {
        products.value = items
      }

      if (products.value.length === 0) {
        listState.value = 'empty'
        selectedProductId.value = null
        preview.value = null
        previewState.value = 'idle'
        return
      }
      listState.value = 'ready'
      const stillSelected = products.value.some((p) => p.id === selectedProductId.value)
      if (!stillSelected) {
        await selectProduct(products.value[0]!.id)
      }
    } catch (e) {
      if (!append) {
        listState.value = 'error'
        listError.value = e instanceof ApiError ? e.message : CATALOG_ERROR_MESSAGE
        products.value = []
        total.value = 0
      } else {
        listError.value = e instanceof ApiError ? e.message : CATALOG_ERROR_MESSAGE
      }
    } finally {
      loadingMore.value = false
    }
  }

  async function loadMore() {
    await loadProducts({ append: true })
  }

  async function selectProduct(productId: string) {
    selectedProductId.value = productId
    previewState.value = 'loading'
    previewError.value = null
    try {
      const res = await getProduct(productId)
      preview.value = res.data
      previewState.value = 'ready'
    } catch (e) {
      preview.value = null
      previewState.value = 'error'
      previewError.value = e instanceof ApiError ? e.message : '加载产品预览失败'
    }
  }

  function selectL1(l1Id: string) {
    filters.value.l1CategoryId = l1Id
    filters.value.l2CategoryId = ''
    filters.value.industryFilterId = ''
    void loadProducts()
  }

  function selectL2(l2Id: string) {
    filters.value.l2CategoryId = l2Id
    // 行业标签与顶栏行业标签对齐时清空冲突的筛选项
    if (filters.value.industryFilterId) {
      const ind = categories.value.find((c) => c.id === filters.value.industryFilterId)
      if (ind?.level === 'L2' && ind.id !== l2Id) filters.value.industryFilterId = ''
      if (ind?.level === 'L3' && ind.parentId !== l2Id) filters.value.industryFilterId = ''
    }
    void loadProducts()
  }

  function applyFilters() {
    void loadProducts()
  }

  function resetFilters() {
    const keepL1 = filters.value.l1CategoryId
    const keepL2 = filters.value.l2CategoryId
    filters.value = {
      ...createDefaultFilters(),
      l1CategoryId: keepL1,
      l2CategoryId: keepL2,
    }
    void loadProducts()
  }

  async function init() {
    await loadCategories()
    await loadProducts()
  }

  watch(
    () => filters.value.l1CategoryId,
    () => {
      const l2 = filters.value.l2CategoryId
      if (l2 && !l2Categories.value.some((c) => c.id === l2)) {
        filters.value.l2CategoryId = ''
      }
      const ind = filters.value.industryFilterId
      if (ind && !industryFilterOptions.value.some((o) => o.id === ind)) {
        filters.value.industryFilterId = ''
      }
    },
  )

  return {
    categories,
    products,
    sections,
    selectedProductId,
    preview,
    listState,
    categoriesState,
    previewState,
    loadingMore,
    hasMore,
    listError,
    categoriesError,
    previewError,
    filters,
    page,
    pageSize,
    total,
    l1Categories,
    l2Categories,
    l3Categories,
    industryFilterOptions,
    loadCategories,
    loadProducts,
    loadMore,
    selectProduct,
    selectL1,
    selectL2,
    applyFilters,
    resetFilters,
    init,
  }
}
