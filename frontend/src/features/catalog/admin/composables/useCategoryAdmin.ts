import { computed, ref } from 'vue'
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  type Category,
  type CategoryLevel,
} from '@/api/catalog'
import { ApiError } from '@/api/client'

export type AdminState = 'idle' | 'loading' | 'ready' | 'saving' | 'error' | 'success'

export function useCategoryAdmin() {
  const state = ref<AdminState>('idle')
  const feedback = ref('')
  const dirty = ref(false)
  const categories = ref<Category[]>([])
  const draftL1Name = ref('')
  const draftL2Name = ref('')
  const draftL3Name = ref('')
  const selectedL1Id = ref('')
  const selectedL2Id = ref('')
  const renameMap = ref<Record<string, string>>({})

  const l1List = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2List = computed(() =>
    categories.value.filter((c) => c.level === 'L2' && c.parentId === selectedL1Id.value),
  )
  const l3List = computed(() =>
    categories.value.filter((c) => c.level === 'L3' && c.parentId === selectedL2Id.value),
  )

  async function load() {
    state.value = 'loading'
    feedback.value = ''
    try {
      const res = await listCategories()
      categories.value = res.data.items
      const map: Record<string, string> = {}
      for (const c of categories.value) map[c.id] = c.name
      renameMap.value = map

      if (!selectedL1Id.value && l1List.value.length) {
        selectedL1Id.value = l1List.value[0].id
      }
      syncL2Selection()
      dirty.value = false
      state.value = 'ready'
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '加载分类失败'
    }
  }

  function selectL1(id: string) {
    selectedL1Id.value = id
    syncL2Selection()
  }

  function selectL2(id: string) {
    selectedL2Id.value = id
  }

  function syncL2Selection() {
    const l2 = l2List.value
    if (!l2.length) {
      selectedL2Id.value = ''
      return
    }
    if (!l2.some((c) => c.id === selectedL2Id.value)) {
      selectedL2Id.value = l2[0].id
    }
  }

  function markDirty() {
    dirty.value = true
  }

  async function addL1() {
    const name = draftL1Name.value.trim()
    if (!name) return
    state.value = 'saving'
    try {
      await createCategory({ name, level: 'L1' })
      draftL1Name.value = ''
      feedback.value = '空间（一级）已保存'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '新增空间失败'
    }
  }

  async function addL2() {
    const name = draftL2Name.value.trim()
    if (!name || !selectedL1Id.value) return
    state.value = 'saving'
    try {
      await createCategory({ name, level: 'L2', parentId: selectedL1Id.value })
      draftL2Name.value = ''
      feedback.value = '行业（二级）已保存'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '新增行业失败'
    }
  }

  async function addL3() {
    const name = draftL3Name.value.trim()
    if (!name || !selectedL2Id.value) return
    state.value = 'saving'
    try {
      await createCategory({ name, level: 'L3', parentId: selectedL2Id.value })
      draftL3Name.value = ''
      feedback.value = '子类（三级）已保存'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '新增子类失败'
    }
  }

  async function saveRename(id: string, level: CategoryLevel) {
    const name = (renameMap.value[id] || '').trim()
    if (!name) return
    const current = categories.value.find((c) => c.id === id)
    state.value = 'saving'
    try {
      await updateCategory(id, {
        name,
        level,
        parentId:
          level === 'L1' ? undefined : current?.parentId || undefined,
      })
      dirty.value = false
      feedback.value = '分类已更新'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '更新失败'
    }
  }

  async function remove(id: string) {
    state.value = 'saving'
    try {
      await deleteCategory(id)
      feedback.value = '分类已删除'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      if (e instanceof ApiError) {
        const data = e.data as { reason?: string; productCount?: number } | undefined
        feedback.value = data?.reason || e.message || '删除失败'
      } else {
        feedback.value = e instanceof Error ? e.message : '删除失败'
      }
    }
  }

  return {
    state,
    feedback,
    dirty,
    draftL1Name,
    draftL2Name,
    draftL3Name,
    selectedL1Id,
    selectedL2Id,
    renameMap,
    l1List,
    l2List,
    l3List,
    load,
    selectL1,
    selectL2,
    markDirty,
    addL1,
    addL2,
    addL3,
    saveRename,
    remove,
  }
}
