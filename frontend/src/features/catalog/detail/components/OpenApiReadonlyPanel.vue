<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ApiEndpoint, TypeSpecificApi } from '@/api/catalog'
import { buildApiDetailView, endpointListLabel } from '../utils/typeSpecificView'

const props = defineProps<{
  api?: TypeSpecificApi | null
}>()

const view = computed(() => buildApiDetailView(props.api))
const selectedId = ref<string | null>(null)

function endpointKey(ep: ApiEndpoint, index: number): string {
  return ep.id?.trim() || `${ep.method || 'GET'}:${ep.path || ''}:${index}`
}

watch(
  () => view.value.endpoints,
  (list) => {
    if (!list.length) {
      selectedId.value = null
      return
    }
    const still = list.some((ep, i) => endpointKey(ep, i) === selectedId.value)
    if (!still) selectedId.value = endpointKey(list[0], 0)
  },
  { immediate: true },
)

const selected = computed(() => {
  const list = view.value.endpoints
  const idx = list.findIndex((ep, i) => endpointKey(ep, i) === selectedId.value)
  return idx >= 0 ? list[idx] : null
})

function selectEndpoint(ep: ApiEndpoint, index: number) {
  selectedId.value = endpointKey(ep, index)
}

function methodClass(method: string | undefined): string {
  const m = (method || 'GET').toUpperCase()
  if (m === 'GET') return 'method-get'
  if (m === 'POST') return 'method-post'
  if (m === 'PUT' || m === 'PATCH') return 'method-put'
  if (m === 'DELETE') return 'method-delete'
  return 'method-other'
}
</script>

