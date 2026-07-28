/**
 * Catalog API — 对齐 contracts/openapi (wsc-contracts@1.1.0)
 * OQ-004: productType=OTHER 时无 typeSpecific 专属字段。
 */
import { apiRequest } from './client'

export type ProductType = 'DATASET' | 'REPORT' | 'API' | 'OTHER'

export type Category = {
  id: string
  name: string
  level: 'L1' | 'L2'
  parentId?: string | null
}

export type ProductWrite = {
  productCode: string
  productName: string
  productType: ProductType
  l2CategoryId: string
  typeSpecific?: {
    dataset?: Record<string, unknown>
    report?: Record<string, unknown>
    api?: Record<string, unknown>
  }
  [key: string]: unknown
}

export type Product = ProductWrite & {
  id: string
  chainCount: number
  categoryPath?: string
  latestVersionNo?: number
}

export function listCategories() {
  return apiRequest<{ items: Category[] }>('/catalog/categories')
}

export function createCategory(body: { name: string; level: 'L1' | 'L2'; parentId?: string }) {
  return apiRequest<Category>('/catalog/categories', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function updateCategory(
  categoryId: string,
  body: { name: string; level: 'L1' | 'L2'; parentId?: string },
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
  return apiRequest<{
    items: Product[]
    page: number
    pageSize: number
    total: number
  }>(`/catalog/products${suffix}`)
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
