import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))
const src = readFileSync(join(dir, 'BrowseFilterBar.vue'), 'utf8')
const labelsSrc = readFileSync(join(dir, '../utils/labels.ts'), 'utf8')

describe('BrowseFilterBar (TASK-WSC-901 / REQ-CAT-014)', () => {
  it('901-no-industryCategory-query: no industry category control', () => {
    expect(src).not.toContain('industryCategory')
    expect(src).not.toContain('INDUSTRY_CATEGORY_OPTIONS')
    expect(src).not.toContain('catalog-filter-industry')
  })

  it('901-cascader-l1-l2-clear: Cascader binds category path with clear', () => {
    expect(src).toContain('categoryPathFromFilters')
    expect(src).toContain('change-on-select')
    expect(src).toContain('allow-clear')
    expect(src).toContain('@change="onCategoryChange"')
    expect(src).toContain('emit(\'categoryChange\'')
  })

  it('filter bar is always visible with Ant token theme', () => {
    expect(src).toContain('data-testid="catalog-filters"')
    expect(src).toContain('data-browse-filter-theme="ant-token-mapped"')
    expect(src).not.toMatch(/v-if=.*advanced/)
  })

  it('labels use business view / category naming', () => {
    expect(labelsSrc).toContain("export const BROWSE_CATEGORY_CASCADER_LABEL = '业务视图 / 业务大类'")
    expect(labelsSrc).toContain("export const BROWSE_CATEGORY_CASCADER_PLACEHOLDER")
    expect(src).toContain('BROWSE_CATEGORY_CASCADER_PLACEHOLDER')
  })
})
