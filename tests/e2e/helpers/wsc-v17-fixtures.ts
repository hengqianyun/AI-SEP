import { expect, type Page, type Request } from '@playwright/test'

/**
 * V1.7 E2E fixtures — 订单模块辅助函数。
 * TASK-WSC-919 / PLAN-WSC-9.2 §6.1。
 */

// ── 路由常量 ──

/** 订单列表路由（三角色共用） */
export const ROUTE_ORDERS = '/orders'
/** 订购入口路由 */
export const ROUTE_ORDER_SUBSCRIBE = '/orders/subscribe'

// ── API 路径匹配 ──

function pathnameOf(url: string): string {
  try {
    return new URL(url).pathname
  } catch {
    return url
  }
}

export function isOrdersListRequest(req: Request): boolean {
  if (req.method() !== 'GET') return false
  return /^\/api\/v1\/orders\/?$/.test(pathnameOf(req.url()))
}

export function isOrderDetailRequest(req: Request): boolean {
  if (req.method() !== 'GET') return false
  return /^\/api\/v1\/orders\/[^/]+$/.test(pathnameOf(req.url()))
}

export function isNoticeRequest(req: Request): boolean {
  if (req.method() !== 'GET') return false
  return /\/orders\/notices\/current\/?$/.test(pathnameOf(req.url()))
}

// ── 通用工具 ──

export function uniqueTag(): string {
  return Date.now().toString().slice(-8)
}

// ── 导航 ──

/** 展开侧栏数据目录子菜单（若收起）。 */
export async function ensureCatalogNavOpen(page: Page) {
  const toggle = page.getByTestId('nav-catalog-group-toggle')
  if ((await toggle.count()) === 0) return
  if ((await toggle.getAttribute('aria-expanded')) === 'false') {
    await toggle.click()
  }
}

/** 导航到订单列表并等待加载完成。 */
export async function gotoOrderList(page: Page) {
  await page.goto(ROUTE_ORDERS)
  await expect(page.getByTestId('order-list')).toBeVisible({ timeout: 20_000 })
}

/** 等待订单列表加载完成（loading 消失、table 或 empty 出现）。 */
export async function waitOrderListReady(page: Page) {
  await expect(page.getByTestId('list-loading')).toHaveCount(0, { timeout: 20_000 })
  await expect
    .poll(async () => {
      const table = await page.getByTestId('order-table').count()
      const empty = await page.getByTestId('list-empty').count()
      return table + empty
    }, { timeout: 20_000 })
    .toBeGreaterThan(0)
}

// ── 订购流程 ──

/** 从产品详情进入订购页（USER 角色需已登录且产品可见）。 */
export async function gotoSubscribePage(page: Page, productId: string) {
  await page.goto(`${ROUTE_ORDER_SUBSCRIBE}/${productId}`)
  await expect(page.getByTestId('order-subscribe')).toBeVisible({ timeout: 20_000 })
}

/** 勾选须知、填写备注、提交订购。返回成功后跳转的订单号（从 URL 或列表提取）。 */
export async function submitOrder(
  page: Page,
  remark = 'E2E 自动测试备注',
): Promise<string> {
  // 等待产品信息加载
  await expect(page.getByTestId('subscribe-product-info')).toBeVisible({ timeout: 15_000 })

  // 填写备注
  await page.getByTestId('subscribe-remark').fill(remark)

  // 勾选须知
  const checkbox = page.getByTestId('notice-checkbox')
  if (!(await checkbox.isChecked())) {
    await checkbox.check()
  }

  // 提交按钮应可用
  const submitBtn = page.getByTestId('subscribe-submit')
  await expect(submitBtn).toBeEnabled()

  // 监听创建成功请求
  const createReq = page.waitForResponse(
    (res) => res.url().includes('/api/v1/orders') && res.request().method() === 'POST',
    { timeout: 30_000 },
  )
  await submitBtn.click()
  const res = await createReq
  expect(res.status()).toBeLessThan(400)

  // 等待成功 toast
  await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10_000 })

  // 从响应体提取 orderId
  const body = await res.json()
  const orderId = body?.data?.orderId ?? body?.data?.id ?? ''
  return String(orderId)
}

