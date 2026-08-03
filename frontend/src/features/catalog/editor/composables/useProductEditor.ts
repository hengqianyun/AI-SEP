import { computed, reactive, ref } from 'vue'
import {
  createProduct,
  getProduct,
  listCategories,
  updateProduct,
  type ApiEndpoint,
  type Category,
  type ProductType,
  type ProductWrite,
  type UpdateFrequency,
} from '@/api/catalog'
import {
  OpenApiParseError,
  assertSwaggerParsable,
  createEmptyEndpoint,
  parseOpenApiEndpoints,
} from '../utils/parseOpenApi'
import { generateProductCode } from '../utils/productCode'

export type EditorMode = 'create' | 'edit'
export type FormState = 'idle' | 'loading' | 'ready' | 'submitting' | 'success' | 'error'

const CODE_RE = /^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$/

export const UPDATE_FREQUENCY_OPTIONS: { value: UpdateFrequency | ''; label: string }[] = [
  { value: '', label: '（未选）' },
  { value: 'REALTIME', label: '实时' },
  { value: 'DAILY', label: '每日' },
  { value: 'WEEKLY', label: '每周' },
  { value: 'MONTHLY', label: '每月' },
  { value: 'YEARLY', label: '每年' },
  { value: 'ON_DEMAND', label: '按需' },
  { value: 'NO_UPDATE', label: '不更新' },
]

export const DATA_FORM_OPTIONS = ['图片', '文本', '视频', '音频', '表格', '其他'] as const

function deriveLegacyEndpoint(endpoints: ApiEndpoint[]): string {
  const first = endpoints[0]
  if (!first) return ''
  const method = (first.method || '').toUpperCase()
  const path = first.path || ''
  return [method, path].filter(Boolean).join(' ')
}

function cloneEndpoints(eps: ApiEndpoint[]): ApiEndpoint[] {
  return eps.map((e) => ({
    ...e,
    id: e.id || createEmptyEndpoint().id,
    parameters: e.parameters ? e.parameters.map((p) => ({ ...p })) : [],
    responses: e.responses ? e.responses.map((r) => ({ ...r })) : [],
  }))
}

