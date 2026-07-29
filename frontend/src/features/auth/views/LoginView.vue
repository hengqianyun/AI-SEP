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
    <form class="login-card" @submit.prevent="onSubmit">
      <h1>接入端工作台</h1>
      <p class="hint">演示账号：admin / provider / user，密码均为 demo</p>
      <label>
        用户名
        <input v-model="username" name="username" autocomplete="username" required />
      </label>
      <label>
        密码
        <input
          v-model="password"
          type="password"
          name="password"
          autocomplete="current-password"
          required
        />
      </label>
      <p v-if="localError" class="error" role="alert">{{ localError }}</p>
      <button type="submit" :disabled="submitting">
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
  background: linear-gradient(160deg, #f0f4f8 0%, #e8eef5 50%, #f7f7f5 100%);
}
.login-card {
  width: min(360px, 92vw);
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 28px 24px;
  background: #fff;
  border: 1px solid #e2e6ec;
  border-radius: 8px;
}
h1 {
  margin: 0;
  font-size: 22px;
}
.hint {
  margin: 0;
  color: #666;
  font-size: 13px;
}
label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
}
input {
  padding: 8px 10px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  font: inherit;
}
button {
  margin-top: 4px;
  padding: 10px 12px;
  border: 0;
  border-radius: 4px;
  background: #1f4b7a;
  color: #fff;
  font: inherit;
  cursor: pointer;
}
button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.error {
  margin: 0;
  color: #b42318;
  font-size: 13px;
}
</style>
