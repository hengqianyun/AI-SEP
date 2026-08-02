import { test, expect, type Page } from '@playwright/test'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { loginAs } from '../fixtures/auth'

/**
 * PLAN-WSC-4.1 §6.1 — WSC V1.3 E2E（导入 v0729 / OpenAPI / 三角色）
 * evidenceId: TESTRUN-WSC-E2E-V13（强制；正式门禁由独立 tester 执行）
 * 规格文件：tests/e2e/specs/p0-wsc-v1.3.spec.ts
 * 报告目录：tests/e2e/reports/p0-wsc-v1.3/（见 playwright.config.ts）
 * 开发侧可本地冒烟；不得自行标 VERIFIED。
 *
 * FIND-WSC-304-R1-001：角色切换/期望助手内联本文件，仅依赖 writeSet 内已有
 * `loginAs`（`tests/e2e/fixtures/auth.ts` 不在 304 writeSet，禁止改其签名）。
 */

type AppRole = 'ADMIN' | 'PROVIDER' | 'USER'

const ROLE_LABEL: Record<AppRole, string> = {
  ADMIN: '管理员',
  PROVIDER: '提供方',
  USER: '普通用户',
}

/** 内联：当前角色（UX RoleSwitcher 下拉；不依赖共享 fixture 新签名） */
async function expectCurrentRole(page: Page, role: AppRole) {
  await expect(page.getByTestId('role-switcher')).toContainText(ROLE_LABEL[role], {
    timeout: 15_000,
  })
}

/** 内联：壳层角色切换（适配 UX 下拉；不修改 tests/e2e/fixtures/auth.ts） */
async function switchRoleInline(page: Page, role: AppRole) {
  const switcher = page.getByTestId('role-switcher')
  await expect(switcher).toBeVisible({ timeout: 15_000 })
  const trigger = switcher.locator('button.role-dropdown-trigger')
  await expect(trigger).toBeEnabled({ timeout: 15_000 })
  const text = (await switcher.innerText()).trim()
  if (text.includes(ROLE_LABEL[role])) {
    return
  }
  await trigger.click()
  await page.getByRole('option', { name: ROLE_LABEL[role] }).click()
  await expectCurrentRole(page, role)
}

const FIXTURES_IMPORT = path.resolve(__dirname, '../../fixtures/import')
const LEGACY_TEMPLATE = path.join(FIXTURES_IMPORT, 'legacy-template.csv')
const OPENAPI_SHARED = path.join(FIXTURES_IMPORT, 'openapi-shared-minimal.yaml')

/**
 * v0729 混行：以仓内已验证 `partial-success.csv` 为模板，仅唯一化成功行名称，
 * 避免历史占用；列集/行业路径与 TASK-WSC-301 集成测一致。
 * （若 live 后端仍为 V1.1 导入，本文件会行级全失败 — 见 BUG-WSC-304-001/002 / DEV 报告）
 */
function makeV0729PartialSuccessFixture(): string {
  const tag = `E2E${Date.now().toString().slice(-8)}`
  const base = path.join(FIXTURES_IMPORT, 'partial-success.csv')
  expect(fs.existsSync(base), '须存在 tests/fixtures/import/partial-success.csv').toBe(true)
  const raw = fs.readFileSync(base, 'utf8').replace(/^\uFEFF/, '')
  const lines = raw.split(/\r?\n/).filter((l) => l.length > 0)
  expect(lines.length).toBeGreaterThanOrEqual(3)
  // 成功行：替换产品名称与简介为唯一值，其余列保持 301 已验证形态
  const okCols = splitCsvLine(lines[1])
  okCols[0] = `部分成功有效行-${tag}`
  okCols[3] = `部分成功有效简介-${tag}`
  const body = [lines[0], okCols.join(','), lines[2], ''].join('\n')
  const file = path.join(os.tmpdir(), `wsc-v13-partial-${tag}.csv`)
  fs.writeFileSync(file, body, 'utf8')
  return file
}

/** 极简 CSV 拆行（本 fixture 无嵌套逗号字段，除数据规模已含引号）。 */
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

