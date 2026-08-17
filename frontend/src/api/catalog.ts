/**
 * Catalog API — 对齐 contracts/openapi (wsc-contracts@2.3.3)
 * V1.6：enterprise scope；maintenance scope=full|myCatalog；l2-distribution?l1CategoryId=；
 * mine=本企业；ADMIN 产品写/导入本企业；浏览 l1/l2 Cascader；supplierName 独立 query。
 * industryCategory（GB/T 门类）；l3CategoryId 可空；Browse 分页 + mine；v0729 同步导入。
 * typeSpecific：api/dataset/report/other；OTHER 允许 contentDescription。
 */
import { apiRequest, getApiBaseUrl } from './client'

export type ProductType = 'DATASET' | 'REPORT' | 'API' | 'OTHER'
export type CategoryLevel = 'L1' | 'L2' | 'L3'
export type MaintenanceStatus = 'PENDING' | 'MAINTAINED'
/** 目录维护 scope（硬冻结 full | myCatalog） */
export type MaintenanceScope = 'full' | 'myCatalog'
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

/** GB/T 4754 门类中文原文（对齐 v0729 C 列；DEC-WSC-006） */
export const INDUSTRY_CATEGORY_OPTIONS = [
  '农、林、牧、渔业',
  '采矿业',
  '制造业',
  '电力、热力、燃气及水生产和供应业',
  '建筑业',
  '批发和零售业',
  '交通运输、仓储和邮政业',
  '住宿和餐饮业',
  '信息传输、软件和信息技术服务业',
  '金融业',
  '房地产业',
  '租赁和商务服务业',
  '科学研究和技术服务业',
  '水利、环境和公共设施管理业',
  '居民服务、修理和其他服务业',
  '教育',
  '卫生和社会工作',
  '文化、体育和娱乐业',
  '公共管理、社会保障和社会组织',
  '国际组织',
] as const

export type IndustryCategory = (typeof INDUSTRY_CATEGORY_OPTIONS)[number]

export type ProductWrite = {
  productCode: string
  productName: string
  productType: ProductType
  /** 必填：GB/T 4754 门类中文原文 */
  industryCategory: IndustryCategory | string
  /** 可选目录树三级挂载（非行业分类语义） */
  l3CategoryId?: string | null
  /** 可选；有 l3 时可派生 */
  l2CategoryId?: string
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
  /** 创建者 userId（审计/归属字段，非本企业 scope 判定依据）。本企业产品范围见 listProducts 的 mine 参数（enterpriseId 比较，非 create_by-only；REQ-CAT-016）。空/缺失不可被 PROVIDER 冒领 */
  createBy?: string | null
  /** 最近操作人 userId（服务端审计；客户端不得覆盖） */
  updateBy?: string | null
  /** 创建人显示名（优先于 createBy） */
  createByName?: string | null
  /** 操作人显示名（优先于 updateBy） */
  updateByName?: string | null
  updatedAt?: string
}

export type ProductPage = {
  items: Product[]
  page: number
  pageSize: number
  total: number
  /**
   * 当前筛选条件下各 L3 全集条数（含 `__uncategorized__`）。
   * 分页只截断 items，不改变本 map。
   */
  l3Counts?: Record<string, number>
}

export type L2DistributionItem = {
  code: string
  name: string
  count: number
}

export type L2Distribution = {
  /** 真实产品总数（非 Top5 之和） */
  totalProducts: number
  items: L2DistributionItem[]
}

export type L2DistributionQuery = {
  /** 可选 L1 过滤（REQ-CAT-015）；空则全平台 */
  l1CategoryId?: string
}

/** 产品列表 query（对齐 OpenAPI listProducts） */
export type ListProductsQuery = {
  l1CategoryId?: string
  l2CategoryId?: string
  l3CategoryId?: string
  /** 浏览场景弃用；schema 保留 */
  industryCategory?: string
  dataSource?: string
  productType?: ProductType | string
  involvesPublicData?: boolean
  deliveryMethod?: string
  /** 产品名/编码 contains */
  q?: string
  /** 产品字段 supplierName 模糊匹配（与 q 分离；REQ-CAT-012） */
  supplierName?: string
  /** true → 本企业产品（REQ-CAT-016） */
  mine?: boolean
  page?: number
  pageSize?: number
}

export type ListMaintenanceEntriesQuery = {
  /**
   * OpenAPI 必填：full=全量目录维护；myCatalog=我的目录。
   * 既有调用方在 907 接入前可省略（后端默认行为由 905 实现）。
   */
  scope?: MaintenanceScope
  status?: 'ALL' | 'MAINTAINED' | 'PENDING'
  l1CategoryId?: string
  l2CategoryId?: string
  l3CategoryId?: string
  page?: number
  pageSize?: number
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

export function getL2Distribution(query: L2DistributionQuery = {}) {
  const qs = new URLSearchParams()
  if (query.l1CategoryId) qs.set('l1CategoryId', query.l1CategoryId)
  const suffix = qs.toString() ? `?${qs}` : ''
  return apiRequest<L2Distribution>(`/catalog/l2-distribution${suffix}`)
}

export function listProducts(query: ListProductsQuery = {}) {
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

export function deleteProduct(productId: string) {
  return apiRequest<{ deleted?: boolean } | null>(`/catalog/products/${productId}`, {
    method: 'DELETE',
  })
}

export function listMaintenanceEntries(query: ListMaintenanceEntriesQuery) {
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
