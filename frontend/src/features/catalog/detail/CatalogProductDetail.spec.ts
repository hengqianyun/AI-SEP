import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { INDUSTRY_CATEGORY_OPTIONS } from '@/api/catalog'

const dir = dirname(fileURLToPath(import.meta.url))
const detailPage = readFileSync(join(dir, 'ProductDetailPage.vue'), 'utf8')

describe('CatalogProductDetail industry regression (REQ-CAT-019)', () => {
  it('907-industry-category-regression: detail still displays industryCategory from INDUSTRY_CATEGORY_OPTIONS enum', () => {
    expect(detailPage).toContain('product.industryCategory')
    expect(detailPage).toContain('data-testid="industry-category"')
    expect(detailPage).toContain('行业分类')
    expect(INDUSTRY_CATEGORY_OPTIONS).toContain('信息传输、软件和信息技术服务业')
    expect(INDUSTRY_CATEGORY_OPTIONS).toContain('卫生和社会工作')
  })
})
