import { expect, type Page, type Request } from '@playwright/test'
import {
  fillCreateProductBasics,
  openUserCreateDialog,
  selectIndustryCategory,
} from './v14-selectors'

/** 跨企占位企业名（backend `EnterpriseEntity.NAME_UNASSIGNED`；禁止改 seed）。 */
export const UNASSIGNED_ENTERPRISE_NAME = '未归属默认企业'

/** E2E 深链权威路径（PLAN-WSC-8.3 PO 锁定；禁止把 /my-maintenance 当权威）。 */
export const ROUTE_MY_CATALOG = '/my-catalog'
export const ROUTE_MY_MAINTENANCE_REDIRECT = '/my-maintenance'
export const ROUTE_MY_PRODUCTS = '/my-products'
export const ROUTE_CATALOG_MAINTENANCE = '/catalog/maintenance'
export const ROUTE_ADMIN_USERS = '/admin/users'
export const ROUTE_CATALOG = '/catalog'

export type QueryAudit = {
  productListUrls: string[]
  l2DistributionUrls: string[]
  maintenanceUrls: string[]
}

function pathnameOf(url: string): string {
  try {
    return new URL(url).pathname
  } catch {
    return url
  }
}

export function isProductListRequest(req: Request): boolean {
  if (req.method() !== 'GET') return false
  return /\/catalog\/products\/?$/.test(pathnameOf(req.url()))
}

export function isL2DistributionRequest(req: Request): boolean {
  if (req.method() !== 'GET') return false
  return /\/catalog\/l2-distribution\/?$/.test(pathnameOf(req.url()))
}

export function isMaintenanceListRequest(req: Request): boolean {
  if (req.method() !== 'GET') return false
  return /\/catalog\/maintenance\/entries\/?$/.test(pathnameOf(req.url()))
}

/** 挂载浏览/座序图/维护 query 审计（断言无 enterpriseName / industryCategory）。 */
export function attachQueryAudit(page: Page): QueryAudit {
  const audit: QueryAudit = {
    productListUrls: [],
    l2DistributionUrls: [],
    maintenanceUrls: [],
  }
  page.on('request', (req) => {
    if (isProductListRequest(req)) audit.productListUrls.push(req.url())
    if (isL2DistributionRequest(req)) audit.l2DistributionUrls.push(req.url())
    if (isMaintenanceListRequest(req)) audit.maintenanceUrls.push(req.url())
  })
  return audit
}

export function assertNoForbiddenBrowseQuery(urls: string[]) {
  expect(urls.length, '应至少发出一次 listProducts').toBeGreaterThan(0)
  for (const raw of urls) {
    const u = new URL(raw)
    expect(u.searchParams.has('enterpriseName'), raw).toBe(false)
    expect(u.searchParams.has('industryCategory'), raw).toBe(false)
  }
}

export function searchParamsOf(url: string): URLSearchParams {
  return new URL(url).searchParams
}

/** 任意用户名登录（密码默认 demo）。先等到登录表或退出按钮，再切会话。 */
export async function loginWithCredentials(
  page: Page,
  username: string,
  password = 'demo',
): Promise<void> {
  await page.goto('/login')
  const loginForm = page.getByTestId('login-form')
  const logoutBtn = page.getByRole('button', { name: '退出登录' })
  await expect
    .poll(async () => (await loginForm.count()) + (await logoutBtn.count()), { timeout: 15_000 })
    .toBeGreaterThan(0)
  if ((await loginForm.count()) === 0) {
    await expect(logoutBtn).toBeVisible({ timeout: 15_000 })
    await logoutBtn.click()
  }
  await expect(loginForm).toBeVisible({ timeout: 15_000 })
  await page.locator('input[name="username"]').fill(username)
  await page.locator('input[name="password"]').fill(password)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/overview/, { timeout: 20_000 })
  await expect(page.getByRole('heading', { name: '总览' })).toBeVisible()
}

/** 尝试登录；失败则停在登录页并返回 false（不抛）。 */
export async function tryLogin(
  page: Page,
  username: string,
  password = 'demo',
): Promise<boolean> {
  try {
    await loginWithCredentials(page, username, password)
    return true
  } catch {
    return false
  }
}

/** 展开数据目录子菜单（sessionStorage 可能收起）。 */
export async function ensureCatalogNavOpen(page: Page) {
  const toggle = page.getByTestId('nav-catalog-group-toggle')
  if ((await toggle.count()) === 0) return
  if ((await toggle.getAttribute('aria-expanded')) === 'false') {
    await toggle.click()
  }
}

export async function gotoCatalogBrowse(page: Page) {
  await page.goto(ROUTE_CATALOG)
  await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 20_000 })
  await expect(page.getByTestId('catalog-filters')).toBeVisible({ timeout: 20_000 })
}

export async function waitCatalogListSettled(page: Page) {
  await expect(page.getByTestId('catalog-list')).toBeVisible({ timeout: 20_000 })
  await expect(page.getByTestId('catalog-loading')).toHaveCount(0, { timeout: 20_000 })
}

