/**
 * Overview API — 对齐 contracts/openapi + overview/stream-events.yaml (wsc-contracts@2.2.0)
 */
import { apiRequest } from './client'

export type OverviewMetrics = {
  assetTotal: number
  activeEnterprises: number
  todayAttestations: number
  updatedAt: string
}

export type StreamEventType = 'CATALOG_REGISTER' | 'DATA_REGISTER' | 'TRADE_ORDER'

export type StreamEvent = {
  type: StreamEventType
  subject: string
  actionSummary: string
  relativeTime: string
  occurredAt: string
  chainRecordId: string
}

export function getOverviewMetrics() {
  return apiRequest<OverviewMetrics>('/overview/metrics')
}

export function getOverviewTrend(days = 30) {
  return apiRequest<{ points: { date: string; count: number }[] }>(
    `/overview/trend?days=${days}`,
  )
}

export function getOverviewTop(limit = 10) {
  return apiRequest<{ items: { productId?: string; productName: string; count: number }[] }>(
    `/overview/top?limit=${limit}`,
  )
}

export function getOverviewStream(limit = 10) {
  return apiRequest<{ items: StreamEvent[] }>(`/overview/stream?limit=${limit}`)
}

export function getOverviewDistribution() {
  return apiRequest<{
    byIndustry: { name: string; value: number }[]
    byRegion: { name: string; value: number }[]
  }>('/overview/distribution')
}
