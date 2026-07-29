<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { STREAM_TYPE_LABEL, useOverviewData } from './composables/useOverviewData'

const {
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
} = useOverviewData()

const trendDays = ref(30)
const chartWidth = ref(640)

let resizeObserver: ResizeObserver | null = null
const trendBox = ref<HTMLElement | null>(null)

onMounted(() => {
  void loadAll()
  resizeObserver = new ResizeObserver((entries) => {
    const w = entries[0]?.contentRect.width
    if (w && w > 0) chartWidth.value = Math.floor(w)
  })
  if (trendBox.value) resizeObserver.observe(trendBox.value)
})

onUnmounted(() => {
  resizeObserver?.disconnect()
})

const maxTrend = computed(() => {
  const pts = trend.value.data ?? []
  return Math.max(1, ...pts.map((p) => p.count))
})

const maxTop = computed(() => {
  const items = top.value.data ?? []
  return Math.max(1, ...items.map((i) => i.count))
})

const industryTotal = computed(() => {
  const slices = distribution.value.data?.byIndustry ?? []
  return slices.reduce((s, x) => s + x.value, 0) || 1
})

const maxRegion = computed(() => {
  const slices = distribution.value.data?.byRegion ?? []
  return Math.max(1, ...slices.map((x) => x.value))
})

const updatedAtText = computed(() => {
  const raw = metrics.value.data?.updatedAt
  if (!raw) return '—'
  try {
    return new Date(raw).toLocaleString('zh-CN')
  } catch {
    return raw
  }
})

async function onTrendDaysChange() {
  await loadTrend(trendDays.value)
}

function barHeight(count: number): string {
  const h = Math.round((count / maxTrend.value) * 120)
  return `${Math.max(count > 0 ? 4 : 0, h)}px`
}
</script>

