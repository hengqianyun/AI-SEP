import { test, expect, type Page } from '@playwright/test'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { loginAs, type DemoUser } from '../fixtures/auth'
import {
  editUserRoleViaDialog,
  expectPublicCatalogNoWrite,
  expectReadonlyRole,
  fillCreateProductBasics,
  makeProductCode,
  openMineImportDialog,
  openUserCreateDialog,
  ROLE_LABEL,
  seatMapRoot,
  selectIndustryCategory,
  USER_TO_ROLE,
} from '../helpers/v14-selectors'

/**
 * PLAN-WSC-5.2 §6.1 — WSC V1.4 E2E（强制七场景）
 * evidenceId: TESTRUN-WSC-E2E-V14（强制；正式门禁由独立 tester 执行）
 * 规格文件：tests/e2e/specs/p0-wsc-v1.4.spec.ts
 * 报告目录：tests/e2e/reports/p0-wsc-v1.4/（见 playwright.config.ts）
 * 开发侧可本地冒烟；不得自行标 VERIFIED。
 *
 * 禁止改 frontend/backend/contracts；选择器失效 → BLOCKED 交 Orchestrator hotfix。
 */

const FIXTURES_IMPORT = path.resolve(__dirname, '../../fixtures/import')
const LEGACY_TEMPLATE = path.join(FIXTURES_IMPORT, 'legacy-template.csv')
const OPENAPI_SHARED = path.join(FIXTURES_IMPORT, 'openapi-shared-minimal.yaml')
const FULL_SUCCESS = path.join(FIXTURES_IMPORT, 'full-success.csv')
const ALL_FAIL = path.join(FIXTURES_IMPORT, 'all-fail.csv')

function splitCsvLine(line: string): string[] {
  const out: string[] = []
  let cur = ''
  let inQ = false
  for (let i = 0; i < line.length; i++) {
    const ch = line[i]
    if (ch === '"') {
      inQ = !inQ
      cur += ch
      continue
    }
    if (ch === ',' && !inQ) {
      out.push(cur)
      cur = ''
      continue
    }
    cur += ch
  }
  out.push(cur)
  return out
}

/** 唯一化成功行，避免历史占用。 */
function makeUniqueCsvFrom(baseName: string, okRowIndex = 1): string {
  const tag = `E2E${Date.now().toString().slice(-8)}`
  const base = path.join(FIXTURES_IMPORT, baseName)
  expect(fs.existsSync(base), `须存在 tests/fixtures/import/${baseName}`).toBe(true)
  const raw = fs.readFileSync(base, 'utf8').replace(/^\uFEFF/, '')
  const lines = raw.split(/\r?\n/).filter((l) => l.length > 0)
  expect(lines.length).toBeGreaterThanOrEqual(2)
  const cols = splitCsvLine(lines[okRowIndex]!)
  cols[0] = `${cols[0]}-${tag}`
  if (cols[3]) cols[3] = `${cols[3]}-${tag}`
  const body = [lines[0], ...lines.slice(1).map((l, i) => (i + 1 === okRowIndex ? cols.join(',') : l)), ''].join(
    '\n',
  )
  const file = path.join(os.tmpdir(), `wsc-v14-${baseName.replace(/\.csv$/, '')}-${tag}.csv`)
  fs.writeFileSync(file, body, 'utf8')
  return file
}

function makeV0729PartialSuccessFixture(): string {
  const tag = `E2E${Date.now().toString().slice(-8)}`
  const base = path.join(FIXTURES_IMPORT, 'partial-success.csv')
  expect(fs.existsSync(base), '须存在 tests/fixtures/import/partial-success.csv').toBe(true)
  const raw = fs.readFileSync(base, 'utf8').replace(/^\uFEFF/, '')
  const lines = raw.split(/\r?\n/).filter((l) => l.length > 0)
  expect(lines.length).toBeGreaterThanOrEqual(3)
  const okCols = splitCsvLine(lines[1]!)
  okCols[0] = `部分成功有效行-${tag}`
  okCols[3] = `部分成功有效简介-${tag}`
  const body = [lines[0], okCols.join(','), lines[2], ''].join('\n')
  const file = path.join(os.tmpdir(), `wsc-v14-partial-${tag}.csv`)
  fs.writeFileSync(file, body, 'utf8')
  return file
}

