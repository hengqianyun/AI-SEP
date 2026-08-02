import { describe, expect, it } from 'vitest'
import type { TypeSpecificApi } from '@/api/catalog'
import {
  buildApiDetailView,
  buildTypeSpecificFields,
  endpointListLabel,
  isDetailEditEntryVisible,
} from './typeSpecificView'

describe('buildTypeSpecificFields — 各类型只读扩展字段', () => {
  it('API：时间/地域/字段描述/样例', () => {
    const fields = buildTypeSpecificFields('API', {
      api: {
        timeRange: '2024/01/01 - 2024/12/31',
        regionScope: '全国',
        fieldDescription: 'code\tstring\t编码',
        dataSample: '{"ok":true}',
      },
    })
    const map = Object.fromEntries(fields.map((f) => [f.key, f.value]))
    expect(map.timeRange).toContain('2024')
    expect(map.regionScope).toBe('全国')
    expect(map.fieldDescription).toContain('code')
    expect(map.dataSample).toContain('ok')
  })

  it('DATASET：规模/形态等', () => {
    const fields = buildTypeSpecificFields('DATASET', {
      dataset: {
        dataScale: '10万条',
        dataForm: '表格',
        fieldDescription: 'id\tint\t主键',
        dataSample: 'id,name',
        timeRange: '2023/01/01 -',
        regionScope: '华东',
      },
    })
    expect(fields.find((f) => f.key === 'dataScale')?.value).toBe('10万条')
    expect(fields.find((f) => f.key === 'dataForm')?.value).toBe('表格')
    expect(fields.find((f) => f.key === 'regionScope')?.value).toBe('华东')
  })

  it('REPORT / OTHER：内容描述与时间地域（OQ-004）', () => {
    const report = buildTypeSpecificFields('REPORT', {
      report: { contentDescription: '年度报告摘要', timeRange: '2024', regionScope: '本市' },
    })
    expect(report.find((f) => f.key === 'contentDescription')?.value).toBe('年度报告摘要')

    const other = buildTypeSpecificFields('OTHER', {
      other: { contentDescription: '其他描述', timeRange: '', regionScope: '国际' },
    })
    expect(other.find((f) => f.key === 'contentDescription')?.value).toBe('其他描述')
    expect(other.find((f) => f.key === 'timeRange')?.value).toBe('—')
    expect(other.find((f) => f.key === 'regionScope')?.value).toBe('国际')
  })
})

describe('buildApiDetailView — OpenAPI / 旧 endpoint 兼容', () => {
  it('展示 endpoints 关键字段与 swagger 文档信息', () => {
    const api: TypeSpecificApi = {
      swaggerFileContent: 'openapi: 3.0.3\ninfo:\n  title: demo\n',
      endpoints: [
        {
          id: 'ep-1',
          method: 'POST',
          path: '/enterprise/security/verify',
          summary: '企业安全信息核验',
        },
      ],
    }
    const view = buildApiDetailView(api)
    expect(view.endpoints).toHaveLength(1)
    expect(view.endpoints[0].method).toBe('POST')
    expect(view.endpoints[0].path).toBe('/enterprise/security/verify')
    expect(view.endpoints[0].summary).toContain('核验')
    expect(view.hasSwagger).toBe(true)
    expect(view.swaggerPreview).toContain('openapi')
    expect(view.legacyEndpoint).toBeNull()
  })

  it('仅有旧 endpoint 时可读且不报错', () => {
    const view = buildApiDetailView({ endpoint: 'GET /legacy/v1/ping' })
    expect(view.endpoints).toEqual([])
    expect(view.legacyEndpoint).toBe('GET /legacy/v1/ping')
    expect(view.hasSwagger).toBe(false)
  })

  it('空 api 块安全降级', () => {
    const view = buildApiDetailView(undefined)
    expect(view.endpoints).toEqual([])
    expect(view.legacyEndpoint).toBeNull()
    expect(view.hasSwagger).toBe(false)
  })
})

describe('isDetailEditEntryVisible — 编辑入口角色', () => {
  it('ADMIN / PROVIDER 可见，USER 不可见', () => {
    expect(isDetailEditEntryVisible('ADMIN')).toBe(true)
    expect(isDetailEditEntryVisible('PROVIDER')).toBe(true)
    expect(isDetailEditEntryVisible('USER')).toBe(false)
    expect(isDetailEditEntryVisible(null)).toBe(false)
  })
})

describe('endpointListLabel', () => {
  it('优先 summary，其次 path', () => {
    expect(endpointListLabel({ summary: '核验', path: '/a' })).toBe('核验')
    expect(endpointListLabel({ path: '/b' })).toBe('/b')
    expect(endpointListLabel({})).toBe('未命名端点')
  })
})