/** Ant Input 可能把 testid 落在 wrapper；优先填内部 input。 */
export async function fillFilterInput(page: Page, testId: string, value: string) {
  const root = page.getByTestId(testId)
  await expect(root).toBeVisible()
  const inner = root.locator('input').first()
  if ((await inner.count()) > 0) {
    await inner.fill(value)
    return
  }
  await root.fill(value)
}

export async function catalogRowCodes(page: Page): Promise<string[]> {
  const rows = page.getByTestId('catalog-row')
  const n = await rows.count()
  const codes: string[] = []
  for (let i = 0; i < n; i += 1) {
    const code = (await rows.nth(i).locator('.product-code').innerText()).trim()
    if (code) codes.push(code)
  }
  return codes
}

/** 全链 listProducts 全部分页 productCode（不依赖首屏自动续载条数）。 */
export async function fetchAllCatalogProductCodes(page: Page): Promise<string[]> {
  return page.evaluate(async () => {
    const codes: string[] = []
    let pageNo = 1
    const pageSize = 50
    for (;;) {
      const res = await fetch(`/api/v1/catalog/products?page=${pageNo}&pageSize=${pageSize}`, {
        credentials: 'include',
      })
      const json = (await res.json()) as {
        data?: { items?: { productCode?: string }[]; total?: number }
      }
      const items = json.data?.items ?? []
      for (const it of items) {
        if (it.productCode) codes.push(it.productCode)
      }
      const total = json.data?.total ?? codes.length
      if (codes.length >= total || items.length === 0) break
      pageNo += 1
      if (pageNo > 40) break
    }
    return codes
  })
}

export async function sectionTitles(page: Page): Promise<string[]> {
  return page.locator('[data-testid="catalog-l3-section"] .section-title').allTextContents()
}

export function seatMapL1Select(page: Page) {
  return page.locator('[data-testid="seat-map-l1-select"], .l1-select').first()
}

export function browseCascader(page: Page) {
  return page.locator('#catalog-filter-category, [data-testid="catalog-filter-category-cascader"]').first()
}

export function seatMapChips(page: Page) {
  return page.locator('[data-testid^="seat-map-chip-"]')
}

export async function captureSeatMapSnapshot(page: Page) {
  const chips = seatMapChips(page)
  await expect(page.getByTestId('seat-map-title-row')).toBeVisible({ timeout: 20_000 })
  const empty = page.getByTestId('seat-map-empty')
  const total = page.getByTestId('seat-map-total')
  await expect.poll(async () => (await empty.count()) + (await total.count()), { timeout: 20_000 }).toBeGreaterThan(0)
  const chipIds = await chips.evaluateAll((els) =>
    els.map((el) => el.getAttribute('data-testid') || ''),
  )
  const totalText = (await total.count()) > 0 ? (await total.innerText()).trim() : ''
  const emptyVisible = (await empty.count()) > 0
  return { chipIds, totalText, emptyVisible, chipCount: chipIds.length }
}

/** 浏览 Cascader：点开并选第一项 L1；等待 listProducts 带 l1CategoryId。 */
export async function selectFirstBrowseL1(page: Page): Promise<string> {
  const cascader = browseCascader(page)
  await expect(cascader).toBeVisible()
  const opener = page.locator('.filter-ant-cascader .ant-select-selector, #catalog-filter-category').first()
  await opener.click()
  const item = page.locator('.ant-cascader-dropdown:visible .ant-cascader-menu-item').first()
  await expect(item).toBeVisible({ timeout: 15_000 })
  const pending = page.waitForRequest(
    (req) => isProductListRequest(req) && new URL(req.url()).searchParams.has('l1CategoryId'),
    { timeout: 20_000 },
  )
  await item.click()
  const req = await pending
  return new URL(req.url()).searchParams.get('l1CategoryId') || ''
}

/** 若第二列 L2 可见则点第一项，返回是否发出 l2CategoryId。 */
export async function trySelectFirstBrowseL2(page: Page): Promise<boolean> {
  const menus = page.locator('.ant-cascader-dropdown:visible .ant-cascader-menu')
  if ((await menus.count()) < 2) return false
  const l2 = menus.nth(1).locator('.ant-cascader-menu-item').first()
  if ((await l2.count()) === 0) return false
  const pending = page.waitForRequest(
    (req) => isProductListRequest(req) && new URL(req.url()).searchParams.has('l2CategoryId'),
    { timeout: 20_000 },
  )
  await l2.click()
  await pending
  return true
}

export async function selectSeatMapL1NotAll(page: Page): Promise<string> {
  const sel = page.getByTestId('seat-map-l1-select')
  await expect(sel).toBeVisible()
  await sel.locator('.ant-select-selector').click()
  const options = page.locator('.ant-select-dropdown:visible .ant-select-item-option')
  await expect(options.first()).toBeVisible({ timeout: 10_000 })
  const count = await options.count()
  let target = options.nth(0)
  for (let i = 0; i < count; i += 1) {
    const title = (await options.nth(i).innerText()).trim()
    if (title && title !== '全部') {
      target = options.nth(i)
      break
    }
  }
  const label = (await target.innerText()).trim()
  const pending = page.waitForRequest(
    (req) => isL2DistributionRequest(req) && new URL(req.url()).searchParams.has('l1CategoryId'),
    { timeout: 20_000 },
  )
  await target.click()
  await pending
  return label
}

