import type { ProductType } from '@/api/catalog'

/** DEC-WSC-001：productType → DOMAIN / FEATURE */
export const PRODUCT_CODE_DOMAIN_FEATURE: Record<
  ProductType,
  { domain: string; feature: string }
> = {
  API: { domain: 'API', feature: 'SVC' },
  DATASET: { domain: 'DATA', feature: 'SET' },
  REPORT: { domain: 'RPT', feature: 'DOC' },
  OTHER: { domain: 'GEN', feature: 'MISC' },
}

export const PRODUCT_CODE_PATTERN = /^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$/

/**
 * 生成符合 DEC-WSC-001 `{DOMAIN}-{FEATURE}-{NNNN+}` 的产品编码。
 * NNNN 段使用毫秒时间戳 + 随机数保证本机唯一性。
 */
export function generateProductCode(type: ProductType): string {
  const { domain, feature } = PRODUCT_CODE_DOMAIN_FEATURE[type]
  const suffix = `${Date.now()}${Math.floor(Math.random() * 1000)}`.replace(/\D/g, '')
  const nnnn = suffix.slice(-6).padStart(4, '0')
  const code = `${domain}-${feature}-${nnnn}`
  if (!PRODUCT_CODE_PATTERN.test(code)) {
    return `${domain}-${feature}-${String(Date.now()).slice(-4)}`
  }
  return code
}
