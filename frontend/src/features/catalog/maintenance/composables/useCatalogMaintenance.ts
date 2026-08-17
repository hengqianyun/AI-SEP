import { computed, ref, toValue, watch, type MaybeRefOrGetter } from 'vue'
import {
  listCategories,
  listMaintenanceEntries,
  type Category,
  type MaintenanceEntry,
  type MaintenanceScope,
  type MaintenanceStatus,
} from '@/api/catalog'
import { ApiError, apiRequest } from '@/api/client'
import {
  canMaintainCatalog,
  canSeeMyCatalog,
} from '@/features/auth/composables/useCanWrite'
import type { Role } from '@/api/auth'

export type MaintenanceListState = 'idle' | 'loading' | 'ready' | 'saving' | 'error' | 'success'
export type StatusTab = 'ALL' | 'MAINTAINED' | 'PENDING'

/** REQ-CAT-013：表头本页全选可见文案 */
export const PAGE_SELECT_ALL_LABEL = '本页全选'
/** REQ-CAT-013：批量主 CTA，禁止「批量保存」跨页暗示 */
export const UNIFIED_MAINTAIN_LABEL = '统一维护'

/** 目录维护 / 我的目录每页条数可选档（≤ 服务端 maximum 100） */
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100] as const
/** 前端默认每页条数（保持现网 10，不跟 OpenAPI schema default 20） */
export const DEFAULT_PAGE_SIZE = 10
/** Pagination `page-size-options` 字面量（ant-design-vue 接受 string | number） */
export const PAGE_SIZE_OPTION_LABELS = PAGE_SIZE_OPTIONS.map(String)

/**
 * 将 pageSize 钳到合法档 10 / 20 / 50 / 100。
 * 非有限数字回落默认 10；超出两端取最近端点；中间非法值取最近档（等距取较小档）。
 *
 * @param value 候选 pageSize
 */
export function clampPageSize(value: number): (typeof PAGE_SIZE_OPTIONS)[number] {
  if (!Number.isFinite(value)) return DEFAULT_PAGE_SIZE
  let best: (typeof PAGE_SIZE_OPTIONS)[number] = PAGE_SIZE_OPTIONS[0]
  let bestDist = Math.abs(value - best)
  for (const option of PAGE_SIZE_OPTIONS) {
    const dist = Math.abs(value - option)
    if (dist < bestDist) {
      best = option
      bestDist = dist
    }
  }
  return best
}

/**
 * 批量条本页范围文案。
 *
 * @param count 本页已选条数
 */
export function batchSelectedCopy(count: number): string {
  return `已选择 ${count} 条（本页）`
}

/**
 * 页级可达：full 仅 ADMIN；myCatalog 为 ADMIN+PROVIDER。
 *
 * @param scope 维护 scope
 * @param role 会话角色
 */
export function canEnterMaintenancePage(
  scope: MaintenanceScope,
  role: Role | null | undefined,
): boolean {
  return scope === 'myCatalog' ? canSeeMyCatalog(role) : canMaintainCatalog(role)
}

/**
 * 将 Cascader 路径映射为 l1/l2/l3（筛选允许停在父节点）。
 *
 * @param path Cascader 选中路径
 */
export function categoryIdsFromPath(path: string[]): {
  l1CategoryId: string
  l2CategoryId: string
  l3CategoryId: string
} {
  return {
    l1CategoryId: path[0] ?? '',
    l2CategoryId: path[1] ?? '',
    l3CategoryId: path[2] ?? '',
  }
}

/**
 * 由已选 l1/l2/l3 还原 Cascader 绑定值。
 *
 * @param l1 一级 id
 * @param l2 二级 id
 * @param l3 三级 id
 */
export function pathFromCategoryIds(l1: string, l2: string, l3: string): string[] {
  const path: string[] = []
  if (l1) path.push(l1)
  if (l2) path.push(l2)
  if (l3) path.push(l3)
  return path
}

/**
 * 由扁平分类列表构建 L1→L2→L3 Cascader 树。
 *
 * @param categories 全量分类
 */
