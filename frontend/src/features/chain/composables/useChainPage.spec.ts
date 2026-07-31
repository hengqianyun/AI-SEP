import { describe, expect, it } from 'vitest'
import {
  chainEmptyMessage,
  formatCategoryPath,
  isRetryableListState,
} from './useChainPage'

describe('CHAIN page empty / error helpers', () => {
  it('empty message is explicit (§3.5)', () => {
    expect(chainEmptyMessage()).toBe('该产品暂无上链记录')
  })

  it('error state is retryable', () => {
    expect(isRetryableListState('error')).toBe(true)
    expect(isRetryableListState('empty')).toBe(false)
    expect(isRetryableListState('loading')).toBe(false)
    expect(isRetryableListState('ready')).toBe(false)
  })

  it('formatCategoryPath prefers full path then parts', () => {
    expect(formatCategoryPath('A / B / C', { l1: 'x', l2: 'y', l3: 'z' })).toBe('A / B / C')
    expect(formatCategoryPath('', { l1: '空间', l2: '行业', l3: '子类' })).toBe(
      '空间 / 行业 / 子类',
    )
    expect(formatCategoryPath(null, { l1: '仅一级' })).toBe('仅一级')
  })
})
