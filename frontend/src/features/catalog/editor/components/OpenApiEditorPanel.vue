<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ApiEndpoint } from '@/api/catalog'
import { OpenApiParseError, createEmptyEndpoint, parseOpenApiEndpoints } from '../utils/parseOpenApi'

const endpoints = defineModel<ApiEndpoint[]>('endpoints', { required: true })
const swaggerFileContent = defineModel<string>('swaggerFileContent', { required: true })
const selectedId = defineModel<string>('selectedId', { default: '' })

const emit = defineEmits<{
  importError: [message: string]
  importOk: []
}>()

const swaggerDraft = ref('')
const showImport = ref(false)
const importError = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

const selected = computed(() => endpoints.value.find((e) => e.id === selectedId.value) ?? null)

const METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'] as const

function selectEp(id: string) {
  selectedId.value = id
}

function addEndpoint() {
  const ep = createEmptyEndpoint()
  endpoints.value = [...endpoints.value, ep]
  selectedId.value = ep.id || ''
}

function removeEndpoint(id: string) {
  const next = endpoints.value.filter((e) => e.id !== id)
  endpoints.value = next
  if (selectedId.value === id) {
    selectedId.value = next[0]?.id || ''
  }
}

function addParameter() {
  if (!selected.value) return
  const params = [...(selected.value.parameters || []), { name: '', type: 'string', required: false, description: '' }]
  selected.value.parameters = params
}

function removeParameter(idx: number) {
  if (!selected.value?.parameters) return
  selected.value.parameters = selected.value.parameters.filter((_, i) => i !== idx)
}

function addResponse() {
  if (!selected.value) return
  const responses = [...(selected.value.responses || []), { code: '200', description: '' }]
  selected.value.responses = responses
}

function removeResponse(idx: number) {
  if (!selected.value?.responses) return
  selected.value.responses = selected.value.responses.filter((_, i) => i !== idx)
}

function applySwaggerImport() {
  importError.value = ''
  const text = swaggerDraft.value.trim()
  if (!text) {
    importError.value = '请粘贴或上传 OpenAPI/Swagger 文本'
    emit('importError', importError.value)
    return
  }
  try {
    const parsed = parseOpenApiEndpoints(text)
    swaggerFileContent.value = text
    endpoints.value = parsed
    selectedId.value = parsed[0]?.id || ''
    showImport.value = false
    emit('importOk')
  } catch (e) {
    importError.value = e instanceof OpenApiParseError ? e.message : 'Swagger 解析失败'
    emit('importError', importError.value)
  }
}

function openImport() {
  swaggerDraft.value = swaggerFileContent.value || ''
  importError.value = ''
  showImport.value = true
}

function onFileChange(ev: Event) {
  const input = ev.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    swaggerDraft.value = String(reader.result || '')
  }
  reader.readAsText(file)
  input.value = ''
}
</script>

