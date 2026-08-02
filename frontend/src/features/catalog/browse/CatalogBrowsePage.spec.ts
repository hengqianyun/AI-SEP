import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { canImportProduct, canWriteProduct } from '@/features/auth/composables/useCanWrite'

const dir = dirname(fileURLToPath(import.meta.url))
const pageSrc = readFileSync(join(dir, 'CatalogBrowsePage.vue'), 'utf8')
const layoutSrc = readFileSync(
  join(dir, '../../../layouts/WorkbenchLayout.vue'),
  'utf8',
)

describe('CatalogBrowsePage (TASK-WSC-306 HOTFIX)', () => {
  it('shows 新增产品 only when productWriteVisible; USER has no write entry', () => {
    expect(canWriteProduct('ADMIN')).toBe(true)
    expect(canWriteProduct('PROVIDER')).toBe(true)
    expect(canWriteProduct('USER')).toBe(false)
    expect(canImportProduct('USER')).toBe(false)

    expect(pageSrc).toContain('data-testid="catalog-open-create"')
    expect(pageSrc).toMatch(
      /v-if="productWriteVisible"[\s\S]*?data-testid="catalog-open-create"/,
    )
    expect(pageSrc).toContain("router.push('/catalog/products/new')")
    expect(pageSrc).toContain('function goCreate')
    expect(pageSrc).toContain('class="header-actions"')
    expect(pageSrc).toContain('data-testid="catalog-open-import"')
  })

  it('keeps list sticky head flush: no top padding on list pane', () => {
    expect(pageSrc).toContain('position: sticky')
    expect(pageSrc).toContain('.list-head')
    expect(pageSrc).toMatch(/\.pane\.list\s*\{[^}]*padding:\s*0\s+20px\s+20px/s)
    expect(pageSrc).toMatch(/\.list-head\s*\{[^}]*padding:\s*20px\s+0\s+12px/s)
    expect(pageSrc).toMatch(/\.list-head\s*\{[^}]*top:\s*0/s)
    expect(pageSrc).toMatch(/\.list-head\s*\{[^}]*z-index:\s*2/s)
  })

  it('fills viewport bi-pane on wide screens while keeping list scroll container', () => {
    expect(pageSrc).toContain('height: calc(100vh - 2 * var(--main-padding, 24px))')
    expect(pageSrc).toContain('max-height: calc(100vh - 2 * var(--main-padding, 24px))')
    expect(pageSrc).toMatch(
      /grid-template-columns:\s*minmax\(0,\s*1\.55fr\)\s+minmax\(380px,\s*1fr\)/,
    )
    expect(pageSrc).toContain('min-height: 280px')
  })

  it('WorkbenchLayout no longer mounts WriteEntryDemo', () => {
    expect(layoutSrc).not.toContain('WriteEntryDemo')
    expect(layoutSrc).not.toMatch(/<WriteEntryDemo\b/)
  })
})
