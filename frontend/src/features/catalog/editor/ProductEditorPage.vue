<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import { useProductEditor } from './composables/useProductEditor'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { productWriteVisible } = useCanWrite(role)

const mode = computed(() => (route.name === 'catalog-create' ? 'create' : 'edit'))
const productId = computed(() =>
  mode.value === 'edit' ? String(route.params.productId || '') : undefined,
)

const editor = useProductEditor(mode.value === 'create' ? 'create' : 'edit')
const { state, feedback, form, isOther, l2Categories, init, onTagKeydown, removeTag, submit } =
  editor

onMounted(() => {
  if (!productWriteVisible.value) {
    void router.replace('/catalog')
    return
  }
  void init(productId.value)
})

watch(mode, () => {
  void init(productId.value)
})

async function onSubmit() {
  const id = await submit(productId.value)
  if (id) {
    void router.push(`/catalog/products/${id}`)
  }
}

function onCancel() {
  void router.push('/catalog')
}
</script>

<template>
  <div class="product-editor" data-testid="product-editor">
    <header class="page-header">
      <h1>{{ mode === 'create' ? '新增数据产品' : '编辑数据产品' }}</h1>
    </header>

    <div v-if="state === 'loading'">加载中…</div>

    <form v-else class="form" @submit.prevent="onSubmit">
      <fieldset>
        <legend>基础信息</legend>
        <label>产品名称 * <input v-model="form.productName" required /></label>
        <label>产品编码 * <input v-model="form.productCode" :readonly="mode === 'edit'" required /></label>
        <label>
          产品类型
          <select v-model="form.productType">
            <option value="DATASET">数据集</option>
            <option value="REPORT">数据报告</option>
            <option value="API">数据接口</option>
            <option value="OTHER">其他数据产品</option>
          </select>
        </label>
        <label>
          二级行业分类
          <select v-model="form.l2CategoryId" required>
            <option disabled value="">请选择</option>
            <option v-for="c in l2Categories" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </label>
        <label>业务大类 <input v-model="form.businessCategory" /></label>
        <label>业务子类 <input v-model="form.businessSubCategory" /></label>
        <label>数据来源 <input v-model="form.dataSource" /></label>
        <label>更新频率 <input v-model="form.updateFrequency" /></label>
        <label>交付方式 <input v-model="form.deliveryMethod" /></label>
        <label class="check"><input v-model="form.involvesPersonalInfo" type="checkbox" /> 涉及个人信息</label>
        <label class="check"><input v-model="form.involvesPublicData" type="checkbox" /> 涉及公共数据</label>
        <label>计费方式 <input v-model="form.billingMethod" /></label>
        <label>价格 <input v-model="form.price" /></label>
      </fieldset>

      <fieldset>
        <legend>供应商与产权</legend>
        <label>供应商名称 <input v-model="form.supplierName" /></label>
        <label>信用代码 <input v-model="form.supplierCreditCode" /></label>
        <label>产权类型 <input v-model="form.propertyRightsType" /></label>
      </fieldset>

      <fieldset v-if="!isOther" data-testid="type-specific-editor">
        <legend>类型专属字段</legend>
        <label v-if="form.productType === 'DATASET'">
          记录数 <input v-model="form.datasetRecordCount" type="number" />
        </label>
        <label v-else-if="form.productType === 'REPORT'">
          页数 <input v-model="form.reportPageCount" type="number" />
        </label>
        <label v-else-if="form.productType === 'API'">
          接口路径 <input v-model="form.apiEndpoint" />
        </label>
      </fieldset>
      <p v-else class="hint" data-testid="type-specific-editor-absent">
        其他数据产品无需填写类型专属字段（OQ-004）
      </p>

      <fieldset>
        <legend>标签 / 简介 / 场景</legend>
        <div class="tags">
          <span v-for="t in form.tags" :key="t" class="tag">
            {{ t }}
            <button type="button" @click="removeTag(t)">×</button>
          </span>
          <input
            v-model="form.tagInput"
            placeholder="输入后回车添加"
            @keydown="onTagKeydown"
          />
        </div>
        <label>简介 <textarea v-model="form.summary" rows="3" /></label>
        <label>应用场景 <textarea v-model="form.scenario" rows="3" /></label>
      </fieldset>

      <p
        v-if="feedback"
        :class="state === 'success' ? 'ok' : state === 'error' ? 'err' : ''"
        data-testid="editor-feedback"
      >
        {{ feedback }}
      </p>

      <div class="actions">
        <button type="submit" class="primary" :disabled="state === 'submitting'">
          {{ state === 'submitting' ? '提交中…' : '提交' }}
        </button>
        <button type="button" @click="onCancel">取消</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.product-editor {
  padding: 16px 20px 40px;
  max-width: 840px;
}
.page-header h1 {
  margin: 0 0 16px;
  font-size: 22px;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
fieldset {
  border: 1px solid #e6ebf0;
  border-radius: 6px;
  padding: 12px 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 14px;
}
legend {
  padding: 0 6px;
  font-weight: 600;
}
label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #444;
}
label.check {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
input,
select,
textarea {
  font: inherit;
  padding: 6px 8px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
}
.tags {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.tag {
  background: #eef2f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}
.tag button {
  border: none;
  background: none;
  cursor: pointer;
}
.hint {
  color: #666;
  font-size: 13px;
  margin: 0;
}
.actions {
  display: flex;
  gap: 8px;
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
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.ok {
  color: #1a7f37;
}
.err {
  color: #a33;
}
</style>
