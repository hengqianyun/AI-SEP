import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))
const src = readFileSync(join(dir, 'MaintenanceCategoryCascader.vue'), 'utf8')

describe('MaintenanceCategoryCascader (REQ-CAT-013)', () => {
  it('uses Ant Cascader with token mapping, allow-clear and optional change-on-select', () => {
    expect(src).toContain('Cascader')
    expect(src).toContain('ConfigProvider')
    expect(src).toContain('allow-clear')
    expect(src).toContain(':change-on-select="changeOnSelect"')
    expect(src).toContain('data-maint-cascader-theme="ant-token-mapped"')
    expect(src).not.toContain('industryCategory')
    expect(src).not.toContain('INDUSTRY_CATEGORY_OPTIONS')
  })
})
