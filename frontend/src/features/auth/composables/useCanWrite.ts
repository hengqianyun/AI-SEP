import type { Role } from '@/api/auth'
import { computed, type Ref } from 'vue'

/** 对齐 contracts/rbac/matrix.yaml（2.2.0 / TASK-WSC-607）UI 可见性。 */
export function canMaintainCategory(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

/** 目录维护 — ADMIN + PROVIDER（PROVIDER 仅本人产品，由服务端隔离）。 */
export function canMaintainCatalog(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

/** 产品增改 — 仅提供方（我的数据产品）。 */
export function canWriteProduct(role: Role | null | undefined): boolean {
  return role === 'PROVIDER'
}

/** 批量导入 — 仅提供方。 */
export function canImportProduct(role: Role | null | undefined): boolean {
  return role === 'PROVIDER'
}

export function canManageUsers(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

export function canSeeMyProducts(role: Role | null | undefined): boolean {
  return role === 'PROVIDER'
}

export function useCanWrite(role: Ref<Role | null | undefined>) {
  const categoryMaintainVisible = computed(() => canMaintainCategory(role.value))
  const catalogMaintenanceVisible = computed(() => canMaintainCatalog(role.value))
  const productWriteVisible = computed(() => canWriteProduct(role.value))
  const productImportVisible = computed(() => canImportProduct(role.value))
  const userManageVisible = computed(() => canManageUsers(role.value))
  const myProductsVisible = computed(() => canSeeMyProducts(role.value))
  return {
    categoryMaintainVisible,
    catalogMaintenanceVisible,
    productWriteVisible,
    productImportVisible,
    userManageVisible,
    myProductsVisible,
  }
}
