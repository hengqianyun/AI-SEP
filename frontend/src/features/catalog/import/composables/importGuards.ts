import { IMPORT_TEMPLATE_COLUMNS } from '@/api/catalog'

/** 请求级错误码 → 中文提示（态④；含 ERR_IMPORT_TEMPLATE_UNSUPPORTED） */
export const IMPORT_REQUEST_ERROR_HINTS: Record<string, string> = {
  ERR_IMPORT_FILE_TOO_LARGE: '导入文件超过 10MB',
  ERR_IMPORT_FORMAT_INVALID: '无法解析文件或非 xlsx/csv',
  ERR_IMPORT_TEMPLATE_UNSUPPORTED:
    '导入模板不受支持：表头须为 v0729 权威列名行，请下载最新模板后重传',
}

/**
 * 将请求级错误映射为结果区文案；保证错误码出现在反馈中以便态④可测。
 */
export function mapImportRequestError(code: string, serverMessage?: string): string {
  const hint = IMPORT_REQUEST_ERROR_HINTS[code]
  if (hint) return `${hint}（${code}）`
  if (serverMessage) return `${serverMessage}（${code}）`
  return `导入失败（${code}）`
}

/** 解析 CSV 首行表头（足敷 E2E/legacy 无嵌套换行场景）。 */
export function peekCsvHeaders(text: string): string[] | null {
  const first = text
    .replace(/^\uFEFF/, '')
    .split(/\r?\n/)
    .find((l) => l.trim().length > 0)
  if (!first) return null
  return first.split(',').map((h) => h.trim().replace(/^"|"$/g, ''))
}

/** 表头集合是否精确等于 v0729 IMPORT_TEMPLATE_COLUMNS（与后端 matchesV0729 对齐）。 */
export function isV0729HeaderRow(headers: string[]): boolean {
  const normalized = new Set(headers.map((h) => h.trim()).filter(Boolean))
  if (normalized.size !== IMPORT_TEMPLATE_COLUMNS.length) return false
  return IMPORT_TEMPLATE_COLUMNS.every((c) => normalized.has(c))
}

/** 是否像产品导入表头（避免把无表头探测文件误判为 TEMPLATE_UNSUPPORTED）。 */
export function looksLikeImportHeaderRow(headers: string[]): boolean {
  return headers.some(
    (h) =>
      h.includes('产品名称') ||
      h.includes('产品编码') ||
      h.includes('产品类型') ||
      h.includes('行业分类'),
  )
}

export function isCsvFile(file: File): boolean {
  const name = file.name.toLowerCase()
  return name.endsWith('.csv') || file.type === 'text/csv' || file.type === 'application/csv'
}
