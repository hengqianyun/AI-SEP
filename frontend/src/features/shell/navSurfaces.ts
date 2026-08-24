/** 路由冻结常量（PLAN-WSC-8.3 §0.2） */
export const ROUTE_MY_CATALOG = '/my-catalog'
export const ROUTE_CATALOG_MAINTENANCE = '/catalog/maintenance'
export const ROUTE_CATALOG_BROWSE = '/catalog'
export const ROUTE_MY_PRODUCTS = '/my-products'
export const ROUTE_ADMIN_USERS = '/admin/users'
export const ROUTE_ORDERS = '/orders'

export type CatalogSurface = 'browse' | 'myCatalog' | 'myProducts' | 'maintenance' | 'other'

function pathIs(path: string, route: string): boolean {
  return path === route || path.startsWith(`${route}/`)
}

/**
 * 四 surface 判定：维护 / 我的目录 / 我的产品 优先于全链 `/catalog` 前缀。
 */
export function resolveCatalogSurface(
  path: string,
  queryFrom?: unknown,
  fromMineMeta?: boolean,
): CatalogSurface {
  if (pathIs(path, ROUTE_MY_CATALOG)) return 'myCatalog'
  if (
    pathIs(path, ROUTE_MY_PRODUCTS) ||
    queryFrom === 'mine' ||
    fromMineMeta === true
  ) {
    return 'myProducts'
  }
  if (pathIs(path, ROUTE_CATALOG_MAINTENANCE)) return 'maintenance'
  if (path === ROUTE_CATALOG_BROWSE || path.startsWith(`${ROUTE_CATALOG_BROWSE}/`)) {
    return 'browse'
  }
  return 'other'
}

export function isNavActive(
  navPath: string,
  path: string,
  queryFrom?: unknown,
  fromMineMeta?: boolean,
): boolean {
  const surface = resolveCatalogSurface(path, queryFrom, fromMineMeta)
  if (navPath === ROUTE_CATALOG_BROWSE) return surface === 'browse'
  if (navPath === ROUTE_MY_CATALOG) return surface === 'myCatalog'
  if (navPath === ROUTE_MY_PRODUCTS) return surface === 'myProducts'
  if (navPath === ROUTE_CATALOG_MAINTENANCE) return surface === 'maintenance'
  return path === navPath || path.startsWith(`${navPath}/`)
}

export function isCatalogGroupActive(
  path: string,
  queryFrom?: unknown,
  fromMineMeta?: boolean,
): boolean {
  const surface = resolveCatalogSurface(path, queryFrom, fromMineMeta)
  return (
    surface === 'browse' ||
    surface === 'myCatalog' ||
    surface === 'myProducts' ||
    surface === 'maintenance'
  )
}