/** 产品编码须三段 `{DOMAIN}-{FEATURE}-{NNNN}`（≥4 位数字），禁止四段。 */
function makeProductCode(feature: string): string {
  const nnnn = Date.now().toString().slice(-6)
  return `E2E-${feature}-${nnnn}`
}

async function selectCascadeL3(page: Page) {
  await page.getByTestId('cat-l1').selectOption({ index: 1 })
  await expect(page.getByTestId('cat-l2')).toBeEnabled()
  await page.getByTestId('cat-l2').selectOption({ index: 1 })
  await expect(page.getByTestId('cat-l3')).toBeEnabled()
  await page.getByTestId('cat-l3').selectOption({ index: 1 })
  await expect(page.getByTestId('category-path-preview')).toBeVisible()
}

function writeDemo(page: Page) {
  return page.getByLabel('写入口演示')
}

async function expectWriteButtons(
  page: Page,
  opts: {
    category: boolean
    maintenance: boolean
    productWrite: boolean
    importEntry: boolean
  },
) {
  const demo = writeDemo(page)
  await expect(demo).toBeVisible()

  const catBtn = demo.getByRole('button', { name: '维护行业分类' })
  const maintBtn = demo.getByRole('button', { name: '目录维护' })
  const newBtn = demo.getByRole('button', { name: '新增产品' })
  const importBtn = demo.getByRole('button', { name: '批量导入' })

  if (opts.category) await expect(catBtn).toBeVisible()
  else await expect(catBtn).toHaveCount(0)

  if (opts.maintenance) await expect(maintBtn).toBeVisible()
  else await expect(maintBtn).toHaveCount(0)

  if (opts.productWrite) await expect(newBtn).toBeVisible()
  else await expect(newBtn).toHaveCount(0)

  if (opts.importEntry) await expect(importBtn).toBeVisible()
  else await expect(importBtn).toHaveCount(0)

  const sidebarMaint = page.locator('nav[aria-label="主导航"] a[href="/catalog/maintenance"]')
  if (opts.maintenance) await expect(sidebarMaint).toBeVisible()
  else await expect(sidebarMaint).toHaveCount(0)
}

async function expectRouteStructurallyBlocked(page: Page, pathName: string) {
  await page.goto(pathName)
  await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
}

async function openImportDialog(page: Page) {
  await page.goto('/catalog?import=1')
  const dialog = page.locator('#import-modal')
  await expect(dialog).toBeVisible({ timeout: 8_000 })
  await expect(page.getByRole('heading', { name: '批量导入产品' })).toBeVisible()
  return dialog
}

