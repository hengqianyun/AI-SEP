<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import {
  DATA_FORM_OPTIONS,
  UPDATE_FREQUENCY_OPTIONS,
  useProductEditor,
} from './composables/useProductEditor'
import OpenApiEditorPanel from './components/OpenApiEditorPanel.vue'

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
const {
  state,
  feedback,
  form,
  isApi,
  isDataset,
  isReport,
  isOther,
  l1Categories,
  l2Categories,
  l3Categories,
  selectedPathLabel,
  onL1Change,
  onL2Change,
  init,
  onTagKeydown,
  removeTag,
  submit,
} = editor

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

function goBack() {
  void router.push('/catalog')
}
</script>

<template>
  <div class="product-editor" data-testid="product-editor">
    <header class="editor-header wsc-card">
      <div class="editor-header-left">
        <button type="button" class="editor-back" aria-label="返回" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>
        <h1 class="editor-title">{{ mode === 'create' ? '新增数据产品' : '编辑数据产品' }}</h1>
      </div>
      <div class="editor-header-right">
        <button type="button" class="btn" @click="onCancel">取消</button>
        <button
          type="button"
          class="btn btn-primary"
          :disabled="state === 'submitting' || state === 'loading'"
          @click="onSubmit"
        >
          {{ state === 'submitting' ? '提交中…' : '提交' }}
        </button>
      </div>
    </header>

    <div v-if="state === 'loading'" class="state-card wsc-card">加载中…</div>

    <form v-else class="editor-body" :class="{ wide: isApi }" @submit.prevent="onSubmit">
      <div class="editor-form-card wsc-card">
        <section class="form-section">
          <h2 class="form-section-title">基础信息</h2>
          <div class="form-grid">
            <label class="form-group">
              <span class="form-label">产品名称 <span class="required">*</span></span>
              <input v-model="form.productName" class="form-input" required />
            </label>
            <label class="form-group">
              <span class="form-label">产品编码 <span class="required">*</span></span>
              <input
                v-model="form.productCode"
                class="form-input"
                :readonly="mode === 'edit'"
                required
              />
            </label>
            <label class="form-group">
              <span class="form-label">产品类型</span>
              <select v-model="form.productType" class="form-select" data-testid="product-type">
                <option value="DATASET">数据集</option>
                <option value="REPORT">数据报告</option>
                <option value="API">数据接口</option>
                <option value="OTHER">其他数据产品</option>
              </select>
            </label>
            <div class="form-group full-width category-cascade" data-testid="category-cascade">
              <div class="cascade-row">
                <label class="form-group">
                  <span class="form-label">空间（一级） <span class="required">*</span></span>
                  <select
                    v-model="form.l1CategoryId"
                    class="form-select"
                    required
                    data-testid="cat-l1"
                    @change="onL1Change"
                  >
                    <option disabled value="">请选择</option>
                    <option v-for="c in l1Categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                  </select>
                </label>
                <label class="form-group">
                  <span class="form-label">行业（二级） <span class="required">*</span></span>
                  <select
                    v-model="form.l2CategoryId"
                    class="form-select"
                    required
                    :disabled="!form.l1CategoryId"
                    data-testid="cat-l2"
                    @change="onL2Change"
                  >
                    <option disabled value="">请选择</option>
                    <option v-for="c in l2Categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                  </select>
                </label>
                <label class="form-group">
                  <span class="form-label">子类（三级） <span class="required">*</span></span>
                  <select
                    v-model="form.l3CategoryId"
                    class="form-select"
                    required
                    :disabled="!form.l2CategoryId"
                    data-testid="cat-l3"
                  >
                    <option disabled value="">请选择</option>
                    <option v-for="c in l3Categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                  </select>
                </label>
              </div>
              <p
                v-if="selectedPathLabel"
                class="path-preview"
                data-testid="category-path-preview"
              >
                路径预览：{{ selectedPathLabel }}
              </p>
            </div>
            <label class="form-group">
              <span class="form-label">业务大类</span>
              <input v-model="form.businessCategory" class="form-input" />
            </label>
            <label class="form-group">
              <span class="form-label">业务子类</span>
              <input v-model="form.businessSubCategory" class="form-input" />
            </label>
            <label class="form-group">
              <span class="form-label">数据来源</span>
              <input v-model="form.dataSource" class="form-input" />
            </label>
            <label class="form-group">
              <span class="form-label">更新频率</span>
              <select v-model="form.updateFrequency" class="form-select" data-testid="update-frequency">
                <option
                  v-for="opt in UPDATE_FREQUENCY_OPTIONS"
                  :key="opt.value || 'empty'"
                  :value="opt.value"
                >
                  {{ opt.label }}
                </option>
              </select>
            </label>
            <label class="form-group">
              <span class="form-label">交付方式</span>
              <input v-model="form.deliveryMethod" class="form-input" />
            </label>
            <label class="form-group check">
              <input v-model="form.involvesPersonalInfo" type="checkbox" />
              <span>涉及个人信息</span>
            </label>
            <label class="form-group check">
              <input v-model="form.involvesPublicData" type="checkbox" />
              <span>涉及公共数据</span>
            </label>
            <label class="form-group">
              <span class="form-label">计费方式</span>
              <input v-model="form.billingMethod" class="form-input" />
            </label>
            <label class="form-group">
              <span class="form-label">价格</span>
              <input v-model="form.price" class="form-input" />
            </label>
          </div>
        </section>

        <section class="form-section">
          <h2 class="form-section-title">供应商与产权</h2>
          <div class="form-grid">
            <label class="form-group">
              <span class="form-label">供应商名称</span>
              <input v-model="form.supplierName" class="form-input" />
            </label>
            <label class="form-group">
              <span class="form-label">信用代码</span>
              <input v-model="form.supplierCreditCode" class="form-input" />
            </label>
            <label class="form-group">
              <span class="form-label">产权类型</span>
              <input v-model="form.propertyRightsType" class="form-input" />
            </label>
          </div>
        </section>

        <section class="form-section" data-testid="type-specific-editor">
          <h2 class="form-section-title">数据描述（按类型）</h2>

          <div class="form-grid">
            <label class="form-group">
              <span class="form-label">时间范围</span>
              <input
                v-model="form.timeRange"
                class="form-input"
                placeholder="YYYY/MM/DD - YYYY/MM/DD"
                data-testid="field-time-range"
              />
            </label>
            <label class="form-group">
              <span class="form-label">地域范围</span>
              <input v-model="form.regionScope" class="form-input" data-testid="field-region-scope" />
            </label>
          </div>

          <div v-if="isApi" class="type-block" data-testid="type-api-fields">
            <OpenApiEditorPanel
              v-model:endpoints="form.endpoints"
              v-model:swagger-file-content="form.swaggerFileContent"
              v-model:selected-id="form.selectedEndpointId"
            />
            <div class="form-grid" style="margin-top: 16px">
              <label v-if="form.apiEndpointLegacy && !form.endpoints.length" class="form-group full-width">
                <span class="form-label">旧接口路径（只读兼容）</span>
                <input
                  v-model="form.apiEndpointLegacy"
                  class="form-input"
                  readonly
                  data-testid="legacy-api-endpoint"
                />
                <p class="form-hint">该产品仅有旧 endpoint 字段；可新增端点或从 Swagger 导入迁移。</p>
              </label>
              <label class="form-group full-width">
                <span class="form-label">字段描述</span>
                <textarea
                  v-model="form.apiFieldDescription"
                  class="form-textarea"
                  rows="2"
                  data-testid="api-field-description"
                />
              </label>
              <label class="form-group full-width">
                <span class="form-label">数据样例</span>
                <textarea
                  v-model="form.apiDataSample"
                  class="form-textarea"
                  rows="2"
                  data-testid="api-data-sample"
                />
              </label>
            </div>
          </div>

          <div v-else-if="isDataset" class="type-block form-grid" data-testid="type-dataset-fields">
            <label class="form-group">
              <span class="form-label">数据规模</span>
              <input v-model="form.dataScale" class="form-input" data-testid="dataset-data-scale" />
            </label>
            <label class="form-group">
              <span class="form-label">数据形态</span>
              <select v-model="form.dataForm" class="form-select" data-testid="dataset-data-form">
                <option value="">请选择</option>
                <option v-for="f in DATA_FORM_OPTIONS" :key="f" :value="f">{{ f }}</option>
              </select>
            </label>
            <label class="form-group full-width">
              <span class="form-label">字段描述</span>
              <textarea
                v-model="form.datasetFieldDescription"
                class="form-textarea"
                rows="2"
                data-testid="dataset-field-description"
              />
            </label>
            <label class="form-group full-width">
              <span class="form-label">数据样例</span>
              <textarea
                v-model="form.datasetDataSample"
                class="form-textarea"
                rows="2"
                data-testid="dataset-data-sample"
              />
            </label>
          </div>

          <div
            v-else-if="isReport || isOther"
            class="type-block form-grid"
            :data-testid="isReport ? 'type-report-fields' : 'type-other-fields'"
          >
            <label class="form-group full-width">
              <span class="form-label">数据内容描述</span>
              <textarea
                v-model="form.contentDescription"
                class="form-textarea"
                rows="3"
                data-testid="content-description"
              />
            </label>
          </div>
        </section>

        <section class="form-section">
          <h2 class="form-section-title">标签 / 简介 / 场景</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <span class="form-label">关键标签</span>
              <div class="tags">
                <span v-for="t in form.tags" :key="t" class="tag">
                  {{ t }}
                  <button type="button" aria-label="删除标签" @click="removeTag(t)">×</button>
                </span>
                <input
                  v-model="form.tagInput"
                  class="tag-input"
                  placeholder="输入后回车添加"
                  @keydown="onTagKeydown"
                />
              </div>
              <p class="form-hint">按回车添加标签，点击 × 删除。</p>
            </div>
            <label class="form-group full-width">
              <span class="form-label">简介</span>
              <textarea v-model="form.summary" class="form-textarea" rows="3" />
            </label>
            <label class="form-group full-width">
              <span class="form-label">应用场景</span>
              <textarea v-model="form.scenario" class="form-textarea" rows="3" />
            </label>
          </div>
        </section>

        <p
          v-if="feedback"
          class="feedback"
          :class="state === 'success' ? 'ok' : state === 'error' ? 'err' : ''"
          data-testid="editor-feedback"
        >
          {{ feedback }}
        </p>

        <div class="form-footer">
          <button type="button" class="btn" @click="onCancel">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="state === 'submitting'">
            {{ state === 'submitting' ? '提交中…' : '提交' }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<style scoped>
.product-editor {
  padding: 0 0 40px;
  color: var(--text-primary);
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
}

.editor-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.editor-back {
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

.editor-back:hover {
  background: #f9fafb;
  color: var(--text-primary);
}

.editor-back svg {
  width: 18px;
  height: 18px;
}

.editor-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.editor-header-right {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.state-card {
  padding: 28px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.editor-body {
  max-width: 900px;
}

.editor-body.wide {
  max-width: 1100px;
}

.editor-form-card {
  padding: 24px;
}

.form-section {
  margin-bottom: 28px;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.form-section-title {
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.type-block {
  margin-top: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--text-primary);
  min-width: 0;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group.check {
  flex-direction: row;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.required {
  color: var(--red);
}

.form-input,
.form-select,
.form-textarea {
  padding: 9px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  background: var(--card-bg);
  font: inherit;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: var(--blue);
}

.form-input:read-only {
  background: #f9fafb;
  color: var(--text-secondary);
}

.form-textarea {
  resize: vertical;
  min-height: 88px;
  line-height: 1.6;
}

.cascade-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.path-preview {
  margin: 10px 0 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--blue);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  background: var(--card-bg);
  min-height: 40px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: var(--blue-light);
  color: var(--blue);
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.tag button {
  border: none;
  background: none;
  cursor: pointer;
  color: inherit;
  padding: 0;
  line-height: 1;
}

.tag-input {
  flex: 1;
  min-width: 120px;
  border: none;
  outline: none;
  font: inherit;
  font-size: 13px;
  background: transparent;
  color: var(--text-primary);
}

.form-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.feedback {
  margin: 20px 0 0;
  padding: 10px 12px;
  border-radius: var(--radius-menu);
  font-size: 13px;
  border: 1px solid var(--border-color);
  background: #f9fafb;
  color: var(--text-secondary);
}

.feedback.ok {
  color: var(--green);
  background: var(--green-light);
  border-color: transparent;
}

.feedback.err {
  color: var(--red);
  background: var(--red-light);
  border-color: transparent;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

@media (max-width: 720px) {
  .editor-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid,
  .cascade-row {
    grid-template-columns: 1fr;
  }
}
</style>
