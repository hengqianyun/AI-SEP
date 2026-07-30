import { ref } from 'vue'
import { getProduct, type Product } from '@/api/catalog'

export type LoadState = 'idle' | 'loading' | 'ready' | 'empty' | 'error'

export function useProductDetail() {
  const product = ref<Product | null>(null)
  const state = ref<LoadState>('idle')
  const error = ref('')

  async function load(productId: string) {
    state.value = 'loading'
    error.value = ''
    product.value = null
    try {
      const res = await getProduct(productId)
      product.value = res.data
      state.value = product.value ? 'ready' : 'empty'
    } catch (e) {
      state.value = 'error'
      error.value = e instanceof Error ? e.message : '加载产品详情失败'
    }
  }

  return { product, state, error, load }
}
