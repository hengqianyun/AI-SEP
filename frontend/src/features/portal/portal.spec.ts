import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const portalDir = dirname(fileURLToPath(import.meta.url))

describe('portal pages smoke (TASK-WSC-401)', () => {
  it('HomePage exposes portal-home testid and nav links', () => {
    const src = readFileSync(join(portalDir, 'HomePage.vue'), 'utf8')
    expect(src).toContain('data-testid="portal-home"')
    expect(src).toContain('to="/docs"')
    expect(src).toContain('to="/workspace"')
  })

  it('WorkspaceIntroPage links 打开接入端 to /login', () => {
    const src = readFileSync(join(portalDir, 'WorkspaceIntroPage.vue'), 'utf8')
    expect(src).toContain('data-testid="portal-workspace"')
    expect(src).toContain('打开接入端')
    expect(src).toContain('to="/login"')
    expect(src).not.toContain('/workspace/console')
  })

  it('DocsPage renders docs summary intro', () => {
    const src = readFileSync(join(portalDir, 'DocsPage.vue'), 'utf8')
    expect(src).toContain('data-testid="portal-docs"')
    expect(src).toContain(':title="hero.title"')
    expect(src).toContain('docsSummary')
  })
})
