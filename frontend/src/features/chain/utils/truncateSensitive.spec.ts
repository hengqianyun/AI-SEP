import { describe, expect, it } from 'vitest'
import { truncateSensitiveId } from './truncateSensitive'

describe('truncateSensitiveId (§3.6)', () => {
  it('truncates long hash/DID with ellipsis', () => {
    const hash =
      'sha256:3333333333333333333333333333333333333333333333333333333333333333'
    const shown = truncateSensitiveId(hash)
    expect(shown).toContain('…')
    expect(shown.startsWith('sha256:333')).toBe(true)
    expect(shown.endsWith('333333')).toBe(true)
    expect(shown.length).toBeLessThan(hash.length)
  })

  it('keeps short values intact', () => {
    expect(truncateSensitiveId('short')).toBe('short')
  })

  it('empty string stays empty', () => {
    expect(truncateSensitiveId('')).toBe('')
  })
})
