<script setup lang="ts">
import { computed } from 'vue'
import type { ProductType, TypeSpecificFields } from '@/api/catalog'
import OpenApiReadonlyPanel from './OpenApiReadonlyPanel.vue'
import { buildTypeSpecificFields } from '../utils/typeSpecificView'

const props = defineProps<{
  productType?: ProductType | string
  typeSpecific?: TypeSpecificFields | null
}>()

const fields = computed(() =>
  buildTypeSpecificFields(props.productType, props.typeSpecific),
)

const isApi = computed(() => props.productType === 'API')
const apiBlock = computed(() => props.typeSpecific?.api)
</script>

<template>
  <section class="detail-section" data-testid="type-specific">
    <h2 class="detail-section-title">数据描述</h2>

    <div v-if="fields.length" class="detail-grid" data-testid="type-specific-fields">
      <div
        v-for="f in fields"
        :key="f.key"
        class="detail-item"
        :data-testid="`ts-field-${f.key}`"
      >
        <span class="detail-label">{{ f.label }}</span>
        <span class="detail-value">{{ f.value }}</span>
      </div>
    </div>
    <div v-else class="detail-desc muted" data-testid="type-specific-empty">暂无扩展数据描述</div>

    <div v-if="isApi" class="api-panel-wrap">
      <h3 class="sub-title">接口定义（只读）</h3>
      <OpenApiReadonlyPanel :api="apiBlock" />
    </div>
  </section>
</template>

<style scoped>
.detail-section {
  margin-bottom: 24px;
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

.detail-label {
  font-size: 11px;
  color: var(--text-tertiary);
}

.detail-value {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-word;
  white-space: pre-wrap;
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

.api-panel-wrap {
  margin-top: 16px;
}

.sub-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
