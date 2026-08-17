import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))
const pageSrc = readFileSync(join(dir, '../CatalogMaintenancePage.vue'), 'utf8')
const routesSrc = readFileSync(join(dir, '../../../../router/routes.ts'), 'utf8')

describe('CatalogMaintenance UX regression (REQ-CAT-013 / REQ-CAT-017)', () => {
  it('907-my-catalog-scope-split: dual entry routes keep 906 meta titles and wire maintenance page', () => {
    expect(routesSrc).toContain("path: '/my-catalog'")
    expect(routesSrc).toContain("title: '我的目录（本企业/本人）'")
    expect(routesSrc).toContain("title: '目录维护（全量）'")
    expect(routesSrc).toContain("path: '/my-maintenance'")
    expect(routesSrc).toContain("redirect: '/my-catalog'")
    expect(routesSrc).toContain("path: '/admin/users'")
    expect(routesSrc).toContain("props: { scope: 'myCatalog' }")
    expect(routesSrc).toContain("props: { scope: 'full' }")
    expect(routesSrc).toContain('beforeEnter: requireDeepLinkAccess')
    expect(routesSrc).toContain("import('@/features/catalog/maintenance/CatalogMaintenancePage.vue')")
    expect(pageSrc).toContain('canEnterMaintenancePage')
    expect(pageSrc).toContain("scope === 'myCatalog' ? '我的目录（本企业/本人）' : '目录维护（全量）'")
  })

  it('FIND-WSC-907-001: page passes reactive scope and re-runs guard on props.scope', () => {
    expect(pageSrc).not.toMatch(/useCatalogMaintenance\(props\.scope\)/)
    expect(pageSrc).toMatch(/useCatalogMaintenance\(\(\)\s*=>\s*props\.scope\)/)
    expect(pageSrc).toMatch(/watch\(\s*\(\)\s*=>\s*props\.scope/)
    expect(pageSrc).toMatch(/watch\([\s\S]*canEnterMaintenancePage/)
  })

  it('Pagination / Cascader / 本页全选 / 统一维护；筛 Tab 清选择；无跨页全选', () => {
    expect(pageSrc).toContain('Pagination')
    expect(pageSrc).toContain(':show-quick-jumper="true"')
    expect(pageSrc).toContain('data-testid="a-pagination"')
    expect(pageSrc).toContain('MaintenanceCategoryCascader')
    expect(pageSrc).toContain('filter-category-cascader')
    expect(pageSrc).toContain('change-on-select')
    expect(pageSrc).toContain('PAGE_SELECT_ALL_LABEL')
    expect(pageSrc).toContain('data-testid="select-all-page"')
    expect(pageSrc).toContain('UNIFIED_MAINTAIN_LABEL')
    expect(pageSrc).toContain('toggleSelectAllOnPage')
    expect(pageSrc).not.toContain('批量保存')
    expect(pageSrc).not.toContain('跨页全选')
    expect(pageSrc).not.toContain('已全选全部')
    expect(pageSrc).toContain('data-maint-filter-theme="ant-token-mapped"')
  })

  it('910-page-size-changer: Pagination shows size changer 10/20/50/100; change and showSizeChange share applyPagination', () => {
    expect(pageSrc).toContain(':show-size-changer="true"')
    expect(pageSrc).not.toContain(':show-size-changer="false"')
    expect(pageSrc).toContain(':show-quick-jumper="true"')
    expect(pageSrc).toContain(':page-size-options="PAGE_SIZE_OPTION_LABELS"')
    expect(pageSrc).toContain('PAGE_SIZE_OPTION_LABELS')
    expect(pageSrc).toContain('@showSizeChange="onShowSizeChange"')
    expect(pageSrc).toContain('@change="onPageChange"')
    expect(pageSrc).toContain('applyPagination(nextPage, nextSize)')
    expect(pageSrc).toContain('applyPagination(1, size)')
    expect(pageSrc).not.toContain('loadMore')
  })
})