<template>
  <div class="overview-page">
    <header class="page-header">
      <div>
        <h1>总览</h1>
        <p class="sub">核心运营指标与上链动态</p>
      </div>
      <button type="button" class="btn" data-testid="overview-refresh" @click="loadAll">
        刷新
      </button>
    </header>

    <!-- 指标卡 REQ-OVW-001 -->
    <section class="panel" aria-labelledby="metrics-title">
      <div class="panel-head">
        <h2 id="metrics-title">核心指标</h2>
        <span v-if="metrics.state === 'success' || metrics.state === 'empty'" class="meta">
          最近更新：{{ updatedAtText }}
        </span>
      </div>
      <div v-if="metrics.state === 'loading'" class="state" data-testid="metrics-loading">
        加载中…
      </div>
      <div v-else-if="metrics.state === 'error'" class="state error" data-testid="metrics-error">
        <p>{{ metrics.error }}</p>
        <button type="button" class="btn ghost" @click="loadMetrics">重试</button>
      </div>
      <div v-else-if="metrics.state === 'empty'" class="state empty" data-testid="metrics-empty">
        暂无指标数据
        <div class="metric-grid muted">
          <div class="metric-card">
            <div class="label">链上数据资产总数</div>
            <div class="value">0</div>
          </div>
          <div class="metric-card">
            <div class="label">活跃企业数</div>
            <div class="value">0</div>
          </div>
          <div class="metric-card">
            <div class="label">今日新增存证</div>
            <div class="value">0</div>
          </div>
        </div>
      </div>
      <div v-else-if="metrics.data" class="metric-grid" data-testid="metrics-success">
        <div class="metric-card">
          <div class="label">链上数据资产总数</div>
          <div class="value">{{ metrics.data.assetTotal }}</div>
        </div>
        <div class="metric-card">
          <div class="label">活跃企业数</div>
          <div class="value">{{ metrics.data.activeEnterprises }}</div>
        </div>
        <div class="metric-card">
          <div class="label">今日新增存证</div>
          <div class="value">{{ metrics.data.todayAttestations }}</div>
        </div>
      </div>
    </section>

    <div class="two-col">
      <!-- 趋势 REQ-OVW-002 -->
      <section class="panel" aria-labelledby="trend-title">
        <div class="panel-head">
          <h2 id="trend-title">近 {{ trendDays }} 天上链趋势</h2>
          <label class="days">
            窗口
            <select v-model.number="trendDays" @change="onTrendDaysChange">
              <option :value="7">7 天</option>
              <option :value="14">14 天</option>
              <option :value="30">30 天</option>
              <option :value="60">60 天</option>
            </select>
          </label>
        </div>
        <div v-if="trend.state === 'loading'" class="state" data-testid="trend-loading">加载中…</div>
        <div v-else-if="trend.state === 'error'" class="state error" data-testid="trend-error">
          <p>{{ trend.error }}</p>
          <button type="button" class="btn ghost" @click="() => loadTrend(trendDays)">重试</button>
        </div>
        <div v-else-if="trend.state === 'empty'" class="state empty" data-testid="trend-empty">
          暂无趋势数据
          <div ref="trendBox" class="trend-chart muted" aria-hidden="true">
            <div
              v-for="n in Math.min(trendDays, 30)"
              :key="n"
              class="bar-wrap"
            >
              <div class="bar" style="height: 0" />
            </div>
          </div>
        </div>
        <div v-else ref="trendBox" class="trend-chart" data-testid="trend-success" :key="chartWidth">
          <div
            v-for="p in trend.data"
            :key="p.date"
            class="bar-wrap"
            :title="`${p.date}: ${p.count}`"
          >
            <div class="bar" :style="{ height: barHeight(p.count) }" />
          </div>
        </div>
        <p v-if="trend.state === 'success'" class="hint">横轴日期 · 纵轴上链量（柱高随窗口自适应）</p>
      </section>

      <!-- TOP10 REQ-OVW-003 -->
      <section class="panel" aria-labelledby="top-title">
        <div class="panel-head">
          <h2 id="top-title">产品上链 TOP10</h2>
        </div>
        <div v-if="top.state === 'loading'" class="state" data-testid="top-loading">加载中…</div>
        <div v-else-if="top.state === 'error'" class="state error" data-testid="top-error">
          <p>{{ top.error }}</p>
          <button type="button" class="btn ghost" @click="() => loadTop()">重试</button>
        </div>
        <div v-else-if="top.state === 'empty'" class="state empty" data-testid="top-empty">
          暂无 TOP 数据
        </div>
        <ol v-else class="top-list" data-testid="top-success">
          <li v-for="(item, idx) in top.data" :key="item.productId || item.productName">
            <span class="rank">{{ idx + 1 }}</span>
            <span class="name">{{ item.productName }}</span>
            <span class="count">{{ item.count }}</span>
            <div class="track">
              <div
                class="fill"
                :style="{ width: `${Math.round((item.count / maxTop) * 100)}%` }"
              />
            </div>
          </li>
        </ol>
      </section>
    </div>

    <div class="two-col">
      <!-- 动态流 REQ-OVW-004 -->
      <section class="panel" aria-labelledby="stream-title">
        <div class="panel-head">
          <h2 id="stream-title">最新上链动态</h2>
        </div>
        <div v-if="stream.state === 'loading'" class="state" data-testid="stream-loading">加载中…</div>
        <div v-else-if="stream.state === 'error'" class="state error" data-testid="stream-error">
          <p>{{ stream.error }}</p>
          <button type="button" class="btn ghost" @click="() => loadStream()">重试</button>
        </div>
        <div v-else-if="stream.state === 'empty'" class="state empty" data-testid="stream-empty">
          暂无上链动态
        </div>
        <ul v-else class="stream-list" data-testid="stream-success">
          <li v-for="ev in stream.data" :key="ev.chainRecordId + ev.occurredAt">
            <span class="type" :data-type="ev.type">{{ STREAM_TYPE_LABEL[ev.type] }}</span>
            <div class="body">
              <div class="subject">{{ ev.subject }}</div>
              <div class="action">{{ ev.actionSummary }}</div>
              <div class="meta-row">
                <span>{{ ev.relativeTime }}</span>
                <code :title="ev.chainRecordId">{{ ev.chainRecordId }}</code>
              </div>
            </div>
          </li>
        </ul>
      </section>

      <!-- 分布 REQ-OVW-005 -->
      <section class="panel" aria-labelledby="dist-title">
        <div class="panel-head">
          <h2 id="dist-title">行业 / 地域分布</h2>
        </div>
        <div
          v-if="distribution.state === 'loading'"
          class="state"
          data-testid="distribution-loading"
        >
          加载中…
        </div>
        <div
          v-else-if="distribution.state === 'error'"
          class="state error"
          data-testid="distribution-error"
        >
          <p>{{ distribution.error }}</p>
          <button type="button" class="btn ghost" @click="loadDistribution">重试</button>
        </div>
        <div
          v-else-if="distribution.state === 'empty'"
          class="state empty"
          data-testid="distribution-empty"
        >
          暂无分布数据
        </div>
        <div v-else class="dist" data-testid="distribution-success">
          <div>
            <h3>行业占比</h3>
            <ul class="pie-list">
              <li v-for="s in distribution.data?.byIndustry" :key="'i-' + s.name">
                <span class="name">{{ s.name }}</span>
                <div class="track">
                  <div
                    class="fill industry"
                    :style="{ width: `${Math.round((s.value / industryTotal) * 100)}%` }"
                  />
                </div>
                <span class="pct">{{ Math.round((s.value / industryTotal) * 100) }}%</span>
              </li>
            </ul>
          </div>
          <div>
            <h3>地域对比</h3>
            <ul class="region-list">
              <li v-for="s in distribution.data?.byRegion" :key="'r-' + s.name">
                <span class="name">{{ s.name }}</span>
                <div class="track">
                  <div
                    class="fill region"
                    :style="{ width: `${Math.round((s.value / maxRegion) * 100)}%` }"
                  />
                </div>
                <span class="val">{{ s.value }}</span>
              </li>
            </ul>
          </div>
          <p class="hint">口径说明：V1 为总览投影/fixture 统计，与目录侧挂载统计对齐前以本页为准。</p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.overview-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1200px;
}
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
h1 {
  margin: 0;
  font-size: 22px;
  color: #1b2a3a;
}
.sub {
  margin: 4px 0 0;
  color: #667788;
  font-size: 13px;
}
.panel {
  background: #fff;
  border: 1px solid #e2e6ec;
  border-radius: 8px;
  padding: 16px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
h2 {
  margin: 0;
  font-size: 16px;
  color: #1b2a3a;
}
h3 {
  margin: 0 0 8px;
  font-size: 13px;
  color: #445566;
}
.meta {
  font-size: 12px;
  color: #8899aa;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.metric-card {
  padding: 14px 16px;
  background: #f7f9fb;
  border-radius: 6px;
  border: 1px solid #e8edf2;
}
.metric-card .label {
  font-size: 13px;
  color: #667788;
}
.metric-card .value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 600;
  color: #1b2a3a;
  font-variant-numeric: tabular-nums;
}
.metric-grid.muted .value {
  color: #99aab8;
}
.two-col {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
}
.state {
  padding: 24px 8px;
  text-align: center;
  color: #667788;
}
.state.error {
  color: #a33;
}
.state.empty {
  color: #8899aa;
}
.btn {
  border: 1px solid #c5ced8;
  background: #1b2a3a;
  color: #fff;
  border-radius: 6px;
  padding: 6px 14px;
  cursor: pointer;
  font-size: 13px;
}
.btn.ghost {
  background: #fff;
  color: #1b2a3a;
  margin-top: 8px;
}
.btn:hover {
  opacity: 0.92;
}
.days {
  font-size: 12px;
  color: #667788;
  display: flex;
  align-items: center;
  gap: 6px;
}
.days select {
  border: 1px solid #c5ced8;
  border-radius: 4px;
  padding: 2px 6px;
}
.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 140px;
  padding: 8px 0 0;
  overflow: hidden;
}
.bar-wrap {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 100%;
}
.bar {
  width: 100%;
  max-width: 14px;
  background: linear-gradient(180deg, #3d6f99 0%, #1b2a3a 100%);
  border-radius: 2px 2px 0 0;
  transition: height 0.2s ease;
}
.hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #99aab8;
}
.top-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.top-list li {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  grid-template-rows: auto auto;
  gap: 2px 8px;
  align-items: center;
}
.rank {
  grid-row: 1 / 3;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e8edf2;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
  color: #1b2a3a;
}
.name {
  font-size: 13px;
  color: #1b2a3a;
}
.count {
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  color: #445566;
}
.track {
  grid-column: 2 / 4;
  height: 6px;
  background: #eef2f6;
  border-radius: 3px;
  overflow: hidden;
}
.fill {
  height: 100%;
  background: #3d6f99;
  border-radius: 3px;
}
.fill.industry {
  background: #2a6a4f;
}
.fill.region {
  background: #8a5a2b;
}
.stream-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 420px;
  overflow: auto;
}
.stream-list li {
  display: flex;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef2f6;
}
.type {
  flex: 0 0 auto;
  align-self: flex-start;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #e8edf2;
  color: #1b2a3a;
  white-space: nowrap;
}
.type[data-type='CATALOG_REGISTER'] {
  background: #dce8f5;
}
.type[data-type='DATA_REGISTER'] {
  background: #dcefe4;
}
.type[data-type='TRADE_ORDER'] {
  background: #f5e6d4;
}
.body {
  min-width: 0;
  flex: 1;
}
.subject {
  font-size: 13px;
  font-weight: 600;
  color: #1b2a3a;
}
.action {
  font-size: 12px;
  color: #556677;
  margin-top: 2px;
}
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 4px;
  font-size: 11px;
  color: #8899aa;
}
.meta-row code {
  font-size: 11px;
  color: #556677;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
  white-space: nowrap;
}
.dist {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.pie-list,
.region-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pie-list li,
.region-list li {
  display: grid;
  grid-template-columns: 56px 1fr 40px;
  gap: 8px;
  align-items: center;
  font-size: 12px;
}
.pct,
.val {
  text-align: right;
  font-variant-numeric: tabular-nums;
  color: #556677;
}
@media (max-width: 960px) {
  .two-col {
    grid-template-columns: 1fr;
  }
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
