import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { OPENAPI_SHARED_CRITICAL } from '../utils/openapiSharedExpectations'
import { useProductEditor } from './useProductEditor'

const createProduct = vi.fn()
const getProduct = vi.fn()
const updateProduct = vi.fn()

vi.mock('@/api/catalog', async () => {
  const actual = await vi.importActual<typeof import('@/api/catalog')>('@/api/catalog')
  return {
    ...actual,
    getProduct: (...args: unknown[]) => getProduct(...args),
    createProduct: (...args: unknown[]) => createProduct(...args),
    updateProduct: (...args: unknown[]) => updateProduct(...args),
  }
})

const here = fileURLToPath(new URL('.', import.meta.url))
const sharedYaml = readFileSync(
  resolve(here, '../../../../../../tests/fixtures/import/openapi-shared-minimal.yaml'),
  'utf8',
)

function fillRequired(ed: ReturnType<typeof useProductEditor>) {
  ed.form.productName = '测试产品'
  ed.form.industryCategory = '建筑业'
  if (!ed.form.productCode) {
    ed.form.productCode = 'GEN-NEW-0001'
  }
}

describe('useProductEditor', () => {
  beforeEach(() => {
    createProduct.mockReset()
    getProduct.mockReset()
    updateProduct.mockReset()
    createProduct.mockImplementation(async (body: { industryCategory?: string }) => ({
      data: {
        id: 'prod-new',
        productCode: 'GEN-NEW-0001',
        industryCategory: body.industryCategory,
      },
    }))
  })

  it('create init auto-generates code; onProductTypeChange regenerates', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    expect(ed.form.productCode).toMatch(/^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$/)
    const before = ed.form.productCode
    ed.form.productType = 'API'
    ed.onProductTypeChange()
    expect(ed.form.productCode).toMatch(/^API-SVC-/)
    expect(ed.form.productCode).not.toBe(before)
  })

  it('edit mode keeps productCode on productType change', async () => {
    getProduct.mockResolvedValueOnce({
      data: {
        id: 'prod-x',
        productCode: 'MED-EMR-0001',
        productName: '已有产品',
        productType: 'DATASET',
        industryCategory: '卫生和社会工作',
        chainCount: 1,
        tags: [],
      },
    })
    const ed = useProductEditor('edit')
    await ed.init('prod-x')
    const code = ed.form.productCode
    ed.form.productType = 'API'
    ed.onProductTypeChange()
    expect(ed.form.productCode).toBe(code)
    expect(ed.form.industryCategory).toBe('卫生和社会工作')
  })

  it('blocks submit without name/code and shows error feedback', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.state.value).toBe('error')
    expect(ed.feedback.value).toContain('产品名称')
  })

  it('blocks submit without industryCategory', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productCode = 'GEN-NEW-0001'
    ed.form.productName = '缺行业分类'
    ed.form.productType = 'OTHER'
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.feedback.value).toContain('行业分类')
  })

  it('OTHER type submits typeSpecific.other; industryCategory and success feedback', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    fillRequired(ed)
    ed.form.productType = 'OTHER'
    ed.form.contentDescription = '其他内容描述'
    ed.form.timeRange = '2020/01/01 -'
    expect(ed.isOther.value).toBe(true)
    expect(ed.isApi.value).toBe(false)
    const id = await ed.submit()
    expect(id).toBe('prod-new')
    expect(ed.state.value).toBe('success')
    expect(ed.feedback.value).toContain('第 1 版')
    const body = createProduct.mock.calls[0][0]
    expect(body.industryCategory).toBe('建筑业')
    expect(body.l3CategoryId).toBeUndefined()
    expect(body.typeSpecific.other.contentDescription).toBe('其他内容描述')
    expect(body.typeSpecific.other.timeRange).toBe('2020/01/01 -')
  })

  it('type flags switch with productType', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productType = 'API'
    expect(ed.isApi.value).toBe(true)
    expect(ed.isDataset.value).toBe(false)
    ed.form.productType = 'DATASET'
    expect(ed.isDataset.value).toBe(true)
    expect(ed.isReport.value).toBe(false)
    ed.form.productType = 'REPORT'
    expect(ed.isReport.value).toBe(true)
    ed.form.productType = 'OTHER'
    expect(ed.isOther.value).toBe(true)
  })

  it('add/remove endpoints', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productType = 'API'
    expect(ed.form.endpoints).toHaveLength(0)
    ed.addEndpoint()
    ed.addEndpoint()
    expect(ed.form.endpoints).toHaveLength(2)
    const id = ed.form.endpoints[0]!.id!
    ed.removeEndpoint(id)
    expect(ed.form.endpoints).toHaveLength(1)
  })

  it('applySwaggerFill from shared fixture', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productType = 'API'
    const ok = ed.applySwaggerFill(sharedYaml)
    expect(ok).toBe(true)
    expect(ed.form.endpoints.length).toBeGreaterThan(0)
    expect(ed.form.endpoints[0]?.path).toBe(OPENAPI_SHARED_CRITICAL.path)
  })

  it('DATASET submits dataForm and regionScope', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    fillRequired(ed)
    ed.form.productType = 'DATASET'
    ed.form.dataForm = '表格'
    ed.form.regionScope = '全国'
    ed.form.deliveryMethod = 'FILE'
    await ed.submit()
    const body = createProduct.mock.calls[0][0]
    expect(body.deliveryMethod).toBe('FILE')
    expect(body.typeSpecific.dataset.dataForm).toBe('表格')
    expect(body.typeSpecific.dataset.regionScope).toBe('全国')
  })
})
