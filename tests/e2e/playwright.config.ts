import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright for WSC V1.3 E2E (PLAN-WSC-4.1 §6.1).
 * evidenceId: TESTRUN-WSC-E2E-V13
 * 报告目录必须为 tests/e2e/reports/p0-wsc-v1.3/（禁止静默复用 v1.2-ux）。
 */
export default defineConfig({
  testDir: './specs',
  outputDir: './reports/test-results-v1.3',
  timeout: 60_000,
  expect: { timeout: 20_000 },
  reporter: [
    ['list'],
    ['html', { outputFolder: './reports/p0-wsc-v1.3', open: 'never' }],
    ['junit', { outputFile: './reports/p0-wsc-v1.3/junit.xml' }],
  ],
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://127.0.0.1:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
