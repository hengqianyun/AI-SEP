import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))

describe('ProductEditorPage layout (TASK-WSC-402)', () => {
  it('uses fluid editor body without narrow max-width caps', () => {
    const src = readFileSync(join(dir, 'ProductEditorPage.vue'), 'utf8')
    expect(src).toContain('width: 100%')
    expect(src).not.toContain('max-width: 900px')
    expect(src).not.toContain('max-width: 1100px')
    expect(src).toMatch(/\.editor-body\s*\{[^}]*width:\s*100%/s)
    expect(src).toContain(':class="{ wide: isApi }"')
    expect(src).toContain('@media (max-width: 720px)')
  })

  it('keeps create-mode auto product code UX from TASK-WSC-401', () => {
    const src = readFileSync(join(dir, 'ProductEditorPage.vue'), 'utf8')
    expect(src).toContain('自动生成')
    expect(src).toMatch(/v-model="form\.productCode"[\s\S]*readonly/)
  })
})
