import type {
  ApiEndpoint,
  ProductType,
  TypeSpecificApi,
  TypeSpecificFields,
} from '@/api/catalog'
import type { Role } from '@/api/auth'
import { canWriteProduct } from '@/features/auth/composables/useCanWrite'

export type DetailField = {
  key: string
  label: string
  value: string
}

export type ApiDetailView = {
  endpoints: ApiEndpoint[]
  /** 旧单字段；有值即可展示，不要求 endpoints 非空 */
  legacyEndpoint: string | null
  hasSwagger: boolean
  swaggerPreview: string | null
  swaggerCharCount: number
}

const EMPTY = '—'

function text(v: unknown): string {
  if (v == null) return EMPTY
  const s = String(v).trim()
  return s === '' ? EMPTY : s
}

function previewSwagger(raw: string, max = 480): string {
  const trimmed = raw.trim()
  if (trimmed.length <= max) return trimmed
  return `${trimmed.slice(0, max)}…`
}

/** PROVIDER 显示编辑入口（我的数据产品）；USER/ADMIN 结构不可达。REQ-RBAC-001 V1.4 */
export function isDetailEditEntryVisible(role: Role | null | undefined): boolean {
  return canWriteProduct(role)
}

/** 从 typeSpecific.api 派生只读 OpenAPI 视图；仅有旧 endpoint 时不报错。 */
export function buildApiDetailView(api: TypeSpecificApi | undefined | null): ApiDetailView {
  const endpoints = Array.isArray(api?.endpoints) ? api!.endpoints!.filter(Boolean) : []
  const legacy =
    api?.endpoint != null && String(api.endpoint).trim() !== ''
      ? String(api.endpoint).trim()
      : null
  const swagger =
    api?.swaggerFileContent != null && String(api.swaggerFileContent).trim() !== ''
      ? String(api.swaggerFileContent)
      : null
  return {
    endpoints,
    legacyEndpoint: legacy,
    hasSwagger: swagger != null,
    swaggerPreview: swagger != null ? previewSwagger(swagger) : null,
    swaggerCharCount: swagger != null ? swagger.length : 0,
  }
}

/** 按产品类型抽取扩展数据描述字段（REQ-CAT-004 / SNAP-WSC-004）。 */
export function buildTypeSpecificFields(
  productType: ProductType | string | undefined,
  typeSpecific: TypeSpecificFields | Record<string, unknown> | undefined | null,
): DetailField[] {
  const ts = (typeSpecific || {}) as TypeSpecificFields
  switch (productType) {
    case 'API': {
      const a = ts.api || {}
      return [
        { key: 'timeRange', label: '时间范围', value: text(a.timeRange) },
        { key: 'regionScope', label: '地域范围', value: text(a.regionScope) },
        { key: 'fieldDescription', label: '字段描述', value: text(a.fieldDescription) },
        { key: 'dataSample', label: '数据样例', value: text(a.dataSample) },
      ]
    }
    case 'DATASET': {
      const d = ts.dataset || {}
      return [
        { key: 'timeRange', label: '时间范围', value: text(d.timeRange) },
        { key: 'regionScope', label: '地域范围', value: text(d.regionScope) },
        { key: 'dataScale', label: '数据规模', value: text(d.dataScale) },
        { key: 'dataForm', label: '数据形态', value: text(d.dataForm) },
        { key: 'fieldDescription', label: '字段描述', value: text(d.fieldDescription) },
        { key: 'dataSample', label: '数据样例', value: text(d.dataSample) },
      ]
    }
    case 'REPORT': {
      const r = ts.report || {}
      return [
        { key: 'timeRange', label: '时间范围', value: text(r.timeRange) },
        { key: 'regionScope', label: '地域范围', value: text(r.regionScope) },
        { key: 'contentDescription', label: '数据内容描述', value: text(r.contentDescription) },
      ]
    }
    case 'OTHER': {
      const o = ts.other || {}
      return [
        { key: 'timeRange', label: '时间范围', value: text(o.timeRange) },
        { key: 'regionScope', label: '地域范围', value: text(o.regionScope) },
        { key: 'contentDescription', label: '数据内容描述', value: text(o.contentDescription) },
      ]
    }
    default:
      return []
  }
}

export function endpointListLabel(ep: ApiEndpoint): string {
  const summary = ep.summary?.trim()
  if (summary) return summary
  const path = ep.path?.trim()
  if (path) return path
  return '未命名端点'
}
