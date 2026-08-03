import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { OPENAPI_SHARED_CRITICAL } from '../utils/openapiSharedExpectations'
import { useProductEditor } from './useProductEditor'

const createProduct = vi.fn()
const getProduct = vi.fn()
const updateProduct = vi.fn()

vi.mock('@/api/catalog', () => ({
  listCategories: vi.fn(async () => ({
    data: {
      items: [
        { id: 'cat-l1-health', name: '医疗卫生', level: 'L1', parentId: null },
        { id: 'cat-l2-emr', name: '电子病历', level: 'L2', parentId: 'cat-l1-health' },
        { id: 'cat-l3-emr-desense', name: '脱敏病历', level: 'L3', parentId: 'cat-l2-emr' },
        { id: 'cat-l3-emr-struct', name: '结构化病历', level: 'L3', parentId: 'cat-l2-emr' },
      ],
    },
  })),
  getProduct: (...args: unknown[]) => getProduct(...args),
  createProduct: (...args: unknown[]) => createProduct(...args),
  updateProduct: (...args: unknown[]) => updateProduct(...args),
}))

const here = fileURLToPath(new URL('.', import.meta.url))
const sharedYaml = readFileSync(
  resolve(here, '../../../../../../tests/fixtures/import/openapi-shared-minimal.yaml'),
  'utf8',
)

function fillRequired(ed: ReturnType<typeof useProductEditor>) {
  ed.form.productName = '测试产品'
  ed.form.l1CategoryId = 'cat-l1-health'
  ed.form.l2CategoryId = 'cat-l2-emr'
  ed.form.l3CategoryId = 'cat-l3-emr-desense'
  if (!ed.form.productCode) {
    ed.form.productCode = 'GEN-NEW-0001'
  }
}

