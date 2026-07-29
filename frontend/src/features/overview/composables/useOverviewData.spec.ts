import { describe, expect, it } from 'vitest'
import { STREAM_TYPE_LABEL } from './useOverviewData'

describe('STREAM_TYPE_LABEL', () => {
  it('covers three OVW stream event types in Chinese', () => {
    expect(STREAM_TYPE_LABEL.CATALOG_REGISTER).toBe('目录登记')
    expect(STREAM_TYPE_LABEL.DATA_REGISTER).toBe('数据登记')
    expect(STREAM_TYPE_LABEL.TRADE_ORDER).toBe('交易订单')
  })
})
