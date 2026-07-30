import { computed, ref } from 'vue'
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  type Category,
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
  const selectedL1Id = ref('')
  const renameMap = ref<Record<string, string>>({})

  const l1List = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2List = computed(() =>
    categories.value.filter((c) => c.level === 'L2' && c.parentId === selectedL1Id.value),
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
      dirty.value = false
      state.value = 'ready'
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '加载分类失败'
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
      feedback.value = '一级分类已保存'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '新增一级分类失败'
    }
  }

  async function addL2() {
    const name = draftL2Name.value.trim()
    if (!name || !selectedL1Id.value) return
    state.value = 'saving'
    try {
      await createCategory({ name, level: 'L2', parentId: selectedL1Id.value })
      draftL2Name.value = ''
      feedback.value = '二级分类已保存'
      state.value = 'success'
      await load()
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '新增二级分类失败'
    }
  }

  async function saveRename(id: string, level: 'L1' | 'L2') {
    const name = (renameMap.value[id] || '').trim()
    if (!name) return
    state.value = 'saving'
    try {
      await updateCategory(id, {
        name,
        level,
        parentId: level === 'L2' ? categories.value.find((c) => c.id === id)?.parentId || undefined : undefined,
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
        feedback.value =
          data?.reason ||
          e.message ||
          '删除失败'
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
    selectedL1Id,
    renameMap,
    l1List,
    l2List,
    load,
    markDirty,
    addL1,
    addL2,
    saveRename,
    remove,
  }
}
