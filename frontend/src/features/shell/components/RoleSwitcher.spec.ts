import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))

describe('RoleSwitcher readonly UX (TASK-WSC-602)', () => {
  const src = readFileSync(join(dir, 'RoleSwitcher.vue'), 'utf8')
  const layoutSrc = readFileSync(
    join(dir, '../../../layouts/WorkbenchLayout.vue'),
    'utf8',
  )

  it('is read-only: no listbox / chevron / option menu / switchRole', () => {
    expect(src).toContain('data-role-readonly="true"')
    expect(src).toContain('role-readonly')
    expect(src).not.toMatch(/aria-haspopup\s*=\s*["']listbox["']/)
    expect(src).not.toContain('role="listbox"')
    expect(src).not.toContain('role="option"')
    expect(src).not.toContain('role-dropdown')
    expect(src).not.toContain('switchRole')
    expect(src).not.toMatch(/<select[\s>]/)
  })

  it('WorkbenchLayout never mounts RoleSwitcher; footer uses unique readonly role label', () => {
    expect(layoutSrc).not.toMatch(/RoleSwitcher/)
    expect(layoutSrc).toContain('data-testid="session-role-label"')
    expect(layoutSrc).toContain('roleLabel')
    expect(layoutSrc).not.toMatch(/aria-haspopup\s*=\s*["']listbox["']/)
    expect(layoutSrc).not.toContain('role-dropdown')
  })
})