// ── 详情页操作 ──

/** 导航到订单详情。 */
export async function gotoOrderDetail(page: Page, orderId: string) {
  await page.goto(`${ROUTE_ORDERS}/${orderId}`)
  await expect(page.getByTestId('order-detail')).toBeVisible({ timeout: 20_000 })
  await expect(page.getByTestId('detail-loading')).toHaveCount(0, { timeout: 20_000 })
}

/** 确认订单（PROVIDER/ADMIN 角色）。等待成功 toast。 */
export async function confirmOrder(page: Page) {
  const btn = page.getByTestId('action-confirm-order')
  await expect(btn).toBeVisible()
  await btn.click()
  await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 15_000 })
}

/** 提交合约（USER 角色）：上传文件 + 填写交易信息 + 提交。 */
export async function submitContract(
  page: Page,
  opts?: { amount?: string },
) {
  // 点击提交合约按钮进入上传页
  await page.getByTestId('action-upload-contract').click()
  await expect(page.getByTestId('contract-upload-page')).toBeVisible({ timeout: 15_000 })

  // 创建测试文件并上传
  const fileInput = page.getByTestId('upload-file-input')
  await fileInput.setInputFiles({
    name: 'e2e-test-contract.pdf',
    mimeType: 'application/pdf',
    buffer: Buffer.from('PDF test content for E2E'),
  })

  // 等待文件信息展示
  await expect(page.getByTestId('upload-file-info')).toBeVisible({ timeout: 10_000 })
  await expect(page.getByTestId('upload-file-name')).toContainText('e2e-test-contract.pdf')

  // 填写交易信息
  const amount = opts?.amount ?? '1000.00'
  await page.getByTestId('upload-amount-inclusive').fill(amount)
  await page.getByTestId('upload-amount-exclusive').fill(amount)

  // 填写订单明细
  const unitInput = page.locator('[data-testid="line-unit-0"]')
  if ((await unitInput.count()) > 0) {
    await unitInput.fill('次')
    await page.locator('[data-testid="line-qty-0"]').fill('1')
    await page.locator('[data-testid="line-price-0"]').fill(amount)
  }

  // 提交
  const submitBtn = page.getByTestId('upload-submit')
  await expect(submitBtn).toBeEnabled()
  await submitBtn.click()

  // 等待成功 toast
  await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 20_000 })
}

/** 确认合约（PROVIDER/ADMIN 角色）：须知勾选 + 确认。 */
export async function confirmContract(page: Page) {
  // 点击确认合约按钮进入确认页
  await page.getByTestId('action-confirm-contract').click()
  await expect(page.getByTestId('contract-confirm-page')).toBeVisible({ timeout: 15_000 })

  // 勾选须知
  const checkbox = page.getByTestId('confirm-checkbox')
  if (!(await checkbox.isChecked())) {
    await checkbox.check()
  }

  // 确认按钮应可用
  const submitBtn = page.getByTestId('confirm-submit')
  await expect(submitBtn).toBeEnabled()
  await submitBtn.click()

  // 等待成功 toast
  await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 15_000 })
}

/** 取消订单（二次确认）。 */
export async function cancelOrder(page: Page, confirmDialog = true) {
  const cancelBtn = page.getByTestId('action-cancel')
  await expect(cancelBtn).toBeVisible()
  await cancelBtn.click()

  // 二次确认对话框
  await expect(page.getByTestId('cancel-dialog')).toBeVisible({ timeout: 10_000 })
  await expect(page.getByTestId('cancel-dialog-body')).toContainText('此操作不可回退')

  if (confirmDialog) {
    await page.getByTestId('cancel-dialog-confirm-btn').click()
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 15_000 })
  } else {
    // 取消对话框，不确认
    await page.getByTestId('cancel-dialog-cancel-btn').click()
    await expect(page.getByTestId('cancel-dialog')).toHaveCount(0, { timeout: 5_000 })
  }
}

