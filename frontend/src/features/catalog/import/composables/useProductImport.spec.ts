import { describe, expect, it, vi, beforeEach } from 'vitest'
import {
  buildErrorReportCsv,
  classifyImportResult,
  collectImportResultDisplayText,
  containsForbiddenImportDisplay,
  IMPORT_REQUEST_ERROR_HINTS,
  mapImportRequestError,
  MAX_IMPORT_BYTES,
  projectWhitelistErrorRows,
  shouldRenderImportOverlay,
  useProductImport,
} from './useProductImport'
import { ApiError } from '@/api/client'
import { canImportProduct } from '@/features/auth/composables/useCanWrite'

const importProducts = vi.fn()
const getImportErrorReport = vi.fn()

/** 与 `@/api/catalog` IMPORT_TEMPLATE_COLUMNS（v0729）对齐 */
const V0729_COLUMNS = [
  '产品名称（必填）',
  '产品类型（必填）',
  '行业分类（必填）',
  '产品简介（必填）',
  '交付方式',
  '时间范围',
  '地域范围',
  '更新频率',
  '接口定义',
  '字段描述',
  '数据样例',
  '数据规模',
  '数据形态',
  '数据内容描述',
] as const

vi.mock('@/api/catalog', () => ({
  IMPORT_TEMPLATE_COLUMNS: [
    '产品名称（必填）',
    '产品类型（必填）',
    '行业分类（必填）',
    '产品简介（必填）',
    '交付方式',
    '时间范围',
    '地域范围',
    '更新频率',
    '接口定义',
    '字段描述',
    '数据样例',
    '数据规模',
    '数据形态',
    '数据内容描述',
  ],
  getImportTemplateUrl: (format: string) =>
    `/api/v1/catalog/products/import/template?format=${format}`,
  importProducts: (...args: unknown[]) => importProducts(...args),
  getImportErrorReport: (...args: unknown[]) => getImportErrorReport(...args),
}))

