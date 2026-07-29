import type { Role } from '@/api/auth'
import { computed, type Ref } from 'vue'

/** 对齐 contracts/rbac/matrix.yaml UI 可见性。 */
export function canMaintainCategory(role: Role | null | undefined): boolean {
  return role === 'ADMIN'
}

export function canWriteProduct(role: Role | null | undefined): boolean {
  return role === 'ADMIN' || role === 'PROVIDER'
}

export function useCanWrite(role: Ref<Role | null | undefined>) {
  const categoryMaintainVisible = computed(() => canMaintainCategory(role.value))
  const productWriteVisible = computed(() => canWriteProduct(role.value))
  return { categoryMaintainVisible, productWriteVisible }
}