async function postProductAsSession(page: Page, body: Record<string, unknown>) {
  return page.evaluate(async (payload) => {
    const res = await fetch('/api/v1/catalog/products', {
      method: 'POST',
      credentials: 'include',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    const json = await res.json()
    return { status: res.status, code: json?.code as string, message: json?.message as string }
  }, body)
}

test.describe('WSC V1.4 E2E (PLAN-WSC-5.2 §6.1 / TESTRUN-WSC-E2E-V14)', () => {
  test.skip(!process.env.E2E_RUN, 'Set E2E_RUN=1 with frontend+backend up to execute')

  // —— 场景 1：真登录三角色 ——
  for (const user of ['admin', 'provider', 'user'] as DemoUser[]) {
    test(`1) 真登录 — ${user} 角色绑定且只读展示`, async ({ page }) => {
      await page.goto('/login')
      await expect(page.getByRole('heading', { name: '接入端工作台' })).toBeVisible()
      await loginAs(page, user)
      await expectReadonlyRole(page, USER_TO_ROLE[user])
      await expect(page.getByTestId('metrics-success')).toBeVisible({ timeout: 20_000 })
    })
  }

  // —— 场景 2：ADMIN 用户管理 ——
  test('2a) ADMIN 用户管理 CRUD 抽样', async ({ page }) => {
    await loginAs(page, 'admin')
    await expect(page.getByTestId('nav-users')).toBeVisible()
    await page.getByTestId('nav-users').click()
    await expect(page).toHaveURL(/\/admin\/users/)
    await expect(page.getByTestId('users-admin-page')).toBeVisible()
    await expect(page.getByTestId('users-forbidden')).toHaveCount(0)
    await expect(page.getByTestId('users-list-card')).toBeVisible({ timeout: 15_000 })

    const tag = Date.now().toString().slice(-6)
    const username = `e2e_u_${tag}`
    // 606：创建字段仅在弹框内 — 先点「创建账号」再填表
    const createDialog = await openUserCreateDialog(page, {
      username,
      password: 'demo-e2e',
      displayName: `E2E用户${tag}`,
      role: 'USER',
    })
    await createDialog.getByTestId('user-create').click()
    await expect(page.getByTestId('user-create-dialog')).toHaveCount(0, { timeout: 15_000 })

    await expect(page.getByTestId('users-table')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('users-table')).toContainText(username)

    const row = page.getByTestId('users-table').locator('tr', { hasText: username })
    // 606：角色 select 仅在编辑角色弹框 — 先点「编辑角色」再改 user-row-role 并保存
    await editUserRoleViaDialog(page, row, 'PROVIDER', ROLE_LABEL.PROVIDER)

    await row.getByTestId('user-soft-delete').click()
    await expect(page.getByTestId('users-table')).not.toContainText(username, { timeout: 15_000 })
  })

  for (const user of ['provider', 'user'] as DemoUser[]) {
    test(`2b) ${user} 不可达用户管理`, async ({ page }) => {
      await loginAs(page, user)
      await expect(page.getByTestId('nav-users')).toHaveCount(0)
      await page.goto('/admin/users')
      await expect(page.getByTestId('users-admin-page')).toBeVisible({ timeout: 15_000 })
      await expect(page.getByTestId('users-forbidden')).toBeVisible()
      await expect(page.getByTestId('user-create')).toHaveCount(0)
      await expect(page.getByTestId('users-table')).toHaveCount(0)
    })
  }

  // —— 场景 3：公共目录只读写入口 ——
  for (const user of ['admin', 'provider', 'user'] as DemoUser[]) {
    test(`3) 公共目录无增改导 — ${user}`, async ({ page }) => {
      await loginAs(page, user)
      await expectPublicCatalogNoWrite(page)
    })
  }

  // —— 场景 4：我的产品 ——
  test('4) PROVIDER 我的产品：增改导 / 无座序图 / 返回仍在我的产品', async ({ page }) => {
    await loginAs(page, 'provider')
    await expect(page.getByTestId('nav-my-products')).toBeVisible()
    await page.getByTestId('nav-my-products').click()
    await expect(page).toHaveURL(/\/my-products/)
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible()
    await expect(page.getByTestId('catalog-open-create')).toBeVisible()
    await expect(page.getByTestId('catalog-open-import')).toBeVisible()
    await expect(page.getByTestId('catalog-seat-map-slot')).toHaveCount(0)
    await expect(page.getByTestId('circulation-seat-map')).toHaveCount(0)

    await page.getByTestId('catalog-open-create').click()
    await expect(page).toHaveURL(/\/catalog\/products\/new/)
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    const tag = Date.now().toString().slice(-6)
    const code = await fillCreateProductBasics(page, {
      name: `E2E V14 我的产品 ${tag}`,
      productType: 'OTHER',
    })
    await selectIndustryCategory(page)
    await page.getByTestId('product-editor').locator('button[type="submit"]').click()
    // 产品 id 为数字主键；禁止匹配仍停留在 /products/new
    await expect(page).toHaveURL(/\/catalog\/products\/\d+(?:\?|$)/, { timeout: 20_000 })
    await expect(page).toHaveURL(/from=mine/)
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })

    // 返回仍在我的产品（详情页 back / 壳层 active）
    await page.getByRole('button', { name: '返回目录' }).click()
    await expect(page).toHaveURL(/\/my-products/, { timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible()
    await expect(page.getByTestId('nav-my-products')).toHaveClass(/active/)

    // 列表本人：应能搜到刚创建的产品
    await page.getByTestId('catalog-search').fill(code)
    await page.getByTestId('catalog-search-btn').click()
    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('catalog-list')).toContainText(code)

    // TASK-WSC-607：PROVIDER 可见目录维护侧栏
    await expect(page.getByTestId('nav-catalog-maintenance')).toBeVisible()
    await page.getByTestId('nav-catalog-maintenance').click()
    await expect(page.getByTestId('catalog-maintenance')).toBeVisible({ timeout: 15_000 })
  })

  test('4x) ADMIN/USER 无「我的产品」菜单且直达被重定向', async ({ page }) => {
    await loginAs(page, 'admin')
    await expect(page.getByTestId('nav-my-products')).toHaveCount(0)
    await page.goto('/my-products')
    await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })

    await loginAs(page, 'user')
    await expect(page.getByTestId('nav-my-products')).toHaveCount(0)
    await page.goto('/my-products')
    await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
  })

  // —— 场景 5：ADMIN 产品写 API 403；分类/维护仍可用 ——
  test('5) ADMIN 产品写 403；分类与维护可达', async ({ page }) => {
    await loginAs(page, 'admin')

    const createRes = await postProductAsSession(page, {
      productCode: makeProductCode('ADM'),
      productName: 'E2E ADMIN 不可写',
      productType: 'OTHER',
      industryCategory: '卫生和社会工作',
    })
    expect(createRes.status).toBe(403)
    expect(createRes.code).toBe('ERR_FORBIDDEN')

    // UI：新增产品路由被重定向离开编辑器
    await page.goto('/catalog/products/new')
    await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
    await expect(page.getByTestId('product-editor')).toHaveCount(0)

    // 分类 / 维护仍可用
    await page.goto('/catalog/admin/categories')
    await expect(page.getByTestId('category-admin')).toBeVisible({ timeout: 15_000 })
    await page.goto('/catalog/maintenance')
    await expect(page.getByTestId('catalog-maintenance')).toBeVisible({ timeout: 15_000 })
    const sidebarMaint = page.locator('nav[aria-label="主导航"] a[href="/catalog/maintenance"]')
    await expect(sidebarMaint).toBeVisible()
  })

  // —— 场景 6：座序图 ——
  test('6) 公共目录座序图 Top5+总数+hover；我的产品无图', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    // 根 testid：父组件 fallthrough `catalog-seat-map-slot` 覆盖组件内 circulation-seat-map
    const mapRoot = seatMapRoot(page)
    await expect(mapRoot).toBeVisible({ timeout: 15_000 })
    await expect(mapRoot.getByRole('heading', { name: '数据流通链' })).toBeVisible()
    await expect(page.getByTestId('seat-map-total')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('seat-map-total')).toContainText(/\d+条数据/)
    await expect(page.getByTestId('seat-map-legend')).toBeVisible()
    const chips = page.locator('[data-testid^="seat-map-chip-"]')
    await expect(chips.first()).toBeVisible()
    const chipCount = await chips.count()
    expect(chipCount).toBeGreaterThan(0)
    expect(chipCount).toBeLessThanOrEqual(5)

    const firstChip = chips.first()
    expect(await firstChip.getAttribute('data-testid')).toBeTruthy()
    await firstChip.hover()
    // hover 后其它 chip 降透明（opacity 内联）；mouseleave 恢复
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

    await loginAs(page, 'provider')
    await page.goto('/my-products')
    await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('catalog-seat-map-slot')).toHaveCount(0)
  })

  // —— 场景 7：V1.3 回归抽样（导入四态 + OpenAPI）——
  test('7a) 导入四态抽样（宿主=我的产品 / PROVIDER）', async ({ page }) => {
    expect(fs.existsSync(FULL_SUCCESS)).toBe(true)
    expect(fs.existsSync(ALL_FAIL)).toBe(true)
    expect(fs.existsSync(LEGACY_TEMPLATE)).toBe(true)

    await loginAs(page, 'provider')

    // 态① all_success
    await openMineImportDialog(page)
    await expect(page.getByTestId('import-template-xlsx')).toContainText(/v0729/)
    await page
      .locator('#import-step-upload input[type="file"]')
      .setInputFiles(makeUniqueCsvFrom('full-success.csv', 1))
    await page.getByRole('button', { name: '开始导入' }).click()
    await expect(page.locator('#import-step-result')).toBeVisible({ timeout: 30_000 })
    await expect(page.locator('#import-step-result')).toHaveAttribute('data-result-state', 'all_success')
    await page.locator('#import-modal-cancel').click()
    await expect(page).toHaveURL(/\/my-products/, { timeout: 10_000 })

    // 态② partial_success
    await openMineImportDialog(page)
    await page
      .locator('#import-step-upload input[type="file"]')
      .setInputFiles(makeV0729PartialSuccessFixture())
    await page.getByRole('button', { name: '开始导入' }).click()
    await expect(page.locator('#import-step-result')).toBeVisible({ timeout: 30_000 })
    await expect(page.locator('#import-step-result')).toHaveAttribute(
      'data-result-state',
      'partial_success',
    )
    await expect(page.getByTestId('import-counts')).toContainText(/成功\s*1/)
    await expect(page.getByTestId('import-counts')).toContainText(/失败\s*1/)
    await expect(page.getByTestId('import-report-link')).toBeVisible()
    await page.locator('#import-modal-cancel').click()

    // 态③ all_row_failure
    await openMineImportDialog(page)
    await page.locator('#import-step-upload input[type="file"]').setInputFiles(ALL_FAIL)
    await page.getByRole('button', { name: '开始导入' }).click()
    await expect(page.locator('#import-step-result')).toBeVisible({ timeout: 30_000 })
    await expect(page.locator('#import-step-result')).toHaveAttribute(
      'data-result-state',
      'all_row_failure',
    )
    await page.locator('#import-modal-cancel').click()

    // 态④ file_rejected（legacy）
    await openMineImportDialog(page)
    await page.locator('#import-step-upload input[type="file"]').setInputFiles(LEGACY_TEMPLATE)
    await page.getByRole('button', { name: '开始导入' }).click()
    await expect(page.locator('#import-step-result')).toBeVisible({ timeout: 30_000 })
    await expect(page.locator('#import-step-result')).toHaveAttribute(
      'data-result-state',
      'file_rejected',
    )
    await expect(page.getByTestId('import-result-title')).toContainText('文件被拒绝')
    await expect(page.locator('#import-step-result .feedback')).toContainText(
      'ERR_IMPORT_TEMPLATE_UNSUPPORTED',
    )
    await expect(page.getByTestId('import-counts')).toHaveCount(0)
  })

  test('7b) OpenAPI 编辑：Swagger 回填 → 保存 → 再开不丢', async ({ page }) => {
    expect(fs.existsSync(OPENAPI_SHARED)).toBe(true)
    const swaggerText = fs.readFileSync(OPENAPI_SHARED, 'utf8')

    await loginAs(page, 'provider')
    await page.goto('/my-products')
    await page.getByTestId('catalog-open-create').click()
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })

    const tag = Date.now().toString().slice(-6)
    await fillCreateProductBasics(page, {
      name: `E2E V14 OpenAPI ${tag}`,
      productType: 'API',
    })
    await selectIndustryCategory(page)

    await expect(page.getByTestId('openapi-editor-panel')).toBeVisible({ timeout: 10_000 })
    await page.getByTestId('openapi-import-btn').click()
    await expect(page.getByTestId('openapi-import-box')).toBeVisible()
    await page.getByTestId('openapi-import-textarea').fill(swaggerText)
    await page.getByTestId('openapi-import-apply').click()
    await expect(page.getByTestId('openapi-sidebar')).toBeVisible()
    await expect(page.getByTestId('ep-path')).toHaveValue(/\/enterprise\/security\/verify/, {
      timeout: 10_000,
    })

    await page.getByTestId('product-editor').locator('button[type="submit"]').click()
    await expect(page).toHaveURL(/\/catalog\/products\/\d+(?:\?|$)/, { timeout: 20_000 })
    const productId = page.url().match(/\/catalog\/products\/(\d+)/)?.[1]
    expect(productId).toBeTruthy()

    await page.goto(`/catalog/products/${productId}/edit?from=mine`)
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('openapi-editor-panel')).toBeVisible()
    await expect(page.getByTestId('ep-path')).toHaveValue(/\/enterprise\/security\/verify/, {
      timeout: 15_000,
    })
    await expect(page.getByTestId('swagger-archive')).toContainText('openapi')
  })
})
