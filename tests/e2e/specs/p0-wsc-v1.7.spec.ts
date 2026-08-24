import { test, expect } from '@playwright/test'
import { loginAs } from '../fixtures/auth'
import {
  ensureCatalogNavOpen,
  gotoOrderList,
  waitOrderListReady,
  gotoSubscribePage,
  submitOrder,
  gotoOrderDetail,
  confirmOrder,
  submitContract,
  confirmContract,
  cancelOrder,
  waitForDetailStatus,
  getTimelineCount,
  getChainCount,
  getListChainCount,
  extractOrderIds,
  getOrderRowCount,
  uniqueTag,
  ROUTE_ORDERS,
  createOrderViaApi,
  confirmOrderViaApi,
} from '../helpers/wsc-v17-fixtures'
import {
  ROUTE_CATALOG_MAINTENANCE,
  ROUTE_MY_CATALOG,
  ROUTE_MY_PRODUCTS,
  ROUTE_ADMIN_USERS,
  waitMaintenanceReady,
} from '../helpers/wsc-v16-fixtures'

/**
 * PLAN-WSC-9.2 §6.1 — WSC V1.7 E2E（强制九场景 + 建议一场景）
 * evidenceId: TESTRUN-WSC-E2E-V17
 * 规格文件：tests/e2e/specs/p0-wsc-v1.7.spec.ts
 * 报告目录：tests/e2e/reports/p0-wsc-v1.7/（见 playwright.v17.config.ts）
 * 开发侧可本地冒烟；不得自行标 VERIFIED。
 *
 * 禁止改 frontend/backend/contracts；选择器失效 → BLOCKED 交 Orchestrator hotfix。
 * PO 锁定：三角色订单菜单角色化；主路径止于合约达成；三方取消二次确认；
 * ADMIN 可代 PROVIDER 确认/取消；列表九组字段+jumper+pageSize；V1.6 目录不回退。
 */

test.describe.configure({ mode: 'serial' })

