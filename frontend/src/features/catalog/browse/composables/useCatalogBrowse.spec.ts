import { describe, expect, it } from 'vitest'
import {
  catalogEmptyMessage,
  createDefaultFilters,
  groupProductsByL3,
  isRetryableListState,
  resolveIndustryQuery,
} from './useCatalogBrowse'
import type { Category, Product } from '@/api/catalog'
import { productTypeLabel } from '../utils/labels'
import { truncateSensitiveId } from '../utils/truncateSensitive'

describe('catalog browse helpers (REQ-CAT-001..003 / TASK-WSC-104)', () => {
  it('empty message is explicit and stable (testid catalog-empty)', () => {
    expect(catalogEmptyMessage()).toContain('未找到符合条件的数据产品')
  })

  it('error state is retryable', () => {
    expect(isRetryableListState('error')).toBe(true)
    expect(isRetryableListState('empty')).toBe(false)
    expect(isRetryableListState('loading')).toBe(false)
    expect(isRetryableListState('ready')).toBe(false)
  })

  it('default filters include industryFilterId (L2/L3)', () => {
    const f = createDefaultFilters()
    expect(f.l1CategoryId).toBe('')
    expect(f.l2CategoryId).toBe('')
    expect(f.industryFilterId).toBe('')
    expect(f.productType).toBe('')
    expect(f.q).toBe('')
  })

  it('product type labels cover four types', () => {
    expect(productTypeLabel('DATASET')).toBe('数据集')
    expect(productTypeLabel('REPORT')).toBe('数据报告')
    expect(productTypeLabel('API')).toBe('数据接口')
    expect(productTypeLabel('OTHER')).toBe('其他数据产品')
  })

  it('resolveIndustryQuery maps L2 and L3 filter ids', () => {
    const cats: Category[] = [
      { id: 'l1', name: '空间', level: 'L1' },
      { id: 'l2', name: '行业', level: 'L2', parentId: 'l1' },
      { id: 'l3', name: '子类', level: 'L3', parentId: 'l2' },
    ]
    expect(resolveIndustryQuery('', cats)).toEqual({
      l2CategoryId: undefined,
      l3CategoryId: undefined,
    })
    expect(resolveIndustryQuery('l2', cats)).toEqual({
      l2CategoryId: 'l2',
      l3CategoryId: undefined,
    })
    expect(resolveIndustryQuery('l3', cats)).toEqual({
      l2CategoryId: undefined,
      l3CategoryId: 'l3',
    })
  })

  it('groupProductsByL3 sections by subcategory with counts', () => {
    const cats: Category[] = [
      { id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' },
      { id: 'l3b', name: '结构化病历', level: 'L3', parentId: 'l2' },
    ]
    const products = [
      { id: '1', l3CategoryId: 'l3a', productName: 'A1' },
      { id: '2', l3CategoryId: 'l3a', productName: 'A2' },
      { id: '3', l3CategoryId: 'l3b', productName: 'B1' },
    ] as Product[]
    const sections = groupProductsByL3(products, cats)
    expect(sections).toHaveLength(2)
    expect(sections[0]).toMatchObject({ l3CategoryId: 'l3a', title: '脱敏病历', count: 2 })
    expect(sections[1]).toMatchObject({ l3CategoryId: 'l3b', title: '结构化病历', count: 1 })
  })

  it('groupProductsByL3 preserves filter semantics for load-more append order', () => {
    const cats: Category[] = [{ id: 'l3a', name: '脱敏病历', level: 'L3', parentId: 'l2' }]
    const page1 = [{ id: '1', l3CategoryId: 'l3a' }] as Product[]
    const page2 = [
      { id: '1', l3CategoryId: 'l3a' },
      { id: '2', l3CategoryId: 'l3a' },
    ] as Product[]
    expect(groupProductsByL3(page1, cats)[0]!.count).toBe(1)
    expect(groupProductsByL3(page2, cats)[0]!.count).toBe(2)
    expect(groupProductsByL3(page2, cats)[0]!.products.every((p) => p.l3CategoryId === 'l3a')).toBe(
      true,
    )
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
