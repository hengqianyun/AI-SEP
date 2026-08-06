import { expect, type Locator, type Page } from '@playwright/test'
import type { DemoUser } from '../fixtures/auth'

/** V1.4 壳层角色只读文案（WorkbenchLayout `session-role-label`；无切角色下拉）。 */
export const ROLE_LABEL: Record<'ADMIN' | 'PROVIDER' | 'USER', string> = {
  ADMIN: '管理员',
  PROVIDER: '数据提供方',
  USER: '普通用户',
}

export const USER_TO_ROLE: Record<DemoUser, 'ADMIN' | 'PROVIDER' | 'USER'> = {
  admin: 'ADMIN',
  provider: 'PROVIDER',
  user: 'USER',
}

/** 断言侧栏只读角色展示；禁止存在可切换控件。 */
export async function expectReadonlyRole(page: Page, role: 'ADMIN' | 'PROVIDER' | 'USER') {
  const label = page.getByTestId('session-role-label')
  await expect(label).toBeVisible({ timeout: 15_000 })
  await expect(label).toContainText(ROLE_LABEL[role])
  // 无自由切角色：无旧版 select / listbox / dropdown trigger
  await expect(page.locator('.role-switch select')).toHaveCount(0)
  await expect(page.locator('button.role-dropdown-trigger')).toHaveCount(0)
  await expect(page.getByRole('listbox')).toHaveCount(0)
}

/**
 * 用户管理：打开「创建账号」弹框并填写必填字段（606 弹框化）。
 * 调用方负责再点 `user-create` 提交。
 */
export async function openUserCreateDialog(
  page: Page,
  opts: { username: string; password: string; displayName: string; role: 'ADMIN' | 'PROVIDER' | 'USER' },
) {
  await page.getByTestId('user-open-create').click()
  const dialog = page.getByTestId('user-create-dialog')
  await expect(dialog).toBeVisible({ timeout: 8_000 })
  await dialog.getByTestId('user-username').fill(opts.username)
  await dialog.getByTestId('user-password').fill(opts.password)
  await dialog.getByTestId('user-display-name').fill(opts.displayName)
  await dialog.getByTestId('user-role').selectOption(opts.role)
  return dialog
}

/**
 * 用户管理：经「编辑角色」按钮打开弹框，选择角色并保存（606）。
 * 断言落在行内 `user-row-role-label` 文案（select 仅存在于弹框内）。
 */
export async function editUserRoleViaDialog(
  page: Page,
  row: Locator,
  role: 'ADMIN' | 'PROVIDER' | 'USER',
  roleLabel: string,
) {
  await row.getByTestId('user-edit-role').click()
  const dialog = page.getByTestId('user-edit-role-dialog')
  await expect(dialog).toBeVisible({ timeout: 8_000 })
  await dialog.getByTestId('user-row-role').selectOption(role)
  await dialog.getByTestId('user-edit-role-save').click()
  await expect(dialog).toHaveCount(0, { timeout: 10_000 })
  await expect(row.getByTestId('user-row-role-label')).toHaveText(roleLabel, { timeout: 10_000 })
}

/** 公共目录不得出现增改导入口（三角色）。 */
export async function expectPublicCatalogNoWrite(page: Page) {
  await page.goto('/catalog')
  await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 15_000 })
  await expect(page.getByRole('heading', { name: '数据目录' })).toBeVisible()
  await expect(page.getByTestId('catalog-open-create')).toHaveCount(0)
  await expect(page.getByTestId('catalog-open-import')).toHaveCount(0)
  await page.goto('/catalog?import=1')
  await expect(page.locator('#import-modal')).toHaveCount(0)
}

/** 打开「我的产品」导入弹窗（宿主=/my-products）。 */
export async function openMineImportDialog(page: Page) {
  await page.goto('/my-products')
  await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 15_000 })
  await expect(page.getByRole('heading', { name: '我的数据产品' })).toBeVisible()
  await page.getByTestId('catalog-open-import').click()
  const dialog = page.locator('#import-modal')
  await expect(dialog).toBeVisible({ timeout: 8_000 })
  await expect(page.getByRole('heading', { name: '批量导入产品' })).toBeVisible()
  return dialog
}

export async function selectIndustryCategory(
  page: Page,
  label = '卫生和社会工作',
) {
  const sel = page.getByTestId('industry-category-select')
  await expect(sel).toBeVisible({ timeout: 15_000 })
  await sel.selectOption({ label })
  await expect(sel).toHaveValue(label)
}

/** 产品编码三段 `{DOMAIN}-{FEATURE}-{NNNN}`（仅用于 ADMIN 写 API 负例等非 UI 编码场景）。 */
export function makeProductCode(feature: string): string {
  const nnnn = Date.now().toString().slice(-6)
  return `E2E-${feature}-${nnnn}`
}

/**
 * 创建态：名称可填；产品编码只读自动生成（选类型后刷新）。
 * 返回当前只读编码，供列表检索断言。
 */
export async function fillCreateProductBasics(
  page: Page,
  opts: { name: string; productType: 'API' | 'DATASET' | 'REPORT' | 'OTHER' },
): Promise<string> {
  await page.locator('label:has-text("产品名称") input').fill(opts.name)
  await page.getByTestId('product-type').selectOption(opts.productType)
  const codeInput = page.locator('label:has-text("产品编码") input')
  await expect(codeInput).toHaveAttribute('readonly', '')
  await expect
    .poll(async () => codeInput.inputValue(), { timeout: 10_000 })
    .toMatch(/^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$/)
  return codeInput.inputValue()
}

/**
 * 座序图根节点：CatalogBrowsePage 将 `catalog-seat-map-slot` fallthrough 到
 * CirculationSeatMap 单根，覆盖组件内 `circulation-seat-map`（Vue 属性合并）。
 * 证据仍可用 slot + 子 testid（total/legend/chip）。
 */
export function seatMapRoot(page: Page) {
  return page.getByTestId('catalog-seat-map-slot')
}
