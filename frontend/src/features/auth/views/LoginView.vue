<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/authStore'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const username = ref('admin')
const password = ref('demo')
const submitting = ref(false)
const localError = ref<string | null>(null)

async function onSubmit() {
  submitting.value = true
  localError.value = null
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/overview'
    await router.replace(redirect)
  } catch (e) {
    localError.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <form class="login-card" data-testid="login-form" @submit.prevent="onSubmit">
      <div class="brand">
        <div class="brand-logo" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <div>
          <p class="brand-eyebrow">可信数据空间</p>
          <h1>接入端工作台</h1>
        </div>
      </div>
      <label>
        用户名
        <input
          v-model="username"
          name="username"
          autocomplete="username"
          data-testid="login-username"
          required
        />
      </label>
      <label>
        密码
        <input
          v-model="password"
          type="password"
          name="password"
          autocomplete="current-password"
          data-testid="login-password"
          required
        />
      </label>
      <p v-if="localError" class="error" role="alert" data-testid="login-error">{{ localError }}</p>
      <button type="submit" class="submit" data-testid="login-submit" :disabled="submitting">
        {{ submitting ? '登录中…' : '登录' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(ellipse 80% 60% at 20% 0%, rgba(56, 189, 248, 0.12), transparent 55%),
    radial-gradient(ellipse 70% 50% at 90% 100%, rgba(59, 130, 246, 0.1), transparent 50%),
    var(--main-bg);
}

.login-card {
  width: min(380px, 92vw);
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 28px 26px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-lg);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: var(--sidebar-bg);
  color: var(--sidebar-accent);
  flex-shrink: 0;
}

.brand-logo svg {
  width: 22px;
  height: 22px;
}

.brand-eyebrow {
  margin: 0 0 2px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.12em;
  color: var(--text-tertiary);
}

h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

input {
  padding: 9px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  color: var(--text-primary);
  font: inherit;
  transition: border-color 0.15s, box-shadow 0.15s;
}

input:focus {
  outline: none;
  border-color: var(--blue);
  box-shadow: 0 0 0 3px var(--blue-light);
}

.submit {
  margin-top: 4px;
  padding: 10px 14px;
  border: 1px solid var(--blue);
  border-radius: var(--radius-menu);
  background: var(--blue);
  color: #fff;
  font: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, opacity 0.15s;
}

.submit:hover:not(:disabled) {
  background: #2563eb;
}

.submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.error {
  margin: 0;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: var(--red-light);
  color: var(--red);
  font-size: 13px;
}
</style>