<template>
  <div class="openapi-panel" data-testid="openapi-editor-panel">
    <div class="openapi-toolbar">
      <h3 class="openapi-title">OpenAPI 接口结构</h3>
      <div class="openapi-actions">
        <button type="button" class="btn btn-sm" data-testid="openapi-import-btn" @click="openImport">
          从 Swagger 导入
        </button>
        <button type="button" class="btn btn-sm btn-primary" data-testid="openapi-add-endpoint" @click="addEndpoint">
          新增端点
        </button>
      </div>
    </div>

    <div v-if="showImport" class="import-box" data-testid="openapi-import-box">
      <p class="import-hint">粘贴 OpenAPI/Swagger JSON 或 YAML，或上传文件。导入后回填端点列表并保留原文。</p>
      <textarea
        v-model="swaggerDraft"
        class="form-textarea import-textarea"
        rows="8"
        placeholder="openapi: 3.0.3 …"
        data-testid="openapi-import-textarea"
      />
      <div class="import-row">
        <input
          ref="fileInputRef"
          type="file"
          accept=".json,.yaml,.yml,.txt,application/json,text/*"
          class="file-input"
          data-testid="openapi-import-file"
          @change="onFileChange"
        />
        <button type="button" class="btn btn-sm" @click="showImport = false">取消</button>
        <button
          type="button"
          class="btn btn-sm btn-primary"
          data-testid="openapi-import-apply"
          @click="applySwaggerImport"
        >
          解析并回填
        </button>
      </div>
      <p v-if="importError" class="import-error" data-testid="openapi-import-error">{{ importError }}</p>
    </div>

    <div class="openapi-layout">
      <aside class="endpoint-sidebar" data-testid="openapi-sidebar">
        <template v-if="endpoints.length">
          <button
            v-for="ep in endpoints"
            :key="ep.id"
            type="button"
            class="endpoint-item"
            :class="{ active: ep.id === selectedId }"
            :data-testid="`openapi-ep-${ep.id}`"
            @click="selectEp(ep.id || '')"
          >
            <span class="method-tag" :data-method="(ep.method || 'GET').toUpperCase()">
              {{ (ep.method || 'GET').toUpperCase() }}
            </span>
            <span class="endpoint-meta">
              <span class="endpoint-summary">{{ ep.summary || '未命名端点' }}</span>
              <span class="endpoint-path">{{ ep.path || '/' }}</span>
            </span>
            <span
              class="endpoint-remove"
              title="删除"
              data-testid="openapi-remove-endpoint"
              @click.stop="removeEndpoint(ep.id || '')"
            >
              ×
            </span>
          </button>
        </template>
        <div v-else class="endpoint-empty" data-testid="openapi-sidebar-empty">
          <p>暂无端点</p>
          <p class="endpoint-empty-hint">请新增端点，或从 Swagger 导入</p>
        </div>
      </aside>

      <div class="endpoint-detail" data-testid="openapi-detail">
        <template v-if="selected">
          <div class="detail-grid">
            <label class="form-group">
              <span class="form-label">摘要</span>
              <input v-model="selected.summary" class="form-input" data-testid="ep-summary" />
            </label>
            <label class="form-group">
              <span class="form-label">方法</span>
              <select v-model="selected.method" class="form-select" data-testid="ep-method">
                <option v-for="m in METHODS" :key="m" :value="m">{{ m }}</option>
              </select>
            </label>
            <label class="form-group full">
              <span class="form-label">路径</span>
              <input v-model="selected.path" class="form-input" data-testid="ep-path" />
            </label>
            <label class="form-group full">
              <span class="form-label">描述</span>
              <textarea v-model="selected.description" class="form-textarea" rows="2" data-testid="ep-description" />
            </label>
          </div>

          <div class="table-block">
            <div class="table-head">
              <h4>参数</h4>
              <button type="button" class="btn btn-sm" data-testid="ep-add-param" @click="addParameter">添加</button>
            </div>
            <table v-if="selected.parameters?.length" class="mini-table">
              <thead>
                <tr>
                  <th>名称</th>
                  <th>类型</th>
                  <th>必填</th>
                  <th>说明</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                <tr v-for="(p, idx) in selected.parameters" :key="idx">
                  <td><input v-model="p.name" class="form-input cell" /></td>
                  <td><input v-model="p.type" class="form-input cell" /></td>
                  <td class="check-cell"><input v-model="p.required" type="checkbox" /></td>
                  <td><input v-model="p.description" class="form-input cell" /></td>
                  <td>
                    <button type="button" class="link-btn" @click="removeParameter(idx)">删</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <p v-else class="table-empty">无参数</p>
          </div>

          <div class="table-block">
            <div class="table-head">
              <h4>响应</h4>
              <button type="button" class="btn btn-sm" data-testid="ep-add-response" @click="addResponse">添加</button>
            </div>
            <table v-if="selected.responses?.length" class="mini-table">
              <thead>
                <tr>
                  <th>状态码</th>
                  <th>说明</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                <tr v-for="(r, idx) in selected.responses" :key="idx">
                  <td><input v-model="r.code" class="form-input cell" /></td>
                  <td><input v-model="r.description" class="form-input cell" /></td>
                  <td>
                    <button type="button" class="link-btn" @click="removeResponse(idx)">删</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <p v-else class="table-empty">无响应项</p>
          </div>

          <div class="detail-grid">
            <label class="form-group full">
              <span class="form-label">请求体 Schema（可选）</span>
              <textarea v-model="selected.requestBodySchema" class="form-textarea mono" rows="3" />
            </label>
            <label class="form-group full">
              <span class="form-label">响应体 Schema（可选）</span>
              <textarea v-model="selected.responseBodySchema" class="form-textarea mono" rows="3" />
            </label>
          </div>
        </template>
        <div v-else class="detail-empty">
          选择左侧端点进行编辑，或新增 / 从 Swagger 导入
        </div>
      </div>
    </div>

    <details v-if="swaggerFileContent" class="swagger-archive">
      <summary>已归档 Swagger 原文（{{ swaggerFileContent.length }} 字符）</summary>
      <pre class="swagger-pre" data-testid="swagger-archive">{{ swaggerFileContent }}</pre>
    </details>
  </div>
