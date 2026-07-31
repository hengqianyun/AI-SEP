import type { Role } from '@/api/auth'
import { computed, type Ref } from 'vue'

/** 对齐 contracts/rbac/matrix.yaml（2.0.0）UI 可见性。 */
export function canMaintainCategory(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

/** 目录维护（关联）— 仅管理员。 */
export function canMaintainCatalog(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

export function canWriteProduct(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

/** 批量导入 — 管理员与提供方。 */
export function canImportProduct(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

export function useCanWrite(role: Ref<Role | null | undefined>) {
  const categoryMaintainVisible = computed(() => canMaintainCategory(role.value))
  const catalogMaintenanceVisible = computed(() => canMaintainCatalog(role.value))
  const productWriteVisible = computed(() => canWriteProduct(role.value))
  const productImportVisible = computed(() => canImportProduct(role.value))
  return {
    categoryMaintainVisible,
    catalogMaintenanceVisible,
    productWriteVisible,
    productImportVisible,
  }
}
