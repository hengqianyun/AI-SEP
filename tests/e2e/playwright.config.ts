import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright scaffold for WSC P0 E2E (PLAN §5.4).
 * Full P0 scenarios execute in W5 by independent tester.
 */
export default defineConfig({
  testDir: './specs',
  outputDir: './reports/test-results',
  reporter: [['list'], ['html', { outputFolder: './reports/p0-wsc', open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
