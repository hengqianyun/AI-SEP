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
  dataSource: string
  productType: string
  involvesPublicData: string
  deliveryMethod: string
  q: string
}

export function createDefaultFilters(): CatalogFilters {
  return {
    l1CategoryId: '',
    l2CategoryId: '',
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

export function useCatalogBrowse() {
  const categories = ref<Category[]>([])
  const products = ref<Product[]>([])
  const selectedProductId = ref<string | null>(null)
  const preview = ref<Product | null>(null)

  const listState = ref<LoadState>('idle')
  const categoriesState = ref<LoadState>('idle')
  const previewState = ref<LoadState>('idle')
  const listError = ref<string | null>(null)
  const categoriesError = ref<string | null>(null)
  const previewError = ref<string | null>(null)

  const filters = ref<CatalogFilters>(createDefaultFilters())
  const page = ref(1)
  const pageSize = ref(20)
  const total = ref(0)

  const l1Categories = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2Categories = computed(() => {
    const l1 = filters.value.l1CategoryId
    return categories.value.filter(
      (c) => c.level === 'L2' && (!l1 || c.parentId === l1),
    )
  })

  async function loadCategories() {
    categoriesState.value = 'loading'
    categoriesError.value = null
    try {
      const res = await listCategories()
      categories.value = res.data?.items ?? []
      categoriesState.value = categories.value.length === 0 ? 'empty' : 'ready'
    } catch (e) {
      categoriesState.value = 'error'
      categoriesError.value =
        e instanceof ApiError ? e.message : CATALOG_ERROR_MESSAGE
    }
  }

  async function loadProducts() {
    listState.value = 'loading'
    listError.value = null
    try {
      const f = filters.value
      const query: Record<string, string | number | boolean | undefined> = {
        page: page.value,
        pageSize: pageSize.value,
      }
      if (f.l1CategoryId) query.l1CategoryId = f.l1CategoryId
      if (f.l2CategoryId) query.l2CategoryId = f.l2CategoryId
      if (f.dataSource) query.dataSource = f.dataSource
      if (f.productType) query.productType = f.productType
      if (f.deliveryMethod) query.deliveryMethod = f.deliveryMethod
      if (f.involvesPublicData === 'true') query.involvesPublicData = true
      if (f.involvesPublicData === 'false') query.involvesPublicData = false
      if (f.q.trim()) query.q = f.q.trim()

      const res = await listProducts(query)
      products.value = res.data?.items ?? []
      total.value = res.data?.total ?? 0
      page.value = res.data?.page ?? page.value
      pageSize.value = res.data?.pageSize ?? pageSize.value

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
      listState.value = 'error'
      listError.value = e instanceof ApiError ? e.message : CATALOG_ERROR_MESSAGE
      products.value = []
      total.value = 0
    }
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
    page.value = 1
    void loadProducts()
  }

  function applyFilters() {
    page.value = 1
    void loadProducts()
  }

  function resetFilters() {
    const keepL1 = filters.value.l1CategoryId
    filters.value = { ...createDefaultFilters(), l1CategoryId: keepL1 }
    page.value = 1
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
      if (!l2) return
      const stillValid = l2Categories.value.some((c) => c.id === l2)
      if (!stillValid) filters.value.l2CategoryId = ''
    },
  )

  return {
    categories,
    products,
    selectedProductId,
    preview,
    listState,
    categoriesState,
    previewState,
    listError,
    categoriesError,
    previewError,
    filters,
    page,
    pageSize,
    total,
    l1Categories,
    l2Categories,
    loadCategories,
    loadProducts,
    selectProduct,
    selectL1,
    applyFilters,
    resetFilters,
    init,
  }
}
