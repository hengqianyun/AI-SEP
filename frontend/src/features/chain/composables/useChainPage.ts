import { computed, ref, watch } from 'vue'
import {
  getChainSnapshot,
  listChainVersions,
  type CatalogSnapshot,
  type ChainVersion,
} from '@/api/chain'
import { ApiError } from '@/api/client'

export type LoadState = 'idle' | 'loading' | 'empty' | 'ready' | 'error'

/** 契约 2.0.0 快照三级路径分段（api 类型尚未展开时在 feature 层补充）。 */
export type CategoryPathParts = { l1?: string; l2?: string; l3?: string }

export type CatalogSnapshotView = CatalogSnapshot & {
  categoryPathParts?: CategoryPathParts
}

export function useChainPage(productIdSource: () => string) {
  const versions = ref<ChainVersion[]>([])
  const selectedVersionId = ref<string | null>(null)
  const snapshot = ref<CatalogSnapshotView | null>(null)
  const listState = ref<LoadState>('idle')
  const snapshotState = ref<LoadState>('idle')
  const listError = ref<string | null>(null)
  const snapshotError = ref<string | null>(null)

  const productId = computed(() => productIdSource())

  async function loadVersions() {
    const id = productId.value
    listState.value = 'loading'
    listError.value = null
    versions.value = []
    selectedVersionId.value = null
    snapshot.value = null
    snapshotState.value = 'idle'
    snapshotError.value = null

    if (!id) {
      listState.value = 'empty'
      return
    }

    try {
      const res = await listChainVersions(id)
      const items = res.data?.items ?? []
      versions.value = items
      if (items.length === 0) {
        listState.value = 'empty'
        return
      }
      listState.value = 'ready'
      selectedVersionId.value = items[0]!.versionId
      await loadSnapshot(items[0]!.versionId)
    } catch (e) {
      listState.value = 'error'
      listError.value = e instanceof ApiError ? e.message : '加载上链版本失败'
    }
  }

  async function loadSnapshot(versionId: string) {
    selectedVersionId.value = versionId
    snapshotState.value = 'loading'
    snapshotError.value = null
    snapshot.value = null
    try {
      const res = await getChainSnapshot(versionId)
      snapshot.value = res.data as CatalogSnapshotView
      snapshotState.value = 'ready'
    } catch (e) {
      snapshotState.value = 'error'
      snapshotError.value = e instanceof ApiError ? e.message : '加载目录快照失败'
    }
  }

  function selectVersion(versionId: string) {
    if (versionId === selectedVersionId.value && snapshotState.value === 'ready') {
      return
    }
    void loadSnapshot(versionId)
  }

  watch(
    productId,
    () => {
      void loadVersions()
    },
    { immediate: true },
  )

  return {
    versions,
    selectedVersionId,
    snapshot,
    listState,
    snapshotState,
    listError,
    snapshotError,
    loadVersions,
    selectVersion,
  }
}

/** 纯函数：列表空态文案（可单测）。 */
export function chainEmptyMessage(): string {
  return '该产品暂无上链记录'
}

/** 纯函数：列表错误是否可重试展示。 */
export function isRetryableListState(state: LoadState): boolean {
  return state === 'error'
}

/** 三级路径展示文案：优先完整 path，否则由 parts 拼接。 */
export function formatCategoryPath(
  path?: string | null,
  parts?: CategoryPathParts | null,
): string {
  if (path && path.trim()) return path.trim()
  if (!parts) return ''
  return [parts.l1, parts.l2, parts.l3].filter((x) => Boolean(x && String(x).trim())).join(' / ')
}
