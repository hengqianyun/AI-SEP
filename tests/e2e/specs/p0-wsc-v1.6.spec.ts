import { test, expect } from '@playwright/test'
import { loginAs } from '../fixtures/auth'
import { expectReadonlyRole, openMineImportDialog, seatMapRoot, USER_TO_ROLE } from '../helpers/v14-selectors'
import {
  attachQueryAudit,
  assertNoForbiddenBrowseQuery,
  browseCascader,
  captureSeatMapSnapshot,
  createCrossEnterpriseUserViaAdminUi,
  createProductOnMine,
  ensureCatalogNavOpen,
  ensureSameEnterprisePeerAdmin,
  fillFilterInput,
  fetchAllCatalogProductCodes,
  gotoCatalogBrowse,
  loginWithCredentials,
  readMaintenanceTotal,
  ROUTE_ADMIN_USERS,
  ROUTE_CATALOG_MAINTENANCE,
  ROUTE_MY_CATALOG,
  ROUTE_MY_MAINTENANCE_REDIRECT,
  ROUTE_MY_PRODUCTS,
  searchParamsOf,
  seatMapChips,
  seatMapL1Select,
  sectionTitles,
  selectFirstBrowseL1,
  selectSeatMapL1NotAll,
  trySelectFirstBrowseL2,
  uniqueTag,
  waitCatalogListSettled,
  waitMaintenanceReady,
} from '../helpers/wsc-v16-fixtures'

/**
 * PLAN-WSC-8.3 §6.1 — WSC V1.6 E2E（强制九场景）
 * evidenceId: TESTRUN-WSC-E2E-V16（强制；正式门禁由独立 tester 执行）
 * 规格文件：tests/e2e/specs/p0-wsc-v1.6.spec.ts
 * 报告目录：tests/e2e/reports/p0-wsc-v1.6/（见 playwright.v16.config.ts）
 * 开发侧可本地冒烟；不得自行标 VERIFIED。
 *
 * 禁止改 frontend/backend/contracts；选择器失效 → BLOCKED 交 Orchestrator hotfix。
 * PO 锁定：ADMIN 双入口；PROVIDER 仅我的目录；无企业管理页；座序图无企业筛；
 * 浏览无 industryCategory；全链无企业过滤；路由冻结 /my-catalog。
 */