export async function createProductOnMine(
  page: Page,
  name: string,
): Promise<{ code: string }> {
  await page.goto(ROUTE_MY_PRODUCTS)
  await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible({ timeout: 15_000 })
  await page.getByTestId('catalog-open-create').click()
  await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
  const code = await fillCreateProductBasics(page, { name, productType: 'OTHER' })
  await selectIndustryCategory(page)
  await page.getByTestId('product-editor').locator('button[type="submit"]').click()
  await expect(page).toHaveURL(/\/catalog\/products\/\d+(?:\?|$)/, { timeout: 20_000 })
  return { code }
}

/**
 * 用用户管理 UI 创建跨企 USER（enterpriseName=未归属默认企业）。
 * 必须清空继承的 DEMO enterpriseId，否则 id 优先、名称被忽略。
 */
export async function createCrossEnterpriseUserViaAdminUi(
  page: Page,
  opts: { username: string; password?: string; role?: 'USER' | 'PROVIDER' | 'ADMIN' },
): Promise<{ username: string; password: string }> {
  const password = opts.password ?? 'demo'
  await loginWithCredentials(page, 'admin')
  await page.goto(ROUTE_ADMIN_USERS)
  await expect(page.getByTestId('users-admin-page')).toBeVisible({ timeout: 15_000 })
  await expect(page.getByTestId('users-list-card')).toBeVisible({ timeout: 15_000 })
  const dialog = await openUserCreateDialog(page, {
    username: opts.username,
    password,
    displayName: `E2E跨企${opts.username}`,
    role: opts.role ?? 'USER',
  })
  await dialog.getByTestId('user-enterprise-id-input').fill('')
  await dialog.getByTestId('user-enterprise-name-input').fill(UNASSIGNED_ENTERPRISE_NAME)
  await dialog.getByTestId('user-create').click()
  await expect(page.getByTestId('user-create-dialog')).toHaveCount(0, { timeout: 15_000 })
  await expect(page.getByTestId('users-table')).toContainText(opts.username, { timeout: 15_000 })
  const row = page.getByTestId('users-table').locator('tr', { hasText: opts.username })
  await expect(row.getByTestId('user-enterprise-name')).toContainText(UNASSIGNED_ENTERPRISE_NAME)
  return { username: opts.username, password }
}

/** 同企第二 ADMIN：优先种子 admin2；不存在则经用户管理创建（不改 backend seed）。 */
export async function ensureSameEnterprisePeerAdmin(page: Page): Promise<string> {
  if (await tryLogin(page, 'admin2')) {
    return 'admin2'
  }
  const username = `e2e_adm2_${Date.now().toString().slice(-6)}`
  await loginWithCredentials(page, 'admin')
  await page.goto(ROUTE_ADMIN_USERS)
  await expect(page.getByTestId('users-list-card')).toBeVisible({ timeout: 15_000 })
  const dialog = await openUserCreateDialog(page, {
    username,
    password: 'demo',
    displayName: `E2E同企管理员${username}`,
    role: 'ADMIN',
  })
  await dialog.getByTestId('user-create').click()
  await expect(page.getByTestId('user-create-dialog')).toHaveCount(0, { timeout: 15_000 })
  await expect(page.getByTestId('users-table')).toContainText(username, { timeout: 15_000 })
  return username
}

export async function waitMaintenanceReady(page: Page, scope?: 'full' | 'myCatalog') {
  await expect(page.getByTestId('catalog-maintenance')).toBeVisible({ timeout: 20_000 })
  if (scope) {
    await expect(page.getByTestId('catalog-maintenance')).toHaveAttribute(
      'data-maintenance-scope',
      scope,
    )
  }
  await expect(page.locator('.hint').filter({ hasText: '加载中' })).toHaveCount(0, { timeout: 20_000 })
  await expect
    .poll(async () => {
      const list = await page.getByTestId('maintenance-list').count()
      const empty = await page.getByTestId('empty').count()
      return list + empty
    }, { timeout: 20_000 })
    .toBeGreaterThan(0)
}

export async function readMaintenanceTotal(page: Page): Promise<number> {
  const pag = page.getByTestId('pagination')
  if ((await pag.count()) === 0) return 0
  const text = await pag.locator('.total').innerText()
  const m = text.match(/(\d+)/)
  return m ? Number(m[1]) : 0
}

export async function maintenanceEntryCodes(page: Page): Promise<string[]> {
  const items = page.locator('[data-testid="maintenance-list"] .item:not(.select-all-row) .meta')
  const texts = await items.allTextContents()
  return texts.map((t) => t.split('·')[0]?.trim() || '').filter(Boolean)
}

export function uniqueTag(): string {
  return Date.now().toString().slice(-8)
}
