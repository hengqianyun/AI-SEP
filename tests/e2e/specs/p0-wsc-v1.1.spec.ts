import { test, expect, type Page } from '@playwright/test'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import {
  expectCurrentRole,
  loginAs,
  switchRole,
  type AppRole,
} from '../fixtures/auth'

/**
 * PLAN-WSC-3.1 §3.2 / PLAN-WSC-2.2 §6.1 — UX 适配扩展
 * evidenceId: TESTRUN-WSC-E2E-V12-UX
 * 正式门禁由独立 tester 执行；本文件为规格交付（developer-wsc-206）。
 */

/** 运行时生成混行 fixture，避免固定编码 IMP-PAR-0001 被历史导入占用导致全失败。 */
function makePartialSuccessFixture(): string {
  const code = `IMP-E2E-${Date.now().toString().slice(-8)}`
  const body = [
    '产品名称,产品编码,产品类型,行业分类,数据来源,更新频率,涉及个人信息,涉及公共数据,交付方式,计费方式,价格',
    `部分成功行,${code},其他数据产品,医疗卫生 / 电子病历 / 脱敏病历,自行生产,每日,否,否,文件传输,按次,面议`,
    '缺名称产品,,其他数据产品,医疗卫生 / 电子病历 / 脱敏病历,自行生产,每日,否,否,文件传输,按次,面议',
    '',
  ].join('\n')
  const file = path.join(os.tmpdir(), `wsc-partial-success-${code}.csv`)
  fs.writeFileSync(file, body, 'utf8')
  return file
}

async function selectCascadeL3(page: Page) {
  await page.getByTestId('cat-l1').selectOption({ index: 1 })
  await expect(page.getByTestId('cat-l2')).toBeEnabled()
  await page.getByTestId('cat-l2').selectOption({ index: 1 })
  await expect(page.getByTestId('cat-l3')).toBeEnabled()
  await page.getByTestId('cat-l3').selectOption({ index: 1 })
  await expect(page.getByTestId('category-path-preview')).toBeVisible()
}

/** 写入口演示区（壳层 WriteEntryDemo）；结构不可达用 v-if，非仅 CSS。 */
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

