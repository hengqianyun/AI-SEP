import { computed, ref } from 'vue'
import {
  getImportErrorReport,
  getImportTemplateUrl,
  importProducts,
  IMPORT_TEMPLATE_COLUMNS,
  type ImportErrorReport,
  type ImportResult,
} from '@/api/catalog'
import { ApiError } from '@/api/client'
import {
  isCsvFile,
  isV0729HeaderRow,
  looksLikeImportHeaderRow,
  mapImportRequestError,
  peekCsvHeaders,
} from './importGuards'

// 再导出供单测 / 调用方；勿再 import 未在本文件使用的符号（TS6133 / BUG-WSC-304-005）
export {
  IMPORT_REQUEST_ERROR_HINTS,
  isV0729HeaderRow,
  looksLikeImportHeaderRow,
  mapImportRequestError,
  peekCsvHeaders,
} from './importGuards'

/** §3.4 / §3.6 导入结果四态（PLAN-WSC-4.1；含旧模板文件级拒绝） */
export type ImportUiPhase = 'upload' | 'submitting' | 'result'
export type ImportResultState =
  | 'idle'
  | 'all_success'
  | 'partial_success'
  | 'all_row_failure'
  | 'file_rejected'

export const MAX_IMPORT_BYTES = 10 * 1024 * 1024

/**
 * 与 ImportDialog 模板 `v-if="open && productImportVisible"` 对齐：
 * 结构不可达（非纯 CSS 隐藏）。USER 时 productImportVisible=false → 不挂载。
 */
export function shouldRenderImportOverlay(
  open: boolean,
  productImportVisible: boolean,
): boolean {
  return open && productImportVisible
}

export function classifyImportResult(result: ImportResult): Exclude<ImportResultState, 'idle' | 'file_rejected'> {
  const ok = result.successCount ?? 0
  const fail = result.failureCount ?? 0
  if (ok > 0 && fail === 0) return 'all_success'
  if (ok > 0 && fail > 0) return 'partial_success'
  return 'all_row_failure'
}

/** §3.5 前端展示白名单字段（禁止信用代码 / ownerDID / 明文哈希）。 */
export const IMPORT_DISPLAY_WHITELIST = [
  'rowNumber',
  'productCode',
  'reasonCode',
  'reasonMessage',
  'successCount',
  'failureCount',
  'reportId',
] as const

/**
 * 从错误报告行投影白名单字段（§3.5）；不透传禁止键（信用代码/ownerDID/明文哈希）。
 */
export function projectWhitelistErrorRows(
  report: ImportErrorReport,
): Array<{
  rowNumber: number
  productCode?: string | null
  reasonCode: string
  reasonMessage: string
}> {
  return report.rows.map((row) => ({
    rowNumber: row.rowNumber,
    productCode: row.productCode ?? null,
    reasonCode: row.reasonCode,
    reasonMessage: row.reasonMessage,
  }))
}

/** 负例辅助：检测文本是否含 §3.5 禁止展示片段。 */
export function containsForbiddenImportDisplay(text: string): boolean {
  const patterns = [
    /信用代码/,
    /ownerDID/i,
    /ownerDid/,
    /supplierCreditCode/i,
    /plainHash/i,
    /contentHash/i,
    /[0-9A-Fa-f]{64}/, // 明文哈希形态（SHA-256 hex）
  ]
  return patterns.some((re) => re.test(text))
}

