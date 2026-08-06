import { describe, expect, it } from 'vitest'
import {
  canImportProduct,
  canMaintainCatalog,
  canMaintainCategory,
  canManageUsers,
  canSeeMyProducts,
  canWriteProduct,
} from './useCanWrite'

describe('useCanWrite / RBAC UI visibility (2.2.0)', () => {
  it('ADMIN: category/maintenance/users; no product write/import; no my-products', () => {
    expect(canMaintainCategory('ADMIN')).toBe(true)
    expect(canMaintainCatalog('ADMIN')).toBe(true)
    expect(canManageUsers('ADMIN')).toBe(true)
    expect(canWriteProduct('ADMIN')).toBe(false)
    expect(canImportProduct('ADMIN')).toBe(false)
    expect(canSeeMyProducts('ADMIN')).toBe(false)
  })

  it('PROVIDER: my-products write/import + catalog maintenance; no category/users', () => {
    expect(canMaintainCategory('PROVIDER')).toBe(false)
    expect(canMaintainCatalog('PROVIDER')).toBe(true)
    expect(canManageUsers('PROVIDER')).toBe(false)
    expect(canWriteProduct('PROVIDER')).toBe(true)
    expect(canImportProduct('PROVIDER')).toBe(true)
    expect(canSeeMyProducts('PROVIDER')).toBe(true)
  })

  it('USER hides all write entries', () => {
    expect(canMaintainCategory('USER')).toBe(false)
    expect(canMaintainCatalog('USER')).toBe(false)
    expect(canWriteProduct('USER')).toBe(false)
    expect(canImportProduct('USER')).toBe(false)
    expect(canManageUsers('USER')).toBe(false)
    expect(canSeeMyProducts('USER')).toBe(false)
  })
})
