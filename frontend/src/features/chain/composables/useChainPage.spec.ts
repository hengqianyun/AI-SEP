import { describe, expect, it } from 'vitest'
import { chainEmptyMessage, isRetryableListState } from './useChainPage'

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
})