/** 将 getImportErrorReport 白名单行序列化为可下载 CSV（FIND-002 / §3.5）。 */
export function buildErrorReportCsv(report: ImportErrorReport): string {
  const escape = (v: string | number | null | undefined) => {
    const s = v == null ? '' : String(v)
    if (/[",\n\r]/.test(s)) return `"${s.replace(/"/g, '""')}"`
    return s
  }
  const header = 'rowNumber,productCode,reasonCode,reasonMessage'
  const lines = projectWhitelistErrorRows(report).map(
    (r) =>
      `${escape(r.rowNumber)},${escape(r.productCode)},${escape(r.reasonCode)},${escape(r.reasonMessage)}`,
  )
  return [header, ...lines].join('\n')
}

/** 结果区可展示文案拼接（仅计数 / 标题 / 详情 / reportId），供负例断言。 */
export function collectImportResultDisplayText(parts: {
  title: string
  detail: string
  successCount?: number
  failureCount?: number
  reportId?: string | null
}): string {
  return [
    parts.title,
    parts.detail,
    parts.successCount != null ? `成功 ${parts.successCount}` : '',
    parts.failureCount != null ? `失败 ${parts.failureCount}` : '',
    parts.reportId ? `reportId=${parts.reportId}` : '',
  ]
    .filter(Boolean)
    .join('\n')
}

export function triggerBrowserDownload(content: string, filename: string, mime: string) {
  if (typeof document === 'undefined' || typeof URL === 'undefined' || typeof Blob === 'undefined') {
    return
  }
  const blob = new Blob([content], { type: mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.rel = 'noopener'
  a.click()
  URL.revokeObjectURL(url)
}

export function useProductImport(options?: { onClosed?: () => void }) {
  const open = ref(false)
  const phase = ref<ImportUiPhase>('upload')
  const resultState = ref<ImportResultState>('idle')
  const feedback = ref('')
  const selectedFile = ref<File | null>(null)
  const result = ref<ImportResult | null>(null)
  const report = ref<ImportErrorReport | null>(null)
  const submitting = computed(() => phase.value === 'submitting')

  const resultTitle = computed(() => {
    switch (resultState.value) {
      case 'all_success':
        return '导入全部成功'
      case 'partial_success':
        return '部分导入成功'
      case 'all_row_failure':
        return '导入行全部失败'
      case 'file_rejected':
        return '文件被拒绝'
      default:
        return ''
    }
  })

  const resultDetail = computed(() => {
    if (resultState.value === 'file_rejected') {
      return feedback.value
    }
    if (!result.value) return ''
    const { successCount, failureCount } = result.value
    if (resultState.value === 'partial_success') {
      return `成功 ${successCount} 条，失败 ${failureCount} 条。请下载错误报告后修正并重传。`
    }
    if (resultState.value === 'all_success') {
      return `成功导入 ${successCount} 条产品，可关闭或重新上传。`
    }
    return `失败 ${failureCount} 条。请下载错误报告后修正并重传。`
  })

  const showReportLink = computed(
    () =>
      !!result.value?.reportId &&
      (resultState.value === 'partial_success' || resultState.value === 'all_row_failure'),
  )

  function openDialog() {
    resetUpload()
    open.value = true
  }

  function closeDialog() {
    open.value = false
    resetUpload()
    options?.onClosed?.()
  }

  function resetUpload() {
    phase.value = 'upload'
    resultState.value = 'idle'
    feedback.value = ''
    selectedFile.value = null
    result.value = null
    report.value = null
  }

  function setFile(file: File | null) {
    selectedFile.value = file
    feedback.value = ''
  }

  function templateUrl(format: 'xlsx' | 'csv' = 'xlsx') {
    return getImportTemplateUrl(format)
  }

  async function submit() {
    const file = selectedFile.value
    if (!file) {
      feedback.value = '请先选择 xlsx/csv 文件'
      return
    }
    if (file.size > MAX_IMPORT_BYTES) {
      phase.value = 'result'
      resultState.value = 'file_rejected'
      feedback.value = mapImportRequestError('ERR_IMPORT_FILE_TOO_LARGE')
      result.value = null
      return
    }

    // CSV 表头预检：旧模板/非 v0729 → 态④（不依赖后端是否已部署 301）
    if (isCsvFile(file)) {
      try {
        const text = await file.text()
        const headers = peekCsvHeaders(text)
        if (
          headers &&
          looksLikeImportHeaderRow(headers) &&
          !isV0729HeaderRow(headers)
        ) {
          phase.value = 'result'
          resultState.value = 'file_rejected'
          feedback.value = mapImportRequestError('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
          result.value = null
          report.value = null
          return
        }
      } catch {
        // 读失败则交由服务端判定
      }
    }

    phase.value = 'submitting'
    feedback.value = ''
    try {
      const res = await importProducts(file)
      result.value = res.data
      resultState.value = classifyImportResult(res.data)
      phase.value = 'result'
      if (res.data.reportId) {
        try {
          const rep = await getImportErrorReport(res.data.reportId)
          report.value = rep.data
        } catch {
          report.value = null
        }
      }
    } catch (e) {
      phase.value = 'result'
      result.value = null
      report.value = null
      resultState.value = 'file_rejected'
      if (e instanceof ApiError) {
        // 态④：旧模板 / 超限 / 格式非法等请求级错误，不得伪装成行级结果态
        feedback.value = mapImportRequestError(e.code, e.message)
      } else {
        feedback.value = '导入失败，请稍后重试'
      }
    }
  }

  /**
   * 对齐 OpenAPI getImportErrorReport：拉取（或复用 prefetch）后触发 CSV 下载。
   * 关闭 FIND-002「@click.prevent 空操作」。
   */
  async function downloadErrorReport(): Promise<{ filename: string; content: string } | null> {
    const reportId = result.value?.reportId
    if (!reportId || !showReportLink.value) return null

    let data = report.value
    if (!data || data.reportId !== reportId) {
      try {
        const rep = await getImportErrorReport(reportId)
        data = rep.data
        report.value = data
      } catch (e) {
        if (e instanceof ApiError) {
          feedback.value = `${e.message}（${e.code}）`
        } else {
          feedback.value = '错误报告下载失败，请稍后重试'
        }
        return null
      }
    }

    const content = buildErrorReportCsv(data)
    const filename = `import-error-report-${data.reportId}.csv`
    triggerBrowserDownload(content, filename, 'text/csv;charset=utf-8')
    return { filename, content }
  }

  return {
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
    templateColumns: IMPORT_TEMPLATE_COLUMNS,
    openDialog,
    closeDialog,
    resetUpload,
    setFile,
    templateUrl,
    submit,
    downloadErrorReport,
  }
}