describe('useProductEditor', () => {
  beforeEach(() => {
    createProduct.mockReset()
    getProduct.mockReset()
    updateProduct.mockReset()
    createProduct.mockImplementation(async (body: { l3CategoryId?: string }) => ({
      data: { id: 'prod-new', productCode: 'GEN-NEW-0001', l3CategoryId: body.l3CategoryId },
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
        l3CategoryId: 'cat-l3-emr-desense',
        l2CategoryId: 'cat-l2-emr',
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
  })

  it('blocks submit without name/code and shows error feedback', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.state.value).toBe('error')
    expect(ed.feedback.value).toContain('产品名称')
  })

  it('blocks submit without L3 category', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productCode = 'GEN-NEW-0001'
    ed.form.productName = '缺三级'
    ed.form.productType = 'OTHER'
    ed.form.l1CategoryId = 'cat-l1-health'
    ed.form.l2CategoryId = 'cat-l2-emr'
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.feedback.value).toContain('三级分类')
  })

  it('OTHER type submits typeSpecific.other; cascades L3 and success feedback', async () => {
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
    expect(body.typeSpecific.other.contentDescription).toBe('其他内容描述')
    expect(body.typeSpecific.other.timeRange).toBe('2020/01/01 -')
  })

  it('changing L1 clears L2/L3', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.l1CategoryId = 'cat-l1-health'
    ed.form.l2CategoryId = 'cat-l2-emr'
    ed.form.l3CategoryId = 'cat-l3-emr-desense'
    ed.form.l1CategoryId = 'other-l1'
    ed.onL1Change()
    expect(ed.form.l2CategoryId).toBe('')
    expect(ed.form.l3CategoryId).toBe('')
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
    const id0 = ed.form.endpoints[0].id!
    ed.removeEndpoint(id0)
    expect(ed.form.endpoints).toHaveLength(1)
    expect(ed.form.endpoints[0].id).not.toBe(id0)
  })

  it('Swagger fill from shared fixture matches 301 critical fields', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productType = 'API'
    const ok = ed.applySwaggerFill(sharedYaml)
    expect(ok).toBe(true)
    expect(ed.form.swaggerFileContent).toContain('openapi')
    expect(ed.form.endpoints[0].method).toBe(OPENAPI_SHARED_CRITICAL.method)
    expect(ed.form.endpoints[0].path).toBe(OPENAPI_SHARED_CRITICAL.path)
    expect(ed.form.endpoints[0].summary).toBe(OPENAPI_SHARED_CRITICAL.summary)
  })

  it('blocks submit when non-empty swagger is garbage', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    fillRequired(ed)
    ed.form.productType = 'API'
    ed.form.swaggerFileContent = 'not-valid-openapi {{{'
    ed.addEndpoint()
    const id = await ed.submit()
    expect(id).toBeNull()
    expect(ed.state.value).toBe('error')
    expect(ed.feedback.value).toMatch(/解析失败|非法/)
    expect(createProduct).not.toHaveBeenCalled()
  })

  it('API submit payload has swaggerFileContent + endpoints shape', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    fillRequired(ed)
    ed.form.productType = 'API'
    ed.form.timeRange = '1990/01/01 -'
    ed.form.regionScope = '全国'
    ed.applySwaggerFill(sharedYaml)
    const id = await ed.submit()
    expect(id).toBe('prod-new')
    const body = createProduct.mock.calls[0][0]
    expect(body.productType).toBe('API')
    expect(body.typeSpecific.api.swaggerFileContent).toContain('openapi')
    expect(body.typeSpecific.api.endpoints[0].method).toBe(OPENAPI_SHARED_CRITICAL.method)
    expect(body.typeSpecific.api.endpoints[0].path).toBe(OPENAPI_SHARED_CRITICAL.path)
    expect(body.typeSpecific.api.endpoints[0].summary).toBe(OPENAPI_SHARED_CRITICAL.summary)
    expect(body.typeSpecific.api.timeRange).toBe('1990/01/01 -')
    expect(body.typeSpecific.api.endpoint).toContain('POST')
  })

  it('DATASET / REPORT payload shape sampling', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    fillRequired(ed)
    ed.form.productType = 'DATASET'
    ed.form.dataScale = '1万条'
    ed.form.dataForm = '表格'
    ed.form.updateFrequency = 'NO_UPDATE'
    let body = ed.buildBody()!
    expect(body.typeSpecific?.dataset?.dataScale).toBe('1万条')
    expect(body.typeSpecific?.dataset?.dataForm).toBe('表格')
    expect(body.updateFrequency).toBe('NO_UPDATE')

    ed.form.productType = 'REPORT'
    ed.form.contentDescription = '报告正文'
    body = ed.buildBody()!
    expect(body.typeSpecific?.report?.contentDescription).toBe('报告正文')
    expect(body.typeSpecific?.dataset).toBeUndefined()
  })

  it('loads legacy api.endpoint without crash; endpoints may be empty', async () => {
    getProduct.mockResolvedValue({
      data: {
        id: 'p1',
        productCode: 'GEN-OLD-0001',
        productName: '旧接口产品',
        productType: 'API',
        l3CategoryId: 'cat-l3-emr-desense',
        l2CategoryId: 'cat-l2-emr',
        chainCount: 1,
        typeSpecific: { api: { endpoint: 'GET /legacy' } },
      },
    })
    const ed = useProductEditor('edit')
    await ed.init('p1')
    expect(ed.state.value).toBe('ready')
    expect(ed.form.apiEndpointLegacy).toBe('GET /legacy')
    expect(ed.form.endpoints).toEqual([])
    expect(ed.form.swaggerFileContent).toBe('')
  })

  it('applySwaggerFill rejects garbage without wiping prior endpoints', async () => {
    const ed = useProductEditor('create')
    await ed.init()
    ed.form.productType = 'API'
    ed.addEndpoint()
    const before = ed.form.endpoints.length
    const ok = ed.applySwaggerFill('{{{bad')
    expect(ok).toBe(false)
    expect(ed.form.endpoints).toHaveLength(before)
  })
})
