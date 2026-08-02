import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))

describe('LoginView shell brand UX (TASK-WSC-202)', () => {
  it('consumes 201 tokens for primary/card surfaces (not hardcoded shell-divergent palette)', () => {
    const src = readFileSync(join(dir, 'LoginView.vue'), 'utf8')
    expect(src).toContain('var(--blue)')
    expect(src).toContain('var(--card-bg)')
    expect(src).toContain('var(--main-bg)')
    expect(src).toContain('var(--radius-card)')
    expect(src).toContain('var(--sidebar-bg)')
    expect(src).toContain('auth.login')
    expect(src).toContain('接入端工作台')
    expect(src).not.toMatch(/background:\s*#1f4b7a/)
  })
})
