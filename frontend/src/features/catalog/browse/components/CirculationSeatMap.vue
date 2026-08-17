<script setup lang="ts">
/**
 * 公共目录座序图（REQ-CAT-009 / TASK-WSC-604；REQ-CAT-015 / TASK-WSC-902）。
 * 仅由 CatalogBrowsePage 在 showSeatMap=true 时挂载；无点击筛选；L1 下拉与 browse Cascader 独立。
 * TASK-WSC-909：标题「数据流通链」相对卡片水平居中；L1 仍靠右。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { ConfigProvider, Select } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'
import {
  getL2Distribution,
  listCategories,
  type Category,
  type L2Distribution,
  type L2DistributionItem,
} from '@/api/catalog'
import { apiRequest } from '@/api/client'

const THEME = [
  { bg: '#06b6d4', text: '#06b6d4' },
  { bg: '#3b82f6', text: '#3b82f6' },
  { bg: '#6366f1', text: '#6366f1' },
  { bg: '#0ea5e9', text: '#0ea5e9' },
  { bg: '#14b8a6', text: '#14b8a6' },
]

/** §1.5 Ant→token 最小映射（座序图 L1 Select） */
const SEAT_MAP_FILTER_THEME: ThemeConfig = {
  token: {
    colorPrimary: '#3b82f6',
    colorBorder: '#e5e7eb',
    colorBgContainer: '#ffffff',
    borderRadius: 6,
    fontSize: 14,
  },
}

const ALL_L1_VALUE = ''
const GRID = 400
const COLS = 50
/** hover 熄灭其它域时的 opacity 上限（可测） */
const DIM_OPACITY = 0.15

const loading = ref(true)
const error = ref<string | null>(null)
const totalProducts = ref(0)
const top5 = ref<L2DistributionItem[]>([])
const hoveredCode = ref<string | null>(null)
/** 座序图 L1 下拉独立 state，不与 browse Cascader 联动 */
const selectedL1Id = ref(ALL_L1_VALUE)
const l1Options = ref<{ value: string; label: string }[]>([
  { value: ALL_L1_VALUE, label: '全部' },
])

type Dot = { code: string; color: string; opacity: number }

const isEmpty = computed(
  () => !loading.value && !error.value && (totalProducts.value === 0 || top5.value.length === 0),
)

const dots = computed<Dot[]>(() => {
  const items = top5.value
  if (items.length === 0) return []
  const sum = items.reduce((a, i) => a + i.count, 0)
  if (sum <= 0) return []

  const arr: Dot[] = []
  items.forEach((item, index) => {
    const color = THEME[index % THEME.length]!.text
    let n = Math.floor((item.count / sum) * GRID)
    if (item.count > 0 && n === 0) n = 1
    for (let i = 0; i < n; i++) {
      const opacities = [0.35, 0.65, 1]
      arr.push({ code: item.code, color, opacity: opacities[i % 3]! })
    }
  })
  while (arr.length < GRID) {
    const item = items[arr.length % items.length]!
    const index = items.findIndex((x) => x.code === item.code)
    const color = THEME[index % THEME.length]!.text
    arr.push({ code: item.code, color, opacity: 0.35 })
  }
  if (arr.length > GRID) arr.length = GRID
  for (let i = arr.length - 1; i > 0; i--) {
    const j = hashIndex(arr[i]!.code, i) % (i + 1)
    ;[arr[i], arr[j]] = [arr[j]!, arr[i]!]
  }
  return arr
})

function hashIndex(code: string, salt: number): number {
  let h = salt * 31
  for (let i = 0; i < code.length; i++) h = (h * 31 + code.charCodeAt(i)) >>> 0
  return h
}

function dimOpacity(code: string, base: number): number {
  if (!hoveredCode.value) return base
  if (hoveredCode.value === code) return 1
  return Math.min(base, DIM_OPACITY)
}

/**
 * 拉取 L2 分布；有 L1 时走 query（903 前 api 未生成带参 helper）。
 */
async function fetchL2Distribution(l1CategoryId: string): Promise<L2Distribution> {
  if (!l1CategoryId) {
    const res = await getL2Distribution()
    return res.data ?? { totalProducts: 0, items: [] }
  }
  const qs = new URLSearchParams({ l1CategoryId })
  const res = await apiRequest<L2Distribution>(`/catalog/l2-distribution?${qs}`)
  return res.data ?? { totalProducts: 0, items: [] }
}

async function loadL1Options() {
  try {
    const res = await listCategories()
    const l1Items = (res.data?.items ?? []).filter((c: Category) => c.level === 'L1')
    l1Options.value = [
      { value: ALL_L1_VALUE, label: '全部' },
      ...l1Items.map((c) => ({ value: c.id, label: c.name })),
    ]
  } catch {
    l1Options.value = [{ value: ALL_L1_VALUE, label: '全部' }]
  }
}

