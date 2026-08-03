import { describe, expect, it } from 'vitest'
import { generateProductCode, PRODUCT_CODE_PATTERN } from './productCode'

describe('generateProductCode (DEC-WSC-001)', () => {
  it('maps productType to DOMAIN-FEATURE-NNNN pattern', () => {
    expect(generateProductCode('API')).toMatch(/^API-SVC-[0-9]{4,}$/)
    expect(generateProductCode('DATASET')).toMatch(/^DATA-SET-[0-9]{4,}$/)
    expect(generateProductCode('REPORT')).toMatch(/^RPT-DOC-[0-9]{4,}$/)
    expect(generateProductCode('OTHER')).toMatch(/^GEN-MISC-[0-9]{4,}$/)
  })

  it('always satisfies PRODUCT_CODE_PATTERN', () => {
    for (const type of ['API', 'DATASET', 'REPORT', 'OTHER'] as const) {
      for (let i = 0; i < 5; i++) {
        expect(generateProductCode(type)).toMatch(PRODUCT_CODE_PATTERN)
      }
    }
  })
})
