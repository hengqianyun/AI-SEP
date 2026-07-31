import { computed, ref } from 'vue'
import {
  batchUpdateMaintenanceEntries,
  listCategories,
  listMaintenanceEntries,
  updateMaintenanceEntry,
  type Category,
  type MaintenanceEntry,
  type MaintenanceStatus,
} from '@/api/catalog'
import { ApiError } from '@/api/client'

export type MaintenanceListState = 'idle' | 'loading' | 'ready' | 'saving' | 'error' | 'success'
export type StatusTab = 'ALL' | 'MAINTAINED' | 'PENDING'

export function useCatalogMaintenance() {
  const state = ref<MaintenanceListState>('idle')
  const feedback = ref('')
  const entries = ref<MaintenanceEntry[]>([])
  const categories = ref<Category[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(10)

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

  const l1Options = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2Options = computed(() => {
    const all = categories.value.filter((c) => c.level === 'L2')
    if (!filterL1Id.value) return all
    return all.filter((c) => c.parentId === filterL1Id.value)
  })
  const l3Options = computed(() => {
    const all = categories.value.filter((c) => c.level === 'L3')
    if (filterL2Id.value) return all.filter((c) => c.parentId === filterL2Id.value)
    if (filterL1Id.value) {
      const l2Ids = new Set(
        categories.value
          .filter((c) => c.level === 'L2' && c.parentId === filterL1Id.value)
          .map((c) => c.id),
      )
      return all.filter((c) => c.parentId && l2Ids.has(c.parentId))
    }
    return all
  })

  const editL2Options = computed(() => {
    const all = categories.value.filter((c) => c.level === 'L2')
    if (!editL1Id.value) return all
    return all.filter((c) => c.parentId === editL1Id.value)
  })
  const editL3Options = computed(() => {
    const all = categories.value.filter((c) => c.level === 'L3')
    if (!editL2Id.value) return all
    return all.filter((c) => c.parentId === editL2Id.value)
  })

  const batchL2Options = computed(() => {
    const all = categories.value.filter((c) => c.level === 'L2')
    if (!batchL1Id.value) return all
    return all.filter((c) => c.parentId === batchL1Id.value)
  })
  const batchL3Options = computed(() => {
    const all = categories.value.filter((c) => c.level === 'L3')
    if (!batchL2Id.value) return all
    return all.filter((c) => c.parentId === batchL2Id.value)
  })

  const selectedCount = computed(() => selectedIds.value.size)
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

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
    return loadEntries()
  }

  function setFilterL1(id: string) {
    filterL1Id.value = id
    filterL2Id.value = ''
    filterL3Id.value = ''
    page.value = 1
    return loadEntries()
  }

  function setFilterL2(id: string) {
    filterL2Id.value = id
    filterL3Id.value = ''
    page.value = 1
    return loadEntries()
  }

  function setFilterL3(id: string) {
    filterL3Id.value = id
    page.value = 1
    return loadEntries()
  }

  function goPage(p: number) {
    const next = Math.min(Math.max(p, 1), totalPages.value)
    if (next === page.value) return Promise.resolve()
    page.value = next
    return loadEntries()
  }

  function toggleSelect(id: string, checked: boolean) {
    const next = new Set(selectedIds.value)
    if (checked) next.add(id)
    else next.delete(id)
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

  function onEditL1Change(id: string) {
    editL1Id.value = id
    editL2Id.value = ''
    editL3Id.value = ''
  }

  function onEditL2Change(id: string) {
    editL2Id.value = id
    editL3Id.value = ''
  }

  function onBatchL1Change(id: string) {
    batchL1Id.value = id
    batchL2Id.value = ''
    batchL3Id.value = ''
  }

  function onBatchL2Change(id: string) {
    batchL2Id.value = id
    batchL3Id.value = ''
  }

  async function saveEdit() {
    if (!editingId.value || !editL3Id.value) {
      feedback.value = '请选择三级分类'
      return
    }
    state.value = 'saving'
    feedback.value = ''
    try {
      await updateMaintenanceEntry(editingId.value, { l3CategoryId: editL3Id.value })
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
      const res = await batchUpdateMaintenanceEntries({
        productIds: [...selectedIds.value],
        l3CategoryId: batchL3Id.value,
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

  return {
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
    selectedIds,
    editingId,
    editL1Id,
    editL2Id,
    editL3Id,
    batchL1Id,
    batchL2Id,
    batchL3Id,
    l1Options,
    l2Options,
    l3Options,
    editL2Options,
    editL3Options,
    batchL2Options,
    batchL3Options,
    selectedCount,
    totalPages,
    init,
    loadEntries,
    setStatusTab,
    setFilterL1,
    setFilterL2,
    setFilterL3,
    goPage,
    toggleSelect,
    startEdit,
    cancelEdit,
    onEditL1Change,
    onEditL2Change,
    onBatchL1Change,
    onBatchL2Change,
    saveEdit,
    saveBatch,
    statusLabel,
  }
}
