<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { watch } from 'vue'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import { useProductImport } from './composables/useProductImport'

const props = defineProps<{
  modelValue?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  closed: []
}>()

const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { productImportVisible } = useCanWrite(role)

const {
  open,
  phase,
  resultState,
  feedback,
  selectedFile,
  result,
  report,
  submitting,
  resultTitle,
  resultDetail,
  showReportLink,
  openDialog,
  closeDialog,
  resetUpload,
  setFile,
  templateUrl,
  submit,
  downloadErrorReport,
} = useProductImport({
  onClosed: () => {
    emit('update:modelValue', false)
    emit('closed')
  },
})

watch(
  () => props.modelValue,
  (v) => {
    if (v && productImportVisible.value) {
      openDialog()
    } else if (!v && open.value) {
      open.value = false
      resetUpload()
    }
  },
  { immediate: true },
)

function onPick(e: Event) {
  const input = e.target as HTMLInputElement
  setFile(input.files?.[0] ?? null)
}

function onDrop(e: DragEvent) {
  e.preventDefault()
  setFile(e.dataTransfer?.files?.[0] ?? null)
}

function onClose() {
  closeDialog()
}
</script>

<template>
  <!-- 权限：结构不可达（v-if），禁止纯 CSS 藏入口 -->
  <div
    v-if="open && productImportVisible"
    class="import-overlay"
    id="import-modal"
    role="dialog"
    aria-modal="true"
    aria-labelledby="import-modal-title"
  >
    <div class="import-modal">
      <header class="import-header">
        <h3 id="import-modal-title">批量导入产品</h3>
        <button type="button" class="modal-close" aria-label="关闭" @click="onClose">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </header>

      <div class="import-body">
        <section v-if="phase === 'upload' || phase === 'submitting'" id="import-step-upload">
          <div
            class="drop-zone"
            :class="{ busy: submitting }"
            @dragover.prevent
            @drop="onDrop"
          >
            <svg
              class="drop-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              width="40"
              height="40"
              aria-hidden="true"
            >
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="15" />
            </svg>
            <p class="drop-text">
              将 Excel / CSV 拖拽到此处，或
              <label class="file-label">
                点击上传
                <input
                  type="file"
                  accept=".xlsx,.csv,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                  :disabled="submitting"
                  @change="onPick"
                />
              </label>
            </p>
            <p class="hint">请使用 v0729 权威模板（.xlsx / .csv），单文件不超过 10MB</p>
            <p v-if="selectedFile" class="file-name">已选：{{ selectedFile.name }}</p>
            <p v-if="submitting" class="submitting-hint" data-testid="import-submitting">导入中…</p>
          </div>
          <p class="template-row">
            <a
              :href="templateUrl('xlsx')"
              download
              class="template-link"
              data-testid="import-template-xlsx"
            >
              下载 v0729 导入模板（xlsx）
            </a>
            <span class="sep">·</span>
            <a
              :href="templateUrl('csv')"
              download
              class="template-link"
              data-testid="import-template-csv"
            >
              CSV
            </a>
          </p>
          <p v-if="feedback && phase === 'upload'" class="feedback">{{ feedback }}</p>
        </section>

        <section
          v-else
          id="import-step-result"
          class="result-panel"
          :class="`state-${resultState}`"
          :data-result-state="resultState"
          data-testid="import-result"
        >
          <div class="result-icon" aria-hidden="true">
            <span v-if="resultState === 'all_success'" class="icon ok">✓</span>
            <span v-else-if="resultState === 'partial_success'" class="icon warn">!</span>
            <span v-else-if="resultState === 'all_row_failure'" class="icon err">✕</span>
            <span v-else-if="resultState === 'file_rejected'" class="icon reject">⊘</span>
          </div>
          <p class="result-title" data-testid="import-result-title">{{ resultTitle }}</p>
          <p class="result-detail" data-testid="import-result-detail">{{ resultDetail }}</p>
          <p
            v-if="result && resultState !== 'file_rejected'"
            class="counts"
            data-testid="import-counts"
          >
            成功 {{ result.successCount }} / 失败 {{ result.failureCount }}
          </p>
          <!-- §3.5：仅展示 reportId / 行数 / 过期；不渲染原始 payload 或禁止字段 -->
          <p v-if="showReportLink" class="report">
            <a
              href="#"
              data-testid="import-report-link"
              @click.prevent="downloadErrorReport()"
            >
              下载错误报告：{{ result?.reportId }}
            </a>
            <span v-if="report" class="report-meta">
              （{{ report.rows.length }} 行，至 {{ report.expiresAt }}）
            </span>
          </p>
          <p v-if="feedback && resultState === 'file_rejected'" class="feedback">{{ feedback }}</p>
        </section>
      </div>

      <footer class="import-footer">
        <button
          v-if="phase === 'result'"
          type="button"
          class="btn"
          id="import-modal-back-upload"
          :disabled="submitting"
          @click="resetUpload()"
        >
          重新上传
        </button>
        <button
          v-if="phase !== 'result'"
          type="button"
          class="btn primary"
          :disabled="submitting || !selectedFile"
          @click="submit()"
        >
          {{ submitting ? '导入中…' : '开始导入' }}
        </button>
        <button type="button" class="btn" id="import-modal-cancel" @click="onClose">关闭</button>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.import-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  font-family: var(--font-family-sans);
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

