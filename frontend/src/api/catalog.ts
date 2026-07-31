/**
 * Catalog API — 对齐 contracts/openapi (wsc-contracts@2.0.0)
 * 三级分类；目录维护条目≡产品；Browse 分页 page/pageSize/total；同步导入。
 * OQ-004: productType=OTHER 时无 typeSpecific 专属字段。
 */
import { apiRequest, getApiBaseUrl } from './client'

export type ProductType = 'DATASET' | 'REPORT' | 'API' | 'OTHER'
export type CategoryLevel = 'L1' | 'L2' | 'L3'
export type MaintenanceStatus = 'PENDING' | 'MAINTAINED'

export type Category = {
  id: string
  name: string
  level: CategoryLevel
  parentId?: string | null
  pathLabels?: string[]
}

export type ProductWrite = {
  productCode: string
  productName: string
  productType: ProductType
  /** V1.1 契约权威：挂三级（后续编辑任务切换写入） */
  l3CategoryId?: string
  /**
   * V1.0 兼容：既有 catalog 编辑页仍必填写入。
   * OpenAPI 2.0.0 产品挂载字段为 `l3CategoryId`。
   */
  l2CategoryId: string
  businessCategory?: string
  businessSubCategory?: string
  dataSource?: string
  updateFrequency?: string
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
  typeSpecific?: {
    dataset?: Record<string, unknown>
    report?: Record<string, unknown>
    api?: Record<string, unknown>
  }
}

export type Product = ProductWrite & {
  id: string
  chainCount: number
  categoryPath?: string
  latestVersionNo?: number
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

/** 导入模板列最小集（PLAN-WSC-2.2 §3.1） */
export const IMPORT_TEMPLATE_COLUMNS = [
  '产品名称',
  '产品编码',
  '产品类型',
  '行业分类',
  '数据来源',
  '更新频率',
  '涉及个人信息',
  '涉及公共数据',
  '交付方式',
  '计费方式',
  '价格',
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
