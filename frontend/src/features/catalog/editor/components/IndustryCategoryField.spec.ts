import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { INDUSTRY_CATEGORY_OPTIONS } from '@/api/catalog'

const dir = dirname(fileURLToPath(import.meta.url))
const editorPage = readFileSync(join(dir, '../ProductEditorPage.vue'), 'utf8')
const editorComposable = readFileSync(join(dir, '../composables/useProductEditor.ts'), 'utf8')

describe('IndustryCategoryField regression (REQ-CAT-019)', () => {
  it('907-industry-category-regression: editor still binds INDUSTRY_CATEGORY_OPTIONS and submits industryCategory', () => {
    expect(INDUSTRY_CATEGORY_OPTIONS).toContain('卫生和社会工作')
    expect(INDUSTRY_CATEGORY_OPTIONS).toContain('建筑业')
    expect(INDUSTRY_CATEGORY_OPTIONS.length).toBeGreaterThanOrEqual(20)
    expect(editorPage).toContain('INDUSTRY_CATEGORY_OPTIONS')
    expect(editorPage).toContain('v-model="form.industryCategory"')
    expect(editorPage).toContain('data-testid="industry-category"')
    expect(editorPage).toContain('data-testid="industry-category-select"')
    expect(editorComposable).toContain('industryCategory')
    expect(editorComposable).toContain('INDUSTRY_CATEGORY_OPTIONS')
  })
})