export function useProductEditor(mode: EditorMode) {
  const state = ref<FormState>('idle')
  const feedback = ref('')
  const categories = ref<Category[]>([])
  const form = reactive({
    productCode: '',
    productName: '',
    productType: 'DATASET' as ProductType,
    l1CategoryId: '',
    l2CategoryId: '',
    l3CategoryId: '',
    businessCategory: '',
    businessSubCategory: '',
    dataSource: '',
    updateFrequency: '' as UpdateFrequency | string,
    deliveryMethod: '',
    involvesPersonalInfo: false,
    involvesPublicData: false,
    billingMethod: '',
    price: '',
    supplierName: '',
    supplierCreditCode: '',
    propertyRightsType: '',
    summary: '',
    scenario: '',
    tags: [] as string[],
    tagInput: '',
    timeRange: '',
    regionScope: '',
    swaggerFileContent: '',
    endpoints: [] as ApiEndpoint[],
    selectedEndpointId: '',
    apiFieldDescription: '',
    apiDataSample: '',
    apiEndpointLegacy: '',
    dataScale: '',
    dataForm: '',
    datasetFieldDescription: '',
    datasetDataSample: '',
    contentDescription: '',
  })

  const isOther = computed(() => form.productType === 'OTHER')
  const isApi = computed(() => form.productType === 'API')
  const isDataset = computed(() => form.productType === 'DATASET')
  const isReport = computed(() => form.productType === 'REPORT')

  const l1Categories = computed(() => categories.value.filter((c) => c.level === 'L1'))
  const l2Categories = computed(() =>
    categories.value.filter((c) => c.level === 'L2' && c.parentId === form.l1CategoryId),
  )
  const l3Categories = computed(() =>
    categories.value.filter((c) => c.level === 'L3' && c.parentId === form.l2CategoryId),
  )
  const selectedPathLabel = computed(() => {
    const parts: string[] = []
    const l1 = categories.value.find((c) => c.id === form.l1CategoryId)
    const l2 = categories.value.find((c) => c.id === form.l2CategoryId)
    const l3 = categories.value.find((c) => c.id === form.l3CategoryId)
    if (l1) parts.push(l1.name)
    if (l2) parts.push(l2.name)
    if (l3) parts.push(l3.name)
    return parts.join(' / ')
  })

  function onL1Change() {
    form.l2CategoryId = ''
    form.l3CategoryId = ''
  }

  function onL2Change() {
    form.l3CategoryId = ''
  }

  function resolveParentsFromL3(l3Id: string) {
    const l3 = categories.value.find((c) => c.id === l3Id && c.level === 'L3')
    if (!l3) {
      form.l3CategoryId = l3Id
      return
    }
    form.l3CategoryId = l3.id
    form.l2CategoryId = String(l3.parentId || '')
    const l2 = categories.value.find((c) => c.id === form.l2CategoryId)
    form.l1CategoryId = String(l2?.parentId || '')
  }

  function refreshAutoCode() {
    if (mode === 'create') {
      form.productCode = generateProductCode(form.productType)
    }
  }

  function onProductTypeChange() {
    resetTypeSpecific()
    refreshAutoCode()
  }

  function resetTypeSpecific() {
    form.timeRange = ''
    form.regionScope = ''
    form.swaggerFileContent = ''
    form.endpoints = []
    form.selectedEndpointId = ''
    form.apiFieldDescription = ''
    form.apiDataSample = ''
    form.apiEndpointLegacy = ''
    form.dataScale = ''
    form.dataForm = ''
    form.datasetFieldDescription = ''
    form.datasetDataSample = ''
    form.contentDescription = ''
  }

  function loadTypeSpecific(ts?: {
    api?: Record<string, unknown>
    dataset?: Record<string, unknown>
    report?: Record<string, unknown>
    other?: Record<string, unknown>
  }) {
    resetTypeSpecific()
    if (!ts) return
    if (form.productType === 'API' && ts.api) {
      const api = ts.api
      form.swaggerFileContent = String(api.swaggerFileContent ?? '')
      form.apiFieldDescription = String(api.fieldDescription ?? '')
      form.apiDataSample = String(api.dataSample ?? '')
      form.timeRange = String(api.timeRange ?? '')
      form.regionScope = String(api.regionScope ?? '')
      form.apiEndpointLegacy = String(api.endpoint ?? '')
      const eps = Array.isArray(api.endpoints) ? (api.endpoints as ApiEndpoint[]) : []
      form.endpoints = cloneEndpoints(eps)
      form.selectedEndpointId = form.endpoints[0]?.id || ''
    } else if (form.productType === 'DATASET' && ts.dataset) {
      const d = ts.dataset
      form.timeRange = String(d.timeRange ?? '')
      form.regionScope = String(d.regionScope ?? '')
      form.dataScale = String(d.dataScale ?? (d.recordCount != null ? String(d.recordCount) : ''))
      form.dataForm = String(d.dataForm ?? '')
      form.datasetFieldDescription = String(d.fieldDescription ?? '')
      form.datasetDataSample = String(d.dataSample ?? '')
    } else if (form.productType === 'REPORT' && ts.report) {
      const r = ts.report
      form.timeRange = String(r.timeRange ?? '')
      form.regionScope = String(r.regionScope ?? '')
      form.contentDescription = String(
        r.contentDescription ?? (r.pageCount != null ? `页数：${r.pageCount}` : ''),
      )
    } else if (form.productType === 'OTHER' && ts.other) {
      const o = ts.other
      form.timeRange = String(o.timeRange ?? '')
      form.regionScope = String(o.regionScope ?? '')
      form.contentDescription = String(o.contentDescription ?? '')
    }
  }

  async function init(productId?: string) {
    state.value = 'loading'
    feedback.value = ''
    try {
      const catRes = await listCategories()
      categories.value = catRes.data.items
      if (mode === 'edit' && productId) {
        const res = await getProduct(productId)
        const p = res.data
        form.productCode = p.productCode
        form.productName = p.productName
        form.productType = p.productType
        if (p.l3CategoryId) {
          resolveParentsFromL3(p.l3CategoryId)
        } else if (p.l2CategoryId) {
          form.l2CategoryId = p.l2CategoryId
          const l2 = categories.value.find((c) => c.id === p.l2CategoryId)
          form.l1CategoryId = String(l2?.parentId || '')
          form.l3CategoryId = ''
        }
        form.businessCategory = String(p.businessCategory || '')
        form.businessSubCategory = String(p.businessSubCategory || '')
        form.dataSource = String(p.dataSource || '')
        form.updateFrequency = String(p.updateFrequency || '')
        form.deliveryMethod = String(p.deliveryMethod || '')
        form.involvesPersonalInfo = Boolean(p.involvesPersonalInfo)
        form.involvesPublicData = Boolean(p.involvesPublicData)
        form.billingMethod = String(p.billingMethod || '')
        form.price = String(p.price || '')
        form.supplierName = String(p.supplierName || '')
        form.supplierCreditCode = String(p.supplierCreditCode || '')
        form.propertyRightsType = String(p.propertyRightsType || '')
        form.summary = String(p.summary || '')
        form.scenario = String(p.scenario || '')
        form.tags = [...(p.tags || [])]
        loadTypeSpecific(p.typeSpecific as Parameters<typeof loadTypeSpecific>[0])
      } else {
        refreshAutoCode()
      }
      state.value = 'ready'
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '加载失败'
    }
  }

  function addTag() {
    const t = form.tagInput.trim()
    if (!t) return
    if (!form.tags.includes(t)) form.tags.push(t)
    form.tagInput = ''
  }

  function removeTag(t: string) {
    form.tags = form.tags.filter((x) => x !== t)
  }

  function onTagKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault()
      addTag()
    }
  }

  function addEndpoint() {
    const ep = createEmptyEndpoint()
    form.endpoints.push(ep)
    form.selectedEndpointId = ep.id || ''
  }

  function removeEndpoint(id: string) {
    form.endpoints = form.endpoints.filter((e) => e.id !== id)
    if (form.selectedEndpointId === id) {
      form.selectedEndpointId = form.endpoints[0]?.id || ''
    }
  }

  /** Swagger 导入回填：保留原文至 swaggerFileContent，替换 endpoints */
  function applySwaggerFill(text: string): boolean {
    const trimmed = text.trim()
    if (!trimmed) {
      feedback.value = '请粘贴或上传 OpenAPI/Swagger 文本'
      state.value = 'error'
      return false
    }
    try {
      const parsed = parseOpenApiEndpoints(trimmed)
      form.swaggerFileContent = trimmed
      form.endpoints = parsed
      form.selectedEndpointId = parsed[0]?.id || ''
      if (state.value === 'error') state.value = 'ready'
      feedback.value = ''
      return true
    } catch (e) {
      feedback.value = e instanceof OpenApiParseError ? e.message : 'Swagger 解析失败'
      state.value = 'error'
      return false
    }
  }

  function buildBody(): ProductWrite | null {
    if (!form.productName.trim() || !form.productCode.trim()) {
      feedback.value = '请填写产品名称与产品编码'
      state.value = 'error'
      return null
    }
    if (!CODE_RE.test(form.productCode.trim())) {
      feedback.value = '产品编码格式非法，须符合 {DOMAIN}-{FEATURE}-{NNNN}'
      state.value = 'error'
      return null
    }
    if (!form.l3CategoryId) {
      feedback.value = '请选择三级分类（空间 / 行业 / 子类）'
      state.value = 'error'
      return null
    }

    if (form.productType === 'API' && form.swaggerFileContent.trim()) {
      try {
        assertSwaggerParsable(form.swaggerFileContent)
      } catch (e) {
        feedback.value =
          e instanceof OpenApiParseError
            ? e.message
            : 'Swagger 原文解析失败，请修正或清空后再提交'
        state.value = 'error'
        return null
      }
    }

    const body: ProductWrite = {
      productCode: form.productCode.trim(),
      productName: form.productName.trim(),
      productType: form.productType,
      l3CategoryId: form.l3CategoryId,
      l2CategoryId: form.l2CategoryId,
      businessCategory: form.businessCategory || undefined,
      businessSubCategory: form.businessSubCategory || undefined,
      dataSource: form.dataSource || undefined,
      updateFrequency: form.updateFrequency || undefined,
      deliveryMethod: form.deliveryMethod || undefined,
      involvesPersonalInfo: form.involvesPersonalInfo,
      involvesPublicData: form.involvesPublicData,
      billingMethod: form.billingMethod || undefined,
      price: form.price || undefined,
      supplierName: form.supplierName || undefined,
      supplierCreditCode: form.supplierCreditCode || undefined,
      propertyRightsType: form.propertyRightsType || undefined,
      summary: form.summary || undefined,
      scenario: form.scenario || undefined,
      tags: [...form.tags],
    }

    if (form.productType === 'API') {
      const legacy =
        form.apiEndpointLegacy.trim() || deriveLegacyEndpoint(form.endpoints) || undefined
      body.typeSpecific = {
        api: {
          swaggerFileContent: form.swaggerFileContent || undefined,
          endpoints: form.endpoints.map((e) => ({
            id: e.id,
            method: e.method,
            path: e.path,
            summary: e.summary,
            description: e.description,
            parameters: e.parameters?.map((p) => ({ ...p })),
            responses: e.responses?.map((r) => ({ ...r })),
            requestBodySchema: e.requestBodySchema,
            responseBodySchema: e.responseBodySchema,
          })),
          fieldDescription: form.apiFieldDescription || undefined,
          dataSample: form.apiDataSample || undefined,
          timeRange: form.timeRange || undefined,
          regionScope: form.regionScope || undefined,
          endpoint: legacy,
        },
      }
    } else if (form.productType === 'DATASET') {
      body.typeSpecific = {
        dataset: {
          timeRange: form.timeRange || undefined,
          regionScope: form.regionScope || undefined,
          dataScale: form.dataScale || undefined,
          dataForm: form.dataForm || undefined,
          fieldDescription: form.datasetFieldDescription || undefined,
          dataSample: form.datasetDataSample || undefined,
        },
      }
    } else if (form.productType === 'REPORT') {
      body.typeSpecific = {
        report: {
          timeRange: form.timeRange || undefined,
          regionScope: form.regionScope || undefined,
          contentDescription: form.contentDescription || undefined,
        },
      }
    } else if (form.productType === 'OTHER') {
      body.typeSpecific = {
        other: {
          timeRange: form.timeRange || undefined,
          regionScope: form.regionScope || undefined,
          contentDescription: form.contentDescription || undefined,
        },
      }
    }
    return body
  }

  async function submit(productId?: string): Promise<string | null> {
    const body = buildBody()
    if (!body) return null
    state.value = 'submitting'
    feedback.value = ''
    try {
      const res =
        mode === 'create' ? await createProduct(body) : await updateProduct(productId!, body)
      state.value = 'success'
      feedback.value = mode === 'create' ? '产品已创建并产生第 1 版上链' : '产品已更新并追加上链版本'
      return res.data.id
    } catch (e) {
      state.value = 'error'
      feedback.value = e instanceof Error ? e.message : '提交失败'
      return null
    }
  }

  return {
    state,
    feedback,
    form,
    isOther,
    isApi,
    isDataset,
    isReport,
    l1Categories,
    l2Categories,
    l3Categories,
    selectedPathLabel,
    onL1Change,
    onL2Change,
    onProductTypeChange,
    init,
    addTag,
    removeTag,
    onTagKeydown,
    addEndpoint,
    removeEndpoint,
    applySwaggerFill,
    buildBody,
    submit,
    UPDATE_FREQUENCY_OPTIONS,
    DATA_FORM_OPTIONS,
  }
}
