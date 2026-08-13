import type { Role } from '@/api/auth'
import { computed, type Ref } from 'vue'

/** 对齐 contracts/rbac/matrix.yaml（2.3.2）UI 可见性。 */
export function canMaintainCategory(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

/** 目录维护 — 仅 ADMIN（TASK-WSC-901）。 */
export function canMaintainCatalog(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
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

/** 我的数据产品 — ADMIN + PROVIDER（REQ-SHELL-008：管理员可查看我的数据产品目录）。 */
export function canSeeMyProducts(role: Role | null | undefined): boolean {
  return role === 'PROVIDER' || role === 'ADMIN'
}

/** 我的目录 — ADMIN + PROVIDER（TASK-WSC-901）。 */
export function canSeeMyMaintenance(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

export function useCanWrite(role: Ref<Role | null | undefined>) {
  const categoryMaintainVisible = computed(() => canMaintainCategory(role.value))
  const catalogMaintenanceVisible = computed(() => canMaintainCatalog(role.value))
  const productWriteVisible = computed(() => canWriteProduct(role.value))
  const productImportVisible = computed(() => canImportProduct(role.value))
  const userManageVisible = computed(() => canManageUsers(role.value))
  const myProductsVisible = computed(() => canSeeMyProducts(role.value))
  const myMaintenanceVisible = computed(() => canSeeMyMaintenance(role.value))
  return {
    categoryMaintainVisible,
    catalogMaintenanceVisible,
    productWriteVisible,
    productImportVisible,
    userManageVisible,
    myProductsVisible,
    myMaintenanceVisible,
  }
}