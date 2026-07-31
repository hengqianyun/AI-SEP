import { computed, reactive, ref } from 'vue'
import {
  createProduct,
  getProduct,
  listCategories,
  updateProduct,
  type Category,
  type ProductType,
  type ProductWrite,
} from '@/api/catalog'

export type EditorMode = 'create' | 'edit'
export type FormState = 'idle' | 'loading' | 'ready' | 'submitting' | 'success' | 'error'

const CODE_RE = /^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$/

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
    updateFrequency: '',
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
    datasetRecordCount: '',
    reportPageCount: '',
    apiEndpoint: '',
  })

  const isOther = computed(() => form.productType === 'OTHER')
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
          // 过渡：仅有 L2 时预填空间/行业，须再选手类
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
        const ts = (p as { typeSpecific?: Record<string, Record<string, unknown>> }).typeSpecific
        form.datasetRecordCount = String(ts?.dataset?.recordCount ?? '')
        form.reportPageCount = String(ts?.report?.pageCount ?? '')
        form.apiEndpoint = String(ts?.api?.endpoint ?? '')
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
    if (form.productType === 'OTHER') {
      // OQ-004: 不携带 typeSpecific
    } else if (form.productType === 'DATASET') {
      body.typeSpecific = {
        dataset: { recordCount: Number(form.datasetRecordCount) || 0 },
      }
    } else if (form.productType === 'REPORT') {
      body.typeSpecific = {
        report: { pageCount: Number(form.reportPageCount) || 0 },
      }
    } else if (form.productType === 'API') {
      body.typeSpecific = {
        api: { endpoint: form.apiEndpoint || '' },
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
    l1Categories,
    l2Categories,
    l3Categories,
    selectedPathLabel,
    onL1Change,
    onL2Change,
    init,
    addTag,
    removeTag,
    onTagKeydown,
    submit,
  }
}
