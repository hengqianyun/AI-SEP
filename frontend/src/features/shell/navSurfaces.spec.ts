import { describe, expect, it } from 'vitest'
import {
  ROUTE_CATALOG_BROWSE,
  ROUTE_CATALOG_MAINTENANCE,
  ROUTE_MY_CATALOG,
  ROUTE_MY_PRODUCTS,
  isCatalogGroupActive,
  isNavActive,
  resolveCatalogSurface,
} from './navSurfaces'

describe('navSurfaces four-surface active (TASK-WSC-906)', () => {
  it('resolves four catalog surfaces without overlap', () => {
    expect(resolveCatalogSurface('/catalog')).toBe('browse')
    expect(resolveCatalogSurface('/catalog/products/1')).toBe('browse')
    expect(resolveCatalogSurface('/my-catalog')).toBe('myCatalog')
    expect(resolveCatalogSurface('/my-products')).toBe('myProducts')
    expect(resolveCatalogSurface('/catalog/products/1', 'mine')).toBe('myProducts')
    expect(resolveCatalogSurface('/catalog/products/1/edit', undefined, true)).toBe('myProducts')
    expect(resolveCatalogSurface('/catalog/maintenance')).toBe('maintenance')
    expect(resolveCatalogSurface('/overview')).toBe('other')
  })

  it('sidebar active is exclusive across /catalog /my-catalog /my-products /catalog/maintenance', () => {
    const paths = [
      ROUTE_CATALOG_BROWSE,
      ROUTE_MY_CATALOG,
      ROUTE_MY_PRODUCTS,
      ROUTE_CATALOG_MAINTENANCE,
    ]
    const navs = [...paths]
    for (const current of paths) {
      const actives = navs.filter((nav) => isNavActive(nav, current))
      expect(actives).toEqual([current])
    }
  })

  it('catalog group stays active when collapsed child route is current', () => {
    expect(isCatalogGroupActive('/catalog')).toBe(true)
    expect(isCatalogGroupActive('/my-catalog')).toBe(true)
    expect(isCatalogGroupActive('/my-products')).toBe(true)
    expect(isCatalogGroupActive('/catalog/maintenance')).toBe(true)
    expect(isCatalogGroupActive('/overview')).toBe(false)
    expect(isCatalogGroupActive('/admin/users')).toBe(false)
  })

  it('from=mine does not light 全链 or 目录维护', () => {
    expect(isNavActive('/catalog', '/catalog/products/9', 'mine')).toBe(false)
    expect(isNavActive('/my-products', '/catalog/products/9', 'mine')).toBe(true)
    expect(isNavActive('/catalog/maintenance', '/catalog/products/9', 'mine')).toBe(false)
    expect(isNavActive('/my-catalog', '/catalog/products/9', 'mine')).toBe(false)
  })
})
