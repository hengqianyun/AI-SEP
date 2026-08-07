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

  it('preview delete: writeVisible gate + inline confirm strip testids', () => {
    expect(pageSrc).toMatch(/v-if="writeVisible"[\s\S]*?data-testid="catalog-go-delete"/)
    expect(pageSrc).toContain('data-testid="catalog-delete-confirm-strip"')
    expect(pageSrc).toContain('data-testid="catalog-delete-cancel"')
    expect(pageSrc).toContain('data-testid="catalog-delete-confirm"')
    expect(pageSrc).toContain('确定删除该产品？删除后不可恢复')
    expect(pageSrc).toContain('deleteProduct')
    expect(pageSrc).toContain('deleteConfirmVisible')
    // 公共目录无删除：删除按钮挂在 writeVisible（mine ∩ PROVIDER）下
    expect(pageSrc).toContain("isMine.value && productWriteVisible")
  })

  it('showSeatMap mount: prop override; mine defaults off; slot testid', () => {
    expect(pageSrc).toContain('showSeatMap?: boolean')
    expect(pageSrc).toContain('seatMapVisible')
    expect(pageSrc).toMatch(/v-if="seatMapVisible"[\s\S]*?CirculationSeatMap/)
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

describe('CatalogBrowsePage UX scheme A', () => {
  it('page-header only on mine surface (v-if isMine)', () => {
    expect(pageSrc).toMatch(/<header\s+v-if="isMine"\s+class="page-header/)
  })

  it('seat-map scroll collapse: class + handler + hysteresis', () => {
    expect(pageSrc).toContain('seat-map-slot--collapsed')
    expect(pageSrc).toContain('seatMapCollapsed')
    expect(pageSrc).toContain('findMainScrollContainer')
    expect(pageSrc).toContain('updateSeatMapCollapse')
    expect(pageSrc).toContain('COLLAPSE_SCROLL_TOP')
    expect(pageSrc).toContain('onCatalogWheel')
    expect(pageSrc).toContain('catalog-browse--pinned')
    expect(pageSrc).toContain('workspace-shell')
    expect(pageSrc).toContain('seat-map-slot-inner')
    expect(pageSrc).toMatch(/grid-template-rows:\s*1fr/)
    expect(pageSrc).toMatch(/\.seat-map-slot--collapsed\s*\{[^}]*grid-template-rows:\s*0fr/s)
    expect(pageSrc).toMatch(/\.seat-map-slot--collapsed\s*\{[^}]*pointer-events:\s*none/s)
    expect(pageSrc).not.toMatch(/\.seat-map-slot--collapsed\s*\{[^}]*display:\s*none/s)
    expect(pageSrc).toMatch(/prefers-reduced-motion:\s*reduce/)
  })

  it('workspace shell flex pin (no sticky overlap) + list min rows', () => {
    expect(pageSrc).toContain('sticky-chrome')
    expect(pageSrc).toContain('--min-visible-rows: 5')
    expect(pageSrc).toContain('--list-min-h')
    expect(pageSrc).toContain('--sticky-chrome-h')
    expect(pageSrc).toContain('--product-row-h: 48px')
    expect(pageSrc).toContain('--list-viewport-bonus')
    expect(pageSrc).toContain('updateStickyChromeHeight')
    expect(pageSrc).toContain('workspace-shell')
    expect(pageSrc).toMatch(/\.sticky-chrome\s*\{[^}]*position:\s*relative/s)
    expect(pageSrc).not.toMatch(/\.sticky-chrome\s*\{[^}]*position:\s*sticky/s)
    expect(pageSrc).toMatch(/\.catalog-browse--pinned\s*\{[^}]*overflow:\s*hidden/s)
    expect(pageSrc).toMatch(/\.nav-card,\s*\n\.filter-bar\s*\{[^}]*background:\s*var\(--card-bg\)/s)
    expect(pageSrc).toMatch(/min-height:\s*calc\(var\(--list-min-h\)\s*\+\s*var\(--product-row-h\)\)/)
    expect(pageSrc).toContain('100dvh')
    expect(pageSrc).not.toContain('max-height: 40vh')
    expect(pageSrc).toMatch(/@media\s*\(max-height:\s*800px\)/)
  })
})
