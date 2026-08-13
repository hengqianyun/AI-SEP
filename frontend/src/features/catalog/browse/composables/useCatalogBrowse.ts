import { computed, ref, toValue, watch, type MaybeRefOrGetter } from 'vue'
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
  /** GB/T 4754 门类中文原文（产品字段 industryCategory；DEC-WSC-006 / TASK-WSC-606） */
  industryCategory: string
  dataSource: string
  productType: string
  involvesPublicData: string
  deliveryMethod: string
  /** 产品名/编码检索（与 supplierName 语义分离；REQ-CAT-012） */
  q: string
  /**
   * 企业名称 → 产品字段 supplierName 模糊匹配。
   * query 键必须为 supplierName，禁止 enterpriseName（REQ-CAT-012 / TASK-WSC-705）。
   */
  supplierName: string
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
    industryCategory: '',
    dataSource: '',
    productType: '',
    involvesPublicData: '',
    deliveryMethod: '',
    q: '',
    supplierName: '',
  }
}

export function catalogEmptyMessage(): string {
  return CATALOG_EMPTY_MESSAGE
}

export function isRetryableListState(state: LoadState): boolean {
  return state === 'error'
}

/** 视觉分区：无 l3 挂载产品的分节 key（钉在已加载列表末段）。 */
export const UNCATEGORIZED_SECTION_KEY = '__uncategorized__'

/**
 * 将扁平产品列表分节：有 l3 按三级子类；无挂载一律归入单一「未分类数据」分节并钉在末段。
 * 桶内顺序保持 API/追加顺序，**不对**产品做跨页破坏性 sort（REQ-CAT-012 / TASK-WSC-705）。
 * industryCategory 仅为产品字段，不作分节 key（DEC-WSC-006 / TASK-WSC-502）。
 */
export function groupProductsByL3(
  products: Product[],
  categories: Category[],
): ProductSection[] {
  const l3Order = categories.filter((c) => c.level === 'L3').map((c) => c.id)
  const l3Name = new Map(categories.filter((c) => c.level === 'L3').map((c) => [c.id, c.name]))
  const buckets = new Map<string, Product[]>()

  // 按 API 出现顺序入桶，桶内不重排
  for (const p of products) {
    const key = p.l3CategoryId || UNCATEGORIZED_SECTION_KEY
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

  // 其余已知路径外的 L3 桶先输出；「未分类」强制殿后（视觉分区，非跨页 sort）
  let uncategorized: Product[] | undefined
  for (const [id, list] of buckets) {
    if (id === UNCATEGORIZED_SECTION_KEY || id === '__unknown__') {
      uncategorized = uncategorized ? uncategorized.concat(list) : list
      continue
    }
    const title = list[0]?.categoryPath?.split('/').pop()?.trim() ?? id
    sections.push({ l3CategoryId: id, title, count: list.length, products: list })
  }
  if (uncategorized && uncategorized.length > 0) {
    sections.push({
      l3CategoryId: UNCATEGORIZED_SECTION_KEY,
      title: '未分类数据',
      count: uncategorized.length,
      products: uncategorized,
    })
  }
  return sections
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

export function useCatalogBrowse(options: { mine?: MaybeRefOrGetter<boolean> } = {}) {
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

  /** 响应 mine（catalog↔my-products 路由复用时不得快照布尔） */
  function isMineList(): boolean {
    return toValue(options.mine) === true
  }

  const l1Categories = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2Categories = computed(() => {
    const l1 = filters.value.l1CategoryId
    return categories.value.filter((c) => c.level === 'L2' && (!l1 || c.parentId === l1))
  })
  const l3Categories = computed(() => {
    const l2Ids = new Set(l2Categories.value.map((c) => c.id))
    return categories.value.filter((c) => c.level === 'L3' && l2Ids.has(c.parentId ?? ''))
  })

  const sections = computed(() => groupProductsByL3(products.value, categories.value))
  const hasMore = computed(() => products.value.length < total.value)

  function buildQuery(pageNo: number) {
    const f = filters.value
    const query: Record<string, string | number | boolean | undefined> = {
      page: pageNo,
      pageSize: pageSize.value,
    }
    if (f.l1CategoryId) query.l1CategoryId = f.l1CategoryId
    if (f.l2CategoryId) query.l2CategoryId = f.l2CategoryId
    // REQ-CAT-011：UI 文案为「行业类别」，query 键仍为 industryCategory
    if (f.industryCategory.trim()) query.industryCategory = f.industryCategory.trim()
    if (f.dataSource) query.dataSource = f.dataSource
    if (f.productType) query.productType = f.productType
    if (f.deliveryMethod) query.deliveryMethod = f.deliveryMethod
    if (f.involvesPublicData === 'true') query.involvesPublicData = true
    if (f.involvesPublicData === 'false') query.involvesPublicData = false
    // q：产品名/编码；supplierName：企业名称（产品字段）；二者并存、语义分离
    if (f.q.trim()) query.q = f.q.trim()
    if (f.supplierName.trim()) query.supplierName = f.supplierName.trim()
    // 禁止发送 enterpriseName（会话/主体名 ≠ 产品检索字段）
    if (isMineList()) query.mine = true
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
    void loadProducts()
  }

  function selectL2(l2Id: string) {
    filters.value.l2CategoryId = l2Id
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
    },
  )

  /** 路由复用时 mode/mine 变化须重拉列表（FIND-WSC-603-R1-001） */
  watch(isMineList, () => {
    void loadProducts()
  })

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



