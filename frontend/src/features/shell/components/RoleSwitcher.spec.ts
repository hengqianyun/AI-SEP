import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))

describe('RoleSwitcher shell UX (TASK-WSC-201)', () => {
  it('uses custom dark dropdown markup (not native select)', () => {
    const src = readFileSync(join(dir, 'RoleSwitcher.vue'), 'utf8')
    expect(src).toContain('role-dropdown')
    expect(src).toContain('role-dropdown-item')
    expect(src).toContain('switchRole')
    expect(src).not.toMatch(/<select[\s>]/)
  })
})
