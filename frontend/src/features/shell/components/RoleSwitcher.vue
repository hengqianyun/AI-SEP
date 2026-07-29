<script setup lang="ts">
import type { Role } from '@/api/auth'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'

const auth = useAuthStore()
const { role, loading } = storeToRefs(auth)

const options: { value: Role; label: string }[] = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'PROVIDER', label: '提供方' },
  { value: 'USER', label: '普通用户' },
]

async function onChange(event: Event) {
  const next = (event.target as HTMLSelectElement).value as Role
  if (!next || next === role.value) return
  try {
    await auth.switchRole(next)
  } catch {
    // error 已写入 store
  }
}
</script>

<template>
  <label class="role-switch">
    <span>当前角色</span>
    <select :value="role ?? ''" :disabled="loading || !role" @change="onChange">
      <option v-for="opt in options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
  </label>
</template>

<style scoped>
.role-switch {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #555;
}
select {
  padding: 4px 6px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  font: inherit;
  background: #fff;
}
</style>
