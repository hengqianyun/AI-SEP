import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const routerDir = dirname(fileURLToPath(import.meta.url))

describe('router public portal meta (TASK-WSC-401)', () => {
  it('registers portal routes as public without root redirect to overview', () => {
    const indexSrc = readFileSync(join(routerDir, 'index.ts'), 'utf8')
    const routesSrc = readFileSync(join(routerDir, 'routes.ts'), 'utf8')

    expect(indexSrc).toContain('PortalLayout')
    expect(indexSrc).toContain("meta: { public: true")
    expect(indexSrc).toContain("name: 'portal-home'")
    expect(indexSrc).toContain("name: 'portal-workspace'")
    expect(indexSrc).toContain("name: 'portal-docs'")
    expect(indexSrc).toContain("return { path: '/overview' }")
    expect(routesSrc).not.toContain("redirect: '/overview'")
    expect(routesSrc).not.toMatch(/path:\s*'\/',\s*redirect/)
  })
})
