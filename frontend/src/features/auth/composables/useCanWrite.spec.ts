import { describe, expect, it } from 'vitest'
import { canMaintainCategory, canWriteProduct } from './useCanWrite'

describe('useCanWrite / RBAC UI visibility', () => {
  it('ADMIN sees category maintain and product write', () => {
    expect(canMaintainCategory('ADMIN')).toBe(true)
    expect(canWriteProduct('ADMIN')).toBe(true)
  })

  it('PROVIDER hides category maintain, shows product write', () => {
    expect(canMaintainCategory('PROVIDER')).toBe(false)
    expect(canWriteProduct('PROVIDER')).toBe(true)
  })

  it('USER hides all write entries', () => {
    expect(canMaintainCategory('USER')).toBe(false)
    expect(canWriteProduct('USER')).toBe(false)
  })
})
