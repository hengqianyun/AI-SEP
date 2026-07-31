import { ref } from 'vue'
/**
 * Overview 数据加载 — 对齐 wsc-contracts@2.0.0（无新增指标；空态友好）。
 */
import {
  getOverviewDistribution,
  getOverviewMetrics,
  getOverviewStream,
  getOverviewTop,
  getOverviewTrend,
  type OverviewMetrics,
  type StreamEvent,
} from '@/api/overview'
import { ApiError } from '@/api/client'

export type LoadState = 'idle' | 'loading' | 'success' | 'empty' | 'error'

export type SectionState<T> = {
  state: LoadState
  data: T | null
  error: string | null
}

function emptySection<T>(): SectionState<T> {
  return { state: 'idle', data: null, error: null }
}

function messageOf(e: unknown): string {
  if (e instanceof ApiError) return e.message || '请求失败'
  if (e instanceof Error) return e.message
  return '请求失败，请稍后重试'
}

export function useOverviewData() {
  const metrics = ref<SectionState<OverviewMetrics>>(emptySection())
  const trend = ref<SectionState<{ date: string; count: number }[]>>(emptySection())
  const top = ref<SectionState<{ productId?: string; productName: string; count: number }[]>>(
    emptySection(),
  )
  const stream = ref<SectionState<StreamEvent[]>>(emptySection())
  const distribution = ref<
    SectionState<{
      byIndustry: { name: string; value: number }[]
      byRegion: { name: string; value: number }[]
    }>
  >(emptySection())

  async function loadMetrics() {
    metrics.value = { state: 'loading', data: null, error: null }
    try {
      const res = await getOverviewMetrics()
      const d = res.data
      const isEmpty =
        d.assetTotal === 0 && d.activeEnterprises === 0 && d.todayAttestations === 0
      metrics.value = {
        state: isEmpty ? 'empty' : 'success',
        data: d,
        error: null,
      }
    } catch (e) {
      metrics.value = { state: 'error', data: null, error: messageOf(e) }
    }
  }

  async function loadTrend(days = 30) {
    trend.value = { state: 'loading', data: null, error: null }
    try {
      const res = await getOverviewTrend(days)
      const points = res.data.points ?? []
      const allZero = points.length === 0 || points.every((p) => p.count === 0)
      trend.value = {
        state: allZero ? 'empty' : 'success',
        data: points,
        error: null,
      }
    } catch (e) {
      trend.value = { state: 'error', data: null, error: messageOf(e) }
    }
  }

  async function loadTop(limit = 10) {
    top.value = { state: 'loading', data: null, error: null }
    try {
      const res = await getOverviewTop(limit)
      const items = res.data.items ?? []
      top.value = {
        state: items.length === 0 ? 'empty' : 'success',
        data: items,
        error: null,
      }
    } catch (e) {
      top.value = { state: 'error', data: null, error: messageOf(e) }
    }
  }

  async function loadStream(limit = 10) {
    stream.value = { state: 'loading', data: null, error: null }
    try {
      const res = await getOverviewStream(limit)
      const items = res.data.items ?? []
      stream.value = {
        state: items.length === 0 ? 'empty' : 'success',
        data: items,
        error: null,
      }
    } catch (e) {
      stream.value = { state: 'error', data: null, error: messageOf(e) }
    }
  }

  async function loadDistribution() {
    distribution.value = { state: 'loading', data: null, error: null }
    try {
      const res = await getOverviewDistribution()
      const d = res.data
      const isEmpty =
        (d.byIndustry?.length ?? 0) === 0 && (d.byRegion?.length ?? 0) === 0
      distribution.value = {
        state: isEmpty ? 'empty' : 'success',
        data: d,
        error: null,
      }
    } catch (e) {
      distribution.value = { state: 'error', data: null, error: messageOf(e) }
    }
  }

  async function loadAll() {
    await Promise.all([
      loadMetrics(),
      loadTrend(),
      loadTop(),
      loadStream(),
      loadDistribution(),
    ])
  }

  return {
    metrics,
    trend,
    top,
    stream,
    distribution,
    loadAll,
    loadMetrics,
    loadTrend,
    loadTop,
    loadStream,
    loadDistribution,
  }
}

export const STREAM_TYPE_LABEL: Record<StreamEvent['type'], string> = {
  CATALOG_REGISTER: '目录登记',
  DATA_REGISTER: '数据登记',
  TRADE_ORDER: '交易订单',
}