// 各场景独立登录，避免单步失败跳过后续场景（便于 §3.2 场景完整举证）
test.describe('WSC V1.1/V1.2-UX E2E (PLAN-WSC-3.1 §3.2)', () => {
  test.skip(!process.env.E2E_RUN, 'Set E2E_RUN=1 with frontend+backend up to execute')

  // —— 场景 1：登录/角色切换/退出；OVW ——
  test('1a) 登录失败反馈 + 登录成功并切换/确认角色', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByRole('heading', { name: '接入端工作台' })).toBeVisible()
    await expect(page.getByText('演示账号')).toBeVisible()
    await expect(page.getByText('用户名', { exact: true })).toBeVisible()
    await expect(page.getByText('密码', { exact: true })).toBeVisible()

    await page.locator('input[name="username"]').fill('admin')
    await page.locator('input[name="password"]').fill('wrong-password')
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page.getByRole('alert')).toBeVisible()
    await expect(page).toHaveURL(/\/login/)

    await page.locator('input[name="password"]').fill('demo')
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\/overview/)
    await expectCurrentRole(page, 'ADMIN')

    await switchRole(page, 'PROVIDER')
    await switchRole(page, 'USER')
    await switchRole(page, 'ADMIN')

    await page.getByRole('button', { name: '退出登录' }).click()
    await expect(page).toHaveURL(/\/login/)
    await loginAs(page, 'provider')
    await expectCurrentRole(page, 'PROVIDER')
    await page.getByRole('button', { name: '退出登录' }).click()
    await loginAs(page, 'user')
    await expectCurrentRole(page, 'USER')
  })

  test('1b) 打开总览并核对核心指标可见（OVW）', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/overview')
    await expect(page.getByRole('heading', { name: '总览' })).toBeVisible()
    await expect(page.getByTestId('metrics-success')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText(/核心运营指标/)).toBeVisible()
  })

  test('1c) 进入数据目录，组合筛选/搜索', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible()
    await expect(page.getByRole('heading', { name: '数据目录' })).toBeVisible()
    await expect(page.getByTestId('catalog-filters')).toBeVisible()

    await page.getByLabel('产品类型').selectOption('DATASET')
    await page.getByTestId('catalog-search').fill('电子病历')
    await page.getByTestId('catalog-search-btn').click()

    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('catalog-row').first()).toContainText('电子病历脱敏数据集')
  })

  test('1d) 打开产品详情', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })
    await page.getByTestId('catalog-row').first().click()
    await page.getByTestId('catalog-go-detail').click()
    await expect(page).toHaveURL(/\/catalog\/products\//)
    await expect(page.getByTestId('product-detail')).toBeVisible()
    await expect(page.getByRole('heading', { name: '产品详情' })).toBeVisible()
    await expect(page.getByText('基础信息')).toBeVisible()
    await expect(page.getByTestId('category-path')).toBeVisible()
  })

  test('1e) 打开上链信息并查看快照', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/chain/products/prod-demo-chain-001')
    await expect(page.getByRole('heading', { name: '上链信息' })).toBeVisible()
    await expect(page.getByTestId('snapshot-ready')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText('存证上链')).toBeVisible()
    await expect(page.getByText('目录快照字段')).toBeVisible()
    await expect(page.getByText('演示数据产品（上链）')).toBeVisible()
  })

  test('1f) 新增产品并确认产生链上版本', async ({ page }) => {
    await loginAs(page, 'provider')
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '新增数据产品' })).toBeVisible()

    const suffix = Date.now().toString().slice(-6)
    const code = `E2E-V12-${suffix}`

    await page.locator('label:has-text("产品名称") input').fill('E2E V1.2 UX 测试产品')
    await page.locator('label:has-text("产品编码") input').fill('bad-code')
    await selectCascadeL3(page)
    // UX：页头 type=button「提交」与页脚 type=submit「提交」并存；点表单提交避免 strict mode
    const submit = page.getByTestId('product-editor').locator('button[type="submit"]')
    await submit.click()
    await expect(page.getByTestId('editor-feedback')).toBeVisible()
    await expect(page.getByTestId('editor-feedback')).toContainText(/非法|格式/)

    await page.locator('label:has-text("产品编码") input').fill(code)
    await page.locator('label:has-text("产品类型") select').selectOption('OTHER')
    await submit.click()

    await expect(page).toHaveURL(/\/catalog\/products\/prod-gen-/, { timeout: 20_000 })
    const productId = page.url().split('/').pop()!

    await page.goto(`/chain/products/${productId}`)
    await expect(page.getByRole('heading', { name: '上链信息' })).toBeVisible()
    await expect(page.getByTestId('snapshot-ready')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText(code, { exact: true })).toBeVisible()
    // UX：列表项与快照标题均有 vN；版本号在 snapshot-ready 外的 .chain-snapshot-version
    await expect(page.locator('.chain-snapshot-version')).toContainText('v1')
  })

  // —— 场景 2 / §3.2-3：三级分类维护（管理员；路由页内 modal 壳） ——
  test('2) 三级分类维护：新增子类并可见反馈', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog/admin/categories')
    await expect(page.getByTestId('category-admin')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '维护三级分类' })).toBeVisible()
    await expect(page.getByTestId('pane-l1')).toBeVisible()
    await expect(page.getByTestId('pane-l2')).toBeVisible()
    await expect(page.getByTestId('pane-l3')).toBeVisible()

    await page.getByTestId('pane-l1').getByRole('button', { name: '选择' }).first().click()
    await expect(page.getByTestId('pane-l2').locator('li').first()).toBeVisible({ timeout: 10_000 })
    await page.getByTestId('pane-l2').getByRole('button', { name: '选择' }).first().click()
    await expect(page.getByTestId('pane-l3').locator('.add-row input')).toBeEnabled()

    const name = `E2E子类-${Date.now().toString().slice(-5)}`
    await page.getByTestId('pane-l3').locator('.add-row input').fill(name)
    // UX 文案：+ 新增三级分类（原「新增子类」）
    await page
      .getByTestId('pane-l3')
      .getByRole('button', { name: /新增三级分类/ })
      .click()
    // load() 会清空短暂 success feedback；以列表出现新子类为准
    await expect
      .poll(async () => {
        return page
          .locator('[data-testid="pane-l3"] li input')
          .evaluateAll((els) => els.map((e) => (e as HTMLInputElement).value))
      }, { timeout: 15_000 })
      .toContain(name)
  })

  // —— 场景 3 / §3.2-4：目录维护 待关联→已维护 ——
  test('3) 目录维护：待关联条目关联三级后变为已维护', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog/maintenance')
    await expect(page.getByTestId('catalog-maintenance')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '目录维护' })).toBeVisible()

    await page.getByTestId('tab-pending').click()
    // UX：无条目时渲染 data-testid=empty，与 maintenance-list 互斥（适配空态 DOM）
    await expect(
      page.getByTestId('maintenance-list').or(page.getByTestId('empty')),
    ).toBeVisible({ timeout: 15_000 })
    expect(
      await page.getByTestId('empty').isVisible(),
      '待关联种子已耗尽（CatalogMaintenanceService.ensurePendingSamples 仅 @PostConstruct）。请重启 data-chain-service 后重跑本场景；此非 UX DOM 选择器缺陷。',
    ).toBe(false)

    const firstEntry = page.locator('[data-testid^="entry-"]').first()
    await expect(firstEntry).toBeVisible()
    await expect(firstEntry).toContainText('待关联')

    await firstEntry.getByTestId('edit-start').click()
    await expect(page.getByTestId('edit-row')).toBeVisible()
    await page.getByTestId('edit-l1').selectOption({ index: 1 })
    await expect(page.getByTestId('edit-l2')).toBeEnabled()
    await page.getByTestId('edit-l2').selectOption({ index: 1 })
    await expect(page.getByTestId('edit-l3')).toBeEnabled()
    await page.getByTestId('edit-l3').selectOption({ index: 1 })
    await page.getByTestId('edit-save').click()
    await expect(page.getByTestId('maintenance-feedback')).toBeVisible({ timeout: 15_000 })

    await page.getByTestId('tab-maintained').click()
    await expect(page.getByTestId('maintenance-list')).toBeVisible({ timeout: 15_000 })
    await expect(page.locator('[data-testid^="entry-"]').first()).toContainText('已维护')
  })

  // —— 场景 4 / §3.2-5：批量导入 ——
  test('4) 批量导入：partial-success 计数与错误报告可下载', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/overview')
    await expect(page.getByRole('button', { name: '批量导入' })).toBeVisible()
    await page.getByRole('button', { name: '批量导入' }).click()
    await expect(page).toHaveURL(/\/catalog\?.*import=1/)

    const dialog = page.locator('#import-modal')
    await expect(
      dialog,
      'ImportDialog 须在目录页可达（INTEGRATION.md / ?import=1）。若因 denyModify browse 未挂载，本场景不得伪造 PASS。',
    ).toBeVisible({ timeout: 8_000 })

    await expect(page.getByRole('heading', { name: '批量导入产品' })).toBeVisible()
    const fixturePartial = makePartialSuccessFixture()
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
    await expect(reportLink).toContainText(/imp-|report|ERR|错误报告/i)
    const meta = page.locator('.report-meta')
    await expect(meta).toBeVisible({ timeout: 10_000 })
    await expect(meta).toContainText(/行/)
  })

  // —— 场景 5 / §3.2-2：标签切换 + 滚动加载 ——
  test('5) 标签切换 + 滚动加载抽样；筛选保留', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible()
    await expect(page.getByTestId('catalog-space-tags')).toBeVisible()
    await expect(page.getByTestId('catalog-industry-tags')).toBeVisible()

    await page.getByLabel('产品类型').selectOption('DATASET')
    await page.getByTestId('catalog-search-btn').click()
    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })

    const spaceTag = page.getByTestId('catalog-space-tag').filter({ hasText: '医疗卫生' })
    await expect(spaceTag).toBeVisible()
    await spaceTag.click()
    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })
    await expect(page.getByLabel('产品类型')).toHaveValue('DATASET')

    const industryTag = page.getByTestId('catalog-industry-tag').first()
    if (await industryTag.isVisible()) {
      await industryTag.click()
      await expect(page.getByLabel('产品类型')).toHaveValue('DATASET')
    }

    const list = page.getByTestId('catalog-list')
    const beforeCount = await page.getByTestId('catalog-row').count()
    for (let i = 0; i < 8; i++) {
      await list.evaluate((el) => {
        el.scrollTop = el.scrollHeight
      })
      await page.waitForTimeout(400)
      const loadingMore = await page.getByTestId('catalog-load-more').isVisible().catch(() => false)
      const endText = await list.getByText('已加载全部').isVisible().catch(() => false)
      if (loadingMore || endText) break
    }
    await expect
      .poll(async () => page.getByTestId('catalog-row').count(), { timeout: 15_000 })
      .toBeGreaterThanOrEqual(beforeCount)
    await expect(page.getByLabel('产品类型')).toHaveValue('DATASET')
  })

  // —— 场景 6a：上链快照三级路径 ——
  test('6a) 上链快照三级路径 categoryPath / categoryPathParts', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/chain/products/prod-demo-chain-001')
    await expect(page.getByTestId('snapshot-ready')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('snapshot-category-path')).toBeVisible()
    await expect(page.getByTestId('snapshot-category-path')).toContainText(/金融服务/)
    await expect(page.getByTestId('snapshot-category-path')).toContainText(/征信/)
    await expect(page.getByTestId('snapshot-category-parts')).toBeVisible()
    await expect(page.getByTestId('snapshot-category-parts')).toContainText(/L1=金融服务/)
    await expect(page.getByTestId('snapshot-category-parts')).toContainText(/L2=征信评估/)
    await expect(page.getByTestId('snapshot-category-parts')).toContainText(/L3=征信评分/)
  })

  // —— 场景 6b / §3.2-6：三角色写入口可见性矩阵（ISSUE-QA-WSC3-R1-003） ——
  test('6b) 三角色写入口矩阵 ADMIN/PROVIDER/USER（结构不可达）', async ({ page }) => {
    // ADMIN：分类维护、目录维护、产品写、导入入口可见/可达
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
    await page.goto('/catalog/admin/categories')
    await expect(page.getByTestId('category-admin')).toBeVisible({ timeout: 15_000 })
    await page.goto('/catalog/maintenance')
    await expect(page.getByTestId('catalog-maintenance')).toBeVisible({ timeout: 15_000 })
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })

    // PROVIDER：产品写、导入可见；分类维护、目录维护结构不可达
    await page.goto('/overview')
    await switchRole(page, 'PROVIDER')
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
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })

    // USER：四类写入口均不可达
    await page.goto('/overview')
    await switchRole(page, 'USER')
    await expectWriteButtons(page, {
      category: false,
      maintenance: false,
      productWrite: false,
      importEntry: false,
    })
    await expect(writeDemo(page).getByText('当前角色无目录写入口')).toBeVisible()
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-open-import')).toHaveCount(0)
    await expectRouteStructurallyBlocked(page, '/catalog/admin/categories')
    await expectRouteStructurallyBlocked(page, '/catalog/maintenance')
    await expectRouteStructurallyBlocked(page, '/catalog/products/new')
  })

  // 独立登录态再验 PROVIDER / USER（防角色切换副作用；可勾选）
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
    test(`6c) 独立会话写入口矩阵 — ${role}`, async ({ page }) => {
      await loginAs(page, user)
      await expectCurrentRole(page, role as AppRole)
      await expectWriteButtons(page, matrix)
      if (!matrix.category) {
        await expectRouteStructurallyBlocked(page, '/catalog/admin/categories')
      }
      if (!matrix.maintenance) {
        await expectRouteStructurallyBlocked(page, '/catalog/maintenance')
      }
      if (!matrix.productWrite) {
        await expectRouteStructurallyBlocked(page, '/catalog/products/new')
      }
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
