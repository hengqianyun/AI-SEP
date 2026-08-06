import { ref, watch, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Role } from '@/api/auth'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'

/**
 * 目录浏览页挂载 ImportDialog：?import=1 打开；关闭清 query（INTEGRATION.md / ISSUE-UX-R1-002）。
 */
export function useCatalogImportEntry(role: Ref<Role | null | undefined>) {
  const route = useRoute()
  const router = useRouter()
  const importOpen = ref(false)
  const { productImportVisible } = useCanWrite(role)

  watch(
    () => route.query.import,
    (v) => {
      if (v === '1' && productImportVisible.value) {
        importOpen.value = true
      }
    },
    { immediate: true },
  )

  function openImport() {
    if (!productImportVisible.value) return
    importOpen.value = true
  }

  function onImportClosed() {
    importOpen.value = false
    const q = { ...route.query }
    delete q.import
    // 导入宿主=我的数据产品 → 关闭回 /my-products（state-matrix 2.2.0）
    void router.replace({ path: '/my-products', query: q })
  }

  return {
    importOpen,
    productImportVisible,
    openImport,
    onImportClosed,
  }
}
