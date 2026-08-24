/**
 * 合约附件上传 composable — 文件校验、上传进度、交易信息表单、扫描失败提示。
 * REQ-WSC-ORDER-013/014/FE-001：附件白名单+大小校验、上传成功 toast。
 * 附件规则与后端一致：Word/PDF，≤20MB，一份。
 */
import { computed, onMounted, ref } from 'vue'
import {
  getOrder,
  submitContract,
  type OrderDetail,
  type OrderTransactionInfo,
  type OrderLine,
} from '@/api/orders'
import { showSubmitContractToast } from '../utils/toast'

export type UploadState = 'idle' | 'loading' | 'uploading' | 'error'

/** 允许的文件扩展名（与后端白名单一致） */
export const ALLOWED_EXTENSIONS = ['.doc', '.docx', '.pdf'] as const

/** 最大文件大小 20MB（与后端一致） */
export const MAX_FILE_SIZE = 20 * 1024 * 1024

/** 校验文件是否符合白名单格式和大小 */
export function validateFile(file: File): string | null {
  const name = file.name.toLowerCase()
  const ext = name.slice(name.lastIndexOf('.'))
  if (!ALLOWED_EXTENSIONS.includes(ext as typeof ALLOWED_EXTENSIONS[number])) {
    return `不支持的文件格式（${ext}），仅允许 Word/PDF`
  }
  if (file.size > MAX_FILE_SIZE) {
    return `文件大小超过 20MB 限制（当前 ${(file.size / 1024 / 1024).toFixed(1)}MB）`
  }
  return null
}

/** 格式化文件大小 */
export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export function useUploadContract(orderId: string, onDone: () => void) {
  const order = ref<OrderDetail | null>(null)
  const state = ref<UploadState>('idle')
  const error = ref<string | null>(null)

  /** 已选择的文件 */
  const selectedFile = ref<File | null>(null)

  /** 上传进度 0-100 */
  const uploadProgress = ref(0)

  /** 后端扫描失败 inline 错误 */
  const scanError = ref<string | null>(null)

  /** 前端即时校验错误 */
  const fileValidationError = ref<string | null>(null)

  /** 替换文件二次确认对话框 */
  const replaceConfirmVisible = ref(false)

  /** 交易信息表单 */
  const orderLines = ref<OrderLine[]>([
    { unit: '', quantity: 1, unitPrice: '', subtotal: '' },
  ])
  const amountTaxInclusive = ref('')
  const amountTaxExclusive = ref('')

  /** 表单是否可提交 */
  const canSubmit = computed(() => {
    if (!selectedFile.value) return false
    if (fileValidationError.value) return false
    if (state.value === 'uploading') return false
    if (!amountTaxInclusive.value.trim()) return false
    return true
  })

  async function load() {
    if (!orderId) return
    state.value = 'loading'
    error.value = null
    try {
      const res = await getOrder(orderId)
      order.value = res.data
      // 预填已有交易信息
      if (res.data.transactionInfo) {
        amountTaxInclusive.value = res.data.transactionInfo.amountTaxInclusive
        amountTaxExclusive.value = res.data.transactionInfo.amountTaxExclusive
        if (res.data.transactionInfo.orderLines.length) {
          orderLines.value = res.data.transactionInfo.orderLines
        }
      }
      state.value = 'idle'
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '加载失败'
    }
  }

  /** 选择文件后即时校验 */
  function handleFileSelect(file: File): boolean {
    fileValidationError.value = null
    scanError.value = null
    const validationMsg = validateFile(file)
    if (validationMsg) {
      fileValidationError.value = validationMsg
      return false
    }
    selectedFile.value = file
    return true
  }

  /** 删除已选文件 */
  function handleRemoveFile() {
    selectedFile.value = null
    fileValidationError.value = null
    scanError.value = null
    uploadProgress.value = 0
  }

  /** 打开替换确认对话框 */
  function openReplaceConfirm() {
    replaceConfirmVisible.value = true
  }

  /** 关闭替换确认对话框 */
  function closeReplaceConfirm() {
    replaceConfirmVisible.value = false
  }

  /** 确认替换后清除文件，等待重新选择 */
  function confirmReplace() {
    handleRemoveFile()
    replaceConfirmVisible.value = false
  }

  /** 添加订单明细行 */
  function addOrderLine() {
    orderLines.value.push({ unit: '', quantity: 1, unitPrice: '', subtotal: '' })
  }

  /** 移除订单明细行 */
  function removeOrderLine(index: number) {
    if (orderLines.value.length > 1) {
      orderLines.value.splice(index, 1)
    }
  }

  /** 计算小计 */
  function calculateSubtotal(line: OrderLine): string {
    const qty = Number(line.quantity) || 0
    const price = Number(line.unitPrice) || 0
    return (qty * price).toFixed(2)
  }

  /** 提交合约附件与交易信息 */
  async function handleSubmit() {
    if (!canSubmit.value || !orderId || !selectedFile.value) return
    state.value = 'uploading'
    error.value = null
    scanError.value = null
    uploadProgress.value = 0

    try {
      // 模拟进度（实际由浏览器 XMLHttpRequest 或 fetch 进度决定）
      const progressTimer = setInterval(() => {
        if (uploadProgress.value < 90) {
          uploadProgress.value += 10
        }
      }, 200)

      const txInfo: OrderTransactionInfo = {
        orderLines: orderLines.value.map((l) => ({
          ...l,
          subtotal: calculateSubtotal(l),
        })),
        amountTaxInclusive: amountTaxInclusive.value,
        amountTaxExclusive: amountTaxExclusive.value || amountTaxInclusive.value,
        currency: 'CNY',
      }

      await submitContract(orderId, selectedFile.value, txInfo)

      clearInterval(progressTimer)
      uploadProgress.value = 100
      showSubmitContractToast()
      onDone()
    } catch (e) {
      uploadProgress.value = 0
      const msg = e instanceof Error ? e.message : '提交失败'
      // 区分扫描失败（后端返回特定错误）与一般错误
      if (msg.includes('扫描') || msg.includes('scan') || msg.includes('安全')) {
        scanError.value = msg
      } else {
        error.value = msg
      }
    } finally {
      state.value = 'idle'
    }
  }

  function goBack() {
    onDone()
  }

  onMounted(() => {
    void load()
  })

  return {
    order,
    state,
    error,
    selectedFile,
    uploadProgress,
    scanError,
    fileValidationError,
    replaceConfirmVisible,
    orderLines,
    amountTaxInclusive,
    amountTaxExclusive,
    canSubmit,
    load,
    handleFileSelect,
    handleRemoveFile,
    openReplaceConfirm,
    closeReplaceConfirm,
    confirmReplace,
    addOrderLine,
    removeOrderLine,
    calculateSubtotal,
    handleSubmit,
    goBack,
  }
}
