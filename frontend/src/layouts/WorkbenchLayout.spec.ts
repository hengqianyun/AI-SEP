import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import {
  canMaintainCatalog,
  canSeeMyCatalog,
  canSeeMyProducts,
} from '@/features/auth/composables/useCanWrite'

const dir = dirname(fileURLToPath(import.meta.url))
const layoutSrc = readFileSync(join(dir, 'WorkbenchLayout.vue'), 'utf8')
const routesSrc = readFileSync(join(dir, '../router/routes.ts'), 'utf8')
const placeholderSrc = readFileSync(
  join(dir, '../features/shell/MyCatalogPlaceholderPage.vue'),
  'utf8',
)

function submenuBlock(src: string): string {
  const start = src.indexOf('id="nav-catalog-submenu"')
  expect(start).toBeGreaterThan(-1)
  const end = src.indexOf('</div>', src.indexOf('nav-catalog-maintenance'))
  expect(end).toBeGreaterThan(start)
  return src.slice(start, end)
}

describe('WorkbenchLayout TASK-WSC-906 (REQ-SHELL-009 / REQ-SHELL-010)', () => {
  it('906-nav-my-catalog-submenu: 我的目录在 nav-catalog-submenu 内且序为全链→我的目录→我的数据产品→目录维护', () => {
    const sub = submenuBlock(layoutSrc)
    expect(sub).toContain('data-testid="nav-catalog-browse"')
    expect(sub).toContain('data-testid="nav-my-catalog"')
    expect(sub).toContain('data-testid="nav-my-products"')
    expect(sub).toContain('data-testid="nav-catalog-maintenance"')
    expect(sub).toContain('全链数据目录')
    expect(sub).toContain('我的目录')
    expect(sub).toContain('我的数据产品')
    expect(sub).toContain('目录维护')

    const browse = sub.indexOf('data-testid="nav-catalog-browse"')
    const myCatalog = sub.indexOf('data-testid="nav-my-catalog"')
    const myProducts = sub.indexOf('data-testid="nav-my-products"')
    const maintenance = sub.indexOf('data-testid="nav-catalog-maintenance"')
    expect(browse).toBeLessThan(myCatalog)
    expect(myCatalog).toBeLessThan(myProducts)
    expect(myProducts).toBeLessThan(maintenance)

    const groupStart = layoutSrc.indexOf('data-testid="nav-catalog-group"')
    expect(layoutSrc.indexOf('data-testid="nav-my-catalog"')).toBeGreaterThan(groupStart)
    expect(layoutSrc).not.toContain('nav-my-maintenance')
    expect(layoutSrc).not.toContain('/my-maintenance')
  })

  it('906-provider-no-maintenance-menu: PROVIDER 无目录维护菜单；有我的目录', () => {
    expect(canMaintainCatalog('PROVIDER')).toBe(false)
    expect(canSeeMyCatalog('PROVIDER')).toBe(true)
    expect(canSeeMyProducts('PROVIDER')).toBe(true)
    expect(layoutSrc).toContain('v-if="catalogMaintenanceVisible"')
    expect(layoutSrc).toContain('v-if="myCatalogVisible"')
    expect(layoutSrc).toContain("to=\"/my-catalog\"")
    expect(layoutSrc).toContain("to=\"/catalog/maintenance\"")
  })

  it('§4.1 三角色菜单正例+负例：ADMIN 双入口；PROVIDER 无维护；USER 仅全链', () => {
    expect(canSeeMyCatalog('ADMIN')).toBe(true)
    expect(canMaintainCatalog('ADMIN')).toBe(true)
    expect(canSeeMyProducts('ADMIN')).toBe(true)

    expect(canSeeMyCatalog('PROVIDER')).toBe(true)
    expect(canMaintainCatalog('PROVIDER')).toBe(false)
    expect(canSeeMyProducts('PROVIDER')).toBe(true)

    expect(canSeeMyCatalog('USER')).toBe(false)
    expect(canMaintainCatalog('USER')).toBe(false)
    expect(canSeeMyProducts('USER')).toBe(false)

    expect(layoutSrc).toContain("role.value === 'ADMIN'")
    expect(layoutSrc).toContain('userManageVisible')
    expect(layoutSrc).toContain('nav-users')
    expect(layoutSrc).toContain('/admin/users')
    expect(layoutSrc).not.toContain('企业管理')
  })

  it('USER 负例：菜单不可见 + 深链结构不可达（含 /my-catalog）', () => {
    expect(layoutSrc).toContain('isDeepLinkAllowed')
    expect(layoutSrc).toContain("router.replace(ROUTE_CATALOG_BROWSE)")
    expect(routesSrc).toContain('beforeEnter: requireDeepLinkAccess')
    expect(routesSrc).toContain("path: '/catalog/maintenance'")
    expect(routesSrc).toContain("path: '/my-catalog'")
    expect(routesSrc).toContain("path: '/my-products'")
    expect(routesSrc).toContain("path: '/admin/users'")
  })

  it('四 surface：侧栏 active 与 meta.title / 页头可区分', () => {
    expect(layoutSrc).toContain("isActive('/catalog')")
    expect(layoutSrc).toContain("isActive('/my-catalog')")
    expect(layoutSrc).toContain("isActive('/my-products')")
    expect(layoutSrc).toContain("isActive('/catalog/maintenance')")
    expect(layoutSrc).toContain('onMineSurface')
    expect(layoutSrc).toContain('document.title')

    expect(routesSrc).toContain("title: '全链数据目录'")
    expect(routesSrc).toContain("title: '我的数据产品'")
    expect(routesSrc).toContain("title: '目录维护（全量）'")
    expect(routesSrc).toContain("title: '我的目录（本企业/本人）'")
    expect(placeholderSrc).toContain('data-testid="workbench-page-title"')
    expect(placeholderSrc).toContain('我的目录（本企业/本人）')
  })

  it('统一路由 /my-catalog；void 前 /my-maintenance 重定向且非权威路径', () => {
    expect(routesSrc).toContain("path: '/my-catalog'")
    expect(routesSrc).toContain("name: 'my-catalog'")
    expect(routesSrc).toContain("path: '/my-maintenance'")
    expect(routesSrc).toContain("redirect: '/my-catalog'")
    expect(routesSrc).not.toMatch(/name:\s*'my-maintenance'/)
    expect(routesSrc).toContain("path: '/admin/users'")
    expect(routesSrc).toContain("path: '/catalog/maintenance'")
  })

  it('可收起组非回归：aria-expanded / aria-controls / sessionStorage / chevron', () => {
    expect(layoutSrc).toContain(':aria-expanded="catalogGroupOpen"')
    expect(layoutSrc).toContain('aria-controls="nav-catalog-submenu"')
    expect(layoutSrc).toContain('id="nav-catalog-submenu"')
    expect(layoutSrc).toContain("sessionStorage.getItem(CATALOG_GROUP_OPEN_KEY)")
    expect(layoutSrc).toContain("sessionStorage.setItem(CATALOG_GROUP_OPEN_KEY")
    expect(layoutSrc).toContain("CATALOG_GROUP_OPEN_KEY = 'wsc.nav.catalogGroupOpen'")
    expect(layoutSrc).toContain('menu-group-chevron')
    expect(layoutSrc).toContain(':class="{ open: catalogGroupOpen }"')
    expect(layoutSrc).toContain('v-show="catalogGroupOpen"')
    expect(layoutSrc).toContain('catalogGroupActive')
  })

  it('侧栏企业信息区只读（P1）：名称/标识；无切换/编辑；无企业管理页', () => {
    expect(layoutSrc).toContain('data-testid="sidebar-enterprise"')
    expect(layoutSrc).toContain('data-enterprise-readonly="true"')
    expect(layoutSrc).toContain('data-testid="sidebar-enterprise-name"')
    expect(layoutSrc).toContain('data-testid="sidebar-enterprise-id"')
    expect(layoutSrc).toContain('enterpriseName')
    expect(layoutSrc).toContain('session?.enterpriseId')
    expect(layoutSrc).toContain('data-testid="session-role-label"')
    expect(layoutSrc).toContain('roleLabel')
    expect(layoutSrc).not.toMatch(/RoleSwitcher/)
    expect(layoutSrc).not.toContain('企业管理')
    expect(layoutSrc).not.toMatch(/switchEnterprise|editEnterprise|企业切换/)
    expect(layoutSrc).not.toMatch(/aria-haspopup\s*=\s*["']listbox["']/)
  })
})
