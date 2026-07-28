import { test, expect } from '@playwright/test'

/**
 * Scaffold placeholder — P0 path steps land in W5 (PLAN §5.4).
 * Do not treat green as V1 release readiness.
 */
test.describe('WSC P0 scaffold', () => {
  test('placeholder app shell is reachable when frontend is running', async ({ page }) => {
    test.skip(!process.env.E2E_RUN, 'Set E2E_RUN=1 with frontend up to execute')
    await page.goto('/overview')
    await expect(page.getByRole('heading', { name: '总览' })).toBeVisible()
  })
})
