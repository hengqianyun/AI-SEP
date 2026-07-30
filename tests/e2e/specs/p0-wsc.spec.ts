import { test, expect, type Page } from '@playwright/test'

/**
 * PLAN-WSC-1.1 §5.4 P0 Playwright E2E
 * actorInstance: tester-wsc-e2e-p0
 */

const DEMO_PASSWORD = 'demo'

async function loginAs(page: Page, username: string) {
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: '接入端工作台' })).toBeVisible()
  await page.locator('input[name="username"]').fill(username)
  await page.locator('input[name="password"]').fill(DEMO_PASSWORD)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/overview/)
}

async function switchRole(page: Page, roleValue: 'ADMIN' | 'PROVIDER' | 'USER') {
  const select = page.locator('.role-switch select')
  await expect(select).toBeEnabled({ timeout: 15_000 })
  await select.selectOption(roleValue)
  // 切换期间 select 会 disabled；结束后须落到目标角色
  await expect(select).toBeEnabled({ timeout: 15_000 })
  await expect(select).toHaveValue(roleValue, { timeout: 15_000 })
}

test.describe.configure({ mode: 'serial' })

test.describe('WSC P0 E2E (§5.4)', () => {
  test.skip(!process.env.E2E_RUN, 'Set E2E_RUN=1 with frontend+backend up to execute')

  test('1) 登录失败反馈 + 登录成功并切换/确认角色', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByRole('heading', { name: '接入端工作台' })).toBeVisible()
    // NFR：界面中文
    await expect(page.getByText('演示账号')).toBeVisible()
    await expect(page.getByText('用户名', { exact: true })).toBeVisible()
    await expect(page.getByText('密码', { exact: true })).toBeVisible()

    // NFR：至少一次失败反馈可观测（错误密码）
    await page.locator('input[name="username"]').fill('admin')
    await page.locator('input[name="password"]').fill('wrong-password')
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page.getByRole('alert')).toBeVisible()
    await expect(page).toHaveURL(/\/login/)

    // 演示账号 admin 登录成功
    await page.locator('input[name="password"]').fill(DEMO_PASSWORD)
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\/overview/)
    await expect(page.locator('.role-switch select')).toHaveValue('ADMIN')

    // 切换角色并确认
    await switchRole(page, 'PROVIDER')
    await switchRole(page, 'USER')
    await switchRole(page, 'ADMIN')

    // 快速冒烟：provider / user 亦可登录
    await page.getByRole('button', { name: '退出登录' }).click()
    await expect(page).toHaveURL(/\/login/)
    await loginAs(page, 'provider')
    await expect(page.locator('.role-switch select')).toHaveValue('PROVIDER')
    await page.getByRole('button', { name: '退出登录' }).click()
    await loginAs(page, 'user')
    await expect(page.locator('.role-switch select')).toHaveValue('USER')
  })

  test('2) 打开总览并核对核心指标可见', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/overview')
    await expect(page.getByRole('heading', { name: '总览' })).toBeVisible()
    await expect(page.getByTestId('metrics-success')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText('核心运营指标')).toBeVisible()
  })

  test('3) 进入数据目录，组合筛选/搜索', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible()
    await expect(page.getByRole('heading', { name: '数据目录' })).toBeVisible()
    await expect(page.getByTestId('catalog-filters')).toBeVisible()

    // 组合筛选：产品类型 + 搜索
    await page.getByLabel('产品类型').selectOption('DATASET')
    await page.getByTestId('catalog-search').fill('电子病历')
    await page.getByTestId('catalog-search-btn').click()

    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('catalog-row').first()).toContainText('电子病历脱敏数据集')
  })

  test('4) 打开产品详情', async ({ page }) => {
    await loginAs(page, 'admin')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-row').first()).toBeVisible({ timeout: 15_000 })
    await page.getByTestId('catalog-row').first().click()
    await page.getByTestId('catalog-go-detail').click()
    await expect(page).toHaveURL(/\/catalog\/products\//)
    await expect(page.getByTestId('product-detail')).toBeVisible()
    await expect(page.getByRole('heading', { name: '产品详情' })).toBeVisible()
    await expect(page.getByText('基础信息')).toBeVisible()
  })

  test('5) 打开上链信息并查看快照', async ({ page }) => {
    await loginAs(page, 'admin')
    // 使用链上种子产品（InMemoryChainStore.SEED_PRODUCT_ID）核对快照可读
    await page.goto('/chain/products/prod-demo-chain-001')
    await expect(page.getByRole('heading', { name: '上链信息' })).toBeVisible()
    await expect(page.getByTestId('snapshot-ready')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText('存证上链')).toBeVisible()
    await expect(page.getByText('目录快照字段')).toBeVisible()
    await expect(page.getByText('演示数据产品（上链）')).toBeVisible()
  })

  test('6) 新增产品并确认产生链上版本（含成功反馈）', async ({ page }) => {
    await loginAs(page, 'provider')
    await page.goto('/catalog/products/new')
    await expect(page.getByTestId('product-editor')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('heading', { name: '新增数据产品' })).toBeVisible()

    const suffix = Date.now().toString().slice(-6)
    const code = `E2E-P0-${suffix}`

    // NFR：失败反馈（非法编码）
    await page.locator('label:has-text("产品名称") input').fill('E2E P0 测试产品')
    await page.locator('label:has-text("产品编码") input').fill('bad-code')
    await page.locator('label:has-text("二级行业分类") select').selectOption({ index: 1 })
    await page.getByRole('button', { name: '提交' }).click()
    await expect(page.getByTestId('editor-feedback')).toBeVisible()
    await expect(page.getByTestId('editor-feedback')).toContainText(/非法|格式/)

    // 成功创建
    await page.locator('label:has-text("产品编码") input').fill(code)
    await page.locator('label:has-text("产品类型") select').selectOption('OTHER')
    await page.getByRole('button', { name: '提交' }).click()

    await expect(page).toHaveURL(/\/catalog\/products\/prod-gen-/, { timeout: 20_000 })
    const productUrl = page.url()
    const productId = productUrl.split('/').pop()!

    await page.goto(`/chain/products/${productId}`)
    await expect(page.getByRole('heading', { name: '上链信息' })).toBeVisible()
    await expect(page.getByTestId('snapshot-ready')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByText(code, { exact: true })).toBeVisible()
    await expect(page.getByText('v1', { exact: true })).toBeVisible()
  })
})