<template>
  <div class="openapi-readonly" data-testid="openapi-readonly">
    <div v-if="view.legacyEndpoint" class="legacy-endpoint" data-testid="legacy-endpoint">
      <span class="detail-label">接口地址（兼容）</span>
      <code class="legacy-value">{{ view.legacyEndpoint }}</code>
    </div>

    <div class="doc-info" data-testid="openapi-doc-info">
      <div class="doc-info-row">
        <span class="detail-label">OpenAPI 文档</span>
        <span class="doc-status" :data-has-swagger="view.hasSwagger ? 'true' : 'false'">
          {{ view.hasSwagger ? `已归档原文（${view.swaggerCharCount} 字符）` : '无归档原文' }}
        </span>
      </div>
      <pre
        v-if="view.swaggerPreview"
        class="swagger-preview"
        data-testid="swagger-preview"
      >{{ view.swaggerPreview }}</pre>
    </div>

    <div class="openapi-body">
      <aside class="endpoint-list" data-testid="endpoint-list" aria-label="端点列表">
        <div class="panel-caption">端点列表</div>
        <ul v-if="view.endpoints.length" class="endpoint-ul">
          <li v-for="(ep, i) in view.endpoints" :key="endpointKey(ep, i)">
            <button
              type="button"
              class="endpoint-item"
              :class="{ active: selectedId === endpointKey(ep, i) }"
              :data-testid="`endpoint-item-${i}`"
              @click="selectEndpoint(ep, i)"
            >
              <span class="method-tag" :class="methodClass(ep.method)">
                {{ (ep.method || 'GET').toUpperCase() }}
              </span>
              <span class="endpoint-meta">
                <span class="endpoint-summary">{{ endpointListLabel(ep) }}</span>
                <span class="endpoint-path">{{ ep.path || '—' }}</span>
              </span>
            </button>
          </li>
        </ul>
        <div v-else class="empty-hint" data-testid="endpoint-empty">
          {{ view.legacyEndpoint ? '无结构化端点；已展示兼容接口地址' : '暂无端点' }}
        </div>
      </aside>

      <section class="endpoint-detail" data-testid="endpoint-detail" aria-label="端点详情">
        <div class="panel-caption">端点详情</div>
        <template v-if="selected">
          <div class="detail-grid compact">
            <div class="detail-item">
              <span class="detail-label">摘要</span>
              <span class="detail-value">{{ selected.summary || '—' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">方法</span>
              <span class="detail-value">
                <span class="method-tag" :class="methodClass(selected.method)">
                  {{ (selected.method || 'GET').toUpperCase() }}
                </span>
              </span>
            </div>
            <div class="detail-item wide">
              <span class="detail-label">路径</span>
              <span class="detail-value mono">{{ selected.path || '—' }}</span>
            </div>
          </div>
          <div class="block">
            <div class="detail-label">描述</div>
            <div class="block-body">{{ selected.description || '—' }}</div>
          </div>
          <div class="block" data-testid="endpoint-parameters">
            <div class="detail-label">参数</div>
            <table v-if="selected.parameters?.length" class="mini-table">
              <thead>
                <tr>
                  <th>名称</th>
                  <th>类型</th>
                  <th>必填</th>
                  <th>说明</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(p, pi) in selected.parameters" :key="`${p.name}-${pi}`">
                  <td>{{ p.name || '—' }}</td>
                  <td>{{ p.type || '—' }}</td>
                  <td>{{ p.required ? '是' : '否' }}</td>
                  <td>{{ p.description || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="block-body muted">无参数</div>
          </div>
          <div class="block" data-testid="endpoint-responses">
            <div class="detail-label">响应</div>
            <table v-if="selected.responses?.length" class="mini-table">
              <thead>
                <tr>
                  <th>状态码</th>
                  <th>说明</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(r, ri) in selected.responses" :key="`${r.code}-${ri}`">
                  <td>{{ r.code || '—' }}</td>
                  <td>{{ r.description || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="block-body muted">无响应项</div>
          </div>
          <div v-if="selected.requestBodySchema" class="block">
            <div class="detail-label">请求 Schema</div>
            <pre class="schema-pre">{{ selected.requestBodySchema }}</pre>
          </div>
          <div v-if="selected.responseBodySchema" class="block">
            <div class="detail-label">响应 Schema</div>
            <pre class="schema-pre">{{ selected.responseBodySchema }}</pre>
          </div>
        </template>
        <div v-else class="empty-hint">选择左侧端点查看详情</div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.openapi-readonly {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.legacy-endpoint {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.legacy-value {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
  color: var(--text-primary);
  word-break: break-all;
}

.doc-info {
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.doc-info-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 16px;
}

.doc-status {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.swagger-preview {
  margin: 10px 0 0;
  padding: 10px 12px;
  max-height: 160px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.45;
  font-family: ui-monospace, Consolas, monospace;
  color: var(--text-secondary);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  white-space: pre-wrap;
  word-break: break-word;
}

.openapi-body {
  display: grid;
  grid-template-columns: minmax(200px, 280px) 1fr;
  gap: 12px;
  min-height: 220px;
}

.endpoint-list,
.endpoint-detail {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  background: var(--card-bg);
  padding: 12px;
  min-width: 0;
}

.panel-caption {
  margin: 0 0 10px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
}

.endpoint-ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.endpoint-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
  text-align: left;
  padding: 8px 10px;
  border-radius: var(--radius-menu);
  border: 1px solid transparent;
  background: #f9fafb;
  cursor: pointer;
  color: inherit;
}

.endpoint-item:hover {
  border-color: var(--border-color);
}

.endpoint-item.active {
  border-color: var(--blue);
  background: var(--blue-light);
}

.endpoint-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.endpoint-summary {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
}

.endpoint-path {
  font-size: 11px;
  color: var(--text-tertiary);
  font-family: ui-monospace, Consolas, monospace;
  word-break: break-all;
}

.method-tag {
  flex-shrink: 0;
  display: inline-block;
  min-width: 3.2em;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
  line-height: 1.4;
}

.method-get {
  background: #dbeafe;
  color: #1d4ed8;
}
.method-post {
  background: #dcfce7;
  color: #15803d;
}
.method-put {
  background: #ffedd5;
  color: #c2410c;
}
.method-delete {
  background: #fee2e2;
  color: #b91c1c;
}
.method-other {
  background: #f3f4f6;
  color: #374151;
}

.detail-grid.compact {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 10px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  min-width: 0;
}

.detail-item.wide {
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

.detail-value.mono {
  font-family: ui-monospace, Consolas, monospace;
}

.block {
  margin-bottom: 12px;
}

.block:last-child {
  margin-bottom: 0;
}

.block-body {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.55;
  padding: 8px 10px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
  white-space: pre-wrap;
  word-break: break-word;
}

.mini-table {
  width: 100%;
  margin-top: 6px;
  border-collapse: collapse;
  font-size: 12px;
}

.mini-table th,
.mini-table td {
  border: 1px solid var(--border-color);
  padding: 6px 8px;
  text-align: left;
  vertical-align: top;
}

.mini-table th {
  background: #f9fafb;
  color: var(--text-tertiary);
  font-weight: 600;
}

.schema-pre {
  margin: 6px 0 0;
  padding: 8px 10px;
  max-height: 140px;
  overflow: auto;
  font-size: 12px;
  font-family: ui-monospace, Consolas, monospace;
  background: #f9fafb;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-secondary);
}

.empty-hint,
.muted {
  color: var(--text-tertiary);
  font-size: 13px;
}

@media (max-width: 800px) {
  .openapi-body {
    grid-template-columns: 1fr;
  }
}
</style>
