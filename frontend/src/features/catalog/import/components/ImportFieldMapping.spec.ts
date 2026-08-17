import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { IMPORT_TEMPLATE_COLUMNS, INDUSTRY_CATEGORY_OPTIONS } from '@/api/catalog'
import { looksLikeImportHeaderRow } from '@/features/catalog/import/composables/importGuards'

const dir = dirname(fileURLToPath(import.meta.url))
const guardsSrc = readFileSync(join(dir, '../composables/importGuards.ts'), 'utf8')
const importComposable = readFileSync(join(dir, '../composables/useProductImport.ts'), 'utf8')

describe('ImportFieldMapping industry regression (REQ-CAT-019)', () => {
  it('907-industry-category-regression: import template maps 行业分类 and reuses INDUSTRY_CATEGORY_OPTIONS', () => {
    expect(IMPORT_TEMPLATE_COLUMNS).toContain('行业分类（必填）')
    expect(INDUSTRY_CATEGORY_OPTIONS.length).toBeGreaterThanOrEqual(20)
    expect(looksLikeImportHeaderRow(['产品名称', '行业分类'])).toBe(true)
    expect(guardsSrc).toContain('行业分类')
    expect(importComposable).toContain('IMPORT_TEMPLATE_COLUMNS')
  })
})
