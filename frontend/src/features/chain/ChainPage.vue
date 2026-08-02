<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SensitiveId from './components/SensitiveId.vue'
import { chainEmptyMessage, formatCategoryPath, useChainPage } from './composables/useChainPage'

const route = useRoute()
const router = useRouter()
const productId = computed(() => String(route.params.productId ?? ''))

const {
  versions,
  selectedVersionId,
  snapshot,
  listState,
  snapshotState,
  listError,
  snapshotError,
  loadVersions,
  selectVersion,
} = useChainPage(() => productId.value)

const attestation = computed(() => {
  const s = snapshot.value
  if (!s) return null
  const fromSnap = s.attestation
  const selected = versions.value.find((v) => v.versionId === selectedVersionId.value)
  return {
    metadataHash: fromSnap?.metadataHash ?? selected?.metadataHash ?? '',
    ownerDID: fromSnap?.ownerDID ?? selected?.ownerDID ?? '',
    timestamp: fromSnap?.timestamp ?? selected?.timestamp ?? '',
    certOwner: fromSnap?.certificate?.owner ?? selected?.certificate?.owner ?? '',
  }
})

const categoryPathDisplay = computed(() =>
  formatCategoryPath(snapshot.value?.categoryPath, snapshot.value?.categoryPathParts),
)

const selectedVersionNo = computed(() => {
  const v = versions.value.find((x) => x.versionId === selectedVersionId.value)
  return v?.versionNo
})

function goBack() {
  if (productId.value) {
    void router.push(`/catalog/products/${productId.value}`)
    return
  }
  void router.push('/catalog')
}
</script>

<template>
  <div class="chain-page">
    <header class="chain-page-header wsc-card">
      <div class="chain-page-header-left">
        <button type="button" class="chain-page-back" aria-label="返回" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
        <div>
          <h1 class="chain-page-title">上链信息</h1>
          <p class="sub">产品 ID：{{ productId || '—' }}</p>
        </div>
      </div>
    </header>

    <div class="chain-page-body">
      <aside class="chain-page-left" aria-label="上链版本列表">
        <div class="wsc-card chain-list-card">
          <div class="chain-list-header">上链记录</div>

          <div v-if="listState === 'loading'" class="state" data-testid="list-loading">加载中…</div>
          <div v-else-if="listState === 'empty'" class="state empty" data-testid="list-empty">
            {{ chainEmptyMessage() }}
          </div>
          <div v-else-if="listState === 'error'" class="state error" data-testid="list-error">
            <p>{{ listError || '加载失败' }}</p>
            <button type="button" class="btn" @click="loadVersions">重试</button>
          </div>
          <ul v-else class="chain-list">
            <li v-for="v in versions" :key="v.versionId">
              <button
                type="button"
                class="chain-list-item"
                :class="{ active: v.versionId === selectedVersionId }"
                @click="selectVersion(v.versionId)"
              >
                <span class="chain-list-item-version">v{{ v.versionNo }}</span>
                <span class="chain-list-item-time">{{ v.timestamp }}</span>
              </button>
            </li>
          </ul>
        </div>
      </aside>

      <section class="chain-page-right" aria-label="目录快照">
        <div class="wsc-card chain-snapshot-card">
          <div class="chain-snapshot-header">
            <span>产品目录上链详情</span>
            <span v-if="selectedVersionNo != null" class="chain-snapshot-version">
              v{{ selectedVersionNo }}
            </span>
          </div>

          <div
            v-if="listState === 'empty'"
            class="state empty"
            data-testid="snapshot-empty"
          >
            无上链版本时无法展示快照
          </div>
          <div
            v-else-if="snapshotState === 'loading' || listState === 'loading'"
            class="state"
            data-testid="snapshot-loading"
          >
            加载中…
          </div>
          <div
            v-else-if="snapshotState === 'error'"
            class="state error"
            data-testid="snapshot-error"
          >
            <p>{{ snapshotError || '加载失败' }}</p>
          </div>
          <div v-else-if="snapshot && attestation" class="snapshot-body" data-testid="snapshot-ready">
            <h2 class="section-title">存证上链</h2>
            <SensitiveId label="metadataHash" :value="attestation.metadataHash" />
            <SensitiveId label="ownerDID" :value="attestation.ownerDID" />
            <div class="field">
              <span class="label">timestamp</span>
              <span>{{ attestation.timestamp }}</span>
            </div>

            <h2 class="section-title">权属证书</h2>
            <div class="field">
              <span class="label">certificate.owner</span>
              <span>{{ attestation.certOwner }}</span>
            </div>

            <h2 class="section-title">目录快照字段</h2>
            <div class="snapshot-grid">
              <div class="snapshot-item">
                <span class="snapshot-label">productCode</span>
                <span class="snapshot-value">{{ snapshot.productCode }}</span>
              </div>
              <div class="snapshot-item">
                <span class="snapshot-label">productName</span>
                <span class="snapshot-value">{{ snapshot.productName }}</span>
              </div>
              <div class="snapshot-item">
                <span class="snapshot-label">productType</span>
                <span class="snapshot-value">{{ snapshot.productType }}</span>
              </div>
              <div
                v-if="categoryPathDisplay"
                class="snapshot-item path-item"
                data-testid="snapshot-category-path"
              >
                <span class="snapshot-label">categoryPath（三级）</span>
                <span class="snapshot-value path">{{ categoryPathDisplay }}</span>
              </div>
              <div
                v-if="snapshot.categoryPathParts"
                class="snapshot-item path-item"
                data-testid="snapshot-category-parts"
              >
                <span class="snapshot-label">categoryPathParts</span>
                <span class="snapshot-value path">
                  L1={{ snapshot.categoryPathParts.l1 || '—' }} /
                  L2={{ snapshot.categoryPathParts.l2 || '—' }} /
                  L3={{ snapshot.categoryPathParts.l3 || '—' }}
                </span>
              </div>
              <div v-if="snapshot.summary" class="snapshot-item path-item">
                <span class="snapshot-label">summary</span>
                <span class="snapshot-value">{{ snapshot.summary }}</span>
              </div>
              <div v-if="snapshot.scenario" class="snapshot-item path-item">
                <span class="snapshot-label">scenario</span>
                <span class="snapshot-value">{{ snapshot.scenario }}</span>
              </div>
              <div class="snapshot-item">
                <span class="snapshot-label">capturedAt</span>
                <span class="snapshot-value">{{ snapshot.capturedAt }}</span>
              </div>
            </div>

            <details v-if="snapshot.basicInfo" class="group">
              <summary>基础信息</summary>
              <pre>{{ JSON.stringify(snapshot.basicInfo, null, 2) }}</pre>
            </details>
            <details v-if="snapshot.supplierInfo" class="group">
              <summary>供应商</summary>
              <pre>{{ JSON.stringify(snapshot.supplierInfo, null, 2) }}</pre>
            </details>
            <details v-if="snapshot.propertyRights" class="group">
              <summary>数据产权</summary>
              <pre>{{ JSON.stringify(snapshot.propertyRights, null, 2) }}</pre>
            </details>
            <details v-if="snapshot.typeSpecific" class="group">
              <summary>类型专属</summary>
              <pre>{{ JSON.stringify(snapshot.typeSpecific, null, 2) }}</pre>
            </details>
            <div v-if="snapshot.tags?.length" class="tags">
              <span v-for="t in snapshot.tags" :key="t" class="tag">{{ t }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.chain-page {
  padding: 0 0 40px;
  color: var(--text-primary);
}

.chain-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  margin-bottom: 12px;
}

