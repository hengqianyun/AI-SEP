/**
 * 订单列表 composable — 分页、九组字段、跳页、pageSize。
 * REQ-WSC-ORDER-007/008/009：列表分页、字段、查看进详情。
 * RULE-GLOBAL-FRONTEND §3：分页须支持跳页与更换每页条数；pageSize 变更回第 1 页。
 */
import { computed, ref } from 'vue'
import { listOrders, type OrderListItem, type OrderStatus } from '@/api/orders'

export type OrderListState = 'idle' | 'loading' | 'error'

export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100] as const
export const DEFAULT_PAGE_SIZE = 10

export function useOrderList() {
  const state = ref<OrderListState>('idle')
  const error = ref<string | null>(null)
  const items = ref<OrderListItem[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(DEFAULT_PAGE_SIZE)
  const keyword = ref('')
  const status = ref<OrderStatus | undefined>(undefined)

  /** pageSize 选项标签（与 CatalogMaintenancePage 保持一致） */
  const pageSizeOptions = computed(() =>
    PAGE_SIZE_OPTIONS.map(String),
  )

  async function fetch() {
    state.value = 'loading'
    error.value = null
    try {
      const res = await listOrders({
        page: page.value,
        pageSize: pageSize.value as 10 | 20 | 50 | 100,
        keyword: keyword.value || undefined,
        status: status.value,
      })
      items.value = res.data.list
      total.value = res.data.total
      state.value = 'idle'
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '加载失败'
    }
  }

  /** 翻页（不改 pageSize） */
  function onPageChange(nextPage: number, _nextSize: number) {
    page.value = nextPage
    void fetch()
  }

  /** pageSize 变化 → page 回 1（RULE-GLOBAL-FRONTEND §3） */
  function onShowSizeChange(_current: number, size: number) {
    pageSize.value = size
    page.value = 1
    void fetch()
  }

  function init() {
    page.value = 1
    void fetch()
  }

  return {
    state,
    error,
    items,
    total,
    page,
    pageSize,
    keyword,
    status,
    pageSizeOptions,
    fetch,
    onPageChange,
    onShowSizeChange,
    init,
  }
}
