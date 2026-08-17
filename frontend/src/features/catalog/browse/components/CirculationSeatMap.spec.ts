import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))
const src = readFileSync(join(dir, 'CirculationSeatMap.vue'), 'utf8')
const pageSrc = readFileSync(join(dir, '../CatalogBrowsePage.vue'), 'utf8')
const browseSrc = readFileSync(join(dir, '../composables/useCatalogBrowse.ts'), 'utf8')
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

describe('CirculationSeatMap (TASK-WSC-902 / REQ-CAT-015 / REQ-CAT-018)', () => {
  it('902-seatmap-l1-dropdown: title row + L1 Select default 全部 + l1CategoryId query', () => {
    expect(src).toContain('data-testid="seat-map-title-row"')
    expect(src).toContain('data-testid="seat-map-l1-select"')
    expect(src).toContain("label: '全部'")
    expect(src).toContain('selectedL1Id')
    expect(src).toContain('listCategories')
    expect(src).toContain("c.level === 'L1'")
    expect(src).toContain('l1CategoryId')
    expect(src).toContain('/catalog/l2-distribution?')
    expect(src).toContain('watch(selectedL1Id')
    expect(src).toContain('.title-row')
    expect(src).toContain('data-testid="seat-map-l1-select"')
  })

  it('909-seatmap-title-center: h2 centered on card; L1 stays right and does not shift title', () => {
    expect(src).toMatch(/\.title-row\s*\{[\s\S]*position:\s*relative/)
    expect(src).toMatch(/\.title-row\s*\{[\s\S]*justify-content:\s*flex-end/)
    expect(src).toMatch(/\.title-row h2\s*\{[\s\S]*left:\s*50%/)
    expect(src).toMatch(/\.title-row h2\s*\{[\s\S]*translateX\(-50%\)/)
    expect(src).not.toContain('justify-content: space-between')
    expect(src).toContain('data-testid="seat-map-l1-select"')
    expect(src).not.toMatch(/enterprise/i)
  })

  it('902-no-enterprise-filter: no enterprise UI or query params', () => {
    expect(src).not.toContain('enterpriseId')
    expect(src).not.toContain('enterpriseName')
    expect(src).not.toMatch(/enterprise/i)
    expect(src).not.toMatch(/企业/)
  })

  it('does not couple to browse Cascader state', () => {
    expect(src).not.toContain('useCatalogBrowse')
    expect(src).not.toContain('l2CategoryId')
    expect(browseSrc).toContain('l1CategoryId')
    expect(src).not.toMatch(/defineProps/)
    expect(src).not.toMatch(/inject\(/)
  })

  it('L1 switch triggers reload with loading preserved', () => {
    expect(src).toContain('loading.value = true')
    expect(src).toContain(':loading="loading"')
    expect(src).toContain('fetchL2Distribution')
  })

  it('totalProducts uses API total for selected L1 subset', () => {
    expect(src).toContain('totalProducts.value = data.totalProducts')
    expect(src).not.toContain('top5.value.reduce')
  })

  it('cross-enterprise parity: full-chain fetch has no session/enterprise params', () => {
    expect(src).toContain('getL2Distribution()')
    expect(src).toContain('URLSearchParams({ l1CategoryId })')
    expect(src).not.toMatch(/enterpriseId|enterpriseName|mine/)
  })

  it('token mapping for L1 Select (§1.5)', () => {
    expect(src).toContain('ConfigProvider')
    expect(src).toContain('SEAT_MAP_FILTER_THEME')
    expect(src).toContain('data-seat-map-filter-theme="ant-token-mapped"')
    expect(src).toContain('colorPrimary')
  })
})
