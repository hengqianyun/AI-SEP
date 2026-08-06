import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))
const src = readFileSync(join(dir, 'CirculationSeatMap.vue'), 'utf8')
const pageSrc = readFileSync(join(dir, '../CatalogBrowsePage.vue'), 'utf8')
const routesSrc = readFileSync(join(dir, '../../../../router/routes.ts'), 'utf8')

describe('CirculationSeatMap (TASK-WSC-604 / REQ-CAT-009)', () => {
  it('Top5 truncate + proportion seats + real total via getL2Distribution', () => {
    expect(src).toContain("from '@/api/catalog'")
    expect(src).toContain('getL2Distribution')
    expect(src).toContain('.slice(0, 5)')
    expect(src).toContain('totalProducts')
    expect(src).toContain('GRID')
    expect(src).toMatch(/\(item\.count\s*\/\s*sum\)\s*\*\s*GRID/)
    expect(src).toContain('data-testid="seat-map-total"')
  })

  it('hover dims others (opacity cap) + mouseleave restores', () => {
    expect(src).toContain('DIM_OPACITY')
    expect(src).toContain('hoveredCode')
    expect(src).toContain('@mouseenter="hoveredCode = item.code"')
    expect(src).toContain('@mouseleave="hoveredCode = null"')
    expect(src).toContain('dimOpacity')
    expect(src).toContain('data-dim-opacity-cap')
  })

  it('empty + loading + error states are readable', () => {
    expect(src).toContain('data-testid="seat-map-loading"')
    expect(src).toContain('data-testid="seat-map-empty"')
    expect(src).toContain('data-testid="seat-map-error"')
    expect(src).toContain('暂无流通数据')
    expect(src).toContain('isEmpty')
  })

  it('no click-filter affordance: legend chips are not buttons / no @click', () => {
    expect(src).not.toMatch(/<button[\s>]/)
    expect(src).not.toMatch(/@click\s*=/)
    expect(src).toContain('role="presentation"')
    expect(src).toContain('cursor: default')
    expect(src).toContain('class="cat-chip"')
  })

  it('mount contract: only catalog showSeatMap=true; mine false', () => {
    expect(pageSrc).toContain('CirculationSeatMap v-if="seatMapVisible"')
    expect(pageSrc).toContain('data-testid="catalog-seat-map-slot"')
    expect(routesSrc).toContain("path: '/catalog'")
    expect(routesSrc).toContain('showSeatMap: true')
    expect(routesSrc).toContain("path: '/my-products'")
    expect(routesSrc).toContain('showSeatMap: false')
  })

  it('prelandReconcile_604: directory card layer + seat-map root testid', () => {
    expect(src).toContain('data-testid="circulation-seat-map"')
    expect(src).toContain('wsc-surface')
    expect(src).toContain('数据流通链')
  })
})