/** 等待订单详情状态变为指定文本。 */
export async function waitForDetailStatus(page: Page, statusText: string) {
  await expect(page.getByTestId('det-status')).toContainText(statusText, { timeout: 20_000 })
}

/** 获取时间线条数。 */
export async function getTimelineCount(page: Page): Promise<number> {
  const items = page.locator('[data-testid^="timeline-"]')
  return items.count()
}

/** 获取上链次数（详情页）。 */
export async function getChainCount(page: Page): Promise<number> {
  const text = await page.getByTestId('det-chain-count').innerText()
  return Number(text) || 0
}

/** 获取列表中指定订单行的状态文本。 */
export async function getOrderRowStatus(page: Page, orderId: string): Promise<string> {
  const row = page.getByTestId(`row-${orderId}`)
  return row.getByTestId('cell-status').innerText()
}

/**
 * 获取列表 API 响应中指定订单的 chainCount。
 * 列表 UI 无 chainCount 列，但 API 响应 JSON 包含该字段（OrderListItem.chainCount）。
 * 拦截 GET /api/v1/orders 响应，解析 list 数组中匹配 orderId 的 chainCount。
 */
export async function getListChainCount(page: Page, orderId: string): Promise<number> {
  // 注册一次性响应拦截器，捕获列表接口返回的 chainCount
  const captured = page.waitForResponse(
    (res) => isOrdersListRequest(res) && res.status() < 400,
    { timeout: 20_000 },
  )

  // 触发列表刷新（导航或翻页均可；此处直接 reload 当前列表页）
  await page.reload()
  const res = await captured
  const body = await res.json()
  const list: Array<{ orderId: string; chainCount?: number }> = body?.data?.list ?? body?.list ?? []
  const match = list.find((item) => item.orderId === orderId)
  return match?.chainCount ?? 0
}

/** 获取订单列表当前页行数。 */
export async function getOrderRowCount(page: Page): Promise<number> {
  const rows = page.locator('[data-testid^="row-"]')
  return rows.count()
}

/** 提取当前列表所有 orderId。 */
export async function extractOrderIds(page: Page): Promise<string[]> {
  const rows = page.locator('[data-testid^="row-"]')
  const count = await rows.count()
  const ids: string[] = []
  for (let i = 0; i < count; i++) {
    const testId = await rows.nth(i).getAttribute('data-testid')
    if (testId) {
      const id = testId.replace('row-', '')
      if (id) ids.push(id)
    }
  }
  return ids
}

// ── 通过 API 直接创建订单（辅助，避免 UI 路径依赖） ──

export async function createOrderViaApi(
  page: Page,
  productId: string,
  remark = 'E2E API 创建',
): Promise<string> {
  const res = await page.evaluate(async (args) => {
    const resp = await fetch('/api/v1/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        productId: args.productId,
        remark: args.remark,
        noticeAccepted: true,
      }),
    })
    return { status: resp.status, body: await resp.json() }
  }, { productId, remark })

  expect(res.status).toBeLessThan(400)
  return String(res.body?.data?.orderId ?? res.body?.data?.id ?? '')
}

/** 通过 API 确认订单。 */
export async function confirmOrderViaApi(page: Page, orderId: string) {
  const res = await page.evaluate(async (id) => {
    const resp = await fetch(`/api/v1/orders/${id}/confirm`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: '{}',
    })
    return resp.status
  }, orderId)
  expect(res).toBeLessThan(400)
}

/** 通过 API 取消订单。 */
export async function cancelOrderViaApi(page: Page, orderId: string) {
  const res = await page.evaluate(async (id) => {
    const resp = await fetch(`/api/v1/orders/${id}/cancel`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: '{}',
    })
    return resp.status
  }, orderId)
  expect(res).toBeLessThan(400)
}
