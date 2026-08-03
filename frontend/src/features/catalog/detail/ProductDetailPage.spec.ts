import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))

describe('ProductDetailPage layout (TASK-WSC-402)', () => {
  it('uses fluid main width without narrow max-width cap', () => {
    const src = readFileSync(join(dir, 'ProductDetailPage.vue'), 'utf8')
    expect(src).toContain('width: 100%')
    expect(src).not.toContain('max-width: 1100px')
    expect(src).toMatch(/\.detail-page-body\s*\{[^}]*width:\s*100%/s)
    expect(src).toContain('@media (max-width: 900px)')
    expect(src).toContain('@media (max-width: 640px)')
  })
})
