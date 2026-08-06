<script setup lang="ts">
/**
 * 角色只读展示（V1.4 / REQ-SHELL-001 supersede 可切换下拉）。
 * 无 listbox、无 chevron、无选项菜单；禁止挂载可切换控件。
 */
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'

const auth = useAuthStore()
const { role } = storeToRefs(auth)

const currentLabel = computed(() => {
  switch (role.value) {
    case 'ADMIN':
      return '管理员'
    case 'PROVIDER':
      return '数据提供方'
    case 'USER':
      return '普通用户'
    default:
      return '—'
  }
})
</script>

<template>
  <p class="role-readonly" data-testid="role-switcher" data-role-readonly="true">
    角色：{{ currentLabel }}
  </p>
</template>

<style scoped>
.role-readonly {
  margin: 0;
  font-size: 12px;
  color: var(--sidebar-text-muted);
}
</style>
