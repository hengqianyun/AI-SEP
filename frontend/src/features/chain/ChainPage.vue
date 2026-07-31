<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SensitiveId from './components/SensitiveId.vue'
import { chainEmptyMessage, formatCategoryPath, useChainPage } from './composables/useChainPage'

const route = useRoute()
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
</script>

<template>
  <div class="chain-page">
    <header class="page-header">
      <h1>上链信息</h1>
      <p class="sub">产品 ID：{{ productId || '—' }}</p>
    </header>

    <div class="panels">
      <aside class="panel versions" aria-label="上链版本列表">
        <div class="panel-title">版本列表</div>

        <div v-if="listState === 'loading'" class="state" data-testid="list-loading">加载中…</div>
        <div v-else-if="listState === 'empty'" class="state empty" data-testid="list-empty">
          {{ chainEmptyMessage() }}
        </div>
        <div v-else-if="listState === 'error'" class="state error" data-testid="list-error">
          <p>{{ listError || '加载失败' }}</p>
          <button type="button" @click="loadVersions">重试</button>
        </div>
        <ul v-else class="version-list">
          <li v-for="v in versions" :key="v.versionId">
            <button
              type="button"
              class="version-item"
              :class="{ active: v.versionId === selectedVersionId }"
              @click="selectVersion(v.versionId)"
            >
              <span class="ver">v{{ v.versionNo }}</span>
              <span class="ts">{{ v.timestamp }}</span>
            </button>
          </li>
        </ul>
      </aside>

      <section class="panel snapshot" aria-label="目录快照">
        <div class="panel-title">目录快照</div>

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
          <dl class="snap-dl">
            <div>
              <dt>productCode</dt>
              <dd>{{ snapshot.productCode }}</dd>
            </div>
            <div>
              <dt>productName</dt>
              <dd>{{ snapshot.productName }}</dd>
            </div>
            <div>
              <dt>productType</dt>
              <dd>{{ snapshot.productType }}</dd>
            </div>
            <div v-if="categoryPathDisplay" data-testid="snapshot-category-path">
              <dt>categoryPath</dt>
              <dd>{{ categoryPathDisplay }}</dd>
            </div>
            <div v-if="snapshot.categoryPathParts" data-testid="snapshot-category-parts">
              <dt>categoryPathParts</dt>
              <dd>
                L1={{ snapshot.categoryPathParts.l1 || '—' }} /
                L2={{ snapshot.categoryPathParts.l2 || '—' }} /
                L3={{ snapshot.categoryPathParts.l3 || '—' }}
              </dd>
            </div>
            <div v-if="snapshot.summary">
              <dt>summary</dt>
              <dd>{{ snapshot.summary }}</dd>
            </div>
            <div v-if="snapshot.scenario">
              <dt>scenario</dt>
              <dd>{{ snapshot.scenario }}</dd>
            </div>
            <div>
              <dt>capturedAt</dt>
              <dd>{{ snapshot.capturedAt }}</dd>
            </div>
          </dl>

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
      </section>
    </div>
  </div>
</template>

<style scoped>
.chain-page {
  padding: 24px 28px;
  color: #1b2a3a;
}
.page-header h1 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 600;
}
.sub {
  margin: 0 0 20px;
  color: #666;
  font-size: 13px;
}
.panels {
  display: grid;
  grid-template-columns: minmax(220px, 280px) 1fr;
  gap: 16px;
  align-items: start;
}
.panel {
  background: #fff;
  border: 1px solid #e2e6ec;
  border-radius: 8px;
  padding: 14px 16px;
  min-height: 320px;
}
.panel-title {
  font-weight: 600;
  margin-bottom: 12px;
  font-size: 14px;
}
.version-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.version-item {
  width: 100%;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #f7f7f5;
  cursor: pointer;
}
.version-item:hover {
  border-color: #c5ccd6;
}
.version-item.active {
  border-color: #1b2a3a;
  background: #eef3f8;
}
.ver {
  font-weight: 600;
}
.ts {
  font-size: 12px;
  color: #666;
}
.state {
  color: #666;
  font-size: 14px;
  padding: 24px 8px;
}
.state.empty {
  color: #555;
}
.state.error {
  color: #a33;
}
.state.error button {
  margin-top: 8px;
  padding: 4px 12px;
  cursor: pointer;
}
.section-title {
  margin: 16px 0 10px;
  font-size: 15px;
  font-weight: 600;
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
  color: #666;
  min-width: 7em;
}
.snap-dl {
  margin: 0;
  display: grid;
  gap: 8px;
}
.snap-dl > div {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 8px;
  font-size: 13px;
}
.snap-dl dt {
  color: #666;
  margin: 0;
}
.snap-dl dd {
  margin: 0;
}
.group {
  margin-top: 10px;
  font-size: 13px;
}
.group pre {
  margin: 6px 0 0;
  padding: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: auto;
  font-size: 12px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}
.tag {
  background: #eef3f8;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
}
@media (max-width: 800px) {
  .panels {
    grid-template-columns: 1fr;
  }
}
</style>
