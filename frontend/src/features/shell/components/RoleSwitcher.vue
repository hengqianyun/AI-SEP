<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
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

const open = ref(false)
const rootEl = ref<HTMLElement | null>(null)

const currentLabel = computed(
  () => options.find((o) => o.value === role.value)?.label ?? '选择角色',
)

async function selectRole(next: Role) {
  open.value = false
  if (!next || next === role.value || loading.value) return
  try {
    await auth.switchRole(next)
  } catch {
    // error 已写入 store
  }
}

function toggle() {
  if (loading.value || !role.value) return
  open.value = !open.value
}

function onDocClick(event: MouseEvent) {
  if (!open.value || !rootEl.value) return
  if (!rootEl.value.contains(event.target as Node)) {
    open.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})
</script>

<template>
  <div ref="rootEl" class="role-dropdown" :class="{ open }" data-testid="role-switcher">
    <button
      type="button"
      class="role-dropdown-trigger"
      :disabled="loading || !role"
      aria-haspopup="listbox"
      :aria-expanded="open"
      @click.stop="toggle"
    >
      <span class="role-dropdown-text">{{ currentLabel }}</span>
      <svg
        class="role-dropdown-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>
    <div v-show="open" class="role-dropdown-menu" role="listbox">
      <button
        v-for="opt in options"
        :key="opt.value"
        type="button"
        class="role-dropdown-item"
        :class="{ active: opt.value === role }"
        role="option"
        :aria-selected="opt.value === role"
        @click="selectRole(opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.role-dropdown {
  position: relative;
}

.role-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  color: var(--sidebar-text-muted);
  transition: color 0.2s;
  text-align: left;
}

.role-dropdown-trigger:hover:not(:disabled) {
  color: rgba(255, 255, 255, 0.7);
}

.role-dropdown-trigger:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.role-dropdown-icon {
  width: 12px;
  height: 12px;
  transition: transform 0.2s;
  flex-shrink: 0;
}

.role-dropdown.open .role-dropdown-icon {
  transform: rotate(180deg);
}

.role-dropdown-menu {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: var(--role-menu-bg);
  border-radius: var(--radius-sm);
  padding: 4px;
  margin-bottom: 8px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.3);
  z-index: 100;
}

.role-dropdown-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: 0;
  background: transparent;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.role-dropdown-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.role-dropdown-item.active {
  background: var(--role-active-bg);
  color: var(--blue);
}
</style>