test.describe('WSC V1.3 E2E (PLAN-WSC-4.1 §6.1 / TESTRUN-WSC-E2E-V13)', () => {
  test.skip(!process.env.E2E_RUN, 'Set E2E_RUN=1 with frontend+backend up to execute')

  // —— 场景 1：V1.1/V1.2 主路径回归抽样 ——
  test('1a) 登录/角色切换抽样 + 总览指标', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByRole('heading', { name: '接入端工作台' })).toBeVisible()
    await loginAs(page, 'admin')
    await expectCurrentRole(page, 'ADMIN')
    await expect(page.getByTestId('metrics-success')).toBeVisible({ timeout: 20_000 })
    await switchRoleInline(page, 'PROVIDER')
    await switchRoleInline(page, 'USER')
    await switchRoleInline(page, 'ADMIN')
  })

  test('1b) 目录筛选/详情/上链不回退', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible()
    await page.getByLabel('产品类型').selectOption('DATASET')
    await page.getByTestId('catalog-search').fill('电子病历')
    await page.getByTestId('catalog-search-btn').click()
    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })

    await page.getByTestId('catalog-row').first().click()
    await page.getByTestId('catalog-go-detail').click()
    await expect(page).toHaveURL(/\/catalog\/products\//)
    await expect(page.getByTestId('product-detail')).toBeVisible()
    await expect(page.getByTestId('category-path')).toBeVisible()

    await page.goto('/chain/products/prod-demo-chain-001')
    await expect(page.getByTestId('snapshot-ready')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText('存证上链')).toBeVisible()
  })

  test('1c) 编辑入口可达（PROVIDER 新增产品页）', async ({ page }) => {
    await loginAs(page, 'provider')
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '新增数据产品' })).toBeVisible()
  })

  // —— 场景 2：v0729 导入 — 强制 partial-success ——
  test('2) v0729 导入强制 partial-success：两计数 + 报告入口', async ({ page }) => {
    expect(fs.existsSync(path.join(FIXTURES_IMPORT, 'partial-success.csv'))).toBe(true)

    await loginAs(page, 'admin')
    await openImportDialog(page)

    await expect(page.getByTestId('import-template-xlsx')).toBeVisible()
    await expect(page.getByTestId('import-template-xlsx')).toContainText(/v0729/)

    const fixturePartial = makeV0729PartialSuccessFixture()
    await page.locator('#import-step-upload input[type="file"]').setInputFiles(fixturePartial)
    await page.getByRole('button', { name: '开始导入' }).click()

    await expect(page.locator('#import-step-result')).toBeVisible({ timeout: 30_000 })
    await expect(page.locator('#import-step-result')).toHaveAttribute(
      'data-result-state',
      'partial_success',
    )
    await expect(page.getByTestId('import-counts')).toContainText(/成功\s*1/)
    await expect(page.getByTestId('import-counts')).toContainText(/失败\s*1/)

    const reportLink = page.getByTestId('import-report-link')
    await expect(reportLink).toBeVisible()
    await expect(page.locator('.report-meta')).toBeVisible({ timeout: 10_000 })
  })

  // —— 场景 3：旧模板拒绝 ——
  test('3) legacy-template → 态④ + ERR_IMPORT_TEMPLATE_UNSUPPORTED', async ({ page }) => {
    expect(
      fs.existsSync(LEGACY_TEMPLATE),
      '须存在 tests/fixtures/import/legacy-template.csv（TASK-WSC-301）',
    ).toBe(true)

    await loginAs(page, 'admin')
    await openImportDialog(page)

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
    // 非行级结果态：无成功/失败双计数伪装
    await expect(page.getByTestId('import-counts')).toHaveCount(0)
    await expect(page.getByTestId('import-report-link')).toHaveCount(0)
  })

  // —— 场景 4：OpenAPI 编辑 ——
  test('4) OpenAPI 编辑：Swagger 回填 → 保存 → 再开不丢', async ({ page }) => {
    expect(fs.existsSync(OPENAPI_SHARED)).toBe(true)
    const swaggerText = fs.readFileSync(OPENAPI_SHARED, 'utf8')

    await loginAs(page, 'provider')
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })

    // BUG-WSC-304-003：须三段式 E2E-{FEATURE}-{NNNN}，禁止 E2E-V13-API-* 四段
    const code = makeProductCode('API')
    await page.locator('label:has-text("产品名称") input').fill(`E2E V13 OpenAPI ${code}`)
    await page.locator('label:has-text("产品编码") input').fill(code)
    await page.getByTestId('product-type').selectOption('API')
    await selectCascadeL3(page)

    await expect(page.getByTestId('openapi-editor-panel')).toBeVisible({ timeout: 10_000 })
    await page.getByTestId('openapi-import-btn').click()
    await expect(page.getByTestId('openapi-import-box')).toBeVisible()
    await page.getByTestId('openapi-import-textarea').fill(swaggerText)
    await page.getByTestId('openapi-import-apply').click()
    await expect(page.getByTestId('openapi-sidebar')).toBeVisible()
    await expect(page.getByTestId('ep-summary')).toBeVisible({ timeout: 10_000 })
    await expect(page.getByTestId('ep-path')).toHaveValue(/\/enterprise\/security\/verify/)

    const submit = page.getByTestId('product-editor').locator('button[type="submit"]')
    await submit.click()
    await expect(page).toHaveURL(/\/catalog\/products\/prod-gen-/, { timeout: 20_000 })
    const productId = page.url().split('/').pop()!

    await page.goto(`/catalog/products/${productId}/edit`)
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('openapi-editor-panel')).toBeVisible()
    await expect(page.getByTestId('ep-path')).toHaveValue(/\/enterprise\/security\/verify/, {
      timeout: 15_000,
    })
    await expect(page.getByTestId('swagger-archive')).toContainText('openapi')
  })

  // —— 场景 5：详情只读 ——
  test('5) 详情只读：API 产品可见 endpoints / 文档信息', async ({ page }) => {
    expect(fs.existsSync(OPENAPI_SHARED)).toBe(true)
    const swaggerText = fs.readFileSync(OPENAPI_SHARED, 'utf8')

    await loginAs(page, 'admin')
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })

    // BUG-WSC-304-003：三段式编码
    const code = makeProductCode('DET')
    await page.locator('label:has-text("产品名称") input').fill(`E2E V13 只读 API ${code}`)
    await page.locator('label:has-text("产品编码") input').fill(code)
    await page.getByTestId('product-type').selectOption('API')
    await selectCascadeL3(page)

    await page.getByTestId('openapi-import-btn').click()
    await page.getByTestId('openapi-import-textarea').fill(swaggerText)
    await page.getByTestId('openapi-import-apply').click()
    await expect(page.getByTestId('ep-path')).toHaveValue(/\/enterprise\/security\/verify/, {
      timeout: 10_000,
    })

    await page.getByTestId('product-editor').locator('button[type="submit"]').click()
    await expect(page).toHaveURL(/\/catalog\/products\/prod-gen-/, { timeout: 20_000 })

    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('type-specific')).toBeVisible()
    await expect(page.getByTestId('openapi-readonly')).toBeVisible()
    await expect(page.getByTestId('openapi-doc-info')).toBeVisible()
    await expect(page.getByTestId('endpoint-list')).toBeVisible()
    await expect(page.getByTestId('endpoint-item-0')).toBeVisible()
    await expect(page.getByTestId('endpoint-detail')).toBeVisible()
    // 只读：无写控件
    await expect(page.getByTestId('openapi-add-endpoint')).toHaveCount(0)
    await expect(page.getByTestId('openapi-import-btn')).toHaveCount(0)
  })

  // —— 场景 6：三角色矩阵 ——
  test('6) 三角色写入口矩阵 ADMIN/PROVIDER/USER（含导入）', async ({ page }) => {
    await loginAs(page, 'admin')
    await expectCurrentRole(page, 'ADMIN')
    await expectWriteButtons(page, {
      category: true,
      maintenance: true,
      productWrite: true,
      importEntry: true,
    })
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-open-import')).toBeVisible()
    await page.goto('/catalog?import=1')
    await expect(page.locator('#import-modal')).toBeVisible({ timeout: 8_000 })
    await page.locator('#import-modal-cancel').click()

    await page.goto('/overview')
    await switchRoleInline(page, 'PROVIDER')
    await expectWriteButtons(page, {
      category: false,
      maintenance: false,
      productWrite: true,
      importEntry: true,
    })
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-open-import')).toBeVisible()
    await expectRouteStructurallyBlocked(page, '/catalog/admin/categories')
    await expectRouteStructurallyBlocked(page, '/catalog/maintenance')

    await page.goto('/overview')
    await switchRoleInline(page, 'USER')
    await expectWriteButtons(page, {
      category: false,
      maintenance: false,
      productWrite: false,
      importEntry: false,
    })
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-open-import')).toHaveCount(0)
    await page.goto('/catalog?import=1')
    await expect(page.locator('#import-modal')).toHaveCount(0)
    await expectRouteStructurallyBlocked(page, '/catalog/products/new')
  })

  for (const [user, role, matrix] of [
    [
      'provider',
      'PROVIDER',
      { category: false, maintenance: false, productWrite: true, importEntry: true },
    ],
    [
      'user',
      'USER',
      { category: false, maintenance: false, productWrite: false, importEntry: false },
    ],
  ] as const) {
    test(`6x) 独立会话写入口矩阵 — ${role}`, async ({ page }) => {
      await loginAs(page, user)
      await expectCurrentRole(page, role as AppRole)
      await expectWriteButtons(page, matrix)
      if (matrix.importEntry) {
        await page.goto('/catalog')
        await expect(page.getByTestId('catalog-open-import')).toBeVisible()
      } else {
        await page.goto('/catalog')
        await expect(page.getByTestId('catalog-open-import')).toHaveCount(0)
      }
    })
  }
})
