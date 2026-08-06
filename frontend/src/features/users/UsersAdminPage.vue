<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import type { Role } from '@/api/auth'
import {
  createUser,
  listUsers,
  updateUser,
  type AdminUser,
} from '@/api/admin'
import { useAuthStore } from '@/features/auth/store/authStore'

const auth = useAuthStore()
const { role } = storeToRefs(auth)
/** 602：用户管理仅 ADMIN（内联门禁，不经 603 矩阵助手） */
const allowed = computed(() => role.value === 'ADMIN')

const items = ref<AdminUser[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const createOpen = ref(false)
const creating = ref(false)
const form = ref({
  username: '',
  password: '',
  displayName: '',
  role: 'PROVIDER' as Role,
  enterpriseName: '演示企业',
})

const editOpen = ref(false)
const editing = ref(false)
const editTarget = ref<AdminUser | null>(null)
const editRole = ref<Role>('PROVIDER')

function resetCreateForm() {
  form.value = {
    username: '',
    password: '',
    displayName: '',
    role: 'PROVIDER',
    enterpriseName: '演示企业',
  }
}

function openCreate() {
  if (!allowed.value) return
  error.value = null
  resetCreateForm()
  createOpen.value = true
}

function closeCreate() {
  createOpen.value = false
}

function openEditRole(u: AdminUser) {
  if (!allowed.value) return
  error.value = null
  editTarget.value = u
  editRole.value = u.role
  editOpen.value = true
}

function closeEdit() {
  editOpen.value = false
  editTarget.value = null
}

async function load() {
  if (!allowed.value) {
    return
  }
  loading.value = true
  error.value = null
  try {
    const res = await listUsers()
    items.value = res.data?.items ?? []
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  if (!allowed.value) return
  creating.value = true
  error.value = null
  try {
    await createUser({
      username: form.value.username.trim(),
      password: form.value.password,
      displayName: form.value.displayName.trim() || undefined,
      role: form.value.role,
      enterpriseName: form.value.enterpriseName.trim() || undefined,
    })
    createOpen.value = false
    resetCreateForm()
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    creating.value = false
  }
}

async function softDelete(userId: string) {
  if (!allowed.value) return
  error.value = null
  try {
    await updateUser(userId, { deleted: true })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
  }
}

async function saveEditRole() {
  if (!allowed.value || !editTarget.value) return
  editing.value = true
  error.value = null
  try {
    await updateUser(editTarget.value.userId, { role: editRole.value })
    closeEdit()
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '改角色失败'
  } finally {
    editing.value = false
  }
}

function roleLabel(r: Role) {
  if (r === 'ADMIN') return '管理员'
  if (r === 'PROVIDER') return '数据提供方'
  return '普通用户'
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="users-page" data-testid="users-admin-page">
    <template v-if="!allowed">
      <header class="page-header wsc-surface">
        <h1>用户管理</h1>
        <p class="subtitle" data-testid="users-forbidden">当前角色无权访问用户管理</p>
      </header>
    </template>

    <template v-else>
      <header class="page-header wsc-surface">
        <div class="header-main">
          <h1>用户管理</h1>
        </div>
        <div class="header-actions">
          <button
            type="button"
            class="btn primary"
            data-testid="user-open-create"
            @click="openCreate"
          >
            创建账号
          </button>
        </div>
      </header>

      <p v-if="error" class="error banner" role="alert" data-testid="users-error">
        {{ error }}
      </p>

      <section class="wsc-surface table-card" data-testid="users-list-card">
        <h2>账号列表</h2>
        <p v-if="loading" class="state" data-testid="users-loading">加载中…</p>
        <p
          v-else-if="items.length === 0"
          class="state empty"
          data-testid="users-empty"
        >
          暂无用户
        </p>
        <table v-else data-testid="users-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>显示名</th>
              <th>角色</th>
              <th>企业</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in items" :key="u.userId">
              <td>{{ u.username }}</td>
              <td>{{ u.displayName }}</td>
              <td data-testid="user-row-role-label">{{ roleLabel(u.role) }}</td>
              <td>{{ u.enterpriseName }}</td>
              <td class="actions">
                <button
                  type="button"
                  class="link edit"
                  data-testid="user-edit-role"
                  @click="openEditRole(u)"
                >
                  编辑角色
                </button>
                <button
                  type="button"
                  class="link danger"
                  data-testid="user-soft-delete"
                  @click="softDelete(u.userId)"
                >
                  停用
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <div
        v-if="createOpen"
        class="modal-overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-create-title"
        data-testid="user-create-dialog"
      >
        <div class="modal-card wsc-surface">
          <header class="modal-header">
            <h2 id="user-create-title">创建账号</h2>
            <button type="button" class="modal-close" aria-label="关闭" @click="closeCreate">
              ×
            </button>
          </header>
          <div class="form-grid">
            <label>
              用户名
              <input v-model="form.username" data-testid="user-username" autocomplete="off" />
            </label>
            <label>
              密码
              <input
                v-model="form.password"
                type="password"
                data-testid="user-password"
                autocomplete="new-password"
              />
            </label>
            <label>
              显示名
              <input v-model="form.displayName" data-testid="user-display-name" />
            </label>
            <label>
              角色
              <select v-model="form.role" data-testid="user-role">
                <option value="ADMIN">管理员</option>
                <option value="PROVIDER">数据提供方</option>
                <option value="USER">普通用户</option>
              </select>
            </label>
            <label>
              企业名
              <input v-model="form.enterpriseName" />
            </label>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn" @click="closeCreate">取消</button>
            <button
              type="button"
              class="btn primary"
              :disabled="creating"
              data-testid="user-create"
              @click="onCreate"
            >
              创建
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="editOpen && editTarget"
        class="modal-overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-edit-title"
        data-testid="user-edit-role-dialog"
      >
        <div class="modal-card wsc-surface">
          <header class="modal-header">
            <h2 id="user-edit-title">编辑角色</h2>
            <button type="button" class="modal-close" aria-label="关闭" @click="closeEdit">
              ×
            </button>
          </header>
          <p class="modal-meta">
            用户 <strong>{{ editTarget.username }}</strong>
          </p>
          <label class="edit-role-field">
            角色
            <select v-model="editRole" data-testid="user-row-role">
              <option value="ADMIN">管理员</option>
              <option value="PROVIDER">数据提供方</option>
              <option value="USER">普通用户</option>
            </select>
          </label>
          <div class="modal-actions">
            <button type="button" class="btn" @click="closeEdit">取消</button>
            <button
              type="button"
              class="btn primary"
              :disabled="editing"
              data-testid="user-edit-role-save"
              @click="saveEditRole"
            >
              保存
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.users-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: var(--text-primary);
  font-family: var(--font-family-sans);
  font-size: var(--font-size-base);
}

.wsc-surface {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
}

.page-header {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.subtitle {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.table-card {
  padding: 16px 20px;
}

.table-card h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin: 4px 0 0;
}

label,
.edit-role-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

input,
select {
  padding: 8px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  color: var(--text-primary);
  font: inherit;
}

.btn {
  height: 34px;
  padding: 0 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-primary);
}

.btn.primary {
  border-color: var(--blue);
  background: var(--blue);
  color: #fff;
}

.btn.primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.state {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.state.empty {
  color: var(--text-tertiary);
}

.banner.error,
.error {
  color: var(--red);
}

.banner {
  margin: 0;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  background: var(--red-light);
  font-size: 13px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 8px;
}

th,
td {
  text-align: left;
  padding: 10px 8px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-primary);
}

th {
  color: var(--text-secondary);
  font-weight: 500;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.link {
  background: none;
  border: none;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  padding: 0;
}

.link.edit {
  color: var(--blue);
}

.link.danger {
  color: var(--red);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.45);
  box-sizing: border-box;
}

.modal-card {
  width: min(520px, 100%);
  padding: 16px 20px 20px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.modal-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.modal-close {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 0 4px;
}

.modal-meta {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
