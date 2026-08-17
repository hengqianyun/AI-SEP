import type { ProductType } from '@/api/catalog'

export const PRODUCT_TYPE_LABELS: Record<ProductType, string> = {
  DATASET: '数据集',
  REPORT: '数据报告',
  API: '数据接口',
  OTHER: '其他数据产品',
}

export const DATA_SOURCE_OPTIONS = [
  { value: '', label: '全部来源' },
  { value: 'SELF_PRODUCED', label: '自行生产' },
  { value: 'PUBLIC_COLLECT', label: '公开收集' },
  { value: 'AGREEMENT', label: '协议取得' },
  { value: 'DERIVED', label: '衍生创造' },
] as const

export const PRODUCT_TYPE_OPTIONS = [
  { value: '', label: '全部类型' },
  { value: 'DATASET', label: '数据集' },
  { value: 'REPORT', label: '数据报告' },
  { value: 'API', label: '数据接口' },
  { value: 'OTHER', label: '其他数据产品' },
] as const

export const DELIVERY_OPTIONS = [
  { value: '', label: '全部交付方式' },
  { value: 'API', label: 'API' },
  { value: 'FILE', label: '文件传输' },
  { value: 'SANDBOX', label: '数据沙箱' },
  { value: 'PRIVACY_COMPUTE', label: '隐私保护计算' },
] as const

export const PUBLIC_DATA_OPTIONS = [
  { value: '', label: '公共数据：全部' },
  { value: 'true', label: '涉及公共数据' },
  { value: 'false', label: '不涉及公共数据' },
] as const

/** REQ-CAT-014：Cascader 标签与占位（业务视图 → 业务大类） */
export const BROWSE_CATEGORY_CASCADER_LABEL = '业务视图 / 业务大类'
export const BROWSE_CATEGORY_CASCADER_PLACEHOLDER = '请选择业务视图 / 业务大类'
export const BROWSE_L1_LABEL = '业务视图'
export const BROWSE_L2_LABEL = '业务大类'
export const BROWSE_SUPPLIER_NAME_LABEL = '企业名称'
export const BROWSE_SUPPLIER_NAME_PLACEHOLDER = '按企业名称模糊搜索'

export const CATALOG_EMPTY_MESSAGE = '未找到符合条件的数据产品，请调整筛选或搜索关键词'

/** 我的数据产品空态（与公共目录可区分，UX-002） */
export const MINE_EMPTY_MESSAGE = '暂无本人创建的数据产品，可通过新增或批量导入添加'
export const CATALOG_ERROR_MESSAGE = '加载目录失败，请重试'

export function productTypeLabel(type: string | undefined): string {
  if (!type) return '—'
  return PRODUCT_TYPE_LABELS[type as ProductType] ?? type
}
