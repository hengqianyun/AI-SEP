<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import SensitiveId from './components/SensitiveId.vue'
import { useProductDetail } from './composables/useProductDetail'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { productWriteVisible } = useCanWrite(role)
const { product, state, error, load } = useProductDetail()

const productId = computed(() => String(route.params.productId || ''))
const isOther = computed(() => product.value?.productType === 'OTHER')
const typeSpecific = computed(() => {
  const ts = (product.value as { typeSpecific?: Record<string, unknown> } | null)?.typeSpecific
  return ts || {}
})

onMounted(() => {
  if (productId.value) void load(productId.value)
})

function goEdit() {
  if (!productWriteVisible.value || !productId.value) return
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
    <header class="page-header">
      <button type="button" class="link" @click="goBack">← 返回目录</button>
      <h1>产品详情</h1>
    </header>

    <div v-if="state === 'loading'" data-testid="detail-loading">加载中…</div>
    <div v-else-if="state === 'error'" class="error" data-testid="detail-error">
      {{ error }}
      <button type="button" @click="load(productId)">重试</button>
    </div>
    <div v-else-if="!product" class="empty">未找到产品</div>

    <template v-else>
      <div class="actions">
        <button v-if="productWriteVisible" type="button" class="primary" @click="goEdit">编辑</button>
        <button type="button" @click="goChain">上链信息</button>
      </div>

      <section class="group">
        <h2>基础信息</h2>
        <dl>
          <div><dt>产品名称</dt><dd>{{ product.productName }}</dd></div>
          <div><dt>产品编码</dt><dd>{{ product.productCode }}</dd></div>
          <div><dt>产品类型</dt><dd>{{ product.productType }}</dd></div>
          <div data-testid="category-path">
            <dt>分类路径（三级）</dt>
            <dd>{{ product.categoryPath || '—' }}</dd>
          </div>
          <div><dt>业务大类</dt><dd>{{ product.businessCategory || '—' }}</dd></div>
          <div><dt>业务子类</dt><dd>{{ product.businessSubCategory || '—' }}</dd></div>
          <div><dt>数据来源</dt><dd>{{ product.dataSource || '—' }}</dd></div>
          <div><dt>更新频率</dt><dd>{{ product.updateFrequency || '—' }}</dd></div>
          <div><dt>交付方式</dt><dd>{{ product.deliveryMethod || '—' }}</dd></div>
          <div><dt>涉及个人信息</dt><dd>{{ product.involvesPersonalInfo ? '是' : '否' }}</dd></div>
          <div><dt>涉及公共数据</dt><dd>{{ product.involvesPublicData ? '是' : '否' }}</dd></div>
          <div><dt>计费方式</dt><dd>{{ product.billingMethod || '—' }}</dd></div>
          <div><dt>价格</dt><dd>{{ product.price || '—' }}</dd></div>
          <div><dt>上链次数</dt><dd>{{ product.chainCount }}</dd></div>
        </dl>
      </section>

      <section class="group">
        <h2>供应商信息</h2>
        <dl>
          <div><dt>供应商</dt><dd>{{ product.supplierName || '—' }}</dd></div>
          <div>
            <dt>信用代码</dt>
            <dd>
              <SensitiveId
                v-if="product.supplierCreditCode"
                :value="String(product.supplierCreditCode)"
                label=""
              />
              <span v-else>—</span>
            </dd>
          </div>
        </dl>
      </section>

      <section class="group">
        <h2>数据产权</h2>
        <dl>
          <div><dt>产权类型</dt><dd>{{ product.propertyRightsType || '—' }}</dd></div>
        </dl>
      </section>

      <section v-if="!isOther" class="group" data-testid="type-specific">
        <h2>类型专属字段</h2>
        <pre>{{ JSON.stringify(typeSpecific, null, 2) }}</pre>
      </section>
      <section v-else class="group muted" data-testid="type-specific-absent">
        <h2>类型专属字段</h2>
        <p>其他数据产品无专属字段区（OQ-004）</p>
      </section>

      <section class="group">
        <h2>关键标签</h2>
        <div class="tags">
          <span v-for="t in product.tags || []" :key="t" class="tag">{{ t }}</span>
          <span v-if="!(product.tags && product.tags.length)">—</span>
        </div>
      </section>

      <section class="group">
        <h2>产品简介</h2>
        <p>{{ product.summary || '—' }}</p>
      </section>

      <section class="group">
        <h2>应用场景</h2>
        <p>{{ product.scenario || '—' }}</p>
      </section>
    </template>
  </div>
</template>

<style scoped>
.product-detail {
  padding: 16px 20px 40px;
  max-width: 960px;
}
.page-header h1 {
  margin: 8px 0 0;
  font-size: 22px;
}
.link {
  border: none;
  background: none;
  color: #1f4b7a;
  cursor: pointer;
  padding: 0;
  font: inherit;
}
.actions {
  display: flex;
  gap: 8px;
  margin: 12px 0 20px;
}
button {
  padding: 6px 12px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font: inherit;
}
button.primary {
  border-color: #1f4b7a;
  color: #1f4b7a;
}
.group {
  margin-bottom: 18px;
  padding: 12px 14px;
  border: 1px solid #e6ebf0;
  border-radius: 6px;
  background: #fff;
}
.group h2 {
  margin: 0 0 10px;
  font-size: 15px;
}
.group.muted {
  background: #fafbfc;
  color: #666;
}
dl {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px 16px;
  margin: 0;
}
dt {
  color: #777;
  font-size: 12px;
}
dd {
  margin: 2px 0 0;
  font-size: 14px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  background: #eef2f6;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.error {
  color: #a33;
}
pre {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
}
</style>
