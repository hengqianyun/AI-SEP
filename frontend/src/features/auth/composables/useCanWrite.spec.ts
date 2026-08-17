import { describe, expect, it } from 'vitest'
import {
  canImportProduct,
  canMaintainCatalog,
  canMaintainCategory,
  canManageUsers,
  canSeeMyCatalog,
  canSeeMyMaintenance,
  canSeeMyProducts,
  canWriteProduct,
  useCanWrite,
} from './useCanWrite'
import { ref } from 'vue'
import type { Role } from '@/api/auth'

const ROLES: Role[] = ['ADMIN', 'PROVIDER', 'USER']

describe('useCanWrite / RBAC UI visibility (2.3.3 / PLAN-WSC-8.3 §3.1)', () => {
  it('ADMIN: category/maintenance/users + myCatalog/myProducts + product write/import true', () => {
    expect(canMaintainCategory('ADMIN')).toBe(true)
    expect(canMaintainCatalog('ADMIN')).toBe(true)
    expect(canManageUsers('ADMIN')).toBe(true)
    expect(canWriteProduct('ADMIN')).toBe(true)
    expect(canImportProduct('ADMIN')).toBe(true)
    expect(canSeeMyProducts('ADMIN')).toBe(true)
    expect(canSeeMyCatalog('ADMIN')).toBe(true)
    expect(canSeeMyMaintenance('ADMIN')).toBe(true)
  })

  it('PROVIDER: my-catalog/my-products write/import; no catalog maintenance/category/users', () => {
    expect(canMaintainCategory('PROVIDER')).toBe(false)
    expect(canMaintainCatalog('PROVIDER')).toBe(false)
    expect(canManageUsers('PROVIDER')).toBe(false)
    expect(canWriteProduct('PROVIDER')).toBe(true)
    expect(canImportProduct('PROVIDER')).toBe(true)
    expect(canSeeMyProducts('PROVIDER')).toBe(true)
    expect(canSeeMyCatalog('PROVIDER')).toBe(true)
    expect(canSeeMyMaintenance('PROVIDER')).toBe(true)
  })

  it('USER hides all write entries and my-catalog/my-products', () => {
    expect(canMaintainCategory('USER')).toBe(false)
    expect(canMaintainCatalog('USER')).toBe(false)
    expect(canWriteProduct('USER')).toBe(false)
    expect(canImportProduct('USER')).toBe(false)
    expect(canManageUsers('USER')).toBe(false)
    expect(canSeeMyProducts('USER')).toBe(false)
    expect(canSeeMyCatalog('USER')).toBe(false)
    expect(canSeeMyMaintenance('USER')).toBe(false)
  })

  it('useCanWrite computed mirrors §3.1 matrix cells for all roles', () => {
    for (const role of ROLES) {
      const api = useCanWrite(ref(role))
      expect(api.catalogMaintenanceVisible.value).toBe(canMaintainCatalog(role))
      expect(api.myCatalogVisible.value).toBe(canSeeMyCatalog(role))
      expect(api.myProductsVisible.value).toBe(canSeeMyProducts(role))
      expect(api.productWriteVisible.value).toBe(canWriteProduct(role))
      expect(api.productImportVisible.value).toBe(canImportProduct(role))
      expect(api.userManageVisible.value).toBe(canManageUsers(role))
      expect(api.categoryMaintainVisible.value).toBe(canMaintainCategory(role))
      expect(api.myMaintenanceVisible.value).toBe(api.myCatalogVisible.value)
    }
  })

  it('null/undefined role hides gated surfaces', () => {
    expect(canSeeMyCatalog(null)).toBe(false)
    expect(canSeeMyCatalog(undefined)).toBe(false)
    expect(canMaintainCatalog(null)).toBe(false)
    expect(canWriteProduct(null)).toBe(false)
  })
})
