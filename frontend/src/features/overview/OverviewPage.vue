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
      <div class="page-header-left">
        <h1>总览</h1>
        <p class="sub">核心运营指标与上链动态</p>
      </div>
      <div class="page-header-right">
        <span
          v-if="metrics.state === 'success' || metrics.state === 'empty'"
          class="page-update-time"
        >
          更新于 {{ updatedAtText }}
        </span>
        <button type="button" class="btn" data-testid="overview-refresh" @click="loadAll">
          刷新
        </button>
      </div>
    </header>

    <!-- 指标卡 REQ-OVW-001 / REQ-UX-003 -->
    <div v-if="metrics.state === 'loading'" class="state-card" data-testid="metrics-loading">
      加载中…
    </div>
    <div v-else-if="metrics.state === 'error'" class="state-card error" data-testid="metrics-error">
      <p>{{ metrics.error }}</p>
      <button type="button" class="btn ghost" @click="loadMetrics">重试</button>
    </div>
    <div
      v-else-if="metrics.state === 'empty'"
      class="stats-grid muted"
      data-testid="metrics-empty"
    >
      <article class="stat-card">
        <div class="stat-card-header">
          <div class="stat-card-title">链上数据资产总数</div>
          <div class="stat-card-icon blue" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6" />
              <line x1="8" y1="12" x2="21" y2="12" />
              <line x1="8" y1="18" x2="21" y2="18" />
              <line x1="3" y1="6" x2="3.01" y2="6" />
              <line x1="3" y1="12" x2="3.01" y2="12" />
              <line x1="3" y1="18" x2="3.01" y2="18" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">0</div>
        <div class="stat-card-desc">暂无指标数据</div>
      </article>
      <article class="stat-card">
        <div class="stat-card-header">
          <div class="stat-card-title">活跃企业数</div>
          <div class="stat-card-icon green" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">0</div>
        <div class="stat-card-desc">暂无指标数据</div>
      </article>
      <article class="stat-card">
        <div class="stat-card-header">
          <div class="stat-card-title">今日新增存证</div>
          <div class="stat-card-icon orange" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">0</div>
        <div class="stat-card-desc">暂无指标数据</div>
      </article>
    </div>
    <div v-else-if="metrics.data" class="stats-grid" data-testid="metrics-success">
      <article class="stat-card">
        <div class="stat-card-header">
          <div class="stat-card-title">链上数据资产总数</div>
          <div class="stat-card-icon blue" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6" />
              <line x1="8" y1="12" x2="21" y2="12" />
              <line x1="8" y1="18" x2="21" y2="18" />
              <line x1="3" y1="6" x2="3.01" y2="6" />
              <line x1="3" y1="12" x2="3.01" y2="12" />
              <line x1="3" y1="18" x2="3.01" y2="18" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">{{ metrics.data.assetTotal }}</div>
        <div class="stat-card-desc">已登记并上链的数据条目数。</div>
      </article>
      <article class="stat-card">
        <div class="stat-card-header">
          <div class="stat-card-title">活跃企业数</div>
          <div class="stat-card-icon green" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">{{ metrics.data.activeEnterprises }}</div>
        <div class="stat-card-desc">参与数据生态的机构数量。</div>
      </article>
      <article class="stat-card">
        <div class="stat-card-header">
          <div class="stat-card-title">今日新增存证</div>
          <div class="stat-card-icon orange" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">{{ metrics.data.todayAttestations }}</div>
        <div class="stat-card-desc">今天新上链的数据量。</div>
      </article>
    </div>

    <div class="overview-charts">
      <!-- 趋势 REQ-OVW-002 -->
      <section class="chart-card" aria-labelledby="trend-title">
        <div class="card-header">
          <div>
            <h2 id="trend-title">近 {{ trendDays }} 天上链趋势</h2>
            <span class="card-sub">近 {{ trendDays }} 天数据上链流量</span>
          </div>
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
            <div v-for="n in Math.min(trendDays, 30)" :key="n" class="bar-wrap">
              <div class="bar" style="height: 0" />
            </div>
          </div>
        </div>
        <div
          v-else
          ref="trendBox"
          class="trend-chart"
          data-testid="trend-success"
          :key="chartWidth"
        >
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
      <section class="chart-card" aria-labelledby="top-title">
        <div class="card-header">
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
            <span class="name" :title="item.productName">{{ item.productName }}</span>
            <div class="track">
              <div
                class="fill"
                :class="idx === 0 ? 'gold' : 'blue'"
                :style="{ width: `${Math.round((item.count / maxTop) * 100)}%` }"
              />
            </div>
            <span class="count">{{ item.count }} 次</span>
          </li>
        </ol>
      </section>
    </div>

    <div class="overview-bottom">
      <!-- 动态流 REQ-OVW-004 -->
      <section class="chart-card" aria-labelledby="stream-title">
        <div class="card-header">
          <div>
            <h2 id="stream-title">最新上链动态</h2>
            <span class="card-sub">最近动态</span>
          </div>
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
            <span class="dot" :data-type="ev.type" aria-hidden="true" />
            <div class="body">
              <div class="stream-head">
                <span class="type" :data-type="ev.type">{{ STREAM_TYPE_LABEL[ev.type] }}</span>
                <span class="time">{{ ev.relativeTime }}</span>
              </div>
              <div class="subject">{{ ev.subject }}</div>
              <div class="action">{{ ev.actionSummary }}</div>
              <div class="meta-row">
                <code :title="ev.chainRecordId">上链记录：{{ ev.chainRecordId }}</code>
              </div>
            </div>
          </li>
        </ul>
      </section>

      <!-- 分布 REQ-OVW-005 -->
      <section class="chart-card" aria-labelledby="dist-title">
        <div class="card-header">
          <div>
            <h2 id="dist-title">行业 / 地域分布</h2>
            <span class="card-sub">行业与地域占比</span>
          </div>
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
            <h3>行业分布</h3>
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
            <h3>地域分布</h3>
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
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.page-header-left h1 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.sub {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.page-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-update-time {
  font-size: 13px;
  color: var(--text-tertiary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.stat-card-title {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-card-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-menu);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card-icon svg {
  width: 18px;
  height: 18px;
}

.stat-card-icon.blue {
  background: var(--blue-light);
  color: var(--blue);
}

.stat-card-icon.green {
  background: var(--green-light);
  color: var(--green);
}

.stat-card-icon.orange {
  background: var(--orange-light);
  color: var(--orange);
}

.stat-card-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.stat-card-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.stats-grid.muted .stat-card-value {
  color: var(--text-tertiary);
}

.overview-charts,
.overview-bottom {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
}

.chart-card {
  padding: 16px 20px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-sub {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.state-card {
  padding: 28px 16px;
  text-align: center;
  color: var(--text-secondary);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
}

.state-card.error,
.state.error {
  color: var(--red);
}

.state {
  padding: 28px 8px;
  text-align: center;
  color: var(--text-secondary);
}

.state.empty {
  color: var(--text-tertiary);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-primary);
  border-radius: var(--radius-menu);
  padding: 8px 14px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.15s;
}

.btn:hover {
  background: #f9fafb;
}

.btn.ghost {
  margin-top: 8px;
  background: var(--card-bg);
  color: var(--blue);
  border-color: #bfdbfe;
}

.days {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.days select {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 4px 8px;
  background: var(--card-bg);
  color: var(--text-primary);
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
  background: linear-gradient(180deg, var(--blue) 0%, #2563eb 100%);
  border-radius: 2px 2px 0 0;
  transition: height 0.2s ease;
}

.trend-chart.muted .bar {
  background: var(--border-color);
}

.hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.top-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-list li {
  display: grid;
  grid-template-columns: minmax(0, 180px) 1fr 50px;
  gap: 10px;
  align-items: center;
}

.top-list .name {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top-list .count {
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
  text-align: right;
}

.top-list .track {
  height: 18px;
  background: #f3f4f6;
  border-radius: 9px;
  overflow: hidden;
}

.top-list .fill {
  height: 100%;
  border-radius: 0 9px 9px 0;
  transition: width 0.3s ease;
}

.top-list .fill.gold {
  background: linear-gradient(90deg, #fbbf24, var(--orange));
}

.top-list .fill.blue {
  background: linear-gradient(90deg, var(--blue), #2563eb);
}

.stream-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 420px;
  overflow: auto;
}

.stream-list li {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.dot {
  width: 6px;
  height: 6px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--green);
  flex-shrink: 0;
}

.dot[data-type='TRADE_ORDER'] {
  background: var(--blue);
}

.dot[data-type='CATALOG_REGISTER'] {
  background: var(--orange);
}

.body {
  min-width: 0;
  flex: 1;
}

.stream-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.type {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 999px;
  background: var(--green-light);
  color: var(--green);
  white-space: nowrap;
}

.type[data-type='CATALOG_REGISTER'] {
  background: var(--orange-light);
  color: var(--orange);
}

.type[data-type='TRADE_ORDER'] {
  background: var(--blue-light);
  color: var(--blue);
}

.time {
  font-size: 11px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.subject {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.action {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.meta-row {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-tertiary);
}

.meta-row code {
  font-size: 11px;
  color: var(--text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  white-space: nowrap;
  display: block;
}

.dist {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dist h3 {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
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
  grid-template-columns: 72px 1fr 40px;
  gap: 8px;
  align-items: center;
  font-size: 12px;
}

.pie-list .name,
.region-list .name {
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pie-list .track,
.region-list .track {
  height: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}

.pie-list .fill,
.region-list .fill {
  height: 100%;
  border-radius: 4px;
}

.fill.industry {
  background: var(--green);
}

.fill.region {
  background: var(--orange);
}

.pct,
.val {
  text-align: right;
  font-variant-numeric: tabular-nums;
  color: var(--text-secondary);
}

@media (max-width: 960px) {
  .overview-charts,
  .overview-bottom {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .top-list li {
    grid-template-columns: minmax(0, 1fr) 1.2fr 48px;
  }
}
</style>
