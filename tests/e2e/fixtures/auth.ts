import { expect, type Page } from '@playwright/test'

export type DemoUser = 'admin' | 'provider' | 'user'

/** 若已登录则退出，保证后续真登录不被会话守卫重定向。 */
export async function logoutIfNeeded(page: Page) {
  const logoutBtn = page.getByRole('button', { name: '退出登录' })
  if (await logoutBtn.isVisible().catch(() => false)) {
    await logoutBtn.click()
    await expect(page).toHaveURL(/\/login/, { timeout: 15_000 })
  }
}

/** 演示账号登录（密码均为 demo）。已有会话时先退出。 */
export async function loginAs(page: Page, username: DemoUser) {
  await page.goto('/login')
  // 已登录时守卫会离开 /login；侧栏也有「接入端工作台」标题，须以 login-form 判定
  const loginForm = page.getByTestId('login-form')
  if ((await loginForm.count()) === 0) {
    await logoutIfNeeded(page)
    await page.goto('/login')
  }
  await expect(loginForm).toBeVisible({ timeout: 15_000 })
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
