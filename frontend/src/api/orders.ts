/**
 * Orders API — 对齐 contracts/openapi (wsc-contracts@2.3.3 / SNAP-WSC-009)
 * V1.7 交易订单（链内一事一议简易流程；止于合约达成）。
 * 订单 scope **仅**信 SessionPrincipal；拒绝客户端 enterpriseId/userId 扩权。
 */
import { apiRequest } from './client'

// ── 枚举 ──

export type OrderStatus =
  | 'PENDING_CONFIRM'
  | 'PENDING_UPLOAD'
  | 'PENDING_CONTRACT_CONFIRM'
  | 'CONTRACT_REACHED'
  | 'CANCELLED'

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING_CONFIRM: '待确认订单',
  PENDING_UPLOAD: '待上传合约',
  PENDING_CONTRACT_CONFIRM: '待确认合约',
  CONTRACT_REACHED: '合约已达成',
  CANCELLED: '已取消',
}

// ── 请求体 ──

export type OrderCreateRequest = {
  productId: string
  remark: string
  noticeAccepted: boolean
}

export type ContractConfirmRequest = {
  noticeAccepted: boolean
}

// ── 响应体 ──

export type OrderListItem = {
  orderId: string
  productName: string
  productCode: string
  productType: string
  sourcePlatform: string
  providerEnterpriseName: string
  demandUserName: string
  demandUserLoginName: string
  demandEnterpriseName: string
  amountTaxInclusive: string
  amountTaxExclusive: string
  currency: 'CNY'
  status: OrderStatus
  statusLabel: string
  chainCount: number
  createTime: string
}

export type OrderPage = {
  total: number
  list: OrderListItem[]
  page: number
  pageSize: number
}

export type OrderProductSnapshot = {
  productId: string
  productName: string
  productCode: string
  productType: string
  sourcePlatform: string
  allowSimpleOrder: boolean
}

export type OrderParticipant = {
  providerEnterpriseId: string
  providerEnterpriseName: string
  demandUserId: string
  demandUserName: string
  demandUserLoginName: string
  demandEnterpriseId: string
  demandEnterpriseName: string
}

export type OrderLine = {
  unit: string
  quantity: number
  unitPrice: string
  subtotal: string
}

export type OrderTransactionInfo = {
  orderLines: OrderLine[]
  amountTaxInclusive: string
  amountTaxExclusive: string
  currency: 'CNY'
}

export type OrderAttachmentMeta = {
  fileName: string
  fileType: string
  fileSize: number
  scanResult: string
}

export type OrderTimelineEvent = {
  eventType: string
  description: string
  operator: string
  operatorRole: string
  occurredAt: string
}

export type OrderChainLog = {
  chainHash: string
  blockHeight: number
  chainNode: string
  timestamp: string
}

export type OrderContractVersion = {
  versionNo: number
  snapshotJson: string
  createdAt: string
}

export type OrderNotice = {
  content: string
  version: string
}

export type OrderDetail = {
  orderId: string
  status: OrderStatus
  statusLabel: string
  productSnapshot: OrderProductSnapshot
  participant: OrderParticipant
  remark: string | null
  noticeVersion: string
  transactionInfo: OrderTransactionInfo | null
  attachment: OrderAttachmentMeta | null
  timeline: OrderTimelineEvent[]
  chainLogs: OrderChainLog[]
  contractVersions: OrderContractVersion[]
  chainCount: number
  amountTaxInclusive: string | null
  amountTaxExclusive: string | null
  currency: 'CNY' | null
  createTime: string
  updateTime: string
}

// ── 查询参数 ──

export type OrderListParams = {
  keyword?: string
  status?: OrderStatus
  page?: number
  pageSize?: 10 | 20 | 50 | 100
}

// ── API 函数 ──

/** GET /orders — 订单分页列表（按角色过滤） */
export function listOrders(params: OrderListParams = {}) {
  const searchParams = new URLSearchParams()
  if (params.keyword) searchParams.set('keyword', params.keyword)
  if (params.status) searchParams.set('status', params.status)
  if (params.page) searchParams.set('page', String(params.page))
  if (params.pageSize) searchParams.set('pageSize', String(params.pageSize))
  const qs = searchParams.toString()
  return apiRequest<OrderPage>(`/orders${qs ? `?${qs}` : ''}`)
}

/** POST /orders — 链内创建订单（仅 USER） */
export function createOrder(body: OrderCreateRequest) {
  return apiRequest<OrderListItem>('/orders', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

/** GET /orders/notices/current — 平台统一须知正文 */
export function getCurrentNotice() {
  return apiRequest<OrderNotice>('/orders/notices/current')
}

/** GET /orders/{orderId} — 订单详情 */
export function getOrder(orderId: string) {
  return apiRequest<OrderDetail>(`/orders/${encodeURIComponent(orderId)}`)
}

/** POST /orders/{orderId}/confirm — 确认订单（PROVIDER 本企业 / ADMIN 代确认；无 body） */
export function confirmOrder(orderId: string) {
  return apiRequest<OrderListItem>(`/orders/${encodeURIComponent(orderId)}/confirm`, {
    method: 'POST',
  })
}

/** POST /orders/{orderId}/contract — 提交合约附件与交易信息（仅需求方本人） */
export function submitContract(orderId: string, file: File, transactionInfo: OrderTransactionInfo) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('transactionInfo', JSON.stringify(transactionInfo))
  return apiRequest<OrderListItem>(`/orders/${encodeURIComponent(orderId)}/contract`, {
    method: 'POST',
    body: formData,
  })
}

/** POST /orders/{orderId}/contract/confirm — 统一确认合约（PROVIDER 本企业 / ADMIN 代确认） */
export function confirmContract(orderId: string, body: ContractConfirmRequest) {
  return apiRequest<OrderListItem>(`/orders/${encodeURIComponent(orderId)}/contract/confirm`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

/** POST /orders/{orderId}/cancel — 取消订单（三未完成态；三方均可） */
export function cancelOrder(orderId: string) {
  return apiRequest<OrderListItem>(`/orders/${encodeURIComponent(orderId)}/cancel`, {
    method: 'POST',
  })
}

/** GET /orders/{orderId}/attachment — 下载签署附件（按角色权限） */
export function getOrderAttachmentUrl(orderId: string): string {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1'
  return `${baseUrl}/orders/${encodeURIComponent(orderId)}/attachment`
}
