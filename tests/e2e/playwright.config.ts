import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright for WSC V1.4 E2E (PLAN-WSC-5.2 §6.1 / TASK-WSC-605).
 * evidenceId: TESTRUN-WSC-E2E-V14
 * 报告目录必须为 tests/e2e/reports/p0-wsc-v1.4/
 */
export default defineConfig({
  testDir: './specs',
  testMatch: '**/p0-wsc-v1.4.spec.ts',
  outputDir: './reports/test-results-v1.4',
  timeout: 60_000,
  expect: { timeout: 20_000 },
  reporter: [
    ['list'],
    ['html', { outputFolder: './reports/p0-wsc-v1.4', open: 'never' }],
    ['junit', { outputFile: './reports/p0-wsc-v1.4/junit.xml' }],
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
