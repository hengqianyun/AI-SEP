import { expect, type Page } from '@playwright/test'

export type DemoUser = 'admin' | 'provider' | 'user'

/** 演示账号登录（密码均为 demo）。 */
export async function loginAs(page: Page, username: DemoUser) {
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: '接入端工作台' })).toBeVisible()
  await page.locator('input[name="username"]').fill(username)
  await page.locator('input[name="password"]').fill('demo')
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/overview/)
  await expect(page.getByRole('heading', { name: '总览' })).toBeVisible()
}

/** 通过壳层角色切换器确认/切换角色。 */
export async function switchRole(
  page: Page,
  role: 'ADMIN' | 'PROVIDER' | 'USER',
  label: string,
) {
  const select = page.locator('.role-switch select')
  await expect(select).toBeVisible()
  await select.selectOption(role)
  await expect(select).toHaveValue(role)
  await expect(select.locator('option:checked')).toHaveText(label)
}
