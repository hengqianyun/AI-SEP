<script setup lang="ts">
/**
 * 目录维护分类 Cascader（REQ-CAT-013）：筛选可停父节点；编辑/统一维护须到 L3。
 */
import { computed } from 'vue'
import { Cascader, ConfigProvider } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

export type MaintenanceCascaderOption = {
  value: string
  label: string
  children?: MaintenanceCascaderOption[]
}

const props = withDefaults(
  defineProps<{
    options: MaintenanceCascaderOption[]
    path: string[]
    placeholder?: string
    changeOnSelect?: boolean
    testid?: string
    disabled?: boolean
  }>(),
  {
    placeholder: '请选择分类',
    changeOnSelect: false,
    testid: 'maintenance-cascader',
    disabled: false,
  },
)

const emit = defineEmits<{
  change: [path: string[]]
}>()

const MAINT_CASCADER_THEME: ThemeConfig = {
  token: {
    colorPrimary: '#3b82f6',
    colorBorder: '#e5e7eb',
    colorBgContainer: '#ffffff',
    borderRadius: 6,
    fontSize: 14,
  },
}

const cascaderValue = computed({
  get: () => props.path,
  set: (path: string[]) => emit('change', path),
})

function onChange(value: (string | number)[] | (string | number)[][] | undefined) {
  const raw = value ?? []
  const path = (Array.isArray(raw[0]) ? raw[0] : raw).map(String)
  emit('change', path)
}
</script>

<template>
  <ConfigProvider :theme="MAINT_CASCADER_THEME">
    <Cascader
      v-model:value="cascaderValue"
      class="maint-cascader"
      :options="options"
      :placeholder="placeholder"
      :change-on-select="changeOnSelect"
      :disabled="disabled"
      allow-clear
      :aria-label="placeholder"
      :data-testid="testid"
      data-maint-cascader-theme="ant-token-mapped"
      @change="onChange"
    />
  </ConfigProvider>
</template>

<style scoped>
.maint-cascader {
  min-width: 220px;
}
</style>
