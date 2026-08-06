import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { canImportProduct, canWriteProduct } from '@/features/auth/composables/useCanWrite'

const dir = dirname(fileURLToPath(import.meta.url))
const pageSrc = readFileSync(join(dir, 'CatalogBrowsePage.vue'), 'utf8')
const layoutSrc = readFileSync(join(dir, '../../../layouts/WorkbenchLayout.vue'), 'utf8')
const routesSrc = readFileSync(join(dir, '../../../router/routes.ts'), 'utf8')
const importEntrySrc = readFileSync(join(dir, 'composables/useCatalogImportEntry.ts'), 'utf8')

describe('CatalogBrowsePage (TASK-WSC-603)', () => {
  it('product write/import UI only PROVIDER; catalog mode gates via writeVisible=isMine', () => {
    expect(canWriteProduct('ADMIN')).toBe(false)
    expect(canWriteProduct('PROVIDER')).toBe(true)
    expect(canWriteProduct('USER')).toBe(false)
    expect(canImportProduct('ADMIN')).toBe(false)
    expect(canImportProduct('PROVIDER')).toBe(true)
    expect(canImportProduct('USER')).toBe(false)

    expect(pageSrc).toContain('data-testid="catalog-open-create"')
    expect(pageSrc).toMatch(/v-if="writeVisible"[\s\S]*?data-testid="catalog-open-create"/)
    expect(pageSrc).toContain("isMine.value && productWriteVisible")
    expect(pageSrc).toContain("path: '/catalog/products/new'")
    expect(pageSrc).toContain("from: 'mine'")
    expect(pageSrc).toContain('data-testid="catalog-open-import"')
  })

  it('showSeatMap mount: prop override; mine defaults off; slot testid', () => {
    expect(pageSrc).toContain('showSeatMap?: boolean')
    expect(pageSrc).toContain('seatMapVisible')
    expect(pageSrc).toContain('CirculationSeatMap v-if="seatMapVisible"')
    expect(pageSrc).toContain('data-testid="catalog-seat-map-slot"')
    expect(pageSrc).toContain('MINE_EMPTY_MESSAGE')
  })

  it('dual surface: my-products route + layout nav; admin-users preserved', () => {
    expect(routesSrc).toContain("path: '/my-products'")
    expect(routesSrc).toContain("mode: 'mine'")
    expect(routesSrc).toContain('showSeatMap: false')
    expect(routesSrc).toContain("path: '/admin/users'")
    expect(layoutSrc).toContain('nav-my-products')
    expect(layoutSrc).toContain('/my-products')
    expect(layoutSrc).toContain('myProductsVisible')
    expect(layoutSrc).toContain('nav-users')
    expect(layoutSrc).toContain('session-role-label')
    expect(layoutSrc).toContain("role.value === 'ADMIN'")
    expect(layoutSrc).toContain('onMineSurface')
  })

  it('FIND-WSC-603-R1-001: passes reactive isMine (not setup snapshot) into useCatalogBrowse', () => {
    expect(pageSrc).toMatch(/useCatalogBrowse\(\{\s*mine:\s*isMine\s*\}\)/)
    expect(pageSrc).not.toMatch(/useCatalogBrowse\(\{\s*mine:\s*isMine\.value\s*\}\)/)
  })

  it('import host closes to /my-products', () => {
    expect(importEntrySrc).toContain("path: '/my-products'")
    expect(importEntrySrc).not.toMatch(/replace\(\{\s*path:\s*'\/catalog'/)
  })

  it('keeps list sticky head flush: no top padding on list pane', () => {
    expect(pageSrc).toContain('position: sticky')
    expect(pageSrc).toContain('.list-head')
    expect(pageSrc).toMatch(/\.pane\.list\s*\{[^}]*padding:\s*0\s+20px\s+20px/s)
  })
})
