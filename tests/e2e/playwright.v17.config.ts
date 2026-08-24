import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright for WSC V1.7 E2E (PLAN-WSC-9.2 §6.1 / TASK-WSC-919).
 * evidenceId: TESTRUN-WSC-E2E-V17
 * 报告目录必须为 tests/e2e/reports/p0-wsc-v1.7/
 * 禁止改 playwright.config.ts（V1.4）或 playwright.v16.config.ts（V1.6）。
 */
export default defineConfig({
  testDir: './specs',
  testMatch: '**/p0-wsc-v1.7.spec.ts',
  outputDir: './reports/test-results-v1.7',
  timeout: 120_000,
  expect: { timeout: 20_000 },
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [
    ['list'],
    ['html', { outputFolder: './reports/p0-wsc-v1.7', open: 'never' }],
    ['junit', { outputFile: './reports/p0-wsc-v1.7/junit.xml' }],
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