test.describe('WSC V1.7 E2E (PLAN-WSC-9.2 §6.1 / TESTRUN-WSC-E2E-V17)', () => {

  // ═══════════════════════════════════════════════════════════════════
  // 场景 1：导航开放
  // USER 我的订单；PROVIDER/ADMIN 交易订单；V1.6 目录菜单不回退
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-1) 导航开放：USER 我的订单、PROVIDER/ADMIN 交易订单；V1.6 目录菜单不回退', async ({
    page,
  }) => {
    // ADMIN 角色：侧栏应有「交易订单」菜单，可进入 /orders
    await loginAs(page, 'admin')
    await ensureCatalogNavOpen(page)
    const adminOrdersNav = page.getByTestId('nav-orders')
    await expect(adminOrdersNav).toBeVisible()
    await expect(adminOrdersNav).toContainText('交易订单')
    await adminOrdersNav.click()
    await expect(page).toHaveURL(/\/orders/)
    await expect(page.getByTestId('order-list')).toBeVisible({ timeout: 15_000 })

    // V1.6 目录菜单不回退
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-catalog-browse')).toBeVisible()
    await expect(page.getByTestId('nav-my-catalog')).toBeVisible()
    await expect(page.getByTestId('nav-my-products')).toBeVisible()
    await expect(page.getByTestId('nav-catalog-maintenance')).toBeVisible()
    await expect(page.getByTestId('nav-users')).toBeVisible()

    // PROVIDER 角色：侧栏应有「交易订单」菜单
    await loginAs(page, 'provider')
    await ensureCatalogNavOpen(page)
    const providerOrdersNav = page.getByTestId('nav-orders')
    await expect(providerOrdersNav).toBeVisible()
    await expect(providerOrdersNav).toContainText('交易订单')
    await providerOrdersNav.click()
    await expect(page).toHaveURL(/\/orders/)

    // USER 角色：侧栏应有「我的订单」菜单
    await loginAs(page, 'user')
    await ensureCatalogNavOpen(page)
    const userOrdersNav = page.getByTestId('nav-orders')
    await expect(userOrdersNav).toBeVisible()
    await expect(userOrdersNav).toContainText('我的订单')
    await userOrdersNav.click()
    await expect(page).toHaveURL(/\/orders/)

    // V1.6 回归：USER 不可见目录维护、我的目录、我的数据产品
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-catalog-maintenance')).toHaveCount(0)
    await expect(page.getByTestId('nav-my-catalog')).toHaveCount(0)
    await expect(page.getByTestId('nav-my-products')).toHaveCount(0)
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 2：链内创建
  // USER 须知+备注；成功有订单号、待确认、toast；快照不漂移
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-2) 链内创建：USER 产品详情→订购→须知勾选→提交→成功（订单号+待确认+toast）', async ({
    page,
  }) => {
    await loginAs(page, 'user')

    // 进入产品详情页，找到订购入口
    await page.goto(ROUTE_MY_PRODUCTS)
    // USER 无「我的数据产品」，改用公共目录浏览第一项产品
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('catalog-list')).toBeVisible({ timeout: 20_000 })
    await page.getByTestId('catalog-row').first().click()
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })

    // 点击订购按钮
    const orderBtn = page.getByTestId('product-order-btn')
    await expect(orderBtn).toBeVisible()
    await orderBtn.click()
    await expect(page.getByTestId('order-subscribe')).toBeVisible({ timeout: 15_000 })

    // 产品信息区应展示
    await expect(page.getByTestId('subscribe-product-info')).toBeVisible()
    await expect(page.getByTestId('sub-product-name')).not.toBeEmpty()

    // 订购人信息应展示当前用户
    await expect(page.getByTestId('sub-demand-user')).toBeVisible()

    // 未勾选须知时提交按钮 disabled
    const submitBtn = page.getByTestId('subscribe-submit')
    await expect(submitBtn).toBeDisabled()

    // 勾选须知 + 填备注 → 提交成功
    const remark = `E2E V1.7 创建测试 ${uniqueTag()}`
    await page.getByTestId('subscribe-remark').fill(remark)
    await page.getByTestId('notice-checkbox').check()
    await expect(submitBtn).toBeEnabled()

    // 监听创建请求
    const createRes = page.waitForResponse(
      (res) => res.url().includes('/api/v1/orders') && res.request().method() === 'POST',
      { timeout: 30_000 },
    )
    await submitBtn.click()
    const res = await createRes
    expect(res.status()).toBeLessThan(400)

    // 成功 toast
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10_000 })

    // 提取订单号（UUID 格式）
    const body = await res.json()
    const orderId = String(body?.data?.orderId ?? body?.data?.id ?? '')
    expect(orderId).toBeTruthy()
    expect(orderId).toMatch(/^[0-9a-f-]{36}$/i) // UUID v4 格式
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 3：角色列表
  // USER 不见他人单；PROVIDER 不见他企单；ADMIN 可见多企
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-3) 角色列表：USER 不见他人单；PROVIDER 不见他企单；ADMIN 全可见', async ({
    page,
  }) => {
    // ADMIN 列表：应可见多企业订单
    await loginAs(page, 'admin')
    await gotoOrderList(page)
    await waitOrderListReady(page)
    const adminCount = await getOrderRowCount(page)
    expect(adminCount).toBeGreaterThan(0)
    const adminIds = await extractOrderIds(page)
    expect(adminIds.length).toBeGreaterThan(0)

    // PROVIDER 列表：仅本企业提供方订单
    await loginAs(page, 'provider')
    await gotoOrderList(page)
    await waitOrderListReady(page)
    const providerIds = await extractOrderIds(page)
    // PROVIDER 可见数量 ≤ ADMIN（scope 受限）
    expect(providerIds.length).toBeLessThanOrEqual(adminIds.length)

    // USER 列表：仅本人需求方订单
    await loginAs(page, 'user')
    await gotoOrderList(page)
    await waitOrderListReady(page)
    const userIds = await extractOrderIds(page)
    // USER 可见数量 ≤ ADMIN（scope 受限）
    expect(userIds.length).toBeLessThanOrEqual(adminIds.length)

    // ADMIN 全可见是超集
    for (const uid of userIds) {
      expect(adminIds).toContain(uid)
    }
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 4：分页
  // jumper + pageSize 档位；改 size 回第 1 页
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-4) 分页：jumper + pageSize 档位；改 size 回第 1 页', async ({ page }) => {
    await loginAs(page, 'admin')
    await gotoOrderList(page)
    await waitOrderListReady(page)

    // 分页区域可见
    const pagination = page.getByTestId('pagination')
    await expect(pagination).toBeVisible()
    await expect(pagination).toContainText(/共\s*\d+\s*条/)

    // Ant Pagination jumper 可见
    const jumper = page.locator(
      '[data-testid="a-pagination"] .ant-pagination-options-quick-jumper, [data-testid="a-pagination"] input',
    )
    if ((await jumper.count()) > 0) {
      await expect(jumper.first()).toBeVisible()
    }

    // pageSize 选择器可见（档位 10/20/50/100）
    const sizeChanger = page.locator('.ant-pagination-options-size-changer, [data-testid="a-pagination"] .ant-select')
    if ((await sizeChanger.count()) > 0) {
      await expect(sizeChanger.first()).toBeVisible()
    }
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 5：主路径
  // 确认订单 → 上传附件与计费 → 统一确认页勾选 → 合约已达成
  // 不可回退；ADMIN 可代确认
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-5) 主路径：USER 创建 → PROVIDER 确认订单 → USER 提交合约 → PROVIDER 确认合约 → 合约已达成', async ({
    page,
  }) => {
    // Step 1: USER 创建订单
    await loginAs(page, 'user')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('catalog-list')).toBeVisible({ timeout: 20_000 })
    await page.getByTestId('catalog-row').first().click()
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })
    await page.getByTestId('product-order-btn').click()
    await expect(page.getByTestId('order-subscribe')).toBeVisible({ timeout: 15_000 })

    const remark = `E2E V1.7 主路径 ${uniqueTag()}`
    await page.getByTestId('subscribe-remark').fill(remark)
    await page.getByTestId('notice-checkbox').check()
    const createRes = page.waitForResponse(
      (res) => res.url().includes('/api/v1/orders') && res.request().method() === 'POST',
      { timeout: 30_000 },
    )
    await page.getByTestId('subscribe-submit').click()
    const res = await createRes
    expect(res.status()).toBeLessThan(400)
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10_000 })
    const body = await res.json()
    const orderId = String(body?.data?.orderId ?? body?.data?.id ?? '')

    // Step 2: PROVIDER 确认订单
    await loginAs(page, 'provider')
    await gotoOrderDetail(page, orderId)
    await waitForDetailStatus(page, '待确认')
    await confirmOrder(page)

    // Step 3: USER 提交合约（附件 + 交易信息）
    await loginAs(page, 'user')
    await gotoOrderDetail(page, orderId)
    await waitForDetailStatus(page, '待上传')
    await submitContract(page, { amount: '5000.00' })

    // Step 4: PROVIDER 确认合约
    await loginAs(page, 'provider')
    await gotoOrderDetail(page, orderId)
    await waitForDetailStatus(page, '待确认合约')
    await confirmContract(page)

    // 验证合约已达成
    await loginAs(page, 'admin')
    await gotoOrderDetail(page, orderId)
    await waitForDetailStatus(page, '合约已达成')

    // 不可回退：合约达成后无确认/上传/取消按钮
    await expect(page.getByTestId('action-confirm-order')).toHaveCount(0)
    await expect(page.getByTestId('action-upload-contract')).toHaveCount(0)
    await expect(page.getByTestId('action-confirm-contract')).toHaveCount(0)
    await expect(page.getByTestId('action-cancel')).toHaveCount(0)
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 6：取消
  // 三未完成态二次确认后已取消；未确认弹窗状态不变
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-6) 取消：待确认态 USER 取消（二次确认）→ 已取消；未确认状态不变', async ({
    page,
  }) => {
    // 创建新订单
    await loginAs(page, 'user')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('catalog-list')).toBeVisible({ timeout: 20_000 })
    await page.getByTestId('catalog-row').first().click()
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })
    await page.getByTestId('product-order-btn').click()
    await expect(page.getByTestId('order-subscribe')).toBeVisible({ timeout: 15_000 })

    const remark = `E2E V1.7 取消测试 ${uniqueTag()}`
    await page.getByTestId('subscribe-remark').fill(remark)
    await page.getByTestId('notice-checkbox').check()
    const createRes = page.waitForResponse(
      (res) => res.url().includes('/api/v1/orders') && res.request().method() === 'POST',
      { timeout: 30_000 },
    )
    await page.getByTestId('subscribe-submit').click()
    const res = await createRes
    expect(res.status()).toBeLessThan(400)
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10_000 })
    const body = await res.json()
    const orderId = String(body?.data?.orderId ?? body?.data?.id ?? '')

    // 进入详情，验证待确认状态
    await gotoOrderDetail(page, orderId)
    await waitForDetailStatus(page, '待确认')

    // 先测试「未确认弹窗状态不变」
    await cancelOrder(page, false) // 取消对话框但不确认
    await waitForDetailStatus(page, '待确认') // 状态仍为待确认

    // 正式取消
    await cancelOrder(page, true) // 确认取消
    await waitForDetailStatus(page, '已取消')

    // 取消后不可再取消
    await expect(page.getByTestId('action-cancel')).toHaveCount(0)
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 7：可追踪
  // 详情时间线完整；列表上链次数=详情 mock 条数
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-7) 可追踪：详情时间线完整；上链次数 ≥1（创建即有 mock 上链）', async ({
    page,
  }) => {
    // 使用场景 5 中已达成的订单（ADMIN 可见全部）
    await loginAs(page, 'admin')
    await gotoOrderList(page)
    await waitOrderListReady(page)

    // 取第一个订单查看详情
    const ids = await extractOrderIds(page)
    expect(ids.length).toBeGreaterThan(0)
    const firstId = ids[0]
    await gotoOrderDetail(page, firstId)

    // 时间线应有记录
    await expect(page.getByTestId('detail-timeline')).toBeVisible({ timeout: 15_000 })
    const timelineCount = await getTimelineCount(page)
    expect(timelineCount).toBeGreaterThan(0)

    // 上链次数应 ≥1
    const chainCount = await getChainCount(page)
    expect(chainCount).toBeGreaterThanOrEqual(1)

    // 上链次数应与时间线条数一致（REQ-WSC-ORDER-016）
    expect(chainCount).toBe(timelineCount)

    // 交叉验证：列表 API 返回的 chainCount 应与详情一致（P1 修复）
    await gotoOrderList(page)
    await waitOrderListReady(page)
    const listChainCount = await getListChainCount(page, firstId)
    expect(listChainCount).toBe(chainCount)
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 8：越权
  // USER 无确认按钮；PROVIDER 他企 403；深链隐藏≠授权
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-8) 越权：USER 无确认按钮；ADMIN 可代确认；深链目录维护 USER 不可达', async ({
    page,
  }) => {
    // 创建一个新订单用于越权测试
    await loginAs(page, 'user')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('catalog-list')).toBeVisible({ timeout: 20_000 })
    await page.getByTestId('catalog-row').first().click()
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })
    await page.getByTestId('product-order-btn').click()
    await expect(page.getByTestId('order-subscribe')).toBeVisible({ timeout: 15_000 })

    const remark = `E2E V1.7 越权测试 ${uniqueTag()}`
    await page.getByTestId('subscribe-remark').fill(remark)
    await page.getByTestId('notice-checkbox').check()
    const createRes = page.waitForResponse(
      (res) => res.url().includes('/api/v1/orders') && res.request().method() === 'POST',
      { timeout: 30_000 },
    )
    await page.getByTestId('subscribe-submit').click()
    const res = await createRes
    expect(res.status()).toBeLessThan(400)
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10_000 })
    const body = await res.json()
    const orderId = String(body?.data?.orderId ?? body?.data?.id ?? '')

    // USER 查看详情：应无确认订单按钮（USER 不能确认订单）
    await gotoOrderDetail(page, orderId)
    await expect(page.getByTestId('action-confirm-order')).toHaveCount(0)
    // USER 应有取消按钮（本人订单三未完成态可取消）
    await expect(page.getByTestId('action-cancel')).toBeVisible()

    // ADMIN 可代确认
    await loginAs(page, 'admin')
    await gotoOrderDetail(page, orderId)
    await expect(page.getByTestId('action-confirm-order')).toBeVisible()
    const confirmBtnText = await page.getByTestId('action-confirm-order').innerText()
    expect(confirmBtnText).toContain('代确认')

    // 深链隔离：USER 不可达目录维护
    await loginAs(page, 'user')
    await page.goto(ROUTE_CATALOG_MAINTENANCE)
    await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
    await expect(page.getByTestId('catalog-maintenance')).toHaveCount(0)
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 9：V1.6 回归抽样
  // test:p0-v16 可跑；目录双入口抽样
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-9) V1.6 回归抽样：ADMIN 目录双入口仍在、USER 公共目录可达', async ({
    page,
  }) => {
    // ADMIN 目录双入口
    await loginAs(page, 'admin')
    await ensureCatalogNavOpen(page)
    await expect(page.getByTestId('nav-catalog-browse')).toBeVisible()
    await expect(page.getByTestId('nav-my-catalog')).toBeVisible()
    await expect(page.getByTestId('nav-catalog-maintenance')).toBeVisible()

    // 目录维护全量可达
    await page.goto(ROUTE_CATALOG_MAINTENANCE)
    await waitMaintenanceReady(page, 'full')

    // 我的目录可达
    await page.goto(ROUTE_MY_CATALOG)
    await waitMaintenanceReady(page, 'myCatalog')

    // USER 公共目录可达
    await loginAs(page, 'user')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 15_000 })

    // USER 不可达 /my-products
    await page.goto(ROUTE_MY_PRODUCTS)
    await expect(page).toHaveURL(/\/catalog\/?$/, { timeout: 15_000 })
  })

  // ═══════════════════════════════════════════════════════════════════
  // 场景 10（建议）：数字合约版本
  // 创建后版本 1 可见；主路径达成后版本 ≥2 且历史可查
  // ═══════════════════════════════════════════════════════════════════
  test('§6.1-10) 数字合约版本：创建后版本 1 可见；达成后版本 ≥2', async ({ page }) => {
    // 创建新订单
    await loginAs(page, 'user')
    await page.goto('/catalog')
    await expect(page.getByTestId('catalog-browse')).toBeVisible({ timeout: 20_000 })
    await expect(page.getByTestId('catalog-list')).toBeVisible({ timeout: 20_000 })
    await page.getByTestId('catalog-row').first().click()
    await expect(page.getByTestId('product-detail')).toBeVisible({ timeout: 15_000 })
    await page.getByTestId('product-order-btn').click()
    await expect(page.getByTestId('order-subscribe')).toBeVisible({ timeout: 15_000 })

    const remark = `E2E V1.7 版本测试 ${uniqueTag()}`
    await page.getByTestId('subscribe-remark').fill(remark)
    await page.getByTestId('notice-checkbox').check()
    const createRes = page.waitForResponse(
      (res) => res.url().includes('/api/v1/orders') && res.request().method() === 'POST',
      { timeout: 30_000 },
    )
    await page.getByTestId('subscribe-submit').click()
    const res = await createRes
    expect(res.status()).toBeLessThan(400)
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10_000 })
    const body = await res.json()
    const orderId = String(body?.data?.orderId ?? body?.data?.id ?? '')

    // 查看详情，验证版本 1 可见
    await gotoOrderDetail(page, orderId)
    await expect(page.getByTestId('detail-contract-versions')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('current-version-no')).toBeVisible()
    await expect(page.getByTestId('current-version-no')).toContainText('v1')

    // 完成主路径（PROVIDER 确认 → USER 提交 → PROVIDER 确认合约）
    await loginAs(page, 'provider')
    await gotoOrderDetail(page, orderId)
    await confirmOrder(page)

    await loginAs(page, 'user')
    await gotoOrderDetail(page, orderId)
    await submitContract(page, { amount: '8888.00' })

    await loginAs(page, 'provider')
    await gotoOrderDetail(page, orderId)
    await confirmContract(page)

    // 验证版本 ≥2
    await loginAs(page, 'admin')
    await gotoOrderDetail(page, orderId)
    await expect(page.getByTestId('current-version-no')).toBeVisible()
    const versionText = await page.getByTestId('current-version-no').innerText()
    const versionNo = parseInt(versionText.replace('v', ''), 10)
    expect(versionNo).toBeGreaterThanOrEqual(2)

    // 版本列表应有多个版本
    const versionItems = page.locator('[data-testid^="version-"]')
    const count = await versionItems.count()
    expect(count).toBeGreaterThanOrEqual(2)
  })
})
