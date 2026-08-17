import type { Role } from '@/api/auth'
import {
  canMaintainCatalog,
  canManageUsers,
  canSeeMyCatalog,
  canSeeMyProducts,
} from '@/features/auth/composables/useCanWrite'
import {
  ROUTE_ADMIN_USERS,
  ROUTE_CATALOG_MAINTENANCE,
  ROUTE_MY_CATALOG,
  ROUTE_MY_PRODUCTS,
} from './navSurfaces'

function pathIs(path: string, route: string): boolean {
  return path === route || path.startsWith(`${route}/`)
}

/**
 * 深链结构可达性（隐藏 ≠ 授权的前端门控）。
 * USER：维护 / 我的目录 / 我的产品 / 用户管理均不可达。
 * PROVIDER：`/catalog/maintenance` 不可达；我的目录 / 我的产品可达。
 */
export function isDeepLinkAllowed(
  path: string,
  role: Role | null | undefined,
): boolean {
  if (pathIs(path, ROUTE_CATALOG_MAINTENANCE)) return canMaintainCatalog(role)
  if (pathIs(path, ROUTE_MY_CATALOG)) return canSeeMyCatalog(role)
  if (pathIs(path, ROUTE_MY_PRODUCTS)) return canSeeMyProducts(role)
  if (pathIs(path, ROUTE_ADMIN_USERS)) return canManageUsers(role)
  return true
}
