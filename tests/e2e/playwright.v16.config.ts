import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright for WSC V1.6 E2E (PLAN-WSC-8.3 §6.1 / TASK-WSC-908).
 * evidenceId: TESTRUN-WSC-E2E-V16
 * 报告目录必须为 tests/e2e/reports/p0-wsc-v1.6/
 * 禁止改 playwright.config.ts（V1.4）或新建 v15 config。
 */
export default defineConfig({
  testDir: './specs',
  testMatch: '**/p0-wsc-v1.6.spec.ts',
  outputDir: './reports/test-results-v1.6',
  timeout: 120_000,
  expect: { timeout: 20_000 },
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [
    ['list'],
    ['html', { outputFolder: './reports/p0-wsc-v1.6', open: 'never' }],
    ['junit', { outputFile: './reports/p0-wsc-v1.6/junit.xml' }],
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
