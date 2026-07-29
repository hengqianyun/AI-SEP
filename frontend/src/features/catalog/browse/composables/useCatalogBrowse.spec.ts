import { describe, expect, it } from 'vitest'
import {
  catalogEmptyMessage,
  createDefaultFilters,
  isRetryableListState,
} from './useCatalogBrowse'
import { productTypeLabel } from '../utils/labels'
import { truncateSensitiveId } from '../utils/truncateSensitive'

describe('catalog browse helpers (REQ-CAT-001..003 / §3.5)', () => {
  it('empty message is explicit and stable (testid catalog-empty)', () => {
    expect(catalogEmptyMessage()).toContain('未找到符合条件的数据产品')
  })

  it('error state is retryable', () => {
    expect(isRetryableListState('error')).toBe(true)
    expect(isRetryableListState('empty')).toBe(false)
    expect(isRetryableListState('loading')).toBe(false)
    expect(isRetryableListState('ready')).toBe(false)
  })

  it('default filters are empty (全部)', () => {
    const f = createDefaultFilters()
    expect(f.l1CategoryId).toBe('')
    expect(f.productType).toBe('')
    expect(f.q).toBe('')
  })

  it('product type labels cover four types', () => {
    expect(productTypeLabel('DATASET')).toBe('数据集')
    expect(productTypeLabel('REPORT')).toBe('数据报告')
    expect(productTypeLabel('API')).toBe('数据接口')
    expect(productTypeLabel('OTHER')).toBe('其他数据产品')
  })
})

describe('§3.6 sensitive id truncate (browse)', () => {
  it('truncates long credit code with ellipsis', () => {
    const code = '91310000MA1KXXXX1AEXTRA'
    const shown = truncateSensitiveId(code)
    expect(shown).toContain('…')
    expect(shown.length).toBeLessThan(code.length)
  })
})