test.describe('WSC V1.6 E2E (PLAN-WSC-8.3 §6.1 / TESTRUN-WSC-E2E-V16)', () => {

  // —— 场景 1：浏览筛选 V1.6 ——
  test('§6.1-1) 浏览筛选 V1.6：无卡片、高级区常显、无 industryCategory、Cascader、抽样 /my-products', async ({
    page,
  }) => {
    const audit = attachQueryAudit(page)
    await loginAs(page, 'admin')
    await gotoCatalogBrowse(page)

    // 无卡片（空间/行业标签卡）
    await expect(page.getByTestId('catalog-space-tags')).toHaveCount(0)
    await expect(page.getByTestId('catalog-industry-tags')).toHaveCount(0)
    await expect(page.getByTestId('catalog-space-tag')).toHaveCount(0)
    await expect(page.getByTestId('catalog-industry-tag')).toHaveCount(0)
    await expect(page.getByTestId('catalog-industry-select')).toHaveCount(0)
    // 公共目录不渲染标题卡
    await expect(page.getByRole('heading', { name: '数据目录', exact: true })).toHaveCount(0)

    // 高级区常显（无折叠开关）
    await expect(page.getByTestId('catalog-filters')).toBeVisible()
    await expect(page.getByTestId('catalog-search')).toBeVisible()
    await expect(page.getByTestId('catalog-filter-supplier')).toBeVisible()
    await expect(page.getByTestId('catalog-toggle-advanced')).toHaveCount(0)
    await expect(page.getByRole('button', { name: /高级筛选/ })).toHaveCount(0)

    // Cascader l1/l2；无 industryCategory 控件
    await expect(page.getByText('业务视图 / 业务大类', { exact: true })).toBeVisible()
    await expect(browseCascader(page)).toBeVisible()
    await expect(page.getByLabel('行业类别')).toHaveCount(0)
    await expect(page.getByLabel('行业分类')).toHaveCount(0)

    await waitCatalogListSettled(page)
    const l1 = await selectFirstBrowseL1(page)
    expect(l1).toBeTruthy()
    await trySelectFirstBrowseL2(page)

    assertNoForbiddenBrowseQuery(audit.productListUrls)
    expect(
      audit.productListUrls.some((u) => searchParamsOf(u).has('l1CategoryId')),
      'listProducts 应带 l1CategoryId',
    ).toBe(true)

    // 抽样 /my-products：同一套筛栏、无座序图、无 industryCategory
    await page.goto(ROUTE_MY_PRODUCTS)
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible()
    await expect(page.getByTestId('catalog-filters')).toBeVisible()
    await expect(browseCascader(page)).toBeVisible()
    await expect(page.getByTestId('catalog-industry-select')).toHaveCount(0)
    await expect(page.getByTestId('catalog-seat-map-slot')).toHaveCount(0)
    assertNoForbiddenBrowseQuery(audit.productListUrls)
  })

  // —— 场景 2：座序图 L1 ——
  test('§6.1-2) 座序图 L1：标题右下拉、默认全部=Top5、选 L1 过滤、无企业筛、cross-enterprise 一致', async ({
    page,
  }) => {
    await loginAs(page, 'admin')
    await gotoCatalogBrowse(page)
    const mapRoot = seatMapRoot(page)
    await expect(mapRoot).toBeVisible({ timeout: 15_000 })
    const titleRow = page.getByTestId('seat-map-title-row')
    await expect(titleRow.getByRole('heading', { name: '数据流通链' })).toBeVisible()
    const l1Sel = seatMapL1Select(page)
    await expect(l1Sel).toBeVisible()
    await expect(l1Sel).toContainText('全部')
    // 无企业筛
    await expect(page.getByTestId('seat-map-enterprise')).toHaveCount(0)
    await expect(titleRow.getByText(/企业筛|所属企业|enterpriseName/)).toHaveCount(0)

    const adminSnap = await captureSeatMapSnapshot(page)
    if (!adminSnap.emptyVisible) {
      expect(adminSnap.chipCount).toBeGreaterThan(0)
      expect(adminSnap.chipCount).toBeLessThanOrEqual(5)
      await expect(page.getByTestId('seat-map-legend')).toBeVisible()
    }

    const beforeChips = adminSnap.chipIds.join('|')
    await selectSeatMapL1NotAll(page)
    const filtered = await captureSeatMapSnapshot(page)
    expect(filtered.emptyVisible || filtered.chipIds.join('|') !== beforeChips || filtered.totalText !== adminSnap.totalText).toBe(
      true,
    )
    if (!filtered.emptyVisible) {
      expect(filtered.chipCount).toBeLessThanOrEqual(5)
    }

    const xUser = `e2e_xent_${uniqueTag()}`
    await createCrossEnterpriseUserViaAdminUi(page, { username: xUser, role: 'USER' })
    await loginWithCredentials(page, xUser)
    await gotoCatalogBrowse(page)
    await expect(seatMapL1Select(page)).toContainText('全部')
    const peerSnap = await captureSeatMapSnapshot(page)
    expect(peerSnap.chipIds).toEqual(adminSnap.chipIds)
    expect(peerSnap.totalText).toBe(adminSnap.totalText)
    expect(peerSnap.emptyVisible).toBe(adminSnap.emptyVisible)
  })

  // —— 场景 3：全链无企业过滤 ——
  test('§6.1-3) 全链无企业过滤：跨企用户列表一致、supplierName/未分类不回退、query 无 enterpriseName', async ({
    page,
  }) => {
    const audit = attachQueryAudit(page)
    await loginAs(page, 'admin')
    await gotoCatalogBrowse(page)
    await waitCatalogListSettled(page)
    const adminCodes = await fetchAllCatalogProductCodes(page)
    expect(adminCodes.length).toBeGreaterThan(0)

    await expect(page.getByTestId('catalog-filter-supplier')).toBeVisible()
    await fillFilterInput(page, 'catalog-filter-supplier', '演示')
    const supplierReq = page.waitForRequest(
      (req) => /\/catalog\/products\/?$/.test(new URL(req.url()).pathname) && new URL(req.url()).searchParams.has('supplierName'),
      { timeout: 20_000 },
    )
    await page.getByTestId('catalog-search-btn').click()
    const sReq = await supplierReq
    expect(searchParamsOf(sReq.url()).get('supplierName')).toBe('演示')
    expect(searchParamsOf(sReq.url()).has('enterpriseName')).toBe(false)
    await page.getByTestId('catalog-reset-btn').click()
    await waitCatalogListSettled(page)

    const titles = await sectionTitles(page)
    if (titles.includes('未分类数据')) {
      expect(titles[titles.length - 1]).toBe('未分类数据')
    }

    const xUser = `e2e_xcat_${uniqueTag()}`
    await createCrossEnterpriseUserViaAdminUi(page, { username: xUser, role: 'USER' })
    await loginWithCredentials(page, xUser)
    await gotoCatalogBrowse(page)
    await waitCatalogListSettled(page)
    const peerCodes = await fetchAllCatalogProductCodes(page)
    expect(peerCodes).toEqual(adminCodes)

    assertNoForbiddenBrowseQuery(audit.productListUrls)
  })

  // —— 场景 4：我的数据产品本企业 ——
  test('§6.1-4) 我的数据产品本企业：ADMIN+PROVIDER 可见、ADMIN 可增改导、同企异 create_by 可见', async ({
    page,
  }) => {
    await loginAs(page, 'admin')
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-my-products')).toBeVisible()
    await page.getByTestId('nav-my-products').click()
    await expect(page).toHaveURL(/\/my-products/)
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible()
    await expect(page.getByTestId('catalog-open-create')).toBeVisible()
    await expect(page.getByTestId('catalog-open-import')).toBeVisible()

    const tag = uniqueTag()
    const adminName = `E2E V16 ADMIN 本企 ${tag}`
    const { code: adminCode } = await createProductOnMine(page, adminName)
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })
    await page.getByRole('button', { name: '返回目录' }).click()
    await expect(page).toHaveURL(/\/my-products/, { timeout: 15_000 })

    await fillFilterInput(page, 'catalog-search', adminCode)
    await page.getByTestId('catalog-search-btn').click()
    await expect(page.getByTestId('catalog-list')).toContainText(adminCode, { timeout: 15_000 })
    await page.getByTestId('catalog-row').first().click()
    await page.getByTestId('catalog-go-edit').click()
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    await page.locator('label:has-text("产品名称") input').fill(`${adminName} 改`)
    await page.getByTestId('product-editor').locator('button[type="submit"]').click()
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 20_000 })

    await openMineImportDialog(page)
    await expect(page.getByRole('heading', { name: '批量导入产品' })).toBeVisible()
    await page.locator('#import-modal-cancel').click()

    const peer = await ensureSameEnterprisePeerAdmin(page)
    const peerName = `E2E V16 PEER ${tag}`
    await loginWithCredentials(page, peer)
    const { code: peerCode } = await createProductOnMine(page, peerName)

    await loginWithCredentials(page, 'admin')
    await page.goto(ROUTE_MY_PRODUCTS)
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible({ timeout: 15_000 })
    await fillFilterInput(page, 'catalog-search', peerCode)
    await page.getByTestId('catalog-search-btn').click()
    await expect(page.getByTestId('catalog-list')).toContainText(peerCode, { timeout: 15_000 })

    await loginWithCredentials(page, 'provider')
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-my-products')).toBeVisible()
    await page.goto(ROUTE_MY_PRODUCTS)
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible()
    await expect(page.getByTestId('catalog-open-create')).toBeVisible()
  })

  // —— 场景 5：导航 ADMIN 双入口 ——
  test('§6.1-5) 导航 ADMIN 双入口：目录维护全量 + /my-catalog 本企业、深链 N vs M、用户管理不删', async ({
    page,
  }) => {
    await loginAs(page, 'admin')
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-catalog-browse')).toBeVisible()
    await expect(page.getByTestId('nav-my-catalog')).toBeVisible()
    await expect(page.getByTestId('nav-my-products')).toBeVisible()
    await expect(page.getByTestId('nav-catalog-maintenance')).toBeVisible()
    await expect(page.getByTestId('nav-users')).toBeVisible()
    await expect(page.getByTestId('workbench-sidebar').getByText('企业管理', { exact: true })).toHaveCount(0)

    await page.getByTestId('nav-catalog-maintenance').click()
    await expect(page).toHaveURL(/\/catalog\/maintenance/)
    await waitMaintenanceReady(page, 'full')
    await expect(page.getByTestId('workbench-page-title')).toHaveText('目录维护（全量）')
    const n = await readMaintenanceTotal(page)

    await page.getByTestId('nav-my-catalog').click()
    await expect(page).toHaveURL(new RegExp(`${ROUTE_MY_CATALOG.replace('/', '\\/')}(?:\\?|$)`))
    await waitMaintenanceReady(page, 'myCatalog')
    await expect(page.getByTestId('workbench-page-title')).toHaveText('我的目录（本企业/本人）')
    const m = await readMaintenanceTotal(page)
    expect(m).toBeLessThanOrEqual(n)
    expect(n).toBeGreaterThan(0)

    await page.goto(ROUTE_MY_MAINTENANCE_REDIRECT)
    await expect(page).toHaveURL(/\/my-catalog/)
    await expect(page.getByTestId('catalog-maintenance')).toHaveAttribute('data-maintenance-scope', 'myCatalog')

    await page.goto(ROUTE_ADMIN_USERS)
    await expect(page.getByTestId('users-admin-page')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('users-forbidden')).toHaveCount(0)
    await expect(page.getByTestId('users-list-card')).toBeVisible()

    // 跨企产品仅出现在全量：用跨企 PROVIDER 建一条后再比 N vs M
    const xProv = `e2e_xp_${uniqueTag()}`
    await createCrossEnterpriseUserViaAdminUi(page, { username: xProv, role: 'PROVIDER' })
    await loginWithCredentials(page, xProv)
    const { code: xCode } = await createProductOnMine(page, `E2E V16 跨企产品 ${uniqueTag()}`)

    await loginWithCredentials(page, 'admin')
    await page.goto(ROUTE_CATALOG_MAINTENANCE)
    await waitMaintenanceReady(page, 'full')
    await expect(page.getByTestId('workbench-page-title')).toHaveText('目录维护（全量）')
    const n2 = await readMaintenanceTotal(page)
    await page.goto(ROUTE_MY_CATALOG)
    await waitMaintenanceReady(page, 'myCatalog')
    await expect(page.getByTestId('workbench-page-title')).toHaveText('我的目录（本企业/本人）')
    const m2 = await readMaintenanceTotal(page)
    expect(n2).toBeGreaterThan(m2)
    expect(n2).toBeGreaterThanOrEqual(n)
    expect(xCode).toBeTruthy()
  })

  // —— 场景 6：PROVIDER 仅我的目录 + USER 隔离 ——
  test('§6.1-6a) PROVIDER 仅我的目录：无目录维护、深链维护不可达、我的目录本人 scope', async ({
    page,
  }) => {
    const tag = uniqueTag()
    await loginAs(page, 'admin')
    const { code: adminOnly } = await createProductOnMine(page, `E2E V16 ADMIN-ONLY ${tag}`)

    await loginWithCredentials(page, 'provider')
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-my-catalog')).toBeVisible()
    await expect(page.getByTestId('nav-my-products')).toBeVisible()
    await expect(page.getByTestId('nav-catalog-browse')).toBeVisible()
    await expect(page.getByTestId('nav-catalog-maintenance')).toHaveCount(0)
    await expect(page.getByTestId('nav-users')).toHaveCount(0)

    await page.goto(ROUTE_CATALOG_MAINTENANCE)
    await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
    await expect(page.getByTestId('catalog-maintenance')).toHaveCount(0)

    await page.goto(ROUTE_MY_CATALOG)
    await expect(page).toHaveURL(/\/my-catalog/)
    await waitMaintenanceReady(page, 'myCatalog')
    await expect(page.getByTestId('catalog-maintenance')).toHaveAttribute('data-maintenance-scope', 'myCatalog')
    const body = await page.getByTestId('catalog-maintenance').innerText()
    expect(body).not.toContain(adminOnly)

    const { code: mineCode } = await createProductOnMine(page, `E2E V16 PROVIDER-MINE ${tag}`)
    await page.goto(ROUTE_MY_CATALOG)
    await waitMaintenanceReady(page, 'myCatalog')
    await expect(page.getByTestId('catalog-maintenance')).toContainText(mineCode, { timeout: 15_000 })
  })

  test('§6.1-6b) USER 隔离：菜单隐藏 + 深链 /my-catalog /my-products /catalog/maintenance /admin/users 不可达', async ({
    page,
  }) => {
    await loginAs(page, 'user')
    await expectReadonlyRole(page, USER_TO_ROLE.user)
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-catalog-browse')).toBeVisible()
    await expect(page.getByTestId('nav-my-catalog')).toHaveCount(0)
    await expect(page.getByTestId('nav-my-products')).toHaveCount(0)
    await expect(page.getByTestId('nav-catalog-maintenance')).toHaveCount(0)
    await expect(page.getByTestId('nav-users')).toHaveCount(0)

    for (const path of [ROUTE_MY_CATALOG, ROUTE_MY_PRODUCTS, ROUTE_CATALOG_MAINTENANCE, ROUTE_ADMIN_USERS] as const) {
      await page.goto(path)
      await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
      await expect(page.getByTestId('catalog-maintenance')).toHaveCount(0)
      await expect(page.getByTestId('users-admin-page')).toHaveCount(0)
      await expect(page.getByRole('heading', { name: '我的数据产品' })).toHaveCount(0)
    }
  })

  // —— 场景 7：我的目录维护 UX ——
  test('§6.1-7) 我的目录维护 UX：Pagination/Cascader/本页全选/统一维护；ADMIN 本企业 vs 全量分离', async ({
    page,
  }) => {
    await loginAs(page, 'admin')
    await page.goto(ROUTE_MY_CATALOG)
    await waitMaintenanceReady(page, 'myCatalog')
    await expect(page.locator('[data-testid="filter-category-cascader"], .filter-right .maint-cascader').first()).toBeVisible()
    await expect(page.getByTestId('pagination')).toBeVisible()
    const jumper = page.locator(
      '[data-testid="pagination"] .ant-pagination-options-quick-jumper, [data-testid="pagination"] .ant-pagination-options input, [data-testid="a-pagination"] input',
    )
    const fullPag = page.getByTestId('pagination')
    await expect(fullPag).toContainText(/共\s*\d+\s*条/)
    await expect(page.locator('.ant-pagination').first()).toBeVisible()
    if ((await jumper.count()) > 0) {
      await expect(jumper.first()).toBeVisible()
    } else {
      await expect(page.getByTestId('pagination').getByRole('listitem', { name: '1' })).toBeVisible()
    }
    await expect(page.getByTestId('select-all-page')).toBeVisible()
    await expect(page.getByText('本页全选')).toBeVisible()

    await page.getByTestId('select-all-page').check()
    await expect(page.getByTestId('batch-bar')).toBeVisible()
    await expect(page.getByTestId('batch-save')).toHaveText('统一维护')
    await expect(page.locator('[data-testid="batch-category-cascader"], .batch-actions .maint-cascader').first()).toBeVisible()

    const m = await readMaintenanceTotal(page)
    await page.goto(ROUTE_CATALOG_MAINTENANCE)
    await waitMaintenanceReady(page, 'full')
    const n = await readMaintenanceTotal(page)
    expect(m).toBeLessThanOrEqual(n)
    await expect(page.getByTestId('catalog-maintenance')).toHaveAttribute('data-maintenance-scope', 'full')
    await expect(page.getByTestId('pagination')).toBeVisible()
    await expect(page.getByTestId('select-all-page')).toBeVisible()
  })

  // —— 场景 8：行业类别非浏览 ——
  test('§6.1-8) 行业类别非浏览：编辑/导入仍含行业类别；浏览 query 无 industryCategory', async ({
    page,
  }) => {
    const audit = attachQueryAudit(page)
    await loginAs(page, 'admin')
    await gotoCatalogBrowse(page)
    await waitCatalogListSettled(page)
    await expect(page.getByTestId('industry-category-select')).toHaveCount(0)
    await expect(page.getByTestId('industry-category')).toHaveCount(0)
    assertNoForbiddenBrowseQuery(audit.productListUrls)

    await page.goto(ROUTE_MY_PRODUCTS)
    await page.getByTestId('catalog-open-create').click()
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    const industry = page.getByTestId('industry-category-select')
    await expect(industry).toBeVisible()
    const optionTexts = await industry.locator('option').allTextContents()
    expect(optionTexts).toContain('卫生和社会工作')
    expect(optionTexts).toContain('建筑业')
    expect(optionTexts.filter((t) => t.trim().length > 0).length).toBeGreaterThanOrEqual(20)

    await page.goto(ROUTE_MY_PRODUCTS)
    const dialog = await openMineImportDialog(page)
    await expect(dialog.getByTestId('import-template-xlsx')).toBeVisible()
    const csvHref = await dialog.getByTestId('import-template-csv').getAttribute('href')
    expect(csvHref).toBeTruthy()
    const csv = await page.evaluate(async (url) => {
      const res = await fetch(url as string, { credentials: 'include' })
      return res.text()
    }, csvHref)
    expect(csv).toMatch(/行业分类/)
    await page.locator('#import-modal-cancel').click()

    assertNoForbiddenBrowseQuery(audit.productListUrls)
  })

  // —— 场景 9：V1.5/V1.4 回归抽样 ——
  test('§6.1-9) V1.5/V1.4 回归抽样：supplierName、未分类垫底、座序图 hover', async ({ page }) => {
    const audit = attachQueryAudit(page)
    await loginAs(page, 'admin')
    await gotoCatalogBrowse(page)
    await waitCatalogListSettled(page)

    await expect(page.getByTestId('catalog-filter-supplier')).toBeVisible()
    await fillFilterInput(page, 'catalog-filter-supplier', '演示企业')
    const pending = page.waitForRequest(
      (req) => /\/catalog\/products\/?$/.test(new URL(req.url()).pathname) && new URL(req.url()).searchParams.has('supplierName'),
      { timeout: 20_000 },
    )
    await page.getByTestId('catalog-search-btn').click()
    const req = await pending
    expect(searchParamsOf(req.url()).get('supplierName')).toBe('演示企业')
    expect(searchParamsOf(req.url()).has('enterpriseName')).toBe(false)
    await page.getByTestId('catalog-reset-btn').click()
    await waitCatalogListSettled(page)

    const titles = await sectionTitles(page)
    if (titles.includes('未分类数据')) {
      expect(titles[titles.length - 1]).toBe('未分类数据')
    } else {
      // 本页已加载列表若暂无未分类分节，仍断言分组机制：不得出现「未分类」插在中间
      const idx = titles.findIndex((t) => t.includes('未分类'))
      if (idx >= 0) expect(idx).toBe(titles.length - 1)
    }

    const chips = seatMapChips(page)
    await expect(chips.first()).toBeVisible({ timeout: 20_000 })
    const chipCount = await chips.count()
    expect(chipCount).toBeGreaterThan(0)
    expect(chipCount).toBeLessThanOrEqual(5)
    await chips.first().hover()
    if (chipCount > 1) {
      const other = chips.nth(1)
      await expect
        .poll(async () => other.evaluate((el) => getComputedStyle(el).opacity), { timeout: 5_000 })
        .toBe('0.45')
    }
    await page.getByTestId('seat-map-total').hover()
    if (chipCount > 1) {
      const other = chips.nth(1)
      await expect
        .poll(async () => other.evaluate((el) => getComputedStyle(el).opacity), { timeout: 5_000 })
        .toBe('1')
    }

    assertNoForbiddenBrowseQuery(audit.productListUrls)
  })
})
