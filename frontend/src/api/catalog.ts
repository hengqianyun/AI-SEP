/**
 * Catalog API — 对齐 contracts/openapi (wsc-contracts@2.1.0)
 * 三级分类；目录维护条目≡产品；Browse 分页 page/pageSize/total；v0729 同步导入。
 * typeSpecific：api/dataset/report/other；OTHER 允许 contentDescription。
 */
import { apiRequest, getApiBaseUrl } from './client'

export type ProductType = 'DATASET' | 'REPORT' | 'API' | 'OTHER'
export type CategoryLevel = 'L1' | 'L2' | 'L3'
export type MaintenanceStatus = 'PENDING' | 'MAINTAINED'
export type UpdateFrequency =
  | 'REALTIME'
  | 'DAILY'
  | 'WEEKLY'
  | 'MONTHLY'
  | 'YEARLY'
  | 'ON_DEMAND'
  | 'NO_UPDATE'

export type Category = {
  id: string
  name: string
  level: CategoryLevel
  parentId?: string | null
  pathLabels?: string[]
}

export type ApiEndpointParameter = {
  name?: string
  type?: string
  required?: boolean
  description?: string
}

export type ApiEndpointResponse = {
  code?: string
  description?: string
}

export type ApiEndpoint = {
  id?: string
  method?: string
  path?: string
  summary?: string
  description?: string
  parameters?: ApiEndpointParameter[]
  responses?: ApiEndpointResponse[]
  responseBodySchema?: string
  requestBodySchema?: string
}

export type TypeSpecificApi = {
  swaggerFileContent?: string
  endpoints?: ApiEndpoint[]
  fieldDescription?: string
  dataSample?: string
  timeRange?: string
  regionScope?: string
  /** 兼容旧字段；非唯一真源 */
  endpoint?: string
}

export type TypeSpecificDataset = {
  timeRange?: string
  regionScope?: string
  dataScale?: string
  dataForm?: string
  fieldDescription?: string
  dataSample?: string
}

export type TypeSpecificReportOrOther = {
  timeRange?: string
  regionScope?: string
  contentDescription?: string
}

export type TypeSpecificFields = {
  dataset?: TypeSpecificDataset
  report?: TypeSpecificReportOrOther
  api?: TypeSpecificApi
  other?: TypeSpecificReportOrOther
}

export type ProductWrite = {
  productCode: string
  productName: string
  productType: ProductType
  /** V1.1+ 契约权威：挂三级 */
  l3CategoryId?: string
  /**
   * V1.0 兼容：既有 catalog 编辑页仍必填写入。
   * OpenAPI 产品挂载字段为 `l3CategoryId`。
   */
  l2CategoryId: string
  businessCategory?: string
  businessSubCategory?: string
  dataSource?: string
  updateFrequency?: UpdateFrequency | string
  deliveryMethod?: string
  involvesPersonalInfo?: boolean
  involvesPublicData?: boolean
  billingMethod?: string
  price?: string
  supplierName?: string
  supplierCreditCode?: string
  propertyRightsType?: string
  tags?: string[]
  summary?: string
  scenario?: string
  typeSpecific?: TypeSpecificFields
}

export type Product = ProductWrite & {
  id: string
  chainCount: number
  categoryPath?: string
  latestVersionNo?: number
  updatedAt?: string
}

export type ProductPage = {
  items: Product[]
  page: number
  pageSize: number
  total: number
}

export type MaintenanceEntry = {
  id: string
  productCode: string
  productName: string
  maintenanceStatus: MaintenanceStatus
  l3CategoryId?: string | null
  categoryPath?: string | null
}

export type MaintenancePage = {
  items: MaintenanceEntry[]
  page: number
  pageSize: number
  total: number
}

export type ImportResult = {
  successCount: number
  failureCount: number
  reportId?: string | null
}

export type ImportErrorRow = {
  rowNumber: number
  productCode?: string | null
  reasonCode: string
  reasonMessage: string
}

export type ImportErrorReport = {
  reportId: string
  expiresAt: string
  rows: ImportErrorRow[]
}

/** v0729 权威列名行（废止 V1.1 最小列；无产品编码列） */
export const IMPORT_TEMPLATE_COLUMNS = [
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

export function listCategories() {
  return apiRequest<{ items: Category[] }>('/catalog/categories')
}

export function createCategory(body: {
  name: string
  level: CategoryLevel
  parentId?: string
}) {
  return apiRequest<Category>('/catalog/categories', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function updateCategory(
  categoryId: string,
  body: { name: string; level: CategoryLevel; parentId?: string },
) {
  return apiRequest<Category>(`/catalog/categories/${categoryId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function deleteCategory(categoryId: string) {
  return apiRequest<null>(`/catalog/categories/${categoryId}`, { method: 'DELETE' })
}

export function listProducts(query: Record<string, string | number | boolean | undefined> = {}) {
  const qs = new URLSearchParams()
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined && v !== '') qs.set(k, String(v))
  }
  const suffix = qs.toString() ? `?${qs}` : ''
  return apiRequest<ProductPage>(`/catalog/products${suffix}`)
}

export function getProduct(productId: string) {
  return apiRequest<Product>(`/catalog/products/${productId}`)
}

export function createProduct(body: ProductWrite) {
  return apiRequest<Product>('/catalog/products', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function updateProduct(productId: string, body: ProductWrite) {
  return apiRequest<Product>(`/catalog/products/${productId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function listMaintenanceEntries(
  query: Record<string, string | number | undefined> = {},
) {
  const qs = new URLSearchParams()
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined && v !== '') qs.set(k, String(v))
  }
  const suffix = qs.toString() ? `?${qs}` : ''
  return apiRequest<MaintenancePage>(`/catalog/maintenance/entries${suffix}`)
}

export function updateMaintenanceEntry(productId: string, body: { l3CategoryId: string }) {
  return apiRequest<MaintenanceEntry>(`/catalog/maintenance/entries/${productId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function batchUpdateMaintenanceEntries(body: {
  productIds: string[]
  l3CategoryId: string
}) {
  return apiRequest<{
    successCount: number
    failureCount: number
    failures?: { productId: string; reasonCode: string; reasonMessage: string }[]
  }>('/catalog/maintenance/entries/batch', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function getImportTemplateUrl(format: 'xlsx' | 'csv' = 'xlsx') {
  return `${getApiBaseUrl()}/catalog/products/import/template?format=${format}`
}

export function importProducts(file: File) {
  const form = new FormData()
  form.append('file', file)
  return apiRequest<ImportResult>('/catalog/products/import', {
    method: 'POST',
    body: form,
  })
}

export function getImportErrorReport(reportId: string) {
  return apiRequest<ImportErrorReport>(`/catalog/products/import/reports/${reportId}`)
}