.chain-page-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chain-page-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  padding: 0;
}

.chain-page-back:hover {
  background: #f3f4f6;
  color: var(--text-primary);
}

.chain-page-back svg {
  width: 18px;
  height: 18px;
}

.chain-page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.sub {
  margin: 2px 0 0;
  color: var(--text-tertiary);
  font-size: 12px;
}

.chain-page-body {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(320px, 1fr);
  gap: 16px;
  align-items: start;
}

.chain-list-card,
.chain-snapshot-card {
  padding: 16px;
  min-height: 320px;
}

.chain-list-header,
.chain-snapshot-header {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.chain-snapshot-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chain-snapshot-version {
  background: var(--blue);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.chain-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.chain-list-item {
  width: 100%;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: transparent;
  cursor: pointer;
  color: inherit;
  font: inherit;
}

.chain-list-item:hover {
  background: #f9fafb;
}

.chain-list-item.active {
  background: var(--blue-light);
  border-color: var(--blue);
}

.chain-list-item-version {
  font-size: 12px;
  font-weight: 600;
  color: var(--blue);
}

.chain-list-item-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

.state {
  color: var(--text-secondary);
  font-size: 14px;
  padding: 24px 8px;
}

.state.empty {
  color: var(--text-tertiary);
}

.state.error {
  color: var(--red);
}

.state.error p {
  margin: 0 0 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: var(--radius-menu);
  font-size: 13px;
  cursor: pointer;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-primary);
}

.section-title {
  margin: 16px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-title:first-child {
  margin-top: 0;
}

.field {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin: 6px 0;
  font-size: 13px;
}

.field .label {
  color: var(--text-secondary);
  min-width: 7em;
}

.snapshot-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.snapshot-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.snapshot-item.path-item {
  grid-column: 1 / -1;
}

.snapshot-label {
  font-size: 11px;
  color: var(--text-tertiary);
}

.snapshot-value {
  font-size: 13px;
  color: var(--text-primary);
  word-break: break-word;
}

.snapshot-value.path {
  color: var(--blue);
  font-weight: 600;
}

.group {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}

.group pre {
  margin: 6px 0 0;
  padding: 8px;
  background: #f9fafb;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: auto;
  font-size: 12px;
  color: var(--text-primary);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.tag {
  background: var(--blue-light);
  color: var(--blue);
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
}

@media (max-width: 900px) {
  .chain-page-body {
    grid-template-columns: 1fr;
  }

  .snapshot-grid {
    grid-template-columns: 1fr;
  }
}
</style>
