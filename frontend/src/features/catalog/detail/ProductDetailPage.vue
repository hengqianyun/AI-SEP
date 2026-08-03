<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import SensitiveId from './components/SensitiveId.vue'
import TypeSpecificReadonly from './components/TypeSpecificReadonly.vue'
import { useProductDetail } from './composables/useProductDetail'
import { isDetailEditEntryVisible } from './utils/typeSpecificView'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const editEntryVisible = computed(() => isDetailEditEntryVisible(role.value))
const { product, state, error, load } = useProductDetail()

const productId = computed(() => String(route.params.productId || ''))

onMounted(() => {
  if (productId.value) void load(productId.value)
})

function goEdit() {
  if (!editEntryVisible.value || !productId.value) return
  void router.push(`/catalog/products/${productId.value}/edit`)
}

function goChain() {
  if (!productId.value) return
  void router.push(`/chain/products/${productId.value}`)
}

function goBack() {
  void router.push('/catalog')
}
</script>

<template>
  <div class="product-detail" data-testid="product-detail">
    <header class="detail-page-header wsc-card">
      <div class="detail-page-header-left">
        <button type="button" class="detail-page-back" aria-label="返回目录" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
        <h1 class="detail-page-title">产品详情</h1>
      </div>
      <div class="detail-page-header-right">
        <button type="button" class="btn" @click="goChain">上链信息</button>
        <button
          v-if="editEntryVisible"
          type="button"
          class="btn btn-primary"
          data-testid="detail-edit-entry"
          @click="goEdit"
        >
          编辑
        </button>
      </div>
    </header>

    <div v-if="state === 'loading'" class="state-card wsc-card" data-testid="detail-loading">
      加载中…
    </div>
    <div v-else-if="state === 'error'" class="state-card wsc-card error" data-testid="detail-error">
      <p>{{ error }}</p>
      <button type="button" class="btn" @click="load(productId)">重试</button>
    </div>
    <div v-else-if="!product" class="state-card wsc-card empty">未找到产品</div>

    <div v-else class="detail-page-body">
      <div class="detail-page-card wsc-card">
        <section class="detail-section">
          <h2 class="detail-section-title">基础信息</h2>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">产品名称</span>
              <span class="detail-value">{{ product.productName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">产品编码</span>
              <span class="detail-value">{{ product.productCode }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">产品类型</span>
              <span class="detail-value">{{ product.productType }}</span>
            </div>
            <div class="detail-item" data-testid="industry-category">
              <span class="detail-label">行业分类</span>
              <span class="detail-value">{{ product.industryCategory || '—' }}</span>
            </div>
            <div class="detail-item detail-item-path" data-testid="category-path">
              <span class="detail-label">目录挂载路径（可选）</span>
              <span class="detail-value path">{{ product.categoryPath || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">业务大类</span>
              <span class="detail-value">{{ product.businessCategory || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">业务子类</span>
              <span class="detail-value">{{ product.businessSubCategory || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">数据来源</span>
              <span class="detail-value">{{ product.dataSource || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">更新频率</span>
              <span class="detail-value">{{ product.updateFrequency || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">交付方式</span>
              <span class="detail-value">{{ product.deliveryMethod || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">涉及个人信息</span>
              <span class="detail-value">{{ product.involvesPersonalInfo ? '是' : '否' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">涉及公共数据</span>
              <span class="detail-value">{{ product.involvesPublicData ? '是' : '否' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">计费方式</span>
              <span class="detail-value">{{ product.billingMethod || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">价格</span>
              <span class="detail-value">{{ product.price || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">上链次数</span>
              <span class="detail-value">{{ product.chainCount }}</span>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h2 class="detail-section-title">供应商信息</h2>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">供应商</span>
              <span class="detail-value">{{ product.supplierName || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">信用代码</span>
              <span class="detail-value">
                <SensitiveId
                  v-if="product.supplierCreditCode"
                  :value="String(product.supplierCreditCode)"
                  label=""
                />
                <template v-else>—</template>
              </span>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h2 class="detail-section-title">数据产权</h2>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">产权类型</span>
              <span class="detail-value">{{ product.propertyRightsType || '—' }}</span>
            </div>
          </div>
        </section>

        <TypeSpecificReadonly
          :product-type="product.productType"
          :type-specific="product.typeSpecific"
        />

        <section class="detail-section">
          <h2 class="detail-section-title">关键标签</h2>
          <div class="detail-tags">
            <span v-for="t in product.tags || []" :key="t" class="detail-tag">{{ t }}</span>
            <span v-if="!(product.tags && product.tags.length)" class="muted">暂无标签</span>
          </div>
        </section>

        <section class="detail-section">
          <h2 class="detail-section-title">产品简介</h2>
          <div class="detail-desc">{{ product.summary || '暂无描述' }}</div>
        </section>

        <section class="detail-section">
          <h2 class="detail-section-title">应用场景</h2>
          <div class="detail-desc">{{ product.scenario || '暂无应用场景描述' }}</div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 0 0 40px;
  color: var(--text-primary);
}

.detail-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
}

.detail-page-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.detail-page-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
}

.detail-page-back:hover {
  background: #f9fafb;
  color: var(--text-primary);
}

.detail-page-back svg {
  width: 18px;
  height: 18px;
}

.detail-page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.detail-page-header-right {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: var(--radius-menu);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-primary);
}

.btn:hover {
  background: #f9fafb;
}

.btn-primary {
  background: var(--blue);
  color: #fff;
  border-color: var(--blue);
}

.btn-primary:hover {
  background: #2563eb;
}

.state-card {
  padding: 28px 20px;
  text-align: center;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}

.state-card.error {
  color: var(--red);
}

.state-card.error p {
  margin: 0 0 12px;
}

.state-card.empty {
  color: var(--text-tertiary);
}

.detail-page-body {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.detail-page-card {
  padding: 24px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section-title {
  margin: 0 0 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

@media (min-width: 1400px) {
  .detail-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  min-width: 0;
}

.detail-item-path {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 11px;
  color: var(--text-tertiary);
}

.detail-value {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-word;
}

.detail-value.path {
  color: var(--blue);
  font-weight: 600;
  letter-spacing: 0.01em;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--blue-light);
  color: var(--blue);
  font-size: 12px;
}

.detail-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.muted {
  color: var(--text-tertiary);
  font-size: 13px;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .detail-page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
