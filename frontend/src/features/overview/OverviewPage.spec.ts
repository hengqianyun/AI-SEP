import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))

describe('OverviewPage visual tokens (TASK-WSC-202)', () => {
  it('renders three colored metric icon blocks and chart card headers via shared tokens', () => {
    const src = readFileSync(join(dir, 'OverviewPage.vue'), 'utf8')
    expect(src).toContain('stat-card-icon blue')
    expect(src).toContain('stat-card-icon green')
    expect(src).toContain('stat-card-icon orange')
    expect(src).toContain('class="card-header"')
    expect(src).toContain('var(--radius-card)')
    expect(src).toContain('var(--shadow)')
    expect(src).toContain('var(--blue-light)')
    expect(src).toContain('data-testid="metrics-loading"')
    expect(src).toContain('data-testid="metrics-empty"')
    expect(src).toContain('data-testid="overview-refresh"')
    expect(src).toContain('width: 100%')
    expect(src).not.toContain('max-width: 1200px')
    expect(src).not.toMatch(/from ['"]ant-design-vue/)
  })
})
