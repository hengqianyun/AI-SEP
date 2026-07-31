import type { RouteRecordRaw } from 'vue-router'
import PlaceholderView from '@/views/PlaceholderView.vue'

/**
 * 根路由清单（TASK-WSC-101 增量：契约 2.0.0 / overview 可编译启动）。
 * 目录维护业务页路由由后续任务追加；本任务不实现 catalog/shell/auth/chain 业务页。
 */
export const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/overview' },
  {
    path: '/overview',
    name: 'overview',
    component: () => import('@/features/overview/OverviewPage.vue'),
    meta: { title: '总览' },
  },
  {
    path: '/catalog',
    name: 'catalog',
    component: () => import('@/features/catalog/browse/CatalogBrowsePage.vue'),
    meta: { title: '数据目录' },
  },
  {
    path: '/catalog/products/:productId',
    name: 'catalog-detail',
    component: () => import('@/features/catalog/detail/ProductDetailPage.vue'),
    meta: { title: '产品详情' },
  },
  {
    path: '/catalog/products/:productId/edit',
    name: 'catalog-edit',
    component: () => import('@/features/catalog/editor/ProductEditorPage.vue'),
    meta: { title: '编辑产品' },
  },
  {
    path: '/catalog/products/new',
    name: 'catalog-create',
    component: () => import('@/features/catalog/editor/ProductEditorPage.vue'),
    meta: { title: '新增产品' },
  },
  {
    path: '/catalog/admin/categories',
    name: 'catalog-admin-categories',
    component: () => import('@/features/catalog/admin/CategoryAdminPage.vue'),
    meta: { title: '维护行业分类' },
  },
  {
    path: '/catalog/maintenance',
    name: 'catalog-maintenance',
    component: () => import('@/features/catalog/maintenance/CatalogMaintenancePage.vue'),
    meta: { title: '目录维护' },
  },
  {
    path: '/chain/products/:productId',
    name: 'chain',
    component: () => import('@/features/chain/ChainPage.vue'),
    meta: { title: '上链信息' },
  },
  {
    path: '/unavailable/:feature?',
    name: 'unavailable',
    component: PlaceholderView,
    meta: {
      title: '功能未开放',
      hint: '数据登记 / 交易订单 / 连接器管理等 Out of Scope 菜单占位。',
      unavailable: true,
    },
  },
]
