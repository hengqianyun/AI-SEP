<script setup lang="ts">
import { computed, ref } from 'vue'
import { truncateSensitiveId } from '../utils/truncateSensitive'

const props = withDefaults(
  defineProps<{
    value: string
    label?: string
    head?: number
    tail?: number
  }>(),
  { head: 10, tail: 6 },
)

const copied = ref(false)
let timer: ReturnType<typeof setTimeout> | undefined

const display = computed(() => truncateSensitiveId(props.value, props.head, props.tail))

async function copyFull() {
  try {
    await navigator.clipboard.writeText(props.value)
    copied.value = true
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      copied.value = false
    }, 1500)
  } catch {
    copied.value = false
  }
}
</script>

<template>
  <div class="sensitive-id">
    <span v-if="label" class="label">{{ label }}</span>
    <code class="value" :title="value" data-testid="sensitive-display">{{ display }}</code>
    <button type="button" class="copy-btn" data-testid="sensitive-copy" @click="copyFull">
      {{ copied ? '已复制' : '复制' }}
    </button>
  </div>
</template>

<style scoped>
.sensitive-id {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.label {
  color: var(--text-secondary);
  min-width: 7em;
}
.value {
  font-family: ui-monospace, Consolas, monospace;
  background: #f9fafb;
  border: 1px solid var(--border-color);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  word-break: break-all;
}
.copy-btn {
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  border-radius: var(--radius-sm);
  padding: 2px 10px;
  cursor: pointer;
  font-size: 12px;
  color: var(--text-primary);
}
.copy-btn:hover {
  background: #f9fafb;
}
</style>