.import-modal {
  width: min(520px, 92vw);
  background: var(--card-bg);
  border-radius: 14px;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
}

.import-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border-color);
}

.import-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.modal-close {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.modal-close:hover {
  background: #f3f4f6;
  color: var(--text-primary);
}

.import-body {
  padding: 24px;
  overflow: auto;
}

.drop-zone {
  border: 2px dashed #d1d5db;
  border-radius: var(--radius-card);
  padding: 40px 20px;
  text-align: center;
  transition: border-color 0.2s, background 0.2s;
  background: #fafbfc;
}

.drop-zone:hover {
  border-color: var(--blue);
  background: #f8faff;
}

.drop-zone.busy {
  opacity: 0.75;
  pointer-events: none;
}

.drop-icon {
  color: var(--text-tertiary);
  margin-bottom: 12px;
}

.drop-text {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.file-label {
  color: var(--blue);
  cursor: pointer;
  font-weight: 500;
}

.file-label input {
  display: none;
}

.hint {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 12px;
}

.file-name {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--text-primary);
}

.submitting-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--blue);
  font-weight: 500;
}

.template-row {
  margin: 16px 0 0;
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.template-link {
  color: var(--blue);
  font-weight: 500;
  text-decoration: none;
}

.template-link:hover {
  text-decoration: underline;
}

.sep {
  color: var(--text-tertiary);
}

.feedback {
  margin-top: 12px;
  color: #b91c1c;
  font-size: 13px;
  text-align: center;
}

.result-panel {
  text-align: center;
  padding: 8px 0 4px;
}

.result-icon {
  margin-bottom: 12px;
}

.result-icon .icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  font-size: 22px;
  font-weight: 700;
}

.state-all_success .icon.ok {
  background: var(--green-light);
  color: var(--green);
}

.state-partial_success .icon.warn {
  background: var(--orange-light);
  color: var(--orange);
}

.state-all_row_failure .icon.err {
  background: var(--red-light);
  color: var(--red);
}

.state-file_rejected .icon.reject {
  background: #f3f4f6;
  color: var(--text-secondary);
}

.result-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.result-detail,
.counts,
.report {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.state-partial_success .counts {
  color: var(--text-primary);
  font-weight: 500;
}

.report a {
  color: var(--blue);
  font-weight: 500;
  text-decoration: none;
}

.report a:hover {
  text-decoration: underline;
}

.report-meta {
  color: var(--text-tertiary);
}

.import-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 6px 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-primary);
}

.btn.primary {
  background: var(--blue);
  border-color: var(--blue);
  color: #fff;
}

.btn.primary:hover {
  background: #2563eb;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