describe('useProductImport', () => {
  beforeEach(() => {
    importProducts.mockReset()
    getImportErrorReport.mockReset()
  })

  it('classifies four result states without conflating file-level reject', () => {
    expect(classifyImportResult({ successCount: 3, failureCount: 0 })).toBe('all_success')
    expect(classifyImportResult({ successCount: 2, failureCount: 1, reportId: 'r1' })).toBe(
      'partial_success',
    )
    expect(classifyImportResult({ successCount: 0, failureCount: 4, reportId: 'r2' })).toBe(
      'all_row_failure',
    )
  })

  it('open then close returns to idle upload and invokes onClosed (browse surface)', () => {
    const onClosed = vi.fn()
    const api = useProductImport({ onClosed })
    api.openDialog()
    expect(api.open.value).toBe(true)
    expect(api.phase.value).toBe('upload')
    api.closeDialog()
    expect(api.open.value).toBe(false)
    expect(api.phase.value).toBe('upload')
    expect(api.resultState.value).toBe('idle')
    expect(onClosed).toHaveBeenCalledTimes(1)
  })

  it('client-side oversize maps to file_rejected (state ④)', async () => {
    const api = useProductImport()
    api.openDialog()
    const big = new File([new Uint8Array(MAX_IMPORT_BYTES + 1)], 'oversize.csv', {
      type: 'text/csv',
    })
    api.setFile(big)
    await api.submit()
    expect(api.resultState.value).toBe('file_rejected')
    expect(api.feedback.value).toContain('ERR_IMPORT_FILE_TOO_LARGE')
    expect(importProducts).not.toHaveBeenCalled()
  })

  it('all_success submit shows success title/detail and no report link (态①)', async () => {
    importProducts.mockResolvedValue({
      data: { successCount: 3, failureCount: 0 },
    })
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['ok'], 'full-success.csv', { type: 'text/csv' }))
    await api.submit()
    expect(api.resultState.value).toBe('all_success')
    expect(api.resultTitle.value).toBe('导入全部成功')
    expect(api.resultDetail.value).toContain('成功导入 3')
    expect(api.showReportLink.value).toBe(false)
    expect(getImportErrorReport).not.toHaveBeenCalled()
  })

  it('partial success shows both counts and report entry', async () => {
    importProducts.mockResolvedValue({
      data: { successCount: 2, failureCount: 1, reportId: 'rep-1' },
    })
    getImportErrorReport.mockResolvedValue({
      data: {
        reportId: 'rep-1',
        expiresAt: '2026-08-01T00:00:00Z',
        rows: [
          {
            rowNumber: 3,
            productCode: 'X',
            reasonCode: 'ERR_IMPORT_ROW_INVALID',
            reasonMessage: '缺字段',
          },
        ],
      },
    })
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['a'], 'partial.csv', { type: 'text/csv' }))
    await api.submit()
    expect(api.resultState.value).toBe('partial_success')
    expect(api.showReportLink.value).toBe(true)
    expect(api.resultDetail.value).toContain('成功 2')
    expect(api.resultDetail.value).toContain('失败 1')
    expect(api.resultTitle.value).not.toBe('导入失败')
  })

  it('all_row_failure (态③) keeps report link and is not file_rejected', async () => {
    importProducts.mockResolvedValue({
      data: { successCount: 0, failureCount: 2, reportId: 'rep-all-fail' },
    })
    getImportErrorReport.mockResolvedValue({
      data: {
        reportId: 'rep-all-fail',
        expiresAt: '2026-08-01T00:00:00Z',
        rows: [
          {
            rowNumber: 2,
            productCode: null,
            reasonCode: 'ERR_IMPORT_ROW_INVALID',
            reasonMessage: '缺名称',
          },
        ],
      },
    })
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['a'], 'all-fail.csv', { type: 'text/csv' }))
    await api.submit()
    expect(api.resultState.value).toBe('all_row_failure')
    expect(api.resultTitle.value).toBe('导入行全部失败')
    expect(api.showReportLink.value).toBe(true)
    expect(api.resultDetail.value).toContain('失败 2')
    // 态③ ≠ 态④：无请求级错误码伪装
    expect(api.feedback.value).not.toContain('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
    expect(api.resultState.value).not.toBe('file_rejected')
  })

  it('legacy template → file_rejected + ERR_IMPORT_TEMPLATE_UNSUPPORTED (态④)', async () => {
    importProducts.mockRejectedValue(
      new ApiError(
        '导入模板不受支持：表头须为 v0729 权威列名行',
        'ERR_IMPORT_TEMPLATE_UNSUPPORTED',
        'corr-legacy',
      ),
    )
    const api = useProductImport()
    api.openDialog()
    // 非 CSV 扩展名：走服务端拒绝路径
    api.setFile(new File(['legacy'], 'legacy-template.xlsx', { type: 'application/vnd.ms-excel' }))
    await api.submit()
    expect(api.resultState.value).toBe('file_rejected')
    expect(api.resultTitle.value).toBe('文件被拒绝')
    expect(api.feedback.value).toContain('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
    expect(api.feedback.value).toMatch(/v0729/)
    expect(api.showReportLink.value).toBe(false)
    expect(api.result.value).toBeNull()
  })

  it('CSV legacy headers preflight → file_rejected without calling API (BUG-001)', async () => {
    const legacyCsv = [
      '产品名称,产品编码,产品类型,行业分类,数据来源,更新频率,涉及个人信息,涉及公共数据,交付方式,计费方式,价格',
      '旧模板产品,LEG-OLD-0001,其他数据产品,医疗卫生 / 电子病历 / 脱敏病历,自行生产,每日,否,是,文件传输,按次,面议',
      '',
    ].join('\n')
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File([legacyCsv], 'legacy-template.csv', { type: 'text/csv' }))
    await api.submit()
    expect(importProducts).not.toHaveBeenCalled()
    expect(api.resultState.value).toBe('file_rejected')
    expect(api.feedback.value).toContain('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
    expect(api.showReportLink.value).toBe(false)
    expect(api.result.value).toBeNull()
  })

  it('mapImportRequestError maps TEMPLATE_UNSUPPORTED distinctly from FORMAT_INVALID', () => {
    const unsupported = mapImportRequestError('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
    const format = mapImportRequestError('ERR_IMPORT_FORMAT_INVALID')
    expect(unsupported).toContain('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
    expect(unsupported).toMatch(/v0729/)
    expect(format).toContain('ERR_IMPORT_FORMAT_INVALID')
    expect(format).not.toContain('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
    expect(IMPORT_REQUEST_ERROR_HINTS.ERR_IMPORT_TEMPLATE_UNSUPPORTED).toBeTruthy()
  })

  it('server FORMAT_INVALID stays file_rejected and does not use TEMPLATE_UNSUPPORTED copy', async () => {
    importProducts.mockRejectedValue(
      new ApiError('无法解析文件或非 xlsx/csv', 'ERR_IMPORT_FORMAT_INVALID', 'corr-fmt'),
    )
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['\0\0'], 'bad.bin', { type: 'application/octet-stream' }))
    await api.submit()
    expect(api.resultState.value).toBe('file_rejected')
    expect(api.feedback.value).toContain('ERR_IMPORT_FORMAT_INVALID')
    expect(api.feedback.value).not.toContain('ERR_IMPORT_TEMPLATE_UNSUPPORTED')
  })

  it('server file-level reject stays on result as file_rejected', async () => {
    importProducts.mockRejectedValue(
      new ApiError('导入文件超过 10MB', 'ERR_IMPORT_FILE_TOO_LARGE', 'corr-x'),
    )
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['ok'], 'x.csv', { type: 'text/csv' }))
    await api.submit()
    expect(api.resultState.value).toBe('file_rejected')
    expect(api.feedback.value).toContain('ERR_IMPORT_FILE_TOO_LARGE')
  })

  it('template columns cover v0729 ImportTemplateColumns (no product code column)', () => {
    const api = useProductImport()
    expect([...api.templateColumns]).toEqual([...V0729_COLUMNS])
    expect(api.templateColumns).not.toContain('产品编码')
    expect(api.templateUrl('xlsx')).toContain('/catalog/products/import/template')
  })

  it('buildErrorReportCsv serializes whitelist fields for download (FIND-002)', () => {
    const csv = buildErrorReportCsv({
      reportId: 'rep-1',
      expiresAt: '2026-08-01T00:00:00Z',
      rows: [
        {
          rowNumber: 3,
          productCode: 'X,Y',
          reasonCode: 'ERR_IMPORT_ROW_INVALID',
          reasonMessage: '缺字段',
        },
      ],
    })
    expect(csv).toContain('rowNumber,productCode,reasonCode,reasonMessage')
    expect(csv).toContain('"X,Y"')
    expect(csv).toContain('ERR_IMPORT_ROW_INVALID')
  })

  it('§3.5 whitelist: forbidden fields never appear in result UI / report CSV', async () => {
    const poisonHash = 'a'.repeat(64)
    importProducts.mockResolvedValue({
      data: { successCount: 1, failureCount: 1, reportId: 'rep-sec' },
    })
    getImportErrorReport.mockResolvedValue({
      data: {
        reportId: 'rep-sec',
        expiresAt: '2026-08-01T00:00:00Z',
        rows: [
          {
            rowNumber: 2,
            productCode: 'DEMO-SAFE',
            reasonCode: 'ERR_IMPORT_ROW_INVALID',
            reasonMessage: '缺字段',
            // 后端误带禁止字段（类型外）— 前端不得回显
            creditCode: '91310000MA1KXXXXXX',
            ownerDID: 'did:wsc:owner-secret',
            plainHash: poisonHash,
            supplierCreditCode: '91310000MA1KYYYYYY',
          } as never,
        ],
      },
    })

    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['a'], 'partial.csv', { type: 'text/csv' }))
    await api.submit()

    const display = collectImportResultDisplayText({
      title: api.resultTitle.value,
      detail: api.resultDetail.value,
      successCount: api.result.value?.successCount,
      failureCount: api.result.value?.failureCount,
      reportId: api.result.value?.reportId,
    })
    expect(containsForbiddenImportDisplay(display)).toBe(false)
    expect(display).not.toContain('信用代码')
    expect(display).not.toContain('did:wsc:owner-secret')
    expect(display).not.toContain(poisonHash)

    const projected = projectWhitelistErrorRows(api.report.value!)
    expect(projected[0]).toEqual({
      rowNumber: 2,
      productCode: 'DEMO-SAFE',
      reasonCode: 'ERR_IMPORT_ROW_INVALID',
      reasonMessage: '缺字段',
    })
    expect(Object.keys(projected[0])).toEqual([
      'rowNumber',
      'productCode',
      'reasonCode',
      'reasonMessage',
    ])

    const csv = buildErrorReportCsv(api.report.value!)
    expect(csv).not.toContain('91310000')
    expect(csv).not.toContain('did:wsc')
    expect(csv).not.toContain(poisonHash)
    expect(containsForbiddenImportDisplay(csv)).toBe(false)
  })

  it('downloadErrorReport fetches via getImportErrorReport and returns CSV payload', async () => {
    importProducts.mockResolvedValue({
      data: { successCount: 1, failureCount: 1, reportId: 'rep-dl' },
    })
    getImportErrorReport.mockResolvedValue({
      data: {
        reportId: 'rep-dl',
        expiresAt: '2026-08-01T00:00:00Z',
        rows: [
          {
            rowNumber: 2,
            productCode: 'BAD',
            reasonCode: 'ERR_PRODUCT_CODE_FORMAT',
            reasonMessage: '非法',
          },
        ],
      },
    })
    const api = useProductImport()
    api.openDialog()
    api.setFile(new File(['a'], 'partial.csv', { type: 'text/csv' }))
    await api.submit()
    expect(api.showReportLink.value).toBe(true)

    // clear prefetch so download path re-fetches
    api.report.value = null
    getImportErrorReport.mockClear()
    getImportErrorReport.mockResolvedValue({
      data: {
        reportId: 'rep-dl',
        expiresAt: '2026-08-01T00:00:00Z',
        rows: [
          {
            rowNumber: 2,
            productCode: 'BAD',
            reasonCode: 'ERR_PRODUCT_CODE_FORMAT',
            reasonMessage: '非法',
          },
        ],
      },
    })

    const payload = await api.downloadErrorReport()
    expect(getImportErrorReport).toHaveBeenCalledWith('rep-dl')
    expect(payload).not.toBeNull()
    expect(payload?.filename).toBe('import-error-report-rep-dl.csv')
    expect(payload?.content).toContain('ERR_PRODUCT_CODE_FORMAT')
    expect(payload?.content).toContain('BAD')
  })

  it('权限隐藏：USER 结构不可达；ADMIN/PROVIDER 可挂载弹层', () => {
    expect(canImportProduct('USER')).toBe(false)
    expect(canImportProduct('ADMIN')).toBe(true)
    expect(canImportProduct('PROVIDER')).toBe(true)
    // 对齐 ImportDialog `v-if="open && productImportVisible"`
    expect(shouldRenderImportOverlay(true, canImportProduct('USER'))).toBe(false)
    expect(shouldRenderImportOverlay(true, canImportProduct('ADMIN'))).toBe(true)
    expect(shouldRenderImportOverlay(true, canImportProduct('PROVIDER'))).toBe(true)
    expect(shouldRenderImportOverlay(false, true)).toBe(false)
  })
})
