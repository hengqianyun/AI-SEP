import { describe, expect, it, vi, beforeEach } from 'vitest'
import { nextTick, reactive, ref } from 'vue'
import type { Role } from '@/api/auth'

const routeState = reactive<{ query: Record<string, string | string[] | undefined> }>({
  query: {},
})
const replace = vi.fn(async (loc: { path: string; query: Record<string, unknown> }) => {
  routeState.query = { ...(loc.query as Record<string, string | undefined>) }
})

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({ replace }),
}))

import { useCatalogImportEntry } from './useCatalogImportEntry'

describe('useCatalogImportEntry (FIND-001 / INTEGRATION.md)', () => {
  beforeEach(() => {
    routeState.query = {}
    replace.mockClear()
  })

  it('opens ImportDialog when route.query.import === "1" and role can import', async () => {
    const role = ref<Role | null>('ADMIN')
    routeState.query = { import: '1' }
    const api = useCatalogImportEntry(role)
    await nextTick()
    expect(api.productImportVisible.value).toBe(true)
    expect(api.importOpen.value).toBe(true)
  })

  it('does not open when import=1 but role cannot import', async () => {
    const role = ref<Role | null>('USER')
    routeState.query = { import: '1' }
    const api = useCatalogImportEntry(role)
    await nextTick()
    expect(api.productImportVisible.value).toBe(false)
    expect(api.importOpen.value).toBe(false)
  })

  it('openImport sets importOpen for ADMIN/PROVIDER', () => {
    const role = ref<Role | null>('PROVIDER')
    const api = useCatalogImportEntry(role)
    api.openImport()
    expect(api.importOpen.value).toBe(true)
  })

  it('onImportClosed clears import query and returns to catalog surface', async () => {
    const role = ref<Role | null>('ADMIN')
    routeState.query = { import: '1', q: 'med' }
    const api = useCatalogImportEntry(role)
    await nextTick()
    expect(api.importOpen.value).toBe(true)

    api.onImportClosed()
    expect(api.importOpen.value).toBe(false)
    expect(replace).toHaveBeenCalledWith({
      path: '/catalog',
      query: { q: 'med' },
    })
  })
})