export function buildMaintenanceCascaderOptions(categories: Category[]) {
  const l2s = categories.filter((c) => c.level === 'L2')
  const l3s = categories.filter((c) => c.level === 'L3')
  return categories
    .filter((c) => c.level === 'L1')
    .map((l1) => ({
      value: l1.id,
      label: l1.name,
      children: l2s
        .filter((l2) => l2.parentId === l1.id)
        .map((l2) => ({
          value: l2.id,
          label: l2.name,
          children: l3s
            .filter((l3) => l3.parentId === l2.id)
            .map((l3) => ({ value: l3.id, label: l3.name })),
        })),
    }))
}

/**
 * 目录维护列表与写路径。
 * scope 须为 MaybeRefOrGetter：双入口复用同一页实例时不得在 setup 快照成常量。
 *
 * @param scopeSource full=全平台；myCatalog=本企业/本人
 */
export function useCatalogMaintenance(
  scopeSource: MaybeRefOrGetter<MaintenanceScope> = 'full',
) {
  const currentScope = computed(() => toValue(scopeSource))
  const state = ref<MaintenanceListState>('idle')
  const feedback = ref('')
  const entries = ref<MaintenanceEntry[]>([])
  const categories = ref<Category[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(DEFAULT_PAGE_SIZE)

  const statusTab = ref<StatusTab>('ALL')
  const filterL1Id = ref('')
  const filterL2Id = ref('')
  const filterL3Id = ref('')

  const selectedIds = ref<Set<string>>(new Set())
  const editingId = ref<string | null>(null)
  const editL1Id = ref('')
  const editL2Id = ref('')
  const editL3Id = ref('')

  const batchL1Id = ref('')
  const batchL2Id = ref('')
  const batchL3Id = ref('')

  const cascaderOptions = computed(() => buildMaintenanceCascaderOptions(categories.value))
  const filterPath = computed(() =>
    pathFromCategoryIds(filterL1Id.value, filterL2Id.value, filterL3Id.value),
  )
  const editPath = computed(() =>
    pathFromCategoryIds(editL1Id.value, editL2Id.value, editL3Id.value),
  )
  const batchPath = computed(() =>
    pathFromCategoryIds(batchL1Id.value, batchL2Id.value, batchL3Id.value),
  )

  const selectedCount = computed(() => selectedIds.value.size)
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
  const allPageSelected = computed(
    () =>
      entries.value.length > 0 && entries.value.every((e) => selectedIds.value.has(e.id)),
  )
  const somePageSelected = computed(
    () => entries.value.some((e) => selectedIds.value.has(e.id)) && !allPageSelected.value,
  )
  const batchBarCopy = computed(() => batchSelectedCopy(selectedCount.value))

  function clearSelection() {
    selectedIds.value = new Set()
  }

  async function loadCategories() {
    const res = await listCategories()
    categories.value = res.data.items
  }

  async function loadEntries(options?: { keepFeedback?: boolean }) {
    state.value = 'loading'
    if (!options?.keepFeedback) {
      feedback.value = ''
    }
    try {
      const res = await listMaintenanceEntries({
        scope: currentScope.value,
        status: statusTab.value,
        l1CategoryId: filterL1Id.value || undefined,
        l2CategoryId: filterL2Id.value || undefined,
        l3CategoryId: filterL3Id.value || undefined,
        page: page.value,
        pageSize: pageSize.value,
      })
      entries.value = res.data.items
      total.value = res.data.total
      page.value = res.data.page
      pageSize.value = res.data.pageSize
      state.value = 'ready'
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '加载目录维护列表失败'
    }
  }

  async function init() {
    try {
      await loadCategories()
      await loadEntries()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '初始化失败'
    }
  }

  function setStatusTab(tab: StatusTab) {
    statusTab.value = tab
    page.value = 1
    clearSelection()
    return loadEntries()
  }

  function applyFilterPath(path: string[]) {
    const ids = categoryIdsFromPath(path)
    filterL1Id.value = ids.l1CategoryId
    filterL2Id.value = ids.l2CategoryId
    filterL3Id.value = ids.l3CategoryId
    page.value = 1
    clearSelection()
    return loadEntries()
  }

  function setFilterL1(id: string) {
    return applyFilterPath(id ? [id] : [])
  }

  function setFilterL2(id: string) {
    return applyFilterPath(pathFromCategoryIds(filterL1Id.value, id, ''))
  }

  function setFilterL3(id: string) {
    return applyFilterPath(pathFromCategoryIds(filterL1Id.value, filterL2Id.value, id))
  }

  function goPage(p: number) {
    const next = Math.min(Math.max(p, 1), totalPages.value)
    if (next === page.value) {
      return Promise.resolve()
    }
    page.value = next
    clearSelection()
    return loadEntries()
  }

  /**
   * 变更每页条数：钳到合法档、回到第 1 页、清本页勾选后重拉。
   * 不可走 goPage(1)：已在第 1 页时 goPage 会直接 return，列表不会刷新。
   *
   * @param size 目标 pageSize
   */
  function setPageSize(size: number) {
    return applyPagination(1, size)
  }

  /**
   * antdv `changePageSize` 同栈先 `showSizeChange(current, size)` 再 `change(current, size)`，
   * 且不把 current 重置为 1。size 分支写入 page=1 后须忽略紧随的 page-only goPage，
   * 否则会冲掉第 1 页（FIND-WSC-910-001）。微任务后恢复，仅翻页仍走 goPage。
   */
  let suppressPageOnlyAfterSizeChange = false

  /**
   * 分页条统一入口：pageSize 变化则重置到第 1 页并重拉；仅翻页则委托 goPage。
   * ant-design-vue `@change(page, pageSize)` 与 `@showSizeChange(current, size)` 都接到此函数，避免漏刷新或双逻辑分叉。
   *
   * @param nextPage 目标页码
   * @param nextSize 目标每页条数；缺省表示沿用当前 pageSize
   */
  function applyPagination(nextPage: number, nextSize?: number) {
    const size = clampPageSize(nextSize ?? pageSize.value)
    if (size !== pageSize.value) {
      pageSize.value = size
      page.value = 1
      clearSelection()
      suppressPageOnlyAfterSizeChange = true
      queueMicrotask(() => {
        suppressPageOnlyAfterSizeChange = false
      })
      return loadEntries()
    }
    if (suppressPageOnlyAfterSizeChange) {
      return Promise.resolve()
    }
    return goPage(nextPage)
  }

  function toggleSelect(id: string, checked: boolean) {
    const next = new Set(selectedIds.value)
    if (checked) next.add(id)
    else next.delete(id)
    selectedIds.value = next
  }

  function toggleSelectAllOnPage(checked: boolean) {
    const next = new Set(selectedIds.value)
    for (const entry of entries.value) {
      if (checked) next.add(entry.id)
      else next.delete(entry.id)
    }
    selectedIds.value = next
  }

  function startEdit(entry: MaintenanceEntry) {
    editingId.value = entry.id
    const l3 = entry.l3CategoryId || ''
    editL3Id.value = l3
    if (l3) {
      const l3Cat = categories.value.find((c) => c.id === l3)
      editL2Id.value = l3Cat?.parentId || ''
      const l2Cat = categories.value.find((c) => c.id === editL2Id.value)
      editL1Id.value = l2Cat?.parentId || ''
    } else {
      editL1Id.value = ''
      editL2Id.value = ''
    }
  }

  function cancelEdit() {
    editingId.value = null
    editL1Id.value = ''
    editL2Id.value = ''
    editL3Id.value = ''
  }

  function applyEditPath(path: string[]) {
    const ids = categoryIdsFromPath(path)
    editL1Id.value = ids.l1CategoryId
    editL2Id.value = ids.l2CategoryId
    editL3Id.value = ids.l3CategoryId
  }

  function applyBatchPath(path: string[]) {
    const ids = categoryIdsFromPath(path)
    batchL1Id.value = ids.l1CategoryId
    batchL2Id.value = ids.l2CategoryId
    batchL3Id.value = ids.l3CategoryId
  }

  function onEditL1Change(id: string) {
    applyEditPath(id ? [id] : [])
  }

  function onEditL2Change(id: string) {
    applyEditPath(pathFromCategoryIds(editL1Id.value, id, ''))
  }

  function onBatchL1Change(id: string) {
    applyBatchPath(id ? [id] : [])
  }

  function onBatchL2Change(id: string) {
    applyBatchPath(pathFromCategoryIds(batchL1Id.value, id, ''))
  }

  async function saveEdit() {
    if (!editingId.value || !editL3Id.value) {
      feedback.value = '请选择三级分类'
      return
    }
    state.value = 'saving'
    feedback.value = ''
    try {
      await apiRequest<MaintenanceEntry>(
        `/catalog/maintenance/entries/${encodeURIComponent(editingId.value)}?scope=${currentScope.value}`,
        {
          method: 'PUT',
          body: JSON.stringify({ l3CategoryId: editL3Id.value }),
        },
      )
      feedback.value = '已保存关联'
      state.value = 'success'
      cancelEdit()
      await loadEntries({ keepFeedback: true })
    } catch (e) {
      state.value = 'error'
      feedback.value =
        e instanceof ApiError ? e.message : e instanceof Error ? e.message : '保存失败'
    }
  }

  async function saveBatch() {
    if (!selectedIds.value.size) {
      feedback.value = '请先勾选条目'
      return
    }
    if (!batchL3Id.value) {
      feedback.value = '请选择批量关联的三级分类'
      return
    }
    state.value = 'saving'
    feedback.value = ''
    try {
      const res = await apiRequest<{
        successCount: number
        failureCount: number
        failures?: { productId: string; reasonCode: string; reasonMessage: string }[]
      }>(`/catalog/maintenance/entries/batch?scope=${currentScope.value}`, {
        method: 'POST',
        body: JSON.stringify({
          productIds: [...selectedIds.value],
          l3CategoryId: batchL3Id.value,
        }),
      })
      feedback.value = `批量完成：成功 ${res.data.successCount}，失败 ${res.data.failureCount}`
      state.value = 'success'
      selectedIds.value = new Set()
      await loadEntries({ keepFeedback: true })
    } catch (e) {
      state.value = 'error'
      feedback.value =
        e instanceof ApiError ? e.message : e instanceof Error ? e.message : '批量保存失败'
    }
  }

  function statusLabel(s: MaintenanceStatus) {
    return s === 'MAINTAINED' ? '已维护' : '待关联'
  }

  /** 路由复用时 props.scope 变化须重拉列表并清选择（FIND-WSC-907-001） */
  watch(currentScope, () => {
    page.value = 1
    clearSelection()
    cancelEdit()
    void init()
  })

  return {
    scope: currentScope,
    state,
    feedback,
    entries,
    categories,
    total,
    page,
    pageSize,
    statusTab,
    filterL1Id,
    filterL2Id,
    filterL3Id,
    filterPath,
    selectedIds,
    editingId,
    editL1Id,
    editL2Id,
    editL3Id,
    editPath,
    batchL1Id,
    batchL2Id,
    batchL3Id,
    batchPath,
    cascaderOptions,
    selectedCount,
    totalPages,
    allPageSelected,
    somePageSelected,
    batchBarCopy,
    init,
    loadEntries,
    setStatusTab,
    applyFilterPath,
    setFilterL1,
    setFilterL2,
    setFilterL3,
    goPage,
    setPageSize,
    applyPagination,
    toggleSelect,
    toggleSelectAllOnPage,
    startEdit,
    cancelEdit,
    applyEditPath,
    applyBatchPath,
    onEditL1Change,
    onEditL2Change,
    onBatchL1Change,
    onBatchL2Change,
    saveEdit,
    saveBatch,
    statusLabel,
    PAGE_SELECT_ALL_LABEL,
    UNIFIED_MAINTAIN_LABEL,
  }
}