async function load() {
  loading.value = true
  error.value = null
  hoveredCode.value = null
  try {
    const data = await fetchL2Distribution(selectedL1Id.value)
    totalProducts.value = data.totalProducts ?? 0
    top5.value = (data.items ?? []).slice(0, 5)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载座序图失败'
    top5.value = []
    totalProducts.value = 0
  } finally {
    loading.value = false
  }
}

watch(selectedL1Id, () => {
  void load()
})

onMounted(() => {
  void loadL1Options()
  void load()
})
</script>

<template>
  <section class="seat-map wsc-surface" data-testid="circulation-seat-map">
    <ConfigProvider :theme="SEAT_MAP_FILTER_THEME">
      <div
        class="title-row"
        data-seat-map-filter-theme="ant-token-mapped"
        data-testid="seat-map-title-row"
      >
        <h2>数据流通链</h2>
        <Select
          v-model:value="selectedL1Id"
          class="l1-select"
          :options="l1Options"
          :loading="loading"
          aria-label="业务视图"
          data-testid="seat-map-l1-select"
        />
      </div>
    </ConfigProvider>

    <div class="title-block">
      <p v-if="loading" data-testid="seat-map-loading">加载中…</p>
      <p v-else-if="error" class="err" data-testid="seat-map-error">{{ error }}</p>
      <p v-else-if="isEmpty" data-testid="seat-map-empty">暂无流通数据</p>
      <p v-else data-testid="seat-map-total">{{ totalProducts }}条数据</p>
    </div>

    <div
      v-if="dots.length"
      class="grid"
      data-testid="seat-map-grid"
      :style="{ gridTemplateColumns: `repeat(${COLS}, minmax(0, 1fr))` }"
    >
      <div
        v-for="(dot, i) in dots"
        :key="i"
        class="cell"
        :data-seat-code="dot.code"
      >
        <svg
          viewBox="0 0 100 100"
          class="icon"
          :style="{
            color: dot.color,
            opacity: dimOpacity(dot.code, dot.opacity),
            transform:
              hoveredCode === dot.code
                ? 'scale(1.12)'
                : hoveredCode && hoveredCode !== dot.code
                  ? 'scale(0.85)'
                  : 'scale(1)',
          }"
        >
          <rect x="25" y="20" width="50" height="60" fill="currentColor" rx="2" />
          <rect x="20" y="30" width="60" height="12" fill="#e2e8f0" opacity="0.9" />
          <rect x="20" y="58" width="60" height="12" fill="#e2e8f0" opacity="0.9" />
        </svg>
      </div>
    </div>

    <div
      v-if="top5.length"
      class="legend"
      data-testid="seat-map-legend"
      role="presentation"
    >
      <div
        v-for="(item, index) in top5"
        :key="item.code"
        class="cat-chip"
        role="presentation"
        :data-testid="`seat-map-chip-${item.code}`"
        :data-dim-opacity-cap="DIM_OPACITY"
        :style="{
          background: THEME[index % THEME.length]!.bg,
          opacity: hoveredCode && hoveredCode !== item.code ? 0.45 : 1,
        }"
        @mouseenter="hoveredCode = item.code"
        @mouseleave="hoveredCode = null"
      >
        <span class="name">{{ item.name }}</span>
        <span class="count">{{ item.count }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.seat-map {
  margin-bottom: 28px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.title-row {
  width: 100%;
  max-width: 960px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-bottom: 8px;
  min-height: 40px;
}
.title-row h2 {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  text-align: center;
  pointer-events: none;
  white-space: nowrap;
}
.l1-select {
  min-width: 160px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}
.title-block {
  text-align: center;
  margin-bottom: 20px;
}
.title-block p {
  margin: 0;
  font-size: 13px;
  color: #475569;
}
.title-block .err {
  color: #b91c1c;
}
.grid {
  width: 100%;
  border-top: 1px solid #e2e8f0;
  border-left: 1px solid #e2e8f0;
  display: grid;
}
.cell {
  aspect-ratio: 1;
  border-right: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.icon {
  width: 85%;
  height: 85%;
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.legend {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  width: 100%;
}
.cat-chip {
  flex: 1 1 120px;
  max-width: 200px;
  border-radius: 12px;
  padding: 12px 14px;
  color: #fff;
  cursor: default;
  user-select: none;
  transition: opacity 0.25s ease;
}
.cat-chip .name {
  display: block;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}
.cat-chip .count {
  display: block;
  font-size: 13px;
  opacity: 0.9;
}
</style>