</template>

<style scoped>
.openapi-panel {
  margin-top: 8px;
}

.openapi-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.openapi-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.openapi-actions {
  display: flex;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: var(--radius-menu);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-primary);
}

.btn-sm {
  padding: 6px 10px;
  font-size: 12px;
}

.btn-primary {
  background: var(--blue);
  color: #fff;
  border-color: var(--blue);
}

.btn-primary:hover {
  background: #2563eb;
}

.import-box {
  margin-bottom: 14px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  background: #f9fafb;
}

.import-hint {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.import-textarea {
  width: 100%;
  box-sizing: border-box;
}

.import-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
}

.file-input {
  font-size: 12px;
  color: var(--text-secondary);
}

.import-error {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--red);
}

.openapi-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 12px;
  min-height: 280px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  overflow: hidden;
  background: var(--card-bg);
}

.endpoint-sidebar {
  border-right: 1px solid var(--border-color);
  background: #fafafa;
  max-height: 480px;
  overflow: auto;
  padding: 8px;
}

.endpoint-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
  text-align: left;
  padding: 8px;
  margin-bottom: 4px;
  border: 1px solid transparent;
  border-radius: var(--radius-menu);
  background: transparent;
  cursor: pointer;
  color: var(--text-primary);
}

.endpoint-item:hover {
  background: #f3f4f6;
}

.endpoint-item.active {
  background: var(--blue-light);
  border-color: #bfdbfe;
}

.method-tag {
  flex-shrink: 0;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  background: #e5e7eb;
  color: var(--text-secondary);
}

.method-tag[data-method='GET'] {
  background: #dcfce7;
  color: #166534;
}
.method-tag[data-method='POST'] {
  background: #dbeafe;
  color: #1d4ed8;
}
.method-tag[data-method='PUT'],
.method-tag[data-method='PATCH'] {
  background: #ffedd5;
  color: #c2410c;
}
.method-tag[data-method='DELETE'] {
  background: #fee2e2;
  color: #b91c1c;
}

.endpoint-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.endpoint-summary {
  font-size: 12px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.endpoint-path {
  font-size: 11px;
  color: var(--text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.endpoint-remove {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--text-tertiary);
  padding: 0 2px;
}

.endpoint-remove:hover {
  color: var(--red);
}

.endpoint-empty {
  padding: 24px 12px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.endpoint-empty-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.endpoint-detail {
  padding: 14px;
  max-height: 480px;
  overflow: auto;
}

.detail-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-grid .full {
  grid-column: 1 / -1;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  min-width: 0;
}

.form-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
}

.form-input,
.form-select,
.form-textarea {
  padding: 8px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  background: var(--card-bg);
  font: inherit;
  box-sizing: border-box;
  width: 100%;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: var(--blue);
}

.form-textarea.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
}

.table-block {
  margin-bottom: 16px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.table-head h4 {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
}

.mini-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.mini-table th,
.mini-table td {
  border: 1px solid var(--border-color);
  padding: 4px;
  vertical-align: middle;
}

.mini-table th {
  background: #f9fafb;
  font-weight: 600;
  text-align: left;
}

.cell {
  padding: 4px 6px;
  font-size: 12px;
}

.check-cell {
  text-align: center;
}

.link-btn {
  border: none;
  background: none;
  color: var(--blue);
  cursor: pointer;
  font-size: 12px;
}

.table-empty {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.swagger-archive {
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.swagger-pre {
  margin: 8px 0 0;
  padding: 10px;
  max-height: 160px;
  overflow: auto;
  background: #f9fafb;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  font-size: 11px;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 720px) {
  .openapi-layout {
    grid-template-columns: 1fr;
  }

  .endpoint-sidebar {
    border-right: none;
    border-bottom: 1px solid var(--border-color);
    max-height: 200px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
