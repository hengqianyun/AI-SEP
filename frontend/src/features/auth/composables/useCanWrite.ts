import type { Role } from '@/api/auth'
import { computed, type Ref } from 'vue'

/**
 * Frontend RBAC UI visibility aligned to contracts/rbac/matrix.yaml 2.3.3
 * and PLAN-WSC-8.3 section 3.1. Hide is not authorize; deep-link and API still need server checks.
 */

/** categoryMaintainUI: ADMIN only */
export function canMaintainCategory(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

/** catalogMaintenanceUI: ADMIN only (full /catalog/maintenance); PROVIDER hidden */
export function canMaintainCatalog(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

/** productWriteUI: ADMIN + PROVIDER (ownEnterprise / ownCreateBy enforced server-side) */
export function canWriteProduct(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

/** productImportUI: ADMIN + PROVIDER */
export function canImportProduct(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

/** userManageUI: ADMIN only */
export function canManageUsers(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

/** myProductsUI: ADMIN + PROVIDER */
export function canSeeMyProducts(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

/** myCatalogUI: ADMIN + PROVIDER (/my-catalog) */
export function canSeeMyCatalog(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

/**
 * Pre-void my-maintenance alias. Same as canSeeMyCatalog so leftover imports keep working.
 */
export function canSeeMyMaintenance(role: Role | null | undefined): boolean {
  return canSeeMyCatalog(role)
}

export function useCanWrite(role: Ref<Role | null | undefined>) {
  const categoryMaintainVisible = computed(() => canMaintainCategory(role.value))
  const catalogMaintenanceVisible = computed(() => canMaintainCatalog(role.value))
  const productWriteVisible = computed(() => canWriteProduct(role.value))
  const productImportVisible = computed(() => canImportProduct(role.value))
  const userManageVisible = computed(() => canManageUsers(role.value))
  const myProductsVisible = computed(() => canSeeMyProducts(role.value))
  const myCatalogVisible = computed(() => canSeeMyCatalog(role.value))
  const myMaintenanceVisible = computed(() => canSeeMyMaintenance(role.value))
  return {
    categoryMaintainVisible,
    catalogMaintenanceVisible,
    productWriteVisible,
    productImportVisible,
    userManageVisible,
    myProductsVisible,
    myCatalogVisible,
    myMaintenanceVisible,
  }
}
